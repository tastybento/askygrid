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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.PlayerCache;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.VaultHelper;

public class JoinLeaveEvents implements Listener {
    private ASkyGrid plugin;
    private PlayerCache players;

    public JoinLeaveEvents(ASkyGrid plugin) {
	this.plugin = plugin;
	this.players = plugin.getPlayers();
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
	final Player player = event.getPlayer();
	final UUID playerUUID = player.getUniqueId();
	// Check language permission
	if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "island.lang")) {
	    // Get language
	    String language = getLanguage(player);
	    //plugin.getLogger().info("DEBUG: language = " + language);
	    // Check if we have this language
	    if (plugin.getResource("locale/" + language + ".yml") != null) {
		if (plugin.getPlayers().getLocale(playerUUID).isEmpty()) {
		    plugin.getPlayers().setLocale(playerUUID, language);
		}
	    }
	} else {
	    // Default locale
	    plugin.getPlayers().setLocale(playerUUID,"");
	}
	if (players == null) {
	    plugin.getLogger().severe("players is NULL");
	}
	// Load any messages for the player
	// plugin.getLogger().info("DEBUG: Checking messages for " +
	// player.getName());
	final List<String> messages = plugin.getMessages().getMessages(playerUUID);
	if (messages != null) {
	    // plugin.getLogger().info("DEBUG: Messages waiting!");
	    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
		@Override
		public void run() {
		    player.sendMessage(ChatColor.AQUA + plugin.myLocale(playerUUID).newsHeadline);
		    int i = 1;
		    for (String message : messages) {
			player.sendMessage(i++ + ": " + message);
		    }
		    // Clear the messages
		    plugin.getMessages().clearMessages(playerUUID);
		}
	    }, 40L);
	} // else {
	// plugin.getLogger().info("no messages");
	// }

	// Set the player's name (it may have changed), but only if it isn't empty
	if (!player.getName().isEmpty()) {
	    players.setPlayerName(playerUUID, player.getName());
	    // Add to tinyDB
	    plugin.getTinyDB().savePlayerName(player.getName(), playerUUID);
	} else {
	    plugin.getLogger().warning("Player that just logged in has no name! " + playerUUID.toString());
	}
	players.save(playerUUID);
	if (Settings.logInRemoveMobs && player.getWorld().getName().startsWith(Settings.worldName)) {
	    plugin.getGrid().removeMobs(player.getLocation());
	}
   }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
	players.removeOnlinePlayer(event.getPlayer().getUniqueId());
    }

    /**
     * Attempts to get the player's language
     * @param p
     * @return language or empty string
     */
    public String getLanguage(Player p){
	Object ep;
	try {
	    ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
	    Field f = ep.getClass().getDeclaredField("locale");
	    f.setAccessible(true);
	    String language = (String) f.get(ep);
	    language.replace('_', '-');
	    return language;
	} catch (Exception e) {
	    //nothing
	}
	return "en-US";
    }

    private Method getMethod(String name, Class<?> clazz) {
	for (Method m : clazz.getDeclaredMethods()) {
	    if (m.getName().equals(name))
		return m;
	}
	return null;
    }

}