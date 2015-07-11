package com.bartbes.mcwatercolor;

import com.bartbes.mcwatercolor.mangler.ReplaceableSuperclass;

import net.minecraft.util.IIcon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

@ReplaceableSuperclass("cofh.asmhooks.block.BlockWater")
public class WaterWrapper extends BlockStaticLiquid implements IRenderInformation
{
	private static BlockStaticLiquid water;
	private static IIcon clearIcon;

	private IBlockAccess access;
	private int x;
	private int y;
	private int z;
	private int meta;

	public WaterWrapper(BlockStaticLiquid water)
	{
		super(water.getMaterial());
		this.water = water;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister registry)
	{
		super.registerBlockIcons(registry);
		clearIcon = registry.registerIcon(String.format("%s:water", Watercolor.MODID));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side)
	{
		if (access == null)
			return water.getIcon(side, 0);

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		int nx = x+dir.offsetX;
		int ny = y+dir.offsetY;
		int nz = z+dir.offsetZ;

		Block neighbour = access.getBlock(nx, ny, nz);

		// Display the default texture if we're touching air, water, or when
		// the block is flowing.
		if (neighbour.isAir(access, nx, ny, nz)
				|| neighbour.getMaterial() == getMaterial()
				|| getFlowDirection(access, x, y, z, getMaterial()) != -1000)
			return water.getIcon(side, meta);

		return clearIcon;
	}

	public void setNextRenderedBlockInfo(IBlockAccess access, int x, int y, int z)
	{
		this.access = access;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		this.meta = meta;
		return getIcon(access, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return Watercolor.renderId;
	}
}
