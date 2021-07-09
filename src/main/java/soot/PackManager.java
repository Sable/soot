package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 - 2004 Ondrej Lhotak
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

import heros.solver.CountingThreadPoolExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.baf.Baf;
import soot.baf.BafASMBackend;
import soot.baf.BafBody;
import soot.baf.toolkits.base.LoadStoreOptimizer;
import soot.baf.toolkits.base.PeepholeOptimizer;
import soot.baf.toolkits.base.StoreChainOptimizer;
import soot.dava.Dava;
import soot.dava.DavaBody;
import soot.dava.DavaBuildFile;
import soot.dava.DavaPrinter;
import soot.dava.DavaStaticBlockCleaner;
import soot.dava.toolkits.base.AST.interProcedural.InterProceduralAnalyses;
import soot.dava.toolkits.base.AST.transformations.RemoveEmptyBodyDefaultConstructor;
import soot.dava.toolkits.base.AST.transformations.VoidReturnRemover;
import soot.dava.toolkits.base.misc.PackageNamer;
import soot.dava.toolkits.base.misc.ThrowFinder;
import soot.grimp.Grimp;
import soot.grimp.GrimpBody;
import soot.grimp.toolkits.base.ConstructorFolder;
import soot.jimple.JimpleBody;
import soot.jimple.paddle.PaddleHook;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.fieldrw.FieldTagAggregator;
import soot.jimple.spark.fieldrw.FieldTagger;
import soot.jimple.toolkits.annotation.AvailExprTagger;
import soot.jimple.toolkits.annotation.DominatorsTagger;
import soot.jimple.toolkits.annotation.LineNumberAdder;
import soot.jimple.toolkits.annotation.arraycheck.ArrayBoundsChecker;
import soot.jimple.toolkits.annotation.arraycheck.RectangularArrayFinder;
import soot.jimple.toolkits.annotation.callgraph.CallGraphGrapher;
import soot.jimple.toolkits.annotation.callgraph.CallGraphTagger;
import soot.jimple.toolkits.annotation.defs.ReachingDefsTagger;
import soot.jimple.toolkits.annotation.fields.UnreachableFieldsTagger;
import soot.jimple.toolkits.annotation.liveness.LiveVarsTagger;
import soot.jimple.toolkits.annotation.logic.LoopInvariantFinder;
import soot.jimple.toolkits.annotation.methods.UnreachableMethodsTagger;
import soot.jimple.toolkits.annotation.nullcheck.NullCheckEliminator;
import soot.jimple.toolkits.annotation.nullcheck.NullPointerChecker;
import soot.jimple.toolkits.annotation.nullcheck.NullPointerColorer;
import soot.jimple.toolkits.annotation.parity.ParityTagger;
import soot.jimple.toolkits.annotation.profiling.ProfilingGenerator;
import soot.jimple.toolkits.annotation.purity.PurityAnalysis;
import soot.jimple.toolkits.annotation.qualifiers.TightestQualifiersTagger;
import soot.jimple.toolkits.annotation.tags.ArrayNullTagAggregator;
import soot.jimple.toolkits.base.Aggregator;
import soot.jimple.toolkits.base.RenameDuplicatedClasses;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraphPack;
import soot.jimple.toolkits.callgraph.UnreachableMethodTransformer;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.StaticMethodBinder;
import soot.jimple.toolkits.pointer.CastCheckEliminatorDumper;
import soot.jimple.toolkits.pointer.DependenceTagAggregator;
import soot.jimple.toolkits.pointer.ParameterAliasTagger;
import soot.jimple.toolkits.pointer.SideEffectTagger;
import soot.jimple.toolkits.reflection.ConstantInvokeMethodBaseTransformer;
import soot.jimple.toolkits.scalar.CommonSubexpressionEliminator;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.EmptySwitchEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.pre.BusyCodeMotion;
import soot.jimple.toolkits.scalar.pre.LazyCodeMotion;
import soot.jimple.toolkits.thread.mhp.MhpTransformer;
import soot.jimple.toolkits.thread.synchronization.LockAllocator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.options.Options;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.shimple.ShimpleTransformer;
import soot.shimple.toolkits.scalar.SConstantPropagatorAndFolder;
import soot.sootify.TemplatePrinter;
import soot.tagkit.InnerClassTagAggregator;
import soot.tagkit.LineNumberTagAggregator;
import soot.toDex.DexPrinter;
import soot.toolkits.exceptions.DuplicateCatchAllTrapRemover;
import soot.toolkits.exceptions.TrapTightener;
import soot.toolkits.graph.interaction.InteractionHandler;
import soot.toolkits.scalar.ConstantInitializerToTagTransformer;
import soot.toolkits.scalar.ConstantValueToInitializerTransformer;
import soot.toolkits.scalar.LocalPacker;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.SharedInitializationLocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.EscapedWriter;
import soot.util.JasminOutputStream;
import soot.util.PhaseDumper;
import soot.xml.TagCollector;
import soot.xml.XMLPrinter;

/**
 * Manages the Packs containing the various phases and their options.
 */
public class PackManager {
  private static final Logger logger = LoggerFactory.getLogger(PackManager.class);
  public static boolean DEBUG = false;

  private final Map<String, Pack> packNameToPack = new HashMap<String, Pack>();
  private final List<Pack> packList = new LinkedList<Pack>();
  private boolean onlyStandardPacks = false;
  private JarOutputStream jarFile = null;
  protected DexPrinter dexPrinter = null;

