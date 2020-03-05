package fr.ensibs.peerExpress;

import javax.xml.ws.Endpoint;
import java.util.Scanner;

/**
 * Web service publisher of the Peer Express signaling server.
 */
public class PeerExpressSignalingPublisher {

    /**
     * the signaling server that manages user registrations
     */
    private final PeerExpressSignaling signaling;

    /**
     * the service name
     */
    private final static String SERVICE = "PeerExpressSignaling";

    /**
     * The point of entry of the publisher.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2 || "-h".equals(args[0]))
            usage();

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            new PeerExpressSignalingPublisher(host, port);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred.");
        }
    }

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java PeerExpressSignalingPublisher <host> <port>");
        System.out.println("Launch a notification broker server");
        System.exit(-1);
    }

    /**
     * Constructor.
     * @param host the host on which the service is published
     * @param port the port number where the service is published
     * @throws Exception when an error occurred.
     */
    public PeerExpressSignalingPublisher(String host, int port) throws Exception {
        this.signaling = new PeerExpressSignalingImpl();
        String address = "http://" + host + ":" + port + "/ws/" + SERVICE;
        Endpoint.publish(address, this.signaling);
        System.out.println("Web service published and running at: " + address);
    }

}
