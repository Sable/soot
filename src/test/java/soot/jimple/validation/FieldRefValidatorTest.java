package soot.jimple.validation;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import soot.Body;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.Value;
import soot.VoidType;
import soot.baf.BafBody;
import soot.baf.Inst;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.util.Chain;
import soot.validation.ValidationException;

/**
 *
 * @author Timothy Hoffman
 */
public class FieldRefValidatorTest {

  public FieldRefValidatorTest() {
  }

  @Before
  public void setUp() {
    G.reset();

    final Options opts = Options.v();
    opts.set_allow_phantom_refs(false);
    opts.set_no_bodies_for_excluded(true);

    // Disable "bb" phase for direct Jimple->Baf translation
    opts.setPhaseOption("bb", "enabled:false");

    final Scene scene = Scene.v();
    scene.loadNecessaryClasses();

    // Ensure that phantom references are not allowed
    Assert.assertFalse(scene.allowsPhantomRefs());
  }

  private static final String DUMMY_FIELD_NAME_STAT = "fStat";
  private static final String DUMMY_FIELD_NAME_INST = "fInst";

  private static class TestBodyGenerator {
    // Important: the fields in this class must NOT be static since G is reset for every test.

    final Scene scene;
    final Jimple jimp;
    final Type fieldType;
    final SootClass fieldClass;
    final JimpleBody body;
    final UnitPatchingChain units;
    final Local fieldLocal;
    final Local objLocal;

    public TestBodyGenerator() {
      this.scene = Scene.v();
      this.jimp = Jimple.v();
      this.fieldType = IntType.v();
      this.fieldClass = generateClassWithFields(this.scene, this.fieldType);
      this.body = generateEmptyMethodBody(this.jimp);
      this.units = this.body.getUnits();

      // Create a Local with the same type as the field references and another
      // with the type of the class containing the fields (to use as the base).
      final Chain<Local> locals = this.body.getLocals();
      this.fieldLocal = this.jimp.newLocal("a", this.fieldType);
      locals.add(this.fieldLocal);
      this.objLocal = this.jimp.newLocal("b", this.fieldClass.getType());
      locals.add(this.objLocal);
    }

    /**
     * Generate a new {@link SootClass} containing a static field and a instance field (both with type {@link #fieldType})
     * and add that class to the {@link Scene} as an application class.
     *
     * @param scene
     *
     * @return
     */
    private static SootClass generateClassWithFields(Scene scene, Type fieldType) {
      SootClass c = new SootClass("DummyClass");

      c.addField(new SootField(DUMMY_FIELD_NAME_STAT, fieldType, Modifier.STATIC));
      c.addField(new SootField(DUMMY_FIELD_NAME_INST, fieldType, 0));

      scene.addClass(c);
      c.setApplicationClass();

      return c;
    }

    /**
     * Create a {@link JimpleBody} and a {@link SootMethod} that is static, has no parameters, and has void return type to
     * simplify later construction of the {@link JimpleBody} (i.e. no @this or @param IdentityRef are needed). The
     * {@link SootMethod} does not have a declaring class set.
     *
     * @return
     */
    private static JimpleBody generateEmptyMethodBody(Jimple jimp) {
      final SootMethod m = new SootMethod("m", Collections.emptyList(), VoidType.v(), Modifier.STATIC);
      Assert.assertTrue(m.isStatic());
      Assert.assertFalse(m.isAbstract());

      JimpleBody body = jimp.newBody(m);
      m.setActiveBody(body);

      return body;
    }

    public SootFieldRef makeValidRefToFieldNameStat() {
      return scene.makeFieldRef(fieldClass, DUMMY_FIELD_NAME_STAT, fieldType, true);
    }

    public SootFieldRef makeValidRefToFieldNameInst() {
      return scene.makeFieldRef(fieldClass, DUMMY_FIELD_NAME_INST, fieldType, false);
    }

    // Invalid because the SootFieldRef is non-static although field is static
    public SootFieldRef makeInvalidRefToFieldNameStat() {
      return scene.makeFieldRef(fieldClass, DUMMY_FIELD_NAME_STAT, fieldType, false);
    }

    // Invalid because the SootFieldRef is static although field is non-static
    public SootFieldRef makeInvalidRefToFieldNameInst() {
      return scene.makeFieldRef(fieldClass, DUMMY_FIELD_NAME_INST, fieldType, true);
    }

    public void generateAssign(Value lhs, Value rhs) {
      units.add(jimp.newAssignStmt(lhs, rhs));
    }
  }

  /**
   * Run {@link FieldRefValidator} and return a list containing any {@link ValidationException} that it generated.
   *
   * @param body
   *
   * @return
   */
  private static List<ValidationException> runValidator(Body body) {
    List<ValidationException> exceptions = new ArrayList<>();
    FieldRefValidator.INSTANCE.validate(body, exceptions);
    return exceptions;
  }

  /**
   * Count the number of {@link Stmt} or {@link Inst} in the given {@link Body} that contain a field reference.
   * 
   * @param body
   * @return
   */
  private static int countFieldReferences(Body body) {
    int count = 0;
    for (Unit u : body.getUnits()) {
      if (u instanceof Stmt) {
        if (((Stmt) u).containsFieldRef()) {
          count++;
        }
      } else if (u instanceof Inst) {
        if (((Inst) u).containsFieldRef()) {
          count++;
        }
      }
    }
    return count;
  }

