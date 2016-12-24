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

package com.wasteofplastic.askygrid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wasteofplastic.askygrid.NotSetup.Reason;
import com.wasteofplastic.askygrid.commands.AdminCmd;
import com.wasteofplastic.askygrid.commands.Challenges;
import com.wasteofplastic.askygrid.commands.SkyGridCmd;
import com.wasteofplastic.askygrid.generators.SkyGridGen;
import com.wasteofplastic.askygrid.listeners.BlockEndDragon;
import com.wasteofplastic.askygrid.listeners.JoinLeaveEvents;
import com.wasteofplastic.askygrid.listeners.NetherPortals;
import com.wasteofplastic.askygrid.listeners.PlayerEvents;
import com.wasteofplastic.askygrid.panels.ChallengePanel;
import com.wasteofplastic.askygrid.panels.WarpPanel;
import com.wasteofplastic.askygrid.protection.GGuard;
import com.wasteofplastic.askygrid.util.VaultHelper;

/**
 * @author tastybento
 *         Main ASkyGrid class - provides an grid minigame in a sea of acid
 */
public class ASkyGrid extends JavaPlugin {
    // This plugin
    private static ASkyGrid plugin;
    // The ASkyGrid world
    private static World gridWorld = null;
    private static World netherWorld = null;
    private static World endWorld = null;
    // Player folder file
    private File playersFolder;
    // Challenges object
    private Challenges challenges;
    // Localization Strings
    private HashMap<String,Locale> availableLocales = new HashMap<String,Locale>();
    // Players object
    private PlayerCache players;
    // Listeners
    private WarpSigns warpSignsListener;
    // grid grid manager
    private GridManager grid;
    // grid command object
    private SkyGridCmd gridCmd;
    // Database
    private TinyDB tinyDB;
    // Warp panel
    private WarpPanel warpPanel;
    // V1.8 or later
    private boolean onePointEight;

    // Update object
    private Update updateCheck = null;

    // Messages object
    private Messages messages;

    // WorldGuard object
    private Plugin wgPlugin;

    // Grid Guard
    private GGuard gguard;

