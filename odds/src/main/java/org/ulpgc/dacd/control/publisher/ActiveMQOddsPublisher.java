package org.ulpgc.dacd.control.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQOddsPublisher implements OddsPublisher {

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public ActiveMQOddsPublisher(String brokerUrl) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.start();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producer = session.createProducer(null);
    }

    @Override
    public void publish(String topicName, String json) throws JMSException {
        Topic topic = session.createTopic(topicName);
        TextMessage message = session.createTextMessage(json);
        producer.send(topic, message);
    }

    @Override
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Error cerrando la conexión con ActiveMQ: " + e.getMessage());
        }
    }
}