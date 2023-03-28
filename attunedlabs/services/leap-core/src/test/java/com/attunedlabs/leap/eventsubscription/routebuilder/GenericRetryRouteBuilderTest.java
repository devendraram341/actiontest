package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Properties;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;

public class GenericRetryRouteBuilderTest extends CamelTestSupport {

	@Before
	public void init() throws PropertiesConfigException {
		System.setProperty(PROFILE_ID, LOCAL);
		Properties globalApp = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		if (globalApp.getProperty("jmsInvocation").equalsIgnoreCase("true"))
			globalApp.replace("jmsInvocation", "false");
	}

	/**
	 * route builder implementation for retrying the failed subscriptions
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigureWithCacheStrategy() throws Exception {
		setCacheStrategyClassInstancePerSubscription();
		addConfigurationService();
		context.addRoutes(new GenericRetryRouteBuilder());
		RouteDefinition route = context.getRouteDefinitions().get(0);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:triggerQuartz");
				weaveByToUri(ALL_SEDA_URI).remove();
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
	
	private SubscribeEvent addConfigurationService() throws EventFrameworkConfigurationException {
		EventFramework subscription = LeapCoreTestFileRead.getKafkaSubscription();
		SubscribeEvent subscribeEvent = subscription.getEventSubscription().getSubscribeEvent().get(0);
		EventFrameworkConfigHelper configHelper = new EventFrameworkConfigHelper();
		configHelper.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), subscribeEvent);
		return subscribeEvent;
	}

	private void setCacheStrategyClassInstancePerSubscription() {
		EventFramework subscription = LeapCoreTestFileRead.getKafkaSubscription();
		SubscribeEvent subscribeEvent = subscription.getEventSubscription().getSubscribeEvent().get(0);
		try {
			InstantiateSubscriptionRetryStrategy.cacheStrategyClassInstancePerSubscription(subscribeEvent, TEST_TENANT,
					TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE, TEST_IMPL, TEST_VENDOR, TEST_VERSION);
		} catch (EventFrameworkConfigurationException e) {
			e.printStackTrace();
		}
	}
}
