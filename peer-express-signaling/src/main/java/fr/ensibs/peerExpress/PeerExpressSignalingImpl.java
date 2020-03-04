package fr.ensibs.peerExpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import javax.jws.WebService;

/**
 * An intermediary between producers and consumers that exchange messages on a
 * forest of topics.
 */
@WebService(name = "PeerExpressSignaling")
public class PeerExpressSignalingImpl implements PeerExpressSignaling {

    /**
     * the list of registered users
     */
    private HashMap<String, User> registeredUsers;

    /**
     * Constructor.
     */
    public PeerExpressSignalingImpl() {
        this.registeredUsers = new HashMap<>();
    }

    @Override
    public String registerUser(String username, String host, int port) {
        if (username == null || host == null || port < 0 || port > 65535 || this.registeredUsers.containsKey(username))
            return null;

        String registrationId = UUID.randomUUID().toString();
        User user = new User(username, host, port, registrationId);
        this.registeredUsers.put(username, user);
        return registrationId;
    }

    @Override
    public boolean unregisterUser(String username, String registrationId) {
        if (username == null || registrationId == null)
            return false;

        User user = this.registeredUsers.get(username);
        if (user == null || registrationId.equals(user.getRegistrationId()))
            return false;

        this.registeredUsers.remove(username);
        return true;
    }

    @Override
    public ArrayList<User> getRegisteredUsers() {
        return new ArrayList<>(this.registeredUsers.values());
    }

}
