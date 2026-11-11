package soot.jimple.toolkits.typing.fast;

import java.util.Collections;
import java.util.Map;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2008 Ben Bellamy 
 * 
 * All rights reserved.
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

import soot.Local;
import soot.Type;

/**
 * Represents a data structure which saves typing information for local variables
 */
public interface ITyping {

  public static final ITyping EMPTY_TYPING = new ITyping() {

    @Override
    public Type get(Local v) {
      return null;
    }

    @Override
    public void set(Local v, Type t) {
    }

    @Override
    public Iterable<Local> getAllLocals() {
      return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public ITyping createCloneTyping() {
      return this;
    }

    @Override
    public Map<Local, Type> getMap() {
      return Collections.emptyMap();
    }

  };

  /**
   * Returns the type (or null)
   * 
   * @param v
   *          the local variable
   * @return the type (or null)
   */
  public Type get(Local v);

  /**
   * Sets the type of the local variable
   * 
   * @param v
   *          the local variable
   * @param t
   *          the type
   */
  public void set(Local v, Type t);

  /**
   * Returns an iterable of all typed locals
   * 
   * @return an iterable of all typed locals
   */
  public Iterable<Local> getAllLocals();

  /**
   * Returns true if no locals are typed
   * 
   * @return true if no locals are typed
   */
  public boolean isEmpty();

  /**
   * Clones all locals and typings from an existing typing. Removes potentially existing typings
   * 
   * @param tg
   *          the typing
   */
  public default void cloneFrom(ITyping tg) {
    clear();
    for (Local l : tg.getAllLocals()) {
      set(l, tg.get(l));
    }
  }

  /**
   * Clears all locals and typings.
   */
  public void clear();

  /**
   * Creates a new typing class as a copy of this topic.
   * 
   * @return the new {@link ITyping}
   */
  public ITyping createCloneTyping();

  public Map<Local, Type> getMap();

}
