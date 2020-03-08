package fr.ensibs.peerExpress;

import javax.xml.ws.Endpoint;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Web service publisher of the Peer Express signaling server.
 */
public class PeerExpressSignalingPublisher {

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
        if (args.length != 1 || "-h".equals(args[0]))
            usage();

        try {
            int port = Integer.parseInt(args[0]);
            new PeerExpressSignalingPublisher(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred.");
        }
    }

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java PeerExpressSignalingPublisher <port>");
        System.out.println("Launch a notification broker server");
        System.exit(-1);
    }

    /**
     * Constructor.
     * @param port the port number where the service is published
     * @throws Exception when an error occurred.
     */
    public PeerExpressSignalingPublisher(int port) throws Exception {
        String host = InetAddress.getLocalHost().getHostName();
        PeerExpressSignaling signaling = new PeerExpressSignalingImpl();
        String address = "http://" + host + ":" + port + "/ws/" + SERVICE;
        Endpoint.publish(address, signaling);
        System.out.println("Web service published and running at: " + address);
    }

}
