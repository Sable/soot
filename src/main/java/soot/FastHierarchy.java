package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002 Ondrej Lhotak
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import soot.dotnet.types.DotNetBasicTypes;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.toolkits.typing.fast.WeakObjectType;
import soot.options.Options;
import soot.toolkits.scalar.Pair;
import soot.util.ConcurrentHashMultiMap;
import soot.util.MultiMap;
import soot.util.NumberedString;

/**
 * Represents the class hierarchy. It is closely linked to a Scene, and must be recreated if the Scene changes.
 *
 * <p>
 * This version supercedes the old soot.Hierarchy class.
 *
 * @author Ondrej Lhotak
 * @author Manuel Benz 22.10.19 - Fixed concrete/abstract dispatch methods to car for default interface methods and account
 *         for overwritten return types
 */
public class FastHierarchy {

  protected static final int USE_INTERVALS_BOUNDARY = 100;
  protected final boolean isDotNet = Options.v().src_prec() == Options.src_prec_dotnet;
  protected final int java_version = Options.v().java_version();

  protected Table<SootClass, NumberedString, SootMethod> typeToVtbl = HashBasedTable.create();

  /**
   * This map holds all key,value pairs such that value.getSuperclass() == key. This is one of the three maps that hold the
   * inverse of the relationships given by the getSuperclass and getInterfaces methods of SootClass.
   */
  protected MultiMap<SootClass, SootClass> classToSubclasses = new ConcurrentHashMultiMap<SootClass, SootClass>();

  /**
   * This map holds all key,value pairs such that value is an interface and key is in value.getInterfaces(). This is one of
   * the three maps that hold the inverse of the relationships given by the getSuperclass and getInterfaces methods of
   * SootClass.
   */
  protected MultiMap<SootClass, SootClass> interfaceToSubinterfaces = new ConcurrentHashMultiMap<SootClass, SootClass>();

  /**
   * This map holds all key,value pairs such that value is a class (NOT an interface) and key is in value.getInterfaces().
   * This is one of the three maps that hold the inverse of the relationships given by the getSuperclass and getInterfaces
   * methods of SootClass.
   */
  protected MultiMap<SootClass, SootClass> interfaceToImplementers = new ConcurrentHashMultiMap<SootClass, SootClass>();

  /**
   * This map is a transitive closure of interfaceToSubinterfaces, and each set contains its superinterface itself.
   */
  protected MultiMap<SootClass, SootClass> interfaceToAllSubinterfaces = new ConcurrentHashMultiMap<SootClass, SootClass>();

  /**
   * This map gives, for an interface, all concrete classes that implement that interface and all its subinterfaces, but NOT
   * their subclasses.
   */
  protected MultiMap<SootClass, SootClass> interfaceToAllImplementers = new ConcurrentHashMultiMap<SootClass, SootClass>();

  /**
   * For each class (NOT interface), this map contains a Interval, which is a pair of numbers giving a preorder and postorder
   * ordering of classes in the inheritance tree.
   */
  protected Map<SootClass, Interval> classToInterval = new HashMap<SootClass, Interval>();

  protected Map<Pair<SootClass, SootMethodRef>, Set<SootMethod>> methodRefToAbstractMethods
      = new ConcurrentHashMap<Pair<SootClass, SootMethodRef>, Set<SootMethod>>();

  protected final Scene sc;
  protected final RefType rtObject;
  protected final RefType rtSerializable;
  protected final RefType rtCloneable;
  protected final RefType cilArray;
  protected final RefType cilIcomparable1;
  protected final RefType cilIcomparable;
  protected final RefType cilIconvertible;
  protected final RefType cilIequatable1;
  protected final RefType cilIformattable;
  private Function<Pair<SootClass, SootMethodRef>, Set<SootMethod>> abstractMethodComputation
      = new Function<Pair<SootClass, SootMethodRef>, Set<SootMethod>>() {

        @Override
        public Set<SootMethod> apply(Pair<SootClass, SootMethodRef> arg0) {
          return doResolveAbstractDispatch(arg0.getO1(), arg0.getO2());
        }

      };

  public static class Interval {
    public int lower;
    public int upper;

    public Interval() {
    }

    public Interval(int lower, int upper) {
      this.lower = lower;
      this.upper = upper;
    }

    public boolean isSubrange(Interval potentialSubrange) {
      return (potentialSubrange == this)
          || (potentialSubrange != null && this.lower <= potentialSubrange.lower && this.upper >= potentialSubrange.upper);
    }

    @Override
    public String toString() {
      return String.format("%d - %d", lower, upper);
    }
  }

