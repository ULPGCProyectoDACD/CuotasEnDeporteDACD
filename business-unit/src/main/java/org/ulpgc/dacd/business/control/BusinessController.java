package org.ulpgc.dacd.business.control;

import com.google.gson.Gson;
import org.ulpgc.dacd.business.model.OddsEvent;
import java.util.Map;

public class BusinessController {
    private final PredictionService predictionService;
    private final Gson gson;

    public BusinessController(PredictionService predictionService) {
        this.predictionService = predictionService;
        this.gson = new Gson();
    }

    public void processOddsMessage(String rawJson) {
        try {
            OddsEvent odd = gson.fromJson(rawJson, OddsEvent.class);

            String homeTeam = TeamNameMapper.getOfficialName(odd.match().homeTeam());
            String awayTeam = TeamNameMapper.getOfficialName(odd.match().awayTeam());
            String commenceTime = odd.match().commenceTime();

            System.out.println("\n⚡ [NUEVA CUOTA RECIBIDA] " + odd.bookmaker().title() + " -> " + odd.outcomeName() + " a " + odd.price());
            Map<Long, Double> probabilities = predictionService.getOrCalculateProbabilities(homeTeam, awayTeam, commenceTime);

            printPredictionResults(probabilities);

        } catch (Exception e) {
            System.err.println("❌ Error procesando el JSON de la cuota: " + e.getMessage());
        }
    }

    private void printPredictionResults(Map<Long, Double> probabilities) {
        System.out.println("🧠 Probabilidades IA:");
        for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
            System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
        }
        System.out.println("--------------------------------------------------");
    }
}