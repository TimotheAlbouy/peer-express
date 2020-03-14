package fr.ensibs.peerExpress.ui;

import fr.ensibs.peerExpress.PeerExpressApp;
import fr.ensibs.peerExpress.User;

import java.util.List;
import java.util.Scanner;

/**
 * A user interface on console for the PeerExpress client.
 */
public class ConsoleUserInterface implements UserInterface {

    /**
     * the PeerExpress app instance
     */
    private PeerExpressApp app;

    /**
     * Constructor.
     * @param app the PeerExpress app instance
     * @throws Exception if the app instance is null
     */
    public ConsoleUserInterface(PeerExpressApp app) throws Exception {
        if (app == null)
            throw new Exception("The app instance must not be null");
        this.app = app;
    }

    @Override
    public void run() {
        this.showCommands();
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (true) {
            String[] tokens = line.split(" +");
            switch (tokens[0]) {
                case "send":
                case "SEND":
                    if (tokens.length >= 2) {
                        String username = tokens[1];
                        String message = line.replaceFirst("send", "")
                                .replaceFirst(username, "")
                                .trim();
                        this.app.send(username, message);
                    } else {
                        System.err.println("Usage: send <username> <message>");
                    }
                    break;
                case "users":
                case "USERS":
                    this.app.showUsers();
                    break;
                case "help":
                case "HELP":
                    this.showCommands();
                    break;
                case "quit":
                case "QUIT":
                    this.app.quit();
                default:
                    System.err.println("Unknown command: \"" + tokens[0] + "\"");
            }
            line = scanner.nextLine();
        }
    }

    @Override
    public void showUsers(List<User> users) {
        for (User user : users) {
            String username = user.getUsername();
            String host = user.getHost();
            int port = user.getPort();
            System.out.println('[' + username + "]: " + host + ':' + port);
        }
    }

    @Override
    public void notifyMessageReceived(String sender, String content) {
        System.out.println('[' + sender + "]: " + content);
    }

    @Override
    public void notifyNewUserRegistration(String username, String host, int port) {
        System.out.println('[' + username + "] logged in.");
    }

    @Override
    public void notifyNewUserDeregistration(String username) {
        System.out.println("[" + username + "] logged out.");
    }

    @Override
    public void displayError(String message) {
        System.err.println(message);
    }

    /**
     * Display the available commands.
     */
    public void showCommands() {
        System.out.println("Hello, " + this.app.getUsername() + ". Enter commands:"
                + "\n QUIT                       to quit the application"
                + "\n USERS                      to display the list of registered users"
                + "\n SEND <username> <message>  to send a message to an user"
                + "\n HELP                       to show these commands again");
    }

}
