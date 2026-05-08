package org.ulpgc.dacd.business.control.persistence;

import java.util.Map;

public interface PredictionRepository {
    void savePrediction(String matchDate, String homeTeam, String awayTeam,
                        String bookmaker, String market, String outcome,
                        double oddPrice, Map<Long, Double> probabilities);
}