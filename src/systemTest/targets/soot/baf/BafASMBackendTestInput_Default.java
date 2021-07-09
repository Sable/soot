package soot.baf;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Timothy Hoffman
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
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

/**
 * Copied {@link #isAnnotationPresent} from {@link java.lang.Class} because it contains a call to a "default" method in an
 * interface which requires {@link soot.baf.BafASMBackend} to produce bytecode version 1.8 or greater.
 * 
 * @author Timothy Hoffman
 */
public class BafASMBackendTestInput_Default implements GenericDeclaration {

  public BafASMBackendTestInput_Default() {
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return GenericDeclaration.super.isAnnotationPresent(annotationClass);
  }

  @Override
  public TypeVariable<?>[] getTypeParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Annotation[] getAnnotations() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    throw new UnsupportedOperationException();
  }
}
