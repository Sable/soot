package soot.jimple.spark;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002 Ondrej Lhotak
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

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;
import soot.Local;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.ReachingTypeDumper;
import soot.jimple.Stmt;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.jimple.spark.geom.geomPA.GeomPointsTo;
import soot.jimple.spark.ondemand.DemandCSPointsTo;
import soot.jimple.spark.pag.AllocDotField;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.PAG2HTML;
import soot.jimple.spark.pag.PAGDumper;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.solver.EBBCollapser;
import soot.jimple.spark.solver.PropAlias;
import soot.jimple.spark.solver.PropCycle;
import soot.jimple.spark.solver.PropIter;
import soot.jimple.spark.solver.PropMerge;
import soot.jimple.spark.solver.PropWorklist;
import soot.jimple.spark.solver.Propagator;
import soot.jimple.spark.solver.SCCCollapser;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.options.SparkOptions;
import soot.tagkit.Host;
import soot.tagkit.StringTag;
import soot.tagkit.Tag;

/**
 * Main entry point for Spark.
 * 
 * @author Ondrej Lhotak
 */
public class SparkTransformer extends SceneTransformer {
  private static final Logger logger = LoggerFactory.getLogger(SparkTransformer.class);

  /** Constructor for client subclasses registered as custom SPARK phases. */
  protected SparkTransformer() {
  }

  public SparkTransformer(Singletons.Global g) {
    this();
  }

  public static SparkTransformer v() {
    return G.v().soot_jimple_spark_SparkTransformer();
  }

  /** Creates the builder for this analysis. Override to customize construction while retaining the SPARK pipeline. */
  protected ContextInsensitiveBuilder createContextInsensitiveBuilder() {
    return new ContextInsensitiveBuilder();
  }

  protected void internalTransform(String phaseName, Map<String, String> options) {
    SparkOptions opts = new SparkOptions(options);
    final String output_dir = SourceLocator.v().getOutputDir();

    // Build pointer assignment graph
    ContextInsensitiveBuilder b = createContextInsensitiveBuilder();
    if (opts.pre_jimplify()) {
      b.preJimplify();
    }
    if (opts.force_gc()) {
      doGC();
    }
    Date startBuild = new Date();
    final PAG pag = b.setup(opts);
    b.build();
    Date endBuild = new Date();
    reportTime("Pointer Assignment Graph", startBuild, endBuild);
    if (opts.force_gc()) {
      doGC();
    }

    // Build type masks
    Date startTM = new Date();
    pag.getTypeManager().makeTypeMask();
    Date endTM = new Date();
    reportTime("Type masks", startTM, endTM);
    if (opts.force_gc()) {
      doGC();
    }

    if (opts.verbose()) {
      logger.debug("VarNodes: " + pag.getVarNodeNumberer().size());
      logger.debug("FieldRefNodes: " + pag.getFieldRefNodeNumberer().size());
      logger.debug("AllocNodes: " + pag.getAllocNodeNumberer().size());
    }

    // Simplify pag
    Date startSimplify = new Date();

    // We only simplify if on_fly_cg is false. But, if vta is true, it
    // overrides on_fly_cg, so we can still simplify. Something to handle
    // these option interdependencies more cleanly would be nice...
    if ((opts.simplify_sccs() && !opts.on_fly_cg()) || opts.vta()) {
      new SCCCollapser(pag, opts.ignore_types_for_sccs()).collapse();
    }
    if (opts.simplify_offline() && !opts.on_fly_cg()) {
      new EBBCollapser(pag).collapse();
    }
    if (true || opts.simplify_sccs() || opts.vta() || opts.simplify_offline()) {
      pag.cleanUpMerges();
    }
    Date endSimplify = new Date();
    reportTime("Pointer Graph simplified", startSimplify, endSimplify);
    if (opts.force_gc()) {
      doGC();
    }

    // Dump pag
    PAGDumper dumper = null;
    if (opts.dump_pag() || opts.dump_solution()) {
      dumper = new PAGDumper(pag, output_dir);
    }
    if (opts.dump_pag()) {
      dumper.dump();
    }

    // Propagate
    Date startProp = new Date();
    propagatePAG(opts, pag);
    Date endProp = new Date();
    reportTime("Propagation", startProp, endProp);
    reportTime("Solution found", startSimplify, endProp);

    if (opts.force_gc()) {
      doGC();
    }

    if (!opts.on_fly_cg() || opts.vta()) {
      CallGraphBuilder cgb = new CallGraphBuilder(pag);
      cgb.build();
    }

    if (opts.verbose()) {
      logger.debug("[Spark] Number of reachable methods: " + Scene.v().getReachableMethods().size());
    }

    if (opts.set_mass()) {
      findSetMass(pag);
    }

    if (opts.dump_answer()) {
      new ReachingTypeDumper(pag, output_dir).dump();
    }
    if (opts.dump_solution()) {
      dumper.dumpPointsToSets();
    }
    if (opts.dump_html()) {
      new PAG2HTML(pag, output_dir).dump();
    }
    Scene.v().setPointsToAnalysis(pag);
    if (opts.add_tags()) {
      addTags(pag);
    }

    if (opts.geom_pta()) {
      if (opts.simplify_offline() || opts.simplify_sccs()) {
        logger.debug("" + "Please turn off the simplify-offline and simplify-sccs to run the geometric points-to analysis");
        logger.debug("Now, we keep the SPARK result for querying.");
      } else {
        // We perform the geometric points-to analysis
        GeomPointsTo geomPTA = (GeomPointsTo) pag;
        geomPTA.parametrize(endProp.getTime() - startSimplify.getTime());
        geomPTA.solve();
      }
    }

    if (opts.cs_demand()) {
      // replace by demand-driven refinement-based context-sensitive analysis
      Date startOnDemand = new Date();
      PointsToAnalysis onDemandAnalysis = DemandCSPointsTo.makeWithBudget(opts.traversal(), opts.passes(), opts.lazy_pts());
      Date endOndemand = new Date();
      reportTime("Initialized on-demand refinement-based context-sensitive analysis", startOnDemand, endOndemand);
      Scene.v().setPointsToAnalysis(onDemandAnalysis);
    }
  }

