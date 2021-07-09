package soot.jimple.toolkits.annotation.arraycheck;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2000 Feng Qian
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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.tags.ArrayCheckTag;
import soot.options.ABCOptions;
import soot.options.Options;
import soot.tagkit.ColorTag;
import soot.tagkit.KeyTag;
import soot.tagkit.Tag;
import soot.util.Chain;

public class ArrayBoundsChecker extends SceneTransformer {
  private static final Logger logger = LoggerFactory.getLogger(ArrayBoundsChecker.class);

  public ArrayBoundsChecker(Singletons.Global g) {
  }

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {
    Scene.v().getReachableMethods().listener().forEachRemaining(method -> {
      internalTransform(method.method().getActiveBody(), options);
    });
  }

  public static ArrayBoundsChecker v() {
    return G.v().soot_jimple_toolkits_annotation_arraycheck_ArrayBoundsChecker();
  }

  protected boolean takeClassField = false;
  protected boolean takeFieldRef = false;
  protected boolean takeArrayRef = false;
  protected boolean takeCSE = false;
  protected boolean takeRectArray = false;
  protected boolean addColorTags = false;

  protected void internalTransform(Body body, Map opts) {
    ABCOptions options = new ABCOptions(opts);
    if (options.with_all()) {
      takeClassField = true;
      takeFieldRef = true;
      takeArrayRef = true;
      takeCSE = true;
      takeRectArray = true;
    } else {
      takeClassField = options.with_classfield();
      takeFieldRef = options.with_fieldref();
      takeArrayRef = options.with_arrayref();
      takeCSE = options.with_cse();
      takeRectArray = options.with_rectarray();
    }

    addColorTags = options.add_color_tags();

    {
      SootMethod m = body.getMethod();

      Date start = new Date();

      if (Options.v().verbose()) {
        logger.debug("[abc] Analyzing array bounds information for " + m.getName());
        logger.debug("[abc] Started on " + start);
      }

      ArrayBoundsCheckerAnalysis analysis = null;

      if (hasArrayLocals(body)) {
        analysis = new ArrayBoundsCheckerAnalysis(body, takeClassField, takeFieldRef, takeArrayRef, takeCSE, takeRectArray);
      }

      SootClass counterClass = null;
      SootMethod increase = null;

      if (options.profiling()) {
        counterClass = Scene.v().loadClassAndSupport("MultiCounter");
        increase = counterClass.getMethod("void increase(int)");
      }

      Chain units = body.getUnits();

      IntContainer zero = new IntContainer(0);

      Iterator unitIt = units.snapshotIterator();

      while (unitIt.hasNext()) {
        Stmt stmt = (Stmt) unitIt.next();

        if (stmt.containsArrayRef()) {
          ArrayRef aref = stmt.getArrayRef();

          {
            WeightedDirectedSparseGraph vgraph = (WeightedDirectedSparseGraph) analysis.getFlowBefore(stmt);

            int res = interpretGraph(vgraph, aref, stmt, zero);

            boolean lowercheck = true;
            boolean uppercheck = true;

            if (res == 0) {
              lowercheck = true;
              uppercheck = true;
            } else if (res == 1) {
              lowercheck = true;
              uppercheck = false;
            } else if (res == 2) {
              lowercheck = false;
              uppercheck = true;
            } else if (res == 3) {
              lowercheck = false;
              uppercheck = false;
            }

            if (addColorTags) {
              if (res == 0) {
                aref.getIndexBox().addTag(new ColorTag(255, 0, 0, false, ArrayCheckTag.NAME));
              } else if (res == 1) {
                aref.getIndexBox().addTag(new ColorTag(255, 248, 35, false, ArrayCheckTag.NAME));
              } else if (res == 2) {
                aref.getIndexBox().addTag(new ColorTag(255, 163, 0, false, ArrayCheckTag.NAME));
              } else if (res == 3) {
                aref.getIndexBox().addTag(new ColorTag(45, 255, 84, false, ArrayCheckTag.NAME));
              }
              SootClass bodyClass = body.getMethod().getDeclaringClass();
              Iterator keysIt = bodyClass.getTags().iterator();
              boolean keysAdded = false;
              while (keysIt.hasNext()) {
                Object next = keysIt.next();
                if (next instanceof KeyTag) {
                  if (((KeyTag) next).analysisType().equals(ArrayCheckTag.NAME)) {
                    keysAdded = true;
                  }
                }
              }
              if (!keysAdded) {
                bodyClass.addTag(new KeyTag(255, 0, 0, "ArrayBounds: Unsafe Lower and Unsafe Upper", ArrayCheckTag.NAME));
                bodyClass.addTag(new KeyTag(255, 248, 35, "ArrayBounds: Unsafe Lower and Safe Upper", ArrayCheckTag.NAME));
                bodyClass.addTag(new KeyTag(255, 163, 0, "ArrayBounds: Safe Lower and Unsafe Upper", ArrayCheckTag.NAME));
                bodyClass.addTag(new KeyTag(45, 255, 84, "ArrayBounds: Safe Lower and Safe Upper", ArrayCheckTag.NAME));
              }
            }

            /*
             * boolean lowercheck = true; boolean uppercheck = true;
             *
             * { if (Options.v().debug()) { if (!vgraph.makeShortestPathGraph()) { logger.debug(""+stmt+" :");
             * logger.debug(""+vgraph); } }
             *
             * Value base = aref.getBase(); Value index = aref.getIndex();
             *
             * if (index instanceof IntConstant) { int indexv = ((IntConstant)index).value;
             *
             * if (vgraph.hasEdge(base, zero)) { int alength = vgraph.edgeWeight(base, zero);
             *
             * if (-alength > indexv) uppercheck = false; }
             *
             * if (indexv >= 0) lowercheck = false; } else { if (vgraph.hasEdge(base, index)) { int upperdistance =
             * vgraph.edgeWeight(base, index); if (upperdistance < 0) uppercheck = false; }
             *
             * if (vgraph.hasEdge(index, zero)) { int lowerdistance = vgraph.edgeWeight(index, zero);
             *
             * if (lowerdistance <= 0) lowercheck = false; } } }
             */

            if (options.profiling()) {
              int lowercounter = 0;
              if (!lowercheck) {
                lowercounter = 1;
              }

              units.insertBefore(
                  Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(increase.makeRef(), IntConstant.v(lowercounter))),
                  stmt);

              int uppercounter = 2;
              if (!uppercheck) {
                uppercounter = 3;
              }

              units.insertBefore(
                  Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(increase.makeRef(), IntConstant.v(uppercounter))),
                  stmt);

              /*
               * if (!lowercheck && !uppercheck) { units.insertBefore(Jimple.v().newInvokeStmt(
               * Jimple.v().newStaticInvokeExpr(increase, IntConstant.v(4))), stmt);
               *
               * NullCheckTag nullTag = (NullCheckTag)stmt.getTag(NullCheckTag.NAME);
               *
               * if (nullTag != null && !nullTag.needCheck()) units.insertBefore(Jimple.v().newInvokeStmt(
               * Jimple.v().newStaticInvokeExpr(increase, IntConstant.v(7))), stmt); }
               */
            } else {
              Tag checkTag = new ArrayCheckTag(lowercheck, uppercheck);
              stmt.addTag(checkTag);
            }
          }
        }
      }

