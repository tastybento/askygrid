package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.wasteofplastic.askygrid.Settings;

public class SkyGridGen extends ChunkGenerator {
    // Blocks that need to be placed on dirt
    private final static List<Material> needDirt = Arrays.asList(Material.SAPLING, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM,
	    Material.SUGAR_CANE_BLOCK, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.DOUBLE_PLANT);

    @Override
    public byte[][] generateBlockSections(World world, Random random, int chunkx, int chunkz, BiomeGrid biomeGrid) {
	BiomeGenerator biomeGenerator = new BiomeGenerator(world);
	// Default block
	Material blockMat = Material.AIR;
	// This gets all the blocks that can be picked and their probabilities
	BlockProbability prob = WorldStyles.get(world.getEnvironment()).getProb();
	// Biomes
	// Settings.createBiomes
	//int noise = (int)Math.floor(voronoi.noise(chunkx, chunkz, 1) * Biome.values().length);
	//Bukkit.getLogger().info("DEBUG: " + noise + " biome = " + Biome.values()[noise]);


	// The chunk we are making
	byte[][] chunk = new byte[world.getMaxHeight() / 16][];
	if (Settings.createBiomes && world.getEnvironment().equals(Environment.NORMAL)) {
	    for (int x=0; x<16; x++) { 
		for (int z=0; z<16; z++) {
		    int realX = x + chunkx * 16; //used so that the noise function gives us
		    int realZ = z + chunkz * 16; //different values each chunk
		    //We get the 3 closest biome's to the temperature and rainfall at this block
		    HashMap<Biomes, Double> biomes = biomeGenerator.getBiomes(realX, realZ);
		    //And tell bukkit (who tells the client) what the biggest biome here is
		    biomeGrid.setBiome(x, z, getDominantBiome(biomes));
		}
	    }

	}
	for (int x = 0; x < 16; x += 4) {
	    for (int z = 0; z < 16; z += 4) {
		for (int y = 0; y < Settings.gridHeight; y += 4) {
		    // Get a random block and feed in the last block (true if cactus or cane)
		    blockMat = prob.getBlock(random, y == 0, blockMat == Material.CACTUS || blockMat == Material.SUGAR_CANE_BLOCK);
		    // Check if the block needs dirt
		    if (needDirt.contains(blockMat)) {
			// Check biome
			if (biomeGrid.getBiome(x, z).equals(Biome.DESERT)) {
			    // No plants in desert except for cactus
			    if (y == 0) {
				blockMat = Material.SAND;
			    } else {
				blockMat = Material.CACTUS;
			    }
			} else {
			    // Add dirt
			    setBlock(chunk, x, y, z, Material.DIRT);
			    setBlock(chunk, x, y+1, z, blockMat);
			    if (blockMat.equals(Material.SUGAR_CANE_BLOCK)) {
				//Bukkit.getLogger().info("DEBUG: sugar cane - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
				setBlock(chunk, x+1, y, z, Material.STATIONARY_WATER);
			    } else if (blockMat.equals(Material.DOUBLE_PLANT)) {
				setBlock(chunk, x, y+2, z, blockMat);
			    }
			}
		    } else {
			switch (blockMat) {
			case LAVA:
			case STATIONARY_LAVA:
			    // Don't allow stationary lava in this biome because the swamp tree vines can cause it to drip and lag
			    if (biomeGrid.getBiome(x, z).equals(Biome.SWAMPLAND)) {
				setBlock(chunk, x, y, z, Material.GRASS);
			    } else {
				setBlock(chunk, x, y, z, Material.STATIONARY_LAVA);
			    }
			    break;
			case CACTUS:
			    //Bukkit.getLogger().info("DEBUG: cactus - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
			    setBlock(chunk, x, y, z, Material.SAND);
			    setBlock(chunk, x, y-1, z, Material.VINE);
			    setBlock(chunk, x, y+1, z, blockMat);
			    break;
			case DIRT:
			    if (biomeGrid.getBiome(x, z).equals(Biome.DESERT)) {
				// Desert
				setBlock(chunk, x, y, z, Material.SAND);
			    } else {
				setBlock(chunk, x, y, z, Material.DIRT);
			    }
			case WATER:
			case STATIONARY_WATER:
			    if (biomeGrid.getBiome(x, z).equals(Biome.DESERT)) {
				// Desert
				setBlock(chunk, x, y, z, Material.SAND);
			    } else {
				setBlock(chunk, x, y, z, Material.STATIONARY_WATER);
			    }
			    break;
			case NETHER_WARTS:
			    //Bukkit.getLogger().info("DEBUG: nether warts - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
			    setBlock(chunk, x, y, z, Material.SOUL_SAND);
			    setBlock(chunk, x, y+1, z, blockMat);
			    break;
			default:
			    // Check strings for backwards compatibility
			    if (blockMat.toString().equals("END_ROD")) {
				//Bukkit.getLogger().info("DEBUG: end rod - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
				setBlock(chunk, x, y, z, Material.ENDER_STONE);
				setBlock(chunk, x, y+1, z, blockMat);
			    } else if (blockMat.toString().equals("CHORUS_PLANT")) {
				//Bukkit.getLogger().info("DEBUG: Chorus plant - result of random selection = " + blockMat + " " + (chunkx*16+x) + " " + y + " " + (chunkz*16+z));
				setBlock(chunk, x, y, z, Material.ENDER_STONE);
				setBlock(chunk, x, y+1, z, blockMat);
			    } else {
				/*
				 * debug
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

    //We get the closest biome to send to the client (using the biomegrid)
    private Biome getDominantBiome(HashMap<Biomes, Double> biomes) {
	double maxNoiz = 0.0;
	Biomes maxBiome = null;

	for (Biomes biome : biomes.keySet()) {
	    if (biomes.get(biome) >= maxNoiz) {
		maxNoiz = biomes.get(biome);
		maxBiome = biome;
	    }
	}
	return maxBiome.biome;
    }


}