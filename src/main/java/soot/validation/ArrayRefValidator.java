package soot.validation;

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

import soot.ArrayType;
import soot.Body;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.ArrayRef;
import soot.jimple.Stmt;
import soot.jimple.internal.IIntLikeType;

/**
 * Validates that each ArrayRef points to an ArrayType and that its index is an IntType.
 */
public enum ArrayRefValidator implements BodyValidator {
  INSTANCE;

  public static ArrayRefValidator v() {
    return INSTANCE;
  }

  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    PatchingChain<Unit> units = body.getUnits();

    for (Unit unit : units) {
      if (!(unit instanceof Stmt)) {
        continue;
      }

      ArrayRef ref = ((Stmt) unit).getArrayRefUnsafe();
      if (ref != null) {
        if (!(ref.getBase().getType() instanceof ArrayType)) {
          exceptions.add(new ValidationException(unit, "Array " + ref.getBase() + " has type " + ref.getBase().getType()
              + " which is no ArrayType in " + body.getMethod()));
        }
        if (!(ref.getIndex().getType() instanceof IIntLikeType)) {
          exceptions.add(new ValidationException(unit, "Array index " + ref + " has type " + ref.getIndex().getType()
              + " which is no intlike type in " + body.getMethod()));
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }

}
