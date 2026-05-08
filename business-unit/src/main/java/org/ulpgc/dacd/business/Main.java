package org.ulpgc.dacd.business;

import org.ulpgc.dacd.business.control.BusinessController;
import org.ulpgc.dacd.business.control.jms.ActiveMQOddsReceiver;
import org.ulpgc.dacd.business.control.jms.OddsReceiver;
import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.predictor.OnnxMatchPredictor;
import org.ulpgc.dacd.business.control.stats.EventStoreTeamStatsManager;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
import org.ulpgc.dacd.business.control.trainer.ModelTrainer;
import org.ulpgc.dacd.business.control.trainer.PythonModelTrainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            String mlDirectory = "machine-learning";
            String scriptToRun = "main.py";
            String eventStorePath = resolveEventStorePath();
            String modelPath = "business-unit/src/main/resources/match_model.onnx";

            ModelTrainer trainer = new PythonModelTrainer(mlDirectory, scriptToRun);
            trainer.trainModel();

            TeamStatsManager statsManager = new EventStoreTeamStatsManager();
            MatchPredictor predictor = new OnnxMatchPredictor(modelPath);
            BusinessController controller = new BusinessController(statsManager, predictor);
            controller.init(eventStorePath);

            System.out.println("\n--- ARRANCANDO ESCUCHADOR DE CUOTAS ---");
            String brokerUrl = "tcp://localhost:61616";
            String topicName = "FootballOdd";

            OddsReceiver receiver = new ActiveMQOddsReceiver(
                    brokerUrl,
                    topicName,
                    controller::processOddsMessage
            );
            receiver.start();

        } catch (Exception e) {
            System.err.println("❌ Error crítico al arrancar el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String resolveEventStorePath() {
        Path baseDir = Paths.get(System.getProperty("user.dir"));

        Path pathFromRoot = baseDir.resolve("eventstore/FootballResult/feeder-results");
        if (Files.exists(pathFromRoot)) return pathFromRoot.toString();

        Path pathFromModule = baseDir.resolveSibling("eventstore").resolve("FootballResult").resolve("feeder-results");
        if (Files.exists(pathFromModule)) return pathFromModule.toString();

        throw new IllegalStateException("❌ ERROR: No se ha podido localizar la carpeta 'eventstore'.");
    }
}