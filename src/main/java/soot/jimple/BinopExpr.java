package soot.jimple;

import soot.Type;

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

import soot.Value;
import soot.ValueBox;

public interface BinopExpr extends Expr {
  public Value getOp1();

  public Value getOp2();

  public ValueBox getOp1Box();

  public ValueBox getOp2Box();

  public void setOp1(Value op1);

  public void setOp2(Value op2);

  public String getSymbol();

  public String toString();

  /**
   * Returns the known operand type or null
   * 
   * @return the known operand type or null
   */
  public default Type getKnownOperandType() {
    return null;
  }
}
