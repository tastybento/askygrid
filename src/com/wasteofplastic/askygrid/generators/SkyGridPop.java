package com.wasteofplastic.askygrid.generators;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;

import com.wasteofplastic.askygrid.Settings;


@SuppressWarnings("deprecation")
public class SkyGridPop extends BlockPopulator {
    private static RandomSeries slt = new RandomSeries(27);
    private final int size;
    private final static boolean DEBUG = false;
    private final static HashMap<String, Double> endItems;
    private boolean spawnEggMeta = false;
    private final static HashMap<String, Short> spawnEggData;

    static {
	endItems = new HashMap<String, Double>();
	endItems.put("FIREWORK",0.2); // for elytra
	endItems.put("EMERALD", 0.1); 
	endItems.put("CHORUS_FRUIT", 0.2);
	endItems.put("ELYTRA", 0.2);
	endItems.put("PURPLE_SHULKER_BOX", 0.2);

	spawnEggData = new HashMap<String, Short>();
	short index = 21; // The magic number
	for (EntityType type : EntityType.values()) {
	    if (type.isAlive()) {
		//Bukkit.getLogger().info("DEBUG: " + type.toString() + "=>" + (index));
		spawnEggData.put(type.toString(), index);
	    }
	    index++;
	}	
    }

    /**
     * @param size
     */
    public SkyGridPop(int size) {
	this.size = size;
	// Work out if SpawnEgg method is available
	if (getMethod("SpawnEggMeta", ItemMeta.class) != null) {
	    spawnEggMeta = true;
	}
    }

