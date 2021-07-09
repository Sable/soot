package soot.jimple.toolkit.scalar;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import soot.Body;
import soot.SootMethod;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.options.Options;
import soot.shimple.ShimpleBody;
import soot.testing.framework.AbstractTestingFramework;

@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class CopyPropagatorTest extends AbstractTestingFramework {

  private static final boolean DEBUG_PRINT = false;

  private static final String TEST_TARGET_CLASS = "soot.jimple.toolkits.scalar.CopyPropagatorTestInput";
  private static final String RUN_TEST_METHOD_NAME = "runTest";
  private static final String RUN_TEST_EXPECT_FIELD_NAME = "EXPECT";

  @Override
  protected void setupSoot() {
    final Options opts = Options.v();

    // Ensure a ShimpleBody is formed
    opts.set_via_shimple(true);
    opts.set_output_format(Options.output_format_shimple);

    // Skip optimizations after eliminating Phi nodes
    opts.setPhaseOption("shimple", "node-elim-opt:false");
  }

  private static void transform(Body b) {
    Map<String, String> options = new HashMap<>();
    options.put("enabled", "true");
    options.put("only-stack-locals", "false");
    options.put("only-regular-locals", "false");

    CopyPropagator.v().transform(b, "test-cp-1", options);
    if (DEBUG_PRINT) {
      System.out.println("[transform](after test-cp-1) " + b);
    }
    CopyPropagator.v().transform(b, "test-cp-2", options);
    if (DEBUG_PRINT) {
      System.out.println("[transform](after test-cp-2) " + b);
    }
    DeadAssignmentEliminator.v().transform(b, "test-dae", options);
  }

  @Test
  public void test_cp_nonSSA() throws Exception {
    SootMethod target =
        prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "implCompressSimpl", "int[]"), TEST_TARGET_CLASS);

    ShimpleBody body = (ShimpleBody) target.retrieveActiveBody();
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_nonSSA](Initial) " + body);
    }

    // Eliminate PhiExpr and then rebuild SSA form to introduce
    // the code that gives CopyPropagator a hard time.
    body.eliminateNodes();
    body.rebuild();
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_nonSSA](elim+rebuild) " + body);
    }

    // In this case, again eliminate all PhiExpr to show that the
    // CopyPropagator does not have the problem when there are none.
    body.eliminateNodes();
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_nonSSA](elim-2) " + body);
    }

    // Now, run CopyPropagator when there are no PhiExpr present.
    transform(body);
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_nonSSA](cp+dae) " + body);
    }

    // Convert the body to a runnable classfile, run, and test output.
    Class<?> c = generateClass(target.getDeclaringClass());
    int[] expect = (int[]) c.getField(RUN_TEST_EXPECT_FIELD_NAME).get(null);
    int[] actual = (int[]) c.getMethod(RUN_TEST_METHOD_NAME).invoke(null);
    if (DEBUG_PRINT) {
      System.out.println("expect = " + Arrays.toString(expect));
      System.out.println("actual = " + Arrays.toString(actual));
    }
    Assert.assertArrayEquals("failure in test_cp_nonSSA", expect, actual);
  }

  @Test
  public void test_cp_withSSA() throws Exception {
    SootMethod target =
        prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "implCompressSimpl", "int[]"), TEST_TARGET_CLASS);

    ShimpleBody body = (ShimpleBody) target.retrieveActiveBody();
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_withSSA](Initial) " + body);
    }

    // Eliminate PhiExpr and then rebuild SSA form to introduce
    // the code that gives CopyPropagator a hard time.
    body.eliminateNodes();
    body.rebuild();
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_withSSA](elim+rebuild) " + body);
    }

    // Run CopyPropagator when there are PhiExpr present.
    transform(body);
    if (DEBUG_PRINT) {
      System.out.println("[test_cp_withSSA](cp+dae) " + body);
    }

    // Convert the body to a runnable classfile, run, and test output.
    Class<?> c = generateClass(target.getDeclaringClass());
    int[] expect = (int[]) c.getField(RUN_TEST_EXPECT_FIELD_NAME).get(null);
    int[] actual = (int[]) c.getMethod(RUN_TEST_METHOD_NAME).invoke(null);
    if (DEBUG_PRINT) {
      System.out.println("expect = " + Arrays.toString(expect));
      System.out.println("actual = " + Arrays.toString(actual));
    }
    Assert.assertArrayEquals("failure in test_cp_withSSA", expect, actual);
  }
}
