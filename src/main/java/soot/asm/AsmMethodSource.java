package soot.asm;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2014 Raja Vallee-Rai and others
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
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DCMPG;
import static org.objectweb.asm.Opcodes.DCMPL;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DNEG;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.F2I;
import static org.objectweb.asm.Opcodes.F2L;
import static org.objectweb.asm.Opcodes.FCMPG;
import static org.objectweb.asm.Opcodes.FCMPL;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FCONST_2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.JSR;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RET;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.T_BOOLEAN;
import static org.objectweb.asm.Opcodes.T_BYTE;
import static org.objectweb.asm.Opcodes.T_CHAR;
import static org.objectweb.asm.Opcodes.T_DOUBLE;
import static org.objectweb.asm.Opcodes.T_FLOAT;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.T_LONG;
import static org.objectweb.asm.Opcodes.T_SHORT;
import static org.objectweb.asm.tree.AbstractInsnNode.FIELD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.FRAME;
import static org.objectweb.asm.tree.AbstractInsnNode.IINC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INT_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.JUMP_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LABEL;
import static org.objectweb.asm.tree.AbstractInsnNode.LDC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LINE;
import static org.objectweb.asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TABLESWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TYPE_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.VAR_INSN;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Body;
import soot.BooleanConstant;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.Immediate;
import soot.IntType;
import soot.LambdaMetaFactory;
import soot.Local;
import soot.LongType;
import soot.MethodSource;
import soot.Modifier;
import soot.ModuleScene;
import soot.ModuleUtil;
import soot.PackManager;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.UnitPatchingChain;
import soot.UnknownType;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.asm.Operand.OperandType;
import soot.dexpler.Util;
import soot.jimple.AddExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MethodHandle;
import soot.jimple.MethodType;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.TrapTightener;
import soot.toolkits.scalar.LocalPacker;
import soot.util.Chain;

/**
 * Generates Jimple bodies from bytecode.
 *
 * @author Aaloan Miftah
 */
public class AsmMethodSource implements MethodSource {
  private static final Logger logger = LoggerFactory.getLogger(AsmMethodSource.class);

  private static final String METAFACTORY_SIGNATURE = "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite "
      + "metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,"
      + "java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>";

  private static final String ALT_METAFACTORY_SIGNATURE = "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite "
      + "altMetafactory(java.lang.invoke.MethodHandles$Lookup,"
      + "java.lang.String,java.lang.invoke.MethodType,java.lang.Object[])>";

  /* -const fields- */
  private final String module;
  private final int maxLocals;
  private final InsnList instructions;
  private final List<LocalVariableNode> localVars;
  private final List<TryCatchBlockNode> tryCatchBlocks;
  private final Set<LabelNode> inlineExceptionLabels = new LinkedHashSet<LabelNode>();
  private final Map<LabelNode, IdentityStmt> inlineExceptionHandlers = new LinkedHashMap<LabelNode, IdentityStmt>();
  private final CastAndReturnInliner castAndReturnInliner = new CastAndReturnInliner();

  /** Labels at which a trap handler range (try block) begins */
  private final Map<LabelNode, TryCatchBlockNode> startTrapHandler = new HashMap<>();

  /** Labels at which a trap handler range (try block) ends */
  private final Map<LabelNode, TryCatchBlockNode> endTrapHandler = new HashMap<>();

  /* -state fields- */
  protected int nextLocal;
  protected Map<Integer, JimpleLocal> locals;
  Map<AbstractInsnNode, Unit> insnToStmt;

  private Multimap<LabelNode, UnitBox> trapHandlers;
  private JimpleBody body;
  private Map<AbstractInsnNode, LocationMetadata> locationMap;
  private Map<InvokeDynamicInsnNode, SootMethodRef> lambdaMethodRefsMap;

  private static class LocationMetadata {
    public int lineNumber = -1, bytecodeOffset = -1;
  }

  public AsmMethodSource(int maxLocals, InsnList insns, List<LocalVariableNode> localVars,
      List<TryCatchBlockNode> tryCatchBlocks, String module) {
    this.maxLocals = maxLocals;
    this.instructions = insns;
    this.localVars = localVars;
    this.tryCatchBlocks = tryCatchBlocks;
    this.module = module;
  }

  private SootClass getClassFromScene(String className) {
    SootClass result;
    if (ModuleUtil.module_mode()) {
      result = ModuleScene.v().getSootClassUnsafe(className, Optional.fromNullable(this.module));
    } else {
      result = Scene.v().getSootClassUnsafe(className);
    }

    if (result == null) {
      String msg = String.format("%s was not found on classpath.", className);
      if (Options.v().allow_phantom_refs()) {
        RefType ref = RefType.v(className);
        // make sure nobody else creates the same class
        synchronized (ref) {
          logger.warn(msg);
          result = Scene.v().makeSootClass(className, Modifier.PUBLIC);
          Scene.v().addClass(result);
          result.setPhantomClass();
          return ref.getSootClass();
        }
      } else {
        throw new RuntimeException(msg);
      }
    }
    return result;
  }

  private final boolean useOriginalTypes
      = PhaseOptions.getBoolean(PhaseOptions.v().getPhaseOptions("jb"), "use-original-types");

  /** Keeps track of all trap handlers that are active at the current instruction */
  Set<TryCatchBlockNode> activeTrapHandlers = new HashSet<>();

  private OperandStack operandStack;

  private LinkedListMultimap<LabelNode, UnitBox> labels;

  private Local getLocal(int idx) {
    if (idx >= maxLocals) {
      throw new IllegalArgumentException("Invalid local index: " + idx);
    }
    Integer i = idx;
    JimpleLocal l = locals.get(i);
    if (l == null) {
      String name = getLocalName(idx);
      Type type = UnknownType.v();
      if (useOriginalTypes) {
        LocalVariableNode local = getLocalVarNode(i);
        if (local != null && local.desc != null) {
          type = AsmUtil.toJimpleType(local.desc, Optional.absent());
        }
      }
      boolean userDefinedName = true;
      if (name == null) {
        name = "l" + idx;
        userDefinedName = false;
      }
      l = Jimple.v().newLocal(name, type);
      if (userDefinedName) {
        l.setUserDefinedLocal();
      }
      locals.put(i, l);
    }
    return l;
  }

  protected String getLocalName(int idx) {
    String name = null;
    if (localVars != null) {
      LocalVariableNode n = getLocalVarNode(idx);
      /* normally for try-catch blocks */
      if (n != null) {
        name = n.name;
      }
    }
    return name;
  }

  private LocalVariableNode getLocalVarNode(int idx) {
    for (LocalVariableNode lvn : localVars) {
      // Ignore LocalVariableNode which don't cover any real units
      if (lvn.index == idx && lvn.start != lvn.end) {
        return lvn;
      }
    }
    return null;
  }

  protected void setUnit(Operand op, Unit u) {
    setUnit(op.insn, u);
    if (op.tag != null) {
      u.addTag(op.tag);
    }
  }

  protected void setUnit(AbstractInsnNode insn, Unit u) {
    if (locationMap != null) {
      LocationMetadata l = locationMap.get(insn);
      if (l != null) {
        int ln = l.lineNumber;
        if (ln >= 0) {
          setLineNumber(u, ln);
        }
        int bo = l.bytecodeOffset;
        if (bo >= 0) {
          setByteCodeOffset(u, bo);
        }
      }
    }

    insnToStmt.put(insn, u);
  }

  protected void setByteCodeOffset(Unit u, int bytecodeOffset) {
    Tag offsetTag = u.getTag(BytecodeOffsetTag.NAME);
    if (offsetTag == null) {
      offsetTag = new BytecodeOffsetTag(bytecodeOffset);
      u.addTag(offsetTag);
    } else if (((BytecodeOffsetTag) offsetTag).getBytecodeOffset() != bytecodeOffset) {
      throw new RuntimeException("Bytecode offset tag mismatch");
    }

  }

  protected void setLineNumber(Unit u, int lineNumber) {
    Tag lineTag = u.getTag(LineNumberTag.NAME);
    if (lineTag == null) {
      lineTag = new LineNumberTag(lineNumber);
      u.addTag(lineTag);
    } else if (((LineNumberTag) lineTag).getLineNumber() != lineNumber) {
      throw new RuntimeException("Line tag mismatch");
    }
  }

  void mergeUnits(AbstractInsnNode insn, Unit u) {
    Unit prev = insnToStmt.put(insn, u);
    if (prev != null) {
      Unit merged = new UnitContainer(prev, u);
      insnToStmt.put(insn, merged);
      merged.addAllTagsOf(prev);
    }
  }

  protected Local newStackLocal() {
    Integer idx = nextLocal++;
    JimpleLocal l = Jimple.v().newLocal("$stack" + idx, UnknownType.v());
    locals.put(idx, l);
    return l;
  }

  @SuppressWarnings("unchecked")
  <A extends Unit> A getUnit(AbstractInsnNode insn) {
    return (A) insnToStmt.get(insn);
  }

  private void assignReadOps(Local l) {
    addReadOperandAssignments(l);
  }

