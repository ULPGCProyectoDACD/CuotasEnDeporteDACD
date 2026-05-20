package org.ulpgc.dacd.datamart;

import org.ulpgc.dacd.datamart.control.BusinessController;
import org.ulpgc.dacd.datamart.control.PredictionService;
import org.ulpgc.dacd.datamart.control.predictor.MatchPredictor;
import org.ulpgc.dacd.datamart.control.predictor.OnnxMatchPredictor;
import org.ulpgc.dacd.datamart.control.stats.EventStoreTeamStatsManager;
import org.ulpgc.dacd.datamart.control.stats.TeamStatsManager;
import org.ulpgc.dacd.datamart.control.trainer.ModelTrainer;
import org.ulpgc.dacd.datamart.control.trainer.PythonModelTrainer;
import org.ulpgc.dacd.datamart.control.persistence.PredictionRepository;
import org.ulpgc.dacd.datamart.control.persistence.SqlitePredictionRepository;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatamartBuilderApp {
    private final String basePath;

    public DatamartBuilderApp(String basePath) {
        this.basePath = basePath;
    }

    public void start() {
        try {
            ModelTrainer trainer = new PythonModelTrainer("machine-learning", "main.py", basePath);
            trainer.trainModel();

            TeamStatsManager statsManager = new EventStoreTeamStatsManager();

            String onnxPath = Paths.get(basePath, "models", "match_model.onnx").toString();
            MatchPredictor predictor = new OnnxMatchPredictor(onnxPath);

            System.out.println("\n--- INICIANDO SISTEMA CORE ---");
            PathResolver pathResolver = new PathResolver(basePath);
            String eventStorePath = pathResolver.resolveEventStorePath();
            statsManager.loadStatsFromEventStore(eventStorePath);

            String dbPath = Paths.get(basePath, "database", "predictions.db").toString();
            PredictionRepository repository = new SqlitePredictionRepository(dbPath);

            PredictionService predictionService = new PredictionService(statsManager, predictor);

            setupScheduledMaintenance(repository, trainer, statsManager, eventStorePath);

            System.out.println("\n--- ARRANCANDO ESCUCHADOR DE CUOTAS ---");
            new BusinessController(predictionService, repository);

        } catch (Exception e) {
            System.err.println("❌ Error crítico al arrancar el sistema: " + e.getMessage());
        }
    }

    private void setupScheduledMaintenance(PredictionRepository repository, ModelTrainer trainer, TeamStatsManager statsManager, String eventStorePath) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable maintenanceTask = () -> {
            try {
                System.out.println("\n🛠️ [MANTENIMIENTO PROGRAMADO] Iniciando tareas de actualización...");
                repository.cleanOldPredictions();
                trainer.trainModel();
                statsManager.loadStatsFromEventStore(eventStorePath);
                System.out.println("✅ [MANTENIMIENTO PROGRAMADO] Actualización completada con éxito. Sistema al 100%.");
            } catch (Exception e) {
                System.err.println("❌ [ERROR MANTENIMIENTO] Falló la tarea programada: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(maintenanceTask, 24, 24, TimeUnit.HOURS);
        System.out.println("⏱️ Tarea de mantenimiento diario configurada (ciclo de 24 horas).");
    }
}