package com.wasteofplastic.askygrid.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.ClaimRegion;
import com.wasteofplastic.askygrid.GetPlayers;
import com.wasteofplastic.askygrid.GetPlayers.Type;
import com.wasteofplastic.askygrid.util.Util;
import com.wasteofplastic.askygrid.util.VaultHelper;

public class ClaimsPanel implements Listener {

    private ASkyGrid plugin;
    private HashMap<UUID, List<CPItem>> controlPanel = new HashMap<UUID, List<CPItem>>();
    //private HashMap<UUID, List<CPItem>> infoPanel = new HashMap<UUID, List<CPItem>>();



    /**
     * @param plugin
     */
    public ClaimsPanel(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    // Check for Inventory Clicking (Control Panel)

    /**
     * @param e
     */

    @EventHandler(priority = EventPriority.LOW)
    public void onControlPanelClick(final InventoryClickEvent e) {
	// Check that it is a control panel
	Inventory panel = e.getInventory();
	if (!panel.getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.myLocale().claimPanelTitle))) {
	    return;
	}
	Player player = (Player)e.getWhoClicked();
	if (!player.getWorld().equals(ASkyGrid.getGridWorld())) {
	    //Wrong world
	    e.setCancelled(true);
	    player.closeInventory();
	    return;
	}
	UUID playerUUID = player.getUniqueId();
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(player.getLocation());

	// Check for clicks outside
	if (e.getSlot() < 0) {
	    player.closeInventory();
	    return;
	}
	// Get the items in the panel for this player
	List<CPItem> cpitems = controlPanel.get(playerUUID);
	Util.logger(2,"DEBUG: slot = " + e.getSlot());
	if (e.getSlot() > cpitems.size()) {
	    e.setCancelled(true);
	    return;
	}
	Util.logger(2,"DEBUG: find out what was clicked");
	// Find out which item was clicked
	CPItem clickedItem = null;
	for (CPItem item : cpitems) {
	    if (item.getSlot() == e.getSlot()) {
		Util.logger(2,"DEBUG: item slot found, item is " + item.getItem().toString());
		Util.logger(2,"DEBUG: clicked item is " + e.getCurrentItem().toString());
		// Check it was the same item and not an item in the player's part of the inventory
		if (e.getCurrentItem().equals(item.getItem())) {
		    Util.logger(2,"DEBUG: item matched!");
		    clickedItem = item;
		    break;
		}
	    }
	}
	if (clickedItem == null) {
	    // Not one of our items
	    Util.logger(2,"DEBUG: not a recognized item");
	    e.setCancelled(true);
	    return;
	}

