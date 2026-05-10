package org.ulpgc.dacd.business.model;

public record MatchContext(String id, String sportKey, String homeTeam, String awayTeam, String commenceTime) {
}