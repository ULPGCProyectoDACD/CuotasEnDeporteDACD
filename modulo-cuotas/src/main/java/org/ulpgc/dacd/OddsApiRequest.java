package org.ulpgc.dacd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OddsApiRequest {
    public static void main(String[] args) {
        String apiKey = System.getenv("ODDS_API_KEY");

        String sportKey = "soccer_spain_la_liga";
        String regions = "eu";
        String markets = "h2h,totals";
        String oddsFormat = "decimal";

        String url = String.format(
                "https://api.the-odds-api.com/v4/sports/%s/odds/?apiKey=%s&regions=%s&markets=%s&oddsFormat=%s",
                sportKey, apiKey, regions, markets, oddsFormat
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("--- DATOS DE LA LIGA ESPAÑOLA ---");
                System.out.println(response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}