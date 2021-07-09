package soot.jimple.toolkit.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Timothy Hoffman
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
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import soot.Body;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.options.Options;
import soot.tagkit.CodeAttribute;
import soot.tagkit.Tag;
import soot.testing.framework.AbstractTestingFramework;

@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class UnreachableCodeEliminatorTest extends AbstractTestingFramework {

  private static final String TEST_TARGET_CLASS = "soot.jimple.toolkits.scalar.UnreachableCodeEliminatorTestInput";

  @Override
  protected void setupSoot() {
    // Skip JimpleBody UnreachableCodeEliminator so we can
    // observe the effect of running it manually below.
    Options.v().setPhaseOption("jb.uce", "enabled:false");

    // Disable validation because it will give an error with UCE disabled.
    Options.v().set_validate(false);
  }

  @Test
  public void unitBoxConsistency() {
    SootMethod target =
        prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "unreachableTrap"), TEST_TARGET_CLASS);

    Body body = target.retrieveActiveBody();

    // There is 1 Trap before the optimization.
    Assert.assertEquals(1, body.getTraps().size());

    // Assert that the set of boxes obtained from all units equals the set
    // obtained by checking Unit#getBoxesPointingToThis() on all units.
    Assert.assertEquals(new HashSet<>(getAllUnitBoxes(body)), new HashSet<>(getAllBoxesPointingToUnits(body)));

    UnreachableCodeEliminator.v().transform(body);

    // There are no Traps after the optimization.
    Assert.assertEquals(0, body.getTraps().size());

    // Assert that the set of boxes obtained from all units equals the set
    // obtained by checking Unit#getBoxesPointingToThis() on all units.
    Assert.assertEquals(new HashSet<>(getAllUnitBoxes(body)), new HashSet<>(getAllBoxesPointingToUnits(body)));
  }

  private static List<UnitBox> getAllUnitBoxes(Body body) {
    ArrayList<UnitBox> unitBoxList = new ArrayList<>();
    for (Unit u : body.getUnits()) {
      unitBoxList.addAll(u.getUnitBoxes());
    }
    for (Trap t : body.getTraps()) {
      unitBoxList.addAll(t.getUnitBoxes());
    }
    for (Tag t : body.getTags()) {
      if (t instanceof CodeAttribute) {
        unitBoxList.addAll(((CodeAttribute) t).getUnitBoxes());
      }
    }
    return unitBoxList;
  }

  private static List<UnitBox> getAllBoxesPointingToUnits(Body body) {
    ArrayList<UnitBox> unitBoxList = new ArrayList<>();
    for (Unit u : body.getUnits()) {
      unitBoxList.addAll(u.getBoxesPointingToThis());
    }
    return unitBoxList;
  }
}
