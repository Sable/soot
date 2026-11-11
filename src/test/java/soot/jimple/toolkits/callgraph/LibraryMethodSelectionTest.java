package soot.jimple.toolkits.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Soot contributors
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import soot.Body;
import soot.G;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.jimple.spark.geom.geomPA.GeomPointsTo;
import soot.jimple.spark.geom.helper.GeomEvaluator;
import soot.options.Options;
import soot.options.SparkOptions;

/** Tests library method selection as an exception to existing call graph restrictions. */
public class LibraryMethodSelectionTest {
  private SootClass api;
  private SootMethod selected;

  @Before
  public void setUp() {
    G.reset();
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    Scene.v().loadBasicClasses();
    api = addClass("example.LibraryApi", false);
    selected = addMethod(api, "selected", false);
  }

  @Test
  public void absentSelectionPreservesAppOnlyDispatch() {
    assertNull(resolve(selected, true));
  }

  @Test
  public void absentFilterDoesNotGrantAnAdditionalLibraryPermission() {
    assertFalse(Scene.v().isLibraryMethodAnalysisAllowed(selected));
  }

  @Test
  public void permissionIdentifiesOnlySelectedLibraryMethods() {
    SootMethod other = addMethod(api, "other", false);
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertTrue(Scene.v().isLibraryMethodAnalysisAllowed(selected));
    assertFalse(Scene.v().isLibraryMethodAnalysisAllowed(other));
  }

  @Test
  public void librarySelectionDoesNotClassifyApplicationMethods() {
    SootClass app = addClass("example.SelectedApplication", true);
    SootMethod method = addMethod(app, "entry", true);
    Scene.v().setLibraryMethodFilter(target -> {
      throw new AssertionError("Application methods must retain their existing rules");
    });
    assertFalse(Scene.v().isLibraryMethodAnalysisAllowed(method));
    assertTrue(app.isApplicationClass());
  }

  @Test
  public void resetRemovesAdditionalLibraryPermissions() {
    Scene.v().setLibraryMethodFilter(method -> true);
    assertTrue(Scene.v().isLibraryMethodAnalysisAllowed(selected));
    G.reset();
    assertFalse(Scene.v().hasLibraryMethodFilter());
    assertFalse(Scene.v().isLibraryMethodAnalysisAllowed(selected));
  }

  @Test
  public void absentSelectionPreservesWholeProgramDispatch() {
    assertSame(selected, resolve(selected, false));
  }

  @Test
  public void selectedLibraryMethodIsAnAppOnlyException() {
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertSame(selected, resolve(selected, true));
    assertTrue(api.isLibraryClass());
  }

  @Test
  public void selectionDoesNotEnableOtherMethodsOfTheSameClass() {
    SootMethod other = addMethod(api, "other", false);
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertNull(resolve(other, true));
    assertSame(other, resolve(other, false));
  }

  @Test
  public void clearingSelectionRestoresOriginalDispatch() {
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertSame(selected, resolve(selected, true));
    Scene.v().setLibraryMethodFilter(null);
    assertNull(resolve(selected, true));
    assertSame(selected, resolve(selected, false));
  }

  @Test
  public void selectionUsesTheResolvedInheritedMethod() {
    SootClass child = addClass("example.ChildApi", false);
    child.setSuperclass(api);
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertSame(selected, VirtualCalls.v().resolveNonSpecial(child.getType(), selected.makeRef(), true));
  }

  @Test
  public void selectingParentDoesNotAuthorizeAnOverride() {
    SootClass child = addClass("example.OverridingApi", false);
    child.setSuperclass(api);
    SootMethod override = addMethod(child, selected.getName(), false);
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    assertNull(VirtualCalls.v().resolveNonSpecial(child.getType(), selected.makeRef(), true));
    assertSame(override, VirtualCalls.v().resolveNonSpecial(child.getType(), selected.makeRef(), false));
  }

  @Test
  public void selectedModelContributesCallEdgesAndPointerConstraints() {
    assertSparkBehavior(true, true);
  }

