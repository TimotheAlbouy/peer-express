package fr.ensibs.photosharing;

import java.util.Properties;
import java.util.Scanner;
import java.io.File;

/**
* The entry point for the user photo sharing application that allows to enter
* descriptions and tags of photos to be shared and filter and choose photos
* from their tags
*/
public class PhotoSharingApp
{

    /**
     * The user of the application
     */
    private final DefaultUser user;

    /**
     * Print a usage message and exit
     */
    private static void usage()
    {
        System.out.println("Usage: java PhotoSharingApp <user_name> <directory> <host> <port>");
        System.out.println("Launch the user photo sharing application");
        System.out.println("with:");
        System.out.println("<user_name>  the user name in the community");
        System.out.println("<directory>  the local directory where photos are stored");
        System.out.println("<host>  the host address of the JORAM server");
        System.out.println("<port>  the opened port of the JORAM server");
        System.exit(0);
    }

    /**
     * Application entry point
     *
     * @param args see usage
     */
    public static void main(String[] args)
    {
        if (args.length != 4) {
            usage();
        }

        String userName = args[0];
        File directory = new File(args[1]);
        String host = args[2];
        int port = 0;

        if (!directory.isDirectory()) {
            System.out.println("Unknown directory: " + args[1]);
            usage();
        }

        try {
            port = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("The port is not a number: " + args[3]);
            usage();
        }

        try {
            PhotoSharingApp instance = new PhotoSharingApp(userName, directory, host, port);
            instance.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     *
     * @param userName the user name in the community
     * @param directory the local directory where photos are stored
     * @param host the host address of the JORAM server
     * @param port the opened port of the JORAM server
     */
    public PhotoSharingApp(String userName, File directory, String host, int port) throws Exception
    {
        this.user = new DefaultUser(userName, directory, host, port);
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
