package soot.jimple.toolkits.typing.fast;

import java.util.ArrayDeque;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2008 Ben Bellamy
 *
 * All rights reserved.
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.G;
import soot.IntegerType;
import soot.Local;
import soot.LocalGenerator;
import soot.NullType;
import soot.PatchingChain;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.UnknownType;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NegExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.typing.Util;
import soot.jimple.toolkits.typing.fast.UseChecker.UseCheckerCache;
import soot.options.JBTROptions;
import soot.tagkit.TagManager;
import soot.toolkits.scalar.LocalDefs;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

/**
 * New Type Resolver by Ben Bellamy (see 'Efficient Local Type Inference' at OOPSLA 08).
 *
 * Ben has tested this code, and verified that it provides a typing that is at least as tight as the original algorithm
 * (tighter in 2914 methods out of 295598) on a number of benchmarks. These are: abc-complete.jar, BlueJ, CSO (Scala code),
 * Gant, Groovy, havoc.jar, Java 3D, jEdit, Java Grande Forum, Jigsaw, Jython, Kawa, rt.jar, Kawa, Scala and tools.jar. The
 * mean execution time improvement is around 10 times, but for the longest methods (abc parser methods and havoc with >9000
 * statements) the improvement is between 200 and 500 times.
 *
 * @author Ben Bellamy
 */
public class TypeResolver {
  private static final int SINGLE_THREAD_LIMIT = 100000;
  private static final Function<? super Local, ? extends Collection<Unit>> CREATE_NEW_SET
      = new Function<Local, Collection<Unit>>() {

        @Override
        public Collection<Unit> apply(Local t) {
          return new HashSet<>();
        }

      };
  private static final int MAX_ITERATION_COUNT = 10000;
  private static int NUM_CORES = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

  protected final JimpleBody jb;

  private List<DefinitionStmt> assignments;
  private HashMap<Local, BitSet> depends;
  private Set<Local> singleAssignments;
  private BitSet simple;
  private final LocalGenerator localGenerator;
  private final UseCheckerCache useCheckerCache;
  private final JBTROptions opt;
  private final TagManager tagManager = TagManager.v();

  private MultiMap<Local, Unit> localToUses;

  public TypeResolver(JimpleBody jb, JBTROptions opt) {
    this.jb = jb;
    this.localGenerator = Scene.v().createLocalGenerator(jb);
    this.useCheckerCache = new UseCheckerCache(jb);
    this.opt = opt;

  }

  private void initAssignments() {
    this.depends = new HashMap<Local, BitSet>(jb.getLocalCount());
    Map<Local, Integer> map = new HashMap<>();
    for (Unit stmt : this.jb.getUnits()) {
      if (stmt instanceof DefinitionStmt) {
        DefinitionStmt def = (DefinitionStmt) stmt;

        Value lhs = def.getLeftOp();
        if (lhs instanceof Local) {
          Local l = (Local) lhs;
          Integer c = map.get(l);
          if (c == null) {
            c = 0;
          }
          c++;
          map.put(l, c);
        }
      }
    }
    Iterator<Entry<Local, Integer>> t = map.entrySet().iterator();
    while (t.hasNext()) {
      if (t.next().getValue() > 1) {
        t.remove();
      }
    }
    this.singleAssignments = map.keySet();

    this.assignments = new ArrayList<DefinitionStmt>();
    for (Unit stmt : this.jb.getUnits()) {
      if (stmt instanceof DefinitionStmt) {
        this.initAssignment((DefinitionStmt) stmt);
      }
    }
  }

