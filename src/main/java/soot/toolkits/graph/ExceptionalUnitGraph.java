package soot.toolkits.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrice Pominville, Raja Vallee-Rai
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.RefType;
import soot.Scene;
import soot.Timers;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.baf.Inst;
import soot.baf.NewInst;
import soot.baf.StaticGetInst;
import soot.baf.StaticPutInst;
import soot.baf.ThrowInst;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.options.Options;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.exceptions.ThrowableSet;
import soot.util.ArraySet;
import soot.util.Chain;

/**
 * <p>
 * Represents a control flow graph for a {@link Body} instance where the nodes are {@link Unit} instances, and where control
 * flow associated with exceptions is taken into account.
 * </p>
 *
 * <p>
 * To describe precisely the circumstances under which exceptional edges are added to the graph, we need to distinguish the
 * exceptions thrown explicitly by a <code>throw</code> instruction from the exceptions which are thrown implicitly by the VM
 * to signal an error it encounters in the course of executing an instruction, which need not be a <code>throw</code>.
 * </p>
 *
 * <p>
 * For every {@link ThrowInst} or {@link ThrowStmt} <code>Unit</code> which may explicitly throw an exception that would be
 * caught by a {@link Trap} in the <code>Body</code>, there will be an edge from the <code>throw</code> <code>Unit</code> to
 * the <code>Trap</code> handler's first <code>Unit</code>.
 * </p>
 *
 * <p>
 * For every <code>Unit</code> which may implicitly throw an exception that could be caught by a <code>Trap</code> in the
 * <code>Body</code>, there will be an edge from each of the excepting <code>Unit</code>'s predecessors to the
 * <code>Trap</code> handler's first <code>Unit</code> (since any of those predecessors may have been the last
 * <code>Unit</code> to complete execution before the handler starts execution). If the excepting <code>Unit</code> might
 * have the side effect of changing some field, then there will definitely be an edge from the excepting <code>Unit</code>
 * itself to its handlers, since the side effect might occur before the exception is raised. If the excepting
 * <code>Unit</code> has no side effects, then parameters passed to the <code>ExceptionalUnitGraph</code> constructor
 * determine whether or not there is an edge from the excepting <code>Unit</code> itself to the handler <code>Unit</code>.
 * </p>
 */
public class ExceptionalUnitGraph extends UnitGraph implements ExceptionalGraph<Unit> {
  // If there are no Traps within the method, these will be the same maps as unitToSuccs and unitToPreds.
  protected Map<Unit, List<Unit>> unitToUnexceptionalSuccs;
  protected Map<Unit, List<Unit>> unitToUnexceptionalPreds;

  protected Map<Unit, List<Unit>> unitToExceptionalSuccs;
  protected Map<Unit, List<Unit>> unitToExceptionalPreds;
  protected Map<Unit, Collection<ExceptionDest>> unitToExceptionDests;

  // Cached reference to the analysis used to generate this graph, for generating responses
  // to getExceptionDests() on the fly for nodes from which all exceptions escape the method.
  protected ThrowAnalysis throwAnalysis;

  /**
   * Constructs the graph for a given Body instance, using the <code>ThrowAnalysis</code> and
   * <code>omitExceptingUnitEdges</code> value that are passed as parameters.
   *
   * @param body
   *          the <code>Body</code> from which to build a graph.
   *
   * @param throwAnalysis
   *          the source of information about the exceptions which each {@link Unit} may throw.
   *
   * @param omitExceptingUnitEdges
   *          indicates whether the CFG should omit edges to a handler from trapped <code>Unit</code>s which may implicitly
   *          throw an exception which the handler catches but which have no potential side effects. The CFG will contain
   *          edges to the handler from all predecessors of <code>Unit</code>s which may implicitly throw a caught exception
   *          regardless of the setting for this parameter. If this parameter is <code>false</code>, there will also be edges
   *          to the handler from all the potentially excepting <code>Unit</code>s themselves. If this parameter is
   *          <code>true</code>, there will be edges to the handler from the excepting <code>Unit</code>s themselves only if
   *          they have potential side effects (or if they are themselves the predecessors of other potentially excepting
   *          <code>Unit</code>s). A setting of <code>true</code> produces CFGs which allow for more precise analyses, since
   *          a <code>Unit</code> without side effects has no effect on the computational state when it throws an exception.
   *          Use settings of <code>false</code> for compatibility with more conservative analyses, or to cater to
   *          conservative bytecode verifiers.
   */
  public ExceptionalUnitGraph(Body body, ThrowAnalysis throwAnalysis, boolean omitExceptingUnitEdges) {
    super(body);
    initialize(throwAnalysis, omitExceptingUnitEdges);
  }

