
package com.tagsgroup.simulators.fix.conn;

import com.tagsgroup.simulators.fix.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.HashMap;

public class TibjmsMsgTopicProducer {
    private static Logger logger = LoggerFactory.getLogger(TibjmsMsgTopicProducer.class);
    String serverUrl;
    String userName;
    String password;

    Connection connection = null;
    Session session = null;
    MessageProducer msgProducer = null;

    public TibjmsMsgTopicProducer(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
        createSession();
        //quick#ix.Session.sendToTarget()
    }

    private void createSession() {

        ConnectionFactory factory = null; // need enterpise package for this new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);

        if(factory==null) {
            System.out.println("factory is not *** implemented ** TibjmsMsgTopicProducer");
            return;
        }

        try {
            connection = factory.createConnection(userName, password);
            logger.info("createConnection {} {}", factory, connection);

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            logger.info("createSession {}", session);
            msgProducer = session.createProducer(null);
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    public void stop() {
        Utils.ignore_exception(() -> session.close());
        Utils.ignore_exception(() -> msgProducer.close());
        Utils.ignore_exception(() -> connection.close());
    }

    HashMap<String, Destination> destinations = new HashMap<>();

    public Message sendTopicMessage(String topicName, String messageStr) throws Exception {

            /* create the destination */
        Destination destination = destinations.computeIfAbsent(topicName, (x) -> {
            try {
                return session.createTopic(topicName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
            /* create the producer */
            /* publish messages */
            /* create text message */

        TextMessage msg = session.createTextMessage();

            /* set message text */
        msg.setText(messageStr);

            /* publish message */
        msgProducer.send(destination, msg);

        System.out.println("Published message: " + messageStr);

            /* close the connection */
// connection.close();

        return msg;

    }



}