/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.wasteofplastic.askygrid.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.util.Vector;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.GridManager;
import com.wasteofplastic.askygrid.SafeSpotTeleport;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.VaultHelper;

public class NetherPortals implements Listener {
    private final ASkyGrid plugin;

    public NetherPortals(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * This handles non-player portal use
     * Currently disables portal use by entities
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onEntityPortal(EntityPortalEvent event) {
	// If the nether is disabled then quit immediately
	if (!Settings.createNether) {
	    return;
	}
	if (event.getEntity() == null) {
	    return;
	}
	Location currentLocation = event.getFrom().clone();
	String currentWorld = currentLocation.getWorld().getName();
	// Only operate if this is Island territory
	if (!currentWorld.equalsIgnoreCase(Settings.worldName) && !currentWorld.equalsIgnoreCase(Settings.worldName + "_nether")) {
	    return;
	}
	// No entities may pass with the old nether
	/*
	if (!Settings.newNether) {
	    event.setCancelled(true);
	    return;
	}*/
	// New nether
	// Entities can pass only if there are adjoining portals
	Location dest = event.getFrom().toVector().toLocation(ASkyGrid.getIslandWorld());
	if (event.getFrom().getWorld().getEnvironment().equals(Environment.NORMAL)) {
	    dest = event.getFrom().toVector().toLocation(ASkyGrid.getNetherWorld());
	}
	// Vehicles
	if (event.getEntity() instanceof Vehicle) {
	    Vehicle vehicle = (Vehicle)event.getEntity();   
	    vehicle.eject();
	}
	new SafeSpotTeleport(plugin, event.getEntity(), dest);
	event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerPortal(PlayerPortalEvent event) {
	//plugin.getLogger().info("Player portal event - reason =" + event.getCause());
	UUID playerUUID = event.getPlayer().getUniqueId();
	// If the nether is disabled then quit immediately
	if (!Settings.createNether) {
	    return;
	}
	if (event.isCancelled()) {
	    // plugin.getLogger().info("PlayerPortalEvent was cancelled! ASkyBlock NOT teleporting!");
	    return;
	}
	Location currentLocation = event.getFrom().clone();
	String currentWorld = currentLocation.getWorld().getName();
	if (!currentWorld.equalsIgnoreCase(Settings.worldName) && !currentWorld.equalsIgnoreCase(Settings.worldName + "_nether")
		&& !currentWorld.equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    return;
	}
	// Check if this player is an island player
	if (plugin.getPlayers().getHomeLocation(playerUUID) == null) {
	    event.getPlayer().sendMessage(ChatColor.YELLOW + "Type /" + Settings.ISLANDCOMMAND + " to start!");
	    event.setCancelled(true);
	    return;
	}

	// Determine what portal it is
	switch (event.getCause()) {
	case END_PORTAL:
	    // Same action for all worlds except the end itself
	    if (!event.getFrom().getWorld().getEnvironment().equals(Environment.THE_END)) {
		if (plugin.getServer().getWorld(Settings.worldName + "_the_end") != null) {
		    // The end exists
		    event.setCancelled(true);
		    Location end_place = plugin.getServer().getWorld(Settings.worldName + "_the_end").getSpawnLocation();
		    if (GridManager.isSafeLocation(end_place)) {
			event.getPlayer().teleport(end_place);
			// event.getPlayer().sendBlockChange(end_place,
			// end_place.getBlock().getType(),end_place.getBlock().getData());
			return;
		    } else {
			event.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(event.getPlayer().getUniqueId()).warpserrorNotSafe);
			plugin.getGrid().homeTeleport(event.getPlayer());
			return;
		    }
		}
	    } else {
		event.setCancelled(true);
		plugin.getGrid().homeTeleport(event.getPlayer());
	    }
	    break;
	case NETHER_PORTAL:
	    // Get the home world of this player
	    World homeWorld = plugin.getPlayers().getHomeLocation(event.getPlayer().getUniqueId()).getWorld();
	    if (event.getFrom().getWorld().getEnvironment().equals(Environment.NORMAL)) {
		// Going to Nether
		if (homeWorld.getEnvironment().equals(Environment.NORMAL)) {
		    // Home world is over world
		    event.setTo(ASkyGrid.getNetherWorld().getSpawnLocation());
		    event.useTravelAgent(true); 
		} else {
		    // Home world is nether - going home
		    event.useTravelAgent(false);
		    Location dest = plugin.getGrid().getSafeHomeLocation(playerUUID,1);
		    if (dest != null) {
			event.setTo(dest);
		    } else {
			event.setCancelled(true);
			new SafeSpotTeleport(plugin, event.getPlayer(), plugin.getPlayers().getHomeLocation(playerUUID), 1);
		    }		
		}
	    } else {
		// Going to Over world
		if (homeWorld.getEnvironment().equals(Environment.NORMAL)) {
		    // Home world is over world
		    event.useTravelAgent(false);
		    Location dest = plugin.getGrid().getSafeHomeLocation(playerUUID,1);
		    if (dest != null) {
			event.setTo(dest);
		    } else {
			event.setCancelled(true);
			new SafeSpotTeleport(plugin, event.getPlayer(), plugin.getPlayers().getHomeLocation(playerUUID), 1);
		    }
		} else {
		    // Home world is nether 
		    event.setTo(ASkyGrid.getIslandWorld().getSpawnLocation());
		    event.useTravelAgent(true); 
		}
	    }

	    break;
	default:
	    break;
	}
    }
    // Nether portal spawn protection

    /**
     * Function to check proximity to nether spawn location
     * 
     * @param player
     * @return
     */
    private boolean awayFromSpawn(Player player) {
	Vector p = player.getLocation().toVector().multiply(new Vector(1, 0, 1));
	Vector spawn = player.getWorld().getSpawnLocation().toVector().multiply(new Vector(1, 0, 1));
	if (spawn.distanceSquared(p) < (Settings.netherSpawnRadius * Settings.netherSpawnRadius)) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Prevents blocks from being broken
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(final BlockBreakEvent e) {
	// plugin.getLogger().info("Block break");
	if ((e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether"))
		|| e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
		return;
	    }
	    // plugin.getLogger().info("Block break in acid island nether");
	    if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
		e.getPlayer().sendMessage(plugin.myLocale(e.getPlayer().getUniqueId()).netherSpawnIsProtected);
		e.setCancelled(true);
	    }
	}

    }

    /**
     * Prevents placing of blocks
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBlockPlace(final BlockPlaceEvent e) {
	if (e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
		|| e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
		return;
	    }
	    if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
		e.setCancelled(true);
	    }
	}

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
	if (e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
		|| e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    if (VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
		return;
	    }
	    if (!awayFromSpawn(e.getPlayer()) && !e.getPlayer().isOp()) {
		e.setCancelled(true);
	    }
	}
    }

    /**
     * Prevent the Nether spawn from being blown up
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
	// Find out what is exploding
	Entity expl = e.getEntity();
	if (expl == null) {
	    return;
	}
	// Check world
	if (!e.getEntity().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_nether")
		|| e.getEntity().getWorld().getName().equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    return;
	}
	Location spawn = e.getLocation().getWorld().getSpawnLocation();
	Location loc = e.getLocation();
	if (spawn.distance(loc) < Settings.netherSpawnRadius) {
	    e.blockList().clear();
	}
    }

}