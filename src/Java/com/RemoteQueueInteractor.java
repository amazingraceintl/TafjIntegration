package Java.com;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class RemoteQueueInteractor {

    private ConnectionFactory remoteQueueCF;
    private Queue remoteQueue;
    private Connection remoteQueueConnection;
    private Session remoteQueueSession;

    public RemoteQueueInteractor() throws NamingException, JMSException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.jboss.naming.remote.client.InitialContextFactory");

        /*
         * The URL below should point to the your instance of Server 1, if no
         * port offset is used for Server 1 the port can remain at 4447
         */
        props.put(Context.PROVIDER_URL, "remote://localhost:5447");

        /*
         * Please note that the credentials passed in here have no effect on the
         * messaging system as we have disabled the security on the HornetQ
         * messaging subsystem
         */
        props.put(Context.SECURITY_PRINCIPAL, "USERNAME");
        props.put(Context.SECURITY_CREDENTIALS, "PASSWORD");

        InitialContext ic = new InitialContext(props);

        /*
         * The following two lookups are based on how you configured the
         * RemoteConnectionFactory and the local queue on Server 1. If you have
         * followed the installation that was provided as-is then you can go
         * with the below code
         */
        remoteQueueCF = (ConnectionFactory) ic.lookup("jms/RemoteConnectionFactory");
        remoteQueue = (Queue) ic.lookup("jms/queues/t24OFSQueue");

        remoteQueueConnection = remoteQueueCF.createConnection("USERNAME","PASSWORD");
        remoteQueueConnection.start();
        remoteQueueSession = remoteQueueConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
    }

    public void sendTextMessage(String msg) throws JMSException {
        TextMessage txtMessage = remoteQueueSession.createTextMessage(msg);
        MessageProducer msgProducer = remoteQueueSession.createProducer(this.remoteQueue);
        msgProducer.send(txtMessage);
        msgProducer.close();
    }

    public String receiveTextMessage() throws JMSException {
        MessageConsumer msgConsumer = remoteQueueSession.createConsumer(this.remoteQueue);
        TextMessage txtMsg = (TextMessage) msgConsumer.receive();
        msgConsumer.close();
        return txtMsg.getText();
    }

    @Override
    protected void finalize() throws Throwable {
        remoteQueueSession.close();
        remoteQueueConnection.close();
    }

    public static void main(String agrs[]) throws NamingException, JMSException {
        RemoteQueueInteractor remoteQInteractor = new RemoteQueueInteractor();
        remoteQInteractor.sendTextMessage("Hello World!");
        System.out.println(remoteQInteractor.receiveTextMessage());
    }
}
