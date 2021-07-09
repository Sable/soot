package soot.dava.internal.javaRep;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Jerome Miecznikowski
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

import java.util.Set;

import soot.SootFieldRef;
import soot.UnitPrinter;
import soot.Value;
import soot.grimp.internal.GInstanceFieldRef;

public class DInstanceFieldRef extends GInstanceFieldRef {

  private final Set<Object> thisLocals;

  public DInstanceFieldRef(Value base, SootFieldRef fieldRef, Set<Object> thisLocals) {
    super(base, fieldRef);

    this.thisLocals = thisLocals;
  }

  @Override
  public void toString(UnitPrinter up) {
    if (thisLocals.contains(getBase())) {
      up.fieldRef(getFieldRef());
    } else {
      super.toString(up);
    }
  }

  @Override
  public String toString() {
    if (thisLocals.contains(getBase())) {
      return getFieldRef().name();
    } else {
      return super.toString();
    }
  }

  @Override
  public Object clone() {
    return new DInstanceFieldRef(getBase(), getFieldRef(), thisLocals);
  }
}
