/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] http://openjdk.java.net/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
 *******************************************************************************/
package com.ibm.j9ddr.vm29;

import com.ibm.j9ddr.CorruptDataException;
import com.ibm.j9ddr.IBootstrapRunnable;
import com.ibm.j9ddr.IVMData;
import com.ibm.j9ddr.InvalidDataTypeException;
import com.ibm.j9ddr.corereaders.memory.BufferedMemorySource;
import com.ibm.j9ddr.corereaders.memory.MemoryFault;
import com.ibm.j9ddr.vm29.j9.BCNames;
import com.ibm.j9ddr.vm29.j9.ConstantPoolHelpers;
import com.ibm.j9ddr.vm29.j9.J9ROMFieldShapeIterator;
import com.ibm.j9ddr.vm29.j9.ROMHelp;
import com.ibm.j9ddr.vm29.pointer.FloatPointer;
import com.ibm.j9ddr.vm29.pointer.I64Pointer;
import com.ibm.j9ddr.vm29.pointer.SelfRelativePointer;
import com.ibm.j9ddr.vm29.pointer.U16Pointer;
import com.ibm.j9ddr.vm29.pointer.U32Pointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ExceptionInfoPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMClassPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMClassRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMConstantPoolItemPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMFieldRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMFieldShapePointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMMethodHandleRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMMethodPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMMethodRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMMethodTypeRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMNameAndSignaturePointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMSingleSlotConstantRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMStaticFieldShapePointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9ROMStringRefPointer;
import com.ibm.j9ddr.vm29.pointer.generated.J9UTF8Pointer;
import com.ibm.j9ddr.vm29.pointer.helper.J9ROMClassHelper;
import com.ibm.j9ddr.vm29.pointer.helper.J9ROMMethodHelper;
import com.ibm.j9ddr.vm29.pointer.helper.J9UTF8Helper;
import com.ibm.j9ddr.vm29.types.UDATA;

import java.nio.ByteOrder;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import soot.asm.SootClassBuilder;
import soot.cache.CacheMemorySingleton;

/*                                                                                                                
 * This implementation strongly relies upon the visitor invoke pattern defined in ASM Classreader:                
 * https://gitlab.ow2.org/asm/asm/blob/ASM_5_2/src/org/objectweb/asm/ClassReader.java                             
 * HOWEVER, did not want inheritance bc need to avoid asm ClassReader constructor behaviour which relies upon     
 * many hardcoded indices                                                                                         
 */

public class ROMClassWrapper implements IBootstrapRunnable {

  // might want to fix. currently stolen hardcode from
  // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/oti/j9nonbuilder.h#L1965
  // the reason is that these are only defined in: com.ibm.j9ddr.vm29.structure.J9BCTranslationData
  // and only sizeof is exposed in:
  // openj9/debugtools/DDR_VM/src/com/ibm/j9ddr/vm29/pointer/generated/J9DescriptionBitsPointer.java
  // though somehow bytecode dumper can use ...
  // https://github.com/eclipse/openj9/blob/v0.14.0-release/debugtools/DDR_VM/src/com/ibm/j9ddr/vm29/tools/ddrinteractive/commands/ByteCodeDumper.java#L96
  // but we are using the structure file, not a loaded class... so this may be the reason...

  private int BCT_J9DescriptionCpTypeScalar = 0;
  private int BCT_J9DescriptionCpTypeObject = 1;
  private int BCT_J9DescriptionCpTypeClass = 2;
  private int J9DescriptionCpTypeShift = 4;
  private int J9AccStatic = 8;

  private int J9CPTYPE_CLASS = 1;
  private int J9CPTYPE_STRING = 2;
  private int J9CPTYPE_INT = 3;
  private int J9CPTYPE_FLOAT = 4;
  private int J9CPTYPE_LONG = 5;
  private int J9CPTYPE_DOUBLE = 6;
  private int J9CPTYPE_FIELD = 7;
  private int J9CPTYPE_INSTANCE_METHOD = 9;
  private int J9CPTYPE_STATIC_METHOD = 10;
  private int J9CPTYPE_HANDLE_METHOD = 11;
  private int J9CPTYPE_INTERFACE_METHOD = 12;
  private int J9CPTYPE_METHOD_TYPE = 13;
  private int J9CPTYPE_METHODHANDLE = 14;

  // https://github.com/eclipse/openj9/blob/master/runtime/oti/cfr.h
  // CFR_ACC_PUBLIC | CFR_ACC_PRIVATE | CFR_ACC_PROTECTED | CFR_ACC_STATIC | CFR_ACC_FINAL | CFR_ACC_SYNCHRONIZED |
  // CFR_ACC_BRIDGE | CFR_ACC_VARARGS | CFR_ACC_NATIVE | CFR_ACC_STRICT | CFR_ACC_ABSTRACT | CFR_ACC_SYNTHETIC)
  private int METHOD_ACCESS_MASK = 0x00000001 | 0x00000002 | 0x00000004 | 0x00000008 | 0x00000010 | 0x00000020 | 0x00000040
      | 0x00000080 | 0x00000100 | 0x00000800 | 0x00000400 | 0x00001000;

  // J9FieldFlagConstant 0x400000
  // J9FieldTypeDouble 0x180000
  // J9FieldTypeLong 0x380000
  // J9FieldTypeFloat 0x100000
  // J9FieldFlagObject 0x20000
  // J9FieldTypeByte 0x200000

  // likely less efficient than hardcoding the ints, but this is easier to read and check since the vals in nonbuilder
  // are in hex
  private int J9FieldTypeDouble = Integer.parseInt("180000", 16);
  private int J9FieldTypeLong = Integer.parseInt("380000", 16);
  private int J9FieldFlagObject = Integer.parseInt("20000", 16);
  private int J9FieldFlagConstant = Integer.parseInt("400000", 16);
  private int J9FieldTypeFloat = Integer.parseInt("100000", 16);
  private int J9FieldTypeMask = Integer.parseInt("380000", 16);
  private int J9FieldTypeByte = Integer.parseInt("200000", 16);
  /////////////////////////////////////

