package org.ulpgc.dacd.business.model;

public record PredictionDTO(
        int id,
        String matchDate,
        String homeTeam,
        String awayTeam,
        String bookmaker,
        String market,
        String outcome,
        double oddPrice,
        double probHome,
        double probDraw,
        double probAway,
        double benefitRiskIndex,
        String timestamp
) {}
