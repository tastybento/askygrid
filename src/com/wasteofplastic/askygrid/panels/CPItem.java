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
package com.wasteofplastic.askygrid.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.wasteofplastic.askygrid.ASkyGrid;


/**
 * Holds all the control panel item info
 * @author tastybento
 *
 */
public class CPItem {
    private ASkyGrid plugin;
    private ItemStack item;
    private List<String> description = new ArrayList<String>();
    private String name;
    private int slot;
    private boolean flagValue;
    public enum Type { TOGGLE, TEXT, INFO, TRUST, UNTRUST, TOGGLEINFO };
    private Type type;
    private String command;

    // For warps
    public CPItem(ASkyGrid plugin, ItemStack itemStack, String command) {
	this.plugin = plugin;
	this.command = command;
	this.item = itemStack;
    }    

    public CPItem(ASkyGrid plugin, Material material, String name, String command) {
	this.plugin = plugin;
	this.command = command;
	item = new ItemStack(material);
	ItemMeta meta = item.getItemMeta();
	// Handle multi line names (split by |)
	List<String> desc = new ArrayList<String>(Arrays.asList(name.split("\\|")));
	meta.setDisplayName(desc.get(0));
	if (desc.size() > 1) {
	    desc.remove(0); // Remove the name
	    meta.setLore(desc);
	}
	item.setItemMeta(meta);
    }
    
    public CPItem(ASkyGrid plugin, ItemStack itemStack, String name, String command) {
	this.plugin = plugin;
	this.command = command;
	this.item = itemStack;
	ItemMeta meta = item.getItemMeta();
	// Handle multi line names (split by |)
	List<String> desc = new ArrayList<String>(Arrays.asList(name.split("\\|")));
	meta.setDisplayName(desc.get(0));
	if (desc.size() > 1) {
	    desc.remove(0); // Remove the name
	    meta.setLore(desc);
	}
	item.setItemMeta(meta);
    }
    
    /**
     * @param plugin 
     * @param item
     * @param material
     * @param durability 
     * @param name
     * @param b
     * @param nextSection
     */
    public CPItem(ASkyGrid plugin, Material material, int durability, String name, boolean flagValue, int slot, List<String> desc, Type type) {
	this.plugin = plugin;
	item = new ItemStack(material);
	item.setDurability((short) durability);
	createItem(item, name, flagValue,slot,desc,type);
    }

    /**
     * @param item
     * @param name
     * @param flagValue
     * @param slot
     * @param desc
     * @param type
     */
    public CPItem(ASkyGrid plugin, ItemStack item, String name, boolean flagValue, int slot, List<String> desc, Type type) {
	this.plugin = plugin;
	createItem(item, name, flagValue, slot,desc, type);
    }
    
    private void createItem(ItemStack item, String name, boolean flagValue, int slot, List<String> desc, Type type) {
	this.item = item;
	this.flagValue = flagValue;
	this.slot = slot;
	this.name = name;
	this.type = type;
	description.clear();
	ItemMeta meta = item.getItemMeta();
	switch (type) {
	case TOGGLEINFO:
	    meta.setDisplayName(name.substring(5));
	    if (flagValue) {
		description.add(ChatColor.GREEN + plugin.myLocale().igsAllowed);
	    } else {
		description.add(ChatColor.RED + plugin.myLocale().igsDisallowed);	    
	    }
	    meta.setLore(description);
	    item.setItemMeta(meta);
	    break;
	case TEXT:
	    meta.setDisplayName(name);
	    break;
	case TOGGLE:
	    meta.setDisplayName(name.substring(5));
	    if (flagValue) {
		description.add(ChatColor.GREEN + plugin.myLocale().igsAllowed);
		description.add(ChatColor.RED + plugin.myLocale().cpclicktotoggle);
	    } else {
		description.add(ChatColor.RED + plugin.myLocale().igsAllowed);
		description.add(ChatColor.GREEN + plugin.myLocale().cpclicktotoggle);	    
	    }
	    break;
	default:
	    meta.setDisplayName(name);
	    if (desc != null)
		description = desc;
	    break;
	}
	meta.setLore(description);
	item.setItemMeta(meta);
    }

    public void setLore(List<String> lore) {
	ItemMeta meta = item.getItemMeta();
	meta.setLore(lore);
	item.setItemMeta(meta);
    }

    public ItemStack getItem() {
	return item;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
	return slot;
    }

    /**
     * @return the flagValue
     */
    public boolean isFlagValue() {
	return flagValue;
    }

    /**
     * @param flagValue the flagValue to set
     */
    public void setFlagValue(boolean flagValue) {
	this.flagValue = flagValue;
	description.clear();
	ItemMeta meta = item.getItemMeta();
	switch (type) {
	case TOGGLE:
	    if (flagValue) {
		description.add(ChatColor.GREEN + plugin.myLocale().igsAllowed);
		description.add(ChatColor.RED + plugin.myLocale().cpclicktotoggle);
	    } else {
		description.add(ChatColor.RED + plugin.myLocale().igsDisallowed);
		description.add(ChatColor.GREEN + plugin.myLocale().cpclicktotoggle);	    
	    }

	    break;
	default:
	    break;

	}
	meta.setLore(description);
	item.setItemMeta(meta);
    }




    public String getName() {
	return name;
    }

    /**
     * @return the type
     */
    public Type getType() {
	return type;
    }

    public String getCommand() {
	return command;
    }


}