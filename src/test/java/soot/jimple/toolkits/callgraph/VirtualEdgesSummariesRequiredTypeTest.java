package soot.jimple.toolkits.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import soot.G;
import soot.Kind;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdge;

/**
 * Tests for the {@code requiredType} attribute support in {@link VirtualEdgesSummaries}. This attribute lets
 * {@code virtualedges.xml} declaratively specify the type a reaching value must be assignable to for a given
 * {@link Kind} of virtual call site (e.g. {@code Runnable} for {@link Kind#THREAD}/{@link Kind#EXECUTOR},
 * {@code Handler} for {@link Kind#HANDLER}, {@code AsyncTask} for {@link Kind#ASYNCTASK}), instead of that
 * information being hardcoded in {@link OnFlyCallGraphBuilder#skipSite}.
 */
public class VirtualEdgesSummariesRequiredTypeTest {

  private static final String XML_HEADER = "<?xml version=\"1.0\"?><virtualedges>";
  private static final String XML_FOOTER = "</virtualedges>";

  @Before
  public void resetSoot() {
    // Soot keeps global state (Scene, RefType numbering) in singletons; reset before each test
    // so that RefType.v(...) calls do not interfere across test methods.
    G.reset();
  }

  private VirtualEdgesSummaries load(String edgesXml) throws Exception {
    String xml = XML_HEADER + edgesXml + XML_FOOTER;
    return new TestableVirtualEdgesSummaries(xml);
  }

  @Test
  public void parsesRequiredTypeForExecutor() throws Exception {
    VirtualEdgesSummaries summaries = load(
            "<edge type=\"EXECUTOR\" requiredType=\"java.lang.Runnable\">"
                    + "<source declaringclass=\"java.util.concurrent.Executor\" invoketype=\"instance\" "
                    + "subsignature=\"void execute(java.lang.Runnable)\"/>"
                    + "<targets><direct index=\"0\" subsignature=\"void run()\" target-position=\"argument\"/></targets>"
                    + "</edge>");

    RefType required = summaries.getRequiredType(Kind.EXECUTOR);
    assertNotNull("EXECUTOR should have a required type declared", required);
    assertEquals("java.lang.Runnable", required.getClassName());
  }

  @Test
  public void parsesRequiredTypeForHandler() throws Exception {
    VirtualEdgesSummaries summaries = load(
            "<edge type=\"HANDLER\" requiredType=\"android.os.Handler\">"
                    + "<source declaringclass=\"android.os.Handler\" invoketype=\"instance\" "
                    + "subsignature=\"boolean sendEmptyMessage(int)\"/>"
                    + "<targets><direct subsignature=\"void handleMessage(android.os.Message)\" "
                    + "target-position=\"base\"/></targets>"
                    + "</edge>");

    RefType required = summaries.getRequiredType(Kind.HANDLER);
    assertNotNull("HANDLER should have a required type declared", required);
    assertEquals("android.os.Handler", required.getClassName());
  }

  @Test
  public void parsesRequiredTypeForAsyncTask() throws Exception {
    VirtualEdgesSummaries summaries = load(
            "<edge type=\"ASYNCTASK\" requiredType=\"android.os.AsyncTask\">"
                    + "<source declaringclass=\"android.os.AsyncTask\" invoketype=\"instance\" "
                    + "subsignature=\"android.os.AsyncTask execute(java.lang.Object[])\"/>"
                    + "<targets><direct subsignature=\"java.lang.Object doInBackground(java.lang.Object[])\" "
                    + "target-position=\"base\"/></targets>"
                    + "</edge>");

    RefType required = summaries.getRequiredType(Kind.ASYNCTASK);
    assertNotNull("ASYNCTASK should have a required type declared", required);
    assertEquals("android.os.AsyncTask", required.getClassName());
  }

  @Test
  public void parsesRequiredTypeForThread() throws Exception {
    VirtualEdgesSummaries summaries = load(
            "<edge type=\"THREAD\" requiredType=\"java.lang.Runnable\">"
                    + "<source declaringclass=\"java.lang.Thread\" invoketype=\"instance\" subsignature=\"void start()\"/>"
                    + "<targets><direct subsignature=\"void run()\" target-position=\"base\"/></targets>"
                    + "</edge>");

    RefType required = summaries.getRequiredType(Kind.THREAD);
    assertNotNull("THREAD should have a required type declared", required);
    assertEquals("java.lang.Runnable", required.getClassName());
  }

  @Test
  public void returnsNullWhenNoRequiredTypeIsDeclared() throws Exception {
    // An edge with no requiredType attribute (e.g. a plain GENERIC_FAKE summary) must not
    // register any entry in the required-type map.
    VirtualEdgesSummaries summaries = load(
            "<edge>"
                    + "<source declaringclass=\"java.util.Iterator\" invoketype=\"instance\" "
                    + "subsignature=\"java.lang.Object next()\"/>"
                    + "<targets><direct subsignature=\"void accept(java.lang.Object)\" target-position=\"base\"/></targets>"
                    + "</edge>");

    assertNull("Kinds without a declared requiredType must resolve to null",
            summaries.getRequiredType(Kind.GENERIC_FAKE));
  }

  @Test
  public void requiredTypeDoesNotAffectEdgeLookup() throws Exception {
    Scene.v().addBasicClass("java.util.concurrent.Executor", SootClass.SIGNATURES);
    Scene.v().loadNecessaryClasses();
    // Sanity check: adding requiredType parsing must not break the existing edge lookup mechanism.
    VirtualEdgesSummaries summaries = load(
            "<edge type=\"EXECUTOR\" requiredType=\"java.lang.Runnable\">"
                    + "<source declaringclass=\"java.util.concurrent.Executor\" invoketype=\"instance\" "
                    + "subsignature=\"void execute(java.lang.Runnable)\"/>"
                    + "<targets><direct index=\"0\" subsignature=\"void run()\" target-position=\"argument\"/></targets>"
                    + "</edge>");

    VirtualEdge edge = summaries.getVirtualEdgesMatchingSubSig(RefType.v("java.util.concurrent.Executor"),
            new soot.MethodSubSignature(Scene.v().getSubSigNumberer().findOrAdd("void execute(java.lang.Runnable)")));

    assertNotNull("The edge itself should still be resolvable by its subsignature", edge);
    assertEquals(Kind.EXECUTOR, edge.getEdgeType());
    assertTrue("The edge summaries collection must not be empty", !summaries.isEmpty());
  }

  /** Exposes {@code loadSummaries} for testing without needing a real {@code virtualedges.xml} file on disk. */
  private static final class TestableVirtualEdgesSummaries extends VirtualEdgesSummaries {
    TestableVirtualEdgesSummaries(String xml) throws Exception {
      super(java.util.Collections.emptyList());
      loadSummaries(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
  }
}