package com.wasteofplastic.askygrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * This class holds all the data on one district.
 * 
 * @author tastybento
 *
 */
public class ClaimRegion {
    private ASkyGrid plugin;
    private UUID id;
    private UUID owner;
    private Vector pos1;
    private Vector pos2;
    private World world;
    private List<UUID> ownerTrusted = new ArrayList<UUID>();
    private HashMap<String,Object> flags = new HashMap<String,Object>();

    public ClaimRegion(ASkyGrid plugin, Location pos, UUID owner) throws IllegalArgumentException {
	this.pos1 = new Vector(pos.getBlockX() - Settings.claim_protectionRange,0,pos.getBlockZ() - Settings.claim_protectionRange);
	this.pos2 = new Vector(pos.getBlockX() + Settings.claim_protectionRange,0,pos.getBlockZ() + Settings.claim_protectionRange);
	this.world = pos.getWorld();
	if (world == null) {
	    // The world does not exist
	    throw new IllegalArgumentException();
	}
	this.owner = owner;
	this.plugin = plugin;
	this.id = UUID.randomUUID();
	this.ownerTrusted = new ArrayList<UUID>();
	flags.put("allowPVP",Settings.allowPvP);
	flags.put("allowBreakBlocks",Settings.allowBreakBlocks);
	flags.put("allowPlaceBlocks", Settings.allowPlaceBlocks);
	flags.put("allowBedUse", Settings.allowBedUse);
	flags.put("allowBucketUse", Settings.allowBucketUse);
	flags.put("allowShearing", Settings.allowShearing);
	flags.put("allowEnderPearls", Settings.allowEnderPearls);
	flags.put("allowDoorUse", Settings.allowDoorUse);
	flags.put("allowLeverButtonUse", Settings.allowLeverButtonUse);
	flags.put("allowCropTrample", Settings.allowCropTrample);
	flags.put("allowChestAccess", Settings.allowChestAccess);
	flags.put("allowFurnaceUse", Settings.allowFurnaceUse);
	flags.put("allowRedStone", Settings.allowRedStone);
	flags.put("allowMusic", Settings.allowMusic);
	flags.put("allowCrafting", Settings.allowCrafting);
	flags.put("allowBrewing", Settings.allowBrewing);
	flags.put("allowGateUse", Settings.allowGateUse);
	flags.put("allowMobHarm", Settings.allowMobHarm);
	flags.put("enterMessage", "Entering " + plugin.getPlayers().getName(owner) + "'s claim");
	flags.put("farewellMessage", "Leaving " + plugin.getPlayers().getName(owner) + "'s claim");
	
    }





    /**
     * Checks if a location is inside this district
     * @param loc
     * @return true if it is, false otherwise
     */
    public boolean intersectsDistrict(Location loc) {
	//plugin.logger(2,"Checking intersection");
	Vector v = new Vector(loc.getBlockX(),0,loc.getBlockZ());
	//plugin.logger(2,"Pos 1 = " + pos1.toString());
	//plugin.logger(2,"Pos 2 = " + pos2.toString());
	//plugin.logger(2,"V = " + v.toString());
	return v.isInAABB(Vector.getMinimum(pos1,  pos2), Vector.getMaximum(pos1, pos2));
    }

    /**
     * @return the owner
     */
    public UUID getOwner() {
        return owner;
    }


    /**
     * Claims are defined by two points at opposite corners of a bounding box. This is point 1.
     * @return the pos1
     */
    public Location getPos1() {
	return new Location (world, pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ());
    }

    /**
     * Claims are defined by two points at opposite corners of a bounding box. This is point 2.
     * @return the pos2
     */
    public Location getPos2() {
	return new Location (world, pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
    }
    
    /**
     * @return the region flags
     */
    public HashMap<String, Object> getFlags() {
        return flags;
    }


    /**
     * @param flags the flags to set
     */
    public void setFlags(HashMap<String, Object> flags) {
        this.flags = flags;
    }

    /**
     * Obtain a specific region flag
     * @param flagName
     * @return
     */
    public boolean getFlag(String flagName) {
	if (!flags.containsKey(flagName)) {
	    return false;
	}
	return (Boolean)flags.get(flagName);
    }

    /**
     * Set a specific region flag
     * @param flagName
     * @param value
     */
    public void setFlag(String flagName, Boolean value) {
	flags.put(flagName,value);
    }
    
    /**
     * @return the allowPVP
     */
    public Boolean getAllowPVP() {
	return (Boolean)flags.get("allowPVP");
    }

    /**
     * @param uuid 
     * @return the allowBreakBlocks
     */
    public Boolean getAllowBreakBlocks(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowBreakBlocks");
    }

    /**
     * Check if this player (uuid) is associated with this district
     * @param uuid
     * @return
     */
    private Boolean checkOwnerTrusted(UUID uuid) {
	if (owner != null && owner.equals(uuid)) {
	    plugin.getLogger().info("DEBUG: Owner");
	    return true;
	}  else if (ownerTrusted.contains(uuid)) {
	    plugin.getLogger().info("DEBUG: Trusted");
	    return true;
	}
	return false;
    }
    
    /**
     * @return the allowPlaceBlocks
     */
    public Boolean getAllowPlaceBlocks(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowPlaceBlocks");
    }

    /**
     * @return the allowBedUse
     */
    public Boolean getAllowBedUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowBedUse");
    }

