package org.ulpgc.dacd;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        String apiKey = System.getenv("FOOTBALL_DATA_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: No se ha encontrado la variable de entorno FOOTBALL_DATA_API_KEY.");
            return;
        }
        String dbUrl = "jdbc:sqlite:data/cuotas_deporte.db";

        MatchFeeder feeder = new FootballDataOrgFeeder(apiKey, new MatchParser());
        MatchStore store = new SqliteMatchStore(dbUrl);
        MatchController controller = new MatchController(feeder, store);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(controller::execute, 0, 6, TimeUnit.HOURS);

    }
}