package com.wasteofplastic.askygrid;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Provides a programming interface
 * 
 * @author tastybento
 */
public class ASkyGridAPI {
    private static ASkyGridAPI instance = new ASkyGridAPI(ASkyGrid.getPlugin());

    /**
     * @return the instance
     */
    public static ASkyGridAPI getInstance() {
	return instance;
    }

    private ASkyGrid plugin;

    private ASkyGridAPI(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * @param playerUUID
     * @return HashMap of all of the known challenges with a boolean marking
     *         them as complete (true) or incomplete (false)
     */
    public HashMap<String, Boolean> getChallengeStatus(UUID playerUUID) {
	return plugin.getPlayers().getChallengeStatus(playerUUID);
    }

    public Location getHomeLocation(UUID playerUUID) {
	return plugin.getPlayers().getHomeLocation(playerUUID,1);
    }

    /**
     * Provides location of the player's warp sign
     * 
     * @param playerUUID
     * @return Location of sign or null if one does not exist
     */
    public Location getWarp(UUID playerUUID) {
	return plugin.getWarpSignsListener().getWarp(playerUUID);
    }

    /**
     * Get the owner of the warp at location
     * 
     * @param location
     * @return Returns name of player or empty string if there is no warp at
     *         that spot
     */
    public String getWarpOwner(Location location) {
	return plugin.getWarpSignsListener().getWarpOwner(location);
    }

    /**
     * Lists all the known warps. As each player can have only one warp, the
     * player's UUID is used. It can be displayed however you like to other
     * users.
     * 
     * @return String set of warps
     */
    public Set<UUID> listWarps() {
	return plugin.getWarpSignsListener().listWarps();
    }

    /**
     * Forces the warp panel to update and the warp list event to fire so that
     * the warps can be sorted how you like.
     */
    public void updateWarpPanel() {
	plugin.getWarpPanel().updatePanel();
    }
    
    /**
   /**
     * Sets a message for the player to receive next time they login
     * 
     * @param playerUUID
     * @param message
     * @return true if player is offline, false if online
     */
    public boolean setMessage(UUID playerUUID, String message) {
	return plugin.getMessages().setMessage(playerUUID, message);
    }

    /**
     * Get the island overworld
     * @return the island overworld
     */
    public World getIslandWorld() {
	return ASkyGrid.getGridWorld();
    }
    
    /**
     * Get the nether world
     * @return the nether world
     */
    public World getNetherWorld() {
	return ASkyGrid.getNetherWorld();
    }
    
}
