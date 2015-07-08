package com.bartbes.mcwatercolor.mangler;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassMangler
{
	public static class InvalidMangleTarget extends Exception
	{
		public InvalidMangleTarget(Class<?> target)
		{
			super("Cannot mangle class to type " + target.getCanonicalName());
		}
	}

	private static byte[] replaceSuperclass(InputStream bytecode, String origName, String mangledName, String target)
	{
		try
		{
			origName = origName.replace('.', '/');
			mangledName = mangledName.replace('.', '/');
			target = target.replace('.', '/');

			ClassReader reader = new ClassReader(bytecode);
			ClassWriter writer = new ClassWriter(reader, 0);
			Visitors.ClassMangler mangler = new Visitors.ClassMangler(writer, origName, mangledName, target);
			reader.accept(mangler, ClassReader.EXPAND_FRAMES);
			return writer.toByteArray();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	private static Class<?> createMangled(Class<?> c, Class<?> target)
	{
		InputStream s = c.getResourceAsStream(c.getSimpleName() + ".class");
		String mangledName = c.getName() + "$Mangled$" + target.getSimpleName();
		byte[] bytecode = replaceSuperclass(s, c.getName(), mangledName, target.getName());

		try
		{
			Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
			define.setAccessible(true);
			ClassLoader loader = ClassMangler.class.getClassLoader();
			return (Class<?>) define.invoke(loader, mangledName, bytecode, 0, bytecode.length);
		}
		catch (NoSuchMethodException e)
		{
			// This is the defined interface of ClassLoader, it exists
			System.err.println("NoSuchMethod");
		}
		catch(IllegalAccessException e)
		{
			// We just set it accessible, won't happen
			System.err.println("Invalid access");
		}
		catch(InvocationTargetException e)
		{
			throw new RuntimeException(e.getCause().getMessage(), e.getCause());
		}
		return null;
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

		System.out.println("Mangling " + c.getName() + " from " + c.getSuperclass().getName() + " to " + target.getName());
		return createMangled(c, target);
	}
}
