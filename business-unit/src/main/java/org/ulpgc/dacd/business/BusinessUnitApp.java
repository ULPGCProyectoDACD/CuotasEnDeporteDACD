package org.ulpgc.dacd.business;

import org.ulpgc.dacd.business.control.BusinessController;
import org.ulpgc.dacd.business.control.PredictionService;
import org.ulpgc.dacd.business.control.jms.ActiveMQOddsReceiver;
import org.ulpgc.dacd.business.control.jms.OddsReceiver;
import org.ulpgc.dacd.business.control.predictor.MatchPredictor;
import org.ulpgc.dacd.business.control.predictor.OnnxMatchPredictor;
import org.ulpgc.dacd.business.control.stats.EventStoreTeamStatsManager;
import org.ulpgc.dacd.business.control.stats.TeamStatsManager;
import org.ulpgc.dacd.business.control.trainer.ModelTrainer;
import org.ulpgc.dacd.business.control.trainer.PythonModelTrainer;
import org.ulpgc.dacd.business.control.persistence.PredictionRepository;
import org.ulpgc.dacd.business.control.persistence.SqlitePredictionRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BusinessUnitApp {
    public void start() {
        try {
            ModelTrainer trainer = new PythonModelTrainer("machine-learning", "main.py");
            trainer.trainModel();

            TeamStatsManager statsManager = new EventStoreTeamStatsManager();
            MatchPredictor predictor = new OnnxMatchPredictor("business-unit/src/main/resources/match_model.onnx");

            System.out.println("\n--- INICIANDO SISTEMA CORE ---");
            statsManager.loadStatsFromEventStore(PathResolver.resolveEventStorePath());

            PredictionRepository repository = new SqlitePredictionRepository("database/predictions.db");
            PredictionService predictionService = new PredictionService(statsManager, predictor);
            BusinessController controller = new BusinessController(predictionService, repository);

            setupScheduledMaintenance(repository, trainer, statsManager);

            System.out.println("\n--- ARRANCANDO ESCUCHADOR DE CUOTAS ---");
            OddsReceiver receiver = new ActiveMQOddsReceiver("tcp://localhost:61616", "FootballOdd", controller::processOddsMessage);
            receiver.start();

        } catch (Exception e) {
            System.err.println("❌ Error crítico al arrancar el sistema: " + e.getMessage());
        }
    }

    private void setupScheduledMaintenance(PredictionRepository repository, ModelTrainer trainer, TeamStatsManager statsManager) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable maintenanceTask = () -> {
            try {
                System.out.println("\n🛠️ [MANTENIMIENTO PROGRAMADO] Iniciando tareas de actualización...");

                repository.cleanOldPredictions();
                trainer.trainModel();
                statsManager.loadStatsFromEventStore(PathResolver.resolveEventStorePath());

                System.out.println("✅ [MANTENIMIENTO PROGRAMADO] Actualización completada con éxito. Sistema al 100%.");
            } catch (Exception e) {
                System.err.println("❌ [ERROR MANTENIMIENTO] Falló la tarea programada: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(maintenanceTask, 24, 24, TimeUnit.HOURS);
        System.out.println("⏱️ Tarea de mantenimiento diario configurada (ciclo de 24 horas).");
    }
}