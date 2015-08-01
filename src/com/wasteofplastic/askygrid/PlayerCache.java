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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

/**
 * Provides a memory cache of online player information
 * This is the one-stop-shop of player info
 * If the player is not cached, then a request is made to Players to obtain it
 * 
 * @author tastybento
 */
public class PlayerCache {
    private HashMap<UUID, Players> playerCache = new HashMap<UUID, Players>();
    private final ASkyGrid plugin;

    public PlayerCache(ASkyGrid plugin) {
	this.plugin = plugin;
	// final Collection<? extends Player> serverPlayers =
	// Bukkit.getServer().getOnlinePlayers();
	for (Player p : getOnlinePlayers()) {
	    if (p.isOnline()) {
		final Players playerInf = new Players(plugin, p.getUniqueId());
		// Add this player to the online cache
		playerCache.put(p.getUniqueId(), playerInf);
	    }
	}
    }

    public static List<Player> getOnlinePlayers() {
	List<Player> list = Lists.newArrayList();
	for (World world : Bukkit.getWorlds()) {
	    list.addAll(world.getPlayers());
	}
	return Collections.unmodifiableList(list);
    }

    /*
     * Cache control methods
     */

    public void addPlayer(final UUID playerUUID) {
	// plugin.getLogger().info("DEBUG: added player");
	if (!playerCache.containsKey(playerUUID)) {
	    final Players player = new Players(plugin, playerUUID);
	    playerCache.put(playerUUID, player);
	}
    }

    /**
     * Stores the player's info to a file and removes the player from the list
     * of currently online players
     * 
     * @param player
     *            - name of player
     */
    public void removeOnlinePlayer(final UUID player) {
	if (playerCache.containsKey(player)) {
	    playerCache.get(player).save();
	    playerCache.remove(player);
	    // plugin.getLogger().info("Removing player from cache: " + player);
	}
    }

    /*
     * Player info query methods
     */
    /**
     * Returns location of player's island from cache if available
     * 
     * @param playerUUID
     * @return Location of player's island
     */
    /*
     * public Location getPlayerIsland(final UUID playerUUID) {
     * if (playerCache.containsKey(playerUUID)) {
     * return playerCache.get(playerUUID).getIslandLocation();
     * }
     * final Players player = new Players(plugin, playerUUID);
     * return player.getIslandLocation();
     * }
     */
    /**
     * Checks if the player is known or not by looking through the filesystem
     * 
     * @param uniqueID
     * @return true if player is know, otherwise false
     */
    public boolean isAKnownPlayer(final UUID uniqueID) {
	if (uniqueID == null) {
	    return false;
	}
	if (playerCache.containsKey(uniqueID)) {
	    return true;
	} else {
	    // Get the file system
	    final File folder = plugin.getPlayersFolder();
	    final File[] files = folder.listFiles();
	    // Go through the native YAML files
	    for (final File f : files) {
		// Need to remove the .yml suffix
		if (f.getName().endsWith(".yml")) {
		    if (UUID.fromString(f.getName().substring(0, f.getName().length() - 4)).equals(uniqueID)) {
			return true;
		    }
		}
	    }
	}
	// Not found, sorry.
	return false;
    }

