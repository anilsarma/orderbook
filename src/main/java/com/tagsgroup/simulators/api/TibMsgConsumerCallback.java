package com.tagsgroup.simulators.api;

public interface TibMsgConsumerCallback {
    void onMessage(String durableid, String topicName, String message);
}
