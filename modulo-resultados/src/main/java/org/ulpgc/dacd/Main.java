package org.ulpgc.dacd;

import org.ulpgc.dacd.Match;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String apiKey = System.getenv("FOOTBALL_DATA_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: No se ha encontrado la variable de entorno FOOTBALL_DATA_API_KEY.");
            System.err.println("Por favor, configúrala en Run > Edit Configurations.");
            return;
        }

        MatchParser parser = new MatchParser();
        MatchFeeder feeder = new FootballDataOrgFeeder(apiKey, parser);

        System.out.println("Conectando con la API de La Liga...");
        List<Match> matches = feeder.getMatches();

        if (matches.isEmpty()) {
            System.out.println("No se han encontrado partidos o ha ocurrido un error.");
        } else {
            System.out.println("¡Éxito! Se han capturado " + matches.size() + " partidos.");
            System.out.println("Aquí tienes el primero de la lista:");
            System.out.println(matches.getFirst().toString());
        }
    }
}