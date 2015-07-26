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
package com.wasteofplastic.askygrid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.wasteofplastic.askygrid.util.Util;

/**
 * Tracks the following info on the player
 * 
 * @author tastybento
 */
public class Players {
    private ASkyGrid plugin;
    private YamlConfiguration playerInfo;
    private HashMap<String, Boolean> challengeList;
    private HashMap<String, Integer> challengeListTimes;
    private HashMap<Integer, Location> homeLocations;
    private UUID uuid;
    private String playerName;
    private String locale;

    /**
     * @param uuid
     *            Constructor - initializes the state variables
     * 
     */
    public Players(final ASkyGrid aSkyBlock, final UUID uuid) {
	this.plugin = aSkyBlock;
	this.uuid = uuid;
	this.homeLocations = new HashMap<Integer,Location>();
	this.challengeList = new HashMap<String, Boolean>();
	this.challengeListTimes = new HashMap<String, Integer>();
	this.playerName = "";
	this.locale = "";
	load(uuid);
    }

    /**
     * Loads a player from file system and if they do not exist, then it is
     * created
     * 
     * @param uuid
     */
    public void load(UUID uuid) {
	playerInfo = Util.loadYamlFile("players/" + uuid.toString() + ".yml");
	// Load in from YAML file
	this.playerName = playerInfo.getString("playerName", "");
	if (playerName.isEmpty()) {
	    try {
		playerName = plugin.getServer().getOfflinePlayer(uuid).getName();
	    } catch (Exception e) {
		plugin.getLogger().severe("Could not obtain a name for the player with UUID " + uuid.toString());
		playerName = "";
	    }
	    if (playerName == null) {
		plugin.getLogger().severe("Could not obtain a name for the player with UUID " + uuid.toString());
		playerName = "";
	    }
	}
	// Locale
	this.locale = playerInfo.getString("locale","");
	// Old home location storage
	Location homeLocation = Util.getLocationString(playerInfo.getString("homeLocation",""));
	// New home location storage
	if (homeLocation != null) {
	    // Transfer the old into the new
	    this.homeLocations.put(1,homeLocation);
	} else {
	    // Import
	    if (playerInfo.contains("homeLocations")) {
		// Import to hashmap
		for (String number : playerInfo.getConfigurationSection("homeLocations").getValues(false).keySet()) {
		    try {
			int num = Integer.valueOf(number);
			Location loc = Util.getLocationString(playerInfo.getString("homeLocations." + number));
			homeLocations.put(num, loc);
		    } catch (Exception e) {
			plugin.getLogger().warning("Error importing home locations for " + playerName);
		    }
		}
	    }
	}
	// Challenges
	// Run through all challenges available
	for (String challenge : Settings.challengeList) {
	    // If they are in the list, then use the value, otherwise use false
	    challengeList.put(challenge, playerInfo.getBoolean("challenges.status." + challenge, false));
	    challengeListTimes.put(challenge, playerInfo.getInt("challenges.times." + challenge, 0));
	}
    }

    /**
     * Saves the player info to the file system
     */
    public void save() {
	//plugin.getLogger().info("Saving player..." + playerName);
	// Save the variables
	playerInfo.set("playerName", playerName);
	// Clear any old home locations
	playerInfo.set("homeLocations",null);
	for (int num : homeLocations.keySet()) {
	    playerInfo.set("homeLocations." + num, Util.getStringLocation(homeLocations.get(num)));
	}
	// Save the challenges
	playerInfo.set("challenges",null);
	for (String challenge : challengeList.keySet()) {
	    playerInfo.set("challenges.status." + challenge, challengeList.get(challenge));
	}
	for (String challenge : challengeListTimes.keySet()) {
	    playerInfo.set("challenges.times." + challenge, challengeListTimes.get(challenge));
	}
	// Locale
	playerInfo.set("locale", locale);
	// Actually save the file
	Util.saveYamlFile(playerInfo, "players/" + uuid.toString() + ".yml");
    }

    /**
     * A maintenance function. Rebuilds the challenge list for this player.
     * Should be used when the challenges change, e.g. config.yml changes.
     */
    public void updateChallengeList() {
	// If it does not exist, then make it
	if (challengeList == null) {
	    challengeList = new HashMap<String, Boolean>();
	}
	// Iterate through all the challenges in the config.yml and if they are
	// not in the list the add them as yet to be done
	final Iterator<?> itr = Settings.challengeList.iterator();
	while (itr.hasNext()) {
	    final String current = (String) itr.next();
	    if (!challengeList.containsKey(current.toLowerCase())) {
		challengeList.put(current.toLowerCase(), Boolean.valueOf(false));
	    }
	}
	// If the challenge list is bigger than the number of challenges in the
	// config.yml (some were removed?)
	// then remove the old ones - the ones that are no longer in Settings
	if (challengeList.size() > Settings.challengeList.size()) {
	    final Object[] challengeArray = challengeList.keySet().toArray();
	    for (int i = 0; i < challengeArray.length; i++) {
		if (!Settings.challengeList.contains(challengeArray[i].toString())) {
		    challengeList.remove(challengeArray[i].toString());
		}
	    }
	}
    }

