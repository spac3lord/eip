/*
 * Example implementations for Enterprise Integration Patterns
 * www.EnterpriseIntegrationPatterns.com
 *
 * Simple example of Publish-Subscribe Channel with Google Cloud PubSub
 */

package com.eaipatterns;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Publishes a text message passed on the command line to a hard-coded topic.
 */
public class Publisher {

    private static final String PROJECT = "eaipubsub";
    private static final String TOPIC = "orders";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        PubSubWrapper pubsub = PubSubWrapper.getInstance(
                System.getProperty("PRIVATE_KEY_FILE_PATH"),
                System.getProperty("SERVICE_ACCOUNT_EMAIL"),
                PROJECT);

        String data = (args.length > 0) ? args[0] : "{ \"type\":\"widget\", \"quantity\":6, \"ID\":123 }";
        pubsub.createTopic(TOPIC);
        List<String> messageIds = pubsub.publishMessage(TOPIC, null, data);
        if (messageIds != null) {
            for (String messageId : messageIds) {
                System.out.println("Published with a message id: " + messageId);
            }
        }
    }
}
