package com.attunedlabs.embedded;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.attunedlabs.embedded.rabbitmq.EmbeddedInMemoryQpidBrokerRule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedRabbitMQTest {

	@ClassRule
	public static EmbeddedInMemoryQpidBrokerRule qpidBrokerRule = new EmbeddedInMemoryQpidBrokerRule();

	private Channel channel;

	@Before
	public void setUp() throws IOException, TimeoutException {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setPort(5555);
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare("EmbeddedTest", false, false, false, null);
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAProducer() throws IOException, TimeoutException {
		String messagePublise = "Hello World! Rabbit MQ";
		boolean channelOpen = channel.isOpen();
		Assert.assertTrue(channelOpen);
		channel.basicPublish("", "EmbeddedTest", null, messagePublise.getBytes("UTF-8"));
	}

	@Test
	public void testBConsume() throws IOException, TimeoutException {
		boolean channelOpen = channel.isOpen();
		Assert.assertTrue(channelOpen);
		channel.basicConsume("EmbeddedTest", (consumerTag, delivery) -> {
			String messageConsume = new String(delivery.getBody(), "UTF-8");
			Assert.assertNotNull(messageConsume);
			Assert.assertEquals("Hello World! Rabbit MQ", messageConsume);
		}, consumerTag -> {
		});
	}
}
