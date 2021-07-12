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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.AnySubType;
import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.Context;
import soot.DoubleType;
import soot.EntryPoints;
import soot.FastHierarchy;
import soot.FloatType;
import soot.IntType;
import soot.Kind;
import soot.Local;
import soot.LongType;
import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.NullType;
import soot.PackManager;
import soot.PhaseOptions;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.spark.pag.AllocDotField;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.annotation.nullcheck.NullnessAnalysis;
import soot.jimple.toolkits.callgraph.ConstantArrayAnalysis.ArrayTypes;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.DirectTarget;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.RegisteredHandlerTarget;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdge;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdgeTarget;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.WrapperTarget;
import soot.jimple.toolkits.reflection.ReflectionTraceInfo;
import soot.options.CGOptions;
import soot.options.Options;
import soot.options.SparkOptions;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.HashMultiMap;
import soot.util.IterableNumberer;
import soot.util.LargeNumberedMap;
import soot.util.MultiMap;
import soot.util.NumberedString;
import soot.util.SmallNumberedMap;
import soot.util.StringNumberer;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

/**
 * Models the call graph.
 *
 * @author Ondrej Lhotak
 */
public class OnFlyCallGraphBuilder {
  private static final Logger logger = LoggerFactory.getLogger(OnFlyCallGraphBuilder.class);

  // NOTE: this field must be static to avoid adding the transformation again if the call graph is rebuilt.
  static boolean registeredGuardsTransformation = false;

  private static final Pattern PATTERN_METHOD_SUBSIG
      = Pattern.compile("(?<returnType>.*?) (?<methodName>.*?)\\((?<parameters>.*?)\\)");

  private static final PrimType[] CHAR_NARROWINGS;
  private static final PrimType[] INT_NARROWINGS;
  private static final PrimType[] SHORT_NARROWINGS;
  private static final PrimType[] LONG_NARROWINGS;
  private static final ByteType[] BYTE_NARROWINGS;
  private static final PrimType[] FLOAT_NARROWINGS;
  private static final PrimType[] BOOLEAN_NARROWINGS;
  private static final PrimType[] DOUBLE_NARROWINGS;

  static {
    final CharType cT = CharType.v();
    final IntType iT = IntType.v();
    final ShortType sT = ShortType.v();
    final ByteType bT = ByteType.v();
    final LongType lT = LongType.v();
    final FloatType fT = FloatType.v();
    CHAR_NARROWINGS = new PrimType[] { cT };
    INT_NARROWINGS = new PrimType[] { iT, cT, sT, bT, sT };
    SHORT_NARROWINGS = new PrimType[] { sT, bT };
    LONG_NARROWINGS = new PrimType[] { lT, iT, cT, sT, bT, sT };
    BYTE_NARROWINGS = new ByteType[] { bT };
    FLOAT_NARROWINGS = new PrimType[] { fT, lT, iT, cT, sT, bT, sT };
    BOOLEAN_NARROWINGS = new PrimType[] { BooleanType.v() };
    DOUBLE_NARROWINGS = new PrimType[] { DoubleType.v(), fT, lT, iT, cT, sT, bT, sT };
  }

  protected final NumberedString sigFinalize;
  protected final NumberedString sigInit;
  protected final NumberedString sigForName;

  protected final RefType clRunnable = RefType.v("java.lang.Runnable");
  protected final RefType clAsyncTask = RefType.v("android.os.AsyncTask");
  protected final RefType clHandler = RefType.v("android.os.Handler");

  /** context-insensitive stuff */
  private final CallGraph cicg = Scene.v().internalMakeCallGraph();

  // end type based reflection resolution
  protected final LargeNumberedMap<Local, List<VirtualCallSite>> receiverToSites;
  protected final LargeNumberedMap<SootMethod, List<Local>> methodToReceivers;
  protected final LargeNumberedMap<SootMethod, List<Local>> methodToInvokeBases;
  protected final LargeNumberedMap<SootMethod, List<Local>> methodToInvokeArgs;
  protected final LargeNumberedMap<SootMethod, List<Local>> methodToStringConstants;
  protected final SmallNumberedMap<Local, List<VirtualCallSite>> stringConstToSites;

  protected final HashSet<SootMethod> analyzedMethods = new HashSet<SootMethod>();
  protected final MultiMap<Local, InvokeCallSite> baseToInvokeSite = new HashMultiMap<>();
  protected final MultiMap<Local, InvokeCallSite> invokeArgsToInvokeSite = new HashMultiMap<>();
  protected final Map<Local, BitSet> invokeArgsToSize = new IdentityHashMap<>();
  protected final MultiMap<AllocDotField, Local> allocDotFieldToLocal = new HashMultiMap<>();
  protected final MultiMap<Local, Type> reachingArgTypes = new HashMultiMap<>();
  protected final MultiMap<Local, Type> reachingBaseTypes = new HashMultiMap<>();
  protected final ChunkedQueue<SootMethod> targetsQueue = new ChunkedQueue<SootMethod>();
  protected final QueueReader<SootMethod> targets = targetsQueue.reader();

  protected final ReflectionModel reflectionModel;
  protected final CGOptions options;
  protected boolean appOnly;

  /** context-sensitive stuff */
  protected final ReachableMethods rm;
  protected final QueueReader<MethodOrMethodContext> worklist;
  protected final ContextManager cm;

  protected final VirtualEdgesSummaries virtualEdgeSummaries = new VirtualEdgesSummaries();

  protected NullnessAnalysis nullnessCache = null;
  protected ConstantArrayAnalysis arrayCache = null;
  protected SootMethod analysisKey = null;

