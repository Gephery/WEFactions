package com.gmail.gephery.factions;

import com.gmail.gephery.util.UtilConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by maxgr on 7/10/2016.
 */
public class ListenerBlockEvents implements Listener {

    private JavaPlugin plugin;

    public ListenerBlockEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String fileFolder  = "/" + player.getWorld().getName() + "/";
        UtilConfig landFile = new UtilConfig(fileFolder, plugin, "land.yml");

        Block block = event.getBlock();
        boolean cancel = landFile.handelEventFac(block.getLocation(), player, "break", "WEF");
        if (cancel) {
            event.setCancelled(cancel);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String fileFolder  = "/" + player.getWorld().getName() + "/";
        UtilConfig landFile = new UtilConfig(fileFolder, plugin, "land.yml");

        // Block coords
        Block block = event.getBlock();
        boolean cancel = landFile.handelEventFac(block.getLocation(), player, "place", "WEF");
        if (cancel) {
            event.setCancelled(cancel);
        }
    }

    @EventHandler
    public void onEntityDamagebyEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getDamager();
            Player p2 = (Player) event.getEntity();
            UtilConfig landFile = new UtilConfig("", plugin, "faction.yml");

            String p1Faction = landFile.file.getString("player." + player.getName() + ".faction");
            String p2Faction = landFile.file.getString("player." + p2.getName() + ".faction");

            if (p1Faction != null && p2Faction != null && p1Faction.equals(p2Faction)) {
                event.setCancelled(true);
            }
        }
    }
    //TODO finish death event for claimable
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UtilConfig factionFile = new UtilConfig("", plugin, "faction.yml");
        if (factionFile.file.get("player." + player.getName() + ".faction") != null) {
            String faction = factionFile.file.getString("player." + player.getName() + ".faction");
            if (factionFile.file.get("faction." + faction + ".claimable") != null) {
                int claimable = factionFile.file.getInt("faction." + faction + ".claimable");
                factionFile.file.set("faction." + faction + ".claimable", claimable -
                                        UtilConfig.DEFAULT_LAND_DEATH_PENALTY);
                factionFile.saveFiles("");
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                        new RunnableClaimChange(factionFile, faction), 1000);
            }
        }
    }

}
