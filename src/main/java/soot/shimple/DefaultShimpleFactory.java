package soot.shimple;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004 Navindra Umanee <navindra@cs.mcgill.ca>
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

import soot.Body;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.shimple.toolkits.graph.GlobalValueNumberer;
import soot.shimple.toolkits.graph.SimpleGlobalValueNumberer;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BlockGraphConverter;
import soot.toolkits.graph.CytronDominanceFrontier;
import soot.toolkits.graph.DominanceFrontier;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.FullExceptionalUnitGraph;
import soot.toolkits.graph.HashReversibleGraph;
import soot.toolkits.graph.ReversibleGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

/**
 * @author Navindra Umanee
 */
public class DefaultShimpleFactory implements ShimpleFactory {

  protected final Body body;

  protected UnitGraph ug;
  protected BlockGraph bg;

  protected DominatorsFinder<Block> dFinder;
  protected DominatorTree<Block> dTree;
  protected DominanceFrontier<Block> dFrontier;
  protected GlobalValueNumberer gvn;

  protected ReversibleGraph<Block> rbg;
  protected DominatorsFinder<Block> rdFinder;
  protected DominatorTree<Block> rdTree;
  protected DominanceFrontier<Block> rdFrontier;

  public DefaultShimpleFactory(Body body) {
    this.body = body;
  }

  @Override
  public void clearCache() {
    this.ug = null;
    this.bg = null;
    this.dFinder = null;
    this.dTree = null;
    this.dFrontier = null;
    this.gvn = null;
    this.rbg = null;
    this.rdFinder = null;
    this.rdTree = null;
    this.rdFrontier = null;
  }

  public Body getBody() {
    Body body = this.body;
    if (body == null) {
      throw new RuntimeException("Assertion failed: Call setBody() first.");
    }
    return body;
  }

  @Override
  public ReversibleGraph<Block> getReverseBlockGraph() {
    ReversibleGraph<Block> rbg = this.rbg;
    if (rbg == null) {
      rbg = new HashReversibleGraph<Block>(getBlockGraph());
      rbg.reverse();
      this.rbg = rbg;
    }
    return rbg;
  }

  @Override
  public DominatorsFinder<Block> getReverseDominatorsFinder() {
    DominatorsFinder<Block> rdFinder = this.rdFinder;
    if (rdFinder == null) {
      rdFinder = new SimpleDominatorsFinder<Block>(getReverseBlockGraph());
      this.rdFinder = rdFinder;
    }
    return rdFinder;
  }

  @Override
  public DominatorTree<Block> getReverseDominatorTree() {
    DominatorTree<Block> rdTree = this.rdTree;
    if (rdTree == null) {
      rdTree = new DominatorTree<Block>(getReverseDominatorsFinder());
      this.rdTree = rdTree;
    }
    return rdTree;
  }

  @Override
  public DominanceFrontier<Block> getReverseDominanceFrontier() {
    DominanceFrontier<Block> rdFrontier = this.rdFrontier;
    if (rdFrontier == null) {
      rdFrontier = new CytronDominanceFrontier<Block>(getReverseDominatorTree());
      this.rdFrontier = rdFrontier;
    }
    return rdFrontier;
  }

  @Override
  public BlockGraph getBlockGraph() {
    BlockGraph bg = this.bg;
    if (bg == null) {
      bg = new ExceptionalBlockGraph((ExceptionalUnitGraph) getUnitGraph());
      BlockGraphConverter.addStartStopNodesTo(bg);
      this.bg = bg;
    }
    return bg;
  }

  @Override
  public UnitGraph getUnitGraph() {
    UnitGraph ug = this.ug;
    if (ug == null) {
      Body body = getBody();
      UnreachableCodeEliminator.v().transform(body);
      ug = new FullExceptionalUnitGraph(body);
      this.ug = ug;
    }
    return ug;
  }

  @Override
  public DominatorsFinder<Block> getDominatorsFinder() {
    DominatorsFinder<Block> dFinder = this.dFinder;
    if (dFinder == null) {
      dFinder = new SimpleDominatorsFinder<Block>(getBlockGraph());
      this.dFinder = dFinder;
    }
    return dFinder;
  }

  @Override
  public DominatorTree<Block> getDominatorTree() {
    DominatorTree<Block> dTree = this.dTree;
    if (dTree == null) {
      dTree = new DominatorTree<Block>(getDominatorsFinder());
      this.dTree = dTree;
    }
    return dTree;
  }

  @Override
  public DominanceFrontier<Block> getDominanceFrontier() {
    DominanceFrontier<Block> dFrontier = this.dFrontier;
    if (dFrontier == null) {
      dFrontier = new CytronDominanceFrontier<Block>(getDominatorTree());
      this.dFrontier = dFrontier;
    }
    return dFrontier;
  }

  @Override
  public GlobalValueNumberer getGlobalValueNumberer() {
    GlobalValueNumberer gvn = this.gvn;
    if (gvn == null) {
      gvn = new SimpleGlobalValueNumberer(getBlockGraph());
      this.gvn = gvn;
    }
    return gvn;
  }
}
