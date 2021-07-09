package soot.baf;

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

import static soot.util.backend.ASMBackendUtils.sizeOfType;
import static soot.util.backend.ASMBackendUtils.slashify;
import static soot.util.backend.ASMBackendUtils.toTypeDesc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import soot.AbstractASMBackend;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.NullType;
import soot.PolymorphicMethodRef;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.StmtAddressType;
import soot.Trap;
import soot.Type;
import soot.TypeSwitch;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.baf.internal.BafLocal;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.ConstantSwitch;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IdentityRef;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.MethodHandle;
import soot.jimple.MethodType;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

/**
 * Concrete ASM based bytecode generation backend for the BAF intermediate representation
 *
 * @author Tobias Hamann, Florian Kuebler, Dominik Helm, Lukas Sommer, Alex Bertram, Andreas Dann
 */
public class BafASMBackend extends AbstractASMBackend {

  // Contains one Label for every Unit that is the target of a branch or jump
  protected final Map<Unit, Label> branchTargetLabels = new HashMap<Unit, Label>();

  // Contains a mapping of local variables to indices in the local variable stack
  protected final Map<Local, Integer> localToSlot = new HashMap<Local, Integer>();

  /**
   * Creates a new BafASMBackend with a given enforced java version
   *
   * @param sc
   *          The SootClass the bytecode is to be generated for
   * @param javaVersion
   *          A particular Java version enforced by the user, may be 0 for automatic detection, must not be lower than
   *          necessary for all features used
   */
  public BafASMBackend(SootClass sc, int javaVersion) {
    super(sc, javaVersion);
  }

  /**
   * Returns the ASM Label for a given Unit that is the target of a branch or jump
   *
   * @param target
   *          The unit that is the branch target
   * @return The Label that specifies this unit
   */
  protected Label getBranchTargetLabel(Unit target) {
    return branchTargetLabels.get(target);
  }

  /*
   * (non-Javadoc)
   *
   * @see soot.AbstractASMBackend#getMinJavaVersion(soot.SootMethod)
   */
  @Override
  protected int getMinJavaVersion(SootMethod method) {
    final BafBody body = getBafBody(method);
    int minVersion = Options.java_version_1_1;

    // http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/87ee5ee27509/src/share/vm/classfile/classFileParser.cpp
    if (method.getDeclaringClass().isInterface()) {
      if (!method.isAbstract()) { // any non-abstract method in an interface requires 1.8
        minVersion = Math.max(minVersion, Options.java_version_1_8);
      }
    }

    for (Unit u : body.getUnits()) {
      if (minVersion >= Options.java_version_1_9) {
        // Stop early since no case below goes above 1.9
        break;
      }
      if (u instanceof DynamicInvokeInst) {
        minVersion = Math.max(minVersion, Options.java_version_1_7);
      } else if (u instanceof SpecialInvokeInst) {
        // INVOKESPECIAL can't be used with interfaces prior to Java 8
        if (((SpecialInvokeInst) u).getMethodRef().getDeclaringClass().isInterface()) {
          minVersion = Math.max(minVersion, Options.java_version_1_8);
        }
      } else if (u instanceof PushInst) {
        Constant constant = ((PushInst) u).getConstant();
        if (constant instanceof ClassConstant) {
          minVersion = Math.max(minVersion, Options.java_version_1_5);
        }
        String typeString = constant.getType().toQuotedString();
        if (typeString != null) {
          switch (typeString) {
            case PolymorphicMethodRef.VARHANDLE_SIGNATURE:
              minVersion = Math.max(minVersion, Options.java_version_1_9);
              break;
            case PolymorphicMethodRef.METHODHANDLE_SIGNATURE:
              minVersion = Math.max(minVersion, Options.java_version_1_7);
              break;
          }
        }
      }
    }

    return minVersion;
  }

