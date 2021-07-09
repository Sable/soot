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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import soot.G;
import soot.ModulePathSourceLocator;
import soot.ModuleScene;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.testing.framework.AbstractTestingFramework;

/**
 * Test the bytecode version computation in {@link soot.baf.BafASMBackend}.
 * 
 * @author Timothy Hoffman
 */
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class BafASMBackendTest extends AbstractTestingFramework {

  @Override
  protected void setupSoot() {
  }

  @Test
  public void testCachingInvalidation() throws Exception {
    final String targetClass = "soot.baf.BafASMBackendTestInput_Default";
    final Scene scene = customSetupLib(rtJar(), Collections.singletonList(targetClass));

    SootClass sc = getOrResolveSootClass(scene, targetClass, SootClass.BODIES);
    Assert.assertNotNull(sc);
    Assert.assertEquals(targetClass, sc.getName());

    // Ensure the class was loaded properly with all bodies
    Assert.assertEquals(6, sc.getMethodCount());
    Assert.assertTrue(sc.getMethods().stream().allMatch(SootMethod::hasActiveBody));

    // Ensure the version will be derived only by BafASMBackend
    // (and not from the input source version).
    Assert.assertEquals(0, Options.v().java_version());

    byte[] bytecode = generateBytecode(sc);

    // Run ASM verifier and ensure the message is empty (i.e. there are no VerifyErrors).
    StringWriter strWriter = new StringWriter();
    CheckClassAdapter.verify(new ClassReader(bytecode), null, false, new PrintWriter(strWriter));
    String verifyMsg = strWriter.toString();
    Assert.assertTrue(verifyMsg, verifyMsg.isEmpty());
  }

  private Scene customSetupLib(String rtJar, List<String> classNames) {
    G.reset();

    final Options opts = Options.v();

    opts.set_whole_program(true);
    opts.set_output_format(Options.output_format_none);
    opts.set_allow_phantom_refs(true);
    opts.set_no_bodies_for_excluded(true);
    opts.set_exclude(getExcludes());
    opts.set_include(classNames);
    opts.set_process_dir(Collections.singletonList(SYSTEMTEST_TARGET_CLASSES_DIR));

    // Disable deriving Java version from the input bytecode so that the
    // version is computed based only on the logic in BafASMBackend.
    opts.set_derive_java_version(false);

    final boolean isModuleJar = isModuleJar(rtJar);
    if (isModuleJar) {
      opts.set_soot_modulepath(ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS);
    } else {
      opts.set_soot_classpath(rtJar);
    }

    // NOTE: must obtain Scene after all options are set
    Scene scene = isModuleJar ? ModuleScene.v() : Scene.v();
    scene.loadNecessaryClasses();
    runSoot();
    return scene;
  }

  private static String rtJar() throws IOException {
    Path p = Paths.get(System.getProperty("java.home"), "lib", "rt.jar");
    return Files.exists(p) ? p.toRealPath().toString() : null;
  }

  private static boolean isModuleJar(String rtJar) {
    return (rtJar == null);
  }
}
