package org.ulpgc.dacd.datamart.control;

import org.ulpgc.dacd.datamart.control.predictor.MatchPredictor;
import org.ulpgc.dacd.datamart.control.stats.TeamStatsManager;
import java.util.Map;

public class PredictionService {
    private final TeamStatsManager statsManager;
    private final MatchPredictor predictor;
    private final PredictionCache cache;

    public PredictionService(TeamStatsManager statsManager, MatchPredictor predictor) {
        this.statsManager = statsManager;
        this.predictor = predictor;
        this.cache = new PredictionCache();
    }

    public Map<Long, Double> getOrCalculateProbabilities(String homeTeam, String awayTeam, String commenceTime) {
        String matchKey = homeTeam + " vs " + awayTeam + " [" + commenceTime + "]";

        if (cache.contains(matchKey)) {
            System.out.println("♻️ (Leído desde el Caché - Ahorro de CPU)");
            return cache.get(matchKey);
        } else {
            return calculateAndStoreProbabilities(homeTeam, awayTeam, matchKey);
        }
    }

    private Map<Long, Double> calculateAndStoreProbabilities(String homeTeam, String awayTeam, String matchKey) {
        float[] matchFeatures = extractMatchFeatures(homeTeam, awayTeam);
        Map<Long, Double> probabilities = predictor.predictProbabilities(matchFeatures);
        cache.update(matchKey, probabilities);
        return probabilities;
    }

    private float[] extractMatchFeatures(String homeTeam, String awayTeam) {
        String officialHome = org.ulpgc.dacd.datamart.control.TeamNameMapper.getOfficialName(homeTeam);
        String officialAway = org.ulpgc.dacd.datamart.control.TeamNameMapper.getOfficialName(awayTeam);
        
        float[] homeStats = statsManager.getTeamStats(officialHome);
        float[] awayStats = statsManager.getTeamStats(officialAway);
        return new float[]{
                homeStats[0], awayStats[0],
                homeStats[1], homeStats[2],
                awayStats[1], awayStats[2]
        };
    }
}