  /**
   * Constructs the graph from a given Body instance using the passed {@link ThrowAnalysis} and a default value, provided by
   * the {@link Options} class, for the <code>omitExceptingUnitEdges</code> parameter.
   *
   * @param body
   *          the {@link Body} from which to build a graph.
   *
   * @param throwAnalysis
   *          the source of information about the exceptions which each {@link Unit} may throw.
   *
   */
  public ExceptionalUnitGraph(Body body, ThrowAnalysis throwAnalysis) {
    this(body, throwAnalysis, Options.v().omit_excepting_unit_edges());
  }

  /**
   * Constructs the graph from a given Body instance, using the {@link Scene} 's default {@link ThrowAnalysis} to estimate
   * the set of exceptions that each {@link Unit} might throw and a default value, provided by the {@link Options} class, for
   * the <code>omitExceptingUnitEdges</code> parameter.
   *
   * @param body
   *          the <code>Body</code> from which to build a graph.
   *
   */
  public ExceptionalUnitGraph(Body body) {
    this(body, Scene.v().getDefaultThrowAnalysis(), Options.v().omit_excepting_unit_edges());
  }

  /**
   * <p>
   * Allocates an <code>ExceptionalUnitGraph</code> object without initializing it. This &ldquo;partial constructor&rdquo; is
   * provided for the benefit of subclasses whose constructors need to perform some subclass-specific processing before
   * actually creating the graph edges (because, for example, the subclass overrides a utility method like
   * {@link #buildExceptionDests(ThrowAnalysis)} or {@link #buildExceptionalEdges(ThrowAnalysis, Map, Map, Map, boolean)}
   * with a replacement method that depends on additional parameters passed to the subclass's constructor). The subclass
   * constructor is responsible for calling {@link #initialize(ThrowAnalysis, boolean)}, or otherwise performing the
   * initialization required to implement <code>ExceptionalUnitGraph</code>'s interface.
   * </p>
   *
   * <p>
   * Clients who opt to extend <code>ExceptionalUnitGraph</code> should be warned that the class has not been carefully
   * designed for inheritance; code that uses the <code>protected</code> members of this class may need to be rewritten for
   * each new Soot release.
   * </p>
   *
   * @param body
   *          the <code>Body</code> from which to build a graph.
   *
   * @param ignoredBogusParameter
   *          a meaningless placeholder, which exists solely to distinguish this constructor from the public
   *          {@link #ExceptionalUnitGraph(Body)} constructor.
   */
  protected ExceptionalUnitGraph(Body body, boolean ignoredBogusParameter) {
    super(body);
  }

