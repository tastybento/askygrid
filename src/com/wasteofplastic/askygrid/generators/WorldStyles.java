package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;

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
		p.addBlock(1, 120);  //stone
		p.addBlock(2, 80);   //grass
		p.addBlock(3, 20);   //dirt
		p.addBlock(9, 10);   //still water
		p.addBlock(11, 5);   //still lava
		p.addBlock(12, 20);  //sand
		p.addBlock(13, 10);  //gravel
		p.addBlock(14, 10);  //gold ore
		p.addBlock(15, 20);  //iron ore
		p.addBlock(16, 40);  //coal ore
		p.addBlock(17, 100); //log
		p.addBlock(18, 40);  //leaves
		p.addBlock(20, 1);   //glass
		p.addBlock(21, 5);   //lapis ore
		p.addBlock(24, 10);  //sandstone
		p.addBlock(29, 1);   //sticky piston
		p.addBlock(30, 10);  //web
		p.addBlock(31, 3);   //shrub
		p.addBlock(32, 3);   //shrub
		p.addBlock(33, 1);   //piston
		p.addBlock(35, 25);  //wool
		p.addBlock(37, 2);   //yellow flower
		p.addBlock(38, 2);   //red flower
		p.addBlock(39, 2);   //brown mushroom
		p.addBlock(40, 2);   //red mushroom
		p.addBlock(46, 2);   //TNT
		p.addBlock(47, 3);   //bookshelves
		p.addBlock(48, 5);   //mossy cobblestone
		p.addBlock(49, 5);   //obsidian
		p.addBlock(52, 2);   //spawner
		p.addBlock(54, 1);   //chest
		p.addBlock(56, 1);   //diamond ore
		p.addBlock(73, 8);   //redstone ore
		p.addBlock(79, 4);   //ice
		p.addBlock(80, 8);   //snow
		p.addBlock(81, 2);   //cactus *****
		p.addBlock(82, 20);  //clay
		p.addBlock(83, 15);  //reeds *****
		p.addBlock(86, 5);   //pumpkin
		p.addBlock(103, 5);  //melon
		p.addBlock(110, 15); //mycelium
		return p;
	}
	
	private static BlockProbability netherP() {
		BlockProbability p = new BlockProbability();
		p.addBlock(11, 50);  //still lava
		p.addBlock(13, 30);  //gravel
		p.addBlock(52, 2);   //mob spawner
		p.addBlock(54, 1);   //chest
		p.addBlock(87, 300); //netherack
		p.addBlock(88, 100); //soulsand
		p.addBlock(89, 50);  //glowstone
		p.addBlock(112, 30); //netherbrick
		p.addBlock(113, 10); //nether fence
		p.addBlock(114, 15); //nether stairs
		p.addBlock(115, 30); //netherwart
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
		return s;
	}
	
	private static List<EntityType> netherSpawns() {
		List<EntityType> s = new ArrayList<EntityType>();
		s.add(EntityType.PIG_ZOMBIE);
		s.add(EntityType.BLAZE);
		s.add(EntityType.MAGMA_CUBE);
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