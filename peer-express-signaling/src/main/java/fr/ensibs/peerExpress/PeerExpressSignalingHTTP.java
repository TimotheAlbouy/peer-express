package fr.ensibs.peerExpress;

import javax.xml.ws.WebFault;

/**
 * An exception occuring in a web method of the PeerExpress signaling service.
 */
@WebFault(name = "BakeryServiceException")
public class PeerExpressSignalingHTTP extends Exception {

    /**
     * the HTTP status code of the error
     */
    private int httpCode;

    /**
     * Constructor.
     * @param message the message of the error
     * @param httpCode the HTTP status code
     */
    public PeerExpressSignalingHTTP(int httpCode, String message) {
        super("HTTP " + httpCode + ": " + message);
        this.httpCode = httpCode;
    }

    /**
     * Constructor.
     * @param httpCode the HTTP status code
     */
    public PeerExpressSignalingHTTP(int httpCode) {
        super("HTTP " + httpCode);
    }

    /**
     * Get the HTTP status code of the error.
     * @return the HTTP status code
     */
    public int getHttpCode() {
        return httpCode;
    }

}
