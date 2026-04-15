package org.ulpgc.dacd;

import java.time.Instant;

public class Match {
    private final int id;
    private final Team homeTeam;
    private final Team awayTeam;
    private final int homeGoals;
    private final int awayGoals;
    private final Instant date;
    private final String status;
    private final Referee referee;
    private final Instant capturedAt;

    public Match(int id, Team homeTeam, Team awayTeam, int homeGoals, int awayGoals, Instant date, String status, Referee referee, Instant capturedAt) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.date = date;
        this.status = status;
        this.referee = referee;
        this.capturedAt = capturedAt;
    }

    public int getId() { return id; }
    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }
    public Instant getDate() { return date; }
    public String getStatus() { return status; }
    public Referee getReferee() { return referee; }
    public Instant getCapturedAt() { return capturedAt; }

    @Override
    public String toString() {
        String refereeName = (referee != null) ? referee.getName() : "Sin asignar";
        return String.format("[%s] %s %d - %d %s (Status: %s) | Árbitro: %s",
                date.toString(), homeTeam.getName(), homeGoals, awayGoals, awayTeam.getName(), status, refereeName);
    }
}