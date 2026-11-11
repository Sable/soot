package soot.jimple.internal;

import soot.BooleanType;
import soot.IntType;

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

import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ExprSwitch;
import soot.jimple.GtExpr;
import soot.jimple.Jimple;
import soot.util.Switch;

public class JGtExpr extends AbstractJimpleIntBinopExpr implements GtExpr {

  public JGtExpr(Value op1, Value op2) {
    super(op1, op2);
  }

  @Override
  public final String getSymbol() {
    return " > ";
  }

  @Override
  public Type getType() {
    return BooleanType.v();
  }

  @Override
  public void apply(Switch sw) {
    ((ExprSwitch) sw).caseGtExpr(this);
  }

  @Override
  public Type getKnownOperandType() {
    return IntType.v();
  }

  @Override
  protected Unit makeBafInst(Type opType) {
    throw new RuntimeException("unsupported conversion: " + this);
    // return Baf.v().newGtInst(this.getOp1().getType());
  }

  @Override
  public Object clone() {
    return new JGtExpr(Jimple.cloneIfNecessary(getOp1()), Jimple.cloneIfNecessary(getOp2()));
  }
}
