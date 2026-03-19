package org.ulpgc.dacd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FootballApiRequest {

        public static void main(String[] args) {
            HttpClient client = HttpClient.newHttpClient();

            String apiKey = System.getenv("API_SPORTS_KEY");
            String url = "https://v3.football.api-sports.io/leagues?season=2024&country=england&type=league";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-apisports-key", apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("Status Code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());

            } catch (IOException | InterruptedException e) {
                System.err.println("Error en la conexión: " + e.getMessage());
            }
        }
    }
