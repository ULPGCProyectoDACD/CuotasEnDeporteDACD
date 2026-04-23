package org.ulpgc.dacd;

import org.ulpgc.dacd.control.feeder.OddsApiFeeder;
import org.ulpgc.dacd.control.OddsController;
import org.ulpgc.dacd.control.feeder.OddsFeeder;
import org.ulpgc.dacd.control.persistence.OddsStore;
import org.ulpgc.dacd.control.persistence.SqliteOddsStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("ERROR: No se ha proporcionado la API Key.");
            System.err.println("Uso correcto: java -jar proyecto.jar <TU_API_KEY>");
            return;
        }

        String apiKey = args[0];
        String dbUrl = "jdbc:sqlite:data/cuotas_deporte.db";

        OddsFeeder feeder = new OddsApiFeeder(apiKey);
        OddsStore store = new SqliteOddsStore(dbUrl);
        OddsController controller = new OddsController(feeder, store);

        System.out.println("Iniciando servicio de captura de datos de Cuotas...");
        System.out.println("El programa se ejecutará ahora mismo y se repetirá cada 24 horas.");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(controller::execute, 0, 1, TimeUnit.DAYS);
    }
}