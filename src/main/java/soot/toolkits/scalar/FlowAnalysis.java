package soot.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.RandomAccess;

import soot.baf.GotoInst;
import soot.jimple.GotoStmt;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.interaction.FlowInfo;
import soot.toolkits.graph.interaction.InteractionHandler;
import soot.util.Numberable;
import soot.util.PriorityQueue;

/**
 * An abstract class providing a framework for carrying out dataflow analysis. Subclassing either BackwardFlowAnalysis or
 * ForwardFlowAnalysis and providing implementations for the abstract methods will allow Soot to compute the corresponding
 * flow analysis.
 */
public abstract class FlowAnalysis<N, A> extends AbstractFlowAnalysis<N, A> {
  public enum Flow {
    IN {
      @Override
      <F> F getFlow(Entry<?, F> e) {
        return e.inFlow;
      }
    },
    OUT {
      @Override
      <F> F getFlow(Entry<?, F> e) {
        return e.outFlow;
      }
    };

    abstract <F> F getFlow(Entry<?, F> e);
  }

  static class Entry<D, F> implements Numberable {
    final D data;
    int number;

    /**
     * This Entry is part of a real scc.
     */
    boolean isRealStronglyConnected;

    Entry<D, F>[] in;
    Entry<D, F>[] out;
    F inFlow;
    F outFlow;

    @SuppressWarnings("unchecked")
    Entry(D u, Entry<D, F> pred) {
      in = new Entry[] { pred };
      data = u;
      number = Integer.MIN_VALUE;
      isRealStronglyConnected = false;
    }

    @Override
    public String toString() {
      return data == null ? "" : data.toString();
    }

    @Override
    public void setNumber(int n) {
      number = n;
    }

    @Override
    public int getNumber() {
      return number;
    }
  }

  static enum Orderer {
    INSTANCE;

    /**
     * Creates a new {@code Entry} graph based on a {@code DirectedGraph}. This includes pseudo topological order, local
     * access for predecessors and successors, a graph entry-point, a {@code Numberable} interface and a real strongly
     * connected component marker.
     *
     * @param g
     * @param gv
     * @param entryFlow
     * @return
     */
    <D, F> List<Entry<D, F>> newUniverse(DirectedGraph<D> g, GraphView gv, F entryFlow, boolean isForward) {
      final int n = g.size();

      Deque<Entry<D, F>> s = new ArrayDeque<Entry<D, F>>(n);
      List<Entry<D, F>> universe = new ArrayList<Entry<D, F>>(n);
      Map<D, Entry<D, F>> visited = new HashMap<D, Entry<D, F>>(((n + 1) * 4) / 3);

      // out of universe node
      Entry<D, F> superEntry = new Entry<D, F>(null, null);

      List<D> entries;
      {
        List<D> actualEntries = gv.getEntries(g);
        if (isForward) {
          if (!actualEntries.isEmpty()) {
            // normal case: there is at least one entry statement
            entries = actualEntries;
          } else {
            // error case: forward flow analysis on a method without any entry point
            throw new RuntimeException("error: forward analysis on an empty entry set.");
          }
        } else {
          // In a backward analysis, entry points must include any return
          // statements that exist (i.e. the 'actualEntries') but there
          // could also be infinite loops with no return statement that
          // must be included in the analysis as well.
          ArrayList<D> extraEntries = new ArrayList<D>();
  
          // Collect all 'goto' statements to capture the 'goto' from infinite loop(s)
          HashSet<D> visitedNodes = new HashSet<D>();
          LinkedList<D> workList = new LinkedList<D>(g.getHeads());
          while (!workList.isEmpty()) {
            D current = workList.remove(0);
            if (visitedNodes.add(current)) {
              // only add 'goto' statements
              if (current instanceof GotoStmt || current instanceof GotoInst) {
                extraEntries.add(current);
              }

              for (D next : g.getSuccsOf(current)) {
                if (!visitedNodes.contains(next)) {
                  workList.add(next);
                }
              }
            }
          }
          if (actualEntries.isEmpty() && extraEntries.isEmpty()) {
            throw new RuntimeException("error: backward analysis on an empty entry set.");
          }
          entries = new ArrayList<D>(actualEntries.size() + extraEntries.size());
          entries.addAll(actualEntries);
          entries.addAll(extraEntries);
        }
        assert (!entries.isEmpty());
        assert (entries.stream().distinct().count() == entries.size());
      }
      
      visitEntry(visited, superEntry, entries);
      superEntry.inFlow = entryFlow;
      superEntry.outFlow = entryFlow;

      @SuppressWarnings("unchecked")
      Entry<D, F>[] sv = new Entry[g.size()];
      int[] si = new int[g.size()];
      int index = 0;

      int i = 0;
      Entry<D, F> v = superEntry;

      for (;;) {
        if (i < v.out.length) {
          Entry<D, F> w = v.out[i++];

          // an unvisited child node
          if (w.number == Integer.MIN_VALUE) {
            w.number = s.size();
            s.add(w);

            visitEntry(visited, w, gv.getOut(g, w.data));

            // save old
            si[index] = i;
            sv[index] = v;
            index++;

            i = 0;
            v = w;
          }
        } else {
          if (index == 0) {
            assert universe.size() <= g.size();
            Collections.reverse(universe);
            return universe;
          }

          universe.add(v);
          sccPop(s, v);

          // restore old
          index--;
          v = sv[index];
          i = si[index];
        }
      }
    }

