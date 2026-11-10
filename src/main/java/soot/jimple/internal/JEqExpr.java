package soot.jimple.internal;

import java.util.List;

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

import soot.BooleanType;
import soot.PrimType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.baf.Baf;
import soot.jimple.Constant;
import soot.jimple.ConvertToBaf;
import soot.jimple.EqExpr;
import soot.jimple.ExprSwitch;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleToBafContext;
import soot.jimple.NullConstant;
import soot.util.Switch;

public class JEqExpr extends AbstractJimpleIntBinopExpr implements EqExpr {

  public JEqExpr(Value op1, Value op2) {
    super(op1, op2);
  }

  @Override
  public final String getSymbol() {
    return " == ";
  }

  @Override
  public void apply(Switch sw) {
    ((ExprSwitch) sw).caseEqExpr(this);
  }

  @Override
  public Type getType() {
    return BooleanType.v();
  }

  @Override
  public Type getKnownOperandType() {
    Value op1 = getOp1();
    Value op2 = getOp2();
    return getKnownOperandType(op1, op2);
  }

  @Override
  protected Unit makeBafInst(Type opType) {
    throw new RuntimeException("unsupported conversion: " + this);
    // return Baf.v().newEqInst(this.getOp1().getType()); }
  }

  @Override
  public void convertToBaf(JimpleToBafContext context, List<Unit> out) {
    // BAF has no value-producing comparison, so materialize the boolean result with branches,
    // mirroring how JIfStmt converts the same condition: push 1 when equal, push 0 otherwise.
    Value op1 = getOp1();
    Value op2 = getOp2();

    // Both sides are the same trivial constant (null == null, 0 == 0): fold to true directly
    // instead of emitting a branch on a materialized constant.
    if ((op1 instanceof NullConstant && op2 instanceof NullConstant) || (isZero(op1) && isZero(op2))) {
      Unit pushOne = Baf.v().newPushInst(IntConstant.v(1));
      pushOne.addAllTagsOf(context.getCurrentUnit());
      out.add(pushOne);
      return;
    }

    Unit pushZero = Baf.v().newPushInst(IntConstant.v(0));
    Unit end = Baf.v().newNopInst();
    Unit jumpToFalse;
    if (op2 instanceof NullConstant || op1 instanceof NullConstant) {
      Value ref = (op2 instanceof NullConstant) ? op1 : op2;
      ((ConvertToBaf) ref).convertToBaf(context, out);
      jumpToFalse = Baf.v().newIfNonNullInst(pushZero);
    } else if (isZero(op2) || isZero(op1)) {
      Value other = isZero(op2) ? op1 : op2;
      ((ConvertToBaf) other).convertToBaf(context, out);
      jumpToFalse = Baf.v().newIfNeInst(pushZero);
    } else {
      ((ConvertToBaf) op1).convertToBaf(context, out);
      ((ConvertToBaf) op2).convertToBaf(context, out);
      jumpToFalse = Baf.v().newIfCmpNeInst(op1.getType(), pushZero);
    }

    Unit pushOne = Baf.v().newPushInst(IntConstant.v(1));
    Unit jumpToEnd = Baf.v().newGotoInst(end);
    for (Unit u : new Unit[] { jumpToFalse, pushOne, jumpToEnd, pushZero, end }) {
      u.addAllTagsOf(context.getCurrentUnit());
      out.add(u);
    }
  }

  private static boolean isZero(Value v) {
    return v instanceof IntConstant && ((IntConstant) v).value == 0;
  }

  @Override
  public Object clone() {
    return new JEqExpr(Jimple.cloneIfNecessary(getOp1()), Jimple.cloneIfNecessary(getOp2()));
  }

  static Type getKnownOperandType(Value op1, Value op2) {
    if (op2 instanceof Constant && op2.getType() instanceof PrimType) {
      return op1.getType();
    }
    if (op1 instanceof Constant && op1.getType() instanceof PrimType) {
      return op1.getType();
    }
    return null;
  }
}
