package com.tagsgroup.simulators.api;

public interface TibmsMsgTopicProducer {
      void createSession();
     Object sendTopicMessage(String topicName, String message);

}
