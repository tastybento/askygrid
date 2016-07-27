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
    private final int size;
    // blocks that need to be placed on dirt
    // Material.SAPLING, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
    // Material.SUGAR_CANE, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE
    private final static List<Material> needDirt = Arrays.asList(Material.SAPLING, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
	    Material.SUGAR_CANE, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE);
    
    public SkyGridGen() {
	this(Settings.gridHeight);
    }

    public SkyGridGen(int size) {
	this.size = size;
    }
    
    @Override
    public byte[][] generateBlockSections(World world, Random random, int chunkx, int chunkz, BiomeGrid biomes) {

	Material blockid = Material.AIR;
	// This gets all the blocks that can be picked and their probabilities
	BlockProbability p = WorldStyles.get(world.getEnvironment()).getP();
	int vsegs = world.getMaxHeight() / 16;
	byte[][] chunk = new byte[vsegs][];
	    for (int x = 0; x < 16; x += 4) {
		for (int z = 0; z < 16; z += 4) {
		    for (int y = 0; y < size; y += 4) {
			// Get a random block and feed in the last block (true if cactus or cane)
			blockid = p.getBlock(random, y == 0, blockid == Material.CACTUS || blockid == Material.SUGAR_CANE);
			// Check if the block needs dirt
			if (needDirt.contains(blockid)) {
			    // Add dirt
			    setBlock(chunk, x, y, z, Material.DIRT); //dirt
			    setBlock(chunk, x, y+1, z, blockid);
			    if (blockid == Material.SUGAR_CANE) //reeds
				setBlock(chunk, x+1, y, z, Material.STATIONARY_WATER); //still water
			} else if (blockid == Material.CACTUS) { //cactus
			    setBlock(chunk, x, y, z, Material.SAND); //sand
			    setBlock(chunk, x, y-1, z, Material.VINE); //vines
			    setBlock(chunk, x, y+1, z, blockid);
			} else if (blockid == Material.NETHER_WARTS) { //netherwart
			    setBlock(chunk, x, y, z, Material.SOUL_SAND); //soul sand
			    setBlock(chunk, x, y+1, z, blockid);
			} else {
			    setBlock(chunk, x, y, z, blockid);
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
	list.add(new SkyGridPop(size));
	return list;
    }
    
    

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
	//Bukkit.getLogger().info("DEBUG: fixed spawn loc requested");
	return new Location(world, 0, size + 2, 0);
    }

}