package org.ulpgc.dacd.business.control.stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.dacd.business.control.TeamNameMapper;
import org.ulpgc.dacd.business.model.MatchStat;
import org.ulpgc.dacd.business.model.ParsedMatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class EventStoreTeamStatsManager implements TeamStatsManager {

    private final Map<String, LinkedList<MatchStat>> history = new HashMap<>();

    public void loadStatsFromEventStore(String directoryPath) {
        System.out.println("⏳ Cargando histórico de equipos en memoria...");

        List<ParsedMatch> allMatches = readAndParseAllMatches(directoryPath);
        sortMatchesChronologically(allMatches);
        simulateSeasonProgress(allMatches);

        System.out.println("✅ Memoria cargada. Equipos rastreados: " + history.size());
    }


    private List<ParsedMatch> readAndParseAllMatches(String directoryPath) {
        List<ParsedMatch> matches = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".events"))
                    .forEach(path -> extractMatchesFromFile(path, matches));
        } catch (IOException e) {
            System.err.println("❌ Error leyendo el eventstore: " + e.getMessage());
        }
        return matches;
    }


    private void extractMatchesFromFile(Path path, List<ParsedMatch> allMatches) {
        try (Stream<String> lines = Files.lines(path)) {
            lines.filter(line -> !line.trim().isEmpty())
                    .map(this::parseJsonToMatch)
                    .filter(Objects::nonNull)
                    .forEach(allMatches::add);
        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo archivo " + path.getFileName() + ": " + e.getMessage());
        }
    }


    private ParsedMatch parseJsonToMatch(String jsonLine) {
        JsonObject json = JsonParser.parseString(jsonLine).getAsJsonObject();
        if (!json.has("status") || !json.get("status").getAsString().equals("FINISHED")) {
            return null;
        }

        Instant date = Instant.parse(json.get("date").getAsString());

        JsonObject homeObj = json.getAsJsonObject("homeTeam");
        JsonObject awayObj = json.getAsJsonObject("awayTeam");
        
        String homeTeam = homeObj.get("name").getAsString();
        String awayTeam = awayObj.get("name").getAsString();

        homeTeam = TeamNameMapper.getOfficialName(homeTeam);
        awayTeam = TeamNameMapper.getOfficialName(awayTeam);

        int homeGoals = json.get("homeGoals").getAsInt();
        int awayGoals = json.get("awayGoals").getAsInt();

        return new ParsedMatch(date, homeTeam, awayTeam, homeGoals, awayGoals);
    }


    private void sortMatchesChronologically(List<ParsedMatch> matches) {
        matches.sort(Comparator.comparing(ParsedMatch::date));
    }


    private void simulateSeasonProgress(List<ParsedMatch> allMatches) {
        for (ParsedMatch match : allMatches) {
            int homePoints = calculatePoints(match.result(), true);
            int awayPoints = calculatePoints(match.result(), false);

            addMatchToHistory(match.homeTeam(), homePoints, match.homeGoals(), match.awayGoals());
            addMatchToHistory(match.awayTeam(), awayPoints, match.awayGoals(), match.homeGoals());
        }
    }


    private int calculatePoints(int result, boolean isHomeTeam) {
        if (result == 0) return 1;
        if (isHomeTeam && result == 1) return 3;
        if (!isHomeTeam && result == 2) return 3;
        return 0;
    }


    private void addMatchToHistory(String teamName, int points, int gf, int gc) {
        history.putIfAbsent(teamName, new LinkedList<>());
        LinkedList<MatchStat> teamMatches = history.get(teamName);

        teamMatches.add(new MatchStat(points, gf, gc));
    }


    public float[] getTeamStats(String teamName) {
        if (!history.containsKey(teamName) || history.get(teamName).isEmpty()) {
            System.out.println("⚠️ [Stats] No hay histórico para '" + teamName + "'. Usando valores por defecto (0).");
            return new float[]{0, 0, 0};
        }

        List<MatchStat> recentMatches = history.get(teamName);
        return calculateAverages(recentMatches);
    }

    private float[] calculateAverages(List<MatchStat> recentMatches) {
        float totalWeight = 0, wPoints = 0, wGf = 0, wGc = 0;
        float decay = 0.9f;
        int n = recentMatches.size();

        for (int i = 0; i < n; i++) {
            MatchStat stat = recentMatches.get(i);
            float weight = (float) Math.pow(decay, n - 1 - i);
            totalWeight += weight;
            wPoints += stat.points() * weight;
            wGf += stat.goalsFor() * weight;
            wGc += stat.goalsAgainst() * weight;
        }

        return new float[]{wPoints / totalWeight, wGf / totalWeight, wGc / totalWeight};
    }
}