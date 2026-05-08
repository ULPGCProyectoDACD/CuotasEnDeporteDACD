package org.ulpgc.dacd.business.control;

import java.util.HashMap;
import java.util.Map;

public class PredictionCache {
    private final Map<String, Map<Long, Double>> cache = new HashMap<>();
    private static final int MAX_SIZE = 1000;

    public boolean contains(String matchKey) {
        return cache.containsKey(matchKey);
    }

    public Map<Long, Double> get(String matchKey) {
        return cache.get(matchKey);
    }

    public void put(String matchKey, Map<Long, Double> probabilities) {
        if (cache.size() >= MAX_SIZE) {
            System.out.println("🧹 [MANTENIMIENTO] Limpiando caché antiguo para liberar RAM...");
            cache.clear();
        }
        cache.put(matchKey, probabilities);
    }
}