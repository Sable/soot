package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
 * Copyright (C) 2004 Ondrej Lhotak
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

import java.util.Objects;
import soot.jimple.paddle.PaddleField;
import soot.jimple.spark.pag.SparkField;
import soot.options.Options;
import soot.tagkit.AbstractHost;
import soot.util.Numberable;

/**
 * Soot representation of a Java field. Can be declared to belong to a SootClass.
 */
public class SootField extends AbstractHost implements ClassMember, SparkField, Numberable, PaddleField {

  protected String name;
  protected Type type;
  protected int modifiers;
  protected boolean isDeclared = false;
  protected SootClass declaringClass;
  protected boolean isPhantom = false;
  protected volatile String sig;
  protected volatile String subSig;
  private int number = 0;

  /**
   * Constructs a Soot field with the given name, type and modifiers.
   */
  public SootField(String name, Type type, int modifiers) {
    if (name == null || type == null) {
      throw new RuntimeException("A SootField cannot have a null name or type.");
    }
    this.name = name;
    this.type = type;
    this.modifiers = modifiers;
  }

  /**
   * Constructs a Soot field with the given name, type and no modifiers.
   */
  public SootField(String name, Type type) {
    this(name, type, 0);
  }

  public int equivHashCode() {
    return type.hashCode() * 101 + modifiers * 17 + name.hashCode();
  }

  public String getSignature() {
    if (sig == null) {
      synchronized (this) {
        if (sig == null) {
          sig = getSignature(getDeclaringClass(), getSubSignature());
        }
      }
    }
    return sig;
  }

  public static String getSignature(SootClass cl, String name, Type type) {
    return getSignature(cl, getSubSignature(name, type));
  }

  public static String getSignature(SootClass cl, String subSignature) {
    StringBuilder buffer = new StringBuilder();
    buffer.append('<').append(Scene.v().quotedNameOf(cl.getName())).append(": ");
    buffer.append(subSignature).append('>');
    return buffer.toString();
  }

  public String getSubSignature() {
    if (subSig == null) {
      synchronized (this) {
        if (subSig == null) {
          subSig = getSubSignature(getName(), getType());
        }
      }
    }
    return subSig;
  }

  protected static String getSubSignature(String name, Type type) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(type.toQuotedString()).append(' ').append(Scene.v().quotedNameOf(name));
    return buffer.toString();
  }

  @Override
  public SootClass getDeclaringClass() {
    if (!isDeclared) {
      throw new RuntimeException("not declared: " + getName() + " " + getType());
    }

    return declaringClass;
  }

  public synchronized void setDeclaringClass(SootClass sc) {
    if (sc != null && type instanceof RefLikeType) {
      Scene.v().getFieldNumberer().add(this);
    }
    this.declaringClass = sc;
    this.sig = null;
  }

  @Override
  public boolean isPhantom() {
    return isPhantom;
  }

  @Override
  public void setPhantom(boolean value) {
    if (value) {
      if (!Scene.v().allowsPhantomRefs()) {
        throw new RuntimeException("Phantom refs not allowed");
      }
      if (!Options.v().allow_phantom_elms() && declaringClass != null && !declaringClass.isPhantom()) {
        throw new RuntimeException("Declaring class would have to be phantom");
      }
    }
    isPhantom = value;
  }

  @Override
  public boolean isDeclared() {
    return isDeclared;
  }

  public void setDeclared(boolean isDeclared) {
    this.isDeclared = isDeclared;
  }

  public String getName() {
    return name;
  }

  public synchronized void setName(String name) {
    if (name != null) {
      this.name = name;
      this.sig = null;
      this.subSig = null;
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  public synchronized void setType(Type t) {
    if (t != null) {
      this.type = t;
      this.sig = null;
      this.subSig = null;
    }
  }

  /**
   * Convenience method returning true if this field is public.
   */
  @Override
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is protected.
   */
  @Override
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is private.
   */
  @Override
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is static.
   */
  @Override
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is final.
   */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  @Override
  public void setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  @Override
  public int getModifiers() {
    return modifiers;
  }

  @Override
  public String toString() {
    return getSignature();
  }

  private String getOriginalStyleDeclaration() {
    String qualifiers = (Modifier.toString(modifiers) + ' ' + type.toQuotedString()).trim();
    if (qualifiers.isEmpty()) {
      return Scene.v().quotedNameOf(name);
    } else {
      return qualifiers + ' ' + Scene.v().quotedNameOf(name);
    }

  }

  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }

  @Override
  public final int getNumber() {
    return number;
  }

  @Override
  public final void setNumber(int number) {
    this.number = number;
  }

  public SootFieldRef makeRef() {
    return Scene.v().makeFieldRef(declaringClass, name, type, isStatic());
  }

  public boolean isValidResolve(SootFieldRef f) {
    return (this.isStatic() == f.isStatic()) && Objects.equals(this.getDeclaringClass(), f.declaringClass())
        && Objects.equals(this.getName(), f.name()) && Objects.equals(this.getType(), f.type());
  }
}
