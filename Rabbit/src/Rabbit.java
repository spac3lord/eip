/*
 * Example implementations for Enterprise Integration Patterns
 * www.EnterpriseIntegrationPatterns.com
 *
 * Simple example of Message Filter with RabbitMQ
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class Rabbit {

    private final Connection connection;

    Rabbit(String host) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        connection = factory.newConnection();
    }

    Channel createChannel() throws IOException, TimeoutException {
        return connection.createChannel();
    }

    void makeQueue(Channel channel, String queueName) throws IOException {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = false;
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
    }

    void close() throws IOException {
        connection.close();
    }
}
