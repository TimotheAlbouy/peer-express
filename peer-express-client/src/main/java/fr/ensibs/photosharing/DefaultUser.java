package fr.ensibs.photosharing;

import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.util.*;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A user of the photo sharing application
 */
public class DefaultUser implements User
{

    /**
     * the user name in the file sharing community
     */
    private String name;

    /**
     * the user directory where his photos are stored
     */
    private final File directory;

    /**
     * the shared photos of the user
     */
    private List<Photo> sharedPhotos;

    /**
     * the received photos of the user
     */
    private List<Photo> receivedPhotos;

    /**
     * the tags of the user that specify the photos he is interested in
     */
    private Properties tags;

    /**
     * The connection instance to the JORAM server
     */
    private final Connection connection;

    /**
     * The destination of the JORAM server
     */
    private final Destination destination;
    
    /**
     * The session between the user and the server for producing messages
     */
    private Session producerSession;

    /**
     * The session between the user and the server for consuming messages
     */
    private Session consumerSession;

    /**
     * The message producer of the application
     */
    private MessageProducer producer;
    
    /**
     * The message consumer of the application
     */
    private MessageConsumer consumer;
    
    /**
     * The JORAM server's endpoint
     */
    private String host;

    /**
     * The JORAM server's opened port
     */
    private int port;

    /**
     * The JORAM server's destination
     */
    private static final String DEST = "PHOTOSHARING";

