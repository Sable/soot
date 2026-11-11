/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

package soot.dexpler;

import static soot.dexpler.instructions.InstructionFactory.fromInstruction;

import com.android.tools.smali.dexlib2.analysis.ClassPath;
import com.android.tools.smali.dexlib2.analysis.ClassPathResolver;
import com.android.tools.smali.dexlib2.analysis.ClassProvider;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.ExceptionHandler;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.MethodParameter;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer.DexEntry;
import com.android.tools.smali.dexlib2.iface.TryBlock;
import com.android.tools.smali.dexlib2.iface.debug.DebugItem;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.immutable.debug.ImmutableEndLocal;
import com.android.tools.smali.dexlib2.immutable.debug.ImmutableLineNumber;
import com.android.tools.smali.dexlib2.immutable.debug.ImmutableRestartLocal;
import com.android.tools.smali.dexlib2.immutable.debug.ImmutableStartLocal;
import com.android.tools.smali.dexlib2.util.MethodUtil;
import com.google.common.collect.ArrayListMultimap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Body;
import soot.BooleanConstant;
import soot.BooleanType;
import soot.ByteType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.NullType;
import soot.PackManager;
import soot.PhaseOptions;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.UnknownType;
import soot.Value;
import soot.ValueBox;
import soot.dexpler.instructions.DanglingInstruction;
import soot.dexpler.instructions.DeferableInstruction;
import soot.dexpler.instructions.DexlibAbstractInstruction;
import soot.dexpler.instructions.MoveExceptionInstruction;
import soot.dexpler.instructions.OdexInstruction;
import soot.dexpler.instructions.PseudoInstruction;
import soot.dexpler.instructions.RetypeableInstruction;
import soot.dexpler.tags.ByteOpTag;
import soot.dexpler.tags.DexplerTag;
import soot.dexpler.tags.DoubleOpTag;
import soot.dexpler.tags.FloatOpTag;
import soot.dexpler.tags.IntOpTag;
import soot.dexpler.tags.IntOrFloatOpTag;
import soot.dexpler.tags.LongOpTag;
import soot.dexpler.tags.LongOrDoubleOpTag;
import soot.dexpler.tags.ShortOpTag;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.MulExpr;
import soot.jimple.NegExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.Stmt;
import soot.jimple.SubExpr;
import soot.jimple.UshrExpr;
import soot.jimple.XorExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.base.Aggregator;
import soot.jimple.toolkits.base.ArrayWriteAggregator;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantCastEliminator;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.FieldStaticnessCorrector;
import soot.jimple.toolkits.scalar.IdentityCastEliminator;
import soot.jimple.toolkits.scalar.IdentityOperationEliminator;
import soot.jimple.toolkits.scalar.MethodStaticnessCorrector;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.fast.BottomType;
import soot.jimple.toolkits.typing.fast.DefaultTypingStrategy;
import soot.jimple.toolkits.typing.fast.IHierarchy;
import soot.jimple.toolkits.typing.fast.ITyping;
import soot.jimple.toolkits.typing.fast.ITypingStrategy;
import soot.jimple.toolkits.typing.fast.Integer1Type;
import soot.jimple.toolkits.typing.fast.NeedCastResult;
import soot.jimple.toolkits.typing.fast.TypePromotionUseVisitor;
import soot.jimple.toolkits.typing.fast.WeakObjectType;
import soot.options.JBOptions;
import soot.options.JBTROptions;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.TrapTightener;
import soot.toolkits.scalar.DifferentArrayTypeSplitter;
import soot.toolkits.scalar.LocalPacker;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.SharedInitializationLocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

/**
 * A DexBody contains the code of a DexMethod and is used as a wrapper around JimpleBody in the jimplification process.
 *
 * @author Michael Markert
 * @author Frank Hartmann
 */
public class DexBody {
  private static final Logger logger = LoggerFactory.getLogger(DexBody.class);
  protected List<DexlibAbstractInstruction> instructions;
  // keeps track about the jimple locals that are associated with the dex
  // registers
  protected JimpleLocal[] registerLocals;
  protected Local storeResultLocal;
  protected TreeMap<Integer, DexlibAbstractInstruction> instructionAtAddress;

  protected List<DeferableInstruction> deferredInstructions;
  protected Set<RetypeableInstruction> instructionsToRetype;
  protected DanglingInstruction dangling;

  protected int numRegisters;
  protected int numParameterRegisters;
  protected final List<Type> parameterTypes;
  protected final List<String> parameterNames;
  protected boolean isStatic;

  protected JimpleBody jBody;
  protected List<? extends TryBlock<? extends ExceptionHandler>> tries;

  protected RefType declaringClassType;

  protected final DexEntry<? extends DexFile> dexEntry;
  protected final Method method;

  /**
   * An entry of debug information for a register from the dex file.
   *
   * @author Zhenghao Hu
   */
  protected class RegDbgEntry {
    public int startAddress;
    public int endAddress;
    public int register;
    public String name;
    public Type type;
    public String signature;

    public RegDbgEntry(int sa, int ea, int reg, String nam, String ty, String sig) {
      this.startAddress = sa;
      this.endAddress = ea;
      this.register = reg;
      this.name = nam;
      this.type = DexType.toSoot(ty);
      this.signature = sig;
    }
  }

  private final ArrayListMultimap<Integer, RegDbgEntry> localDebugs;

  // detect array/instructions overlapping obfuscation
  protected List<PseudoInstruction> pseudoInstructionData = new ArrayList<PseudoInstruction>();

  PseudoInstruction isAddressInData(int a) {
    for (PseudoInstruction pi : pseudoInstructionData) {
      int fb = pi.getDataFirstByte();
      int lb = pi.getDataLastByte();
      if (fb <= a && a <= lb) {
        return pi;
      }
    }
    return null;
  }

  // the set of names used by Jimple locals
  protected Set<String> takenLocalNames;
  private JBOptions jbOptions = new JBOptions(PhaseOptions.v().getPhaseOptions("jb"));

  /**
   * Allocate a fresh name for Jimple local
   *
   * @param hint
   *          A name that the fresh name will look like
   * @author Zhixuan Yang (yangzhixuan@sbrella.com)
   */
  protected String freshLocalName(String hint) {
    if (hint == null || hint.equals("")) {
      hint = "$local";
    }
    String fresh;
    if (!takenLocalNames.contains(hint)) {
      fresh = hint;
    } else {
      for (int i = 1;; i++) {
        fresh = hint + Integer.toString(i);
        if (!takenLocalNames.contains(fresh)) {
          break;
        }
      }
    }
    takenLocalNames.add(fresh);
    return fresh;
  }