  private J9ROMClassPointer pointer;
  private CacheMemorySingleton cacheMem;
  private static ClassVisitor classVisitor;

  // classreader specific attributes
  static final boolean FRAMES = true;

  public void run(IVMData vmData, Object[] userData) {

    Long addr = new Long((long) userData[0]);
    this.pointer = J9ROMClassPointer.cast(addr);

    this.classVisitor = (SootClassBuilder) userData[1];

    // need this for method iter, getting a bit much to have everyone here
    this.cacheMem = (CacheMemorySingleton) userData[2];

    accept(this.classVisitor);
  }

  public static ClassVisitor getClassVisitor() {
    return classVisitor;
  }

  public void accept(final ClassVisitor classVisitor) {

    try {
      int version = pointer.majorVersion().intValue();
      int classModifiers = pointer.modifiers().intValue();
      String classname = J9UTF8Helper.stringValue(pointer.className());
      J9UTF8Pointer superclassPointer = pointer.superclassName();
      String superclassname;
      // java.lang.Object has no superclass
      if (superclassPointer == J9UTF8Pointer.NULL) {
        superclassname = null;
      } else {
        superclassname = J9UTF8Helper.stringValue(superclassPointer);
      }

      // reference to constant pool
      J9ROMConstantPoolItemPointer constantPool = J9ROMClassHelper.constantPool(pointer);

      // header class info
      // version, int access, String name, String signature, String superName, String[] interfaces
      classVisitor.visit(version, classModifiers, classname, "", superclassname, new String[] {});

      readFields(classVisitor, constantPool);

      // method handling
      int methodCount = pointer.romMethodCount().intValue();
      J9ROMMethodPointer romMethod = pointer.romMethods();
      for (int i = 0; i < methodCount; i++) {
        readMethod(romMethod, constantPool);
        romMethod = ROMHelp.nextROMMethod(romMethod);
      }

    } catch (Exception e) {
      System.out.println("Issue in visitor pattern driving: " + e.getMessage());
      e.printStackTrace(System.out);
    }
    // finish up
    classVisitor.visitEnd();
  }

  void readFields(ClassVisitor classVisitor, J9ROMConstantPoolItemPointer constantPool) throws CorruptDataException {

    FieldVisitor fv = null;
    Object value = null;

    // FieldVisitor visitField(int access, String name, String desc, String signature, Object value
    UDATA romFieldCount = pointer.romFieldCount();
    J9ROMFieldShapeIterator iterator = new J9ROMFieldShapeIterator(pointer.romFields(), romFieldCount);
    J9ROMFieldShapePointer currentField = null;

    while (iterator.hasNext()) {
      currentField = (J9ROMFieldShapePointer) iterator.next();

      String name = J9UTF8Helper.stringValue(currentField.nameAndSignature().name());
      String signature = J9UTF8Helper.stringValue(currentField.nameAndSignature().signature());

      if (!currentField.modifiers().bitAnd(J9AccStatic).eq(0)) {

        // if its static, we should get its initial value:
        // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/oti/j9nonbuilder.h#L686

        value = getStaticFieldValue(currentField.modifiers(), J9ROMStaticFieldShapePointer.cast(currentField), constantPool);

      }

      // visitField(int access, String name, String desc, String signature, Object value)
      fv = classVisitor.visitField(currentField.modifiers().intValue(), name, signature, signature, value);
      fv.visitEnd();
      value = null;
    }

  }

  void readMethod(J9ROMMethodPointer romMethod, J9ROMConstantPoolItemPointer constantPool) throws CorruptDataException {
    // method info

    int methodModifiers = romMethod.modifiers().intValue();
    J9ROMNameAndSignaturePointer nameAndSignature = romMethod.nameAndSignature();
    String name = J9UTF8Helper.stringValue(nameAndSignature.name());
    String signature = J9UTF8Helper.stringValue(nameAndSignature.signature());

    // for debugging only
    /*
     * if(name.contains("insert-some-method-name-here")){ System.out.println("---------------------------------");
     * System.out.println("DUMPING copyof"); long dumpFlags = (this.cacheMem.getMemorySource().getByteOrder() ==
     * ByteOrder.BIG_ENDIAN) ? 1 : 0; J9BCUtil.j9bcutil_dumpRomMethod(System.out, romMethod, pointer, dumpFlags,
     * J9BCUtil.BCUtil_DumpAnnotations); System.out.println("---------------------------------"); }
     */

    int maxStack = romMethod.maxStack().intValue();
    int maxLocals = romMethod.tempCount().intValue() + romMethod.argCount().intValue();
    int argCount = romMethod.argCount().intValue();

    int romMethodSize = J9ROMMethodHelper.bytecodeSize(romMethod).intValue();

    long bytecodeSt = J9ROMMethodHelper.bytecodes(romMethod).longValue();
    long bytecodeEnd = J9ROMMethodHelper.bytecodeEnd(romMethod).longValue();
    char returnType = signature.charAt(signature.lastIndexOf(")") + 1);

    String[] exceptions = getExceptions(romMethod);

    MethodVisitor mv
        = classVisitor.visitMethod(methodModifiers & METHOD_ACCESS_MASK, name, signature, signature, exceptions);
    readMethodBody(bytecodeSt, bytecodeEnd, mv, constantPool, returnType);
    // for now
    mv.visitMaxs(maxStack, maxLocals);
    mv.visitEnd();
  }

