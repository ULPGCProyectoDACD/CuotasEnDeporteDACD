package org.ulpgc.dacd.model;

import java.time.Instant;

public record Match(int id, Team homeTeam, Team awayTeam, int homeGoals, int awayGoals, Instant date, String status,
                    Referee referee, Instant capturedAt) {

    @Override
    public String toString() {
        String refereeName = (referee != null) ? referee.name() : "Sin asignar";
        return String.format("[%s] %s %d - %d %s (Status: %s) | Árbitro: %s",
                date.toString(), homeTeam.name(), homeGoals, awayGoals, awayTeam.name(), status, refereeName);
    }
}