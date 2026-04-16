package org.ulpgc.dacd;

import org.ulpgc.dacd.control.FootballDataOrgFeeder;
import org.ulpgc.dacd.control.MatchController;
import org.ulpgc.dacd.control.MatchFeeder;
import org.ulpgc.dacd.control.MatchStore;
import org.ulpgc.dacd.control.SqliteMatchStore;

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

        MatchFeeder feeder = new FootballDataOrgFeeder(apiKey);
        MatchStore store = new SqliteMatchStore(dbUrl);
        MatchController controller = new MatchController(feeder, store);

        System.out.println("Iniciando servicio de captura de datos de Fútbol...");
        System.out.println("El programa se ejecutará ahora mismo y se repetirá cada 6 horas.");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(controller::execute, 0, 1, TimeUnit.DAYS);
    }
}