package org.ulpgc.dacd.business.control;

import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;

import java.util.Map;

public class BusinessController {
    private final TeamStatsManager statsManager;
    private final MatchPredictor predictor;

    public BusinessController(TeamStatsManager statsManager, MatchPredictor predictor) {
        this.statsManager = statsManager;
        this.predictor = predictor;
    }

    public void execute(String eventStorePath) {
        System.out.println("\n--- INICIANDO SISTEMA CORE ---");
        statsManager.loadStatsFromEventStore(eventStorePath);

        System.out.println("\n--- SISTEMA LISTO Y A LA ESPERA DE EVENTOS ---");
        evaluateMatch("Real Madrid CF", "FC Barcelona");
    }


    public void evaluateMatch(String homeTeam, String awayTeam) {
        System.out.println("\n⚡ [NUEVO EVENTO] Analizando partido: " + homeTeam + " vs " + awayTeam);

        float[] homeStats = statsManager.getTeamStats(homeTeam);
        float[] awayStats = statsManager.getTeamStats(awayTeam);

        float[] matchFeatures = new float[]{
                homeStats[0], awayStats[0],
                homeStats[1], homeStats[2],
                awayStats[1], awayStats[2]
        };

        Map<Long, Double> probabilities = predictor.predictProbabilities(matchFeatures);

        System.out.println("🧠 Probabilidades calculadas:");
        for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
            System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
        }
        System.out.println("--------------------------------------------------");
    }
}