package org.ulpgc.dacd.frontend.repository;

import org.ulpgc.dacd.frontend.model.FilterOptionsDTO;
import org.ulpgc.dacd.frontend.model.PredictionDTO;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class SqlitePredictionReader implements PredictionReader {
    private final String dbUrl;

    public SqlitePredictionReader(String dbPath) {
        File file = new File(dbPath);
        if (!file.exists()) {
            System.err.println("⚠️ [Frontend] Base de datos no encontrada en: " + dbPath
                    + ". El dashboard mostrará datos vacíos hasta que business-unit genere predicciones.");
        }
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    @Override
    public List<PredictionDTO> getPredictions(String team, String bookmaker) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, match_date, home_team, away_team, bookmaker, market, outcome, " +
                "odd_price, prob_home, prob_draw, prob_away, benefit_risk_index, timestamp " +
                "FROM predictions WHERE 1=1 ");

        List<String> params = new ArrayList<>();

        if (team != null && !team.isBlank()) {
            sql.append("AND (home_team = ? OR away_team = ?) ");
            params.add(team);
            params.add(team);
        }

        if (bookmaker != null && !bookmaker.isBlank()) {
            sql.append("AND bookmaker = ? ");
            params.add(bookmaker);
        }

        sql.append("ORDER BY benefit_risk_index DESC");

        List<PredictionDTO> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [Frontend] Error leyendo predicciones: " + e.getMessage());
        }

        return results;
    }

    @Override
    public FilterOptionsDTO getFilterOptions() {
        TreeSet<String> teams = new TreeSet<>();
        List<String> bookmakers = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT home_team FROM predictions")) {
                while (rs.next()) teams.add(rs.getString(1));
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT away_team FROM predictions")) {
                while (rs.next()) teams.add(rs.getString(1));
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT bookmaker FROM predictions ORDER BY bookmaker")) {
                while (rs.next()) bookmakers.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("❌ [Frontend] Error leyendo filtros: " + e.getMessage());
        }

        return new FilterOptionsDTO(new ArrayList<>(teams), bookmakers);
    }

    private PredictionDTO mapRow(ResultSet rs) throws SQLException {
        return new PredictionDTO(
                rs.getInt("id"),
                rs.getString("match_date"),
                rs.getString("home_team"),
                rs.getString("away_team"),
                rs.getString("bookmaker"),
                rs.getString("market"),
                rs.getString("outcome"),
                rs.getDouble("odd_price"),
                rs.getDouble("prob_home"),
                rs.getDouble("prob_draw"),
                rs.getDouble("prob_away"),
                rs.getDouble("benefit_risk_index"),
                rs.getString("timestamp")
        );
    }
}