  private void initAssignment(DefinitionStmt ds) {
    Value lhs = ds.getLeftOp();
    if (lhs instanceof Local || lhs instanceof ArrayRef) {
      int assignmentIdx = this.assignments.size();
      this.assignments.add(ds);

      Value rhs = ds.getRightOp();
      if (rhs instanceof Local) {
        this.addDepend((Local) rhs, assignmentIdx);
      } else if (rhs instanceof BinopExpr) {
        BinopExpr be = (BinopExpr) rhs;
        Value lop = be.getOp1(), rop = be.getOp2();
        if (lop instanceof Local) {
          this.addDepend((Local) lop, assignmentIdx);
        }
        if (rop instanceof Local) {
          this.addDepend((Local) rop, assignmentIdx);
        }
      } else if (rhs instanceof NegExpr) {
        Value op = ((NegExpr) rhs).getOp();
        if (op instanceof Local) {
          this.addDepend((Local) op, assignmentIdx);
        }
      } else if (rhs instanceof CastExpr) {
        Value op = ((CastExpr) rhs).getOp();
        if (op instanceof Local) {
          this.addDepend((Local) op, assignmentIdx);
        }
      } else if (rhs instanceof ArrayRef) {
        this.addDepend((Local) ((ArrayRef) rhs).getBase(), assignmentIdx);
      }
    }
  }

  private void addDepend(Local v, int stmtIndex) {
    BitSet d = this.depends.get(v);
    if (d == null) {
      d = new BitSet();
      this.depends.put(v, d);
    }
    d.set(stmtIndex);
  }

  public void inferTypes() {
    this.split_new();
    // split_new creates new assignments...
    this.initAssignments();
    ITypingStrategy typingStrategy = getTypingStrategy();
    AugEvalFunction ef = createAugEvalFunction(this.jb);
    BytecodeHierarchy bh = createBytecodeHierarchy();
    PartialConstantTyping constantTyping = new PartialConstantTyping(typingStrategy.createEmptyTyping(this.jb.getLocals()));
    Collection<ITyping> sigma = this.applyAssignmentConstraints(constantTyping, ef, bh);

    // If there is nothing to type, we can quit
    if (sigma.isEmpty()) {
      return;
    }

    AtomicInteger castCount = new AtomicInteger();
    ITyping tg = this.minCasts(sigma, bh, castCount);
    if (castCount.get() > 0) {
      this.insertCasts(tg, bh, false, Integer.MAX_VALUE);
    }

    final BottomType bottom = BottomType.v();
    for (Local v : this.jb.getLocals()) {
      Type t = tg.get(v);
      if (t instanceof IntegerType) {
        // t = inttype;
        tg.set(v, bottom);
      }
      v.setType(t);
    }

    tg = this.typePromotion(tg);
    if (tg == null) {
      // Use original soot algorithm for inserting casts
      soot.jimple.toolkits.typing.integer.TypeResolver.resolve(this.jb);
    } else {
      for (Local v : this.jb.getLocals()) {
        Type type = tg.get(v);
        v.setType(type);
      }
    }
    RefType obj = Scene.v().getObjectType();
    for (Local v : this.jb.getLocals()) {
      Type type = v.getType();
      if (type instanceof WeakObjectType) {
        v.setType(obj);
      }
    }
  }

  protected BytecodeHierarchy createBytecodeHierarchy() {
    return new BytecodeHierarchy();
  }

  protected AugEvalFunction createAugEvalFunction(JimpleBody jb) {
    return new AugEvalFunction(jb);
  }

  protected ITypingStrategy getTypingStrategy() {
    return DefaultTypingStrategy.INSTANCE;
  }

  public class CastInsertionUseVisitor implements IUseVisitor {
    protected JimpleBody jb;
    protected ITyping tg;
    protected IHierarchy h;

    private final boolean countOnly;
    private int count;
    protected boolean eliminateUnnecessaryCasts = eliminateUnnecessaryCasts();
    private final int maximumCasts;

    public CastInsertionUseVisitor(boolean countOnly, JimpleBody jb, ITyping tg, IHierarchy h, int maximumCasts) {
      this.jb = jb;
      this.tg = tg;
      this.h = h;

      this.countOnly = countOnly;
      this.maximumCasts = maximumCasts;
      this.count = 0;
    }

    protected boolean eliminateUnnecessaryCasts() {
      return true;
    }

