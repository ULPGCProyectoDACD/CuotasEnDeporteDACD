package org.ulpgc.dacd.business;

import org.ulpgc.dacd.business.control.PythonModelTrainer;
import org.ulpgc.dacd.business.control.MatchPredictor;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        String mlDirectory = "machine-learning";
        String scriptToRun = "main.py";

        PythonModelTrainer trainer = new PythonModelTrainer(mlDirectory, scriptToRun);
        trainer.trainModel();

        System.out.println("\n--- INICIANDO PRUEBA DEL PREDICTOR ---");

        try {
            String modelPath = "business-unit/src/main/resources/match_model.onnx";
            MatchPredictor predictor = new MatchPredictor(modelPath);

            float[] datosPartido = new float[]{15.0f, 2.0f, 2.4f, 0.8f, 0.5f, 2.1f};

            Map<Long, Double> probabilidades = predictor.predictProbabilities(datosPartido);

            System.out.println("🧠 Probabilidades reales:");

            for (Map.Entry<Long, Double> entry : probabilidades.entrySet()) {
                System.out.printf("  -> Clase %d: %.2f%%\n", entry.getKey(), entry.getValue() * 100);
            }

            predictor.close();

        } catch (Exception e) {
            System.err.println("❌ Error en la prueba del predictor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}