package org.ulpgc.dacd.model;

public record Odd(
        String ts,
        String ss,
        MatchContext match,
        BookmakerContext bookmaker,
        String marketKey,
        String outcomeName,
        double price,
        Double point
) {}