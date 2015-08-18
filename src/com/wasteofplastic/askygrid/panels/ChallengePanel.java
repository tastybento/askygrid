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
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.wasteofplastic.askygrid.panels;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;


/**
 * @author tastybento
 */
public class ChallengePanel implements Listener {

    private ASkyGrid plugin;

    /**
     * @param plugin
     */
    public ChallengePanel(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    /**
     * Map of CP inventories by name
     */
    public static HashMap<String, Inventory> challengePanel = new HashMap<String, Inventory>();


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
	// TODO : this needs optimization
	Player player = (Player) event.getWhoClicked(); // The player that
							// clicked the item
	ItemStack clicked = event.getCurrentItem(); // The item that was clicked
	Inventory inventory = event.getInventory(); // The inventory that was
						    // clicked in
	int slot = event.getRawSlot();
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
		// Check that it is the top items that are being clicked on
		// These two should be identical because it is made before
		if (clicked.equals(item.getItem())) {
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
    }
}