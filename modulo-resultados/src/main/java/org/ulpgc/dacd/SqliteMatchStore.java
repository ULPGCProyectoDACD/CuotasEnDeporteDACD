package org.ulpgc.dacd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

public class SqliteMatchStore implements MatchStore {

    private final String dbPath;

    public SqliteMatchStore(String dbPath) {
        this.dbPath = dbPath;
        initDatabase();
    }

    private void initDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS matches (
                    id INTEGER,
                    home_team TEXT,
                    away_team TEXT,
                    home_goals INTEGER,
                    away_goals INTEGER,
                    date TEXT,
                    status TEXT,
                    referee TEXT,
                    captured_at TEXT,
                    PRIMARY KEY (id, captured_at)
                );
                """;

        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void save(List<Match> matches) {
        String sql = "INSERT OR IGNORE INTO matches (id, home_team, away_team, home_goals, away_goals, date, status, referee, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Match match : matches) {
                pstmt.setInt(1, match.getId());
                pstmt.setString(2, match.getHomeTeam().getName());
                pstmt.setString(3, match.getAwayTeam().getName());
                pstmt.setInt(4, match.getHomeGoals());
                pstmt.setInt(5, match.getAwayGoals());
                pstmt.setString(6, match.getDate().toString());
                pstmt.setString(7, match.getStatus());

                String refereeName = (match.getReferee() != null) ? match.getReferee().getName() : "Sin asignar";
                pstmt.setString(8, refereeName);
                pstmt.setString(9, match.getCapturedAt().toString());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("✅ Se han guardado " + matches.size() + " partidos en la base de datos.");

        } catch (SQLException e) {
            System.err.println("Error al guardar los partidos: " + e.getMessage());
        }
    }
}