    private <D, F> Entry<D, F>[] visitEntry(Map<D, Entry<D, F>> visited, Entry<D, F> v, List<D> out) {
      final int n = out.size();
      @SuppressWarnings("unchecked")
      Entry<D, F>[] a = new Entry[n];

      assert (out instanceof RandomAccess);

      for (int i = 0; i < n; i++) {
        a[i] = getEntryOf(visited, out.get(i), v);
      }

      return v.out = a;
    }

    private <D, F> Entry<D, F> getEntryOf(Map<D, Entry<D, F>> visited, D d, Entry<D, F> v) {
      // either we reach a new node or a merge node, the latter one is rare
      // so put and restore should be better that a lookup
      // putIfAbsent would be the ideal strategy

      // add and restore if required
      Entry<D, F> newEntry = new Entry<D, F>(d, v);
      Entry<D, F> oldEntry = visited.put(d, newEntry);

      // no restore required
      if (oldEntry == null) {
        return newEntry;
      }

      // false prediction, restore the entry
      visited.put(d, oldEntry);

      // adding self ref (real strongly connected with itself)
      if (oldEntry == v) {
        oldEntry.isRealStronglyConnected = true;
      }

      // merge nodes are rare, so this is ok
      int l = oldEntry.in.length;
      oldEntry.in = Arrays.copyOf(oldEntry.in, l + 1);
      oldEntry.in[l] = v;
      return oldEntry;
    }

    private <D, F> void sccPop(Deque<Entry<D, F>> s, Entry<D, F> v) {
      int min = v.number;
      for (Entry<D, F> e : v.out) {
        assert e.number > Integer.MIN_VALUE;
        min = Math.min(min, e.number);
      }

      // not our SCC
      if (min != v.number) {
        v.number = min;
        return;
      }

      // we only want real SCCs (size > 1)
      Entry<D, F> w = s.removeLast();
      w.number = Integer.MAX_VALUE;
      if (w == v) {
        return;
      }

      w.isRealStronglyConnected = true;
      for (;;) {
        w = s.removeLast();
        assert w.number >= v.number;
        w.isRealStronglyConnected = true;
        w.number = Integer.MAX_VALUE;
        if (w == v) {
          assert w.in.length >= 2;
          return;
        }
      }
    }
  }

  enum InteractionFlowHandler {
    NONE, FORWARD {
      @Override
      public <A, N> void handleFlowIn(FlowAnalysis<N, A> a, N s) {
        beforeEvent(stop(s), a, s);
      }

      @Override
      public <A, N> void handleFlowOut(FlowAnalysis<N, A> a, N s) {
        afterEvent(InteractionHandler.v(), a, s);
      }
    },
    BACKWARD {
      @Override
      public <A, N> void handleFlowIn(FlowAnalysis<N, A> a, N s) {
        afterEvent(stop(s), a, s);
      }

      @Override
      public <A, N> void handleFlowOut(FlowAnalysis<N, A> a, N s) {
        beforeEvent(InteractionHandler.v(), a, s);
      }
    };