  /**
   * Performs the real work of constructing an <code>ExceptionalUnitGraph</code>, factored out of the constructors so that
   * subclasses have the option to delay creating the graph's edges until after they have performed some subclass-specific
   * initialization.
   *
   * @param throwAnalysis
   *          the source of information about the exceptions which each {@link Unit} may throw.
   *
   * @param omitExceptingUnitEdges
   *          indicates whether the CFG should omit edges to a handler from trapped <code>Unit</code>s which may throw an
   *          exception which the handler catches but which have no potential side effects.
   */
  protected void initialize(ThrowAnalysis throwAnalysis, boolean omitExceptingUnitEdges) {
    int size = unitChain.size();

    if (Options.v().time()) {
      Timers.v().graphTimer.start();
    }

    unitToUnexceptionalSuccs = new LinkedHashMap<Unit, List<Unit>>(size * 2 + 1, 0.7f);
    unitToUnexceptionalPreds = new LinkedHashMap<Unit, List<Unit>>(size * 2 + 1, 0.7f);
    buildUnexceptionalEdges(unitToUnexceptionalSuccs, unitToUnexceptionalPreds);
    this.throwAnalysis = throwAnalysis;

    Set<Unit> trapUnitsThatAreHeads;
    if (body.getTraps().isEmpty()) {
      // No handlers, so all exceptional control flow exits the method.
      unitToExceptionDests = Collections.emptyMap();
      unitToExceptionalSuccs = Collections.emptyMap();
      unitToExceptionalPreds = Collections.emptyMap();
      trapUnitsThatAreHeads = Collections.emptySet();
      unitToSuccs = unitToUnexceptionalSuccs;
      unitToPreds = unitToUnexceptionalPreds;
    } else {
      unitToExceptionDests = buildExceptionDests(throwAnalysis);
      unitToExceptionalSuccs = new LinkedHashMap<Unit, List<Unit>>(unitToExceptionDests.size() * 2 + 1, 0.7f);
      unitToExceptionalPreds = new LinkedHashMap<Unit, List<Unit>>(body.getTraps().size() * 2 + 1, 0.7f);
      trapUnitsThatAreHeads = buildExceptionalEdges(throwAnalysis, unitToExceptionDests, unitToExceptionalSuccs,
          unitToExceptionalPreds, omitExceptingUnitEdges);

      // We'll need separate maps for the combined
      // exceptional and unexceptional edges:
      unitToSuccs = combineMapValues(unitToUnexceptionalSuccs, unitToExceptionalSuccs);
      unitToPreds = combineMapValues(unitToUnexceptionalPreds, unitToExceptionalPreds);
    }
    buildHeadsAndTails(trapUnitsThatAreHeads);

    if (Options.v().time()) {
      Timers.v().graphTimer.end();
    }

    soot.util.PhaseDumper.v().dumpGraph(this);
  }

  /**
   * <p>
   * Utility method used in the construction of {@link soot.toolkits.graph.UnitGraph UnitGraph} variants which include
   * exceptional control flow. It determines which {@link Unit}s may throw exceptions that would be caught by {@link Trap}s
   * within the method.
   * </p>
   *
   * @param throwAnalysis
   *          The source of information about which exceptions each <code>Unit</code> may throw.
   *
   * @return <code>null</code> if no <code>Unit</code>s in the method throw any exceptions caught within the method.
   *         Otherwise, a {@link Map} from <code>Unit</code>s to {@link Collection}s of {@link ExceptionDest}s.
   *
   *         <p>
   *         The returned map has an idiosyncracy which is hidden from most client code, but which is exposed to subclasses
   *         extending <code>ExceptionalUnitGraph</code>. If a <code>Unit</code> throws one or more exceptions which are
   *         caught within the method, it will be mapped to a <code>Collection</code> of <code>ExceptionDest</code>s
   *         describing the sets of exceptions that the <code>Unit</code> might throw to each {@link Trap}. But if all of a
   *         <code>Unit</code>'s exceptions escape the method, it will be mapped to <code>null</code>, rather than to a
   *         <code>Collection</code> containing a single <code>ExceptionDest</code> with a <code>null</code> trap. (The
   *         special case for <code>Unit</code>s with no caught exceptions allows <code>buildExceptionDests()</code> to
   *         ignore completely <code>Unit</code>s which are outside the scope of all <code>Trap</code>s.)
   *         </p>
   */
  protected Map<Unit, Collection<ExceptionDest>> buildExceptionDests(ThrowAnalysis throwAnalysis) {
    Map<Unit, Collection<ExceptionDest>> result = null;

    final Chain<Trap> traps = body.getTraps();
    if (!traps.isEmpty()) {
      final ThrowableSet EMPTY = ThrowableSet.Manager.v().EMPTY;
      final Chain<Unit> units = body.getUnits();
      final Map<Unit, ThrowableSet> unitToUncaughtThrowables = new LinkedHashMap<Unit, ThrowableSet>(units.size());

      // Record the caught exceptions.
      for (Trap trap : traps) {
        RefType catcher = trap.getException().getType();
        for (Iterator<Unit> it = units.iterator(trap.getBeginUnit(), units.getPredOf(trap.getEndUnit())); it.hasNext();) {
          Unit unit = it.next();
          ThrowableSet thrownSet = unitToUncaughtThrowables.get(unit);
          if (thrownSet == null) {
            thrownSet = throwAnalysis.mightThrow(unit);
          }

          ThrowableSet.Pair catchableAs = thrownSet.whichCatchableAs(catcher);
          if (!EMPTY.equals(catchableAs.getCaught())) {
            result = addDestToMap(result, unit, trap, catchableAs.getCaught());
            unitToUncaughtThrowables.put(unit, catchableAs.getUncaught());
          } else {
            assert thrownSet.equals(catchableAs.getUncaught()) : "ExceptionalUnitGraph.buildExceptionDests(): "
                + "catchableAs.caught == EMPTY, but catchableAs.uncaught != thrownSet" + System.getProperty("line.separator")
                + body.getMethod().getSubSignature() + " Unit: " + unit.toString() + System.getProperty("line.separator")
                + " catchableAs.getUncaught() == " + catchableAs.getUncaught().toString()
                + System.getProperty("line.separator") + " thrownSet == " + thrownSet.toString();
          }
        }
      }

      for (Map.Entry<Unit, ThrowableSet> entry : unitToUncaughtThrowables.entrySet()) {
        ThrowableSet escaping = entry.getValue();
        if (escaping != EMPTY) {
          result = addDestToMap(result, entry.getKey(), null, escaping);
        }
      }
    }
    return result == null ? Collections.emptyMap() : result;
  }

