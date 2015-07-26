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

package com.wasteofplastic.askygrid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.wasteofplastic.askygrid.NotSetup.Reason;
import com.wasteofplastic.askygrid.commands.AdminCmd;
import com.wasteofplastic.askygrid.commands.Challenges;
import com.wasteofplastic.askygrid.commands.SkyGridCmd;
import com.wasteofplastic.askygrid.generators.SkyGridGenerator;
import com.wasteofplastic.askygrid.listeners.JoinLeaveEvents;
import com.wasteofplastic.askygrid.listeners.NetherPortals;
import com.wasteofplastic.askygrid.listeners.PlayerEvents;
import com.wasteofplastic.askygrid.panels.ControlPanel;
import com.wasteofplastic.askygrid.panels.WarpPanel;
import com.wasteofplastic.askygrid.util.VaultHelper;

/**
 * @author tastybento
 *         Main ASkyBlock class - provides an island minigame in a sea of acid
 */
public class ASkyGrid extends JavaPlugin {
    // This plugin
    private static ASkyGrid plugin;
    // The ASkyBlock world
    private static World islandWorld = null;
    private static World netherWorld = null;
    // Flag indicating if a new islands is in the process of being generated or
    // not
    private boolean newIsland = false;
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
    // Island grid manager
    private GridManager grid;
    // Island command object
    private SkyGridCmd islandCmd;
    // Database
    private TinyDB tinyDB;
    // Warp panel
    private WarpPanel warpPanel;
    // V1.8 or later
    private boolean onePointEight;

    private boolean debug = false;

    // Level calc
    private boolean calculatingLevel = false;

    // Update object
    private Update updateCheck = null;

    // Messages object
    private Messages messages;

