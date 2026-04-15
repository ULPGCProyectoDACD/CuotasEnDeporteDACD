package org.ulpgc.dacd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FootballDataOrgFeeder implements MatchFeeder {

    private static final String API_URL = "https://api.football-data.org/v4/competitions/PD/matches";
    private final String apiKey;

    public FootballDataOrgFeeder(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Match> getMatches() {
        String jsonResponse = readMatches();

        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return new ArrayList<>();
        }

        return parseMatches(jsonResponse);
    }

    private String readMatches() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-Auth-Token", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Error en la API. Código HTTP: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error de conexión a Internet: " + e.getMessage());
            return null;
        }
    }

    private List<Match> parseMatches(String jsonString) {
        List<Match> matches = new ArrayList<>();

        JsonObject rootObject = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray matchesArray = rootObject.getAsJsonArray("matches");

        Instant capturedAt = Instant.now();

        if (matchesArray == null) return matches;

        for (JsonElement element : matchesArray) {
            JsonObject matchJson = element.getAsJsonObject();
            matches.add(parseMatch(matchJson, capturedAt));
        }

        return matches;
    }

    private Match parseMatch(JsonObject matchJson, Instant capturedAt) {
        int id = matchJson.get("id").getAsInt();

        String utcDateStr = matchJson.get("utcDate").getAsString();
        Instant date = Instant.parse(utcDateStr);

        String status = matchJson.get("status").getAsString();

        Team homeTeam = parseTeam(matchJson.getAsJsonObject("homeTeam"));
        Team awayTeam = parseTeam(matchJson.getAsJsonObject("awayTeam"));

        JsonObject scoreJson = matchJson.getAsJsonObject("score");
        JsonObject fullTimeJson = scoreJson.getAsJsonObject("fullTime");

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