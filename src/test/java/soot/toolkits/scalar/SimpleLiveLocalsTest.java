package soot.toolkits.scalar;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import soot.G;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.VoidType;
import soot.baf.Baf;
import soot.baf.BafBody;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;
import soot.util.PhaseDumper;

/**
 * This test generates a few sample methods that contain a branch where one path returns and the other goes into an infinite
 * loop. The {@link SimpleLiveLocals} analysis failed to compute accurate results for units on the infinite loop path
 * because the {@link soot.toolkits.scalar.FlowAnalysis} did not attempt to search for a non-returning loop if the was at
 * least one return statement found somewhere in the body. The tests are run on both the {@link JimpleBody} and the
 * {@link BafBody} which also happened to present a case where several methods in the {@link soot.Body} class considered
 * only {@link soot.jimple.IdentityStmt} rather than {@link soot.IdentityUnit} (thus excluding the other subclass,
 * {@link soot.baf.IdentityInst} which are found in {@link BafBody} instances.
 *
 * @author Timothy Hoffman
 */
public class SimpleLiveLocalsTest {

  private static final boolean DEBUG_PRINT = false;

  static {
    G.reset();
    Scene.v().loadBasicClasses();
  }

  /**
   * Construct {@link #jBody} as the Jimple code for the following:
   * 
   * <pre>
   * void run(Object running) {
   *   if (running == null) {
   *     for (;;) {
   *       try {
   *         this.run(running);
   *       } catch (Throwable x) {
   *       }
   *     }
   *   }
   * }
   * </pre>
   */
  private static JimpleBody createA() {
    final Jimple jimp = Jimple.v();
    final RefType objTy = Scene.v().getRefType("java.lang.Object");
    final RefType throwTy = Scene.v().getRefType("java.lang.Throwable");

    final SootClass sc = new SootClass("SimpleLiveLocalsTestDummy");
    final SootMethod m = new SootMethod("run", Collections.singletonList(objTy), VoidType.v());
    sc.addMethod(m);
    final JimpleBody jb = jimp.newBody(m);
    jb.insertIdentityStmts(sc);

    // Create all of the statements
    final Stmt[] stmts = new Stmt[6];
    final Local param0 = jb.getParameterLocal(0);
    stmts[5] = jimp.newReturnVoidStmt();
    stmts[0] = jimp.newIfStmt(jimp.newNeExpr(param0, NullConstant.v()), stmts[5]);
    stmts[1] = jimp.newInvokeStmt(jimp.newVirtualInvokeExpr(jb.getThisLocal(), m.makeRef(), param0));
    stmts[2] = jimp.newGotoStmt(stmts[1]);
    Local exLoc = jimp.newLocal("$e", throwTy);
    jb.getLocals().add(exLoc);
    stmts[3] = jimp.newIdentityStmt(exLoc, jimp.newCaughtExceptionRef());
    stmts[4] = jimp.newGotoStmt(stmts[1]);

    // Add all statements to the body
    jb.getUnits().getNonPatchingChain().addAll(Arrays.asList(stmts));

    // Add the trap
    jb.getTraps().add(jimp.newTrap(throwTy.getSootClass(), stmts[1], stmts[2], stmts[3]));

    return jb;
  }

