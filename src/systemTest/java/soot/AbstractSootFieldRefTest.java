package soot;

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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import soot.jimple.Stmt;
import soot.testing.framework.AbstractTestingFramework;

/**
 * @author Timothy Hoffman
 */
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class AbstractSootFieldRefTest extends AbstractTestingFramework {

  private static final String TEST_TARGET_CLASS = "soot.AbstractSootFieldRefTestInput";

  @Override
  protected void setupSoot() {
  }

  @Test
  public void testCachingInvalidation_Name() throws Exception {
    SootMethod m1 = prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "m1"), TEST_TARGET_CLASS);
    final SootClass clas = m1.getDeclaringClass();

    // There is only 1 field in the class originally.
    Assert.assertEquals(Arrays.asList("f"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));

    // Ensure the previous value of AbstractSootFieldRef#resolveCache
    // is not used if the referenced field itself is modified.
    final Body b = m1.retrieveActiveBody();
    final SootFieldRef fRef = getFieldRef(b);
    Assert.assertEquals("f", fRef.name());

    // Get the original referenced field appearing in the test source (i.e. "f")
    final SootField origF = fRef.resolve();
    Assert.assertTrue(!origF.isPhantom());
    Assert.assertEquals("f", origF.getName());

    // Change the name of the method so the method reference no
    // longer refers to that method.
    origF.setName("newFieldName");
    Assert.assertEquals("newFieldName", origF.getName());

    // Changing the field itself does not change the reference
    Assert.assertEquals("f", fRef.name());

    // There is still just 1 field in the class (but "f" was renamed).
    Assert.assertEquals(Arrays.asList("newFieldName"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));

    // When resolving the reference, the cached value is not used since the
    // original field was renamed. It now gives a different field (that was
    // created automatically since a field with the name "f" no longer exists).
    final SootField newF = fRef.resolve();
    Assert.assertNotSame(origF, newF);
    Assert.assertEquals("f", newF.getName());

    // There are now 2 fields since resolving "f" created it again.
    Assert.assertEquals(Arrays.asList("f", "newFieldName"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));
  }

  @Test
  public void testCachingInvalidation_Type() throws Exception {
    SootMethod m1 = prepareTarget(methodSigFromComponents(TEST_TARGET_CLASS, "void", "m1"), TEST_TARGET_CLASS);
    final SootClass clas = m1.getDeclaringClass();

    // There is only 1 field in the class originally with boolean type.
    Assert.assertEquals(Arrays.asList("f"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));
    Assert.assertEquals(Arrays.<Type>asList(BooleanType.v()),
        clas.getFields().stream().map(SootField::getType).sorted().collect(Collectors.toList()));

    // Ensure the previous value of AbstractSootFieldRef#resolveCache
    // is not used if the referenced field itself is modified.
    final Body b = m1.retrieveActiveBody();
    final SootFieldRef fRef = getFieldRef(b);
    Assert.assertEquals("f", fRef.name());
    Assert.assertEquals(BooleanType.v(), fRef.type());

    // Get the original referenced field appearing in the test source (i.e. "f")
    final SootField origF = fRef.resolve();
    Assert.assertTrue(!origF.isPhantom());
    Assert.assertEquals(BooleanType.v(), origF.getType());

    // Change the type of the method so the method reference no
    // longer refers to that method.
    origF.setType(IntType.v());
    Assert.assertEquals(IntType.v(), origF.getType());

    // Changing the field itself does not change the reference
    Assert.assertEquals(BooleanType.v(), fRef.type());

    // There is still just 1 field in the class (but type of "f" was changed).
    Assert.assertEquals(Arrays.asList("f"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));
    Assert.assertEquals(Arrays.<Type>asList(IntType.v()),
        clas.getFields().stream().map(SootField::getType).sorted().collect(Collectors.toList()));

    // When resolving the reference, the cached value is not used since
    // the original field's type was changed. It now gives 'null'.
    final SootField newF = fRef.resolve();
    Assert.assertNotSame(origF, newF);
    Assert.assertNull(newF);

    // There is still just 1 field in the class (but type of "f" was changed).
    Assert.assertEquals(Arrays.asList("f"),
        clas.getFields().stream().map(SootField::getName).sorted().collect(Collectors.toList()));
    Assert.assertEquals(Arrays.<Type>asList(IntType.v()),
        clas.getFields().stream().map(SootField::getType).sorted().collect(Collectors.toList()));
  }

  private static SootFieldRef getFieldRef(Body b) {
    SootFieldRef retVal = null;
    for (Unit u : b.getUnits()) {
      Assert.assertTrue(u instanceof Stmt);
      Stmt s = (Stmt) u;
      if (s.containsFieldRef()) {
        Assert.assertNull(retVal);// the body has exactly 1 FieldRef
        retVal = s.getFieldRef().getFieldRef();
      }
    }
    Assert.assertNotNull(retVal);// the body has exactly 1 FieldRef
    return retVal;
  }
}
