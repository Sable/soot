package soot.grimp.internal;

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

import soot.SootFieldRef;
import soot.Value;
import soot.grimp.Grimp;
import soot.grimp.Precedence;
import soot.jimple.internal.AbstractInstanceFieldRef;

public class GInstanceFieldRef extends AbstractInstanceFieldRef implements Precedence {

  public GInstanceFieldRef(Value base, SootFieldRef fieldRef) {
    super(Grimp.v().newObjExprBox(base), fieldRef);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    final Value op = getBase();
    if (op instanceof Precedence && ((Precedence) op).getPrecedence() < getPrecedence()) {
      sb.append('(').append(op.toString()).append(')');
    } else {
      sb.append(op.toString());
    }
    sb.append('.').append(getFieldRef().getSignature());
    return sb.toString();
  }

  @Override
  public int getPrecedence() {
    return 950;
  }

  @Override
  public Object clone() {
    return new GInstanceFieldRef(Grimp.cloneIfNecessary(getBase()), getFieldRef());
  }
}
