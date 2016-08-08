/*******************************************************************************
 * This file is part of A SkyGrid.
 *
 *     A SkyGrid is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     A SkyGrid is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with A SkyGrid.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.wasteofplastic.askygrid;

import java.io.File;
import java.io.InputStream;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


/**
 * All the text strings in the game sent to players
 * This version enables different players to have different locales.
 * 
 * @author tastybento
 */
public class Locale {
    // Localization Strings
    private FileConfiguration locale = null;
    private File localeFile = null;
    private ASkyGrid plugin;

    // Errors
    public String errorUnknownPlayer;
    public String errorNoPermission;
    public String errorCommandNotReady;
    public String errorOfflinePlayer;
    public String errorUnknownCommand;

    // IslandGuard
    public String claimProtected;

    // WarpSigns
    public String warpswelcomeLine;
    public String warpswarpTip;
    public String warpssuccess;
    public String warpsremoved;
    public String warpssignRemoved;
    public String warpsdeactivate;
    public String warpserrorNoRemove;
    public String warpserrorNoPerm;
    public String warpserrorNoPlace;
    public String warpserrorDuplicate;
    public String warpserrorDoesNotExist;
    public String warpserrorNotReadyYet;
    public String warpserrorNotSafe;
    // island warp help
    public String warpswarpToPlayersSign;
    public String warpserrorNoWarpsYet;
    public String warpswarpsAvailable;
    public String warpsPlayerWarped;

    // sethome
    public String setHomehomeSet;

    // Challenges
    public String challengesyouHaveCompleted;
    public String challengesnameHasCompleted;
    public String challengesyouRepeated;
    public String challengestoComplete;
    public String challengeshelp1;
    public String challengeshelp2;
    public String challengescolors;
    public String challengescomplete;
    public String challengesincomplete;
    public String challengescompleteNotRepeatable;
    public String challengescompleteRepeatable;
    public String challengesname;
    public String challengeslevel;
    public String challengesitemTakeWarning;
    public String challengesnotRepeatable;
    public String challengesfirstTimeRewards;
    public String challengesrepeatRewards;
    public String challengesexpReward;
    public String challengesmoneyReward;
    public String challengestoCompleteUse;
    public String challengesinvalidChallengeName;
    public String challengesrewards;
    public String challengesyouHaveNotUnlocked;
    public String challengesunknownChallenge;
    public String challengeserrorNotEnoughItems;
    public String challengeserrorNotCloseEnough;
    public String challengeserrorItemsNotThere;
    public String challengeserrorYouAreMissing;
    public String homeTeleport;
    public String newPlayer;
    public String errorYouDoNotHavePermission;

    public String help;
   // /island sethome
    public String helpSetHome;
    // /island warps;
    public String gridhelpWarps;
    // /island warp <player>
    public String gridhelpWarp;
    public String griderrorInvalidPlayer;
    // Teleport go
    public String gridhelpTeleport;
    // Help
    public String adminHelpHelp;
    public String adminHelpreload;
    public String adminHelpdelete;
    public String adminHelpcompleteChallenge;
    public String adminHelpresetChallenge;
    public String adminHelpresetAllChallenges;
    public String adminHelpinfo;
    public String adminHelptp;
    public String reloadconfigReloaded;
    public String deleteremoving;

    // info
    public String adminInfoHomeLocation;

    // resetallchallenges
    public String resetChallengessuccess;

    // completechallenge
    public String completeChallengeerrorChallengeDoesNotExist;
    public String completeChallengechallangeCompleted;

    // resetchallenge
    public String resetChallengeerrorChallengeDoesNotExist;
    public String resetChallengechallengeReset;

    // A SkyGrid news
    public String newsHeadline;