    @Override
    public Value visit(Value op, Type useType, Stmt stmt, boolean checkOnly) {
      Type t = AugEvalFunction.eval_(this.tg, op, stmt, this.jb);
      if (useType == t) {
        if (op instanceof CastExpr) {
          CastExpr ce = (CastExpr) op;
          // by default, t only checks for the type of the cast target
          t = AugEvalFunction.eval_(this.tg, ce.getOp(), stmt, this.jb);
          if (eliminateUnnecessaryCasts && ce.getType() == t) {
            // no cast necessary!
            return ce.getOp();
          }
        }
        return op;
      }

      NeedCastResult needCast = needCast(useType, t, h);
      if (needCast == NeedCastResult.DOESNT_NEED_CAST && this.h.ancestor(useType, t)) {
        return op;
      }

      this.count++;
      if (count > maximumCasts) {
        return op;
      }

      if (countOnly || needCast == NeedCastResult.DISCOURAGED_TARGET_TYPE) {
        return op;
      } else {
        // If we're referencing an array of the base type java.lang.Object,
        // we also need to fix the type of the assignment's target variable.
        if (stmt.containsArrayRef() && stmt.getArrayRef().getBase() == op && stmt instanceof DefinitionStmt) {
          Value leftOp = ((DefinitionStmt) stmt).getLeftOp();
          if (leftOp instanceof Local) {
            Type baseType = tg.get((Local) op);
            if (baseType instanceof RefType && isObjectLikeType((RefType) baseType)) {
              tg.set((Local) leftOp, ((ArrayType) useType).getElementType());
            }
          }
        }

        Local vold;
        if (op instanceof Local) {
          vold = (Local) op;
        } else {
          /*
           * By the time we have countOnly == false, all variables must by typed with concrete Jimple types, and never
           * [0..1], [0..127] or [0..32767].
           */
          vold = localGenerator.generateLocal(t);
          this.tg.set(vold, t);
          AssignStmt newU = Jimple.v().newAssignStmt(vold, op);
          tagManager.copyLineTags(newU, stmt);
          this.jb.getUnits().insertBefore(newU, Util.findFirstNonIdentityUnit(this.jb, stmt));
        }
        // Cast from the original type to the type that we use in the code
        return createCast(useType, stmt, vold, false);
      }
    }

    protected NeedCastResult needCast(Type target, Type from, IHierarchy h) {
      if (target instanceof PrimType && from instanceof PrimType) {
        if (from.isAllowedInFinalCode() && target.isAllowedInFinalCode()) {
          return NeedCastResult.NEEDS_CAST;
        }
      }
      return NeedCastResult.DOESNT_NEED_CAST;
    }

    private boolean isObjectLikeType(RefType rt) {
      if (rt instanceof WeakObjectType) {
        return true;
      } else {
        final String name = rt.getSootClass().getName();
        return "java.lang.Object".equals(name) || "java.io.Serializable".equals(name) || "java.lang.Cloneable".equals(name);
      }
    }

    /**
     * Creates a cast at stmt of vold to the given type.
     *
     * @param useType
     *          the new type
     * @param stmt
     *          stmt
     * @param old
     *          the old local
     * @param after
     *          True to insert the cast after the statement, false to insert it before
     * @return the new local
     */
    protected Local createCast(Type useType, Stmt stmt, Local old, boolean after) {
      Local vnew = localGenerator.generateLocal(useType);
      this.tg.set(vnew, useType);
      Jimple jimple = Jimple.v();
      AssignStmt newStmt = jimple.newAssignStmt(vnew, jimple.newCastExpr(old, useType));
      Unit u = Util.findFirstNonIdentityUnit(this.jb, stmt);
      if (after) {
        this.jb.getUnits().insertAfter(newStmt, u);
      } else {
        this.jb.getUnits().insertBefore(newStmt, u);
      }
      return vnew;
    }

    public int getCount() {
      return this.count;
    }

    @Override
    public boolean finish() {
      return count >= maximumCasts;
    }
  }

  final BooleanType booleanType = BooleanType.v();
  final ByteType byteType = ByteType.v();
  final ShortType shortType = ShortType.v();

