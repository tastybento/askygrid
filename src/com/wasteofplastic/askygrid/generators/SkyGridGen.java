package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.wasteofplastic.askygrid.Settings;

public class SkyGridGen extends ChunkGenerator {
    // Blocks that need to be placed on dirt
    private final static List<Material> needDirt = Arrays.asList(Material.SAPLING, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
	    Material.SUGAR_CANE_BLOCK, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.DOUBLE_PLANT);

    @Override
    public byte[][] generateBlockSections(World world, Random random, int chunkx, int chunkz, BiomeGrid biomes) {
	// Default block
	Material blockMat = Material.AIR;
	// This gets all the blocks that can be picked and their probabilities
	BlockProbability prob = WorldStyles.get(world.getEnvironment()).getProb();
	// The chunk we are making
	byte[][] chunk = new byte[world.getMaxHeight() / 16][];
	for (int x = 0; x < 16; x += 4) {
	    for (int z = 0; z < 16; z += 4) {
		for (int y = 0; y < Settings.gridHeight; y += 4) {
		    // Get a random block and feed in the last block (true if cactus or cane)
		    blockMat = prob.getBlock(random, y == 0, blockMat == Material.CACTUS || blockMat == Material.SUGAR_CANE_BLOCK);
		    // Check if the block needs dirt
		    if (needDirt.contains(blockMat)) {
			// Add dirt
			setBlock(chunk, x, y, z, Material.DIRT);
			setBlock(chunk, x, y+1, z, blockMat);
			
			if (blockMat.equals(Material.SUGAR_CANE_BLOCK)) {
			    //Bukkit.getLogger().info("DEBUG: sugar cane - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
			    setBlock(chunk, x+1, y, z, Material.STATIONARY_WATER);
			} else if (blockMat.equals(Material.DOUBLE_PLANT)) {
			    setBlock(chunk, x, y+2, z, blockMat);
			}
		    } else if (blockMat.equals(Material.CACTUS)) {
			//Bukkit.getLogger().info("DEBUG: cactus - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));

			setBlock(chunk, x, y, z, Material.SAND);
			setBlock(chunk, x, y-1, z, Material.VINE);
			setBlock(chunk, x, y+1, z, blockMat);
		    } else if (blockMat.equals(Material.NETHER_WARTS)) {
			//Bukkit.getLogger().info("DEBUG: nether warts - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));

			setBlock(chunk, x, y, z, Material.SOUL_SAND);
			setBlock(chunk, x, y+1, z, blockMat);
		    } else if (blockMat.toString().equals("END_ROD")) {
			//Bukkit.getLogger().info("DEBUG: end rod - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
			setBlock(chunk, x, y, z, Material.ENDER_STONE);
			setBlock(chunk, x, y+1, z, blockMat);
		    } else if (blockMat.toString().equals("CHORUS_PLANT")) {
			//Bukkit.getLogger().info("DEBUG: Chorus plant - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
			setBlock(chunk, x, y, z, Material.ENDER_STONE);
			setBlock(chunk, x, y+1, z, blockMat);
		    } else {
			/*
			if (blockMat.equals(Material.CHEST)) {
			    int xLoc = (chunkx*16+x);
			    int zLoc = (chunkz*16+z);
			    Bukkit.getLogger().info("DEBUG: setting chest at (" + xLoc + " " + y + " " + zLoc + ")");
			}
			*/
			setBlock(chunk, x, y, z, blockMat);
		    }
		}
	    }
	}
	return chunk;
    }

    @SuppressWarnings("deprecation")
    void setBlock(byte[][] result, int x, int y, int z, Material block) {
	// is this chunk part already initialized?
	if (result[y >> 4] == null) {
	    // Initialize the chunk part
	    result[y >> 4] = new byte[4096];
	}
	// set the block (look above, how this is done)
	result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte)block.getId();
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
	//return Arrays.asList(new BlockPopulator[0]);
	List<BlockPopulator> list = new ArrayList<BlockPopulator>(1);
	list.add(new SkyGridPop(Settings.gridHeight));
	return list;
    }



    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
	//Bukkit.getLogger().info("DEBUG: fixed spawn loc requested");
	return new Location(world, 0, Settings.gridHeight + 2, 0);
    }

}