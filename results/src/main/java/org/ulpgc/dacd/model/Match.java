package org.ulpgc.dacd.model;

public record Match(
        String ts,
        String ss,
        int id,
        Team homeTeam,
        Team awayTeam,
        Integer homeGoals,
        Integer awayGoals,
        String date,        // <-- Cambiado a String para que Gson lo ponga bonito
        String status,
        Referee referee
) {
    @Override
    public String toString() {
        String refereeName = (referee != null) ? referee.name() : "Sin asignar";
        return String.format("[%s] %s %d - %d %s (Status: %s) | Árbitro: %s",
                date, homeTeam.name(), homeGoals, awayGoals, awayTeam.name(), status, refereeName);
    }
}