    public String errorWrongWorld;
    public String gridcannotTeleport;
    public String prefix;
    // Titles
    public String gridSubTitle;
    public String gridTitle;
    public String gridDonate;
    public String gridURL;
    public String challengeserrorRewardProblem;
    public String challengesNavigation;
    public String gridHelpChallenges;
    public String challengesmaxreached;
    public String challengescompletedtimes;
    public String targetInNoPVPArea;
    public String setHomeerrorNumHomes;
    public String schematicsTitle;
    public String warpsPrevious;
    public String warpsNext;
    public String warpsTitle;
    public String adminDeleteIslandError;
    public String errorUseInGame;
    public String adminInfotitle;
    public String adminInfounowned;
    public String adminHelpSetHome;
    public String adminSetHomeNoneFound;
    public String adminSetHomeHomeSet;
    public String adminTpManualWarp;
    public String adminInfoPlayer;
    public String adminInfoLastLogin;

    public String challengesguiTitle;
    public String helpColor;
    public String adminHelpinfoPlayer;
    public String gridSubTitleColor = "blue";
    public String gridTitleColor = "red";
    public String gridDonateColor = "gold";
    public String warpsentry;
    public String warpsexit;
    public String warpsProtectionLost;
    public String warpsProtectionEnabled;

    /**
     * Creates a locale object full of localized strings for a language
     * @param plugin
     * @param localeName - name of the yml file that will be used
     */
    public Locale(ASkyGrid plugin, String localeName) {
	this.plugin = plugin;
	getLocale(localeName);
	loadLocale();
    }

    /**
     * @return locale FileConfiguration object
     */
    public FileConfiguration getLocale(String localeName) {
	if (this.locale == null) {
	    reloadLocale(localeName);
	}
	return locale;
    }

    /**
     * Reloads the locale file
     */
    public void reloadLocale(String localeName) {
	//plugin.getLogger().info("DEBUG: loading local file " + localeName + ".yml");
	// Make directory if it doesn't exist
	File localeDir = new File(plugin.getDataFolder() + File.separator + "locale");
	if (!localeDir.exists()) {
	    localeDir.mkdir();
	}
	if (localeFile == null) {
	    localeFile = new File(localeDir.getPath(), localeName + ".yml");
	}
	if (localeFile.exists()) {
	    //plugin.getLogger().info("DEBUG: File exists!");
	    locale = YamlConfiguration.loadConfiguration(localeFile);
	} else {
	    // Look for defaults in the jar
	    InputStream defLocaleStream = plugin.getResource("locale/" + localeName + ".yml");
	    if (defLocaleStream != null) {
		//plugin.getLogger().info("DEBUG: Saving from jar");
		plugin.saveResource("locale/" + localeName + ".yml", true);
		localeFile = new File(plugin.getDataFolder() + File.separator + "locale", localeName + ".yml");
		locale = YamlConfiguration.loadConfiguration(localeFile);
		//locale.setDefaults(defLocale);
	    } else {
		// Use the default file
		localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
		if (localeFile.exists()) {
		    locale = YamlConfiguration.loadConfiguration(localeFile);
		} else {
		    // Look for defaults in the jar
		    defLocaleStream = plugin.getResource("locale/locale.yml");
		    if (defLocaleStream != null) {
			plugin.saveResource("locale/locale.yml", true);
			localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
			locale = YamlConfiguration.loadConfiguration(localeFile);
		    } else {
			plugin.getLogger().severe("Could not find any locale file!");
		    }
		}
	    }
	}
    }

