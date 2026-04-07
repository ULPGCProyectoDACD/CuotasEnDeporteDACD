package org.ulpgc.dacd;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OddsDatabase {

    private static final String DB_PATH = "data/cuotas_deporte.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_PATH;

    public static void initializeDatabase() {
        createDataDirectory();
        try (Connection connection = connect()) {
            configurePragmas(connection);
            createOddsTable(connection);
        } catch (SQLException e) {
            System.err.println("Error initializing odds database: " + e.getMessage());
        }
    }

    public static void saveOdds(List<Odd> odds) {
        String sql = """
                INSERT OR REPLACE INTO odds
                (match_id, sport_key, home_team, away_team, commence_time,
                 bookmaker_key, bookmaker_title, market_key, outcome_name, price, point, last_update)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            for (Odd odd : odds) {
                statement.setString(1, odd.matchId());
                statement.setString(2, odd.sportKey());
                statement.setString(3, odd.homeTeam());
                statement.setString(4, odd.awayTeam());
                statement.setString(5, odd.commenceTime());
                statement.setString(6, odd.bookmakerKey());
                statement.setString(7, odd.bookmakerTitle());
                statement.setString(8, odd.marketKey());
                statement.setString(9, odd.outcomeName());
                statement.setDouble(10, odd.price());
                if (odd.point() != null) {
                    statement.setDouble(11, odd.point());
                } else {
                    statement.setNull(11, Types.REAL);
                }
                statement.setString(12, odd.lastUpdate());
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
            System.out.println("Saved " + odds.size() + " odds to database.");

        } catch (SQLException e) {
            System.err.println("Error saving odds: " + e.getMessage());
        }
    }

    public static List<Odd> getAllOdds() {
        String sql = "SELECT * FROM odds";
        List<Odd> odds = new ArrayList<>();

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                odds.add(mapResultSetToOdd(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading odds: " + e.getMessage());
        }
        return odds;
    }

    public static List<Odd> getOddsByMatch(String matchId) {
        String sql = "SELECT * FROM odds WHERE match_id = ?";
        List<Odd> odds = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, matchId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    odds.add(mapResultSetToOdd(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading odds by match: " + e.getMessage());
        }
        return odds;
    }

    private static Odd mapResultSetToOdd(ResultSet rs) throws SQLException {
        Double point = rs.getObject("point") != null ? rs.getDouble("point") : null;
        return new Odd(
                rs.getString("match_id"),
                rs.getString("sport_key"),
                rs.getString("home_team"),
                rs.getString("away_team"),
                rs.getString("commence_time"),
                rs.getString("bookmaker_key"),
                rs.getString("bookmaker_title"),
                rs.getString("market_key"),
                rs.getString("outcome_name"),
                rs.getDouble("price"),
                point,
                rs.getString("last_update")
        );
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

    private static void createOddsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS odds (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    match_id TEXT NOT NULL,
                    sport_key TEXT NOT NULL,
                    home_team TEXT NOT NULL,
                    away_team TEXT NOT NULL,
                    commence_time TEXT NOT NULL,
                    bookmaker_key TEXT NOT NULL,
                    bookmaker_title TEXT NOT NULL,
                    market_key TEXT NOT NULL,
                    outcome_name TEXT NOT NULL,
                    price REAL NOT NULL,
                    point REAL,
                    last_update TEXT NOT NULL,
                    UNIQUE(match_id, bookmaker_key, market_key, outcome_name, point)
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
