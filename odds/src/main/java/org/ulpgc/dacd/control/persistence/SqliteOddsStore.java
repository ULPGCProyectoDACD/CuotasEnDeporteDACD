package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.Odd;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.ulpgc.dacd.model.MatchContext;
import org.ulpgc.dacd.model.BookmakerContext;

public class SqliteOddsStore implements OddsStore {

    private final String dbPath;

    public SqliteOddsStore(String dbPath) {
        this.dbPath = dbPath;
        createDataDirectory();
        initDatabase();
    }

    private void initDatabase() {
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

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            configurePragmas(conn);
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void save(List<Odd> odds) {
        String sql = """
                INSERT OR REPLACE INTO odds
                (match_id, sport_key, home_team, away_team, commence_time,
                 bookmaker_key, bookmaker_title, market_key, outcome_name, price, point, last_update)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Odd odd : odds) {
                pstmt.setString(1, odd.match().id());
                pstmt.setString(2, odd.match().sportKey());
                pstmt.setString(3, odd.match().homeTeam());
                pstmt.setString(4, odd.match().awayTeam());
                pstmt.setString(5, odd.match().commenceTime());
                pstmt.setString(6, odd.bookmaker().key());
                pstmt.setString(7, odd.bookmaker().title());
                pstmt.setString(8, odd.marketKey());
                pstmt.setString(9, odd.outcomeName());
                pstmt.setDouble(10, odd.price());
                if (odd.point() != null) {
                    pstmt.setDouble(11, odd.point());
                } else {
                    pstmt.setNull(11, Types.REAL);
                }
                pstmt.setString(12, odd.bookmaker().lastUpdate());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("✅ Proceso de guardado en SQLite finalizado. (Las cuotas repetidas han sido ignoradas).");

        } catch (SQLException e) {
            System.err.println("Error al guardar las cuotas: " + e.getMessage());
        }
    }

    // ── Métodos de consulta (fuera del contrato OddsStore) ───────────────────

    public List<Odd> getAllOdds() {
        String sql = "SELECT * FROM odds";
        List<Odd> odds = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                odds.add(mapResultSetToOdd(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al leer las cuotas: " + e.getMessage());
        }
        return odds;
    }

    public List<Odd> getOddsByMatch(String matchId) {
        String sql = "SELECT * FROM odds WHERE match_id = ?";
        List<Odd> odds = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, matchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    odds.add(mapResultSetToOdd(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al leer las cuotas por partido: " + e.getMessage());
        }
        return odds;
    }

    // ── Privados de infraestructura ──────────────────────────────────────────

    private Odd mapResultSetToOdd(ResultSet rs) throws SQLException {
        Double point = rs.getObject("point") != null ? rs.getDouble("point") : null;
        MatchContext match = new MatchContext(
                rs.getString("match_id"),
                rs.getString("sport_key"),
                rs.getString("home_team"),
                rs.getString("away_team"),
                rs.getString("commence_time")
        );
        BookmakerContext bookmaker = new BookmakerContext(
                rs.getString("bookmaker_key"),
                rs.getString("bookmaker_title"),
                rs.getString("last_update")
        );
        return new Odd(match, bookmaker,
                rs.getString("market_key"),
                rs.getString("outcome_name"),
                rs.getDouble("price"),
                point
        );
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbPath);
    }

    private void configurePragmas(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA busy_timeout=5000");
        }
    }

    private void createDataDirectory() {
        new File("data").mkdirs();
    }
}
