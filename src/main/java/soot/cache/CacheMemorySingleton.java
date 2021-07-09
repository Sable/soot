package soot.cache;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Kristen Newbury
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

import com.ibm.j9ddr.corereaders.memory.BufferedMemory;
import com.ibm.j9ddr.corereaders.memory.BufferedMemorySource;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//@author Kristen Newbury

public class CacheMemorySingleton {

  // this singleton can only have one memory source ever added to it
  private static CacheMemorySingleton cacheMemorySingleton;
  private BufferedMemory memory;
  private BufferedMemorySource memorySource;

  private CacheMemorySingleton() {
    memory = new BufferedMemory(ByteOrder.nativeOrder());
  }

  public static CacheMemorySingleton getInstance() {
    if (cacheMemorySingleton == null) {
      cacheMemorySingleton = new CacheMemorySingleton();
    }
    return cacheMemorySingleton;
  }

  public BufferedMemory getMemory() {
    return memory;
  }

  public BufferedMemorySource getMemorySource() {
    return memorySource;
  }

  public void addMemorySource(long addr, int size) {
    if (cacheMemorySingleton != null && memorySource == null) {
      ByteBuffer bb = makeCacheBuffer(addr, size);
      memorySource = new BufferedMemorySource(addr, bb);
      memory.addMemorySource(memorySource);
    }
  }

  static final Field address;
  static final Field capacity;

  static {
    // first set the buffer to be configurable
    try {
      address = Buffer.class.getDeclaredField("address");
      address.setAccessible(true);
      capacity = Buffer.class.getDeclaredField("capacity");
      capacity.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  private ByteBuffer makeCacheBuffer(long addr, int size) {
    // then set it up
    try {
      ByteBuffer bb = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
      address.setLong(bb, addr);
      capacity.setInt(bb, size);
      bb.clear();
      return bb;
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

}
