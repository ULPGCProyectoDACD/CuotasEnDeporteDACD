package org.ulpgc.dacd.business.control;

import com.google.gson.Gson;
import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
import org.ulpgc.dacd.business.model.OddsEvent;

import java.util.HashMap;
import java.util.Map;

public class BusinessController {
    private final TeamStatsManager statsManager;
    private final MatchPredictor predictor;
    private final Gson gson;
    private final Map<String, Map<Long, Double>> predictionCache;

    public BusinessController(TeamStatsManager statsManager, MatchPredictor predictor) {
        this.statsManager = statsManager;
        this.predictor = predictor;
        this.gson = new Gson();
        this.predictionCache = new HashMap<>();
    }

    public void init(String eventStorePath) {
        System.out.println("\n--- INICIANDO SISTEMA CORE ---");
        statsManager.loadStatsFromEventStore(eventStorePath);
    }


    public void processOddsMessage(String rawJson) {
        try {
            OddsEvent odd = gson.fromJson(rawJson, OddsEvent.class);

            String rawHomeTeam = odd.match().homeTeam();
            String rawAwayTeam = odd.match().awayTeam();
            String commenceTime = odd.match().commenceTime();

            String homeTeam = TeamNameMapper.getOfficialName(rawHomeTeam);
            String awayTeam = TeamNameMapper.getOfficialName(rawAwayTeam);

            System.out.println("\n⚡ [NUEVA CUOTA RECIBIDA] " + odd.bookmaker().title() + " -> " + odd.outcomeName() + " a " + odd.price());

            Map<Long, Double> probabilities = getOrCalculateProbabilities(homeTeam, awayTeam, commenceTime);

            System.out.println("🧠 Probabilidades IA:");
            for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
                System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
            }
            System.out.println("--------------------------------------------------");

        } catch (Exception e) {
            System.err.println("❌ Error procesando el JSON de la cuota: " + e.getMessage());
        }
    }


    private Map<Long, Double> getOrCalculateProbabilities(String homeTeam, String awayTeam, String commenceTime) {

        String matchKey = homeTeam + " vs " + awayTeam + " [" + commenceTime + "]";

        if (predictionCache.containsKey(matchKey)) {
            System.out.println("♻️ (Leído desde el Caché - Ahorro de CPU)");
            return predictionCache.get(matchKey);
        }

        if (predictionCache.size() > 1000) {
            System.out.println("🧹 [MANTENIMIENTO] Limpiando caché antiguo para liberar RAM...");
            predictionCache.clear();
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
        predictionCache.put(matchKey, probabilities);

        return probabilities;
    }
}