package fr.ensibs.peerExpress;

import javax.swing.*;
import java.util.List;

public class GraphicUserInterface extends JFrame implements UserInterface {

    /**
     * the PeerExpress app instance
     */
    private PeerExpressApp app;

    /**
     * Constructor.
     * @param app the PeerExpress app instance
     */
    public GraphicUserInterface(PeerExpressApp app) {
        super("PeerExpress — Decentralized messaging application");
        this.app = app;

    }

    @Override
    public void run() {
        this.setVisible(true);
    }

    @Override
    public void showUsers(List<User> users) {

    }

    @Override
    public void notifyMessageReceived(String sender, String content) {

    }

    @Override
    public void notifyNewUserRegistration(String username) {

    }

    @Override
    public void notifyNewUserDeregistration(String username) {
        //
    }

}
