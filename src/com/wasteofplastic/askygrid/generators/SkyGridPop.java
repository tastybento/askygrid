package com.wasteofplastic.askygrid.generators;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class SkyGridPop extends BlockPopulator {
    private static RandomSeries slt = new RandomSeries(27);
    private final int size;
    private final static boolean DEBUG = false;

    /**
     * @param size
     */
    public SkyGridPop(int size) {
	this.size = size;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void populate(World world, Random random, Chunk chunk) {
	if (DEBUG)
	    Bukkit.getLogger().info("DEBUG: populate chunk");
	for (int x = 0; x < 16; x += 4) {
	    for (int y = 0; y < size; y += 4) {
		for (int z = 0; z < 16; z +=4) {
		    Block b = chunk.getBlock(x, y, z);
		    if (b.getType().equals(Material.AIR))
			continue;
		    // Alter blocks
		    switch (b.getType()) {
		    case CHEST:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: chest");
			setChest(b, random);
			break;
		    case MOB_SPAWNER:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: mob spawner");
			setSpawner(b, random);
			break;
		    case STONE:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: stone");
			double type = random.nextDouble();
			if (type < 0.1) {
			    b.setData((byte)1); // Granite 
			} else if (type < 0.2) {
			    b.setData((byte)3); // Diorite 
			} else if (type < 0.3) {
			    b.setData((byte)5); // Andesite
			}
			break;
		    case DIRT:
			if (DEBUG)
			    Bukkit.getLogger().info("DIRT");
			b.setData((byte)random.nextInt(3));
			break;
		    case SAND:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG SAND");
			b.setData((byte)random.nextInt(2));
		    case LOG:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: LOG");
			int r = random.nextInt(6);
			if (r < 4) {
			    b.setData((byte)r);
			} else {
			    b.setType(Material.LOG_2);
			    b.setData((byte)random.nextInt(2));
			}
			break;
		    case LEAVES:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: leaves");
			int ra = random.nextInt(6);
			if (ra < 4) {
			    b.setData((byte)ra);
			} else {
			    b.setType(Material.LEAVES_2);
			    b.setData((byte)random.nextInt(2));
			}
			break;
		    default:
			break;
		    }
		    // Check blocks above the block
		    switch (b.getRelative(BlockFace.UP).getType()) {
		    case LONG_GRASS:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: LONG grass");
			b.getRelative(BlockFace.UP).setData((byte)random.nextInt(3));
			break;
		    case RED_ROSE:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: red rose");
			b.getRelative(BlockFace.UP).setData((byte)random.nextInt(9));
			break;
		    case DOUBLE_PLANT:
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG: Double plant");
			    b.getRelative(BlockFace.UP).setData((byte)random.nextInt(6));
			    b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setData((byte)8);
			break;
		    default:
			break;
		    }
		    // Nether
		    if (b.getWorld().getEnvironment().equals(Environment.NETHER)) {
			if (b.getType().equals(Material.STONE)) {
			    b.setType(Material.QUARTZ_ORE);
			}
		    }
		}
	    }
	}
    }

    private void setSpawner(Block b, Random random) {
	CreatureSpawner spawner = (CreatureSpawner) b.getState();
	List<EntityType> spawns = WorldStyles.get(b.getWorld().getEnvironment()).getSpawns();
	EntityType type = spawns.get(random.nextInt(spawns.size()));
	spawner.setDelay(120);
	spawner.setSpawnedType(type);
	spawner.update(true);
    }

    @SuppressWarnings("deprecation")
    private void setChest(Block b, Random random) {
	//Bukkit.getLogger().info("DEBUG: setChest");
	Chest chest = (Chest) b.getState();
	Inventory inv = chest.getBlockInventory();
	HashSet<ItemStack> set = new HashSet<ItemStack>();

	if (random.nextDouble() < 0.7)
	    set.add(itemInRange(256, 294, random)); //weapon/random

	if (random.nextDouble() < 0.7)
	    set.add(itemInRange(298, 317, random)); //armor

	if (random.nextDouble() < 0.7)
	    set.add(itemInRange(318, 350, random)); //food/tools

	if (random.nextDouble() < 0.3)
	    // Creeper, skeleton, spider
	    set.add(damageInRange(383, 50, 52, random)); //spawn eggs

	if (random.nextDouble() < 0.9)
	    // Zombie, slime, ghast, pigman, enderman, cave spider, silverfish
	    // blaze, magma cube
	    set.add(damageInRange(383, 54, 62, random)); //spawn eggs

	if (random.nextDouble() < 0.4)
	    // Sheep, Cow, chicken, squid, wolf, mooshroom
	    set.add(damageInRange(383, 91, 96, random)); //spawn eggs

	if (random.nextDouble() < 0.1)
	    // Ocelot
	    set.add(new ItemStack(383, 1, (short) 98)); //ocelot spawn egg

	if (random.nextDouble() < 0.1)
	    set.add(new ItemStack(383, 1, (short) 120)); //villager spawn egg

	if (random.nextDouble() < 0.1)
	    set.add(new ItemStack(383, 1, (short) 100)); //horse spawn egg

	if (random.nextDouble() < 0.1)
	    set.add(new ItemStack(383, 1, (short) 101)); //rabbit spawn egg

	if (random.nextDouble() < 0.7)
	    // Stone, Grass, Dirt, Cobblestone, Planks
	    set.add(itemMas(1, 5, 10, 64, random)); //materials

	set.add(damageInRange(6, 0, 5, random)); //sapling

	if (random.nextDouble() < 0.1)
	    // Prismarine
	    set.add(itemInRange(409, 410, random));

	//for dyes
	if (random.nextDouble() < 0.3)
	    set.add(damageInRange(351, 0, 15, random));

	for (ItemStack i : set) {
	    inv.setItem(slt.next(random), i);
	}
	slt.reset();
    }

    @SuppressWarnings("deprecation")
    private ItemStack itemInRange(int min, int max, Random random) {
	return new ItemStack(random.nextInt(max - min + 1) + min, 1);
    }

    @SuppressWarnings("deprecation")
    private ItemStack damageInRange(int type, int min, int max, Random random) {
	return new ItemStack(type, 1, (short) (random.nextInt(max - min + 1) + min));
    }

    @SuppressWarnings("deprecation")
    private ItemStack itemMas(int min, int max, int sm, int lg, Random random) {
	return new ItemStack(random.nextInt(max - min + 1) + min, 
		random.nextInt(lg - sm + 1) + sm);
    }
}