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
    public final static String ISLANDCOMMAND = "asg";
    // The challenge command
    public static final String CHALLENGECOMMAND = "asgc";
    // The spawn command (Essentials spawn for example)
    public final static String SPAWNCOMMAND = "spawn";
    // Admin command
    public static final String ADMINCOMMAND = "asgadmin";
   
    public static Set<String> challengeList;
    public static int waiverAmount;
    public static List<String> challengeLevels;
    public static String worldName;
    public static int monsterSpawnLimit;
    public static int animalSpawnLimit;
    public static int waterAnimalSpawnLimit;
    // IslandGuard settings
    public static boolean allowPvP;
    public static int spawnDistance;
    public static int islandXOffset;
    public static int islandZOffset;
    public static int claim_protectionRange;
    public static Double startingMoney;
    public static boolean resetMoney;

    // public static boolean ultraSafeBoats;
    public static boolean logInRemoveMobs;
    public static boolean removeMobs;

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
    public static boolean respawnAtHome;
    public static boolean restrictWither;
    public static List<String> startCommands;
    public static boolean useWarpPanel;
    public static List<EntityType> mobWhiteList = new ArrayList<EntityType>();
    public static boolean muteDeathMessages;
    public static boolean allowFireSpread;
    public static int gridHeight;
    public static boolean allowBreakBlocks;
    public static boolean allowPlaceBlocks;
    public static boolean allowBedUse;
    public static boolean allowBucketUse;
    public static boolean allowShearing;
    public static boolean allowEnderPearls;
    public static boolean allowDoorUse;
    public static boolean allowLeverButtonUse;
    public static boolean allowCropTrample;
    public static boolean allowChestAccess;
    public static boolean allowFurnaceUse;
    public static boolean allowRedStone;
    public static boolean allowMusic;
    public static boolean allowCrafting;
    public static boolean allowBrewing;
    public static boolean allowGateUse;
    public static boolean allowMobHarm;
    public static boolean allowFlowIn;
    public static boolean allowFlowOut;
    public static int spawnHeight;
    public static boolean allowTNTDamage;
    public static boolean allowChestDamage;
    public static List<String> bannedCommandList;
}
