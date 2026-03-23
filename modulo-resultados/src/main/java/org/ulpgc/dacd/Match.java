package org.ulpgc.dacd;

import com.google.gson.annotations.SerializedName;

public class Match {

    private Fixture fixture;
    private League league;
    private Teams teams;
    private Goals goals;
    private Score score;

    public Fixture getFixture() {
        return fixture;
    }
    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    public League getLeague() {
        return league;
    }

    public Teams getTeams() {
        return teams;
    }

    public Goals getGoals() {
        return goals;
    }

    public Score getScore() {
        return score;
    }

    public static class Fixture {
        private Integer id;
        private String referee;
        private String timezone;
        private String date;
        private Long timestamp;
        private Periods periods;
        private Venue venue;
        private Status status;

        public Integer getId() {
            return id;
        }

        public String getReferee() {
            return referee;
        }

        public String getTimezone() {
            return timezone;
        }

        public String getDate() {
            return date;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public Periods getPeriods() {
            return periods;
        }

        public Venue getVenue() {
            return venue;
        }

        public Status getStatus() {
            return status;
        }
    }

    public static class Periods {
        private Long first;
        private Long second;

        public Long getFirst() {
            return first;
        }

        public Long getSecond() {
            return second;
        }
    }

    public static class Venue {
        private Integer id;
        private String name;
        private String city;

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }
    }

    public static class Status {
        @SerializedName("long")
        private String statusLong;
        @SerializedName("short")
        private String statusShort;
        private Integer elapsed;
        private Integer extra;

        public String getStatusLong() {
            return statusLong;
        }

        public String getStatusShort() {
            return statusShort;
        }

        public Integer getElapsed() {
            return elapsed;
        }

        public Integer getExtra() {
            return extra;
        }
    }

    public static class League {
        private Integer id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private Integer season;
        private String round;
        private Boolean standings;

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public String getLogo() {
            return logo;
        }

        public String getFlag() {
            return flag;
        }

        public Integer getSeason() {
            return season;
        }

        public String getRound() {
            return round;
        }

        public Boolean getStandings() {
            return standings;
        }
    }

    public static class Teams {
        private Team home;
        private Team away;

        public Team getHome() {
            return home;
        }

        public Team getAway() {
            return away;
        }
    }

    public static class Team {
        private Integer id;
        private String name;
        private String logo;
        private Boolean winner;

        public Integer getId() {
            return id;
        }

        public String getName() { return name; }

        public String getLogo() { return logo; }

        public Boolean getWinner() { return winner; }
    }

    public static class Goals {
        private Integer home;
        private Integer away;

        public Integer getHome() { return home; }

        public Integer getAway() { return away; }
    }

    public static class Score {
        private ScoreDetail halftime;
        private ScoreDetail fulltime;
        private ScoreDetail extratime;
        private ScoreDetail penalty;

        public ScoreDetail getHalftime() { return halftime; }

        public ScoreDetail getFulltime() { return fulltime; }

        public ScoreDetail getExtratime() { return extratime; }

        public ScoreDetail getPenalty() { return penalty; }
    }

    public static class ScoreDetail {
        private Integer home;
        private Integer away;

        public Integer getHome() { return home; }

        public Integer getAway() { return away; }
    }
}
