package com.wasteofplastic.askygrid.generators;

import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;

public class BlockProbability {
	
	TreeMap<Integer, Byte> p = new TreeMap<Integer, Byte>();
	int total = 0;
	
	
	
	public void addBlock(int id, int prob) {
		addBlock((byte) id, prob);
	}
	
	public void addBlock(byte id, int prob) {
		p.put(total, id);
		total += prob;
	}
	
	/**
	 * This picks a random block with the following constraints:
	 * A cactus is never chosen as the bottom block.
	 * Water or lava never is placed above sugar cane or cactuses because when they grow, they will touch the
	 * liquid and cause it to flow.
	 * @param random
	 * @param bottom
	 * @param b
	 * @return
	 */
	
	public byte getBlock(Random random, boolean bottom, boolean b) {
		byte temp = p.floorEntry(random.nextInt(total)).getValue();
		if (bottom && temp == Material.CACTUS.getId()) {
			return getBlock(random, bottom, b);
		} else if (b && (temp == Material.STATIONARY_WATER.getId() || temp == Material.STATIONARY_LAVA.getId())) {
			return getBlock(random, bottom, b);
		}
		return temp;
	}
	
}