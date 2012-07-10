package soot.asm;

import java.util.ArrayList;
import java.util.List;

import soot.*;

/**
 * Contains static utility methods.
 */
class AsmUtil {
	
	/**
	 * Determines if a type is a dword type.
	 * @param type the type to check.
	 * @return {@code true} if its a dword type.
	 */
	public static boolean isDWord(Type type) {
		return type instanceof LongType || type instanceof DoubleType;
	}
	
	/**
	 * Converts an internal class name to a fully qualified name.
	 * @param internal internal name.
	 * @return fully qualified name.
	 */
	public static String toBaseQualifiedName(String internal) {
		if (internal.charAt(0) == '[') {
			/* [Ljava/lang/Object; */
			internal = internal.substring(internal.indexOf('L') + 1, internal.length() - 1);
		}
		return internal.replace('/', '.');
	}
	
	/**
	 * Converts an internal class name to a fully qualified name.
	 * @param internal internal name.
	 * @return fully qualified name.
	 */
	public static String toQualifiedName(String internal) {
		return internal.replace('/', '.');
	}
	
	/**
	 * Converts a fully qualified class name to an internal name.
	 * @param qual fully qualified class name.
	 * @return internal name.
	 */
	public static String toInternalName(String qual) {
		return qual.replace('.', '/');
	}
	
	/**
	 * Determines and returns the internal name of a class.
	 * @param cls the class.
	 * @return corresponding internal name.
	 */
	public static String toInternalName(SootClass cls) {
		return toInternalName(cls.getName());
	}
	
	/**
	 * Converts a type descriptor to a Jimple reference type.
	 * @param desc the descriptor.
	 * @return the reference type.
	 */
	public static Type toJimpleRefType(String desc) {
		return desc.charAt(0) == '[' ?
				toJimpleType(desc) : RefType.v(toQualifiedName(desc));
	}
	
	/**
	 * Converts a type descriptor to a Jimple type.
	 * @param desc the descriptor.
	 * @return equivalent Jimple type.
	 */
	public static Type toJimpleType(String desc) {
		int idx = desc.lastIndexOf('[');
		int nrDims = idx + 1;
		if (nrDims > 0) {
			if (desc.charAt(0) != '[')
				throw new AssertionError("Invalid array descriptor: " + desc);
			desc = desc.substring(idx + 1);
		}
		Type baseType;
		switch (desc.charAt(0)) {
		case 'Z':
			baseType = BooleanType.v();
			break;
		case 'B':
			baseType = ByteType.v();
			break;
		case 'C':
			baseType = CharType.v();
			break;
		case 'S':
			baseType = ShortType.v();
			break;
		case 'I':
			baseType = IntType.v();
			break;
		case 'F':
			baseType = FloatType.v();
			break;
		case 'J':
			baseType = LongType.v();
			break;
		case 'D':
			baseType = DoubleType.v();
			break;
		case 'L':
			if (desc.charAt(desc.length() - 1) != ';')
				throw new AssertionError("Invalid reference descriptor: " + desc);
			String name = desc.substring(1, desc.length() - 1);
			name = toQualifiedName(name);
			baseType = RefType.v(name);
			break;
		default:
			throw new AssertionError("Unknown descriptor: " + desc);	
		}
		if (!(baseType instanceof RefLikeType) && desc.length() > 1)
			throw new AssertionError("Invalid primitive type descriptor: " + desc);
		return nrDims > 0 ? ArrayType.v(baseType, nrDims) : baseType;
	}
	
	/**
	 * Converts a method signature to a list of types, with the last entry
	 * in the returned list denoting the return type.
	 * @param desc method signature.
	 * @return list of types.
	 */
	public static List<Type> toJimpleDesc(String desc) {
		ArrayList<Type> types = new ArrayList<Type>(2);
		int len = desc.length();
		int idx = 0;
		all:
		while (idx != len) {
			int nrDims = 0;
			Type baseType = null;
			this_type:
			while (idx != len) {
				char c = desc.charAt(idx++);
				switch (c) {
				case '(':
				case ')':
					continue all;
				case '[':
					++nrDims;
					continue this_type;
				case 'Z':
					baseType = BooleanType.v();
					break this_type;
				case 'B':
					baseType = ByteType.v();
					break this_type;
				case 'C':
					baseType = CharType.v();
					break this_type;
				case 'S':
					baseType = ShortType.v();
					break this_type;
				case 'I':
					baseType = IntType.v();
					break this_type;
				case 'F':
					baseType = FloatType.v();
					break this_type;
				case 'J':
					baseType = LongType.v();
					break this_type;
				case 'D':
					baseType = DoubleType.v();
					break this_type;
				case 'V':
					baseType = VoidType.v();
					break this_type;
				case 'L':
					int begin = idx;
					while (desc.charAt(++idx) != ';');
					String cls = desc.substring(begin, idx++);
					baseType = RefType.v(toQualifiedName(cls));
					break this_type;
				default:
					throw new AssertionError("Unknown type: " + c);
				}
			}
			if (nrDims > 0)
				types.add(ArrayType.v(baseType, nrDims));
			else
				types.add(baseType);
		}
		return types;
	}

	private AsmUtil() {
	}
}