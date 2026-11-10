package soot.jimple.spark.pag;

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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.AnySubType;
import soot.Context;
import soot.FastHierarchy;
import soot.Kind;
import soot.Local;
import soot.MethodSubSignature;
import soot.PhaseOptions;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.spark.builder.GlobalNodeFactory;
import soot.jimple.spark.builder.MethodNodeFactory;
import soot.jimple.spark.internal.ClientAccessibilityOracle;
import soot.jimple.spark.internal.SparkLibraryHelper;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.sets.BitPointsToSet;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.spark.sets.EmptyPointsToSet;
import soot.jimple.spark.sets.HashPointsToSet;
import soot.jimple.spark.sets.HybridPointsToSet;
import soot.jimple.spark.sets.P2SetFactory;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.sets.SharedHybridSet;
import soot.jimple.spark.sets.SharedListSet;
import soot.jimple.spark.sets.SortedArraySet;
import soot.jimple.spark.solver.OnFlyCallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.DeferredVirtualEdgeTarget;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.InstanceinvokeSource;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.InvocationVirtualEdgeTarget;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.StaticinvokeSource;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdge;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdgeSource;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdgeTarget;
import soot.jimple.toolkits.pointer.util.NativeMethodDriver;
import soot.options.CGOptions;
import soot.options.SparkOptions;
import soot.tagkit.ExpectedTypeTag;
import soot.tagkit.LinkTag;
import soot.tagkit.StringTag;
import soot.tagkit.Tag;
import soot.toolkits.scalar.Pair;
import soot.util.ArrayNumberer;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

