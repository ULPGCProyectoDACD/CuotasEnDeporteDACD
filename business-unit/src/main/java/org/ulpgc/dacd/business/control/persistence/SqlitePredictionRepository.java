package org.ulpgc.dacd.business.control.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class SqlitePredictionRepository implements PredictionRepository {
    private final String dbUrl;

    public SqlitePredictionRepository(String dbPath) {
        File file = new File(dbPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                System.out.println("📂 Carpeta de base de datos creada: " + parentDir.getPath());
            }
        }

        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDatabase();
        cleanOldPredictions();
    }

    private void initDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS predictions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                match_date TEXT NOT NULL,
                home_team TEXT NOT NULL,
                away_team TEXT NOT NULL,
                bookmaker TEXT NOT NULL,
                market TEXT NOT NULL,
                outcome TEXT NOT NULL,
                odd_price REAL NOT NULL,
                prob_home REAL NOT NULL,
                prob_draw REAL NOT NULL,
                prob_away REAL NOT NULL,
                benefit_risk_index REAL NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        String createIndexesSQL = """
            CREATE INDEX IF NOT EXISTS idx_teams ON predictions(home_team, away_team);
            CREATE INDEX IF NOT EXISTS idx_bookmaker ON predictions(bookmaker);
            CREATE INDEX IF NOT EXISTS idx_benefit ON predictions(benefit_risk_index DESC);
            """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute(createTableSQL);
            stmt.execute(createIndexesSQL);

        } catch (SQLException e) {
            System.err.println("❌ Error inicializando la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void savePrediction(String matchDate, String homeTeam, String awayTeam,
                               String bookmaker, String market, String outcome,
                               double oddPrice, Map<Long, Double> probabilities) {

        double probHome = probabilities.getOrDefault(1L, 0.0);
        double probDraw = probabilities.getOrDefault(0L, 0.0);
        double probAway = probabilities.getOrDefault(2L, 0.0);

        double index = calculateBenefitRiskIndex(outcome, homeTeam, awayTeam, oddPrice, probHome, probDraw, probAway);

        String insertSQL = "INSERT INTO predictions(match_date, home_team, away_team, bookmaker, market, outcome, odd_price, prob_home, prob_draw, prob_away, benefit_risk_index) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, matchDate);
            pstmt.setString(2, homeTeam);
            pstmt.setString(3, awayTeam);
            pstmt.setString(4, bookmaker);
            pstmt.setString(5, market);
            pstmt.setString(6, outcome);
            pstmt.setDouble(7, oddPrice);
            pstmt.setDouble(8, probHome);
            pstmt.setDouble(9, probDraw);
            pstmt.setDouble(10, probAway);
            pstmt.setDouble(11, index);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error guardando la predicción: " + e.getMessage());
        }
    }

    private double calculateBenefitRiskIndex(String outcome, String homeTeam, String awayTeam, double oddPrice, double probHome, double probDraw, double probAway) {
        double targetProbability = 0.0;

        if (outcome.equalsIgnoreCase("Draw") || outcome.equalsIgnoreCase("Empate")) {
            targetProbability = probDraw;
        } else if (outcome.equals(homeTeam)) {
            targetProbability = probHome;
        } else if (outcome.equals(awayTeam)) {
            targetProbability = probAway;
        }

        if (targetProbability == 0.0) {
            return 0.0;
        }

        return (targetProbability * oddPrice) - 1;
    }

    @Override
    public void cleanOldPredictions() {
        String deleteSQL = "DELETE FROM predictions WHERE datetime(match_date) < datetime('now', '-1 day')";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            int deletedRows = stmt.executeUpdate(deleteSQL);
            if (deletedRows > 0) {
                System.out.println("🧹 [MANTENIMIENTO DB] Se eliminaron " + deletedRows + " predicciones caducadas.");
            } else {
                System.out.println("✨ [MANTENIMIENTO DB] La base de datos está limpia.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error limpiando la base de datos antigua: " + e.getMessage());
        }
    }

}