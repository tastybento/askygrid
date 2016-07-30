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

package com.wasteofplastic.askygrid.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.wasteofplastic.askygrid.ASkyGrid;
import com.wasteofplastic.askygrid.GridManager;
import com.wasteofplastic.askygrid.SafeSpotTeleport;
import com.wasteofplastic.askygrid.Settings;
import com.wasteofplastic.askygrid.util.Util;
import com.wasteofplastic.askygrid.util.VaultHelper;


/**
 * This class handles admin commands
 * 
 */
public class AdminCmd implements CommandExecutor, TabCompleter {
    private ASkyGrid plugin;

    public AdminCmd(ASkyGrid plugin) {
	this.plugin = plugin;
    }

    private void help(CommandSender sender, String label) {
	if (!(sender instanceof Player)) {
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " completechallenge <challengename> <player>:" + ChatColor.WHITE + " "
		    + plugin.myLocale().adminHelpcompleteChallenge);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " delete <player>:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpdelete);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " info <player>:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpinfo);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " info challenges <player>:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpinfo);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " info:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpinfoPlayer);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpreload);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " resetallchallenges <player>:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpresetAllChallenges);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " resetchallenge <challengename> <player>:" + ChatColor.WHITE + " "
		    + plugin.myLocale().adminHelpresetChallenge);
	    sender.sendMessage(ChatColor.YELLOW + "/" + label + " resethome <player>:" + ChatColor.WHITE + " " + plugin.myLocale().adminHelpResetHome);
	} else {
	    // Only give help if the player has permissions
	    // Permissions are split into admin permissions and mod permissions
	    // Listed in alphabetical order
	    Player player = (Player) sender;
	    player.sendMessage(plugin.myLocale(player.getUniqueId()).adminHelpHelp);
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " completechallenge <challengename> <player>:" + ChatColor.WHITE + " "
			+ plugin.myLocale(player.getUniqueId()).adminHelpcompleteChallenge);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "admin.delete") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " delete <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpdelete);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.info") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " info <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpinfo);
		player.sendMessage(ChatColor.YELLOW + "/" + label + " info challenges <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpinfo);

	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "admin.reload") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " reload:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpreload);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.resethome") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " resethome <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpResetHome);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " resetchallenge <challengename> <player>:" + ChatColor.WHITE + " "
			+ plugin.myLocale(player.getUniqueId()).adminHelpresetChallenge);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " resetallchallenges <player>:" + ChatColor.WHITE + " "
			+ plugin.myLocale(player.getUniqueId()).adminHelpresetAllChallenges);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.resethome") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " sethome <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelpSetHome);
	    }
	    if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.tp") || player.isOp()) {
		player.sendMessage(ChatColor.YELLOW + "/" + label + " tp <player>:" + ChatColor.WHITE + " " + plugin.myLocale(player.getUniqueId()).adminHelptp);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * @see
     * org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender
     * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
	// Console commands
	Player player;
	if (sender instanceof Player) {
	    player = (Player) sender;
	    if (split.length > 0) {
		// Admin-only commands : reload, register, delete and purge
		if (split[0].equalsIgnoreCase("reload")) {
		    if (!checkAdminPerms(player, split)) {
			player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).errorNoPermission);
			return true;
		    }
		} else {
		    // Mod commands
		    if (!checkModPerms(player, split)) {
			player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).errorNoPermission);
			return true;
		    }
		}
	    }
	}
	// Check for zero parameters e.g., /asadmin
	switch (split.length) {
	case 0:
	    help(sender, label);
	    return true;
	case 1:
	    if (split[0].equalsIgnoreCase("reload")) {
		plugin.reloadConfig();
		plugin.loadPluginConfig();
		plugin.getChallenges().reloadChallengeConfig();
		sender.sendMessage(ChatColor.YELLOW + plugin.myLocale().reloadconfigReloaded);
		return true;
	    } else {
		sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownCommand);
		return false;
	    }
	case 2:
	    if (split[0].equalsIgnoreCase("resethome")) { 
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[1]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		} else {
		    Location safeHome = plugin.getGrid().getSafeHomeLocation(playerUUID, 1);
		    if (safeHome == null) {
			sender.sendMessage(ChatColor.RED + plugin.myLocale().adminSetHomeNoneFound);
		    } else {
			plugin.getPlayers().setHomeLocation(playerUUID, safeHome);
			sender.sendMessage(ChatColor.GREEN + plugin.myLocale().adminSetHomeHomeSet.replace("[location]", safeHome.getBlockX() + ", " + safeHome.getBlockY() + "," + safeHome.getBlockZ()));
		    }
		}
		return true;
	    } else if (split[0].equalsIgnoreCase("sethome")) { 
		if (!(sender instanceof Player)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUseInGame);
		    return true;
		}
		player = (Player)sender;
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[1]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		} else {
		    // Check that the location is safe
		    if (!GridManager.isSafeLocation(player.getLocation())) {
			// Not safe
			player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).adminSetHomeNoneFound);
		    } else {
			// Success
			plugin.getPlayers().setHomeLocation(playerUUID, player.getLocation());
			player.sendMessage(ChatColor.GREEN + plugin.myLocale(player.getUniqueId()).adminSetHomeHomeSet.replace("[location]", player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()));
		    }
		}
		return true;
	    } else if (split[0].equalsIgnoreCase("tp")) {
		if (!(sender instanceof Player)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownCommand);
		    return true;
		}
		player = (Player)sender;
		// Convert name to a UUID
		final UUID targetUUID = plugin.getPlayers().getUUID(split[1]);
		if (!plugin.getPlayers().isAKnownPlayer(targetUUID)) {
		    player.sendMessage(ChatColor.RED + plugin.myLocale(player.getUniqueId()).errorUnknownPlayer);
		    return true;
		} else {
		    Location warpSpot = plugin.getPlayers().getHomeLocation(targetUUID).toVector().toLocation(ASkyGrid.getGridWorld());
		    String failureMessage = ChatColor.RED + plugin.myLocale(player.getUniqueId()).adminTpManualWarp.replace("[location]", warpSpot.getBlockX() + " " + warpSpot.getBlockY() + " "
			    + warpSpot.getBlockZ());
		    new SafeSpotTeleport(plugin, player, warpSpot, failureMessage);
		    return true;
		}
	    } else if (split[0].equalsIgnoreCase("delete")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[1]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		} else {
		    // This now deletes the player and cleans them up even if
		    // they don't have an island
		    sender.sendMessage(ChatColor.YELLOW + plugin.myLocale().deleteremoving.replace("[name]", split[1]));
		    // If they are online and in ASkyGrid then delete their
		    // stuff too
		    Player target = plugin.getServer().getPlayer(playerUUID);
		    if (target != null) {
			plugin.resetPlayer(target);
		    }
		    // plugin.getLogger().info("DEBUG: deleting player");
		    return true;
		}
	    } else if (split[0].equalsIgnoreCase("info")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[1]);
		// plugin.getLogger().info("DEBUG: console player info UUID = "
		// + playerUUID);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		} else {
		    showInfo(playerUUID, sender);
		    return true;
		}
	    } else if (split[0].equalsIgnoreCase("resetallchallenges")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[1]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		}
		plugin.getPlayers().resetAllChallenges(playerUUID);
		sender.sendMessage(ChatColor.YELLOW + plugin.myLocale().resetChallengessuccess.replace("[name]", split[1]));
		return true;
	    } else {
		return false;
	    }
	case 3:
	    if (split[0].equalsIgnoreCase("completechallenge")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[2]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		}
		if (plugin.getPlayers().checkChallenge(playerUUID, split[1].toLowerCase())
			|| !plugin.getPlayers().get(playerUUID).challengeExists(split[1].toLowerCase())) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().completeChallengeerrorChallengeDoesNotExist);
		    return true;
		}
		plugin.getPlayers().get(playerUUID).completeChallenge(split[1].toLowerCase());
		sender.sendMessage(ChatColor.YELLOW
			+ plugin.myLocale().completeChallengechallangeCompleted.replace("[challengename]", split[1].toLowerCase()).replace("[name]", split[2]));
		return true;
	    } else if (split[0].equalsIgnoreCase("resetchallenge")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[2]);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		}
		if (!plugin.getPlayers().checkChallenge(playerUUID, split[1].toLowerCase())
			|| !plugin.getPlayers().get(playerUUID).challengeExists(split[1].toLowerCase())) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().resetChallengeerrorChallengeDoesNotExist);
		    return true;
		}
		plugin.getPlayers().resetChallenge(playerUUID, split[1].toLowerCase());
		sender.sendMessage(ChatColor.YELLOW
			+ plugin.myLocale().resetChallengechallengeReset.replace("[challengename]", split[1].toLowerCase()).replace("[name]", split[2]));
		return true;
	    } else if (split[0].equalsIgnoreCase("info") && split[1].equalsIgnoreCase("challenges")) {
		// Convert name to a UUID
		final UUID playerUUID = plugin.getPlayers().getUUID(split[2]);
		// plugin.getLogger().info("DEBUG: console player info UUID = "
		// + playerUUID);
		if (!plugin.getPlayers().isAKnownPlayer(playerUUID)) {
		    sender.sendMessage(ChatColor.RED + plugin.myLocale().errorUnknownPlayer);
		    return true;
		} else {
		    showInfoChallenges(playerUUID, sender);
		    return true;
		}
	    }
	    return false;
	default:
	    return false;
	}
    }

    /**
     * Shows info on a player
     * 
     * @param playerUUID
     * @param sender
     */
    private void showInfo(UUID playerUUID, CommandSender sender) {
	sender.sendMessage(plugin.myLocale().adminInfoPlayer + ": " + ChatColor.GREEN + plugin.getPlayers().getName(playerUUID));
	sender.sendMessage(ChatColor.WHITE + "UUID: " + playerUUID.toString());
	// Last login
	try {
	    Date d = new Date(plugin.getServer().getOfflinePlayer(playerUUID).getLastPlayed());
	    sender.sendMessage(ChatColor.GOLD + plugin.myLocale().adminInfoLastLogin + ": " + d.toString());
	} catch (Exception e) {
	}
	Location homeLoc = null;
	homeLoc = plugin.getPlayers().getHomeLocation(playerUUID);
	if (homeLoc != null) {
	    sender.sendMessage(ChatColor.YELLOW + plugin.myLocale().adminInfoHomeLocation + ":" + ChatColor.WHITE + " (" + homeLoc.getBlockX() + ","
		    + homeLoc.getBlockY() + "," + homeLoc.getBlockZ() + ")");
	}
    }

    /**
     * Shows info on the challenge situation for player
     * 
     * @param playerUUID
     * @param sender
     */
    private void showInfoChallenges(UUID playerUUID, CommandSender sender) {
	sender.sendMessage("Name:" + ChatColor.GREEN + plugin.getPlayers().getName(playerUUID));
	sender.sendMessage(ChatColor.WHITE + "UUID: " + playerUUID.toString());
	// Completed challenges
	sender.sendMessage(ChatColor.WHITE + plugin.myLocale().challengesguiTitle + ":");
	HashMap<String, Boolean> challenges = plugin.getPlayers().getChallengeStatus(playerUUID);
	HashMap<String, Integer> challengeTimes = plugin.getPlayers().getChallengeTimes(playerUUID);
	for (String c : challenges.keySet()) {
	    if (challengeTimes.containsKey(c)) {
		sender.sendMessage(c + ": "
			+ ((challenges.get(c)) ? ChatColor.GREEN + plugin.myLocale().challengescomplete : ChatColor.AQUA + plugin.myLocale().challengesincomplete) + "("
			+ plugin.getPlayers().checkChallengeTimes(playerUUID, c) + ")");

	    } else {
		sender.sendMessage(c + ": "
			+ ((challenges.get(c)) ? ChatColor.GREEN + plugin.myLocale().challengescomplete : ChatColor.AQUA + plugin.myLocale().challengesincomplete));
	    }
	}
    }

    private boolean checkAdminPerms(Player player2, String[] split) {
	// Check perms quickly for this command
	if (player2.isOp()) {
	    return true;
	}
	String check = split[0];
	if (check.equalsIgnoreCase("confirm"))
	    check = "purge";
	if (VaultHelper.checkPerm(player2, Settings.PERMPREFIX + "admin." + split[0].toLowerCase())) {
	    return true;
	}
	return false;
    }

    private boolean checkModPerms(Player player2, String[] split) {
	// Check perms quickly for this command
	if (player2.isOp()) {
	    return true;
	}
	String check = split[0];
	// Check special cases
	if (check.contains("challenge".toLowerCase())) {
	    check = "challenges";
	}
	if (VaultHelper.checkPerm(player2, Settings.PERMPREFIX + "mod." + split[0].toLowerCase())) {
	    return true;
	}
	return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
	final List<String> options = new ArrayList<String>();
	String lastArg = (args.length != 0 ? args[args.length - 1] : "");

	if (!(sender instanceof Player)) {
	    //Server console or something else; doesn't have
	    //permission checking.

	    switch (args.length) {
	    case 0: 
	    case 1:
		options.addAll(Arrays.asList("reload", "completechallenge", "resetchallenge",
			"resetallchallenges"));
		break;
	    case 2:
		if (args[0].equalsIgnoreCase("delete")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if (args[0].equalsIgnoreCase("completechallenge")
			|| args[0].equalsIgnoreCase("resetchallenge")) {
		    options.addAll(plugin.getChallenges().getAllChallenges());
		}
		if (args[0].equalsIgnoreCase("resetallchallenges")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if (args[0].equalsIgnoreCase("info")) {
		    options.add("challenges");

		    options.addAll(Util.getOnlinePlayerList());
		}
		break;
	    case 3: 
		if (args[0].equalsIgnoreCase("completechallenge")
			|| args[0].equalsIgnoreCase("resetchallenge")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if (args[0].equalsIgnoreCase("info")
			&& args[1].equalsIgnoreCase("challenges")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		break;
	    }
	} else {
	    final Player player = (Player) sender;

	    switch (args.length) {
	    case 0: 
	    case 1: 
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "admin.reload") || player.isOp()) {
		    options.add("reload");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "admin.delete") || player.isOp()) {
		    options.add("delete");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp()) {
		    options.add("completechallenge");
		    options.add("resetchallenge");
		    options.add("resetallchallenges");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.info") || player.isOp()) {
		    options.add("info");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.clearreset") || player.isOp()) {
		    options.add("clearreset");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.tp") || player.isOp()) {
		    options.add("tp");
		}
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.signadmin") || player.isOp()) {
		    options.add("resetsign");
		}
		break;
	    case 2:
		if (VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.signadmin") || player.isOp()) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "admin.delete") || player.isOp())
			&& args[0].equalsIgnoreCase("delete")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp())
			&& (args[0].equalsIgnoreCase("completechallenge") || args[0].equalsIgnoreCase("resetchallenge"))) {
		    options.addAll(plugin.getChallenges().getAllChallenges());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp())
			&& args[0].equalsIgnoreCase("resetallchallenges")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.info") || player.isOp())
			&& args[0].equalsIgnoreCase("info")) {
		    options.add("challenges");

		    options.addAll(Util.getOnlinePlayerList());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.tp") || player.isOp())
			&& (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("tpnether"))) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		break;
	    case 3: 
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.challenges") || player.isOp())
			&& (args[0].equalsIgnoreCase("completechallenge") || args[0].equalsIgnoreCase("resetchallenge"))) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		if ((VaultHelper.checkPerm(player, Settings.PERMPREFIX + "mod.info") || player.isOp())
			&& args[0].equalsIgnoreCase("info")
			&& args[1].equalsIgnoreCase("challenges")) {
		    options.addAll(Util.getOnlinePlayerList());
		}
		break;
	    }
	}

	return Util.tabLimit(options, lastArg);
    }
}