  /**
   * A utility method for recording the exceptions that a <code>Unit</code> throws to a particular <code>Trap</code>. Note
   * that this method relies on the fact that the call to add escaping exceptions for a <code>Unit</code> will always follow
   * all calls for its caught exceptions.
   *
   * @param map
   *          A <code>Map</code> from <code>Unit</code>s to <code>Collection</code>s of <code>ExceptionDest</code>s.
   *          <code>null</code> if no exceptions have been recorded yet.
   *
   * @param u
   *          The <code>Unit</code> throwing the exceptions.
   *
   * @param t
   *          The <code>Trap</code> which catches the exceptions, or <code>null</code> if the exceptions escape the method.
   *
   * @param caught
   *          The set of exception types thrown by <code>u</code> which are caught by <code>t</code>.
   *
   * @return a <code>Map</code> which whose contents are equivalent to the input <code>map</code>, plus the information that
   *         <code>u</code> throws <code>caught</code> to <code>t</code>.
   */
  protected Map<Unit, Collection<ExceptionDest>> addDestToMap(Map<Unit, Collection<ExceptionDest>> map, Unit u,
      Trap t, ThrowableSet caught) {
    Collection<ExceptionDest> dests = (map == null ? null : map.get(u));
    if (dests == null) {
      if (t == null) {
        // All exceptions from u escape, so don't record any.
        return map;
      } else {
        if (map == null) {
          map = new LinkedHashMap<Unit, Collection<ExceptionDest>>(unitChain.size() * 2 + 1);
        }
        dests = new ArrayList<ExceptionDest>(3);
        map.put(u, dests);
      }
    }
    dests.add(new ExceptionDest(t, caught));
    return map;
  }

