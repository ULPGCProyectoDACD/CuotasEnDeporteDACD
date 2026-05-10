package org.ulpgc.dacd.business.model;

public record OddsEvent(
        String ts,
        String ss,
        MatchContext match,
        BookmakerContext bookmaker,
        String marketKey,
        String outcomeName,
        double price
) {}