      if (addColorTags && takeRectArray) {
        RectangularArrayFinder raf = RectangularArrayFinder.v();
        for (Iterator vbIt = body.getUseAndDefBoxes().iterator(); vbIt.hasNext();) {
          final ValueBox vb = (ValueBox) vbIt.next();
          Value v = vb.getValue();
          if (!(v instanceof Local)) {
            continue;
          }
          Type t = v.getType();
          if (!(t instanceof ArrayType)) {
            continue;
          }
          ArrayType at = (ArrayType) t;
          if (at.numDimensions <= 1) {
            continue;
          }
          vb.addTag(new ColorTag(raf.isRectangular(new MethodLocal(m, (Local) v)) ? ColorTag.GREEN : ColorTag.RED));
        }
      }

      Date finish = new Date();
      if (Options.v().verbose()) {
        long runtime = finish.getTime() - start.getTime();
        logger.debug(
            "[abc] ended on " + finish + ". It took " + (runtime / 60000) + " min. " + ((runtime % 60000) / 1000) + " sec.");
      }
    }
  }

  private boolean hasArrayLocals(Body body) {
    Iterator localIt = body.getLocals().iterator();

    while (localIt.hasNext()) {
      Local local = (Local) localIt.next();
      if (local.getType() instanceof ArrayType) {
        return true;
      }
    }

    return false;
  }

  protected int interpretGraph(WeightedDirectedSparseGraph vgraph, ArrayRef aref, Stmt stmt, IntContainer zero) {

    boolean lowercheck = true;
    boolean uppercheck = true;

    {
      if (Options.v().debug()) {
        if (!vgraph.makeShortestPathGraph()) {
          logger.debug("" + stmt + " :");
          logger.debug("" + vgraph);
        }
      }

      Value base = aref.getBase();
      Value index = aref.getIndex();

      if (index instanceof IntConstant) {
        int indexv = ((IntConstant) index).value;

        if (vgraph.hasEdge(base, zero)) {
          int alength = vgraph.edgeWeight(base, zero);

          if (-alength > indexv) {
            uppercheck = false;
          }
        }

        if (indexv >= 0) {
          lowercheck = false;
        }
      } else {
        if (vgraph.hasEdge(base, index)) {
          int upperdistance = vgraph.edgeWeight(base, index);
          if (upperdistance < 0) {
            uppercheck = false;
          }
        }

        if (vgraph.hasEdge(index, zero)) {
          int lowerdistance = vgraph.edgeWeight(index, zero);

          if (lowerdistance <= 0) {
            lowercheck = false;
          }
        }
      }
    }

    if (lowercheck && uppercheck) {
      return 0;
    } else if (lowercheck && !uppercheck) {
      return 1;
    } else if (!lowercheck && uppercheck) {
      return 2;
    } else {
      return 3;
    }
  }
}