  protected ITyping typePromotion(ITyping tg) {
    boolean conversionsPending;
    int iterations = 0;
    do {
      AugEvalFunction ef = createAugEvalFunction(this.jb);
      AugHierarchy h = new AugHierarchy();
      UseChecker uc = createUseChecker(this.jb);
      TypePromotionUseVisitor uv = createTypePromotionUseVisitor(jb, tg);
      do {
        Collection<ITyping> sigma = this.applyAssignmentConstraints(tg, ef, h);
        if (sigma.isEmpty()) {
          return null;
        }
        tg = sigma.iterator().next();
        uv.typingChanged = false;
        uc.check(tg, uv, this.jb.getUnits().snapshotIterator());
        if (uv.fail) {
          return null;
        }

      } while (uv.typingChanged);

      conversionsPending = false;
      for (Local v : this.jb.getLocals()) {
        Type t = tg.get(v);
        Type r = convert(t);
        if (r != null) {
          tg.set(v, r);
          conversionsPending = true;
        }
      }
      if (iterations > MAX_ITERATION_COUNT) {
        throw new RuntimeException(String.format("Maximum number of iterations (%s) reached", MAX_ITERATION_COUNT));
      }
    } while (conversionsPending);

    return tg;
  }

  protected UseChecker createUseChecker(JimpleBody jb) {
    return new UseChecker(jb, useCheckerCache);
  }

  protected TypePromotionUseVisitor createTypePromotionUseVisitor(JimpleBody jb, ITyping tg) {
    return new TypePromotionUseVisitor(jb, tg);
  }

  protected Type convert(Type t) {
    if (t instanceof Integer1Type) {
      return booleanType;
    } else if (t instanceof Integer127Type) {
      return byteType;
    } else if (t instanceof Integer32767Type) {
      return shortType;
    } else if (t instanceof WeakObjectType) {
      return RefType.v(((WeakObjectType) t).getClassName());
    } else if (t instanceof ArrayType) {
      ArrayType r = (ArrayType) t;
      Type cv = convert(r.getElementType());
      if (cv != null) {
        return ArrayType.v(cv, r.numDimensions);
      }
    }
    return null;
  }

  private int insertCasts(ITyping tg, IHierarchy h, boolean countOnly, int maximumCasts) {
    UseChecker uc = createUseChecker(this.jb);
    CastInsertionUseVisitor uv = createCastInsertionUseVisitor(tg, h, countOnly, maximumCasts);
    UnitPatchingChain units = this.jb.getUnits();
    uc.check(tg, uv, countOnly ? units.iterator() : units.snapshotIterator());
    return uv.getCount();
  }

  /**
   * Allows clients to provide an own visitor for cast insertion
   *
   * @param tg
   *          the typing
   * @param h
   *          the hierarchy
   * @param countOnly
   *          whether to count only (no actual changes)
   * @param maximumCasts
   *          the maximum casts to insert (aborts when max casts have been reached). Only use when countOnly is true,
   *          otherwise use MAX_INT
   * @return the visitor
   */
  protected CastInsertionUseVisitor createCastInsertionUseVisitor(ITyping tg, IHierarchy h, boolean countOnly,
      int maximumCasts) {
    return new CastInsertionUseVisitor(countOnly, this.jb, tg, h, maximumCasts);
  }

  private ITyping minCasts(Collection<ITyping> sigma, IHierarchy h, AtomicInteger castCount) {
    castCount.set(Integer.MAX_VALUE);
    ITyping r = null;
    int sz = sigma.size();
    if (sz <= SINGLE_THREAD_LIMIT) {
      if (sz == 0) {
        // compatibility
        castCount.set(-1);
      }
      for (ITyping tg : sigma) {
        int n = this.insertCasts(tg, h, true, castCount.get());
        int g = castCount.get();
        if (n < g) {
          castCount.set(n);
          r = tg;
          if (n == 0) {
            // We can't be better than this
            break;
          }
        }
      }
      return r;
    } else {
      ExecutorService executionService = Executors.newFixedThreadPool(Math.min(NUM_CORES, sigma.size()));
      ITyping[] minTyping = new ITyping[1];
      try {
        for (ITyping tg : sigma) {
          executionService.submit(new Runnable() {

            @Override
            public void run() {
              final int prevBest = castCount.get();
              if (prevBest == 0) {
                // We can't be better than this
                return;
              }
              int n = insertCasts(tg, h, true, prevBest);
              while (true) {
                int t = castCount.get();
                if (n < t) {
                  if (castCount.compareAndSet(t, n)) {
                    // update successful!
                    minTyping[0] = tg;
                    break;
                  }
                } else {
                  // worse than the current typing
                  break;
                }
              }
            }

          });
        }
      } finally {
        executionService.shutdown();
        try {
          executionService.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
          throw new RuntimeException("Interrupted during type resolving", e);
        }
      }
      return minTyping[0];
    }
  }