  public OnFlyCallGraphBuilder(ContextManager cm, ReachableMethods rm, boolean appOnly) {
    final Scene sc = Scene.v();
    {
      final StringNumberer nmbr = sc.getSubSigNumberer();
      this.sigFinalize = nmbr.findOrAdd("void finalize()");
      this.sigInit = nmbr.findOrAdd("void <init>()");
      this.sigForName = nmbr.findOrAdd("java.lang.Class forName(java.lang.String)");
    }
    {
      this.receiverToSites = new LargeNumberedMap<Local, List<VirtualCallSite>>(sc.getLocalNumberer());
      final IterableNumberer<SootMethod> methodNumberer = sc.getMethodNumberer();
      this.methodToReceivers = new LargeNumberedMap<SootMethod, List<Local>>(methodNumberer);
      this.methodToInvokeBases = new LargeNumberedMap<SootMethod, List<Local>>(methodNumberer);
      this.methodToInvokeArgs = new LargeNumberedMap<SootMethod, List<Local>>(methodNumberer);
      this.methodToStringConstants = new LargeNumberedMap<SootMethod, List<Local>>(methodNumberer);
      this.stringConstToSites = new SmallNumberedMap<Local, List<VirtualCallSite>>();
    }

    this.cm = cm;
    this.rm = rm;
    this.worklist = rm.listener();
    this.options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));
    if (!options.verbose()) {
      logger.debug("[Call Graph] For information on where the call graph may be incomplete,"
          + " use the verbose option to the cg phase.");
    }

    if (options.reflection_log() == null || options.reflection_log().length() == 0) {
      if (options.types_for_invoke() && new SparkOptions(PhaseOptions.v().getPhaseOptions("cg.spark")).enabled()) {
        this.reflectionModel = new TypeBasedReflectionModel();
      } else {
        this.reflectionModel = new DefaultReflectionModel();
      }
    } else {
      this.reflectionModel = new TraceBasedReflectionModel();
    }
    this.appOnly = appOnly;
  }

  public OnFlyCallGraphBuilder(ContextManager cm, ReachableMethods rm) {
    this(cm, rm, false);
  }

  public ContextManager getContextManager() {
    return cm;
  }

  public LargeNumberedMap<SootMethod, List<Local>> methodToReceivers() {
    return methodToReceivers;
  }

  public LargeNumberedMap<SootMethod, List<Local>> methodToInvokeArgs() {
    return methodToInvokeArgs;
  }

  public LargeNumberedMap<SootMethod, List<Local>> methodToInvokeBases() {
    return methodToInvokeBases;
  }

  public LargeNumberedMap<SootMethod, List<Local>> methodToStringConstants() {
    return methodToStringConstants;
  }

  public void processReachables() {
    while (true) {
      if (!worklist.hasNext()) {
        rm.update();
        if (!worklist.hasNext()) {
          break;
        }
      }
      MethodOrMethodContext momc = worklist.next();
      SootMethod m = momc.method();
      if (appOnly && !m.getDeclaringClass().isApplicationClass()) {
        continue;
      }
      if (analyzedMethods.add(m)) {
        processNewMethod(m);
      }
      processNewMethodContext(momc);
    }
  }

  public boolean wantTypes(Local receiver) {
    return receiverToSites.get(receiver) != null || baseToInvokeSite.get(receiver) != null;
  }

  public void addBaseType(Local base, Context context, Type ty) {
    assert (context == null);
    final Set<InvokeCallSite> invokeSites = baseToInvokeSite.get(base);
    if (invokeSites != null) {
      if (reachingBaseTypes.put(base, ty) && !invokeSites.isEmpty()) {
        resolveInvoke(invokeSites);
      }
    }
  }

  public void addInvokeArgType(Local argArray, Context context, Type t) {
    assert (context == null);
    final Set<InvokeCallSite> invokeSites = invokeArgsToInvokeSite.get(argArray);
    if (invokeSites != null) {
      if (reachingArgTypes.put(argArray, t)) {
        resolveInvoke(invokeSites);
      }
    }
  }

  public void setArgArrayNonDetSize(Local argArray, Context context) {
    assert (context == null);
    final Set<InvokeCallSite> invokeSites = invokeArgsToInvokeSite.get(argArray);
    if (invokeSites != null) {
      if (!invokeArgsToSize.containsKey(argArray)) {
        invokeArgsToSize.put(argArray, null);
        resolveInvoke(invokeSites);
      }
    }
  }

  public void addPossibleArgArraySize(Local argArray, int value, Context context) {
    assert (context == null);
    final Set<InvokeCallSite> invokeSites = invokeArgsToInvokeSite.get(argArray);
    if (invokeSites != null) {
      // non-det size
      BitSet sizeSet = invokeArgsToSize.get(argArray);
      if (sizeSet == null || !sizeSet.isEmpty()) {
        if (sizeSet == null) {
          invokeArgsToSize.put(argArray, sizeSet = new BitSet());
        }
        if (!sizeSet.get(value)) {
          sizeSet.set(value);
          resolveInvoke(invokeSites);
        }
      }
    }
  }

  private static Set<RefLikeType> resolveToClasses(Set<Type> rawTypes) {
    Set<RefLikeType> toReturn = new HashSet<>();
    if (!rawTypes.isEmpty()) {
      final FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
      for (Type ty : rawTypes) {
        if (ty instanceof AnySubType) {
          AnySubType anySubType = (AnySubType) ty;
          RefType base = anySubType.getBase();
          Set<SootClass> classRoots;
          if (base.getSootClass().isInterface()) {
            classRoots = fh.getAllImplementersOfInterface(base.getSootClass());
          } else {
            classRoots = Collections.singleton(base.getSootClass());
          }
          toReturn.addAll(getTransitiveSubClasses(classRoots));
        } else if (ty instanceof RefType) {
          toReturn.add((RefType) ty);
        }
      }
    }
    return toReturn;
  }

  private static Collection<RefLikeType> getTransitiveSubClasses(Set<SootClass> classRoots) {
    Set<RefLikeType> resolved = new HashSet<>();
    if (!classRoots.isEmpty()) {
      final FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
      for (LinkedList<SootClass> worklist = new LinkedList<>(classRoots); !worklist.isEmpty();) {
        SootClass cls = worklist.removeFirst();
        if (resolved.add(cls.getType())) {
          worklist.addAll(fh.getSubclassesOf(cls));
        }
      }
    }
    return resolved;
  }

  private void resolveInvoke(Collection<InvokeCallSite> list) {
    for (InvokeCallSite ics : list) {
      Set<Type> s = reachingBaseTypes.get(ics.base());
      if (s == null || s.isEmpty()) {
        continue;
      }
      if (ics.reachingTypes() != null) {
        assert (ics.nullnessCode() != InvokeCallSite.MUST_BE_NULL);
        resolveStaticTypes(s, ics);
        continue;
      }
      boolean mustNotBeNull = ics.nullnessCode() == InvokeCallSite.MUST_NOT_BE_NULL;
      boolean mustBeNull = ics.nullnessCode() == InvokeCallSite.MUST_BE_NULL;
      // if the arg array may be null and we haven't seen a size or type
      // yet, then generate nullary methods
      if (mustBeNull || (ics.nullnessCode() == InvokeCallSite.MAY_BE_NULL
          && (!invokeArgsToSize.containsKey(ics.argArray()) || !reachingArgTypes.containsKey(ics.argArray())))) {
        for (Type bType : resolveToClasses(s)) {
          assert (bType instanceof RefType);
          // do not handle array reflection
          if (bType instanceof ArrayType) {
            continue;
          }
          SootClass baseClass = ((RefType) bType).getSootClass();
          assert (!baseClass.isInterface());
          for (Iterator<SootMethod> mIt = getPublicNullaryMethodIterator(baseClass); mIt.hasNext();) {
            SootMethod sm = mIt.next();
            cm.addVirtualEdge(ics.container(), ics.stmt(), sm, Kind.REFL_INVOKE, null);
          }
        }
      } else {
        /*
         * In this branch, either the invoke arg must not be null, or may be null and we have size and type information.
         * Invert the above condition: ~mustBeNull && (~mayBeNull || (has-size && has-type)) => (~mustBeNull && ~mayBeNull)
         * || (~mustBeNull && has-size && has-type) => mustNotBeNull || (~mustBeNull && has-types && has-size) =>
         * mustNotBeNull || (mayBeNull && has-types && has-size)
         */
        Set<Type> reachingTypes = reachingArgTypes.get(ics.argArray());
        /*
         * the path condition allows must-not-be null without type and size info. Do nothing in this case. THIS IS UNSOUND if
         * default null values in an argument array are used.
         */
        if (reachingTypes == null || !invokeArgsToSize.containsKey(ics.argArray())) {
          assert (ics.nullnessCode() == InvokeCallSite.MUST_NOT_BE_NULL) : ics;
          return;
        }
        BitSet methodSizes = invokeArgsToSize.get(ics.argArray());
        for (Type bType : resolveToClasses(s)) {
          assert (bType instanceof RefLikeType);
          // we do not handle static methods or array reflection
          if (!(bType instanceof NullType) && !(bType instanceof ArrayType)) {
            SootClass baseClass = ((RefType) bType).getSootClass();
            Iterator<SootMethod> mIt = getPublicMethodIterator(baseClass, reachingTypes, methodSizes, mustNotBeNull);
            while (mIt.hasNext()) {
              SootMethod sm = mIt.next();
              cm.addVirtualEdge(ics.container(), ics.stmt(), sm, Kind.REFL_INVOKE, null);
            }
          }
        }
      }
    }
  }

  /* End of public methods. */

  private void resolveStaticTypes(Set<Type> s, InvokeCallSite ics) {
    ArrayTypes at = ics.reachingTypes();
    for (Type bType : resolveToClasses(s)) {
      // do not handle array reflection
      if (bType instanceof ArrayType) {
        continue;
      }
      SootClass baseClass = ((RefType) bType).getSootClass();
      for (Iterator<SootMethod> mIt = getPublicMethodIterator(baseClass, at); mIt.hasNext();) {
        SootMethod sm = mIt.next();
        cm.addVirtualEdge(ics.getContainer(), ics.getStmt(), sm, Kind.REFL_INVOKE, null);
      }
    }
  }

  private static Iterator<SootMethod> getPublicMethodIterator(SootClass baseClass, final ArrayTypes at) {
    return new AbstractMethodIterator(baseClass) {
      @Override
      protected boolean acceptMethod(SootMethod m) {
        if (!at.possibleSizes.contains(m.getParameterCount())) {
          return false;
        }
        for (int i = 0; i < m.getParameterCount(); i++) {
          Set<Type> possibleType = at.possibleTypes[i];
          if (possibleType.isEmpty()) {
            continue;
          }
          if (!isReflectionCompatible(m.getParameterType(i), possibleType)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private static PrimType[] narrowings(PrimType f) {
    if (f instanceof IntType) {
      return INT_NARROWINGS;
    } else if (f instanceof ShortType) {
      return SHORT_NARROWINGS;
    } else if (f instanceof LongType) {
      return LONG_NARROWINGS;
    } else if (f instanceof ByteType) {
      return BYTE_NARROWINGS;
    } else if (f instanceof FloatType) {
      return FLOAT_NARROWINGS;
    } else if (f instanceof BooleanType) {
      return BOOLEAN_NARROWINGS;
    } else if (f instanceof DoubleType) {
      return DOUBLE_NARROWINGS;
    } else if (f instanceof CharType) {
      return CHAR_NARROWINGS;
    } else {
      throw new RuntimeException("Unexpected primitive type: " + f);
    }
  }

  private static boolean isReflectionCompatible(Type paramType, Set<Type> reachingTypes) {
    /*
     * attempting to pass in a null will match any type (although attempting to pass it to a primitive arg will give an NPE)
     */
    if (reachingTypes.contains(NullType.v())) {
      return true;
    }
    if (paramType instanceof RefLikeType) {
      final FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
      for (Type rType : reachingTypes) {
        if (fh.canStoreType(rType, paramType)) {
          return true;
        }
      }
      return false;
    } else if (paramType instanceof PrimType) {
      /*
       * It appears, java reflection allows for unboxing followed by widening, so if there is a wrapper type that whose
       * corresponding primitive type can be widened into the expected primitive type, we're set
       */
      for (PrimType narrowings : narrowings((PrimType) paramType)) {
        if (reachingTypes.contains(narrowings.boxedType())) {
          return true;
        }
      }
      return false;
    } else {
      // impossible?
      return false;
    }
  }

  private static Iterator<SootMethod> getPublicMethodIterator(final SootClass baseClass, final Set<Type> reachingTypes,
      final BitSet methodSizes, final boolean mustNotBeNull) {
    if (baseClass.isPhantom()) {
      return Collections.emptyIterator();
    }
    return new AbstractMethodIterator(baseClass) {
      @Override
      protected boolean acceptMethod(SootMethod n) {
        if (methodSizes != null) {
          // if the arg array can be null we have to still allow for nullary methods
          int nParams = n.getParameterCount();
          boolean compatibleSize = methodSizes.get(nParams) || (!mustNotBeNull && nParams == 0);
          if (!compatibleSize) {
            return false;
          }
        }
        for (Type pTy : n.getParameterTypes()) {
          if (!isReflectionCompatible(pTy, reachingTypes)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private static Iterator<SootMethod> getPublicNullaryMethodIterator(final SootClass baseClass) {
    if (baseClass.isPhantom()) {
      return Collections.emptyIterator();
    }
    return new AbstractMethodIterator(baseClass) {
      @Override
      protected boolean acceptMethod(SootMethod n) {
        return n.getParameterCount() == 0;
      }
    };
  }

  public void addType(Local receiver, Context srcContext, Type type, Context typeContext) {
    final List<VirtualCallSite> rcvrToCallSites = receiverToSites.get(receiver);
    if (rcvrToCallSites != null) {
      final VirtualCalls virtualCalls = VirtualCalls.v();
      final Scene sc = Scene.v();
      final FastHierarchy fh = sc.getOrMakeFastHierarchy();
      for (final VirtualCallSite site : rcvrToCallSites) {
        if (skipSite(site, fh, type)) {
          continue;
        }

        final InstanceInvokeExpr iie = site.iie();
        if (iie instanceof SpecialInvokeExpr && !Kind.isFake(site.kind())) {
          SootMethod target = virtualCalls.resolveSpecial(iie.getMethodRef(), site.getContainer(), appOnly);
          // if the call target resides in a phantom class then "target" will be null;
          // simply do not add the target in that case
          if (target != null) {
            targetsQueue.add(target);
          }
        } else {
          SootMethodRef ref = null;
          Type receiverType = receiver.getType();

          // Fake edges map to a different method signature, e.g., from execute(a) to a.run()
          if (receiverType instanceof RefType) {
            SootClass receiverClass = ((RefType) receiverType).getSootClass();
            Matcher m = PATTERN_METHOD_SUBSIG.matcher(site.subSig().toString());
            if (m.matches()) {
              String methodName = m.group("methodName");
              String returnType = m.group("returnType");
              if (methodName != null && returnType != null) {
                List<Type> params = new ArrayList<>();
                String parameters = m.group("parameters");
                if (parameters != null && !parameters.isEmpty()) {
                  for (String p : parameters.split(",")) {
                    params.add(sc.getTypeUnsafeUnescape(p.trim()));
                  }
                }
                ref = sc.makeMethodRef(receiverClass, methodName, params, sc.getTypeUnsafeUnescape(returnType),
                    Kind.isStatic(site.kind()));
              }
            }
          } else {
            ref = site.getStmt().getInvokeExpr().getMethodRef();
          }

          if (ref != null) {
            virtualCalls.resolve(type, receiver.getType(), ref, site.getContainer(), targetsQueue, appOnly);
            if (!targets.hasNext() && options.resolve_all_abstract_invokes()) {
              /*
               * In the situation where we find nothing to resolve an invoke to in the first call, this might be because the
               * type for the invoking object is a abstract class and the method is declared in a parent class. In this
               * situation, when the abstract class has no classes that extend it in the scene, resolve would not find any
               * targets for the invoke, even if the parent contained a possible target.
               *
               * This may have been by design since without a concrete class, we have no idea if the method in the parent
               * class is overridden. However, the same could be said for any non private method in the abstract class (and
               * these all resolve fine inside the abstract class even though there are no sub classes of the abstract
               * class). This makes this situation a corner case.
               *
               * Where as, it used to not resolve any targets in this situation, I want to at least resolve the method in the
               * parent class if there is one (as this is technically a possibility and the only information we have).
               */
              virtualCalls.resolveSuperType(type, receiver.getType(), iie.getMethodRef(), targetsQueue, appOnly);
            }
          }
        }
        while (targets.hasNext()) {
          SootMethod target = targets.next();
          cm.addVirtualEdge(MethodContext.v(site.getContainer(), srcContext), site.getStmt(), target, site.kind(),
              typeContext);
        }
      }
    }
    if (baseToInvokeSite.get(receiver) != null) {
      addBaseType(receiver, srcContext, type);
    }
  }

  protected boolean skipSite(VirtualCallSite site, FastHierarchy fh, Type type) {
    Kind k = site.kind();
    if (k == Kind.THREAD) {
      return !fh.canStoreType(type, clRunnable);
    } else if (k == Kind.EXECUTOR) {
      return !fh.canStoreType(type, clRunnable);
    } else if (k == Kind.ASYNCTASK) {
      return !fh.canStoreType(type, clAsyncTask);
    } else if (k == Kind.HANDLER) {
      return !fh.canStoreType(type, clHandler);
    } else {
      return false;
    }
  }

  public boolean wantStringConstants(Local stringConst) {
    return stringConstToSites.get(stringConst) != null;
  }

  public void addStringConstant(Local l, Context srcContext, String constant) {
    if (constant != null) {
      final Scene sc = Scene.v();
      for (Iterator<VirtualCallSite> siteIt = stringConstToSites.get(l).iterator(); siteIt.hasNext();) {
        final VirtualCallSite site = siteIt.next();
        final int constLen = constant.length();
        if (constLen > 0 && constant.charAt(0) == '[') {
          if (constLen > 2 && constant.charAt(1) == 'L' && constant.charAt(constLen - 1) == ';') {
            constant = constant.substring(2, constLen - 1);
          } else {
            continue;
          }
        }
        if (sc.containsClass(constant)) {
          SootClass sootcls = sc.getSootClass(constant);
          if (!sootcls.isApplicationClass() && !sootcls.isPhantom()) {
            sootcls.setLibraryClass();
          }
          for (SootMethod clinit : EntryPoints.v().clinitsOf(sootcls)) {
            cm.addStaticEdge(MethodContext.v(site.getContainer(), srcContext), site.getStmt(), clinit, Kind.CLINIT);
          }
        } else if (options.verbose()) {
          logger.warn("Class " + constant + " is a dynamic class and was not specified as such; graph will be incomplete!");
        }
      }
    } else if (options.verbose()) {
      for (Iterator<VirtualCallSite> siteIt = stringConstToSites.get(l).iterator(); siteIt.hasNext();) {
        final VirtualCallSite site = siteIt.next();
        logger.warn("Method " + site.getContainer() + " is reachable, and calls Class.forName on a non-constant"
            + " String; graph will be incomplete! Use safe-forname option for a conservative result.");
      }
    }
  }

  public boolean wantArrayField(AllocDotField df) {
    return allocDotFieldToLocal.containsKey(df);
  }

  public void addInvokeArgType(AllocDotField df, Context context, Type type) {
    if (allocDotFieldToLocal.containsKey(df)) {
      for (Local l : allocDotFieldToLocal.get(df)) {
        addInvokeArgType(l, context, type);
      }
    }
  }

  public boolean wantInvokeArg(Local receiver) {
    return invokeArgsToInvokeSite.containsKey(receiver);
  }

  public void addInvokeArgDotField(Local receiver, AllocDotField dot) {
    allocDotFieldToLocal.put(dot, receiver);
  }

  /*
   * How type based reflection resolution works:
   *
   * In general, for each call to invoke(), we record the local of the receiver argument and the argument array. Whenever a
   * new type is added to the points to set of the receiver argument we add that type to the reachingBaseTypes and try to
   * resolve the reflective method call (see addType, addBaseType, and updatedNode() in OnFlyCallGraph).
   *
   * For added precision, we also record the second argument to invoke. If it is always null, this means the invoke() call
   * resolves only to nullary methods.
   *
   * When the second argument is a variable that must not be null we can narrow down the called method based on the possible
   * sizes of the argument array and the types it contains. Whenever a new allocation reaches this variable we record the
   * possible size of the array (by looking at the allocation site) and the possible types stored in the array (see
   * updatedNode in OnFlyCallGraph in the branch wantInvokeArg()). If the size of the array isn't statically known, the
   * analysis considers methods of all possible arities. In addition, we track the PAG node corresponding to the array
   * contents. If a new type reaches this node, we update the possible argument types. (see propagate() in PropWorklist and
   * the visitor, and updatedFieldRef in OnFlyCallGraph).
   *
   * For details on the method resolution process, see resolveInvoke()
   *
   * Finally, for cases like o.invoke(b, foo, bar, baz); it is very easy to statically determine precisely which types are in
   * which argument positions. This is computed using the ConstantArrayAnalysis and are resolved using resolveStaticTypes().
   */
  private void addInvokeCallSite(Stmt s, SootMethod container, InstanceInvokeExpr d) {
    Local l = (Local) d.getArg(0);
    Value argArray = d.getArg(1);
    InvokeCallSite ics;
    if (argArray instanceof NullConstant) {
      ics = new InvokeCallSite(s, container, d, l);
    } else {
      if (analysisKey != container) {
        ExceptionalUnitGraph graph = new ExceptionalUnitGraph(container.getActiveBody());
        nullnessCache = new NullnessAnalysis(graph);
        arrayCache = new ConstantArrayAnalysis(graph, container.getActiveBody());
        analysisKey = container;
      }
      Local argLocal = (Local) argArray;
      int nullnessCode;
      if (nullnessCache.isAlwaysNonNullBefore(s, argLocal)) {
        nullnessCode = InvokeCallSite.MUST_NOT_BE_NULL;
      } else if (nullnessCache.isAlwaysNullBefore(s, argLocal)) {
        nullnessCode = InvokeCallSite.MUST_BE_NULL;
      } else {
        nullnessCode = InvokeCallSite.MAY_BE_NULL;
      }
      if (nullnessCode != InvokeCallSite.MUST_BE_NULL && arrayCache.isConstantBefore(s, argLocal)) {
        ArrayTypes reachingArgTypes = arrayCache.getArrayTypesBefore(s, argLocal);
        if (nullnessCode == InvokeCallSite.MAY_BE_NULL) {
          reachingArgTypes.possibleSizes.add(0);
        }
        ics = new InvokeCallSite(s, container, d, l, reachingArgTypes, nullnessCode);
      } else {
        ics = new InvokeCallSite(s, container, d, l, argLocal, nullnessCode);
        invokeArgsToInvokeSite.put(argLocal, ics);
      }
    }
    baseToInvokeSite.put(l, ics);
  }

  private void addVirtualCallSite(Stmt s, SootMethod m, Local receiver, InstanceInvokeExpr iie, NumberedString subSig,
      Kind kind) {
    List<VirtualCallSite> sites = receiverToSites.get(receiver);
    if (sites == null) {
      receiverToSites.put(receiver, sites = new ArrayList<VirtualCallSite>());
      List<Local> receivers = methodToReceivers.get(m);
      if (receivers == null) {
        methodToReceivers.put(m, receivers = new ArrayList<Local>());
      }
      receivers.add(receiver);
    }
    sites.add(new VirtualCallSite(s, m, iie, subSig, kind));
  }

  private void processNewMethod(SootMethod m) {
    if (m.isConcrete()) {
      Body b = m.retrieveActiveBody();
      getImplicitTargets(m);
      findReceivers(m, b);
    }
  }

  private void findReceivers(SootMethod m, Body b) {
    for (final Unit u : b.getUnits()) {
      final Stmt s = (Stmt) u;
      if (s.containsInvokeExpr()) {
        InvokeExpr ie = s.getInvokeExpr();
        if (ie instanceof InstanceInvokeExpr) {
          InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
          Local receiver = (Local) iie.getBase();
          NumberedString subSig = iie.getMethodRef().getSubSignature();
          addVirtualCallSite(s, m, receiver, iie, subSig, Edge.ieToKind(iie));
          VirtualEdge virtualEdge = virtualEdgeSummaries.getVirtualEdgesMatchingSubSig(subSig);
          if (virtualEdge != null) {
            for (VirtualEdgeTarget t : virtualEdge.targets) {
              if (t instanceof DirectTarget) {
                DirectTarget directTarget = (DirectTarget) t;
                if (t.isBase) {
                  addVirtualCallSite(s, m, receiver, iie, directTarget.targetMethod, virtualEdge.edgeType);
                } else {
                  Value runnable = iie.getArg(t.argIndex);
                  if (runnable instanceof Local) {
                    addVirtualCallSite(s, m, (Local) runnable, iie, directTarget.targetMethod, virtualEdge.edgeType);
                  }
                }
              } else if (t instanceof WrapperTarget) {
                WrapperTarget w = (WrapperTarget) t;
                Local wrapperObject = null;
                if (t.isBase) {
                  wrapperObject = receiver;
                } else {
                  Value runnable = iie.getArg(t.argIndex);
                  if (runnable instanceof Local) {
                    wrapperObject = (Local) runnable;
                  }
                }

                if (wrapperObject != null && receiverToSites.get(wrapperObject) != null) {
                  // addVirtualCallSite() may change receiverToSites, which may lead to a ConcurrentModificationException
                  // I'm not entirely sure whether we ought to deal with the new call sites that are being added, instead of
                  // just working on a snapshot, though.
                  List<VirtualCallSite> callSites = new ArrayList<>(receiverToSites.get(wrapperObject));
                  for (final VirtualCallSite site : callSites) {
                    if (w.registrationSignature == site.subSig()) {
                      for (RegisteredHandlerTarget target : w.targets) {
                        Value runnable = iie.getArg(t.argIndex);
                        if (runnable instanceof Local) {
                          addVirtualCallSite(s, m, (Local) runnable, iie, target.targetMethod, virtualEdge.edgeType);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        } else if (ie instanceof DynamicInvokeExpr) {
          if (options.verbose()) {
            logger.warn("InvokeDynamic to " + ie + " not resolved during call-graph construction.");
          }
        } else {
          SootMethod tgt = ie.getMethod();
          if (tgt != null) {
            addEdge(m, s, tgt);
            String signature = tgt.getSignature();
            VirtualEdge virtualEdge = virtualEdgeSummaries.getVirtualEdgesMatchingFunction(signature);
            if (virtualEdge != null) {
              for (VirtualEdgeTarget t : virtualEdge.targets) {
                if (t instanceof DirectTarget) {
                  DirectTarget directTarget = (DirectTarget) t;
                  if (t.isBase) {
                    // this should not happen
                  } else {
                    Value runnable = ie.getArg(t.argIndex);
                    if (runnable instanceof Local) {
                      addVirtualCallSite(s, m, (Local) runnable, null, directTarget.targetMethod, Kind.GENERIC_FAKE);
                    }
                  }
                }
              }
            }
          } else if (!Options.v().ignore_resolution_errors()) {
            throw new InternalError(
                "Unresolved target " + ie.getMethod() + ". Resolution error should have occured earlier.");
          }
        }
      }
    }
  }

  private void getImplicitTargets(SootMethod source) {
    final SootClass scl = source.getDeclaringClass();
    if (!source.isConcrete()) {
      return;
    }
    if (source.getSubSignature().contains("<init>")) {
      handleInit(source, scl);
    }
    for (Unit u : source.retrieveActiveBody().getUnits()) {
      final Stmt s = (Stmt) u;
      if (s.containsInvokeExpr()) {
        InvokeExpr ie = s.getInvokeExpr();
        SootMethodRef methodRef = ie.getMethodRef();
        switch (methodRef.getDeclaringClass().getName()) {
          case "java.lang.reflect.Method":
            if ("java.lang.Object invoke(java.lang.Object,java.lang.Object[])"
                .equals(methodRef.getSubSignature().getString())) {
              reflectionModel.methodInvoke(source, s);
            }
            break;
          case "java.lang.Class":
            if ("java.lang.Object newInstance()".equals(methodRef.getSubSignature().getString())) {
              reflectionModel.classNewInstance(source, s);
            }
            break;
          case "java.lang.reflect.Constructor":
            if ("java.lang.Object newInstance(java.lang.Object[])".equals(methodRef.getSubSignature().getString())) {
              reflectionModel.contructorNewInstance(source, s);
            }
            break;
        }
        if (methodRef.getSubSignature() == sigForName) {
          reflectionModel.classForName(source, s);
        }
        if (ie instanceof StaticInvokeExpr) {
          SootClass cl = ie.getMethodRef().getDeclaringClass();
          for (SootMethod clinit : EntryPoints.v().clinitsOf(cl)) {
            addEdge(source, s, clinit, Kind.CLINIT);
          }
        }
      }
      if (s.containsFieldRef()) {
        FieldRef fr = s.getFieldRef();
        if (fr instanceof StaticFieldRef) {
          SootClass cl = fr.getFieldRef().declaringClass();
          for (SootMethod clinit : EntryPoints.v().clinitsOf(cl)) {
            addEdge(source, s, clinit, Kind.CLINIT);
          }
        }
      }
      if (s instanceof AssignStmt) {
        Value rhs = ((AssignStmt) s).getRightOp();
        if (rhs instanceof NewExpr) {
          NewExpr r = (NewExpr) rhs;
          SootClass cl = r.getBaseType().getSootClass();
          for (SootMethod clinit : EntryPoints.v().clinitsOf(cl)) {
            addEdge(source, s, clinit, Kind.CLINIT);
          }
        } else if (rhs instanceof NewArrayExpr || rhs instanceof NewMultiArrayExpr) {
          Type t = rhs.getType();
          if (t instanceof ArrayType) {
            t = ((ArrayType) t).baseType;
          }
          if (t instanceof RefType) {
            SootClass cl = ((RefType) t).getSootClass();
            for (SootMethod clinit : EntryPoints.v().clinitsOf(cl)) {
              addEdge(source, s, clinit, Kind.CLINIT);
            }
          }
        }
      }
    }
  }

  protected void processNewMethodContext(MethodOrMethodContext momc) {
    SootMethod m = momc.method();
    for (Iterator<Edge> it = cicg.edgesOutOf(m); it.hasNext();) {
      Edge e = it.next();
      cm.addStaticEdge(momc, e.srcUnit(), e.tgt(), e.kind());
    }
  }

  private void handleInit(SootMethod source, final SootClass scl) {
    addEdge(source, null, scl, sigFinalize, Kind.FINALIZE);
  }

  private void constantForName(final String cls, SootMethod src, Stmt srcUnit) {
    final int clsLen = cls.length();
    if (clsLen > 0 && cls.charAt(0) == '[') {
      if (clsLen > 2 && cls.charAt(1) == 'L' && cls.charAt(clsLen - 1) == ';') {
        constantForName(cls.substring(2, clsLen - 1), src, srcUnit);
      }
    } else {
      final Scene sc = Scene.v();
      if (sc.containsClass(cls)) {
        SootClass sootcls = sc.getSootClass(cls);
        if (!sootcls.isPhantomClass()) {
          if (!sootcls.isApplicationClass()) {
            sootcls.setLibraryClass();
          }
          for (SootMethod clinit : EntryPoints.v().clinitsOf(sootcls)) {
            addEdge(src, srcUnit, clinit, Kind.CLINIT);
          }
        }
      } else if (options.verbose()) {
        logger.warn("Class " + cls + " is a dynamic class and was not specified as such; graph will be incomplete!");
      }
    }
  }

  private void addEdge(SootMethod src, Stmt stmt, SootMethod tgt, Kind kind) {
    if (src.equals(tgt) && src.isStaticInitializer()) { 
      return; 
    }
    cicg.addEdge(new Edge(src, stmt, tgt, kind));
  }

  private void addEdge(SootMethod src, Stmt stmt, SootClass cls, NumberedString methodSubSig, Kind kind) {
    SootMethod sm = cls.getMethodUnsafe(methodSubSig);
    if (sm != null) {
      addEdge(src, stmt, sm, kind);
    }
  }

  private void addEdge(SootMethod src, Stmt stmt, SootMethod tgt) {
    InvokeExpr ie = stmt.getInvokeExpr();
    addEdge(src, stmt, tgt, Edge.ieToKind(ie));
  }

  public class DefaultReflectionModel implements ReflectionModel {

    protected final CGOptions options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));

    protected final HashSet<SootMethod> warnedAlready = new HashSet<SootMethod>();

    @Override
    public void classForName(SootMethod source, Stmt s) {
      List<Local> stringConstants = methodToStringConstants.get(source);
      if (stringConstants == null) {
        methodToStringConstants.put(source, stringConstants = new ArrayList<Local>());
      }
      Value className = s.getInvokeExpr().getArg(0);
      if (className instanceof StringConstant) {
        String cls = ((StringConstant) className).value;
        constantForName(cls, source, s);
      } else if (className instanceof Local) {
        Local constant = (Local) className;
        if (options.safe_forname()) {
          for (SootMethod tgt : EntryPoints.v().clinits()) {
            addEdge(source, s, tgt, Kind.CLINIT);
          }
        } else {
          final EntryPoints ep = EntryPoints.v();
          for (SootClass cls : Scene.v().dynamicClasses()) {
            for (SootMethod clinit : ep.clinitsOf(cls)) {
              addEdge(source, s, clinit, Kind.CLINIT);
            }
          }
          VirtualCallSite site = new VirtualCallSite(s, source, null, null, Kind.CLINIT);
          List<VirtualCallSite> sites = stringConstToSites.get(constant);
          if (sites == null) {
            stringConstToSites.put(constant, sites = new ArrayList<VirtualCallSite>());
            stringConstants.add(constant);
          }
          sites.add(site);
        }
      }
    }

    @Override
    public void classNewInstance(SootMethod source, Stmt s) {
      if (options.safe_newinstance()) {
        for (SootMethod tgt : EntryPoints.v().inits()) {
          addEdge(source, s, tgt, Kind.NEWINSTANCE);
        }
      } else {
        for (SootClass cls : Scene.v().dynamicClasses()) {
          SootMethod sm = cls.getMethodUnsafe(sigInit);
          if (sm != null) {
            addEdge(source, s, sm, Kind.NEWINSTANCE);
          }
        }

        if (options.verbose()) {
          logger.warn("Method " + source + " is reachable, and calls Class.newInstance; graph will be incomplete!"
              + " Use safe-newinstance option for a conservative result.");
        }
      }
    }

    @Override
    public void contructorNewInstance(SootMethod source, Stmt s) {
      if (options.safe_newinstance()) {
        for (SootMethod tgt : EntryPoints.v().allInits()) {
          addEdge(source, s, tgt, Kind.NEWINSTANCE);
        }
      } else {
        for (SootClass cls : Scene.v().dynamicClasses()) {
          for (SootMethod m : cls.getMethods()) {
            if ("<init>".equals(m.getName())) {
              addEdge(source, s, m, Kind.NEWINSTANCE);
            }
          }
        }
        if (options.verbose()) {
          logger.warn("Method " + source + " is reachable, and calls Constructor.newInstance; graph will be incomplete!"
              + " Use safe-newinstance option for a conservative result.");
        }
      }
    }

    @Override
    public void methodInvoke(SootMethod container, Stmt invokeStmt) {
      if (!warnedAlready(container)) {
        if (options.verbose()) {
          logger.warn("Call to java.lang.reflect.Method: invoke() from " + container + "; graph will be incomplete!");
        }
        markWarned(container);
      }
    }

    private void markWarned(SootMethod m) {
      warnedAlready.add(m);
    }

    private boolean warnedAlready(SootMethod m) {
      return warnedAlready.contains(m);
    }
  }

  public class TypeBasedReflectionModel extends DefaultReflectionModel {
    @Override
    public void methodInvoke(SootMethod container, Stmt invokeStmt) {
      if (container.getDeclaringClass().isJavaLibraryClass()) {
        super.methodInvoke(container, invokeStmt);
        return;
      }
      InstanceInvokeExpr d = (InstanceInvokeExpr) invokeStmt.getInvokeExpr();
      Value base = d.getArg(0);
      // TODO no support for statics at the moment

      // SA: Better just fall back to degraded functionality than fail altogether
      if (!(base instanceof Local)) {
        super.methodInvoke(container, invokeStmt);
        return;
      }
      addInvokeCallSite(invokeStmt, container, d);
    }
  }

  public class TraceBasedReflectionModel implements ReflectionModel {

    protected final Set<Guard> guards;
    protected final ReflectionTraceInfo reflectionInfo;

    private TraceBasedReflectionModel() {
      String logFile = options.reflection_log();
      if (logFile == null) {
        throw new InternalError("Trace based refection model enabled but no trace file given!?");
      }

      this.reflectionInfo = new ReflectionTraceInfo(logFile);
      this.guards = new HashSet<Guard>();
    }

    /**
     * Adds an edge to all class initializers of all possible receivers of Class.forName() calls within source.
     */
    @Override
    public void classForName(SootMethod container, Stmt forNameInvokeStmt) {
      Set<String> classNames = reflectionInfo.classForNameClassNames(container);
      if (classNames == null || classNames.isEmpty()) {
        registerGuard(container, forNameInvokeStmt,
            "Class.forName() call site; Soot did not expect this site to be reached");
      } else {
        for (String clsName : classNames) {
          constantForName(clsName, container, forNameInvokeStmt);
        }
      }
    }

    /**
     * Adds an edge to the constructor of the target class from this call to {@link Class#newInstance()}.
     */
    @Override
    public void classNewInstance(SootMethod container, Stmt newInstanceInvokeStmt) {
      Set<String> classNames = reflectionInfo.classNewInstanceClassNames(container);
      if (classNames == null || classNames.isEmpty()) {
        registerGuard(container, newInstanceInvokeStmt,
            "Class.newInstance() call site; Soot did not expect this site to be reached");
      } else {
        final Scene sc = Scene.v();
        for (String clsName : classNames) {
          SootMethod constructor = sc.getSootClass(clsName).getMethodUnsafe(sigInit);
          if (constructor != null) {
            addEdge(container, newInstanceInvokeStmt, constructor, Kind.REFL_CLASS_NEWINSTANCE);
          }
        }
      }
    }

    /**
     * Adds a special edge of kind {@link Kind#REFL_CONSTR_NEWINSTANCE} to all possible target constructors of this call to
     * {@link Constructor#newInstance(Object...)}. Those kinds of edges are treated specially in terms of how parameters are
     * assigned, as parameters to the reflective call are passed into the argument array of
     * {@link Constructor#newInstance(Object...)}.
     *
     * @see PAG#addCallTarget(Edge)
     */
    @Override
    public void contructorNewInstance(SootMethod container, Stmt newInstanceInvokeStmt) {
      Set<String> constructorSignatures = reflectionInfo.constructorNewInstanceSignatures(container);
      if (constructorSignatures == null || constructorSignatures.isEmpty()) {
        registerGuard(container, newInstanceInvokeStmt,
            "Constructor.newInstance(..) call site; Soot did not expect this site to be reached");
      } else {
        final Scene sc = Scene.v();
        for (String constructorSignature : constructorSignatures) {
          SootMethod constructor = sc.getMethod(constructorSignature);
          addEdge(container, newInstanceInvokeStmt, constructor, Kind.REFL_CONSTR_NEWINSTANCE);
        }
      }
    }

    /**
     * Adds a special edge of kind {@link Kind#REFL_INVOKE} to all possible target methods of this call to
     * {@link Method#invoke(Object, Object...)}. Those kinds of edges are treated specially in terms of how parameters are
     * assigned, as parameters to the reflective call are passed into the argument array of
     * {@link Method#invoke(Object, Object...)}.
     *
     * @see PAG#addCallTarget(Edge)
     */
    @Override
    public void methodInvoke(SootMethod container, Stmt invokeStmt) {
      Set<String> methodSignatures = reflectionInfo.methodInvokeSignatures(container);
      if (methodSignatures == null || methodSignatures.isEmpty()) {
        registerGuard(container, invokeStmt, "Method.invoke(..) call site; Soot did not expect this site to be reached");
      } else {
        final Scene sc = Scene.v();
        for (String methodSignature : methodSignatures) {
          SootMethod method = sc.getMethod(methodSignature);
          addEdge(container, invokeStmt, method, Kind.REFL_INVOKE);
        }
      }
    }

    private void registerGuard(SootMethod container, Stmt stmt, String string) {
      guards.add(new Guard(container, stmt, string));

      if (options.verbose()) {
        logger.debug("Incomplete trace file: Class.forName() is called in method '" + container
            + "' but trace contains no information about the receiver class of this call.");
        switch (options.guards()) {
          case "ignore":
            logger.debug("Guarding strategy is set to 'ignore'. Will ignore this problem.");
            break;
          case "print":
            logger.debug("Guarding strategy is set to 'print'. "
                + "Program will print a stack trace if this location is reached during execution.");
            break;
          case "throw":
            logger.debug("Guarding strategy is set to 'throw'. "
                + "Program will throw an error if this location is reached during execution.");
            break;
          default:
            throw new RuntimeException("Invalid value for phase option (guarding): " + options.guards());
        }
      }

      if (!registeredGuardsTransformation) {
        registeredGuardsTransformation = true;
        PackManager.v().getPack("wjap").add(new Transform("wjap.guards", new SceneTransformer() {

          @Override
          protected void internalTransform(String phaseName, Map<String, String> options) {
            for (Guard g : guards) {
              insertGuard(g);
            }
          }
        }));
        PhaseOptions.v().setPhaseOption("wjap.guards", "enabled");
      }
    }

    private void insertGuard(Guard guard) {
      if ("ignore".equals(options.guards())) {
        return;
      }

      SootMethod container = guard.container;
      if (!container.hasActiveBody()) {
        logger.warn("Tried to insert guard into " + container + " but couldn't because method has no body.");
      } else {
        final Jimple jimp = Jimple.v();
        final Body body = container.getActiveBody();
        final UnitPatchingChain units = body.getUnits();
        final LocalGenerator lg = new LocalGenerator(body);

        // exc = new Error
        RefType runtimeExceptionType = RefType.v("java.lang.Error");
        Local exceptionLocal = lg.generateLocal(runtimeExceptionType);
        AssignStmt assignStmt = jimp.newAssignStmt(exceptionLocal, jimp.newNewExpr(runtimeExceptionType));
        units.insertBefore(assignStmt, guard.stmt);

        // exc.<init>(message)
        SootMethodRef cref = runtimeExceptionType.getSootClass()
            .getMethod("<init>", Collections.<Type>singletonList(RefType.v("java.lang.String"))).makeRef();
        InvokeStmt initStmt
            = jimp.newInvokeStmt(jimp.newSpecialInvokeExpr(exceptionLocal, cref, StringConstant.v(guard.message)));
        units.insertAfter(initStmt, assignStmt);

        switch (options.guards()) {
          case "print":
            // logger.error(exc.getMessage(), exc);
            VirtualInvokeExpr printStackTraceExpr = jimp.newVirtualInvokeExpr(exceptionLocal, Scene.v()
                .getSootClass("java.lang.Throwable").getMethod("printStackTrace", Collections.<Type>emptyList()).makeRef());
            units.insertAfter(jimp.newInvokeStmt(printStackTraceExpr), initStmt);
            break;
          case "throw":
            units.insertAfter(jimp.newThrowStmt(exceptionLocal), initStmt);
            break;
          default:
            throw new RuntimeException("Invalid value for phase option (guarding): " + options.guards());
        }
      }
    }
  }

  static final class Guard {
    final SootMethod container;
    final Stmt stmt;
    final String message;

    public Guard(SootMethod container, Stmt stmt, String message) {
      this.container = container;
      this.stmt = stmt;
      this.message = message;
    }
  }

  private static abstract class AbstractMethodIterator implements Iterator<SootMethod> {
    private SootMethod next;
    private SootClass currClass;
    private Iterator<SootMethod> methodIterator;

    AbstractMethodIterator(SootClass baseClass) {
      this.currClass = baseClass;
      this.next = null;
      this.methodIterator = baseClass.methodIterator();
      this.findNextMethod();
    }

    protected void findNextMethod() {
      next = null;
      if (methodIterator != null) {
        while (true) {
          while (methodIterator.hasNext()) {
            SootMethod n = methodIterator.next();
            if (!n.isPublic() || n.isStatic() || n.isConstructor() || n.isStaticInitializer() || !n.isConcrete()) {
              continue;
            }
            if (!acceptMethod(n)) {
              continue;
            }
            next = n;
            return;
          }
          if (!currClass.hasSuperclass()) {
            methodIterator = null;
            return;
          }
          SootClass superclass = currClass.getSuperclass();
          if (superclass.isPhantom() || "java.lang.Object".equals(superclass.getName())) {
            methodIterator = null;
            return;
          } else {
            methodIterator = superclass.methodIterator();
            currClass = superclass;
          }
        }
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public SootMethod next() {
      SootMethod toRet = next;
      findNextMethod();
      return toRet;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    protected abstract boolean acceptMethod(SootMethod m);
  }
}
