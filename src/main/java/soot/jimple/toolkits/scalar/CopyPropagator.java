package soot.jimple.toolkits.scalar;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.NullType;
import soot.RefLikeType;
import soot.Scene;
import soot.Singletons;
import soot.Timers;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.options.CPOptions;
import soot.options.Options;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.graph.FullExceptionalUnitGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LocalDefs;

public class CopyPropagator extends BodyTransformer {
  private static final Logger logger = LoggerFactory.getLogger(CopyPropagator.class);

  protected final ThrowAnalysis throwAnalysis;
  protected final boolean forceOmitExceptingUnitEdges;

  public CopyPropagator(Singletons.Global g) {
    this(null, false);
  }

  public CopyPropagator(ThrowAnalysis ta) {
    this(ta, false);
  }

  public CopyPropagator(ThrowAnalysis ta, boolean forceOmitExceptingUnitEdges) {
    this.throwAnalysis = ta;
    this.forceOmitExceptingUnitEdges = forceOmitExceptingUnitEdges ? true : Options.v().omit_excepting_unit_edges();
  }

  public static CopyPropagator v() {
    return G.v().soot_jimple_toolkits_scalar_CopyPropagator();
  }

  /**
   * Cascaded copy propagator.
   *
   * <p>
   * If it encounters situations of the form: A: a = ...; B: ... x = a; C:... use (x); where a has only one definition, and x
   * has only one definition (B), then it can propagate immediately without checking between B and C for redefinitions of a
   * (namely) A because they cannot occur. In this case the propagator is global.
   *
   * <p>
   * Otherwise, if a has multiple definitions then it only checks for redefinitions of Propagates constants and copies in
   * extended basic blocks.
   *
   * <p>
   * Does not propagate stack locals when the "only-regular-locals" option is true.
   */
  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> opts) {
    if (Options.v().verbose()) {
      logger.debug("[" + b.getMethod().getName() + "] Propagating copies...");
    }
    if (Options.v().time()) {
      Timers.v().propagatorTimer.start();
    }

    // Count number of definitions for each local.
    Map<Local, Integer> localToDefCount = new HashMap<Local, Integer>();
    for (Unit u : b.getUnits()) {
      if (u instanceof DefinitionStmt) {
        Value leftOp = ((DefinitionStmt) u).getLeftOp();
        if (leftOp instanceof Local) {
          Local loc = (Local) leftOp;

          Integer old = localToDefCount.get(loc);
          localToDefCount.put(loc, (old == null) ? 1 : (old + 1));
        }
      }
    }

    // Go through the definitions, building the webs
    int fastCopyPropagationCount = 0;
    int slowCopyPropagationCount = 0;

    ThrowAnalysis ta = this.throwAnalysis;
    if (ta == null) {
      // NOTE: the CopyPropagator constructor should not call "Scene.v()"
      // thus, this condition check must remain here.
      ta = Scene.v().getDefaultThrowAnalysis();
    }
    UnitGraph graph = new FullExceptionalUnitGraph(b, ta, forceOmitExceptingUnitEdges);
    LocalDefs localDefs = G.v().soot_toolkits_scalar_LocalDefsFactory().newLocalDefs(graph);
    CPOptions options = new CPOptions(opts);
    // Perform a local propagation pass.
    for (Unit u : (new PseudoTopologicalOrderer<Unit>()).newList(graph, false)) {
      for (ValueBox useBox : u.getUseBoxes()) {
        Value value = useBox.getValue();
        if (value instanceof Local) {
          Local l = (Local) value;

          // We force propagating nulls. If a target can only be
          // null due to typing, we always inline that constant.
          if (!(l.getType() instanceof NullType)) {
            if (options.only_regular_locals() && l.getName().startsWith("$")) {
              continue;
            }
            if (options.only_stack_locals() && !l.getName().startsWith("$")) {
              continue;
            }
          }

          // We can propagate the definition if we either only have one definition
          // or all definitions are side-effect free and equal. For starters, we
          // only support constants in the case of multiple definitions.
          List<Unit> defsOfUse = localDefs.getDefsOfAt(l, u);
          boolean propagateDef = defsOfUse.size() == 1;
          if (!propagateDef && defsOfUse.size() > 0) {
            boolean agrees = true;
            Constant constVal = null;
            for (Unit defUnit : defsOfUse) {
              boolean defAgrees = false;
              if (defUnit instanceof AssignStmt) {
                Value rightOp = ((AssignStmt) defUnit).getRightOp();
                if (rightOp instanceof Constant) {
                  if (constVal == null) {
                    constVal = (Constant) rightOp;
                    defAgrees = true;
                  } else if (constVal.equals(rightOp)) {
                    defAgrees = true;
                  }
                }
              }
              agrees &= defAgrees;
            }
            propagateDef = agrees;
          }

          if (propagateDef) {
            final DefinitionStmt def = (DefinitionStmt) defsOfUse.get(0);
            final Value rightOp = def.getRightOp();

            if (rightOp instanceof Constant) {
              if (useBox.canContainValue(rightOp)) {
                useBox.setValue(rightOp);
                copyLineTags(useBox, def);
              }
            } else if (rightOp instanceof CastExpr) {
              CastExpr ce = (CastExpr) rightOp;
              if (ce.getCastType() instanceof RefLikeType) {
                Value op = ce.getOp();
                if ((op instanceof IntConstant && ((IntConstant) op).value == 0)
                    || (op instanceof LongConstant && ((LongConstant) op).value == 0)) {
                  if (useBox.canContainValue(NullConstant.v())) {
                    useBox.setValue(NullConstant.v());
                    copyLineTags(useBox, def);
                  }
                }
              }
            } else if (rightOp instanceof Local) {
              Local m = (Local) rightOp;
              if (l != m) {
                Integer defCount = localToDefCount.get(m);
                if (defCount == null || defCount == 0) {
                  throw new RuntimeException("Variable " + m + " used without definition!");
                } else if (defCount == 1) {
                  useBox.setValue(m);
                  copyLineTags(useBox, def);
                  fastCopyPropagationCount++;
                  continue;
                }

                List<Unit> path = graph.getExtendedBasicBlockPathBetween(def, u);
                if (path == null) {
                  // no path in the extended basic block
                  continue;
                }

                {
                  boolean isRedefined = false;

                  Iterator<Unit> pathIt = path.iterator();
                  // Skip first node
                  pathIt.next();
                  // Make sure that m is not redefined along path
                  while (pathIt.hasNext()) {
                    Stmt s = (Stmt) pathIt.next();

                    if (u == s) {
                      // Don't look at the last statement
                      // since it is evaluated after the uses.
                      break;
                    }
                    if (s instanceof DefinitionStmt) {
                      if (((DefinitionStmt) s).getLeftOp() == m) {
                        isRedefined = true;
                        break;
                      }
                    }
                  }

                  if (isRedefined) {
                    continue;
                  }
                }

                useBox.setValue(m);
                slowCopyPropagationCount++;
              }
            }
          }
        }
      }
    }

    if (Options.v().verbose()) {
      logger.debug("[" + b.getMethod().getName() + "]     Propagated: " + fastCopyPropagationCount + " fast copies  "
          + slowCopyPropagationCount + " slow copies");
    }
    if (Options.v().time()) {
      Timers.v().propagatorTimer.end();
    }
  }

  public static void copyLineTags(ValueBox useBox, DefinitionStmt def) {
    // we might have a def statement which contains a propagated constant itself as right-op. we
    // want to propagate the tags of this constant and not the def statement itself in this case.
    if (!copyLineTags(useBox, def.getRightOpBox())) {
      copyLineTags(useBox, (Host) def);
    }
  }

  /**
   * Copies the {@link SourceLnPosTag} and {@link LineNumberTag}s from the given host to the given ValueBox
   *
   * @param useBox
   *          The box to which the position tags should be copied
   * @param host
   *          The host from which the position tags should be copied
   * @return True if a copy was conducted, false otherwise
   */
  private static boolean copyLineTags(ValueBox useBox, Host host) {
    boolean res = false;

    Tag tag = host.getTag(SourceLnPosTag.NAME);
    if (tag != null) {
      useBox.addTag(tag);
      res = true;
    }

    tag = host.getTag(LineNumberTag.NAME);
    if (tag != null) {
      useBox.addTag(tag);
      res = true;
    }

    return res;
  }
}
