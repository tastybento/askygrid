package com.wasteofplastic.askygrid.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.potion.Potion;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.ClaimRegion;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.Util;


public class GridGuard implements Listener {

    private ASkyGrid plugin;
    private boolean debug = false;

    public GridGuard(final ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * Tracks player movement
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
	Player player = event.getPlayer();
	World world = player.getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	if (player.getVehicle() != null) {
	    return; // handled in vehicle listener
	}
	// Check if there are any claims
	if (!plugin.getGrid().isClaims()) {
	    return;
	}
	// Did we move a block? Only check in x and z
	if (event.getFrom().getBlockX() != event.getTo().getBlockX()
		|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
	    boolean result = checkMove(player, event.getFrom(), event.getTo());
	    if (result) {
		Location newLoc = event.getFrom();
		newLoc.setX(newLoc.getBlockX() + 0.5);
		newLoc.setY(newLoc.getBlockY());
		newLoc.setZ(newLoc.getBlockZ() + 0.5);
		event.setTo(newLoc);
	    }
	}
    }


    /**
     * @param player
     * @param from
     * @param to
     * @return false if the player can move into that area, true if not allowed
     */
    private boolean checkMove(Player player, Location from, Location to) {
	ClaimRegion fromClaim= plugin.getGrid().getClaimRegionAt(from);
	ClaimRegion toClaim = plugin.getGrid().getClaimRegionAt(to);
	// No claim interaction
	if (fromClaim == null && toClaim == null) {
	    // Clear the claim flag (the claim may have been deleted while they were offline)
	    plugin.getPlayers().setInClaim(player.getUniqueId(), null);
	    return false;	    
	} else if (fromClaim == toClaim) {
	    // Set the claim - needs to be done if the player teleports too (should be done on a teleport event)
	    plugin.getPlayers().setInClaim(player.getUniqueId(), toClaim);
	    return false;
	}
	if (fromClaim != null && toClaim == null) {
	    // leaving a claim
	    if (!fromClaim.getFarewellMessage().isEmpty()) {
		player.sendMessage(fromClaim.getFarewellMessage());
	    }
	    plugin.getPlayers().setInClaim(player.getUniqueId(), null);
	} else if (fromClaim == null && toClaim != null){
	    // Going into a claim
	    if (!toClaim.getEnterMessage().isEmpty()) {
		player.sendMessage(toClaim.getEnterMessage());
	    }
	    plugin.getPlayers().setInClaim(player.getUniqueId(), toClaim);	    

	} else if (fromClaim != null && toClaim != null){
	    // Leaving one claim and entering another claim
	    if (!fromClaim.getFarewellMessage().isEmpty()) {
		player.sendMessage(fromClaim.getFarewellMessage());
	    }
	    if (!toClaim.getEnterMessage().isEmpty()) {
		player.sendMessage(toClaim.getEnterMessage());
	    }
	    plugin.getPlayers().setInClaim(player.getUniqueId(), toClaim);	    
	}  
	return false;
    }

