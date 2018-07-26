package Java.com;

import javax.jms.*;
import java.util.Properties;
import javax.naming.*;
import java.io.IOException;

public class TafjIntegratiion {
    private static final String hostname = "127.0.0.1";
    private static final String port = "5455";

    public static void main(String[] args) throws NamingException, JMSException {

        // Setup connection properties to get the initial context
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

        env.put(Context.PROVIDER_URL, "remote://"+hostname+":"+port);

        env.put(Context.SECURITY_PRINCIPAL, "bunmi");
        env.put(Context.SECURITY_CREDENTIALS, "jethro@123");
        env.put(Context.SECURITY_AUTHENTICATION, "amV0aHJvQDEyMw==");
        env.put("jboss.naming.client.ejb.context", true);

        Context initialContext = new InitialContext(env);
        // Lookup JMS objects

        ConnectionFactory jmsCF = (ConnectionFactory) initialContext.lookup("jms/RemoteConnectionFactory");
        //change fpr

        Destination queue = (Destination) initialContext.lookup("jms/queue/t24OFSQueue");
        Destination replyQueue = (Destination) initialContext.lookup("jms/queue/t24OFSReplyQueue");
        // Create connection and session
        Connection connection = jmsCF.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create producer
        MessageProducer outgoing = session.createProducer(queue);
        // Create message and set correlation ID
        String txn="FUNDS.TRANSFER,/I/////,AUTHOR/123456/,,TRANSACTION.TYPE::=AC,DEBIT.ACCT.NO::=69108,DEBIT.CURRENCY::=GBP,DEBIT.AMOUNT::=500,DEBIT.VALUE.DATE::=20140422,DEBIT.THEIR.REF::=TESTPAY,CREDIT.ACCT.NO::=69407";
        TextMessage request = session.createTextMessage(txn);
        request.setJMSCorrelationID("1987");
        outgoing.send(request);
        System.out.println(request.getJMSMessageID()+" and "+request.getJMSCorrelationID());
        // Create consumer for the specified correlation ID and receive response
        MessageConsumer incoming = session.createConsumer(replyQueue, "JMSCorrelationID= '" + request.getJMSCorrelationID() + "'");
        // Wait 3s max. for the message response.
        Message message = incoming.receive(8000);

        String response = ((TextMessage) message).getText();
        System.out.println(response);

        session.close();
        connection.close();
    }

}
