package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import jakarta.jms.JMSException;
import org.ulpgc.dacd.control.feeder.OddsFeeder;
import org.ulpgc.dacd.control.publisher.OddsPublisher;
import org.ulpgc.dacd.model.Odd;

import java.util.List;

public class OddsController {

    private static final String TOPIC = "FootballOdd";
    private final OddsFeeder feeder;
    private final OddsPublisher publisher;
    private final Gson gson = new Gson();

    public OddsController(OddsFeeder feeder, OddsPublisher publisher) {
        this.feeder = feeder;
        this.publisher = publisher;
    }

    public void execute() {
        System.out.println("Iniciando tarea de captura de cuotas...");

        List<Odd> odds = feeder.getOdds();

        if (odds.isEmpty()) {
            System.out.println("⚠️ No se obtuvieron datos nuevos.");
            return;
        }

        for (Odd odd : odds) {
            try {
                publisher.publish(TOPIC, gson.toJson(odd));
            } catch (JMSException e) {
                System.err.println("[OddsController] Error publicando evento: " + e.getMessage());
            }
        }
    }
}