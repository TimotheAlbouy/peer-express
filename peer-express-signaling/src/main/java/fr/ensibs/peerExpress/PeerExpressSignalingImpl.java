package fr.ensibs.peerExpress;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.jws.WebService;

/**
 * An intermediary between producers and consumers that exchange messages on a
 * forest of topics.
 */
@WebService(name = "PeerExpressSignaling")
public class PeerExpressSignalingImpl implements PeerExpressSignaling {

    /**
     * the topic space
     */
    private TopicSpace topicSpace;

    /**
     * the producer registration list
     */
    private HashMap<String, ProducerRegistration> registrations;

    /**
     * the consumer subscription list
     */
    private HashMap<String, ConsumerSubscription> subscriptions;

    /**
     * the notification messages sent
     */
    private HashMap<String, NotificationMessage> messages;

    /**
     * Constructor.
     * @throws MalformedURLException
     */
    public PeerExpressSignalingImpl() throws Exception {
        this.topicSpace = new TopicSpace(new URL("https://peerExpress.ensibs.fr"));
        this.registrations = new HashMap<>();
        this.subscriptions = new HashMap<>();
        this.messages = new HashMap<>();

        // Dummy topics
        Topic computerScience = new Topic(new TopicPath("/computer-science"));
        Topic cryptography = new Topic(new TopicPath("/computer-science/cryptography"));
        Topic blockchain = new Topic(new TopicPath("/computer-science/blockchain"));
        this.topicSpace.addTopic(computerScience);
        this.topicSpace.addTopic(cryptography);
        this.topicSpace.addTopic(blockchain);
    }
    
    @Override
    public TopicSpace getTopicSpace() {
        return this.topicSpace;
    }
    
    @Override
    public String notify(NotificationMessage message) {
        String id = message.getMessageId();
        this.messages.put(id, message);

        // thread to remove the message asynchronously in 5 seconds
        Thread autoremoveThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                messages.remove(id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        autoremoveThread.start();

        return id;
    }
    
    @Override
    public ProducerRegistration register(NotificationProducer producer, TopicPath expression) {
        String id = UUID.randomUUID().toString();
        ProducerRegistration registration = new ProducerRegistration(id, producer, expression);
        this.registrations.put(id, registration);
        return registration;
    }
    
    @Override
    public void unregister(ProducerRegistration registration) {
        this.registrations.remove(registration);
    }
    
    @Override
    public ConsumerSubscription subscribe(NotificationConsumer consumer, TopicPath expression) {
        String id = UUID.randomUUID().toString();
        ConsumerSubscription subscription = new ConsumerSubscription(id, consumer, expression);
        this.subscriptions.put(id, subscription);
        return subscription;
    }
    
    @Override
    public void unsubscribe(ConsumerSubscription subscription) {
        this.subscriptions.remove(subscription);
    }
    
    @Override
    public ArrayList<NotificationMessage> getCurrentMessages(ConsumerSubscription subscription) {
        ArrayList<NotificationMessage> messages = new ArrayList<>();

        ConsumerSubscription localSubscription = this.subscriptions.get(subscription.getSubscriptionId());
        TopicPath subscriptionPath = localSubscription.getPath();
        for (NotificationMessage message : this.messages.values()) {
            if (subscriptionPath.matches(message.getTopicPath()))
                messages.add(message);
        }

        return messages;
    }

    @Override
    public NotificationMessage fetchMessage(ConsumerSubscription subscription) {
        try {
            ConsumerSubscription localSubscription = this.subscriptions.get(subscription.getSubscriptionId());
            TopicPath subscriptionPath = localSubscription.getPath();
            while (true) {
                Thread.sleep(5000);
                for (NotificationMessage message : this.messages.values()) {
                    if (subscriptionPath.matches(message.getTopicPath()))
                        return message;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<TopicPath> getPaths() {
        return this.topicSpace.getPaths();
    }

    @Override
    public NotificationMessage createMessage(TopicPath topicPath, NotificationProducer producer, String payload) {
        String id = UUID.randomUUID().toString();
        return new NotificationMessage(topicPath, producer, payload, id);
    }
    
}