  static class WorklistElement {
    ITyping typing;
    BitSet worklist;
    TypeDecision decision;

    public WorklistElement(ITyping tg, BitSet wl, TypeDecision decision) {
      this.typing = tg;
      this.worklist = wl;
      this.decision = decision;
    }

    @Override
    public String toString() {
      return "Left in worklist: " + worklist.size() + ", typing: " + typing;
    }
  }

  static class TypeContainer {
    // First assigned type
    Type lType;
    // First assigned type
    Type rType;

    // The base type decision from lType and rType.
    Type target;

    TypeContainer(Type l, Type r, Type t) {
      this.lType = l;
      this.rType = r;
      this.target = t;
    }
  }

  static class TypeDecision {

    // Record type decision for different type tuples.
    List<TypeContainer> containers;

    TypeDecision() {
      containers = new ArrayList<>();
    }

    public void addTypeDecision(TypeContainer container) {
      for (TypeContainer c : this.containers) {
        if ((typesEqual(container.lType, c.lType) && typesEqual(container.rType, c.rType)
            || (typesEqual(container.lType, c.rType) && typesEqual(container.rType, c.lType)))) {
          return;
        }
      }
      this.containers.add(container);
    }

    public Type getTypeDecision(Type l, Type r) {
      for (TypeContainer container : this.containers) {
        if (typesEqual(container.lType, l) && typesEqual(container.rType, r)) {
          return container.target;
        }
        if (typesEqual(container.lType, r) && typesEqual(container.rType, l)) {
          return container.target;
        }
      }
      return BottomType.v();
    }

    public TypeDecision copy() {
      TypeDecision decision = new TypeDecision();
      for (TypeContainer container : this.containers) {
        decision.containers.add(new TypeContainer(container.lType, container.rType, container.target));
      }
      return decision;
    }

    public void removeLast() {
      // Remove the last one.
      if (!this.containers.isEmpty()) {
        this.containers.remove(this.containers.size() - 1);
      }
    }
  }

