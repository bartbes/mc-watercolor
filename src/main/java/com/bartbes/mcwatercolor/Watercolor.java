package com.bartbes.mcwatercolor;

import com.bartbes.mcwatercolor.mangler.ClassMangler;

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

@Mod(name = Watercolor.MODNAME,
		modid = Watercolor.MODID)
public class Watercolor
{
	public static final String MODNAME = "Minecraft Watercolor";
	public static final String MODID = "mcwatercolor";

	@Mod.Instance(MODID)
	public static Watercolor instance;

	static int renderId;

	private Block waterBlock;
	private static Class<?> wrapperClass;
	private Block wrappedBlock;

	private Block createWrapper()
	{
		try
		{
			if (wrapperClass == null)
				wrapperClass = ClassMangler.mangle(WaterWrapper.class, waterBlock.getClass());
			Constructor<?> cons = wrapperClass.getConstructor(BlockStaticLiquid.class);
			return (Block) cons.newInstance((BlockStaticLiquid) waterBlock);
		}
		catch (Exception ex)
		{
			// Here we're going to blanket-catch a whole bunch of exceptions
			// of which only one has an actual chance of occurring, which is the
			// invalid mangling exception
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		waterBlock = GameRegistry.findBlock("minecraft", "water");
		wrappedBlock = createWrapper();

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
