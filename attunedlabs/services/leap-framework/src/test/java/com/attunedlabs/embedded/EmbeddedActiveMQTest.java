package com.attunedlabs.embedded;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmbeddedActiveMQTest {

	private static final String SUBJECT = "Testing";
	private static BrokerService service;

	private String URL;
	private Session session;
	private Queue destination;
	private Connection connection;

	@BeforeClass
	public static void before() throws Exception {
		service = new BrokerService();
		service.addConnector("tcp://localhost:6161");
		service.start();
	}

	@Before
	public void setUp() throws JMSException {
		URL = service.getTransportConnectors().get(0).toString();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
		connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		destination = session.createQueue(SUBJECT);
	}

	@Test
	public void testAProducer() throws JMSException {
		MessageProducer producer = session.createProducer(destination);
		TextMessage messageproducer = session.createTextMessage("Testing");
		producer.send(messageproducer);
		Assert.assertNotNull(messageproducer);
		Assert.assertEquals("Testing", messageproducer.getText());
	}

	@Test
	public void testBConsumer() throws JMSException {
		MessageConsumer consumer = session.createConsumer(destination);
		Message messageConsumer = consumer.receive();
		Assert.assertNotNull(messageConsumer);
		if (messageConsumer instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) messageConsumer;
			Assert.assertEquals("Testing", textMessage.getText());
		}
	}

	@After
	public void cleanUp() throws JMSException {
		connection.close();
	}
}
