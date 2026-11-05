package soot.jimple.toolkits.base;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Marc Miltenberger
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.Singletons;
import soot.SootClass;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.thread.mhp.SCC;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalPacker;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.UnusedLocalEliminator;

/**
 * Tries to reorder array writes of the form to reuse the same local: From <code>
 * a1 = new a();
 * a1.&lt;init&gt;();
 * 
 * a2 = new a();
 * a2.&lt;init&gt;();
 * ...
 * 
 * arr[0] = a1;
 * arr[1] = a2;
 * </code> into
 * 
 * <code>
 * a = new a();
 * a.&lt;init&gt;();
 * arr[0] = a;
 * 
 * a = new a();
 * a.&lt;init&gt;();
 * arr[1] = a;
 * ...
 * </code>
 * 
 * There is still room for improvement, though. Currently, we don't touch arrays that are either initialized or written to in
 * loops at all. Technically, we could transform these arrays as long as these statements are all part of the same loop.
 * Still, we would need even more tests to make sure everything stays intact even in obscure cases (e.g. reading from the
 * last iteration, etc).
 * 
 * Therefore, I decided to not tackle this problem yet; this version already transforms many common use cases, such as most
 * array initializers in static initializers.
 */
public class ArrayWriteAggregator extends BodyTransformer {
  /**
   * This "must" be *at least* 2, otherwise we have more statements than before. Since we complicate the flow a bit, the
   * number can also be a bit higher.
   */
  public static final int MIN_ARRAY_SIZE = 4;

  /**
   * We do not want to trigger an OutOfMemoryError at another place in the code, since this might to different semantics,
   * e.g. <code>
   * x = ...;
   * doStuff();
   * arr = new Foo[Integer.MAX_VALUE];
   * arr[0] = x;
   * </code> runs doStuff before potentially throwing the OOM, while <code>
   * x = ...;
   * arr = new Foo[Integer.MAX_VALUE];
   * arr[0] = x;
   * doStuff();
   * </code> would throw the exception before executing doStuff()
   * 
   * Therefore, we ignore really large arrays that have a higher chance of triggering such an exception. This is best effort
   * only, and when an OOM occurs for a lower array size, the semantics still differ. Hopefully this is not a problem in
   * practice.
   */
  private static final int THRESHOLD_MEMORY = 100000;

  /**
   * Maximum method size for which to attempt this analysis. The problem is that a large method size requires a lot of memory
   * due to the MHGDominatorsFinder analysis.
   */
  private static final int MAX_METHOD_SIZE = 100000;

  public ArrayWriteAggregator(Singletons.Global g) {
  }

  public static ArrayWriteAggregator v() {
    return G.v().soot_jimple_toolkits_base_ArrayWriteAggregator();
  }

  /**
   * For extensibility, we use a custom class. Potentially we want to carry more info around in the future. Currently, we are
   * merely interested in the classes that are initialized before reaching a new-array definition, since creating that array
   * triggers a class load.
   */
  private static class ArraySafetyFact {

    Set<SootClass> initializedClasses;

    public void copyTo(ArraySafetyFact dest) {
      if (initializedClasses != null) {
        dest.initializedClasses = new HashSet<>(initializedClasses);
      }
    }

    public void intersection(ArraySafetyFact in1, ArraySafetyFact in2) {
      initializedClasses = intersect(in1.initializedClasses, in2.initializedClasses);
    }

    private static <T> Set<T> intersect(Set<T> s1, Set<T> s2) {
      if (s1 == null) {
        if (s2 == null) {
          return null;
        } else {
          return new HashSet<>(s2);
        }
      } else if (s2 == null) {
        return new HashSet<>(s1);
      }
      if (s1.size() > s2.size()) {
        Set<T> o = s1;
        s1 = s2;
        s2 = o;
      }
      Set<T> res = new HashSet<T>(s1);
      res.retainAll(s2);
      return res;
    }

    public void mergeFrom(ArraySafetyFact in) {
      if (initializedClasses != null && in.initializedClasses != null) {
        initializedClasses.retainAll(in.initializedClasses);
      }
    }

    public void addInitializedClass(SootClass init) {
      if (initializedClasses == null) {
        initializedClasses = new HashSet<>();
      }
      initializedClasses.add(init);
    }

