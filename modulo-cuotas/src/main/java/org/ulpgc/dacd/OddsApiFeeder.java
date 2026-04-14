package org.ulpgc.dacd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class OddsApiFeeder implements OddsFeeder {

    private static final String ODDS_API_KEY = System.getenv("ODDS_API_KEY");
    private static final String LIGA = "soccer_spain_la_liga";
    private static final String REGIONS = "eu";
    private static final String MARKETS = "h2h,totals";
    private static final String ODDS_FORMAT = "decimal";
    private static final String BASE_URL = String.format(
            "https://api.the-odds-api.com/v4/sports/%s/odds/?apiKey=%s&regions=%s&markets=%s&oddsFormat=%s",
            LIGA, ODDS_API_KEY, REGIONS, MARKETS, ODDS_FORMAT);
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @Override
    public List<Odd> feed() {
        try {
            String body = fetchResponse().body();
            return OddsParser.parseOdds(body);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching odds from The Odds API", e);
        }
    }

    private HttpResponse<String> fetchResponse() throws IOException, InterruptedException {
        return CLIENT.send(buildRequest(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
    }
}
