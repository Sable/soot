package soot.jimple.internal;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
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
import java.util.List;

import soot.SootField;
import soot.SootFieldRef;
import soot.Type;
import soot.Unit;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.baf.Baf;
import soot.grimp.PrecedenceTest;
import soot.jimple.ConvertToBaf;
import soot.jimple.InstanceFieldRef;
import soot.jimple.JimpleToBafContext;
import soot.jimple.RefSwitch;
import soot.util.Switch;

@SuppressWarnings("serial")
public abstract class AbstractInstanceFieldRef implements InstanceFieldRef, ConvertToBaf {

  protected final ValueBox baseBox;
  protected SootFieldRef fieldRef;

  protected AbstractInstanceFieldRef(ValueBox baseBox, SootFieldRef fieldRef) {
    if (fieldRef.isStatic()) {
      throw new RuntimeException("wrong static-ness");
    }
    this.baseBox = baseBox;
    this.fieldRef = fieldRef;
  }

  @Override
  public abstract Object clone();

  @Override
  public String toString() {
    return baseBox.getValue().toString() + "." + fieldRef.getSignature();
  }

  @Override
  public void toString(UnitPrinter up) {
    final boolean needsBrackets = PrecedenceTest.needsBrackets(baseBox, this);
    if (needsBrackets) {
      up.literal("(");
    }
    baseBox.toString(up);
    if (needsBrackets) {
      up.literal(")");
    }
    up.literal(".");
    up.fieldRef(fieldRef);
  }

  @Override
  public Value getBase() {
    return baseBox.getValue();
  }

  @Override
  public ValueBox getBaseBox() {
    return baseBox;
  }

  @Override
  public void setBase(Value base) {
    baseBox.setValue(base);
  }

  @Override
  public SootFieldRef getFieldRef() {
    return fieldRef;
  }

  @Override
  public void setFieldRef(SootFieldRef fieldRef) {
    this.fieldRef = fieldRef;
  }

  @Override
  public SootField getField() {
    return fieldRef.resolve();
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> useBoxes = new ArrayList<ValueBox>(baseBox.getValue().getUseBoxes());
    useBoxes.add(baseBox);
    return useBoxes;
  }

  @Override
  public Type getType() {
    return fieldRef.type();
  }

  @Override
  public void apply(Switch sw) {
    ((RefSwitch) sw).caseInstanceFieldRef(this);
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof AbstractInstanceFieldRef) {
      AbstractInstanceFieldRef fr = (AbstractInstanceFieldRef) o;
      return fr.getField().equals(this.getField()) && fr.baseBox.getValue().equivTo(this.baseBox.getValue());
    }
    return false;
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getField().equivHashCode() * 101 + baseBox.getValue().equivHashCode() + 17;
  }

  @Override
  public void convertToBaf(JimpleToBafContext context, List<Unit> out) {
    ((ConvertToBaf) getBase()).convertToBaf(context, out);
    Unit u = Baf.v().newFieldGetInst(fieldRef);
    out.add(u);
    u.addAllTagsOf(context.getCurrentUnit());
  }
}
