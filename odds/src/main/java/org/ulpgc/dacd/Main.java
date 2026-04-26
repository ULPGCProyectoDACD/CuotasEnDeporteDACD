package org.ulpgc.dacd;

import jakarta.jms.JMSException;
import org.ulpgc.dacd.control.EventPublisher;
import org.ulpgc.dacd.control.OddsController;
import org.ulpgc.dacd.control.feeder.OddsApiFeeder;
import org.ulpgc.dacd.control.feeder.OddsFeeder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws JMSException {

        if (args.length == 0) {
            System.err.println("ERROR: No se ha proporcionado la API Key.");
            System.err.println("Uso correcto: java -jar proyecto.jar <TU_API_KEY>");
            return;
        }

        String apiKey = args[0];

        OddsFeeder feeder = new OddsApiFeeder(apiKey);
        EventPublisher publisher = new EventPublisher("tcp://localhost:61616");
        OddsController controller = new OddsController(feeder, publisher);

        System.out.println("Iniciando servicio de captura de datos de Cuotas...");
        System.out.println("El programa se ejecutará ahora mismo y se repetirá cada 24 horas.");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(controller::execute, 0, 1, TimeUnit.DAYS);
    }
}