  protected Collection<ITyping> applyAssignmentConstraints(ITyping tg, IEvalFunction ef, IHierarchy h) {
    final int numAssignments = this.assignments.size();
    if (numAssignments == 0) {
      return Collections.emptyList();
    }

    ArrayDeque<WorklistElement> sigma = createSigmaQueue();
    List<ITyping> r = createResultList();

    final ITypingStrategy typingStrategy = getTypingStrategy();

    BitSet wl = new BitSet(numAssignments);
    wl.set(0, numAssignments);
    sigma.add(new WorklistElement(tg, wl, new TypeDecision()));

    if (tg.isEmpty()) {
      simple = new BitSet(numAssignments);
      // First get the easy cases out of the way.
      for (int i = 0; i < numAssignments; i++) {
        final DefinitionStmt stmt = this.assignments.get(i);
        Value lhs = stmt.getLeftOp();
        if (lhs instanceof Local) {
          Local v = (Local) lhs;
          Type t = getDefiniteType(v);
          if (t != null) {
            simple.set(i);
            wl.clear(i);
            for (BitSet bs : this.depends.values()) {
              bs.clear(i);
            }
            ((PartialConstantTyping) tg).setConstantTyping(v, t);
            continue;
          }

          if (singleAssignments.contains(v)) {
            Collection<Type> d = ef.eval(tg, stmt.getRightOp(), stmt);
            if (d.size() == 1) {
              Type t_ = d.iterator().next();
              if (stmt.getRightOp() instanceof NullConstant) {
                t_ = NullType.v();
              }
              if (t_.isAllowedInFinalCode() || t_ instanceof NullType) {
                d = reduceToAllowedTypesForLocal(Collections.singleton(t_), v);
                if (d.size() == 1) {
                  ((PartialConstantTyping) tg).setConstantTyping(v, d.iterator().next());
                  simple.set(i);
                  wl.clear(i);
                  for (BitSet bs : this.depends.values()) {
                    bs.clear(i);
                  }
                }
              }
            }
          }
        }
      }
    } else {
      if (simple != null) {
        wl.andNot(simple);
      }
    }

    Set<Type> throwable = null;
    BottomType bt = BottomType.v();

    while (!sigma.isEmpty()) {
      WorklistElement element = sigma.element();
      tg = element.typing;
      wl = element.worklist;
      TypeDecision ds = element.decision;
      int defIdx = wl.nextSetBit(0);
      if (defIdx == -1) {
        // worklist is empty
        r.add(tg);
        sigma.remove();
      } else {
        // Get the next definition statement
        wl.clear(defIdx);
        final DefinitionStmt stmt = this.assignments.get(defIdx);

        Value lhs = stmt.getLeftOp();

        Local v = (lhs instanceof Local) ? (Local) lhs : (Local) ((ArrayRef) lhs).getBase();
        Type told = tg.get(v);

        boolean isFirstType = true;
        for (Type t_ : ef.eval(tg, stmt.getRightOp(), stmt)) {
          if (lhs instanceof ArrayRef) {
            /*
             * We only need to consider array references on the LHS of assignments where there is supertyping between array
             * types, which is only for arrays of reference types and multidimensional arrays.
             */
            if (!(t_ instanceof RefType || t_ instanceof ArrayType || t_ instanceof WeakObjectType)) {
              continue;
            }

            t_ = t_.makeArrayType();
          }

          // Special handling for exception objects with phantom types
          Collection<Type> lcas;
          if (!typesEqual(told, t_) && told instanceof RefType && t_ instanceof RefType
              && (((RefType) told).getSootClass().isPhantom() || ((RefType) t_).getSootClass().isPhantom())
              && (stmt.getRightOp() instanceof CaughtExceptionRef)) {
            if (throwable == null) {
              throwable = Collections.<Type>singleton(Scene.v().getBaseExceptionType());
            }
            lcas = throwable;
          } else {
            Type featureType = ds.getTypeDecision(told, t_);
            if (!typesEqual(featureType, bt)) {
              // Use feature type.
              lcas = Collections.singleton(featureType);
            } else {
              lcas = h.lcas(told, t_, true);
            }
          }
          boolean addFirstDecision = false;

          lcas = reduceToAllowedTypesForLocal(lcas, v);
          if (!opt.use_precise_typing() && lcas.size() > 1) {
            BitSet dependsV = this.depends.get(v);
            if (dependsV == null) {
              if (localToUses == null) {
                localToUses = new HashMultiMap<>();
                for (Unit sstmt : this.jb.getUnits()) {
                  Iterator<ValueBox> it = sstmt.getUseBoxesIterator();
                  while (it.hasNext()) {
                    ValueBox vb = it.next();
                    Value b = vb.getValue();
                    if (b instanceof Local) {
                      Local l = (Local) b;
                      localToUses.put(l, sstmt);
                    }
                  }
                }

              }
              // no other local is dependent on our local, meaning that we can reduce the lcas set immediately
              int castCount = Integer.MAX_VALUE;
              Collection<Type> newLCAS = null;
              Collection<Unit> uses = localToUses.get(v);
              if (uses.isEmpty()) {
                newLCAS = Collections.singletonList(lcas.iterator().next());
              } else {
                for (Type t : lcas) {
                  UseChecker uc = createUseChecker(this.jb);
                  CastInsertionUseVisitor uv = createCastInsertionUseVisitor(tg, h, true, castCount);
                  uc.check(tg, uv, uses.iterator());
                  int g = uv.getCount();
                  if (g < castCount) {
                    castCount = g;

                    if (castCount == 0) {
                      // We can't be better than this
                      newLCAS = Collections.singletonList(t);
                      break;
                    }
                    newLCAS = new ArrayList<>();
                    newLCAS.add(t);
                  } else if (g == castCount && newLCAS != null) {
                    // tie break
                    newLCAS.add(t);
                  }
                }
              }
              lcas = newLCAS;
            }

          }
          for (Type t : lcas) {
            if (!typesEqual(t, told)) {
              BitSet dependsV = this.depends.get(v);
              ITyping tg_;
              BitSet wl_;
              TypeDecision ds_;
              if (/* (eval.size() == 1 && lcas.size() == 1) || */isFirstType) {
                // The types agree, we have a type we can directly use
                tg_ = tg;
                wl_ = wl;
                ds_ = ds;
              } else {
                // The types do not agree, add all supertype candidates
                tg_ = tg.createCloneTyping();
                wl_ = (BitSet) wl.clone();
                ds_ = ds.copy();
                if (addFirstDecision) {
                  ds_.removeLast();
                }
                WorklistElement e = new WorklistElement(tg_, wl_, ds_);

                sigma.add(e);
              }

              if (!typesEqual(told, bt) && !typesEqual(t_, bt)) {
                // 't' is base class of type 'told' & 't_';
                // It will decide the feature type by target value.
                TypeContainer container = new TypeContainer(told, t_, t);
                ds_.addTypeDecision(container);

                // At first type, we will modify the base feature type decisions, and at the next type it will
                // copy the old feature type decisions and used the dirty data(so mark it and remove).
                addFirstDecision = isFirstType;
              }

              tg_.set(v, t);
              if (dependsV != null) {
                wl_.or(dependsV);
              }

            }
            isFirstType = false;
          }
        } // end for
      }
    }

    typingStrategy.minimize(r, h);
    return r;
  }

