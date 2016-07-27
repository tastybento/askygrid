package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import com.wasteofplastic.askygrid.ASkyGrid;

public class WorldStyles {
	private static final Map<World.Environment, WorldStyles> map = 
			new HashMap<World.Environment, WorldStyles>(); 
	
	private final BlockProbability p;
	private final List<EntityType> spawns;
	
	private WorldStyles(BlockProbability prob, List<EntityType> spwn) {
		p = prob;
		spawns = spwn;
	}
	
	static {
		map.put(World.Environment.NORMAL, new WorldStyles(normalP(), normalSpawns()));
		map.put(World.Environment.NETHER, new WorldStyles(netherP(), netherSpawns()));
	}
	
	public static WorldStyles get(World.Environment style) {
		if (!map.containsKey(style))
			throw new Error("SkyGrid can only generate The Overworld and The Nether");
		return map.get(style);
	}
	
	/**
	 * @return the p
	 */
	public BlockProbability getP() {
	    return p;
	}

	/**
	 * @return the spawns
	 */
	public List<EntityType> getSpawns() {
	    return spawns;
	}

	private static BlockProbability normalP() {
		BlockProbability p = new BlockProbability();
		FileConfiguration config = ASkyGrid.getPlugin().getConfig();
		int count = 0;
		for (String material: config.getConfigurationSection("world.blocks").getValues(false).keySet()) {
		    try {
			Material blockMaterial = Material.valueOf(material.toUpperCase());
			p.addBlock(blockMaterial, config.getInt("world.blocks." + material));
			count++;
		    } catch (Exception e) {
			Bukkit.getLogger().info("Do not know what " + material + " is so skipping...");
		    }
		}
		Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid over world");
		return p;
	}
	
	private static BlockProbability netherP() {
		BlockProbability p = new BlockProbability();
		FileConfiguration config = ASkyGrid.getPlugin().getConfig();
		int count = 0;
		for (String material: config.getConfigurationSection("world.netherblocks").getValues(false).keySet()) {
		    try {
			Material blockMaterial = Material.valueOf(material.toUpperCase());
			p.addBlock(blockMaterial, config.getInt("world.netherblocks." + material));
			count++;
		    } catch (Exception e) {
			Bukkit.getLogger().info("Do not know what " + material + " is so skipping...");
		    }
		}
		Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid nether");
		return p;
	}
	
	private static List<EntityType> normalSpawns() {
		List<EntityType> s = new ArrayList<EntityType>();
		s.add(EntityType.CREEPER);
		s.add(EntityType.SKELETON);
		s.add(EntityType.SPIDER);
		s.add(EntityType.CAVE_SPIDER);
		s.add(EntityType.ZOMBIE);
		s.add(EntityType.SLIME);
		s.add(EntityType.PIG);
		s.add(EntityType.SHEEP);
		s.add(EntityType.COW);
		s.add(EntityType.CHICKEN);
		s.add(EntityType.SQUID);
		s.add(EntityType.WOLF);
		s.add(EntityType.ENDERMAN);
		s.add(EntityType.SILVERFISH);
		s.add(EntityType.VILLAGER);
		s.add(EntityType.RABBIT);
		s.add(EntityType.GUARDIAN);
		s.add(EntityType.HORSE);
		s.add(EntityType.WITCH);
		return s;
	}
	
	private static List<EntityType> netherSpawns() {
		List<EntityType> s = new ArrayList<EntityType>();
		s.add(EntityType.PIG_ZOMBIE);
		s.add(EntityType.BLAZE);
		s.add(EntityType.MAGMA_CUBE);
		s.add(EntityType.SKELETON);
		//s.add(EntityType.GHAST);
		return s;
	}
	
	public static double getSProb(World world, int size, int it) {
		int x = get(world.getEnvironment()).p.total;
		int y = 4 * size;
		int n = (world.getEnvironment() == Environment.NETHER ? 3 : 2);
		x += n;
		return 1 - Math.pow((double) (x-n) / x, y - it);
	}
	
	public static boolean isChest(World world, Random random) {
		switch (world.getEnvironment()) {
		case NORMAL:
			return random.nextDouble() < 0.5;
		case NETHER:
			return random.nextDouble() < (1/3);
		default:
		    break;
		}
		return false;
	}
	
}