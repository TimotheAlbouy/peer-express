package fr.ensibs.peerExpress;

import java.util.ArrayList;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * A web service allowing the signaling phase before the peer-to-peer
 * communication between users.
 */
@WebService(name = "PeerExpressSignaling", targetNamespace = "http://bakery.ensibs.fr")
public interface PeerExpressSignaling {

    /**
     * Register a new user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @param host the host address of the local JORAM server of the user
     * @param port the opened port of the local JORAM server of the user
     * @return the registration id if the user was successfully registered, null otherwise
     */
    @WebMethod(operationName = "registerUser")
    String registerUser(@WebParam(name = "username") String username,
                        @WebParam(name = "host") String host,
                        @WebParam(name = "port") int port)
            throws PeerExpressSignalingHTTP;

    /**
     * Unregister an user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @param registrationId the registration id of the user
     * @return true if the user was successfully unregistered, false otherwise
     */
    @WebMethod(operationName = "unregisterUser")
    void unregisterUser(@WebParam(name = "username") String username,
                        @WebParam(name = "registrationId") String registrationId)
            throws PeerExpressSignalingHTTP;

    /**
     * Get all the registered users of the signaling rendezvous point.
     * @return the list of registered users
     */
    @WebMethod(operationName = "getRegisteredUsers")
    ArrayList<User> getRegisteredUsers();

}