  /**
   * Method to compute the edges corresponding to exceptional control flow.
   *
   * @param throwAnalysis
   *          the source of information about the exceptions which each {@link Unit} may throw.
   *
   * @param unitToExceptionDests
   *          A <code>Map</code> from {@link Unit}s to {@link Collection}s of {@link ExceptionalUnitGraph.ExceptionDest
   *          ExceptionDest}s which represent the handlers that might catch exceptions thrown by the <code>Unit</code>. This
   *          is an ``in parameter''.
   *
   * @param unitToSuccs
   *          A <code>Map</code> from <code>Unit</code>s to {@link List}s of <code>Unit</code>s. This is an ``out
   *          parameter''; <code>buildExceptionalEdges</code> will add a mapping from every <code>Unit</code> in the body
   *          that may throw an exception that could be caught by a {@link Trap} in the body to a list of its exceptional
   *          successors.
   *
   * @param unitToPreds
   *          A <code>Map</code> from <code>Unit</code>s to <code>List</code>s of <code>Unit</code>s. This is an ``out
   *          parameter''; <code>buildExceptionalEdges</code> will add a mapping from each handler unit that may catch an
   *          exception to the list of <code>Unit</code>s whose exceptions it may catch.
   * @param omitExceptingUnitEdges
   *          Indicates whether to omit exceptional edges from excepting units which lack side effects
   *
   * @return a {@link Set} of trap <code>Unit</code>s that might catch exceptions thrown by the first <code>Unit</code> in
   *         the {@link Body} associated with the graph being constructed. Such trap <code>Unit</code>s may need to be added
   *         to the list of heads (depending on your definition of heads), since they can be the first <code>Unit</code> in
   *         the <code>Body</code> which actually completes execution.
   */
  protected Set<Unit> buildExceptionalEdges(ThrowAnalysis throwAnalysis,
      Map<Unit, Collection<ExceptionDest>> unitToExceptionDests, Map<Unit, List<Unit>> unitToSuccs,
      Map<Unit, List<Unit>> unitToPreds, boolean omitExceptingUnitEdges) {

    Set<Unit> trapsThatAreHeads = new ArraySet<Unit>();
    Unit entryPoint = unitChain.getFirst();
    for (Entry<Unit, Collection<ExceptionDest>> entry : unitToExceptionDests.entrySet()) {
      Unit thrower = entry.getKey();
      List<Unit> throwersPreds = getUnexceptionalPredsOf(thrower);
      Collection<ExceptionDest> dests = entry.getValue();

      // We need to recognize:
      // - caught exceptions for which we must add edges from the
      // thrower's predecessors to the catcher:
      // - all exceptions of non-throw instructions;
      // - implicit exceptions of throw instructions.
      //
      // - caught exceptions where we must add edges from the
      // thrower itself to the catcher:
      // - any exception of non-throw instructions if
      // omitExceptingUnitEdges is not set.
      // - any exception of non-throw instructions with side effects.
      // - explicit exceptions of throw instructions
      // - implicit exceptions of throw instructions if
      // omitExceptingUnitEdges is not set.
      // - implicit exceptions of throw instructions with possible
      // side effects (this is only possible for the grimp
      // IR, where the throw's argument may be an
      // expression---probably a NewInvokeExpr---which
      // might have executed partially before the
      // exception arose).
      //
      // Note that a throw instruction may be capable of throwing a given
      // Throwable type both implicitly and explicitly.
      //
      // We track these situations using predThrowables and
      // selfThrowables. Essentially predThrowables is the set
      // of Throwable types to whose catchers there should be
      // edges from predecessors of the thrower, while
      // selfThrowables is the set of Throwable types to whose
      // catchers there should be edges from the thrower itself,
      // but we we take some short cuts to avoid calling
      // ThrowableSet.catchableAs() when we can avoid it.

      boolean alwaysAddSelfEdges = ((!omitExceptingUnitEdges) || mightHaveSideEffects(thrower));
      ThrowableSet predThrowables = null;
      ThrowableSet selfThrowables = null;
      if (thrower instanceof ThrowInst) {
        ThrowInst throwInst = (ThrowInst) thrower;
        predThrowables = throwAnalysis.mightThrowImplicitly(throwInst);
        selfThrowables = throwAnalysis.mightThrowExplicitly(throwInst);
      } else if (thrower instanceof ThrowStmt) {
        ThrowStmt throwStmt = (ThrowStmt) thrower;
        predThrowables = throwAnalysis.mightThrowImplicitly(throwStmt);
        selfThrowables = throwAnalysis.mightThrowExplicitly(throwStmt);
      }

      for (ExceptionDest dest : dests) {
        if (dest.getTrap() != null) {
          Unit catcher = dest.getTrap().getHandlerUnit();
          RefType trapsType = dest.getTrap().getException().getType();
          if (predThrowables == null || predThrowables.catchableAs(trapsType)) {
            // Add edges from the thrower's predecessors to the
            // catcher.
            if (thrower == entryPoint) {
              trapsThatAreHeads.add(catcher);
            }
            for (Unit pred : throwersPreds) {
              addEdge(unitToSuccs, unitToPreds, pred, catcher);
            }
          }
          if (alwaysAddSelfEdges || (selfThrowables != null && selfThrowables.catchableAs(trapsType))) {
            addEdge(unitToSuccs, unitToPreds, thrower, catcher);
          }
        }
      }
    }

    // Now we have to worry about transitive exceptional
    // edges, when a handler might itself throw an exception
    // that is caught within the method. For that we need a
    // worklist containing CFG edges that lead to such a handler.
    class CFGEdge {
      Unit head; // If null, represents an edge to the handler
      // from the fictitious "predecessor" of the
      // very first unit in the chain. I.e., tail
      // is a handler which might be reached as a
      // result of an exception thrown by the
      // first Unit in the Body.
      Unit tail;

      CFGEdge(Unit head, Unit tail) {
        if (tail == null) {
          throw new RuntimeException("invalid CFGEdge(" + (head == null ? "null" : head.toString()) + ',' + "null" + ')');
        }
        this.head = head;
        this.tail = tail;
      }

      @Override
      public boolean equals(Object rhs) {
        if (rhs == this) {
          return true;
        }
        if (!(rhs instanceof CFGEdge)) {
          return false;
        }
        CFGEdge rhsEdge = (CFGEdge) rhs;
        return ((this.head == rhsEdge.head) && (this.tail == rhsEdge.tail));
      }

      @Override
      public int hashCode() {
        // Following Joshua Bloch's recipe in "Effective Java", Item 8:
        int result = 17;
        result = 37 * result + this.head.hashCode();
        result = 37 * result + this.tail.hashCode();
        return result;
      }
    }

    LinkedList<CFGEdge> workList = new LinkedList<CFGEdge>();

    for (Trap trap : body.getTraps()) {
      Unit handlerStart = trap.getHandlerUnit();
      if (mightThrowToIntraproceduralCatcher(handlerStart)) {
        List<Unit> handlerPreds = getUnexceptionalPredsOf(handlerStart);
        for (Unit pred : handlerPreds) {
          workList.addLast(new CFGEdge(pred, handlerStart));
        }
        handlerPreds = getExceptionalPredsOf(handlerStart);
        for (Unit pred : handlerPreds) {
          workList.addLast(new CFGEdge(pred, handlerStart));
        }
        if (trapsThatAreHeads.contains(handlerStart)) {
          workList.addLast(new CFGEdge(null, handlerStart));
        }
      }
    }

    // Now for every CFG edge that leads to a handler that may
    // itself throw an exception catchable within the method, add
    // edges from the head of that edge to the unit that catches
    // the handler's exception.
    while (workList.size() > 0) {
      CFGEdge edgeToThrower = workList.removeFirst();
      Unit pred = edgeToThrower.head;
      Unit thrower = edgeToThrower.tail;
      Collection<ExceptionDest> throwerDests = getExceptionDests(thrower);
      for (ExceptionDest dest : throwerDests) {
        if (dest.getTrap() != null) {
          Unit handlerStart = dest.getTrap().getHandlerUnit();
          boolean edgeAdded = false;
          if (pred == null) {
            if (!trapsThatAreHeads.contains(handlerStart)) {
              trapsThatAreHeads.add(handlerStart);
              edgeAdded = true;
            }
          } else {
            if (!getExceptionalSuccsOf(pred).contains(handlerStart)) {
              addEdge(unitToSuccs, unitToPreds, pred, handlerStart);
              edgeAdded = true;
            }
          }
          if (edgeAdded && mightThrowToIntraproceduralCatcher(handlerStart)) {
            workList.addLast(new CFGEdge(pred, handlerStart));
          }
        }
      }
    }
    return trapsThatAreHeads;
  }