  String[] getExceptions(J9ROMMethodPointer romMethod) throws CorruptDataException {

    if (J9ROMMethodHelper.hasExceptionInfo(romMethod)) {

      J9ExceptionInfoPointer exceptionData = ROMHelp.J9_EXCEPTION_DATA_FROM_ROM_METHOD(romMethod);
      long throwCount = exceptionData.throwCount().longValue();
      String[] exceptions = new String[(int) throwCount];

      if (throwCount > 0) {
        SelfRelativePointer currentThrowName = ROMHelp.J9EXCEPTIONINFO_THROWNAMES(exceptionData);
        for (int i = 0; i < throwCount; i++) {
          exceptions[i] = J9UTF8Helper.stringValue(J9UTF8Pointer.cast(currentThrowName.get()));
        }
      }
      return exceptions;
    }
    return new String[] {};
  }

  Object getStaticFieldValue(UDATA modsFull, J9ROMStaticFieldShapePointer field, J9ROMConstantPoolItemPointer constantPool)
      throws CorruptDataException {

    // get just the type portion of the mods
    int mods = modsFull.intValue() & J9FieldTypeMask;

    // aka if not a null static
    if (modsFull.anyBitsIn(J9FieldFlagConstant)) {

      if (mods == J9FieldTypeLong) {
        return new Long(field.initialValue().longValue());

      } else if (mods == J9FieldTypeDouble) {

        // field.initialValue is setup like a cp entry, even longValue() +double cast cannot get this as double
        // so we manually read it, even though that relies on hard offsets
        BufferedMemorySource src = this.cacheMem.getMemorySource();
        long first = I64Pointer.cast(field.add(1)).at(0).longValue();
        long second = I64Pointer.cast(field.add(2)).at(0).longValue();
        // This is honestly the opposite of what we expect it to be, completely unsure of why
        long constantvalue = (src.getByteOrder() == ByteOrder.BIG_ENDIAN) ? (second << 32) | (first & 0xffffffffL)
            : (first << 32) | (second & 0xffffffffL);

        return new Double(Double.longBitsToDouble(constantvalue));
      }

      else if (modsFull.allBitsIn(J9FieldFlagObject)) {
        // this initial value is actually an index into the constant pool
        J9ROMConstantPoolItemPointer info = constantPool.add(field.initialValue().intValue());
        String value = J9UTF8Helper.stringValue(J9ROMStringRefPointer.cast(info).utf8Data());
        return value;

      } else {

        /* by default, type is anything that can be read as an int, except for float */
        if (mods == J9FieldTypeFloat) {
          return new Float(Float.intBitsToFloat((int) field.initialValue().longValue()));
        } else if (mods == J9FieldTypeByte) {
          return new Byte(field.initialValue().byteValue());
        } else {
          try {
            return new Integer(field.initialValue().intValue());
          } catch (InvalidDataTypeException e) {
            return new Integer((int) field.initialValue().longValue());
          }
        }
      }
    } else {
      return null;
    }

  }

