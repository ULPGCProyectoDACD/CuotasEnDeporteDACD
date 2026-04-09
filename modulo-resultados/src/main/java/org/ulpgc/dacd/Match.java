package org.ulpgc.dacd;

import java.time.LocalDateTime;

public class Match {
    private final int id;
    private final Team homeTeam;
    private final Team awayTeam;
    private final int homeGoals;
    private final int awayGoals;
    private final LocalDateTime date;
    private final String status;
    private final Referee referee;
    private final LocalDateTime capturedAt;

    public Match(int id, Team homeTeam, Team awayTeam, int homeGoals, int awayGoals, LocalDateTime date, String status, Referee referee, LocalDateTime capturedAt) {
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
    public LocalDateTime getDate() { return date; }
    public String getStatus() { return status; }
    public Referee getReferee() { return referee; }
    public LocalDateTime getCapturedAt() { return capturedAt; }

    @Override
    public String toString() {
        String refereeName = (referee != null) ? referee.getName() : "Sin asignar";
        return String.format("[%s] %s %d - %d %s (Status: %s) | Árbitro: %s",
                date.toLocalDate(), homeTeam.getName(), homeGoals, awayGoals, awayTeam.getName(), status, refereeName);
    }
}