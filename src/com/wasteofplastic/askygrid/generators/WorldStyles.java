package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import com.wasteofplastic.askygrid.ASkyGrid;

/**
 * Provides different results for different world types: normal, nether, etc.
 *
 */
public class WorldStyles {
    private static final Map<World.Environment, WorldStyles> map = new HashMap<World.Environment, WorldStyles>(); 

    private final BlockProbability prob;
    private final List<EntityType> spawns;

    private WorldStyles(BlockProbability prob, List<EntityType> spawns) {
	this.prob = prob;
	this.spawns = spawns;
    }

    static {
	map.put(World.Environment.NORMAL, new WorldStyles(normalWorldProbabilities(), normalSpawns()));
	map.put(World.Environment.NETHER, new WorldStyles(netherWorldProbabilities(), netherSpawns()));
    }

    public static WorldStyles get(World.Environment style) {
	if (!map.containsKey(style))
	    throw new Error("ASkyGrid can only generate The Overworld and The Nether");
	return map.get(style);
    }

    /**
     * @return the block probability
     */
    public BlockProbability getProb() {
	return prob;
    }

    /**
     * @return the spawns
     */
    public List<EntityType> getSpawns() {
	return spawns;
    }

    /**
     * Set up the block probabilities for the normal world
     * @return Block Probabilities
     */
    private static BlockProbability normalWorldProbabilities() {
	BlockProbability blockProbability = new BlockProbability();
	FileConfiguration config = ASkyGrid.getPlugin().getConfig();
	int count = 0;
	for (String material: config.getConfigurationSection("world.blocks").getValues(false).keySet()) {
	    try {
		Material blockMaterial = Material.valueOf(material.toUpperCase());
		//Bukkit.getLogger().info("DEBUG: read in material " + blockMaterial + " value " + config.getInt("world.blocks." + material));
		blockProbability.addBlock(blockMaterial, config.getInt("world.blocks." + material));
		count++;
	    } catch (Exception e) {
		Bukkit.getLogger().info("Do not know what " + material + " is so skipping...");
	    }
	}
	Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid over world");
	return blockProbability;
    }

    private static BlockProbability netherWorldProbabilities() {
	BlockProbability blockProbability = new BlockProbability();
	FileConfiguration config = ASkyGrid.getPlugin().getConfig();
	int count = 0;
	for (String material: config.getConfigurationSection("world.netherblocks").getValues(false).keySet()) {
	    try {
		Material blockMaterial = Material.valueOf(material.toUpperCase());
		blockProbability.addBlock(blockMaterial, config.getInt("world.netherblocks." + material));
		count++;
	    } catch (Exception e) {
		Bukkit.getLogger().info("Do not know what " + material + " is so skipping...");
	    }
	}
	Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid nether");
	return blockProbability;
    }

    /**
     * What will come out of spawners
     * @return
     */
    private static List<EntityType> normalSpawns() {
	List<EntityType> s = new ArrayList<EntityType>();
	List<String> types = new ArrayList<String>();
	types.add("CREEPER");
	types.add("SKELETON");
	types.add("SPIDER");
	types.add("CAVE_SPIDER");
	types.add("ZOMBIE");
	types.add("SLIME");
	types.add("PIG");
	types.add("SHEEP");
	types.add("COW");
	types.add("CHICKEN");
	types.add("SQUID");
	types.add("WOLF");
	types.add("ENDERMAN");
	types.add("SILVERFISH");
	types.add("VILLAGER");
	types.add("RABBIT");
	types.add("GUARDIAN");
	types.add("HORSE");
	types.add("WITCH");
	types.add("LLAMA");
	types.add("POLAR_BEAR");
	for (EntityType type: EntityType.values()) {
	    if (types.contains(type.toString())) {
		s.add(type);
	    }
	}
	//Bukkit.getLogger().info("DEBUG: spawner list = " + s);
	return s;
    }

    /**
     * What will come out of spawners in the nether
     * @return
     */
    private static List<EntityType> netherSpawns() {
	List<EntityType> s = new ArrayList<EntityType>();
	s.add(EntityType.PIG_ZOMBIE);
	s.add(EntityType.BLAZE);
	s.add(EntityType.MAGMA_CUBE);
	s.add(EntityType.SKELETON);
	//s.add(EntityType.GHAST);
	return s;
    }

}