/**
 * Pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class PAG implements PointsToAnalysis {
  private static final Logger logger = LoggerFactory.getLogger(PAG.class);

  public PAG(final SparkOptions opts) {
    this.opts = opts;
    this.cgOpts = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));
    if (opts.add_tags()) {
      nodeToTag = new HashMap<Node, Tag>();
    }
    if (opts.rta() && opts.on_fly_cg()) {
      throw new RuntimeException("Incompatible options rta:true and on-fly-cg:true for cg.spark. Use -p cg-"
          + ".spark on-fly-cg:false when using RTA.");
    }
    typeManager = new TypeManager(this);
    if (!opts.ignore_types()) {
      typeManager.setFastHierarchy(() -> Scene.v().getOrMakeFastHierarchy());
    }
    if (opts.cs_demand()) {
      virtualCallsToReceivers = new HashMap<InvokeExpr, Node>();
      callToMethod = new HashMap<InvokeExpr, SootMethod>();
      callAssigns = new HashMultiMap<InvokeExpr, Pair<Node, Node>>();
    }
    switch (opts.set_impl()) {
      case SparkOptions.set_impl_hash:
        setFactory = HashPointsToSet.getFactory();
        break;
      case SparkOptions.set_impl_hybrid:
        setFactory = HybridPointsToSet.getFactory();
        break;
      case SparkOptions.set_impl_heintze:
        setFactory = SharedHybridSet.getFactory();
        break;
      case SparkOptions.set_impl_sharedlist:
        setFactory = SharedListSet.getFactory();
        break;
      case SparkOptions.set_impl_array:
        setFactory = SortedArraySet.getFactory();
        break;
      case SparkOptions.set_impl_bit:
        setFactory = BitPointsToSet.getFactory();
        break;
      case SparkOptions.set_impl_double:
        P2SetFactory oldF;
        P2SetFactory newF;
        switch (opts.double_set_old()) {
          case SparkOptions.double_set_old_hash:
            oldF = HashPointsToSet.getFactory();
            break;
          case SparkOptions.double_set_old_hybrid:
            oldF = HybridPointsToSet.getFactory();
            break;
          case SparkOptions.double_set_old_heintze:
            oldF = SharedHybridSet.getFactory();
            break;
          case SparkOptions.double_set_old_sharedlist:
            oldF = SharedListSet.getFactory();
            break;
          case SparkOptions.double_set_old_array:
            oldF = SortedArraySet.getFactory();
            break;
          case SparkOptions.double_set_old_bit:
            oldF = BitPointsToSet.getFactory();
            break;
          default:
            throw new RuntimeException();
        }
        switch (opts.double_set_new()) {
          case SparkOptions.double_set_new_hash:
            newF = HashPointsToSet.getFactory();
            break;
          case SparkOptions.double_set_new_hybrid:
            newF = HybridPointsToSet.getFactory();
            break;
          case SparkOptions.double_set_new_heintze:
            newF = SharedHybridSet.getFactory();
            break;
          case SparkOptions.double_set_new_sharedlist:
            newF = SharedListSet.getFactory();
            break;
          case SparkOptions.double_set_new_array:
            newF = SortedArraySet.getFactory();
            break;
          case SparkOptions.double_set_new_bit:
            newF = BitPointsToSet.getFactory();
            break;
          default:
            throw new RuntimeException();
        }
        setFactory = DoublePointsToSet.getFactory(newF, oldF);
        break;
      default:
        throw new RuntimeException();
    }
    runGeomPTA = opts.geom_pta();
  }

  /** Returns the set of objects pointed to by variable l. */
  @Override
  public PointsToSet reachingObjects(Local l) {
    VarNode n = findLocalVarNode(l);
    if (n == null) {
      return EmptyPointsToSet.v();
    }
    return n.getP2Set();
  }

  /** Returns the set of objects pointed to by variable l in context c. */
  @Override
  public PointsToSet reachingObjects(Context c, Local l) {
    VarNode n = findContextVarNode(l, c);
    if (n == null) {
      return EmptyPointsToSet.v();
    }
    return n.getP2Set();
  }

  /** Returns the set of objects pointed to by static field f. */
  @Override
  public PointsToSet reachingObjects(SootField f) {
    if (!f.isStatic()) {
      throw new RuntimeException("The parameter f must be a *static* field.");
    }
    VarNode n = findGlobalVarNode(f);
    if (n == null) {
      return EmptyPointsToSet.v();
    }
    return n.getP2Set();
  }

  /**
   * Returns the set of objects pointed to by instance field f of the objects in the PointsToSet s.
   */
  @Override
  public PointsToSet reachingObjects(PointsToSet s, final SootField f) {
    if (f.isStatic()) {
      throw new RuntimeException("The parameter f must be an *instance* field.");
    }

    return reachingObjectsInternal(s, f);
  }

  /**
   * Returns the set of objects pointed to by elements of the arrays in the PointsToSet s.
   */
  @Override
  public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
    return reachingObjectsInternal(s, ArrayElement.v());
  }

  private PointsToSet reachingObjectsInternal(PointsToSet s, final SparkField f) {
    if (getOpts().field_based() || getOpts().vta()) {
      VarNode n = findGlobalVarNode(f);
      if (n == null) {
        return EmptyPointsToSet.v();
      }
      return n.getP2Set();
    }
    if ((getOpts()).propagator() == SparkOptions.propagator_alias) {
      throw new RuntimeException("The alias edge propagator does not compute points-to information for instance fields!"
          + "Use a different propagator.");
    }
    PointsToSetInternal bases = (PointsToSetInternal) s;
    final PointsToSetInternal ret = setFactory.newSet((f instanceof SootField) ? ((SootField) f).getType() : null, this);
    bases.forall(new P2SetVisitor() {
      @Override
      public final void visit(Node n) {
        Node nDotF = ((AllocNode) n).dot(f);
        if (nDotF != null) {
          ret.addAll(nDotF.getP2Set(), null);
        }
      }
    });
    return ret;
  }

  public P2SetFactory getSetFactory() {
    return setFactory;
  }

  private <K extends Node> void lookupInMap(Map<K, Object> map) {
    for (K object : map.keySet()) {
      lookup(map, object);
    }
  }

  public void cleanUpMerges() {
    if (opts.verbose()) {
      logger.debug("Cleaning up graph for merged nodes");
    }
    lookupInMap(simple);
    lookupInMap(alloc);
    lookupInMap(store);
    lookupInMap(load);
    lookupInMap(simpleInv);
    lookupInMap(allocInv);
    lookupInMap(storeInv);
    lookupInMap(loadInv);

    somethingMerged = false;
    if (opts.verbose()) {
      logger.debug("Done cleaning up graph for merged nodes");
    }
  }

  public boolean doAddSimpleEdge(VarNode from, VarNode to) {
    return addToMap(simple, from, to) | addToMap(simpleInv, to, from);
  }

  public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
    return addToMap(store, from, to) | addToMap(storeInv, to, from);
  }

  public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
    return addToMap(load, from, to) | addToMap(loadInv, to, from);
  }

  public boolean doAddAllocEdge(AllocNode from, VarNode to) {
    return addToMap(alloc, from, to) | addToMap(allocInv, to, from);
  }

  public boolean doAddNewInstanceEdge(VarNode from, NewInstanceNode to) {
    return addToMap(newInstance, from, to) | addToMap(newInstanceInv, to, from);
  }

  public boolean doAddAssignInstanceEdge(NewInstanceNode from, VarNode to) {
    return addToMap(assignInstance, from, to) | addToMap(assignInstanceInv, to, from);
  }

  /** Node uses this to notify PAG that n2 has been merged into n1. */
  void mergedWith(Node n1, Node n2) {
    if (n1.equals(n2)) {
      throw new RuntimeException("oops");
    }

    somethingMerged = true;
    if (ofcg() != null) {
      ofcg().mergedWith(n1, n2);
    }

    Map[] maps = { simple, alloc, store, load, simpleInv, allocInv, storeInv, loadInv };
    for (Map<Node, Object> m : maps) {
      if (!m.keySet().contains(n2)) {
        continue;
      }

      Object[] os = { m.get(n1), m.get(n2) };
      int size1 = getSize(os[0]);
      int size2 = getSize(os[1]);
      if (size1 == 0) {
        if (os[1] != null) {
          m.put(n1, os[1]);
        }
      } else if (size2 == 0) {
        // nothing needed
      } else if (os[0] instanceof HashSet) {
        if (os[1] instanceof HashSet) {
          ((HashSet) os[0]).addAll((HashSet) os[1]);
        } else {
          Node[] ar = (Node[]) os[1];
          for (Node element0 : ar) {
            ((HashSet<Node>) os[0]).add(element0);
          }
        }
      } else if (os[1] instanceof HashSet) {
        Node[] ar = (Node[]) os[0];
        for (Node element0 : ar) {
          ((HashSet<Node>) os[1]).add(element0);
        }
        m.put(n1, os[1]);
      } else if (size1 * size2 < 1000) {
        Node[] a1 = (Node[]) os[0];
        Node[] a2 = (Node[]) os[1];
        Node[] ret = new Node[size1 + size2];
        System.arraycopy(a1, 0, ret, 0, a1.length);
        int j = a1.length;
        outer: for (Node rep : a2) {
          for (int k = 0; k < j; k++) {
            if (rep == ret[k]) {
              continue outer;
            }
          }
          ret[j++] = rep;
        }
        Node[] newArray = new Node[j];
        System.arraycopy(ret, 0, newArray, 0, j);
        m.put(n1, ret = newArray);
      } else {
        HashSet<Node> s = new HashSet<Node>(size1 + size2);
        for (Object o : os) {
          if (o == null) {
            continue;
          }
          if (o instanceof Set) {
            s.addAll((Set<Node>) o);
          } else {
            Node[] ar = (Node[]) o;
            for (Node element1 : ar) {
              s.add(element1);
            }
          }
        }
        m.put(n1, s);
      }
      m.remove(n2);
    }
  }

  protected final static Node[] EMPTY_NODE_ARRAY = new Node[0];
  private static final InstanceInvokeExpr Ie = null;

  protected <K extends Node> Node[] lookup(Map<K, Object> m, K key) {
    Object valueList = m.get(key);
    if (valueList == null) {
      return EMPTY_NODE_ARRAY;
    }
    if (valueList instanceof Set) {
      try {
        m.put(key, valueList = ((Set) valueList).toArray(EMPTY_NODE_ARRAY));
      } catch (Exception e) {
        for (Iterator it = ((Set) valueList).iterator(); it.hasNext();) {
          logger.debug("" + it.next());
        }
        throw new RuntimeException("" + valueList + e);
      }
    }
    Node[] ret = (Node[]) valueList;
    if (somethingMerged) {
      for (int i = 0; i < ret.length; i++) {
        Node reti = ret[i];
        Node rep = reti.getReplacement();
        if (rep != reti || rep == key) {
          Set<Node> s;
          if (ret.length <= 75) {
            int j = i;
            outer: for (; i < ret.length; i++) {
              reti = ret[i];
              rep = reti.getReplacement();
              if (rep == key) {
                continue;
              }
              for (int k = 0; k < j; k++) {
                if (rep == ret[k]) {
                  continue outer;
                }
              }
              ret[j++] = rep;
            }
            Node[] newArray = new Node[j];
            System.arraycopy(ret, 0, newArray, 0, j);
            m.put(key, ret = newArray);
          } else {
            s = new HashSet<Node>(ret.length * 2);
            for (int j = 0; j < i; j++) {
              s.add(ret[j]);
            }
            for (int j = i; j < ret.length; j++) {
              rep = ret[j].getReplacement();
              if (rep != key) {
                s.add(rep);
              }
            }
            m.put(key, ret = s.toArray(EMPTY_NODE_ARRAY));
          }
          break;
        }
      }
    }
    return ret;
  }

  public Node[] simpleLookup(VarNode key) {
    return lookup(simple, key);
  }

  public Node[] simpleInvLookup(VarNode key) {
    return lookup(simpleInv, key);
  }

  public Node[] loadLookup(FieldRefNode key) {
    return lookup(load, key);
  }

  public Node[] loadInvLookup(VarNode key) {
    return lookup(loadInv, key);
  }

  public Node[] storeLookup(VarNode key) {
    return lookup(store, key);
  }

  public Node[] newInstanceLookup(VarNode key) {
    return lookup(newInstance, key);
  }

  public Node[] assignInstanceLookup(NewInstanceNode key) {
    return lookup(assignInstance, key);
  }

  public Node[] storeInvLookup(FieldRefNode key) {
    return lookup(storeInv, key);
  }

  public Node[] allocLookup(AllocNode key) {
    return lookup(alloc, key);
  }

  public Node[] allocInvLookup(VarNode key) {
    return lookup(allocInv, key);
  }

  public Set<VarNode> simpleSources() {
    return simple.keySet();
  }

  public Set<AllocNode> allocSources() {
    return alloc.keySet();
  }

  public Set<VarNode> storeSources() {
    return store.keySet();
  }

  public Set<FieldRefNode> loadSources() {
    return load.keySet();
  }

  public Set<VarNode> newInstanceSources() {
    return newInstance.keySet();
  }

  public Set<NewInstanceNode> assignInstanceSources() {
    return assignInstance.keySet();
  }

  public Set<VarNode> simpleInvSources() {
    return simpleInv.keySet();
  }

  public Set<VarNode> allocInvSources() {
    return allocInv.keySet();
  }

  public Set<FieldRefNode> storeInvSources() {
    return storeInv.keySet();
  }

  public Set<VarNode> loadInvSources() {
    return loadInv.keySet();
  }

  public Iterator<VarNode> simpleSourcesIterator() {
    return simple.keySet().iterator();
  }

  public Iterator<AllocNode> allocSourcesIterator() {
    return alloc.keySet().iterator();
  }

  public Iterator<VarNode> storeSourcesIterator() {
    return store.keySet().iterator();
  }

  public Iterator<FieldRefNode> loadSourcesIterator() {
    return load.keySet().iterator();
  }

  public Iterator<VarNode> simpleInvSourcesIterator() {
    return simpleInv.keySet().iterator();
  }

  public Iterator<VarNode> allocInvSourcesIterator() {
    return allocInv.keySet().iterator();
  }

  public Iterator<FieldRefNode> storeInvSourcesIterator() {
    return storeInv.keySet().iterator();
  }

  public Iterator<VarNode> loadInvSourcesIterator() {
    return loadInv.keySet().iterator();
  }

  static private int getSize(Object set) {
    if (set instanceof Set) {
      return ((Set<?>) set).size();
    } else if (set == null) {
      return 0;
    } else {
      return ((Object[]) set).length;
    }
  }

  protected P2SetFactory setFactory;
  protected boolean somethingMerged = false;

  /**
   * Returns the set of objects pointed to by instance field f of the objects pointed to by l.
   */
  @Override
  public PointsToSet reachingObjects(Local l, SootField f) {
    return reachingObjects(reachingObjects(l), f);
  }

  /**
   * Returns the set of objects pointed to by instance field f of the objects pointed to by l in context c.
   */
  @Override
  public PointsToSet reachingObjects(Context c, Local l, SootField f) {
    return reachingObjects(reachingObjects(c, l), f);
  }

  private void addNodeTag(Node node, SootMethod m) {
    if (nodeToTag != null) {
      Tag tag;
      if (m == null) {
        tag = new StringTag(node.toString());
      } else {
        tag = new LinkTag(node.toString(), m, m.getDeclaringClass().getName());
      }
      nodeToTag.put(node, tag);
    }
  }

  public AllocNode makeAllocNode(Object newExpr, Type type, SootMethod m) {
    if (opts.types_for_sites() || opts.vta()) {
      newExpr = type;
    }

    AllocNode ret = valToAllocNode.get(newExpr);
    if (newExpr instanceof NewExpr) {
      // Do we need to create a new allocation node?
      if (ret == null) {
        valToAllocNode.put(newExpr, ret = new AllocNode(this, newExpr, type, m));
        newAllocNodes.add(ret);
        addNodeTag(ret, m);
      }
      // For a normal "new" expression, there may only be one type
      else if (!(ret.getType().equals(type))) {
        throw new RuntimeException("NewExpr " + newExpr + " of type " + type + " previously had type " + ret.getType());
      }
    }
    // Check for reflective allocation sites
    else {
      ret = valToReflAllocNode.get(newExpr, type);
      if (ret == null) {
        valToReflAllocNode.put(newExpr, type, ret = new AllocNode(this, newExpr, type, m));
        newAllocNodes.add(ret);
        addNodeTag(ret, m);
      }
    }
    return ret;
  }

  public AllocNode makeStringConstantNode(String s) {
    if (opts.types_for_sites() || opts.vta()) {
      return makeAllocNode(RefType.v("java.lang.String"), RefType.v("java.lang.String"), null);
    }
    StringConstantNode ret = (StringConstantNode) valToAllocNode.get(s);
    if (ret == null) {
      valToAllocNode.put(s, ret = new StringConstantNode(this, s));
      newAllocNodes.add(ret);
      addNodeTag(ret, null);
    }
    return ret;
  }

  public AllocNode makeClassConstantNode(ClassConstant cc) {
    if (opts.types_for_sites() || opts.vta()) {
      return makeAllocNode(RefType.v("java.lang.Class"), RefType.v("java.lang.Class"), null);
    }
    ClassConstantNode ret = (ClassConstantNode) valToAllocNode.get(cc);
    if (ret == null) {
      valToAllocNode.put(cc, ret = new ClassConstantNode(this, cc));
      newAllocNodes.add(ret);
      addNodeTag(ret, null);
    }
    return ret;
  }

  ChunkedQueue<AllocNode> newAllocNodes = new ChunkedQueue<AllocNode>();

  public QueueReader<AllocNode> allocNodeListener() {
    return newAllocNodes.reader();
  }

  /** Finds the GlobalVarNode for the variable value, or returns null. */
  public GlobalVarNode findGlobalVarNode(Object value) {
    if (opts.rta()) {
      value = null;
    }
    return valToGlobalVarNode.get(value);
  }

  /** Finds the LocalVarNode for the variable value, or returns null. */
  public LocalVarNode findLocalVarNode(Object value) {
    if (opts.rta()) {
      value = null;
    } else if (value instanceof Local) {
      return localToNodeMap.get(value);
    }
    return valToLocalVarNode.get(value);
  }

  /**
   * Finds or creates the GlobalVarNode for the variable value, of type type.
   */
  public GlobalVarNode makeGlobalVarNode(Object value, Type type) {
    if (opts.rta()) {
      value = null;
      type = Scene.v().getObjectType();
    }
    GlobalVarNode ret = valToGlobalVarNode.get(value);
    if (ret == null) {
      valToGlobalVarNode.put(value, ret = new GlobalVarNode(this, value, type));

      // if library mode is activated, add allocation of every possible
      // type to accessible fields
      if (cgOpts.library() != CGOptions.library_disabled) {
        if (value instanceof SootField) {
          SootField sf = (SootField) value;

          if (accessibilityOracle.isAccessible(sf)) {
            type.apply(new SparkLibraryHelper(this, ret, null));
          }
        }
      }
      addNodeTag(ret, null);
    } else if (!(ret.getType().equals(type))) {
      throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
    }
    return ret;
  }

  /**
   * Finds or creates the LocalVarNode for the variable value, of type type.
   */
  public LocalVarNode makeLocalVarNode(Object value, Type type, SootMethod method) {
    if (opts.rta()) {
      value = null;
      type = Scene.v().getObjectType();
      method = null;
    } else if (value instanceof Local) {
      Local val = (Local) value;
      LocalVarNode ret = localToNodeMap.get(val);
      if (ret == null) {
        localToNodeMap.put((Local) value, ret = new LocalVarNode(this, value, type, method));
        addNodeTag(ret, method);
      } else if (!(ret.getType().equals(type))) {
        throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
      }
      return ret;
    }
    LocalVarNode ret = valToLocalVarNode.get(value);
    if (ret == null) {
      valToLocalVarNode.put(value, ret = new LocalVarNode(this, value, type, method));
      addNodeTag(ret, method);
    } else if (!(ret.getType().equals(type))) {
      throw new RuntimeException("Value " + value + " of type " + type + " previously had type " + ret.getType());
    }
    return ret;
  }

  public NewInstanceNode makeNewInstanceNode(Value value, Type type, SootMethod method) {
    NewInstanceNode node = newInstToNodeMap.get(value);
    if (node == null) {
      node = new NewInstanceNode(this, value, type);
      newInstToNodeMap.put(value, node);
      addNodeTag(node, method);
    }
    return node;
  }

  /**
   * Finds the ContextVarNode for base variable value and context context, or returns null.
   */
  public ContextVarNode findContextVarNode(Object baseValue, Context context) {
    LocalVarNode base = findLocalVarNode(baseValue);
    if (base == null) {
      return null;
    }
    return base.context(context);
  }

  /**
   * Finds or creates the ContextVarNode for base variable baseValue and context context, of type type.
   */
  public ContextVarNode makeContextVarNode(Object baseValue, Type baseType, Context context, SootMethod method) {
    LocalVarNode base = makeLocalVarNode(baseValue, baseType, method);
    return makeContextVarNode(base, context);
  }

  /**
   * Finds or creates the ContextVarNode for base variable base and context context, of type type.
   */
  public ContextVarNode makeContextVarNode(LocalVarNode base, Context context) {
    ContextVarNode ret = base.context(context);
    if (ret == null) {
      ret = new ContextVarNode(this, base, context);
      addNodeTag(ret, base.getMethod());
    }
    return ret;
  }

  /**
   * Finds the FieldRefNode for base variable value and field field, or returns null.
   */
  public FieldRefNode findLocalFieldRefNode(Object baseValue, SparkField field) {
    VarNode base = findLocalVarNode(baseValue);
    if (base == null) {
      return null;
    }
    return base.dot(field);
  }

  /**
   * Finds the FieldRefNode for base variable value and field field, or returns null.
   */
  public FieldRefNode findGlobalFieldRefNode(Object baseValue, SparkField field) {
    VarNode base = findGlobalVarNode(baseValue);
    if (base == null) {
      return null;
    }
    return base.dot(field);
  }

  /**
   * Finds or creates the FieldRefNode for base variable baseValue and field field, of type type.
   */
  public FieldRefNode makeLocalFieldRefNode(Object baseValue, Type baseType, SparkField field, SootMethod method) {
    VarNode base = makeLocalVarNode(baseValue, baseType, method);
    FieldRefNode ret = makeFieldRefNode(base, field);

    // if library mode is activated, add allocation of every possible type
    // to accessible fields
    if (cgOpts.library() != CGOptions.library_disabled) {
      if (field instanceof SootField) {
        SootField sf = (SootField) field;
        Type type = sf.getType();
        if (accessibilityOracle.isAccessible(sf)) {
          type.apply(new SparkLibraryHelper(this, ret, method));
        }
      }
    }

    return ret;
  }

  /**
   * Finds or creates the FieldRefNode for base variable baseValue and field field, of type type.
   */
  public FieldRefNode makeGlobalFieldRefNode(Object baseValue, Type baseType, SparkField field) {
    VarNode base = makeGlobalVarNode(baseValue, baseType);
    return makeFieldRefNode(base, field);
  }

  /**
   * Finds or creates the FieldRefNode for base variable base and field field, of type type.
   */
  public FieldRefNode makeFieldRefNode(VarNode base, SparkField field) {
    FieldRefNode ret = base.dot(field);
    if (ret == null) {
      ret = new FieldRefNode(this, base, field);
      if (base instanceof LocalVarNode) {
        addNodeTag(ret, ((LocalVarNode) base).getMethod());
      } else {
        addNodeTag(ret, null);
      }

    }
    return ret;
  }

  /**
   * Finds the AllocDotField for base AllocNode an and field field, or returns null.
   */
  public AllocDotField findAllocDotField(AllocNode an, SparkField field) {
    return an.dot(field);
  }

  /**
   * Finds or creates the AllocDotField for base variable baseValue and field field, of type t.
   */
  public AllocDotField makeAllocDotField(AllocNode an, SparkField field) {
    AllocDotField ret = an.dot(field);
    if (ret == null) {
      ret = new AllocDotField(this, an, field);
    }
    return ret;
  }

  public boolean addSimpleEdge(VarNode from, VarNode to) {
    boolean ret = false;
    if (doAddSimpleEdge(from, to)) {
      edgeQueue.add(from);
      edgeQueue.add(to);
      ret = true;
    }
    if (opts.simple_edges_bidirectional()) {
      if (doAddSimpleEdge(to, from)) {
        edgeQueue.add(to);
        edgeQueue.add(from);
        ret = true;
      }
    }
    return ret;
  }

  public boolean addStoreEdge(VarNode from, FieldRefNode to) {
    if (!opts.rta()) {
      if (doAddStoreEdge(from, to)) {
        edgeQueue.add(from);
        edgeQueue.add(to);
        return true;
      }
    }
    return false;
  }

  public boolean addLoadEdge(FieldRefNode from, VarNode to) {
    if (!opts.rta()) {
      if (doAddLoadEdge(from, to)) {
        edgeQueue.add(from);
        edgeQueue.add(to);
        return true;
      }
    }
    return false;
  }

  public boolean addAllocEdge(AllocNode from, VarNode to) {
    FastHierarchy fh = typeManager.getFastHierarchy();
    if (fh == null || to.getType() == null || fh.canStoreType(from.getType(), to.getType())) {
      if (doAddAllocEdge(from, to)) {
        edgeQueue.add(from);
        edgeQueue.add(to);
        return true;
      }
    }
    return false;
  }

  public boolean addNewInstanceEdge(VarNode from, NewInstanceNode to) {
    if (!opts.rta()) {
      if (doAddNewInstanceEdge(from, to)) {
        edgeQueue.add(from);
        edgeQueue.add(to);
        return true;
      }
    }
    return false;
  }

  public boolean addAssignInstanceEdge(NewInstanceNode from, VarNode to) {
    if (!opts.rta()) {
      if (doAddAssignInstanceEdge(from, to)) {
        edgeQueue.add(from);
        edgeQueue.add(to);
        return true;
      }
    }
    return false;
  }

  /** Adds an edge to the graph, returning false if it was already there. */
  public boolean addEdge(Node from, Node to) {
    from = from.getReplacement();
    to = to.getReplacement();
    if (from instanceof VarNode) {
      if (to instanceof VarNode) {
        return addSimpleEdge((VarNode) from, (VarNode) to);
      } else if (to instanceof FieldRefNode) {
        return addStoreEdge((VarNode) from, (FieldRefNode) to);
      } else if (to instanceof NewInstanceNode) {
        return addNewInstanceEdge((VarNode) from, (NewInstanceNode) to);
      } else {
        throw new RuntimeException("Invalid node type");
      }
    } else if (from instanceof FieldRefNode) {
      return addLoadEdge((FieldRefNode) from, (VarNode) to);

    } else if (from instanceof NewInstanceNode) {
      return addAssignInstanceEdge((NewInstanceNode) from, (VarNode) to);
    } else {
      return addAllocEdge((AllocNode) from, (VarNode) to);
    }
  }

  protected ChunkedQueue<Node> edgeQueue = new ChunkedQueue<Node>();

  public QueueReader<Node> edgeReader() {
    return edgeQueue.reader();
  }

  public int getNumAllocNodes() {
    return allocNodeNumberer.size();
  }

  public TypeManager getTypeManager() {
    return typeManager;
  }

  public void setOnFlyCallGraph(OnFlyCallGraph ofcg) {
    this.ofcg = ofcg;
  }

  public OnFlyCallGraph getOnFlyCallGraph() {
    return ofcg;
  }

  public OnFlyCallGraph ofcg() {
    return ofcg;
  }

  /**
   * Adds the base of a dereference to the list of dereferenced variables.
   */
  public void addDereference(VarNode base) {
    dereferences.add(base);
  }

  /** Returns list of dereferences variables. */
  public List<VarNode> getDereferences() {
    return dereferences;
  }

  public Map<Node, Tag> getNodeTags() {
    return nodeToTag;
  }

  protected final ArrayNumberer<AllocNode> allocNodeNumberer = new ArrayNumberer<AllocNode>();

  public ArrayNumberer<AllocNode> getAllocNodeNumberer() {
    return allocNodeNumberer;
  }

  private final ArrayNumberer<VarNode> varNodeNumberer = new ArrayNumberer<VarNode>();

  public ArrayNumberer<VarNode> getVarNodeNumberer() {
    return varNodeNumberer;
  }

  private final ArrayNumberer<FieldRefNode> fieldRefNodeNumberer = new ArrayNumberer<FieldRefNode>();

  public ArrayNumberer<FieldRefNode> getFieldRefNodeNumberer() {
    return fieldRefNodeNumberer;
  }

  private final ArrayNumberer<AllocDotField> allocDotFieldNodeNumberer = new ArrayNumberer<AllocDotField>();

  public ArrayNumberer<AllocDotField> getAllocDotFieldNodeNumberer() {
    return allocDotFieldNodeNumberer;
  }

  /** Returns SparkOptions for this graph. */
  public SparkOptions getOpts() {
    return opts;
  }

  /** Returns CGOptions for this graph. */
  public CGOptions getCGOpts() {
    return cgOpts;
  }

  // Must be simple edges
  public Pair<Node, Node> addInterproceduralAssignment(Node from, Node to, Edge e) {
    Pair<Node, Node> val = new Pair<Node, Node>(from, to);
    if (runGeomPTA) {
      assign2edges.put(val, e);
    }
    return val;
  }

  public Set<Edge> lookupEdgesForAssignment(Pair<Node, Node> val) {
    return assign2edges.get(val);
  }

  public void addCallTarget(Edge e) {
    if (!e.passesParameters()) {
      return;
    }
    MethodPAG srcmpag = MethodPAG.v(this, e.src());
    MethodPAG tgtmpag = MethodPAG.v(this, e.tgt());
    Pair<Node, Node> pval;

    if (e.kind() == Kind.GENERIC_FAKE) {
      if (getOnFlyCallGraph() == null) {
        return;
      }
      VirtualEdgesSummaries summaries = getOnFlyCallGraph().ofcgb().getVirtualEdgeSummaries();
      InvokeExpr ie = e.srcStmt().getInvokeExpr();
      VirtualEdge ve = null;
      if (ie instanceof InstanceInvokeExpr) {
        InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
        ve = summaries.getVirtualEdgesMatchingSubSig(iie.getBase().getType(), 
            new MethodSubSignature(ie.getMethodRef().getSubSignature()));
      } else if (ie instanceof StaticInvokeExpr) {
        ve = summaries.getVirtualEdgesMatchingFunction(ie.getMethodRef().getSignature());
      }
      boolean isExpectedType = false;
      if (e.srcStmt().hasTag(ExpectedTypeTag.NAME)) {
        ExpectedTypeTag tag = (ExpectedTypeTag) e.srcStmt().getTag(ExpectedTypeTag.NAME);
        ve = new VirtualEdge(Kind.GENERIC_FAKE, null, new DeferredVirtualEdgeTarget(tag.getExpectedType()));
        isExpectedType = true;
      }

      // if there is no virtual edge there is no point in continuing
      if (ve == null) {
        return;
      }
      // The source is equal for direct and indirect targets
      VirtualEdgeSource edgeSrc = ve.getSource();

      if (edgeSrc instanceof InstanceinvokeSource || edgeSrc instanceof StaticinvokeSource || isExpectedType) {
        for (VirtualEdgeTarget edgeTgt : ve.getTargets()) {
          if (edgeTgt instanceof InvocationVirtualEdgeTarget) {
            InvocationVirtualEdgeTarget ieEdgeTgt = (InvocationVirtualEdgeTarget) edgeTgt;
            for (Local local : getOnFlyCallGraph().ofcgb().getReceiversOfVirtualEdge(ieEdgeTgt, ie)) {
              Node parm = srcmpag.nodeFactory().getNode(local);
              parm = srcmpag.parameterize(parm, e.srcCtxt());
              parm = parm.getReplacement();

              // Get the PAG node for the "this" local in the callback
              Node thiz = tgtmpag.nodeFactory().caseThis();
              thiz = tgtmpag.parameterize(thiz, e.tgtCtxt());
              thiz = thiz.getReplacement();

              // Make an edge from caller.argument to callee.this
              addEdge(parm, thiz);
              pval = addInterproceduralAssignment(parm, thiz, e);

              // We need to ensure that the target method exists, at least as a phantom
              if (local.getType() instanceof RefType) {
                RefType rtTarget = (RefType) local.getType();
                Scene.v().makeMethodRef(rtTarget.getSootClass(), ieEdgeTgt.getTargetMethod(), false).resolve();
              }
            }
          } else if (edgeTgt instanceof DeferredVirtualEdgeTarget && e.srcStmt() instanceof AssignStmt
              && ie.getMethodRef().getReturnType() instanceof RefType) {
            DeferredVirtualEdgeTarget de = (DeferredVirtualEdgeTarget) edgeTgt;

            // We need to fake an edge to the return value of the call
            Local lop = (Local) ((AssignStmt) e.srcStmt()).getLeftOp();
            Node ln = srcmpag.nodeFactory().getNode(lop);
            ln = srcmpag.parameterize(ln, e.srcCtxt());
            ln = ln.getReplacement();

            RefType rt = de.getTargetType();
            if (rt == null) {
              rt = (RefType) ie.getMethodRef().getReturnType();
            }

            // Fake an allocation node
            AllocNode alloc
                = makeAllocNode(new Pair<VarNode, SootClass>((VarNode) ln, rt.getSootClass()), AnySubType.v(rt), e.src());

            // temporary variable
            VarNode tmp = makeLocalVarNode(alloc, rt, e.src());

            // tmp = new T();
            addAllocEdge(alloc, tmp);
            addEdge(tmp, ln);
            // ofcg.updatedNode((VarNode) ln);
          }
        }
      }
    } else if (e.isExplicit() || e.kind() == Kind.THREAD) {
      addCallTarget(srcmpag, tgtmpag, (Stmt) e.srcUnit(), e.srcCtxt(), e.tgtCtxt(), e);
    } else if (e.kind() == Kind.ASYNCTASK) {
      addCallTarget(srcmpag, tgtmpag, (Stmt) e.srcUnit(), e.srcCtxt(), e.tgtCtxt(), e, false);
    } else if (e.kind() == Kind.EXECUTOR) {
      InvokeExpr ie = e.srcStmt().getInvokeExpr();

      Node parm = srcmpag.nodeFactory().getNode(ie.getArg(0));
      parm = srcmpag.parameterize(parm, e.srcCtxt());
      parm = parm.getReplacement();

      Node thiz = tgtmpag.nodeFactory().caseThis();
      thiz = tgtmpag.parameterize(thiz, e.tgtCtxt());
      thiz = thiz.getReplacement();

      addEdge(parm, thiz);
      pval = addInterproceduralAssignment(parm, thiz, e);

      if (callAssigns != null) {
        callToMethod.put(ie, srcmpag.getMethod());
        boolean virtualCall = !callAssigns.put(ie, pval);
        if (virtualCall) {
          virtualCallsToReceivers.putIfAbsent(ie, parm);
        }
      }
    } else if (e.kind() == Kind.HANDLER) {
      InvokeExpr ie = e.srcStmt().getInvokeExpr();

      Node base = srcmpag.nodeFactory().getNode(((VirtualInvokeExpr) ie).getBase());
      base = srcmpag.parameterize(base, e.srcCtxt());
      base = base.getReplacement();

      Node thiz = tgtmpag.nodeFactory().caseThis();
      thiz = tgtmpag.parameterize(thiz, e.tgtCtxt());
      thiz = thiz.getReplacement();

      addEdge(base, thiz);
      pval = addInterproceduralAssignment(base, thiz, e);
      if (callAssigns != null) {
        boolean virtualCall = !callAssigns.put(ie, pval);
        assert virtualCall;
        callToMethod.put(ie, srcmpag.getMethod());
        virtualCallsToReceivers.put(ie, base);
      }
    } else if (e.kind() == Kind.PRIVILEGED) {
      // Flow from first parameter of doPrivileged() invocation
      // to this of target, and from return of target to the
      // return of doPrivileged()
      InvokeExpr ie = e.srcStmt().getInvokeExpr();

      Node parm = srcmpag.nodeFactory().getNode(ie.getArg(0));
      parm = srcmpag.parameterize(parm, e.srcCtxt());
      parm = parm.getReplacement();

      Node thiz = tgtmpag.nodeFactory().caseThis();
      thiz = tgtmpag.parameterize(thiz, e.tgtCtxt());
      thiz = thiz.getReplacement();

      addEdge(parm, thiz);
      pval = addInterproceduralAssignment(parm, thiz, e);
      if (callAssigns != null) {
        callAssigns.put(ie, pval);
        callToMethod.put(ie, srcmpag.getMethod());
      }

      if (e.srcUnit() instanceof AssignStmt) {
        AssignStmt as = (AssignStmt) e.srcUnit();

        Node ret = tgtmpag.nodeFactory().caseRet();
        ret = tgtmpag.parameterize(ret, e.tgtCtxt());
        ret = ret.getReplacement();

        Node lhs = srcmpag.nodeFactory().getNode(as.getLeftOp());
        lhs = srcmpag.parameterize(lhs, e.srcCtxt());
        lhs = lhs.getReplacement();

        addEdge(ret, lhs);
        pval = addInterproceduralAssignment(ret, lhs, e);
        if (callAssigns != null) {
          callAssigns.put(ie, pval);
          callToMethod.put(ie, srcmpag.getMethod());
        }
      }
    } else if (e.kind() == Kind.FINALIZE) {
      Node srcThis = srcmpag.nodeFactory().caseThis();
      srcThis = srcmpag.parameterize(srcThis, e.srcCtxt());
      srcThis = srcThis.getReplacement();

      Node tgtThis = tgtmpag.nodeFactory().caseThis();
      tgtThis = tgtmpag.parameterize(tgtThis, e.tgtCtxt());
      tgtThis = tgtThis.getReplacement();

      addEdge(srcThis, tgtThis);
      pval = addInterproceduralAssignment(srcThis, tgtThis, e);
    } else if (e.kind() == Kind.NEWINSTANCE) {
      Stmt s = (Stmt) e.srcUnit();
      InstanceInvokeExpr iie = (InstanceInvokeExpr) s.getInvokeExpr();

      Node cls = srcmpag.nodeFactory().getNode(iie.getBase());
      cls = srcmpag.parameterize(cls, e.srcCtxt());
      cls = cls.getReplacement();
      Node newObject = nodeFactory.caseNewInstance((VarNode) cls);

      Node initThis = tgtmpag.nodeFactory().caseThis();
      initThis = tgtmpag.parameterize(initThis, e.tgtCtxt());
      initThis = initThis.getReplacement();

      addEdge(newObject, initThis);
      if (s instanceof AssignStmt) {
        AssignStmt as = (AssignStmt) s;
        Node asLHS = srcmpag.nodeFactory().getNode(as.getLeftOp());
        asLHS = srcmpag.parameterize(asLHS, e.srcCtxt());
        asLHS = asLHS.getReplacement();
        addEdge(newObject, asLHS);
      }

      pval = addInterproceduralAssignment(newObject, initThis, e);
      if (callAssigns != null) {
        callAssigns.put(s.getInvokeExpr(), pval);
        callToMethod.put(s.getInvokeExpr(), srcmpag.getMethod());
      }
    } else if (e.kind() == Kind.REFL_INVOKE) {
      // Flow (1) from first parameter of invoke(..) invocation
      // to this of target, (2) from the contents of the second (array)
      // parameter
      // to all parameters of the target, and (3) from return of target to
      // the
      // return of invoke(..)

      // (1)
      InvokeExpr ie = e.srcStmt().getInvokeExpr();
      Value arg0 = ie.getArg(0);
      final NullConstant nc = NullConstant.v();
      // if "null" is passed in, omit the edge
      if (arg0 != nc) {
        Node parm0 = srcmpag.nodeFactory().getNode(arg0);
        parm0 = srcmpag.parameterize(parm0, e.srcCtxt());
        parm0 = parm0.getReplacement();

        Node thiz = tgtmpag.nodeFactory().caseThis();
        thiz = tgtmpag.parameterize(thiz, e.tgtCtxt());
        thiz = thiz.getReplacement();

        addEdge(parm0, thiz);
        pval = addInterproceduralAssignment(parm0, thiz, e);
        if (callAssigns != null) {
          callAssigns.put(ie, pval);
          callToMethod.put(ie, srcmpag.getMethod());
        }
      }

      // (2)
      Value arg1 = ie.getArg(1);
      SootMethod tgt = e.getTgt().method();
      // if "null" is passed in, or target has no parameters, omit the
      // edge
      if (arg1 != nc && tgt.getParameterCount() > 0) {
        Node parm1 = srcmpag.nodeFactory().getNode(arg1);
        parm1 = srcmpag.parameterize(parm1, e.srcCtxt());
        parm1 = parm1.getReplacement();
        FieldRefNode parm1contents = makeFieldRefNode((VarNode) parm1, ArrayElement.v());

        for (int i = 0; i < tgt.getParameterCount(); i++) {
          // if no reference type, create no edge
          if (!(tgt.getParameterType(i) instanceof RefLikeType)) {
            continue;
          }

          Node tgtParmI = tgtmpag.nodeFactory().caseParm(i);
          tgtParmI = tgtmpag.parameterize(tgtParmI, e.tgtCtxt());
          tgtParmI = tgtParmI.getReplacement();

          addEdge(parm1contents, tgtParmI);
          pval = addInterproceduralAssignment(parm1contents, tgtParmI, e);
          if (callAssigns != null) {
            callAssigns.put(ie, pval);
          }
        }
      }

      // (3)
      // only create return edge if we are actually assigning the return
      // value and
      // the return type of the callee is actually a reference type
      if (e.srcUnit() instanceof AssignStmt && (tgt.getReturnType() instanceof RefLikeType)) {
        AssignStmt as = (AssignStmt) e.srcUnit();

        Node ret = tgtmpag.nodeFactory().caseRet();
        ret = tgtmpag.parameterize(ret, e.tgtCtxt());
        ret = ret.getReplacement();

        Node lhs = srcmpag.nodeFactory().getNode(as.getLeftOp());
        lhs = srcmpag.parameterize(lhs, e.srcCtxt());
        lhs = lhs.getReplacement();

        addEdge(ret, lhs);
        pval = addInterproceduralAssignment(ret, lhs, e);
        if (callAssigns != null) {
          callAssigns.put(ie, pval);
        }
      }
    } else if (e.kind() == Kind.REFL_CLASS_NEWINSTANCE || e.kind() == Kind.REFL_CONSTR_NEWINSTANCE) {
      // (1) create a fresh node for the new object
      // (2) create edge from this object to "this" of the constructor
      // (3) if this is a call to Constructor.newInstance and not
      // Class.newInstance,
      // create edges passing the contents of the arguments array of the
      // call
      // to all possible parameters of the target
      // (4) if we are inside an assign statement,
      // assign the fresh object from (1) to the LHS of the assign
      // statement

      Stmt s = (Stmt) e.srcUnit();
      InstanceInvokeExpr iie = (InstanceInvokeExpr) s.getInvokeExpr();

      // (1)
      Node cls = srcmpag.nodeFactory().getNode(iie.getBase());
      cls = srcmpag.parameterize(cls, e.srcCtxt());
      cls = cls.getReplacement();
      if (cls instanceof ContextVarNode) {
        cls = findLocalVarNode(((VarNode) cls).getVariable());
      }

      VarNode newObject = makeGlobalVarNode(cls, Scene.v().getObjectType());
      SootClass tgtClass = e.getTgt().method().getDeclaringClass();
      RefType tgtType = tgtClass.getType();
      AllocNode site = makeAllocNode(new Pair<Node, SootClass>(cls, tgtClass), tgtType, null);
      addEdge(site, newObject);

      // (2)
      Node initThis = tgtmpag.nodeFactory().caseThis();
      initThis = tgtmpag.parameterize(initThis, e.tgtCtxt());
      initThis = initThis.getReplacement();
      addEdge(newObject, initThis);
      addInterproceduralAssignment(newObject, initThis, e);

      // (3)
      if (e.kind() == Kind.REFL_CONSTR_NEWINSTANCE) {
        Value arg = iie.getArg(0);
        SootMethod tgt = e.getTgt().method();
        // if "null" is passed in, or target has no parameters, omit the
        // edge
        if (arg != NullConstant.v() && tgt.getParameterCount() > 0) {
          Node parm0 = srcmpag.nodeFactory().getNode(arg);
          parm0 = srcmpag.parameterize(parm0, e.srcCtxt());
          parm0 = parm0.getReplacement();
          FieldRefNode parm1contents = makeFieldRefNode((VarNode) parm0, ArrayElement.v());

          for (int i = 0; i < tgt.getParameterCount(); i++) {
            // if no reference type, create no edge
            if (!(tgt.getParameterType(i) instanceof RefLikeType)) {
              continue;
            }

            Node tgtParmI = tgtmpag.nodeFactory().caseParm(i);
            tgtParmI = tgtmpag.parameterize(tgtParmI, e.tgtCtxt());
            tgtParmI = tgtParmI.getReplacement();

            addEdge(parm1contents, tgtParmI);
            pval = addInterproceduralAssignment(parm1contents, tgtParmI, e);
            if (callAssigns != null) {
              callAssigns.put(iie, pval);
            }
          }
        }
      }

      // (4)
      if (s instanceof AssignStmt) {
        AssignStmt as = (AssignStmt) s;
        Node asLHS = srcmpag.nodeFactory().getNode(as.getLeftOp());
        asLHS = srcmpag.parameterize(asLHS, e.srcCtxt());
        asLHS = asLHS.getReplacement();
        addEdge(newObject, asLHS);
      }

      pval = addInterproceduralAssignment(newObject, initThis, e);
      if (callAssigns != null) {
        callAssigns.put(s.getInvokeExpr(), pval);
        callToMethod.put(s.getInvokeExpr(), srcmpag.getMethod());
      }
    } else {
      throw new RuntimeException("Unhandled edge " + e);
    }
  }

  /**
   * Adds method target as a possible target of the invoke expression in s. If target is null, only creates the nodes for the
   * call site, without actually connecting them to any target method.
   **/
  public void addCallTarget(MethodPAG srcmpag, MethodPAG tgtmpag, Stmt s, Context srcContext, Context tgtContext, Edge e) {
    addCallTarget(srcmpag, tgtmpag, s, srcContext, tgtContext, e, true);
  }

  /**
   * Adds method target as a possible target of the invoke expression in s. If target is null, only creates the nodes for the
   * call site, without actually connecting them to any target method.
   **/
  public void addCallTarget(MethodPAG srcmpag, MethodPAG tgtmpag, Stmt s, Context srcContext, Context tgtContext, Edge e,
      boolean propagateReturn) {
    MethodNodeFactory srcnf = srcmpag.nodeFactory();
    MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
    InvokeExpr ie = s.getInvokeExpr();
    int numArgs = ie.getArgCount();
    for (int i = 0; i < numArgs; i++) {
      Value arg = ie.getArg(i);
      if (!(arg.getType() instanceof RefLikeType) || (arg instanceof NullConstant)) {
        continue;
      }

      Node argNode = srcnf.getNode(arg);
      argNode = srcmpag.parameterize(argNode, srcContext);
      argNode = argNode.getReplacement();

      // target method's argument number may be less than source method, i.e. AsyncTask.execute(1) vs onPreExecute(0)
      // check for param return here, or NPE in paramTypes will be thrown
      Node parm = tgtnf.caseParm(i);
      if (parm == null) {
        continue;
      }

      parm = tgtmpag.parameterize(parm, tgtContext);
      parm = parm.getReplacement();

      addEdge(argNode, parm);
      Pair<Node, Node> pval = addInterproceduralAssignment(argNode, parm, e);
      if (callAssigns != null) {
        callAssigns.put(ie, pval);
        callToMethod.put(ie, srcmpag.getMethod());
      }
    }
    if (ie instanceof InstanceInvokeExpr) {
      InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;

      Node baseNode = srcnf.getNode(iie.getBase());
      baseNode = srcmpag.parameterize(baseNode, srcContext);
      baseNode = baseNode.getReplacement();

      Node thisRef = tgtnf.caseThis();
      thisRef = tgtmpag.parameterize(thisRef, tgtContext);
      thisRef = thisRef.getReplacement();
      addEdge(baseNode, thisRef);
      Pair<Node, Node> pval = addInterproceduralAssignment(baseNode, thisRef, e);
      if (callAssigns != null) {
        boolean virtualCall = !callAssigns.put(ie, pval);
        callToMethod.put(ie, srcmpag.getMethod());
        if (virtualCall) {
          virtualCallsToReceivers.putIfAbsent(ie, baseNode);
        }
      }
    }
    if (propagateReturn && s instanceof AssignStmt) {
      Value dest = ((AssignStmt) s).getLeftOp();
      if (dest.getType() instanceof RefLikeType && !(dest instanceof NullConstant)) {
        if (tgtnf.getMethod().getReturnType() instanceof VoidType) {
          logger.warn(
              tgtnf.getMethod() + " has a void return type, but we found a statement which uses its return value: " + s);
        } else {

          Node destNode = srcnf.getNode(dest);
          destNode = srcmpag.parameterize(destNode, srcContext);
          destNode = destNode.getReplacement();

          Node retNode = tgtnf.caseRet();
          retNode = tgtmpag.parameterize(retNode, tgtContext);
          retNode = retNode.getReplacement();

          addEdge(retNode, destNode);
          Pair<Node, Node> pval = addInterproceduralAssignment(retNode, destNode, e);
          if (callAssigns != null) {
            callAssigns.put(ie, pval);
            callToMethod.put(ie, srcmpag.getMethod());
          }
        }
      }
    }
  }

  /**
   * Delete all the assignment edges.
   */
  public void cleanPAG() {
    simple.clear();
    load.clear();
    store.clear();
    alloc.clear();
    simpleInv.clear();
    loadInv.clear();
    storeInv.clear();
    allocInv.clear();
  }

  /* End of package methods. */

  protected SparkOptions opts;
  protected CGOptions cgOpts;
  protected ClientAccessibilityOracle accessibilityOracle = Scene.v().getClientAccessibilityOracle();

  protected Map<VarNode, Object> simple = new HashMap<VarNode, Object>();
  protected Map<FieldRefNode, Object> load = new HashMap<FieldRefNode, Object>();
  protected Map<VarNode, Object> store = new HashMap<VarNode, Object>();
  protected Map<AllocNode, Object> alloc = new HashMap<AllocNode, Object>();
  protected Map<VarNode, Object> newInstance = new HashMap<VarNode, Object>();
  protected Map<NewInstanceNode, Object> assignInstance = new HashMap<NewInstanceNode, Object>();

  protected Map<VarNode, Object> simpleInv = new HashMap<VarNode, Object>();
  protected Map<VarNode, Object> loadInv = new HashMap<VarNode, Object>();
  protected Map<FieldRefNode, Object> storeInv = new HashMap<FieldRefNode, Object>();
  protected Map<VarNode, Object> allocInv = new HashMap<VarNode, Object>();
  protected Map<NewInstanceNode, Object> newInstanceInv = new HashMap<NewInstanceNode, Object>();
  protected Map<VarNode, Object> assignInstanceInv = new HashMap<VarNode, Object>();

  protected <K extends Node> boolean addToMap(Map<K, Object> m, K key, Node value) {
    Object valueList = m.get(key);

    if (valueList == null) {
      m.put(key, valueList = new HashSet(4));
    } else if (!(valueList instanceof Set)) {
      Node[] ar = (Node[]) valueList;
      HashSet<Node> vl = new HashSet<Node>(ar.length + 4);
      m.put(key, vl);
      for (Node element : ar) {
        vl.add(element);
      }
      return vl.add(value);
    }
    return ((Set<Node>) valueList).add(value);
  }

  private boolean runGeomPTA = false;
  protected MultiMap<Pair<Node, Node>, Edge> assign2edges = new HashMultiMap<>();
  private final Map<Object, LocalVarNode> valToLocalVarNode = new HashMap<>(1000);
  private final Map<Object, GlobalVarNode> valToGlobalVarNode = new HashMap<>(1000);
  private final Map<Object, AllocNode> valToAllocNode = new HashMap<>(1000);
  private final Table<Object, Type, AllocNode> valToReflAllocNode = HashBasedTable.create();
  private OnFlyCallGraph ofcg;
  private final ArrayList<VarNode> dereferences = new ArrayList<VarNode>();
  protected TypeManager typeManager;
  protected Map<Local, LocalVarNode> localToNodeMap = new HashMap<>();
  private final Map<Value, NewInstanceNode> newInstToNodeMap = new HashMap<>();
  public int maxFinishNumber = 0;
  private Map<Node, Tag> nodeToTag;
  private final GlobalNodeFactory nodeFactory = new GlobalNodeFactory(this);

  public GlobalNodeFactory nodeFactory() {
    return nodeFactory;
  }

  public NativeMethodDriver nativeMethodDriver;

  public HashMultiMap<InvokeExpr, Pair<Node, Node>> callAssigns;
  public Map<InvokeExpr, SootMethod> callToMethod;
  public Map<InvokeExpr, Node> virtualCallsToReceivers;

}
