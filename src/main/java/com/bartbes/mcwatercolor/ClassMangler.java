package com.bartbes.mcwatercolor;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

public class ClassMangler
{
	private static class MethodMangler extends MethodVisitor
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
		public void visitMethodInsn(int opcode, String owner, String name, String desc)
		{
			if (opcode == Opcodes.INVOKESPECIAL && owner.equals(origOwner))
				owner = target;

			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	private static class Mangler extends ClassVisitor
	{
		private String origSuper;
		private String mangledName;
		private String target;

		public Mangler(ClassVisitor visitor, String origName, String mangledName, String target)
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

	private static class Loader extends ClassLoader
	{
		public Loader()
		{
			super(ClassMangler.class.getClassLoader());
		}

		private byte[] replaceSuperclass(InputStream bytecode, String origName, String mangledName, String target)
		{
			try
			{
				ClassReader reader = new ClassReader(bytecode);
				ClassWriter writer = new ClassWriter(reader, 0);
				Mangler mangler = new Mangler(writer, origName.replace('.', '/'), mangledName.replace('.', '/'), target.replace('.', '/'));
				reader.accept(mangler, ClassReader.EXPAND_FRAMES);
				return writer.toByteArray();
			}
			catch (IOException e)
			{
				return null;
			}
		}

		public Class<?> createMangled(Class<?> c, Class<?> target)
		{
			InputStream s = c.getResourceAsStream(c.getSimpleName() + ".class");
			String mangledName = c.getName() + "$Mangled$" + target.getSimpleName();
			byte[] bytecode = replaceSuperclass(s, c.getName(), mangledName, target.getName());

			try
			{
				Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
				define.setAccessible(true);
				return (Class<?>) define.invoke(getParent(), mangledName, bytecode, 0, bytecode.length);
			}
			catch (NoSuchMethodException e)
			{
				System.err.println("NoSuchMethod");
			}
			catch(IllegalAccessException e)
			{
				System.err.println("Invalid access");
			}
			catch(InvocationTargetException e)
			{
				throw new RuntimeException(e.getCause().getMessage(), e.getCause());
			}
			return null;
		}
	}

	public static class InvalidMangleTarget extends Exception
	{
		public InvalidMangleTarget(Class<?> target)
		{
			super("Cannot mangle class to type " + target.getCanonicalName());
		}
	}

	private static Loader loader = new Loader();

	private static void makeAccessible(Class<?> target)
	{
		for (Field f : target.getDeclaredFields())
			f.setAccessible(true);

		for (Method m : target.getDeclaredMethods())
			m.setAccessible(true);
	}

	public static Class<?> mangle(Class<?> c, Class<?> target) throws InvalidMangleTarget
	{
		if (target.isAssignableFrom(c))
			return c;

		ReplacableSuperclass replacements = (ReplacableSuperclass) c.getAnnotation(ReplacableSuperclass.class);
		boolean eligible = false;
		if (replacements != null)
			for (String alternative : replacements.value())
				if (alternative.equals(target.getCanonicalName()))
				{
					eligible = true;
					break;
				}

		if (!eligible)
			throw new InvalidMangleTarget(target);

		makeAccessible(target);

		System.out.println("Mangling " + c.getName() + " from " + c.getSuperclass().getName() + " to " + target.getName());
		return loader.createMangled(c, target);
	}
}
