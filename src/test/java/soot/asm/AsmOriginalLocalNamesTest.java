package soot.asm;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2026 Raja Vallée-Rai and others
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.ToolProvider;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.options.Options;

@RunWith(Parameterized.class)
public class AsmOriginalLocalNamesTest {
  @Parameterized.Parameters(name = "name-aware packing={0}")
  public static Object[] options() {
    return new Object[] { false, true };
  }

  @Parameterized.Parameter
  public boolean nameAwarePacking;
  @Rule
  public TemporaryFolder temporary = new TemporaryFolder();

  @After
  public void cleanup() {
    G.reset();
  }

  @Test
  public void temporaryExpressionMustNotBorrowALaterPasswordName() throws Exception {
    Body body = loadNamesBody(true);
    Local username = resultLocal(body, "username");
    Local password = resultLocal(body, "secret");
    assertNotSame("An unrelated temporary must not share the source variable", username, password);
    assertFalse("A username expression must not be called password", username.getName().startsWith("password"));
    assertEquals("The genuine source variable must retain its name", "password", password.getName());
  }

  @Test
  public void disablingPackingMustOverrideAnExplicitGeneralPackingPolicy() throws Exception {
    Body body = loadNamesBody(false);
    Local username = resultLocal(body, "username");
    Local password = resultLocal(body, "secret");
    if (nameAwarePacking) {
      assertSame("An explicitly enabled general packing policy may reuse non-overlapping locals", username, password);
    } else {
      assertNotSame("A disabled phase must not apply its general packing policy", username, password);
    }
  }

  private Body loadNamesBody(boolean unsplitOriginalLocals) throws Exception {
    Path root = temporary.getRoot().toPath();
    Path source = root.resolve("Names.java");
    Files.writeString(source, "public class Names {"
        + " static native String username(); static native String secret(); static native void output(String s);"
        + " public static void run(boolean rejected) { if (rejected) { output(username()); return; }"
        + " String password = secret(); output(password); } }");
    assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
        "-g", "--release", "11", "-d", root.toString(), source.toString()));
    G.reset();
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_soot_classpath(root + File.pathSeparator + Scene.v().defaultClassPath());
    Options.v().set_process_dir(List.of(root.toString()));
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("jb.ulp", "enabled:" + nameAwarePacking);
    Options.v().setPhaseOption("jb.ulp", "unsplit-original-locals:" + unsplitOriginalLocals);
    Options.v().setPhaseOption("jb.sils", "enabled:false");
    Scene.v().loadNecessaryClasses();
    SootClass type = Scene.v().forceResolve("Names", SootClass.BODIES);
    type.setApplicationClass();
    Body body = type.getMethodByName("run").retrieveActiveBody();
    body.validate();
    return body;
  }

  private static Local resultLocal(Body body, String method) {
    for (Unit unit : body.getUnits()) {
      if (unit instanceof AssignStmt && ((Stmt) unit).containsInvokeExpr()
          && ((Stmt) unit).getInvokeExpr().getMethodRef().getName().equals(method)) {
        return (Local) ((AssignStmt) unit).getLeftOp();
      }
    }
    throw new AssertionError("Missing invocation: " + method + " in " + body);
  }
}
