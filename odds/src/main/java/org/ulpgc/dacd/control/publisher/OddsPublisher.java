package org.ulpgc.dacd.control.publisher;

import jakarta.jms.JMSException;

public interface OddsPublisher extends AutoCloseable {
    void publish(String topicName, String json) throws JMSException;
}