    /**
     * Returns the player object for the named player
     * 
     * @param playerUUID
     *            - String name of player
     * @return - player object
     */
    public Players get(UUID playerUUID) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID);
    }

    /**
     * Removes any island associated with this player and generally cleans up
     * the player
     * 
     * @param playerUUID
     */
    public void zeroPlayerData(UUID playerUUID) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).clearHomeLocations();
	playerCache.get(playerUUID).save(); // Needed?
    }

    /**
     * Sets the home location for the player
     * @param playerUUID
     * @param location
     * @param number - 1 is default. Can be any number.
     */
    public void setHomeLocation(UUID playerUUID, Location location, int number) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).setHomeLocation(location,number);
    }

    /**
     * Set the default home location for player
     * @param playerUUID
     * @param location
     */
    public void setHomeLocation(UUID playerUUID, Location location) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).setHomeLocation(location,1);
    }

    /**
     * Clears any home locations for player
     * @param playerUUID
     */
    public void clearHomeLocations(UUID playerUUID) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).clearHomeLocations();
    }

    /**
     * Returns the home location, or null if none
     * 
     * @param playerUUID
     * @param number 
     * @return Home location or null if none
     */
    public Location getHomeLocation(UUID playerUUID, int number) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getHomeLocation(number);
    }

    /**
     * Gets the default home location for player
     * @param playerUUID
     * @return Home location or null if none
     */
    public Location getHomeLocation(UUID playerUUID) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getHomeLocation(1);
    }

    /**
     * Provides all home locations for player
     * @param playerUUID
     * @return List of home locations
     */
    public HashMap<Integer, Location> getHomeLocations(UUID playerUUID) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getHomeLocations();
    }

    /**
     * Checks if a challenge has been completed or not
     * 
     * @param playerUUID
     * @param challenge
     * @return
     */
    public boolean checkChallenge(UUID playerUUID, String challenge) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).checkChallenge(challenge);
    }

    /**
     * Checks how often a challenge has been completed
     * 
     * @param playerUUID
     * @param challenge
     * @return
     */
    public int checkChallengeTimes(UUID playerUUID, String challenge) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).checkChallengeTimes(challenge);
    }

    /**
     * Provides the status of all challenges for this player
     * 
     * @param playerUUID
     * @return
     */
    public HashMap<String, Boolean> getChallengeStatus(UUID playerUUID) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getChallengeStatus();
    }

    /**
     * How many times a challenge has been completed
     * 
     * @param playerUUID
     * @return map of completion times
     */
    public HashMap<String, Integer> getChallengeTimes(UUID playerUUID) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getChallengeCompleteTimes();
    }

    public void resetChallenge(UUID playerUUID, String challenge) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).resetChallenge(challenge);
    }

    public void resetAllChallenges(UUID playerUUID) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).resetAllChallenges();
    }

    /**
     * Saves the player's info to the file system
     * 
     * @param playerUUID
     */
    public void save(UUID playerUUID) {
	playerCache.get(playerUUID).save();
	// Save the name + UUID in the database if it ready
	if (plugin.getTinyDB().isDbReady()) {
	    plugin.getTinyDB().savePlayerName(playerCache.get(playerUUID).getPlayerName(), playerUUID);
	}
    }

    public void completeChallenge(UUID playerUUID, String challenge) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).completeChallenge(challenge);
    }

    public boolean challengeExists(UUID playerUUID, String challenge) {
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).challengeExists(challenge);
    }

    /**
     * Attempts to return a UUID for a given player's name
     * 
     * @param string
     * @return
     */
    public UUID getUUID(String string) {
	for (UUID id : playerCache.keySet()) {
	    String name = playerCache.get(id).getPlayerName();
	    // plugin.getLogger().info("DEBUG: Testing name " + name);
	    if (name != null && name.equalsIgnoreCase(string)) {
		return id;
	    }
	}
	// Look in the database if it ready
	if (plugin.getTinyDB().isDbReady()) {
	    return plugin.getTinyDB().getPlayerUUID(string);
	}
	return null;
    }

    /**
     * Sets the player's name and updates the name>UUID database is up to date
     * @param uniqueId
     * @param name
     */
    public void setPlayerName(UUID uniqueId, String name) {
	addPlayer(uniqueId);
	playerCache.get(uniqueId).setPlayerN(name);
	// Save the name in the name database. Note that the old name will still work until someone takes it
	// This feature enables admins to locate 'fugitive' players even if they change their name
	plugin.getTinyDB().savePlayerName(name, uniqueId);
    }

    /**
     * Obtains the name of the player from their UUID
     * Player must have logged into the game before
     * 
     * @param playerUUID
     * @return String - playerName
     */
    public String getName(UUID playerUUID) {
	if (playerUUID == null) {
	    return "";
	}
	addPlayer(playerUUID);
	return playerCache.get(playerUUID).getPlayerName();
    }

    /**
     * Returns the locale for this player. If missing, will return nothing
     * @param playerUUID
     * @return name of the locale this player uses
     */
    public String getLocale(UUID playerUUID) {
	addPlayer(playerUUID);
	if (playerUUID == null) {
	    return "";
	}
	return playerCache.get(playerUUID).getLocale();
    }

    /**
     * Sets the locale this player wants to use
     * @param playerUUID
     * @param localeName
     */
    public void setLocale(UUID playerUUID, String localeName) {
	addPlayer(playerUUID);
	playerCache.get(playerUUID).setLocale(localeName);
    }

    public void removeAllPlayers() {
	for (UUID pl : playerCache.keySet()) {
	    playerCache.get(pl).save();
	}
	playerCache.clear();
    }

    public void setInClaim(UUID uniqueId, Object object) {
	// TODO Auto-generated method stub
	
    }
}
