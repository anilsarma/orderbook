package com.tagsgroup.simulators.api;

public interface TibmsMsgTopicComsumer {
    void createSession();

    void start();
    void stop();

    void addTopicListener(String durableid, String topicName, TibMsgConsumerCallback callback);
}
