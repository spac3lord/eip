/*
 * Example implementations for Enterprise Integration Patterns
 * www.EnterpriseIntegrationPatterns.com
 *
 * Simple example of Event-driven Consumer with RabbitMQ
 */

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueReceiver {

    private final static String QUEUE_NAME = "eipqueue";

    public static void main(String[] argv) throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

        Rabbit rabbit = new Rabbit("localhost");
        Channel channel = rabbit.createChannel();

        rabbit.makeQueue(channel, QUEUE_NAME);

        receive(channel, QUEUE_NAME);

    }

    static void receive(final Channel channel, String queueName) throws IOException {
        System.out.println("Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received '" + message + "'");
            }
        };

        boolean autoAck = true;
        channel.basicConsume(queueName, autoAck, consumer);
    }

}
