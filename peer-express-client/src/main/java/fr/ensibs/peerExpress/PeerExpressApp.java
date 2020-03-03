package fr.ensibs.peerExpress;

import java.util.Properties;
import java.util.Scanner;

/**
* The entry point for the Peer Express messaging application that allows to
* send messages in a peer-to-peer manner to other users.
*/
public class PeerExpressApp {

    /**
     * The user of the application
     */
    private final DefaultUser user;

    /**
     * Print a usage message and exit.
     */
    private static void usage() {
        System.out.println("Usage: java PeerExpressApp <username> <host> <port> <service>");
        System.out.println("Launch the Peer Express client application, with:");
        System.out.println("<username>  the username in the community");
        System.out.println("<host>      the host address of the local JORAM server");
        System.out.println("<port>      the opened port of the local JORAM server");
        System.exit(0);
    }

    /**
     * Application entry point.
     * @param args see usage
     */
    public static void main(String[] args) {
        if (args.length != 3)
            usage();

        String username = args[0];
        String host = args[1];
        int port = 0;

        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("The port is not a number: " + args[2]);
            usage();
        }

        try {
            PeerExpressApp instance = new PeerExpressApp(username, host, port);
            instance.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     * @param username the username in the community
     * @param host the host address of the local JORAM server
     * @param port the opened port of the local JORAM server
     */
    public PeerExpressApp(String username, String host, int port) throws Exception {
        //this.user = new DefaultUser(username, host, port);
        PeerExpressSignalingImplService service = new PeerExpressImplService();
        PeerExpressSignaling signaling = service.getPeerExpressSignalingPort();
        signaling.registerUser(username, host, port);
    }

    /**
     * Launch the application process that executes user commands: SHARE, FILTER
     */
    public void run()
    {
        System.out.println("Hello, " + this.user.getName() + ". Enter commands:"
        + "\n QUIT                     to quit the application"
        + "\n SHARE <filename> <tags>  to share a new photo"
        + "\n FILTER <tags>            to specify the photos you are interested in"
        + "\n where <tags> is a list of tags in the form \"key1=value1 key2=value2\"");

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!line.equals("quit") && !line.equals("QUIT")) {
            String[] command = line.split(" +");
            switch (command[0]) {
            case "share":
            case "SHARE":
                if (command.length >= 2) {
                    File file = new File(this.user.getDirectory(), command[1]);
                    Properties tags = parseTags(command, 2);
                    share(file, tags);
                } else {
                    System.err.println("Usage: share <filename> <tags>");
                }
                break;
            case "filter":
            case "FILTER":
                filter(parseTags(command, 1));
                break;
            default:
                System.err.println("Unknown command: \"" + command[0] + "\"");
            }
            line = scanner.nextLine();
        }
        quit();
    }

    /**
     * Share a new photo
     *
     * @param file the photo file in the local directory
     * @param tags a list of tags that describe the photo
     */
    public void share(File file, Properties tags)
    {
        if (file.isFile()) {
            Photo photo = new DefaultPhoto(file, tags, this.user.getName());
            boolean success = this.user.share(photo);
            if (success)
                System.out.println(photo + " has been shared");
            else System.out.println("An error occurred during photo sharing");
        } else {
            System.err.println("File " + file.getAbsolutePath() + " not found");
        }
    }

    /**
     * Specify the photos the user is interested in by setting new tags
     *
     * @param tags the new user tags
     */
    public void filter(Properties tags)
    {
        boolean success = this.user.setFilter(tags);
        if (success) {
          System.out.println("Filter " + tags + " has been set");
        }
    }

    /**
     * Stop the application
     */
    public void quit()
    {
        this.user.saveUser();
        System.exit(0);
    }

    /**
     * Transform a list of words in the form key=value to tags
     *
     * @param tokens a list of strings
     * @param startIdx the index of the first token in the given list that represent
     * a tag
     * @return the tags
     */
    private Properties parseTags(String[] tokens, int startIdx)
    {
        Properties tags = new Properties();
        for (int i=startIdx; i< tokens.length; i++) {
            String[] token = tokens[i].split("=");
            if (token.length == 2) {
                tags.put(token[0], token[1]);
            } else {
                System.err.println("Cannot parse tag: \"" + tokens[i] + "\"");
            }
        }
        return tags;
    }
}
