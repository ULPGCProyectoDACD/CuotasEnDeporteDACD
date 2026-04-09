package org.ulpgc.dacd;

public class Referee {
    private final int id;
    private final String name;

    public Referee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
