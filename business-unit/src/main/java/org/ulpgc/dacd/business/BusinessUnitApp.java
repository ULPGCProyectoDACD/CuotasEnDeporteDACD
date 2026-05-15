package org.ulpgc.dacd.business;

import io.javalin.Javalin;
import org.ulpgc.dacd.business.controller.PredictionController;
import org.ulpgc.dacd.business.repository.PredictionReader;
import org.ulpgc.dacd.business.repository.SqlitePredictionReader;
//import org.ulpgc.dacd.business.repository.MockPredictionReader; // <-- Corregido

import java.util.Map;

public class BusinessUnitApp {
    private static final int PORT = 7070;
    private static final String DB_PATH = "database/predictions.db";

    public void start() {
        PredictionReader reader = new SqlitePredictionReader(DB_PATH);
        //PredictionReader reader = new MockPredictionReader();
        PredictionController controller = new PredictionController(reader);

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.http.gzipOnlyCompression();
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        });

        app.get("/api/predictions", controller::getPredictions);
        app.get("/api/filters", controller::getFilters);
        app.get("/api/health", ctx -> ctx.json(Map.of(
                "status", "ok",
                "module", "business-unit",
                "port", PORT)));

        app.start(PORT);

        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════╗\n" +
                "║                 CuotasEnDeporteDACD                  ║\n" +
                "║   🌐 Business Unit:  http://localhost:" + PORT + "   ║\n" +
                "╚══════════════════════════════════════════════════════╝\n");
    }
}