    @SuppressWarnings("deprecation")
    @Override
    public void populate(World world, Random random, Chunk chunk) {
	if (DEBUG)
	    Bukkit.getLogger().info("DEBUG: populate chunk");
	boolean chunkHasPortal = false;
	for (int x = 0; x < 16; x += 4) {
	    for (int y = 0; y < size; y += 4) {
		for (int z = 0; z < 16; z +=4) {
		    Block b = chunk.getBlock(x, y, z);
		    // Do an end portal check
		    if (Settings.createEnd && world.getEnvironment().equals(Environment.NORMAL)
			    && x==0 && z==0 && y == 0 && !chunkHasPortal) {
			if (random.nextDouble() < Settings.endPortalProb) {
			    chunkHasPortal = true;
			    for (int xx = 0; xx< 5; xx++) {
				for (int zz = 0; zz < 5; zz++) {
				    if (xx == zz || (xx==0 && zz==4) || (xx==4 && zz==0))
					continue;
				    if (xx>0 && xx<4 && zz>0 && zz<4) {
					continue;
				    }
				    Block frame = chunk.getBlock(xx, 0, zz);
				    frame.setType(Material.ENDER_PORTAL_FRAME);
				    // Add the odd eye of ender
				    byte addEye = (byte)0;
				    if (random.nextDouble() < 0.1) {
					addEye = (byte)0x4;
				    }
				    if (zz == 0) {
					// Face South
					frame.setData(addEye);
				    } else if (zz == 4) {
					// Face North
					frame.setData((byte)((byte)2 | (byte)addEye));
				    } else if (xx == 0) {
					// Face East
					frame.setData((byte)((byte)3 | (byte)addEye));
				    } else if (xx == 4) {
					// Face West
					frame.setData((byte)((byte)1 | (byte)addEye));
				    }
				}
			    }			    
			}
		    }
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
			if (type < 0.03) {
			    b.setData((byte)1); // Granite 
			} else if (type < 0.06) {
			    b.setData((byte)3); // Diorite 
			} else if (type < 0.09) {
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
		    // End
		    if (b.getWorld().getEnvironment().equals(Environment.THE_END)) {
			if (DEBUG)
			    Bukkit.getLogger().info("DEBUG the end " + b);
			if (b.getType().equals(Material.STAINED_GLASS)) {
			    // Make it purple
			    b.setData((byte)10);
			}
			// End crystal becomes hay block in the generator - leave lighting calcs crash server
			/*
			if (b.getRelative(BlockFace.UP).getType().equals(Material.HAY_BLOCK)) {
			    b.getRelative(BlockFace.UP).setType(Material.AIR);
			    b.getWorld().spawn(b.getRelative(BlockFace.UP).getLocation(), EnderCrystal.class);
			}
			 */
		    }
		}
	    }
	}
    }

    private void setSpawner(Block b, Random random) {
	CreatureSpawner spawner = (CreatureSpawner) b.getState();
	TreeMap<Integer,EntityType> spawns = WorldStyles.get(b.getWorld().getEnvironment()).getSpawns();
	int randKey = random.nextInt(spawns.lastKey());
	//Bukkit.getLogger().info("DEBUG: spawner rand key = " + randKey + " out of " + spawns.lastKey());
	EntityType type = spawns.ceilingEntry(randKey).getValue();
	//Bukkit.getLogger().info("DEBUG: spawner type = " + type);
	spawner.setDelay(120);
	spawner.setSpawnedType(type);
	spawner.update(true);
    }

    private void setChest(Block b, Random random) {
	//Bukkit.getLogger().info("DEBUG: setChest");
	Chest chest = (Chest) b.getState();
	Inventory inv = chest.getBlockInventory();
	HashSet<ItemStack> set = new HashSet<ItemStack>();
	// Overworld
	switch (b.getWorld().getEnvironment()) {
	case NETHER:
	    if (random.nextDouble() < 0.7)
		set.add(itemInRange(256, 294, random)); //weapon/random
	    if (random.nextDouble() < 0.7)
		set.add(itemInRange(298, 317, random)); //armor
	    if (random.nextDouble() < 0.9) {
		// ghast, pigman, enderman
		set.add(damageInRange(383, 56, 58, random)); //spawn eggs
	    } else if (random.nextDouble() < 0.9) {
		// Blaze, Magma Cube
		set.add(damageInRange(383, 61, 62, random)); //spawn eggs
	    }
	    if (random.nextDouble() < 0.3) {
		Double rand1 = random.nextDouble();
		if (rand1 < 0.1)
		    set.add(new ItemStack(Material.WATCH)); // clock
		else if (rand1 < 0.5) {
		    set.add(new ItemStack(Material.BLAZE_ROD));
		} else if (rand1 < 0.6) {
		    set.add(new ItemStack(Material.SADDLE));
		} else if (rand1 < 0.7) {
		    set.add(new ItemStack(Material.IRON_BARDING));
		} else if (rand1 < 0.8) {
		    set.add(new ItemStack(Material.GOLD_BARDING));
		} else if (rand1 < 0.9) {
		    set.add(new ItemStack(Material.DIAMOND_BARDING));
		} else {
		    set.add(new ItemStack(Material.GHAST_TEAR));
		}
	    }
	    break;
	case NORMAL:
	    if (random.nextDouble() < 0.7)
		set.add(itemInRange(256, 294, random)); //weapon/random

	    if (random.nextDouble() < 0.7)
		set.add(itemInRange(298, 317, random)); //armor

	    if (random.nextDouble() < 0.7)
		set.add(itemInRange(318, 350, random)); //food/tools
	    if (random.nextDouble() < 0.3) {
		// Creeper, skeleton, spider
		set.add(damageInRange(383, 50, 52, random)); //spawn eggs
	    } else if (random.nextDouble() < 0.9) {
		// Zombie, slime
		set.add(damageInRange(383, 54, 55, random)); //spawn eggs
	    } else if (random.nextDouble() < 0.9) {
		// Enderman, cave spider, silverfish
		set.add(damageInRange(383, 58, 60, random)); //spawn eggs
	    }
	    if (random.nextDouble() < 0.4) {
		// Sheep, Cow, chicken, squid, wolf, mooshroom
		set.add(damageInRange(383, 91, 96, random)); //spawn eggs
	    }
	    if (random.nextDouble() < 0.1) {
		// Ocelot
		set.add(spawnEgg("OCELOT")); //ocelot spawn egg
	    }
	    if (random.nextDouble() < 0.1)
		set.add(spawnEgg("VILLAGER")); //villager spawn egg

	    if (random.nextDouble() < 0.1) {
		Double rand = random.nextDouble();
		if (rand < 0.25) {
		    set.add(spawnEgg("HORSE")); //horse spawn egg
		} else if (rand < 0.5) {
		    set.add(spawnEgg("RABBIT")); //rabbit spawn egg
		} else if (rand < 0.75) {
		    set.add(spawnEgg("POLAR_BEAR")); //polar bear spawn egg
		} else
		    set.add(spawnEgg("GUARDIAN")); //guardian spawn egg
	    }
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

	    break;
	case THE_END:
	    set.add(itemInRange(318, 350, random)); //food/tools
	    if (random.nextDouble() < 0.2)
		set.add(spawnEgg("ENDERMAN")); //enderman spawn egg
	    if (random.nextDouble() < 0.4)
		set.add(itemInRange(256, 294, random)); //weapon/random
	    for (Material mat : Material.values()) {
		if (endItems.containsKey(mat.toString())) {
		    if (random.nextDouble() < endItems.get(mat.toString())) {
			set.add(new ItemStack(mat));
		    }
		}
	    }
	    if (random.nextDouble() < 0.2)
		set.add(spawnEgg("SHULKER")); //shulker spawn egg
	    break;
	default:
	    break;

	}

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

    /**
     * Gets an item stack egg of type entityType
     * @param entityType
     * @return A spawn egg of type entityType or AIR if not known
     */
    @SuppressWarnings("deprecation")
    private ItemStack spawnEgg(String entityType) {
	ItemStack egg = new ItemStack(Material.AIR);
	try {
	    if (spawnEggMeta) {
		// Yeah, 1.11
		egg = new ItemStack(Material.MONSTER_EGG, 1);
		SpawnEggMeta sem = (SpawnEggMeta)egg.getItemMeta();
		sem.setSpawnedType(EntityType.valueOf(entityType));
	    } else {
		// 1.9 and before
		SpawnEgg spawnEgg = new SpawnEgg(EntityType.valueOf(entityType));
		egg = spawnEgg.toItemStack();
		egg.setAmount(1);
		// Check if SpawnEgg even works due to 1.10
		if (spawnEgg.getSpawnedType() == null) {
		    //Bukkit.getLogger().warning("DEBUG: No SpawnEgg 1.10?");
		    if (spawnEggData.containsKey(entityType)) {
			// Set using durability - will it work?
			egg.setDurability(spawnEggData.get(entityType));
		    } else {
			// No luck
			egg.setType(Material.AIR); 
		    }
		}
	    }
	} catch (Exception e) {
	    //Bukkit.getLogger().warning("DEBUG: failed for " + entityType);
	    //e.printStackTrace();
	}
	return egg;
    }

    private Method getMethod(String name, Class<?> clazz) {
	for (Method m : clazz.getDeclaredMethods()) {
	    if (m.getName().equals(name))
		return m;
	}
	return null;
    }
}