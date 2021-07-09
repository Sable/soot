package soot.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Reference counting wrapper for a {@link Closeable} that closes the resource when the reference count reaches {@code 0}.
 * Extends {@link AutoCloseable} so that each region using the resource can simply be wrapped in a try-with-resources block.
 * 
 * @author Timothy Hoffman
 *
 * @param <T>
 *
 * @see AutoCloseable
 */
public final class SharedCloseable<T extends Closeable> implements AutoCloseable {

  private static final AtomicLongFieldUpdater<SharedCloseable> NUM_REFS_UPDATER =
      AtomicLongFieldUpdater.newUpdater(SharedCloseable.class, "numRefs");

  // NOTE: only accessed via the AtomicLongFieldUpdater
  private volatile long numRefs = 1;

  private final T resource;

  public SharedCloseable(T resource) {
    this.resource = resource;
  }

  public SharedCloseable<T> acquire() {
    long oldCount = NUM_REFS_UPDATER.getAndIncrement(this);
    if (oldCount <= 0) {
      NUM_REFS_UPDATER.getAndDecrement(this);// restore to 0 (or lower)
      throw new IllegalStateException("Already closed");
    }
    return this;
  }

  public boolean release() {
    long newCount = NUM_REFS_UPDATER.decrementAndGet(this);
    if (newCount == 0) {
      try {
        resource.close();
      } catch (IOException ex) {
        // ignored
      }
      return true;
    } else if (newCount < 0) {
      throw new IllegalStateException("Already closed");
    } else {
      return false;
    }
  }

  public T get() {
    return resource;
  }

  @Override
  public void close() {
    release();
  }
}