  private static void testValid(Function<TestBodyGenerator, Body> converter) {
    TestBodyGenerator g = new TestBodyGenerator();

    // Generate a valid RHS static field reference
    g.generateAssign(g.fieldLocal, g.jimp.newStaticFieldRef(g.makeValidRefToFieldNameStat()));

    // Generate a valid LHS static field reference
    g.generateAssign(g.jimp.newStaticFieldRef(g.makeValidRefToFieldNameStat()), g.fieldLocal);

    // Generate a valid RHS instance field reference
    g.generateAssign(g.fieldLocal, g.jimp.newInstanceFieldRef(g.objLocal, g.makeValidRefToFieldNameInst()));

    // Generate a valid LHS instance field reference
    g.generateAssign(g.jimp.newInstanceFieldRef(g.objLocal, g.makeValidRefToFieldNameInst()), g.fieldLocal);

    final Body b = converter.apply(g);

    // Ensure all of the expected field references appear in the Body
    Assert.assertEquals(4, countFieldReferences(b));

    // Run the validator and ensure there are no ValidationExceptions
    Assert.assertTrue(runValidator(b).isEmpty());
  }

  private static final Function<TestBodyGenerator, Body> JIMP = g -> g.body;
  private static final Function<TestBodyGenerator, Body> BAF = g -> new BafBody(g.body, Collections.emptyMap());

  @Test
  public void testJimpleValid() {
    testValid(JIMP);
  }

  @Test
  public void testBafValid() {
    testValid(BAF);
  }

  // TESTING: LHS InstanceFieldRef used on static field
  private static void testInvalid_LIS(Function<TestBodyGenerator, Body> converter) {
    TestBodyGenerator g = new TestBodyGenerator();
    g.generateAssign(g.jimp.newInstanceFieldRef(g.objLocal, g.makeInvalidRefToFieldNameStat()), g.fieldLocal);

    final Body b = converter.apply(g);

    // Ensure all of the expected field references appear in the Body
    Assert.assertEquals(1, countFieldReferences(b));

    // Run the validator and ensure there is at least one ValidationException
    Assert.assertFalse(runValidator(b).isEmpty());
  }

  @Test
  public void testJimpleInvalid_LIS() {
    testInvalid_LIS(JIMP);
  }

  @Test
  public void testBafInvalid_LIS() {
    testInvalid_LIS(BAF);
  }

  // TESTING: RHS InstanceFieldRef used on static field
  private static void testInvalid_RIS(Function<TestBodyGenerator, Body> converter) {
    TestBodyGenerator g = new TestBodyGenerator();
    g.generateAssign(g.fieldLocal, g.jimp.newInstanceFieldRef(g.objLocal, g.makeInvalidRefToFieldNameStat()));

    final Body b = converter.apply(g);

    // Ensure all of the expected field references appear in the Body
    Assert.assertEquals(1, countFieldReferences(b));

    // Run the validator and ensure there is at least one ValidationException
    Assert.assertFalse(runValidator(b).isEmpty());
  }

  @Test
  public void testJimpleInvalid_RIS() {
    testInvalid_RIS(JIMP);
  }

  @Test
  public void testBafInvalid_RIS() {
    testInvalid_RIS(BAF);
  }

  // TESTING: LHS StaticFieldRef used on instance field
  private static void testInvalid_LSI(Function<TestBodyGenerator, Body> converter) {
    TestBodyGenerator g = new TestBodyGenerator();
    g.generateAssign(g.jimp.newStaticFieldRef(g.makeInvalidRefToFieldNameInst()), g.fieldLocal);

    final Body b = converter.apply(g);

    // Ensure all of the expected field references appear in the Body
    Assert.assertEquals(1, countFieldReferences(b));

    // Run the validator and ensure there is at least one ValidationException
    Assert.assertFalse(runValidator(b).isEmpty());
  }

  @Test
  public void testJimpleInvalid_LSI() {
    testInvalid_LSI(JIMP);
  }

  @Test
  public void testBafInvalid_LSI() {
    testInvalid_LSI(BAF);
  }

  // TESTING: RHS StaticFieldRef used on instance field
  private static void testInvalid_RSI(Function<TestBodyGenerator, Body> converter) {
    TestBodyGenerator g = new TestBodyGenerator();
    g.generateAssign(g.fieldLocal, g.jimp.newStaticFieldRef(g.makeInvalidRefToFieldNameInst()));

    final Body b = converter.apply(g);

    // Ensure all of the expected field references appear in the Body
    Assert.assertEquals(1, countFieldReferences(b));

    // Run the validator and ensure there is at least one ValidationException
    Assert.assertFalse(runValidator(b).isEmpty());
  }

  @Test
  public void testJimpleInvalid_RSI() {
    testInvalid_RSI(JIMP);
  }

  @Test
  public void testBafInvalid_RSI() {
    testInvalid_RSI(BAF);
  }
}
