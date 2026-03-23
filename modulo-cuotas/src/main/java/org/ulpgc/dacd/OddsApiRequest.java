package org.ulpgc.dacd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OddsApiRequest {

    private static final String ODDS_API_KEY = System.getenv("ODDS_API_KEY");

    private static final String LIGA = "soccer_spain_la_liga";
    private static final String REGIONS = "eu";
    private static final String MARKETS = "h2h,totals";
    private static final String ODDS_FORMAT = "decimal";
    private static final String BASE_URL = String.format(
            "https://api.the-odds-api.com/v4/sports/%s/odds/?apiKey=%s&regions=%s&markets=%s&oddsFormat=%s",
            LIGA, ODDS_API_KEY, REGIONS, MARKETS, ODDS_FORMAT
    );
    private static final HttpRequest HTTP_REQUEST = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    
    public static void main(String[] args) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(HTTP_REQUEST, HttpResponse.BodyHandlers.ofString());
                System.out.println("--- DATOS DE LA LIGA ESPAÑOLA ---");
                System.out.println(response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}