    public void loadLocale() {
	// Localization Locale Setting
	// Command prefix - can be added to the beginning of any message
	prefix = ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', locale.getString("prefix", "")));
	newsHeadline = ChatColor.translateAlternateColorCodes('&', locale.getString("news.headline", "[ASkyGrid News] While you were offline..."));
	errorUnknownPlayer = ChatColor.translateAlternateColorCodes('&', locale.getString("error.unknownPlayer", "That player is unknown."));
	errorNoPermission = ChatColor.translateAlternateColorCodes('&',
		locale.getString("error.noPermission", "You don't have permission to use that command!"));
	errorCommandNotReady = ChatColor.translateAlternateColorCodes('&',
		locale.getString("error.commandNotReady", "You can't use that command right now."));
	errorOfflinePlayer = ChatColor.translateAlternateColorCodes('&',
		locale.getString("error.offlinePlayer", "That player is offline or doesn't exist."));
	errorUnknownCommand = ChatColor.translateAlternateColorCodes('&', locale.getString("error.unknownCommand", "Unknown command."));
	errorWrongWorld = ChatColor.translateAlternateColorCodes('&', locale.getString("error.wrongWorld", "You cannot do that in this world."));
	claimProtected = ChatColor.translateAlternateColorCodes('&', locale.getString("claimProtected", "Claim protected."));
	targetInNoPVPArea = ChatColor.translateAlternateColorCodes('&', locale.getString("targetInPVPArea", "Target is in a no-PVP area!"));
	warpswelcomeLine = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.welcomeLine", "[WELCOME]"));
	warpswarpTip = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.warpTip", "Create a warp by placing a sign with [WELCOME] at the top."));
	warpssuccess = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.success", "Welcome sign placed successfully!"));
	warpsremoved = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.removed", "Welcome sign removed!"));
	warpssignRemoved = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.signRemoved", "Your welcome sign was removed!"));
	warpsdeactivate = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.deactivate", "Deactivating old sign!"));
	warpsentry = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.entry", "Now entering [player]'s protected area"));
	warpsexit = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.exit", "Now leaving [player]'s protected area"));
	warpserrorNoRemove = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNoRemove", "You can only remove your own Welcome Sign!"));
	warpserrorNoPerm = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNoPerm", "You do not have permission to place Welcome Signs yet!"));
	warpserrorNoPlace = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNoPlace", "You cannot place a sign here because it will overlap another player's claim."));
	warpserrorDuplicate = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorDuplicate", "Sorry! There is a sign already in that location!"));
	warpserrorDoesNotExist = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.errorDoesNotExist", "That warp doesn't exist!"));
	warpserrorNotReadyYet = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNotReadyYet", "That warp is not ready yet. Try again later."));
	warpserrorNotSafe = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNotSafe", "That warp is not safe right now. Try again later."));
	warpswarpToPlayersSign = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.warpToPlayersSign", "Warping to <player>'s welcome sign."));
	warpserrorNoWarpsYet = ChatColor.translateAlternateColorCodes('&',
		locale.getString("warps.errorNoWarpsYet", "There are no warps available yet!"));
	warpswarpsAvailable = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.warpsAvailable", "The following warps are available"));
	warpsPlayerWarped = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.playerWarped", "[name] &2warped to your sign!"));
	warpsPrevious = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.previous", "Previous"));
	warpsNext = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.next", "Next"));
	warpsProtectionEnabled = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.protectionenabled", "Protection enabled: [number] x [number] around sign"));
	warpsProtectionLost = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.protectionlost", "Protection lost."));
	warpsTitle = ChatColor.translateAlternateColorCodes('&', locale.getString("warps.title", "ASkyGrid warps"));
	setHomehomeSet = ChatColor.translateAlternateColorCodes('&',
		locale.getString("sethome.homeSet", "Your home has been set to your current location."));
	setHomeerrorNumHomes = ChatColor.translateAlternateColorCodes('&',
		locale.getString("sethome.errorNumHomes", "Homes can be 1 to [max]"));
	challengesyouHaveCompleted = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.youHaveCompleted", "You have completed the [challenge] challenge!"));
	challengesnameHasCompleted = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.nameHasCompleted", "[name] has completed the [challenge] challenge!"));
	challengesyouRepeated = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.youRepeated", "You repeated the [challenge] challenge!"));
	challengestoComplete = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.toComplete", "Complete [challengesToDo] more [thisLevel] challenges to unlock this level!"));
	challengescomplete = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.complete", "Complete"));
	challengesincomplete = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.incomplete", "Incomplete"));
	challengescompleteNotRepeatable = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.completeNotRepeatable", "Completed(not repeatable)"));
	challengescompleteRepeatable = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.completeRepeatable", "Completed(repeatable)"));
	challengesname = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.name", "Challenge Name"));
	challengeslevel = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.level", "Level"));
	challengesitemTakeWarning = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.itemTakeWarning", "All required items are taken when you complete this challenge!"));
	challengesnotRepeatable = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.notRepeatable", "This Challenge is not repeatable!"));
	challengesfirstTimeRewards = ChatColor
		.translateAlternateColorCodes('&', locale.getString("challenges.firstTimeRewards", "First time reward(s)"));
	challengesrepeatRewards = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.repeatRewards", "Repeat reward(s)"));
	challengesexpReward = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.expReward", "Exp reward"));
	challengesmoneyReward = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.moneyReward", "Money reward"));
	challengestoCompleteUse = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.toCompleteUse", "To complete this challenge, use"));
	challengesinvalidChallengeName = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.invalidChallengeName", "Invalid challenge name! Use /c help for more information"));
	challengesrewards = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.rewards", "Reward(s)"));
	challengesyouHaveNotUnlocked = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.youHaveNotUnlocked", "You have not unlocked this challenge yet!"));
	challengesunknownChallenge = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.unknownChallenge", "Unknown challenge name (check spelling)!"));
	challengeserrorNotEnoughItems = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.errorNotEnoughItems", "You do not have enough of the required item(s)"));
	challengeserrorNotCloseEnough = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.errorNotCloseEnough", "You must be standing within 10 blocks of all required items."));
	challengeserrorItemsNotThere = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.errorItemsNotThere", "All required items must be within 10 blocks of you!"));
	challengeserrorRewardProblem = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.errorRewardProblem", "There was a problem giving your reward. Ask Admin to check log!"));
	challengesguiTitle = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.guititle", "Challenges"));
	challengeserrorYouAreMissing = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.erroryouaremissing", "You are missing"));
	challengesNavigation = ChatColor
		.translateAlternateColorCodes('&', locale.getString("challenges.navigation", "Click to see [level] challenges!"));
	challengescompletedtimes = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.completedtimes", "Completed [donetimes] out of [maxtimes]"));
	challengesmaxreached = ChatColor.translateAlternateColorCodes('&',
		locale.getString("challenges.maxreached", "Max reached [donetimes] out of [maxtimes]"));
	homeTeleport = ChatColor.translateAlternateColorCodes('&',
		locale.getString("grid.teleport", "Teleporting home."));
	gridcannotTeleport = ChatColor.translateAlternateColorCodes('&',
		locale.getString("grid.cannotTeleport", "You cannot teleport when falling!"));
	newPlayer = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.new", "Your adventure begins!"));
	gridSubTitle = locale.getString("grid.subtitle", "by tastybento");
	gridDonate = locale.getString("grid.donate", "ASkyGrid by tastybento");
	gridTitle = locale.getString("grid.title", "ASkyGrid");
	gridURL = locale.getString("grid.url", "");
	errorYouDoNotHavePermission = ChatColor.translateAlternateColorCodes('&',
		locale.getString("grid.errorYouDoNotHavePermission", "You do not have permission to use that command!"));
	help = ChatColor.translateAlternateColorCodes('&',
		locale.getString("grid.helpStart", "start, or teleport home."));
	gridhelpTeleport = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpTeleport", "teleport home."));
	helpSetHome = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpSetHome", "set home."));
	gridhelpWarps = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpWarps", "Lists all available welcome-sign warps."));
	gridhelpWarp = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpWarp", "Warp to <player>'s welcome sign."));
	gridHelpChallenges = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpChallenges", "/asgc: &fshow challenges"));
	adminHelpHelp = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.help", "Admin Commands:"));
	griderrorInvalidPlayer = ChatColor.translateAlternateColorCodes('&',
		locale.getString("grid.errorInvalidPlayer", "That player is invalid!"));
	adminHelpreload = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.reload", "reload configuration from file."));
	adminHelpdelete = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.delete", "delete a player"));
	adminHelpcompleteChallenge = ChatColor.translateAlternateColorCodes('&',
		locale.getString("adminHelp.completeChallenge", "marks a challenge as complete"));
	adminHelpresetChallenge = ChatColor.translateAlternateColorCodes('&',
		locale.getString("adminHelp.resetChallenge", "marks a challenge as incomplete"));
	adminHelpresetAllChallenges = ChatColor.translateAlternateColorCodes('&',
		locale.getString("adminHelp.resetAllChallenges", "resets all of the player's challenges"));
	adminHelpinfo = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.info", "check information on the given player"));
	adminHelptp = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.tp", "Teleport to a player's home."));
	reloadconfigReloaded = ChatColor.translateAlternateColorCodes('&',
		locale.getString("reload.configReloaded", "Configuration reloaded from file."));
	adminInfoHomeLocation = ChatColor.translateAlternateColorCodes('&', locale.getString("adminInfo.homeLocation", "Home Location"));
	resetChallengessuccess = ChatColor.translateAlternateColorCodes('&',
		locale.getString("resetallchallenges.success", "[name] has had all challenges reset."));
	completeChallengeerrorChallengeDoesNotExist = ChatColor.translateAlternateColorCodes('&',
		locale.getString("completechallenge.errorChallengeDoesNotExist", "Challenge doesn't exist or is already completed"));
	completeChallengechallangeCompleted = ChatColor.translateAlternateColorCodes('&',
		locale.getString("completechallenge.challangeCompleted", "[challengename] has been completed for [name]"));
	resetChallengeerrorChallengeDoesNotExist = ChatColor.translateAlternateColorCodes('&',
		locale.getString("resetchallenge.errorChallengeDoesNotExist", "[challengename] has been reset for [name]"));
	deleteremoving = ChatColor.translateAlternateColorCodes('&', locale.getString("delete.removing", "Removing [name]."));
	resetChallengessuccess = ChatColor.translateAlternateColorCodes('&',
		locale.getString("resetallchallenges.success", "[name] has had all challenges reset."));
	completeChallengeerrorChallengeDoesNotExist = ChatColor.translateAlternateColorCodes('&',
		locale.getString("completechallenge.errorChallengeDoesNotExist", "Challenge doesn't exist or is already completed"));
	completeChallengechallangeCompleted = ChatColor.translateAlternateColorCodes('&',
		locale.getString("completechallenge.challangeCompleted", "[challengename] has been completed for [name]"));
	resetChallengeerrorChallengeDoesNotExist = ChatColor.translateAlternateColorCodes('&',
		locale.getString("resetchallenge.errorChallengeDoesNotExist", "Challenge doesn't exist or isn't yet completed"));
	resetChallengechallengeReset = ChatColor.translateAlternateColorCodes('&',
		locale.getString("resetchallenge.challengeReset", "[challengename] has been reset for [name]"));
	helpColor = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpColor", "&e"));
	errorUseInGame = ChatColor.translateAlternateColorCodes('&', locale.getString("error.useInGame", "This command must be used in-game."));
	adminHelpSetHome = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.sethome", "Set player's home to your position"));
	adminSetHomeNoneFound = ChatColor.translateAlternateColorCodes('&', locale.getString("adminSetHome.noneFound", "No safe location found!"));
	adminSetHomeHomeSet = ChatColor.translateAlternateColorCodes('&', locale.getString("adminSetHome.homeSet", "Home set to [location]"));
	adminTpManualWarp = ChatColor.translateAlternateColorCodes('&', locale.getString("adminTp.manualWarp", "No safe spot found. Manually warp to somewhere near [location]."));
	adminInfoPlayer = ChatColor.translateAlternateColorCodes('&', locale.getString("adminInfo.player","Player"));
	adminInfoLastLogin = ChatColor.translateAlternateColorCodes('&', locale.getString("adminInfo.lastLogin","Last Login"));
	challengesguiTitle = ChatColor.translateAlternateColorCodes('&', locale.getString("challenges.guititle","ASkyGrid Challenges"));
	helpColor = ChatColor.translateAlternateColorCodes('&', locale.getString("grid.helpcolor","&e"));
	adminHelpinfoPlayer = ChatColor.translateAlternateColorCodes('&', locale.getString("adminHelp.info","info for the given player"));
    }
}
