package org.ulpgc.dacd.datamart.control.predictor;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnnxMatchPredictor implements MatchPredictor {
    private final OrtEnvironment env;
    private final OrtSession session;

    public OnnxMatchPredictor(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("✅ Modelo ONNX cargado correctamente en memoria.");
    }

    @Override
    public Map<Long, Double> predictProbabilities(float[] features) {
        try {
            OnnxTensor inputTensor = createInputTensor(features);
            OrtSession.Result result = executeModel(inputTensor);
            Map<Long, Double> probabilities = extractProbabilities(result);

            inputTensor.close();
            return probabilities;
        } catch (OrtException e) {
            System.err.println("❌ Error al realizar la predicción: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private OnnxTensor createInputTensor(float[] features) throws OrtException {
        float[][] inputData = new float[][]{features};
        return OnnxTensor.createTensor(env, inputData);
    }

    private OrtSession.Result executeModel(OnnxTensor inputTensor) throws OrtException {
        String inputName = session.getInputNames().iterator().next();
        return session.run(Collections.singletonMap(inputName, inputTensor));
    }

    private Map<Long, Double> extractProbabilities(OrtSession.Result result) throws OrtException {
        Map<Long, Double> resultProbabilities = new HashMap<>();

        @SuppressWarnings("unchecked")
        java.util.List<ai.onnxruntime.OnnxMap> onnxSequence =
                (java.util.List<ai.onnxruntime.OnnxMap>) result.get(1).getValue();

        @SuppressWarnings("unchecked")
        Map<Long, Float> rawProbs = (Map<Long, Float>) onnxSequence.get(0).getValue();

        for (Map.Entry<Long, Float> entry : rawProbs.entrySet()) {
            resultProbabilities.put(entry.getKey(), entry.getValue().doubleValue());
        }
        return resultProbabilities;
    }

    @Override
    public void close() throws OrtException {
        session.close();
        env.close();
    }
}