  @Test
  public void selectionDoesNotRestrictWholeProgramAnalysis() {
    assertSparkBehavior(false, true);
  }

  @Test
  public void defaultAppOnlyAnalysisIsUnchanged() {
    assertSparkBehavior(true, false);
  }

  @Test
  public void defaultWholeProgramAnalysisIsUnchanged() {
    assertSparkBehavior(false, false);
  }

  private void assertSparkBehavior(boolean appOnly, boolean installSelection) {
    SootClass app = addClass("example.Application", true);
    SootMethod selectedCallback = addMethod(app, "selectedCallback", true);
    SootMethod otherCallback = addMethod(app, "otherCallback", true);
    SootMethod other = addMethod(api, "other", false);
    Local selectedAllocation = setAllocatingBody(selected, selectedCallback);
    Local otherAllocation = setAllocatingBody(other, otherCallback);
    SootMethod entry = addMethod(app, "entry", true);
    Body body = Jimple.v().newBody(entry);
    entry.setActiveBody(body);
    Local receiver = addLocal(body, "receiver");
    Local selectedResult = addLocal(body, "selectedResult");
    Local otherResult = addLocal(body, "otherResult");
    body.getUnits().add(Jimple.v().newAssignStmt(receiver, Jimple.v().newNewExpr(api.getType())));
    body.getUnits().add(Jimple.v().newAssignStmt(selectedResult,
        Jimple.v().newVirtualInvokeExpr(receiver, selected.makeRef())));
    body.getUnits().add(Jimple.v().newAssignStmt(otherResult,
        Jimple.v().newVirtualInvokeExpr(receiver, other.makeRef())));
    body.getUnits().add(Jimple.v().newReturnVoidStmt());
    if (installSelection) {
      Scene.v().setLibraryMethodFilter(method -> method == selected);
    }
    runSpark(entry, appOnly);
    CallGraph graph = Scene.v().getCallGraph();
    boolean selectedIsReachable = !appOnly || installSelection;
    assertEquals(selectedIsReachable, hasEdge(graph, entry, selected));
    assertEquals(selectedIsReachable, hasEdge(graph, selected, selectedCallback));
    assertEquals(!appOnly, hasEdge(graph, entry, other));
    assertEquals(!appOnly, hasEdge(graph, other, otherCallback));
    if (selectedIsReachable) {
      assertTrue(Scene.v().getPointsToAnalysis().reachingObjects(selectedResult)
          .hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects(selectedAllocation)));
    }
    if (!appOnly) {
      assertTrue(Scene.v().getPointsToAnalysis().reachingObjects(otherResult)
          .hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects(otherAllocation)));
    }
    assertTrue(api.isLibraryClass());
  }

  @Test
  public void unselectedStaticLibraryMethodStillContributesPointerConstraints() {
    SootClass app = addClass("example.StaticApplication", true);
    SootMethod callback = addMethod(app, "callback", true);
    SootMethod ordinary = addMethod(api, "ordinary", true);
    Local allocation = setAllocatingBody(ordinary, callback);
    SootMethod entry = addMethod(app, "entry", true);
    Body body = Jimple.v().newBody(entry);
    entry.setActiveBody(body);
    Local result = addLocal(body, "result");
    body.getUnits().add(Jimple.v().newAssignStmt(result, Jimple.v().newStaticInvokeExpr(ordinary.makeRef())));
    body.getUnits().add(Jimple.v().newReturnVoidStmt());
    Scene.v().setLibraryMethodFilter(method -> method == selected);
    runSpark(entry, true);
    assertTrue(hasEdge(Scene.v().getCallGraph(), entry, ordinary));
    assertFalse(hasEdge(Scene.v().getCallGraph(), ordinary, callback));
    assertTrue(Scene.v().getPointsToAnalysis().reachingObjects(result)
        .hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects(allocation)));
  }

  @Test
  public void selectionDoesNotRestrictPreJimplification() {
    G.reset();
    SootClass library = new SootClass("example.PreloadedLibrary", Modifier.PUBLIC);
    Scene.v().addClass(library);
    library.setLibraryClass();
    SootMethod chosen = addMethod(library, "chosen", true);
    SootMethod ordinary = addMethod(library, "ordinary", true);
    Scene.v().setLibraryMethodFilter(method -> method == chosen);
    new ContextInsensitiveBuilder().preJimplify();
    assertTrue(chosen.hasActiveBody());
    assertTrue(ordinary.hasActiveBody());
    assertTrue(library.isLibraryClass());
  }

  @Test
  public void selectionDoesNotRestrictGeometricEvaluation() {
    Scene.v().setLibraryMethodFilter(method -> false);
    GeomPointsTo pointsTo = new GeomPointsTo(new SparkOptions(Collections.emptyMap())) {
      @Override
      public Set<SootMethod> getAllReachableMethods() {
        return Collections.singleton(selected);
      }

      @Override
      public boolean isValidMethod(SootMethod method) {
        return false;
      }
    };
    pointsTo.ps = new PrintStream(new ByteArrayOutputStream());
    new GeomEvaluator(pointsTo, pointsTo.ps).checkCastsSafety();
    assertTrue(selected.hasActiveBody());
  }

  private SootMethod resolve(SootMethod method, boolean appOnly) {
    return VirtualCalls.v().resolveNonSpecial(api.getType(), method.makeRef(), appOnly);
  }

  private SootClass addClass(String name, boolean application) {
    SootClass result = new SootClass(name, Modifier.PUBLIC);
    result.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
    Scene.v().addClass(result);
    if (application) {
      result.setApplicationClass();
    } else {
      result.setLibraryClass();
    }
    return result;
  }

  private SootMethod addMethod(SootClass owner, String name, boolean isStatic) {
    SootMethod result = new SootMethod(name, Collections.emptyList(), VoidType.v(),
        Modifier.PUBLIC | (isStatic ? Modifier.STATIC : 0));
    owner.addMethod(result);
    result.setSource((method, phase) -> {
      Body body = newMethodBody(method);
      body.getUnits().add(Jimple.v().newReturnVoidStmt());
      return body;
    });
    return result;
  }

  private Local setAllocatingBody(SootMethod method, SootMethod callback) {
    method.setReturnType(api.getType());
    Body body = newMethodBody(method);
    Local allocation = addLocal(body, "created");
    body.getUnits().add(Jimple.v().newAssignStmt(allocation, Jimple.v().newNewExpr(api.getType())));
    if (callback != null) {
      body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(callback.makeRef())));
    }
    body.getUnits().add(Jimple.v().newReturnStmt(allocation));
    method.setSource((target, phase) -> body);
    return allocation;
  }

  private Body newMethodBody(SootMethod method) {
    Body body = Jimple.v().newBody(method);
    if (!method.isStatic()) {
      Local receiver = Jimple.v().newLocal("this", method.getDeclaringClass().getType());
      body.getLocals().add(receiver);
      body.getUnits().add(Jimple.v().newIdentityStmt(receiver,
          Jimple.v().newThisRef(method.getDeclaringClass().getType())));
    }
    return body;
  }

  private Local addLocal(Body body, String name) {
    Local result = Jimple.v().newLocal(name, api.getType());
    body.getLocals().add(result);
    return result;
  }

  private void runSpark(SootMethod entry, boolean appOnly) {
    Scene.v().setEntryPoints(Collections.singletonList(entry));
    Map<String, String> options = new HashMap<>(soot.PhaseOptions.v().getPhaseOptions("cg.spark"));
    options.put("enabled", "true");
    options.put("on-fly-cg", "true");
    options.put("apponly", Boolean.toString(appOnly));
    options.put("simulate-natives", "false");
    SparkTransformer.v().transform("cg.spark", options);
  }

  private boolean hasEdge(CallGraph graph, SootMethod source, SootMethod target) {
    for (Iterator<Edge> edges = graph.edgesOutOf(source); edges.hasNext();) {
      if (edges.next().tgt() == target) {
        return true;
      }
    }
    return false;
  }
}