    /**
     * Checks if a challenge exists in the player's challenge list
     * 
     * @param challenge
     * @return true if challenge is listed in the player's challenge list,
     *         otherwise false
     */
    public boolean challengeExists(final String challenge) {
	if (challengeList.containsKey(challenge.toLowerCase())) {
	    return true;
	}
	// for (String s : challengeList.keySet()) {
	// ASkyBlock.getInstance().getLogger().info("DEBUG: challenge list: " +
	// s);
	// }
	return false;
    }

    /**
     * Checks if a challenge is recorded as completed in the player's challenge
     * list or not
     * 
     * @param challenge
     * @return true if the challenge is listed as complete, false if not
     */
    public boolean checkChallenge(final String challenge) {
	if (challengeList.containsKey(challenge.toLowerCase())) {
	    // plugin.getLogger().info("DEBUG: " + challenge + ":" +
	    // challengeList.get(challenge.toLowerCase()).booleanValue() );
	    return challengeList.get(challenge.toLowerCase()).booleanValue();
	}
	return false;
    }

    /**
     * Checks how many times a challenge has been done
     * 
     * @param challenge
     * @return
     */
    public int checkChallengeTimes(final String challenge) {
	if (challengeListTimes.containsKey(challenge.toLowerCase())) {
	    // plugin.getLogger().info("DEBUG: check " + challenge + ":" +
	    // challengeListTimes.get(challenge.toLowerCase()).intValue() );
	    return challengeListTimes.get(challenge.toLowerCase()).intValue();
	}
	return 0;
    }

    public HashMap<String, Boolean> getChallengeStatus() {
	return challengeList;
    }

    /**
     * Records the challenge as being complete in the player's list If the
     * challenge is not listed in the player's challenge list already, then it
     * will not be recorded! TODO: Possible systemic bug here as a result
     * 
     * @param challenge
     */
    public void completeChallenge(final String challenge) {
	// plugin.getLogger().info("DEBUG: Complete challenge");
	if (challengeList.containsKey(challenge)) {
	    challengeList.remove(challenge);
	    challengeList.put(challenge, Boolean.valueOf(true));
	    // Count how many times the challenge has been done
	    int times = 0;
	    if (challengeListTimes.containsKey(challenge)) {
		times = challengeListTimes.get(challenge);
	    }
	    times++;
	    challengeListTimes.put(challenge, times);
	    // plugin.getLogger().info("DEBUG: complete " + challenge + ":" +
	    // challengeListTimes.get(challenge.toLowerCase()).intValue() );
	}
    }

    /**
     * Gets the default home location.
     * @return
     */
    public Location getHomeLocation() {
	return getHomeLocation(1); // Default
    }

    /**
     * Gets the home location by number. Note that the number is a string (to avoid conversion)
     * @param number
     * @return Location of this home or null if not available
     */
    public Location getHomeLocation(int number) {
	if (homeLocations.containsKey(number)) {
	    return homeLocations.get(number);
	} else {
	    return null;
	}
    }

    /**
     * Provides a list of all home locations - used when searching for a safe spot to place someone
     * @return List of home locations
     */
    public HashMap<Integer,Location> getHomeLocations() {
	HashMap<Integer,Location> result = new HashMap<Integer,Location>();
	for (int number : homeLocations.keySet()) {
	    result.put(number, homeLocations.get(number));
	}
	return result;
    }

    public Player getPlayer() {
	return Bukkit.getPlayer(uuid);
    }

    public UUID getPlayerUUID() {
	return uuid;
    }

    public String getPlayerName() {
	return playerName;
    }

    public void setPlayerN(String playerName) {
	this.playerName = playerName;
    }

    /**
     * Resets all the challenges for the player and rebuilds the challenge list
     */
    public void resetAllChallenges() {
	challengeList.clear();
	challengeListTimes.clear();
	updateChallengeList();
    }

    /**
     * Resets a specific challenge. Will not reset a challenge that does not
     * exist in the player's list TODO: Add a success or failure return
     * 
     * @param challenge
     */
    public void resetChallenge(final String challenge) {
	if (challengeList.containsKey(challenge)) {
	    challengeList.put(challenge, Boolean.valueOf(false));
	    challengeListTimes.put(challenge, 0);
	}
    }

    /**
     * Stores the home location of the player in a String format
     * 
     * @param l
     *            a Bukkit location
     */
    public void setHomeLocation(final Location l) {
	setHomeLocation(l, 1);
    }

    /**
     * Stores the numbered home location of the player. Numbering starts at 1. 
     * @param location
     * @param number
     */
    public void setHomeLocation(final Location location, int number) {
	if (location == null) {
	    homeLocations.clear();
	} else {
	    // Make the location x,y,z integer, but keep the yaw and pitch
	    homeLocations.put(number, new Location(location.getWorld(),location.getBlockX(),location.getBlockY(),location.getBlockZ(),location.getYaw(), location.getPitch()));
	}
    }

    /**
     * @param s
     *            a String name of the player
     */
    public void setPlayerUUID(final UUID s) {
	uuid = s;
    }

    /**
     * @return the challengeListTimes
     */
    public HashMap<String, Integer> getChallengeCompleteTimes() {
	return challengeListTimes;
    }

    /**
     * Clears all home Locations
     */
    public void clearHomeLocations() {
	homeLocations.clear();
    }

    /**
     * @return the locale
     */
    public String getLocale() {
	return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
	this.locale = locale;
    }
}