  private void convertGetFieldInsn(FieldInsnNode insn) {
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Type type;
    SootClass declClass = this.getClassFromScene(AsmUtil.toQualifiedName(insn.owner));
    type = AsmUtil.toJimpleType(insn.desc, Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));
    Value val;
    SootFieldRef ref;
    if (insn.getOpcode() == GETSTATIC) {
      ref = Scene.v().makeFieldRef(declClass, insn.name, type, true);
      val = Jimple.v().newStaticFieldRef(ref);
    } else {
      Operand base = operandStack.pop();
      merging.mergeInputs(base);
      ref = Scene.v().makeFieldRef(declClass, insn.name, type, false);
      InstanceFieldRef ifr = Jimple.v().newInstanceFieldRef(base.toLocal(), ref);
      val = ifr;
    }
    Operand opr = new Operand(insn, val, this);
    merging.mergeOutput(opr);
    operandStack.push(type, opr);
  }

  private void convertPutFieldInsn(FieldInsnNode insn) {
    boolean instance = insn.getOpcode() == PUTFIELD;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Type type;
    SootClass declClass = this.getClassFromScene(AsmUtil.toQualifiedName(insn.owner));
    type = AsmUtil.toJimpleType(insn.desc, Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));

    Operand rvalue = operandStack.pop(type);
    Value val;
    SootFieldRef ref;
    if (!instance) {
      merging.mergeInputs(rvalue);
      ref = Scene.v().makeFieldRef(declClass, insn.name, type, true);
      val = Jimple.v().newStaticFieldRef(ref);
    } else {
      Operand base = operandStack.pop();
      merging.mergeInputs(rvalue, base);
      ref = Scene.v().makeFieldRef(declClass, insn.name, type, false);
      InstanceFieldRef ifr = Jimple.v().newInstanceFieldRef(base.toLocal(), ref);
      val = ifr;
    }
    AssignStmt as = Jimple.v().newAssignStmt(val, rvalue.toImmediate());
    setUnit(insn, as);
    /*
     * in case any static field or array is read from, and the static constructor or the field this instruction writes to,
     * modifies that field, write out any previous read from field/array
     */
    addReadOperandAssignments();

  }

  private void addReadOperandAssignments() {
    addReadOperandAssignments_internal((opValue, operand) -> {
      if (opValue instanceof Local) {
        return true;
      }
      int op = operand.insn.getOpcode();
      return op != GETFIELD && op != GETSTATIC && (op < IALOAD || op > SALOAD);
    });
  }

  private void addReadOperandAssignments(Local local) {
    addReadOperandAssignments_internal((opValue, operand) -> {
      if (!opValue.equivTo(local)) {
        boolean noRef = true;
        for (Unit i : insnToStmt.values()) {
          for (Iterator<ValueBox> iterator = i.getUseAndDefBoxesIterator(); iterator.hasNext();) {
            ValueBox use = iterator.next();
            if (local.equivTo(use.getValue())) {
              noRef = false;
              break;
            }
          }

        }
        return noRef;
      }
      return false;
    });
  }

  private void addReadOperandAssignments_internal(BiFunction<Value, Operand, Boolean> func) {
    // determine which Operand(s) from the stack needs explicit assignments in Jimple
    for (Operand operand : operandStack.getStack()) {
      final Value opValue = operand.value;
      if (operand == Operand.DWORD_DUMMY || operand.stackLocal != null) {
        continue;
      }
      if (func.apply(opValue, operand)) {
        continue;
      }

      operand.emitStatement();
    }
  }

  private void convertFieldInsn(FieldInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GETSTATIC || op == GETFIELD) {
      convertGetFieldInsn(insn);
    } else {
      convertPutFieldInsn(insn);
    }
  }

  private void convertIincInsn(IincInsnNode insn) {
    Local local = getLocal(insn.var);
    assignReadOps(local);
    if (!insnToStmt.containsKey(insn)) {
      AddExpr add = Jimple.v().newAddExpr(local, IntConstant.v(insn.incr));
      setUnit(insn, Jimple.v().newAssignStmt(local, add));
    }
  }

  private void convertConstInsn(InsnNode insn) {
    int op = insn.getOpcode();
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Value v;
    if (op == ACONST_NULL) {
      v = NullConstant.v();
    } else if (op >= ICONST_M1 && op <= ICONST_5) {
      v = IntConstant.v(op - ICONST_0);
    } else if (op == LCONST_0 || op == LCONST_1) {
      v = LongConstant.v(op - LCONST_0);
    } else if (op >= FCONST_0 && op <= FCONST_2) {
      v = FloatConstant.v(op - FCONST_0);
    } else if (op == DCONST_0 || op == DCONST_1) {
      v = DoubleConstant.v(op - DCONST_0);
    } else {
      throw new UnsupportedOperationException("Unknown constant opcode: " + op);
    }
    Operand opr = new Operand(insn, v, this);
    merging.mergeOutput(opr);
    if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertArrayLoadInsn(InsnNode insn) {
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand indx = operandStack.pop();
    Operand base = operandStack.pop();
    merging.mergeInputs(indx, base);
    ArrayRef ar = Jimple.v().newArrayRef(base.toLocal(), indx.toImmediate());
    Operand opr = new Operand(insn, ar, this);
    merging.mergeOutput(opr);
    int op = insn.getOpcode();
    if (op == DALOAD || op == LALOAD) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertArrayStoreInsn(InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LASTORE || op == DASTORE;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand valueOp = dword ? operandStack.popDual() : operandStack.pop();
    Operand indexOp = operandStack.pop();
    Operand baseOp = operandStack.pop();
    merging.mergeInputs(valueOp, indexOp, baseOp);
    ArrayRef ar = Jimple.v().newArrayRef(baseOp.toLocal(), indexOp.toImmediate());
    AssignStmt as = Jimple.v().newAssignStmt(ar, valueOp.toImmediate());
    setUnit(insn, as);
  }

  private void convertDupInsn(InsnNode insn) {
    int op = insn.getOpcode();

    // Get the top stack value which we need in either case
    Operand dupd = operandStack.pop();
    Operand dupd2 = null;

    // Some instructions allow operands that take two registers
    boolean dword = op == DUP2 || op == DUP2_X1 || op == DUP2_X2;
    if (dword) {
      if (operandStack.peek() == Operand.DWORD_DUMMY) {
        operandStack.pop();
        dupd2 = dupd;
      } else {
        dupd2 = operandStack.pop();
      }
    }

    if (op == DUP) {
      // val -> val, val
      operandStack.push(dupd);
      operandStack.push(dupd);
    } else if (op == DUP_X1) {
      // val2, val1 -> val1, val2, val1
      // value1, value2 must not be of type double or long
      Operand o2 = operandStack.pop();
      operandStack.push(dupd);
      operandStack.push(o2);
      operandStack.push(dupd);
    } else if (op == DUP_X2) {
      // value3, value2, value1 -> value1, value3, value2, value1
      Operand o2 = operandStack.pop();
      // pops either the `Operand.DWORD_DUMMY` or the third value
      Operand o3 = operandStack.pop();
      operandStack.push(dupd);
      operandStack.push(o3);
      operandStack.push(o2);
      operandStack.push(dupd);
    } else if (op == DUP2) {
      // value2, value1 -> value2, value1, value2, value1
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    } else if (op == DUP2_X1) {
      // value3, value2, value1 -> value2, value1, value3, value2, value1
      // Attention: value2 may be
      Operand o2 = operandStack.pop();
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(o2);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    } else if (op == DUP2_X2) {
      // (value4, value3), (value2, value1) -> (value2, value1), (value4, value3),
      // (value2, value1)
      Operand o2 = operandStack.pop();
      Operand o2h = operandStack.pop();
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(o2h);
      operandStack.push(o2);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    }
  }

  private void convertBinopInsn(InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == DADD || op == LADD || op == DSUB || op == LSUB || op == DMUL || op == LMUL || op == DDIV
        || op == LDIV || op == DREM || op == LREM || op == LSHL || op == LSHR || op == LUSHR || op == LAND || op == LOR
        || op == LXOR || op == LCMP || op == DCMPL || op == DCMPG;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand op2 = (dword && op != LSHL && op != LSHR && op != LUSHR) ? operandStack.popDual() : operandStack.pop();
    Operand op1 = dword ? operandStack.popDual() : operandStack.pop();
    merging.mergeInputs(op2, op1);
    Immediate v1 = op1.toImmediate();
    Immediate v2 = op2.toImmediate();
    Value binop;
    if (op >= IADD && op <= DADD) {
      binop = Jimple.v().newAddExpr(v1, v2);
    } else if (op >= ISUB && op <= DSUB) {
      binop = Jimple.v().newSubExpr(v1, v2);
    } else if (op >= IMUL && op <= DMUL) {
      binop = Jimple.v().newMulExpr(v1, v2);
    } else if (op >= IDIV && op <= DDIV) {
      binop = Jimple.v().newDivExpr(v1, v2);
    } else if (op >= IREM && op <= DREM) {
      binop = Jimple.v().newRemExpr(v1, v2);
    } else if (op >= ISHL && op <= LSHL) {
      binop = Jimple.v().newShlExpr(v1, v2);
    } else if (op >= ISHR && op <= LSHR) {
      binop = Jimple.v().newShrExpr(v1, v2);
    } else if (op >= IUSHR && op <= LUSHR) {
      binop = Jimple.v().newUshrExpr(v1, v2);
    } else if (op >= IAND && op <= LAND) {
      binop = Jimple.v().newAndExpr(v1, v2);
    } else if (op >= IOR && op <= LOR) {
      binop = Jimple.v().newOrExpr(v1, v2);
    } else if (op >= IXOR && op <= LXOR) {
      binop = Jimple.v().newXorExpr(v1, v2);
    } else if (op == LCMP) {
      binop = Jimple.v().newCmpExpr(v1, v2);
    } else if (op == FCMPL || op == DCMPL) {
      binop = Jimple.v().newCmplExpr(v1, v2);
    } else if (op == FCMPG || op == DCMPG) {
      binop = Jimple.v().newCmpgExpr(v1, v2);
    } else {
      throw new UnsupportedOperationException("Unknown binop: " + op);
    }

    Operand opr = new Operand(insn, binop, this);
    merging.mergeOutput(opr);

    if (dword && op < LCMP) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertUnopInsn(InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LNEG || op == DNEG;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand op1 = dword ? operandStack.popDual() : operandStack.pop();
    merging.mergeInputs(op1);
    Value unop;
    if (op >= INEG && op <= DNEG) {
      unop = Jimple.v().newNegExpr(op1.toImmediate());
    } else if (op == ARRAYLENGTH) {
      unop = Jimple.v().newLengthExpr(op1.toImmediate());
    } else {
      throw new UnsupportedOperationException("Unknown unop: " + op);
    }
    Operand opr = new Operand(insn, unop, this);
    merging.mergeOutput(opr);
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertPrimCastInsn(InsnNode insn) {
    int op = insn.getOpcode();
    boolean tod = op == I2L || op == I2D || op == F2L || op == F2D || op == D2L || op == L2D;
    boolean fromd = op == D2L || op == L2D || op == D2I || op == L2I || op == D2F || op == L2F;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Type totype;
    switch (op) {
      case I2L:
      case F2L:
      case D2L:
        totype = LongType.v();
        break;
      case L2I:
      case F2I:
      case D2I:
        totype = IntType.v();
        break;
      case I2F:
      case L2F:
      case D2F:
        totype = FloatType.v();
        break;
      case I2D:
      case L2D:
      case F2D:
        totype = DoubleType.v();
        break;
      case I2B:
        totype = ByteType.v();
        break;
      case I2S:
        totype = ShortType.v();
        break;
      case I2C:
        totype = CharType.v();
        break;
      default:
        throw new IllegalStateException("Unknown prim cast op: " + op);
    }
    Operand val = fromd ? operandStack.popDual() : operandStack.pop();
    merging.mergeInputs(val);
    CastExpr cast = Jimple.v().newCastExpr(val.toImmediate(), totype);
    Operand opr = new Operand(insn, cast, this);
    merging.mergeOutput(opr);
    if (tod) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertReturnInsn(InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LRETURN || op == DRETURN;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand val = dword ? operandStack.popDual() : operandStack.pop();
    merging.mergeInputs(val);
    ReturnStmt ret = Jimple.v().newReturnStmt(val.toImmediate());
    setUnit(insn, ret);
    handleStackLeftover();
  }

  private void convertInsn(InsnNode insn) {
    int op = insn.getOpcode();
    if (op == NOP) {
      /*
       * We can ignore NOP instructions, but for completeness, we handle them
       */
      if (!insnToStmt.containsKey(insn)) {
        insnToStmt.put(insn, Jimple.v().newNopStmt());
      }
    } else if (op >= ACONST_NULL && op <= DCONST_1) {
      convertConstInsn(insn);
    } else if (op >= IALOAD && op <= SALOAD) {
      convertArrayLoadInsn(insn);
    } else if (op >= IASTORE && op <= SASTORE) {
      convertArrayStoreInsn(insn);
    } else if (op == POP) {
      operandStack.pop().emitStatement();
    } else if (op == POP2) {
      operandStack.pop().emitStatement();
      // pops the `Operand.DWORD_DUMMY` or the second value
      operandStack.pop().emitStatement();
    } else if (op >= DUP && op <= DUP2_X2) {
      convertDupInsn(insn);
    } else if (op == SWAP) {
      Operand o1 = operandStack.pop();
      Operand o2 = operandStack.pop();
      operandStack.push(o1);
      operandStack.push(o2);
    } else if ((op >= IADD && op <= DREM) || (op >= ISHL && op <= LXOR) || (op >= LCMP && op <= DCMPG)) {
      convertBinopInsn(insn);
    } else if ((op >= INEG && op <= DNEG) || op == ARRAYLENGTH) {
      convertUnopInsn(insn);
    } else if (op >= I2L && op <= I2S) {
      convertPrimCastInsn(insn);
    } else if (op >= IRETURN && op <= ARETURN) {
      convertReturnInsn(insn);
    } else if (op == RETURN) {
      if (!insnToStmt.containsKey(insn)) {
        handleStackLeftover();
        setUnit(insn, Jimple.v().newReturnVoidStmt());
      }
    } else if (op == ATHROW) {
      OperandMerging merging = operandStack.getOrCreateMerging(insn);
      Operand opr = operandStack.pop();
      merging.mergeInputs(opr);
      ThrowStmt ts = Jimple.v().newThrowStmt(opr.toImmediate());
      setUnit(insn, ts);
      merging.mergeOutput(opr);
      operandStack.push(opr);
    } else if (op == MONITORENTER || op == MONITOREXIT) {
      OperandMerging merging = operandStack.getOrCreateMerging(insn);
      Operand opr = operandStack.popStackConst();
      merging.mergeInputs(opr);
      Stmt ts = op == MONITORENTER ? Jimple.v().newEnterMonitorStmt(opr.toImmediate())
          : Jimple.v().newExitMonitorStmt(opr.toImmediate());
      setUnit(insn, ts);
    } else {
      throw new AssertionError("Unknown insn op: " + op);
    }
  }

  private void handleStackLeftover() {
    while (!operandStack.isEmpty()) {
      Operand leftover = operandStack.peek();
      if (leftover == null) {
        break;
      }
      OperandType type = leftover.type;

      if (type == OperandType.DOUBLE || type == OperandType.DOUBLE) {
        operandStack.popDual();
      } else {
        operandStack.pop();
      }

      if (leftover.value instanceof InvokeExpr) {
        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(leftover.value);
        if (!insnToStmt.containsKey(leftover.insn)) {
          setUnit(leftover.insn, invokeStmt);
        }
      }
    }
  }

  private void convertIntInsn(IntInsnNode insn) {
    int op = insn.getOpcode();
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Value v;
    if (op == BIPUSH || op == SIPUSH) {
      v = IntConstant.v(insn.operand);
    } else {
      Type type;
      switch (insn.operand) {
        case T_BOOLEAN:
          type = BooleanType.v();
          break;
        case T_CHAR:
          type = CharType.v();
          break;
        case T_FLOAT:
          type = FloatType.v();
          break;
        case T_DOUBLE:
          type = DoubleType.v();
          break;
        case T_BYTE:
          type = ByteType.v();
          break;
        case T_SHORT:
          type = ShortType.v();
          break;
        case T_INT:
          type = IntType.v();
          break;
        case T_LONG:
          type = LongType.v();
          break;
        default:
          throw new AssertionError("Unknown NEWARRAY type!");
      }
      Operand size = operandStack.pop();
      merging.mergeInputs(size);
      v = Jimple.v().newNewArrayExpr(type, size.toImmediate());
    }
    Operand opr = new Operand(insn, v, this);
    merging.mergeOutput(opr);
    operandStack.push(opr);
  }

  private void convertJumpInsn(JumpInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GOTO) {
      if (!insnToStmt.containsKey(insn)) {
        UnitBox box = Jimple.v().newStmtBox(null);
        GotoStmt gotoStmt = Jimple.v().newGotoStmt(box);
        labels.put(insn.label, box);
        setUnit(insn, gotoStmt);
      }
      return;
    }

    /* must be ifX insn */
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    if (!insnToStmt.containsKey(insn)) {
      Operand val = operandStack.pop();
      Immediate v = val.toImmediate();
      Value cond;

      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        Operand val1 = operandStack.pop();
        merging.mergeInputs(val, val1);
        Immediate v1 = val1.toImmediate();
        switch (op) {
          case IF_ICMPEQ:
          case IF_ACMPEQ:
            cond = Jimple.v().newEqExpr(v1, v);
            break;
          case IF_ICMPNE:
            cond = Jimple.v().newNeExpr(v1, v);
            break;
          case IF_ICMPLT:
            cond = Jimple.v().newLtExpr(v1, v);
            break;
          case IF_ICMPGE:
            cond = Jimple.v().newGeExpr(v1, v);
            break;
          case IF_ICMPGT:
            cond = Jimple.v().newGtExpr(v1, v);
            break;
          case IF_ICMPLE:
            cond = Jimple.v().newLeExpr(v1, v);
            break;
          case IF_ACMPNE:
            cond = Jimple.v().newNeExpr(v1, v);
            break;
          default:
            throw new UnsupportedOperationException("Unknown if op: " + op);
        }
      } else {
        merging.mergeInputs(val);
        switch (op) {
          case IFEQ:
            cond = Jimple.v().newEqExpr(v, BooleanConstant.v(false));
            break;
          case IFNE:
            cond = Jimple.v().newNeExpr(v, IntConstant.v(0));
            break;
          case IFLT:
            cond = Jimple.v().newLtExpr(v, IntConstant.v(0));
            break;
          case IFGE:
            cond = Jimple.v().newGeExpr(v, IntConstant.v(0));
            break;
          case IFGT:
            cond = Jimple.v().newGtExpr(v, IntConstant.v(0));
            break;
          case IFLE:
            cond = Jimple.v().newLeExpr(v, IntConstant.v(0));
            break;
          case IFNULL:
            cond = Jimple.v().newEqExpr(v, NullConstant.v());
            break;
          case IFNONNULL:
            cond = Jimple.v().newNeExpr(v, NullConstant.v());
            break;
          default:
            throw new UnsupportedOperationException("Unknown if op: " + op);
        }
      }
      UnitBox box = Jimple.v().newStmtBox(null);
      labels.put(insn.label, box);
      IfStmt ifStmt = Jimple.v().newIfStmt(cond, box);
      setUnit(insn, ifStmt);
    } else {
      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        merging.mergeInputs(operandStack.pop(), operandStack.pop());
      } else {
        merging.mergeInputs(operandStack.pop());
      }
    }
  }

  private void convertLdcInsn(LdcInsnNode insn) {
    Object val = insn.cst;
    boolean dword = val instanceof Long || val instanceof Double;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Value v = toSootValue(val);
    Operand opr = new Operand(insn, v, this);
    merging.mergeOutput(opr);
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private Value toSootValue(Object val) throws AssertionError {
    Value v;
    if (val instanceof Integer) {
      v = IntConstant.v((Integer) val);
    } else if (val instanceof Float) {
      v = FloatConstant.v((Float) val);
    } else if (val instanceof Long) {
      v = LongConstant.v((Long) val);
    } else if (val instanceof Double) {
      v = DoubleConstant.v((Double) val);
    } else if (val instanceof String) {
      v = StringConstant.v(val.toString());
    } else if (val instanceof org.objectweb.asm.Type) {
      org.objectweb.asm.Type t = (org.objectweb.asm.Type) val;
      if (t.getSort() == org.objectweb.asm.Type.METHOD) {
        List<Type> paramTypes = AsmUtil.toJimpleDesc(((org.objectweb.asm.Type) val).getDescriptor(),
            Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));
        Type returnType = paramTypes.remove(paramTypes.size() - 1);
        v = MethodType.v(paramTypes, returnType);
      } else {
        v = ClassConstant.v(((org.objectweb.asm.Type) val).getDescriptor());
      }
    } else if (val instanceof Handle) {
      Handle h = (Handle) val;
      if (MethodHandle.isMethodRef(h.getTag())) {
        v = MethodHandle.v(toSootMethodRef(h), h.getTag());
      } else {
        v = MethodHandle.v(toSootFieldRef(h), h.getTag());
      }
    } else if (val instanceof ConstantDynamic) {
      // JEP 309: dynamic class-file constants (CONSTANT_Dynamic). Soot does not resolve these
      // to their computed value. We model them gracefully as a dynamic invocation of their
      // bootstrap method, which preserves the declared constant type as well as the bootstrap
      // method and its static arguments, and warn that the value is not resolved.
      v = toSootDynamicConstant((ConstantDynamic) val);
    } else {
      throw new AssertionError("Unknown constant type: " + val.getClass());
    }
    return v;
  }

  /**
   * Models a JEP 309 dynamic class-file constant (CONSTANT_Dynamic). Soot cannot resolve the value that the constant's
   * bootstrap method would compute at run time, so the constant is represented as a dynamic invocation of its bootstrap
   * method. The resulting expression carries the constant's declared type, the bootstrap method reference and the static
   * bootstrap arguments, so that downstream analyses can at least reason about the type and the bootstrap linkage. A warning
   * is issued because the actual constant value is not resolved.
   *
   * @param cd
   *          the dynamic constant to model
   * @return a {@link soot.jimple.DynamicInvokeExpr} approximating the dynamic constant
   */
  private Value toSootDynamicConstant(ConstantDynamic cd) {
    logger.warn(
        "Encountered dynamic class-file constant (JEP 309) '{}' of type '{}' in method {}. "
            + "Soot does not resolve the computed value and models it as a dynamic invocation of its bootstrap method.",
        cd.getName(), cd.getDescriptor(), body.getMethod().getSignature());

    Handle bsm = cd.getBootstrapMethod();
    SootMethodRef bsmMethodRef = toSootMethodRef(bsm);

    int argCount = cd.getBootstrapMethodArgumentCount();
    List<Value> bsmArgs = new ArrayList<>(argCount);
    for (int i = 0; i < argCount; i++) {
      bsmArgs.add(toSootValue(cd.getBootstrapMethodArgument(i)));
    }

    Type constType = AsmUtil.toJimpleType(cd.getDescriptor(),
        Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));

    // A dynamic constant takes no dynamic arguments; it resolves to a single value of its declared type. We model it on
    // the synthetic invokedynamic dummy class, mirroring how genuine invokedynamic call sites are handled.
    SootClass bclass = Scene.v().getSootClass(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME);
    SootMethodRef methodRef = Scene.v().makeMethodRef(bclass, cd.getName(), Collections.<Type>emptyList(), constType, true);

    return Jimple.v().newDynamicInvokeExpr(bsmMethodRef, bsmArgs, methodRef, bsm.getTag(), Collections.<Value>emptyList());
  }

  private void convertLookupSwitchInsn(LookupSwitchInsnNode insn) {

    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    if (insnToStmt.containsKey(insn)) {
      merging.mergeInputs(operandStack.pop());
      return;
    }
    Operand key = operandStack.pop();
    merging.mergeInputs(key);

    List<IntConstant> keys = new ArrayList<>(insn.keys.size());
    for (Integer i : insn.keys) {
      keys.add(IntConstant.v(i));
    }

    List<UnitBox> targets = new ArrayList<UnitBox>(insn.labels.size());
    UnitBox dflt = Jimple.v().newStmtBox(null);
    labels.put(insn.dflt, dflt);
    for (LabelNode ln : insn.labels) {
      UnitBox box = Jimple.v().newStmtBox(null);
      targets.add(box);
      labels.put(ln, box);
    }
    LookupSwitchStmt lss = Jimple.v().newLookupSwitchStmt(key.toImmediate(), keys, targets, dflt);

    setUnit(insn, lss);

  }

  private void convertMethodInsn(MethodInsnNode insn) {

    int op = insn.getOpcode();
    boolean isInstance = op != INVOKESTATIC;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    String clsName = AsmUtil.toQualifiedName(insn.owner);
    if (clsName.charAt(0) == '[') {
      clsName = "java.lang.Object";
    }

    List<Type> sigTypes
        = AsmUtil.toJimpleDesc(insn.desc, Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));
    Type returnType = sigTypes.remove(sigTypes.size() - 1);
    SootMethodRef ref
        = Scene.v().makeMethodRef(this.getClassFromScene(clsName), insn.name, sigTypes, returnType, !isInstance);

    int nrArgs = sigTypes.size();
    final Operand[] operands;
    Immediate[] argList = new Immediate[nrArgs];
    final List<Immediate> args;
    if (!isInstance) {
      operands = nrArgs == 0 ? null : new Operand[nrArgs];
    } else {
      operands = new Operand[nrArgs + 1];
    }
    while (nrArgs-- != 0) {
      operands[nrArgs] = operandStack.pop(sigTypes.get(nrArgs));
    }
    if (isInstance) {
      operands[operands.length - 1] = operandStack.pop();
    }
    if (operands != null) {
      merging.mergeInputs(operands);
    }
    nrArgs = sigTypes.size();
    while (nrArgs-- != 0) {
      argList[nrArgs] = operands[nrArgs].toImmediate();
    }
    args = Arrays.asList(argList);
    Value invoke;
    if (!isInstance) {
      invoke = Jimple.v().newStaticInvokeExpr(ref, args);
    } else {
      Operand baseOperand = operands[operands.length - 1];
      Local base = baseOperand.toLocal();

      switch (op) {
        case INVOKESPECIAL:
          invoke = Jimple.v().newSpecialInvokeExpr(base, ref, args);
          break;
        case INVOKEVIRTUAL:
          invoke = Jimple.v().newVirtualInvokeExpr(base, ref, args);
          break;
        case INVOKEINTERFACE:
          invoke = Jimple.v().newInterfaceInvokeExpr(base, ref, args);
          break;
        default:
          throw new UnsupportedOperationException("Unknown invoke op:" + op);
      }
    }
    Operand opr = new Operand(insn, invoke, this);
    merging.mergeOutput(opr);

    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (returnType != VoidType.v()) {
      operandStack.push(opr);
    } else if (!insnToStmt.containsKey(insn)) {
      InvokeStmt stmt = Jimple.v().newInvokeStmt(opr.value);
      setUnit(insn, stmt);
    }
    /*
     * assign all read ops in case the method modifies any of the fields
     */
    addReadOperandAssignments();
  }

  private void convertInvokeDynamicInsn(InvokeDynamicInsnNode insn) {

    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    // convert info on bootstrap method
    SootMethodRef bsmMethodRef = toSootMethodRef(insn.bsm);
    List<Value> bsmMethodArgs = new ArrayList<>(insn.bsmArgs.length);
    for (Object bsmArg : insn.bsmArgs) {
      bsmMethodArgs.add(toSootValue(bsmArg));
    }

    // Generate parameters & returnType & parameterTypes
    Type[] types = AsmUtil.jimpleTypesOfFieldOrMethodDescriptor(insn.desc);
    SootMethodRef bootstrap_model = null;
    if (PhaseOptions.getBoolean(PhaseOptions.v().getPhaseOptions("jb"), "model-lambdametafactory")) {
      String bsmMethodRefStr = bsmMethodRef.toString();
      if (bsmMethodRefStr.equals(METAFACTORY_SIGNATURE) || bsmMethodRefStr.equals(ALT_METAFACTORY_SIGNATURE)) {
        if (lambdaMethodRefsMap == null) {
          lambdaMethodRefsMap = new HashMap<>();
        }
        bootstrap_model = lambdaMethodRefsMap.computeIfAbsent(insn, (cinsn) -> {
          int bytecodeOffset = getBytecodeOffset(insn);
          return LambdaMetaFactory.v().makeLambdaHelper(bsmMethodArgs, insn.bsm.getTag(), insn.name, types, body.getMethod(),
              bytecodeOffset);
        });
      }
    }

    int nrArgs = types.length - 1;
    Type[] parameterTypes = new Type[nrArgs];
    Immediate[] methodArgs = new Immediate[nrArgs];

    Operand[] args = new Operand[nrArgs];
    // Beware: Call stack is FIFO, Jimple is linear

    for (int i = nrArgs - 1; i >= 0; i--) {
      parameterTypes[i] = types[i];

      args[i] = operandStack.pop(types[i]);
    }
    Type returnType = types[types.length - 1];
    merging.mergeInputs(args);
    for (int i = nrArgs - 1; i >= 0; i--) {
      methodArgs[i] = args[i].toImmediate();
    }

    InvokeExpr indy;
    if (bootstrap_model != null) {
      indy = Jimple.v().newStaticInvokeExpr(bootstrap_model, methodArgs);
    } else {
      // if not mimicking the LambdaMetaFactory, we model invokeDynamic method refs as static
      // method references of methods on the type SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME
      SootClass bclass = Scene.v().getSootClass(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME);
      SootMethodRef methodRef = Scene.v().makeMethodRef(bclass, insn.name, Arrays.asList(parameterTypes), returnType, true);
      indy = Jimple.v().newDynamicInvokeExpr(bsmMethodRef, bsmMethodArgs, methodRef, insn.bsm.getTag(),
          Arrays.asList(methodArgs));
    }
    Operand opr = new Operand(insn, indy, this);
    merging.mergeOutput(opr);
    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (!(returnType instanceof VoidType)) {
      operandStack.push(opr);
    } else if (!insnToStmt.containsKey(insn)) {
      InvokeStmt stmt = Jimple.v().newInvokeStmt(opr.value);
      setUnit(insn, stmt);
    }
    /*
     * assign all read ops in case the method modifies any of the fields
     */
    addReadOperandAssignments();
  }

  /**
   * Searches backwards for the bytecode offset
   * 
   * @param insn
   *          the instruction node
   * @return the bytecode offset or -1
   */
  private int getBytecodeOffset(AbstractInsnNode insn) {
    if (locationMap == null) {
      return -1;
    }
    LocationMetadata info;
    AbstractInsnNode i = insn;
    while (i != null) {
      info = locationMap.get(i);
      if (info != null && info.bytecodeOffset > 0) {
        return info.bytecodeOffset;
      }
      i = i.getPrevious();
    }
    return -1;
  }

  private SootMethodRef toSootMethodRef(Handle methodHandle) {
    String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
    SootClass bsmCls = this.getClassFromScene(bsmClsName);
    List<Type> bsmSigTypes = AsmUtil.toJimpleDesc(methodHandle.getDesc(),
        Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));
    Type returnType = bsmSigTypes.remove(bsmSigTypes.size() - 1);
    return Scene.v().makeMethodRef(bsmCls, methodHandle.getName(), bsmSigTypes, returnType,
        methodHandle.getTag() == MethodHandle.Kind.REF_INVOKE_STATIC.getValue());
  }

  private SootFieldRef toSootFieldRef(Handle methodHandle) {
    String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
    SootClass bsmCls = Scene.v().getSootClass(bsmClsName);
    Type t = AsmUtil
        .toJimpleDesc(methodHandle.getDesc(), Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName))
        .get(0);
    int kind = methodHandle.getTag();
    return Scene.v().makeFieldRef(bsmCls, methodHandle.getName(), t,
        kind == MethodHandle.Kind.REF_GET_FIELD_STATIC.getValue()
            || kind == MethodHandle.Kind.REF_PUT_FIELD_STATIC.getValue());
  }

  private void convertMultiANewArrayInsn(MultiANewArrayInsnNode insn) {
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    ArrayType t = (ArrayType) AsmUtil.toJimpleType(insn.desc,
        Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName));
    int dims = insn.dims;
    Operand[] sizes = new Operand[dims];
    Immediate[] sizeVals = new Immediate[dims];
    while (dims-- != 0) {
      sizes[dims] = operandStack.pop();
    }
    merging.mergeInputs(sizes);
    dims = insn.dims;
    while (dims-- != 0) {
      sizeVals[dims] = sizes[dims].toImmediate();
    }
    NewMultiArrayExpr nm = Jimple.v().newNewMultiArrayExpr(t, Arrays.asList(sizeVals));
    Operand opr = new Operand(insn, nm, this);
    merging.mergeOutput(opr);
    operandStack.push(opr);
  }

  private void convertTableSwitchInsn(TableSwitchInsnNode insn) {

    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    if (insnToStmt.containsKey(insn)) {
      merging.mergeInputs(operandStack.pop());
      return;
    }
    Operand key = operandStack.pop();
    merging.mergeInputs(key);
    UnitBox dflt = Jimple.v().newStmtBox(null);
    List<UnitBox> targets = new ArrayList<UnitBox>(insn.labels.size());
    labels.put(insn.dflt, dflt);
    for (LabelNode ln : insn.labels) {
      UnitBox box = Jimple.v().newStmtBox(null);
      targets.add(box);
      labels.put(ln, box);
    }

    TableSwitchStmt tss = Jimple.v().newTableSwitchStmt(key.toImmediate(), insn.min, insn.max, targets, dflt);
    setUnit(insn, tss);
  }

  private void convertTypeInsn(TypeInsnNode insn) {
    int op = insn.getOpcode();
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Optional<String> module = Optional.fromNullable(this.body.getMethod().getDeclaringClass().moduleName);
    Type t = AsmUtil.toJimpleRefType(insn.desc, module);
    Value val;
    if (op == NEW) {
      val = Jimple.v().newNewExpr((RefType) t);
    } else {
      Operand op1 = operandStack.pop();
      merging.mergeInputs(op1);
      switch (op) {
        case ANEWARRAY: {
          NewArrayExpr expr = Jimple.v().newNewArrayExpr(t, op1.toImmediate());
          val = expr;
          break;
        }
        case CHECKCAST: {
          CastExpr expr = Jimple.v().newCastExpr(op1.toImmediate(), t);
          val = expr;
          break;
        }
        case INSTANCEOF: {
          InstanceOfExpr expr = Jimple.v().newInstanceOfExpr(op1.toImmediate(), t);
          val = expr;
          break;
        }
        default:
          throw new UnsupportedOperationException("Unknown type op: " + op);
      }
    }
    Operand opr = new Operand(insn, val, this);
    merging.mergeOutput(opr);
    operandStack.push(opr);
  }

  private void convertVarLoadInsn(VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LLOAD || op == DLOAD;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand opr = new Operand(insn, getLocal(insn.var), this);
    merging.mergeOutput(opr);
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertVarStoreInsn(VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LSTORE || op == DSTORE;
    OperandMerging merging = operandStack.getOrCreateMerging(insn);
    Operand opr = dword ? operandStack.popDual() : operandStack.pop();
    merging.mergeInputs(opr);
    Local local = getLocal(insn.var);
    Stmt as;
    if (opr.stackLocal == null) {
      // Can skip creating a new stack local for the operand
      // and store the value in the local directly.
      as = Jimple.v().newAssignStmt(local, opr.value);
      // TODO check that this works correctly with the merging
      opr.stackLocal = local;
      setUnit(opr.insn, as);
    } else if (opr.stackLocal != local) {
      as = Jimple.v().newAssignStmt(local, opr.toImmediate());
      setUnit(insn, as);
    }
    // The `local` has just been assigned a new value,
    // but an operand with `value == local` might still be on the stack.
    // That operand should use the old value,
    // so the following call adds a `$stackLocalX = $local` statement
    // to persist the old value when necessary.
    addReadOperandAssignments(local);
  }

  /* Conversion */

  private void convertLabel(LabelNode ln) {
    if (startTrapHandler.containsKey(ln)) {
      activeTrapHandlers.add(startTrapHandler.get(ln));
    }

    if (endTrapHandler.containsKey(ln)) {
      activeTrapHandlers.remove(endTrapHandler.get(ln));
    }

    // only do it for Labels which are referring to a traphandler
    if (!trapHandlers.containsKey(ln)) {
      return;
    }

    // We create a nop statement as a placeholder so that we can jump
    // somewhere from the real exception handler in case this is inline
    // code
    if (inlineExceptionLabels.contains(ln)) {
      if (!insnToStmt.containsKey(ln)) {
        NopStmt nop = Jimple.v().newNopStmt();
        setUnit(ln, nop);
      }
      return;
    }

    OperandMerging merging = operandStack.getOrCreateMerging(ln);
    CaughtExceptionRef ref = Jimple.v().newCaughtExceptionRef();
    Operand opr = new Operand(ln, ref, this);
    merging.mergeOutput(opr);
    if (opr.stackLocal == null) {
      opr.stackLocal = newStackLocal();
    }
    IdentityStmt as = Jimple.v().newIdentityStmt(opr.stackLocal, ref);
    setUnit(ln, as);
    operandStack.push(opr);
  }

  // inline exceptionhandler := exceptionhandler thats reachable through unexceptional "normal" flow
  // and exceptional flow
  private void indexInlineExceptionHandlers() {
    final Set<LabelNode> handlerLabelNodes = trapHandlers.keySet();

    if (handlerLabelNodes.isEmpty()) {
      // my job is done here
      return;
    }

    for (AbstractInsnNode node : instructions) {
      if (node instanceof JumpInsnNode) {
        final LabelNode handlerLabel = ((JumpInsnNode) node).label;
        if (handlerLabelNodes.contains(handlerLabel)) {
          inlineExceptionLabels.add(handlerLabel);
        }
      } else if (node instanceof LookupSwitchInsnNode) {

        final LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) node;
        if (handlerLabelNodes.contains(lookupSwitchInsnNode.dflt)) {
          inlineExceptionLabels.add(lookupSwitchInsnNode.dflt);
          continue;
        }
        for (LabelNode l : lookupSwitchInsnNode.labels) {
          if (handlerLabelNodes.contains(l)) {
            inlineExceptionLabels.add(l);
            break;
          }
        }
      } else if (node instanceof TableSwitchInsnNode) {

        final TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) node;
        if (handlerLabelNodes.contains(tableSwitchInsnNode.dflt)) {
          inlineExceptionLabels.add(tableSwitchInsnNode.dflt);
          continue;
        }
        for (LabelNode l : tableSwitchInsnNode.labels) {
          if (handlerLabelNodes.contains(l)) {
            inlineExceptionLabels.add(l);
            break;
          }
        }
      }
    }
  }

  /* Conversion */
  private void addEdges(Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges,
      ArrayDeque<BranchedInsnInfo> conversionWorklist, AbstractInsnNode branchingInsn, /*
                                                                                        * branching instruction node
                                                                                        */
      AbstractInsnNode tgt, /* "default" targets i.e. LabelNode or fallsthrough "target" of if */
      List<LabelNode> tgts /* other branch target(s) */) {
    Operand[] stackss = operandStack.getStack().toArray(new Operand[0]);
    /* iterate over possible following/successing instructions which is: combined(tgt, tgts) */
    int i = 0;
    int lastIdx = tgts.size();
    outer_loop: do {
      BranchedInsnInfo edge = edges.get(branchingInsn, tgt);
      if (edge == null) {
        // [ms] check why this edge could be already there
        edge = new BranchedInsnInfo(tgt, operandStack.getStack(), activeTrapHandlers);
        edge.addToPrevStack(stackss);
        edges.put(branchingInsn, tgt, edge);
        conversionWorklist.add(edge);
        continue;
      }
      for (List<Operand> stackTemp : edge.getOperandStacks()) {
        if (stackTemp.size() == stackss.length) {
          int j = 0;
          while (j < stackss.length && stackTemp.get(j).equivTo(stackss[j])) {
            j++;
          }
          if (j == stackss.length) {
            continue outer_loop;
          }
        }
      }
      final LinkedList<Operand[]> prevStacks = edge.getPrevStacks();
      for (Operand[] ps : prevStacks) {
        if (Arrays.equals(ps, stackss)) {
          continue outer_loop;
        }
      }
      edge.addOperandStack(operandStack.getStack());
      edge.addToPrevStack(stackss);
      conversionWorklist.add(edge);
    } while (i < lastIdx && (tgt = tgts.get(i++)) != null);
  }

  private void convert() {
    if (instructions == null || instructions.size() == 0) {
      return;
    }
    ArrayDeque<BranchedInsnInfo> worklist = new ArrayDeque<>();
    indexInlineExceptionHandlers();

    // If this label is reachable through an exception and through normal
    // code, we have to split the exceptional case (with the exception on
    // the stack) from the normal fall-through case without anything on the
    // stack.
    for (LabelNode handlerNode : trapHandlers.keySet()) {
      if (inlineExceptionLabels.contains(handlerNode)) {
        // Catch the exception
        CaughtExceptionRef ref = Jimple.v().newCaughtExceptionRef();
        Local local = newStackLocal();
        IdentityStmt as = Jimple.v().newIdentityStmt(local, ref);

        Operand opr = new Operand(handlerNode, ref, this);
        opr.stackLocal = local;

        worklist.add(new BranchedInsnInfo(handlerNode, Collections.singletonList(opr), activeTrapHandlers));

        // Save the statements
        inlineExceptionHandlers.put(handlerNode, as);
      } else {
        worklist.add(new BranchedInsnInfo(handlerNode, new ArrayList<>(), activeTrapHandlers));
      }
    }
    worklist.add(new BranchedInsnInfo(instructions.getFirst(), Collections.emptyList(), activeTrapHandlers));
    Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges = HashBasedTable.create(1, 1);
    if (Options.v().keep_line_number()) {
      setLineNumberMap();
    }
    if (Options.v().keep_offset()) {
      setBytecodeOffsetMap();
    }

    do {
      BranchedInsnInfo edge = worklist.pollLast();
      AbstractInsnNode insn = edge.getInsn();
      operandStack.setOperandStack(new ArrayList<>(edge.getOperandStacks().get(edge.getOperandStacks().size() - 1)));
      activeTrapHandlers = edge.getActiveTrapHandlers();

      insnLoop: do {
        int type = insn.getType();
        switch (type) {
          case FIELD_INSN:
            convertFieldInsn((FieldInsnNode) insn);
            continue;
          case IINC_INSN:
            convertIincInsn((IincInsnNode) insn);
            continue;
          case INSN:
            convertInsn((InsnNode) insn);
            int op = insn.getOpcode();
            if ((op >= IRETURN && op <= RETURN) || op == ATHROW) {
              break insnLoop;
            }
            continue;
          case INT_INSN:
            convertIntInsn((IntInsnNode) insn);
            continue;
          case LDC_INSN:
            convertLdcInsn((LdcInsnNode) insn);
            continue;
          case JUMP_INSN:
            JumpInsnNode jmp = (JumpInsnNode) insn;
            convertJumpInsn(jmp);
            op = jmp.getOpcode();
            if (op == JSR) {
              throw new UnsupportedOperationException("JSR!");
            }
            if (op != GOTO) {
              /* ifX opcode, i.e. two successors */
              AbstractInsnNode next = insn.getNext();
              addEdges(edges, worklist, insn, next, Collections.singletonList(jmp.label));
            } else {
              addEdges(edges, worklist, insn, jmp.label, Collections.emptyList());
            }
            break insnLoop;
          case LOOKUPSWITCH_INSN:
            LookupSwitchInsnNode swtch = (LookupSwitchInsnNode) insn;
            convertLookupSwitchInsn(swtch);
            LabelNode dflt = swtch.dflt;
            addEdges(edges, worklist, insn, dflt, swtch.labels);
            break insnLoop;
          case METHOD_INSN:
            convertMethodInsn((MethodInsnNode) insn);
            continue;
          case INVOKE_DYNAMIC_INSN:
            convertInvokeDynamicInsn((InvokeDynamicInsnNode) insn);
            continue;
          case MULTIANEWARRAY_INSN:
            convertMultiANewArrayInsn((MultiANewArrayInsnNode) insn);
            continue;
          case TABLESWITCH_INSN:
            TableSwitchInsnNode tswtch = (TableSwitchInsnNode) insn;
            convertTableSwitchInsn(tswtch);
            LabelNode ldflt = tswtch.dflt;
            addEdges(edges, worklist, insn, ldflt, tswtch.labels);
            break insnLoop;
          case TYPE_INSN:
            convertTypeInsn((TypeInsnNode) insn);
            continue;
          case VAR_INSN:
            if (insn.getOpcode() == RET) {
              throw new UnsupportedOperationException("RET!");
            }
            convertVarInsn((VarInsnNode) insn);
            continue;
          case LABEL:
            convertLabel((LabelNode) insn);
            continue;
          case LINE:
            // was already handled in setLineNumberMap()
            continue;
          case BytecodeOffsetNode.BYTECODE_OFFSET_TYPE:
            if (insn instanceof BytecodeOffsetNode) {
              // was already handled in setBytecodeOffsetMap()
              break;
            }
          case FRAME:
            // we can ignore it
            continue;
          default:
            throw new RuntimeException("Unknown instruction type: " + type);
        }
      } while ((insn = insn.getNext()) != null);
    } while (!worklist.isEmpty());
    worklist = null;
    edges = null;
  }

  private void convertVarInsn(VarInsnNode insn) {
    int op = insn.getOpcode();
    if (op >= ILOAD && op <= ALOAD) {
      convertVarLoadInsn(insn);
    } else if (op >= ISTORE && op <= ASTORE) {
      convertVarStoreInsn(insn);
    } else if (op == RET) {
      /* we handle it, even though it should be removed */
      if (!insnToStmt.containsKey(insn)) {
        setUnit(insn, Jimple.v().newRetStmt(getLocal(insn.var)));
      }
    } else {
      throw new UnsupportedOperationException("Unknown var op: " + op);
    }
  }

  // For potential future debugging efforts
  private static String getInstructionAsString(AbstractInsnNode n) {
    Textifier textifier = new Textifier();
    TraceMethodVisitor tm = new TraceMethodVisitor(textifier);
    n.accept(tm);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (PrintWriter pw = new PrintWriter(bos)) {
      textifier.print(pw);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8).trim();
  }

  private void setLineNumberMap() {
    if (locationMap == null) {
      locationMap = new HashMap<>(instructions.size() * 2 + 1);
    }

    AbstractInsnNode current = instructions.getFirst();

    int lastNumber = -1;
    while (current != null) {
      if (current.getType() == LINE) {
        LineNumberNode ln = (LineNumberNode) current;
        lastNumber = ln.line;
      } else if (lastNumber >= 0) {
        locationMap.computeIfAbsent(current, (x) -> new LocationMetadata()).lineNumber = lastNumber;
      }
      current = current.getNext();
    }
  }

  private void setBytecodeOffsetMap() {
    if (locationMap == null) {
      locationMap = new HashMap<>(instructions.size() * 2 + 1);
    }
    AbstractInsnNode current = instructions.getFirst();

    int lastOffset = -1;
    while (current != null) {
      if (current instanceof BytecodeOffsetNode) {
        BytecodeOffsetNode ln = (BytecodeOffsetNode) current;
        lastOffset = ln.bytecodeOffset;
      } else if (lastOffset >= 0) {
        locationMap.computeIfAbsent(current, (x) -> new LocationMetadata()).bytecodeOffset = lastOffset;
      }
      current = current.getNext();
    }
  }

  private void emitLocals() {
    JimpleBody jb = body;
    SootMethod m = jb.getMethod();
    Collection<Local> jbl = jb.getLocals();
    Collection<Unit> jbu = jb.getUnits();
    int iloc = 0;
    if (!m.isStatic()) {
      Local l = getLocal(iloc++);
      jbu.add(Jimple.v().newIdentityStmt(l, Jimple.v().newThisRef(m.getDeclaringClass().getType())));
    }
    int nrp = 0;
    for (Object ot : m.getParameterTypes()) {
      Type t = (Type) ot;
      Local l = getLocal(iloc);
      jbu.add(Jimple.v().newIdentityStmt(l, Jimple.v().newParameterRef(t, nrp++)));
      if (AsmUtil.isDWord(t)) {
        iloc += 2;
      } else {
        iloc++;
      }
    }
    for (Local l : locals.values()) {
      jbl.add(l);
    }
  }

  private void emitTraps() {
    Chain<Trap> traps = body.getTraps();
    SootClass throwable = Scene.v().getSootClass("java.lang.Throwable");
    Map<LabelNode, Iterator<UnitBox>> handlers = new LinkedHashMap<LabelNode, Iterator<UnitBox>>(tryCatchBlocks.size());
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      UnitBox start = Jimple.v().newStmtBox(null);
      UnitBox end = Jimple.v().newStmtBox(null);
      Iterator<UnitBox> hitr = handlers.get(tc.handler);
      if (hitr == null) {
        hitr = trapHandlers.get(tc.handler).iterator();
        handlers.put(tc.handler, hitr);
      }
      UnitBox handler = hitr.next();
      SootClass cls = tc.type == null ? throwable : getClassFromScene(AsmUtil.toQualifiedName(tc.type));
      Trap trap = Jimple.v().newTrap(cls, start, end, handler);
      traps.add(trap);
      labels.put(tc.start, start);
      labels.put(tc.end, end);
    }
  }

  private static class UnitContainerWorklistElement {
    UnitContainer u;
    int position;

    public UnitContainerWorklistElement(UnitContainer u) {
      this.u = u;
    }

  }

  static void emitUnits(Unit u, UnitPatchingChain chain) {
    if (u instanceof UnitContainer) {
      Stack<UnitContainerWorklistElement> stack = new Stack<>();
      stack.push(new UnitContainerWorklistElement((UnitContainer) u));
      processStack: while (!stack.isEmpty()) {
        UnitContainerWorklistElement r = stack.peek();
        for (int i = r.position; i < r.u.units.length; i++) {
          r.position = i + 1;
          Unit e = r.u.units[i];
          if (e instanceof UnitContainer) {
            stack.push(new UnitContainerWorklistElement((UnitContainer) e));
            continue processStack;
          } else {
            chain.add(e);
          }
        }
        if (stack.pop() != r) {
          throw new AssertionError("Not expected element");
        }

      }
    } else {
      chain.add(u);
    }
  }

  private void emitUnits() {
    AbstractInsnNode insn = instructions.getFirst();
    ArrayDeque<LabelNode> labls = new ArrayDeque<LabelNode>();

    while (insn != null) {
      // Save the label to assign it to the next real unit
      if (insn instanceof LabelNode) {
        labls.add((LabelNode) insn);
      }

      // Get the unit associated with the current instruction
      Unit u = insnToStmt.get(insn);
      if (u == null) {
        insn = insn.getNext();
        continue;
      }

      emitUnits(u, body.getUnits());

      // If this is an exception handler, register the starting unit for it
      {
        IdentityStmt caughtEx = null;
        if (u instanceof IdentityStmt) {
          caughtEx = (IdentityStmt) u;
        } else if (u instanceof UnitContainer) {
          caughtEx = getIdentityRefFromContrainer((UnitContainer) u);
        }

        if (insn instanceof LabelNode && caughtEx != null && caughtEx.getRightOp() instanceof CaughtExceptionRef) {
          // We directly place this label
          Collection<UnitBox> traps = trapHandlers.get((LabelNode) insn);
          for (UnitBox ub : traps) {
            ub.setUnit(caughtEx);
          }
        }
      }

      // Register this unit for all targets of the labels ending up at it
      while (!labls.isEmpty()) {
        LabelNode ln = labls.poll();
        Collection<UnitBox> boxes = labels.get(ln);
        if (boxes != null) {
          for (UnitBox box : boxes) {
            box.setUnit(u instanceof UnitContainer ? ((UnitContainer) u).getFirstUnit() : u);
          }
        }
      }
      insn = insn.getNext();
    }

    // Emit the inline exception handlers
    for (LabelNode ln : this.inlineExceptionHandlers.keySet()) {
      Unit handler = this.inlineExceptionHandlers.get(ln);
      emitUnits(handler, body.getUnits());

      Collection<UnitBox> traps = trapHandlers.get(ln);
      for (UnitBox ub : traps) {
        ub.setUnit(handler);
      }

      // We need to jump to the original implementation
      Unit targetUnit = insnToStmt.get(ln);
      GotoStmt gotoImpl = Jimple.v().newGotoStmt(targetUnit);
      body.getUnits().add(gotoImpl);
    }

    /* set remaining labels & boxes to last unit of chain */
    if (labls.isEmpty()) {
      return;
    }
    Unit end = Jimple.v().newNopStmt();
    body.getUnits().add(end);
    while (!labls.isEmpty()) {
      LabelNode ln = labls.poll();
      Collection<UnitBox> boxes = labels.get(ln);
      if (boxes != null) {
        for (UnitBox box : boxes) {
          box.setUnit(end);
        }
      }
    }
  }

  private IdentityStmt getIdentityRefFromContrainer(UnitContainer u) {
    for (Unit uu : u.units) {
      if (uu instanceof IdentityStmt) {
        return (IdentityStmt) uu;
      } else if (uu instanceof UnitContainer) {
        return getIdentityRefFromContrainer((UnitContainer) uu);
      }
    }
    return null;
  }

  @Override
  public Body getBody(SootMethod m, String phaseName) {
    if (!m.isConcrete()) {
      return null;
    }
    final Jimple jimp = Jimple.v();
    final JimpleBody jb = jimp.newBody(m);
    if (instructions == null || instructions.size() == 0) {
      logger.warn(m.getSignature() + " has no instructions");

      Util.emptyBody(jb);
      Util.addExceptionAfterUnit(jb, "java.lang.RuntimeException", jb.getUnits().getLast(),
          "Soot has detected that this method has no instructions. The JVM would throw an exception when loading the class");
      TypeAssigner.v().transform(jb);
      return jb;
    }
    /* initialize */
    int nrInsn = instructions.size();
    nextLocal = maxLocals;
    locals = new LinkedHashMap<Integer, JimpleLocal>(maxLocals + (maxLocals / 2));
    labels = LinkedListMultimap.create();

    insnToStmt = new LinkedHashMap<AbstractInsnNode, Unit>(nrInsn);
    trapHandlers = LinkedListMultimap.create(tryCatchBlocks.size());
    operandStack = new OperandStack(this, instructions.size());
    body = jb;
    /* retrieve all trap handlers */
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      trapHandlers.put(tc.handler, jimp.newStmtBox(null));
      startTrapHandler.put(tc.start, tc);
      endTrapHandler.put(tc.end, tc);
    }
    /* convert instructions */
    try {
      convert();
    } catch (Throwable t) {
      throw new RuntimeException("Failed to convert " + m, t);
    }

    /* build body (add units, locals, traps, etc.) */
    emitLocals();
    emitTraps();
    emitUnits();

    if (PhaseOptions.getBoolean(PhaseOptions.v().getPhaseOptions("jb"), "use-original-names")) {
      tryCorrectingLocalNames(jimp, jb);
    }

    /* clean up */
    locals = null;
    labels = null;
    insnToStmt = null;
    body = null;
    locationMap = null;
    lambdaMethodRefsMap = null;

    // We want to have somewhat correct ordering of the locals
    // (the asm backend's code depends on this)
    Set<Local> seenLocals = new HashSet<>();
    jb.getLocals().clear();
    for (Unit i : jb.getUnits()) {
      Iterator<ValueBox> it = i.getUseAndDefBoxesIterator();
      while (it.hasNext()) {
        ValueBox vb = it.next();
        Value v = vb.getValue();
        if (v instanceof Local && seenLocals.add((Local) v)) {
          Local l = (Local) v;
          jb.getLocals().add(l);
        }
      }
    }

    if (!"false".equalsIgnoreCase(PhaseOptions.v().getPhaseOptions("jb.cp").get("enabled"))) {
      CopyPropagator.v().transform(jb);
      ConditionalBranchFolder.v().transform(jb);
    }

    // We can have cases where the Java compiler inserts unnecessary traps, which might cause problems later in typing
    new TrapTightener(PedanticThrowAnalysis.v()).transform(jb);
    if (!"false".equalsIgnoreCase(PhaseOptions.v().getPhaseOptions("jb.uce").get("enabled"))) {
      UnreachableCodeEliminator.v().transform(jb);
    }

    NopEliminator.v().transform(jb);

    // Make sure to inline patterns of the form to enable proper variable
    // splitting and type assignment:
    // a = new A();
    // goto l0;
    // l0:
    // b = (B) a;
    // return b;
    castAndReturnInliner.transform(jb);
    if (!"false".equalsIgnoreCase(PhaseOptions.v().getPhaseOptions("jb.dae").get("enabled"))) {
      DeadAssignmentEliminator.v().transform(jb);
    }

    try {
      PackManager.v().getPack("jb").apply(jb);
    } catch (Throwable t) {
      throw new RuntimeException("Failed to apply jb to " + m, t);
    }
    TrapTightener.removeInvalidTraps(jb);
    LocalPacker.v().transform(jb);
    if (!"false".equalsIgnoreCase(PhaseOptions.v().getPhaseOptions("jb.dae").get("enabled"))) {
      DeadAssignmentEliminator.v().transform(jb);
    }
    UnconditionalBranchFolder.v().transform(jb);

    jb.ensureUniqueLocalNames();

    return jb;
  }

  /**
   * When preserving original names, try to use the local variable table for guidance. The LocalVariableTable from the input
   * bytecode may contain two weird cases which can cause the loss of original local names, or worse, the appearance of the
   * '#' character in local names in the output LocalVariableTable (some JVM implementations will give an error when trying
   * to execute a method whose LocalVariableTable contains names with the '#' character).
   * <ol>
   * <li>When the LocalVariableTable associates different names with the same local variable index at different points in the
   * method body, the "locals" Map would end up preserving only one of those names as the designated local name for that
   * index. This leaves it up to the SharedInitializationLocalSplitter and LocalSplitter to then split that single Local back
   * into distinct Locals, but at that time, information about the other original name(s) has been ignored (and the
   * LocalVariableTable which contains that information is no longer available) so the best it can do is append "#x" (where x
   * is a unique integer) to the end of the current name. In the end, those locals may be combined back into a single Local
   * by the LocalPacker using whichever name was originally chosen by the "locals" Map here. In the worst case however, the
   * LocalPacker cannot combine them back into a single Local (see the "Icky fix" in LocalPacker) and ends up keeping the '#'
   * character in the Local name which leads to a problem if the "write-local-annotations" Soot option is also because the
   * names containing a '#' character will end up in the output bytecode.</li>
   * <li>When the LocalVariableTable associates different indices with the same name at the same code location, we end up
   * again with a case where the LocalPacker cannot remove the '#' character from local names.</li>
   * </ol>
   *
   * Thus, this method checks for these ambiguous cases while the LocalVariableTable is still available, and assigns a unique
   * name to each local that is based on the original name from the LocalVariableTable and does not use the '#' character.
   */
  protected void tryCorrectingLocalNames(final Jimple jimp, final JimpleBody jb) {
    final Chain<Local> jbLocals = jb.getLocals();
    final int sizeLVT = this.localVars.size();
    if (sizeLVT > 0) {
      // Group LocalVariableNode by index to find any that are associated with
      // different names at different points in the method. For each such
      // occurrence, determine which name was chosen via "locals.get(i)" and,
      // in the range of Units specified for all other names, replace that
      // chosen Local with a new Local.
      Multimap<Integer, LocalVariableNode> groups = LinkedListMultimap.create(sizeLVT);
      for (LocalVariableNode lvn : this.localVars) {
        if (lvn.start != lvn.end) { // these are ignored by getLocal(int)
          groups.put(lvn.index, lvn);
        }
      }
      // NOTE: When creating new variables, group by both name and index because
      // the LocalVariableTable allows multiple local variable indices to
      // have the same name simultaneously but they must be distinguished here.
      final Chain<Unit> jbUnits = jb.getUnits();
      Table<Integer, String, JimpleLocal> newLocals = null;
      for (Map.Entry<Integer, Collection<LocalVariableNode>> e : groups.asMap().entrySet()) {
        Collection<LocalVariableNode> lvns = e.getValue();
        if (lvns.size() > 1) {
          final Integer localNum = e.getKey();

          // Skip this index if it has not been referenced by a converted instruction
          if (!this.locals.containsKey(localNum)) {
            continue;
          }

          final JimpleLocal chosen = this.locals.get(localNum);
          final String chosenName = chosen.getName();
          final Type chosenType = chosen.getType();
          // Detect inconsistencies in the LocalVariableTable.
          // 1. If there exists any use of local variable 'chosen' outside of a
          // range defined by one of the LocalVariableNode in 'vals', then it is
          // not safe to make any replacements of 'chosen' because it is not
          // clear which actual variable should be used at a location outside of
          // the defined ranges (unless a use-def analysis is applied but that
          // is left for future implementation).
          // 2. If any of the LocalVariableNode in 'vals' cover any of the same
          // units, then they are ambiguous and cannot be used.
          //
          // To implement these checks, first collect all ValueBoxes in the body
          // that reference the chosen Local. Then, as each LocalVariableNode is
          // processed, map each ValueBox to the new Local that it should hold.
          // If any ValueBox is found more than once or not found at all, then
          // one of the inconsistency cases mentioned above exists and thus no
          // changes should be made.
          IdentityHashMap<ValueBox, Local> boxToNewLoc = new IdentityHashMap<>();
          for (Unit u : jbUnits) {
            for (Iterator<ValueBox> iterator = u.getUseAndDefBoxesIterator(); iterator.hasNext();) {
              ValueBox box = iterator.next();
              Value val = box.getValue();
              if (val == chosen) {
                Local old = boxToNewLoc.put(box, null);
                assert (old == null);// each box appears only once
              }
            }
          }
          boolean isConsistent = true;
          LV_LOOP: for (LocalVariableNode lvn : lvns) {
            final String name = lvn.name;
            if (!chosenName.equals(name)) {
              // Get the next real instruction after 'start'
              // NOTE: Although it seems obvious to use lvn.start.getNext() as
              // the initial instruction to check, the bytecode generated by
              // some compilers has the start PC one instruction late it seems.
              Unit uStart;
              for (AbstractInsnNode i = lvn.start.getPrevious(); (uStart = insnToStmt.get(i)) == null && i != null;) {
                i = i.getNext();
              }
              if (uStart instanceof UnitContainer) {
                uStart = ((UnitContainer) uStart).getFirstUnit();
              }
              // Get the previous real instruction before 'end'
              Unit uEnd;
              for (AbstractInsnNode i = lvn.end.getPrevious(); (uEnd = insnToStmt.get(i)) == null && i != null;) {
                i = i.getPrevious();
              }
              if (uEnd instanceof UnitContainer) {
                uEnd = ((UnitContainer) uEnd).getFirstUnit();
              }
              if (newLocals == null) {
                newLocals = HashBasedTable.create(this.maxLocals, 1);
              }
              JimpleLocal newLocal = newLocals.get(localNum, name);
              if (newLocal == null) {
                newLocal = jimp.newLocal(name, chosenType);
                newLocal.setUserDefinedLocal();
                Local old = newLocals.put(localNum, name, newLocal);
                assert (old == null);
              }
              for (Iterator<Unit> it = jbUnits.iterator(uStart, uEnd); it.hasNext();) {
                Unit u = it.next();
                for (Iterator<ValueBox> iterator = u.getUseAndDefBoxesIterator(); iterator.hasNext();) {
                  ValueBox box = iterator.next();
                  Value val = box.getValue();
                  if (val == chosen) {
                    assert (boxToNewLoc.containsKey(box));// it was found at the start
                    Local conflict = boxToNewLoc.put(box, newLocal);
                    if (conflict != null) {
                      isConsistent = false;
                      break LV_LOOP;
                    }
                  }
                }
              }
            }
          }
          // Finally, replace the locals only if both consistency conditions pass.
          HashSet<Local> newLocalSet = new HashSet<>(boxToNewLoc.values());
          if (isConsistent && !newLocalSet.contains(null)) {
            jbLocals.addAll(newLocalSet);
            for (Map.Entry<ValueBox, Local> r : boxToNewLoc.entrySet()) {
              r.getKey().setValue(r.getValue());
            }
          }
        }
      }
    }
    // In the end, ensure the names of locals (not just from those that were newly added) are unique.
    jb.ensureUniqueLocalNames();
  }

  /**
   * Updates the identity statement of an inline exception handler to use a new local variable. This is needed when operand
   * merging assigns a common stack local to the caught exception operand.
   *
   * @param insn
   *          the handler label node
   * @param newLocal
   *          the new local to assign the caught exception to
   */
  void updateInlineExceptionHandler(AbstractInsnNode insn, Local newLocal) {
    LabelNode labelNode = (LabelNode) insn;
    IdentityStmt oldStmt = inlineExceptionHandlers.get(labelNode);
    if (oldStmt != null) {
      IdentityStmt newStmt = Jimple.v().newIdentityStmt(newLocal, oldStmt.getRightOp());
      inlineExceptionHandlers.put(labelNode, newStmt);
    }
  }

  @SuppressWarnings("unchecked")
  <A extends Stmt> A getStmt(AbstractInsnNode insn) {
    return (A) insnToStmt.get(insn);
  }

  void replace(Value old, Local newStackLocal) {
    for (Unit i : insnToStmt.values()) {
      Iterator<ValueBox> it = i.getUseAndDefBoxesIterator();
      while (it.hasNext()) {
        ValueBox vb = it.next();
        if (vb.getValue() == old) {
          vb.setValue(newStackLocal);
        }
      }
    }

  }
}
