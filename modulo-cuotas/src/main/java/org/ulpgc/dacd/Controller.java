package org.ulpgc.dacd;

import java.util.List;

public class Controller {

    private final OddsFeeder feeder;
    private final OddsSerializer serializer;

    public Controller(OddsFeeder feeder, OddsSerializer serializer) {
        this.feeder = feeder;
        this.serializer = serializer;
    }

    public void start() {
        try {
            List<Odd> odds = feeder.feed();
            odds.forEach(serializer::serialize);
            System.out.println("Success! Processed " + odds.size() + " odds for La Liga.");
        } catch (RuntimeException e) {
            System.err.println("Error during odds processing: " + e.getMessage());
        }
    }
}