  protected int dfsVisit(int start, SootClass c) {
    Interval r = new Interval();
    r.lower = start++;
    Collection<SootClass> col = classToSubclasses.get(c);
    if (col != null) {
      for (SootClass sc : col) {
        // For some awful reason, Soot thinks interface are subclasses
        // of java.lang.Object
        if (sc.isInterface()) {
          continue;
        }
        start = dfsVisit(start, sc);
      }
    }
    r.upper = start++;
    if (c.isInterface()) {
      throw new RuntimeException("Attempt to dfs visit interface " + c);
    }
    classToInterval.putIfAbsent(c, r);
    return start;
  }

  /**
   * Returns the interval for a given class or null
   * 
   * @param clz
   *          the class
   * @return the interval for the class, or null if no interval was found
   */
  public Interval getIntervalForClass(SootClass clz) {
    return classToInterval.get(clz);
  }

  /**
   * Constructs a hierarchy from the current scene.
   */
  public FastHierarchy() {
    this.sc = Scene.v();

    this.rtObject = sc.getObjectType();
    this.rtSerializable = RefType.v("java.io.Serializable");
    this.rtCloneable = RefType.v("java.lang.Cloneable");
    this.cilArray = RefType.v(DotNetBasicTypes.SYSTEM_ARRAY);
    // for CIL prim type structs, which implement these interfaces
    this.cilIcomparable = RefType.v(DotNetBasicTypes.SYSTEM_ICOMPARABLE);
    this.cilIcomparable1 = RefType.v(DotNetBasicTypes.SYSTEM_ICOMPARABLE_1);
    this.cilIconvertible = RefType.v(DotNetBasicTypes.SYSTEM_ICONVERTIBLE);
    this.cilIequatable1 = RefType.v(DotNetBasicTypes.SYSTEM_IEQUATABLE_1);
    this.cilIformattable = RefType.v(DotNetBasicTypes.SYSTEM_IFORMATTABLE);

    /* First build the inverse maps. */
    buildInverseMaps();

    /* Now do a dfs traversal to get the Interval numbers. */
    int r = dfsVisit(0, sc.getObjectType().getSootClass());
    /*
     * also have to traverse for all phantom classes because they also can be roots of the type hierarchy
     */
    for (Iterator<SootClass> phantomClassIt = sc.getPhantomClasses().snapshotIterator(); phantomClassIt.hasNext();) {
      SootClass phantomClass = phantomClassIt.next();
      if (!phantomClass.isInterface()) {
        r = dfsVisit(r, phantomClass);
      }
    }
  }

  protected void buildInverseMaps() {
    for (SootClass cl : sc.getClasses().getElementsUnsorted()) {
      if (cl.resolvingLevel() < SootClass.HIERARCHY) {
        continue;
      }
      if (!cl.isInterface()) {
        SootClass superClass = cl.getSuperclassUnsafe();
        if (superClass != null) {
          classToSubclasses.put(superClass, cl);
        }
      }
      for (SootClass supercl : cl.getInterfaces()) {
        if (cl.isInterface()) {
          interfaceToSubinterfaces.put(supercl, cl);
        } else {
          interfaceToImplementers.put(supercl, cl);
        }
      }
    }
  }

  /**
   * Return true if class child is a subclass of class parent, neither of them being allowed to be interfaces. If we don't
   * know any of the classes, we always return false
   */
  public boolean isSubclass(SootClass child, SootClass parent) {
    child.checkLevel(SootClass.HIERARCHY);
    parent.checkLevel(SootClass.HIERARCHY);

    Interval parentInterval = classToInterval.get(parent);
    Interval childInterval = classToInterval.get(child);
    return parentInterval != null && childInterval != null && parentInterval.isSubrange(childInterval);
  }

  /**
   * For an interface parent (MUST be an interface), returns set of all implementers of it but NOT their subclasses.
   *
   * <p>
   * This method can be used concurrently (is thread safe).
   *
   * @param parent
   *          the parent interface.
   * @return an set, possibly empty
   */
  public Set<SootClass> getAllImplementersOfInterface(SootClass parent) {
    parent.checkLevel(SootClass.HIERARCHY);

    Set<SootClass> result = interfaceToAllImplementers.get(parent);
    if (!result.isEmpty()) {
      return result;
    }
    result = new HashSet<>();
    for (SootClass subinterface : getAllSubinterfaces(parent)) {
      if (subinterface == parent) {
        continue;
      }
      result.addAll(getAllImplementersOfInterface(subinterface));
    }
    result.addAll(interfaceToImplementers.get(parent));
    interfaceToAllImplementers.putAll(parent, result);
    return result;
  }

