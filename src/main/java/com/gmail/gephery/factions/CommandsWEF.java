package com.gmail.gephery.factions;

import com.gmail.gephery.util.UtilConfig;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by maxgr on 7/9/2016.
 */
public class CommandsWEF implements CommandExecutor {

    private JavaPlugin plugin;

    public CommandsWEF(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length > 0) {
            Player player = (Player) sender;
            String fileFolder = "/" + player.getWorld().getName() + "/";
            UtilConfig factionFile = new UtilConfig("", plugin, "faction.yml");
            UtilConfig landFile = new UtilConfig(fileFolder, plugin, "land.yml");
            String playerPath = "player." + player.getName();

            if (args[0].equals("create") && player.hasPermission("WEF.create")) {
                if (args.length > 1) {
                    Set<String> factions = null;
                    String factionName = args[1];
                    //TODO add banned names, like bukkit and other extensions
                    if (factionFile.isSafePath("faction")) {
                        factions = factionFile.file.getConfigurationSection("faction").getKeys(false);
                    }
                    if (factions == null || !factions.contains(factionName)) {
                        // Faction set up
                        String pathToFaction = "faction." + factionName;
                        List<String> players = new ArrayList<String>();
                        factionFile.file.createSection(pathToFaction);
                        players.add(player.getName());
                        factionFile.file.set(pathToFaction + ".claimed", 0);
                        factionFile.file.set(pathToFaction + ".claimable", UtilConfig.DEFAULT_P_LAND);
                        List<Integer> unClaimed = new ArrayList<Integer>();
                        for (int i = 1; i <= UtilConfig.DEFAULT_P_LAND; i++) {
                            unClaimed.add(i);
                        }
                        factionFile.file.set(pathToFaction + ".unclaimed", unClaimed);
                        factionFile.file.set(pathToFaction + ".players", players);

                        // Player set up
                        player.setGlowing(true);
                        factionFile.givePermission(player, "WEF." + factionName + ".break", true);
                        factionFile.givePermission(player, "WEF." + factionName + ".place", true);
                        factionFile.givePermission(player, UtilConfig.CLAIM_PERMISSION, true);
                        factionFile.file.createSection(playerPath);
                        factionFile.file.set(playerPath + ".faction", factionName);
                        factionFile.file.set(playerPath + ".position", "owner");
                        factionFile.saveFiles("");

                        player.sendMessage(factionName + " has been set up and you are the new proud owner!");
                    } else {
                        player.sendMessage("That faction name is already taken.");
                    }
                } else {
                    player.sendMessage("/wef create <faction name>");
                }
                return true;
            } else if (args[0].equals("claim")) {
                if (player.hasPermission(UtilConfig.CLAIM_PERMISSION) &&
                        factionFile.file.get(playerPath + ".faction") != null) {
                    String factionName = factionFile.file.getString(playerPath + ".faction");
                    String factionPath = "faction." + factionName;
                    int claimed = factionFile.file.getInt(factionPath + ".claimed");
                    int claimable = factionFile.file.getInt(factionPath + ".claimable");
                    List<Integer> unclaimed = factionFile.file.getIntegerList(factionPath + ".unclaimed");
                    if (!unclaimed.isEmpty()) {
                        Location pLoc = player.getLocation();
                        int pX = pLoc.getBlockX();
                        int pZ = pLoc.getBlockZ();

                        // 0 because changing world height, other to make world height to 0 claim.
                        // Four corners to determine if intersecting another region
                        Location upLLoc = new Location(player.getWorld(),
                                            pX + UtilConfig.DEFAULT_CLAIM_SIZE,
                                            0, pZ + UtilConfig.DEFAULT_CLAIM_SIZE);
                        Location upRLoc = new Location(player.getWorld(),
                                            pX - UtilConfig.DEFAULT_CLAIM_SIZE,
                                            0, pZ + UtilConfig.DEFAULT_CLAIM_SIZE);
                        Location downLLoc = new Location(player.getWorld(),
                                            pX + UtilConfig.DEFAULT_CLAIM_SIZE,
                                            0, pZ - UtilConfig.DEFAULT_CLAIM_SIZE);
                        Location downRLoc = new Location(player.getWorld(),
                                            pX - UtilConfig.DEFAULT_CLAIM_SIZE,
                                            player.getWorld().getMaxHeight(), pZ - UtilConfig.DEFAULT_CLAIM_SIZE);
                        //TODO add claimable enemy land
                        boolean regionGuard = true; // Is set by world guard and stops a land claim
                        boolean claimGuard = claimed <= claimable;
                        if (plugin.getServer().getPluginManager().getPlugin("WorldExplorerGuard") != null) {
                            String fileFolderR = "/" + player.getWorld().getName() + "/";
                            JavaPlugin regionP = (JavaPlugin) plugin.getServer()
                                    .getPluginManager()
                                    .getPlugin("WorldExplorerGuard");
                            UtilConfig regionFile = new UtilConfig(fileFolderR, regionP, "region.yml");
                            if (!regionFile.getRegion(upLLoc.getBlock()).equals("") ||
                                    !regionFile.getRegion(upRLoc.getBlock()).equals("") ||
                                    !regionFile.getRegion(downLLoc.getBlock()).equals("") ||
                                    !regionFile.getRegion(downRLoc.getBlock()).equals("")) {
                                regionGuard = false;
                            }
                        }

                        // Corner checks for claimable enemy land.
                        String r1 = landFile.getRegion(upLLoc.getBlock());
                        boolean secure1 = r1.equals("");
                        boolean deadLand1 = false;
                        String r11 = "";
                        if (!secure1) {
                            r11 = r1.substring(0, r1.indexOf('^'));
                            deadLand1 = ((factionFile.file.getInt("faction." + r11 + ".claimable") -
                                    factionFile.file.getInt("faction." + r11 + ".claimed")) < 0);
                            secure1 = secure1 || deadLand1;
                        }
                        String r2 = landFile.getRegion(upRLoc.getBlock());
                        boolean secure2 = r2.equals("");
                        boolean deadLand2 = false;
                        String r22 = "";
                        if (!secure2) {
                            r22 = r2.substring(0, r2.indexOf('^'));
                            deadLand2 = ((factionFile.file.getInt("faction." + r22 + ".claimable") -
                                    factionFile.file.getInt("faction." + r22 + ".claimed")) < 0);
                            secure2 = secure2 || deadLand2;
                        }
                        player.sendMessage(deadLand2 + "");
                        String r3 = landFile.getRegion(downRLoc.getBlock());
                        boolean secure3 = r3.equals("");
                        boolean deadLand3 = false;
                        String r33 = "";
                        if (!secure3) {
                            r33 = r3.substring(0, r3.indexOf('^'));
                            deadLand3 = ((factionFile.file.getInt("faction." + r33 + ".claimable") -
                                    factionFile.file.getInt("faction." + r33 + ".claimed")) < 0);
                            secure3 = secure3 || deadLand3;
                        }
                        player.sendMessage(deadLand3 + "");
                        String r4 = landFile.getRegion(downLLoc.getBlock());
                        boolean secure4 = r4.equals("");
                        boolean deadLand4 = false;
                        String r44 = "";
                        if (!secure4) {
                            r44 = r4.substring(0, r4.indexOf('^'));
                            deadLand4 = ((factionFile.file.getInt("faction." + r44 + ".claimable") -
                                    factionFile.file.getInt("faction." + r44 + ".claimed")) < 0);
                            secure4 = secure4 || deadLand4;
                        }
                        player.sendMessage(deadLand4 + "");
                        boolean mainGuard = secure1 && secure2 && secure3 && secure4;
                        player.sendMessage(regionGuard + "");
                        player.sendMessage(claimGuard + "");
                        if (mainGuard && regionGuard && claimGuard) {
                            if (deadLand1) {
                                unclaimLand(landFile, factionFile, "faction." + r11, r1, fileFolder);
                            }
                            // Setting up claim
                            String landID = factionName + "^" + unclaimed.remove(0);
                            String landPath = "region." + landID;
                            factionFile.file.set(factionPath + ".unclaimed", unclaimed);
                            factionFile.file.set(factionPath + ".claimed",
                                    factionFile.file.getInt(factionPath + ".claimed") + 1);
                            landFile.file.createSection(landPath + ".attributes");
                            landFile.file.set(landPath + ".attributes.break", "false");
                            landFile.file.set(landPath + ".attributes.place", "false");
                            landFile.file.set(landPath + ".faction", factionName);
                            landFile.file.set(landPath + ".x1", upLLoc.getBlockX());
                            landFile.file.set(landPath + ".y1", upLLoc.getBlockY());
                            landFile.file.set(landPath + ".z1", upLLoc.getBlockZ());
                            landFile.file.set(landPath + ".x2", downRLoc.getBlockX());
                            landFile.file.set(landPath + ".y2", downRLoc.getBlockY());
                            landFile.file.set(landPath + ".z2", downRLoc.getBlockZ());
                            landFile.saveFiles(fileFolder);
                            factionFile.saveFiles("");
                            player.sendMessage("You have claimed some land for " + factionName + ".");

                        } else {
                            player.sendMessage("Sorry mate, that region can't be claimed.");
                        }
                    } else {
                        player.sendMessage("You already claimed your maximum land.");
                    }
                } else {
                    player.sendMessage("You can't do that, mate.");
                }
                return true;
            } else if (args[0].equals("leave") && player.hasPermission("WEF.leave")) {
                if (factionFile.file.get(playerPath + ".faction") != null) {
                    String oldFac = factionFile.file.getString(playerPath + ".faction");
                    factionFile.file.set(playerPath + ".faction", null);
                    factionFile.file.set(playerPath + ".position", null);
                    factionFile.saveFiles("");
                    player.sendMessage("You have left " + oldFac + ", have fun!");
                } else {
                    player.sendMessage("It seems you have not joined a faction...");
                }
            } else if (args[0].equals("list") && player.hasPermission("WEF.list") &&
                        factionFile.isSafePath("faction")) {
                Set<String> factions = factionFile.file.getConfigurationSection("faction").getKeys(false);
                String list = "Factions: ";
                for (String factionN : factions) {
                    list += ", " + factionN;
                }
                list = list.replaceFirst(", ", "");
                player.sendMessage(list);
                return true;
            } else if (args[0].equals("unclaim") && player.hasPermission("WEF.claim")) {
                String factionName = factionFile.file.getString(playerPath + ".faction");
                String factionPath = "faction." + factionName;
                String region = landFile.getRegion(player.getLocation().getBlock());
                if (region.contains(factionName)) {
                    landFile.file.set("region." + region, null);
                    factionFile.file.set(factionPath + ".claimed",
                                         factionFile.file.getInt(factionPath + ".claimed") - 1);
                    List<Integer> unclaimed = factionFile.file.getIntegerList(factionPath + ".unclaimed");
                    if (unclaimed == null) {
                        unclaimed = new ArrayList<Integer>();
                    }
                    int regionID = Integer.parseInt(region.substring(region.indexOf('^') + 1, region.length()));
                    unclaimed.add(regionID);
                    factionFile.file.set(factionPath + ".unclaimed", unclaimed);
                    player.sendMessage("You have unclaimed some land!");
                    factionFile.saveFiles("");
                    landFile.saveFiles(fileFolder);
                } else {
                    player.sendMessage("This is not your land, mate.");
                }
                return true;
            } else if (args[0].equals("map") && player.hasPermission(UtilConfig.CLAIM_PERMISSION)) {
                Location pLoc = player.getLocation();
                int pX = pLoc.getBlockX();
                int pZ = pLoc.getBlockZ();
                String map = "";
                String faction = factionFile.file.getString(playerPath + ".faction");
                player.sendMessage("+ = your land, - = enemy land, # = unclimbable, nothing is open space");
                player.sendMessage("Map:");
                String spaces = " ";
                for (int i = 0; i < UtilConfig.DEFAULT_MAP_SIZE; i++) {
                    spaces += " ";
                }
                player.sendMessage(spaces + "N");
                for (int i = -UtilConfig.DEFAULT_MAP_SIZE; i <= UtilConfig.DEFAULT_MAP_SIZE; i++) {
                    for(int j = -UtilConfig.DEFAULT_MAP_SIZE; j <= UtilConfig.DEFAULT_MAP_SIZE; j++) {
                        String currentR = landFile.getRegion((new Location(
                                            player.getWorld(), pX + j, 1, pZ + i)).getBlock());
                        String currentRR = "";
                        if (plugin.getServer().getPluginManager().getPlugin("WorldExplorerGuard") != null) {
                            String fileFolderR = "/" + player.getWorld().getName() + "/";
                            JavaPlugin regionP = (JavaPlugin) plugin.getServer()
                                    .getPluginManager()
                                    .getPlugin("WorldExplorerGuard");
                            UtilConfig regionFile = new UtilConfig(fileFolderR, regionP, "region.yml");
                            currentRR = regionFile.getRegion((new Location(
                                        player.getWorld(), pX + j, 1, pZ + i)).getBlock());

                        }
                        if (i == 0 && j == 0) {
                            map += "@";
                        } else if (currentRR.length() > 0) {
                            map += "#";
                        } else if (faction != null && currentR.contains(faction)) {
                            map += "+";
                        } else if (currentR.equals("")) {
                            map += "  ";
                        } else {
                            map += "-";
                        }
                    }
                    player.sendMessage(map);
                    map = "";
                }
                player.sendMessage(spaces + "S");
                return true;
            } else if (args[0].equals("stats") && player.hasPermission("")) {
                String factionName = factionFile.file.getString(playerPath + ".faction");
                String factionPath = "faction." + factionName;
                int claimed = factionFile.file.getInt(factionPath + ".claimed");
                int claimable = factionFile.file.getInt(factionPath + ".claimable");
                if (factionName != null) {
                    player.sendMessage("You faction stats:");
                    player.sendMessage("  Name: " + factionName);
                    player.sendMessage("  Power: " + claimed + "/" + claimable);
                    String attributes = "";
                    //TODO add attributes
                    player.sendMessage(" Attributes: ");
                }
            }
        }
        return false;
    }

    private void unclaimLand(UtilConfig landFile, UtilConfig factionFile, String factionPath,
                             String region, String fileFolder) {
        landFile.file.set("region." + region, null);
        factionFile.file.set(factionPath + ".claimed",
                factionFile.file.getInt(factionPath + ".claimed") - 1);
        List<Integer> unclaimed = factionFile.file.getIntegerList(factionPath + ".unclaimed");
        if (unclaimed == null) {
            unclaimed = new ArrayList<Integer>();
        }
        int regionID = Integer.parseInt(region.substring(region.indexOf('^') + 1, region.length()));
        unclaimed.add(regionID);
        factionFile.file.set(factionPath + ".unclaimed", unclaimed);
        factionFile.saveFiles("");
        landFile.saveFiles(fileFolder);
    }
}
