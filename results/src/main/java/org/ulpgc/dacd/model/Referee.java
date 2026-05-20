package org.ulpgc.dacd.model;

public record Referee(int id, String name) {

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
