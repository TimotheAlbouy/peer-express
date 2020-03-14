package fr.ensibs.peerExpress.ui;

import fr.ensibs.peerExpress.User;

import java.util.List;

/**
 * A user interface for the PeerExpress client.
 */
public interface UserInterface {

    /**
     * Launch the user interface process that retrieves user commands.
     */
    void run();

    /**
     * Show on the interface the currently registered users.
     * @param users the users to show
     */
    void showUsers(List<User> users);

    /**
     * Show on the interface the new message that has been received.
     * @param sender the username of the sender of the message
     * @param content the content of the message
     */
    void notifyMessageReceived(String sender, String content);

    /**
     * Notify on the interface that a new user has been registered.
     * @param username the username of the new user
     * @param host the host of the new user
     * @param port the port of the new user
     */
    void notifyNewUserRegistration(String username, String host, int port);

    /**
     * Notify on the interface that a user logged out.
     * @param username the username of the user
     */
    void notifyNewUserDeregistration(String username);

    /**
     * Display on the interface an error message.
     * @param message the error message
     */
    void displayError(String message);

}
