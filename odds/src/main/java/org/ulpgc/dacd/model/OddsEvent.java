package org.ulpgc.dacd.model;

public record OddsEvent(
        String ts,
        String ss,
        String matchId,
        String sportKey,
        String homeTeam,
        String awayTeam,
        String bookmaker,
        String marketKey,
        String outcomeName,
        double price,
        Double point
) {}
