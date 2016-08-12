package com.gmail.gephery.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creator: Gephery
 * Description: Keeps track of group.yml and user.yml, can save and load them in.
 */
public class UtilConfig {

    public static final int DEFAULT_P_LAND = 2;
    public static final int DEFAULT_CLAIM_SIZE = 2;
    public static final String CLAIM_PERMISSION = "WEF.claim";
    public static final int DEFAULT_MAP_SIZE = 2;
    public static final int DEFAULT_LAND_DEATH_PENALTY = 1;

    public FileConfiguration file;
    public final JavaPlugin plugin;
    private final String fileName;
    private final String root;

    /**
     * Description: Constructor for the UtilUGFiles, sets up the file folder being used and
     *              the plugin responsible for it.
     * @param fileFolder = The last file folder being used for the world name in format: /name/.
     * @param plugin = Plugin managing the files, a JavaPlugin.
     */
    public UtilConfig(String fileFolder, JavaPlugin plugin, String fileName, String root) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.root = root;
        loadFiles(fileFolder);

    }

    /**
     * Description: Loads the two files, user.yml and group.yml
     * @param fileFolder =  The last file folder being used for the world name in format: /name/.
     */
    public void loadFiles(String fileFolder) {
        file = YamlConfiguration.loadConfiguration(
                new File(this.plugin.getDataFolder() + fileFolder, fileName));
        saveFiles(fileFolder);
    }

    /**
     * Description: Save the two files, user.yml and group.yml.
     * @param fileFolder =  The last file folder being used for the world name in format: /name/.
     */
    public void saveFiles(String fileFolder) {
        try {
            file.save(new File(plugin.getDataFolder() + fileFolder, fileName));
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public boolean isSafePath(String path) {
        try {
            file.getConfigurationSection(path).getKeys(false);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void givePermission(Player player, String perm, boolean isSetting) {
        PermissionAttachment pA = player.addAttachment(plugin);
        pA.setPermission(perm, isSetting);
    }

    public String getRegion(Block block) {
        int pX = block.getX();
        int pY = block.getY();
        int pZ = block.getZ();
        String current = "";
        // Region get
        if (isSafePath(root)) {
            Set<String> regions = file.getConfigurationSection(root).getKeys(false);
            for (String region : regions) {
                if (checkIfInRegion(pX, pY, pZ, region)) {
                    current = region;
                    for (String region2Check : regions) {
                        if (checkIfInRegion(pX, pY, pZ, region2Check)) {
                            if (containedW(current, region2Check) <= 0) {
                                current = region2Check;
                            }
                        }
                    }
                    if (containedW(current, region) <= 0) {
                        current = region;
                    }
                }
            }
        }
        return current;
    }

    public String format(String... arg) {
        if (arg.length > 0) {
            String formatted = arg[0];
            if (arg.length > 1) {
                for (int i = 1; i < arg.length; i++) {
                    formatted += "." + arg[i];
                }
            }
            return formatted;
        }
        return "";
    }

    public boolean checkIfInRegion(int pX, int pY, int pZ, String region) {
        Set<String> regions = file.getConfigurationSection(root).getKeys(false);
        int w1X = file.getInt(format(root, region, "x1"));
        int w1Y = file.getInt(format(root, region, "y1"));
        int w1Z = file.getInt(format(root, region, "z1"));
        int w2X = file.getInt(format(root, region, "x2"));
        int w2Y = file.getInt(format(root, region, "y2"));
        int w2Z = file.getInt(format(root, region, "z2"));
        if ((w1X >= pX && w2X <= pX || w1X <= pX && w2X >= pX) &&
                (w1Y >= pY && w2Y <= pY || w1Y <= pY && w2Y >= pY) &&
                (w1Z >= pZ && w2Z <= pZ || w1Z <= pZ && w2Z >= pZ)) {
            return true;
        }
        return false;
    }

    public int containedW(String current, String region) {
        // Region points
        int w1X = file.getInt("region." + region + ".x1");
        int w1Y = file.getInt("region." + region + ".y1");
        int w1Z = file.getInt("region." + region + ".z1");
        int w2X = file.getInt("region." + region + ".x2");
        int w2Y = file.getInt("region." + region + ".y2");
        int w2Z = file.getInt("region." + region + ".z2");

        // Regin calc
        int xR = Math.abs(w1X - w2X);
        int yR = Math.abs(w1Y - w2Y);
        int zR = Math.abs(w1Z - w2Z);
        int areaR = xR * yR * zR;

        // Current points
        int c1X = file.getInt("region." + current + ".x1");
        int c1Y = file.getInt("region." + current + ".y1");
        int c1Z = file.getInt("region." + current + ".z1");
        int c2X = file.getInt("region." + current + ".x2");
        int c2Y = file.getInt("region." + current + ".y2");
        int c2Z = file.getInt("region." + current + ".z2");

        // Current calc
        int xC = Math.abs(c1X - c2X);
        int yC = Math.abs(c1Y - c2Y);
        int zC = Math.abs(c1Z - c2Z);
        int areaC = xC * yC * zC;

        return areaR - areaC;
    }

    public boolean handelEvent(Location loc, Player player, String type, Map<String,
                                String> pToInner, String pluginExtension) {
        int pX = loc.getBlockX();
        int pY = loc.getBlockY();
        int pZ = loc.getBlockZ();
        String name = "";
        if (player == null) {
            name = "null" + pX + pY;
        } else {
            name = player.getName();
        }

        // Region get
        if (file.get("region") != null) {
            Set<String> regions = file.getConfigurationSection("region").getKeys(false);
            // The tits of block events
            for (String region : regions) {
                if (checkIfInRegion(pX, pY, pZ, region)) {
                    pToInner.put(name, region);
                    for (String region2Check: regions) {
                        if (checkIfInRegion(pX, pY, pZ, region2Check)) {
                            if (containedW(pToInner.get(name), region2Check) <= 0) {
                                pToInner.put(name, region2Check);
                            }
                        }
                    }
                    if (containedW(pToInner.get(name), region) <= 0) {
                        pToInner.put(name, region);
                        if (file.contains("region." + region + ".attributes." + type)) {
                            if (file.getString("region." + region + ".attributes." + type)
                                    .equalsIgnoreCase("false")) {
                                if (player == null ||
                                    !player.hasPermission(pluginExtension + "" + region + "." + type)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean handelEventFac(Location loc, Player player, String type,
                                    String pluginExtension) {
        int pX = loc.getBlockX();
        int pY = loc.getBlockY();
        int pZ = loc.getBlockZ();
        String name = "";
        if (player == null) {
            name = "null" + pX + pY;
        } else {
            name = player.getName();
        }

        // Region get
        if (file.get("region") != null) {
            Set<String> regions = file.getConfigurationSection("region").getKeys(false);
            // The tits of block events
            String factionName = "";
            for (String region : regions) {
                if (region.contains("^")) {
                    factionName = region.substring(0, region.indexOf('^'));
                }
                if (checkIfInRegion(pX, pY, pZ, region)) {
                    if (file.contains("region." + region + ".attributes." + type)) {
                        if (file.getString("region." + region + ".attributes." + type)
                                .equalsIgnoreCase("false")) {
                            if (player == null ||
                                    !player.hasPermission(pluginExtension + "" + factionName + "." + type)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
