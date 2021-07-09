package soot.asm;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 hluwa
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

import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.internal.JNopStmt;
import soot.testing.framework.AbstractTestingFramework;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*"})
public class NestLabelJumpTest extends AbstractTestingFramework {

  private static final String TEST_TARGET_CLASS = "soot.asm.NestLabel";

  static class JSwitchBuilder {
    private static final Local defaultKey = Jimple.v().newLocal("v0", UnknownType.v());

    private JLookupSwitchStmt stmt;

    private Unit defaultTarget;
    private Value key = defaultKey;
    private final Map<Integer, Unit> cases = new LinkedHashMap<>();


    public JSwitchBuilder() {
    }

    public JSwitchBuilder(JLookupSwitchStmt stmt) {
      this.stmt = stmt;
      this.key = stmt.getKey();
      this.defaultTarget = stmt.getDefaultTarget();
      for (int idx = 0; idx < stmt.getLookupValues().size(); idx++) {
        this.cases.put(stmt.getLookupValue(idx), stmt.getTarget(idx));
      }
    }

    public JSwitchBuilder defaultTarget(Unit stmt) {
      this.defaultTarget = stmt;
      return this;
    }

    public JSwitchBuilder addCase(int caseValue, Unit caseTarget) {
      this.cases.put(caseValue, caseTarget);
      return this;
    }

    public JLookupSwitchStmt build() {
      List<IntConstant> caseValues = new LinkedList<>();
      List<Unit> caseTargets = new LinkedList<>();
      for (Map.Entry<Integer, Unit> entry : this.cases.entrySet()) {
        caseValues.add(IntConstant.v(entry.getKey()));
        caseTargets.add(entry.getValue());
      }
      if (this.stmt == null) {
        this.stmt = new JLookupSwitchStmt(this.key, caseValues, caseTargets, defaultTarget);
      } else {
        this.stmt.setKey(this.key);
        this.stmt.setLookupValues(caseValues);
        this.stmt.setTargets(caseTargets);
        this.stmt.setDefaultTarget(this.defaultTarget);
      }
      return this.stmt;
    }
  }


  @Test
  public void switchToString() {
    SootMethod switchMethod = prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "nestSwitch"), TEST_TARGET_CLASS);
    Body body = addNestSwitch(switchMethod.getActiveBody());
    String exception = null;
    try {
      for (Unit unit : body.getUnits()) {
        String ignored = unit.toString();
      }
    } catch (Throwable throwable) {
      exception = throwable.toString();
    }
    assertNull(exception);
  }

  private Body addNestSwitch(Body body) {
    UnitPatchingChain units = body.getUnits();
    Unit defaultBranch = new JNopStmt();
    JLookupSwitchStmt switchStmt1 = new JSwitchBuilder().defaultTarget(defaultBranch).addCase(0, defaultBranch).build();
    JLookupSwitchStmt switchStmt2 = new JSwitchBuilder().defaultTarget(defaultBranch).addCase(0, switchStmt1).build();
    switchStmt1 = new JSwitchBuilder(switchStmt1).addCase(0, switchStmt2).build();
    units.add(switchStmt1);
    units.add(switchStmt2);
    units.add(defaultBranch);
    return body;
  }

}
