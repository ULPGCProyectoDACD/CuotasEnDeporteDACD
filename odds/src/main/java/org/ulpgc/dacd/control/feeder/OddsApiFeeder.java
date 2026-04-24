package org.ulpgc.dacd.control.feeder;

import com.google.gson.*;
import org.ulpgc.dacd.model.MatchContext;
import org.ulpgc.dacd.model.Odd;
import org.ulpgc.dacd.model.BookmakerContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

public class OddsApiFeeder implements OddsFeeder {

    private static final String LIGA = "soccer_spain_la_liga";
    private static final String REGIONS = "eu";
    private static final String MARKETS = "h2h,totals";
    private static final String ODDS_FORMAT = "decimal";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private final String baseUrl;

    public OddsApiFeeder(String apiKey) {
        this.baseUrl = String.format(
                "https://api.the-odds-api.com/v4/sports/%s/odds/?apiKey=%s&regions=%s&markets=%s&oddsFormat=%s",
                LIGA, apiKey, REGIONS, MARKETS, ODDS_FORMAT);
    }

    @Override
    public List<Odd> getOdds() {
        try {
            String body = fetchResponse().body();
            return parseOdds(body);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching odds from The Odds API", e);
        }
    }

    private HttpResponse<String> fetchResponse() throws IOException, InterruptedException {
        return CLIENT.send(buildRequest(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();
    }

    // ── Lógica de parseo (antes en OddsParser) ──────────────────────────────

    private List<Odd> parseOdds(String rawJson) {
        return toStream(JsonParser.parseString(rawJson).getAsJsonArray())
                .map(JsonElement::getAsJsonObject)
                .flatMap(match -> extractOddsFromMatch(match).stream())
                .toList();
    }

    private List<Odd> extractOddsFromMatch(JsonObject match) {
        MatchContext matchContext = parseMatchContext(match);
        return toStream(match.getAsJsonArray("bookmakers"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(bookmaker -> extractOddsFromBookmaker(matchContext, bookmaker).stream())
                .toList();
    }

    private List<Odd> extractOddsFromBookmaker(MatchContext matchContext, JsonObject bookmaker) {
        BookmakerContext bookmarker = parseBookmakerContext(bookmaker);
        return toStream(bookmaker.getAsJsonArray("markets"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(market -> extractOddsFromMarket(matchContext, bookmarker, market).stream())
                .toList();
    }

    private List<Odd> extractOddsFromMarket(MatchContext matchContext, BookmakerContext bookmarker,
            JsonObject market) {
        String marketKey = market.get("key").getAsString();
        return toStream(market.getAsJsonArray("outcomes"))
                .map(JsonElement::getAsJsonObject)
                .map(outcome -> buildOdd(matchContext, bookmarker, marketKey, outcome))
                .toList();
    }

    private Odd buildOdd(MatchContext match, BookmakerContext bookmaker,
                         String marketKey, JsonObject outcome) {
        return new Odd(
                match,
                bookmaker,
                marketKey,
                outcome.get("name").getAsString(),
                outcome.get("price").getAsDouble(),
                parseNullableDouble(outcome, "point")
        );
    }

    private MatchContext parseMatchContext(JsonObject match) {
        return new MatchContext(
                match.get("id").getAsString(),
                match.get("sport_key").getAsString(),
                match.get("home_team").getAsString(),
                match.get("away_team").getAsString(),
                match.get("commence_time").getAsString());
    }

    private BookmakerContext parseBookmakerContext(JsonObject bookmaker) {
        return new BookmakerContext(
                bookmaker.get("key").getAsString(),
                bookmaker.get("title").getAsString(),
                bookmaker.get("last_update").getAsString());
    }

    private Double parseNullableDouble(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsDouble() : null;
    }

    private Stream<JsonElement> toStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}
