package com.gmail.gephery.factions;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by maxgr on 7/8/2016.
 */
public class WorldExplorerFactions extends JavaPlugin {
    public void onEnable() {

        this.getCommand("wef").setExecutor(new CommandsWEF(this));

        this.getServer().getPluginManager().registerEvents(new
                ListenerBlockEvents(this), this);
    }

    public void onDisable() {
    }
}
