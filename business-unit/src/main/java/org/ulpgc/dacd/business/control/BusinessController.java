package org.ulpgc.dacd.business.control;

import com.google.gson.Gson;
import org.ulpgc.dacd.business.control.jms.ActiveMQOddsReceiver;
import org.ulpgc.dacd.business.control.jms.OddsReceiver;
import org.ulpgc.dacd.business.control.persistence.PredictionRepository;
import org.ulpgc.dacd.business.model.OddsEvent;
import java.util.Map;

public class BusinessController {
    private final PredictionService predictionService;
    private final PredictionRepository repository;
    private final Gson gson;

    public BusinessController(PredictionService predictionService, PredictionRepository repository) {
        this.predictionService = predictionService;
        this.repository = repository;
        this.gson = new Gson();
        OddsReceiver receiver = new ActiveMQOddsReceiver("tcp://localhost:61616", "FootballOdd", this::processOddsMessage);
        receiver.start();
    }

    private void processOddsMessage(String rawJson) {
        try {
            OddsEvent odd = gson.fromJson(rawJson, OddsEvent.class);

            if (!"h2h".equalsIgnoreCase(odd.marketKey())) {
                return;
            }

            String homeTeam = TeamNameMapper.getOfficialName(odd.match().homeTeam());
            String awayTeam = TeamNameMapper.getOfficialName(odd.match().awayTeam());
            String commenceTime = odd.match().commenceTime();

            String outcomeName = odd.outcomeName();
            String mappedOutcome = (outcomeName.equalsIgnoreCase("Draw") || outcomeName.equalsIgnoreCase("Empate"))
                    ? "Draw"
                    : TeamNameMapper.getOfficialName(outcomeName);

            System.out.println("\n⚡ [CUOTA h2h] " + odd.bookmaker().title() + " -> " + odd.outcomeName() + " a " + odd.price());

            Map<Long, Double> probabilities = predictionService.getOrCalculateProbabilities(homeTeam, awayTeam, commenceTime);
            printPredictionResults(probabilities);

            repository.savePrediction(
                    commenceTime,
                    homeTeam,
                    awayTeam,
                    odd.bookmaker().title(),
                    odd.marketKey(),
                    mappedOutcome,
                    odd.price(),
                    probabilities
            );

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