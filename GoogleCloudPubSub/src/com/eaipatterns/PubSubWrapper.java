package com.eaipatterns;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * A thin wrapper around the Google Cloud PubSub API
 */
public class PubSubWrapper {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final String project;
    private Pubsub pubsub;

    private PubSubWrapper(String projectName) {
        this.project = String.format("projects/%s", projectName);
    }

    public static PubSubWrapper getInstance(
            String private_key_file_path,
            String service_account_email,
            String projectName)
            throws IOException, GeneralSecurityException {
        PubSubWrapper wrapper = new PubSubWrapper(projectName);
        wrapper.createClient(private_key_file_path, service_account_email);
        return wrapper;
    }

    /**
     * Setup authorization for local app based on private key.
     * See <a href="https://cloud.google.com/pubsub/configure">cloud.google.com/pubsub/configure</a>
     */
    private void createClient(String private_key_file, String email) throws IOException, GeneralSecurityException {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(transport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountScopes(PubsubScopes.all())
                .setServiceAccountId(email)
                .setServiceAccountPrivateKeyFromP12File(new File(private_key_file))
                .build();
        // Please use custom HttpRequestInitializer for automatic retry upon failures.
//        HttpRequestInitializer initializer = new RetryHttpInitializerWrapper(credential);
        pubsub = new Pubsub.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName("eaipubsub")
                .build();
    }

    /**
     * Creates a topic <code>/projects/</code>appName<code>/topics/</code>topicName if it does not already exist.
     */
    Topic createTopic(String topicName) throws IOException {
        String topic = getTopic(topicName);
        Pubsub.Projects.Topics topics = pubsub.projects().topics();
        ListTopicsResponse list = topics.list(project).execute();
        if (list.getTopics() == null || !list.getTopics().contains(new Topic().setName(topic))) {
            return topics.create(topic, new Topic()).execute();
        } else {
            return new Topic().setName(topic);
        }
    }

    /**
     * Publishes one message containing {@code data} to projects/<code>appName</code>/topics/<code>topicName</code>.
     *
     * @return List of message IDs or null if no message was published
     */
    List<String> publishMessage(String topicName, String data) throws IOException {
        List<PubsubMessage> messages = Lists.newArrayList();
        messages.add(new PubsubMessage().encodeData(data.getBytes("UTF-8")));
        PublishRequest publishRequest = new PublishRequest().setMessages(messages);
        PublishResponse publishResponse = pubsub.projects().topics()
                .publish(getTopic(topicName), publishRequest)
                .execute();
        return publishResponse.getMessageIds();
    }

    /**
     * Sets up a subscription to projects/<code>appName</code>/subscriptions/<code>subName</code>.
     * Ignores error if the subscription already exists.
     * <p/>
     * See <a href="https://cloud.google.com/pubsub/subscriber">cloud.google.com/pubsub/subscriber</a>
     */
    Subscription subscribeTopic(String subscriptionName, String topicName) throws IOException {
        String sub = getSubscription(subscriptionName);
        Subscription subscription = new Subscription()
                .setName(sub)
                .setAckDeadlineSeconds(15)
                .setTopic(getTopic(topicName));
        try {
            return pubsub.projects().subscriptions().create(sub, subscription).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                return subscription;
            } else {
                throw e;
            }
        }
    }

    /**
     * Pulls messages..
     * <p/>
     * See <a href="https://cloud.google.com/pubsub/subscriber">cloud.google.com/pubsub/subscriber</a>
     */
    List<String> pullMessage(Subscription subscription, int maxMessages, boolean doAck) throws IOException {
        PullRequest pullRequest = new PullRequest()
                .setReturnImmediately(true)
                .setMaxMessages(maxMessages);
        PullResponse response = pubsub.projects().subscriptions().pull(subscription.getName(), pullRequest).execute();
        List<ReceivedMessage> messages = response.getReceivedMessages();
        List<String> ackIds = Lists.newArrayList();
        List<String> data = Lists.newArrayList();
        if (messages != null) {
            for (ReceivedMessage receivedMessage : messages) {
                PubsubMessage message = receivedMessage.getMessage();
                if (message != null) {
                    byte[] bytes = message.decodeData();
                    if (bytes != null) {
                        data.add(new String(bytes, "UTF-8"));
                    }
                }
                ackIds.add(receivedMessage.getAckId());
            }
            if (doAck) {
                AcknowledgeRequest ackRequest = new AcknowledgeRequest().setAckIds(ackIds);
                pubsub.projects().subscriptions().acknowledge(subscription.getName(), ackRequest).execute();
            }
        }
        return data;
    }

    private String getTopic(String topicName) {
        return String.format("%s/topics/%s", project, topicName);
    }

    private String getSubscription(String subscriptionName) {
        return String.format("%s/subscriptions/%s", project, subscriptionName);
    }

}
