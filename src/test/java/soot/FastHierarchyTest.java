package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;


public class FastHierarchyTest {

  @Test
  public void testGetAllSubinterfaces() {
    G.reset();

    Scene s = Scene.v();
    s.loadNecessaryClasses();

    SootClass scA = generacteSceneClass("InterfaceA", Modifier.INTERFACE);
    SootClass scB = generacteSceneClass("InterfaceB", Modifier.INTERFACE);
    SootClass scC1 = generacteSceneClass("InterfaceC1", Modifier.INTERFACE);
    SootClass scC2 = generacteSceneClass("InterfaceC2", Modifier.INTERFACE);
    SootClass scD = generacteSceneClass("InterfaceD", Modifier.INTERFACE);
    SootClass scE1 = generacteSceneClass("InterfaceE1", Modifier.INTERFACE);
    SootClass scE2 = generacteSceneClass("InterfaceE2", Modifier.INTERFACE);
    SootClass sc6 = generacteSceneClass("Class1", 0);

    scA.addInterface(scB);
    scB.addInterface(scC1);
    scB.addInterface(scC2);
    scC1.addInterface(scD);
    scD.addInterface(scE1);
    scD.addInterface(scE2);

    FastHierarchy fh = s.getOrMakeFastHierarchy();

    // A sc6 is not an interface -> empty result
    assertEquals(Collections.emptySet(), fh.getAllSubinterfaces(sc6));

    assertThat(fh.getAllSubinterfaces(scA), containsInAnyOrder(scA));
    assertThat(fh.getAllSubinterfaces(scB), containsInAnyOrder(scA, scB));

    assertThat(fh.getAllSubinterfaces(scC1), containsInAnyOrder(scA, scB, scC1));
    assertThat(fh.getAllSubinterfaces(scC2), containsInAnyOrder(scA, scB, scC2));

    assertThat(fh.getAllSubinterfaces(scD), containsInAnyOrder(scA, scB, scC1, scD));

    assertThat(fh.getAllSubinterfaces(scE1), containsInAnyOrder(scA, scB, scC1, scD, scE1));
    assertThat(fh.getAllSubinterfaces(scE2), containsInAnyOrder(scA, scB, scC1, scD, scE2));
  }

  /**
   * Execute {@link FastHierarchy#getAllSubinterfaces(SootClass)} concurrently and check the result
   * 
   * This test uses a subclass of {@link FastHierarchy} that has a built-in delay to increase the time-span a potential
   * problem because of concurrent access can arise.
   * 
   * @throws Exception
   */
  @Test
  public void testGetAllSubinterfacesMultiThreaded() throws Exception {

    G.reset();

    Scene s = Scene.v();
    s.loadNecessaryClasses();

    SootClass scA = generacteSceneClass("InterfaceA", Modifier.INTERFACE);
    SootClass scB = generacteSceneClass("InterfaceB", Modifier.INTERFACE);
    SootClass scC = generacteSceneClass("InterfaceC", Modifier.INTERFACE);
    SootClass scD = generacteSceneClass("InterfaceD", Modifier.INTERFACE);
    SootClass scE = generacteSceneClass("InterfaceE", Modifier.INTERFACE);

    scA.addInterface(scB);
    scB.addInterface(scC);
    scC.addInterface(scD);
    scD.addInterface(scE);

    final FastHierarchy hierarchy = new FastHierarchyForUnittest();
    s.setFastHierarchy(hierarchy);

    ExecutorService executor = Executors.newFixedThreadPool(4);

    Callable<Set<SootClass>> c = new Callable<Set<SootClass>>() {

      @Override
      public Set<SootClass> call() throws Exception {
        return hierarchy.getAllSubinterfaces(scE);
      }
    };

    ArrayList<Future<Set<SootClass>>> results = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      results.add(executor.submit(c));
    }

