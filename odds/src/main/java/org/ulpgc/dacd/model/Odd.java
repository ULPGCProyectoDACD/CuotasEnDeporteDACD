package org.ulpgc.dacd.model;

public record Odd(
        MatchContext match,
        BookmakerContext bookmaker,
        String marketKey,
        String outcomeName,
        double price,
        Double point
) {}
