package org.ulpgc.dacd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class FootballDataOrgFeeder implements MatchFeeder {

    private static final String API_URL = "https://api.football-data.org/v4/competitions/PD/matches?status=FINISHED";

    private final String apiKey;
    private final MatchParser parser;

    public FootballDataOrgFeeder(String apiKey, MatchParser parser) {
        this.apiKey = apiKey;
        this.parser = parser;
    }

    @Override
    public List<Match> getMatches() {
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-Auth-Token", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parser.parse(response.body());
            } else {
                System.err.println("Error en la API. Código HTTP: " + response.statusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("Error de conexión a Internet: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}