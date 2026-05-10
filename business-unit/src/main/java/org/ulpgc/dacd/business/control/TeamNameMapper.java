package org.ulpgc.dacd.business.control;

import java.util.HashMap;
import java.util.Map;

public class TeamNameMapper {
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        DICTIONARY.put("Alavés", "Deportivo Alavés");
        DICTIONARY.put("Espanyol", "RCD Espanyol de Barcelona");
        DICTIONARY.put("Athletic Bilbao", "Athletic Club");
        DICTIONARY.put("Levante", "Levante UD");
        DICTIONARY.put("Barcelona", "FC Barcelona");
        DICTIONARY.put("Real Madrid", "Real Madrid CF");
        DICTIONARY.put("Rayo Vallecano", "Rayo Vallecano de Madrid");
        DICTIONARY.put("Getafe", "Getafe CF");
        DICTIONARY.put("Oviedo", "Real Oviedo");
        DICTIONARY.put("Sevilla", "Sevilla FC");
        DICTIONARY.put("Atlético Madrid", "Club Atlético de Madrid");
        DICTIONARY.put("Girona", "Girona FC");
        DICTIONARY.put("Real Betis", "Real Betis Balompié");
        DICTIONARY.put("Real Sociedad", "Real Sociedad de Fútbol");
        DICTIONARY.put("Mallorca", "RCD Mallorca");
        DICTIONARY.put("Villarreal", "Villarreal CF");
        DICTIONARY.put("Valencia", "Valencia CF");
        DICTIONARY.put("Celta Vigo", "RC Celta de Vigo");
        DICTIONARY.put("Elche CF", "Elche CF");
        DICTIONARY.put("CA Osasuna", "CA Osasuna");
    }

    public static String getOfficialName(String apiName) {
        return DICTIONARY.getOrDefault(apiName, apiName);
    }
}