	switch (clickedItem.getType()) {
	case TOGGLE:
	    Util.logger(2,"DEBUG: toggling settings");
	    // Now toggle the setting
	    clickedItem.setFlagValue(!clickedItem.isFlagValue());
	    // Set the district value
	    d.setFlag(clickedItem.getName(), clickedItem.isFlagValue());
	    // Change the item in this inventory
	    panel.setItem(e.getSlot(), clickedItem.getItem());
	    break;
	case TRUST:
	    getPlayers(d,player,GetPlayers.Type.TRUST);
	    player.closeInventory();
	    break;
	case UNTRUST:
	    getPlayers(d,player,GetPlayers.Type.UNTRUST);
	    player.closeInventory();	    
	    break;
	default:
	    break;
	}
	e.setCancelled(true);
	return;
    }
    
    private void getPlayers(ClaimRegion d, Player player, Type type) {
	final HashMap<Object,Object> map = new HashMap<Object,Object>();
	map.put("Claim", d);

	ConversationFactory factory = new ConversationFactory(plugin);
	Conversation conv = factory.withFirstPrompt(new GetPlayers(plugin,type)).withLocalEcho(false).withInitialSessionData(map)
		.withEscapeSequence("").withTimeout(10).buildConversation(player);
	conv.addConversationAbandonedListener(new ConversationAbandonedListener() {

	    @Override
	    public void conversationAbandoned(ConversationAbandonedEvent event) {
		if (event.getCanceller() instanceof InactivityConversationCanceller) {
		    event.getContext().getForWhom().sendRawMessage(ChatColor.RED + "Cancelling - time out.");
		    return;
		}  
	    }});
	conv.begin();

    }

    /*
    public Inventory infoPanel(Player player) {
	List<ClaimItem> ip = new ArrayList<ClaimItem>();
	// Create in info panel for the district the player is in
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(player.getLocation());
	if (d == null) {
	    player.sendMessage(ChatColor.RED + "You must be in a district to see info!");
	    return null;
	}
	// New panel map
	HashMap<String,Material> icons = new HashMap<String,Material>();
	icons.put("allowShearing",Material.WOOL);
	icons.put("allowGateUse", Material.FENCE_GATE);
	icons.put("allowBucketUse", Material.BUCKET);
	icons.put("allowChestAccess", Material.CHEST);
	icons.put("allowRedStone", Material.REDSTONE);
	icons.put("allowEnderPearls", Material.ENDER_PEARL);
	icons.put("allowFurnaceUse", Material.FURNACE);
	icons.put("allowCrafting", Material.WORKBENCH);
	icons.put("allowBedUse", Material.BED);
	icons.put("allowBrewing", Material.BREWING_STAND_ITEM);
	icons.put("allowDoorUse", Material.TRAP_DOOR);
	icons.put("allowMusic", Material.JUKEBOX);
	icons.put("allowPVP", Material.DIAMOND_SWORD);
	icons.put("allowLeverButtonUse", Material.LEVER);
	icons.put("allowMobHarm", Material.LEATHER);
	icons.put("allowPlaceBlocks", Material.COBBLESTONE);
	icons.put("allowBreakBlocks", Material.MOSSY_COBBLESTONE);
	icons.put("allowCropTrample", Material.WHEAT);
	int slot = 0;
	// Put the owner's name
	UUID o = d.getOwner();

	// Find out if these guys are online
	Player owner = plugin.getServer().getPlayer(o);

	// Get the list of trusted players
	if (o != null) {
	    List<String> trusted = d.getOwnerTrusted();
	    if (!trusted.isEmpty()) {
		trusted.add(0, ChatColor.GREEN + "Trusted players:");
	    }
	    if (owner != null) {

		ip.add(new ClaimItem(Material.SKULL_ITEM, 3,  "Owner: " + owner.getDisplayName(), false, slot++, trusted, ClaimItem.Type.INFO));
	    } else {
		ip.add(new ClaimItem(Material.SKULL_ITEM, 3,  "Owner: " + plugin.getPlayers().getName(o), false, slot++, trusted, ClaimItem.Type.INFO));		
	    }
	}

	// Loop through district flags for this player
	for (String flagName : d.getFlags().keySet()) {
	    // Get the icon
	    if (icons.containsKey(flagName)) {
		//Utils.logger(1,"DEBUG:" + flagName + " : " + d.getFlag(flagName) + " slot " + slot);
		ip.add(new ClaimItem(icons.get(flagName), 0, flagName, d.getFlag(flagName), slot++));
		// Put all the items into the store for this player so when they click on items we know what to do
		infoPanel.put(player.getUniqueId(),ip);
	    }
	}

	if (ip.size() > 0) {
	    // Make sure size is a multiple of 9
	    int size = ip.size() +8;
	    size -= (size % 9);
	    Inventory newPanel = Bukkit.createInventory(player, size, Locale.infoPanelTitle);
	    // Fill the inventory and return
	    for (ClaimItem i : ip) {
		newPanel.addItem(i.getItem());
	    }
	    return newPanel;
	}
	return null;
    }
     */
    public Inventory infoPanel(Player player) {
	List<CPItem> ip = new ArrayList<CPItem>();
	// Create in info panel for the district the player is in
	ClaimRegion d = plugin.getGrid().getClaimRegionAt(player.getLocation());
	if (d == null) {
	    player.sendMessage(ChatColor.RED + "You must be in a district to see info!");
	    return null;
	}
	// New panel map
	HashMap<String,Material> icons = new HashMap<String,Material>();
	icons.put("allowShearing",Material.WOOL);
	icons.put("allowGateUse", Material.FENCE_GATE);
	icons.put("allowBucketUse", Material.BUCKET);
	icons.put("allowChestAccess", Material.CHEST);
	icons.put("allowRedStone", Material.REDSTONE);
	icons.put("allowEnderPearls", Material.ENDER_PEARL);
	icons.put("allowFurnaceUse", Material.FURNACE);
	icons.put("allowCrafting", Material.WORKBENCH);
	icons.put("allowBedUse", Material.BED);
	icons.put("allowBrewing", Material.BREWING_STAND_ITEM);
	icons.put("allowDoorUse", Material.TRAP_DOOR);
	icons.put("allowMusic", Material.JUKEBOX);
	icons.put("allowPVP", Material.DIAMOND_SWORD);
	icons.put("allowLeverButtonUse", Material.LEVER);
	icons.put("allowMobHarm", Material.LEATHER);
	icons.put("allowPlaceBlocks", Material.COBBLESTONE);
	icons.put("allowBreakBlocks", Material.MOSSY_COBBLESTONE);
	icons.put("allowCropTrample", Material.WHEAT);
	int slot = 0;
	// Put the owner's name
	UUID o = d.getOwner();
	// Find out if these guys are online
	Player owner = plugin.getServer().getPlayer(o);

	// Get the list of trusted players
	if (o != null) {
	    List<String> trusted = d.getOwnerTrusted();
	    if (!trusted.isEmpty()) {
		trusted.add(0, ChatColor.GREEN + "Trusted players:");
	    }
	    if (owner != null) {

		ip.add(new CPItem(plugin, Material.SKULL_ITEM, 3,  "Owner: " + owner.getDisplayName(), false, slot++, trusted, CPItem.Type.INFO));
	    } else {
		ip.add(new CPItem(plugin, Material.SKULL_ITEM, 3,  "Owner: " + plugin.getPlayers().getName(o), false, slot++, trusted, CPItem.Type.INFO));		
	    }
	}

	// Loop through district flags for this player
	for (String flagName : d.getFlags().keySet()) {
	    // Get the icon
	    if (icons.containsKey(flagName)) {
		//Utils.logger(1,"DEBUG:" + flagName + " : " + d.getFlag(flagName) + " slot " + slot);
		ip.add(new CPItem(plugin, icons.get(flagName), 0, flagName, d.getFlag(flagName), slot++, null, null));
		// Put all the items into the store for this player so when they click on items we know what to do
		controlPanel.put(player.getUniqueId(),ip);
	    }
	}

	if (ip.size() > 0) {
	    // Make sure size is a multiple of 9
	    int size = ip.size() +8;
	    size -= (size % 9);
	    Inventory newPanel = Bukkit.createInventory(player, size, plugin.myLocale().claimPanelTitle);
	    // Fill the inventory and return
	    for (CPItem i : ip) {
		newPanel.addItem(i.getItem());
	    }
	    return newPanel;
	}
	return null;
    }

    /**
     * Dynamically creates an inventory of challenges for the player
     * @param player
     * @return
     */
    public Inventory controlPanel(Player player) {
	// Create the control panel for the player
	ClaimRegion claim = plugin.getGrid().getClaimRegionAt(player.getLocation());
	if (claim == null) {
	    return null;
	}
	// New panel map
	HashMap<String,Material> icons = new HashMap<String,Material>();
	icons.put("allowShearing",Material.WOOL);
	icons.put("allowGateUse", Material.FENCE_GATE);
	icons.put("allowBucketUse", Material.BUCKET);
	icons.put("allowChestAccess", Material.CHEST);
	icons.put("allowRedStone", Material.REDSTONE);
	icons.put("allowEnderPearls", Material.ENDER_PEARL);
	icons.put("allowFurnaceUse", Material.FURNACE);
	icons.put("allowCrafting", Material.WORKBENCH);
	icons.put("allowBedUse", Material.BED);
	icons.put("allowBrewing", Material.BREWING_STAND_ITEM);
	icons.put("allowDoorUse", Material.TRAP_DOOR);
	icons.put("allowMusic", Material.JUKEBOX);
	icons.put("allowPVP", Material.DIAMOND_SWORD);
	icons.put("allowLeverButtonUse", Material.LEVER);
	icons.put("allowMobHarm", Material.LEATHER);
	icons.put("allowPlaceBlocks", Material.COBBLESTONE);
	icons.put("allowBreakBlocks", Material.MOSSY_COBBLESTONE);
	icons.put("allowCropTrample", Material.WHEAT);
	List<CPItem> cp = new ArrayList<CPItem>();
	int slot = 0;
	if (claim.getOwner().equals(player.getUniqueId()) || player.isOp() || VaultHelper.checkPerm(player, "districts.admin")) {
	    List<String> trusted = claim.getOwnerTrusted();
	    if (!trusted.isEmpty()) {
		trusted.add(0, ChatColor.GREEN + "Owner's trusted players:");
		cp.add(new CPItem(plugin, Material.SKULL_ITEM, 3, "Trust players", false, slot++, trusted, CPItem.Type.TRUST));
		cp.add(new CPItem(plugin, Material.SKULL_ITEM, 4, "Untrust players", false, slot++, null, CPItem.Type.UNTRUST));
	    } else {
		trusted.addAll(Util.chop(ChatColor.YELLOW,"Trusting allows full access to district",20));
		cp.add(new CPItem(plugin, Material.SKULL_ITEM, 3, "Trust players", false, slot++, trusted, CPItem.Type.TRUST));
	    }    
	} 
	// Loop through district flags for this player
	for (String flagName : claim.getFlags().keySet()) {
	    // Get the icon
	    if (icons.containsKey(flagName)) {
		//Utils.logger(1,"DEBUG:" + flagName + " : " + d.getFlag(flagName) + " slot " + slot);

		cp.add(new CPItem(plugin, icons.get(flagName), 0, flagName, claim.getFlag(flagName), slot++, null, CPItem.Type.TOGGLE));
	    }
	}

	// Put all the items into the store for this player so when they click on items we know what to do
	controlPanel.put(player.getUniqueId(),cp);

	if (cp.size() > 0) {
	    // Make sure size is a multiple of 9
	    int size = cp.size() +8;
	    size -= (size % 9);
	    Inventory newPanel = Bukkit.createInventory(player, size, ChatColor.translateAlternateColorCodes('&', plugin.myLocale().claimPanelTitle));
	    // Fill the inventory and return
	    for (CPItem i : cp) {
		newPanel.addItem(i.getItem());
	    }
	    return newPanel;
	}
	return null;
    }

}
