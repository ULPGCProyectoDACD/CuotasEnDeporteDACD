package org.ulpgc.dacd.business.control;

import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
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
        }

        System.out.println("⚙️ Calculando predicción con IA por primera vez...");
        System.out.println("⚽ Analizando: " + homeTeam + " vs " + awayTeam);

        float[] homeStats = statsManager.getTeamStats(homeTeam);
        float[] awayStats = statsManager.getTeamStats(awayTeam);

        float[] matchFeatures = new float[]{
                homeStats[0], awayStats[0],
                homeStats[1], homeStats[2],
                awayStats[1], awayStats[2]
        };

        Map<Long, Double> probabilities = predictor.predictProbabilities(matchFeatures);
        cache.put(matchKey, probabilities);

        return probabilities;
    }
}