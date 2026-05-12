package org.ulpgc.dacd.business.control;

import java.util.HashMap;
import java.util.Map;

public class TeamNameMapper {
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        DICTIONARY.put("Alavés", "Alaves");
        DICTIONARY.put("Deportivo Alavés", "Alaves");
        DICTIONARY.put("Espanyol", "Espanyol");
        DICTIONARY.put("RCD Espanyol de Barcelona", "Espanyol");
        DICTIONARY.put("Athletic Bilbao", "Athletic Club");
        DICTIONARY.put("Levante UD", "Levante");
        DICTIONARY.put("Levante", "Levante");
        DICTIONARY.put("Barcelona", "Barcelona");
        DICTIONARY.put("FC Barcelona", "Barcelona");
        DICTIONARY.put("Real Madrid CF", "Real Madrid");
        DICTIONARY.put("Real Madrid", "Real Madrid");
        DICTIONARY.put("Rayo Vallecano de Madrid", "Rayo Vallecano");
        DICTIONARY.put("Rayo Vallecano", "Rayo Vallecano");
        DICTIONARY.put("Getafe CF", "Getafe");
        DICTIONARY.put("Getafe", "Getafe");
        DICTIONARY.put("Real Oviedo", "Oviedo");
        DICTIONARY.put("Oviedo", "Oviedo");
        DICTIONARY.put("Sevilla FC", "Sevilla");
        DICTIONARY.put("Sevilla", "Sevilla");
        DICTIONARY.put("Club Atlético de Madrid", "Atletico Madrid");
        DICTIONARY.put("Atlético Madrid", "Atletico Madrid");
        DICTIONARY.put("Girona FC", "Girona");
        DICTIONARY.put("Girona", "Girona");
        DICTIONARY.put("Real Betis Balompié", "Real Betis");
        DICTIONARY.put("Real Betis", "Real Betis");
        DICTIONARY.put("Real Sociedad de Fútbol", "Real Sociedad");
        DICTIONARY.put("Real Sociedad", "Real Sociedad");
        DICTIONARY.put("RCD Mallorca", "Mallorca");
        DICTIONARY.put("Mallorca", "Mallorca");
        DICTIONARY.put("Villarreal CF", "Villarreal");
        DICTIONARY.put("Villarreal", "Villarreal");
        DICTIONARY.put("Valencia CF", "Valencia");
        DICTIONARY.put("Valencia", "Valencia");
        DICTIONARY.put("RC Celta de Vigo", "Celta Vigo");
        DICTIONARY.put("Celta de Vigo", "Celta Vigo");
        DICTIONARY.put("Celta Vigo", "Celta Vigo");
        DICTIONARY.put("Elche CF", "Elche");
        DICTIONARY.put("Elche", "Elche");
        DICTIONARY.put("CA Osasuna", "Osasuna");
        DICTIONARY.put("Osasuna", "Osasuna");
        DICTIONARY.put("Celta", "Celta Vigo");
    }

    public static String getOfficialName(String apiName) {
        return DICTIONARY.getOrDefault(apiName, apiName);
    }
}