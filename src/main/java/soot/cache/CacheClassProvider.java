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

import com.ibm.oti.shared.HelperAlreadyDefinedException;
import com.ibm.oti.shared.Shared;
import com.ibm.oti.shared.SharedClassHelperFactory;
import com.ibm.oti.shared.SharedClassURLClasspathHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import soot.ClassProvider;
import soot.ClassSource;

/**
 * OpenJ9 Shared Cache Class class provider.
 * 
 * @author Kristen Newbury
 */

public class CacheClassProvider implements ClassProvider {

  private static URL testClassUrl;

  public CacheClassProvider() {
    if (!System.getProperty("java.vm.name").contains("OpenJ9")) {
      throw new RuntimeException("CacheClassProvider feature only works with OpenJ9 JVM running Soot");
    }
  }

  public static void setTestClassUrl(String url) {
    try {
      testClassUrl = new URL("file://" + url);
      System.out.println("CacheClassProvider: setting testClassUrl: " + url);
    } catch (MalformedURLException e) {
      System.out.println("Bad URL provided, not using url: " + url);
      e.printStackTrace();
    }
  }

  public ClassSource find(String cls) {

    byte[] romCookie = null;
    CacheMemorySingleton cacheMem = null;
    ByteBuffer wrapper = null;

    SharedClassHelperFactory factory = Shared.getSharedClassHelperFactory();
    if (factory != null) {
      URL[] urlsForRuntime = null;
      URL[] urlsForApp = null;
      URL[] urlsForJCE = null;
      URL jceurl = null;
      URL rturl = null;
      SharedClassURLClasspathHelper helperForAppClasses = null;
      SharedClassURLClasspathHelper helperForRuntimeClasses = null;
      SharedClassURLClasspathHelper helperForJCE = null;
      try {
        rturl = new URL("file://" + System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar");
        jceurl = new URL("file://" + System.getProperty("java.home") + File.separator + "lib" + File.separator + "jce.jar");
        urlsForRuntime = new URL[] { rturl };
        if (testClassUrl != null) {
          urlsForApp = new URL[] { testClassUrl };
        } else {
          System.out.println("No test class url set");
          return null;
        }

        urlsForJCE = new URL[] { jceurl };
      } catch (MalformedURLException e) {
        System.out.println("Bad URL provided");
        e.printStackTrace();
      }
      URLClassLoader loaderForRuntime = new URLClassLoader(urlsForRuntime);
      URLClassLoader loaderForApp = new URLClassLoader(urlsForApp);
      URLClassLoader loaderForJCE = new URLClassLoader(urlsForJCE);

      // get helper to find classes in cache
      try {
        helperForRuntimeClasses = factory.getURLClasspathHelper(loaderForRuntime, urlsForRuntime);
        helperForAppClasses = factory.getURLClasspathHelper(loaderForApp, urlsForApp);
        helperForJCE = factory.getURLClasspathHelper(loaderForJCE, urlsForJCE);
      } catch (HelperAlreadyDefinedException e) {
        System.out.println("Helper already defined?" + e.getMessage());
        e.printStackTrace();
      }

      helperForAppClasses.confirmAllEntries();
      helperForRuntimeClasses.confirmAllEntries();
      helperForJCE.confirmAllEntries();

      try {
        // for now this part happens every time
        // maybe consider avoiding that later
        byte[] cacheInfo = helperForAppClasses.findSharedCache();
        if (cacheInfo != null) {
          wrapper = ByteBuffer.wrap(cacheInfo);
          // jni filled byte array
          wrapper.order(ByteOrder.nativeOrder());
          cacheMem = CacheMemorySingleton.getInstance();
        } else {
          System.out.println("Cannot get cache start");
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }

      romCookie = helperForAppClasses.findSharedClass(cls, null);
      if (romCookie == null) {
        romCookie = helperForRuntimeClasses.findSharedClass(cls, null);
        if (romCookie == null) {
          romCookie = helperForJCE.findSharedClass(cls, null);
          if (romCookie == null) {
            System.out.println("Cannot find class in cache: " + cls);
          } else {
            System.out.println("Located the class in the cache: " + cls);
          }
        } else {
          System.out.println("Located the class in the cache: " + cls);
        }
      } else {
        System.out.println("Located the class in the cache: " + cls);
      }

    } else {
      System.out.println("Cache helper null, cannot find class in cache: " + cls);
      System.out.println("Is Shared Class Cache enabled on command line?");
    }
    return romCookie == null ? null : new CacheClassSource(cls, romCookie, cacheMem, wrapper.getLong(), wrapper.getInt());
  }
}
