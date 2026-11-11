package soot.jimple.toolkits.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.AnySubType;
import soot.ArrayType;
import soot.FastHierarchy;
import soot.G;
import soot.NullType;
import soot.PhaseOptions;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.Singletons;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.options.CGOptions;
import soot.toolkits.scalar.Pair;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import soot.util.queue.ChunkedQueue;

/**
 * Resolves virtual calls.
 *
 * @author Ondrej Lhotak
 * @author Manuel Benz 22.10.19 - Delegate dispatch behavior to FastHierarchy to have one common place for extension
 */
public class VirtualCalls {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualCalls.class);

  private final CGOptions options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));

  protected MultiMap<Pair<Type, SootMethodRef>, Pair<Type, SootMethodRef>> baseToPossibleSubTypes = new HashMultiMap<>();

  public VirtualCalls(Singletons.Global g) {
  }

  public static VirtualCalls v() {
    return G.v().soot_jimple_toolkits_callgraph_VirtualCalls();
  }

  public SootMethod resolveSpecial(SootMethodRef calleeRef, SootMethod container) {
    return resolveSpecial(calleeRef, container, false);
  }

  public SootMethod resolveSpecial(SootMethodRef calleeRef, SootMethod container, boolean appOnly) {
    SootMethod callee = calleeRef.resolve();
    /* cf. JVM spec, invokespecial instruction */
    final SootClass containerCls = container.getDeclaringClass();
    final SootClass calleeCls = callee.getDeclaringClass();
    if (containerCls.getType() != calleeCls.getType()
        && Scene.v().getOrMakeFastHierarchy().canStoreType(containerCls.getType(), calleeCls.getType())
        && !SootMethod.constructorName.equals(callee.getName()) && !SootMethod.staticInitializerName.equals(callee.getName())
        // default interface methods are explicitly dispatched to the default
        // method with a specialinvoke instruction (i.e. do not dispatch to an
        // overwritten version of that method)
        && !calleeCls.isInterface()) {
      // The invokespecial instruction is used to invoke instance initialization methods as well
      // as private methods and methods of a superclass of the current class.
      return resolveNonSpecial(containerCls.getSuperclass().getType(), calleeRef, appOnly);
    } else {
      return callee;
    }
  }

  public SootMethod resolveNonSpecial(RefType t, SootMethodRef callee) {
    return resolveNonSpecial(t, callee, false);
  }

  public SootMethod resolveNonSpecial(RefType t, SootMethodRef callee, boolean appOnly) {
    SootClass cls = t.getSootClass();
    Scene scene = Scene.v();
    boolean restrictedLibrary = appOnly && cls.isLibraryClass();
    if (restrictedLibrary && !scene.hasLibraryMethodFilter()) {
      return null;
    }
    if (cls.isInterface()) {
      return null;
    }
    SootMethod target = scene.getOrMakeFastHierarchy().resolveConcreteDispatch(cls, callee);
    if (restrictedLibrary && target != null && !scene.isLibraryMethodAnalysisAllowed(target)) {
      return null;
    }
    return target;
  }

  public void resolve(Type t, Type declaredType, SootMethodRef callee, SootMethod container,
      ChunkedQueue<SootMethod> targets) {
    resolve(t, declaredType, null, callee, container, targets);
  }

  public void resolve(Type t, Type declaredType, SootMethodRef callee, SootMethod container,
      ChunkedQueue<SootMethod> targets, boolean appOnly) {
    resolve(t, declaredType, null, callee, container, targets, appOnly);
  }

  public void resolve(Type t, Type declaredType, Type sigType, SootMethodRef callee, SootMethod container,
      ChunkedQueue<SootMethod> targets) {
    resolve(t, declaredType, sigType, callee, container, targets, false);
  }

  public void resolve(Type t, Type declaredType, Type sigType, SootMethodRef callee, SootMethod container,
      ChunkedQueue<SootMethod> targets, boolean appOnly) {
    if (declaredType instanceof ArrayType) {
      declaredType = Scene.v().getObjectType();
    }
    if (sigType instanceof ArrayType) {
      sigType = Scene.v().getObjectType();
    }
    if (t instanceof ArrayType) {
      t = Scene.v().getObjectType();
    }
    FastHierarchy fastHierachy = Scene.v().getOrMakeFastHierarchy();
    if (declaredType != null && !fastHierachy.canStoreType(t, declaredType)) {
      return;
    } else if (sigType != null && !fastHierachy.canStoreType(t, sigType)) {
      return;
    } else if (t instanceof RefType) {
      SootMethod target = resolveNonSpecial((RefType) t, callee, appOnly);
      if (target != null) {
        targets.add(target);
      }
    } else if (t instanceof AnySubType) {
      RefType base = ((AnySubType) t).getBase();

      /*
       * Whenever any sub type of a specific type is considered as receiver for a method to call and the base type is an
       * interface, calls to existing methods with matching signature (possible implementation of method to call) are also
       * added. As Javas' subtyping allows contra-variance for return types and co-variance for parameters when overriding a
       * method, these cases are also considered here.
       *
       * Example: Classes A, B (B sub type of A), interface I with method public A foo(B b); and a class C with method public
       * B foo(A a) { ... }. The extended class hierarchy will contain C as possible implementation of I.
       *
       * Since Java has no multiple inheritance call by signature resolution is only activated if the base is an interface.
       */
      if (options.library() == CGOptions.library_signature_resolution && base.getSootClass().isInterface()) {
        LOGGER.warn("Deprecated library dispatch is conducted. The results might be unsound...");
        resolveLibrarySignature(declaredType, sigType, callee, container, targets, appOnly, base);
      } else {
        for (SootMethod dispatch : Scene.v().getOrMakeFastHierarchy().resolveAbstractDispatch(base.getSootClass(), callee)) {
          targets.add(dispatch);
        }
      }
    } else if (t instanceof NullType) {
      return;
    } else if (t instanceof PrimType) {
      return;
    } else {
      throw new RuntimeException("oops " + t);
    }
  }

  public void resolveSuperType(Type t, Type declaredType, SootMethodRef callee, ChunkedQueue<SootMethod> targets,
      boolean appOnly) {
    if (declaredType == null || t == null) {
      return;
    }
    if (declaredType instanceof ArrayType) {
      declaredType = Scene.v().getObjectType();
    }
    if (t instanceof ArrayType) {
      t = Scene.v().getObjectType();
    }
    if (declaredType instanceof RefType) {
      RefType child;
      if (t instanceof AnySubType) {
        child = ((AnySubType) t).getBase();
      } else if (t instanceof RefType) {
        child = (RefType) t;
      } else {
        return;
      }
      FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
      if (fh.canStoreClass(child.getSootClass(), ((RefType) declaredType).getSootClass())) {
        SootMethod target = resolveNonSpecial(child, callee, appOnly);
        if (target != null) {
          targets.add(target);
        }
      }
    }
  }

  @Deprecated
  protected void resolveLibrarySignature(Type declaredType, Type sigType, SootMethodRef callee, SootMethod container,
      ChunkedQueue<SootMethod> targets, boolean appOnly, RefType base) {
    // This is an old piece of code from before the refactoring of dispatch behavior to
    // FastHierarchy
    // This cannot handle default interfaces and it's questionable if the logic makes sense. The
    // author states that Java allows for co-variant parameters which is not true (it will introduce
    // overloading in this case) and co-variance for return values is already managed by the
    // FastHierarchy

    FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();

    assert (declaredType instanceof RefType);
    Pair<Type, SootMethodRef> pair = new Pair<Type, SootMethodRef>(base, callee);
    {
      Set<Pair<Type, SootMethodRef>> types = baseToPossibleSubTypes.get(pair);
      // if this type and method has been resolved earlier we can
      // just retrieve the previous result.
      if (types != null) {
        for (Pair<Type, SootMethodRef> tuple : types) {
          Type st = tuple.getO1();
          if (!fh.canStoreType(st, declaredType)) {
            resolve(st, st, sigType, callee, container, targets, appOnly);
          } else {
            resolve(st, declaredType, sigType, callee, container, targets, appOnly);
          }
        }
        return;
      }
    }

    Set<Pair<Type, SootMethodRef>> types = new HashSet<>();

    Type declaredReturnType = callee.getReturnType();
    List<Type> declaredParamTypes = callee.getParameterTypes();
    String declaredName = callee.getName();
    for (SootClass sc : Scene.v().getClasses()) {
      for (SootMethod sm : sc.getMethods()) {
        if (!sm.isAbstract()) {
          // method name has to match
          // the return type has to be a the declared return
          // type or a sub type of it
          if (!sm.getName().equals(declaredName) || !fh.canStoreType(sm.getReturnType(), declaredReturnType)) {
            continue;
          }
          List<Type> paramTypes = sm.getParameterTypes();

          // method parameters have to match to the declared
          // ones (same type or super type).
          if (declaredParamTypes.size() != paramTypes.size()) {
            continue;
          }
          boolean check = true;
          for (int i = 0; i < paramTypes.size(); i++) {
            if (!fh.canStoreType(declaredParamTypes.get(i), paramTypes.get(i))) {
              check = false;
              break;
            }
          }

          if (check) {
            Type st = sc.getType();
            if (!fh.canStoreType(st, declaredType)) {
              // final classes can not be extended and
              // therefore not used in library client
              if (!sc.isFinal()) {
                resolve(st, st, sigType, sm.makeRef(), container, targets, appOnly);
                types.add(new Pair<Type, SootMethodRef>(st, sm.makeRef()));
              }
            } else {
              resolve(st, declaredType, sigType, callee, container, targets, appOnly);
              types.add(new Pair<Type, SootMethodRef>(st, callee));
            }
          }
        }
      }
    }
    baseToPossibleSubTypes.putAll(pair, types);
  }
}
