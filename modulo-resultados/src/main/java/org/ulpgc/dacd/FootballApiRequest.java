package org.ulpgc.dacd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class FootballApiRequest {

    private static final String BASE_URL = "https://v3.football.api-sports.io/fixtures?league=140&season=";
    private static final List<Integer> SEASONS = List.of(2022, 2023, 2024);
    private static final String API_KEY = System.getenv("API_SPORTS_KEY");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) {
        for (int season : SEASONS) {
            try {
                String body = response(season).body();
            } catch (IOException | InterruptedException e) {
                System.err.println("Error temporada " + season + ": " + e.getMessage());
            }
        }
    }

    private static HttpResponse<String> response(int season) throws IOException, InterruptedException {
        HttpResponse<String> response = CLIENT.send(buildRequest(season), HttpResponse.BodyHandlers.ofString());
        return response;
    }

    private static HttpRequest buildRequest(int season) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + season))
                .header("x-apisports-key", API_KEY)
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}