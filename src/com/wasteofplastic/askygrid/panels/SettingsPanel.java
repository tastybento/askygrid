package com.wasteofplastic.askygrid.panels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.Settings;

public class SettingsPanel {
    // Island Guard Settings Panel
    private static List<IPItem> ip = new ArrayList<IPItem>();
    private static Inventory newPanel;
    static {
	ip.add(new IPItem(Settings.allowPvP, Material.ARROW, ASkyGrid.getPlugin().myLocale().igsPVP));
	ip.add(new IPItem(Settings.allowNetherPvP, Material.NETHERRACK, ASkyGrid.getPlugin().myLocale().igsNetherPVP));
	ip.add(new IPItem(Settings.allowTeleportWhenFalling, Material.GLASS, ASkyGrid.getPlugin().myLocale().igsTeleport));
	if (ip.size() > 0) {
	    // Make sure size is a multiple of 9
	    int size = ip.size() + 8;
	    size -= (size % 9);
	    newPanel = Bukkit.createInventory(null, size, ASkyGrid.getPlugin().myLocale().igsTitle);
	    // Fill the inventory and return
	    int slot = 0;
	    for (IPItem i : ip) {
		i.setSlot(slot);
		newPanel.addItem(i.getItem());
	    }
	}
    }

    public static Inventory islandGuardPanel() {
	return newPanel;
    }
}