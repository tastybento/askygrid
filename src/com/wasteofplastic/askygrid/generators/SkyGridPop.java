package com.wasteofplastic.askygrid.generators;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class SkyGridPop extends BlockPopulator {
    private static RandomSeries slt = new RandomSeries(27);
    private final int size;

    public SkyGridPop(int size) {
	this.size = size;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void populate(World world, Random random, Chunk chunk) {
	for (int x = 0; x < 16; x += 4) {
	    for (int y = 0; y < size; y +=4) {
		for (int z = 0; z < 16; z +=4) {
		    Block b = chunk.getBlock(x, y, z);
		    // Set chests and spawners
		    if (b.getType().equals(Material.CHEST)) {
			setChest(b, random);
		    } else if (b.getType().equals(Material.MOB_SPAWNER)) {
			setSpawner(b, random);
		    } else if (b.getType().equals(Material.STONE)) {
			b.setData((byte)random.nextInt(7));
		    } else if (b.getType().equals(Material.DIRT)) {
			b.setData((byte)random.nextInt(3));
		    } else if (b.getType().equals(Material.STONE)) {
			b.setData((byte)random.nextInt(7));
		    } else if (b.getType().equals(Material.SAND)) {
			b.setData((byte)random.nextInt(2));
		    } else if (b.getType().equals(Material.LOG)) {
			int r = random.nextInt(6);
			if (r < 4) {
			    b.setData((byte)r);
			} else {
			    b.setType(Material.LOG_2);
			    b.setData((byte)random.nextInt(2));
			}
		    } else if (b.getType().equals(Material.LONG_GRASS)) {
			b.setData((byte)random.nextInt(3));
		    } else if (b.getType().equals(Material.STONE)) {
			b.setData((byte)random.nextInt(7));
		    } else if (b.getType().equals(Material.RED_ROSE)) {
			b.setData((byte)random.nextInt(9));
		    } else if (b.getType().equals(Material.LEAVES)) {
			int r = random.nextInt(6);
			if (r < 4) {
			    b.setData((byte)r);
			} else {
			    b.setType(Material.LEAVES_2);
			    b.setData((byte)random.nextInt(2));
			}
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
	if (chunk.getX() == 0 && chunk.getZ() == 0) {
	    setEndPortal(chunk);
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

    private void setEndPortal(Chunk chunk) {

	chunk.getBlock(1, 4, 0).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(2, 4, 0).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(3, 4, 0).setType(Material.ENDER_PORTAL_FRAME);

	chunk.getBlock(4, 4, 1).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(4, 4, 2).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(4, 4, 3).setType(Material.ENDER_PORTAL_FRAME);

	chunk.getBlock(3, 4, 4).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(2, 4, 4).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(1, 4, 4).setType(Material.ENDER_PORTAL_FRAME);

	chunk.getBlock(0, 4, 3).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(0, 4, 2).setType(Material.ENDER_PORTAL_FRAME);
	chunk.getBlock(0, 4, 1).setType(Material.ENDER_PORTAL_FRAME);
    }

}