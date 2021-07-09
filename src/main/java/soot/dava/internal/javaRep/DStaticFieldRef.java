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

import soot.SootFieldRef;
import soot.UnitPrinter;
import soot.jimple.StaticFieldRef;

public class DStaticFieldRef extends StaticFieldRef {

  private final boolean supressDeclaringClass;

  public DStaticFieldRef(SootFieldRef fieldRef, String myClassName) {
    this(fieldRef, myClassName.equals(fieldRef.declaringClass().getName()));
  }

  public DStaticFieldRef(SootFieldRef fieldRef, boolean supressDeclaringClass) {
    super(fieldRef);
    this.supressDeclaringClass = supressDeclaringClass;
  }

  @Override
  public Object clone() {
    return new DStaticFieldRef(getFieldRef(), supressDeclaringClass);
  }

  @Override
  public void toString(UnitPrinter up) {
    SootFieldRef fRef = getFieldRef();
    if (!supressDeclaringClass) {
      up.type(fRef.declaringClass().getType());
      up.literal(".");
    }
    up.fieldRef(fRef);
  }
}
