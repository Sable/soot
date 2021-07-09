package soot.jimple.internal;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.baf.Baf;
import soot.baf.PlaceholderInst;
import soot.jimple.ConvertToBaf;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleToBafContext;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.StmtSwitch;
import soot.util.Switch;

public class JLookupSwitchStmt extends AbstractSwitchStmt implements LookupSwitchStmt {
  /**
   * List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
   */
  protected List<IntConstant> lookupValues;

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends Unit> targets, Unit defaultTarget) {
    this(Jimple.v().newImmediateBox(key), lookupValues, getTargetBoxesArray(targets, Jimple.v()::newStmtBox),
        Jimple.v().newStmtBox(defaultTarget));
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends UnitBox> targets,
      UnitBox defaultTarget) {
    this(Jimple.v().newImmediateBox(key), lookupValues, targets.toArray(new UnitBox[targets.size()]), defaultTarget);
  }

  protected JLookupSwitchStmt(ValueBox keyBox, List<IntConstant> lookupValues, UnitBox[] targetBoxes,
      UnitBox defaultTargetBox) {
    super(keyBox, defaultTargetBox, targetBoxes);
    setLookupValues(lookupValues);
  }

  @Override
  public Object clone() {
    List<IntConstant> clonedLookupValues = new ArrayList<IntConstant>(lookupValues.size());
    for (IntConstant c : lookupValues) {
      clonedLookupValues.add(IntConstant.v(c.value));
    }
    return new JLookupSwitchStmt(getKey(), clonedLookupValues, getTargets(), getDefaultTarget());
  }

  private String toSimpleString() {
    final char endOfLine = ' ';
    List<String> cases = lookupValues.stream()
            .map((lookupValue) -> String.valueOf(lookupValue.value))
            .distinct()
            .collect(Collectors.toList());
    if (getDefaultTarget() != null) {
      cases.add("default");
    }
    return Jimple.LOOKUPSWITCH + "(" + keyBox.getValue().toString() + ')' + endOfLine
              + "{" + endOfLine
              + "cases:" + String.join(",", cases) + endOfLine
              + "}";
  }

  @Override
  public String toString() {
    final char endOfLine = ' ';
    StringBuilder buf = new StringBuilder(Jimple.LOOKUPSWITCH + "(");

    buf.append(keyBox.getValue().toString()).append(')').append(endOfLine);
    buf.append('{').append(endOfLine);

    for (ListIterator<IntConstant> it = lookupValues.listIterator(); it.hasNext();) {
      IntConstant c = it.next();
      buf.append("    " + Jimple.CASE + " ").append(c).append(": " + Jimple.GOTO + " ");
      Unit target = getTarget(it.previousIndex());
      if (target instanceof JLookupSwitchStmt) {
        buf.append(target == this ? "self" : ((JLookupSwitchStmt) target).toSimpleString()).append(';').append(endOfLine);
      } else {
        buf.append(target).append(';').append(endOfLine);
      }
    }
    {
      buf.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ");
      Unit target = getDefaultTarget();
      if (target instanceof JLookupSwitchStmt) {
        buf.append(target == this ? "self" : ((JLookupSwitchStmt) target).toSimpleString()).append(';').append(endOfLine);
      } else {
        buf.append(target).append(';').append(endOfLine);
      }
    }
    buf.append('}');

    return buf.toString();
  }

  @Override
  public void toString(UnitPrinter up) {
    up.literal(Jimple.LOOKUPSWITCH + "(");
    keyBox.toString(up);
    up.literal(")");
    up.newline();
    up.literal("{");
    up.newline();
    for (ListIterator<IntConstant> it = lookupValues.listIterator(); it.hasNext();) {
      IntConstant c = it.next();
      up.literal("    " + Jimple.CASE + " ");
      up.constant(c);
      up.literal(": " + Jimple.GOTO + " ");
      targetBoxes[it.previousIndex()].toString(up);
      up.literal(";");
      up.newline();
    }

    up.literal("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ");
    defaultTargetBox.toString(up);
    up.literal(";");
    up.newline();
    up.literal("}");
  }

  @Override
  public void setLookupValues(List<IntConstant> lookupValues) {
    this.lookupValues = new ArrayList<IntConstant>(lookupValues);
  }

  @Override
  public void setLookupValue(int index, int value) {
    lookupValues.set(index, IntConstant.v(value));
  }

  @Override
  public int getLookupValue(int index) {
    return lookupValues.get(index).value;
  }

  @Override
  public List<IntConstant> getLookupValues() {
    return Collections.unmodifiableList(lookupValues);
  }

  @Override
  public void apply(Switch sw) {
    ((StmtSwitch) sw).caseLookupSwitchStmt(this);
  }

  @Override
  public void convertToBaf(JimpleToBafContext context, List<Unit> out) {
    ((ConvertToBaf) getKey()).convertToBaf(context, out);

    final Baf baf = Baf.v();
    final List<Unit> targets = getTargets();
    List<PlaceholderInst> targetPlaceholders = new ArrayList<PlaceholderInst>(targets.size());
    for (Unit target : targets) {
      targetPlaceholders.add(baf.newPlaceholderInst(target));
    }

    Unit u = baf.newLookupSwitchInst(baf.newPlaceholderInst(getDefaultTarget()), getLookupValues(), targetPlaceholders);
    u.addAllTagsOf(this);
    out.add(u);
  }
}
