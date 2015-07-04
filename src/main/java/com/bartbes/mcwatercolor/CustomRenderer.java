package com.bartbes.mcwatercolor;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.RenderBlocks;

import net.minecraftforge.fluids.RenderBlockFluid;

public class CustomRenderer extends RenderBlockFluid
{
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		((WaterWrapper) block).setNextRenderedBlockInfo(world, x, y, z);
		boolean b = renderer.renderBlockLiquid(block, x, y, z);
		((WaterWrapper) block).setNextRenderedBlockInfo(null, x, y, z);
		return b;
	}
}