  /*
   * (non-Javadoc)
   *
   * @see soot.AbstractASMBackend#generateMethodBody(org.objectweb.asm. MethodVisitor, soot.SootMethod)
   */
  @Override
  protected void generateMethodBody(MethodVisitor mv, SootMethod method) {
    final BafBody body = getBafBody(method);

    /*
     * Create a label for each instruction that is the target of some branch
     */
    for (UnitBox box : body.getUnitBoxes(true)) {
      Unit u = box.getUnit();
      if (!branchTargetLabels.containsKey(u)) {
        branchTargetLabels.put(u, new Label());
      }
    }

    Label startLabel = null;
    if (Options.v().write_local_annotations()) {
      startLabel = new Label();
      mv.visitLabel(startLabel);
    }

    /*
     * Handle all TRY-CATCH-blocks
     */
    for (Trap trap : body.getTraps()) {
      // Check if the try-block contains any statement
      if (trap.getBeginUnit() != trap.getEndUnit()) {
        Label start = branchTargetLabels.get(trap.getBeginUnit());
        Label end = branchTargetLabels.get(trap.getEndUnit());
        Label handler = branchTargetLabels.get(trap.getHandlerUnit());
        String type = slashify(trap.getException().getName());
        mv.visitTryCatchBlock(start, end, handler, type);
      }
    }

    /*
     * Handle local variable slots for the "this"-local and the parameters. For non-static methods the first parameters and
     * zero-slot is the "this"-local.
     */
    int localCount = method.isStatic() ? 0 : 1;
    int[] paramSlots = new int[method.getParameterCount()];
    for (int i = 0, e = paramSlots.length; i < e; ++i) {
      paramSlots[i] = localCount;
      localCount += sizeOfType(method.getParameterType(i));
    }

    Set<Local> assignedLocals = new HashSet<Local>();
    Chain<Unit> instructions = body.getUnits();
    for (Unit u : instructions) {
      if (u instanceof IdentityInst) {
        Value leftOp = ((IdentityInst) u).getLeftOp();
        if (leftOp instanceof Local) {
          int slot = 0;
          IdentityRef identity = (IdentityRef) ((IdentityInst) u).getRightOp();
          if (identity instanceof ThisRef) {
            if (method.isStatic()) {
              throw new RuntimeException("Attempting to use 'this' in static method");
            }
          } else if (identity instanceof ParameterRef) {
            slot = paramSlots[((ParameterRef) identity).getIndex()];
          } else {
            // Exception ref. Skip over this
            continue;
          }
          Local l = (Local) leftOp;
          localToSlot.put(l, slot);
          assignedLocals.add(l);
        }
      }
    }

    for (Local local : body.getLocals()) {
      if (assignedLocals.add(local)) {
        localToSlot.put(local, localCount);
        localCount += sizeOfType(local.getType());
      }
    }

    // Generate the code
    for (Unit u : instructions) {
      if (branchTargetLabels.containsKey(u)) {
        mv.visitLabel(branchTargetLabels.get(u));
      }
      generateTagsForUnit(mv, u);
      generateInstruction(mv, (Inst) u);
    }

    // Generate the local annotations
    if (Options.v().write_local_annotations()) {
      Label endLabel = new Label();
      mv.visitLabel(endLabel);

      for (Local local : body.getLocals()) {
        Integer slot = localToSlot.get(local);
        if (slot != null) {
          BafLocal l = (BafLocal) local;
          Local jimpleLocal = l.getOriginalLocal();
          if (jimpleLocal != null) {
            mv.visitLocalVariable(jimpleLocal.getName(), toTypeDesc(jimpleLocal.getType()), null, startLabel, endLabel,
                slot);
          }
        }
      }
    }
  }

  /**
   * Writes out the information stored in tags associated with the given unit
   * 
   * @param mv
   *          The method visitor for writing out the bytecode
   * @param u
   *          The unit for which to write out the tags
   */
  protected void generateTagsForUnit(MethodVisitor mv, Unit u) {
    LineNumberTag lnt = (LineNumberTag) u.getTag(LineNumberTag.NAME);
    if (lnt != null) {
      Label l;
      if (branchTargetLabels.containsKey(u)) {
        l = branchTargetLabels.get(u);
      } else {
        l = new Label();
        mv.visitLabel(l);
      }
      mv.visitLineNumber(lnt.getLineNumber(), l);
    }
  }