    /**
     * Returns the World object for the island world named in config.yml.
     * If the world does not exist then it is created.
     * 
     * @return islandWorld - Bukkit World object for the ASkyBlock world
     */
    public static World getIslandWorld() {
	if (islandWorld == null) {
	    // Bukkit.getLogger().info("DEBUG worldName = " +
	    // Settings.worldName);
	    islandWorld = WorldCreator.name(Settings.worldName).type(WorldType.FLAT).environment(World.Environment.NORMAL).generator(new SkyGridGenerator())
		    .createWorld();
	    // Make the nether if it does not exist
	    if (Settings.createNether) {
		getNetherWorld();
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
			}}
		} catch (Exception e) {
		    Bukkit.getLogger().severe("Not successfull! Disabling " + plugin.getName() + "!");
		    e.printStackTrace();
		    Bukkit.getServer().getPluginManager().disablePlugin(plugin);
		}
	    }

	}
	// Set world settings
	islandWorld.setWaterAnimalSpawnLimit(Settings.waterAnimalSpawnLimit);
	islandWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	islandWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);

	return islandWorld;
    }

    /**
     * @return ASkyBlock object instance
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
	    // Close the name database
	    if (tinyDB != null) {
		tinyDB.closeDB();
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
	// getIslandWorld();

	// Set and make the player's directory if it does not exist and then
	// load players into memory
	playersFolder = new File(getDataFolder() + File.separator + "players");
	if (!playersFolder.exists()) {
	    playersFolder.mkdir();
	}
	// Set up commands for this plugin
	islandCmd = new SkyGridCmd(this);

	AdminCmd adminCmd = new AdminCmd(this);
	getCommand("asg").setExecutor(islandCmd);
	getCommand("asg").setTabCompleter(islandCmd);
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
	// By calling getIslandWorld(), if there is no island
	// world, it will be created
	getServer().getScheduler().runTask(this, new Runnable() {
	    @Override
	    public void run() {
		// Create the world if it does not exist. This is run after the
		// server starts.
		getIslandWorld();
		// Load warps
		getWarpSignsListener().loadWarpList();
		// Load the warp panel
		if (Settings.useWarpPanel) {
		    warpPanel = new WarpPanel(plugin);
		    getServer().getPluginManager().registerEvents(warpPanel, plugin);
		}
		// Minishop - must wait for economy to load before we can use
		// econ
		getServer().getPluginManager().registerEvents(new ControlPanel(plugin), plugin);
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
			getLogger().info("All files loaded. Ready to play...");
		    }
		});
		// Check for updates asynchronously
		if (Settings.updateCheck) {
		    checkUpdates();
		    new BukkitRunnable() {
			int count = 0;
			@Override
			public void run() {
			    if (count++ > 10) {
				plugin.getLogger().info("No updates found. (No response from server after 10s)");
				this.cancel();
			    } else {
				// Wait for the response
				if (updateCheck != null) {
				    if (updateCheck.isSuccess()) {
					checkUpdatesNotify(null);
				    } else {
					plugin.getLogger().info("No update.");
				    }
				    this.cancel();
				}
			    }
			}
		    }.runTaskTimer(plugin, 0L, 20L); // Check status every second
		}
	    }
	});
    }

    /**
     * Checks to see if there are any plugin updates
     * Called when reloading settings too
     */
    public void checkUpdates() {
	// Version checker
	getLogger().info("Checking for new updates...");
	getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
	    @Override
	    public void run() {
		//updateCheck = new Update(80095); // AcidIsland
		//if (!updateCheck.isSuccess()) {
		//    updateCheck = null;
		//}
	    }
	});
    }

    public void checkUpdatesNotify(Player p) {
	boolean update = false;
	final String pluginVersion = plugin.getDescription().getVersion();
	// Check to see if the latest file is newer that this one
	String[] split = plugin.getUpdateCheck().getVersionName().split(" V");
	// Only do this if the format is what we expect
	if (split.length == 2) {
	    //getLogger().info("DEBUG: " + split[1]);
	    // Need to escape the period in the regex expression
	    String[] updateVer = split[1].split("\\.");
	    //getLogger().info("DEBUG: split length = " + updateVer.length);
	    // CHeck the version #'s
	    String[] pluginVer = pluginVersion.split("\\.");
	    //getLogger().info("DEBUG: split length = " + pluginVer.length);
	    // Run through major, minor, sub
	    for (int i = 0; i < Math.max(updateVer.length, pluginVer.length); i++) {
		try {
		    int updateCheck = 0;
		    if (i < updateVer.length) {
			updateCheck = Integer.valueOf(updateVer[i]);
		    }
		    int pluginCheck = 0;
		    if (i < pluginVer.length) {
			pluginCheck = Integer.valueOf(pluginVer[i]);
		    }
		    //getLogger().info("DEBUG: update is " + updateCheck + " plugin is " + pluginCheck);
		    if (updateCheck < pluginCheck) {
			//getLogger().info("DEBUG: plugin is newer!");
			//plugin is newer
			update = false;
			break;
		    } else if (updateCheck > pluginCheck) {
			//getLogger().info("DEBUG: update is newer!");
			update = true;
			break;
		    }
		} catch (Exception e) {
		    getLogger().warning("Could not determine update's version # ");
		    getLogger().warning("Plugin version: "+ pluginVersion);
		    getLogger().warning("Update version: " + plugin.getUpdateCheck().getVersionName());
		    return;
		}
	    }
	}
	// Show the results
	if (p != null) {
	    if (!update) {
		return;
	    } else {
		// Player login
		p.sendMessage(ChatColor.GOLD + plugin.getUpdateCheck().getVersionName() + " is available! You are running " + pluginVersion);

		p.sendMessage(ChatColor.RED + "Update at: http://dev.bukkit.org/bukkit-plugins/askygrid");

	    }
	} else {
	    // Console
	    if (!update) {
		getLogger().info("No updates available.");
		return;
	    } else {
		getLogger().info(plugin.getUpdateCheck().getVersionName() + " is available! You are running " + pluginVersion);
		getLogger().info("Update at: http://dev.bukkit.org/bukkit-plugins/askygrid");
	    }
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
	return new SkyGridGenerator();
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
     * @return the calculatingLevel
     */
    public boolean isCalculatingLevel() {
	return calculatingLevel;
    }

    /**
     * @return the newIsland
     */
    public boolean isNewIsland() {
	return newIsland;
    }

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    @SuppressWarnings("deprecation")
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
	Settings.debug = getConfig().getInt("debug", 0);
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
	// Check if /island command is allowed when falling
	Settings.allowTeleportWhenFalling = getConfig().getBoolean("general.allowfallingteleport", true);
	Settings.fallingCommandBlockList = getConfig().getStringList("general.blockingcommands");
	// Max home number
	Settings.maxHomes = getConfig().getInt("general.maxhomes",1);
	if (Settings.maxHomes < 1) {
	    Settings.maxHomes = 1;
	}
	// Settings from config.yml
	Settings.worldName = getConfig().getString("general.worldName");
	Settings.createNether = getConfig().getBoolean("general.createnether", true);
	if (!Settings.createNether) {
	    getLogger().info("The Nether is disabled");
	}
	Settings.islandDistance = getConfig().getInt("island.distance", 200);
	if (Settings.islandDistance < 50) {
	    Settings.islandDistance = 50;
	    getLogger().info("Setting minimum island distance to 50");
	}
	Settings.islandXOffset = getConfig().getInt("island.xoffset", 0);
	if (Settings.islandXOffset < 0) {
	    Settings.islandXOffset = 0;
	    getLogger().info("Setting minimum island X Offset to 0");
	} else if (Settings.islandXOffset > Settings.islandDistance) {
	    Settings.islandXOffset = Settings.islandDistance;
	    getLogger().info("Setting maximum island X Offset to " + Settings.islandDistance);
	}
	Settings.islandZOffset = getConfig().getInt("island.zoffset", 0);
	if (Settings.islandZOffset < 0) {
	    Settings.islandZOffset = 0;
	    getLogger().info("Setting minimum island Z Offset to 0");
	} else if (Settings.islandZOffset > Settings.islandDistance) {
	    Settings.islandZOffset = Settings.islandDistance;
	    getLogger().info("Setting maximum island Z Offset to " + Settings.islandDistance);
	}
	long x = getConfig().getLong("island.startx", 0);
	// Check this is a multiple of island distance
	long z = getConfig().getLong("island.startz", 0);
	Settings.islandStartX = Math.round((double) x / Settings.islandDistance) * Settings.islandDistance + Settings.islandXOffset;
	Settings.islandStartZ = Math.round((double) z / Settings.islandDistance) * Settings.islandDistance + Settings.islandZOffset;
	Settings.island_level = getConfig().getInt("general.islandlevel", 120) - 5;
	if (Settings.island_level < 0) {
	    Settings.island_level = 0;
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

	Settings.island_protectionRange = getConfig().getInt("island.protectionRange", 100);
	if (Settings.island_protectionRange < 1) {
	    Settings.island_protectionRange = 1;
	}
	Settings.resetChallenges = getConfig().getBoolean("general.resetchallenges", true);
	Settings.resetMoney = getConfig().getBoolean("general.resetmoney", true);
	Settings.clearInventory = getConfig().getBoolean("general.resetinventory", true);
	Settings.resetEnderChest = getConfig().getBoolean("general.resetenderchest", false);

	Settings.startingMoney = getConfig().getDouble("general.startingmoney", 0D);
	Settings.respawnOnIsland = getConfig().getBoolean("general.respawnonisland", false);
	// Nether spawn protection radius
	Settings.netherSpawnRadius = getConfig().getInt("general.netherspawnradius", 25);
	if (Settings.netherSpawnRadius < 0) {
	    Settings.netherSpawnRadius = 0;
	} else if (Settings.netherSpawnRadius > 100) {
	    Settings.netherSpawnRadius = 100;
	}
	Settings.logInRemoveMobs = getConfig().getBoolean("general.loginremovemobs", true);
	Settings.islandRemoveMobs = getConfig().getBoolean("general.islandremovemobs", false);
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
	Settings.allowPvP = getConfig().getBoolean("island.allowPvP", false);
	Settings.allowNetherPvP = getConfig().getBoolean("island.allowNetherPvP", false);
	Settings.allowFireSpread = getConfig().getBoolean("island.allowfirespread", false);
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
	// Enables warp signs in ASkyBlock
	warpSignsListener = new WarpSigns(this);
	manager.registerEvents(warpSignsListener, this);
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
	if (Settings.resetChallenges) {
	    // Reset the player's challenge status
	    players.resetAllChallenges(player.getUniqueId());
	}
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
	// Enables warp signs in ASkyBlock
	warpSignsListener = new WarpSigns(this);
	manager.registerEvents(warpSignsListener, this);
    }

    /**
     * @param calculatingLevel
     *            the calculatingLevel to set
     */
    public void setCalculatingLevel(boolean calculatingLevel) {
	this.calculatingLevel = calculatingLevel;
    }

    /**
     * @param newIsland
     *            the newIsland to set
     */
    public void setNewIsland(boolean newIsland) {
	this.newIsland = newIsland;
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
	    netherWorld = WorldCreator.name(Settings.worldName + "_nether").type(WorldType.NORMAL).environment(World.Environment.NETHER).generator(new SkyGridGenerator()).createWorld();
	    netherWorld.setMonsterSpawnLimit(Settings.monsterSpawnLimit);
	    netherWorld.setAnimalSpawnLimit(Settings.animalSpawnLimit);
	}
	return netherWorld;
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
     * @return the islandCmd
     */
    public SkyGridCmd getIslandCmd() {
	return islandCmd;
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
}
