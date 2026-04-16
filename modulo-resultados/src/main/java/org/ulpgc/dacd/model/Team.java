package org.ulpgc.dacd.model;

public class Team {
    private final int id;
    private final String name;
    private final String shortName;

    public Team(int id, String name, String shortName) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }

    @Override
    public String toString() {
        return name;
    }
}
