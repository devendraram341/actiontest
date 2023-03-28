package com.attunedlabs.leap.eventsubscription.routebuilder;

import java.util.Properties;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;

public class GenericJMSRetryRouteBuilderTest extends CamelTestSupport {

	@Before
	public void init() throws PropertiesConfigException {
		System.setProperty(PROFILE_ID, LOCAL);
		Properties globalApp = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		if (globalApp.getProperty("jmsInvocation").equalsIgnoreCase("false"))
			globalApp.replace("jmsInvocation", "true");

	}

	/**
	 * route builder implementation for retrying the failed jms subscriptions
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigure() throws Exception {
		setCacheStrategyClassInstancePerSubscription();
		addConfiguration();
		context.addRoutes(new GenericJMSRetryRouteBuilder());
		RouteDefinition route = context.getRouteDefinitions().get(0);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:triggerQuartz");
				weaveByType(ToDynamicDefinition.class).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		context.start();
		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);

		template.sendBody("direct:triggerQuartz", null);
		mockEndpoint.assertIsSatisfied();
		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Assert.assertNotNull(message);
		Assert.assertTrue((boolean) message.getHeader("subscriptionQuartzTrigger"));
	}

	private void addConfiguration() throws EventFrameworkConfigurationException {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);
		EventFrameworkConfigHelper configHelper = new EventFrameworkConfigHelper();
		configHelper.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), jmsSubscribeEvent);
	}

	private static void setCacheStrategyClassInstancePerSubscription() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);
		try {
			InstantiateSubscriptionRetryStrategy.cacheStrategyClassInstancePerSubscription(jmsSubscribeEvent,
					TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR, TEST_VERSION);
		} catch (EventFrameworkConfigurationException e) {
			e.printStackTrace();
		}
	}
}
