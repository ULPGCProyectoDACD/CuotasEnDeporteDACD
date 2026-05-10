package org.ulpgc.dacd.business.control.stats;

public interface TeamStatsManager {
    void loadStatsFromEventStore(String directoryPath);
    float[] getTeamStats(String teamName);
}