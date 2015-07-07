package com.bartbes.mcwatercolor;

import net.minecraft.world.IBlockAccess;

public interface IRenderInformation
{
	void setNextRenderedBlockInfo(IBlockAccess access, int x, int y, int z);
}