  /**
   * Emits the bytecode for a single Baf instruction
   *
   * @param mv
   *          The ASM MethodVisitor the bytecode is to be emitted to
   * @param inst
   *          The Baf instruction to be converted into bytecode
   */
  protected void generateInstruction(final MethodVisitor mv, Inst inst) {
    inst.apply(new InstSwitch() {

      @Override
      public void caseReturnVoidInst(ReturnVoidInst i) {
        mv.visitInsn(Opcodes.RETURN);
      }

      @Override
      public void caseReturnInst(ReturnInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitInsn(Opcodes.ARETURN);
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.IRETURN);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.IRETURN);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.IRETURN);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DRETURN);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FRETURN);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IRETURN);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LRETURN);
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitInsn(Opcodes.ARETURN);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.IRETURN);
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitInsn(Opcodes.ARETURN);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("Invalid return type " + t.toString());
          }
        });
      }

      @Override
      public void caseNopInst(NopInst i) {
        mv.visitInsn(Opcodes.NOP);
      }

      @Override
      public void caseJSRInst(JSRInst i) {
        mv.visitJumpInsn(Opcodes.JSR, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void casePushInst(PushInst i) {
        Constant c = i.getConstant();
        if (c instanceof IntConstant) {
          int v = ((IntConstant) c).value;
          switch (v) {
            case -1:
              mv.visitInsn(Opcodes.ICONST_M1);
              break;
            case 0:
              mv.visitInsn(Opcodes.ICONST_0);
              break;
            case 1:
              mv.visitInsn(Opcodes.ICONST_1);
              break;
            case 2:
              mv.visitInsn(Opcodes.ICONST_2);
              break;
            case 3:
              mv.visitInsn(Opcodes.ICONST_3);
              break;
            case 4:
              mv.visitInsn(Opcodes.ICONST_4);
              break;
            case 5:
              mv.visitInsn(Opcodes.ICONST_5);
              break;
            default:
              if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.BIPUSH, v);
              } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.SIPUSH, v);
              } else {
                mv.visitLdcInsn(v);
              }
          }
        } else if (c instanceof StringConstant) {
          mv.visitLdcInsn(((StringConstant) c).value);
        } else if (c instanceof ClassConstant) {
          mv.visitLdcInsn(org.objectweb.asm.Type.getType(((ClassConstant) c).getValue()));
        } else if (c instanceof DoubleConstant) {
          double v = ((DoubleConstant) c).value;
          /*
           * Do not emit a DCONST_0 for negative zero, therefore we need the following check.
           */
          if (new Double(v).equals(0.0)) {
            mv.visitInsn(Opcodes.DCONST_0);
          } else if (v == 1) {
            mv.visitInsn(Opcodes.DCONST_1);
          } else {
            mv.visitLdcInsn(v);
          }
        } else if (c instanceof FloatConstant) {
          float v = ((FloatConstant) c).value;
          /*
           * Do not emit a FCONST_0 for negative zero, therefore we need the following check.
           */
          if (new Float(v).equals(0.0f)) {
            mv.visitInsn(Opcodes.FCONST_0);
          } else if (v == 1) {
            mv.visitInsn(Opcodes.FCONST_1);
          } else if (v == 2) {
            mv.visitInsn(Opcodes.FCONST_2);
          } else {
            mv.visitLdcInsn(v);
          }
        } else if (c instanceof LongConstant) {
          long v = ((LongConstant) c).value;
          if (v == 0) {
            mv.visitInsn(Opcodes.LCONST_0);
          } else if (v == 1) {
            mv.visitInsn(Opcodes.LCONST_1);
          } else {
            mv.visitLdcInsn(v);
          }
        } else if (c instanceof NullConstant) {
          mv.visitInsn(Opcodes.ACONST_NULL);
        } else if (c instanceof MethodHandle) {
          Handle handle;
          if (((MethodHandle) c).isMethodRef()) {
            SootMethodRef methodRef = ((MethodHandle) c).getMethodRef();
            handle = new Handle(((MethodHandle) c).getKind(), slashify(methodRef.declaringClass().getName()),
                methodRef.name(), toTypeDesc(methodRef), methodRef.declaringClass().isInterface());
          } else {
            SootFieldRef fieldRef = ((MethodHandle) c).getFieldRef();
            handle = new Handle(((MethodHandle) c).getKind(), slashify(fieldRef.declaringClass().getName()), fieldRef.name(),
                toTypeDesc(fieldRef.type()), fieldRef.declaringClass().isInterface());
          }
          mv.visitLdcInsn(handle);
        } else {
          throw new RuntimeException("unsupported opcode");
        }
      }

      @Override
      public void casePopInst(PopInst i) {
        if (i.getWordCount() == 2) {
          mv.visitInsn(Opcodes.POP2);
        } else {
          mv.visitInsn(Opcodes.POP);
        }
      }

      @Override
      public void caseIdentityInst(IdentityInst i) {
        Value l = i.getLeftOp();
        Value r = i.getRightOp();
        if (r instanceof CaughtExceptionRef && l instanceof Local) {
          mv.visitVarInsn(Opcodes.ASTORE, localToSlot.get((Local) l));
          // asm handles constant opcodes automatically here
        }
      }

      @Override
      public void caseStoreInst(StoreInst i) {
        final int slot = localToSlot.get(i.getLocal());

        i.getOpType().apply(new TypeSwitch() {
          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitVarInsn(Opcodes.ASTORE, slot);
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitVarInsn(Opcodes.ISTORE, slot);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitVarInsn(Opcodes.ISTORE, slot);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitVarInsn(Opcodes.ISTORE, slot);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitVarInsn(Opcodes.DSTORE, slot);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitVarInsn(Opcodes.FSTORE, slot);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitVarInsn(Opcodes.ISTORE, slot);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitVarInsn(Opcodes.LSTORE, slot);
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitVarInsn(Opcodes.ASTORE, slot);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitVarInsn(Opcodes.ISTORE, slot);
          }

          @Override
          public void caseStmtAddressType(StmtAddressType t) {
            throw new RuntimeException("JSR not supported, use recent Java compiler!");
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitVarInsn(Opcodes.ASTORE, slot);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("Invalid local type: " + t);
          }
        });
      }

      @Override
      public void caseGotoInst(GotoInst i) {
        mv.visitJumpInsn(Opcodes.GOTO, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseLoadInst(LoadInst i) {
        final int slot = localToSlot.get(i.getLocal());

        i.getOpType().apply(new TypeSwitch() {
          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitVarInsn(Opcodes.ALOAD, slot);
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitVarInsn(Opcodes.ILOAD, slot);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitVarInsn(Opcodes.ILOAD, slot);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitVarInsn(Opcodes.ILOAD, slot);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitVarInsn(Opcodes.DLOAD, slot);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitVarInsn(Opcodes.FLOAD, slot);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitVarInsn(Opcodes.ILOAD, slot);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitVarInsn(Opcodes.LLOAD, slot);
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitVarInsn(Opcodes.ALOAD, slot);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitVarInsn(Opcodes.ILOAD, slot);
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitVarInsn(Opcodes.ALOAD, slot);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("Invalid local type: " + t);
          }
        });
      }

      @Override
      public void caseArrayWriteInst(ArrayWriteInst i) {
        i.getOpType().apply(new TypeSwitch() {
          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitInsn(Opcodes.AASTORE);
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.BASTORE);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.BASTORE);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.CASTORE);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DASTORE);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FASTORE);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IASTORE);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LASTORE);
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitInsn(Opcodes.AASTORE);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.SASTORE);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("Invalid type: " + t);
          }
        });
      }

      @Override
      public void caseArrayReadInst(ArrayReadInst i) {
        i.getOpType().apply(new TypeSwitch() {
          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitInsn(Opcodes.AALOAD);
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.BALOAD);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.BALOAD);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.CALOAD);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DALOAD);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FALOAD);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IALOAD);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LALOAD);
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitInsn(Opcodes.AALOAD);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.SALOAD);
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitInsn(Opcodes.AALOAD);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("Invalid type: " + t);
          }
        });
      }

      @Override
      public void caseIfNullInst(IfNullInst i) {
        mv.visitJumpInsn(Opcodes.IFNULL, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfNonNullInst(IfNonNullInst i) {
        mv.visitJumpInsn(Opcodes.IFNONNULL, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfEqInst(IfEqInst i) {
        mv.visitJumpInsn(Opcodes.IFEQ, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfNeInst(IfNeInst i) {
        mv.visitJumpInsn(Opcodes.IFNE, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfGtInst(IfGtInst i) {
        mv.visitJumpInsn(Opcodes.IFGT, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfGeInst(IfGeInst i) {
        mv.visitJumpInsn(Opcodes.IFGE, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfLtInst(IfLtInst i) {
        mv.visitJumpInsn(Opcodes.IFLT, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfLeInst(IfLeInst i) {
        mv.visitJumpInsn(Opcodes.IFLE, getBranchTargetLabel(i.getTarget()));
      }

      @Override
      public void caseIfCmpEqInst(final IfCmpEqInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIfCmpNeInst(final IfCmpNeInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseArrayType(ArrayType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseRefType(RefType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseNullType(NullType t) {
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIfCmpGtInst(final IfCmpGtInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIfCmpGeInst(final IfCmpGeInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIfCmpLtInst(final IfCmpLtInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIfCmpLeInst(final IfCmpLeInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, getBranchTargetLabel(i.getTarget()));
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseStaticGetInst(StaticGetInst i) {
        SootFieldRef field = i.getFieldRef();
        mv.visitFieldInsn(Opcodes.GETSTATIC, slashify(field.declaringClass().getName()), field.name(),
            toTypeDesc(field.type()));
      }

      @Override
      public void caseStaticPutInst(StaticPutInst i) {
        SootFieldRef field = i.getFieldRef();
        mv.visitFieldInsn(Opcodes.PUTSTATIC, slashify(field.declaringClass().getName()), field.name(),
            toTypeDesc(field.type()));
      }

      @Override
      public void caseFieldGetInst(FieldGetInst i) {
        SootFieldRef field = i.getFieldRef();
        mv.visitFieldInsn(Opcodes.GETFIELD, slashify(field.declaringClass().getName()), field.name(),
            toTypeDesc(field.type()));
      }

      @Override
      public void caseFieldPutInst(FieldPutInst i) {
        SootFieldRef field = i.getFieldRef();
        mv.visitFieldInsn(Opcodes.PUTFIELD, slashify(field.declaringClass().getName()), field.name(),
            toTypeDesc(field.type()));
      }

      @Override
      public void caseInstanceCastInst(InstanceCastInst i) {
        Type castType = i.getCastType();
        if (castType instanceof RefType) {
          mv.visitTypeInsn(Opcodes.CHECKCAST, slashify(((RefType) castType).getClassName()));
        } else if (castType instanceof ArrayType) {
          mv.visitTypeInsn(Opcodes.CHECKCAST, toTypeDesc(castType));
        }
      }

      @Override
      public void caseInstanceOfInst(InstanceOfInst i) {
        Type checkType = i.getCheckType();
        if (checkType instanceof RefType) {
          mv.visitTypeInsn(Opcodes.INSTANCEOF, slashify(((RefType) checkType).getClassName()));
        } else if (checkType instanceof ArrayType) {
          mv.visitTypeInsn(Opcodes.INSTANCEOF, toTypeDesc(checkType));
        }
      }

      @Override
      public void casePrimitiveCastInst(PrimitiveCastInst i) {
        final Type to = i.getToType();
        i.getFromType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            emitIntToTypeCast();
          }

          @Override
          public void caseByteType(ByteType t) {
            emitIntToTypeCast();
          }

          @Override
          public void caseCharType(CharType t) {
            emitIntToTypeCast();
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            if (IntType.v().equals(to)) {
              mv.visitInsn(Opcodes.D2I);
            } else if (LongType.v().equals(to)) {
              mv.visitInsn(Opcodes.D2L);
            } else if (FloatType.v().equals(to)) {
              mv.visitInsn(Opcodes.D2F);
            } else {
              throw new RuntimeException("invalid to-type from double");
            }
          }

          @Override
          public void caseFloatType(FloatType t) {
            if (IntType.v().equals(to)) {
              mv.visitInsn(Opcodes.F2I);
            } else if (LongType.v().equals(to)) {
              mv.visitInsn(Opcodes.F2L);
            } else if (DoubleType.v().equals(to)) {
              mv.visitInsn(Opcodes.F2D);
            } else {
              throw new RuntimeException("invalid to-type from float");
            }
          }

          @Override
          public void caseIntType(IntType t) {
            emitIntToTypeCast();
          }

          @Override
          public void caseLongType(LongType t) {
            if (IntType.v().equals(to)) {
              mv.visitInsn(Opcodes.L2I);
            } else if (FloatType.v().equals(to)) {
              mv.visitInsn(Opcodes.L2F);
            } else if (DoubleType.v().equals(to)) {
              mv.visitInsn(Opcodes.L2D);
            } else {
              throw new RuntimeException("invalid to-type from long");
            }
          }

          @Override
          public void caseShortType(ShortType t) {
            emitIntToTypeCast();
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid from-type: " + t);
          }

          private void emitIntToTypeCast() {
            if (ByteType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2B);
            } else if (CharType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2C);
            } else if (ShortType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2S);
            } else if (FloatType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2F);
            } else if (LongType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2L);
            } else if (DoubleType.v().equals(to)) {
              mv.visitInsn(Opcodes.I2D);
            } else if (IntType.v().equals(to)) {
            } else if (BooleanType.v().equals(to)) {
            } else {
              throw new RuntimeException("invalid to-type from int");
            }
          }
        });
      }

      @Override
      public void caseDynamicInvokeInst(DynamicInvokeInst i) {
        List<Value> args = i.getBootstrapArgs();
        final Object[] argsArray = new Object[args.size()];
        int index = 0;
        for (Value v : args) {
          final int j = index;
          v.apply(new ConstantSwitch() {

            @Override
            public void defaultCase(Object object) {
              throw new RuntimeException("Unexpected constant type!");
            }

            @Override
            public void caseStringConstant(StringConstant v) {
              argsArray[j] = v.value;
            }

            @Override
            public void caseNullConstant(NullConstant v) {
              /*
               * The Jasmin-backend throws an exception for the null-type.
               */
              throw new RuntimeException("Unexpected NullType as argument-type in invokedynamic!");
            }

            @Override
            public void caseMethodHandle(MethodHandle handle) {
              if (handle.isMethodRef()) {
                SootMethodRef methodRef = handle.getMethodRef();
                argsArray[j] = new Handle(handle.getKind(), slashify(methodRef.declaringClass().getName()), methodRef.name(),
                    toTypeDesc(methodRef), methodRef.declaringClass().isInterface());
              } else {
                SootFieldRef fieldRef = handle.getFieldRef();
                argsArray[j] = new Handle(handle.getKind(), slashify(fieldRef.declaringClass().getName()), fieldRef.name(),
                    toTypeDesc(fieldRef.type()), fieldRef.declaringClass().isInterface());
              }
            }

            @Override
            public void caseMethodType(MethodType type) {
              argsArray[j] = org.objectweb.asm.Type.getType(toTypeDesc(type.getParameterTypes(), type.getReturnType()));
            }

            @Override
            public void caseLongConstant(LongConstant v) {
              argsArray[j] = v.value;
            }

            @Override
            public void caseIntConstant(IntConstant v) {
              argsArray[j] = v.value;
            }

            @Override
            public void caseFloatConstant(FloatConstant v) {
              argsArray[j] = v.value;
            }

            @Override
            public void caseDoubleConstant(DoubleConstant v) {
              argsArray[j] = v.value;
            }

            @Override
            public void caseClassConstant(ClassConstant v) {
              argsArray[j] = org.objectweb.asm.Type.getType(v.getValue());
            }
          });
          ++index;
        }
        SootMethodRef m = i.getMethodRef();
        SootMethodRef bsm = i.getBootstrapMethodRef();
        mv.visitInvokeDynamicInsn(m.name(), toTypeDesc(m), new Handle(i.getHandleTag(),
            slashify(bsm.declaringClass().getName()), bsm.name(), toTypeDesc(bsm), bsm.declaringClass().isInterface()),
            argsArray);
      }

      @Override
      public void caseStaticInvokeInst(StaticInvokeInst i) {
        SootMethodRef m = i.getMethodRef();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, slashify(m.declaringClass().getName()), m.name(), toTypeDesc(m),
            m.declaringClass().isInterface() && !m.isStatic());
      }

      @Override
      public void caseVirtualInvokeInst(VirtualInvokeInst i) {
        SootMethodRef m = i.getMethodRef();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, slashify(m.declaringClass().getName()), m.name(), toTypeDesc(m),
            m.declaringClass().isInterface());
      }

      @Override
      public void caseInterfaceInvokeInst(InterfaceInvokeInst i) {
        SootMethodRef m = i.getMethodRef();
        SootClass declaration = m.declaringClass();
        boolean isInterface = true;
        if (!declaration.isPhantom() && !declaration.isInterface()) {
          /*
           * If the declaring class of a method called via invokeinterface is a phantom class we assume the declaring class
           * to be an interface. This might not be true in general, but as of today Soot can not evaluate isInterface() for
           * phantom classes correctly.
           */
          isInterface = false;
        }
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, slashify(declaration.getName()), m.name(), toTypeDesc(m), isInterface);
      }

      @Override
      public void caseSpecialInvokeInst(SpecialInvokeInst i) {
        SootMethodRef m = i.getMethodRef();
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, slashify(m.declaringClass().getName()), m.name(), toTypeDesc(m),
            m.declaringClass().isInterface());
      }

      @Override
      public void caseThrowInst(ThrowInst i) {
        mv.visitInsn(Opcodes.ATHROW);
      }

      @Override
      public void caseAddInst(AddInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.IADD);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.IADD);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.IADD);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DADD);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FADD);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IADD);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LADD);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.IADD);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseAndInst(AndInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LAND);
        } else {
          mv.visitInsn(Opcodes.IAND);
        }
      }

      @Override
      public void caseOrInst(OrInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LOR);
        } else {
          mv.visitInsn(Opcodes.IOR);
        }
      }

      @Override
      public void caseXorInst(XorInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LXOR);
        } else {
          mv.visitInsn(Opcodes.IXOR);
        }
      }

      @Override
      public void caseArrayLengthInst(ArrayLengthInst i) {
        mv.visitInsn(Opcodes.ARRAYLENGTH);
      }

      @Override
      public void caseCmpInst(CmpInst i) {
        mv.visitInsn(Opcodes.LCMP);
      }

      @Override
      public void caseCmpgInst(CmpgInst i) {
        if (i.getOpType().equals(FloatType.v())) {
          mv.visitInsn(Opcodes.FCMPG);
        } else {
          mv.visitInsn(Opcodes.DCMPG);
        }
      }

      @Override
      public void caseCmplInst(CmplInst i) {
        if (i.getOpType().equals(FloatType.v())) {
          mv.visitInsn(Opcodes.FCMPL);
        } else {
          mv.visitInsn(Opcodes.DCMPL);
        }
      }

      @Override
      public void caseDivInst(DivInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.IDIV);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.IDIV);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.IDIV);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DDIV);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FDIV);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IDIV);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LDIV);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.IDIV);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseIncInst(IncInst i) {
        if (i.getUseBoxes().get(0).getValue() != i.getDefBoxes().get(0).getValue()) {
          throw new RuntimeException("iinc def and use boxes don't match");
        }
        if (i.getConstant() instanceof IntConstant) {
          mv.visitIincInsn(localToSlot.get(i.getLocal()), ((IntConstant) i.getConstant()).value);
        } else {
          throw new RuntimeException("Wrong constant type for increment!");
        }

      }

      @Override
      public void caseMulInst(MulInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.IMUL);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.IMUL);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.IMUL);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DMUL);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FMUL);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IMUL);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LMUL);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.IMUL);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseRemInst(RemInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.IREM);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.IREM);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.IREM);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DREM);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FREM);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.IREM);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LREM);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.IREM);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseSubInst(SubInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.ISUB);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.ISUB);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.ISUB);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DSUB);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FSUB);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.ISUB);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LSUB);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.ISUB);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseShlInst(ShlInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LSHL);
        } else {
          mv.visitInsn(Opcodes.ISHL);
        }
      }

      @Override
      public void caseShrInst(ShrInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LSHR);
        } else {
          mv.visitInsn(Opcodes.ISHR);
        }
      }

      @Override
      public void caseUshrInst(UshrInst i) {
        if (i.getOpType().equals(LongType.v())) {
          mv.visitInsn(Opcodes.LUSHR);
        } else {
          mv.visitInsn(Opcodes.IUSHR);
        }
      }

      @Override
      public void caseNewInst(NewInst i) {
        mv.visitTypeInsn(Opcodes.NEW, slashify(i.getBaseType().getClassName()));
      }

      @Override
      public void caseNegInst(NegInst i) {
        i.getOpType().apply(new TypeSwitch() {

          @Override
          public void caseBooleanType(BooleanType t) {
            mv.visitInsn(Opcodes.INEG);
          }

          @Override
          public void caseByteType(ByteType t) {
            mv.visitInsn(Opcodes.INEG);
          }

          @Override
          public void caseCharType(CharType t) {
            mv.visitInsn(Opcodes.INEG);
          }

          @Override
          public void caseDoubleType(DoubleType t) {
            mv.visitInsn(Opcodes.DNEG);
          }

          @Override
          public void caseFloatType(FloatType t) {
            mv.visitInsn(Opcodes.FNEG);
          }

          @Override
          public void caseIntType(IntType t) {
            mv.visitInsn(Opcodes.INEG);
          }

          @Override
          public void caseLongType(LongType t) {
            mv.visitInsn(Opcodes.LNEG);
          }

          @Override
          public void caseShortType(ShortType t) {
            mv.visitInsn(Opcodes.INEG);
          }

          @Override
          public void defaultCase(Type t) {
            throw new RuntimeException("invalid type");
          }
        });
      }

      @Override
      public void caseSwapInst(SwapInst i) {
        mv.visitInsn(Opcodes.SWAP);
      }

      @Override
      public void caseDup1Inst(Dup1Inst i) {
        if (sizeOfType(i.getOp1Type()) == 2) {
          mv.visitInsn(Opcodes.DUP2);
        } else {
          mv.visitInsn(Opcodes.DUP);
        }
      }

      @Override
      public void caseDup2Inst(Dup2Inst i) {
        Type firstOpType = i.getOp1Type();
        Type secondOpType = i.getOp2Type();
        // The first two cases have no real bytecode equivalents.
        // Use a pair of instructions to simulate them.
        if (sizeOfType(firstOpType) == 2) {
          mv.visitInsn(Opcodes.DUP2);
          if (sizeOfType(secondOpType) == 2) {
            mv.visitInsn(Opcodes.DUP2);
          } else {
            mv.visitInsn(Opcodes.DUP);
          }
        } else if (sizeOfType(secondOpType) == 2) {
          mv.visitInsn(Opcodes.DUP);
          mv.visitInsn(Opcodes.DUP2);
        } else {
          mv.visitInsn(Opcodes.DUP2);
        }
      }

      @Override
      public void caseDup1_x1Inst(Dup1_x1Inst i) {
        Type opType = i.getOp1Type();
        Type underType = i.getUnder1Type();

        if (sizeOfType(opType) == 2) {
          if (sizeOfType(underType) == 2) {
            mv.visitInsn(Opcodes.DUP2_X2);
          } else {
            mv.visitInsn(Opcodes.DUP2_X1);
          }
        } else {
          if (sizeOfType(underType) == 2) {
            mv.visitInsn(Opcodes.DUP_X2);
          } else {
            mv.visitInsn(Opcodes.DUP_X1);
          }
        }
      }

      @Override
      public void caseDup1_x2Inst(Dup1_x2Inst i) {
        int toSkip = sizeOfType(i.getUnder1Type()) + sizeOfType(i.getUnder2Type());

        if (sizeOfType(i.getOp1Type()) == 2) {
          if (toSkip == 2) {
            mv.visitInsn(Opcodes.DUP2_X2);
          } else {
            throw new RuntimeException("magic not implemented yet");
          }
        } else {
          if (toSkip == 2) {
            mv.visitInsn(Opcodes.DUP_X2);
          } else {
            throw new RuntimeException("magic not implemented yet");
          }
        }
      }

      @Override
      public void caseDup2_x1Inst(Dup2_x1Inst i) {
        int toDup = sizeOfType(i.getOp1Type()) + sizeOfType(i.getOp2Type());

        if (toDup == 2) {
          if (sizeOfType(i.getUnder1Type()) == 2) {
            mv.visitInsn(Opcodes.DUP2_X2);
          } else {
            mv.visitInsn(Opcodes.DUP2_X1);
          }
        } else {
          throw new RuntimeException("magic not implemented yet");
        }
      }

      @Override
      public void caseDup2_x2Inst(Dup2_x2Inst i) {
        int toDup = sizeOfType(i.getOp1Type()) + sizeOfType(i.getOp2Type());
        int toSkip = sizeOfType(i.getUnder1Type()) + sizeOfType(i.getUnder2Type());

        if (toDup > 2 || toSkip > 2) {
          throw new RuntimeException("magic not implemented yet");
        }

        if (toDup == 2 && toSkip == 2) {
          mv.visitInsn(Opcodes.DUP2_X2);
        } else {
          throw new RuntimeException("VoidType not allowed in Dup2_x2 Instruction");
        }
      }

      @Override
      public void caseNewArrayInst(NewArrayInst i) {
        Type t = i.getBaseType();
        if (t instanceof RefType) {
          mv.visitTypeInsn(Opcodes.ANEWARRAY, slashify(((RefType) t).getClassName()));
        } else if (t instanceof ArrayType) {
          mv.visitTypeInsn(Opcodes.ANEWARRAY, toTypeDesc(t));
        } else {
          int type;
          if (BooleanType.v().equals(t)) {
            type = Opcodes.T_BOOLEAN;
          } else if (CharType.v().equals(t)) {
            type = Opcodes.T_CHAR;
          } else if (FloatType.v().equals(t)) {
            type = Opcodes.T_FLOAT;
          } else if (DoubleType.v().equals(t)) {
            type = Opcodes.T_DOUBLE;
          } else if (ByteType.v().equals(t)) {
            type = Opcodes.T_BYTE;
          } else if (ShortType.v().equals(t)) {
            type = Opcodes.T_SHORT;
          } else if (IntType.v().equals(t)) {
            type = Opcodes.T_INT;
          } else if (LongType.v().equals(t)) {
            type = Opcodes.T_LONG;
          } else {
            throw new RuntimeException("invalid type");
          }
          mv.visitIntInsn(Opcodes.NEWARRAY, type);
        }
      }

      @Override
      public void caseNewMultiArrayInst(NewMultiArrayInst i) {
        mv.visitMultiANewArrayInsn(toTypeDesc(i.getBaseType()), i.getDimensionCount());
      }

      @Override
      public void caseLookupSwitchInst(LookupSwitchInst i) {
        List<IntConstant> values = i.getLookupValues();
        List<Unit> targets = i.getTargets();

        final int size = values.size();
        int[] keys = new int[size];
        Label[] labels = new Label[size];

        for (int j = 0; j < size; j++) {
          keys[j] = values.get(j).value;
          labels[j] = branchTargetLabels.get(targets.get(j));
        }

        mv.visitLookupSwitchInsn(branchTargetLabels.get(i.getDefaultTarget()), keys, labels);
      }

      @Override
      public void caseTableSwitchInst(TableSwitchInst i) {
        List<Unit> targets = i.getTargets();

        final int size = targets.size();
        Label[] labels = new Label[size];

        for (int j = 0; j < size; j++) {
          labels[j] = branchTargetLabels.get(targets.get(j));
        }

        mv.visitTableSwitchInsn(i.getLowIndex(), i.getHighIndex(), branchTargetLabels.get(i.getDefaultTarget()), labels);
      }

      @Override
      public void caseEnterMonitorInst(EnterMonitorInst i) {
        mv.visitInsn(Opcodes.MONITORENTER);
      }

      @Override
      public void caseExitMonitorInst(ExitMonitorInst i) {
        mv.visitInsn(Opcodes.MONITOREXIT);
      }
    });
  }
}