  public PackManager(Singletons.Global g) {
    PhaseOptions.v().setPackManager(this);
    init();
  }

  public static PackManager v() {
    return G.v().soot_PackManager();
  }

  public boolean onlyStandardPacks() {
    return onlyStandardPacks;
  }

  void notifyAddPack() {
    onlyStandardPacks = false;
  }

  private void init() {
    Pack p;

    // Jimple body creation
    addPack(p = new JimpleBodyPack());
    {
      p.add(new Transform("jb.tt", TrapTightener.v()));
      p.add(new Transform("jb.dtr", DuplicateCatchAllTrapRemover.v()));
      p.add(new Transform("jb.ese", EmptySwitchEliminator.v()));
      p.add(new Transform("jb.ls", LocalSplitter.v()));
      p.add(new Transform("jb.sils", SharedInitializationLocalSplitter.v()));
      p.add(new Transform("jb.a", Aggregator.v()));
      p.add(new Transform("jb.ule", UnusedLocalEliminator.v()));
      p.add(new Transform("jb.tr", TypeAssigner.v()));
      p.add(new Transform("jb.ulp", LocalPacker.v()));
      p.add(new Transform("jb.lns", LocalNameStandardizer.v()));
      p.add(new Transform("jb.cp", CopyPropagator.v()));
      p.add(new Transform("jb.dae", DeadAssignmentEliminator.v()));
      p.add(new Transform("jb.cp-ule", UnusedLocalEliminator.v()));
      p.add(new Transform("jb.lp", LocalPacker.v()));
      p.add(new Transform("jb.ne", NopEliminator.v()));
      p.add(new Transform("jb.uce", UnreachableCodeEliminator.v()));
      p.add(new Transform("jb.cbf", ConditionalBranchFolder.v()));
    }

    // Java to Jimple - Jimple body creation
    addPack(p = new JavaToJimpleBodyPack());
    {
      p.add(new Transform("jj.ls", LocalSplitter.v()));
      p.add(new Transform("jj.sils", SharedInitializationLocalSplitter.v()));
      p.add(new Transform("jj.a", Aggregator.v()));
      p.add(new Transform("jj.ule", UnusedLocalEliminator.v()));
      p.add(new Transform("jj.ne", NopEliminator.v()));
      p.add(new Transform("jj.tr", TypeAssigner.v()));
      // p.add(new Transform("jj.ct", CondTransformer.v()));
      p.add(new Transform("jj.ulp", LocalPacker.v()));
      p.add(new Transform("jj.lns", LocalNameStandardizer.v()));
      p.add(new Transform("jj.cp", CopyPropagator.v()));
      p.add(new Transform("jj.dae", DeadAssignmentEliminator.v()));
      p.add(new Transform("jj.cp-ule", UnusedLocalEliminator.v()));
      p.add(new Transform("jj.lp", LocalPacker.v()));
      p.add(new Transform("jj.uce", UnreachableCodeEliminator.v()));

    }

    // Whole-Jimple Pre-processing Pack
    addPack(p = new ScenePack("wjpp"));
    {
      p.add(new Transform("wjpp.cimbt", ConstantInvokeMethodBaseTransformer.v()));
    }

    // Whole-Shimple Pre-processing Pack
    addPack(p = new ScenePack("wspp"));

    // Call graph pack
    addPack(p = new CallGraphPack("cg"));
    {
      p.add(new Transform("cg.cha", CHATransformer.v()));
      p.add(new Transform("cg.spark", SparkTransformer.v()));
      p.add(new Transform("cg.paddle", PaddleHook.v()));
    }

    // Whole-Shimple transformation pack
    addPack(p = new ScenePack("wstp"));

    // Whole-Shimple Optimization pack
    addPack(p = new ScenePack("wsop"));

    // Whole-Jimple transformation pack
    addPack(p = new ScenePack("wjtp"));
    {
      p.add(new Transform("wjtp.mhp", MhpTransformer.v()));
      p.add(new Transform("wjtp.tn", LockAllocator.v()));
      p.add(new Transform("wjtp.rdc", RenameDuplicatedClasses.v()));
    }

    // Whole-Jimple Optimization pack
    addPack(p = new ScenePack("wjop"));
    {
      p.add(new Transform("wjop.smb", StaticMethodBinder.v()));
      p.add(new Transform("wjop.si", StaticInliner.v()));
    }

    // Give another chance to do Whole-Jimple transformation
    // The RectangularArrayFinder will be put into this package.
    addPack(p = new ScenePack("wjap"));
    {
      p.add(new Transform("wjap.ra", RectangularArrayFinder.v()));
      p.add(new Transform("wjap.umt", UnreachableMethodsTagger.v()));
      p.add(new Transform("wjap.uft", UnreachableFieldsTagger.v()));
      p.add(new Transform("wjap.tqt", TightestQualifiersTagger.v()));
      p.add(new Transform("wjap.cgg", CallGraphGrapher.v()));
      p.add(new Transform("wjap.purity", PurityAnalysis.v())); // [AM]
      p.add(new Transform("wjap.abc", ArrayBoundsChecker.v()));
    }

    // Shimple pack
    addPack(p = new BodyPack(Shimple.PHASE));

    // Shimple transformation pack
    addPack(p = new BodyPack("stp"));

    // Shimple optimization pack
    addPack(p = new BodyPack("sop"));
    {
      p.add(new Transform("sop.cpf", SConstantPropagatorAndFolder.v()));
    }

    // Jimple transformation pack
    addPack(p = new BodyPack("jtp"));

    // Jimple optimization pack
    addPack(p = new BodyPack("jop"));
    {
      p.add(new Transform("jop.cse", CommonSubexpressionEliminator.v()));
      p.add(new Transform("jop.bcm", BusyCodeMotion.v()));
      p.add(new Transform("jop.lcm", LazyCodeMotion.v()));
      p.add(new Transform("jop.cp", CopyPropagator.v()));
      p.add(new Transform("jop.cpf", ConstantPropagatorAndFolder.v()));
      p.add(new Transform("jop.cbf", ConditionalBranchFolder.v()));
      p.add(new Transform("jop.dae", DeadAssignmentEliminator.v()));
      p.add(new Transform("jop.nce", new NullCheckEliminator()));
      p.add(new Transform("jop.uce1", UnreachableCodeEliminator.v()));
      p.add(new Transform("jop.ubf1", UnconditionalBranchFolder.v()));
      p.add(new Transform("jop.uce2", UnreachableCodeEliminator.v()));
      p.add(new Transform("jop.ubf2", UnconditionalBranchFolder.v()));
      p.add(new Transform("jop.ule", UnusedLocalEliminator.v()));
    }

    // Jimple annotation pack
    addPack(p = new BodyPack("jap"));
    {
      p.add(new Transform("jap.npc", NullPointerChecker.v()));
      p.add(new Transform("jap.npcolorer", NullPointerColorer.v()));
      p.add(new Transform("jap.profiling", ProfilingGenerator.v()));
      p.add(new Transform("jap.sea", SideEffectTagger.v()));
      p.add(new Transform("jap.fieldrw", FieldTagger.v()));
      p.add(new Transform("jap.cgtagger", CallGraphTagger.v()));
      p.add(new Transform("jap.parity", ParityTagger.v()));
      p.add(new Transform("jap.pat", ParameterAliasTagger.v()));
      p.add(new Transform("jap.rdtagger", ReachingDefsTagger.v()));
      p.add(new Transform("jap.lvtagger", LiveVarsTagger.v()));
      p.add(new Transform("jap.che", CastCheckEliminatorDumper.v()));
      p.add(new Transform("jap.umt", new UnreachableMethodTransformer()));
      p.add(new Transform("jap.lit", LoopInvariantFinder.v()));
      p.add(new Transform("jap.aet", AvailExprTagger.v()));
      p.add(new Transform("jap.dmt", DominatorsTagger.v()));

    }

    // CFG Viewer
    // addPack(p = new BodyPack("cfg"));
    // {
    // p.add(new Transform("cfg.output", CFGPrinter.v()));
    // }

    // Grimp body creation
    addPack(p = new BodyPack("gb"));
    {
      p.add(new Transform("gb.a1", Aggregator.v()));
      p.add(new Transform("gb.cf", ConstructorFolder.v()));
      p.add(new Transform("gb.a2", Aggregator.v()));
      p.add(new Transform("gb.ule", UnusedLocalEliminator.v()));
    }

    // Grimp optimization pack
    addPack(p = new BodyPack("gop"));

    // Baf body creation
    addPack(p = new BodyPack("bb"));
    {
      p.add(new Transform("bb.lso", LoadStoreOptimizer.v()));
      p.add(new Transform("bb.pho", PeepholeOptimizer.v()));
      p.add(new Transform("bb.ule", UnusedLocalEliminator.v()));
      p.add(new Transform("bb.lp", LocalPacker.v()));
      p.add(new Transform("bb.sco", StoreChainOptimizer.v()));
      p.add(new Transform("bb.ne", NopEliminator.v()));
    }

    // Baf optimization pack
    addPack(p = new BodyPack("bop"));

    // Code attribute tag aggregation pack
    addPack(p = new BodyPack("tag"));
    {
      p.add(new Transform("tag.ln", LineNumberTagAggregator.v()));
      p.add(new Transform("tag.an", ArrayNullTagAggregator.v()));
      p.add(new Transform("tag.dep", DependenceTagAggregator.v()));
      p.add(new Transform("tag.fieldrw", FieldTagAggregator.v()));
    }

    // Dummy Dava Phase
    /*
     * Nomair A. Naeem 13th Feb 2006 Added so that Dava Options can be added as phase options rather than main soot options
     * since they only make sense when decompiling The db phase options are added in soot_options.xml
     */
    addPack(p = new BodyPack("db"));
    {
      p.add(new Transform("db.transformations", null));
      p.add(new Transform("db.renamer", null));
      p.add(new Transform("db.deobfuscate", null));
      p.add(new Transform("db.force-recompile", null));
    }

    onlyStandardPacks = true;
  }

