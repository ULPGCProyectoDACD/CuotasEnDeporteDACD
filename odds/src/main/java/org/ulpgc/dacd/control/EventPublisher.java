package org.ulpgc.dacd.control;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class EventPublisher implements AutoCloseable {

    private final Connection connection;
    private final Session session;

    public EventPublisher(String brokerUrl) throws JMSException {
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.start();
        this.session = connection.createSession(
                false, Session.AUTO_ACKNOWLEDGE);
    }

    public void publish(String topicName, String json)
            throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageProducer producer = session.createProducer(topic);
        producer.send(session.createTextMessage(json));
    }

    @Override
    public void close() throws JMSException {
        session.close();
        connection.close();
    }
}