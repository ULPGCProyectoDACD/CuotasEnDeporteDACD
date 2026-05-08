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
// Nuevos imports para SQLite
import org.ulpgc.dacd.business.control.persistence.PredictionRepository;
import org.ulpgc.dacd.business.control.persistence.SqlitePredictionRepository;

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

            System.out.println("\n--- ARRANCANDO ESCUCHADOR DE CUOTAS ---");
            OddsReceiver receiver = new ActiveMQOddsReceiver("tcp://localhost:61616", "FootballOdd", controller::processOddsMessage);
            receiver.start();

        } catch (Exception e) {
            System.err.println("❌ Error crítico al arrancar el sistema: " + e.getMessage());
        }
    }
}