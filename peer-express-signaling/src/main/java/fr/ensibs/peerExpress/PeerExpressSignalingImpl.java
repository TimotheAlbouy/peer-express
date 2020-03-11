package fr.ensibs.peerExpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import javax.jws.WebService;

/**
 * A web service allowing the signaling phase before the peer-to-peer
 * communication between users.
 */
@WebService(endpointInterface = "fr.ensibs.peerExpress.PeerExpressSignaling",
        serviceName = "PeerExpressSignaling",
        portName = "PeerExpressSignalingPort")
public class PeerExpressSignalingImpl implements PeerExpressSignaling {

    /**
     * the list of registered users
     */
    private HashMap<String, User> registeredUsers = new HashMap<>();

    @Override
    public String registerUser(String username, String host, int port) throws PeerExpressSignalingHTTP {
        if (username == null || host == null)
            throw new PeerExpressSignalingHTTP(400, "The parameters must be specified");

        if (port < 0 || port > 65535)
            throw new PeerExpressSignalingHTTP(400, "The port number is invalid");

        if (this.registeredUsers.containsKey(username))
            throw new PeerExpressSignalingHTTP(409, "The username is already taken");

        String registrationId = UUID.randomUUID().toString();
        User user = new User(username, host, port, registrationId);

        for (User registeredUser : this.registeredUsers.values())
            registeredUser.addNewUserRegistration(user);

        this.registeredUsers.put(username, user);
        return registrationId;
    }

    @Override
    public void unregisterUser(String username, String token) throws PeerExpressSignalingHTTP {
        if (username == null || token == null)
            throw new PeerExpressSignalingHTTP(400, "The parameters must be specified");

        User user = this.registeredUsers.get(username);
        if (user == null)
            throw new PeerExpressSignalingHTTP(404, "The user does not exist");

        if (!token.equals(user.getToken()))
            throw new PeerExpressSignalingHTTP(401, "The registration token is incorrect");

        this.registeredUsers.remove(username);

        for (User registeredUser : this.registeredUsers.values())
            registeredUser.addNewUserDeregistration(user);
    }

    @Override
    public ArrayList<User> getRegisteredUsers() {
        return new ArrayList<>(this.registeredUsers.values());
    }

    @Override
    public User takeNewUserRegistration(String username, String token) throws PeerExpressSignalingHTTP {
        if (username == null || token == null)
            throw new PeerExpressSignalingHTTP(400, "The parameters must be specified");

        User user = this.registeredUsers.get(username);
        if (user == null)
            throw new PeerExpressSignalingHTTP(404, "The user does not exist");

        if (!token.equals(user.getToken()))
            throw new PeerExpressSignalingHTTP(401, "The registration token is incorrect");

        return user.takeNewUserRegistration();
    }

    @Override
    public User takeNewUserDeregistration(String username, String token) throws PeerExpressSignalingHTTP {
        if (username == null || token == null)
            throw new PeerExpressSignalingHTTP(400, "The parameters must be specified");

        User user = this.registeredUsers.get(username);
        if (user == null)
            throw new PeerExpressSignalingHTTP(404, "The user does not exist");

        if (!token.equals(user.getToken()))
            throw new PeerExpressSignalingHTTP(401, "The registration token is incorrect");

        return user.takeNewUserDeregistration();
    }

}