    public void addInitializedType(Type type) {
      if (type instanceof ArrayType) {
        type = ((ArrayType) type).getBaseType();
      }
      if (type instanceof RefType) {
        SootClass sc = ((RefType) type).getSootClass();
        if (sc.isApplicationClass()) {
          if (initializedClasses == null) {
            initializedClasses = new HashSet<>();
          }
          initializedClasses.add(sc);
        }
      }

    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((initializedClasses == null) ? 0 : initializedClasses.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof ArraySafetyFact)) {
        return false;
      }
      ArraySafetyFact other = (ArraySafetyFact) obj;
      if (initializedClasses == null) {
        if (other.initializedClasses != null) {
          return false;
        }
      } else if (!initializedClasses.equals(other.initializedClasses)) {
        return false;
      }
      return true;
    }

    public boolean containsInitializedClass(SootClass c) {
      if (initializedClasses == null) {
        return false;
      }
      return initializedClasses.contains(c);
    }

  }

  private static class Result {

    private AssignStmt assign;
    private int arraySize;
    private Set<Unit> loopParticipants;
    private Map<Unit, Unit> mapAssignToCtor;

    public Result(AssignStmt assign, int arraySize, Map<Unit, Unit> mapAssignToCtor, Set<Unit> loopParticipants) {
      this.assign = assign;
      this.arraySize = arraySize;
      this.loopParticipants = loopParticipants;
      this.mapAssignToCtor = mapAssignToCtor;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((assign == null) ? 0 : assign.hashCode());
      return result;
    }

    // For our purposes, the results only need to be unique by the statement
    // The rest is the same anyway.
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Result)) {
        return false;
      }
      Result other = (Result) obj;
      if (assign == null) {
        if (other.assign != null) {
          return false;
        }
      } else if (!assign.equals(other.assign)) {
        return false;
      }
      return true;
    }

    public Runnable getReorderAction(Body body, ExceptionalUnitGraph graph, LocalDefs ld, SimpleLocalUses uses,
        DominatorsFinder<Unit> dom) {
      UnitPatchingChain units = body.getUnits();
      // now, let's check whether we can factor out the array into it's own local.
      // this is necessary to prevent any other unrelated defs/uses using the same local to become problematic
      Local lclArray = (Local) assign.getLeftOp();
      List<UnitValueBoxPair> allUses = uses.getUsesOf(assign);
      BitSet writtenIndices = new BitSet();
      List<Unit> allWrites = new ArrayList<>();

      // usages that might be causing the array to be visible to the outside world
      List<Unit> exposingUses = new ArrayList<>();
      List<Unit> allConstantWrites = new ArrayList<>();
      for (UnitValueBoxPair use : allUses) {
        if (ld.getDefsOfAt(lclArray, use.unit).size() != 1) {
          // we have a problem, we cannot apply our algorithm here
          return null;
        }

        if (use.unit instanceof AssignStmt) {
          AssignStmt cassign = (AssignStmt) use.unit;
          if (cassign.getLeftOp() instanceof ArrayRef) {
            ArrayRef ref = (ArrayRef) cassign.getLeftOp();
            Value idx = ref.getIndex();
            if (idx instanceof IntConstant) {
              int i = ((IntConstant) idx).value;
              if (i >= 0 && i < arraySize) {
                if (writtenIndices.get(i)) {
                  // when an array element is potentially overwritten, we cannot move the array writes freely
                  return null;
                }
                writtenIndices.set(i);
                // we ignore constant writes and let them be where they are
                if (cassign.getRightOp() instanceof Local) {
                  if (loopParticipants.contains(cassign)) {
                    return null;
                  }
                  allWrites.add(cassign);
                } else {
                  allConstantWrites.add(cassign);
                }

              } else {
                // This should definitely throw an ArrayIndexOutOfBounds,
                // but we don't touch that here...
                return null;
              }
            } else {
              // we can't handle arbitrary integer indices, since we cannot guarantee
              // that they don't throw an ArrayIndexOutOfBounds.
              // We don't want to alter the semantics, even when an exception is thrown.
              return null;
            }
          } else {
            // Potentially exposes the array to the outside world (e.g. via a (static) field)
            exposingUses.add(cassign);
          }
        } else {
          // Possibly an invoke statement, where the array escapes the method
          exposingUses.add(use.unit);
        }
      }
      if (allWrites.size() < MIN_ARRAY_SIZE) {
        // nothing to do
        return null;
      }
      for (Unit i : exposingUses) {
        if (!dom.isDominatedByAll(i, allWrites)) {
          // Array is potentially exposed to the outside world before it is completely written,
          // we don't support that since we don't want to change the semantics.
          return null;
        }
      }

      int defCount = 0;
      for (Unit write : allWrites) {
        AssignStmt assignWrite = (AssignStmt) write;
        Value rop = assignWrite.getRightOp();
        List<Unit> defs = ld.getDefsOfAt((Local) rop, write);
        defCount += defs.size();
      }
      if (defCount > allWrites.size() * 2) {
        // we are not simplifying the code anymore
        // we would generate far too many new statements, since
        // we duplicate the array write for each definition of y at
        // arr[2] = y
        return null;
      }

      Set<Unit> allDefs = new HashSet<>();
      for (Unit write : allWrites) {
        AssignStmt assignWrite = (AssignStmt) write;
        Value rop = assignWrite.getRightOp();
        allDefs.addAll(ld.getDefsOfAt((Local) rop, write));
      }
      allDefs.addAll(allConstantWrites);
      Unit dominatesAll = null;
      for (Unit d : allDefs) {
        if (d instanceof IdentityStmt) {
          // we don't want to destroy parameter chains
          return null;
        }
        if (dom.isDominatingAllGiven(d, allDefs)) {
          dominatesAll = d;
        }
      }

      final Unit insertNewArrayAt;
      final boolean insertNewArrayBefore;
      if (dominatesAll != null) {
        insertNewArrayAt = dominatesAll;
        insertNewArrayBefore = allConstantWrites.contains(dominatesAll);
      } else {
        insertNewArrayAt = null;
        insertNewArrayBefore = true;
      }

      // since these actions we are about to perform break the local defs, we queue them up first and
      // after we don't need them anymore, we can perform the actions in the queue
      Runnable r = new Runnable() {

        @Override
        public void run() {
          // now we have established that we have a safe block of uses, so we can move the array initializer around
          // but first, let's create a new local variable for that block specifically
          Local copy = (Local) lclArray.clone();
          body.getLocals().add(copy);
          assign.setLeftOp(copy);
          for (UnitValueBoxPair use : allUses) {
            use.valueBox.setValue(copy);
          }
          units.remove(assign);

          // now, we can move each array write to it's definition site
          for (Unit write : allWrites) {
            units.remove(write);
            AssignStmt assignWrite = (AssignStmt) write;
            Value rop = assignWrite.getRightOp();
            List<Unit> defs = ld.getDefsOfAt((Local) rop, write);
            for (Unit def : defs) {
              Unit d = mapAssignToCtor.getOrDefault(def, def);
              units.insertAfter((Unit) assignWrite.clone(), d);
            }
          }

          Unit insertNewArray = insertNewArrayAt;
          if (insertNewArray == null ||
          // may happen when another action removed this assignment;
          // unfortunate but doesn't hurt
              !body.getUnits().contains(insertNewArray)) {
            // last resort... but we've established that neither the array initialization
            // nor any of it's non-constant writes are in a loop, so this should be fine
            insertNewArray = body.getFirstNonIdentityStmt();
          }

          // make sure we get a new one
          AssignStmt nassign = (AssignStmt) assign.clone();
          if (insertNewArrayBefore) {
            units.insertBefore(nassign, insertNewArray);
          } else {
            units.insertAfter(nassign, insertNewArray);
          }
        }

      };
      return r;
    }

  }

  private static class ArraySafetyAnalysis extends ForwardFlowAnalysis<Unit, ArraySafetyFact> {

    private Body body;
    private Set<Result> results = new HashSet<>();

    private Set<Unit> loopParticipants = new HashSet<>();
    private Map<Unit, Unit> mapAssignToCtor;

    public ArraySafetyAnalysis(Body body, DirectedGraph<Unit> graph, DominatorsFinder<Unit> mhg,
        Map<Unit, Unit> mapAssignToCtor) {
      super(graph);
      this.body = body;
      this.mapAssignToCtor = mapAssignToCtor;
      calcLoopInformation(graph);
      doAnalysis();
    }

    private void calcLoopInformation(DirectedGraph<Unit> graph) {
      SCC<Unit> scc = new SCC<Unit>(body.getUnits().iterator(), graph);

      for (List<Unit> r : scc.getSccList()) {
        if (r.size() > 1) {
          loopParticipants.addAll(r);
        }
      }
    }

    @Override
    protected boolean omissible(Unit n) {
      Stmt s = (Stmt) n;
      if (!s.containsArrayRef() && !s.containsFieldRef() && !s.containsInvokeExpr()) {
        if (s instanceof AssignStmt && ((AssignStmt) s).getRightOp() instanceof NewArrayExpr) {
          return false;
        }
        return true;
      }
      return false;
    }

    @Override
    protected void merge(ArraySafetyFact in1, ArraySafetyFact in2, ArraySafetyFact out) {
      out.intersection(in1, in2);
    }

    @Override
    protected void mergeInto(Unit succNode, ArraySafetyFact inout, ArraySafetyFact in) {
      inout.mergeFrom(in);
    }

    @Override
    protected void copy(ArraySafetyFact source, ArraySafetyFact dest) {
      source.copyTo(dest);
    }

    @Override
    protected ArraySafetyFact entryInitialFlow() {
      return new ArraySafetyFact();
    }

    @Override
    protected ArraySafetyFact newInitialFlow() {
      return new ArraySafetyFact();
    }

    @Override
    public void doAnalysis() {
      super.doAnalysis();
    }

    @Override
    protected void flowThrough(ArraySafetyFact in, Unit d, ArraySafetyFact out) {
      in.copyTo(out);
      if (d instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) d;
        int arraySize = 0;
        if (assign.getRightOp() instanceof NewArrayExpr) {
          NewArrayExpr e = (NewArrayExpr) assign.getRightOp();
          // When the size is not constant, there might be a negative value, leading to an exception
          boolean safe = false;
          Type type = e.getBaseType();
          if (type instanceof ArrayType) {
            type = ((ArrayType) type).baseType;
          }
          if (type instanceof RefType) {
            RefType arrBaseRefType = (RefType) type;
            SootClass arrBaseClass = arrBaseRefType.getSootClass();
            out.addInitializedClass(arrBaseClass);
            if (arrBaseClass.isLibraryClass() || in.containsInitializedClass(arrBaseClass)) {
              Value size = e.getSize();
              if (size instanceof IntConstant) {
                arraySize = ((IntConstant) size).value;
                // for really large array sizes, there might an overflow due to memory shortage
                // Let's try to minimize that risk
                // Because if we move the array initialization, we potentially destroy the flow when there is
                // such a memory exhaustion
                // Furthermore, we want to have arrays larger than a certain threshold to have a meaningful optimization
                if (arraySize < THRESHOLD_MEMORY && arraySize > MIN_ARRAY_SIZE) {
                  if (!loopParticipants.contains(d)) {
                    safe = true;
                  }
                }
              }
            }
          }
          if (safe) {
            Result res = new Result(assign, arraySize, mapAssignToCtor, loopParticipants);
            results.add(res);
            return;
          }
        }

      }

      Stmt s = (Stmt) d;
      InvokeExpr invoke = s.getInvokeExprUnsafe();
      if (invoke != null) {
        out.addInitializedClass(invoke.getMethodRef().getDeclaringClass());
        return;
      }
      FieldRef fieldRef = s.getFieldRefUnsafe();
      if (fieldRef != null) {
        out.addInitializedClass(fieldRef.getFieldRef().declaringClass());
        out.addInitializedType(fieldRef.getType());
        return;
      }
      ArrayRef ar = s.getArrayRefUnsafe();

      if (ar != null) {
        out.addInitializedType(ar.getType());
      }
    }
  }

  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    Map<Unit, Unit> mapAssignToCtor = transformConstructorAliases(b);

    boolean hasInterestingArrayWrites = false;
    boolean hasInterestingNewArray = false;
    for (Unit u : b.getUnits()) {
      Stmt s = (Stmt) u;
      ArrayRef arr = s.getArrayRefUnsafe();
      if (arr != null) {
        AssignStmt assign = (AssignStmt) s;
        if (assign.getRightOp() instanceof Local) {
          hasInterestingArrayWrites = true;
          break;
        }
      }
      if (s instanceof AssignStmt) {
        Value rop = ((AssignStmt) s).getRightOp();
        if (rop instanceof NewArrayExpr) {
          NewArrayExpr n = (NewArrayExpr) rop;
          if (n.getType() instanceof RefLikeType && n.getSize() instanceof IntConstant) {
            hasInterestingNewArray = true;
          }
        }
      }
    }
    if (!hasInterestingArrayWrites || !hasInterestingNewArray) {
      return;
    }
    if (b.getUnits().size() > MAX_METHOD_SIZE) {
      return;
    }

    ExceptionalUnitGraph graph = ExceptionalUnitGraphFactory.createExceptionalUnitGraph(b);

    LocalDefs ld = G.v().soot_toolkits_scalar_LocalDefsFactory().newLocalDefs(graph, false);
    SimpleLocalUses uses = new SimpleLocalUses(b, ld);
    DominatorsFinder<Unit> mhg = new MHGDominatorsFinder<Unit>(graph);

    if (mapAssignToCtor == null) {
      mapAssignToCtor = Collections.emptyMap();
    }
    // tries to figure out
    // whether it is safe to move an array initializer and the setter upward, i.e.
    // it must not trigger any exception (ArrayIndexOutOfBounds, Class loading exception)
    // and it must not escape the method before the write
    ArraySafetyAnalysis analysis = new ArraySafetyAnalysis(b, graph, mhg, mapAssignToCtor);
    analysis.doAnalysis();
    List<Runnable> allActions = new ArrayList<>();

    for (Result r : analysis.results) {
      Runnable action = r.getReorderAction(b, graph, ld, uses, mhg);
      if (action != null) {
        allActions.add(action);
      }
    }
    if (!allActions.isEmpty()) {
      for (Runnable a : allActions) {
        a.run();
      }
      LocalPacker.v().transform(b);
      UnusedLocalEliminator.v().transform(b);

    }

  }

  /**
   * Dexpler often creates constructs such as
   *
   * <code>
        $u1 = new A3.e;
        $u0 = $u1;
        specialinvoke $u1.<java.lang.Enum: void <init>(java.lang.String,int)>("DEVICE", 0);
    </code>
   * 
   * We want to try to defer these aliases to after the constructor call so that it can be removed later on. I've decided to
   * not make this as its own analysis since this is mostly relevant for this analysis.
   * 
   * @return a map from each new assignment to the constructor call
   */
  private Map<Unit, Unit> transformConstructorAliases(Body body) {
    UnitPatchingChain units = body.getUnits();
    Iterator<Unit> snit = units.snapshotIterator();
    Map<Unit, Unit> mapAssignToCtor = null;
    while (snit.hasNext()) {
      Unit u = snit.next();
      Stmt s = (Stmt) u;
      if (s instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) s;
        if (assign.getRightOp() instanceof NewExpr) {
          Unit n = assign;
          List<Unit> aliasesCreators = new ArrayList<>();
          Set<Local> aliases = new HashSet<>();
          aliases.add((Local) assign.getLeftOp());
          while (n != null) {
            n = body.getUnits().getSuccOf(n);
            if (n == null) {
              return mapAssignToCtor;
            }
            if (n instanceof AssignStmt) {
              AssignStmt potAlias = (AssignStmt) n;
              if (aliases.contains(potAlias.getRightOp())) {
                Value lop = potAlias.getLeftOp();
                if (lop instanceof Local) {
                  aliases.add((Local) lop);
                  aliasesCreators.add(potAlias);
                } else {
                  // weird
                  return mapAssignToCtor;
                }
              } else {
                aliases.remove(potAlias.getLeftOp());
              }
            }
            InvokeExpr inv = ((Stmt) n).getInvokeExprUnsafe();
            if (inv instanceof SpecialInvokeExpr && inv.getMethodRef().isConstructor()) {
              SpecialInvokeExpr sp = (SpecialInvokeExpr) inv;
              if (aliases.remove(sp.getBase())) {
                if (mapAssignToCtor == null) {
                  mapAssignToCtor = new HashMap<>();
                }
                mapAssignToCtor.put(assign, n);
                if (!aliasesCreators.isEmpty()) {
                  // we have found the correct special invoke
                  // insert the aliases *after* the constructor call
                  units.removeAll(aliasesCreators);
                  units.insertAfter(aliasesCreators, n);
                }
                break;
              }
            }

          }
        }
      }
    }
    return mapAssignToCtor;
  }

}
