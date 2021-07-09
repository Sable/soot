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

import com.ibm.j9ddr.IVMData;
import com.ibm.j9ddr.VMDataFactory;
import com.ibm.j9ddr.corereaders.memory.IProcess;

import java.io.IOException;
import java.io.InputStream;

import soot.ClassSource;
import soot.SootClass;
import soot.asm.SootClassBuilder;
import soot.javaToJimple.IInitialResolver.Dependencies;

/**
 * Cache class source implementation.
 * 
 * @author Kristen Newbury
 */

public class CacheClassSource extends ClassSource {

  private byte[] cookiesource;
  private byte[] classsource;
  private CacheMemorySingleton memory;
  private long cacheaddr;
  private int cachesize;

  /**
   * Constructs a new Cache class source.
   * 
   * @param cls
   *          fully qualified name of the class.
   * @param data
   *          stream containing data for class.
   */
  CacheClassSource(String cls, byte[] source, CacheMemorySingleton memory, long cacheaddr, int cachesize) {
    super(cls);
    if (source == null) {
      throw new IllegalStateException("Error: The class source must not be null.");
    }
    this.cookiesource = source;
    this.memory = memory;
    this.cacheaddr = cacheaddr;
    this.cachesize = cachesize;
  }

  @Override
  public Dependencies resolve(SootClass sc) {
    InputStream d = null;
    try {

      // ideally replace index (24) into cookie with the runtime val for offset of romclass address
      // but no current way to know that value from the scc/jvm side
      long addr = 0;
      for (int i = 0; i < 8; i++) {
        addr += ((long) cookiesource[i + 24] & 0xffL) << (8 * i);
      }

      Dependencies deps = new Dependencies();
      SootClassBuilder scb = new SootClassBuilder(sc);
      tryWithMemModel(addr, scb);
      deps.typesToSignature.addAll(scb.getDeps());
      return deps;
    } catch (Exception e) {
      throw new RuntimeException("Error: Failed to create class reader from class source.", e);
    } finally {
      try {
        if (d != null) {
          d.close();
          d = null;
        }
      } catch (IOException e) {
        throw new RuntimeException("Error: Failed to close source input stream.", e);
      } finally {
        close();
      }
    }
  }

  void tryWithMemModel(long addr, SootClassBuilder scb) {

    IProcess proc = (IProcess) memory.getMemory();
    try {
      // setup DDR - init datatype
      assert proc != null : "Process should not be null";
      IVMData aVMData = VMDataFactory.getVMData(proc);
      assert aVMData != null : "VMDATA should not be null";

      // now add the memory source
      memory.addMemorySource(this.cacheaddr, this.cachesize);

      // can force our wrapper to be loaded by J9DDRClassLoader
      // additionally, this is why this must be built with OpenJ9 JVM
      aVMData.bootstrap("com.ibm.j9ddr.vm29.ROMClassWrapper", new Object[] { addr, scb, memory });

    } catch (Exception e) {
      System.out.println("Could not setup ddr" + e.getMessage());
      e.printStackTrace(System.out);
    }

  }
}
