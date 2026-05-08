package org.ulpgc.dacd.business.control;

import com.google.gson.Gson;
import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
import org.ulpgc.dacd.business.model.OddsEvent;

import java.util.Map;

public class BusinessController {
    private final TeamStatsManager statsManager;
    private final MatchPredictor predictor;
    private final Gson gson;

    public BusinessController(TeamStatsManager statsManager, MatchPredictor predictor) {
        this.statsManager = statsManager;
        this.predictor = predictor;
        this.gson = new Gson();
    }

    public void init(String eventStorePath) {
        System.out.println("\n--- INICIANDO SISTEMA CORE ---");
        statsManager.loadStatsFromEventStore(eventStorePath);
    }


    public void processOddsMessage(String rawJson) {
        try {
            OddsEvent odd = gson.fromJson(rawJson, OddsEvent.class);
            String homeTeam = odd.match().homeTeam();
            String awayTeam = odd.match().awayTeam();

            System.out.println("\n⚡ [NUEVA CUOTA RECIBIDA] " + odd.bookmaker().title() + " -> " + odd.outcomeName() + " a " + odd.price());
            evaluateMatch(homeTeam, awayTeam);

        } catch (Exception e) {
            System.err.println("❌ Error procesando el JSON de la cuota: " + e.getMessage());
        }
    }

    private void evaluateMatch(String homeTeam, String awayTeam) {
        try {
            System.out.println("⚽ Analizando: " + homeTeam + " vs " + awayTeam);

            float[] homeStats = statsManager.getTeamStats(homeTeam);
            float[] awayStats = statsManager.getTeamStats(awayTeam);

            float[] matchFeatures = new float[]{
                    homeStats[0], awayStats[0],
                    homeStats[1], homeStats[2],
                    awayStats[1], awayStats[2]
            };

            Map<Long, Double> probabilities = predictor.predictProbabilities(matchFeatures);

            System.out.println("🧠 Probabilidades IA:");
            for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
                System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
            }
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo evaluar el partido: " + e.getMessage());
            System.out.println("--------------------------------------------------");
        }
    }
}