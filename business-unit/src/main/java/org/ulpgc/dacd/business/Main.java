package org.ulpgc.dacd.business;

import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
import org.ulpgc.dacd.business.control.stats.EventStoreTeamStatsManager;
import org.ulpgc.dacd.business.control.trainer.ModelTrainer;
import org.ulpgc.dacd.business.control.trainer.PythonModelTrainer;
import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.predictor.OnnxMatchPredictor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        String mlDirectory = "machine-learning";
        String scriptToRun = "main.py";

        String eventStorePath = resolveEventStorePath();
        String modelPath = "business-unit/src/main/resources/match_model.onnx";

        ModelTrainer trainer = new PythonModelTrainer(mlDirectory, scriptToRun);
        trainer.trainModel();

        System.out.println("\n--- INICIANDO CARGA DE MEMORIA ---");
        TeamStatsManager statsManager = new EventStoreTeamStatsManager();
        statsManager.loadStatsFromEventStore(eventStorePath);

        System.out.println("\n--- INICIANDO PRUEBA DEL PREDICTOR CON DATOS REALES ---");

        try {
            MatchPredictor predictor = new OnnxMatchPredictor(modelPath);

            String homeTeam = "Real Madrid CF";
            String awayTeam = "FC Barcelona";

            System.out.println("⚽ Partido a predecir: " + homeTeam + " vs " + awayTeam);

            float[] homeStats = statsManager.getTeamStats(homeTeam);
            float[] awayStats = statsManager.getTeamStats(awayTeam);

            System.out.printf("   -> Stats Local (%s): %.0f pts | GF: %.2f | GC: %.2f\n",
                    homeTeam, homeStats[0], homeStats[1], homeStats[2]);
            System.out.printf("   -> Stats Visitante (%s): %.0f pts | GF: %.2f | GC: %.2f\n",
                    awayTeam, awayStats[0], awayStats[1], awayStats[2]);

            float[] matchFeatures = new float[]{
                    homeStats[0], awayStats[0],
                    homeStats[1], homeStats[2],
                    awayStats[1], awayStats[2]
            };

            Map<Long, Double> probabilities = predictor.predictProbabilities(matchFeatures);

            System.out.println("\n🧠 Probabilidades reales calculadas por IA:");
            for (Map.Entry<Long, Double> entry : probabilities.entrySet()) {
                System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
            }

            predictor.close();

        } catch (Exception e) {
            System.err.println("❌ Error en la prueba del predictor: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static String resolveEventStorePath() {
        Path baseDir = Paths.get(System.getProperty("user.dir"));

        Path pathFromRoot = baseDir.resolve("eventstore/FootballResult/feeder-results");
        if (Files.exists(pathFromRoot)) {
            return pathFromRoot.toString();
        }

        Path pathFromModule = baseDir.resolveSibling("eventstore").resolve("FootballResult").resolve("feeder-results");
        if (Files.exists(pathFromModule)) {
            return pathFromModule.toString();
        }

        throw new IllegalStateException("❌ ERROR: No se ha podido localizar la carpeta 'eventstore'. Asegúrate de estar ejecutando el programa desde el directorio correcto.");
    }
}