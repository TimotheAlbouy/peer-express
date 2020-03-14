package fr.ensibs.peerExpress;

/**
 * Wrapper class containing the sender and content of a message.
 */
public class MessageInfo {

    /**
     * the sender of the message
     */
    private String sender;

    /**
     * the content of the message
     */
    private String content;

    /**
     * Constructor.
     * @param sender the sender of the message
     * @param content the content of the message
     */
    public MessageInfo(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    /**
     * Get the sender of the message.
     * @return the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get the content of the message.
     * @return the content of the message
     */
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "[" + sender + "]: " + content;
    }

}
