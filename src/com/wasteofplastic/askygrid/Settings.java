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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.EntityType;

/**
 * Where all the settings are
 * 
 * @author tastybento
 */
public class Settings {
    // Permission prefix
    public final static String PERMPREFIX = "askygrid.";
    // The island command
    public final static String ISLANDCOMMAND = "agrid";
    // The challenge command
    public static final String CHALLENGECOMMAND = "gridc";
    // The spawn command (Essentials spawn for example)
    public final static String SPAWNCOMMAND = "spawn";
    // Admin command
    public static final String ADMINCOMMAND = "skgadmin";
   
    public static Set<String> challengeList;
    public static int waiverAmount;
    public static List<String> challengeLevels;
    public static String worldName;
    public static int monsterSpawnLimit;
    public static int animalSpawnLimit;
    public static int waterAnimalSpawnLimit;
    // IslandGuard settings
    public static boolean allowPvP;
    public static int islandDistance;
    public static int islandXOffset;
    public static int islandZOffset;
    public static int island_protectionRange;
    public static Double startingMoney;
    public static double netherSpawnRadius;
    public static boolean resetMoney;

    // public static boolean ultraSafeBoats;
    public static boolean logInRemoveMobs;
    public static boolean islandRemoveMobs;
    public static boolean resetChallenges;

    // Challenge completion broadcast
    public static boolean broadcastMessages;
    // Nether world
    public static boolean createNether;
    public static boolean clearInventory;
    // Use control panel for /island
    public static boolean useControlPanel;
    // Prevent /island when falling
    public static boolean allowTeleportWhenFalling;
   // Challenges - show or remove completed on-time challenges
    public static boolean removeCompleteOntimeChallenges;
    public static boolean addCompletedGlow;

    // Use Economy
    public static boolean useEconomy;

    // Use physics when pasting schematic blocks
    public static boolean usePhysics;

    // Falling blocked commands
    public static List<String> fallingCommandBlockList;
    public static boolean resetEnderChest;
    public static boolean updateCheck;
    public static long islandStartX;
    public static long islandStartZ;
    public static boolean allowNetherPvP;
    public static int maxHomes;
    public static boolean immediateTeleport;
    public static int debug;
    public static boolean chooseIslandRandomly;
    public static boolean respawnOnIsland;
    public static boolean restrictWither;
    public static List<String> startCommands;
    public static boolean useWarpPanel;
    public static List<EntityType> mobWhiteList = new ArrayList<EntityType>();
    public static boolean muteDeathMessages;
    public static boolean allowFireSpread;
    public static int island_level;
}
