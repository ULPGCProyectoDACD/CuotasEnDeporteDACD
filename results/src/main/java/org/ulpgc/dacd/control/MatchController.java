package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import javax.jms.JMSException;
import org.ulpgc.dacd.control.feeder.MatchFeeder;
import org.ulpgc.dacd.control.publisher.MatchPublisher;
import org.ulpgc.dacd.model.Match;

import java.util.List;

public class MatchController {

    private static final String TOPIC = "FootballResult";
    private final MatchFeeder feeder;
    private final MatchPublisher publisher;
    private final Gson gson = new Gson();

    public MatchController(MatchFeeder feeder, MatchPublisher publisher) {
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
            try {
                publisher.publish(TOPIC, gson.toJson(match));
            } catch (JMSException e) {
                System.err.println("[MatchController] Error publicando evento: " + e.getMessage());
            }
        }
    }
}