    /**
     * @return the allowBucketUse
     */
    public Boolean getAllowBucketUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowBucketUse");
    }

    /**
     * @return the allowShearing
     */
    public Boolean getAllowShearing(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowShearing");
    }

    /**
     * @return the allowEnderPearls
     */
    public Boolean getAllowEnderPearls(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}

	return (Boolean)flags.get("allowEnderPearls");
    }

    /**
     * @return the allowDoorUse
     */
    public Boolean getAllowDoorUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowDoorUse");
    }

    /**
     * @return the allowLeverButtonUse
     */
    public Boolean getAllowLeverButtonUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowLeverButtonUse");
    }

    /**
     * @return the allowCropTrample
     */
    public Boolean getAllowCropTrample(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowCropTrample");
    }

    /**
     * @return the allowChestAccess
     */
    public Boolean getAllowChestAccess(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowChestAccess");
    }

    /**
     * @return the allowFurnaceUse
     */
    public Boolean getAllowFurnaceUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowFurnaceUse");
    }

    /**
     * @return the allowRedStone
     */
    public Boolean getAllowRedStone(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowRedStone");
    }

    /**
     * @return the allowMusic
     */
    public Boolean getAllowMusic(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowMusic");
    }

    /**
     * @return the allowCrafting
     */
    public Boolean getAllowCrafting(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowCrafting");
    }

    /**
     * @return the allowBrewing
     */
    public Boolean getAllowBrewing(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowBrewing");
    }

    /**
     * @return the allowGateUse
     */
    public Boolean getAllowGateUse(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowGateUse");
    }

    public Boolean getAllowHurtMobs(UUID uuid) {
	if (checkOwnerTrusted(uuid)) {
	    return true;
	}
	return (Boolean)flags.get("allowMobHarm");
    }

    
    /**
     * @param pos1 the pos1 to set
     */
    public void setPos1(Vector pos1) {
	this.pos1 = pos1;
    }

    /**
     * @param pos2 the pos2 to set
     */
    public void setPos2(Vector pos2) {
	this.pos2 = pos2;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(UUID owner) {
	this.owner = owner;
    }

    /**
     * @param allowPVP the allowPVP to set
     */
    public void setAllowPVP(Boolean allowPVP) {
	flags.put("allowPVP",allowPVP);
    }

    /**
     * @param allowBreakBlocks the allowBreakBlocks to set
     */
    public void setAllowBreakBlocks(Boolean allowBreakBlocks) {
	flags.put("allowBreakBlocks",allowBreakBlocks);
    }

    /**
     * @param allowPlaceBlocks the allowPlaceBlocks to set
     */
    public void setAllowPlaceBlocks(Boolean allowPlaceBlocks) {
	flags.put("allowPlaceBlocks",allowPlaceBlocks);
    }

    /**
     * @param allowBedUse the allowBedUse to set
     */
    public void setAllowBedUse(Boolean allowBedUse) {
	flags.put("allowBedUse",allowBedUse);
    }

    /**
     * @param allowBucketUse the allowBucketUse to set
     */
    public void setAllowBucketUse(Boolean allowBucketUse) {
	flags.put("allowBucketUse",allowBucketUse);
    }

    /**
     * @param allowShearing the allowShearing to set
     */
    public void setAllowShearing(Boolean allowShearing) {
	flags.put("allowShearing",allowShearing);
    }

    /**
     * @param allowEnderPearls the allowEnderPearls to set
     */
    public void setAllowEnderPearls(Boolean allowEnderPearls) {
	flags.put("allowEnderPearls",allowEnderPearls);
    }

    /**
     * @param allowDoorUse the allowDoorUse to set
     */
    public void setAllowDoorUse(Boolean allowDoorUse) {
	flags.put("allowDoorUse",allowDoorUse);
    }

    /**
     * @param allowLeverButtonUse the allowLeverButtonUse to set
     */
    public void setAllowLeverButtonUse(Boolean allowLeverButtonUse) {
	flags.put("allowLeverButtonUse",allowLeverButtonUse);
    }

    /**
     * @param allowCropTrample the allowCropTrample to set
     */
    public void setAllowCropTrample(Boolean allowCropTrample) {
	flags.put("allowCropTrample",allowCropTrample);
    }

    /**
     * @param allowChestAccess the allowChestAccess to set
     */
    public void setAllowChestAccess(Boolean allowChestAccess) {
	flags.put("allowChestAccess",allowChestAccess);
    }

    /**
     * @param allowFurnaceUse the allowFurnaceUse to set
     */
    public void setAllowFurnaceUse(Boolean allowFurnaceUse) {
	flags.put("allowFurnaceUse",allowFurnaceUse);
    }

    /**
     * @param allowRedStone the allowRedStone to set
     */
    public void setAllowRedStone(Boolean allowRedStone) {
	flags.put("allowRedStone",allowRedStone);
    }

    /**
     * @param allowMusic the allowMusic to set
     */
    public void setAllowMusic(Boolean allowMusic) {
	flags.put("allowMusic",allowMusic);
    }

    /**
     * @param allowCrafting the allowCrafting to set
     */
    public void setAllowCrafting(Boolean allowCrafting) {
	flags.put("allowCrafting",allowCrafting);
    }

    /**
     * @param allowBrewing the allowBrewing to set
     */
    public void setAllowBrewing(Boolean allowBrewing) {
	flags.put("allowBrewing",allowBrewing);
    }

    /**
     * @param allowGateUse the allowGateUse to set
     */
    public void setAllowGateUse(Boolean allowGateUse) {
	flags.put("allowGateUse",allowGateUse);
    }


    /**
     * @return the ownerTrusted
     */
    public boolean isTrusted(Player player) {
	if (ownerTrusted.contains(player.getUniqueId())) {
	    return true;
	}
	return false;
    }

    /**
     * @param Trust a player
     */
    public boolean addOwnerTrusted(UUID trusted) {
	if (ownerTrusted.contains(trusted)) {
	    return false;
	}
	ownerTrusted.add(trusted);
	return true;
    }

    public void removeOwnerTrusted(UUID player) {
	ownerTrusted.remove(player);
    }
    /**
     * @return the ownerTrusted
     */
    public List<String> getOwnerTrusted() {
	List<String> trustedByOwner = new ArrayList<String>();
	for (UUID playerUUID: ownerTrusted) {
	    trustedByOwner.add(plugin.getPlayers().getName(playerUUID));
	}
	return trustedByOwner;
    }
 
    public List<UUID> getOwnerTrustedUUID() {
	return ownerTrusted;
    }

    public List<String> getOwnerTrustedUUIDString() {
	List<String> trustedByOwner = new ArrayList<String>();
	for (UUID playerUUID: ownerTrusted) {
	    trustedByOwner.add(playerUUID.toString());
	}
	return trustedByOwner;
    }

    /**
     * @return the enterMessage
     */
    public String getEnterMessage() {
	if ((Boolean)flags.get("allowPVP")) {
	    return (ChatColor.RED + "[PVP] " + (String)flags.get("enterMessage"));
	}
	return (String)flags.get("enterMessage");
    }


    /**
     * @return the farewallMessage
     */
    public String getFarewellMessage() {
	return (String)flags.get("farewellMessage");
    }


    /**
     * @param enterMessage the enterMessage to set
     */
    public void setEnterMessage(String enterMessage) {
	flags.put("enterMessage",enterMessage);
    }


    /**
     * @param farewallMessage the farewallMessage to set
     */
    public void setFarewellMessage(String farewellMessage) {
	flags.put("farewellMessage",farewellMessage);
    }


    /**
     * @return the id
     */
    public UUID getId() {
	return id;
    }


    /**
     * @param id the id to set
     */
    public void setId(UUID id) {
	this.id = id;
    }


    /**
     * @param ownerTrusted the ownerTrusted to set
     */
    public void setOwnerTrusted(List<UUID> ownerTrusted) {
        this.ownerTrusted = ownerTrusted;
    }


   /**
     * @return the area
     */
    public int getArea() {
	return (Math.abs(pos2.getBlockX()-pos1.getBlockX()) + 1) * (Math.abs(pos2.getBlockZ()-pos1.getBlockZ()) + 1);
    }


    public int getMinX() {
	return Math.min(pos1.getBlockX(),pos2.getBlockX());
    }


    public int getMinZ() {
	return Math.min(pos1.getBlockZ(),pos2.getBlockZ());
    }

    public int getMaxX() {
	return Math.max(pos1.getBlockX(),pos2.getBlockX());
    }


    public int getMaxZ() {
	return Math.max(pos1.getBlockZ(),pos2.getBlockZ());
    }


    /**
     * Check if a location is in this district
     * @param x
     * @param z
     * @return
     */
    public boolean inDistrict(int x, int z) {
	//plugin.getLogger().info("DEBUG: Check:" + x + "," + z + " Min:" + getMinX() + "," + getMinZ()
		//+ " Max:" + getMaxX() + "," + getMaxZ());
	if (x >= getMinX() && z >= getMinZ() && x <= getMaxX() && z <= getMaxZ()) {
	    //plugin.getLogger().info("DEBUG: inside");
	    return true;
	}
	//plugin.getLogger().info("DEBUG: outside");
	return false;
    }  



}
