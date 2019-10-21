package soot.testing.framework;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Assert;
import soot.Body;
import soot.G;
import soot.G.Global;
import soot.MethodOrMethodContext;
import soot.PhaseOptions;
import soot.RefType;
import soot.SootClass;
import soot.Scene;
import soot.SootMethod;
import soot.SootMethodRefImpl;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.VirtualCalls;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;

public class DefaultInterfaceTest extends AbstractTestingFramework {
	@Override
	protected void setupSoot() {
		super.setupSoot();
		PhaseOptions.v().setPhaseOption("cg.cha", "on");
	}


  @Test
  public void interfaceTest() {
	  
	  String testClass = "soot.interfaceTesting.TestSimpleDefault";
	  String defaultClass = "soot.interfaceTesting.Default";

	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.Default");

	  SootMethod defaultMethod = Scene.v().getMethod("<soot.interfaceTesting.Default: void target()>"); 
	  Body body = target.retrieveActiveBody();	  
	  SootMethod targetMethod = getSootMethodRef(body.getUnits(), "void target()");	  
	  SootMethod resolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultMethod.getNumberedSubSignature(), false);	  
	  SootMethod concreteImpl = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), defaultMethod);	  
	  Set<SootMethod> abstractImpl = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultClass), defaultMethod);
	  final CallGraph cg = Scene.v().getCallGraph(); 	  
	  boolean edgePresent = checkInEdges(cg, defaultMethod, target);	  	  
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods(); 	  
	  /* Arguments for assert function */	  
	  assertdefaultInterfaceTest(defaultMethod, reachableMethods, resolvedMethod, edgePresent, targetMethod, concreteImpl);
  }
  
  @Test
  public void interfaceWithSameSignatureTest() {
	  String testClass = "soot.interfaceTesting.TestInterfaceSameSignature";
	  String interfaceReadClass = "soot.interfaceTesting.Read";
	  String interfaceWriteClass = "soot.interfaceTesting.Write";

	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.Read", "soot.interfaceTesting.Write");

	  SootMethod mainPrintMethod = Scene.v().getMethod("<soot.interfaceTesting.TestInterfaceSameSignature: void print()>");
	  SootMethod readInterfacePrint = Scene.v().getMethod("<soot.interfaceTesting.Read: void print()>");
	  SootMethod writeInterfacePrint = Scene.v().getMethod("<soot.interfaceTesting.Write: void print()>");
	  SootMethod defaultRead = Scene.v().getMethod("<soot.interfaceTesting.Read: void read()>");
	  SootMethod defaultWrite = Scene.v().getMethod("<soot.interfaceTesting.Write: void write()>");
	  
	  Body mainBody = target.retrieveActiveBody();
	  Body mainPrintBody = mainPrintMethod.retrieveActiveBody();
	  
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");
	  SootMethod refWritePrintMethod = getSootMethodRef(mainPrintBody.getUnits(), "soot.interfaceTesting.Write: void print()");
	  SootMethod refReadPrintMethod = getSootMethodRef(mainPrintBody.getUnits(), "soot.interfaceTesting.Read: void print()");
	  SootMethod refDefaultRead = getSootMethodRef(mainBody.getUnits(), "void read()");
	  SootMethod refDefaultWrite = getSootMethodRef(mainBody.getUnits(), "void write()");
	  
	  SootMethod resolvedMainMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), mainPrintMethod, false);
	  SootMethod resolvedWritePrintMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), writeInterfacePrint, false);
	  SootMethod resolvedReadPrintMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), readInterfacePrint, false);
	  SootMethod resolvedDefaultReadMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultRead, false);
	  SootMethod resolvedDefaultWriteMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultWrite, false);
	  
	  SootMethod concreteImplMainPrint = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), mainPrintMethod);
	  SootMethod concreteImplReadPrint = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), refReadPrintMethod);
	  SootMethod concreteImplDefaultRead = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), refDefaultRead);
	  SootMethod concreteImplDefaultWrite = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), refDefaultWrite);
	  
	  Set<SootMethod> abstractImplDefaultRead = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(interfaceReadClass), refDefaultRead); 
	  Set<SootMethod> abstractImplDefaultWrite = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(interfaceWriteClass), refDefaultWrite); 
	  Set<SootMethod> abstractImplReadDefaultPrint = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(interfaceReadClass), refReadPrintMethod);
	  Set<SootMethod> abstractImplWriteDefaultPrint = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(interfaceWriteClass), refWritePrintMethod);  
	  
	  
	  final CallGraph cg = Scene.v().getCallGraph();
	  
	  /* Edges should be present */
	  boolean edgeMainPrintToReadPrint = checkInEdges(cg, readInterfacePrint, mainPrintMethod);
	  boolean edgeMainPrintToWritePrint = checkInEdges(cg, writeInterfacePrint, mainPrintMethod);
	  boolean edgeMainMethodToPrint = checkInEdges(cg, mainPrintMethod, target);
	  
	  /* Edges should not be present */
	  boolean edgeMainMethodToReadPrint = checkInEdges(cg, readInterfacePrint, target);
	  boolean edgeMainMethodToWritePrint = checkInEdges(cg, writeInterfacePrint, target);
	  
	  /* Edges should be present */
	  boolean edgeMainMethodToReadMethod = checkInEdges(cg, defaultRead, target);
	  boolean edgeMainMethodToWriteMethod = checkInEdges(cg, defaultWrite, target);

	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  /* Arguments for assert function */
	  Map<SootMethod, String> targetMethods = new HashMap<SootMethod, String>() {{
		  put(mainPrintMethod, "print");
		  put(readInterfacePrint, "print");
		  put(writeInterfacePrint, "print");
		  put(defaultRead, "read");
		  put(defaultWrite, "write");
	  }};
	  
	  Map<SootMethod, SootMethod> resolvedMethods = new HashMap<SootMethod, SootMethod>() {{
		  put(mainPrintMethod, resolvedMainMethod);
		  put(mainPrintMethod, resolvedWritePrintMethod);
		  put(mainPrintMethod, resolvedReadPrintMethod);
		  put(defaultRead, resolvedDefaultReadMethod);
		  put(defaultWrite, resolvedDefaultWriteMethod);		  
	  }};
	  
	  Map<SootMethod, SootMethod> methodRef = new HashMap<SootMethod, SootMethod>() {{
		  put(mainPrintMethod, refMainMethod);
		  put(writeInterfacePrint, refWritePrintMethod);
		  put(readInterfacePrint, refReadPrintMethod);
		  put(defaultRead, refDefaultRead);
		  put(defaultWrite, refDefaultWrite);		  
	  }};
	  
	  Map<SootMethod, SootMethod> concreteImpl = new HashMap<SootMethod, SootMethod>() {{
		  put(mainPrintMethod, concreteImplMainPrint);
		  put(writeInterfacePrint, concreteImplWritePrint);
		  put(readInterfacePrint, concreteImplReadPrint);
		  put(defaultRead, concreteImplDefaultRead);
		  put(defaultWrite, concreteImplDefaultWrite);		  
	  }};
	  
	  ArrayList<Boolean> edgePresent = new ArrayList<Boolean>() {{
		  add(edgeMainPrintToReadPrint);
		  add(edgeMainPrintToWritePrint);
		  add(edgeMainMethodToPrint);		  
	  }};
	  
	  ArrayList<Boolean> edgeNotPresent = new ArrayList<Boolean>() {{
		  add(edgeMainMethodToReadPrint);
		  add(edgeMainMethodToWritePrint);
		  add(edgeMainMethodToReadMethod);	
		  add(edgeMainMethodToWriteMethod);
	  }};
	  
	  assertInterfaceWithSameSignature(targetMethods, reachableMethods, resolvedMethods, edgePresent, edgeNotPresent, methodRef, concreteImpl);
  }
  
  @Test
  public void classInterfaceWithSameSignatureTest() {
	  String testClass = "soot.interfaceTesting.TestClassPreferenceOverInterface";
	  String defaultClass = "soot.interfaceTesting.HelloWorld";

	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.HelloWorld");

	  SootMethod mainPrintMethod = Scene.v().getMethod("<soot.interfaceTesting.TestClassPreferenceOverInterface: void print()>");
	  SootMethod defaultPrintMethod = Scene.v().getMethod("<soot.interfaceTesting.HelloWorld: void print()>");
	  final CallGraph cg = Scene.v().getCallGraph();	  
	  Body mainBody = target.retrieveActiveBody();	  
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");	  
	  SootMethod resolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultPrintMethod.getNumberedSubSignature(), false);	  
	  SootMethod concreteImpl = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), defaultPrintMethod);	  
	  Set<SootMethod> abstractImpl = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultClass), defaultPrintMethod);	  
	  boolean edgeMainMethodToMainPrint = checkInEdges(cg, mainPrintMethod, target);
	  boolean edgeMainPrintToDefaultPrint = checkInEdges(cg, defaultPrintMethod, target);
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods(); 
	  
	  Map<SootMethod, String> targetMethods = new HashMap<SootMethod, String>() {{
		  put(mainPrintMethod, "print");
		  put(defaultPrintMethod, "print");		  
	  }};
	  
	  ArrayList<Boolean> edgePresent = new ArrayList<Boolean>() {{
		  add(edgeMainMethodToMainPrint);
		  add(edgeMainPrintToDefaultPrint);		  		  
	  }};
	  
	  assertClassInterfaceWithSameSignature(mainPrintMethod, targetMethods, resolvedMethod, refMainMethod, reachableMethods, edgePresent, concreteImpl);	  
  }
  
  @Test
  public void superClassInterfaceWithSameSignatureTest() {
	  String testClass = "soot.interfaceTesting.TestSuperClassInterfaceSameSignature";
	  String defaultClass = "soot.interfaceTesting.PrintInterface";
	  String defaultSuperClass = "soot.interfaceTesting.DefaultPrint";

	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.PrintInterface", "soot.interfaceTesting.TestSuperClassImplementsInterface");

	  SootMethod defaultSuperMainMethod = Scene.v().getMethod("<soot.interfaceTesting.TestSuperClassImplementsInterface: void main()>");
	  SootMethod mainMethod = Scene.v().getMethod("<soot.interfaceTesting.TestSuperClassImplementsInterface: void print()>");
	  SootMethod defaultMethod = Scene.v().getMethod("<soot.interfaceTesting.PrintInterface: void print()>");
	  SootMethod defaultSuperClassMethod = Scene.v().getMethod("<soot.interfaceTesting.DefaultPrint: void print()>");
	  
	  Body mainBody = target.retrieveActiveBody();
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");
	  
	  SootMethod resolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultMethod, false);
	  SootMethod resolvedSuperClassDefaultMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), defaultSuperClassMethod, false);
	  
	  SootMethod concreteImpl = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), defaultMethod);
	  
	  Set<SootMethod> abstractImpl = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultClass), defaultMethod);
	  Set<SootMethod> abstractImplSuperClass = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultSuperClass), defaultSuperClassMethod);

	  final CallGraph cg = Scene.v().getCallGraph();
	  
	  boolean edgeMainToSuperClassPrint = checkInEdges(cg, mainMethod, target);
	  boolean edgeMainToDefaultPrint = checkInEdges(cg, defaultMethod, target);
	  boolean edgeMainToSuperDefaultPrint = checkInEdges(cg, defaultSuperClassMethod, target);
	  boolean edgeSuperMainToSuperPrint = checkInEdges(cg, defaultSuperClassMethod, defaultSuperMainMethod);

	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  List<SootMethod> targetMethods = new ArrayList<SootMethod>() {{
		  add(mainMethod);
		  add(defaultMethod);
		  add(defaultSuperClassMethod);
	  }};
	  
	  ArrayList<Boolean> edgeNotPresent = new ArrayList<Boolean>() {{
		  add(edgeMainToDefaultPrint);
		  add(edgeMainToSuperDefaultPrint);
		  add(edgeSuperMainToSuperPrint);
	  }};
	  
	  Map<SootMethod, SootMethod> resolvedMethods = new HashMap<SootMethod, SootMethod>() {{
		  put(mainMethod, resolvedMethod);
		  put(resolvedSuperClassDefaultMethod, resolvedMethod);		  		  
	  }};
	  
	  assertSuperClassInterfaceWithSameSignature(targetMethods, resolvedMethods, refMainMethod, reachableMethods, edgeMainToSuperClassPrint, edgeNotPresent, concreteImpl);  
  }  

  @Test
  public void derivedInterfacesTest() {
	  String testClass = "soot.interfaceTesting.TestDerivedInterfaces";
	  String defaultInterfaceOne = "soot.interfaceTesting.InterfaceTestOne";
	  String defaultInterfaceTwo = "soot.interfaceTesting.InterfaceTestTwo";
	  
	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.InterfaceTestOne", "soot.interfaceTesting.InterfaceTestTwo");	  
	  
	  SootMethod interfaceOnePrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceTestOne: void print()>");
	  SootMethod interfaceTwoPrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceTestTwo: void print()>");
	  
	  Body mainBody = target.retrieveActiveBody();
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");
	  
	  SootMethod interfaceOneResolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceOnePrint, false);
	  SootMethod interfaceTwoResolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceTwoPrint, false);
	  
	  SootMethod concreteImplInterfaceOne = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceOnePrint);
	  SootMethod concreteImplInterfaceTwo = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceTwoPrint);
	  
	  Set<SootMethod> abstractImplInterfaceOne = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultInterfaceOne), interfaceOnePrint);
	  Set<SootMethod> abstractImplInterfaceTwo = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultInterfaceTwo), interfaceTwoPrint);
	  
	  final CallGraph cg = Scene.v().getCallGraph();
	  
	  boolean edgeMainToInterfaceTwoPrint = checkInEdges(cg, interfaceTwoPrint, target);
	  boolean edgeMainToInterfaceOnePrint = checkInEdges(cg, interfaceOnePrint, target);
	  
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  List<SootMethod> targetMethods = new ArrayList<SootMethod>() {{
		  add(interfaceOnePrint);
		  add(interfaceTwoPrint);		  
	  }};
	  
	  Map<SootMethod, SootMethod> resolvedMethods = new HashMap<SootMethod, SootMethod>() {{
		  put(interfaceTwoPrint, interfaceOneResolvedMethod);
		  put(interfaceTwoPrint, interfaceTwoResolvedMethod);		  		  
	  }};
	  
	  Map<SootMethod, SootMethod> concreteImplTrue = new HashMap<SootMethod, SootMethod>() {{
		  put(interfaceTwoPrint, concreteImplInterfaceOne);
		  put(interfaceTwoPrint, concreteImplInterfaceTwo);		  		  
	  }};
	  
	  Map<SootMethod, SootMethod> concreteImplNotTrue = new HashMap<SootMethod, SootMethod>() {{
		  put(interfaceOnePrint, concreteImplInterfaceOne);
		  put(interfaceOnePrint, concreteImplInterfaceTwo);		  		  
	  }};
	  
	  assertDerivedInterface(targetMethods, resolvedMethods, refMainMethod, reachableMethods, edgeMainToInterfaceTwoPrint, edgeMainToInterfaceOnePrint, concreteImplTrue, concreteImplNotTrue);
  }
  
  @Test
  public void interfaceInheritanceTest() {
	  String testClass = "soot.interfaceTesting.TestInterfaceInheritance";
	  String defaultClass = "soot.interfaceTesting.InterfaceTestA";
	  
	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.InterfaceTestA", "soot.interfaceTesting.InterfaceTestB");	  
	  
	  SootMethod interfaceTestAPrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceTestA: void print()>");
	  SootMethod mainPrintMessageMethod = Scene.v().getMethod("<soot.interfaceTesting.TestInterfaceInheritance: void printMessage()>");	  
	  Body mainBody = target.retrieveActiveBody();
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");	  
	  SootMethod resolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceTestAPrint.getNumberedSubSignature(), false);	  
	  SootMethod concreteImpl = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceTestAPrint);	  
	  //Set<SootMethod> abstractImpl = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultClass), interfaceTestAPrint);	  
	  final CallGraph cg = Scene.v().getCallGraph();	  
	  boolean edgeMainToInterfaceTestAPrint = checkInEdges(cg, interfaceTestAPrint, target);
	  boolean edgeMainToMainPrintMessage = checkInEdges(cg, mainPrintMessageMethod, target);	  
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  List<SootMethod> targetMethods = new ArrayList<SootMethod>() {{
	  Assert.assertNotNull(interfaceTestAPrint);
	  Assert.assertNotNull(mainPrintMessageMethod);
	  }};
	  Assert.assertTrue(edgeMainToInterfaceTestAPrint);
	  Assert.assertFalse(edgeMainToMainPrintMessage);
	  
	  Assert.assertTrue(reachableMethods.contains(interfaceTestAPrint));
	  Assert.assertFalse(reachableMethods.contains(mainPrintMessageMethod));
	  Assert.assertEquals(interfaceTestAPrint, refMainMethod);
	  Assert.assertEquals(interfaceTestAPrint, resolvedMethod);
	  Assert.assertEquals(interfaceTestAPrint, concreteImpl);
  }
  
  @Test
  public void interfaceReAbstractionTest() {
	  String testClass = "soot.interfaceTesting.TestInterfaceReAbstracting";
	  String defaultClass = "soot.interfaceTesting.InterfaceA";
	  
	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.InterfaceA", "soot.interfaceTesting.InterfaceB");
	  
	  SootMethod interfaceAPrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceA: void print()>");
	  SootMethod mainMethodPrint = Scene.v().getMethod("<soot.interfaceTesting.TestInterfaceReAbstracting: void print()>");
	  
	  Body mainBody = target.retrieveActiveBody();
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");	  
	  SootMethod resolvedMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceAPrint.getNumberedSubSignature(), false);	  
	  SootMethod concreteImpl = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceAPrint);	  
	  Set<SootMethod> abstractImpl = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultClass), interfaceAPrint);	  
	  final CallGraph cg = Scene.v().getCallGraph();	  
	  boolean edgeMainMethodToMainPrint = checkInEdges(cg, mainMethodPrint, target);
	  boolean edgeMainMethodToInterfaceAPrint = checkInEdges(cg, interfaceAPrint, target);	  
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  List<SootMethod> targetMethods = new ArrayList<SootMethod>() {{
		  add(mainMethodPrint);
		  add(interfaceAPrint);		  
	  }};
	  
	  assertInterfaceReAbstraction(targetMethods, edgeMainMethodToMainPrint, edgeMainMethodToInterfaceAPrint, reachableMethods, resolvedMethod, refMainMethod, concreteImpl); 
  }
  
  @Test
  public void superClassPreferenceOverDefaultMethodTest() {
	  String testClass = "soot.interfaceTesting.TestSuperClassPreferenceOverInterface";
	  String defaultInterfaceOne = "soot.interfaceTesting.InterfaceOne";
	  String defaultInterfaceTwo = "soot.interfaceTesting.InterfaceTwo";
	  
	  final SootMethod target =
			  prepareTarget(
					  methodSigFromComponents(testClass, "void", "main"),
					  testClass,
					  "soot.interfaceTesting.InterfaceOne", "soot.interfaceTesting.InterfaceTwo", "soot.interfaceTesting.TestSuperClass");
	  
	  SootMethod interfaceOnePrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceOne: void print()>");
	  SootMethod interfaceTwoPrint = Scene.v().getMethod("<soot.interfaceTesting.InterfaceTwo: void print()>");
	  SootMethod superClassPrint = Scene.v().getMethod("<soot.interfaceTesting.TestSuperClass: void print()>");
	  
	  Body mainBody = target.retrieveActiveBody();
	  SootMethod refMainMethod = getSootMethodRef(mainBody.getUnits(), "void print()");
	  
	  SootMethod resolvedInterfaceOneDefaultMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceOnePrint, false);
	  SootMethod resolvedInterfaceTwoDefaultMethod = G.v().soot_jimple_toolkits_callgraph_VirtualCalls().resolveNonSpecial(Scene.v().getRefType(testClass), interfaceTwoPrint, false);
	  
	  SootMethod concreteImplInterfaceOne = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceOnePrint);
	  SootMethod concreteImplInterfaceTwo = Scene.v().getFastHierarchy().resolveConcreteDispatch(Scene.v().getSootClass(testClass), interfaceTwoPrint);
	  
	  Set<SootMethod> abstractImplInterfaceOne = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultInterfaceOne), interfaceOnePrint);
	  Set<SootMethod> abstractImplInterfaceTwo = Scene.v().getFastHierarchy().resolveAbstractDispatch(Scene.v().getSootClass(defaultInterfaceTwo), interfaceTwoPrint);
	  
	  final CallGraph cg = Scene.v().getCallGraph();
	  
	  boolean edgeMainToInterfaceOnePrint = checkInEdges(cg, interfaceOnePrint, target);
	  boolean edgeMainToInterfaceTwoPrint = checkInEdges(cg, interfaceTwoPrint, target);
	  boolean edgeMainToSuperClassPrint = checkInEdges(cg, superClassPrint, target);
	  
	  final ReachableMethods reachableMethods = Scene.v().getReachableMethods();
	  
	  List<SootMethod> targetMethods = new ArrayList<SootMethod>() {{
		  add(superClassPrint);
		  add(interfaceOnePrint);	
		  add(interfaceTwoPrint);	
	  }};
	  
	  ArrayList<Boolean> edgeNotPresent = new ArrayList<Boolean>() {{
		  add(edgeMainToInterfaceOnePrint);
		  add(edgeMainToInterfaceTwoPrint);		  
	  }};
	  
	  Map<SootMethod, SootMethod> resolvedMethods = new HashMap<SootMethod, SootMethod>() {{
		  put(superClassPrint, resolvedInterfaceOneDefaultMethod);
		  put(superClassPrint, resolvedInterfaceTwoDefaultMethod);		  		  
	  }};
	  
	  Map<SootMethod, SootMethod> concreteImplTrue = new HashMap<SootMethod, SootMethod>() {{
		  put(superClassPrint, concreteImplInterfaceOne);
		  put(superClassPrint, concreteImplInterfaceTwo);		  		  
	  }};
	  
	  Map<SootMethod, SootMethod> concreteImplNotTrue = new HashMap<SootMethod, SootMethod>() {{
		  put(interfaceOnePrint, concreteImplInterfaceOne);
		  put(interfaceOnePrint, concreteImplInterfaceTwo);		  		  
	  }};
	  
	  assertSuperClassPreferenceOverDefaultMethod(targetMethods, refMainMethod, edgeMainToSuperClassPrint, edgeNotPresent, reachableMethods, resolvedMethods, concreteImplTrue, concreteImplNotTrue); 
  }
  
  private boolean checkInEdges(CallGraph callGraph, SootMethod defaultMethod, SootMethod targetMethod) {

	  boolean isPresent = false;
	  Iterator<Edge> inEdges = callGraph.edgesInto(defaultMethod);
	  while(inEdges.hasNext()) {
		  MethodOrMethodContext sourceMethod = inEdges.next().getSrc();
		  if(sourceMethod.equals(targetMethod)) {
			  isPresent = true;
		  }
	  }
	  return isPresent;
  }
  
  private SootMethod getSootMethodRef(UnitPatchingChain units, String targetMethod) {
	  
	  SootMethod method = null;
	  for(Unit unit: units) {
		  if(unit instanceof JInvokeStmt && unit.toString().contains(targetMethod) ) {
			  method = ((JInvokeStmt) unit).getInvokeExpr().getMethodRef().tryResolve();			 
		  }
	  }
	  return method;
  }
  
  private void assertdefaultInterfaceTest(SootMethod defaultTargetMethod, ReachableMethods reachableMethods, SootMethod virtualResolvedMethod, boolean edgePresent, SootMethod methodRef, SootMethod concreteImpl) {
	  
	  Assert.assertEquals(defaultTargetMethod, virtualResolvedMethod);
	  Assert.assertEquals(defaultTargetMethod, methodRef);
	  Assert.assertEquals(defaultTargetMethod.getName(), "target");
	  Assert.assertNotNull(defaultTargetMethod);
	  Assert.assertTrue(reachableMethods.contains(defaultTargetMethod));
	  Assert.assertTrue(edgePresent);
	  Assert.assertEquals(defaultTargetMethod, concreteImpl);
	  
  }
  
  private void assertInterfaceWithSameSignature(Map<SootMethod, String> targetMethods, ReachableMethods reachableMethods, Map<SootMethod, SootMethod> virtualResolvedMethods, List<Boolean> edgePresent, List<Boolean> edgeNotPresent, Map<SootMethod, SootMethod> methodRefs, Map<SootMethod, SootMethod> concreteImpls) {
	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertNotNull(targetMethod.getKey());
	  }	  
	  for(Map.Entry<SootMethod, SootMethod> virtualResolvedMethod:virtualResolvedMethods.entrySet()) {
		  assertEquals(virtualResolvedMethod.getKey(), virtualResolvedMethod.getValue());
	  }	  
	  for(Map.Entry<SootMethod, SootMethod> methodRef:methodRefs.entrySet()) {
		  assertEquals(methodRef.getKey(), methodRef.getValue());
	  }	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertEquals(targetMethod.getKey().getName(), targetMethod.getValue());
	  }	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertTrue(reachableMethods.contains(targetMethod.getKey()));
	  }	  
	  for(boolean isPresent:edgePresent) {
		  assertTrue(isPresent);
	  }	  
	  for(boolean notPresent:edgeNotPresent) {
		  assertFalse(notPresent);
	  }	  
	  for(Map.Entry<SootMethod, SootMethod> concreteImpl:concreteImpls.entrySet()) {
		  assertEquals(concreteImpl.getKey(), concreteImpl.getValue());
	  }
  }
  
  private void assertClassInterfaceWithSameSignature(SootMethod defaultMethod, Map<SootMethod, String> targetMethods, SootMethod virtualResolvedMethod, SootMethod methodRef, ReachableMethods reachableMethods, List<Boolean> edgePresent, SootMethod concreteImpl) {
	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertNotNull(targetMethod.getKey());
	  }	  
	  assertEquals(defaultMethod, virtualResolvedMethod);
	  assertEquals(defaultMethod, methodRef);	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertEquals(targetMethod.getKey().getName(), targetMethod.getValue());
	  }	  
	  for(Map.Entry<SootMethod, String> targetMethod:targetMethods.entrySet()) {
		  assertTrue(reachableMethods.contains(targetMethod.getKey()));
	  }	  
	  for(boolean isPresent:edgePresent) {
		  assertTrue(isPresent);
	  }	  
	  assertEquals(defaultMethod, concreteImpl);
  }
  
  private void assertSuperClassInterfaceWithSameSignature(List<SootMethod> targetMethods, Map<SootMethod, SootMethod> virtualResolvedMethods, SootMethod methodRef, ReachableMethods reachableMethods, boolean edgePresent, List<Boolean> edgeNotPresent, SootMethod concreteImpl) {
	  for(SootMethod targetMethod: targetMethods) {
		  assertNotNull(targetMethod);
	  }	  
	  assertEquals(targetMethods.get(0), methodRef);
	  assertEquals(targetMethods.get(0).getName(), "print");
	  assertTrue(edgePresent);	  
	  for(boolean notPresent:edgeNotPresent) {
		  assertFalse(notPresent);
	  }
	  assertEquals(targetMethods.get(0), concreteImpl);	  
	  assertNotEquals(targetMethods.get(1), concreteImpl);  
	  
  }
  
  private void assertDerivedInterface(List<SootMethod> targetMethods, Map<SootMethod, SootMethod> virtualResolvedMethods, SootMethod methodRef, ReachableMethods reachableMethods, boolean edgePresent, boolean edgeNotPresent, Map<SootMethod, SootMethod> concreteImplsTrue, Map<SootMethod, SootMethod> concreteImplsNotTrue) {
	  
	  for(SootMethod targetMethod: targetMethods) {
		  Assert.assertNotNull(targetMethod);
	  }	  
	  assertEquals(targetMethods.get(0), methodRef);
	  assertEquals(targetMethods.get(1).getName(), "print"); 	  
	  assertFalse(edgeNotPresent);
	  assertTrue(edgePresent);
	  assertTrue(reachableMethods.contains(targetMethods.get(1)));
	  assertFalse(reachableMethods.contains(targetMethods.get(0)));
	  for(Map.Entry<SootMethod, SootMethod> virtualResolvedMethod:virtualResolvedMethods.entrySet()) {
		  assertEquals(virtualResolvedMethod.getKey(), virtualResolvedMethod.getValue());
	  }
	  for(Map.Entry<SootMethod, SootMethod> concreteImpl:concreteImplsTrue.entrySet()) {
		  assertEquals(concreteImpl.getKey(), concreteImpl.getValue());
	  }
	  for(Map.Entry<SootMethod, SootMethod> concreteImpl:concreteImplsNotTrue.entrySet()) {
		  assertNotEquals(concreteImpl.getKey(), concreteImpl.getValue());
	  } 
  }
  
  private void assertInterfaceInheritanceTest(List<SootMethod> targetMethods, boolean edgePresent, boolean edgeNotPresent, ReachableMethods reachableMethods, SootMethod methodRef, SootMethod resolvedMethod, SootMethod concreteImpl) {
	  
	  for(SootMethod targetMethod: targetMethods) {
		  Assert.assertNotNull(targetMethod);
	  }	  
	  assertEquals(targetMethods.get(0), methodRef);
	  assertEquals(targetMethods.get(0).getName(), "print");	  
	  assertTrue(edgePresent);
	  assertFalse(edgeNotPresent);	  
	  assertTrue(reachableMethods.contains(targetMethods.get(0)));
	  assertFalse(reachableMethods.contains(targetMethods.get(1)));
	  assertEquals(targetMethods.get(0), resolvedMethod);	  
	  assertEquals(targetMethods.get(0), concreteImpl);
	  
  }
  
  private void assertInterfaceReAbstraction(List<SootMethod> targetMethods, boolean edgePresent, boolean edgeNotPresent, ReachableMethods reachableMethods, SootMethod resolvedMethod, SootMethod methodRef, SootMethod concreteImpl) {
	  
	  for(SootMethod targetMethod: targetMethods) {
		  Assert.assertNotNull(targetMethod);
	  }
	  assertEquals(targetMethods.get(0), methodRef);
	  assertEquals(targetMethods.get(0).getName(), "print"); 
	  assertTrue(edgePresent);
	  assertFalse(edgeNotPresent);	  
	  assertTrue(reachableMethods.contains(targetMethods.get(0)));
	  assertFalse(reachableMethods.contains(targetMethods.get(1)));
	  assertEquals(targetMethods.get(0), resolvedMethod);
	  assertEquals(targetMethods.get(0), concreteImpl);
	  assertNotEquals(targetMethods.get(1), concreteImpl);
	  
  }
  
  private void assertSuperClassPreferenceOverDefaultMethod(List<SootMethod> targetMethods, SootMethod methodRef, boolean edgePresent, List<Boolean> edgeNotPresent, ReachableMethods reachableMethods, Map<SootMethod, SootMethod> virtualResolvedMethods, Map<SootMethod, SootMethod> concreteImplsTrue, Map<SootMethod, SootMethod> concreteImplsNotTrue) {
	  
	  for(SootMethod targetMethod: targetMethods) {
		  assertNotNull(targetMethod);
	  }
	  assertEquals(targetMethods.get(0), methodRef);
	  assertEquals(targetMethods.get(0).getName(), "print");	  
	  assertTrue(edgePresent);
	  for(boolean notPresent:edgeNotPresent) {
		  assertFalse(notPresent);
	  }	  
	  assertTrue(reachableMethods.contains(targetMethods.get(0)));
	  assertFalse(reachableMethods.contains(targetMethods.get(1)));
	  assertFalse(reachableMethods.contains(targetMethods.get(2)));	  
	  for(Map.Entry<SootMethod, SootMethod> virtualResolvedMethod:virtualResolvedMethods.entrySet()) {
		  assertEquals(virtualResolvedMethod.getKey(), virtualResolvedMethod.getValue());
	  }	  
	  for(Map.Entry<SootMethod, SootMethod> concreteImpl:concreteImplsTrue.entrySet()) {
		  assertEquals(concreteImpl.getKey(), concreteImpl.getValue());
	  }
	  for(Map.Entry<SootMethod, SootMethod> concreteImpl:concreteImplsNotTrue.entrySet()) {
		  assertNotEquals(concreteImpl.getKey(), concreteImpl.getValue());
	  }
  }
}
  
