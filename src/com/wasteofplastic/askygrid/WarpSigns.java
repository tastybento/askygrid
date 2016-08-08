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
package com.wasteofplastic.askygrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.wasteofplastic.askygrid.events.WarpListEvent;
import com.wasteofplastic.askygrid.util.Util;
import com.wasteofplastic.askygrid.util.VaultHelper;

/**
 * Handles warping in ASkyGrid Players can add one sign
 * 
 * @author tastybento
 * 
 */
public class WarpSigns implements Listener {
    private final ASkyGrid plugin;
    // Map of all warps stored as player, warp sign Location
    private HashMap<UUID, Object> warpList = new HashMap<UUID, Object>();
    // Where warps are stored
    private YamlConfiguration welcomeWarps;
    //private HashMap<UUID, Rectangle2D> protectionArea = new HashMap<UUID, Rectangle2D>();

    /**
     * @param plugin
     */
    public WarpSigns(ASkyGrid plugin) {
	this.plugin = plugin;
	this.warpList = new HashMap<UUID, Object>();
    }

    /**
     * Checks to see if a sign has been broken
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignBreak(BlockBreakEvent e) {
	Block b = e.getBlock();
	Player player = e.getPlayer();
	if (b.getWorld().equals(ASkyGrid.getGridWorld())) {
	    if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
		Sign s = (Sign) b.getState();
		if (s != null) {
		    if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
			// Do a quick check to see if this sign location is in
			// the list of warp signs
			if (checkWarp(s.getLocation())) {
			    // Welcome sign detected - check to see if it is
			    // this player's sign
			    final Location playerSignLoc = getWarp(player.getUniqueId());
			    if (playerSignLoc != null) {
				if (playerSignLoc.equals(s.getLocation())) {
				    // This is the player's sign, so allow it to
				    // be destroyed
				    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).warpssignRemoved);
				    if (Settings.claim_protectionRange > 0 && plugin.getGguard() != null) {
					player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpsProtectionLost);
				    }
				    removeWarp(player.getUniqueId());
				} else {
				    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoRemove);
				    e.setCancelled(true);
				}
			    } else {
				// Someone else's sign because this player has
				// none registered
				player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoRemove);
				e.setCancelled(true);
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Event handler for Sign Changes
     * 
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSignWarpCreate(SignChangeEvent e) {
	//plugin.getLogger().info("DEBUG: SignChangeEvent called");
	String title = e.getLine(0);
	Player player = e.getPlayer();
	if (player.getWorld().equals(ASkyGrid.getGridWorld())) {
	    //plugin.getLogger().info("DEBUG: Correct world");
	    if (e.getBlock().getType().equals(Material.SIGN_POST) || e.getBlock().getType().equals(Material.WALL_SIGN)) {
		//plugin.getLogger().info("DEBUG: The first line of the sign says " + title);
		if (title.equalsIgnoreCase(plugin.myLocale().warpswelcomeLine)) {
		    //plugin.getLogger().info("DEBUG: Welcome sign detected");
		    // Welcome sign detected - check permissions
		    if (!(VaultHelper.checkPerm(player, Settings.PERMPREFIX + "player.addwarp"))) {
			player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoPerm);
			return;
		    }
		    // Make WG region.
		    // Check if this sign is close to another sign (will protection overlap?)
		    int claimSize = 0;
		    if (plugin.getWorldGuard() != null && Settings.claim_protectionRange > 0) {
			claimSize = plugin.getGguard().createRegion(player, e.getBlock().getLocation());
			if (claimSize == 0) {
			    // Too close
			    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorNoPlace);
			    return;
			}
		    }
		    // WG region successfully made.

		    // Check if the player already has a sign
		    final Location oldSignLoc = getWarp(player.getUniqueId());
		    if (oldSignLoc == null) {
			//plugin.getLogger().info("DEBUG: Player does not have a sign already");
			// First time the sign has been placed or this is a new
			// sign
			if (addWarp(player, e.getBlock().getLocation())) {
			    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).warpssuccess);
			    if (claimSize > 0) {
				player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).warpsProtectionEnabled.replace("[number]", String.valueOf(claimSize)));
			    }
			    e.setLine(0, ChatColor.GREEN + plugin.myLocale().warpswelcomeLine);
			    for (int i = 1; i<4; i++) {
				e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
			    }			   
			} else {
			    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorDuplicate);
			    e.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
			    for (int i = 1; i<4; i++) {
				e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
			    }
			}
		    } else {
			//plugin.getLogger().info("DEBUG: Player already has a Sign");
			// A sign already exists. Check if it still there and if
			// so,
			// deactivate it
			Block oldSignBlock = oldSignLoc.getBlock();
			if (oldSignBlock.getType().equals(Material.SIGN_POST) || oldSignBlock.getType().equals(Material.WALL_SIGN)) {
			    // The block is still a sign
			    //plugin.getLogger().info("DEBUG: The block is still a sign");
			    Sign oldSign = (Sign) oldSignBlock.getState();
			    if (oldSign != null) {
				//plugin.getLogger().info("DEBUG: Sign block is a sign");
				if (oldSign.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
				    //plugin.getLogger().info("DEBUG: Old sign had a green welcome");
				    oldSign.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
				    oldSign.update();
				    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpsdeactivate);
				    //removeWarp(player.getUniqueId());
				}
			    }
			}
			// Set up the warp
			if (addWarp(player, e.getBlock().getLocation())) {
			    player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).warpssuccess);
			    if (claimSize > 0) {
				player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).warpsProtectionEnabled.replace("[number]", String.valueOf(claimSize)));
			    }
			    e.setLine(0, ChatColor.GREEN + plugin.myLocale().warpswelcomeLine);
			} else {
			    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).warpserrorDuplicate);
			    e.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
			}
		    }
		}
	    }
	}
    }

    /**
     * Saves the warp lists to file
     */
    public void saveWarpList(boolean reloadPanel) {
	if (warpList == null || welcomeWarps == null) {
	    return;
	}
	//plugin.getLogger().info("Saving warps...");
	final HashMap<String, Object> warps = new HashMap<String, Object>();
	for (UUID p : warpList.keySet()) {
	    warps.put(p.toString(), warpList.get(p));
	}
	welcomeWarps.set("warps", warps);
	// Save the protection areas
	/*
	for (UUID p: protectionArea.keySet()) {
	    warps.put(p.toString(), protectionArea.get(p));
	}
	 */
	welcomeWarps.set("protections", warps);
	Util.saveYamlFile(welcomeWarps, "warps.yml");
	// Update the warp panel - needs to be done 1 tick later so that the sign
	// text will be updated.
	if (reloadPanel) {
	    // This is not done on shutdown
	    if (Settings.useWarpPanel && plugin.getWarpPanel() != null) {
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

		    @Override
		    public void run() {
			plugin.getWarpPanel().updatePanel();
		    }});
	    }
	}
	//plugin.getLogger().info("End of saving warps");
    }

    /**
     * Creates the warp list if it does not exist
     */
    public void loadWarpList() {
	plugin.getLogger().info("Loading warps...");
	warpList.clear();
	//protectionArea.clear();
	// warpList.clear();
	welcomeWarps = Util.loadYamlFile("warps.yml");
	if (welcomeWarps.getConfigurationSection("warps") == null) {
	    welcomeWarps.createSection("warps"); // This is only used to create
	    // the warp.yml file so forgive
	    // this code
	}
	HashMap<String, Object> temp = (HashMap<String, Object>) welcomeWarps.getConfigurationSection("warps").getValues(true);
	for (String s : temp.keySet()) {
	    try {
		UUID playerUUID = UUID.fromString(s);
		Location l = Util.getLocationString((String) temp.get(s));
		//plugin.getLogger().info("DEBUG: Loading warp at " + l);
		Block b = l.getBlock();
		// Check that a warp sign is still there
		if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
		    warpList.put(playerUUID, temp.get(s));
		    // Get the protection area
		    /*
		    try {
			Rectangle2D rectangle = (Rectangle2D) welcomeWarps.get("protections." + s);
			protectionArea.put(playerUUID, rectangle);
		    } catch (Exception e) {
			plugin.getLogger().warning("Could not load protection for warp at location " + (String) temp.get(s) + ". Using default protection size");
			// TODO: set default
			e.printStackTrace();
		    }*/
		} else {
		    plugin.getLogger().warning("Warp at location " + (String) temp.get(s) + " has no sign - removing.");
		    if (plugin.getGguard() != null) {
			plugin.getGguard().removeRegion(playerUUID);
			plugin.getLogger().warning("Removed WG protection region.");
		    }
		}
	    } catch (Exception e) {
		plugin.getLogger().severe("Problem loading warp at location " + (String) temp.get(s) + " - ignoring.");
	    }
	}
    }

    /**
     * Stores warps in the warp array. Assumes that checking for the protection area has already been done
     * @param player
     * @param loc
     * @return true if successful, false if not
     */
    public boolean addWarp(Player player, Location loc) {
	final String locS = Util.getStringLocation(loc);
	// Do not allow warps to be in the same location
	if (warpList.containsValue(locS)) {
	    return false;
	}
	warpList.put(player.getUniqueId(), locS);
	// Put the protection
	//protectionArea.put(player.getUniqueId(), getProtectionRectangle(player, loc));
	saveWarpList(true);
	return true;
    }

    /**
     * Returns the protection rectangle applicable for this player
     * @param player
     * @param loc
     * @return rectangle
     */
    /*
    private Rectangle2D getProtectionRectangle(Player player, Location loc) {
	// Get the distance
	int maxSize = Settings.claim_protectionRange;
	for (PermissionAttachmentInfo perms : player.getEffectivePermissions()) {
	    if (perms.getPermission().startsWith(Settings.PERMPREFIX + "protectionradius.")) {
		if (perms.getPermission().contains(Settings.PERMPREFIX + "protectionradius.*")) {
		    maxSize = Settings.claim_protectionRange;
		    break;
		} else {
		    // Get the max value should there be more than one
		    maxSize = Math.max(maxSize, Integer.valueOf(perms.getPermission().split(Settings.PERMPREFIX + "protectionradius.")[1]));
		}
	    }
	    // Do some sanity checking
	    if (maxSize < Settings.claim_protectionRange) {
		maxSize = Settings.claim_protectionRange;
	    }
	}
	double x = loc.getX() - maxSize;
	double z = loc.getZ() - maxSize;
	return new Rectangle2D.Double(x, z, maxSize * 2, maxSize * 2);
    }*/

    /**
     * Removes a warp when the welcome sign is destroyed. Called by
     * WarpSigns.java. Removed the WG region too if it exists.
     * 
     * @param uuid
     */
    public void removeWarp(UUID uuid) {
	if (warpList.containsKey(uuid)) {
	    popSign(Util.getLocationString((String) warpList.get(uuid)));
	    warpList.remove(uuid);
	}
	if (Settings.claim_protectionRange > 0 && plugin.getGguard() != null) {
	    plugin.getGguard().removeRegion(uuid);
	}
	saveWarpList(true);
    }

    private void popSign(Location loc) {
	Block b = loc.getBlock();
	if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
	    Sign s = (Sign) b.getState();
	    if (s != null) {
		if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + plugin.myLocale().warpswelcomeLine)) {
		    s.setLine(0, ChatColor.RED + plugin.myLocale().warpswelcomeLine);
		    s.update();
		}
	    }
	}
    }

    /**
     * Removes a warp at a location. Called by WarpSigns.java.
     * 
     * @param loc
     */
    public void removeWarp(Location loc) {
	final String locS = Util.getStringLocation(loc);
	plugin.getLogger().info("Asked to remove warp at " + locS);
	popSign(loc);
	if (warpList.containsValue(locS)) {
	    // Step through every key (sigh)
	    List<UUID> playerList = new ArrayList<UUID>();
	    for (UUID player : warpList.keySet()) {
		if (locS.equals(warpList.get(player))) {
		    playerList.add(player);
		}
	    }
	    for (UUID rp : playerList) {
		warpList.remove(rp);
		boolean removed = false;
		// Remove region
		if (plugin.getWorldGuard() != null && Settings.claim_protectionRange > 0) {
		    plugin.getGguard().removeRegion(rp);
		    removed = true;
		}
		final Player p = plugin.getServer().getPlayer(rp);
		if (p != null) {
		    // Inform the player
		    p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).warpssignRemoved);
		    if (removed) {
			p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).warpsProtectionLost);
		    }
		}
		plugin.getLogger().warning(rp.toString() + "'s welcome sign at " + loc.toString() + " was removed by something.");
	    }
	} else {
	    plugin.getLogger().info("Not in the list which is:");
	    for (UUID player : warpList.keySet()) {
		plugin.getLogger().info(player.toString() + "," + warpList.get(player));
	    }

	}
	saveWarpList(true);
    }

    /**
     * Returns true if the location supplied is a warp location
     * 
     * @param loc
     * @return true if this location has a warp sign, false if not
     */
    public boolean checkWarp(Location loc) {
	final String locS = Util.getStringLocation(loc);
	if (warpList.containsValue(locS)) {
	    return true;
	}
	return false;
    }

    /**
     * Lists all the known warps
     * 
     * @return String set of warps
     */
    public Set<UUID> listWarps() {
	return warpList.keySet();
    }

    /**
     * @return Sorted list of warps with most recent players listed first
     */
    public Collection<UUID> listSortedWarps() {
	// Bigger value of time means a more recent login
	TreeMap<Long, UUID> map = new TreeMap<Long, UUID>();
	for (UUID uuid : warpList.keySet()) {
	    map.put(plugin.getServer().getOfflinePlayer(uuid).getLastPlayed(), uuid);
	}
	Collection<UUID> result = map.descendingMap().values();
	// Fire event
	WarpListEvent event = new WarpListEvent(plugin, result);
	plugin.getServer().getPluginManager().callEvent(event);
	// Get the result of any changes by listeners
	result = event.getWarps();
	return result;
    }
    /**
     * Provides the location of the warp for player or null if one is not found
     * 
     * @param player
     *            - the warp requested
     * @return Location of warp
     */
    public Location getWarp(UUID player) {
	if (warpList.containsKey(player)) {
	    return Util.getLocationString((String) warpList.get(player));
	} else {
	    return null;
	}
    }

    /**
     * @param location
     * @return Name of warp owner
     */
    public String getWarpOwner(Location location) {
	for (UUID playerUUID : warpList.keySet()) {
	    Location l = Util.getLocationString((String) warpList.get(playerUUID));
	    if (l.equals(location)) {
		return plugin.getPlayers().getName(playerUUID);
	    }
	}
	return "";
    }

    /**
     * Checks if a player is in a protected area. 
     * @param playerUUID
     * @param loc
     * @return true if in a protected area, false if not or in your own protected area
     */
    /*
    public boolean inProtectedArea(UUID playerUUID, Location loc) {
	for (Entry<UUID, Rectangle2D> rect: protectionArea.entrySet()) {
	    // Only check non player areas
	    if (!playerUUID.equals(rect.getKey()) && rect.getValue().contains(loc.getX(), loc.getZ())) {
		// check world
		Location l = Util.getLocationString((String) warpList.get(playerUUID));
		if (l.getWorld().equals(loc.getWorld())) {
		    return true;
		}		
	    }
	}
	return false;
    }
     */
}