    <A, N> void beforeEvent(InteractionHandler i, FlowAnalysis<N, A> a, N s) {
      A savedFlow = a.filterUnitToBeforeFlow.get(s);
      if (savedFlow == null) {
        savedFlow = a.newInitialFlow();
      }
      a.copy(a.unitToBeforeFlow.get(s), savedFlow);
      i.handleBeforeAnalysisEvent(new FlowInfo<A, N>(savedFlow, s, true));
    }

    <A, N> void afterEvent(InteractionHandler i, FlowAnalysis<N, A> a, N s) {
      A savedFlow = a.filterUnitToAfterFlow.get(s);
      if (savedFlow == null) {
        savedFlow = a.newInitialFlow();
      }
      a.copy(a.unitToAfterFlow.get(s), savedFlow);
      i.handleAfterAnalysisEvent(new FlowInfo<A, N>(savedFlow, s, false));
    }

    InteractionHandler stop(Object s) {
      InteractionHandler h = InteractionHandler.v();
      List<?> stopList = h.getStopUnitList();
      if (stopList != null && stopList.contains(s)) {
        h.handleStopAtNodeEvent(s);
      }
      return h;
    }

    public <A, N> void handleFlowIn(FlowAnalysis<N, A> a, N s) {
    }

    public <A, N> void handleFlowOut(FlowAnalysis<N, A> a, N s) {
    }
  }

  enum GraphView {
    BACKWARD {
      @Override
      <N> List<N> getEntries(DirectedGraph<N> g) {
        return g.getTails();
      }

      @Override
      <N> List<N> getOut(DirectedGraph<N> g, N s) {
        return g.getPredsOf(s);
      }
    },
    FORWARD {
      @Override
      <N> List<N> getEntries(DirectedGraph<N> g) {
        return g.getHeads();
      }

      @Override
      <N> List<N> getOut(DirectedGraph<N> g, N s) {
        return g.getSuccsOf(s);
      }
    };

    abstract <N> List<N> getEntries(DirectedGraph<N> g);

    abstract <N> List<N> getOut(DirectedGraph<N> g, N s);
  }

  /** Maps graph nodes to OUT sets. */
  protected final Map<N, A> unitToAfterFlow;

  /** Filtered: Maps graph nodes to OUT sets. */
  protected Map<N, A> filterUnitToAfterFlow;

  /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
  public FlowAnalysis(DirectedGraph<N> graph) {
    super(graph);
    this.unitToAfterFlow = new IdentityHashMap<N, A>(graph.size() * 2 + 1);
    this.filterUnitToAfterFlow = Collections.emptyMap();
  }

  /**
   * Given the merge of the <code>out</code> sets, compute the <code>in</code> set for <code>s</code> (or in to out,
   * depending on direction).
   *
   * This function often causes confusion, because the same interface is used for both forward and backward flow analyses.
   * The first parameter is always the argument to the flow function (i.e. it is the "in" set in a forward analysis and the
   * "out" set in a backward analysis), and the third parameter is always the result of the flow function (i.e. it is the
   * "out" set in a forward analysis and the "in" set in a backward analysis).
   *
   * @param in
   *          the input flow
   * @param d
   *          the current node
   * @param out
   *          the returned flow
   **/
  protected abstract void flowThrough(A in, N d, A out);

  /** Accessor function returning value of OUT set for s. */

