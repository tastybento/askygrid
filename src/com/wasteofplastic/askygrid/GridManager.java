/**
 * 
 */
package com.wasteofplastic.askygrid;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.util.Vector;

/**
 * This class manages the island islandGrid. It knows where every island is, and
 * where new
 * ones should go. It can handle any size of island or protection size
 * The islandGrid is stored in a YML file.
 * 
 * @author tastybento
 */
public class GridManager {
    private ASkyGrid plugin;

    /**
     * @param plugin
     */
    public GridManager(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     * 
     * @param l
     *            - Location to be checked
     * @return true if safe, otherwise false
     */
    public static boolean isSafeLocation(final Location l) {
	if (l == null) {
	    return false;
	}
	// TODO: improve the safe location finding.
	//Bukkit.getLogger().info("DEBUG: " + l.toString());
	final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
	final Block space1 = l.getBlock();
	final Block space2 = l.getBlock().getRelative(BlockFace.UP);
	//Bukkit.getLogger().info("DEBUG: ground = " + ground.getType());
	//Bukkit.getLogger().info("DEBUG: space 1 = " + space1.getType());
	//Bukkit.getLogger().info("DEBUG: space 2 = " + space2.getType());
	// Portals are not "safe"
	if (space1.getType() == Material.PORTAL || ground.getType() == Material.PORTAL || space2.getType() == Material.PORTAL
		|| space1.getType() == Material.ENDER_PORTAL || ground.getType() == Material.ENDER_PORTAL || space2.getType() == Material.ENDER_PORTAL) {
	    return false;
	}
	// If ground is AIR, then this is either not good, or they are on slab,
	// stair, etc.
	if (ground.getType() == Material.AIR) {
	    // Bukkit.getLogger().info("DEBUG: air");
	    return false;
	}
	// In aSkyblock, liquid may be unsafe
	if (ground.isLiquid() || space1.isLiquid() || space2.isLiquid()) {
	    // Check if acid has no damage
	    if (ground.getType().equals(Material.STATIONARY_LAVA) || ground.getType().equals(Material.LAVA)
		    || space1.getType().equals(Material.STATIONARY_LAVA) || space1.getType().equals(Material.LAVA)
		    || space2.getType().equals(Material.STATIONARY_LAVA) || space2.getType().equals(Material.LAVA)) {
		// Lava check only
		// Bukkit.getLogger().info("DEBUG: lava");
		return false;
	    }
	}
	MaterialData md = ground.getState().getData();
	if (md instanceof SimpleAttachableMaterialData) {
	    //Bukkit.getLogger().info("DEBUG: trapdoor/button/tripwire hook etc.");
	    if (md instanceof TrapDoor) {
		TrapDoor trapDoor = (TrapDoor)md;
		if (trapDoor.isOpen()) {
		    //Bukkit.getLogger().info("DEBUG: trapdoor open");
		    return false;
		}
	    } else {
		return false;
	    }
	    //Bukkit.getLogger().info("DEBUG: trapdoor closed");
	}
	if (ground.getType().equals(Material.CACTUS) || ground.getType().equals(Material.BOAT) || ground.getType().equals(Material.FENCE)
		|| ground.getType().equals(Material.NETHER_FENCE) || ground.getType().equals(Material.SIGN_POST) || ground.getType().equals(Material.WALL_SIGN)) {
	    // Bukkit.getLogger().info("DEBUG: cactus");
	    return false;
	}
	// Check that the space is not solid
	// The isSolid function is not fully accurate (yet) so we have to
	// check
	// a few other items
	// isSolid thinks that PLATEs and SIGNS are solid, but they are not
	if (space1.getType().isSolid() && !space1.getType().equals(Material.SIGN_POST) && !space1.getType().equals(Material.WALL_SIGN)) {
	    return false;
	}
	if (space2.getType().isSolid()&& !space2.getType().equals(Material.SIGN_POST) && !space2.getType().equals(Material.WALL_SIGN)) {
	    return false;
	}
	// Safe
	//Bukkit.getLogger().info("DEBUG: safe!");
	return true;
    }

    /**
     * Determines a safe teleport spot on player's island or the team island
     * they belong to.
     * 
     * @param p UUID of player
     * @param number - starting home location e.g., 1
     * @return Location of a safe teleport spot or null if one cannot be found
     */
    public Location getSafeHomeLocation(final UUID p, int number) {
	// Try the numbered home location first
	Location l = plugin.getPlayers().getHomeLocation(p, number);
	if (l == null) {
	    // Get the default home, which may be null too, but that's okay
	    number = 1;
	    l = plugin.getPlayers().getHomeLocation(p, number);
	}
	// Check if it is safe
	//plugin.getLogger().info("DEBUG: Home location " + l);
	if (l != null) {
	    // Homes are stored as integers and need correcting to be more central
	    if (isSafeLocation(l)) {
		return l.clone().add(new Vector(0.5D,0,0.5D));
	    }
	    // To cover slabs, stairs and other half blocks, try one block above
	    Location lPlusOne = l.clone();
	    lPlusOne.add(new Vector(0, 1, 0));
	    if (lPlusOne != null) {
		if (isSafeLocation(lPlusOne)) {
		    // Adjust the home location accordingly
		    plugin.getPlayers().setHomeLocation(p, lPlusOne, number);
		    return lPlusOne.clone().add(new Vector(0.5D,0,0.5D));
		}
	    }
	}

	// Try all the way up to the sky
	//plugin.getLogger().info("DEBUG: try all the way to the sky");
	for (int y = l.getBlockY(); y < 255; y++) {
	    final Location n = new Location(l.getWorld(), l.getX() + 0.5D, y, l.getZ() + 0.5D);
	    if (isSafeLocation(n)) {
		plugin.getPlayers().setHomeLocation(p, n, number);
		return n;
	    }
	}
	//plugin.getLogger().info("DEBUG: unsuccessful");
	// Unsuccessful
	return null;
    }

    /**
     * This is a generic scan that can work in the overworld or the nether
     * @param l - location around which to scan
     * @param i - the range to scan for a location < 0 means the full island.
     * @return - safe location, or null if none can be found
     */
    public Location bigScan(Location l, int i) {
	final int height;
	final int depth;
	if (i > 0) {
	    height = i;
	    depth = i;
	} else {
	    i = Settings.claim_protectionRange;
	    height = l.getWorld().getMaxHeight() - l.getBlockY();
	    depth = l.getBlockY();
	}


	//plugin.getLogger().info("DEBUG: ranges i = " + i);
	//plugin.getLogger().info(" " + minX + "," + minZ + " " + maxX + " " + maxZ);
	//plugin.getLogger().info("DEBUG: height = " + height);
	//plugin.getLogger().info("DEBUG: depth = " + depth);
	//plugin.getLogger().info("DEBUG: trying to find a safe spot at " + l.toString());

	// Work outwards from l until the closest safe location is found.
	int minXradius = 0;
	int maxXradius = 0;
	int minZradius = 0;
	int maxZradius = 0;
	int minYradius = 0;
	int maxYradius = 0;

	do {
	    int minX = l.getBlockX()-minXradius;
	    int minZ = l.getBlockZ()-minZradius;
	    int minY = l.getBlockY()-minYradius;
	    int maxX = l.getBlockX()+maxXradius;
	    int maxZ = l.getBlockZ()+maxZradius;
	    int maxY = l.getBlockY()+maxYradius;
	    for (int x = minX; x<= maxX; x++) {
		for (int z = minZ; z <= maxZ; z++) {
		    for (int y = minY; y <= maxY; y++) {
			if (!((x > minX && x < maxX) && (z > minZ && z < maxZ) && (y > minY && y < maxY))) {
			    //plugin.getLogger().info("DEBUG: checking " + x + "," + y + "," + z);
			    Location ultimate = new Location(l.getWorld(), x + 0.5D, y, z + 0.5D);
			    if (isSafeLocation(ultimate)) {
				//plugin.getLogger().info("DEBUG: Found! " + ultimate);
				return ultimate;
			    }
			}
		    }
		}
	    }
	    if (minXradius < i) {
		minXradius++;
	    }
	    if (maxXradius < i) {
		maxXradius++;
	    }
	    if (minZradius < i) {
		minZradius++;
	    }
	    if (maxZradius < i) {
		maxZradius++;
	    }
	    if (minYradius < depth) {
		minYradius++;
	    }
	    if (maxYradius < height) {
		maxYradius++;
	    }
	    //plugin.getLogger().info("DEBUG: Radii " + minXradius + "," + minYradius + "," + minZradius + 
	    //    "," + maxXradius + "," + maxYradius + "," + maxZradius);
	} while (minXradius < i || maxXradius < i || minZradius < i || maxZradius < i || minYradius < depth 
		|| maxYradius < height);
	// Nothing worked
	return null;
    }

    /**
     * This teleports player to their island. If not safe place can be found
     * then the player is sent to spawn via /spawn command
     * 
     * @param player
     * @return
     */
    public boolean homeTeleport(final Player player) {
	return homeTeleport(player, 1);
    }
    /**
     * Teleport player to a home location. If one cannot be found a search is done to
     * find a safe place.
     * @param player
     * @param number - home location to do to
     * @return true if successful, false if not
     */
    public boolean homeTeleport(final Player player, int number) {
	Location home = null;
	//plugin.getLogger().info("home teleport called for #" + number);
	home = getSafeHomeLocation(player.getUniqueId(), number);
	//plugin.getLogger().info("home get safe loc = " + home);
	// Check if the player is a passenger in a boat
	if (player.isInsideVehicle()) {
	    Entity boat = player.getVehicle();
	    if (boat instanceof Boat) {
		player.leaveVehicle();
		// Remove the boat so they don't lie around everywhere
		boat.remove();
		player.getInventory().addItem(new ItemStack(Material.BOAT, 1));
		player.updateInventory();
	    }
	}
	if (home == null) {
	    //plugin.getLogger().info("Fixing home location using safe spot teleport");
	    // Try to fix this teleport location and teleport the player if possible
	    new SafeSpotTeleport(plugin, player, plugin.getPlayers().getHomeLocation(player.getUniqueId(), number), number);
	    return true;
	}
	//plugin.getLogger().info("DEBUG: home loc = " + home + " teleporting");
	//home.getChunk().load();
	player.teleport(home);
	//player.sendBlockChange(home, Material.GLOWSTONE, (byte)0);
	if (number ==1 ) {
	    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).homeTeleport);
	} else {
	    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).homeTeleport + " #" + number);
	}
	return true;

    }

    /**
     * Sets the numbered home location based on where the player is now
     * @param player
     * @param number
     */
    public void homeSet(Player player, int number) {
	// Check if player is in their home world
	if (!player.getWorld().equals(ASkyGrid.getGridWorld())) {
	    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).setHomeerrorNotOnIsland);
	    return; 
	}
	plugin.getPlayers().setHomeLocation(player.getUniqueId(), player.getLocation(), number);
	if (number == 1) {
	    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).setHomehomeSet);
	} else {
	    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).setHomehomeSet + " #" + number);
	}
    }

    /**
     * Sets the home location based on where the player is now
     * 
     * @param player
     * @return
     */
    public void homeSet(final Player player) {
	homeSet(player, 1);
    }

    /**
     * Removes monsters around location l
     * 
     * @param l
     */
    public void removeMobs(final Location l) {
	final int px = l.getBlockX();
	final int py = l.getBlockY();
	final int pz = l.getBlockZ();
	for (int x = -1; x <= 1; x++) {
	    for (int z = -1; z <= 1; z++) {
		final Chunk c = l.getWorld().getChunkAt(new Location(l.getWorld(), px + x * 16, py, pz + z * 16));
		if (c.isLoaded()) {
		    for (final Entity e : c.getEntities()) {
			if (e instanceof Monster && !Settings.mobWhiteList.contains(e.getType())) {
			    e.remove();
			}
		    }
		}
	    }
	}
    }
}