package com.wasteofplastic.askygrid.generators;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class SkyGridPopulator extends BlockPopulator {
    private RandomBlockSeries rnd = null;
    private static RandomSeries slt = new RandomSeries(27);
    private final int size;

    public SkyGridPopulator(int size) {
	this.size = size;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
	if (rnd == null) {
	    rnd = new RandomBlockSeries(world, size);
	}
	// This for loop can result in duplicate setting of blocks
	if (random.nextDouble() < WorldStyles.getSProb(world, size, 0)) {
	//for(int i = 0; random.nextDouble() < WorldStyles.getSProb(world, size, i); i++) {
	    if (WorldStyles.isChest(world, random)) {
		newChest(chunk, random);
	    } else {
		newSpawner(chunk, random);
	    }
	}
	rnd.reset();
	if (chunk.getX() == 0 && chunk.getZ() == 0) {
	    int sy = (int) (size * 0.75) - 4;

	    chunk.getBlock(0, sy, 0).setType(Material.STONE); //Set Spawn to Stone
	    chunk.getBlock(0, sy + 1, 0).setType(Material.AIR);
	    chunk.getBlock(1, sy, 0).setType(Material.AIR);
	    chunk.getBlock(0, sy - 1, 0).setType(Material.AIR);

	    setEndPortal(chunk);
	}
    }

    private void newSpawner(Chunk chunk, Random random) {
	Block b = rnd.nextBlock(chunk, random);

	b.getRelative(BlockFace.UP).setType(Material.AIR);
	b.getRelative(BlockFace.DOWN).setType(Material.AIR);
	b.getRelative(BlockFace.SOUTH).setType(Material.AIR);
	//Bukkit.getLogger().info("DEBUG: setting spawner at " + b.getLocation());
	b.setType(Material.MOB_SPAWNER);
	CreatureSpawner spawner = (CreatureSpawner) b.getState();

	List<EntityType> spawns = WorldStyles.get(chunk.getWorld().getEnvironment()).spawns;
	EntityType type = spawns.get(random.nextInt(spawns.size()));
	spawner.setDelay(120);
	spawner.setSpawnedType(type);
    }

    private void newChest(Chunk chunk, Random random) {
	Block b = rnd.nextBlock(chunk, random);

	b.getRelative(BlockFace.UP).setType(Material.AIR);
	b.getRelative(BlockFace.DOWN).setType(Material.AIR);
	b.getRelative(BlockFace.SOUTH).setType(Material.AIR);
	//Bukkit.getLogger().info("DEBUG: setting chest at " + b.getLocation());
	b.setType(Material.CHEST);
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
	    set.add(damageInRange(383, 50, 52, random)); //spawn eggs

	if (random.nextDouble() < 0.9)
	    set.add(damageInRange(383, 54, 62, random)); //spawn eggs

	if (random.nextDouble() < 0.4)
	    set.add(damageInRange(383, 92, 96, random)); //spawn eggs

	if (random.nextDouble() < 0.1)
	    set.add(new ItemStack(383, 1, (short) 98)); //ocelot spawn egg

	if (random.nextDouble() < 0.1)
	    set.add(new ItemStack(383, 1, (short) 120)); //villager spawn egg

	if (random.nextDouble() < 0.7)
	    set.add(itemMas(1, 5, 10, 64, random)); //materials

	set.add(damageInRange(6, 0, 3, random)); //sapling

	//for dyes
	if (random.nextDouble() < 0.3)
	    set.add(damageInRange(351, 0, 15, random));

	for (ItemStack i : set) {
	    inv.setItem(slt.next(random), i);
	}
	slt.reset();
    }

    private ItemStack itemInRange(int min, int max, Random random) {
	return new ItemStack(random.nextInt(max - min + 1) + min, 1);
    }

    private ItemStack damageInRange(int type, int min, int max, Random random) {
	return new ItemStack(type, 1, (short) (random.nextInt(max - min + 1) + min));
    }

    private ItemStack itemMas(int min, int max, int sm, int lg, Random random) {
	return new ItemStack(random.nextInt(max - min + 1) + min, 
		random.nextInt(lg - sm + 1) + sm);
    }

    private void setEndPortal(Chunk chunk) {

	chunk.getBlock(1, 4, 0).setTypeId(120);
	chunk.getBlock(2, 4, 0).setTypeId(120);
	chunk.getBlock(3, 4, 0).setTypeId(120);

	chunk.getBlock(4, 4, 1).setTypeId(120);
	chunk.getBlock(4, 4, 2).setTypeId(120);
	chunk.getBlock(4, 4, 3).setTypeId(120);

	chunk.getBlock(3, 4, 4).setTypeId(120);
	chunk.getBlock(2, 4, 4).setTypeId(120);
	chunk.getBlock(1, 4, 4).setTypeId(120);

	chunk.getBlock(0, 4, 3).setTypeId(120);
	chunk.getBlock(0, 4, 2).setTypeId(120);
	chunk.getBlock(0, 4, 1).setTypeId(120);

	//orient
	chunk.getBlock(1, 4, 0).setData((byte) 0);
	chunk.getBlock(2, 4, 0).setData((byte) 0);
	chunk.getBlock(3, 4, 0).setData((byte) 0);

	chunk.getBlock(4, 4, 1).setData((byte) 1);
	chunk.getBlock(4, 4, 2).setData((byte) 1);
	chunk.getBlock(4, 4, 3).setData((byte) 1);

	chunk.getBlock(3, 4, 4).setData((byte) 2);
	chunk.getBlock(2, 4, 4).setData((byte) 2);
	chunk.getBlock(1, 4, 4).setData((byte) 2);

	chunk.getBlock(0, 4, 3).setData((byte) 3);
	chunk.getBlock(0, 4, 2).setData((byte) 3);
	chunk.getBlock(0, 4, 1).setData((byte) 3);
    }

}