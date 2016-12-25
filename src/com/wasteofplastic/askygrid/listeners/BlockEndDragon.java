package com.wasteofplastic.askygrid.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;

public class BlockEndDragon implements Listener {
    private final ASkyGrid plugin;

    public BlockEndDragon(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * This handles end dragon spawning
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDragonSpawn(EntitySpawnEvent event) {
	if (!Settings.createEnd) {
	    return;
	}
	if (!event.getLocation().getWorld().equals(ASkyGrid.getEndWorld())) {
	    return;
	}
	if (event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
	    //plugin.getLogger().info("DEBUG: removing ender dragon");
	    LivingEntity dragon = (LivingEntity)event.getEntity();
	    dragon.setHealth(0);
	    event.getEntity().remove();
	    event.setCancelled(true);
	}
    }


}
