package fr.ensibs.peerExpress.ui;

import fr.ensibs.peerExpress.PeerExpressApp;
import fr.ensibs.peerExpress.MessageInfo;
import fr.ensibs.peerExpress.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class GraphicUserInterface extends JFrame implements UserInterface {

    /**
     * the PeerExpress app instance
     */
    private PeerExpressApp app;

    /**
     * the table model to add registered users dynamically
     */
    private DefaultTableModel registeredUsersModel;

    /**
     * the display are to show errors
     */
    private JLabel errorDisplay;

    /**
     * the info on the discussion between the local user and another user identified by their username
     */
    private Map<String, List<MessageInfo>> messagesInfo = new HashMap<>();

    /**
     * the username of the current correspondent
     */
    private String correspondent;

    /**
     * the display area of the username of the current correspondent
     */
    private JLabel correspondentDisplay;

    /**
     * the display area of the messages of the active discussion
     */
    private JPanel messagesDisplay;

    /**
     * Constructor.
     * @param app the PeerExpress app instance
     * @throws Exception if the app instance is null
     */
    public GraphicUserInterface(PeerExpressApp app) throws Exception {
        super("PeerExpress — Decentralized messaging application");
        if (app == null)
            throw new Exception("The app instance must not be null");
        this.app = app;

        // set up the layout manager of the window
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());

        // create a welcome message on the top of the window
        JLabel welcomeMessage = new JLabel("Welcome " + this.app.getUsername() + ".");
        container.add(welcomeMessage, BorderLayout.NORTH);

        // create the two panels
        this.createLeftPanel();
        this.createRightPanel();

        // create the error display area on the bottom of the window
        this.errorDisplay = new JLabel(".");
        container.add(this.errorDisplay, BorderLayout.SOUTH);

        // execute method on close
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                app.quit();
            }
        });

        this.setSize(new Dimension(700, 500));
        this.setResizable(false);
    }

    @Override
    public void run() {
        this.app.showUsers();
        this.setVisible(true);
    }

    @Override
    public void showUsers(List<User> users) {
        for (User user : users) {
            String username = user.getUsername();
            if (!username.equals(this.app.getUsername())) {
                Object[] row = {username, user.getHost(), user.getPort()};
                this.registeredUsersModel.addRow(row);
            }
        }
    }

    @Override
    public void notifyMessageReceived(String sender, String content) {
        List<MessageInfo> messages = this.messagesInfo.computeIfAbsent(sender, k -> new ArrayList<>());
        messages.add(new MessageInfo(sender, content));
        if (sender.equals(this.correspondent))
            this.addMessageLabel(content, false);
    }

    @Override
    public void notifyNewUserRegistration(String username, String host, int port) {
        Object[] row = {username, host, port};
        this.registeredUsersModel.addRow(row);
    }

    @Override
    public void notifyNewUserDeregistration(String username) {
        int row = 0;
        while (!this.registeredUsersModel.getValueAt(row, 0).equals(username)) row++;
        this.registeredUsersModel.removeRow(row);
    }

    @Override
    public void displayError(String message) {
        this.errorDisplay.setText(message);
    }

    /**
     * Create the left panel containing the list of registered users.
     */
    private void createLeftPanel() {
        // create a table that will show the registered users
        Object[] headers = {"Username", "Host", "Port"};
        this.registeredUsersModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable registeredUsers = new JTable(this.registeredUsersModel);

        // when a row on the table is selected
        registeredUsers.getSelectionModel().addListSelectionListener(e -> {
            int row = registeredUsers.getSelectedRow();
            String username = (String) registeredUsers.getValueAt(row, 0);
            if (!username.equals(this.correspondent))
                this.switchCorrespondent(username);
        });
        registeredUsers.getTableHeader().setReorderingAllowed(false);

        // add the components to the panel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(registeredUsers.getTableHeader(), BorderLayout.NORTH);
        leftPanel.add(registeredUsers, BorderLayout.CENTER);

        // add the panel to the window
        this.getContentPane().add(leftPanel, BorderLayout.WEST);
    }

    /**
     * Create the right panel containing the message area.
     */
    private void createRightPanel() {
        // create a display area that will show the correspondent's username
        JPanel correspondentWrapper = new JPanel();
        this.correspondentDisplay = new JLabel("");
        this.correspondentDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        correspondentWrapper.add(this.correspondentDisplay);

        // create a display area that will show the discussion with the correspondent
        this.messagesDisplay = new JPanel();
        this.messagesDisplay.setLayout(new BoxLayout(this.messagesDisplay, BoxLayout.Y_AXIS));

        // create a text field to write messages
        JTextField messageField = new JTextField("", 50);

        // when the ENTER key is pressed on the text field
        messageField.addActionListener(e -> {
            String sender = this.app.getUsername();
            String content = messageField.getText();
            if (this.correspondent != null && !content.isEmpty()) {
                List<MessageInfo> messages = this.messagesInfo
                        .computeIfAbsent(this.correspondent, k -> new ArrayList<>());
                messages.add(new MessageInfo(sender, content));
                this.addMessageLabel(content, true);

                messageField.setText("");
                this.app.send(this.correspondent, content);
            }
        });

        // add the components to the panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(correspondentWrapper, BorderLayout.NORTH);
        rightPanel.add(this.messagesDisplay, BorderLayout.CENTER);
        rightPanel.add(messageField, BorderLayout.SOUTH);

        // add the panel to the window
        this.getContentPane().add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Switch the correspondent to the given username.
     * @param username the new correspondent
     */
    private void switchCorrespondent(String username) {
        this.correspondent = username;
        this.correspondentDisplay.setText(username != null ? username : "");

        this.messagesDisplay.removeAll();
        this.messagesDisplay.revalidate();
        this.messagesDisplay.repaint();

        if (username != null) {
            List<MessageInfo> messages = this.messagesInfo.get(username);
            if (messages != null) {
                for (MessageInfo message : messages) {
                    boolean alignRight = this.app.getUsername().equals(message.getSender());
                    this.addMessageLabel(message.getContent(), alignRight);
                }
            }
        }
    }

    /**
     * Add a message label to the message area.
     * @param content the content of the message
     * @param alignRight true if the message has been sent by the local user,
     *                   false if it has been sent by the remote user
     */
    private void addMessageLabel(String content, boolean alignRight) {
        JPanel messageWrapper = new JPanel();
        int alignment = alignRight ? SwingConstants.RIGHT : SwingConstants.LEFT;
        JLabel messageLabel = new JLabel(content, alignment);
        messageWrapper.add(messageLabel);
        //this.messagesDisplay.add(messageWrapper);
        this.messagesDisplay.add(messageLabel);
        this.messagesDisplay.revalidate();
        this.messagesDisplay.repaint();
    }

}
