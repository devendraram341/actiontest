package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.junit.Assert;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.leap.LeapDataContext;

public class JMSSubscriberEvaluationRouteBuilderTest extends CamelSpringTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigure() throws Exception {
		addConfiguration();
		context.addRoutes(new JMSSubscriberEvaluationRouteBuilder());
		RouteDefinition route = context.getRouteDefinitions().get(0);

		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByType(ToDynamicDefinition.class).remove();
				replaceFromWith("direct:jmsEndPoint");
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders("direct:jmsEndPoint", DEMO_JSON_DATA, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Map<String, Object> headers = message.getHeaders();

		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertEquals("Exhange header key Action data should be same as Expected ::", "InvokeCamelRoute",
				headers.get("Action"));
		Assert.assertEquals("Exhange header key JMSDestination data should be same as Expected ::", "testJmsTopicName",
				headers.get("JMSDestination"));
		Assert.assertEquals("Exhange header key PRE_STAGE data should be same as false ::", false,
				headers.get("PRE_STAGE"));

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
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(LEAP_DATA_CONTEXT, dataContext);
		headers.put("JMSDestination", "testJmsTopicName");
		headers.put(SUBSCRIPTION_ID, setSubscriptionId());
		return headers;
	}

}
