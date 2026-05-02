package org.ulpgc.dacd;

import org.ulpgc.dacd.control.ActiveMQSubscriber;
import org.ulpgc.dacd.control.EventStoreWriter;
import org.ulpgc.dacd.control.TopicSubscriber;

public class Main {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String[] TOPICS = { "FootballOdd", "FootballResult" };

    public static void main(String[] args) {
        System.out.println("=== Event Store Builder ===");
        System.out.println("Broker: " + BROKER_URL);
        System.out.println("Topics: " + String.join(", ", TOPICS));
        System.out.println("===========================");

        EventStoreWriter writer = new EventStoreWriter();
        TopicSubscriber subscriber = new ActiveMQSubscriber(BROKER_URL, writer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ESB] Apagando Event Store Builder");
            try {
                subscriber.close();
            } catch (Exception e) {
                System.err.println("[ESB] Error durante el cierre: " + e.getMessage());
            }
        }));

        subscriber.start(TOPICS);
    }
}