    for (Future<Set<SootClass>> future : results) {
      Set<SootClass> res = future.get();
      assertThat(res, containsInAnyOrder(scA, scB, scC, scD, scE));
    }
    executor.shutdown();
  }

  @Test
  public void testGetAllImplementersOfInterface() {
    G.reset();

    Scene s = Scene.v();
    s.loadNecessaryClasses();

    SootClass interfaceA = generacteSceneClass("InterfaceA", Modifier.INTERFACE);
    SootClass interfaceB = generacteSceneClass("InterfaceB", Modifier.INTERFACE);
    SootClass interfaceC1 = generacteSceneClass("InterfaceC1", Modifier.INTERFACE);
    SootClass interfaceC2 = generacteSceneClass("InterfaceC2", Modifier.INTERFACE);
    SootClass interfaceD = generacteSceneClass("InterfaceD", Modifier.INTERFACE);

    SootClass scA = generacteSceneClass("ClassA", 0);
    SootClass scB = generacteSceneClass("ClassB", 0);
    SootClass scC1 = generacteSceneClass("ClassC1", 0);
    SootClass scC2 = generacteSceneClass("ClassC2", 0);
    SootClass scD = generacteSceneClass("ClassD", 0);
    SootClass scZ = generacteSceneClass("ClassZ", 0);

    interfaceA.addInterface(interfaceB);
    interfaceB.addInterface(interfaceC1);
    interfaceB.addInterface(interfaceC2);
    interfaceC1.addInterface(interfaceD);

    scA.addInterface(interfaceA);
    scB.addInterface(interfaceB);
    scC1.addInterface(interfaceC1);
    scC2.addInterface(interfaceC2);
    scD.addInterface(interfaceD);

    FastHierarchy fh = s.getOrMakeFastHierarchy();

    // A sc6 is not an interface -> empty result
    assertEquals(Collections.emptySet(), fh.getAllImplementersOfInterface(scZ));

    assertThat(fh.getAllImplementersOfInterface(interfaceA), containsInAnyOrder(scA));
    assertThat(fh.getAllImplementersOfInterface(interfaceB), containsInAnyOrder(scA, scB));

    assertThat(fh.getAllImplementersOfInterface(interfaceC1), containsInAnyOrder(scA, scB, scC1));
    assertThat(fh.getAllImplementersOfInterface(interfaceC2), containsInAnyOrder(scA, scB, scC2));

    assertThat(fh.getAllImplementersOfInterface(interfaceD), containsInAnyOrder(scA, scB, scC1, scD));
  }

  @Test
  public void testCanStoreAnySubTypeInSupertypeWithoutKnownConcreteSubclass() {
    G.reset();
    Scene s = Scene.v();
    s.loadNecessaryClasses();

    SootClass contract = generacteSceneClass("Contract", Modifier.INTERFACE);
    SootClass otherContract = generacteSceneClass("OtherContract", Modifier.INTERFACE);
    SootClass abstractBase = generacteSceneClass("AbstractBase", Modifier.ABSTRACT);
    abstractBase.addInterface(contract);
    SootClass subContract = generacteSceneClass("SubContract", Modifier.INTERFACE);
    subContract.addInterface(contract);

    FastHierarchy fh = s.getOrMakeFastHierarchy();
    Type anyAbstractBase = AnySubType.v(abstractBase.getType());
    Type anySubContract = AnySubType.v(subContract.getType());

    // any_subtype_of(x) is storable in x (already the case before) ...
    assertTrue(fh.canStoreType(anyAbstractBase, abstractBase.getType()));
    // ... and therefore in every supertype of x, even if no concrete subclass of x is known
    assertTrue(fh.canStoreType(anyAbstractBase, contract.getType()));
    assertTrue(fh.canStoreType(anyAbstractBase, s.getObjectType()));
    assertTrue(fh.canStoreType(anySubContract, contract.getType()));
    assertTrue(fh.canStoreType(anySubContract, s.getObjectType()));

    // A type unrelated to x still requires a known concrete subclass of x that implements it
    assertFalse(fh.canStoreType(anyAbstractBase, otherContract.getType()));
    assertFalse(fh.canStoreType(anySubContract, otherContract.getType()));
  }

  @Test
  public void testCanStoreAnySubTypeInUnrelatedTypeViaConcreteSubclass() {
    G.reset();
    Scene s = Scene.v();
    s.loadNecessaryClasses();

    SootClass contract = generacteSceneClass("Contract", Modifier.INTERFACE);
    SootClass otherContract = generacteSceneClass("OtherContract", Modifier.INTERFACE);
    SootClass abstractBase = generacteSceneClass("AbstractBase", Modifier.ABSTRACT);
    abstractBase.addInterface(contract);
    SootClass concrete = generacteSceneClass("Concrete", 0);
    concrete.setSuperclass(abstractBase);
    concrete.addInterface(otherContract);

    FastHierarchy fh = s.getOrMakeFastHierarchy();

    // OtherContract is unrelated to AbstractBase, but the concrete subclass is a witness
    assertTrue(fh.canStoreType(AnySubType.v(abstractBase.getType()), otherContract.getType()));
  }

  private static class FastHierarchyForUnittest extends FastHierarchy {

    @Override
    public Set<SootClass> getAllSubinterfaces(SootClass parent) {
      try {
        // We add a delay to increase the chance of a concurrent access
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
      return super.getAllSubinterfaces(parent);
    }
  }

  private static SootClass generacteSceneClass(String name, int modifier) {
    SootClass sootClass = new SootClass(name, modifier);
    Scene.v().addClass(sootClass);
    SootClass objectClass = Scene.v().getObjectType().getSootClass();
    sootClass.setSuperclass(objectClass);
    return sootClass;
  }
}
