package com.attunedlabs.leap.eventsubscription.routebuilder;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

public class ConnectionFactoryConfiguration {
	
	public static Connection connection;
	public static Session session;
	@Bean
	public ConnectionFactory connectionFactory() throws Exception {
		BrokerService service = new BrokerService();
		service.addConnector("tcp://localhost:6161");
		service.start();
		String URL = service.getTransportConnectors().get(0).toString();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
		connection = connectionFactory.createConnection();
		connection.start();
		return connectionFactory;
	}
	
	@Bean
	public DestinationResolver setDestination() throws Exception {
		session=connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		DestinationResolver destinationResolver=new DynamicDestinationResolver();
		destinationResolver.resolveDestinationName(session, "testJmsTopicName", false);
		return destinationResolver;
	}
}
