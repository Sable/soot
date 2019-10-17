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

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.AnnotationConstants;
import soot.tagkit.AnnotationDefaultTag;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityLocalVariableAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;

/**
 * Soot method builder.
 *
 * @author Aaloan Miftah
 */
class MethodBuilder extends JSRInlinerAdapter {

  private TagBuilder tb;
  private VisibilityAnnotationTag[] visibleParamAnnotations;
  private VisibilityAnnotationTag[] invisibleParamAnnotations;
  private List<VisibilityAnnotationTag> visibleLocalVarAnnotations;
  private List<VisibilityAnnotationTag> invisibleLocalVarAnnotations;
  private final SootMethod method;
  private final SootClassBuilder scb;

  MethodBuilder(SootMethod method, SootClassBuilder scb, String desc, String[] ex) {
    super(Opcodes.ASM6, null, method.getModifiers(), method.getName(), desc, null, ex);
    this.method = method;
    this.scb = scb;
  }

  private TagBuilder getTagBuilder() {
    TagBuilder t = tb;
    if (t == null) {
      t = tb = new TagBuilder(method, scb);
    }
    return t;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return getTagBuilder().visitAnnotation(desc, visible);
  }

  @Override
  public AnnotationVisitor visitAnnotationDefault() {
    return new AnnotationElemBuilder(1) {
      @Override
      public void visitEnd() {
        method.addTag(new AnnotationDefaultTag(elems.get(0)));
      }
    };
  }

  @Override
  public void visitAttribute(Attribute attr) {
    getTagBuilder().visitAttribute(attr);
  }

  @Override
  public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start,
      final Label[] end, final int[] index, final String descriptor, final boolean visible) {
    final VisibilityAnnotationTag vat
        = new VisibilityAnnotationTag(visible ? AnnotationConstants.RUNTIME_VISIBLE : AnnotationConstants.RUNTIME_INVISIBLE);
    if (visible) {
      if (visibleLocalVarAnnotations == null) {
        visibleLocalVarAnnotations = new ArrayList<VisibilityAnnotationTag>(2);
      }
      visibleLocalVarAnnotations.add(vat);
    } else {
      if (invisibleLocalVarAnnotations == null) {
        invisibleLocalVarAnnotations = new ArrayList<VisibilityAnnotationTag>(2);
      }
      invisibleLocalVarAnnotations.add(vat);
    }
    return new AnnotationElemBuilder() {
      @Override
      public void visitEnd() {
        AnnotationTag annotTag = new AnnotationTag(desc, elems);
        vat.addAnnotation(annotTag);
      }
    };
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(int parameter, final String desc, boolean visible) {
    VisibilityAnnotationTag vat;
    VisibilityAnnotationTag[] vats;
    if (visible) {
      vats = visibleParamAnnotations;
      if (vats == null) {
        vats = new VisibilityAnnotationTag[method.getParameterCount()];
        visibleParamAnnotations = vats;
      }
      vat = vats[parameter];
      if (vat == null) {
        vat = new VisibilityAnnotationTag(AnnotationConstants.RUNTIME_VISIBLE);
        vats[parameter] = vat;
      }
    } else {
      vats = invisibleParamAnnotations;
      if (vats == null) {
        vats = new VisibilityAnnotationTag[method.getParameterCount()];
        invisibleParamAnnotations = vats;
      }
      vat = vats[parameter];
      if (vat == null) {
        vat = new VisibilityAnnotationTag(AnnotationConstants.RUNTIME_INVISIBLE);
        vats[parameter] = vat;
      }
    }
    final VisibilityAnnotationTag _vat = vat;
    return new AnnotationElemBuilder() {
      @Override
      public void visitEnd() {
        AnnotationTag annotTag = new AnnotationTag(desc, elems);
        _vat.addAnnotation(annotTag);
      }
    };
  }

  @Override
  public void visitTypeInsn(int op, String t) {
    super.visitTypeInsn(op, t);
    Type rt = AsmUtil.toJimpleRefType(t, Optional.fromNullable(this.scb.getKlass().moduleName));
    if (rt instanceof ArrayType) {
      scb.addDep(((ArrayType) rt).baseType);
    } else {
      scb.addDep(rt);
    }
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    super.visitFieldInsn(opcode, owner, name, desc);
    for (Type t : AsmUtil.toJimpleDesc(desc, Optional.fromNullable(this.scb.getKlass().moduleName))) {
      if (t instanceof RefType) {
        scb.addDep(t);
      }
    }

    scb.addDep(AsmUtil.toQualifiedName(owner));
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterf) {
    super.visitMethodInsn(opcode, owner, name, desc, isInterf);
    for (Type t : AsmUtil.toJimpleDesc(desc, Optional.fromNullable(this.scb.getKlass().moduleName))) {
      addDeps(t);
    }

    scb.addDep(AsmUtil.toBaseType(owner, Optional.fromNullable(this.scb.getKlass().moduleName)));
  }

  @Override
  public void visitLdcInsn(Object cst) {
    super.visitLdcInsn(cst);

    if (cst instanceof Handle) {
      Handle methodHandle = (Handle) cst;
      scb.addDep(AsmUtil.toBaseType(methodHandle.getOwner(), Optional.fromNullable(this.scb.getKlass().moduleName)));
    }
  }

  private void addDeps(Type t) {
    if (t instanceof RefType) {
      scb.addDep(t);
    } else if (t instanceof ArrayType) {
      ArrayType at = (ArrayType) t;
      addDeps(at.getElementType());
    }
  }

  @Override
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    super.visitTryCatchBlock(start, end, handler, type);
    if (type != null) {
      scb.addDep(AsmUtil.toQualifiedName(type));
    }
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
    if (visibleParamAnnotations != null) {
      VisibilityParameterAnnotationTag tag
          = new VisibilityParameterAnnotationTag(visibleParamAnnotations.length, AnnotationConstants.RUNTIME_VISIBLE);
      for (VisibilityAnnotationTag vat : visibleParamAnnotations) {
        tag.addVisibilityAnnotation(vat);
      }
      method.addTag(tag);
    }
    if (invisibleParamAnnotations != null) {
      VisibilityParameterAnnotationTag tag
          = new VisibilityParameterAnnotationTag(invisibleParamAnnotations.length, AnnotationConstants.RUNTIME_INVISIBLE);
      for (VisibilityAnnotationTag vat : invisibleParamAnnotations) {
        tag.addVisibilityAnnotation(vat);
      }
      method.addTag(tag);
    }
    if (visibleLocalVarAnnotations != null) {
      VisibilityLocalVariableAnnotationTag tag
          = new VisibilityLocalVariableAnnotationTag(visibleLocalVarAnnotations.size(), AnnotationConstants.RUNTIME_VISIBLE);
      for (VisibilityAnnotationTag vat : visibleLocalVarAnnotations) {
        tag.addVisibilityAnnotation(vat);
      }
      method.addTag(tag);
    }
    if (invisibleLocalVarAnnotations != null) {
      VisibilityLocalVariableAnnotationTag tag
          = new VisibilityLocalVariableAnnotationTag(invisibleLocalVarAnnotations.size(),
          AnnotationConstants.RUNTIME_INVISIBLE);
      for (VisibilityAnnotationTag vat : invisibleLocalVarAnnotations) {
        tag.addVisibilityAnnotation(vat);
      }
      method.addTag(tag);
    }
    if (method.isConcrete()) {
      method.setSource(
          new AsmMethodSource(maxLocals, instructions, localVariables, tryCatchBlocks, scb.getKlass().moduleName));
    }
  }
}
