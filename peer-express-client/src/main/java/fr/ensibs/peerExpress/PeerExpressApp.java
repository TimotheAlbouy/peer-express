package fr.ensibs.peerExpress;

import fr.ensibs.joram.Joram;
import fr.ensibs.joram.JoramAdmin;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
* The entry point for the PeerExpress messaging application that allows to
* send messages in a peer-to-peer manner to other users.
*/
public class PeerExpressApp {

    /**
     * the username in the community
     */
    private String username;

    /**
     * the registration token of the user
     */
    private String token;

    /**
     * the info on the users identified by their username
     */
    private HashMap<String, UserInfo> usersInfo = new HashMap<>();

    /**
     * the signaling server
     */
    private PeerExpressSignaling signaling;

    /**
     * the user interface of the app
     */
    private UserInterface userInterface;

    /**
     * the destination name for the application
     */
    private static String DEST = "PEEREXPRESS";

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java PeerExpressApp <username> <port> <config path>?");
        System.out.println("Launch the PeerExpress client application, with:");
        System.out.println("<username>      the username in the community");
        System.out.println("<port>          the opened port of the local JORAM server");
        System.out.println("<config path>   (optional) the path of the config directory of the JORAM server");
        System.out.println("             use different paths if you want to run multiple clients on the same machine");
        System.exit(0);
    }

    /**
     * Application entry point.
     * @param args see usage
     */
    public static void main(String[] args) {
        if (args.length < 2)
            usage();

        try {
            String username = args[0];
            int port = Integer.parseInt(args[1]);

            File configDirectory = null;
            if (args.length >= 3)
                configDirectory = new File(args[2]);

            boolean consoleMode = false;
            if (args.length >= 4 && "--console".equals(args[3]))
                consoleMode = true;

            PeerExpressApp instance = new PeerExpressApp(username, port, configDirectory, consoleMode);
            instance.run();
        } catch (NumberFormatException e) {
            System.err.println("The port is not a number: " + args[1]);
            usage();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Constructor.
     * @param username the username in the community
     * @param port the opened port of the local JORAM server
     * @param configDirectory the JORAM configuration directory, if set to null, use $USER_HOME/.joram
     * @param consoleMode if set to true, the app is in console mode, otherwise the graphic user interface is used
     */
    public PeerExpressApp(String username, int port, File configDirectory, boolean consoleMode) throws Exception {
        this.username = username;
        String host = InetAddress.getLocalHost().getHostName();

        // create the user interface
        this.userInterface = consoleMode ? new ConsoleUserInterface(this) : new ConsoleUserInterface(this);

        // set up the local JORAM server
        Joram joram;
        if (configDirectory != null)
            joram = new Joram(port, configDirectory);
        else joram = new Joram(port);
        joram.run();

        // create a queue
        JoramAdmin joramAdmin = new JoramAdmin(host, port);
        joramAdmin.createQueue(DEST);

        // create the consumer session to the local JORAM server
        Connection localConnection = this.createConnection(host, port);
        Destination destination = this.getDestination(host, port);
        Session consumerSession = localConnection.createSession();
        MessageConsumer consumer = consumerSession.createConsumer(destination);
        consumer.setMessageListener(message -> {
            try {
                String sender = message.getStringProperty("sender");
                String content = ((TextMessage) message).getText();
                this.userInterface.notifyMessageReceived(sender, content);
            } catch (JMSException e) {
                System.err.println(e.getMessage());
            }
        });
        localConnection.start();

        // register the user in the SOAP signaling server
        PeerExpressSignaling_Service service = new PeerExpressSignaling_Service();
        this.signaling = service.getPeerExpressSignalingPort();
        this.token = this.signaling.registerUser(username, host, port);

        // create the list containing info on the other users
        List<User> registeredUsers = this.signaling.getRegisteredUsers();
        for (User user : registeredUsers) {
            UserInfo info = new UserInfo(user, null, null);
            this.usersInfo.put(user.getUsername(), info);
        }

        // start fetching newly registered users and deregistered users
        this.startLongPollingRegistration();
        this.startLongPollingDeregistration();
    }

    /**
     * Get the username of the user of the app.
     * @return the username of the user
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Launch the user interface process that retrieves user commands.
     */
    public void run() {
        this.userInterface.run();
    }

    /**
     * Send a message to an user.
     * @param username the username of the user
     * @param message the message to send to the user
     */
    public void send(String username, String message) {
        try {
            UserInfo info = this.usersInfo.get(username);
            if (!info.isSessionEstablished())
                this.establishUserSession(username);

            Session session = info.getSession();
            MessageProducer producer = info.getProducer();
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setStringProperty("sender", this.username);
            producer.send(textMessage);
        } catch (JMSException | NamingException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Show the registered users.
     */
    public void showUsers() {
        List<User> users = new ArrayList<>();
        for (UserInfo info : this.usersInfo.values())
            users.add(info.getUser());
        this.userInterface.showUsers(users);
    }

    /**
     * Stop the application
     */
    public void quit() {
        try {
            this.signaling.unregisterUser(this.username, this.token);
        } catch (PeerExpressSignalingHTTP_Exception e) {
            System.err.println(e.getMessage());
        }
        System.exit(0);
    }

    /**
     * Establish a session with a JORAM server and update the UserInfo object
     * @param username the name of the user to establish a session with
     * @throws NamingException if an error occurred
     * @throws JMSException if an error occurred
     */
    private void establishUserSession(String username) throws NamingException, JMSException {
        UserInfo info = this.usersInfo.get(username);
        User user = info.getUser();
        String host = user.getHost();
        int port = user.getPort();
        Connection connection = this.createConnection(host, port);
        Destination destination = this.getDestination(host, port);

        Session session = connection.createSession();
        MessageProducer producer = session.createProducer(destination);
        info.setSession(session);
        info.setProducer(producer);
        connection.start();
    }

    /**
     * Set the needed JNDI properties.
     * @param host the host of the JNDI service
     * @param port the port of the JNDI service
     */
    private void setJNDIProps(String host, int port) {
        System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
        System.setProperty("java.naming.factory.host", host);
        System.setProperty("java.naming.factory.port", Integer.toString(port));
    }

    /**
     * Create a connection to a JORAM server
     * @param host the host of the JNDI service
     * @param port the port of the JNDI service
     * @return the connection instance
     * @throws NamingException if an error occurred
     * @throws JMSException if an error occurred
     */
    private Connection createConnection(String host, int port) throws NamingException, JMSException {
        this.setJNDIProps(host, port);
        Context context = new InitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
        return factory.createConnection();
    }

    /**
     * Get the destination of a JORAM server
     * @param host the host of the JNDI service
     * @param port the port of the JNDI service
     * @return the destination instance
     * @throws NamingException if an error occurred
     */
    private Destination getDestination(String host, int port) throws NamingException {
        this.setJNDIProps(host, port);
        Context context = new InitialContext();
        return (Destination) context.lookup(DEST);
    }

    /**
     * Start the long polling to fetch newly registered users.
     */
    private void startLongPollingRegistration() {
        AsyncHandler<TakeNewUserRegistrationResponse> handler = new AsyncHandler<TakeNewUserRegistrationResponse>() {
            @Override
            public void handleResponse(Response<TakeNewUserRegistrationResponse> response) {
                try {
                    User user = response.get().getReturn();
                    UserInfo info = new UserInfo(user, null, null);
                    usersInfo.put(user.getUsername(), info);
                    userInterface.notifyNewUserRegistration(user.getUsername());
                    signaling.takeNewUserRegistrationAsync(username, token, this);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        this.signaling.takeNewUserRegistrationAsync(this.username, this.token, handler);
    }

    /**
     * Start the long polling to fetch deregistered users.
     */
    private void startLongPollingDeregistration() {
        AsyncHandler<TakeNewUserDeregistrationResponse> handler = new AsyncHandler<TakeNewUserDeregistrationResponse>() {
            @Override
            public void handleResponse(Response<TakeNewUserDeregistrationResponse> response) {
                try {
                    User user = response.get().getReturn();
                    usersInfo.remove(user.getUsername());
                    userInterface.notifyNewUserDeregistration(user.getUsername());
                    signaling.takeNewUserDeregistrationAsync(username, token, this);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        this.signaling.takeNewUserDeregistrationAsync(this.username, this.token, handler);
    }

}
