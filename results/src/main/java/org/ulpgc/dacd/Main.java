package org.ulpgc.dacd;

import javax.jms.JMSException;
import org.ulpgc.dacd.control.publisher.ActiveMQMatchPublisher;
import org.ulpgc.dacd.control.publisher.MatchPublisher;
import org.ulpgc.dacd.control.MatchController;
import org.ulpgc.dacd.control.feeder.FootballDataOrgFeeder;
import org.ulpgc.dacd.control.feeder.MatchFeeder;

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

        MatchFeeder feeder = new FootballDataOrgFeeder(apiKey);
        MatchPublisher publisher = new ActiveMQMatchPublisher("tcp://localhost:61616");
        MatchController controller = new MatchController(feeder, publisher);

        System.out.println("Iniciando servicio de captura de datos de Fútbol...");
        System.out.println("El programa se ejecutará ahora mismo y se repetirá cada 24 horas.");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(controller::execute, 0, 1, TimeUnit.DAYS);
    }
}