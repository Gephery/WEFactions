package com.gmail.gephery.factions;

import com.gmail.gephery.util.UtilConfig;

/**
 * Created by maxgr on 7/11/2016.
 */
public class RunnableClaimChange implements Runnable {

    private UtilConfig factionFile;
    private String faction;

    public RunnableClaimChange(UtilConfig factionFile, String faction) {
        this.factionFile = factionFile;
        this.faction = faction;
    }

    public void run() {
        int claimable = factionFile.file.getInt("faction." + faction + ".claimable");
        factionFile.file.set("faction." + faction + ".claimable", claimable +
                UtilConfig.DEFAULT_LAND_DEATH_PENALTY);
        factionFile.saveFiles("");
    }
}
