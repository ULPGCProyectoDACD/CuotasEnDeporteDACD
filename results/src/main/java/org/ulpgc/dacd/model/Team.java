package org.ulpgc.dacd.model;

public record Team(int id, String name, String shortName) {

    @Override
    public String toString() {
        return name;
    }
}
