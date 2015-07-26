package com.wasteofplastic.askygrid.generators;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

public class GridBlockAbstract {
	
	public final int x;
	public final int y;
	public final int z;
	
	public GridBlockAbstract(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Block getBlock(Chunk chunk) {
		return chunk.getBlock(x*4, y*4, z*4);
	}
	
	@Override
	public String toString() {
		return "(" + Integer.toString(x) + ", " + 
				Integer.toString(y) + ", " + 
				Integer.toString(z) + ")";
	}
	
}