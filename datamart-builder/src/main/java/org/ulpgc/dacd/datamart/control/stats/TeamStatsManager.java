package org.ulpgc.dacd.datamart.control.stats;

public interface TeamStatsManager {
    void loadStatsFromEventStore(String directoryPath);
    float[] getTeamStats(String teamName);
}