package com.bartbes.mcwatercolor;

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

	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		waterBlock = GameRegistry.findBlock("minecraft", "water");
		wrappedBlock = new WaterWrapper((BlockStaticLiquid) waterBlock);
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
