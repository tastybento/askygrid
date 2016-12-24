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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.GridManager;
import com.wasteofplastic.askygrid.Settings;

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
	// If this is not ASkyGrid then return
	if (!event.getFrom().getWorld().equals(ASkyGrid.getGridWorld()) && !event.getFrom().getWorld().equals(ASkyGrid.getNetherWorld())) {
	    return;
	}
	// Entities don't go through sky grid portals, sorry.
	event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
	// If the nether is disabled then quit immediately
	if (!Settings.createNether) {
	    return;
	}
	Location currentLocation = event.getFrom().clone();
	String currentWorld = currentLocation.getWorld().getName();
	if (!currentWorld.equalsIgnoreCase(Settings.worldName) && !currentWorld.equalsIgnoreCase(Settings.worldName + "_nether")
		&& !currentWorld.equalsIgnoreCase(Settings.worldName + "_the_end")) {
	    return;
	}

	// Determine what portal it is
	switch (event.getCause()) {
	case END_PORTAL:
	    if (Settings.createEnd) {
		// Same action for all worlds except the end itself
		if (!event.getFrom().getWorld().getEnvironment().equals(Environment.THE_END)) {
		    if (ASkyGrid.getEndWorld() != null) {
			// The end exists
			event.setCancelled(true);
			// Convert current location to end world location
			Location end_place = event.getPlayer().getLocation().toVector().toLocation(ASkyGrid.getEndWorld());
			// Set the height to the top of the end grid
			end_place.setY(Settings.gridHeight);
			if (GridManager.isSafeLocation(end_place)) {
			    event.getPlayer().teleport(end_place);
			    return;
			} else {
			    end_place = plugin.getGrid().bigScan(end_place, 10);
			    if (end_place != null) {
				event.getPlayer().teleport(end_place);
			    } else {
				// Try the spawn
				end_place = ASkyGrid.getEndWorld().getSpawnLocation();
				if (GridManager.isSafeLocation(end_place)) {
				    event.getPlayer().teleport(end_place);
				    return;
				} else {
				    event.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(event.getPlayer().getUniqueId()).warpserrorNotSafe);
				}
			    }
			    return;
			}
		    }
		} else {
		    // Returning from the end - just go home
		    event.setCancelled(true);
		    plugin.getGrid().homeTeleport(event.getPlayer());
		}
	    }
	    break;
	case NETHER_PORTAL:
	    // Get the home world of this player
	    if (event.getFrom().getWorld().getEnvironment().equals(Environment.NORMAL)) {
		// Going to Nether
		event.setTo(event.getFrom().toVector().toLocation(ASkyGrid.getNetherWorld()));
		event.useTravelAgent(true); 
	    } else {
		// Returning
		event.setTo(event.getFrom().toVector().toLocation(ASkyGrid.getGridWorld()));
		event.useTravelAgent(true); 
	    }
	    break;
	default:
	    break;
	}
    }
}