  private void addPack(Pack p) {
    if (packNameToPack.containsKey(p.getPhaseName())) {
      throw new RuntimeException("Duplicate pack " + p.getPhaseName());
    }
    packNameToPack.put(p.getPhaseName(), p);
    packList.add(p);
  }

  public boolean hasPack(String phaseName) {
    return getPhase(phaseName) != null;
  }

  public Pack getPack(String phaseName) {
    Pack p = packNameToPack.get(phaseName);
    return p;
  }

  public boolean hasPhase(String phaseName) {
    return getPhase(phaseName) != null;
  }

  public HasPhaseOptions getPhase(String phaseName) {
    int index = phaseName.indexOf('.');
    if (index < 0) {
      return getPack(phaseName);
    }
    String packName = phaseName.substring(0, index);
    return hasPack(packName) ? getPack(packName).get(phaseName) : null;
  }

  public Transform getTransform(String phaseName) {
    return (Transform) getPhase(phaseName);
  }

  public Collection<Pack> allPacks() {
    return Collections.unmodifiableList(packList);
  }

  public void runPacks() {
    if (Options.v().oaat()) {
      runPacksForOneClassAtATime();
    } else {
      runPacksNormally();
    }
  }

  private void runPacksForOneClassAtATime() {
    if (Options.v().src_prec() == Options.src_prec_class && Options.v().keep_line_number()) {
      LineNumberAdder.v().internalTransform("", null);
    }

    setupJAR();

    final boolean validate = Options.v().validate();
    final SourceLocator srcLoc = SourceLocator.v();
    final Scene scene = Scene.v();
    for (String path : Options.v().process_dir()) {
      // hack1: resolve to signatures only
      for (String cl : srcLoc.getClassesUnder(path)) {
        SootClass clazz = scene.forceResolve(cl, SootClass.SIGNATURES);
        clazz.setApplicationClass();
      }
      // hack2: for each class one after another:
      // a) resolve to bodies
      // b) run packs
      // c) write class
      // d) remove bodies
      for (String cl : srcLoc.getClassesUnder(path)) {
        SootClass clazz = null;
        ClassSource source = srcLoc.getClassSource(cl);
        if (source == null) {
          throw new RuntimeException("Could not locate class source");
        }
        try {
          clazz = scene.getSootClass(cl);
          clazz.setResolvingLevel(SootClass.BODIES);
          source.resolve(clazz);
        } finally {
          source.close();
        }

        // Create tags from all values we only have in code assingments now
        for (SootClass sc : scene.getApplicationClasses()) {
          if (validate) {
            sc.validate();
          }
          if (!sc.isPhantom) {
            ConstantInitializerToTagTransformer.v().transformClass(sc, true);
          }
        }

        runBodyPacks(clazz);
        // generate output
        writeClass(clazz);

        if (!Options.v().no_writeout_body_releasing()) {
          releaseBodies(clazz);
        }
      }

      // for (String cl : SourceLocator.v().getClassesUnder(path)) {
      // SootClass clazz = Scene.v().forceResolve(cl, SootClass.BODIES);
      // releaseBodies(clazz);
      // Scene.v().removeClass(clazz);
      // }
    }
    tearDownJAR();

    handleInnerClasses();
  }