  /**
   * For an interface parent (MUST be an interface), returns set of all subinterfaces including <code>parent</code>.
   *
   * <p>
   * This method can be used concurrently (is thread safe).
   *
   * @param parent
   *          the parent interface.
   * @return an set, possibly empty
   */
  public Set<SootClass> getAllSubinterfaces(SootClass parent) {
    parent.checkLevel(SootClass.HIERARCHY);

    if (!parent.isInterface()) {
      return Collections.emptySet();
    }
    Set<SootClass> result = interfaceToAllSubinterfaces.get(parent);
    if (!result.isEmpty()) {
      return result;
    }
    result = new HashSet<>();
    result.add(parent);
    for (SootClass si : interfaceToSubinterfaces.get(parent)) {
      result.addAll(getAllSubinterfaces(si));
    }
    interfaceToAllSubinterfaces.putAll(parent, result);
    return result;
  }

  /**
   * Given an object of declared type child, returns true if the object can be stored in a variable of type parent. If child
   * is an interface that is not a subinterface of parent, this method will return false even though some objects
   * implementing the child interface may also implement the parent interface.
   */
  public boolean canStoreType(final Type child, final Type parent) {
    if (child == parent || child.equals(parent)) {
      return true;
    } else if (parent instanceof NullType) {
      return false;
    } else if (child instanceof NullType) {
      return parent instanceof RefLikeType;
    } else if (child instanceof RefType) {
      if (parent == rtObject) {
        return true;
      } else if (parent instanceof RefType) {
        return canStoreClass(((RefType) child).getSootClass(), ((RefType) parent).getSootClass());
      } else {
        return false;
      }
    } else if (child instanceof AnySubType) {
      if (!(parent instanceof RefLikeType)) {
        throw new RuntimeException("Unhandled type " + parent + "! Type " + child + " cannot be stored in type " + parent);
      } else if (parent instanceof ArrayType) {
        Type base = ((AnySubType) child).getBase();
        // System.Array base class of arrays in CIL
        if (isDotNet) {
          return base == cilArray;
        }
        // From Java Language Spec 2nd ed., Chapter 10, Arrays
        return base == rtObject || base == rtSerializable || base == rtCloneable || base instanceof WeakObjectType;
      } else {
        // We can store any_subtype_of(x) in a variable of type x. Checked before touching the SootClasses so that
        // phantom bases keep working (see 7bb46b9082).
        RefType childBase = ((AnySubType) child).getBase();
        if (childBase == parent) {
          return true;
        }

        // Likewise, we can store any_subtype_of(x) in a variable of any supertype of x: whatever the concrete runtime
        // type turns out to be, it is a subtype of x and therefore of every supertype of x. This holds even if x is
        // abstract or an interface and no concrete subclass of x is known yet.
        SootClass base = childBase.getSootClass();
        final SootClass parentClass = ((RefType) parent).getSootClass();
        if (canStoreClass(base, parentClass)) {
          return true;
        }

        // If the parent is unrelated to x, this only works if some known concrete subclass of x is cast-compatible to
        // the parent
        Deque<SootClass> worklist = new ArrayDeque<SootClass>();
        if (base.isInterface()) {
          worklist.addAll(getAllImplementersOfInterface(base));
        } else {
          worklist.add(base);
        }
        {
          Set<SootClass> workset = new HashSet<>();
          SootClass cl;
          while ((cl = worklist.poll()) != null) {
            if (!workset.add(cl)) {
              continue;
            } else if (cl.isConcrete() && canStoreClass(cl, parentClass)) {
              return true;
            }
            worklist.addAll(getSubclassesOf(cl));
          }
        }
        return false;
      }
    } else if (child instanceof ArrayType) {
      if (parent instanceof RefType) {
        // base class System.Array for all arrays
        if (isDotNet) {
          return parent == cilArray;
        }
        // From Java Language Spec 2nd ed., Chapter 10, Arrays
        return parent == rtObject || parent == rtSerializable || parent == rtCloneable || parent instanceof WeakObjectType;
      } else if (parent instanceof ArrayType) {
        // You can store a int[][] in a Object[]. Yuck!
        // Also, you can store a Interface[] in a Object[]
        final ArrayType aparent = (ArrayType) parent;
        final ArrayType achild = (ArrayType) child;
        if (achild.numDimensions == aparent.numDimensions) {
          final Type pBaseType = aparent.baseType;
          final Type cBaseType = achild.baseType;
          if (cBaseType.equals(pBaseType)) {
            return true;
          } else if ((cBaseType instanceof RefType) && (pBaseType instanceof RefType)) {
            return canStoreType(cBaseType, pBaseType);
          } else {
            return false;
          }
        } else if (achild.numDimensions > aparent.numDimensions) {
          final Type pBaseType = aparent.baseType;
          if (isDotNet) {
            return pBaseType == cilArray;
          }
          return pBaseType == rtObject || pBaseType == rtSerializable || pBaseType == rtCloneable;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else if (isDotNet && child instanceof PrimType && parent instanceof RefType) {
      // only dotnet
      // if right type prim type struct which implements these interfaces
      // if generic, base class System.Object is possible
      return parent == cilIcomparable || parent == cilIcomparable1 || parent == cilIconvertible || parent == cilIformattable
          || parent == cilIequatable1 || parent == rtObject;
    } else {
      return false;
    }
  }

  /**
   * Given an object of declared type child, returns true if the object can be stored in a variable of type parent. If child
   * is an interface that is not a subinterface of parent, this method will return false even though some objects
   * implementing the child interface may also implement the parent interface.
   */
  public boolean canStoreClass(SootClass child, SootClass parent) {
    parent.checkLevel(SootClass.HIERARCHY);
    child.checkLevel(SootClass.HIERARCHY);

    Interval parentInterval = classToInterval.get(parent);
    Interval childInterval = classToInterval.get(child);
    if (parentInterval != null && childInterval != null) {
      return parentInterval.isSubrange(childInterval);
    } else if (childInterval == null) { // child is interface
      if (parentInterval != null) { // parent is not interface
        return parent == rtObject.getSootClass();
      } else {
        return getAllSubinterfaces(parent).contains(child);
      }
    } else {
      final Set<SootClass> impl = getAllImplementersOfInterface(parent);
      if (impl.size() > USE_INTERVALS_BOUNDARY) {
        // If we have more than 100 entries it is quite time consuming to check each and every
        // implementing class
        // if it is the "child" class. Therefore we use an alternative implementation which just
        // checks the client
        // class it's super classes and all the interfaces it implements.

        return canStoreClassClassic(child, parent);
      } else {
        // If we only have a few entries, you can't beat the performance of a plain old loop
        // in combination with the interval approach.
        for (SootClass c : impl) {
          Interval interval = classToInterval.get(c);
          if (interval != null && interval.isSubrange(childInterval)) {
            return true;
          }
        }
        return false;
      }
    }
  }

  /**
   * "Classic" implementation using the intuitive approach (without using {@link Interval}) to check whether
   * <code>child</code> can be stored in a type of <code>parent</code>:
   *
   * <p>
   * If <code>parent</code> is not an interface we simply traverse and check the super-classes of <code>child</code>.
   *
   * <p>
   * If <code>parent</code> is an interface we traverse the super-classes of <code>child</code> and check each interface
   * implemented by this class. Also each interface is checked recursively for super interfaces it implements.
   *
   * <p>
   * This implementation can be much faster (compared to the interval based implementation of
   * {@link #canStoreClass(SootClass, SootClass)} in cases where one interface is implemented in thousands of classes.
   *
   * @param child
   * @param parent
   * @return
   */
  protected boolean canStoreClassClassic(final SootClass child, final SootClass parent) {
    final boolean parentIsInterface = parent.isInterface();
    ArrayDeque<SootClass> children = new ArrayDeque<>();
    children.add(child);
    for (SootClass p; (p = children.poll()) != null;) {
      for (SootClass sc = p; sc != null;) {
        if (sc == parent) {
          return true;
        }
        if (parentIsInterface) {
          for (SootClass interf : sc.getInterfaces()) {
            if (interf == parent) {
              return true;
            }
            children.push(interf);
          }
        }
        sc = sc.getSuperclassUnsafe();
      }
    }
    return false;
  }

  public Collection<SootMethod> resolveConcreteDispatchWithoutFailing(Collection<Type> concreteTypes, SootMethod m,
      RefType declaredTypeOfBase) {

    final SootClass declaringClass = declaredTypeOfBase.getSootClass();
    declaringClass.checkLevel(SootClass.HIERARCHY);

    Set<SootMethod> ret = new HashSet<SootMethod>();
    for (final Type t : concreteTypes) {
      if (t instanceof AnySubType) {
        HashSet<SootClass> s = new HashSet<SootClass>();
        s.add(declaringClass);
        while (!s.isEmpty()) {
          final SootClass c = s.iterator().next();
          s.remove(c);
          if (!c.isInterface() && !c.isAbstract() && canStoreClass(c, declaringClass)) {
            SootMethod concreteM = resolveConcreteDispatch(c, m);
            if (concreteM != null) {
              ret.add(concreteM);
            }
          }
          {
            Set<SootClass> subclasses = classToSubclasses.get(c);
            if (subclasses != null) {
              s.addAll(subclasses);
            }
          }
          {
            Set<SootClass> subinterfaces = interfaceToSubinterfaces.get(c);
            if (subinterfaces != null) {
              s.addAll(subinterfaces);
            }
          }
          {
            Set<SootClass> implementers = interfaceToImplementers.get(c);
            if (implementers != null) {
              s.addAll(implementers);
            }
          }
        }
        return ret;
      } else if (t instanceof RefType) {
        SootClass concreteClass = ((RefType) t).getSootClass();
        if (!canStoreClass(concreteClass, declaringClass)) {
          continue;
        }
        SootMethod concreteM;
        try {
          concreteM = resolveConcreteDispatch(concreteClass, m);
        } catch (Exception e) {
          concreteM = null;
        }
        if (concreteM != null) {
          ret.add(concreteM);
        }
      } else if (t instanceof ArrayType) {
        SootMethod concreteM;
        try {
          concreteM = resolveConcreteDispatch(sc.getObjectType().getSootClass(), m);
        } catch (Exception e) {
          concreteM = null;
        }
        if (concreteM != null) {
          ret.add(concreteM);
        }
      } else {
        throw new RuntimeException("Unrecognized reaching type " + t);
      }
    }
    return ret;
  }

  public Collection<SootMethod> resolveConcreteDispatch(Collection<Type> concreteTypes, SootMethod m,
      RefType declaredTypeOfBase) {

    final SootClass declaringClass = declaredTypeOfBase.getSootClass();
    declaringClass.checkLevel(SootClass.HIERARCHY);

    Set<SootMethod> ret = new HashSet<SootMethod>();
    for (final Type t : concreteTypes) {
      if (t instanceof AnySubType) {
        HashSet<SootClass> s = new HashSet<SootClass>();
        s.add(declaringClass);
        while (!s.isEmpty()) {
          final SootClass c = s.iterator().next();
          s.remove(c);
          if (!c.isInterface() && !c.isAbstract() && canStoreClass(c, declaringClass)) {
            SootMethod concreteM = resolveConcreteDispatch(c, m);
            if (concreteM != null) {
              ret.add(concreteM);
            }
          }
          {
            Set<SootClass> subclasses = classToSubclasses.get(c);
            if (subclasses != null) {
              s.addAll(subclasses);
            }
          }
          {
            Set<SootClass> subinterfaces = interfaceToSubinterfaces.get(c);
            if (subinterfaces != null) {
              s.addAll(subinterfaces);
            }
          }
          {
            Set<SootClass> implementers = interfaceToImplementers.get(c);
            if (implementers != null) {
              s.addAll(implementers);
            }
          }
        }
        return ret;
      } else if (t instanceof RefType) {
        SootClass concreteClass = ((RefType) t).getSootClass();
        if (!canStoreClass(concreteClass, declaringClass)) {
          continue;
        }
        SootMethod concreteM = resolveConcreteDispatch(concreteClass, m);
        if (concreteM != null) {
          ret.add(concreteM);
        }
      } else if (t instanceof ArrayType) {
        SootMethod concreteM = resolveConcreteDispatch(rtObject.getSootClass(), m);
        if (concreteM != null) {
          ret.add(concreteM);
        }
      } else {
        throw new RuntimeException("Unrecognized reaching type " + t);
      }
    }
    return ret;
  }

  /**
   * Returns true if a method defined in declaringClass with the given modifiers is visible from the class from.
   */
  private boolean isVisible(SootClass from, SootClass declaringClass, int modifier) {
    from.checkLevel(SootClass.HIERARCHY);

    if (Modifier.isPublic(modifier)) {
      return true;
    }

    // If two inner classes are (transitively) inside the same outer class, such as A$B$C and A$D$E they can override methods
    // from one another, even if all methods are private. In the example, it's perfectly fine for private class A$D$E to
    // extend private class A$B$C and override a method in it.
    for (SootClass curDecl = declaringClass; curDecl.hasOuterClass();) {
      curDecl = curDecl.getOuterClass();
      if (from.equals(curDecl)) {
        return true;
      }

      for (SootClass curFrom = from; curFrom.hasOuterClass();) {
        curFrom = curFrom.getOuterClass();
        if (curDecl.equals(curFrom)) {
          return true;
        }
      }
    }

    if (Modifier.isPrivate(modifier)) {
      return from.equals(declaringClass);
    }
    if (Modifier.isProtected(modifier)) {
      return canStoreClass(from, declaringClass);
    }
    // m is package
    return from.getJavaPackageName().equals(declaringClass.getJavaPackageName());
  }

  /**
   * Given an object of declared type C, returns the methods which could be called on an o.f() invocation.
   *
   * @param baseType
   *          The declared type C
   */
  public Set<SootMethod> resolveAbstractDispatch(SootClass baseType, SootMethod m) {
    return resolveAbstractDispatch(baseType, m.makeRef());
  }

  /**
   * Given an object of declared type C, returns the methods which could be called on an o.f() invocation.
   *
   * @param baseType
   *          The declared type C
   */
  public Set<SootMethod> resolveAbstractDispatch(SootClass baseType, SootMethodRef m) {
    Pair<SootClass, SootMethodRef> pair = new Pair<>(baseType, m);
    return methodRefToAbstractMethods.computeIfAbsent(pair, abstractMethodComputation);
  }

  protected Set<SootMethod> doResolveAbstractDispatch(SootClass baseType, SootMethodRef m) {
    HashSet<SootClass> resolved = new HashSet<>();
    HashSet<SootMethod> ret = new HashSet<>();

    ArrayDeque<SootClass> worklist = new ArrayDeque<SootClass>() {
      @Override
      public boolean addAll(Collection<? extends SootClass> c) {
        boolean b = false;
        for (SootClass e : c) {
          if (add(e)) {
            b = true;
          }
        }
        return b;
      }

      @Override
      public boolean add(SootClass e) {
        if (resolved.contains(e) && classToSubclasses.get(e).isEmpty()) {
          return false;
        }
        return super.add(e);
      }
    };
    worklist.add(baseType);
    while (true) {
      SootClass concreteType = worklist.poll();
      if (concreteType == null) {
        break;
      }

      if (concreteType.isInterface()) {
        worklist.addAll(getAllImplementersOfInterface(concreteType));
        continue;
      } else {
        Collection<SootClass> c = classToSubclasses.get(concreteType);
        if (c != null) {
          worklist.addAll(c);
        }
      }

      if (!resolved.contains(concreteType)) {
        SootMethod resolvedMethod = resolveMethod(concreteType, m.getDeclaringClass(), m.getName(), m.getParameterTypes(),
            m.getReturnType(), false, false, resolved, m.getSubSignature());
        if (resolvedMethod != null) {
          ret.add(resolvedMethod);
        }
      }
    }

    return ret;
  }

  /**
   * Given an object of actual type C (o = new C()), returns the method which will be called on an o.f() invocation.
   *
   * @param baseType
   *          The actual type C
   */
  public SootMethod resolveConcreteDispatch(SootClass baseType, SootMethod m) {
    return resolveConcreteDispatch(baseType, m.makeRef());
  }

  /**
   * Given an object of actual type C (o = new C()), returns the method which will be called on an o.f() invocation.
   *
   * @param baseType
   *          The actual type C
   */
  public SootMethod resolveConcreteDispatch(SootClass baseType, SootMethodRef m) {
    baseType.checkLevel(SootClass.HIERARCHY);
    if (baseType.isInterface()) {
      throw new RuntimeException("A concrete type cannot be an interface: " + baseType);
    }

    return resolveMethod(baseType, m, false);
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param m
   *          The method f to resolve
   * @return The concrete method o.f() to call
   */
  public SootMethod resolveMethod(SootClass baseType, SootMethod m, boolean allowAbstract) {
    return resolveMethod(baseType, m.makeRef(), allowAbstract);
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param m
   *          The method f to resolve
   * @return The concrete method o.f() to call
   */
  public SootMethod resolveMethod(SootClass baseType, SootMethodRef m, boolean allowAbstract) {
    return resolveMethod(baseType, m, allowAbstract, new HashSet<>());
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * *
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to * javas method resolution
   * process: * https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param m
   *          The method f to resolve
   * @param ignoreList
   *          A set of classes that should be ignored during dispatch. This set will also be modified since every traversed
   *          class/interface will be added. This is required for the abstract dispatch to not do additional resolving effort
   *          by resolving the same classes multiple times.
   * @return The concrete method o.f() to call
   */
  private SootMethod resolveMethod(SootClass baseType, SootMethodRef m, boolean allowAbstract, Set<SootClass> ignoreList) {
    return resolveMethod(baseType, m.getDeclaringClass(), m.getName(), m.getParameterTypes(), m.getReturnType(),
        allowAbstract, false, ignoreList, m.getSubSignature());
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param declaringClass
   *          declaring class of the method to resolve
   * @param name
   *          Name of the method to resolve
   * @return The concrete method o.f() to call
   */
  public SootMethod resolveMethod(SootClass baseType, SootClass declaringClass, String name, List<Type> parameterTypes,
      Type returnType, boolean allowAbstract) {
    return resolveMethod(baseType, declaringClass, name, parameterTypes, returnType, allowAbstract, false, new HashSet<>(),
        null);
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param declaringClass
   *          declaring class of the method to resolve
   * @param name
   *          Name of the method to resolve
   * @param subsignature
   *          The subsignature (can be null) to speed up the resolving process.
   * @return The concrete method o.f() to call
   */
  public SootMethod resolveMethod(SootClass baseType, SootClass declaringClass, String name, List<Type> parameterTypes,
      Type returnType, boolean allowAbstract, NumberedString subsignature) {
    return resolveMethod(baseType, declaringClass, name, parameterTypes, returnType, allowAbstract, false, new HashSet<>(),
        subsignature);
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param declaringClass
   *          declaring class of the method to resolve
   * @param name
   *          Name of the method to resolve
   * @param subsignature
   *          The subsignature (can be null) to speed up the resolving process.
   * @param allowStatic
   *          Whether static methods are allowed as result
   * @return The concrete method o.f() to call
   */
  public SootMethod resolveMethod(SootClass baseType, SootClass declaringClass, String name, List<Type> parameterTypes,
      Type returnType, boolean allowAbstract, boolean allowStatic, NumberedString subsignature) {
    return resolveMethod(baseType, declaringClass, name, parameterTypes, returnType, allowAbstract, allowStatic,
        new HashSet<>(), subsignature);
  }

  /**
   * Conducts the actual dispatch by searching up the baseType's superclass hierarchy and interface hierarchy if the
   * sourcecode level is beyond Java 7 (due to default interface methods.) Given an object of actual type C (o = new C()),
   * returns the method which will be called on an o.f() invocation.
   *
   * <p>
   * If abstract methods are allowed, it will just resolve to the first method found according to javas method resolution
   * process: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
   *
   * @param baseType
   *          The type C
   * @param declaringClass
   *          declaring class of the method to resolve
   * @param name
   *          Name of the method to resolve
   * @param ignoreList
   *          A set of classes that should be ignored during dispatch. This set will also be modified since every traversed
   *          class/interface will be added. This is required for the abstract dispatch to not do additional resolving effort
   *          by resolving the same classes multiple times.
   * @param allowStatic
   *          Whether static methods are allowed as result
   * @param subsignature
   *          The subsignature (can be null) to speed up the resolving process.
   * @return The concrete method o.f() to call
   */
  protected SootMethod resolveMethod(final SootClass baseType, final SootClass declaringClass, final String name,
      final List<Type> parameterTypes, final Type returnType, final boolean allowAbstract, final boolean allowStatic,
      final Set<SootClass> ignoreList, NumberedString subsignature) {
    final NumberedString methodSignature;
    if (subsignature == null) {
      methodSignature
          = Scene.v().getSubSigNumberer().findOrAdd(SootMethod.getSubSignature(name, parameterTypes, returnType));
    } else {
      methodSignature = subsignature;
    }

    // When there is no proper dispatch found, we simply return null to let the caller decide what to do
    SootMethod candidate = null;
    boolean calleeExist = declaringClass.getMethodUnsafe(subsignature) != null;
    for (SootClass concreteType = baseType; concreteType != null;) {
      SootMethod previouslyResolvedMethod = typeToVtbl.get(concreteType, methodSignature);
      if (previouslyResolvedMethod != null && previouslyResolvedMethod.getDeclaringClass() != null) {
        if (!previouslyResolvedMethod.getDeclaringClass().isInterface()) {
          if (baseType != concreteType) {
            typeToVtbl.put(baseType, methodSignature, previouslyResolvedMethod);
          }
          return previouslyResolvedMethod;
        } else {
          // Hmm... we might have another, more specific implementation in one of our interfaces (see comments below
          // for default interfaces)
          candidate = previouslyResolvedMethod;
          break;
        }
      }

      candidate = getSignaturePolymorphicMethod(concreteType, name, parameterTypes, returnType);
      if (candidate != null && (allowStatic || !candidate.isStatic())) {
        if (!calleeExist || isVisible(concreteType, declaringClass, candidate.getModifiers())) {
          if (!allowAbstract && candidate.isAbstract()) {
            candidate = null;
            break;
          }

          if (!candidate.isAbstract()) {
            typeToVtbl.put(baseType, methodSignature, candidate);
          }
          return candidate;
        }
      }

      concreteType = concreteType.getSuperclassUnsafe();
    }

    // for java > 7 we have to go through the interface hierarchy after the superclass hierarchy to
    // look for default methods:
    // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.3
    if (isHandleDefaultMethods()) {
      // keep our own ignorelist here so we are not restricted to already hit super interfaces when
      // determining the most specific super interface
      HashSet<SootClass> interfaceIgnoreList = new HashSet<>();
      for (SootClass concreteType = baseType; concreteType != null;) {
        Queue<SootClass> worklist = new ArrayDeque<>(concreteType.getInterfaces());
        // we have to determine the "most specific super interface"
        while (!worklist.isEmpty()) {
          SootClass iFace = worklist.poll();

          if (!interfaceIgnoreList.add(iFace)) {
            continue;
          }

          SootMethod method = getSignaturePolymorphicMethod(iFace, name, parameterTypes, returnType);
          if (method != null && isVisible(declaringClass, iFace, method.getModifiers())) {
            if ((!allowAbstract && method.isAbstract()) || (!allowStatic && method.isStatic())) {
              // abstract/static method cannot be dispatched
            } else if (candidate == null || canStoreClass(method.getDeclaringClass(), candidate.getDeclaringClass())) {
              // the found method is more specific than our current candidate
              candidate = method;
            }
          } else {
            // go up the interface hierarchy
            worklist.addAll(iFace.getInterfaces());
          }
        }

        // we also have to search upwards the class hierarchy again to find the most specific
        // super interface
        concreteType = concreteType.getSuperclassUnsafe();
      }

      ignoreList.addAll(interfaceIgnoreList);
    }

    if (candidate != null &&
    // We cannot use it in the vtable, since others might ask for
    // concrete methods
        !candidate.isAbstract() && !allowStatic) {
      typeToVtbl.put(baseType, methodSignature, candidate);
    }
    return candidate;
  }

  protected boolean isHandleDefaultMethods() {
    int version = java_version;
    return version == 0 || version > 7;
  }

  /** Returns the target for the given SpecialInvokeExpr. */
  public SootMethod resolveSpecialDispatch(SootMethod callee, SootMethod container) {
    /*
     * This is a bizarre condition! Hopefully the implementation is correct. See VM Spec, 2nd Edition, Chapter 6, in the
     * definition of invokespecial.
     */
    final SootClass containerClass = container.getDeclaringClass();
    final SootClass calleeClass = callee.getDeclaringClass();
    if (containerClass.getType() != calleeClass.getType() && canStoreType(containerClass.getType(), calleeClass.getType())
        && !SootMethod.constructorName.equals(callee.getName()) && !SootMethod.staticInitializerName.equals(callee.getName())
        // default interface methods are explicitly dispatched to the default
        // method with a specialinvoke instruction (i.e. do not dispatch to an
        // overwritten version of that method)
        && !calleeClass.isInterface()) {
      return resolveConcreteDispatch(containerClass, callee);
    } else {
      return callee;
    }
  }

  /**
   * Searches the given class for a method that is signature polymorphic according to the given facts, i.e., matches name and
   * parameter types and ensures that the return type is a an equal or subtype of the given method's subtype.
   *
   * @param concreteType
   * @return
   */
  private SootMethod getSignaturePolymorphicMethod(SootClass concreteType, String name, List<Type> parameterTypes,
      Type returnType) {
    if (concreteType == null) {
      throw new RuntimeException("The concreteType cannot not be null!");
    }
    SootMethod candidate = null;
    for (SootMethod method : concreteType.getMethodsByNameAndParamCount(name, parameterTypes.size())) {
      if (method.getParameterTypes().equals(parameterTypes) && canStoreType(method.getReturnType(), returnType)) {
        if (candidate == null || returnType.equals(method.getReturnType())) {
          candidate = method;
          returnType = method.getReturnType();
        }
      }
      // if dotnet structs or generics
      if (isDotNet) {
        if (method.getParameterCount() == parameterTypes.size() && canStoreType(returnType, method.getReturnType())) {
          boolean canStore = true;
          for (int i = 0; i < method.getParameterCount(); i++) {
            Type methodParameter = method.getParameterType(i);
            Type calleeParameter = parameterTypes.get(i);
            // base class can System.Object
            if (!(methodParameter.equals(calleeParameter) || canStoreType(calleeParameter, methodParameter))) {
              canStore = false;
            }
          }
          if (canStore) {
            candidate = method;
            returnType = method.getReturnType();
          }
        }
      }
    }
    return candidate;
  }

  /**
   * Gets the direct subclasses of a given class. The class needs to be resolved at least at the HIERARCHY level.
   *
   * @param c
   *          the class
   * @return a collection (possibly empty) of the direct subclasses
   */
  public Collection<SootClass> getSubclassesOf(SootClass c) {
    c.checkLevel(SootClass.HIERARCHY);
    Set<SootClass> ret = classToSubclasses.get(c);
    return (ret == null) ? Collections.emptySet() : ret;
  }

  /**
   * Returns a list of types which can be used to store the given type
   *
   * @param nt
   *          the given type
   * @return the list of types which can be used to store the given type
   */
  public Iterable<Type> canStoreTypeList(final Type nt) {
    return new Iterable<Type>() {

      @Override
      public Iterator<Type> iterator() {
        Iterator<Type> it = Scene.v().getTypeNumberer().iterator();
        return new Iterator<Type>() {

          Type crt = null;

          @Override
          public boolean hasNext() {
            if (crt != null) {
              return true;
            }
            Type c = null;
            while (it.hasNext()) {
              c = it.next();
              if (TypeManager.isUnresolved(c)) {
                continue;
              }
              if (canStoreType(nt, c)) {
                crt = c;
                return true;
              }
            }
            return false;
          }

          @Override
          public Type next() {
            Type old = crt;
            crt = null;
            hasNext();
            return old;
          }

        };
      }
    };
  }

}
