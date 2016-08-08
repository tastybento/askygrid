package com.wasteofplastic.askygrid.protection;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;

public class GGuard {

    RegionManager rm;
    RegionManager rmNether;
    ApplicableRegionSet set;
    ASkyGrid plugin;
    /**
     * @param plugin
     */
    public GGuard(ASkyGrid plugin) {
	this.plugin = plugin;
	rm = WGBukkit.getRegionManager(ASkyGrid.getGridWorld());
	if (Settings.createNether && ASkyGrid.getNetherWorld() != null) {
	    rmNether = WGBukkit.getRegionManager(ASkyGrid.getNetherWorld());
	}
    }

    /**
     * Tries to create a WG region for player at location. Size is determined by the global size variable or
     * the player's permission node.
     * @param player
     * @param loc
     * @return radius size of region created
     */
    public int createRegion(Player player, Location loc) {
	if (loc.getWorld().equals(ASkyGrid.getGridWorld()) || (Settings.createNether && ASkyGrid.getNetherWorld() != null && loc.getWorld().equals(ASkyGrid.getNetherWorld()) ))
	{
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
	    BlockVector l1 = new BlockVector(x,0,z);
	    BlockVector l2 = new BlockVector(x + maxSize*2, loc.getWorld().getMaxHeight(), z + maxSize *2);
	    ProtectedCuboidRegion pr = new ProtectedCuboidRegion("askygrid-" + player.getUniqueId().toString() , l1, l2);
	    LocalPlayer localPlayer = plugin.getWorldGuard().wrapPlayer(player);

	    if (loc.getWorld().equals(ASkyGrid.getGridWorld())) {
		if (rm.overlapsUnownedRegion(pr, localPlayer)) {
		    return 0;
		}
		// Remove any current regions
		rm.removeRegion("askygrid-" + player.getUniqueId().toString());
		if (rmNether != null) {
		    rmNether.removeRegion("askygrid-" + player.getUniqueId().toString());
		}
		// make region
		// Flags
		if (!plugin.myLocale(player.getUniqueId()).warpsentry.isEmpty()) { 
		    StringFlag flag = DefaultFlag.GREET_MESSAGE;
		    pr.setFlag(flag, plugin.myLocale(player.getUniqueId()).warpsentry.replace("[player]", player.getName()));
		}
		if (!plugin.myLocale(player.getUniqueId()).warpsexit.isEmpty()) { 
		    StringFlag flag = DefaultFlag.FAREWELL_MESSAGE;
		    pr.setFlag(flag, plugin.myLocale(player.getUniqueId()).warpsexit.replace("[player]", player.getName()));
		}
		//StateFlag stateFlag = DefaultFlag.BUILD;
		//pr.setFlag(stateFlag, State.DENY);
		//plugin.getLogger().info("DEBUG: adding region");
		pr.getOwners().addPlayer(localPlayer);
		rm.addRegion(pr);
	    } else {
		// Nether warp
		if (rmNether.overlapsUnownedRegion(pr, localPlayer)) {
		    return 0;
		}
		// Remove any current regions
		rm.removeRegion("askygrid-" + player.getUniqueId().toString());
		rmNether.removeRegion("askygrid-" + player.getUniqueId().toString());
		// make region
		// Flags
		if (!plugin.myLocale(player.getUniqueId()).warpsentry.isEmpty()) { 
		    StringFlag flag = DefaultFlag.GREET_MESSAGE;
		    pr.setFlag(flag, plugin.myLocale(player.getUniqueId()).warpsentry.replace("[player]", player.getName()));
		}
		if (!plugin.myLocale(player.getUniqueId()).warpsexit.isEmpty()) { 
		    StringFlag flag = DefaultFlag.FAREWELL_MESSAGE;
		    pr.setFlag(flag, plugin.myLocale(player.getUniqueId()).warpsexit.replace("[player]", player.getName()));
		}
		//StateFlag stateFlag = DefaultFlag.BUILD;
		//pr.setFlag(stateFlag, State.DENY);
		//plugin.getLogger().info("DEBUG: adding region");
		rmNether.addRegion(pr);
	    }
	    return maxSize;
	}
	return 0;
    }

    /**
     * Removes any region that a player has.
     * @param player
     */
    public void removeRegion(UUID player) {
	//plugin.getLogger().info("DEBUG: removing region");
	rm.removeRegion("askygrid-" + player.toString());
	if (rmNether != null) {
	    rmNether.removeRegion("askygrid-" + player.toString());
	}
    }

    /**
     * Removes all the regions when the protection is set to zero
     * @return true if some regions were deleted
     */
    public boolean removeAllRegions() {
	boolean deleted = false;
	for (String region: rm.getRegions().keySet()) {
	    rm.removeRegion(region);
	    deleted = true;
	}
	if (rmNether != null) {
	    for (String region: rmNether.getRegions().keySet()) {
		rmNether.removeRegion(region);
		deleted = true;
	    }
	}
	return deleted;
    }
}
