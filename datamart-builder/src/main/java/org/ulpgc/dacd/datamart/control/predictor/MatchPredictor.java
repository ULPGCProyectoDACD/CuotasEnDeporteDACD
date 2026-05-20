package org.ulpgc.dacd.datamart.control.predictor;

import java.util.Map;

public interface MatchPredictor {
    Map<Long, Double> predictProbabilities(float[] features);
    void close() throws Exception;
}