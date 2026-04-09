package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MatchParser {

    public List<Match> parse(String jsonString) {
        List<Match> matches = new ArrayList<>();

        JsonObject rootObject = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray matchesArray = rootObject.getAsJsonArray("matches");

        LocalDateTime capturedAt = LocalDateTime.now();

        if (matchesArray == null) return matches;

        for (JsonElement element : matchesArray) {
            JsonObject matchJson = element.getAsJsonObject();
            matches.add(parseMatch(matchJson, capturedAt));
        }

        return matches;
    }

    private Match parseMatch(JsonObject matchJson, LocalDateTime capturedAt) {
        int id = matchJson.get("id").getAsInt();

        String utcDateStr = matchJson.get("utcDate").getAsString();
        LocalDateTime date = LocalDateTime.parse(utcDateStr, DateTimeFormatter.ISO_DATE_TIME);

        String status = matchJson.get("status").getAsString();

        Team homeTeam = parseTeam(matchJson.getAsJsonObject("homeTeam"));
        Team awayTeam = parseTeam(matchJson.getAsJsonObject("awayTeam"));

        JsonObject fullTimeJson = matchJson.getAsJsonObject("score").getAsJsonObject("fullTime");
        int homeGoals = fullTimeJson.get("home").isJsonNull() ? 0 : fullTimeJson.get("home").getAsInt();
        int awayGoals = fullTimeJson.get("away").isJsonNull() ? 0 : fullTimeJson.get("away").getAsInt();

        Referee referee = parseReferee(matchJson.getAsJsonArray("referees"));

        return new Match(id, homeTeam, awayTeam, homeGoals, awayGoals, date, status, referee, capturedAt);
    }

    private Team parseTeam(JsonObject teamJson) {
        int id = teamJson.get("id").getAsInt();
        String name = teamJson.get("name").getAsString();
        String shortName = teamJson.get("shortName").isJsonNull() ? name : teamJson.get("shortName").getAsString();

        return new Team(id, name, shortName);
    }

    private Referee parseReferee(JsonArray refereesArray) {
        if (refereesArray == null || refereesArray.isEmpty()) {
            return null;
        }

        JsonObject refJson = refereesArray.get(0).getAsJsonObject();
        int id = refJson.get("id").getAsInt();
        String name = refJson.get("name").getAsString();

        return new Referee(id, name);
    }
}