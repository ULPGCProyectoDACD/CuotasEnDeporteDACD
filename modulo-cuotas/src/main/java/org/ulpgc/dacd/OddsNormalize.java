package org.ulpgc.dacd;

import com.google.gson.*;

import java.util.List;
import java.util.stream.StreamSupport;

public class OddsNormalize {

    private static final Gson GSON = new Gson();

    public static List<Odd> parseOdds(String rawJson) {
        return toStream(JsonParser.parseString(rawJson).getAsJsonArray())
                .map(JsonElement::getAsJsonObject)
                .flatMap(match -> extractOddsFromMatch(match).stream())
                .toList();
    }

    private static List<Odd> extractOddsFromMatch(JsonObject match) {
        MatchContext matchContext = parseMatchContext(match);
        return toStream(match.getAsJsonArray("bookmakers"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(bookmaker -> extractOddsFromBookmaker(matchContext, bookmaker).stream())
                .toList();
    }

    private static List<Odd> extractOddsFromBookmaker(MatchContext matchContext, JsonObject bookmaker) {
        BookmakerContext bookmakerContext = parseBookmakerContext(bookmaker);
        return toStream(bookmaker.getAsJsonArray("markets"))
                .map(JsonElement::getAsJsonObject)
                .flatMap(market -> extractOddsFromMarket(matchContext, bookmakerContext, market).stream())
                .toList();
    }

    private static List<Odd> extractOddsFromMarket(MatchContext matchContext, BookmakerContext bookmakerContext, JsonObject market) {
        String marketKey = market.get("key").getAsString();
        return toStream(market.getAsJsonArray("outcomes"))
                .map(JsonElement::getAsJsonObject)
                .map(outcome -> buildOdd(matchContext, bookmakerContext, marketKey, outcome))
                .toList();
    }

    private static Odd buildOdd(MatchContext match, BookmakerContext bookmaker, String marketKey, JsonObject outcome) {
        return new Odd(
                match.id(), match.sportKey(), match.homeTeam(), match.awayTeam(), match.commenceTime(),
                bookmaker.key(), bookmaker.title(), marketKey,
                outcome.get("name").getAsString(),
                outcome.get("price").getAsDouble(),
                parseNullableDouble(outcome, "point"),
                bookmaker.lastUpdate()
        );
    }

    private static MatchContext parseMatchContext(JsonObject match) {
        return new MatchContext(
                match.get("id").getAsString(),
                match.get("sport_key").getAsString(),
                match.get("home_team").getAsString(),
                match.get("away_team").getAsString(),
                match.get("commence_time").getAsString()
        );
    }

    private static BookmakerContext parseBookmakerContext(JsonObject bookmaker) {
        return new BookmakerContext(
                bookmaker.get("key").getAsString(),
                bookmaker.get("title").getAsString(),
                bookmaker.get("last_update").getAsString()
        );
    }

    private static Double parseNullableDouble(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsDouble() : null;
    }

    private static java.util.stream.Stream<JsonElement> toStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    private record MatchContext(String id, String sportKey, String homeTeam, String awayTeam, String commenceTime) {}
    private record BookmakerContext(String key, String title, String lastUpdate) {}
}