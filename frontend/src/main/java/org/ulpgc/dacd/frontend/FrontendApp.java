package org.ulpgc.dacd.frontend;

import io.javalin.Javalin;
import org.ulpgc.dacd.frontend.controller.PredictionController;
import org.ulpgc.dacd.frontend.repository.PredictionReader;
import org.ulpgc.dacd.frontend.repository.SqlitePredictionReader;

import java.util.Map;

public class FrontendApp {
    private static final int PORT = 7070;
    private static final String DB_PATH = "database/predictions.db";

    public void start() {
        PredictionReader reader = new SqlitePredictionReader(DB_PATH);
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
                "module", "frontend",
                "port", PORT
        )));

        app.start(PORT);

        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════╗\n" +
                "║                 CuotasEnDeporteDACD                  ║\n" +
                "║   🌐 Frontend:  http://localhost:" + PORT + "        ║\n" +
                "╚══════════════════════════════════════════════════════╝\n"
        );
    }
}
