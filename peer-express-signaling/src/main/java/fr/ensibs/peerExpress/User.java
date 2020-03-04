package fr.ensibs.peerExpress;

import javax.xml.bind.annotation.XmlElement;

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
     * the registration id of the user
     */
    private String registrationId;

    /**
     * Constructor.
     * @param username the username of the user
     * @param host the host of the user
     * @param port the port of the user
     * @param registrationId the registration id of the user
     */
    public User(String username, String host, int port, String registrationId) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.registrationId = registrationId;
    }

    /**
     * Get the registration id of the user.
     * @return the registration id of the user
     */
    public String getRegistrationId() {
        return registrationId;
    }

}
