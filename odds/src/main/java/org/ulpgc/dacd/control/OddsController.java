package org.ulpgc.dacd.control;

import com.google.gson.Gson;
import jakarta.jms.JMSException;
import org.ulpgc.dacd.control.feeder.OddsFeeder;
import org.ulpgc.dacd.model.Odd;
import org.ulpgc.dacd.model.OddsEvent;

import java.time.Instant;
import java.util.List;

public class OddsController {

    private static final String TOPIC = "Prediction";

    private final OddsFeeder feeder;
    private final EventPublisher publisher;
    private final Gson gson = new Gson();

    public OddsController(OddsFeeder feeder, EventPublisher publisher) {
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
            OddsEvent event = new OddsEvent(
                    Instant.now().toString(),
                    "feeder-odds",
                    odd.match().id(),
                    odd.match().sportKey(),
                    odd.match().homeTeam(),
                    odd.match().awayTeam(),
                    odd.bookmaker().key(),
                    odd.marketKey(),
                    odd.outcomeName(),
                    odd.price(),
                    odd.point()
            );;
            try {
                publisher.publish(TOPIC, gson.toJson(event));
            } catch (JMSException e) {
                System.err.println("[OddsController] Error publicando evento: "
                        + e.getMessage());
            }
        }
    }
}