    // Vehicle damage
    @EventHandler(priority = EventPriority.LOW)
    void vehicleDamageEvent(VehicleDamageEvent e){
	if (e.getVehicle() instanceof Boat) {
	    // Boats can always be hit
	    return;
	}
	if (!(e.getAttacker() instanceof Player)) {
	    return;
	}
	Player p = (Player)e.getAttacker();
	World world = p.getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	if (p.isOp()) {
	    // You can do anything if you are Op
	    return;
	}
	// Get the claim that this block is in (if any)
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getVehicle().getLocation());
	if (d == null) {
	    // Not in a claim
	    return;
	}
	if (!d.getAllowBreakBlocks(p.getUniqueId())) {
	    p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}

    }

    /**
     * Prevents blocks from being broken
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(final BlockBreakEvent e) {
	World world = e.getBlock().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}

	// Get the claim that this block is in (if any)
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBlock().getLocation());
	if (d == null || e.getPlayer().isOp()) {
	    // Not in a claim
	    return;
	}
	if (!d.getAllowBreakBlocks(e.getPlayer().getUniqueId())) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }

    /**
     * This method protects players from PVP if it is not allowed and from arrows fired by other players
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(final EntityDamageByEntityEvent e) {
	World world = e.getEntity().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// Get the claim that this block is in (if any)
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getEntity().getLocation());
	if (d == null) {
	    Util.logger(2,"Not in a claim");
	    return;	    
	}
	// Ops can do anything
	if (e.getDamager() instanceof Player) {
	    if (((Player)e.getDamager()).isOp()) {
		return;
	    }
	}
	// Check to see if it's an item frame
	if (e.getEntity() instanceof ItemFrame) {
	    if (e.getDamager() instanceof Player) {
		if (!d.getAllowBreakBlocks(e.getDamager().getUniqueId())) {
		    Player p = (Player)e.getDamager();
		    p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return;
		}
	    } else if (e.getDamager() instanceof Projectile) {
		// Prevent projectiles shot by players from removing items from frames
		Projectile p = (Projectile)e.getDamager();
		if (p.getShooter() instanceof Player) {
		    if (!d.getAllowBreakBlocks(((Player)p.getShooter()).getUniqueId())) {
			((Player)p.getShooter()).sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).claimProtected);
			e.setCancelled(true);
			return;
		    }		    
		}
	    } 

	}
	// If the attacker is non-human and not an arrow then everything is okay
	if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Projectile)) {
	    return;
	}
	Util.logger(2,"Entity is " + e.getEntity().toString());
	// Check for player initiated damage
	if (e.getDamager() instanceof Player) {
	    Util.logger(2,"Damager is " + ((Player)e.getDamager()).getName());
	    // If the target is not a player check if mobs can be hurt
	    if (!(e.getEntity() instanceof Player)) {
		if (e.getEntity() instanceof Monster) {
		    Util.logger(2,"Entity is a monster - ok to hurt"); 
		    return;
		} else {
		    Util.logger(2,"Entity is a non-monster - check if ok to hurt"); 
		    UUID playerUUID = e.getDamager().getUniqueId();
		    if (playerUUID == null) {
			Util.logger(2,"player ID is null");
		    }
		    if (!d.getAllowHurtMobs(playerUUID)) {
			Player p = (Player)e.getDamager();
			p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).claimProtected);
			e.setCancelled(true);
			return;
		    }
		    return;
		}
	    } else {
		// PVP
		// If PVP is okay then return
		// Target is in a claim
		if (d.getAllowPVP()) {
		    Util.logger(2,"PVP allowed");
		    return;
		}
		Util.logger(2,"PVP not allowed");

	    }
	}

	Util.logger(2,"Player attack (or arrow)");
	// Only damagers who are players or arrows are left
	// If the projectile is anything else than an arrow don't worry about it in this listener
	// Handle splash potions separately.
	if (e.getDamager() instanceof Arrow) {
	    Util.logger(2,"Arrow attack");
	    Arrow arrow = (Arrow)e.getDamager();
	    // It really is an Arrow
	    if (arrow.getShooter() instanceof Player) {
		Player shooter = (Player)arrow.getShooter();
		Util.logger(2,"Player arrow attack");
		if (e.getEntity() instanceof Player) {
		    Util.logger(2,"Player vs Player!");
		    // Arrow shot by a player at another player
		    if (!d.getAllowPVP()) {
			Util.logger(2,"Target player is in a no-PVP claim!");
			((Player)arrow.getShooter()).sendMessage("Target is in a no-PVP claim!");
			e.setCancelled(true);
			return;
		    } 
		} else {
		    if (!(e.getEntity() instanceof Monster)) {
			Util.logger(2,"Entity is a non-monster - check if ok to hurt"); 
			UUID playerUUID = shooter.getUniqueId();
			if (!d.getAllowHurtMobs(playerUUID)) {
			    shooter.sendMessage(ChatColor.RED + plugin.myLocale(shooter.getUniqueId()).claimProtected);
			    e.setCancelled(true);
			    return;
			}
			return;
		    }
		}
	    }
	} else if (e.getDamager() instanceof Player){
	    Util.logger(2,"Player attack");
	    // Just a player attack
	    if (!d.getAllowPVP()) {
		((Player)e.getDamager()).sendMessage("Target is in a no-PVP claim!");
		e.setCancelled(true);
		return;
	    } 
	}
	return;
    }


    /**
     * Prevents placing of blocks
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBlockPlace(final BlockPlaceEvent e) {
	World world = e.getBlock().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	//"DEBUG: correct world");
	// If the offending block is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBlock().getLocation());
	if (d == null) {
	    //"DEBUG: claim is null!");
	    return;
	}
	if (!d.getAllowPlaceBlocks(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}

    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBlockPlace(final HangingPlaceEvent e) {
	World world = e.getBlock().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// If the offending block is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBlock().getLocation());
	if (d == null) {
	    return;
	}
	if (!d.getAllowPlaceBlocks(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }

    // Prevent sleeping in other beds
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBedEnter(final PlayerBedEnterEvent e) {
	World world = e.getBed().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// If the offending bed is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBed().getLocation());
	if (d == null) {
	    return;
	}
	if (!d.getAllowBedUse(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }
    /**
     * Prevents the breakage of hanging items
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBreakHanging(final HangingBreakByEntityEvent e) {
	World world = e.getEntity().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	if (!(e.getRemover() instanceof Player)) {
	    // Enderman?
	    return;
	}
	// If the offending item is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getEntity().getLocation());
	if (d == null) {
	    return;
	}
	Player p = (Player)e.getRemover();
	if (!d.getAllowBreakBlocks(e.getRemover().getUniqueId()) && !p.isOp()) {
	    p.sendMessage(ChatColor.RED + plugin.myLocale(p.getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
	World world = e.getBlockClicked().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// If the offending item is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBlockClicked().getLocation());
	if (d == null) {
	    return;
	}
	if (!d.getAllowBucketUse(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBucketFill(final PlayerBucketFillEvent e) {
	World world = e.getBlockClicked().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// If the offending item is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getBlockClicked().getLocation());
	if (d == null) {
	    return;
	}

	if (!d.getAllowBucketUse(e.getPlayer().getUniqueId()) && !e.getPlayer().isOp()) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }

    // Protect sheep
    @EventHandler(priority = EventPriority.LOW)
    public void onShear(final PlayerShearEntityEvent e) {
	World world = e.getEntity().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// If the offending item is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getEntity().getLocation());
	if (d == null) {
	    return;
	}
	if (!d.getAllowShearing(e.getPlayer().getUniqueId())) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }

    // Stop lava flow or water into a claim
    @EventHandler(priority = EventPriority.LOW)
    public void onFlow(final BlockFromToEvent e) {
	// Flow may be allowed anyway
	if (Settings.allowFlowIn && Settings.allowFlowOut) {
	    return;
	}
	World world = e.getBlock().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	// Only check lateral movement
	if (e.getBlock().getLocation().getBlockX() == e.getToBlock().getLocation().getBlockX()
		&& e.getBlock().getLocation().getBlockZ() == e.getToBlock().getLocation().getBlockZ()) {
	    return;
	}
	// Ignore flows within flow
	if (e.getBlock().getType().equals(e.getToBlock().getType())) {
	    return;
	}
	// Ignore stationary to non-stationary
	if (e.getBlock().getType().equals(Material.STATIONARY_WATER) && e.getToBlock().getType().equals(Material.WATER) ) {
	    return;
	}
	if (e.getBlock().getType().equals(Material.STATIONARY_LAVA) && e.getToBlock().getType().equals(Material.LAVA) ) {
	    return;
	}
	// Get To and From claims
	//"DEBUG: " + e.getBlock().getType() + " to " + e.getToBlock().getType());
	ClaimRegion to = plugin.getGrid().getClaimRegionAt(e.getToBlock().getLocation());
	ClaimRegion from = plugin.getGrid().getClaimRegionAt(e.getBlock().getLocation());

	// Scenarios
	// 1. inside claim or outside - always ok
	// 2. inside to outside - allowFlowOut determines
	// 3. outside to inside - allowFlowIn determines
	if (to == null && from == null) {
	    return;
	}
	if (to !=null && from != null && to.equals(from)) {
	    return;
	}
	// to or from or both are claims, NOT the same and flow is across a boundary
	// if to is a claim, flow in is allowed 
	if (to != null && Settings.allowFlowIn) {
	    return;
	}
	// if from is a claim, flow may allowed
	if (from != null && Settings.allowFlowOut) {
	    return;
	}
	// Otherwise cancel - the flow is not allowed
	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(final PlayerInteractEntityEvent e) {
	World world = e.getPlayer().getWorld();
	if (!world.equals(ASkyGrid.getGridWorld())) {
	    return;
	}
	Util.logger(3,"Frame right click!");
	Entity entity = e.getRightClicked();
	Util.logger(3,entity.getType().toString());
	if (entity.getType() != EntityType.ITEM_FRAME) {
	    return;
	}
	ItemFrame frame = (ItemFrame)entity;
	if ((frame.getItem() == null || frame.getItem().getType() == Material.AIR)) {
	    Util.logger(3,"Nothing in frame!");
	    return;
	}
	// If the offending item is not in a claim, forget it!
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(entity.getLocation());
	if (d == null) {
	    Util.logger(3,"Not in a claim!");
	    return;
	}
	if (!d.getAllowChestAccess(e.getPlayer().getUniqueId())) {
	    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
	    e.setCancelled(true);
	}
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(final PlayerInteractEvent e) {
	if (!Settings.worldName.isEmpty() && !Settings.worldName.contains(e.getPlayer().getWorld().getName())) {
	    return;
	}
	// Check for disallowed clicked blocks
	if (e.getClickedBlock() != null) {
	    // If the offending item is not in a claim, forget it!
	    ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getClickedBlock().getLocation());
	    if (d == null) {
		return;
	    }

	    Util.logger(2,"DEBUG: clicked block " + e.getClickedBlock());
	    Util.logger(2,"DEBUG: Material " + e.getMaterial());

	    switch (e.getClickedBlock().getType()) {
	    case WOODEN_DOOR:
	    case SPRUCE_DOOR:
	    case ACACIA_DOOR:
	    case DARK_OAK_DOOR:
	    case BIRCH_DOOR:
	    case JUNGLE_DOOR:
	    case TRAP_DOOR:
		if (!d.getAllowDoorUse(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case FENCE_GATE:
	    case SPRUCE_FENCE_GATE:
	    case ACACIA_FENCE_GATE:
	    case DARK_OAK_FENCE_GATE:
	    case BIRCH_FENCE_GATE:
	    case JUNGLE_FENCE_GATE:
		if (!d.getAllowGateUse(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return;  
		}
		break;
	    case CHEST:
	    case TRAPPED_CHEST:
	    case ENDER_CHEST:
	    case DISPENSER:
	    case DROPPER:
	    case HOPPER:
	    case HOPPER_MINECART:
	    case STORAGE_MINECART:
		if (!d.getAllowChestAccess(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case SOIL:
		if (!d.getAllowCropTrample(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case BREWING_STAND:
	    case CAULDRON:
		if (!d.getAllowBrewing(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case CAKE_BLOCK:
		break;
	    case DIODE:
	    case DIODE_BLOCK_OFF:
	    case DIODE_BLOCK_ON:
	    case REDSTONE_COMPARATOR_ON:
	    case REDSTONE_COMPARATOR_OFF:
		if (!d.getAllowRedStone(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case ENCHANTMENT_TABLE:
		break;
	    case FURNACE:
	    case BURNING_FURNACE:
		if (!d.getAllowFurnaceUse(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case ICE:
		break;
	    case ITEM_FRAME:
		if (!d.getAllowPlaceBlocks(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case JUKEBOX:
	    case NOTE_BLOCK:
		if (!d.getAllowMusic(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    case PACKED_ICE:
		break;
	    case STONE_BUTTON:
	    case WOOD_BUTTON:
	    case LEVER:
		if (!d.getAllowLeverButtonUse(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}	
		break;
	    case TNT:
		break;
	    case WORKBENCH:
		if (!d.getAllowCrafting(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		    return; 
		}
		break;
	    default:
		break;
	    }
	}
	// Check for disallowed in-hand items
	if (e.getMaterial() != null) {
	    // If the player is not in a claim, forget it!
	    ClaimRegion d = plugin.getGrid().getClaimRegionAt(e.getPlayer().getLocation());
	    if (d == null) {
		return;
	    }

	    if (e.getMaterial().equals(Material.BOAT) && (e.getClickedBlock() != null && !e.getClickedBlock().isLiquid())) {
		// Trying to put a boat on non-liquid
		e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		e.setCancelled(true);
		return;
	    }
	    if (e.getMaterial().equals(Material.ENDER_PEARL)) {
		if (!d.getAllowEnderPearls(e.getPlayer().getUniqueId())) {
		    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
		    e.setCancelled(true);
		}
		return;
	    } else if (e.getMaterial().equals(Material.POTION) && e.getItem().getDurability() != 0) {
		// Potion
		Util.logger(2,"DEBUG: potion");
		try {
		    Potion p = Potion.fromItemStack(e.getItem());
		    if (!p.isSplash()) {
			Util.logger(2,"DEBUG: not a splash potion");
			return;
		    } else {
			// Splash potions are allowed only if PVP is allowed
			if (!d.getAllowPVP()) {
			    e.getPlayer().sendMessage(ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).claimProtected);
			    e.setCancelled(true);
			}
		    }
		} catch (Exception ex) {
		}
	    }
	    // Everything else is okay
	}
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
	if (debug) {
	    plugin.getLogger().info(e.getEventName());
	    plugin.getLogger().info("Entity exploding is " + e.getEntity());
	}
	// Find out what is exploding
	EntityType exploding = e.getEntityType();
	if (exploding == null) {
	    return;
	}
	if (!e.getEntity().getWorld().equals(ASkyGrid.getGridWorld()) && !e.getEntity().getWorld().equals(ASkyGrid.getNetherWorld())) {
	    return;
	}
	switch (exploding) {
	case CREEPER:
	    if (!Settings.allowChestDamage) {
		List<Block> toberemoved = new ArrayList<Block>();
		// Save the chest blocks in a list
		for (Block b : e.blockList()) {
		    switch (b.getType()) {
		    case CHEST:
		    case ENDER_CHEST:
		    case STORAGE_MINECART:
		    case TRAPPED_CHEST:
			toberemoved.add(b);
			break;
		    default:
			break;
		    }
		}
		// Now delete them
		for (Block b : toberemoved) {
		    e.blockList().remove(b);
		}
	    }
	    break;
	case PRIMED_TNT:
	case MINECART_TNT:
	    if (!Settings.allowTNTDamage) {
		// plugin.getLogger().info("TNT block damage prevented");
		e.blockList().clear();
	    } else {
		if (!Settings.allowChestDamage) {
		    List<Block> toberemoved = new ArrayList<Block>();
		    // Save the chest blocks in a list
		    for (Block b : e.blockList()) {
			switch (b.getType()) {
			case CHEST:
			case ENDER_CHEST:
			case STORAGE_MINECART:
			case TRAPPED_CHEST:
			    toberemoved.add(b);
			    break;
			default:
			    break;
			}
		    }
		    // Now delete them
		    for (Block b : toberemoved) {
			e.blockList().remove(b);
		    }
		}
	    }
	    break;
	default:
	    break;
	}
    }

}
