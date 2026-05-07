package org.ulpgc.dacd.business.control.predictor;

import java.util.Map;

public interface MatchPredictor {
    Map<Long, Double> predictProbabilities(float[] features);
    void close() throws Exception;
}