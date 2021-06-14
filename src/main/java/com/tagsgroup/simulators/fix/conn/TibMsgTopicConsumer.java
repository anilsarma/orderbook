package com.tagsgroup.simulators.fix.conn;

import com.tagsgroup.simulators.fix.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

//import javax.jms.*;

public class TibMsgTopicConsumer {
    private static Logger logger = LoggerFactory.getLogger(TibMsgTopicConsumer.class);

    String serverUrl;
    String userName;
    String password;

    Connection connection = null;
    Session session = null;

    boolean dummyConnection = true;

    public TibMsgTopicConsumer(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;

        createSession();
    }

    private void createSession() {
        logger.info("create session {}", serverUrl);
        ConnectionFactory factory = null; // new TibjmsConnectionFactory(serverUrl);
        if(factory==null) {
            System.out.println("factory is not *** implemented ** TibMsgTopicConsumer");
            return;
        }
        try {
            connection = factory.createConnection(userName, password);
            /* create the session */
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    public void start() {
        if(dummyConnection) {
            return;
        }
        try {
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        logger.info("Stopping connection");

        Utils.ignore_exception(() -> session.close());
        Utils.ignore_exception(() -> connection.stop());

    }

    public void addTopicListener(String durableid, String topicName, TibMsgConsumerCallback callback) {
        if(dummyConnection) {
            return;
        }
        try {
            Topic destination = session.createTopic(topicName);
            logger.info("Subscribing to topic '" + topicName + " durable:" + durableid);
            /* create the consumer */
            MessageConsumer msgConsumer = session.createDurableSubscriber(destination, durableid);

            msgConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    callback.onMessage(durableid, topicName, message);
                }
            });

        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}