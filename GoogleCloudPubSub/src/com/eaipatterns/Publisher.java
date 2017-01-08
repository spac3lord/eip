package com.eaipatterns;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Publisher {

    static final String PROJECT = "eaipubsub";
    static final String TOPIC = "sometopic";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        PubSubWrapper pubsub = PubSubWrapper.getInstance(
                System.getProperty("PRIVATE_KEY_FILE_PATH"),
                System.getProperty("SERVICE_ACCOUNT_EMAIL"),
                PROJECT);

        String data = (args.length > 0) ? args[0] : "hello world";
        pubsub.createTopic(TOPIC);
        List<String> messageIds = pubsub.publishMessage(TOPIC, data);
        if (messageIds != null) {
            for (String messageId : messageIds) {
                System.out.println("Published with a message id: " + messageId);
            }
        }
    }
}
