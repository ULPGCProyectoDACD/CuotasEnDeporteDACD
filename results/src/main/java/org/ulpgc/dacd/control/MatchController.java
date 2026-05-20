package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import javax.jms.JMSException;
import org.ulpgc.dacd.control.feeder.MatchFeeder;
import org.ulpgc.dacd.control.publisher.MatchPublisher;
import org.ulpgc.dacd.model.Match;

import java.time.Instant;
import java.util.List;

public class MatchController {

    private static final String TOPIC = "FootballResult";
    private final MatchFeeder feeder;
    private final MatchPublisher publisher;
    private final WatermarkManager watermarkManager;
    private final Gson gson = new Gson();

    public MatchController(MatchFeeder feeder, MatchPublisher publisher, WatermarkManager watermarkManager) {
        this.feeder = feeder;
        this.publisher = publisher;
        this.watermarkManager = watermarkManager;
    }

    public void execute() {
        System.out.println("Iniciando tarea de captura de resultados...");
        List<Match> matches = feeder.getMatches();

        if (matches.isEmpty()) {
            System.out.println("No se obtuvieron datos nuevos.");
            return;
        }

        Instant lastDate = watermarkManager.getLastProcessedDate();
        Instant maxDateEncontrada = lastDate;

        for (Match match : matches) {
            Instant matchDate = Instant.parse(match.date());
            if (matchDate.isAfter(lastDate)) {
                try {
                    publisher.publish(TOPIC, gson.toJson(match));
                    System.out.println("Publicado nuevo partido del " + match.date());
                    if (matchDate.isAfter(maxDateEncontrada)) {
                        maxDateEncontrada = matchDate;
                    }
                } catch (JMSException e) {
                    System.err.println("[MatchController] Error publicando evento: " + e.getMessage());
                }
            }
        }
        if (maxDateEncontrada.isAfter(lastDate)) {
            watermarkManager.saveLastProcessedDate(maxDateEncontrada);
        }
    }
}