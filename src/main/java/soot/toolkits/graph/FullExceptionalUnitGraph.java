package soot.toolkits.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Timothy Hoffman
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.RefType;
import soot.Trap;
import soot.Unit;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.exceptions.ThrowableSet;
import soot.util.Chain;

/**
 * Extension of {@link ExceptionalUnitGraph} that aligns more closely with the representation used by the JVM bytecode
 * verifier. The standard {@link ExceptionalUnitGraph} will not add an exception edge for an exception table entry if an
 * earlier entry already caught a broader exception type (i.e. the edge for the later entry will never actually execute
 * during runtime). However, the JVM bytecode verifier considers all exceptional edges verbatim from the exception table and
 * thus, may consider more possible paths in the CFG. Furthermore, this graph uses a {@link PedanticThrowAnalysis} to ensure
 * that all Units covered by an exception table entry will have an edge to the exception handler which forces phi-node
 * removal to back-propagate assignments all the way back to their original location to avoid "uninitialized register" errors
 * from the JVM bytecode verifier.
 *
 * @author Timothy Hoffman
 */
public class FullExceptionalUnitGraph extends ExceptionalUnitGraph {

  public FullExceptionalUnitGraph(Body body) {
    // Set 'omitExceptingUnitEdges' as false and use PedanticThrowAnalysis
    // so that all units will have an edge into exception handler blocks.
    this(body, PedanticThrowAnalysis.v(), false);
  }

  /**
   * IMPORTANT: This constructor should be used with care because the {@link ThrowAnalysis} should normally be
   * {@link PedanticThrowAnalysis} for the most accurate result (this is what the recommended constructor
   * {@link #FullExceptionalUnitGraph(soot.Body)} uses).
   * 
   * @param body
   * @param ta
   */
  public FullExceptionalUnitGraph(Body body, ThrowAnalysis ta) {
    this(body, ta, false);
  }

  /**
   * IMPORTANT: This constructor should be used with care because the {@link ThrowAnalysis} should normally be
   * {@link PedanticThrowAnalysis} and 'omitExceptingUnitEdges' should normally be 'false' for the most accurate result (this
   * is what the recommended constructor {@link #FullExceptionalUnitGraph(soot.Body)} uses).
   * 
   * @param body
   * @param ta
   * @param omitExceptingUnitEdges
   */
  public FullExceptionalUnitGraph(Body body, ThrowAnalysis ta, boolean omitExceptingUnitEdges) {
    super(body);
    initialize(ta, omitExceptingUnitEdges);
  }

  @Override
  protected Map<Unit, Collection<ExceptionDest>> buildExceptionDests(ThrowAnalysis throwAnalysis) {
    // Identical to the original except it doesn't track the uncaught
    // throwables when multiple Traps cover the same Unit. That way, the full
    // effect of all traps is reflected in the graph, even if some edges
    // will never be used because an earlier trap subsumes a later one.
    //
    Map<Unit, Collection<ExceptionDest>> result = null;

    final Chain<Trap> traps = body.getTraps();
    if (!traps.isEmpty()) {
      final ThrowableSet EMPTY = ThrowableSet.Manager.v().EMPTY;
      final Chain<Unit> units = body.getUnits();

      // Record the caught exceptions.
      for (Trap trap : traps) {
        RefType catcher = trap.getException().getType();
        for (Iterator<Unit> it = units.iterator(trap.getBeginUnit(), units.getPredOf(trap.getEndUnit())); it.hasNext();) {
          Unit unit = it.next();
          ThrowableSet thrownSet = throwAnalysis.mightThrow(unit);
          ThrowableSet.Pair catchableAs = thrownSet.whichCatchableAs(catcher);
          if (!EMPTY.equals(catchableAs.getCaught())) {
            result = addDestToMap(result, unit, trap, catchableAs.getCaught());
          } else {
            assert (thrownSet.equals(catchableAs.getUncaught())) :
              "ExceptionalUnitGraph.buildExceptionDests(): "
              + "catchableAs.caught == EMPTY, but catchableAs.uncaught != thrownSet" + System.getProperty("line.separator")
              + body.getMethod().getSubSignature() + " Unit: " + unit.toString() + System.getProperty("line.separator")
              + " catchableAs.getUncaught() == " + catchableAs.getUncaught().toString()
              + System.getProperty("line.separator") + " thrownSet == " + thrownSet.toString();
          }
        }
      }
    }
    return result == null ? Collections.emptyMap() : result;
  }
}
