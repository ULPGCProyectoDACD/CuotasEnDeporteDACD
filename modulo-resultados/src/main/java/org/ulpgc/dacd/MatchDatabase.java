package org.ulpgc.dacd;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDatabase {

    private static final String DB_PATH = "data/cuotas_deporte.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_PATH;

    public static void initializeDatabase() {
        createDataDirectory();
        try (Connection connection = connect()) {
            configurePragmas(connection);
            createMatchesTable(connection);
        } catch (SQLException e) {
            System.err.println("Error initializing matches database: " + e.getMessage());
        }
    }

    public static void saveMatches(List<Match> matches) {
        String sql = """
                INSERT OR REPLACE INTO matches
                (fixture_id, referee, timezone, date, timestamp, venue_name, venue_city,
                 status_long, status_short, status_elapsed,
                 league_id, league_name, league_country, league_season, league_round,
                 home_team_id, home_team_name, away_team_id, away_team_name,
                 goals_home, goals_away,
                 score_halftime_home, score_halftime_away,
                 score_fulltime_home, score_fulltime_away,
                 score_extratime_home, score_extratime_away,
                 score_penalty_home, score_penalty_away,
                 home_winner)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            for (Match match : matches) {
                setMatchParameters(statement, match);
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
            System.out.println("Saved " + matches.size() + " matches to database.");

        } catch (SQLException e) {
            System.err.println("Error saving matches: " + e.getMessage());
        }
    }

    public static List<Match> getAllMatches() {
        String sql = "SELECT * FROM matches";
        List<Match> matches = new ArrayList<>();

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                matches.add(mapResultSetToMatch(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading matches: " + e.getMessage());
        }
        return matches;
    }

    public static List<Match> getMatchesBySeason(int season) {
        String sql = "SELECT * FROM matches WHERE league_season = ?";
        List<Match> matches = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, season);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapResultSetToMatch(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading matches by season: " + e.getMessage());
        }
        return matches;
    }

    private static void setMatchParameters(PreparedStatement stmt, Match match) throws SQLException {
        stmt.setObject(1, match.fixture() != null ? match.fixture().id() : null);
        stmt.setString(2, match.fixture() != null ? match.fixture().referee() : null);
        stmt.setString(3, match.fixture() != null ? match.fixture().timezone() : null);
        stmt.setString(4, match.fixture() != null ? match.fixture().date() : null);
        stmt.setObject(5, match.fixture() != null ? match.fixture().timestamp() : null);

        Match.Venue venue = (match.fixture() != null) ? match.fixture().venue() : null;
        stmt.setString(6, venue != null ? venue.name() : null);
        stmt.setString(7, venue != null ? venue.city() : null);

        Match.Status status = (match.fixture() != null) ? match.fixture().status() : null;
        stmt.setString(8, status != null ? status.statusLong() : null);
        stmt.setString(9, status != null ? status.statusShort() : null);
        stmt.setObject(10, status != null ? status.elapsed() : null);

        stmt.setObject(11, match.league() != null ? match.league().id() : null);
        stmt.setString(12, match.league() != null ? match.league().name() : null);
        stmt.setString(13, match.league() != null ? match.league().country() : null);
        stmt.setObject(14, match.league() != null ? match.league().season() : null);
        stmt.setString(15, match.league() != null ? match.league().round() : null);

        Match.Team home = (match.teams() != null) ? match.teams().home() : null;
        Match.Team away = (match.teams() != null) ? match.teams().away() : null;
        stmt.setObject(16, home != null ? home.id() : null);
        stmt.setString(17, home != null ? home.name() : null);
        stmt.setObject(18, away != null ? away.id() : null);
        stmt.setString(19, away != null ? away.name() : null);

        stmt.setObject(20, match.goals() != null ? match.goals().home() : null);
        stmt.setObject(21, match.goals() != null ? match.goals().away() : null);

        Match.Score score = match.score();
        stmt.setObject(22, score != null && score.halftime() != null ? score.halftime().home() : null);
        stmt.setObject(23, score != null && score.halftime() != null ? score.halftime().away() : null);
        stmt.setObject(24, score != null && score.fulltime() != null ? score.fulltime().home() : null);
        stmt.setObject(25, score != null && score.fulltime() != null ? score.fulltime().away() : null);
        stmt.setObject(26, score != null && score.extratime() != null ? score.extratime().home() : null);
        stmt.setObject(27, score != null && score.extratime() != null ? score.extratime().away() : null);
        stmt.setObject(28, score != null && score.penalty() != null ? score.penalty().home() : null);
        stmt.setObject(29, score != null && score.penalty() != null ? score.penalty().away() : null);

        Boolean homeWinner = home != null ? home.winner() : null;
        stmt.setObject(30, homeWinner != null ? (homeWinner ? 1 : 0) : null);
    }

    private static Match mapResultSetToMatch(ResultSet rs) throws SQLException {
        Match.Venue venue = new Match.Venue(
                null, rs.getString("venue_name"), rs.getString("venue_city"));

        Match.Status status = new Match.Status(
                rs.getString("status_long"), rs.getString("status_short"),
                getIntOrNull(rs, "status_elapsed"), null);

        Match.Periods periods = new Match.Periods(null, null);

        Match.Fixture fixture = new Match.Fixture(
                getIntOrNull(rs, "fixture_id"), rs.getString("referee"),
                rs.getString("timezone"), rs.getString("date"),
                getLongOrNull(rs, "timestamp"), periods, venue, status);

        Match.League league = new Match.League(
                getIntOrNull(rs, "league_id"), rs.getString("league_name"),
                rs.getString("league_country"), null, null,
                getIntOrNull(rs, "league_season"), rs.getString("league_round"), null);

        Boolean homeWinner = rs.getObject("home_winner") != null ? rs.getInt("home_winner") == 1 : null;
        Boolean awayWinner = homeWinner != null ? !homeWinner : null;

        Match.Team home = new Match.Team(
                getIntOrNull(rs, "home_team_id"), rs.getString("home_team_name"), null, homeWinner);
        Match.Team away = new Match.Team(
                getIntOrNull(rs, "away_team_id"), rs.getString("away_team_name"), null, awayWinner);
        Match.Teams teams = new Match.Teams(home, away);

        Match.Goals goals = new Match.Goals(
                getIntOrNull(rs, "goals_home"), getIntOrNull(rs, "goals_away"));

        Match.ScoreDetail halftime = new Match.ScoreDetail(
                getIntOrNull(rs, "score_halftime_home"), getIntOrNull(rs, "score_halftime_away"));
        Match.ScoreDetail fulltime = new Match.ScoreDetail(
                getIntOrNull(rs, "score_fulltime_home"), getIntOrNull(rs, "score_fulltime_away"));
        Match.ScoreDetail extratime = new Match.ScoreDetail(
                getIntOrNull(rs, "score_extratime_home"), getIntOrNull(rs, "score_extratime_away"));
        Match.ScoreDetail penalty = new Match.ScoreDetail(
                getIntOrNull(rs, "score_penalty_home"), getIntOrNull(rs, "score_penalty_away"));
        Match.Score score = new Match.Score(halftime, fulltime, extratime, penalty);

        return new Match(fixture, league, teams, goals, score);
    }

    private static Integer getIntOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    private static void configurePragmas(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA busy_timeout=5000");
        }
    }

    private static void createMatchesTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS matches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fixture_id INTEGER NOT NULL UNIQUE,
                    referee TEXT,
                    timezone TEXT,
                    date TEXT,
                    timestamp INTEGER,
                    venue_name TEXT,
                    venue_city TEXT,
                    status_long TEXT,
                    status_short TEXT,
                    status_elapsed INTEGER,
                    league_id INTEGER,
                    league_name TEXT,
                    league_country TEXT,
                    league_season INTEGER,
                    league_round TEXT,
                    home_team_id INTEGER,
                    home_team_name TEXT,
                    away_team_id INTEGER,
                    away_team_name TEXT,
                    goals_home INTEGER,
                    goals_away INTEGER,
                    score_halftime_home INTEGER,
                    score_halftime_away INTEGER,
                    score_fulltime_home INTEGER,
                    score_fulltime_away INTEGER,
                    score_extratime_home INTEGER,
                    score_extratime_away INTEGER,
                    score_penalty_home INTEGER,
                    score_penalty_away INTEGER,
                    home_winner INTEGER
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void createDataDirectory() {
        new File("data").mkdirs();
    }
}
