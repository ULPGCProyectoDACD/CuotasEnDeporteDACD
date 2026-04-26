package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import jakarta.jms.JMSException;
import org.ulpgc.dacd.control.feeder.MatchFeeder;
import org.ulpgc.dacd.model.Match;
import org.ulpgc.dacd.model.MatchEvent;

import java.util.List;

public class MatchController {

    private static final String TOPIC = "FootballResult";

    private final MatchFeeder feeder;
    private final EventPublisher publisher;
    private final Gson gson = new Gson();

    public MatchController(MatchFeeder feeder, EventPublisher publisher) {
        this.feeder = feeder;
        this.publisher = publisher;
    }

    public void execute() {
        System.out.println("Iniciando tarea de captura de resultados...");

        List<Match> matches = feeder.getMatches();

        if (matches.isEmpty()) {
            System.out.println("⚠️ No se obtuvieron datos nuevos.");
            return;
        }

        for (Match match : matches) {
            MatchEvent event = buildMatchEvent(match);
            try {
                publisher.publish(TOPIC, gson.toJson(event));
            } catch (JMSException e) {
                System.err.println("[MatchController] Error publicando evento: "
                        + e.getMessage());
            }
        }
    }

    private static MatchEvent buildMatchEvent(Match match) {
        return new MatchEvent(
                match.capturedAt().toString(),
                "feeder-results",
                match.id(),
                match.status(),
                match.homeTeam().name(),
                match.awayTeam().name(),
                match.homeGoals(),
                match.awayGoals(),
                match.date().toString()
        );
    }
}