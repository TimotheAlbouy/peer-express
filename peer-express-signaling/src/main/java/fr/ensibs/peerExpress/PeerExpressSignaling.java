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
     * @throws PeerExpressSignalingHTTP if an error occurred
     */
    @WebMethod(operationName = "registerUser")
    String registerUser(
            @WebParam(name = "username", partName = "username") String username,
            @WebParam(name = "host", partName = "host") String host,
            @WebParam(name = "port", partName = "port") int port)
            throws PeerExpressSignalingHTTP
    ;

    /**
     * Unregister an user in the signaling rendezvous point.
     * @param username the username of the user in the community
     * @param token the registration token of the user
     * @throws PeerExpressSignalingHTTP if an error occurred
     */
    @WebMethod(operationName = "unregisterUser")
    void unregisterUser(
            @WebParam(name = "username", partName = "username") String username,
            @WebParam(name = "token", partName = "token") String token)
            throws PeerExpressSignalingHTTP
    ;

    /**
     * Get all the registered users of the signaling rendezvous point.
     * @return the list of registered users
     */
    @WebMethod(operationName = "getRegisteredUsers")
    ArrayList<User> getRegisteredUsers();

    /**
     * Block the execution until a new user has registered, and return it.
     * @param username the username of the user in the community
     * @param token the registration token of the user
     * @return the last user that has registered
     * @throws PeerExpressSignalingHTTP if an error occurred
     */
    @WebMethod(operationName = "takeNewlyRegisteredUser")
    User takeNewlyRegisteredUser(
            @WebParam(name = "username", partName = "username") String username,
            @WebParam(name = "token", partName = "token") String token)
            throws PeerExpressSignalingHTTP
    ;

}
