package org.ulpgc.dacd;

import com.google.gson.annotations.SerializedName;

public record Match(
        Fixture fixture,
        League league,
        Teams teams,
        Goals goals,
        Score score
) {

    public record Fixture(
            Integer id,
            String referee,
            String timezone,
            String date,
            Long timestamp,
            Periods periods,
            Venue venue,
            Status status
    ) {}

    public record Periods(Long first, Long second) {}

    public record Venue(Integer id, String name, String city) {}

    public record Status(
            @SerializedName("long") String statusLong,
            @SerializedName("short") String statusShort,
            Integer elapsed,
            Integer extra
    ) {}

    public record League(
            Integer id,
            String name,
            String country,
            String logo,
            String flag,
            Integer season,
            String round,
            Boolean standings
    ) {}

    public record Teams(Team home, Team away) {}

    public record Team(
            Integer id,
            String name,
            String logo,
            Boolean winner
    ) {}

    public record Goals(Integer home, Integer away) {}

    public record Score(
            ScoreDetail halftime,
            ScoreDetail fulltime,
            ScoreDetail extratime,
            ScoreDetail penalty
    ) {}

    public record ScoreDetail(Integer home, Integer away) {}
}