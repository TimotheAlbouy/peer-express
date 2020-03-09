package fr.ensibs.peerExpress;

import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Wrapper class containing the user object and other objects
 * needed for peer-to-peer communication with this user.
 */
public class UserInfo {

    /**
     * the user object
     */
    private User user;

    /**
     * the session with the user's destination
     */
    private Session session;

    /**
     * the message producer
     */
    private MessageProducer producer;

    /**
     * Constructor.
     * @param user the user object
     * @param session the session with the user's destination
     * @param producer the message producer
     */
    public UserInfo(User user, Session session, MessageProducer producer) {
        this.user = user;
        this.session = session;
        this.producer = producer;
    }

    /**
     * Get the user.
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the producer session.
     * @return the producer session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get the message producer.
     * @return the message producer
     */
    public MessageProducer getProducer() {
        return producer;
    }

    /**
     * Tell if the session has been established with the user.
     * @return true if and only if the session has been established
     */
    public boolean isSessionEstablished() {
        return session != null && producer != null;
    }

    /**
     * Set the user.
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Set the producer session.
     * @param session the producer session
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Set the message producer.
     * @param producer the message producer
     */
    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    @Override
    public String toString() {
        return "UserInfo{" + user.getUsername() +
                ", " + user.getHost() + ':' + user.getPort() +
                ", session established: " + isSessionEstablished() +
                '}';
    }

}
