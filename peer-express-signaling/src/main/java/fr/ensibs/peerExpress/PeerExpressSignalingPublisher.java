package fr.ensibs.peerExpress;

import javax.xml.ws.Endpoint;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
        } catch (NumberFormatException e) {
            System.err.println("The port is not a number: " + args[0]);
            usage();
        } catch (UnknownHostException e) {
            System.err.println("The local hostname is not found");
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
     * @throws UnknownHostException if the local hostname is not found
     */
    public PeerExpressSignalingPublisher(int port) throws UnknownHostException {
        String host = InetAddress.getLocalHost().getHostName();
        PeerExpressSignaling signaling = new PeerExpressSignalingImpl();
        String address = "http://" + host + ":" + port + "/ws/" + SERVICE;
        Endpoint.publish(address, signaling);
        System.out.println("Web service published and running at: " + address);
    }

}