  protected Type getDefiniteType(Local v) {
    Type type = v.getType();
    if (type != null && !(type instanceof UnknownType)) {
      return type;
    }
    return null;
  }

  protected Collection<Type> reduceToAllowedTypesForLocal(Collection<Type> lcas, Local v) {
    return lcas;
  }

  protected boolean isAllowedTypeForLocal(Local v, Type t) {
    return true;
  }

  protected ArrayDeque<WorklistElement> createSigmaQueue() {
    return new ArrayDeque<>();
  }

  protected List<ITyping> createResultList() {
    return new ArrayList<ITyping>();
  }

  // The ArrayType.equals method seems odd in Soot 2.2.5
  public static boolean typesEqual(Type a, Type b) {
    if (a instanceof ArrayType && b instanceof ArrayType) {
      ArrayType a_ = (ArrayType) a, b_ = (ArrayType) b;
      return a_.numDimensions == b_.numDimensions && a_.baseType.equals(b_.baseType);
    } else {
      return a.equals(b);
    }
  }

  /*
   * Taken from the soot.jimple.toolkits.typing.TypeResolver class of Soot version 2.2.5.
   */
  private void split_new() {
    final Jimple jimp = Jimple.v();
    final JimpleBody body = this.jb;
    final LocalDefs defs = G.v().soot_toolkits_scalar_LocalDefsFactory().newLocalDefs(body);

    PatchingChain<Unit> units = body.getUnits();
    for (Iterator<Unit> it = units.snapshotIterator(); it.hasNext();) {
      Unit stmt = it.next();
      if (stmt instanceof InvokeStmt) {
        InvokeExpr invokeExpr = ((InvokeStmt) stmt).getInvokeExpr();
        if ((invokeExpr instanceof SpecialInvokeExpr) && ("<init>".equals(invokeExpr.getMethodRef().getName()))) {
          SpecialInvokeExpr special = (SpecialInvokeExpr) invokeExpr;
          for (List<Unit> deflist = defs.getDefsOfAt((Local) special.getBase(), stmt); deflist.size() == 1;) {
            Stmt stmt2 = (Stmt) deflist.get(0);

            if (stmt2 instanceof AssignStmt) {
              AssignStmt assign = (AssignStmt) stmt2;
              Value rightOp = assign.getRightOp();

              if (rightOp instanceof Local) {
                deflist = defs.getDefsOfAt((Local) rightOp, assign);
                continue;
              } else if (rightOp instanceof NewExpr) {
                Local newlocal = localGenerator.generateLocal(assign.getLeftOp().getType());

                special.setBase(newlocal);

                DefinitionStmt assignStmt = jimp.newAssignStmt(assign.getLeftOp(), newlocal);
                units.insertAfter(assignStmt, Util.findLastIdentityUnit(body, assign));

                assign.setLeftOp(newlocal);
              }
            }
            break;
          } // end for(List<Unit>)
        }
      }
    }
  }
}