    /**
     * Returns the World object for the grid world named in config.yml.
     * If the world does not exist then it is created.
     * 
     * @return gridWorld - Bukkit World object for the ASkyGrid world
     */
    public static World getGridWorld() {
	if (gridWorld == null) {
	    // Bukkit.getLogger().info("DEBUG worldName = " +
	    // Settings.worldName);
	    gridWorld = WorldCreator.name(Settings.worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new SkyGridGen())
		    .createWorld();
	    // Make the nether if it does not exist
	    if (Settings.createNether) {
		getNetherWorld();
	    }
	    // Make the end if it does not exist
	    if (Settings.createEnd) {
		getEndWorld();
	    }
	    // Multiverse configuration
	    if (Bukkit.getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
		Bukkit.getLogger().info("Trying to register generator with Multiverse ");
		try {
		    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
			    "mv import " + Settings.worldName + " normal -g " + plugin.getName());
		    if (!Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
			    "mv modify set generator " + plugin.getName() + " " + Settings.worldName)) {
			Bukkit.getLogger().severe("Multiverse is out of date! - Upgrade to latest version!");
		    }
		    if (Settings.createNether) {		
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				"mv import " + Settings.worldName + "_nether nether -g " + plugin.getName());
			if (!Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				"mv modify set generator " + plugin.getName() + " " + Settings.worldName + "_nether")) {
			    Bukkit.getLogger().severe("Multiverse is out of date! - Upgrade to latest version!");
			}
		    }
		    if (Settings.createEnd) {		
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				"mv import " + Settings.worldName + "_the_end end -g " + plugin.getName());
			if (!Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				"mv modify set generator " + plugin.getName() + " " + Settings.worldName + "_the_end")) {
			    Bukkit.getLogger().severe("Multiverse is out of date! - Upgrade to latest version!");
			}
		    }
		} catch (Exception e) {
		    Bukkit.getLogger().severe("Not successfull! Disabling " + plugin.getName() + "!");
		    e.printStackTrace();
		    Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		}
	    }

	}
	// Set world settings
	gridWorld.setWaterAnimalSpawnLimit(Settings.waterAnimalSpawnLimit);
	gridWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	gridWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	if (Settings.createNether) {
	    netherWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	    netherWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	}
	if (Settings.createEnd) {
	    endWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	    endWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	}
	return gridWorld;
    }

    /**
     * @return ASkyGrid object instance
     */
    public static ASkyGrid getPlugin() {
	return plugin;
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
	try {
	    if (players != null) {
		players.removeAllPlayers();
	    }
	    // Save the warps and do not reload the panel
	    if (warpSignsListener != null) {
		warpSignsListener.saveWarpList(false);
	    }
	    if (messages != null) {
		messages.saveMessages();
	    }
	} catch (final Exception e) {
	    getLogger().severe("Something went wrong saving files!");
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	// instance of this plugin
	plugin = this;
	// Check server version - check for a class that only 1.8 has
	Class<?> clazz;
	try {
	    clazz = Class.forName("org.bukkit.event.player.PlayerInteractAtEntityEvent");
	} catch (Exception e) {
	    //getLogger().info("No PlayerInteractAtEntityEvent found.");
	    clazz = null;
	}
	if (clazz != null) {
	    onePointEight = true;
	}
	saveDefaultConfig();
	// Load all the configuration of the plugin and localization strings
	loadPluginConfig();
	if (Settings.useEconomy && !VaultHelper.setupEconomy()) {
	    getLogger().warning("Could not set up economy! - Running without an economy.");
	    Settings.useEconomy = false;
	}
	if (!VaultHelper.setupPermissions()) {
	    getLogger().severe("Cannot link with Vault for permissions! Disabling plugin!");
	    getServer().getPluginManager().disablePlugin(this);
	    return;
	}

	// This can no longer be run in onEnable because the plugin is loaded at
	// startup and so key variables are
	// not known to the server. Instead it is run one tick after startup.
	// getgridWorld();

	// Set and make the player's directory if it does not exist and then
	// load players into memory
	playersFolder = new File(getDataFolder() + File.separator + "players");
	if (!playersFolder.exists()) {
	    playersFolder.mkdir();
	}
	// Set up commands for this plugin
	gridCmd = new SkyGridCmd(this);

	AdminCmd adminCmd = new AdminCmd(this);
	getCommand("asg").setExecutor(gridCmd);
	getCommand("asg").setTabCompleter(gridCmd);
	getCommand("asgc").setExecutor(getChallenges());
	getCommand("asgc").setTabCompleter(getChallenges());
	getCommand("asgadmin").setExecutor(adminCmd);
	getCommand("asgadmin").setTabCompleter(adminCmd);
	// Register events that this plugin uses
	// registerEvents();
	// Load messages
	messages = new Messages(this);
	messages.loadMessages();
	// Register events
	registerEvents();
	// Metrics
	try {
	    final Metrics metrics = new Metrics(this);
	    metrics.start();
	} catch (final IOException localIOException) {
	}
	// Kick off a few tasks on the next tick
	// By calling getgridWorld(), if there is no grid
	// world, it will be created
	getServer().getScheduler().runTask(this, new Runnable() {
	    @Override
	    public void run() {
		// Create the world if it does not exist. This is run after the
		// server starts.
		getGridWorld();
		// Load warps
		getWarpSignsListener().loadWarpList();
		// Load the warp panel
		if (Settings.useWarpPanel) {
		    warpPanel = new WarpPanel(plugin);
		    getServer().getPluginManager().registerEvents(warpPanel, plugin);
		}
		getServer().getPluginManager().registerEvents(new ChallengePanel(plugin), plugin);
		// econ
		if (getServer().getWorld(Settings.worldName).getGenerator() == null) {
		    // Check if the world generator is registered correctly
		    getLogger().severe("********* The Generator for " + plugin.getName() + " is not registered so the plugin cannot start ********");
		    getLogger().severe("Make sure you have the following in bukkit.yml (case sensitive):");
		    getLogger().severe("worlds:");
		    getLogger().severe("  # The next line must be the name of your world:");
		    getLogger().severe("  " + Settings.worldName + ":");
		    getLogger().severe("    generator: " + plugin.getName());

		    getCommand("asg").setExecutor(new NotSetup(Reason.GENERATOR));
		    getCommand("asgc").setExecutor(new NotSetup(Reason.GENERATOR));
		    getCommand("asgadmin").setExecutor(new NotSetup(Reason.GENERATOR));

		    return;
		}
		getServer().getScheduler().runTask(plugin, new Runnable() {		    

		    @Override
		    public void run() {
			// load the list - order matters - grid first, then top
			// ten to optimize upgrades
			// Load grid
			if (grid == null) {
			    grid = new GridManager(plugin);
			}
			if (tinyDB == null) {
			    tinyDB = new TinyDB(plugin);
			}
			// Add any online players to the DB
			for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			    tinyDB.savePlayerName(onlinePlayer.getName(), onlinePlayer.getUniqueId());
			}
			startProtection();
			getLogger().info("All files loaded. Ready to play...");
		    }
		});
	    }
	});
    }

    /**
     * Start the WorldGuard protection if WG is enabled
     */
    public void startProtection() {
	// WorldGuard
	if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
	    if (getWorldGuard() != null) {
		gguard = new GGuard(plugin);
		if (Settings.claim_protectionRange > 0) {
		    getLogger().info("Warp sign protection powered by WorldGuard");				    
		} else {
		    getLogger().info("No protection for warp signs.");
		    // Remove all the protections
		    if (gguard.removeAllRegions()) {
			getLogger().info("Old warp protection regions removed!");
		    }
		}
	    }
	} else {
	    getLogger().info("No protection for warp signs."); 
	}
    }

    /**
     * @return the challenges
     */
    public Challenges getChallenges() {
	if (challenges == null) {
	    challenges = new Challenges(this);
	}
	return challenges;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {
	return new SkyGridGen();
    }

    /**
     * @return the grid
     */
    public GridManager getGrid() {
	if (grid == null) {
	    grid = new GridManager(this);
	}
	return grid;
    }

    /**
     * @return the players
     */
    public PlayerCache getPlayers() {
	if (players == null) {
	    players = new PlayerCache(this);
	}
	return players;
    }

    /**
     * @return the playersFolder
     */
    public File getPlayersFolder() {
	return playersFolder;
    }

    /**
     * @return the updateCheck
     */
    public Update getUpdateCheck() {
	return updateCheck;
    }

    /**
     * @param updateCheck the updateCheck to set
     */
    public void setUpdateCheck(Update updateCheck) {
	this.updateCheck = updateCheck;
    }

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public boolean loadPluginConfig() {
	// getLogger().info("*********************************************");
	try {
	    getConfig();
	} catch (final Exception e) {
	    e.printStackTrace();
	}
	//CompareConfigs.compareConfigs();
	// Get the localization strings
	//getLocale();
	// Add this to the config
	// Default is locale.yml
	availableLocales.put("locale", new Locale(this, "locale"));
	/*
	availableLocales.put("de-DE", new Locale(this,"de-DE"));
	availableLocales.put("en-US", new Locale(this,"en-US"));
	availableLocales.put("es-ES", new Locale(this,"es-ES"));
	availableLocales.put("fr-FR", new Locale(this,"fr-FR"));
	availableLocales.put("it-IT", new Locale(this,"it-IT"));
	availableLocales.put("ko-KR", new Locale(this,"ko-KR"));
	availableLocales.put("pl-PL", new Locale(this,"pl-PL"));
	availableLocales.put("pt-BR", new Locale(this,"pt-BR"));
	availableLocales.put("zh-CN", new Locale(this,"zh-CN"));
	availableLocales.put("cs-CS", new Locale(this,"cs-CS"));
	availableLocales.put("sk-SK", new Locale(this,"sk-SK"));
	availableLocales.put("zh-TW", new Locale(this,"zh-TW"));
	 */
	// Assign settings
	String configVersion = getConfig().getString("general.version", "");
	//getLogger().info("DEBUG: config ver length " + configVersion.split("\\.").length);
	// Ignore last digit if it is 4 digits long
	if (configVersion.split("\\.").length == 4) {
	    configVersion = configVersion.substring(0, configVersion.lastIndexOf('.')); 
	}
	// Save for plugin version
	String version = plugin.getDescription().getVersion();
	//getLogger().info("DEBUG: version length " + version.split("\\.").length);
	if (version.split("\\.").length == 4) {
	    version = version.substring(0, version.lastIndexOf('.')); 
	}
	if (configVersion.isEmpty() || !configVersion.equalsIgnoreCase(version)) {
	    // Check to see if this has already been shared
	    File newConfig = new File(plugin.getDataFolder(),"config.new.yml");
	    getLogger().warning("***********************************************************");
	    getLogger().warning("Config file is out of date. See config.new.yml for updates!");
	    getLogger().warning("config.yml version is '" + configVersion + "'");
	    getLogger().warning("Latest config version is '" + version + "'");
	    getLogger().warning("***********************************************************");
	    if (!newConfig.exists()) {
		File oldConfig = new File(plugin.getDataFolder(),"config.yml");
		File bakConfig = new File(plugin.getDataFolder(),"config.bak");
		if (oldConfig.renameTo(bakConfig)) {
		    plugin.saveResource("config.yml", false);
		    oldConfig.renameTo(newConfig);
		    bakConfig.renameTo(oldConfig);
		} 
	    }
	}
	// Debug
	Settings.debug = getConfig().getInt("general.debug", 0);
	// End portal probability
	Settings.endPortalProb = getConfig().getDouble("general.endframeprobability", 0.05);
	// Banned commands
	Settings.bannedCommandList = getConfig().getStringList("general.bannedcommands");
	// Mute death messages
	Settings.muteDeathMessages = getConfig().getBoolean("general.mutedeathmessages", false);
	// Warp panel
	Settings.useWarpPanel = getConfig().getBoolean("general.usewarppanel", true);
	// Restrict wither
	Settings.restrictWither = getConfig().getBoolean("general.restrictwither", true);
	// Immediate teleport
	Settings.immediateTeleport = getConfig().getBoolean("general.immediateteleport", false);
	// Use economy or not
	// In future expand to include internal economy
	Settings.useEconomy = getConfig().getBoolean("general.useeconomy", true);
	// Check for updates
	Settings.updateCheck = getConfig().getBoolean("general.checkupdates", true);
	Settings.startCommands = getConfig().getStringList("general.startcommands");
	Settings.useControlPanel = getConfig().getBoolean("general.usecontrolpanel", false);
	// Check if /asg command is allowed when falling
	Settings.allowTeleportWhenFalling = getConfig().getBoolean("general.allowfallingteleport", true);
	Settings.fallingCommandBlockList = getConfig().getStringList("general.blockingcommands");
	// Max home number
	Settings.maxHomes = getConfig().getInt("general.maxhomes",1);
	if (Settings.maxHomes < 1) {
	    Settings.maxHomes = 1;
	}
	// Settings from config.yml
	Settings.worldName = getConfig().getString("general.worldName");
	Settings.gridHeight = getConfig().getInt("general.gridheight", 128);
	Settings.spawnHeight = getConfig().getInt("general.spawnheight", 128);
	Settings.createNether = getConfig().getBoolean("general.createnether", true);
	Settings.createEnd = getConfig().getBoolean("general.createend", true);
	if (!Settings.createNether) {
	    getLogger().info("The Nether is disabled");
	}
	if (!Settings.createEnd) {
	    getLogger().info("The End is disabled");
	}
	Settings.spawnDistance = getConfig().getInt("general.distance", 1000);
	if (Settings.spawnDistance < 1) {
	    Settings.spawnDistance = 1;
	    getLogger().info("Setting minimum spawn distance to 1");
	}

	Settings.animalSpawnLimit = getConfig().getInt("general.animalspawnlimit", 15);
	if (Settings.animalSpawnLimit < -1) {
	    Settings.animalSpawnLimit = -1;
	}

	Settings.monsterSpawnLimit = getConfig().getInt("general.monsterspawnlimit", 70);
	if (Settings.monsterSpawnLimit < -1) {
	    Settings.monsterSpawnLimit = -1;
	}

	Settings.waterAnimalSpawnLimit = getConfig().getInt("general.wateranimalspawnlimit", 15);
	if (Settings.waterAnimalSpawnLimit < -1) {
	    Settings.waterAnimalSpawnLimit = -1;
	}

	Settings.claim_protectionRange = getConfig().getInt("general.protectionRange", 10);
	if (Settings.claim_protectionRange < 0) {
	    Settings.claim_protectionRange = 0;
	}
	Settings.resetMoney = getConfig().getBoolean("general.resetmoney", true);
	Settings.resetEnderChest = getConfig().getBoolean("general.resetenderchest", false);

	Settings.startingMoney = getConfig().getDouble("general.startingmoney", 0D);
	Settings.logInRemoveMobs = getConfig().getBoolean("general.loginremovemobs", true);
	Settings.removeMobs = getConfig().getBoolean("general.asgremovemobs", false);
	List<String> mobWhiteList = getConfig().getStringList("general.mobwhitelist");
	Settings.mobWhiteList.clear();
	String valid = "BLAZE, CREEPER, SKELETON, SPIDER, GIANT, ZOMBIE, GHAST, PIG_ZOMBIE, "
		+ "ENDERMAN, CAVE_SPIDER, SILVERFISH,  WITHER, WITCH, ENDERMITE,"
		+ " GUARDIAN";
	for (String mobName : mobWhiteList) {
	    if (valid.contains(mobName.toUpperCase())) {
		try {
		    Settings.mobWhiteList.add(EntityType.valueOf(mobName.toUpperCase()));
		} catch (Exception e) {
		    plugin.getLogger().severe("Error in config.yml, mobwhitelist value '" + mobName + "' is invalid.");
		    plugin.getLogger().severe("Possible values are : Blaze, Cave_Spider, Creeper, Enderman, Endermite, Giant, Guardian, "
			    + "Pig_Zombie, Silverfish, Skeleton, Spider, Witch, Wither, Zombie");
		}
	    } else {
		plugin.getLogger().severe("Error in config.yml, mobwhitelist value '" + mobName + "' is invalid.");
		plugin.getLogger().severe("Possible values are : Blaze, Cave_Spider, Creeper, Enderman, Endermite, Giant, Guardian, "
			+ "Pig_Zombie, Silverfish, Skeleton, Spider, Witch, Wither, Zombie");
	    }
	}
	// Assign settings
	// General settings that affect the world
	Settings.allowPvP = getConfig().getBoolean("general.allowPvP",true);
	Settings.allowEnderPearls = getConfig().getBoolean("general.allowenderpearls", true);
	Settings.allowFlowIn = getConfig().getBoolean("general.allowflowin", true);
	Settings.allowFlowOut = getConfig().getBoolean("general.allowflowout", true);
	Settings.allowTNTDamage = getConfig().getBoolean("general.allowTNTdamage", true);
	Settings.allowChestDamage = getConfig().getBoolean("general.allowchestdamage", true);
	// In-claim settings
	Settings.allowBreakBlocks = getConfig().getBoolean("general.allowbreakblocks", false);
	Settings.allowPlaceBlocks= getConfig().getBoolean("general.allowplaceblocks", false);
	Settings.allowBedUse= getConfig().getBoolean("general.allowbeduse", false);
	Settings.allowBucketUse = getConfig().getBoolean("general.allowbucketuse", false);
	Settings.allowShearing = getConfig().getBoolean("general.allowshearing", false);
	Settings.allowDoorUse = getConfig().getBoolean("general.allowdooruse", false);
	Settings.allowLeverButtonUse = getConfig().getBoolean("general.allowleverbuttonuse", false);
	Settings.allowCropTrample = getConfig().getBoolean("general.allowcroptrample", false);
	Settings.allowChestAccess = getConfig().getBoolean("general.allowchestaccess", false);
	Settings.allowFurnaceUse = getConfig().getBoolean("general.allowfurnaceuse", false);
	Settings.allowRedStone = getConfig().getBoolean("general.allowredstone", false);
	Settings.allowMusic = getConfig().getBoolean("general.allowmusic", false);
	Settings.allowCrafting = getConfig().getBoolean("general.allowcrafting", false);
	Settings.allowBrewing = getConfig().getBoolean("general.allowbrewing", false);
	Settings.allowGateUse = getConfig().getBoolean("general.allowgateuse", false);
	Settings.allowMobHarm = getConfig().getBoolean("general.allowmobharm", false);

	// Challenges
	getChallenges();
	// Challenge completion
	Settings.broadcastMessages = getConfig().getBoolean("general.broadcastmessages", true);
	Settings.removeCompleteOntimeChallenges = getConfig().getBoolean("general.removecompleteonetimechallenges", false);
	Settings.addCompletedGlow = getConfig().getBoolean("general.addcompletedglow", true);
	// All done
	return true;
    }


    /**
     * Registers events
     */
    public void registerEvents() {
	final PluginManager manager = getServer().getPluginManager();
	// Nether portal events
	manager.registerEvents(new NetherPortals(this), this);
	// Player events
	manager.registerEvents(new PlayerEvents(this), this);
	// Events for when a player joins or leaves the server
	manager.registerEvents(new JoinLeaveEvents(this), this);
	// Enables warp signs in ASkyGrid
	warpSignsListener = new WarpSigns(this);
	manager.registerEvents(warpSignsListener, this);
	// EnderDragon removal
	if (Settings.createEnd) {
	    manager.registerEvents(new BlockEndDragon(this), this);
	}
    }



    /**
     * Resets a player's inventory, armor slots, equipment, enderchest and
     * potion effects
     * 
     * @param player
     */
    public void resetPlayer(Player player) {
	// getLogger().info("DEBUG: clear inventory = " +
	// Settings.clearInventory);
	if (Settings.clearInventory
		&& (player.getWorld().getName().equalsIgnoreCase(Settings.worldName) || player.getWorld().getName()
			.equalsIgnoreCase(Settings.worldName + "_nether"))) {
	    // Clear their inventory and equipment and set them as survival
	    player.getInventory().clear(); // Javadocs are wrong - this does not
	    // clear armor slots! So...
	    player.getInventory().setArmorContents(null);
	    player.getInventory().setHelmet(null);
	    player.getInventory().setChestplate(null);
	    player.getInventory().setLeggings(null);
	    player.getInventory().setBoots(null);
	    player.getEquipment().clear();
	}
	player.setGameMode(GameMode.SURVIVAL);
	// Reset the player's challenge status
	players.resetAllChallenges(player.getUniqueId());
	// Save the player
	players.save(player.getUniqueId());
	// Update the inventory
	player.updateInventory();
	if (Settings.resetEnderChest) {
	    // Clear any Enderchest contents
	    final ItemStack[] items = new ItemStack[player.getEnderChest().getContents().length];
	    player.getEnderChest().setContents(items);
	}
	// Clear any potion effects
	for (PotionEffect effect : player.getActivePotionEffects())
	    player.removePotionEffect(effect.getType());
    }

    public void restartEvents() {
	final PluginManager manager = getServer().getPluginManager();
	// Enables warp signs in ASkyGrid
	warpSignsListener = new WarpSigns(this);
	manager.registerEvents(warpSignsListener, this);
    }

    public void unregisterEvents() {
	HandlerList.unregisterAll(warpSignsListener);
    }

    /**
     * @return the netherWorld
     */
    public static World getNetherWorld() {
	if (netherWorld == null && Settings.createNether) {
	    if (plugin.getServer().getWorld(Settings.worldName + "_nether") == null) {
		Bukkit.getLogger().info("Creating " + plugin.getName() + "'s Nether...");
	    }
	    netherWorld = WorldCreator.name(Settings.worldName + "_nether").type(WorldType.NORMAL).environment(World.Environment.NETHER).generator(new SkyGridGen()).createWorld();
	    netherWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	    netherWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	}
	return netherWorld;
    }

    /**
     * @return the endWorld
     */
    public static World getEndWorld() {
	if (endWorld == null && Settings.createEnd) {
	    if (plugin.getServer().getWorld(Settings.worldName + "_the_end") == null) {
		Bukkit.getLogger().info("Creating " + plugin.getName() + "'s End...");
	    }
	    endWorld = WorldCreator.name(Settings.worldName + "_the_end").type(WorldType.NORMAL).environment(World.Environment.THE_END).generator(new SkyGridGen()).createWorld();
	    endWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	    endWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	}
	return endWorld;
    }

    /**
     * @return Locale for this player
     */
    public Locale myLocale(UUID player) {
	String locale = players.getLocale(player);
	if (locale.isEmpty() || !availableLocales.containsKey(locale)) {
	    return availableLocales.get("locale");
	}
	return availableLocales.get(locale);
    }

    /**
     * @return System locale
     */
    public Locale myLocale() {
	return availableLocales.get("locale");
    }

    /**
     * @return the messages
     */
    public Messages getMessages() {
	return messages;
    }

    /**
     * @return the gridCmd
     */
    public SkyGridCmd getGridCmd() {
	return gridCmd;
    }

    /**
     * @return the nameDB
     */
    public TinyDB getTinyDB() {
	if (tinyDB == null) {
	    tinyDB = new TinyDB(this);
	}
	return tinyDB;
    }

    /**
     * @return the warpSignsListener
     */
    public WarpSigns getWarpSignsListener() {
	return warpSignsListener;
    }

    /**
     * @return the warpPanel
     */
    public WarpPanel getWarpPanel() {
	if (warpPanel == null) {
	    // Probably due to a reload
	    warpPanel = new WarpPanel(this);
	    getServer().getPluginManager().registerEvents(warpPanel, plugin);
	}
	return warpPanel;
    }

    /**
     * @return the onePointEight
     */
    public boolean isOnePointEight() {
	return onePointEight;
    }

    public WorldGuardPlugin getWorldGuard() {
	wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");

	// WorldGuard may not be loaded
	if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
	    getLogger().severe("Warp sign protection is > 0 but could not load WorldGuard!");
	    return null;
	}
	return (WorldGuardPlugin) wgPlugin;
    }

    /**
     * @return the gguard
     */
    public GGuard getGguard() {
	return gguard;
    } 
}
