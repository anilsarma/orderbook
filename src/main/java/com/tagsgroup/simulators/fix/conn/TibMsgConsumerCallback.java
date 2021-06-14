package com.tagsgroup.simulators.fix.conn;

import javax.jms.Message;

public interface TibMsgConsumerCallback {
    void onMessage(String durableId, String topicName, Message message);
}
