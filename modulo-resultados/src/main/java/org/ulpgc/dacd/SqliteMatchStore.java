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
                    id INTEGER PRIMARY KEY,
                    home_team_id INTEGER,
                    home_team TEXT,
                    away_team_id INTEGER,
                    away_team TEXT,
                    home_goals INTEGER,
                    away_goals INTEGER,
                    date TEXT,
                    status TEXT,
                    referee_id INTEGER,
                    referee TEXT,
                    captured_at TEXT
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
        String sql = "INSERT OR IGNORE INTO matches (id, home_team_id, home_team, away_team_id, away_team, home_goals, away_goals, date, status, referee_id, referee, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Match match : matches) {
                pstmt.setInt(1, match.getId());
                pstmt.setInt(2, match.getHomeTeam().getId());
                pstmt.setString(3, match.getHomeTeam().getName());
                pstmt.setInt(4, match.getAwayTeam().getId());
                pstmt.setString(5, match.getAwayTeam().getName());
                pstmt.setInt(6, match.getHomeGoals());
                pstmt.setInt(7, match.getAwayGoals());
                pstmt.setString(8, match.getDate().toString());
                pstmt.setString(9, match.getStatus());

                if (match.getReferee() != null) {
                    pstmt.setInt(10, match.getReferee().getId());
                    pstmt.setString(11, match.getReferee().getName());
                } else {
                    pstmt.setNull(10, java.sql.Types.INTEGER);
                    pstmt.setString(11, "Sin asignar");
                }

                pstmt.setString(12, match.getCapturedAt().toString());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("✅ Proceso de guardado en SQLite finalizado. (Los partidos repetidos han sido ignorados).");

        } catch (SQLException e) {
            System.err.println("Error al guardar los partidos: " + e.getMessage());
        }
    }
}