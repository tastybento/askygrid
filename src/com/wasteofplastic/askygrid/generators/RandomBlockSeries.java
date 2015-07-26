package com.wasteofplastic.askygrid.generators;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class RandomBlockSeries {
	private GridBlockAbstract[] reset;
	private GridBlockAbstract[] series;
	private int pos;
	public final int height;
	
	public RandomBlockSeries (World world, int size) {
		int t = size;
		t -= t % 16;
		height = t;
		
		reset = new GridBlockAbstract[4 * height];
		
		int i = 0;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < height / 4; y++) {
				for (int z = 0; z < 4; z++) {
					reset[i] = new GridBlockAbstract(x, y, z);
					i++;
				}
			}
		}
		
		reset();
	}
	
	public void reset() {
		pos = 0;
		series = reset.clone();
	}
	
	public Block nextBlock(Chunk chunk, Random random) {
		return nextAbstractBlock(random).getBlock(chunk);
	}
	
	public GridBlockAbstract nextAbstractBlock(Random random) {
		int swap = pos + random.nextInt(series.length - pos);
		GridBlockAbstract temp = series[pos];
		series[pos] = series[swap];
		series[swap] = temp;
		return series[pos++];
	}
	
}