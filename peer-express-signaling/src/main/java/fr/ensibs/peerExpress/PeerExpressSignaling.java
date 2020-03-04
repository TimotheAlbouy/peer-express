package fr.ensibs.peerExpress;

import java.util.ArrayList;
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
     * @return the registration id if the user was successfully registered, null otherwise
     */
    String registerUser(String username, String host, int port);

    /**
     * Unregister an user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @param registrationId the registration id of the user
     * @return true if the user was successfully unregistered, false otherwise
     */
    boolean unregisterUser(String username, String registrationId);

    /**
     * Get all the registered users of the signaling rendezvous point.
     * @return the list of registered users
     */
    ArrayList<User> getRegisteredUsers();

}