  private void runPacksNormally() {
    if (Options.v().src_prec() == Options.src_prec_class && Options.v().keep_line_number()) {
      LineNumberAdder.v().internalTransform("", null);
    }

    if (Options.v().whole_program() || Options.v().whole_shimple()) {
      runWholeProgramPacks();
    }
    retrieveAllBodies();

    // Create tags from all values we only have in code assignments now
    final boolean validate = Options.v().validate();
    for (SootClass sc : Scene.v().getApplicationClasses()) {
      if (validate) {
        sc.validate();
      }
      if (!sc.isPhantom) {
        ConstantInitializerToTagTransformer.v().transformClass(sc, true);
      }
    }

    // if running coffi cfg metrics, print out results and exit
    if (soot.jbco.Main.metrics) {
      coffiMetrics();
      System.exit(0);
    }

    preProcessDAVA();
    if (Options.v().interactive_mode()) {
      if (InteractionHandler.v().getInteractionListener() == null) {
        logger.debug("Cannot run in interactive mode. No listeners available. Continuing in regular mode.");
        Options.v().set_interactive_mode(false);
      } else {
        logger.debug("Running in interactive mode.");
      }
    }
    runBodyPacks();
    handleInnerClasses();
  }

  public void coffiMetrics() {
    int tV = 0, tE = 0, hM = 0;
    double aM = 0;
    HashMap<SootMethod, int[]> hashVem = soot.coffi.CFG.methodsToVEM;
    for (int[] vem : hashVem.values()) {
      tV += vem[0];
      tE += vem[1];
      aM += vem[2];
      if (vem[2] > hM) {
        hM = vem[2];
      }
    }
    if (hashVem.size() > 0) {
      aM /= hashVem.size();
    }

    logger.debug("Vertices, Edges, Avg Degree, Highest Deg:    " + tV + "  " + tE + "  " + aM + "  " + hM);
  }

  public void runBodyPacks() {
    runBodyPacks(reachableClasses());
  }

  public JarOutputStream getJarFile() {
    return jarFile;
  }

  public void writeOutput() {
    setupJAR();
    if (Options.v().verbose()) {
      PhaseDumper.v().dumpBefore("output");
    }
    switch (Options.v().output_format()) {
      case Options.output_format_dava:
        postProcessDAVA();
        outputDava();
        break;
      case Options.output_format_dex:
      case Options.output_format_force_dex:
        writeDexOutput();
        break;
      default:
        writeOutput(reachableClasses());
        tearDownJAR();
        break;
    }
    postProcessXML(reachableClasses());

    if (!Options.v().no_writeout_body_releasing()) {
      releaseBodies(reachableClasses());
    }
    if (Options.v().verbose()) {
      PhaseDumper.v().dumpAfter("output");
    }
  }

