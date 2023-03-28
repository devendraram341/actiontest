package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.jms.JmsBinding;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;

public class JMSSubscriberRouteBuilderTest extends CamelSpringTest {

	private Exchange exchange;

	@Before
	public void init() throws JMSException {
		CamelContext camelContext = new DefaultCamelContext();
		exchange = new DefaultExchange(camelContext);
		exchange.setIn(setMessage());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigure() throws Exception {
		LeapHeaderConstant.tenant = TEST_TENANT;
		LeapHeaderConstant.site = TEST_SITE;
		addConfiguration();
		context.addRoutes(new JMSSubscriberRouteBuilder());
		RouteDefinition route = context.getRouteDefinitions().get(0);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:JmsEndPoint");
				weaveByType(ToDynamicDefinition.class).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders("direct:JmsEndPoint", DEMO_JSON_DATA, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Map<String, Object> headers = message.getHeaders();

		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertEquals("Exhange header key JMSRedelivered data should be same as false ::", false,
				headers.get("JMSRedelivered"));
		Assert.assertEquals("Exhange header key JMSDestination data should be same as Expected ::", "testJmsTopicName",
				headers.get("JMSDestination"));
		Assert.assertEquals("Exhange header key JMSDeliveryMode data should be same as 1 ::", 1,
				headers.get("JMSDeliveryMode"));

	}

	private void addConfiguration() throws EventFrameworkConfigurationException {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);
		EventFrameworkConfigHelper configHelper = new EventFrameworkConfigHelper();
		configHelper.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), jmsSubscribeEvent);
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION
				+ "-TestJMS";
	}

	private Map<String, Object> setHeader() {
		LeapDataContext dataContext = new LeapDataContext();
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(dataContext);
		Message message = exchange.getIn();
		Map<String, Object> headers = message.getHeaders();
		headers.put(SUBSCRIPTION_ID, setSubscriptionId());
		headers.put(LEAP_DATA_CONTEXT, dataContext);
		headers.put("JMSDestination", "testJmsTopicName");
		return headers;
	}

	private Message setMessage() throws JMSException {
		String payload = "Hi, I am text message";
		Session session = ConnectionFactoryConfiguration.session;
		javax.jms.Message msg = session.createTextMessage(payload);
		JmsBinding binding = new JmsBinding();
		Message message = new JmsMessage(msg, session, binding);
		return message;
	}
}
