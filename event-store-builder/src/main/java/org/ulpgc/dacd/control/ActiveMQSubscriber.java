package org.ulpgc.dacd.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQSubscriber implements TopicSubscriber {

    private static final String CLIENT_ID = "event-store-builder";
    private static final long INITIAL_RECONNECT_DELAY_MS = 5000;
    private static final long MAX_RECONNECT_DELAY_MS = 60000;

    private final String brokerUrl;
    private final EventStoreWriter writer;
    private Connection connection;

    public ActiveMQSubscriber(String brokerUrl, EventStoreWriter writer) {
        this.brokerUrl = brokerUrl;
        this.writer = writer;
    }

    @Override
    public void start(String[] topics) {
        long reconnectDelay = INITIAL_RECONNECT_DELAY_MS;

        while (true) {
            try {
                connect(topics);
                System.out.println("[ESB] Conexión establecida.");
                reconnectDelay = INITIAL_RECONNECT_DELAY_MS;
                Thread.currentThread().join();
            } catch (JMSException e) {
                System.err.println("[ESB] Error de conexión con ActiveMQ: " + e.getMessage());
                closeQuietly();
                System.out.println("[ESB] Reintentando conexión en " + (reconnectDelay / 1000) + "s...");
                sleep(reconnectDelay);
                reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS);
            } catch (InterruptedException e) {
                System.out.println("[ESB] Suscriptor interrumpido.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void connect(String[] topics) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.setClientID(CLIENT_ID);
        this.connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        for (String topicName : topics) {
            Topic topic = session.createTopic(topicName);
            String subscriberName = CLIENT_ID + "-" + topicName;
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberName);
            consumer.setMessageListener(createListener(topicName));
            System.out.println("[ESB] Suscrito de forma durable al topic: " + topicName);
        }
    }

    private MessageListener createListener(String topicName) {
        return message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    String json = textMessage.getText();
                    System.out.println("[ESB] Evento recibido en topic '" + topicName + "'");
                    writer.save(topicName, json);
                }
            } catch (Exception e) {
                System.err.println("[ESB] Error procesando evento del topic '" + topicName + "': " + e.getMessage());
            }
        };
    }

    private void closeQuietly() {
        try {
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            System.err.println("[ESB] Error cerrando conexión: " + e.getMessage());
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        closeQuietly();
        System.out.println("[ESB] Suscriptor cerrado.");
    }
}
