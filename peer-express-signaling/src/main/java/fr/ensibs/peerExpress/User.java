package fr.ensibs.peerExpress;

import javax.xml.bind.annotation.XmlElement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents an user registered in the signaling server.
 */
public class User {

    /**
     * the username of the user
     */
    @XmlElement
    private String username;

    /**
     * the host of the user
     */
    @XmlElement
    private String host;

    /**
     * the opened port of the user
     */
    @XmlElement
    private int port;

    /**
     * the registration token of the user that must be kept secret
     */
    private String token;

    /**
     * the blocking queue containing newly registered users
     */
    private BlockingQueue<User> newUserRegistrations = new LinkedBlockingQueue<>();

    /**
     * the blocking queue containing deregistered users
     */
    private BlockingQueue<User> newUserDeregistrations = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     * @param username the username of the user
     * @param host the host of the user
     * @param port the port of the user
     * @param token the registration token of the user
     */
    public User(String username, String host, int port, String token) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.token = token;
    }

    /**
     * Add a newly registered user to the queue.
     * @param user the user to add
     */
    public void addNewUserRegistration(User user) {
        this.newUserRegistrations.add(user);
    }

    /**
     * Add a deregistered user to the queue.
     * @param user the user to add
     */
    public void addNewUserDeregistration(User user) {
        this.newUserDeregistrations.add(user);
    }

    /**
     * Retrieve and remove the first newly registered user from the blocking queue,
     * waiting if necessary until one becomes available.
     * @return the first newly registered user
     */
    public User takeNewUserRegistration() {
        try {
            return this.newUserRegistrations.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Retrieve and remove the first deregistered user from the blocking queue,
     * waiting if necessary until one becomes available.
     * @return the first newly registered user
     */
    public User takeNewUserDeregistration() {
        try {
            return this.newUserDeregistrations.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Get the registration token of the user.
     * @return the registration token of the user
     */
    public String getToken() {
        return token;
    }

}
