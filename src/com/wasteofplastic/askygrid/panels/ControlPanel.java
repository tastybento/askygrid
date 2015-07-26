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
package com.wasteofplastic.askygrid.panels;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.Util;

/**
 * @author tastybento
 *         Provides a handy control panel and minishop
 */
public class ControlPanel implements Listener {

    private static YamlConfiguration cpFile;
    private ASkyGrid plugin;
    private static String defaultPanelName;

    /**
     * @param plugin
     */
    public ControlPanel(ASkyGrid plugin) {
	this.plugin = plugin;
	loadControlPanel();
    }

    /**
     * Map of panel contents by name
     */
    private static HashMap<String, HashMap<Integer, CPItem>> panels = new HashMap<String, HashMap<Integer, CPItem>>();
    // public static final Inventory challenges = Bukkit.createInventory(null,
    // 9, ChatColor.YELLOW + "Challenges");

    /**
     * Map of CP inventories by name
     */
    public static HashMap<String, Inventory> controlPanel = new HashMap<String, Inventory>();

    

    // The first parameter, is the inventory owner. I make it null to let
    // everyone use it.
    // The second parameter, is the slots in a inventory. Must be a multiple of
    // 9. Can be up to 54.
    // The third parameter, is the inventory name. This will accept chat colors.