  @Test
  public void testNonTerminatingBodyJimpleA() {
    final JimpleBody body = createA();
    if (DEBUG_PRINT) {
      System.out.println("[testNonTerminatingBodyJimpleA] JimpleBody = " + body);
    }

    // Run SimpleLiveLocals
    final ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
    final LiveLocals ll = new SimpleLiveLocals(graph);
    if (DEBUG_PRINT) {
      PhaseDumper.v().dumpGraph(graph, true);
    }

    // Check LiveLocals result
    final Local thisLoc = body.getThisLocal();
    final Local paramLoc = body.getParameterLocal(0);
    final Chain<Unit> units = body.getUnits().getNonPatchingChain();
    Assert.assertEquals(8, units.size());

    final Iterator<Unit> it = units.iterator();
    // this := @this
    check(ll, it.next(), Arrays.asList(), Arrays.asList(thisLoc));
    // parameter0 := @parameter0
    check(ll, it.next(), Arrays.asList(thisLoc), Arrays.asList(thisLoc, paramLoc));
    // if parameter0 != null goto label4
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // this.run(parameter0)
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // $e := @caughtexception
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // return
    check(ll, it.next(), Arrays.asList(), Arrays.asList());

    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void testNonTerminatingBodyBafA() {
    // Disable "bb" Pack for a direct translation from Jimple->Baf
    Options.v().setPhaseOption("bb", "enabled:false");
    // Translate the body to Baf
    final BafBody body = Baf.v().newBody(createA());
    if (DEBUG_PRINT) {
      System.out.println("[testNonTerminatingBodyBafA] BafBody = " + body);
    }

    // Run SimpleLiveLocals
    final ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
    final LiveLocals ll = new SimpleLiveLocals(graph);
    if (DEBUG_PRINT) {
      PhaseDumper.v().dumpGraph(graph, true);
    }

    // Check LiveLocals result
    final Local thisLoc = body.getThisLocal();
    final Local paramLoc = body.getParameterLocal(0);
    final Chain<Unit> units = body.getUnits().getNonPatchingChain();
    Assert.assertEquals(11, units.size());

    final Iterator<Unit> it = units.iterator();
    // this := @this
    check(ll, it.next(), Arrays.asList(), Arrays.asList(thisLoc));
    // parameter0 := @parameter0
    check(ll, it.next(), Arrays.asList(thisLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r parameter0
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // ifnonnull label4
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r this
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r parameter0
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // invoke run(Object)
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // store.r $e
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // return
    check(ll, it.next(), Arrays.asList(), Arrays.asList());

    Assert.assertFalse(it.hasNext());
  }

  /**
   * Construct {@link #jBody} as the Jimple code for the following:
   * 
   * <pre>
   * void run(Object running) {
   *   if (running == null) {
   *     for (;;) {
   *       this.run(running);
   *     }
   *   }
   * }
   * }
   * </pre>
   */
  private static JimpleBody createB() {
    final Jimple jimp = Jimple.v();
    final RefType objTy = Scene.v().getRefType("java.lang.Object");

    final SootClass sc = new SootClass("SimpleLiveLocalsTestDummy");
    final SootMethod m = new SootMethod("run", Collections.singletonList(objTy), VoidType.v());
    sc.addMethod(m);
    final JimpleBody jb = jimp.newBody(m);
    jb.insertIdentityStmts(sc);

    // Create all of the statements
    final Stmt[] stmts = new Stmt[4];
    final Local param0 = jb.getParameterLocal(0);
    stmts[3] = jimp.newReturnVoidStmt();
    stmts[0] = jimp.newIfStmt(jimp.newNeExpr(param0, NullConstant.v()), stmts[3]);
    stmts[1] = jimp.newInvokeStmt(jimp.newVirtualInvokeExpr(jb.getThisLocal(), m.makeRef(), param0));
    stmts[2] = jimp.newGotoStmt(stmts[1]);

    // Add all statements to the body
    jb.getUnits().getNonPatchingChain().addAll(Arrays.asList(stmts));

    return jb;
  }

  @Test
  public void testNonTerminatingBodyJimpleB() {
    final JimpleBody body = createB();
    if (DEBUG_PRINT) {
      System.out.println("[testNonTerminatingBodyJimpleB] JimpleBody = " + body);
    }

    // Run SimpleLiveLocals
    final ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
    final LiveLocals ll = new SimpleLiveLocals(graph);
    if (DEBUG_PRINT) {
      PhaseDumper.v().dumpGraph(graph, true);
    }

    // Check LiveLocals result
    final Local thisLoc = body.getThisLocal();
    final Local paramLoc = body.getParameterLocal(0);
    final Chain<Unit> units = body.getUnits().getNonPatchingChain();
    Assert.assertEquals(6, units.size());

    final Iterator<Unit> it = units.iterator();
    // this := @this
    check(ll, it.next(), Arrays.asList(), Arrays.asList(thisLoc));
    // parameter0 := @parameter0
    check(ll, it.next(), Arrays.asList(thisLoc), Arrays.asList(thisLoc, paramLoc));
    // if parameter0 != null goto label2
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // this.run(parameter0)
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // return
    check(ll, it.next(), Arrays.asList(), Arrays.asList());

    Assert.assertFalse(it.hasNext());
  }

  @Test
  public void testNonTerminatingBodyBafB() {
    // Disable "bb" Pack for a direct translation from Jimple->Baf
    Options.v().setPhaseOption("bb", "enabled:false");
    // Translate the body to Baf
    final BafBody body = Baf.v().newBody(createB());
    if (DEBUG_PRINT) {
      System.out.println("[testNonTerminatingBodyBafB] BafBody = " + body);
    }

    // Run SimpleLiveLocals
    final ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
    final LiveLocals ll = new SimpleLiveLocals(graph);
    if (DEBUG_PRINT) {
      PhaseDumper.v().dumpGraph(graph, true);
    }

    // Check LiveLocals result
    final Local thisLoc = body.getThisLocal();
    final Local paramLoc = body.getParameterLocal(0);
    final Chain<Unit> units = body.getUnits().getNonPatchingChain();
    Assert.assertEquals(9, units.size());

    final Iterator<Unit> it = units.iterator();
    // this := @this
    check(ll, it.next(), Arrays.asList(), Arrays.asList(thisLoc));
    // parameter0 := @parameter0
    check(ll, it.next(), Arrays.asList(thisLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r parameter0
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // ifnonnull label4
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r this
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // load.r parameter0
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // invoke run(Object)
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // goto label1
    check(ll, it.next(), Arrays.asList(thisLoc, paramLoc), Arrays.asList(thisLoc, paramLoc));
    // return
    check(ll, it.next(), Arrays.asList(), Arrays.asList());

    Assert.assertFalse(it.hasNext());
  }

  private static void check(LiveLocals ll, Unit u, Collection<Local> expectBefore, Collection<Local> expectAfter) {
    Assert.assertEquals("before(" + u + ")", new HashSet<>(expectBefore), new HashSet<>(ll.getLiveLocalsBefore(u)));
    Assert.assertEquals("after(" + u + ")", new HashSet<>(expectAfter), new HashSet<>(ll.getLiveLocalsAfter(u)));
  }
}
