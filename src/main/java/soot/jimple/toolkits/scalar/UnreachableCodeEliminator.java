package soot.jimple.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Phong Co
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
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.Singletons;
import soot.Trap;
import soot.Unit;
import soot.UnitBox;
import soot.options.Options;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class UnreachableCodeEliminator extends BodyTransformer {
  private static final Logger logger = LoggerFactory.getLogger(UnreachableCodeEliminator.class);

  protected ThrowAnalysis throwAnalysis = null;

  public static UnreachableCodeEliminator v() {
    return G.v().soot_jimple_toolkits_scalar_UnreachableCodeEliminator();
  }

  public UnreachableCodeEliminator(Singletons.Global g) {
  }

  public UnreachableCodeEliminator(ThrowAnalysis ta) {
    this.throwAnalysis = ta;
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    final boolean verbose = Options.v().verbose();
    if (verbose) {
      logger.debug("[" + body.getMethod().getName() + "] Eliminating unreachable code...");
    }

    // Force a conservative ExceptionalUnitGraph() which
    // necessarily includes an edge from every trapped Unit to
    // its handler, so that we retain Traps in the case where
    // trapped units remain, but the default ThrowAnalysis
    // says that none of them can throw the caught exception.
    if (this.throwAnalysis == null) {
      boolean opt = PhaseOptions.getBoolean(options, "remove-unreachable-traps", true);
      this.throwAnalysis = opt ? Scene.v().getDefaultThrowAnalysis() : PedanticThrowAnalysis.v();
    }

    final Chain<Unit> units = body.getUnits();
    final int origSize = units.size();
    final Set<Unit> reachable = origSize == 0 ? Collections.emptySet()
        : reachable(units.getFirst(), new ExceptionalUnitGraph(body, throwAnalysis, false));

    // Now eliminate empty traps. (and unreachable handlers)
    //
    // For the most part, this is an atavism, an an artifact of
    // pre-ExceptionalUnitGraph code, when the only way for a trap to
    // become unreachable was if all its trapped units were removed, and
    // the stmtIt loop did not remove Traps as it removed handler units.
    // We've left this separate test for empty traps here, even though
    // most such traps would already have been eliminated by the preceding
    // loop, because in arbitrary bytecode you could have
    // handler unit that was still reachable by normal control flow, even
    // though it no longer trapped any units (though such code is unlikely to
    // occur in practice, and certainly no in code generated from Java source.
    final Chain<Trap> traps = body.getTraps();
    for (Iterator<Trap> it = traps.iterator(); it.hasNext();) {
      final Trap trap = it.next();
      UnitBox beginBox = trap.getBeginUnitBox();
      UnitBox endBox = trap.getEndUnitBox();
      UnitBox handlerBox = trap.getHandlerUnitBox();
      if ((beginBox.getUnit() == endBox.getUnit()) || !reachable.contains(handlerBox.getUnit())) {
        it.remove();
        // Cleanup UnitBox references that are no longer used
        beginBox.getUnit().removeBoxPointingToThis(beginBox);
        endBox.getUnit().removeBoxPointingToThis(endBox);
        handlerBox.getUnit().removeBoxPointingToThis(handlerBox);
      }
    }

    // We must make sure that the end units of all traps which are still alive are kept in the code
    {
      final Unit lastUnit = units.getLast();
      for (Trap t : traps) {
        if (t.getEndUnit() == lastUnit) {
          reachable.add(lastUnit);
          break;
        }
      }
    }

    Set<Unit> notReachable = null;
    if (verbose) {
      notReachable = new HashSet<Unit>();
      for (Unit u : units) {
        if (!reachable.contains(u)) {
          notReachable.add(u);
        }
      }
    }

    units.retainAll(reachable);

    if (verbose) {
      final String name = body.getMethod().getName();
      logger.debug("[" + name + "]	 Removed " + (origSize - units.size()) + " statements: ");
      for (Unit u : notReachable) {
        logger.debug("[" + name + "]	         " + u);
      }
    }
  }

  // Used to be: "mark first statement and all its successors, recursively"
  // Bad idea! Some methods are extremely long. It broke because the recursion reached the
  // 3799th level.
  private <T> Set<T> reachable(T first, DirectedGraph<T> g) {
    if (first == null || g == null) {
      return Collections.emptySet();
    }
    Set<T> visited = new HashSet<T>(g.size());
    Deque<T> q = new ArrayDeque<T>();
    q.addFirst(first);
    do {
      T t = q.removeFirst();
      if (visited.add(t)) {
        q.addAll(g.getSuccsOf(t));
      }
    } while (!q.isEmpty());

    return visited;
  }
}
