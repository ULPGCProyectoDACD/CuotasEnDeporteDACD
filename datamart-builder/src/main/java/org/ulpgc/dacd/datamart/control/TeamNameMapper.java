package org.ulpgc.dacd.datamart.control;

import java.util.HashMap;
import java.util.Map;

public class TeamNameMapper {
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        loadDictionary();
    }

    private static void loadDictionary() {
        try (java.io.InputStream is = TeamNameMapper.class.getResourceAsStream("/teams.json")) {
            if (is == null) {
                System.err.println("❌ No se encontró teams.json en los recursos.");
                return;
            }
            java.io.InputStreamReader reader = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8);
            com.google.gson.reflect.TypeToken<Map<String, String>> typeToken = new com.google.gson.reflect.TypeToken<>(){};
            Map<String, String> loaded = new com.google.gson.Gson().fromJson(reader, typeToken.getType());
            if (loaded != null) {
                DICTIONARY.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando el diccionario de equipos: " + e.getMessage());
        }
    }

    public static String getOfficialName(String apiName) {
        return DICTIONARY.getOrDefault(apiName, apiName);
    }
}