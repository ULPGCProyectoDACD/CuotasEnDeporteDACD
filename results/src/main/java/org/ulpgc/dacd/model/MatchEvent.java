package org.ulpgc.dacd.model;

public record MatchEvent(
        String ts,
        String ss,
        int matchId,
        String status,
        int homeTeamId,
        String homeTeam,
        int awayTeamId,
        String awayTeam,
        Integer homeGoals,
        Integer awayGoals,
        String date,
        Integer refereeId,
        String refereeName
) {}