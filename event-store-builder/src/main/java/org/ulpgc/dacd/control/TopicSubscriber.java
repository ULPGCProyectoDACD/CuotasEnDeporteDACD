package org.ulpgc.dacd.control;

public interface TopicSubscriber extends AutoCloseable {
    void start(String[] topics);
}