  protected void writeDexOutput() {
    dexPrinter = new DexPrinter();
    writeOutput(reachableClasses());
    dexPrinter.print();
    dexPrinter = null;
  }

  private void setupJAR() {
    if (Options.v().output_jar()) {
      String outFileName = SourceLocator.v().getOutputJarName();
      try {
        jarFile = new JarOutputStream(new FileOutputStream(outFileName));
      } catch (IOException e) {
        throw new CompilationDeathException("Cannot open output Jar file " + outFileName);
      }
    } else {
      jarFile = null;
    }
  }

  private void runWholeProgramPacks() {
    if (Options.v().whole_shimple()) {
      ShimpleTransformer.v().transform();
      getPack("wspp").apply();
      getPack("cg").apply();
      getPack("wstp").apply();
      getPack("wsop").apply();
    } else {
      getPack("wjpp").apply();
      getPack("cg").apply();
      getPack("wjtp").apply();
      getPack("wjop").apply();
      getPack("wjap").apply();
    }
    PaddleHook.v().finishPhases();
  }

  /* preprocess classes for DAVA */
  private void preProcessDAVA() {
    if (Options.v().output_format() == Options.output_format_dava) {
      if (!PhaseOptions.getBoolean(PhaseOptions.v().getPhaseOptions("db"), "source-is-javac")) {
        /*
         * It turns out that the exception attributes of a method i.e. those exceptions that a method can throw are only
         * checked by the Java compiler and not the JVM
         *
         * Javac does place this information into the attributes but other compilers dont hence if the source is not javac
         * then we have to do this fancy analysis to find all the potential exceptions that might get thrown
         *
         * BY DEFAULT the option javac of db is set to true so we assume that the source is javac
         *
         * See ThrowFinder for more details
         */
        if (DEBUG) {
          System.out.println("Source is not Javac hence invoking ThrowFinder");
        }

        ThrowFinder.v().find();
      } else {
        if (DEBUG) {
          System.out.println("Source is javac hence we dont need to invoke ThrowFinder");
        }
      }

      PackageNamer.v().fixNames();
    }
  }

