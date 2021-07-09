package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.options.Options;
import soot.tagkit.AbstractHost;
import soot.tagkit.CodeAttribute;
import soot.tagkit.Tag;
import soot.util.Chain;
import soot.util.EscapedWriter;
import soot.util.HashChain;
import soot.validation.BodyValidator;
import soot.validation.CheckEscapingValidator;
import soot.validation.CheckInitValidator;
import soot.validation.CheckTypesValidator;
import soot.validation.CheckVoidLocalesValidator;
import soot.validation.LocalsValidator;
import soot.validation.TrapsValidator;
import soot.validation.UnitBoxesValidator;
import soot.validation.UsesValidator;
import soot.validation.ValidationException;
import soot.validation.ValueBoxesValidator;

/**
 * Abstract base class that models the body (code attribute) of a Java method. Classes that implement an Intermediate
 * Representation for a method body should subclass it. In particular the classes GrimpBody, JimpleBody and BafBody all
 * extend this class. This class provides methods that are common to any IR, such as methods to get the body's units
 * (statements), traps, and locals.
 *
 * @see soot.grimp.GrimpBody
 * @see soot.jimple.JimpleBody
 * @see soot.baf.BafBody
 */
@SuppressWarnings("serial")
public abstract class Body extends AbstractHost implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(Body.class);

  /**
   * The method associated with this Body.
   */
  protected transient SootMethod method = null;

  /**
   * The chain of locals for this Body.
   */
  protected Chain<Local> localChain = new HashChain<>();

  /**
   * The chain of traps for this Body.
   */
  protected Chain<Trap> trapChain = new HashChain<>();

  /**
   * The chain of units for this Body.
   */
  protected UnitPatchingChain unitChain = new UnitPatchingChain(new HashChain<>());

  /**
   * Lazy initialized array containing some validators in order to validate the Body.
   */
  private static class LazyValidatorsSingleton {
    static final BodyValidator[] V = new BodyValidator[] { LocalsValidator.v(), TrapsValidator.v(), UnitBoxesValidator.v(),
        UsesValidator.v(), ValueBoxesValidator.v(), /* CheckInitValidator.v(), */ CheckTypesValidator.v(),
        CheckVoidLocalesValidator.v(), CheckEscapingValidator.v() };

    private LazyValidatorsSingleton() {
    }
  }

  /**
   * Creates a deep copy of this Body.
   *
   * @return
   */
  @Override
  abstract public Object clone();

  /**
   * Creates a Body associated to the given method. Used by subclasses during initialization. Creation of a Body is triggered
   * by e.g. Jimple.v().newBody(options).
   *
   * @param m
   */
  protected Body(SootMethod m) {
    this.method = m;
  }

  /**
   * Creates an extremely empty Body. The Body is not associated to any method.
   */
  protected Body() {
  }

  /**
   * Returns the method associated with this Body.
   *
   * @return the method that owns this body.
   */
  public SootMethod getMethod() {
    if (method == null) {
      throw new RuntimeException("no method associated w/ body");
    }
    return method;
  }

  /**
   * Returns the method associated with this Body.
   *
   * @return the method that owns this body.
   */
  public SootMethod getMethodUnsafe() {
    return method;
  }

  /**
   * Sets the method associated with this Body.
   *
   * @param method
   *          the method that owns this body.
   */
  public void setMethod(SootMethod method) {
    this.method = method;
  }

  /**
   * Returns the number of locals declared in this body.
   *
   * @return
   */
  public int getLocalCount() {
    return localChain.size();
  }

  /**
   * Copies the contents of the given Body into this one.
   *
   * @param b
   *
   * @return
   */
  public Map<Object, Object> importBodyContentsFrom(Body b) {
    HashMap<Object, Object> bindings = new HashMap<>();

    // Clone units in body's statement list
    for (Unit original : b.getUnits()) {
      Unit copy = (Unit) original.clone();

      copy.addAllTagsOf(original);

      // Add cloned unit to our unitChain.
      unitChain.addLast(copy);

      // Build old <-> new map to be able to patch up references to other units
      // within the cloned units. (these are still refering to the original
      // unit objects).
      bindings.put(original, copy);
    }

    // Clone trap units.
    for (Trap original : b.getTraps()) {
      Trap copy = (Trap) original.clone();

      // Add cloned unit to our trap list.
      trapChain.addLast(copy);

      // Store old <-> new mapping.
      bindings.put(original, copy);
    }

    // Clone local units.
    for (Local original : b.getLocals()) {
      Local copy = (Local) original.clone();

      // Add cloned unit to our trap list.
      localChain.addLast(copy);

      // Build old <-> new mapping.
      bindings.put(original, copy);
    }

    // Patch up references within units using our (old <-> new) map.
    for (UnitBox box : getAllUnitBoxes()) {
      Unit newObject = (Unit) bindings.get(box.getUnit());
      // if we have a reference to an old object, replace it with its clone.
      if (newObject != null) {
        box.setUnit(newObject);
      }
    }

    {
      // backpatching all local variables.
      for (ValueBox vb : getUseBoxes()) {
        Value val = vb.getValue();
        if (val instanceof Local) {
          vb.setValue((Value) bindings.get(val));
        }
      }
      for (ValueBox vb : getDefBoxes()) {
        Value val = vb.getValue();
        if (val instanceof Local) {
          vb.setValue((Value) bindings.get(val));
        }
      }
    }
    return bindings;
  }

  protected void runValidation(BodyValidator validator) {
    final List<ValidationException> exceptionList = new ArrayList<>();
    validator.validate(this, exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /**
   * Verifies a few sanity conditions on the contents on this body.
   */
  public void validate() {
    List<ValidationException> exceptionList = new ArrayList<>();
    validate(exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /**
   * Validates the jimple body and saves a list of all validation errors
   *
   * @param exceptionList
   *          the list of validation errors
   */
  public void validate(List<ValidationException> exceptionList) {
    final boolean runAllValidators = Options.v().debug() || Options.v().validate();
    for (BodyValidator validator : LazyValidatorsSingleton.V) {
      if (!validator.isBasicValidator() && !runAllValidators) {
        continue;
      }
      validator.validate(this, exceptionList);
    }
  }

  /**
   * Verifies that a ValueBox is not used in more than one place.
   */
  public void validateValueBoxes() {
    runValidation(ValueBoxesValidator.v());
  }

  /**
   * Verifies that each Local of {@link #getUseAndDefBoxes()} is in this body's locals Chain.
   */
  public void validateLocals() {
    runValidation(LocalsValidator.v());
  }

  /**
   * Verifies that the begin, end and handler units of each trap are in this body.
   */
  public void validateTraps() {
    runValidation(TrapsValidator.v());
  }

  /**
   * Verifies that the UnitBoxes of this Body all point to a Unit contained within this body.
   */
  public void validateUnitBoxes() {
    runValidation(UnitBoxesValidator.v());
  }

  /**
   * Verifies that each use in this Body has a def.
   */
  public void validateUses() {
    runValidation(UsesValidator.v());
  }

  public void checkInit() {
    runValidation(CheckInitValidator.v());
  }

  /**
   * Returns a backed chain of the locals declared in this Body.
   *
   * @return
   */
  public Chain<Local> getLocals() {
    return localChain;
  }

  /**
   * Returns a backed view of the traps found in this Body.
   *
   * @return
   */
  public Chain<Trap> getTraps() {
    return trapChain;
  }

  /**
   * Return unit containing the \@this-assignment
   *
   * @return
   */
  public Unit getThisUnit() {
    for (Unit u : getUnits()) {
      if (u instanceof IdentityUnit && ((IdentityUnit) u).getRightOp() instanceof ThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethod());
  }

  /**
   * Return LHS of the first identity stmt assigning from \@this.
   *
   * @return
   */
  public Local getThisLocal() {
    return (Local) (((IdentityUnit) getThisUnit()).getLeftOp());
  }

  /**
   * Return LHS of the first identity stmt assigning from \@parameter i.
   *
   * @param i
   *
   * @return
   */
  public Local getParameterLocal(int i) {
    for (Unit s : getUnits()) {
      if (s instanceof IdentityUnit) {
        IdentityUnit is = (IdentityUnit) s;
        Value rightOp = is.getRightOp();
        if (rightOp instanceof ParameterRef) {
          ParameterRef pr = (ParameterRef) rightOp;
          if (pr.getIndex() == i) {
            return (Local) is.getLeftOp();
          }
        }
      }
    }
    throw new RuntimeException("couldn't find parameterref" + i + " in " + getMethod());
  }

  /**
   * Get all the LHS of the identity statements assigning from parameter references.
   *
   * @return a list of size as per <code>getMethod().getParameterCount()</code> with all elements ordered as per the
   *         parameter index.
   *
   * @throws RuntimeException
   *           if a parameterref is missing
   */
  public List<Local> getParameterLocals() {
    final int numParams = getMethod().getParameterCount();
    Local[] res = new Local[numParams];
    int numFound = 0;
    for (Unit u : getUnits()) {
      if (u instanceof IdentityUnit) {
        IdentityUnit is = (IdentityUnit) u;
        Value rightOp = is.getRightOp();
        if (rightOp instanceof ParameterRef) {
          int idx = ((ParameterRef) rightOp).getIndex();
          if (res[idx] != null) {
            throw new RuntimeException("duplicate parameterref" + idx + " in " + getMethod());
          }
          res[idx] = (Local) is.getLeftOp();
          numFound++;
          if (numFound >= numParams) {
            break;
          }
        }
      }
    }
    if (numFound != numParams) {
      for (int i = 0; i < numParams; i++) {
        if (res[i] == null) {
          throw new RuntimeException("couldn't find parameterref" + i + " in " + getMethod());
        }
      }
      throw new RuntimeException("couldn't find parameterref? in " + getMethod());
    }
    return Arrays.asList(res);
  }

  /**
   * Returns the list of parameter references used in this body. The list is as long as the number of parameters declared in
   * the associated method's signature. The list may have <code>null</code> entries for parameters not referenced in the
   * body. The returned list is of fixed size.
   *
   * @return
   */
  public List<Value> getParameterRefs() {
    final int numParams = getMethod().getParameterCount();
    Value[] res = new Value[numParams];
    int numFound = 0;
    for (Unit u : getUnits()) {
      if (u instanceof IdentityUnit) {
        Value rightOp = ((IdentityUnit) u).getRightOp();
        if (rightOp instanceof ParameterRef) {
          ParameterRef pr = (ParameterRef) rightOp;
          int idx = pr.getIndex();
          if (res[idx] != null) {
            throw new RuntimeException("duplicate parameterref" + idx + " in " + getMethod());
          }
          res[idx] = pr;
          numFound++;
          if (numFound >= numParams) {
            break;
          }
        }
      }
    }
    return Arrays.asList(res);
  }

  /**
   * Returns the Chain of Units that make up this body. The units are returned as a PatchingChain. The client can then
   * manipulate the chain, adding and removing units, and the changes will be reflected in the body. Since a PatchingChain is
   * returned the client need <i>not</i> worry about removing exception boundary units or otherwise corrupting the chain.
   *
   * @return the units in this Body
   *
   * @see PatchingChain
   * @see Unit
   */
  public UnitPatchingChain getUnits() {
    return unitChain;
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for their UnitBoxes. All UnitBoxes thus
   * found are returned. Branching Units and statements which use PhiExpr will have UnitBoxes; a UnitBox contains a Unit that
   * is either a target of a branch or is being used as a pointer to the end of a CFG block.
   *
   * <p>
   * This method is typically used for pointer patching, eg when the unit chain is cloned.
   *
   * @return A list of all the UnitBoxes held by this body's units.
   *
   * @see UnitBox
   * @see #getUnitBoxes(boolean)
   * @see Unit#getUnitBoxes()
   * @see soot.shimple.PhiExpr#getUnitBoxes()
   */
  public List<UnitBox> getAllUnitBoxes() {
    ArrayList<UnitBox> unitBoxList = new ArrayList<>();
    for (Unit item : unitChain) {
      unitBoxList.addAll(item.getUnitBoxes());
    }
    for (Trap item : trapChain) {
      unitBoxList.addAll(item.getUnitBoxes());
    }
    for (Tag t : getTags()) {
      if (t instanceof CodeAttribute) {
        unitBoxList.addAll(((CodeAttribute) t).getUnitBoxes());
      }
    }

    return unitBoxList;
  }

  /**
   * If branchTarget is true, returns the result of iterating through all branching Units in this body and querying them for
   * their UnitBoxes. These UnitBoxes contain Units that are the target of a branch. This is useful for, say, labeling blocks
   * or updating the targets of branching statements.
   *
   * <p>
   * If branchTarget is false, returns the result of iterating through the non-branching Units in this body and querying them
   * for their UnitBoxes. Any such UnitBoxes (typically from PhiExpr) contain a Unit that indicates the end of a CFG block.
   *
   * @param branchTarget
   *
   * @return a list of all the UnitBoxes held by this body's branching units.
   *
   * @see UnitBox
   * @see #getAllUnitBoxes()
   * @see Unit#getUnitBoxes()
   * @see soot.shimple.PhiExpr#getUnitBoxes()
   */
  public List<UnitBox> getUnitBoxes(boolean branchTarget) {
    ArrayList<UnitBox> unitBoxList = new ArrayList<>();
    for (Unit item : unitChain) {
      if (item.branches() == branchTarget) {
        unitBoxList.addAll(item.getUnitBoxes());
      }
    }
    for (Trap item : trapChain) {
      unitBoxList.addAll(item.getUnitBoxes());
    }
    for (Tag t : getTags()) {
      if (t instanceof CodeAttribute) {
        unitBoxList.addAll(((CodeAttribute) t).getUnitBoxes());
      }
    }

    return unitBoxList;
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for ValueBoxes used. All of the
   * ValueBoxes found are then returned as a List.
   *
   * @return a list of all the ValueBoxes for the Values used this body's units.
   *
   * @see Value
   * @see Unit#getUseBoxes
   * @see ValueBox
   * @see Value
   */
  public List<ValueBox> getUseBoxes() {
    ArrayList<ValueBox> useBoxList = new ArrayList<>();
    for (Unit item : unitChain) {
      useBoxList.addAll(item.getUseBoxes());
    }
    return useBoxList;
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for ValueBoxes defined. All of the
   * ValueBoxes found are then returned as a List.
   *
   * @return a list of all the ValueBoxes for Values defined by this body's units.
   *
   * @see Value
   * @see Unit#getDefBoxes
   * @see ValueBox
   * @see Value
   */
  public List<ValueBox> getDefBoxes() {
    ArrayList<ValueBox> defBoxList = new ArrayList<>();
    for (Unit item : unitChain) {
      defBoxList.addAll(item.getDefBoxes());
    }
    return defBoxList;
  }

  /**
   * Returns a list of boxes corresponding to Values either used or defined in any unit of this Body.
   *
   * @return a list of ValueBoxes for held by the body's Units.
   *
   * @see Value
   * @see Unit#getUseAndDefBoxes
   * @see ValueBox
   * @see Value
   */
  public List<ValueBox> getUseAndDefBoxes() {
    ArrayList<ValueBox> useAndDefBoxList = new ArrayList<>();
    for (Unit item : unitChain) {
      useAndDefBoxList.addAll(item.getUseBoxes());
      useAndDefBoxList.addAll(item.getDefBoxes());
    }
    return useAndDefBoxList;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(new OutputStreamWriter(streamOut)))) {
      Printer.v().printTo(this, writerOut);
      writerOut.flush();
    } catch (RuntimeException e) {
      logger.error(e.getMessage(), e);
    }
    return streamOut.toString();
  }

  public long getModificationCount() {
    return localChain.getModificationCount() + unitChain.getModificationCount() + trapChain.getModificationCount();
  }
}
