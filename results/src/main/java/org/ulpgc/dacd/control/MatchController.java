package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.Match;
import java.util.List;

public class MatchController {

    private final MatchFeeder feeder;
    private final MatchStore store;

    public MatchController(MatchFeeder feeder, MatchStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        System.out.println("Iniciando tarea de captura de resultados...");

        List<Match> matches = feeder.getMatches();

        if (!matches.isEmpty()) {
            store.save(matches);
        } else {
            System.out.println("⚠️ No se obtuvieron datos nuevos para guardar.");
        }
    }
}