  private void runBodyPacks(final Iterator<SootClass> classes) {
    int threadNum = Options.v().num_threads();
    if (threadNum < 1) {
      threadNum = Runtime.getRuntime().availableProcessors();
    }
    CountingThreadPoolExecutor executor =
        new CountingThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    while (classes.hasNext()) {
      final SootClass c = classes.next();
      executor.execute(() -> runBodyPacks(c));
    }

    // Wait till all packs have been executed
    try {
      executor.awaitCompletion();
      executor.shutdown();
    } catch (InterruptedException e) {
      // Something went horribly wrong
      throw new RuntimeException("Could not wait for pack threads to finish: " + e.getMessage(), e);
    }

    // If something went wrong, we tell the world
    Throwable exception = executor.getException();
    if (exception != null) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new RuntimeException(exception);
      }
    }
  }

  private void handleInnerClasses() {
    InnerClassTagAggregator.v().internalTransform("", null);
  }

  protected void writeOutput(Iterator<SootClass> classes) {
    // If we're writing individual class files, we can write them
    // concurrently. Otherwise, we need to synchronize for not destroying
    // the shared output stream.
    int threadNum = Options.v().output_format() == Options.output_format_class && jarFile == null
        ? Runtime.getRuntime().availableProcessors() : 1;
    CountingThreadPoolExecutor executor =
        new CountingThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    while (classes.hasNext()) {
      final SootClass c = classes.next();
      executor.execute(() -> writeClass(c));
    }

    // Wait till all classes have been written
    try {
      executor.awaitCompletion();
      executor.shutdown();
    } catch (InterruptedException e) {
      // Something went horribly wrong
      throw new RuntimeException("Could not wait for writer threads to finish: " + e.getMessage(), e);
    }

    // If something went wrong, we tell the world
    Throwable exception = executor.getException();
    if (exception != null) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new RuntimeException(exception);
      }
    }
  }

  private void tearDownJAR() {
    try {
      if (jarFile != null) {
        jarFile.close();
      }
    } catch (IOException e) {
      throw new CompilationDeathException("Error closing output jar: " + e);
    }
  }

  private void releaseBodies(Iterator<SootClass> classes) {
    while (classes.hasNext()) {
      releaseBodies(classes.next());
    }
  }

  private Iterator<SootClass> reachableClasses() {
    return Scene.v().getApplicationClasses().snapshotIterator();
  }

  /* post process for DAVA */
  private void postProcessDAVA() {
    final boolean transformations =
        PhaseOptions.getBoolean(PhaseOptions.v().getPhaseOptions("db.transformations"), "enabled");

    /*
     * apply analyses etc
     */
    for (SootClass s : Scene.v().getApplicationClasses()) {
      /*
       * Nomair A. Naeem 5-Jun-2005 Added to remove the *final* bug in Dava (often seen in AspectJ programs)
       */
      DavaStaticBlockCleaner.v().staticBlockInlining(s);

      // remove returns from void methods
      VoidReturnRemover.cleanClass(s);

      // remove the default constructor if this is the only one present
      RemoveEmptyBodyDefaultConstructor.checkAndRemoveDefault(s);

      /*
       * Nomair A. Naeem 1st March 2006 Check if we want to apply transformations one reason we might not want to do this is
       * when gathering old metrics data!!
       */

      // debug("analyzeAST","Advanced Analyses ALL DISABLED");
      logger.debug("Analyzing " + SourceLocator.v().getFileNameFor(s, Options.v().output_format()) + "... ");

      /*
       * Nomair A. Naeem 29th Jan 2006 Added hook into going through each decompiled method again Need it for all the
       * implemented AST analyses
       */
      for (SootMethod m : s.getMethods()) {
        /*
         * 3rd April 2006 Fixing RuntimeException caused when you retrieve an active body when one is not present
         */
        if (m.hasActiveBody()) {
          DavaBody body = (DavaBody) m.getActiveBody();
          // System.out.println("body"+body.toString());
          if (transformations) {
            body.analyzeAST();
          } else {
            body.applyBugFixes();
          }
        }
      }

    } // going through all classes

    /*
     * Nomair A. Naeem March 6th, 2006
     *
     * SHOULD BE INVOKED ONLY ONCE!!! If interprocedural analyses are turned off they are checked within this method.
     *
     * HAVE TO invoke this analysis since this invokes the renamer!!
     */
    if (transformations) {
      InterProceduralAnalyses.applyInterProceduralAnalyses();
    }
  }

  private void outputDava() {
    /*
     * Generate decompiled code
     */
    String pathForBuild = null;
    ArrayList<String> decompiledClasses = new ArrayList<String>();
    for (SootClass s : Scene.v().getApplicationClasses()) {
      String fileName = SourceLocator.v().getFileNameFor(s, Options.v().output_format());
      decompiledClasses.add(fileName.substring(fileName.lastIndexOf('/') + 1));
      if (pathForBuild == null) {
        pathForBuild = fileName.substring(0, fileName.lastIndexOf('/') + 1);
        // System.out.println(pathForBuild);
      }
      if (Options.v().gzip()) {
        fileName = fileName + ".gz";
      }

      PrintWriter writerOut = null;
      try {
        OutputStream streamOut;
        if (jarFile != null) {
          jarFile.putNextEntry(new JarEntry(fileName.replace('\\', '/')));
          streamOut = jarFile;
        } else {
          streamOut = new FileOutputStream(fileName);
        }
        if (Options.v().gzip()) {
          streamOut = new GZIPOutputStream(streamOut);
        }
        writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
      } catch (IOException e) {
        throw new CompilationDeathException("Cannot output file " + fileName, e);
      }

      logger.debug("Generating " + fileName + "... ");

      DavaPrinter.v().printTo(s, writerOut);

      try {
        writerOut.flush();
        if (jarFile == null) {
          writerOut.close();
        } else {
          jarFile.closeEntry();
        }
      } catch (IOException e) {
        throw new CompilationDeathException("Cannot close output file " + fileName);
      }
    } // going through all classes

    /*
     * Create the build.xml for Dava
     */
    if (pathForBuild != null) {
      // path for build is probably ending in sootoutput/dava/src
      // definetly remove the src
      if (pathForBuild.endsWith("src/")) {
        pathForBuild = pathForBuild.substring(0, pathForBuild.length() - 4);
      }

      String fileName = pathForBuild + "build.xml";
      try (OutputStream streamOut = new FileOutputStream(fileName)) {
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        DavaBuildFile.generate(writerOut, decompiledClasses);
        writerOut.flush();
      } catch (IOException e) {
        throw new CompilationDeathException("Cannot open output file " + fileName, e);
      }
    }
  }

  @SuppressWarnings("fallthrough")
  private void runBodyPacks(SootClass c) {
    final int format = Options.v().output_format();
    if (format == Options.output_format_dava) {
      logger.debug("Decompiling {}...", c.getName());

      // January 13th, 2006 SootMethodAddedByDava is set to false for
      // SuperFirstStmtHandler
      G.v().SootMethodAddedByDava = false;
    } else {
      logger.debug("Transforming {}...", c.getName());
    }

    boolean produceBaf = false, produceGrimp = false, produceDava = false, produceJimple = true, produceShimple = false;

    switch (format) {
      case Options.output_format_none:
      case Options.output_format_xml:
      case Options.output_format_jimple:
      case Options.output_format_jimp:
      case Options.output_format_template:
      case Options.output_format_dex:
      case Options.output_format_force_dex:
        break;
      case Options.output_format_shimp:
      case Options.output_format_shimple:
        produceShimple = true;
        // FLIP produceJimple
        produceJimple = false;
        break;
      case Options.output_format_dava:
        produceDava = true;
        // FALL THROUGH
      case Options.output_format_grimp:
      case Options.output_format_grimple:
        produceGrimp = true;
        break;
      case Options.output_format_baf:
      case Options.output_format_b:
        produceBaf = true;
        break;
      case Options.output_format_jasmin:
      case Options.output_format_class:
      case Options.output_format_asm:
        produceGrimp = Options.v().via_grimp();
        produceBaf = !produceGrimp;
        break;
      default:
        throw new RuntimeException();
    }

    TagCollector tc = (format != Options.output_format_jimple && Options.v().xml_attributes()) ? new TagCollector() : null;

    boolean wholeShimple = Options.v().whole_shimple();
    if (Options.v().via_shimple()) {
      produceShimple = true;
    }

    // here we create a copy of the methods so that transformers are able
    // to add method bodies during the following iteration;
    // such adding of methods happens in rare occasions: for instance when
    // resolving a method reference to a non-existing method, then this
    // method is created as a phantom method when phantom-refs are enabled
    for (SootMethod m : new ArrayList<SootMethod>(c.getMethods())) {
      if (DEBUG) {
        if (!m.getExceptions().isEmpty()) {
          System.out.println("PackManager printing out jimple body exceptions for method " + m.toString() + " "
              + m.getExceptions().toString());
        }
      }

      if (!m.isConcrete()) {
        continue;
      }

      if (produceShimple || wholeShimple) {
        ShimpleBody sBody;

        // whole shimple or not?
        {
          Body body = m.retrieveActiveBody();
          if (!m.hasActiveBody()) {
            continue;
          }
          if (body instanceof ShimpleBody) {
            sBody = (ShimpleBody) body;
            if (!sBody.isSSA()) {
              sBody.rebuild();
            }
          } else {
            sBody = Shimple.v().newBody(body);
          }
        }

        m.setActiveBody(sBody);
        getPack("stp").apply(sBody);
        getPack("sop").apply(sBody);

        if (produceJimple || (wholeShimple && !produceShimple)) {
          m.setActiveBody(sBody.toJimpleBody());
        }
      }

      if (produceJimple) {
        Body body = m.retrieveActiveBody();

        getTransform("jb.cp").apply(body); // CopyPropagator
        getTransform("jb.cbf").apply(body); // ConditionalBranchFolder
        getTransform("jb.uce").apply(body); // UnreachableCodeEliminator
        getTransform("jb.dae").apply(body); // DeadAssignmentEliminator
        getTransform("jb.cp-ule").apply(body); // UnusedLocalEliminator
        getPack("jtp").apply(body);

        if (Options.v().validate()) {
          body.validate();
        }
        getPack("jop").apply(body);
        getPack("jap").apply(body);
        if (tc != null) {
          tc.collectBodyTags(body);
        }
      }

      // getPack("cfg").apply(m.retrieveActiveBody());
      if (m.hasActiveBody()) {
        if (produceGrimp) {
          GrimpBody newBody = Grimp.v().newBody(m.getActiveBody(), "gb");
          m.setActiveBody(newBody);
          getPack("gop").apply(newBody);
        } else if (produceBaf) {
          m.setActiveBody(convertJimpleBodyToBaf(m));
        }
      }
    }

    if (tc != null) {
      processXMLForClass(c, tc);
    }

    if (produceDava) {
      for (SootMethod m : c.getMethods()) {
        if (!m.isConcrete() || !m.hasActiveBody()) {
          //note: abnormal class can have a concrete method without body.
          continue;
        }
        // all the work done in decompilation is done in DavaBody which
        // is invoked from within newBody
        m.setActiveBody(Dava.v().newBody(m.getActiveBody()));
      }

      /*
       * January 13th, 2006 SuperFirstStmtHandler might have set SootMethodAddedByDava if it needs to create a new method.
       */
      // could use G to add new method...................
      if (G.v().SootMethodAddedByDava) {
        // System.out.println("PACKMANAGER SAYS:----------------Have to
        // add the new method(s)");
        for (SootMethod m : G.v().SootMethodsAdded) {
          c.addMethod(m);
        }
        G.v().SootMethodsAdded = new ArrayList<SootMethod>();
        G.v().SootMethodAddedByDava = false;
      }
    } // end if produceDava
  }

  public BafBody convertJimpleBodyToBaf(SootMethod m) {
    JimpleBody body = (JimpleBody) m.getActiveBody().clone();
    // Change
    // ConditionalBranchFolder.v().transform(body);
    // UnreachableCodeEliminator.v().transform(body);
    // DeadAssignmentEliminator.v().transform(body);
    // UnusedLocalEliminator.v().transform(body);
    BafBody bafBody = Baf.v().newBody(body);
    getPack("bop").apply(bafBody);
    getPack("tag").apply(bafBody);
    if (Options.v().validate()) {
      bafBody.validate();
    }
    return bafBody;
  }

  protected void writeClass(SootClass c) {
    final int format = Options.v().output_format();
    switch (format) {
      case Options.output_format_none:
      case Options.output_format_dava:
        return;
      case Options.output_format_dex:
      case Options.output_format_force_dex:
        // just add the class to the dex printer, writing is done after
        // adding all classes
        dexPrinter.add(c);
        return;
      case Options.output_format_jimple:
        // Create code assignments for those values we only have in code assignments
        if (!c.isPhantom) {
          ConstantValueToInitializerTransformer.v().transformClass(c);
        }
        break;
      default:
        break;
    }

    String fileName = SourceLocator.v().getFileNameFor(c, format);
    if (Options.v().gzip()) {
      fileName = fileName + ".gz";
    }

    OutputStream streamOut = null;
    PrintWriter writerOut = null;
    try {
      if (jarFile != null) {
        // Fix path delimiters according to ZIP specification
        fileName = fileName.replace("\\", "/");
        JarEntry entry = new JarEntry(fileName);
        entry.setMethod(ZipEntry.DEFLATED);
        jarFile.putNextEntry(entry);
        streamOut = jarFile;
      } else {
        new File(fileName).getParentFile().mkdirs();
        streamOut = new FileOutputStream(fileName);
      }
      if (Options.v().gzip()) {
        streamOut = new GZIPOutputStream(streamOut);
      }
      if (format == Options.output_format_class) {
        if (Options.v().jasmin_backend()) {
          streamOut = new JasminOutputStream(streamOut);
        }
      }
      writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
      logger.debug("Writing to " + fileName);
    } catch (IOException e) {
      throw new CompilationDeathException("Cannot output file " + fileName, e);
    }

    if (Options.v().xml_attributes()) {
      Printer.v().setOption(Printer.ADD_JIMPLE_LN);
    }

    switch (format) {
      case Options.output_format_class:
        if (!Options.v().jasmin_backend()) {
          createASMBackend(c).generateClassFile(streamOut);
          break;
        }
      case Options.output_format_jasmin:
        createJasminBackend(c).print(writerOut);
        break;
      case Options.output_format_jimp:
      case Options.output_format_shimp:
      case Options.output_format_b:
      case Options.output_format_grimp:
        Printer.v().setOption(Printer.USE_ABBREVIATIONS);
        Printer.v().printTo(c, writerOut);
        break;
      case Options.output_format_baf:
      case Options.output_format_jimple:
      case Options.output_format_shimple:
      case Options.output_format_grimple:
        writerOut = new PrintWriter(new EscapedWriter(new OutputStreamWriter(streamOut)));
        Printer.v().printTo(c, writerOut);
        break;
      case Options.output_format_xml:
        writerOut = new PrintWriter(new EscapedWriter(new OutputStreamWriter(streamOut)));
        XMLPrinter.v().printJimpleStyleTo(c, writerOut);
        break;
      case Options.output_format_template:
        writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        TemplatePrinter.v().printTo(c, writerOut);
        break;
      case Options.output_format_asm:
        createASMBackend(c).generateTextualRepresentation(writerOut);
        break;
      default:
        throw new RuntimeException();
    }

    try {
      writerOut.flush();
      if (jarFile == null) {
        streamOut.close();
        writerOut.close();
      } else {
        jarFile.closeEntry();
      }
    } catch (IOException e) {
      throw new CompilationDeathException("Cannot close output file " + fileName);
    }
  }

  /**
   * Factory method for creating a new backend on top of Jasmin
   *
   * @param c
   *          The class for which to create a Jasmin-based backend
   * @return The Jasmin-based backend for writing the given class into bytecode
   */
  private AbstractJasminClass createJasminBackend(SootClass c) {
    if (c.containsBafBody()) {
      return new soot.baf.JasminClass(c);
    } else {
      return new soot.jimple.JasminClass(c);
    }
  }

  /**
   * Factory method for creating a new backend on top of ASM. At the moment, we always start from BAF. Custom implementations
   * can use other techniques.
   *
   * @param c
   *          The class for which to create the ASM backend
   * @return The ASM backend for writing the class into bytecode
   */
  protected BafASMBackend createASMBackend(SootClass c) {
    return new BafASMBackend(c, Options.v().java_version());
  }

  private void postProcessXML(Iterator<SootClass> classes) {
    if (!Options.v().xml_attributes()) {
      return;
    }
    if (Options.v().output_format() != Options.output_format_jimple) {
      return;
    }
    while (classes.hasNext()) {
      SootClass c = classes.next();
      processXMLForClass(c);
    }
  }

  private void processXMLForClass(SootClass c, TagCollector tc) {
    int ofmt = Options.v().output_format();
    final int format = ofmt != Options.output_format_none ? ofmt : Options.output_format_jimple;
    String fileName = SourceLocator.v().getFileNameFor(c, format);
    XMLAttributesPrinter xap = new XMLAttributesPrinter(fileName, SourceLocator.v().getOutputDir());
    xap.printAttrs(c, tc);
  }

  /**
   * assumption: only called when <code>Options.v().output_format() == Options.output_format_jimple</code>
   */
  private void processXMLForClass(SootClass c) {
    final int format = Options.v().output_format();
    String fileName = SourceLocator.v().getFileNameFor(c, format);
    XMLAttributesPrinter xap = new XMLAttributesPrinter(fileName, SourceLocator.v().getOutputDir());
    xap.printAttrs(c);
  }

  private void releaseBodies(SootClass cl) {
    for (Iterator<SootMethod> methodIt = cl.methodIterator(); methodIt.hasNext();) {
      SootMethod m = methodIt.next();
      if (m.hasActiveBody()) {
        m.releaseActiveBody();
      }
    }
  }

  private void retrieveAllBodies() {
    // The old coffi front-end is not thread-safe
    int threadNum = Options.v().coffi() ? 1 : Runtime.getRuntime().availableProcessors();
    CountingThreadPoolExecutor executor =
        new CountingThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    for (Iterator<SootClass> clIt = reachableClasses(); clIt.hasNext();) {
      SootClass cl = clIt.next();
      // note: the following is a snapshot iterator;
      // this is necessary because it can happen that phantom methods
      // are added during resolution
      for (SootMethod m : new ArrayList<SootMethod>(cl.getMethods())) {
        if (m.isConcrete()) {
          executor.execute(() -> m.retrieveActiveBody());
        }
      }
    }

    // Wait till all method bodies have been loaded
    try {
      executor.awaitCompletion();
      executor.shutdown();
    } catch (InterruptedException e) {
      // Something went horribly wrong
      throw new RuntimeException("Could not wait for loader threads to finish: " + e.getMessage(), e);
    }

    // If something went wrong, we tell the world
    Throwable exception = executor.getException();
    if (exception != null) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new RuntimeException(exception);
      }
    }
  }
}