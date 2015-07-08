package com.bartbes.mcwatercolor.mangler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

class Visitors
{
	public static class MethodMangler extends MethodVisitor
	{
		private String origOwner;
		private String target;

		public MethodMangler(MethodVisitor visitor, String origOwner, String target)
		{
			super(Opcodes.ASM4, visitor);
			this.origOwner = origOwner;
			this.target = target;
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
		{
			if (opcode == Opcodes.INVOKESPECIAL && owner.equals(origOwner))
				owner = target;

			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
	}

	public static class ClassMangler extends ClassVisitor
	{
		private String origSuper;
		private String mangledName;
		private String target;

		public ClassMangler(ClassVisitor visitor, String origName, String mangledName, String target)
		{
			super(Opcodes.ASM4, new RemappingClassAdapter(visitor, new SimpleRemapper(origName, mangledName)));
			this.mangledName = mangledName;
			this.target = target;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String supername, String[] interfaces)
		{
			origSuper = supername;
			super.visit(version, access, name, signature, target, interfaces);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
			return new MethodMangler(visitor, origSuper, target);
		}
	}
}
