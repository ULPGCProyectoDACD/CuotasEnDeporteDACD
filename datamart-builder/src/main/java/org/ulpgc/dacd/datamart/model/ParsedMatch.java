package org.ulpgc.dacd.datamart.model;

import java.time.Instant;

public record ParsedMatch(Instant date, String homeTeam, String awayTeam, int homeGoals, int awayGoals, int result) {

    public ParsedMatch(Instant date, String homeTeam, String awayTeam, int homeGoals, int awayGoals) {
        this(date, homeTeam, awayTeam, homeGoals, awayGoals, calculateResult(homeGoals, awayGoals));
    }

    private static int calculateResult(int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) return 1;
        if (homeGoals == awayGoals) return 0;
        return 2;
    }
}