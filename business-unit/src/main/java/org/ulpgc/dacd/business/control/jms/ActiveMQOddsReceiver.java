package org.ulpgc.dacd.business.control.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.function.Consumer;

public class ActiveMQOddsReceiver implements OddsReceiver {
    private final String brokerUrl;
    private final String topicName;
    private final Consumer<String> messageProcessor;
    private Connection connection;
    private Session session;

    public ActiveMQOddsReceiver(String brokerUrl, String topicName, Consumer<String> messageProcessor) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void start() {
        try {
            connectToBroker();
            subscribeToTopic();
            System.out.println("🎧 [Business Unit] Conectado a ActiveMQ. Escuchando cuotas en: " + topicName);
        } catch (JMSException e) {
            System.err.println("❌ Error conectando a ActiveMQ: " + e.getMessage());
        }
    }


    private void connectToBroker() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }


    private void subscribeToTopic() throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    System.out.println("🔔 [DEBUG-RECEIVER] ¡ActiveMQ acaba de escupir un mensaje!");
                    messageProcessor.accept(textMessage.getText());
                } catch (JMSException e) {
                    System.err.println("❌ Error leyendo el mensaje: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void close() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("🔌 Desconectado de ActiveMQ.");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}