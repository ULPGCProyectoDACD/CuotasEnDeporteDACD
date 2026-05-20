package org.ulpgc.dacd.control.publisher;

import javax.jms.JMSException;

public interface MatchPublisher extends AutoCloseable {
    void publish(String topicName, String json) throws JMSException;
}