  protected void propagatePAG(SparkOptions opts, final PAG pag) {
    Propagator propagator = null;
    switch (opts.propagator()) {
      case SparkOptions.propagator_iter:
        propagator = new PropIter(pag);
        break;
      case SparkOptions.propagator_worklist:
        propagator = new PropWorklist(pag);
        break;
      case SparkOptions.propagator_cycle:
        propagator = new PropCycle(pag);
        break;
      case SparkOptions.propagator_merge:
        propagator = new PropMerge(pag);
        break;
      case SparkOptions.propagator_alias:
        propagator = new PropAlias(pag);
        break;
      case SparkOptions.propagator_none:
        break;
      default:
        throw new RuntimeException();
    }

    if (propagator != null) {
      propagator.propagate();
    }
  }

  protected void addTags(PAG pag) {
    final Tag unknown = new StringTag("Untagged Spark node");
    final Map<Node, Tag> nodeToTag = pag.getNodeTags();
    for (final SootClass c : Scene.v().getClasses()) {
      for (final SootMethod m : c.getMethods()) {
        if (!m.isConcrete() || !m.hasActiveBody()) {
          continue;
        }
        for (final Unit u : m.getActiveBody().getUnits()) {
          final Stmt s = (Stmt) u;
          if (s instanceof DefinitionStmt) {
            Value lhs = ((DefinitionStmt) s).getLeftOp();
            VarNode v = null;
            if (lhs instanceof Local) {
              v = pag.findLocalVarNode(lhs);
            } else if (lhs instanceof FieldRef) {
              v = pag.findGlobalVarNode(((FieldRef) lhs).getField());
            }
            if (v != null) {
              PointsToSetInternal p2set = v.getP2Set();
              p2set.forall(new P2SetVisitor() {
                public final void visit(Node n) {
                  addTag(s, n, nodeToTag, unknown);
                }
              });
              Node[] simpleSources = pag.simpleInvLookup(v);
              for (Node element : simpleSources) {
                addTag(s, element, nodeToTag, unknown);
              }
              simpleSources = pag.allocInvLookup(v);
              for (Node element : simpleSources) {
                addTag(s, element, nodeToTag, unknown);
              }
              simpleSources = pag.loadInvLookup(v);
              for (Node element : simpleSources) {
                addTag(s, element, nodeToTag, unknown);
              }
            }
          }
        }
      }
    }
  }

  protected static void reportTime(String desc, Date start, Date end) {
    long time = end.getTime() - start.getTime();
    logger.debug("[Spark] " + desc + " in " + time / 1000 + "." + (time / 100) % 10 + " seconds.");
  }

  protected static void doGC() {
    // Do 5 times because the garbage collector doesn't seem to always collect
    // everything on the first try.
    System.gc();
    System.gc();
    System.gc();
    System.gc();
    System.gc();
  }

  protected void addTag(Host h, Node n, Map<Node, Tag> nodeToTag, Tag unknown) {
    if (nodeToTag.containsKey(n)) {
      h.addTag(nodeToTag.get(n));
    } else {
      h.addTag(unknown);
    }
  }

  protected void findSetMass(PAG pag) {
    int mass = 0;
    int varMass = 0;
    int adfs = 0;
    int scalars = 0;

    for (final VarNode v : pag.getVarNodeNumberer()) {
      scalars++;
      PointsToSetInternal set = v.getP2Set();
      if (set != null) {
        mass += set.size();
      }
      if (set != null) {
        varMass += set.size();
      }
    }
    for (final AllocNode an : pag.allocSources()) {
      for (final AllocDotField adf : an.getFields()) {
        PointsToSetInternal set = adf.getP2Set();
        if (set != null) {
          mass += set.size();
        }
        if (set != null && set.size() > 0) {
          adfs++;
        }
      }
    }
    logger.debug("Set mass: " + mass);
    logger.debug("Variable mass: " + varMass);
    logger.debug("Scalars: " + scalars);
    logger.debug("adfs: " + adfs);
    // Compute points-to set sizes of dereference sites BEFORE
    // trimming sets by declared type
    int[] deRefCounts = new int[30001];
    for (VarNode v : pag.getDereferences()) {
      PointsToSetInternal set = v.getP2Set();
      int size = 0;
      if (set != null) {
        size = set.size();
      }
      deRefCounts[size]++;
    }
    int total = 0;
    for (int element : deRefCounts) {
      total += element;
    }
    logger.debug("Dereference counts BEFORE trimming (total = " + total + "):");
    for (int i = 0; i < deRefCounts.length; i++) {
      if (deRefCounts[i] > 0) {
        logger.debug("" + i + " " + deRefCounts[i] + " " + (deRefCounts[i] * 100.0 / total) + "%");
      }
    }
  }
}
