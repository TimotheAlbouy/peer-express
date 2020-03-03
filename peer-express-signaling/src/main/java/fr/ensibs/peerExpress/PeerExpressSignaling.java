package fr.ensibs.peerExpress;

import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;

/**
 * A web service allowing the signaling phase before the peer-to-peer communication between users.
 */
@WebService(name = "PeerExpressSignaling")
public interface PeerExpressSignaling {

    /**
     * Register a new user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @param host the host address of the local JORAM server of the user
     * @param port the opened port of the local JORAM server of the user
     * @return true if and only if the user was registered successfully
     */
    boolean registerUser(String username, String host, int port);

    /**
     * Unregister an user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @return true if and only if the user was unregistered successfully
     */
    boolean unregisterUser(String username);

    /**
     * Get all the registered users of the signaling rendezvous point.
     * @param message the message to be delivered
     * @return a unique id that identifies the message if it has been delivered
     * @throws Exception if an error occurs while accessing the broker or
     * delivering the message
     */
    ArrayList<> getRegisteredUsers(NotificationMessage message) throws Exception;

}
