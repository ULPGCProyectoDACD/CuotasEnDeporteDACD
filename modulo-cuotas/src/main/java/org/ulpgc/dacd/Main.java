package org.ulpgc.dacd;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        OddsSerializer serializer = new DatabaseOdds();
        OddsFeeder feeder = new OddsApiFeeder();
        Controller controller = new Controller(feeder, serializer);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                controller::start,
                0,
                1,
                TimeUnit.DAYS
        );
    }
}