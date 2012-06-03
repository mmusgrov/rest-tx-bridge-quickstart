package org.jboss.jbossts.resttxbridge.quickstart.jms;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.jbossts.resttxbridge.annotation.Transactional;

/**
 * 
 * @author Gytis Trikleris
 * 
 */
@Path("/")
public class QueueResource {

    @Resource(mappedName = "java:/JmsXA")
    private XAConnectionFactory xaConnectionFactory;

    @Resource(mappedName = "java:/queue/resttx")
    private Queue queue;

    @POST
    @Transactional
    public Response sendNotification(@QueryParam("message") String message) throws Exception {
        XAConnection connection = null;
        XASession session = null;

        try {
            connection = xaConnectionFactory.createXAConnection();
            session = connection.createXASession();
            MessageProducer messageProducer = session.createProducer(queue);

            connection.start();
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(message);

            messageProducer.send(textMessage);
            messageProducer.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    session.close();
                } catch (JMSException e) {
                    System.out.println("Error closing JMS connection: " + e.getMessage());
                }
            }
        }

        return Response.ok().build();
    }

    @GET
    @Transactional
    public String getMessage() throws JMSException {
        XAConnection connection = xaConnectionFactory.createXAConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(queue);
        connection.start();

        TextMessage message = (TextMessage) consumer.receive(5000);

        connection.close();
        session.close();

        if (message != null)
            return message.getText();

        return null;
    }

}
