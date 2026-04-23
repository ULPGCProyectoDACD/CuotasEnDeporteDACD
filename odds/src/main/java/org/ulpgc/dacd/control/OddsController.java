package org.ulpgc.dacd.control;

import org.ulpgc.dacd.control.feeder.OddsFeeder;
import org.ulpgc.dacd.control.persistence.OddsStore;
import org.ulpgc.dacd.model.Odd;

import java.util.List;

public class OddsController {

    private final OddsFeeder feeder;
    private final OddsStore store;

    public OddsController(OddsFeeder feeder, OddsStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        System.out.println("Iniciando tarea de captura de cuotas...");

        List<Odd> odds = feeder.getOdds();

        if (!odds.isEmpty()) {
            store.save(odds);
        } else {
            System.out.println("⚠️ No se obtuvieron datos nuevos para guardar.");
        }
    }
}
