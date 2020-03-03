package fr.ensibs.peerExpress;

import javax.xml.ws.Endpoint;
import java.util.Scanner;

/**
 * Web service publisher of the Peer Express signaling server.
 */
public class PeerExpressSignalingPublisher {

    /**
     * the broker that manages the registrations and messages
     */
    private final NotificationBroker broker;

    /**
     * the service name
     */
    private final static String SERVICE = "NotificationBroker";

    /**
     * the prompt message displayed before asking for user commands
     */
    private static final String PROMPT = "Enter TOPICS, REGISTER <path>, UNREGISTER <path> commands to manage the topics";

    /**
     * The point of entry of the publisher.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2 || "-h".equals(args[0]))
            usage();

        try {
            String hostName = args[0];
            int portNumber = Integer.parseInt(args[1]);
            new PeerExpressSignalingPublisher(hostName, portNumber);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred.");
        }
    }

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java NotificationAppLauncher <host> <port>");
        System.out.println("Launch a notification broker server");
        System.exit(-1);
    }

    /**
     * Constructor.
     *
     * @param hostName the host on which the service is published
     * @param portNumber the port number where the service is published
     * @throws Exception when an error occurred.
     */
    public PeerExpressSignalingPublisher(String hostName, int portNumber) throws Exception {
        this.broker = new NotificationBrokerImpl();
        String address = "http://" + hostName + ":" + portNumber + "/ws/" + SERVICE;
        Endpoint.publish(address, broker);
        System.out.println("Web service published and running at: " + address);
        run();
    }

    /**
     * Ask the user to enter commands to add or remove topics and process the commands.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println(PROMPT);
            String line = scanner.nextLine();
            boolean processed = process(line);
            if (!processed) {
                System.err.println("Unable to process command: " + line);
            }
        }
    }

    /**
     * Process a user command
     *
     * @param line the line that contains the user command
     */
    private boolean process(String line) {
        String[] tokens = line.split(" +");
        if (tokens.length >= 2) {
            switch (tokens[0]) {
                case "register":
                case "REGISTER":
                    register(tokens[1]);
                    return true;
                case "unregister":
                case "UNREGISTER":
                    unregister(tokens[1]);
                    return true;
            }
        } else if (tokens.length == 1) {
            switch (tokens[0]) {
                case "topics":
                case "TOPICS":
                    topics();
                    return true;
            }
        }
        return false;
    }

    /**
     * Displays the topics available on the topics space
     */
    protected void topics() {
        try {
            StringBuilder builder = new StringBuilder();
            for (TopicPath path : broker.getPaths()) {
                builder.append("\n").append(path);
            }
            System.out.println(builder.toString());
        } catch (Exception e) {
            System.err.println("Error while getting the topics space: " + e.getClass().getName() + ". " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register a new topic represented by the given path expression.
     *
     * @param path a concrete path expression
     */
    private void register(String path) {
        try {
            TopicPath topicPath = new TopicPath(path);
            if (topicPath.isConcrete()) {
                Topic topic = new Topic(topicPath);
                broker.getTopicSpace().addTopic(topic);
                System.out.println("Topic successfully registered.");
            } else {
                System.out.println("The path must not contain wildcards.");
            }
        } catch (Exception e) {
            System.err.println("Error while registering the topic: " + e.getClass().getName() + ". " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Unregister the topic represented by the given path expression.
     *
     * @param path a concrete path expression
     */
    private void unregister(String path) {
        try {
            TopicPath topicPath = new TopicPath(path);
            if (topicPath.isConcrete()) {
                Topic topic = broker.getTopicSpace().getTopic(topicPath);
                broker.getTopicSpace().removeTopic(topic);
                System.out.println("Topic successfully unregistered.");
                System.out.println("If there were descendant topics, they were unregistered as well.");
            } else {
                System.err.println("The path must not contain wildcards.");
            }
        } catch (Exception e) {
            System.err.println("Error while unregistering the topic: " + e.getClass().getName() + ". " + e.getMessage());
            e.printStackTrace();
        }
    }

}
