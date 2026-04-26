package org.ulpgc.dacd.model;

public record MatchEvent(
        String ts,
        String ss,
        int matchId,
        String status,
        String homeTeam,
        String awayTeam,
        Integer homeGoals,
        Integer awayGoals,
        String date
) {}