    /**
     * Constructor
     *
     * @param name the user name in the file sharing community
     * @param directory the user directory where his photos are stored
     * @param host the host address of the JORAM server
     * @param port the opened port of the JORAM server
     */
    public DefaultUser(String name, File directory, String host, int port) throws NamingException, JMSException
    {
        this.name = name;
        this.directory = directory;
        this.host = host;
        this.port = port;
        this.sharedPhotos = new ArrayList<>();
        this.receivedPhotos = new ArrayList<>();
        this.loadUser();
        System.out.println("User " + name + " " + directory + " " + sharedPhotos + " " + receivedPhotos);
        
        System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
        System.setProperty("java.naming.factory.host", host);
        System.setProperty("java.naming.factory.port", Integer.toString(port));
        Context context = new InitialContext();

        ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
        this.destination = (Destination) context.lookup(DEST);
        this.connection = factory.createConnection();
        this.connection.start();

        this.createProducer();
        this.createConsumer(null);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public File getDirectory()
    {
        return this.directory;
    }

    @Override
    public List<Photo> getSharedPhotos()
    {
        return this.sharedPhotos;
    }

    @Override
    public List<Photo> getReceivedPhotos()
    {
        return this.receivedPhotos;
    }

    @Override
    public boolean share(Photo photo)
    {
        try {
            File file = photo.getFile();
            String photoName = file.getName();
            String ownerName = this.name;
            Properties photoTags = photo.getTags();

            // Send a message containing only the photo's description to the topic
            Message message = this.producerSession.createMessage();
            message.setStringProperty("name", photoName);
            message.setStringProperty("owner", ownerName);
            for (String key : photo.getTags().stringPropertyNames())
                message.setStringProperty("tag_" + key, photoTags.getProperty(key));

            Session requestSession = this.connection.createSession();
            Destination requestDestination = requestSession.createTemporaryQueue();
            MessageConsumer requestConsumer = requestSession.createConsumer(requestDestination);

            // Receive a request from another user to send the photo's data
            requestConsumer.setMessageListener(request -> {
                try {
                    // Send the photo's data
                    Session responseSession = this.connection.createSession();
                    MessageProducer responseProducer = responseSession.createProducer(request.getJMSReplyTo());
                    BytesMessage response = responseSession.createBytesMessage();

                    response.setStringProperty("name", photoName);
                    response.setStringProperty("owner", ownerName);
                    for (String key : photoTags.stringPropertyNames())
                        response.setStringProperty("tag_" + key, photoTags.getProperty(key));

                    byte[] bytes = Files.readAllBytes(file.toPath());
                    for (byte b : bytes)
                        response.writeByte(b);

                    responseProducer.send(response);
                    requestSession.close();
                    responseSession.close();
                } catch (IOException | JMSException e) {
                    e.printStackTrace();
                    System.out.println("An error occurred during photo sharing");
                }
            });

            message.setJMSReplyTo(requestDestination);
            this.producer.send(message);
            this.sharedPhotos.add(photo);
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void receive(Photo photo)
    {
        this.receivedPhotos.add(photo);
        System.out.println("Photo received: " + photo);
    }

    @Override
    public boolean setFilter(Properties tags)
    {
        try {
            this.tags = tags;
            this.createConsumer(tags);
            return true;
        } catch (JMSException e) {
            return false;
        }
    }

    /**
     * Create or recreate the message producer and its session
     */
    private void createProducer() throws JMSException
    {
        this.producerSession = connection.createSession();
        this.producer = producerSession.createProducer(this.destination);
    }

    /**
     * Create or recreate the message consumer and its session
     *
     * @param tags the tags for filtering
     */
    private void createConsumer(Properties tags) throws JMSException
    {
        StringBuilder tagsBuilder = new StringBuilder();
        if (tags != null && !tags.isEmpty()) {
            Iterator<String> iterator = tags.stringPropertyNames().iterator();
            String key = iterator.next();
            String value = tags.getProperty(key);
            tagsBuilder.append("tag_").append(key).append("='").append(value).append("'");
            for (int i = 1; i < tags.size(); i++) {
                key = iterator.next();
                value = tags.getProperty(key);
                tagsBuilder.append(" OR ").append("tag_").append(key).append("='").append(value).append("'");
            }
        }

        if (this.consumerSession != null)
            this.consumerSession.close();
        this.consumerSession = connection.createSession();
        this.consumer = consumerSession.createConsumer(this.destination, tagsBuilder.toString(), true);

        // Receive a message notifying that an user shared a photo
        this.consumer.setMessageListener(message -> {
            try {
                // Send a request to this user to retrieve the photo's data
                Session requestSession = this.connection.createSession();
                MessageProducer requestProducer = requestSession.createProducer(message.getJMSReplyTo());
                Message request = requestSession.createMessage();
                String photoName = message.getStringProperty("name");
                request.setStringProperty("name", photoName);

                Session responseSession = this.connection.createSession();
                Destination responseDestination = responseSession.createTemporaryQueue();
                MessageConsumer responseConsumer = responseSession.createConsumer(responseDestination);

                // Receive the response containing the photo's data from this user
                responseConsumer.setMessageListener(response -> {
                    try {
                        String owner = response.getStringProperty("owner");
                        Properties photoTags = new Properties();
                        @SuppressWarnings("unchecked")
                        Enumeration<String> keys = (Enumeration<String>) response.getPropertyNames();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement();
                            if (key.startsWith("tag_") && key.length() > 4)
                                photoTags.put(key.substring(4), response.getStringProperty(key));
                        }

                        BytesMessage bytesMessage = (BytesMessage) response;
                        File file = new File(this.directory.getPath() + "/" + photoName);
                        FileOutputStream out = new FileOutputStream(file);
                        for (int i = 0; i < bytesMessage.getBodyLength(); i++)
                            out.write(bytesMessage.readByte());
                        out.close();

                        requestSession.close();
                        responseSession.close();
                        Photo photo = new DefaultPhoto(file, photoTags, owner);
                        receive(photo);
                    } catch (IOException | JMSException e) {
                        e.printStackTrace();
                        System.out.println("An error occurred during photo reception");
                    }
                });

                request.setJMSReplyTo(responseDestination);
                requestProducer.send(request);
            } catch (JMSException e) {
                e.printStackTrace();
                System.out.println("An error occurred during photo reception");
            }
        });
    }

    /**
     * Load the user tags and photos from the user.bak file in the user
     * directory if it exist
     */
    @SuppressWarnings("unchecked")
    private void loadUser()
    {
        File file = new File(this.directory, "user.bak");
        if (file.isFile()) {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                this.tags = (Properties) in.readObject();
                this.sharedPhotos = (List<Photo>) in.readObject();
                this.receivedPhotos = (List<Photo>) in.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the user tags and photos to the user.bak file in the user
     * directory
     */
    public void saveUser()
    {
        File file = new File(this.directory, "user.bak");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(this.tags);
            out.writeObject(this.sharedPhotos);
            out.writeObject(this.receivedPhotos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
