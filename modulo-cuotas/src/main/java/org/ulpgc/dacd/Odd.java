package org.ulpgc.dacd;

public record Odd(String matchId, String sportKey, String homeTeam, String awayTeam, String commenceTime,
                  String bookmakerKey, String bookmakerTitle, String marketKey, String outcomeName, double price,
                  Double point, String lastUpdate) {
}