  /**
   * <p>
   * Utility method for checking if a {@link Unit} might have side effects. It simply returns true for any unit which invokes
   * a method directly or which might invoke static initializers indirectly (by creating a new object or by referring to a
   * static field; see sections 2.17.4, 2.17.5, and 5.5 of the Java Virtual Machine Specification).
   * </p>
   *
   * <code>mightHaveSideEffects()</code> is declared package-private so that it is available to unit tests that are part of
   * this package.
   *
   * @param u
   *          The unit whose potential for side effects is to be checked.
   *
   * @return whether or not <code>u</code> has the potential for side effects.
   */
  static boolean mightHaveSideEffects(Unit u) {
    if (u instanceof Inst) {
      Inst i = (Inst) u;
      return (i.containsInvokeExpr() || (i instanceof StaticPutInst) || (i instanceof StaticGetInst)
          || (i instanceof NewInst));
    } else if (u instanceof Stmt) {
      for (ValueBox vb : u.getUseBoxes()) {
        Value v = vb.getValue();
        if ((v instanceof StaticFieldRef) || (v instanceof InvokeExpr) || (v instanceof NewExpr)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Utility method for checking if a Unit might throw an exception which may be caught by a {@link Trap} within this method.
   *
   * @param u
   *          The unit for whose exceptions are to be checked
   *
   * @return whether or not <code>u</code> may throw an exception which may be caught by a <code>Trap</code> in this method,
   */
  private boolean mightThrowToIntraproceduralCatcher(Unit u) {
    for (ExceptionDest dest : getExceptionDests(u)) {
      if (dest.getTrap() != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>
   * A placeholder that overrides {@link UnitGraph#buildHeadsAndTails()} with a method which always throws an exception. The
   * placeholder serves to indicate that <code>ExceptionalUnitGraph</code> does not use <code>buildHeadsAndTails()</code>,
   * and to document the conditions under which <code>ExceptionalUnitGraph considers a node to be a head or
   * tail.
   * </p>
   *
   * <p>
   * <code>ExceptionalUnitGraph</code> defines the graph's set of heads to include the first {@link Unit} in the graph's
   * body, together with the first <code>Unit</code> in any exception handler which might catch an exception thrown by the
   * first <code>Unit</code> in the body (because any of those <code>Unit</code>s might be the first to successfully complete
   * execution). <code>ExceptionalUnitGraph</code> defines the graph's set of tails to include all <code>Unit</code>s which
   * represent some variety of return bytecode or an <code>athrow</code> bytecode whose argument might escape the method.
   * </p>
   */
  @Override
  protected void buildHeadsAndTails() throws IllegalStateException {
    throw new IllegalStateException("ExceptionalUnitGraph uses buildHeadsAndTails(List) instead of buildHeadsAndTails()");
  }

  /**
   * Utility method, to be called only after the unitToPreds and unitToSuccs maps have been built. It defines the graph's set
   * of heads to include the first {@link Unit} in the graph's body, together with all the <code>Unit</code>s in
   * <code>additionalHeads</code>. It defines the graph's set of tails to include all <code>Unit</code>s which represent some
   * sort of return bytecode or an <code>athrow</code> bytecode which may escape the method.
   */
  private void buildHeadsAndTails(Set<Unit> additionalHeads) {
    heads = new ArrayList<Unit>(additionalHeads.size() + 1);
    heads.addAll(additionalHeads);

    if (unitChain.isEmpty()) {
      throw new IllegalStateException("No body for method " + body.getMethod().getSignature());
    }

    Unit entryPoint = unitChain.getFirst();
    if (!heads.contains(entryPoint)) {
      heads.add(entryPoint);
    }

    tails = new ArrayList<Unit>();
    for (Unit u : unitChain) {
      if (u instanceof soot.jimple.ReturnStmt || u instanceof soot.jimple.ReturnVoidStmt || u instanceof soot.baf.ReturnInst
          || u instanceof soot.baf.ReturnVoidInst) {
        tails.add(u);
      } else if (u instanceof soot.jimple.ThrowStmt || u instanceof soot.baf.ThrowInst) {
        int escapeMethodCount = 0;
        for (ExceptionDest dest : getExceptionDests(u)) {
          if (dest.getTrap() == null) {
            escapeMethodCount++;
          }
        }
        if (escapeMethodCount > 0) {
          tails.add(u);
        }
      }
    }
  }

  /**
   * Returns a collection of {@link ExceptionalUnitGraph.ExceptionDest ExceptionDest} objects which represent how exceptions
   * thrown by a specified unit will be handled.
   *
   * @param u
   *          The unit for which to provide exception information. ( <code>u</code> must be a <code>Unit</code>, though the
   *          parameter is declared as an <code>Object</code> to satisfy the interface of
   *          {@link soot.toolkits.graph.ExceptionalGraph ExceptionalGraph}.
   *
   * @return a collection of <code>ExceptionDest</code> objects describing the traps, if any, which catch the exceptions
   *         which may be thrown by <code>u</code>.
   */
  @Override
  public Collection<ExceptionDest> getExceptionDests(final Unit u) {
    Collection<ExceptionDest> result = unitToExceptionDests.get(u);
    if (result == null) {
      ExceptionDest e = new ExceptionDest(null, null) {
        @Override
        public ThrowableSet getThrowables() {
          if (null == throwables) {
            throwables = throwAnalysis.mightThrow(u);
          }
          return throwables;
        }
      };
      result = Collections.singletonList(e);
    }
    return result;
  }

  public static class ExceptionDest implements ExceptionalGraph.ExceptionDest<Unit> {
    private final Trap trap;
    ThrowableSet throwables;

    protected ExceptionDest(Trap trap, ThrowableSet throwables) {
      this.trap = trap;
      this.throwables = throwables;
    }

    @Override
    public Trap getTrap() {
      return trap;
    }

    @Override
    public ThrowableSet getThrowables() {
      return throwables;
    }

    @Override
    public Unit getHandlerNode() {
      if (trap == null) {
        return null;
      } else {
        return trap.getHandlerUnit();
      }
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append(getThrowables());
      buf.append(" -> ");
      if (trap == null) {
        buf.append("(escapes)");
      } else {
        buf.append(trap.toString());
      }
      return buf.toString();
    }
  }

  @Override
  public List<Unit> getUnexceptionalPredsOf(Unit u) {
    List<Unit> preds = unitToUnexceptionalPreds.get(u);
    return preds == null ? Collections.<Unit>emptyList() : preds;
  }

  @Override
  public List<Unit> getUnexceptionalSuccsOf(Unit u) {
    List<Unit> succs = unitToUnexceptionalSuccs.get(u);
    return succs == null ? Collections.<Unit>emptyList() : succs;
  }

  @Override
  public List<Unit> getExceptionalPredsOf(Unit u) {
    List<Unit> preds = unitToExceptionalPreds.get(u);
    return preds == null ? Collections.<Unit>emptyList() : preds;
  }

  @Override
  public List<Unit> getExceptionalSuccsOf(Unit u) {
    List<Unit> succs = unitToExceptionalSuccs.get(u);
    return succs == null ? Collections.<Unit>emptyList() : succs;
  }

  /**
   * <p>
   * Return the {@link ThrowAnalysis} used to construct this graph, if the graph contains no {@link Trap}s, or
   * <code>null</code> if the graph does contain <code>Trap</code>s. A reference to the <code>ThrowAnalysis</code> is kept
   * when there are no <code>Trap</code>s so that the graph can generate responses to {@link #getExceptionDests(Object)} on
   * the fly, rather than precomputing information that may never be needed.
   * </p>
   *
   * <p>
   * This method is package-private because it exposes a detail of the implementation of <code>ExceptionalUnitGraph</code> so
   * that the {@link soot.toolkits.graph.ExceptionalBlockGraph ExceptionalBlockGraph} constructor can cache the same
   * <code>ThrowAnalysis</code> for the same purpose.
   *
   * @return the {@link ThrowAnalysis} used to generate this graph if the graph contains no {@link Trap}s, or
   *         <code>null</code> if the graph contains one or more {@link Trap}s.
   */
  public ThrowAnalysis getThrowAnalysis() {
    return throwAnalysis;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (Unit u : unitChain) {
      buf.append("  preds: ").append(getPredsOf(u)).append('\n');
      buf.append("  unexceptional preds: ").append(getUnexceptionalPredsOf(u)).append('\n');
      buf.append("  exceptional preds: ").append(getExceptionalPredsOf(u)).append('\n');
      buf.append(u.toString()).append('\n');
      buf.append("  exception destinations: ").append(getExceptionDests(u)).append('\n');
      buf.append("  unexceptional succs: ").append(getUnexceptionalSuccsOf(u)).append('\n');
      buf.append("  exceptional succs: ").append(getExceptionalSuccsOf(u)).append('\n');
      buf.append("  succs ").append(getSuccsOf(u)).append("\n\n");
    }
    return buf.toString();
  }
}