    /**
     * This loads the control panel from the controlpanel.yml file
     */
    public static void loadControlPanel() {
	ASkyGrid plugin = ASkyGrid.getPlugin();
	// Map of known panel contents by name
	panels.clear();
	// Map of panel inventories by name
	controlPanel.clear();
	cpFile = Util.loadYamlFile("controlpanel.yml");
	ConfigurationSection controlPanels = cpFile.getRoot();
	if (controlPanels == null) {
	    plugin.getLogger().severe("Controlpanel.yml is corrupted! Delete so it can be regenerated or fix!");
	    return;
	}
	// Go through the yml file and create inventories and panel maps
	for (String panel : controlPanels.getKeys(false)) {
	    // plugin.getLogger().info("DEBUG: Panel " + panel);
	    ConfigurationSection panelConf = cpFile.getConfigurationSection(panel);
	    // New panel map
	    HashMap<Integer, CPItem> cp = new HashMap<Integer, CPItem>();
	    String panelName = ChatColor.translateAlternateColorCodes('&', panelConf.getString("panelname", "Commands"));
	    if (panel.equalsIgnoreCase("default")) {
		defaultPanelName = panelName;
	    }
	    // plugin.getLogger().info("DEBUG: Panel section " + panelName);
	    // plugin.getLogger().info("DEBUG: putting panel " +
	    // newPanel.getName());
	    ConfigurationSection buttons = cpFile.getConfigurationSection(panel + ".buttons");
	    if (buttons != null) {
		// Get how many buttons can be in the CP
		int size = buttons.getKeys(false).size() + 8;
		size -= (size % 9);
		// Add inventory to map of inventories
		controlPanel.put(panelName, Bukkit.createInventory(null, size, panelName));
		// Run through buttons
		int slot = 0;
		for (String item : buttons.getKeys(false)) {
		    try {
			String m = buttons.getString(item + ".material", "BOOK");
			// Split off damage
			String[] icon = m.split(":");
			// plugin.getLogger().info("Material = " + m);
			Material material = Material.matchMaterial(icon[0]);
			String description = ChatColor.translateAlternateColorCodes('&',buttons.getString(item + ".description", ""));
			String command = buttons.getString(item + ".command", "").replace("[island]", Settings.ISLANDCOMMAND);
			String nextSection = buttons.getString(item + ".nextsection", "");
			ItemStack i = new ItemStack(material);
			if (icon.length == 2) {
			    i.setDurability(Short.parseShort(icon[1]));
			}
			CPItem cpItem = new CPItem(i, description, command, nextSection);
			cp.put(slot, cpItem);
			controlPanel.get(panelName).setItem(slot, cpItem.getItem());
			slot++;
		    } catch (Exception e) {
			plugin.getLogger().warning("Problem loading control panel " + panel + " item #" + slot);
			plugin.getLogger().warning(e.getMessage());
			e.printStackTrace();
		    }
		}
		// Add overall control panel
		panels.put(panelName, cp);
	    }
	}
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
	// TODO : this needs optimization
	Player player = (Player) event.getWhoClicked(); // The player that
							// clicked the item
	ItemStack clicked = event.getCurrentItem(); // The item that was clicked
	Inventory inventory = event.getInventory(); // The inventory that was
						    // clicked in
	// ASkyBlock plugin = ASkyBlock.getPlugin();
	int slot = event.getRawSlot();
	// Settings
	if (inventory.getName().equalsIgnoreCase(plugin.myLocale().igsTitle)) {
	    if (event.getSlotType() == SlotType.OUTSIDE) {
		player.closeInventory();
		return;
	    }
	    event.setCancelled(true);
	    return;
	}
	// Challenges
	if (inventory.getName().equals(plugin.myLocale(player.getUniqueId()).challengesguiTitle)) {
	    event.setCancelled(true);
	    if (event.getSlotType() == SlotType.OUTSIDE) {
		player.closeInventory();
		return;
	    }

	    // Get the list of items in this inventory
	    // plugin.getLogger().info("DEBUG: You clicked on slot " + slot);
	    List<CPItem> challenges = plugin.getChallenges().getCP(player);
	    if (challenges == null) {
		plugin.getLogger().warning("Player was accessing Challenge Inventory, but it had lost state - was server restarted?");
		player.closeInventory();
		player.performCommand(Settings.CHALLENGECOMMAND);
		return;
	    }
	    // plugin.getLogger().info("DEBUG: Challenges size = " +
	    // challenges.size());
	    if (slot >= 0 && slot < challenges.size()) {
		CPItem item = challenges.get(slot);
		// plugin.getLogger().info("DEBUG: CP Item is " +
		// item.getItem().toString());
		// plugin.getLogger().info("DEBUG: Clicked is " +
		// clicked.toString());
		// Check that it is the top items that are being clicked on
		// These two should be identical because it is made before
		if (clicked.equals(item.getItem())) {
		    // plugin.getLogger().info("DEBUG: You clicked on a challenge item");
		    // plugin.getLogger().info("DEBUG: performing  /" +
		    // item.getCommand());
		    // plugin.getLogger().info("DEBUG: going to " +
		    // item.getNextSection());
		    // Next section indicates the level of panel to open
		    if (item.getNextSection() != null) {
			player.closeInventory();
			player.openInventory(plugin.getChallenges().challengePanel(player, item.getNextSection()));
		    } else if (item.getCommand() != null) {
			player.performCommand(item.getCommand());
			player.closeInventory();
			player.openInventory(plugin.getChallenges().challengePanel(player));
		    }
		}
	    }
	}
	// Check control panels
	for (String panelName : controlPanel.keySet()) {
	    if (inventory.getName().equals(panelName)) {
		// plugin.getLogger().info("DEBUG: panels length " +
		// panels.size());
		// plugin.getLogger().info("DEBUG: panel name " + panelName);
		if (slot == -999) {
		    player.closeInventory();
		    event.setCancelled(true);
		    return;
		}
		HashMap<Integer, CPItem> thisPanel = panels.get(panelName);
		if (slot >= 0 && slot < thisPanel.size()) {
		    // plugin.getLogger().info("DEBUG: slot is " + slot);
		    // Do something
		    String command = thisPanel.get(slot).getCommand();
		    String nextSection = ChatColor.translateAlternateColorCodes('&', thisPanel.get(slot).getNextSection());
		    if (!command.isEmpty()) {
			player.closeInventory(); // Closes the inventory
			event.setCancelled(true);
			// plugin.getLogger().info("DEBUG: performing command "
			// + command);
			player.performCommand(command);
			return;
		    }
		    if (!nextSection.isEmpty()) {
			player.closeInventory(); // Closes the inventory
			Inventory next = controlPanel.get(nextSection);
			if (next == null) {
			    // plugin.getLogger().info("DEBUG: next panel is null");
			}
			// plugin.getLogger().info("DEBUG: opening next cp "+nextSection);
			player.openInventory(next);
			event.setCancelled(true);
			return;
		    }
		    player.closeInventory(); // Closes the inventory
		    event.setCancelled(true);
		    return;
		}
	    }
	}
    }

    /**
     * @return the defaultPanelName
     */
    public static String getDefaultPanelName() {
	return defaultPanelName;
    }
}