  /**
   * @param code
   *          the codeitem that is contained in this body
   * @param method
   *          the method that is associated with this body
   */
  protected DexBody(DexEntry<? extends DexFile> dexFile, Method method, RefType declaringClassType) {
    MethodImplementation code = method.getImplementation();
    if (code == null) {
      throw new RuntimeException("error: no code for method " + method.getName());
    }
    this.declaringClassType = declaringClassType;
    tries = code.getTryBlocks();

    List<? extends MethodParameter> parameters = method.getParameters();
    if (parameters != null) {
      parameterNames = new ArrayList<String>();
      parameterTypes = new ArrayList<Type>();
      for (MethodParameter param : method.getParameters()) {
        parameterNames.add(param.getName());
        parameterTypes.add(DexType.toSoot(param.getType()));
      }
    } else {
      parameterNames = Collections.emptyList();
      parameterTypes = Collections.emptyList();
    }

    isStatic = Modifier.isStatic(method.getAccessFlags());
    numRegisters = code.getRegisterCount();
    numParameterRegisters = MethodUtil.getParameterRegisterCount(method);
    if (!isStatic) {
      numParameterRegisters--;
    }

    instructions = new ArrayList<DexlibAbstractInstruction>();

    // Use descending order
    instructionAtAddress = new TreeMap<Integer, DexlibAbstractInstruction>();
    localDebugs = ArrayListMultimap.create();
    takenLocalNames = new HashSet<String>();

    registerLocals = new JimpleLocal[numRegisters];

    extractDexInstructions(code);

    // Check taken from Android's dalvik/libdex/DexSwapVerify.cpp
    if (numParameterRegisters > numRegisters) {
      throw new RuntimeException(
          "Malformed dex file: insSize (" + numParameterRegisters + ") > registersSize (" + numRegisters + ")");
    }

    for (DebugItem di : code.getDebugItems()) {
      if (di instanceof ImmutableLineNumber) {
        ImmutableLineNumber ln = (ImmutableLineNumber) di;
        DexlibAbstractInstruction ins = instructionAtAddress(ln.getCodeAddress());
        if (ins == null) {
          // Debug.printDbg("Line number tag pointing to invalid
          // offset: " + ln.getCodeAddress());
          continue;
        }
        ins.setLineNumber(ln.getLineNumber());
      } else if (di instanceof ImmutableStartLocal || di instanceof ImmutableRestartLocal) {
        int reg, codeAddr;
        String type, signature, name;
        if (di instanceof ImmutableStartLocal) {
          ImmutableStartLocal sl = (ImmutableStartLocal) di;
          reg = sl.getRegister();
          codeAddr = sl.getCodeAddress();
          name = sl.getName();
          type = sl.getType();
          signature = sl.getSignature();
        } else {
          ImmutableRestartLocal sl = (ImmutableRestartLocal) di;
          // ImmutableRestartLocal and ImmutableStartLocal share the same members but
          // don't share a base. So we have to write some duplicated code.
          reg = sl.getRegister();
          codeAddr = sl.getCodeAddress();
          name = sl.getName();
          type = sl.getType();
          signature = sl.getSignature();
        }
        if (name != null && type != null) {
          if (jbOptions.use_original_names()) {
            localDebugs.put(reg, new RegDbgEntry(codeAddr, -1 /* endAddress */, reg, name, type, signature));
          }
        }
      } else if (di instanceof ImmutableEndLocal) {
        ImmutableEndLocal el = (ImmutableEndLocal) di;
        List<RegDbgEntry> lds = localDebugs.get(el.getRegister());
        if (lds == null || lds.isEmpty()) {
          // Invalid debug info
          continue;
        } else {
          lds.get(lds.size() - 1).endAddress = el.getCodeAddress();
        }
      }
    }

    this.dexEntry = dexFile;
    this.method = method;
  }

  /**
   * Extracts the list of dalvik instructions from dexlib and converts them into our own instruction data model
   *
   * @param code
   *          The dexlib method implementation
   */
  protected void extractDexInstructions(MethodImplementation code) {
    int address = 0;
    for (Instruction instruction : code.getInstructions()) {
      DexlibAbstractInstruction dexInstruction = fromInstruction(instruction, address);
      instructions.add(dexInstruction);
      instructionAtAddress.put(address, dexInstruction);
      address += instruction.getCodeUnits();
    }
  }

  /** Return the types that are used in this body. */
  public Set<Type> usedTypes() {
    Set<Type> types = new HashSet<Type>();
    for (DexlibAbstractInstruction i : instructions) {
      types.addAll(i.introducedTypes());
    }

    if (tries != null) {
      for (TryBlock<? extends ExceptionHandler> tryItem : tries) {
        List<? extends ExceptionHandler> hList = tryItem.getExceptionHandlers();
        for (ExceptionHandler handler : hList) {
          String exType = handler.getExceptionType();
          if (exType == null) {
            // Exceptions
            continue;
          }
          types.add(DexType.toSoot(exType));
        }
      }
    }

    return types;
  }

  /**
   * Add unit to this body.
   *
   * @param u
   *          Unit to add.
   */
  public void add(Unit u) {
    getBody().getUnits().add(u);
  }

  /**
   * Add a deferred instruction to this body.
   *
   * @param i
   *          the deferred instruction.
   */
  public void addDeferredJimplification(DeferableInstruction i) {
    deferredInstructions.add(i);
  }

  /**
   * Add a retypeable instruction to this body.
   *
   * @param i
   *          the retypeable instruction.
   */
  public void addRetype(RetypeableInstruction i) {
    instructionsToRetype.add(i);
  }

  /**
   * Return the associated JimpleBody.
   *
   * @throws RuntimeException
   *           if no jimplification happened yet.
   */
  public Body getBody() {
    if (jBody == null) {
      throw new RuntimeException("No jimplification happened yet, no body available.");
    }
    return jBody;
  }

  /** Return the Locals that are associated with the current register state. */
  public Local[] getRegisterLocals() {
    return registerLocals;
  }

  /**
   * Return the Local that are associated with the number in the current register state.
   *
   * <p>
   * Handles if the register number actually points to a method parameter.
   *
   * @param num
   *          the register number
   * @throws InvalidDalvikBytecodeException
   */
  public Local getRegisterLocal(int num) throws InvalidDalvikBytecodeException {
    int totalRegisters = registerLocals.length;
    if (num >= totalRegisters) {
      throw new InvalidDalvikBytecodeException(
          "Trying to access register " + num + " but only " + totalRegisters + " is/are available.");
    }
    return registerLocals[num];
  }

  public Local getStoreResultLocal() {
    return storeResultLocal;
  }

  /**
   * Return the instruction that is present at the byte code address.
   *
   * @param address
   *          the byte code address.
   * @throws RuntimeException
   *           if address is not part of this body.
   */
  public DexlibAbstractInstruction instructionAtAddress(int address) {

    // catch addresses can be in the middlde of last instruction. Ex. in
    // com.letang.ldzja.en.apk:
    //
    // 042c46: 7020 2a15 0100 |008f: invoke-direct {v1, v0},
    // Ljavax/mi...
    // 042c4c: 2701 |0092: throw v1
    // catches : 4
    // <any> -> 0x0065
    // 0x0069 - 0x0093
    //
    // SA, 14.05.2014: We originally scanned only two code units back.
    // This is not sufficient
    // if we e.g., have a wide constant and the line number in the debug
    // sections points to
    // some address the middle.
    Integer key = instructionAtAddress.floorKey(address);
    if (key == null) {
      return null;
    }
    return instructionAtAddress.get(key);
  }

  protected ITypingStrategy getTypingStrategy() {
    return new DefaultTypingStrategy();
  }

