package com.bartbes.mcwatercolor;

import com.bartbes.mcwatercolor.mangler.ClassMangler;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.block.BlockStaticLiquid;

import com.google.common.collect.BiMap;

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

	private boolean registered = false;

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

	@SuppressWarnings("unchecked")
	private <T> void registerSubstitution(FMLControlledNamespacedRegistry<?> registry, T target, int id)
	{
		Field blockToName = null;
		Field idToBlock = null;

		try
		{
			idToBlock = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
		}
		catch (NoSuchFieldException e)
		{
			// Let's try the obfuscated name next
		}

		try
		{
			blockToName = RegistryNamespaced.class.getDeclaredField("field_148758_b");
			if (idToBlock == null)
				idToBlock = RegistryNamespaced.class.getDeclaredField("field_148759_a");
		}
		catch (NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}

		blockToName.setAccessible(true);
		idToBlock.setAccessible(true);

		try
		{
			((BiMap<T, String>) blockToName.get(registry)).forcePut(target, "minecraft:water");
			((net.minecraft.util.ObjectIntIdentityMap) idToBlock.get(registry)).func_148746_a(target, id);
		}
		catch (IllegalAccessException e)
		{
			// Not happening, we just called setAccessible
			throw new RuntimeException(e);
		}
	}

	private void registerSubstitution()
	{
		if (registered)
			return;

		int id = Block.getIdFromBlock(waterBlock);
		registerSubstitution(GameData.getBlockRegistry(), wrappedBlock, id);
		registerSubstitution(GameData.getItemRegistry(), new ItemBlock(wrappedBlock), id);
		registered = true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		waterBlock = GameRegistry.findBlock("minecraft", "water");
		wrappedBlock = createWrapper();

		// Register a "fake" block for the IIcon
		GameRegistry.registerBlock(createWrapper(), "water");

		renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(renderId, new CustomRenderer());
		cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void clientJoined(FMLNetworkEvent.ClientConnectedToServerEvent event)
	{
		registerSubstitution();
	}

	@SubscribeEvent
	public void clientJoined(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
	{
		registered = false;
	}
}
