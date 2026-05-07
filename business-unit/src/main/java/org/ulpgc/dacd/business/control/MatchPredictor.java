package org.ulpgc.dacd.business.control;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MatchPredictor {
    private final OrtEnvironment env;
    private final OrtSession session;

    public MatchPredictor(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("✅ Modelo ONNX cargado correctamente en memoria.");
    }

    public Map<Long, Double> predictProbabilities(float[] features) {
        Map<Long, Double> resultProbabilities = new HashMap<>();

        try {
            String inputName = session.getInputNames().iterator().next();
            float[][] inputData = new float[][]{features};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);

            OrtSession.Result result = session.run(Collections.singletonMap(inputName, inputTensor));

            @SuppressWarnings("unchecked")
            java.util.List<ai.onnxruntime.OnnxMap> onnxSequence =
                    (java.util.List<ai.onnxruntime.OnnxMap>) result.get(1).getValue();

            @SuppressWarnings("unchecked")
            Map<Long, Float> rawProbs = (Map<Long, Float>) onnxSequence.get(0).getValue();

            inputTensor.close();

            for (Map.Entry<Long, Float> entry : rawProbs.entrySet()) {
                resultProbabilities.put(entry.getKey(), entry.getValue().doubleValue());
            }

        } catch (OrtException e) {
            System.err.println("❌ Error al realizar la predicción: " + e.getMessage());
        }

        return resultProbabilities;
    }

    public void close() throws OrtException {
        session.close();
        env.close();
    }
}