  public A getFlowAfter(N s) {
    A a = unitToAfterFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  @Override
  public A getFlowBefore(N s) {
    A a = unitToBeforeFlow.get(s);
    return a == null ? newInitialFlow() : a;
  }

  private void initFlow(Iterable<Entry<N, A>> universe, Map<N, A> in, Map<N, A> out) {
    assert universe != null;
    assert in != null;
    assert out != null;

    // If a node has only a single in-flow, the in-flow is always equal
    // to the out-flow if its predecessor, so we use the same object.
    // this saves memory and requires less object creation and copy calls.

    // Furthermore a node can be marked as omissible, this allows us to use
    // the same "flow-set" for out-flow and in-flow. A merge node with within
    // a real scc cannot be omitted, as it could cause endless loops within
    // the fixpoint-iteration!

    for (Entry<N, A> n : universe) {
      boolean omit = true;
      if (n.in.length > 1) {
        n.inFlow = newInitialFlow();

        // no merge points in loops
        omit = !n.isRealStronglyConnected;
      } else {
        assert n.in.length == 1 : "missing superhead";
        n.inFlow = getFlow(n.in[0], n);
        assert n.inFlow != null : "topological order is broken";
      }

      if (omit && omissible(n.data)) {
        // We could recalculate the graph itself but thats more expensive than
        // just falling through such nodes.
        n.outFlow = n.inFlow;
      } else {
        n.outFlow = newInitialFlow();
      }

      // for legacy api
      in.put(n.data, n.inFlow);
      out.put(n.data, n.outFlow);
    }
  }

  /**
   * If a flow node can be omitted return <code>true</code>, otherwise <code>false</code>. There is no guarantee a node will
   * be omitted. A omissible node does not influence the result of an analysis.
   *
   * If you are unsure, don't overwrite this method
   *
   * @param n
   *          the node to check
   * @return <code>false</code>
   */
  protected boolean omissible(N n) {
    return false;
  }

  /**
   * You can specify which flow set you would like to use of node {@code from}
   *
   * @param from
   * @param mergeNode
   * @return Flow.OUT
   */
  protected Flow getFlow(N from, N mergeNode) {
    return Flow.OUT;
  }

  private A getFlow(Entry<N, A> o, Entry<N, A> e) {
    return (o.inFlow == o.outFlow) ? o.outFlow : getFlow(o.data, e.data).getFlow(o);
  }

  private void meetFlows(Entry<N, A> e) {
    assert e.in.length >= 1;

    if (e.in.length > 1) {
      boolean copy = true;
      for (Entry<N, A> o : e.in) {
        A flow = getFlow(o, e);
        if (copy) {
          copy = false;
          copy(flow, e.inFlow);
        } else {
          mergeInto(e.data, e.inFlow, flow);
        }
      }
    }
  }

  final int doAnalysis(GraphView gv, InteractionFlowHandler ifh, Map<N, A> inFlow, Map<N, A> outFlow) {
    assert gv != null;
    assert ifh != null;

    ifh = Options.v().interactive_mode() ? ifh : InteractionFlowHandler.NONE;

    final List<Entry<N, A>> universe = Orderer.INSTANCE.newUniverse(graph, gv, entryInitialFlow(), isForward());
    initFlow(universe, inFlow, outFlow);

    Queue<Entry<N, A>> q = PriorityQueue.of(universe, true);

    // Perform fixed point flow analysis
    for (int numComputations = 0;; numComputations++) {
      Entry<N, A> e = q.poll();
      if (e == null) {
        return numComputations;
      }

      meetFlows(e);

      // Compute beforeFlow and store it.
      ifh.handleFlowIn(this, e.data);
      boolean hasChanged = flowThrough(e);
      ifh.handleFlowOut(this, e.data);

      // Update queue appropriately
      if (hasChanged) {
        q.addAll(Arrays.asList(e.out));
      }
    }
  }

  private boolean flowThrough(Entry<N, A> d) {
    // omitted, just fall through
    if (d.inFlow == d.outFlow) {
      assert !d.isRealStronglyConnected || d.in.length == 1;
      return true;
    }

    if (d.isRealStronglyConnected) {
      // A flow node that is influenced by at least one back-reference.
      // It's essential to check if "flowThrough" changes the result.
      // This requires the calculation of "equals", which itself
      // can be really expensive - depending on the used flow-model.
      // Depending on the "merge"+"flowThrough" costs, it can be cheaper
      // to fall through. Only nodes with real back-references always
      // need to be checked for changes
      A out = newInitialFlow();
      flowThrough(d.inFlow, d.data, out);
      if (out.equals(d.outFlow)) {
        return false;
      }
      // copy back the result, as it has changed
      copy(out, d.outFlow);
      return true;
    }

    // no back-references, just calculate "flowThrough"
    flowThrough(d.inFlow, d.data, d.outFlow);
    return true;
  }
}
