/*******************************************************************************
 * This file is part of ASkyGrid.
 *
 *     ASkyGrid is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyGrid is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyGrid.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.wasteofplastic.askygrid.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.VaultHelper;

/**
 * @author tastybento
 *         Provides protection to islands
 */
public class PlayerEvents implements Listener {
    private final ASkyGrid plugin;
    private final static boolean DEBUG = true;
    // A set of falling players
    private static HashSet<UUID> fallingPlayers = new HashSet<UUID>();
    private List<UUID> respawn;
    //private boolean spawnEggMeta = false;

    public PlayerEvents(final ASkyGrid plugin) {
	this.plugin = plugin;
	respawn = new ArrayList<UUID>();
	// Work out if SpawnEgg method is available
	/*
	if (getMethod("SpawnEggMeta", ItemMeta.class) != null) {
	    spawnEggMeta = true;
	}
	 */
	//plugin.getLogger().info("DEBUG: spawneggmeta = " + spawnEggMeta);
    }

    /**
     * Places player back at their home on the grid if the setting is true
     * @param e
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info(e.getEventName());
	}
	if (!Settings.respawnAtHome) {
	    return;
	}
	if (respawn.contains(e.getPlayer().getUniqueId())) {
	    if (DEBUG)
		plugin.getLogger().info("DEBUG: found player to respawn");
	    respawn.remove(e.getPlayer().getUniqueId());
	    Location respawnLocation = plugin.getGrid().getSafeHomeLocation(e.getPlayer().getUniqueId(), 1);
	    if (respawnLocation != null) {
		if (DEBUG)
		    plugin.getLogger().info("DEBUG: Setting respawn location to " + respawnLocation);
		e.setRespawnLocation(respawnLocation);
	    }
	}
    }

    /**
     * Places the player on the home respawn list if they are eligible
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info(e.getEventName());
	}
	if (!Settings.respawnAtHome) {
	    return;
	}
	// Died in island space?
	if (!inASkyGridWorld(e.getEntity().getWorld())) {
	    if (DEBUG)
		plugin.getLogger().info("DEBUG: player did not die in ASKyGrid space");
	    return;
	}
	UUID playerUUID = e.getEntity().getUniqueId();
	// Check if player has home
	if (plugin.getPlayers().getHomeLocation(playerUUID) != null) {
	    // Add them to the list to be respawned on their island
	    if (DEBUG)
		plugin.getLogger().info("DEBUG: player has a home - adding");
	    respawn.add(playerUUID);
	}
	// Mute death messages
	if (Settings.muteDeathMessages) {
	    e.setDeathMessage(null);
	}
    } 

    /*
     * Prevent typing /island if falling - hard core
     * Checked if player teleports
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerFall(final PlayerMoveEvent e) {
	if (e.getPlayer().isDead()) {
	    return;
	}
	/*
	 * too spammy
	 * if (debug) {
	 * plugin.getLogger().info(e.getEventName());
	 * }
	 */
	if (!inASkyGridWorld(e.getPlayer().getWorld())) {
	    // If the player is not in the right world, then cancel any falling flags
	    unsetFalling(e.getPlayer().getUniqueId());
	    return;
	}
	if (Settings.allowTeleportWhenFalling) {
	    return;
	}
	if (!e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || e.getPlayer().isOp()) {
	    return;
	}
	// Check if air below player
	// plugin.getLogger().info("DEBUG:" +
	// Math.round(e.getPlayer().getVelocity().getY()));
	if ((Math.round(e.getPlayer().getVelocity().getY()) < 0L)
		&& e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR
		&& e.getPlayer().getLocation().getBlock().getType() == Material.AIR) {
	    // plugin.getLogger().info("DEBUG: falling");
	    setFalling(e.getPlayer().getUniqueId());
	} else {
	    // plugin.getLogger().info("DEBUG: not falling");
	    unsetFalling(e.getPlayer().getUniqueId());
	}
    }

    /**
     * Prevents teleporting when falling based on setting by stopping commands
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(final PlayerCommandPreprocessEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info(e.getEventName());
	}
	if (Settings.allowTeleportWhenFalling || e.getPlayer().isOp() || !e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
	    return;
	}
	if (!inASkyGridWorld(e.getPlayer().getWorld())) {
	    return;
	}
	// Check commands
	// plugin.getLogger().info("DEBUG: falling command: '" +
	// e.getMessage().substring(1).toLowerCase() + "'");
	if (isFalling(e.getPlayer().getUniqueId()) && Settings.fallingCommandBlockList.contains(e.getMessage().substring(1).toLowerCase())) {
	    // Sorry you are going to die
	    e.getPlayer().sendMessage(plugin.myLocale(e.getPlayer().getUniqueId()).gridcannotTeleport);
	    e.setCancelled(true);
	}
    }

    /**
     * Prevents teleporting when falling based on setting
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info(e.getEventName());
	}
	// We only check if the player is teleporting from an Island world and to is not null
	if (e.getTo() == null || !inASkyGridWorld(e.getPlayer().getWorld())) {
	    return;
	}
	// Check if ready
	if (plugin.getGrid() == null) {
	    return;
	}
	// Teleporting while falling check
	if (!Settings.allowTeleportWhenFalling && e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && !e.getPlayer().isOp()) {
	    //plugin.getLogger().info("DEBUG: teleport when falling is not allowed - check if falling");
	    if (isFalling(e.getPlayer().getUniqueId())) {
		//plugin.getLogger().info("DEBUG: player is falling");
		// Sorry you are going to die
		e.getPlayer().sendMessage(plugin.myLocale(e.getPlayer().getUniqueId()).gridcannotTeleport);
		e.setCancelled(true);
		// Check if the player is in the void and kill them just in case
		if (e.getPlayer().getLocation().getBlockY() < 0) {
		    e.getPlayer().setHealth(0D);
		    unsetFalling(e.getPlayer().getUniqueId());
		}
		return;
	    }
	}
	//plugin.getLogger().info("DEBUG: From : " + e.getFrom());
	//plugin.getLogger().info("DEBUG: To : " + e.getTo());
    }




    /**
     * Used to prevent teleporting when falling
     * 
     * @param uniqueId
     * @return true or false
     */
    public static boolean isFalling(UUID uniqueId) {
	return fallingPlayers.contains(uniqueId);
    }

    /**
     * Used to prevent teleporting when falling
     * 
     * @param uniqueId
     */
    public static void setFalling(UUID uniqueId) {
	fallingPlayers.add(uniqueId);
    }

    /**
     * Unset the falling flag
     * 
     * @param uniqueId
     */
    public static void unsetFalling(UUID uniqueId) {
	// getLogger().info("DEBUG: unset falling");
	fallingPlayers.remove(uniqueId);
    }

    /**
     * Prevents players from using commands like /spawner
     * @param e
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info("Player command " + e.getEventName() + ": " + e.getMessage());
	}
	if (e.getPlayer().isOp() || VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bannedcommands")) {
	    return;
	}
	// Check world
	if (!inASkyGridWorld(e.getPlayer().getWorld())) {
	    return;
	}
	// Check banned commands
	//plugin.getLogger().info(Settings.visitorCommandBlockList.toString());
	String[] args = e.getMessage().substring(1).toLowerCase().split(" ");
	if (Settings.bannedCommandList.contains(args[0])) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).errorNoPermission);
	    e.setCancelled(true);
	}
    }

    /**
     * Prevents fall damage if required
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerFallDamage(final EntityDamageEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info(e.getEventName());
	}
	if (Settings.allowFallDamage) {
	    return;
	}
	// Hurt in island space?
	if (!inASkyGridWorld(e.getEntity().getWorld())) {
	    return;
	}
	if (e.getCause() == null) {
	    return;
	}
	if (e.getCause().equals(DamageCause.FALL)) {
	    e.setCancelled(true);
	}
    }


    /**
     * Removes invalid spawn eggs
     * @param e
     */
    /*
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChestOpen(final InventoryOpenEvent e) {
	if (DEBUG) {
	    plugin.getLogger().info("Chest open " + e.getEventName());
	}
	// Check world
	if (!inASkyGridWorld(e.getPlayer().getWorld())) {
	    return;
	}

	Inventory chestInv = e.getInventory();
	if (chestInv.getType().equals(InventoryType.CHEST)) {
	    if (chestInv.contains(Material.MONSTER_EGG)) {
		for (ItemStack item: chestInv.getContents()) {
		    if (item != null && item.getType().equals(Material.MONSTER_EGG)) {
			if (spawnEggMeta) {
			    SpawnEggMeta me = (SpawnEggMeta)item.getItemMeta();
			    plugin.getLogger().info("DEBUG: monster egg me = " + me.getSpawnedType());
			    if (me.getSpawnedType() == null) {
				item.setType(Material.AIR);
				//plugin.getLogger().info("DEBUG: spawn egg type meta = null");
			    }
			} else {
			    @SuppressWarnings("deprecation")
			    SpawnEgg spawnEgg = (SpawnEgg)item.getData();
			    if (spawnEgg.getSpawnedType() == null) {
				//item.setType(Material.DIAMOND_BLOCK);
				//plugin.getLogger().info("DEBUG: spawn egg type = null ");
			    }
			}
		    }
		}
	    }
	}
    }
    private Method getMethod(String name, Class<?> clazz) {
	for (Method m : clazz.getDeclaredMethods()) {
	    if (m.getName().equals(name))
		return m;
	}
	return null;
    }*/

    /**
     * Checks if a world is an ASkyGrid world.
     * @param world
     * @return true if in an ASkyGrid world
     */
    private boolean inASkyGridWorld(World world) {
	if (world.equals(ASkyGrid.getGridWorld())) {
	    return true;
	}
	if (Settings.createNether && world.equals(ASkyGrid.getNetherWorld())) {
	    return true;
	}
	if (Settings.createEnd && world.equals(ASkyGrid.getEndWorld())) {
	    return true;
	}
	return false;
    }
}