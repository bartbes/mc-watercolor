package com.bartbes.mcwatercolor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.ExistingSubstitutionException;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.block.BlockStaticLiquid;

import net.minecraftforge.fluids.RenderBlockFluid;

// FIXME
import java.lang.reflect.Modifier;

@Mod(name = Watercolor.MODNAME,
		modid = Watercolor.MODID,
		version = Watercolor.VERSION)
public class Watercolor
{
	public static final String MODNAME = "Minecraft Watercolor";
	public static final String MODID = "mcwatercolor";
	public static final String VERSION = "1.0";

	@Mod.Instance(MODID)
	public static Watercolor instance;

	static int renderId;

	private Block waterBlock;
	private Block wrappedBlock;

	private Class<?> getWrapperClass()
	{
		Class<?> newWrapper;
		try
		{
			newWrapper = ClassMangler.mangle(WaterWrapper.class, waterBlock.getClass());
		}
		catch (ClassMangler.InvalidMangleTarget ex)
		{
			throw new RuntimeException(ex.getMessage(), ex);
		}

		return newWrapper;
	}

	private Block createWrapper(BlockStaticLiquid waterBlock)
	{
		Class<?> c = getWrapperClass();

		try
		{
			Constructor<?> cons = c.getConstructor(BlockStaticLiquid.class);
			return (Block) cons.newInstance(waterBlock);
		}
		catch (InstantiationException e)
		{
			System.err.println("Cannot create instance of wrapper");
		}
		catch (IllegalAccessException e)
		{
			// Never going to happen
			System.err.println("Illegal access to constructor");
		}
		catch (InvocationTargetException e)
		{
			// Not happening either
			System.err.println("Invocation target exception: " + e.getMessage());
		}
		catch (NoSuchMethodException e)
		{
			// Nor this
			System.err.println("NoSuchMethodException");
		}

		return null;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		waterBlock = GameRegistry.findBlock("minecraft", "water");
		wrappedBlock = createWrapper((BlockStaticLiquid) waterBlock);

		try
		{
			GameRegistry.addSubstitutionAlias("minecraft:water", GameRegistry.Type.BLOCK, wrappedBlock);
			GameRegistry.addSubstitutionAlias("minecraft:water", GameRegistry.Type.ITEM, new ItemBlock(wrappedBlock));
		}
		catch (ExistingSubstitutionException e)
		{
			System.err.println("Water already substituted");
		}

		renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(renderId, new CustomRenderer());
	}
}
