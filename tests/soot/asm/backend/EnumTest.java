package soot.asm.backend;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Test for enum classes
 *
 * @author Tobias Hamann, Florian Kuebler, Dominik Helm, Lukas Sommer
 *
 */
public class EnumTest extends AbstractASMBackendTest {

	@Override
	protected void generate(TraceClassVisitor cw) {
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(V1_5, ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_ENUM,
				"soot/asm/backend/targets/MyEnum",
				"Ljava/lang/Enum<Lsoot/asm/backend/targets/MyEnum;>;",
				"java/lang/Enum", null);
		cw.visitSource("MyEnum.java", null);
		{
		fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM, "JA", "Lsoot/asm/backend/targets/MyEnum;", null, null);
		fv.visitEnd();
		}
		{
		fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM, "NEIN", "Lsoot/asm/backend/targets/MyEnum;", null, null);
		fv.visitEnd();
		}
		{
		fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC, "ENUM$VALUES", "[Lsoot/asm/backend/targets/MyEnum;", null, null);
		fv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();
		mv.visitTypeInsn(NEW, "soot/asm/backend/targets/MyEnum");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("JA");
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKESPECIAL, "soot/asm/backend/targets/MyEnum", "<init>", "(Ljava/lang/String;I)V", false);
		mv.visitFieldInsn(PUTSTATIC, "soot/asm/backend/targets/MyEnum", "JA", "Lsoot/asm/backend/targets/MyEnum;");
		mv.visitTypeInsn(NEW, "soot/asm/backend/targets/MyEnum");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("NEIN");
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKESPECIAL, "soot/asm/backend/targets/MyEnum", "<init>", "(Ljava/lang/String;I)V", false);
		mv.visitFieldInsn(PUTSTATIC, "soot/asm/backend/targets/MyEnum", "NEIN", "Lsoot/asm/backend/targets/MyEnum;");
		mv.visitInsn(ICONST_2);
		mv.visitTypeInsn(ANEWARRAY, "soot/asm/backend/targets/MyEnum");
		mv.visitVarInsn(ASTORE, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_0);
		mv.visitFieldInsn(GETSTATIC, "soot/asm/backend/targets/MyEnum", "JA", "Lsoot/asm/backend/targets/MyEnum;");
		mv.visitInsn(AASTORE);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(GETSTATIC, "soot/asm/backend/targets/MyEnum", "NEIN", "Lsoot/asm/backend/targets/MyEnum;");
		mv.visitInsn(AASTORE);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(PUTSTATIC, "soot/asm/backend/targets/MyEnum", "ENUM$VALUES", "[Lsoot/asm/backend/targets/MyEnum;");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "valueOf", "(Ljava/lang/String;)Lsoot/asm/backend/targets/MyEnum;", null, null);
		mv.visitCode();
		mv.visitLdcInsn(Type.getType("Lsoot/asm/backend/targets/MyEnum;"));
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
		mv.visitTypeInsn(CHECKCAST, "soot/asm/backend/targets/MyEnum");
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "values", "()[Lsoot/asm/backend/targets/MyEnum;", null, null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "soot/asm/backend/targets/MyEnum", "ENUM$VALUES", "[Lsoot/asm/backend/targets/MyEnum;");
		mv.visitInsn(DUP);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ISTORE, 0);
		mv.visitTypeInsn(ANEWARRAY, "soot/asm/backend/targets/MyEnum");
		mv.visitVarInsn(ASTORE, 1);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ILOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		}
		cw.visitEnd();


	}

	@Override
	protected String getTargetClass() {
		return "soot.asm.backend.targets.MyEnum";
	}

	@Override
	protected String getTargetFolder() {
		return "./testclasses";
	}

	@Override
	protected String getClassPathFolder() {
		return "./testclasses";
	}

}