  void readMethodBody(long bytecodeSt, long bytecodeEnd, MethodVisitor mv, J9ROMConstantPoolItemPointer constantPool,
      char returnType) throws CorruptDataException {
    // drives the visitor to define the body of the method
    BufferedMemorySource src = this.cacheMem.getMemorySource();
    long ptr = bytecodeSt;

    // for our targets, as we find them
    Label[] labels = findLabels(bytecodeSt, bytecodeEnd, ptr, src);

    while (ptr < bytecodeEnd) {
      int offset = (int) (ptr - bytecodeSt);
      int opcode = (int) (src.getByte(ptr) & 0xFF);

      // visit a label if there is one
      Label l = labels[offset];
      if (l != null) {
        mv.visitLabel(l);
      }

      if ((opcode == BCNames.JBnop) || (opcode == BCNames.JBinvokeinterface2)) {
        ptr += 1;
      } else if ((opcode == BCNames.JBdefaultvalue) || (opcode == BCNames.JBwithfield)) {
        // these only exist for #if defined(J9VM_OPT_VALHALLA_VALUE_TYPES)?
        ptr += 2;
      } else if (opcode == BCNames.JBiinc) {
        int LVAindex = src.getByte(ptr + 1);
        int increment = src.getByte(ptr + 2);
        mv.visitIincInsn(LVAindex, increment);
        ptr += 3;
      } else if (opcode == BCNames.JBiincw) {
        int LVAindex = src.getShort(ptr + 1);
        int increment = src.getShort(ptr + 3);
        mv.visitIincInsn(LVAindex, increment);
        ptr += 6;
      } else if (opcode == BCNames.JBmultianewarray) {

        int index = src.getShort(ptr + 1);

        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        int dim = src.getByte(ptr + 3) & 0xFF;

        String arrName = J9UTF8Helper.stringValue(J9ROMStringRefPointer.cast(info).utf8Data());
        mv.visitMultiANewArrayInsn(arrName, dim);
        ptr += 4;
      }
      // maybe don't expect to see the returnFromConstructor:
      // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/compiler/ilgen/J9ByteCode.hpp
      else if ((opcode == BCNames.JBreturnFromConstructor) || (opcode == BCNames.JBgenericReturn)
          || (opcode == BCNames.JBreturn0)) {
        mv.visitInsn(Opcodes.RETURN);
        ptr += 1;
      } else if ((opcode == BCNames.JBreturnC) || (opcode == BCNames.JBreturnS) || (opcode == BCNames.JBreturnB)
          || (opcode == BCNames.JBreturnZ)) {
        mv.visitInsn(Opcodes.IRETURN);
        ptr += 1;
      } else if ((opcode == BCNames.JBreturn1) || (opcode == BCNames.JBreturn2)) {
        // return0,1,2 correspond to return(pop 0 slots), return(pop 1 slot) and return(pop 2 slots)
        opcode = getReturnType(returnType);
        mv.visitInsn(opcode);
        ptr += 1;
      } else if ((opcode == BCNames.JBinvokehandle) || (opcode == BCNames.JBinvokehandlegeneric)
          || (opcode == BCNames.JBinvokestaticsplit) || (opcode == BCNames.JBinvokespecialsplit)) {
        // TODO handle
        ptr += 3;
      } else if ((opcode == BCNames.JBiload0) || (opcode == BCNames.JBiload1) || (opcode == BCNames.JBiload2)
          || (opcode == BCNames.JBiload3) || (opcode == BCNames.JBlload0) || (opcode == BCNames.JBlload1)
          || (opcode == BCNames.JBlload2) || (opcode == BCNames.JBlload3) || (opcode == BCNames.JBfload0)
          || (opcode == BCNames.JBfload1) || (opcode == BCNames.JBfload2) || (opcode == BCNames.JBfload3)
          || (opcode == BCNames.JBdload0) || (opcode == BCNames.JBdload1) || (opcode == BCNames.JBdload2)
          || (opcode == BCNames.JBdload3) || (opcode == BCNames.JBaload0) || (opcode == BCNames.JBaload1)
          || (opcode == BCNames.JBaload2) || (opcode == BCNames.JBaload3) ||

          (opcode == BCNames.JBistore0) || (opcode == BCNames.JBistore1) || (opcode == BCNames.JBistore2)
          || (opcode == BCNames.JBistore3) || (opcode == BCNames.JBlstore0) || (opcode == BCNames.JBlstore1)
          || (opcode == BCNames.JBlstore2) || (opcode == BCNames.JBlstore3) || (opcode == BCNames.JBfstore0)
          || (opcode == BCNames.JBfstore1) || (opcode == BCNames.JBfstore2) || (opcode == BCNames.JBfstore3)
          || (opcode == BCNames.JBdstore0) || (opcode == BCNames.JBdstore1) || (opcode == BCNames.JBdstore2)
          || (opcode == BCNames.JBdstore3) || (opcode == BCNames.JBastore0) || (opcode == BCNames.JBastore1)
          || (opcode == BCNames.JBastore2) || (opcode == BCNames.JBastore3)) {
        if (opcode > Opcodes.ISTORE) {

          opcode -= 59; // ISTORE_0
          mv.visitVarInsn(Opcodes.ISTORE + (opcode >> 2), opcode & 0x3);
        } else {

          opcode -= 26;
          mv.visitVarInsn(Opcodes.ILOAD + (opcode >> 2), opcode & 0x3);
        }
        ptr += 1;
      } else if (opcode == BCNames.JBaload0getfield) {
        // handle this as an aload0, followed by a getfield (where the index is 2 bytes in?)
        // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/vm/BytecodeInterpreter.hpp#L6480
        mv.visitVarInsn(25, 0);
        int index = src.getUnsignedShort(ptr + 2);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        J9ROMFieldRefPointer romFieldRef = J9ROMFieldRefPointer.cast(info);
        String owner
            = J9UTF8Helper.stringValue(J9ROMClassRefPointer.cast(constantPool.add(romFieldRef.classRefCPIndex())).name());

        J9ROMNameAndSignaturePointer nameAndSig = romFieldRef.nameAndSignature();
        String name = J9UTF8Helper.stringValue(nameAndSig.name());
        String desc = J9UTF8Helper.stringValue(nameAndSig.signature());
        mv.visitFieldInsn(180, owner, name, desc);
        ptr += 4;
      } else if ((opcode == BCNames.JBifeq) || (opcode == BCNames.JBifne) || (opcode == BCNames.JBiflt)
          || (opcode == BCNames.JBifge) || (opcode == BCNames.JBifgt) || (opcode == BCNames.JBifle)
          || (opcode == BCNames.JBificmpeq) || (opcode == BCNames.JBificmpne) || (opcode == BCNames.JBificmplt)
          || (opcode == BCNames.JBificmpge) || (opcode == BCNames.JBificmpgt) || (opcode == BCNames.JBificmple)
          || (opcode == BCNames.JBifacmpeq) || (opcode == BCNames.JBifacmpne) || (opcode == BCNames.JBgoto)
          || (opcode == BCNames.JBifnull) || (opcode == BCNames.JBifnonnull)) {
        mv.visitJumpInsn(opcode, labels[offset + src.getShort(ptr + 1)]);
        ptr += 3;
      } else if (opcode == BCNames.JBgotow) {
        mv.visitJumpInsn(opcode, labels[offset + src.getInt(ptr + 1)]);
        ptr += 5;
      } else if (opcode == 196) {
        // 196 == WIDE
        // BCNames does not have a wide opcode...
        // but both Walker https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/compiler/ilgen/Walker.cpp#L1494
        // and ilgen maybe expect it to exist
        // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/compiler/ilgen/J9ByteCode.hpp#L111
        opcode = src.getInt(ptr + 1);
        if (opcode == BCNames.JBiinc) {
          mv.visitIincInsn(src.getUnsignedShort(ptr + 2), src.getShort(ptr + 4));
          ptr += 6;
        } else {
          mv.visitVarInsn(opcode, src.getUnsignedShort(ptr + 2));
          ptr += 4;
        }
      } else if (opcode == BCNames.JBtableswitch) {
        ptr = ptr + 4 - (offset & 3);
        // reads instruction
        int label = offset + src.getInt(ptr);
        int min = src.getInt(ptr + 4);
        int max = src.getInt(ptr + 8);
        Label[] table = new Label[max - min + 1];
        ptr += 12;
        for (int i = 0; i < table.length; ++i) {
          table[i] = labels[offset + src.getInt(ptr)];
          ptr += 4;
        }
        mv.visitTableSwitchInsn(min, max, labels[label], table);
      } else if (opcode == BCNames.JBlookupswitch) {
        // skips 0 to 3 padding bytes
        ptr = ptr + 4 - (offset & 3);
        // reads instruction
        int label = offset + src.getInt(ptr);
        int len = src.getInt(ptr + 4);
        int[] keys = new int[len];
        Label[] values = new Label[len];
        ptr += 8;
        for (int i = 0; i < len; ++i) {
          keys[i] = src.getInt(ptr);
          values[i] = labels[offset + src.getInt(ptr + 4)];
          ptr += 8;
        }
        mv.visitLookupSwitchInsn(labels[label], keys, values);
      } else if (opcode == BCNames.JBiloadw) {
        mv.visitVarInsn(Opcodes.ILOAD, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBlloadw) {
        mv.visitVarInsn(Opcodes.LLOAD, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBfloadw) {
        mv.visitVarInsn(Opcodes.FLOAD, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBdloadw) {
        mv.visitVarInsn(Opcodes.DLOAD, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBaloadw) {
        mv.visitVarInsn(Opcodes.ALOAD, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBistorew) {
        mv.visitVarInsn(Opcodes.ISTORE, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBlstorew) {
        mv.visitVarInsn(Opcodes.LSTORE, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBfstorew) {
        mv.visitVarInsn(Opcodes.FSTORE, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBdstorew) {
        mv.visitVarInsn(Opcodes.DSTORE, src.getInt(ptr + 1));
        ptr += 3;
      } else if (opcode == BCNames.JBastorew) {
        mv.visitVarInsn(Opcodes.ASTORE, src.getInt(ptr + 1));
        ptr += 3;
      } else if ((opcode == BCNames.JBistore) || (opcode == BCNames.JBlstore) || (opcode == BCNames.JBfstore)
          || (opcode == BCNames.JBdstore) || (opcode == BCNames.JBastore) || (opcode == BCNames.JBiload)
          || (opcode == BCNames.JBlload) || (opcode == BCNames.JBfload) || (opcode == BCNames.JBdload)
          || (opcode == BCNames.JBaload)) {
        mv.visitVarInsn(opcode, src.getByte(ptr + 1) & 0xFF);
        ptr += 2;
      } else if ((opcode == BCNames.JBnewarray) || (opcode == BCNames.JBbipush)) {
        mv.visitIntInsn(opcode, src.getByte(ptr + 1));
        ptr += 2;
      } else if (opcode == BCNames.JBsipush) {
        mv.visitIntInsn(opcode, src.getShort(ptr + 1));
        ptr += 3;
      }
      // not sure if its most readable to handle all separate or do a immediate double check on op val
      else if (opcode == BCNames.JBldc) {

        mv.visitLdcInsn(readConst(src.getByte(ptr + 1) & 0xFF, constantPool));
        ptr += 2;
      } else if (opcode == BCNames.JBldcw) {
        mv.visitLdcInsn(readConst(src.getShort(ptr + 1), constantPool));
        ptr += 3;
      } else if (opcode == BCNames.JBldc2lw) {

        int index = src.getShort(ptr + 1);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        // since we are working with infoslots the order check is a bit ugly against the mem model, but must do
        long constantvalue = (src.getByteOrder() == ByteOrder.BIG_ENDIAN)
            ? ((info.slot1().longValue()) << 32) | (info.slot2().longValue() & 0xffffffffL)
            : ((info.slot2().longValue()) << 32) | (info.slot1().longValue() & 0xffffffffL);

        mv.visitLdcInsn(constantvalue);
        ptr += 3;
      } else if (opcode == BCNames.JBldc2dw) {

        int index = src.getShort(ptr + 1);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        // since we are working with infoslots the order check is a bit ugly against the mem model, but must do
        long constantvalue = (src.getByteOrder() == ByteOrder.BIG_ENDIAN)
            ? ((info.slot1().longValue()) << 32) | (info.slot2().longValue() & 0xffffffffL)
            : ((info.slot2().longValue()) << 32) | (info.slot1().longValue() & 0xffffffffL);
        mv.visitLdcInsn(Double.longBitsToDouble(constantvalue));
        ptr += 3;
      } else if ((opcode == BCNames.JBgetstatic) || (opcode == BCNames.JBputstatic) || (opcode == BCNames.JBgetfield)
          || (opcode == BCNames.JBputfield)) {

        int index = src.getUnsignedShort(ptr + 1);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        J9ROMFieldRefPointer romFieldRef = J9ROMFieldRefPointer.cast(info);
        String owner
            = J9UTF8Helper.stringValue(J9ROMClassRefPointer.cast(constantPool.add(romFieldRef.classRefCPIndex())).name());

        J9ROMNameAndSignaturePointer nameAndSig = romFieldRef.nameAndSignature();
        String name = J9UTF8Helper.stringValue(nameAndSig.name());
        String desc = J9UTF8Helper.stringValue(nameAndSig.signature());
        mv.visitFieldInsn(opcode, owner, name, desc);

        ptr += 3;

        // this has a strong duplication with the prev case except pointer types
        // maybe can be cleaner
      } else if ((opcode == BCNames.JBinvokevirtual) || (opcode == BCNames.JBinvokespecial)
          || (opcode == BCNames.JBinvokestatic) || (opcode == BCNames.JBinvokeinterface)) {

        int index = src.getUnsignedShort(ptr + 1);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);

        J9ROMMethodRefPointer romMethodRef = J9ROMMethodRefPointer.cast(info);
        UDATA classRefCPIndex = romMethodRef.classRefCPIndex();
        J9ROMConstantPoolItemPointer cpItem = constantPool.add(classRefCPIndex);
        J9ROMClassRefPointer romClassRef = J9ROMClassRefPointer.cast(cpItem);

        String owner = J9UTF8Helper.stringValue(romClassRef.name());

        J9ROMNameAndSignaturePointer nameAndSig = romMethodRef.nameAndSignature();
        String name = J9UTF8Helper.stringValue(nameAndSig.name());
        String desc = J9UTF8Helper.stringValue(nameAndSig.signature());

        boolean itf = (opcode == BCNames.JBinvokeinterface);

        mv.visitMethodInsn(opcode, owner, name, desc, itf);

        // if (itf) {
        // ptr += 5;
        // } else {
        ptr += 3;
        // }
      } else if (opcode == BCNames.JBinvokedynamic) {

        int index = src.getShort(ptr + 1);

        long callSiteCount = pointer.callSiteCount().longValue();
        SelfRelativePointer callSiteData = SelfRelativePointer.cast(pointer.callSiteData());
        U16Pointer bsmIndices = U16Pointer.cast(callSiteData.addOffset(4 * callSiteCount));

        J9ROMNameAndSignaturePointer invokedNameAndSig = J9ROMNameAndSignaturePointer.cast(callSiteData.add(index).get());
        String invokedName = J9UTF8Helper.stringValue(invokedNameAndSig.name());
        String invokedDesc = J9UTF8Helper.stringValue(invokedNameAndSig.signature());

        // gets a pointer to the exact entry in the callSiteData table to the bsm data of this invoked method
        long bsmIndex = bsmIndices.at(index).longValue(); // Bootstrap method index

        // get beginning of bsmData section, then the relevant entry
        U16Pointer bsmDataCursor = bsmIndices.add(callSiteCount);
        int currentBsmDataItem = 0;
        // need to traverse to the point in the bootStrapMethodData array where this bsmIndex
        // refers to, problem is that these are variable length since the contain unknown number args each
        while (currentBsmDataItem != bsmIndex) {
          // skip methodhandleref right off, just need to read num args
          long bsmArgumentCount = bsmDataCursor.at(1).longValue();
          bsmDataCursor = bsmDataCursor.add(2 + bsmArgumentCount);
          currentBsmDataItem += 1;
        }

        J9ROMMethodHandleRefPointer methodHandleRef
            = J9ROMMethodHandleRefPointer.cast(constantPool.add(bsmDataCursor.at(0).longValue()));

        bsmDataCursor = bsmDataCursor.add(1);
        J9ROMMethodRefPointer methodRef
            = J9ROMMethodRefPointer.cast(constantPool.add(methodHandleRef.methodOrFieldRefIndex().longValue()));

        // callee
        J9ROMNameAndSignaturePointer nameAndSig = methodRef.nameAndSignature();
        String name = J9UTF8Helper.stringValue(nameAndSig.name());
        String desc = J9UTF8Helper.stringValue(nameAndSig.signature());

        long bsmArgCount = bsmDataCursor.at(0).longValue();
        bsmDataCursor = bsmDataCursor.add(1);

        // map this info into the asm understanding of a handle
        int methodType = methodHandleRef.handleTypeAndCpType().rightShift((int) J9DescriptionCpTypeShift).intValue();
        J9ROMClassRefPointer classRef = J9ROMClassRefPointer.cast(constantPool.add(methodRef.classRefCPIndex().longValue()));
        String owner = J9UTF8Helper.stringValue(classRef.name());
        // public Handle(int tag, String owner, String name, String desc, boolean itf)
        Handle bsm = new Handle(methodType, owner, name, desc, false);
        Object[] bsmArgs = new Object[(int) bsmArgCount];

        // populate the invoked method args array
        for (int i = 0; i < bsmArgCount; i++) {
          long argCPIndex = bsmDataCursor.at(0).longValue();
          bsmArgs[i] = readConst((int) argCPIndex, constantPool);
          bsmDataCursor = bsmDataCursor.add(1);
        }

        // how is providing name+desc not redundant with the bsm handle also haing those...
        mv.visitInvokeDynamicInsn(invokedName, invokedDesc, bsm, bsmArgs);
        ptr += 5;
      } else if ((opcode == BCNames.JBnewdup) || (opcode == BCNames.JBnew) || (opcode == BCNames.JBanewarray)
          || (opcode == BCNames.JBcheckcast) || (opcode == BCNames.JBinstanceof)) {
        // newdup is the only openj9 specific
        if (opcode == BCNames.JBnewdup) {
          opcode = Opcodes.NEW;
        }
        int index = src.getShort(ptr + 1);
        J9ROMConstantPoolItemPointer info = constantPool.add(index);
        String classname = J9UTF8Helper.stringValue(J9ROMStringRefPointer.cast(info).utf8Data());
        mv.visitTypeInsn(opcode, classname);
        ptr += 3;
      } else if (opcode == BCNames.JBsyncReturn0) {
        mv.visitInsn(Opcodes.RETURN);
        ptr += 1;
      } else if ((opcode == BCNames.JBsyncReturn1) || (opcode == BCNames.JBsyncReturn2)) {
        opcode = getReturnType(returnType);
        mv.visitInsn(opcode);
        ptr += 1;
      } else {
        try {
          mv.visitInsn(opcode);
        } catch (Exception e) {
          System.out.println("Encountered some not handled openj9 specific opcode: " + opcode);
        }
        ptr += 1;
      }
    }
  }

  public int getReturnType(char returnType) {

    // default is void return
    int opcode = 177;

    switch (returnType) {
      case 'Z':
      case 'B':
      case 'C':
      case 'S':
      case 'I':
        opcode = 172; // ireturn
        break;
      case 'J':
        opcode = 173; // lreturn
        break;
      case 'F':
        opcode = 174; // freturn
        break;
      case 'D':
        opcode = 175; // dreturn
        break;
      case 'L':
      case '[':
        opcode = 176; // areturn
        break;
      default:
        break; // void
    }

    return opcode;
  }

  // need to find labels before everything else
  public Label[] findLabels(long bytecodeSt, long bytecodeEnd, long ptr, BufferedMemorySource src) throws MemoryFault {
    Label[] labels = new Label[(int) (bytecodeEnd - bytecodeSt)];
    while (ptr < bytecodeEnd) {
      int offset = (int) (ptr - bytecodeSt);
      int opcode = (int) (src.getByte(ptr) & 0xFF);

      if ((opcode == BCNames.JBifeq) || (opcode == BCNames.JBifne) || (opcode == BCNames.JBiflt)
          || (opcode == BCNames.JBifge) || (opcode == BCNames.JBifgt) || (opcode == BCNames.JBifle)
          || (opcode == BCNames.JBificmpeq) || (opcode == BCNames.JBificmpne) || (opcode == BCNames.JBificmplt)
          || (opcode == BCNames.JBificmpge) || (opcode == BCNames.JBificmpgt) || (opcode == BCNames.JBificmple)
          || (opcode == BCNames.JBifacmpeq) || (opcode == BCNames.JBifacmpne) || (opcode == BCNames.JBgoto)
          || (opcode == BCNames.JBifnull) || (opcode == BCNames.JBifnonnull)) {

        createLabel(opcode, ptr, labels, src, bytecodeSt);
        ptr += 3;
      } else if (opcode == BCNames.JBgotow) {
        createLabel(opcode, ptr, labels, src, bytecodeSt);
        ptr += 5;
      } else if (opcode == 196) {
        // 196 == WIDE
        // BCNames does not have a wide opcode...
        // but both Walker https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/compiler/ilgen/Walker.cpp#L1494
        // and ilgen maybe expect it to exist
        // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/compiler/ilgen/J9ByteCode.hpp#L111
        opcode = src.getInt(ptr + 1);
        if (opcode == BCNames.JBiinc) {
          ptr += 6;
        } else {
          ptr += 4;
        }
      } else if ((opcode == BCNames.JBdefaultvalue) || (opcode == BCNames.JBwithfield) || (opcode == BCNames.JBiinc)
          || (opcode == BCNames.JBistore) || (opcode == BCNames.JBlstore) || (opcode == BCNames.JBfstore)
          || (opcode == BCNames.JBdstore) || (opcode == BCNames.JBastore) || (opcode == BCNames.JBiload)
          || (opcode == BCNames.JBlload) || (opcode == BCNames.JBfload) || (opcode == BCNames.JBdload)
          || (opcode == BCNames.JBaload) || (opcode == BCNames.JBnewarray) || (opcode == BCNames.JBbipush)
          || (opcode == BCNames.JBldc)) {
        ptr += 2;
      }

      else if (opcode == BCNames.JBiincw) {
        ptr += 6;
      } else if (opcode == BCNames.JBmultianewarray) {
        ptr += 4;
      }

      else if ((opcode == BCNames.JBnewdup) || (opcode == BCNames.JBnew) || (opcode == BCNames.JBanewarray)
          || (opcode == BCNames.JBcheckcast) || (opcode == BCNames.JBinstanceof) || (opcode == BCNames.JBiinc)
          || (opcode == BCNames.JBnewdup) || (opcode == BCNames.JBinvokehandle) || (opcode == BCNames.JBinvokehandlegeneric)
          || (opcode == BCNames.JBinvokestaticsplit) || (opcode == BCNames.JBinvokespecialsplit)
          || (opcode == BCNames.JBiloadw) || (opcode == BCNames.JBistorew) || (opcode == BCNames.JBlloadw)
          || (opcode == BCNames.JBlstorew) || (opcode == BCNames.JBdloadw) || (opcode == BCNames.JBfloadw)
          || (opcode == BCNames.JBaloadw) || (opcode == BCNames.JBfstorew) || (opcode == BCNames.JBdstorew)
          || (opcode == BCNames.JBastorew) || (opcode == BCNames.JBsipush) || (opcode == BCNames.JBldcw)
          || (opcode == BCNames.JBldc2lw) || (opcode == BCNames.JBldc2dw) || (opcode == BCNames.JBgetstatic)
          || (opcode == BCNames.JBputstatic) || (opcode == BCNames.JBgetfield) || (opcode == BCNames.JBputfield)
          || (opcode == BCNames.JBinvokevirtual) || (opcode == BCNames.JBinvokespecial) || (opcode == BCNames.JBinvokestatic)
          || (opcode == BCNames.JBinvokeinterface)) {
        ptr += 3;
      } else if (opcode == BCNames.JBinvokedynamic) {
        ptr += 5;
      }

      else if (opcode == BCNames.JBtableswitch) {
        ptr = ptr + 4 - (offset & 3);
        // reads the default
        addLabel(labels, offset + src.getInt(ptr));
        int min = src.getInt(ptr + 4);
        int max = src.getInt(ptr + 8);
        Label[] table = new Label[max - min + 1];
        ptr += 12;
        for (int i = 0; i < table.length; ++i) {
          addLabel(labels, offset + src.getInt(ptr));
          ptr += 4;
        }
      } else if (opcode == BCNames.JBlookupswitch) {
        // skips 0 to 3 padding bytes
        ptr = ptr + 4 - (offset & 3);
        // reads instruction
        addLabel(labels, offset + src.getInt(ptr));
        int len = src.getInt(ptr + 4);
        int[] keys = new int[len];
        Label[] values = new Label[len];
        ptr += 8;
        for (int i = 0; i < len; ++i) {
          addLabel(labels, offset + src.getInt(ptr + 4));
          ptr += 8;
        }
      } else {
        ptr += 1;
      }

    }
    return labels;
  }

  // populates a label table for us, to use later, that represent targets of jumps
  public void createLabel(int opcode, long ptr, Label[] labels, BufferedMemorySource src, long methodSt) throws MemoryFault {
    int offset;
    if (opcode == BCNames.JBgotow) {
      offset = src.getInt(ptr + 1);
    } else {
      offset = src.getShort(ptr + 1);
    }
    long target = ptr + offset;
    int labelIndex = (int) (target - methodSt);

    addLabel(labels, labelIndex);
  }

  public void addLabel(Label[] labels, int labelIndex) {
    if (labels[labelIndex] == null) {
      labels[labelIndex] = new Label();
    }

  }

  /*
   * Reads a constant pool info entry
   *
   */

  public Object readConst(final int index, J9ROMConstantPoolItemPointer constantPool) throws CorruptDataException {

    int HEX_RADIX = 16;
    U32Pointer cpShapeDescription = J9ROMClassHelper.cpShapeDescription(pointer);
    long shapeDesc = ConstantPoolHelpers.J9_CP_TYPE(cpShapeDescription, index);
    BufferedMemorySource src = this.cacheMem.getMemorySource();

    J9ROMConstantPoolItemPointer item = constantPool.add(index);

    if (shapeDesc == J9CPTYPE_CLASS) {
      J9ROMClassRefPointer romClassRef = J9ROMClassRefPointer.cast(item);
      return J9UTF8Helper.stringValue(romClassRef.name());
    } else if (shapeDesc == J9CPTYPE_STRING) {
      J9ROMStringRefPointer romStringRef = J9ROMStringRefPointer.cast(item);
      return J9UTF8Helper.stringValue(romStringRef.utf8Data());
    } else if (shapeDesc == J9CPTYPE_INT) {
      J9ROMSingleSlotConstantRefPointer singleSlotConstantRef = J9ROMSingleSlotConstantRefPointer.cast(item);
      return new Integer((int) singleSlotConstantRef.data().longValue());
    } else if (shapeDesc == J9CPTYPE_FLOAT) {
      J9ROMSingleSlotConstantRefPointer singleSlotConstantRef = J9ROMSingleSlotConstantRefPointer.cast(item);
      FloatPointer floatPtr = FloatPointer.cast(singleSlotConstantRef.dataEA());
      return new Float(floatPtr.floatAt(0));
    } else if (shapeDesc == J9CPTYPE_LONG) {
      String hexValue = "";
      if (src.getByteOrder() == ByteOrder.BIG_ENDIAN) {
        hexValue += item.slot1().getHexValue();
        hexValue += item.slot2().getHexValue().substring(2);
      } else {
        hexValue += item.slot2().getHexValue();
        hexValue += item.slot1().getHexValue().substring(2);
      }
      long longValue = Long.parseLong(hexValue.substring(2), HEX_RADIX);
      return new Long(longValue);
    } else if (shapeDesc == J9CPTYPE_DOUBLE) {
      String hexValue = "";
      if (src.getByteOrder() == ByteOrder.BIG_ENDIAN) {
        hexValue += item.slot1().getHexValue();
        hexValue += item.slot2().getHexValue().substring(2);
      } else {
        hexValue += item.slot2().getHexValue();
        hexValue += item.slot1().getHexValue().substring(2);
      }
      long longValue = Long.parseLong(hexValue.substring(2), HEX_RADIX);
      double doubleValue = Double.longBitsToDouble(longValue);
      return new Double(doubleValue);
    } else if (shapeDesc == J9CPTYPE_FIELD) {
      J9ROMFieldRefPointer romFieldRef = J9ROMFieldRefPointer.cast(item);
      J9ROMClassRefPointer classRef = J9ROMClassRefPointer.cast(constantPool.add(romFieldRef.classRefCPIndex()));
      J9ROMNameAndSignaturePointer nameAndSig = romFieldRef.nameAndSignature();
      return J9UTF8Helper.stringValue(nameAndSig.name());
    } else if ((shapeDesc == J9CPTYPE_INSTANCE_METHOD) || (shapeDesc == J9CPTYPE_STATIC_METHOD)
        || (shapeDesc == J9CPTYPE_HANDLE_METHOD) || (shapeDesc == J9CPTYPE_INTERFACE_METHOD)) {
      J9ROMMethodRefPointer romMethodRef = J9ROMMethodRefPointer.cast(item);
      J9ROMClassRefPointer classRef = J9ROMClassRefPointer.cast(constantPool.add(romMethodRef.classRefCPIndex()));
      J9ROMNameAndSignaturePointer nameAndSig = romMethodRef.nameAndSignature();
      return J9UTF8Helper.stringValue(nameAndSig.name());
    } else if (shapeDesc == J9CPTYPE_METHOD_TYPE) {
      J9ROMMethodTypeRefPointer romMethodTypeRef = J9ROMMethodTypeRefPointer.cast(item);
      return Type.getType(J9UTF8Helper.stringValue(J9UTF8Pointer.cast(romMethodTypeRef.signature())));
    } else if (shapeDesc == J9CPTYPE_METHODHANDLE) {
      J9ROMMethodHandleRefPointer methodHandleRef = J9ROMMethodHandleRefPointer.cast(item);
      J9ROMMethodRefPointer methodRef
          = J9ROMMethodRefPointer.cast(constantPool.add(methodHandleRef.methodOrFieldRefIndex()));
      J9ROMClassRefPointer classRef = J9ROMClassRefPointer.cast(constantPool.add(methodRef.classRefCPIndex()));
      J9ROMNameAndSignaturePointer nameAndSig = methodRef.nameAndSignature();
      String owner = J9UTF8Helper.stringValue(classRef.name());
      String name = J9UTF8Helper.stringValue(nameAndSig.name());
      String signature = J9UTF8Helper.stringValue(nameAndSig.signature());

      // for once, asm and romclass constants are fully aligned
      // https://gitlab.ow2.org/asm/asm/blob/ASM_5_2/src/org/objectweb/asm/Opcodes.java#L101
      // https://github.com/eclipse/openj9/blob/v0.14.0-release/runtime/oti/j9nonbuilder.h#L2165
      long methodType = methodHandleRef.handleTypeAndCpType().rightShift((int) J9DescriptionCpTypeShift).longValue();

      boolean itf = false;
      if (methodType == Opcodes.H_INVOKEINTERFACE) {
        itf = true;
      }
      // public Handle(int tag, String owner, String name, String desc, boolean itf)
      return new Handle((int) methodType, owner, name, signature, itf);
    } else {
      System.out.println("      <unknown type>");
      return null;
    }
  }

}