  /**
   * Return the jimple equivalent of this body.
   *
   * @param m
   *          the SootMethod that contains this body
   */
  public Body jimplify(Body b, SootMethod m) {
    final Jimple jimple = Jimple.v();
    final UnknownType unknownType = UnknownType.v();
    final NullConstant nullConstant = NullConstant.v();
    final Options options = Options.v();
    final PhaseOptions phaseOptions = PhaseOptions.v();

    /*
     * Timer t_whole_jimplification = new Timer(); Timer t_num = new Timer(); Timer t_null = new Timer();
     *
     * t_whole_jimplification.start();
     */

    jBody = (JimpleBody) b;
    deferredInstructions = new ArrayList<DeferableInstruction>();
    instructionsToRetype = new HashSet<RetypeableInstruction>();

    if (jbOptions.use_original_names()) {
      phaseOptions.setPhaseOptionIfUnset("jb.lns", "only-stack-locals");
    }
    if (jbOptions.stabilize_local_names()) {
      phaseOptions.setPhaseOption("jb.lns", "sort-locals:true");
    }

    // process method parameters and generate Jimple locals from Dalvik
    // registers
    List<Local> paramLocals = new LinkedList<Local>();
    if (!isStatic) {
      int thisRegister = numRegisters - numParameterRegisters - 1;

      JimpleLocal thisLocal = jimple.newLocal(freshLocalName("this"), unknownType); // generateLocal(UnknownType.v());
      thisLocal.setUserDefinedLocal();
      jBody.getLocals().add(thisLocal);

      registerLocals[thisRegister] = thisLocal;
      JIdentityStmt idStmt = (JIdentityStmt) jimple.newIdentityStmt(thisLocal, jimple.newThisRef(declaringClassType));
      add(idStmt);
      paramLocals.add(thisLocal);
    }
    {
      int i = 0; // index of parameter type
      int argIdx = 0;
      int parameterRegister = numRegisters - numParameterRegisters; // index of parameter register
      for (Type t : parameterTypes) {

        boolean isUserLocal = false;
        String localName = null;
        Type localType = null;
        if (jbOptions.use_original_names()) {
          // Attempt to read original parameter name.
          try {
            localName = parameterNames.get(argIdx);
            if (localName != null) {
              isUserLocal = true;
              if (jbOptions.use_original_types()) {
                localType = parameterTypes.get(argIdx);
              }
            }
          } catch (Exception ex) {
            logger.error("Exception while reading original parameter names.", ex);
          }
        }
        if (localName == null) {
          if (localDebugs.containsKey(parameterRegister)) {
            localName = localDebugs.get(parameterRegister).get(0).name;
          } else {
            localName = "$u" + parameterRegister;
          }
        }
        if (localType == null) {
          // may only use UnknownType here because the local may be
          // reused with a different type later (before splitting)
          localType = unknownType;
        }

        JimpleLocal gen = jimple.newLocal(freshLocalName(localName), localType);
        if (isUserLocal) {
          gen.setUserDefinedLocal();
        }
        jBody.getLocals().add(gen);
        registerLocals[parameterRegister] = gen;

        JIdentityStmt idStmt = (JIdentityStmt) jimple.newIdentityStmt(gen, jimple.newParameterRef(t, i++));
        add(idStmt);
        paramLocals.add(gen);

        // some parameters may be encoded on two registers.
        // in Jimple only the first Dalvik register name is used
        // as the corresponding Jimple Local name. However, we also add
        // the second register to the registerLocals array since it
        // could be used later in the Dalvik bytecode
        if (t instanceof LongType || t instanceof DoubleType) {
          parameterRegister++;
          // may only use UnknownType here because the local may be reused with a different
          // type later (before splitting)
          String name;
          boolean userLocal;
          if (localDebugs.containsKey(parameterRegister)) {
            name = localDebugs.get(parameterRegister).get(0).name;
            userLocal = true;
          } else {
            name = "$u" + parameterRegister;
            userLocal = false;
          }
          JimpleLocal g = jimple.newLocal(freshLocalName(name), unknownType);
          if (userLocal) {
            g.setUserDefinedLocal();
          }
          jBody.getLocals().add(g);
          registerLocals[parameterRegister] = g;
        }

        parameterRegister++;
        argIdx++;
      }
    }

    for (int i = 0; i < (numRegisters - numParameterRegisters - (isStatic ? 0 : 1)); i++) {
      Type type = unknownType;
      String name;
      List<RegDbgEntry> dbg = localDebugs.get(i);
      boolean userLocal;
      if (!dbg.isEmpty()) {
        RegDbgEntry n = dbg.get(0);
        name = n.name;
        userLocal = true;
        if (jbOptions.use_original_types()) {
          type = n.type;
        }
      } else {
        name = "$u" + i;
        userLocal = false;
      }
      JimpleLocal l = jimple.newLocal(freshLocalName(name), type);
      if (userLocal) {
        l.setUserDefinedLocal();
      }
      jBody.getLocals().add(l);
      registerLocals[i] = l;
    }

    // add local to store intermediate results
    storeResultLocal = jimple.newLocal(freshLocalName("$u-1"), unknownType);
    jBody.getLocals().add(storeResultLocal);

    // process bytecode instructions
    final DexFile dexFile = dexEntry.getDexFile();
    final boolean isOdex
        = dexFile instanceof DexBackedDexFile ? ((DexBackedDexFile) dexFile).supportsOptimizedOpcodes() : false;

    ClassPath cp = null;
    if (isOdex) {
      String[] sootClasspath = options.soot_classpath().split(File.pathSeparator);
      List<String> classpathList = new ArrayList<String>();
      for (String str : sootClasspath) {
        classpathList.add(str);
      }
      try {
        ClassPathResolver resolver = new ClassPathResolver(classpathList, classpathList, classpathList, dexEntry);
        cp = new ClassPath(resolver.getResolvedClassProviders().toArray(new ClassProvider[0]));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    int prevLineNumber = -1;
    for (DexlibAbstractInstruction instruction : instructions) {
      if (isOdex && instruction instanceof OdexInstruction) {
        ((OdexInstruction) instruction).deOdex(dexFile, method, cp);
      }
      if (dangling != null) {
        dangling.finalize(this, instruction);
        dangling = null;
      }
      if (instruction.getLineNumber() > 0) {
        prevLineNumber = instruction.getLineNumber();
      } else {
        instruction.setLineNumber(prevLineNumber);
      }
      instruction.jimplify(this);
    }
    if (dangling != null) {
      dangling.finalize(this, null);
    }
    for (DeferableInstruction instruction : deferredInstructions) {
      instruction.deferredJimplify(this);
    }

    if (tries != null) {
      addTraps();
    }

    if (options.keep_line_number()) {
      fixLineNumbers();
    }

    // At this point Jimple code is generated
    // Cleaning...

    instructions = null;
    // registerLocals = null;
    // storeResultLocal = null;
    instructionAtAddress.clear();
    // localGenerator = null;
    deferredInstructions = null;
    // instructionsToRetype = null;
    dangling = null;
    tries = null;
    parameterNames.clear();

    /*
     * We eliminate dead code. Dead code has been shown to occur under the following circumstances.
     *
     * 0006ec: 0d00 |00a2: move-exception v0 ... 0006f2: 0d00 |00a5: move-exception v0 ... 0x0041 - 0x008a
     * Ljava/lang/Throwable; -> 0x00a5 <any> -> 0x00a2
     *
     * Here there are two traps both over the same region. But the same always fires, hence rendering the code at a2
     * unreachable. Dead code yields problems during local splitting because locals within dead code will not be split. Hence
     * we remove all dead code here.
     */
    UnitPatchingChain chain = jBody.getUnits();
    if (chain.isEmpty()) {
      throw new RuntimeException(String.format("%s: No units found; dex instructions: %s", m.getSignature(), instructions));
    }

    // Fix traps that do not catch exceptions
    DexTrapStackFixer.v().transform(jBody);

    // Sort out jump chains
    DexJumpChainShortener.v().transform(jBody);

    // Make sure that we don't have any overlapping uses due to returns
    DexReturnInliner.v().transform(jBody);

    getLocalSplitter().transform(jBody);
    // Remove dead code and the corresponding locals before assigning types
    getUnreachableCodeEliminator().transform(jBody);
    DeadAssignmentEliminator.v().transform(jBody);
    UnusedLocalEliminator.v().transform(jBody);

    for (RetypeableInstruction i : instructionsToRetype) {
      i.retype(jBody);
    }

    // {
    // // remove instructions from instructions list
    // List<DexlibAbstractInstruction> iToRemove = new
    // ArrayList<DexlibAbstractInstruction>();
    // for (DexlibAbstractInstruction i: instructions)
    // if (!jBody.getUnits().contains(i.getUnit()))
    // iToRemove.add(i);
    // for (DexlibAbstractInstruction i: iToRemove) {
    // Debug.printDbg("removing dexinstruction containing unit '",
    // i.getUnit() ,"'");
    // instructions.remove(i);
    // }
    // }

    // t_num.start();
    DexNumTransformer.v().transform(jBody);
    // t_num.end();

    DexReturnValuePropagator.v().transform(jBody);
    getCopyPopagator().transform(jBody);

    DexNullThrowTransformer.v().transform(jBody);

    // t_null.start();
    DexNullTransformer.v().transform(jBody);
    // t_null.end();

    DexIfTransformer.v().transform(jBody);

    DeadAssignmentEliminator.v().transform(jBody);
    UnusedLocalEliminator.v().transform(jBody);

    // DexRefsChecker.v().transform(jBody);
    DexNullArrayRefTransformer.v().transform(jBody);

    // Remove "instanceof" checks on the null constant
    DexNullInstanceofTransformer.v().transform(jBody);
    DexNullIfTransformer ni = DexNullIfTransformer.v();
    ni.transform(jBody);
    if (ni.hasModifiedBody()) {
      // Now we might have unreachable code
      ConditionalBranchFolder.v().transform(jBody);
      UnreachableCodeEliminator.v().transform(jBody);
      DeadAssignmentEliminator.v().transform(jBody);
      UnconditionalBranchFolder.v().transform(jBody);
    }
    DexFillArrayDataTransformer.v().transform(jBody);
    new SharedInitializationLocalSplitter(DalvikThrowAnalysis.v()).transform(jBody);
    // SharedInitializationLocalSplitter destroys the inserted casts, so we have to reintroduce them
    getLocalSplitter().transform(jBody);
    new DifferentArrayTypeSplitter().transform(jBody);

    MultiMap<Local, Type> maybetypeConstraints = new HashMultiMap<>();
    Map<Local, Collection<Type>> definiteConstraints = new HashMap<>();
    handleKnownDexTypes(b, jimple);
    handleAgreegingTypes(b, definiteConstraints);
    handleKnownDexArrayTypes(b, jimple, maybetypeConstraints);
    handleIncompatibleDexArrayTypes(b, maybetypeConstraints, definiteConstraints);
    for (Local l : b.getLocals()) {
      Type type = l.getType();
      if (type instanceof PrimType) {
        definiteConstraints.put(l, Collections.singleton(type));
      }
    }
    JBTROptions jbtrOptions = new JBTROptions(phaseOptions.getPhaseOptions("jb.tr"));

    new soot.jimple.toolkits.typing.fast.TypeResolver(jBody, jbtrOptions) {
      @Override
      protected ITyping typePromotion(ITyping tg) {
        // remove weak object types prior to type promotion
        Collection<Collection<Type>> values = definiteConstraints.values();
        Iterator<Collection<Type>> it = values.iterator();
        while (it.hasNext()) {
          Collection<Type> i = it.next();
          Iterator<Type> typeIt = i.iterator();
          while (typeIt.hasNext()) {
            if (typeIt.next() instanceof WeakObjectType) {
              typeIt.remove();
            }
          }
          if (i.isEmpty()) {
            it.remove();
          }
        }
        return super.typePromotion(tg);
      }

      @Override
      protected soot.jimple.toolkits.typing.fast.TypePromotionUseVisitor createTypePromotionUseVisitor(JimpleBody jb,
          ITyping tg) {
        return new TypePromotionUseVisitor(jb, tg) {
          @Override
          protected boolean allowConversion(Type ancestor, Type child) {
            if (ancestor == child) {
              return true;
            }
            if ((ancestor instanceof IntegerType || ancestor instanceof FloatType)
                && (child instanceof IntegerType || child instanceof FloatType)) {
              return true;
            }
            if ((ancestor instanceof LongType || ancestor instanceof DoubleType)
                && (child instanceof LongType || child instanceof DoubleType)) {
              return true;
            }
            return super.allowConversion(ancestor, child);
          }

          @Override
          public Type promote(Type tlow, Type thigh) {
            if (thigh instanceof BooleanType && tlow instanceof IntegerType) {
              // Well... in Android's dex code, 0 = false and everything else is true
              // While the compiler should never generate such code, there can be found code like this in the wild.
              // And Android accepts it!
              // Thus, we allow the type promotion and then correct the boolean constants
              return thigh;
            }
            if (tlow instanceof IntegerType) {
              if (thigh instanceof FloatType || thigh instanceof IntType) {
                return thigh;
              }
            }
            return super.promote(tlow, thigh);

          }
        };

      }

      @Override
      protected Type getDefiniteType(Local v) {
        Collection<Type> r = definiteConstraints.get(v);
        if (r != null && r.size() == 1) {
          Type n = r.iterator().next();
          if (!(n instanceof WeakObjectType)) {
            return n;
          }
        }
        return null;
      }

      @Override
      protected soot.jimple.toolkits.typing.fast.BytecodeHierarchy createBytecodeHierarchy() {
        return new soot.jimple.toolkits.typing.fast.BytecodeHierarchy() {
          @Override
          public java.util.Collection<Type> lcas(Type a, Type b, boolean useWeakObjectType) {
            Collection<Type> s = super.lcas(a, b, useWeakObjectType);
            if (s.isEmpty()) {
              // when we merge a null constant and anything non-primitive, we use the non-primitive type
              if (a instanceof Integer1Type && b instanceof RefLikeType) {
                return Collections.singleton(b);
              }
              if (b instanceof Integer1Type && a instanceof RefLikeType) {
                return Collections.singleton(a);
              }
            }
            return s;
          }
        };
      }

      @Override
      protected Collection<Type> reduceToAllowedTypesForLocal(Collection<Type> lcas, Local v) {
        Collection<Type> t = definiteConstraints.get(v);
        if (t != null) {
          return t;
        }
        Set<Type> constraints = maybetypeConstraints.get(v);
        if (constraints.isEmpty()) {
          return lcas;
        }
        if (lcas.size() == 1) {
          Type e = lcas.iterator().next();
          // Only one element, we can check this directly
          if (!constraints.contains(e)) {
            // No typing left
            Set<Type> res = new HashSet<>(constraints);
            res.add(e);
            return res;
          } else {
            return lcas;
          }
        }
        Set<Type> res = new HashSet<>(lcas);
        res.retainAll(constraints);
        if (res.isEmpty()) {
          // No typing left
          res.addAll(lcas);
          res.addAll(constraints);
          return res;
        }

        return res;
      }

      @Override
      protected soot.jimple.toolkits.typing.fast.ITypingStrategy getTypingStrategy() {
        ITypingStrategy useTyping = DexBody.this.getTypingStrategy();
        return useTyping;
      }

      @Override
      protected CastInsertionUseVisitor createCastInsertionUseVisitor(ITyping tg,
          soot.jimple.toolkits.typing.fast.IHierarchy h, boolean countOnly, int maxCasts) {
        return new CastInsertionUseVisitor(countOnly, jBody, tg, h, maxCasts) {

          @Override
          protected boolean eliminateUnnecessaryCasts() {
            // We do not want to eliminate casts that were explicitly present in the original dex code
            // Otherwise we have problems in certain edge cases, were our typings are suboptimal
            // with respect to float/int and double/long
            return false;
          }

          @Override
          protected NeedCastResult needCast(Type target, Type from, IHierarchy h) {
            NeedCastResult r = super.needCast(target, from, h);
            if (r == NeedCastResult.NEEDS_CAST) {
              if (target instanceof IntType || target instanceof FloatType) {
                if (from instanceof IntegerType || from instanceof FloatType) {
                  return NeedCastResult.DISCOURAGED_TARGET_TYPE;
                }
              }
              if (target instanceof LongType || target instanceof DoubleType) {
                if (from instanceof IntegerType || from instanceof LongType || from instanceof DoubleType) {
                  return NeedCastResult.DISCOURAGED_TARGET_TYPE;
                }
              }
              return r;
            }

            // we need to this since some types are final already. Otherwise,
            // we get no casts at all.
            if (target instanceof PrimType && from instanceof PrimType) {
              if (!from.isAllowedInFinalCode()) {
                from = from.getDefaultFinalType();
              }
              if (target.isAllowedInFinalCode()) {
                if (target == from) {
                  return NeedCastResult.DOESNT_NEED_CAST;
                }
                if (target instanceof IntType || target instanceof FloatType) {
                  if (from instanceof IntegerType || from instanceof FloatType) {
                    return NeedCastResult.DISCOURAGED_TARGET_TYPE;
                  }
                }
                if (target instanceof LongType || target instanceof DoubleType) {
                  if (from instanceof IntegerType || from instanceof LongType || from instanceof DoubleType) {
                    return NeedCastResult.DISCOURAGED_TARGET_TYPE;
                  }
                }
                return NeedCastResult.NEEDS_CAST;
              }
            }
            return NeedCastResult.DOESNT_NEED_CAST;
          }

          @Override
          public Value visit(Value op, Type useType, Stmt stmt, boolean checkOnly) {
            if (op instanceof LongConstant && useType instanceof DoubleType) {
              // no cast necessary for Dex
              return op;
            }
            if (op instanceof IntConstant && useType instanceof FloatType) {
              // no cast necessary for Dex
              return op;
            }
            return super.visit(op, useType, stmt, checkOnly);
          }
        };

      }
    }.inferTypes();
    for (Unit u : jBody.getUnits()) {
      Stmt s = (Stmt) u;
      if (s.containsArrayRef() && s instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) s;
        Value lop = assign.getLeftOp();
        Value rop = assign.getRightOp();
        if (lop.getType() instanceof FloatType && rop instanceof IntConstant) {
          IntConstant intC = (IntConstant) rop;
          assign.setRightOp(FloatConstant.v(Float.intBitsToFloat(intC.value)));
        }
        if (lop.getType() instanceof DoubleType && rop instanceof LongConstant) {
          LongConstant longC = (LongConstant) rop;
          assign.setRightOp(DoubleConstant.v(Double.longBitsToDouble(longC.value)));
        }
      }
    }

    checkUnrealizableCasts();

    getCopyPopagator().transform(jBody);
    // Shortcut: Reduce array initializations
    // We need to do this after typing, because otherwise we run into problems
    // when float constants (saved as int in dex code) are saved in the array.
    DexArrayInitReducer.v().transform(jBody);

    final RefType objectType = RefType.v("java.lang.Object");

    Unit last = chain.getLast();
    if (last.fallsThrough()) {
      logger.warn(String.format(
          "%s: Last statement is neither a throw, return or goto statement. This is likely the result of a packer.",
          m.getSignature(), instructions));
    } else {
      // We pack locals that are not used in overlapping regions. This may
      // again lead to unused locals which we have to remove.
      LocalPacker.v().transform(jBody);
      UnusedLocalEliminator.v().transform(jBody);
    }

    // Some apps reference static fields as instance fields. We fix this
    // on the fly.
    if (options.wrong_staticness() == Options.wrong_staticness_fix
        || options.wrong_staticness() == Options.wrong_staticness_fixstrict) {
      FieldStaticnessCorrector.v().transform(jBody);
      MethodStaticnessCorrector.v().transform(jBody);
    }

    // Inline PackManager.v().getPack("jb").apply(jBody);
    // Keep only transformations that have not been done
    // at this point.
    TrapTightener.v().transform(jBody);
    TrapMinimizer.v().transform(jBody);
    // LocalSplitter.v().transform(jBody);
    ArrayWriteAggregator.v().transform(jBody);
    Aggregator.v().transform(jBody);
    // UnusedLocalEliminator.v().transform(jBody);
    // TypeAssigner.v().transform(jBody);
    // LocalPacker.v().transform(jBody);
    // LocalNameStandardizer.v().transform(jBody);

    // Remove if (null == null) goto x else <madness>. We can only do this
    // after we have run the constant propagation as we might not be able
    // to statically decide the conditions earlier.
    ConditionalBranchFolder.v().transform(jBody);

    // Remove unnecessary typecasts
    ConstantCastEliminator.v().transform(jBody);
    IdentityCastEliminator.v().transform(jBody);

    // Remove unnecessary logic operations
    IdentityOperationEliminator.v().transform(jBody);

    // We need to run this transformer since the conditional branch folder
    // might have rendered some code unreachable (well, it was unreachable
    // before as well, but we didn't know).
    UnreachableCodeEliminator.v().transform(jBody);

    // Both, the original type assigner (Efficient Inference of Static
    // Types for Java Bytecode, 2000) and the fast type assigner
    // (Efficient local type inference, 2008) do split the local used in
    // the NewExpr and <init> InvokeExpr in stage 2 to obtain bytecode for
    // which a valid typing exists. This happens eagerly _for all_ object
    // creation sites, leading to unnecessary aliases at most of the
    // object creation sites. Copy Propagation here removes all of these
    // unnecessary copies from the TypeAssigner.
    CopyPropagator.v().transform(jBody);

    // we might have gotten new dead assignments and unused locals through
    // copy propagation and unreachable code elimination, so we have to do
    // this again
    DeadAssignmentEliminator.v().transform(jBody);
    UnusedLocalEliminator.v().transform(jBody);
    NopEliminator.v().transform(jBody);

    // Remove unnecessary chains of return statements
    DexReturnPacker.v().transform(jBody);

    for (Unit u : jBody.getUnits()) {

      if (u instanceof AssignStmt) {
        final AssignStmt ass = (AssignStmt) u;
        final Value rop = ass.getRightOp();
        if (rop instanceof CastExpr) {
          CastExpr c = (CastExpr) rop;
          if (c.getType() instanceof NullType) {
            ass.setRightOp(nullConstant);
          }
        }
        if (rop instanceof IntConstant) {
          if (ass.getLeftOp().getType() instanceof BooleanType) {
            ass.setRightOp(fixBooleanConstant((IntConstant) rop));
          }
        }

      }
      Stmt s = (Stmt) u;
      if (s.containsInvokeExpr()) {
        InvokeExpr inv = s.getInvokeExpr();
        for (int p = 0; p < inv.getArgCount(); p++) {
          if (inv.getMethodRef().getParameterType(p) instanceof BooleanType) {
            Value arg = inv.getArg(p);
            if (arg instanceof IntConstant) {
              inv.setArg(p, fixBooleanConstant((IntConstant) arg));
            }
          }
        }
      }
      if (u instanceof DefinitionStmt) {
        DefinitionStmt def = (DefinitionStmt) u;
        // If the body references a phantom class in a
        // CaughtExceptionRef,
        // we must manually fix the hierarchy
        Value rop = def.getRightOp();
        if (def.getLeftOp() instanceof Local && rop instanceof CaughtExceptionRef) {
          Type t = def.getLeftOp().getType();
          if (t instanceof RefType) {
            RefType rt = (RefType) t;
            if (rt.getSootClass().isPhantom() && !rt.getSootClass().hasSuperclass()
                && !rt.getSootClass().getName().equals("java.lang.Throwable")) {
              rt.getSootClass().setSuperclass(Scene.v().getSootClass("java.lang.Throwable"));
            }
          }
        }
      }
      removeDexplerTags(u);
    }

    // Replace local type null_type by java.lang.Object.
    //
    // The typing engine cannot find correct type for such code:
    //
    // null_type $n0;
    // $n0 = null;
    // $r4 = virtualinvoke $n0.<java.lang.ref.WeakReference:
    // java.lang.Object get()>();
    //
    for (Local l : jBody.getLocals()) {
      Type t = l.getType();
      if (t instanceof NullType || t instanceof BottomType) {
        l.setType(objectType);
      }
    }

    TrapTightener.removeInvalidTraps(jBody);

    // Must be last to ensure local ordering does not change
    PackManager.v().getTransform("jb.lns").apply(jBody);

    // Check that we don't have anything weird
    checkUnrealizableCasts();

    UnitPatchingChain units = jBody.getUnits();
    Unit u = units.getFirst();
    while (u != null) {
      if (u instanceof GotoStmt) {
        GotoStmt gt = (GotoStmt) u;
        if (gt.getTarget() == gt) {
          // There are crazy cases like that in the wild.
          NopStmt nop = jimple.newNopStmt();
          units.insertBefore(nop, u);
          gt.setTarget(nop);
        }
      }
      u = units.getSuccOf(u);
    }
    // t_whole_jimplification.end();

    return jBody;
  }

  /**
   * We try to find locals where all definitions agree on the type already. Since this can cause more local types to be
   * known, we do it until a fixed point is reached. This helps the type assigner in some cases massively
   * 
   * @param b
   *          the body
   * @param definiteConstraints
   *          the map containing definite constraints
   */
  private void handleAgreegingTypes(Body b, Map<Local, Collection<Type>> definiteConstraints) {

    BiFunction<Type, Type, Type> merge = new BiFunction<Type, Type, Type>() {

      final BottomType bot = BottomType.v();

      @Override
      public Type apply(Type t, Type u) {
        if (t instanceof UnknownType || u instanceof UnknownType) {
          return bot;
        }
        if (t.equals(u)) {
          return t;
        }
        return bot;
      }

    };

    final UnitPatchingChain units = b.getUnits();
    boolean changed = true;
    while (changed == true) {
      changed = false;
      Map<Local, Type> type = new HashMap<>();
      for (Unit u1 : units) {
        if (u1 instanceof DefinitionStmt) {
          DefinitionStmt assign = (DefinitionStmt) u1;
          Value lop = assign.getLeftOp();
          if (lop instanceof Local) {
            Value rop = assign.getRightOp();
            Type ropT = rop.getType();
            if (!(rop instanceof NullConstant)) {
              type.merge((Local) lop, ropT, merge);
            }
          }
        }
      }
      for (Entry<Local, Type> e : type.entrySet()) {
        Type etype = e.getValue();
        if (!(etype instanceof BottomType) && !(etype instanceof UnknownType)) {
          Local lcl = e.getKey();
          if (lcl.getType() != etype) {
            lcl.setType(etype);
            changed = true;
            definiteConstraints.put(lcl, Collections.singleton(etype));
          }
        }
      }
    }
  }

  /**
   * Handles cases where the array types are incompatible (any two different array types)
   */
  private void handleIncompatibleDexArrayTypes(Body b, MultiMap<Local, Type> maybetypeConstraints,
      Map<Local, Collection<Type>> definiteConstraints) {
    boolean arrayConstraintsNecessary = false;
    boolean hasArrayTypes = false;
    UnknownType unknownType = UnknownType.v();
    for (Unit u : b.getUnits()) {
      Stmt s = (Stmt) u;
      if (s instanceof DefinitionStmt) {
        DefinitionStmt def = (DefinitionStmt) s;
        Value lop = def.getLeftOp();
        if (lop instanceof Local && lop.getType() == unknownType && !maybetypeConstraints.containsKey((Local) lop)) {
          Local write = (Local) lop;
          Type ropType = def.getRightOp().getType();
          // Since all Java arrays are Serializable, Clonable and Object, the same Jimple local can be used among different
          // array types without splitting
          if (ropType instanceof ArrayType) {
            hasArrayTypes = true;
            Collection<Type> p = definiteConstraints.computeIfAbsent(write, new Function<Local, Collection<Type>>() {

              @Override
              public Collection<Type> apply(Local t) {
                LinkedList<Type> types = new LinkedList<>();
                types.add(ropType);
                return types;
              }
            });
            p.retainAll(Arrays.asList(ropType));
            if (p.size() == 0) {
              arrayConstraintsNecessary = true;
            }
          }
        }
      }
    }
    if (hasArrayTypes) {
      RefType object = RefType.v("java.lang.Object");
      RefType serializable = RefType.v("java.io.Serializable");
      RefType clonable = RefType.v("java.lang.Cloneable");

      // There are cases where variables are used as Objects. We need
      // to make sure that Soot considers Object as a possible typing for these variables,
      // since there are cases where this is the best option.
      // minCasts will take care to take the more appropriate typing.
      // Otherwise, we might end up with a lot of casts since Soot thinks it's forced
      // to use the non-object typing.
      // This is relevant only if there are multiple definitions for that variable, that could
      // end up in a lot of casts.
      Map<Local, Integer> defCount = new HashMap<>();
      for (Unit u : b.getUnits()) {
        if (u instanceof AssignStmt) {
          AssignStmt assign = (AssignStmt) u;
          Value lop = assign.getLeftOp();
          if (definiteConstraints.containsKey(lop)) {
            Local l = (Local) lop;
            defCount.compute(l, (cl, p) -> {
              if (p == null) {
                return 1;
              } else {
                return 2;
              }
            });
          }
        }
      }
      for (Unit u : b.getUnits()) {
        Stmt s = (Stmt) u;
        if (s.containsInvokeExpr()) {
          int i = 0;
          InvokeExpr iexpr = s.getInvokeExpr();
          for (Type p : iexpr.getMethodRef().getParameterTypes()) {
            Value a = iexpr.getArg(i);
            if (a instanceof Local) {
              Local l = (Local) a;
              if (p == clonable || p == serializable || p == object) {
                Collection<Type> d = definiteConstraints.get(l);
                if (d != null && defCount.getOrDefault(l, 0) > 1) {
                  boolean hasArrayType = false;
                  for (Type t : d) {
                    if (t instanceof ArrayType) {
                      hasArrayType = true;
                    }
                  }
                  if (hasArrayType) {
                    LinkedHashSet<Type> dnew = new LinkedHashSet<>();
                    dnew.add(p);
                    dnew.addAll(d);
                    definiteConstraints.put(l, dnew);
                  }
                }
              }

            }
            i++;
          }
        }
      }

      if (arrayConstraintsNecessary) {
        RefType weakObject = new WeakObjectType("java.lang.Object");
        RefType weakSerializable = null;
        RefType weakCloneable = null;
        for (Unit u : b.getUnits()) {
          Stmt s = (Stmt) u;
          // Performance optimization: Only if another method or field requires us to use the Cloneable or Serializable,
          // we consider this as a valid type.
          if (s.containsInvokeExpr()) {
            for (Type p : s.getInvokeExpr().getMethodRef().getParameterTypes()) {
              if (p == clonable) {
                weakCloneable = new WeakObjectType("java.lang.Cloneable");
              } else if (p == serializable) {
                weakSerializable = new WeakObjectType("java.io.Serializable");
              }
            }
          } else if (s.containsFieldRef()) {
            Type ft = s.getFieldRef().getType();
            if (ft == clonable) {
              weakCloneable = new WeakObjectType("java.lang.Cloneable");
            } else if (ft == serializable) {
              weakSerializable = new WeakObjectType("java.io.Serializable");
            }
          }
        }

        for (Entry<Local, Collection<Type>> key : definiteConstraints.entrySet()) {
          Collection<Type> vs = key.getValue();
          if (vs.isEmpty()) {
            // incompatible arrays -> we can use Serializable/Object
            vs.add(weakObject);
            if (weakSerializable != null) {
              vs.add(weakSerializable);
            }
            if (weakCloneable != null) {
              vs.add(weakCloneable);
            }
          }
        }
      }
    }
  }

  /**
   * In Dex, every int is a valid boolean. 0 = false and everything else = true.
   * 
   * @param arg
   * @return
   */
  private static BooleanConstant fixBooleanConstant(IntConstant arg) {
    return BooleanConstant.v(arg.value != 0);
  }

  /**
   * For non-object array instructions, we know from the bytecode already what the types are, or at least we can reduce it to
   * two possibilities (int/float or float/double).
   * 
   * @param b
   *          the body
   * @param jimple
   *          the jimple instance to use (caching is slightly faster)
   * @param typeConstraints
   *          type constraints (these might be multiple valid possibilities)
   */
  private void handleKnownDexArrayTypes(Body b, Jimple jimple, MultiMap<Local, Type> typeConstraints) {
    Map<Local, Integer> localsSingleDefinitions = new HashMap<>();
    for (Unit u : b.getUnits()) {
      if (u instanceof DefinitionStmt) {
        Value l = ((DefinitionStmt) u).getLeftOp();
        if (l instanceof Local) {
          int counter = localsSingleDefinitions.getOrDefault(l, 0);
          localsSingleDefinitions.put((Local) l, counter + 1);
        }
      }
    }
    UnitPatchingChain units = jBody.getUnits();
    Unit u = units.getFirst();
    while (u != null) {
      if (u instanceof AssignStmt) {
        AssignStmt assign = ((AssignStmt) u);
        Value rop = assign.getRightOp();
        if (rop instanceof ArrayRef) {
          for (Tag tg : u.getTags()) {
            if (tg instanceof DexplerTag) {
              DexplerTag dexplerTypeTag = (DexplerTag) tg;
              Type definiteType = dexplerTypeTag.getDefiniteType();
              if (definiteType != null) {
                Local prev = (Local) assign.getLeftOp();
                if (!(definiteType instanceof PrimType) || localsSingleDefinitions.getOrDefault(prev, 0) == 1) {
                  prev.setType(definiteType);
                } else {
                  // Since there are multiple definitions, e.g. for a byte retrieved from a byte[],
                  // there could be another non-distinct definition which uses the same variable as an int.
                  PrimType[] wider = DexType.getWiderTypes((PrimType) definiteType);
                  if (wider.length == 1) {
                    prev.setType(wider[0]);
                  }
                }

                ArrayType tp = ArrayType.v(definiteType, 1);

                ArrayRef array = (ArrayRef) rop;
                Local lbase = (Local) array.getBase();
                lbase.setType(tp);

              } else if (tg instanceof IntOrFloatOpTag || tg instanceof LongOrDoubleOpTag) {
                // sadly, we don't know for sure. But: we know that it's either of these two.
                // we need a fresh local or each instance, no re-use allowed.
                Local l = jimple.newLocal(freshLocalName("lcl" + tg.getName()), UnknownType.v());
                b.getLocals().add(l);
                ArrayRef array = (ArrayRef) rop;
                units.insertBefore(jimple.newAssignStmt(l, array.getBase()), u);
                array.setBase(l);
                if (typeConstraints != null) {
                  if (tg instanceof IntOrFloatOpTag) {
                    typeConstraints.put(l, ArrayType.v(IntType.v(), 1));
                    typeConstraints.put(l, ArrayType.v(FloatType.v(), 1));
                  } else {
                    typeConstraints.put(l, ArrayType.v(LongType.v(), 1));
                    typeConstraints.put(l, ArrayType.v(DoubleType.v(), 1));
                  }
                }
              }
            }
          }
        }
      }
      u = units.getSuccOf(u);
    }
  }

  /**
   * For several instructions, we know from the bytecode already what the types are. We use that knowledge here to help the
   * type assigner.
   * 
   * @param b
   *          the body
   * @param jimple
   *          the jimple instance to use (caching is slightly faster)
   */
  private void handleKnownDexTypes(Body b, final Jimple jimple) {
    UnitPatchingChain units = jBody.getUnits();
    Unit u = units.getFirst();
    while (u != null) {
      if (u instanceof AssignStmt) {
        AssignStmt def = (AssignStmt) u;
        Value rop = def.getRightOp();
        if (rop instanceof NegExpr) {
          boolean isDouble = u.hasTag(DoubleOpTag.NAME);
          boolean isFloat = u.hasTag(FloatOpTag.NAME);
          boolean isLong = u.hasTag(LongOpTag.NAME);
          NegExpr neg = ((NegExpr) rop);
          Value op = neg.getOp();
          Type t = null;
          // As for ints, shorts etc.: the type assigner
          // already handles this automatically
          if (isDouble) {
            t = DoubleType.v();
          } else if (isFloat) {
            t = FloatType.v();
          } else if (isLong) {
            t = LongType.v();
          }
          if (t != null) {
            Local l = (Local) op;
            l.setType(t);
          }

        } else if (rop instanceof BinopExpr) {
          boolean isDouble = u.hasTag(DoubleOpTag.NAME);
          boolean isFloat = u.hasTag(FloatOpTag.NAME);
          boolean isInt = u.hasTag(IntOpTag.NAME);
          boolean isShort = u.hasTag(ShortOpTag.NAME);
          boolean isByte = u.hasTag(ByteOpTag.NAME);
          if (rop instanceof AddExpr || rop instanceof SubExpr || rop instanceof MulExpr || rop instanceof DivExpr
              || rop instanceof RemExpr || rop instanceof XorExpr || rop instanceof UshrExpr || rop instanceof ShrExpr
              || rop instanceof ShlExpr || rop instanceof AndExpr || rop instanceof OrExpr) {
            Type t = null;
            if (isDouble) {
              t = DoubleType.v();
            } else if (isFloat) {
              t = FloatType.v();
            } else if (isInt) {
              t = IntType.v();
            } else if (isShort) {
              t = ShortType.v();
            } else if (isByte) {
              t = ByteType.v();
            }
            if (t != null) {
              Local left = (Local) def.getLeftOp();
              left.setType(t);
            }
          }
          BinopExpr bop = (BinopExpr) rop;
          for (ValueBox cmp : bop.getUseBoxes()) {
            Value c = cmp.getValue();
            if (c instanceof Constant) {
              if (isDouble) {
                if (c instanceof LongConstant) {
                  long vVal = ((LongConstant) c).value;
                  cmp.setValue(DoubleConstant.v(Double.longBitsToDouble(vVal)));
                }
              } else if (isFloat && c instanceof IntConstant) {
                int vVal = ((IntConstant) c).value;
                cmp.setValue(FloatConstant.v(Float.intBitsToFloat(vVal)));
              }
            } else {
              Local t = (Local) cmp.getValue();
              if (isDouble) {
                t.setType(DoubleType.v());
              } else if (isFloat) {
                t.setType(FloatType.v());
              }
            }
          }
        } else if (rop instanceof CastExpr) {
          CastExpr ce = (CastExpr) rop;
          Value op = ce.getOp();
          if (op instanceof Constant) {
            boolean isDouble = u.hasTag(DoubleOpTag.NAME);
            boolean isFloat = u.hasTag(FloatOpTag.NAME);
            if (isFloat) {
              if (op instanceof IntConstant) {
                int vVal = ((IntConstant) op).value;
                ce.setOp(FloatConstant.v(Float.intBitsToFloat(vVal)));
              } else if (op instanceof LongConstant) {
                long vVal = ((LongConstant) op).value;
                ce.setOp(FloatConstant.v(Float.intBitsToFloat((int) vVal)));
              }
            } else if (isDouble) {
              if (op instanceof LongConstant) {
                long vVal = ((LongConstant) op).value;
                ce.setOp(DoubleConstant.v(Double.longBitsToDouble(vVal)));
              } else if (op instanceof IntConstant) {
                int vVal = ((IntConstant) op).value;
                ce.setOp(DoubleConstant.v(Double.longBitsToDouble(vVal)));
              }
            }
          }
        }

      }
      if (u instanceof IfStmt) {
        IfStmt ifS = (IfStmt) u;
        BinopExpr expr = (BinopExpr) ifS.getCondition();
        Type t = expr.getKnownOperandType();
        if (t != null) {
          Value lop = expr.getOp1();
          Value rop = expr.getOp2();
          if (lop instanceof Local) {
            Local l = (Local) lop;
            l.setType(t);
          }
          if (rop instanceof Local) {
            Local r = (Local) rop;
            r.setType(t);
          }
        }
      }
      Stmt s = (Stmt) u;
      if (s.containsInvokeExpr()) {
        InvokeExpr inv = s.getInvokeExpr();
        Type it = inv.getType();
        if (it instanceof PrimType && s instanceof AssignStmt) {
          AssignStmt assign = (AssignStmt) s;
          Local l = (Local) assign.getLeftOp();
          l.setType(it);
        }
        for (int pidx = 0; pidx < inv.getArgCount(); pidx++) {
          Value arg = inv.getArg(pidx);
          Type at = arg.getType();
          if (at instanceof PrimType && arg instanceof Local) {
            Local l = (Local) arg;
            l.setType(at);
          }
          if (arg instanceof Constant) {
            Type t = inv.getMethodRef().getParameterType(pidx);
            if (t instanceof DoubleType && arg instanceof LongConstant) {
              long vVal = ((LongConstant) arg).value;
              inv.setArg(pidx, DoubleConstant.v(Double.longBitsToDouble(vVal)));
            }
            if (t instanceof FloatType && arg instanceof IntConstant) {
              int vVal = ((IntConstant) arg).value;
              inv.setArg(pidx, FloatConstant.v(Float.intBitsToFloat(vVal)));
            }
          }
        }
      }
      u = units.getSuccOf(u);
    }
    for (Unit u1 : units) {
      if (u1 instanceof AssignStmt) {
        AssignStmt assign = (AssignStmt) u1;
        Type tl = assign.getLeftOp().getType();
        Value rop = assign.getRightOp();
        if (rop instanceof Constant) {
          Constant c = (Constant) assign.getRightOp();
          if (tl instanceof DoubleType && c instanceof LongConstant) {
            long vVal = ((LongConstant) c).value;
            assign.setRightOp(DoubleConstant.v(Double.longBitsToDouble(vVal)));
          } else if (tl instanceof FloatType && c instanceof IntConstant) {
            int vVal = ((IntConstant) c).value;
            assign.setRightOp(FloatConstant.v(Float.intBitsToFloat(vVal)));
          }
        }
      }
    }

  }

  /**
   * Removes all dexpler specific tags. Saves some memory.
   * 
   * @param unit
   *          the statement
   */
  private void removeDexplerTags(Unit unit) {
    for (Iterator<Tag> it = unit.getTags().iterator(); it.hasNext();) {
      Tag t = it.next();
      if (t instanceof DexplerTag) {
        it.remove();
      }
    }
  }

  /**
   * Checks whether the Jimple code contains unrealizable casts between reference types and primitives
   */
  private void checkUnrealizableCasts() {
    for (Unit u : jBody.getUnits()) {
      if (u instanceof AssignStmt) {
        AssignStmt assignStmt = (AssignStmt) u;
        Value rop = assignStmt.getRightOp();
        if (rop instanceof CastExpr) {
          CastExpr cast = (CastExpr) rop;
          if ((cast.getCastType() instanceof PrimType && cast.getOp().getType() instanceof RefType)
              || (cast.getCastType() instanceof RefType && cast.getOp().getType() instanceof PrimType)) {

            throw new RuntimeException("Unrealizable cast " + u + " detected in method " + jBody.getMethod().getSignature());
          }
        }
      }
    }
  }

  /**
   * Fixes the line numbers. If there is a unit without a line number, it gets the line number of the last (transitive)
   * predecessor that has a line number.
   */
  protected void fixLineNumbers() {
    int prevLn = -1;
    for (DexlibAbstractInstruction instruction : instructions) {
      Unit unit = instruction.getUnit();
      int lineNumber = unit.getJavaSourceStartLineNumber();
      if (lineNumber < 0) {
        if (prevLn >= 0) {
          unit.addTag(new LineNumberTag(prevLn));
          unit.addTag(new SourceLineNumberTag(prevLn));
        }
      } else {
        prevLn = lineNumber;
      }
    }
  }

  private LocalSplitter localSplitter = null;

  protected LocalSplitter getLocalSplitter() {
    if (this.localSplitter == null) {
      this.localSplitter = new LocalSplitter(DalvikThrowAnalysis.v());
    }
    return this.localSplitter;
  }

  private UnreachableCodeEliminator unreachableCodeEliminator = null;

  protected UnreachableCodeEliminator getUnreachableCodeEliminator() {
    if (this.unreachableCodeEliminator == null) {
      this.unreachableCodeEliminator = new UnreachableCodeEliminator(DalvikThrowAnalysis.v());
    }
    return this.unreachableCodeEliminator;
  }

  private CopyPropagator copyPropagator = null;

  protected CopyPropagator getCopyPopagator() {
    if (this.copyPropagator == null) {
      this.copyPropagator = new CopyPropagator(DalvikThrowAnalysis.v(), false);
    }
    return this.copyPropagator;
  }

  /** Set a dangling instruction for this body. */
  public void setDanglingInstruction(DanglingInstruction i) {
    dangling = i;
  }

  /**
   * Return the instructions that appear (lexically) after the given instruction.
   *
   * @param instruction
   *          the instruction which successors will be returned.
   */
  public List<DexlibAbstractInstruction> instructionsAfter(DexlibAbstractInstruction instruction) {
    int i = instructions.indexOf(instruction);
    if (i == -1) {
      throw new IllegalArgumentException("Instruction" + instruction + "not part of this body.");
    }

    return instructions.subList(i + 1, instructions.size());
  }

  /**
   * Return the instructions that appear (lexically) before the given instruction.
   *
   * <p>
   * The instruction immediately before the given is the first instruction and so on.
   *
   * @param instruction
   *          the instruction which successors will be returned.
   */
  public List<DexlibAbstractInstruction> instructionsBefore(DexlibAbstractInstruction instruction) {
    int i = instructions.indexOf(instruction);
    if (i == -1) {
      throw new IllegalArgumentException("Instruction " + instruction + " not part of this body.");
    }

    List<DexlibAbstractInstruction> l = new ArrayList<DexlibAbstractInstruction>();
    l.addAll(instructions.subList(0, i));
    Collections.reverse(l);
    return l;
  }

  /**
   * Add the traps of this body.
   *
   * <p>
   * Should only be called at the end jimplify.
   */
  private void addTraps() {
    final Jimple jimple = Jimple.v();
    boolean removedOneTrap = false;
    for (TryBlock<? extends ExceptionHandler> tryItem : tries) {
      int startAddress = tryItem.getStartCodeAddress();
      int length = tryItem.getCodeUnitCount(); // .getTryLength();
      int endAddress = startAddress + length; // - 1;
      DexlibAbstractInstruction s = instructionAtAddress(startAddress);
      // Narrow the traps: when the statement cannot throw, we can take the next one
      // This trap narrowing has multiple purposes: It not only makes the code easier to read,
      // but also potentially easier to type.
      // I've encountered a case where due too-far reaching traps, the local splitter cannot split
      // to locals, since the catch-handler uses this local, although the trap handler cannot be reached from
      // the definition of the local.
      // Therefore, want the traps as tight as possible.
      while (!s.getInstruction().getOpcode().canThrow()) {
        Integer ceiling = instructionAtAddress.navigableKeySet().ceiling(startAddress + 1);
        if (ceiling == null) {
          break;
        }
        startAddress = ceiling;
        s = instructionAtAddress(startAddress);
      }
      Unit beginStmt = s.getUnit();
      // (startAddress + length) typically points to the first byte of the
      // first instruction after the try block
      // except if there is no instruction after the try block in which
      // case it points to the last byte of the last
      // instruction of the try block. Removing 1 from (startAddress +
      // length) always points to "somewhere" in
      // the last instruction of the try block since the smallest
      // instruction is on two bytes (nop = 0x0000).
      DexlibAbstractInstruction e = instructionAtAddress(endAddress);
      int endAddressU = endAddress;
      endAddress = instructionAtAddress.navigableKeySet().floor(endAddressU - 1);
      // Narrow the traps: when the statement cannot throw, we can narrow the end
      while (!instructionAtAddress(endAddress).getInstruction().getOpcode().canThrow()) {
        e = instructionAtAddress(endAddress);
        endAddressU = endAddress;
        Integer res = instructionAtAddress.navigableKeySet().floor(endAddress - 1);
        if (res == null) {
          break;
        }
        endAddress = res;
      }
      if (endAddress < startAddress) {
        // eliminated the trap entirely
        removedOneTrap = true;
        continue;
      }
      Unit endStmt = e.getUnit();
      // if the try block ends on the last instruction of the body, add a
      // nop instruction so Soot can include
      // the last instruction in the try block.
      if (jBody.getUnits().getLast() == endStmt && instructionAtAddress(endAddressU - 1).getUnit() == endStmt) {
        Unit nop = jimple.newNopStmt();
        jBody.getUnits().insertAfter(nop, endStmt);
        endStmt = nop;
      }

      List<? extends ExceptionHandler> hList = tryItem.getExceptionHandlers();
      for (ExceptionHandler handler : hList) {
        String exceptionType = handler.getExceptionType();
        if (exceptionType == null) {
          exceptionType = "Ljava/lang/Throwable;";
        }
        Type t = DexType.toSoot(exceptionType);
        // exceptions can only be of RefType
        if (t instanceof RefType) {
          SootClass exception = ((RefType) t).getSootClass();
          DexlibAbstractInstruction instruction = instructionAtAddress(handler.getHandlerCodeAddress());
          if (!(instruction instanceof MoveExceptionInstruction)) {
            logger.debug(String.format("First instruction of trap handler unit not MoveException but %s",
                instruction.getClass().getName()));
          } else {
            ((MoveExceptionInstruction) instruction).setRealType(this, exception.getType());
          }

          Unit handlerStmt = instruction.getUnit();
          Trap trap = jimple.newTrap(exception, beginStmt, endStmt, handlerStmt);
          jBody.getTraps().add(trap);
        }
      }
    }
    if (removedOneTrap) {
      getUnreachableCodeEliminator().transform(jBody);
    }
  }
}
