package soot.jimple.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import java.util.List;

import soot.Body;
import soot.ResolutionFailedException;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.baf.BafBody;
import soot.baf.FieldArgInst;
import soot.baf.FieldGetInst;
import soot.baf.FieldPutInst;
import soot.baf.Inst;
import soot.baf.StaticGetInst;
import soot.baf.StaticPutInst;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.validation.BodyValidator;
import soot.validation.UnitValidationException;
import soot.validation.ValidationException;

/**
 * @author Alexandre Bartel
 * @author Timothy Hoffman
 */
public enum FieldRefValidator implements BodyValidator {
  INSTANCE;

  public static FieldRefValidator v() {
    return INSTANCE;
  }

  /**
   * Checks the consistency of field references.
   */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    final SootMethod method = body.getMethod();
    if (method.isAbstract()) {
      return;
    }

    final ValidatorImpl<?> vi;
    if (body instanceof BafBody) {
      vi = new ValidatorImpl<Inst>(body, exceptions) {
        @Override
        public void check(Unit unit) {
          Inst inst = (Inst) unit;
          if (inst.containsFieldRef()) {
            assert (inst instanceof FieldArgInst); // interface that defines getFieldRef()
            check(unit, ((FieldArgInst) unit).getFieldRef(), inst);
          }
        }

        @Override
        protected boolean isValidStaticRef(Inst refToCheck) {
          return (refToCheck instanceof StaticPutInst) || (refToCheck instanceof StaticGetInst);
        }

        @Override
        protected boolean isValidNonStaticRef(Inst refToCheck) {
          return (refToCheck instanceof FieldPutInst) || (refToCheck instanceof FieldGetInst);
        }
      };
    } else {
      vi = new ValidatorImpl<FieldRef>(body, exceptions) {
        @Override
        public void check(Unit unit) {
          Stmt stmt = (Stmt) unit;
          if (stmt.containsFieldRef()) {
            FieldRef ref = stmt.getFieldRef();
            check(unit, ref.getFieldRef(), ref);
          }
        }

        @Override
        protected boolean isValidStaticRef(FieldRef refToCheck) {
          return (refToCheck instanceof StaticFieldRef);
        }

        @Override
        protected boolean isValidNonStaticRef(FieldRef refToCheck) {
          return (refToCheck instanceof InstanceFieldRef);
        }
      };
    }

    // Run the validator on all units in the body
    for (Unit unit : body.getUnits().getNonPatchingChain()) {
      vi.check(unit);
    }
  }

  private static abstract class ValidatorImpl<T> {

    private final Body body;
    private final List<ValidationException> exs;

    public ValidatorImpl(Body body, List<ValidationException> exceptions) {
      this.body = body;
      this.exs = exceptions;
    }

    public abstract void check(Unit unit);

    protected abstract boolean isValidStaticRef(T refToCheck);

    protected abstract boolean isValidNonStaticRef(T refToCheck);

    protected final void check(Unit unit, SootFieldRef sRef, T refToCheck) {
      SootField field;
      try {
        field = sRef.resolve();
      } catch (ResolutionFailedException e) {
        exs.add(new UnitValidationException(unit, body, "Unable to resolve SootFieldRef " + sRef + ": " + e.getMessage()));
        field = null;
      }
      if (field == null) {
        exs.add(new UnitValidationException(unit, body, "SootFieldRef resolved to null: " + sRef));
      } else if (!field.isPhantom()) {
        if (field.isStatic()) {
          if (!isValidStaticRef(refToCheck)) {
            String s = refToCheck.getClass().getName();
            exs.add(new UnitValidationException(unit, body, "Used " + s + " for static field " + sRef));
          }
        } else {
          if (!isValidNonStaticRef(refToCheck)) {
            String s = refToCheck.getClass().getName();
            exs.add(new UnitValidationException(unit, body, "Used " + s + " for non-static field " + sRef));
          }
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
