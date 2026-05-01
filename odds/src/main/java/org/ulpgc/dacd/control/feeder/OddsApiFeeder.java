package org.ulpgc.dacd.control.feeder;

import com.google.gson.*;
import org.ulpgc.dacd.model.MatchContext;
import org.ulpgc.dacd.model.Odd;
import org.ulpgc.dacd.model.BookmakerContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

public class OddsApiFeeder implements OddsFeeder {

    private static final String API_URL_TEMPLATE = "https://api.the-odds-api.com/v4/sports/soccer_spain_la_liga/odds/?apiKey=%s&regions=eu&markets=h2h,totals&oddsFormat=decimal";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private final String apiKey;

    public OddsApiFeeder(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Odd> getOdds() {
        try {
            HttpResponse<String> response = fetchResponse();
            if (response.statusCode() == 200) {
                return parseOdds(response.body());
            } else {
                System.err.println("[OddsApiFeeder] Error en la API. Código HTTP: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[OddsApiFeeder] Error de conexión a Internet: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private HttpResponse<String> fetchResponse() throws Exception {
        return CLIENT.send(buildRequest(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest buildRequest() {
        String finalUrl = String.format(API_URL_TEMPLATE, apiKey);
        return HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .GET()
                .build();
    }

    private List<Odd> parseOdds(String rawJson) {
        Instant capturedAt = Instant.now();
        return toStream(JsonParser.parseString(rawJson).getAsJsonArray())
                .map(JsonElement::getAsJsonObject)
                .flatMap(match -> extractOddsFromMatch(match, capturedAt).stream())
                .toList();
    }

    private List<Odd> extractOddsFromMatch(JsonObject match, Instant capturedAt) {
        MatchContext matchContext = parseMatchContext(match);
        return toStream(match.getAsJsonArray("bookmakers"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(bookmaker -> extractOddsFromBookmaker(matchContext, bookmaker, capturedAt).stream())
                .toList();
    }

    private List<Odd> extractOddsFromBookmaker(MatchContext matchContext, JsonObject bookmaker, Instant capturedAt) {
        BookmakerContext bookmarker = parseBookmakerContext(bookmaker);
        return toStream(bookmaker.getAsJsonArray("markets"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(market -> extractOddsFromMarket(matchContext, bookmarker, market, capturedAt).stream())
                .toList();
    }

    private List<Odd> extractOddsFromMarket(MatchContext matchContext, BookmakerContext bookmarker,
                                            JsonObject market, Instant capturedAt) {
        String marketKey = market.get("key").getAsString();
        return toStream(market.getAsJsonArray("outcomes"))
                .map(JsonElement::getAsJsonObject)
                .map(outcome -> buildOdd(matchContext, bookmarker, marketKey, outcome, capturedAt))
                .toList();
    }

    private Odd buildOdd(MatchContext match, BookmakerContext bookmaker,
                         String marketKey, JsonObject outcome, Instant capturedAt) {
        return new Odd(
                capturedAt.toString(),
                "feeder-odds",
                match,
                bookmaker,
                marketKey,
                outcome.get("name").getAsString(),
                outcome.get("price").getAsDouble(),
                parseNullableDouble(outcome, "point")
        );
    }

    private MatchContext parseMatchContext(JsonObject match) {
        String commenceTimeStr = match.get("commence_time").getAsString();
        Instant commenceTime = Instant.parse(commenceTimeStr);
        return new MatchContext(
                match.get("id").getAsString(),
                match.get("sport_key").getAsString(),
                match.get("home_team").getAsString(),
                match.get("away_team").getAsString(),
                commenceTime.toString());
    }

    private BookmakerContext parseBookmakerContext(JsonObject bookmaker) {
        String lastUpdateStr = bookmaker.get("last_update").getAsString();
        Instant lastUpdate = Instant.parse(lastUpdateStr);
        return new BookmakerContext(
                bookmaker.get("key").getAsString(),
                bookmaker.get("title").getAsString(),
                lastUpdate.toString());
    }

    private Double parseNullableDouble(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsDouble() : null;
    }

    private Stream<JsonElement> toStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}