package fr.ensibs.peerExpress;

import fr.ensibs.joram.Joram;
import fr.ensibs.joram.JoramAdmin;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
     * the registration id of the user
     */
    private String registrationId;

    /**
     * the info on the users identified by their username
     */
    private HashMap<String, UserInfo> usersInfo = new HashMap<>();

    /**
     * the signaling server
     */
    private PeerExpressSignaling signaling;

    /**
     * the destination name for the application
     */
    private static String DEST = "PEEREXPRESS";

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java PeerExpressApp <username> <port>");
        System.out.println("Launch the PeerExpress client application, with:");
        System.out.println("<username>  the username in the community");
        System.out.println("<port>      the opened port of the local JORAM server");
        System.exit(0);
    }

    /**
     * Application entry point.
     * @param args see usage
     */
    public static void main(String[] args) {
        if (args.length != 2)
            usage();

        try {
            String username = args[0];
            int port = Integer.parseInt(args[1]);
            PeerExpressApp instance = new PeerExpressApp(username, port);
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
     */
    public PeerExpressApp(String username, int port) throws Exception {
        this.username = username;
        String host = InetAddress.getLocalHost().getHostName();

        // set up the local JORAM server
        Joram joram = new Joram(port);
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
                System.out.println("[" + sender + "]: " + content);
            } catch (JMSException e) {
                System.err.println(e.getMessage());
            }
        });

        // register the user in the SOAP signaling server
        PeerExpressSignaling_Service service = new PeerExpressSignaling_Service();
        this.signaling = service.getPeerExpressSignalingPort();
        this.registrationId = this.signaling.registerUser(username, host, port);

        // create the list containing info on the other users
        List<User> registeredUsers = this.signaling.getRegisteredUsers();
        for (User user : registeredUsers) {
            UserInfo info = new UserInfo(user, null, null);
            this.usersInfo.put(user.getUsername(), info);
        }
    }

    /**
     * Launch the application process that executes user commands: SHARE, FILTER
     */
    public void run() {
        System.out.println("Hello, " + this.username + ". Enter commands:"
        + "\n QUIT                       to quit the application"
        + "\n USERS                      to display the list of registered users"
        + "\n SEND <username> <message>  to send a message to an user");

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (true) {
            String[] tokens = line.split(" +");
            switch (tokens[0]) {
            case "send":
            case "SEND":
                if (tokens.length >= 2) {
                    String username = tokens[1];
                    String message = line.replaceFirst("send", "")
                                         .replaceFirst(username, "")
                                         .trim();
                    this.send(username, message);
                } else {
                    System.err.println("Usage: send <username> <message>");
                }
                break;
            case "users":
            case "USERS":
                this.showUsers();
                break;
            case "quit":
            case "QUIT":
                this.quit();
            default:
                System.err.println("Unknown command: \"" + tokens[0] + "\"");
            }
            line = scanner.nextLine();
        }
    }

    /**
     * Send a message to an user.
     * @param username the username of the user
     * @param message the message to send to the user
     */
    public void send(String username, String message) {
        try {
            UserInfo info = this.usersInfo.get(username);
            if (info.isSessionEstablished())
                this.establishUserSession(username);

            Session session = info.getSession();
            MessageProducer producer = info.getProducer();
            TextMessage textMessage = session.createTextMessage(message);
            producer.send(textMessage);
        } catch (JMSException | NamingException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Show the registered users.
     */
    public void showUsers() {
        for (UserInfo info : this.usersInfo.values()) {
            User user = info.getUser();
            System.out.println(user.getUsername() + " => " + user.getHost() + ":" + user.getPort());
        }
    }

    /**
     * Stop the application
     */
    public void quit() {
        try {
            this.signaling.unregisterUser(this.username, this.registrationId);
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
    public void establishUserSession(String username) throws NamingException, JMSException {
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

}
