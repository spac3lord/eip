/*
 * Example implementations for Enterprise Integration Patterns
 * www.EnterpriseIntegrationPatterns.com
 *
 * Simple example of Publish-Subscribe Channel with Google Cloud PubSub
 */

package com.eaipatterns;

import com.google.api.services.pubsub.model.Subscription;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Subscriber {
    private static final String PROJECT = "eaipubsub";
    private static final String TOPIC = "widgets";
    private static final String SUBSCRIPTION = "mysubscription";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        PubSubWrapper pubsub = PubSubWrapper.getInstance(
                System.getProperty("PRIVATE_KEY_FILE_PATH"),
                System.getProperty("SERVICE_ACCOUNT_EMAIL"),
                PROJECT);

        String topic = (args.length > 0) ? args[0] : TOPIC;
        String subscription = (args.length > 1) ? args[1] : SUBSCRIPTION;
        boolean doAck = args.length < 3 || args[2].toLowerCase().startsWith("ack");

        pubsub.createTopic(topic);
        Subscription sub = pubsub.subscribeTopic(subscription, topic);
        System.out.println("Subscribed " + sub.getName() + " to " + sub.getTopic());
        System.out.println("Ack: " + doAck);
        while (true) {
            List<String> data = pubsub.pullMessage(sub, 1, doAck);
            for (String s : data) {
                System.out.println(s);
            }
        }
    }
}
