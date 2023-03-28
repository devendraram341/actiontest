package com.attunedlabs.leap.eventsubscription.routebuilder;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.HashMap;
import java.util.Map;

public class SubscriberEvaluationRouteBuilderTest extends CamelTestSupport {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);
	private static EmbeddedKafkaBroker embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();

	/**
	 * route builder implementation for evaluating the subscriber configuration on
	 * the consumed event message with the help of different processors
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigure() throws Exception {
		addConfigurationService();
		context.addRoutes(new SubscriberEvaluationRouteBuilder());
		RouteDefinition route = context.getRouteDefinitions().get(0);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:sedaEndpint");
				weaveByType(ToDynamicDefinition.class).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders("direct:sedaEndpint", DEMO_LEAP_DATA, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull(message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertEquals("Exchange Header Action key value should be same as expected ::", "InvokeCamelRoute",
				headers.get("Action"));
		Assert.assertEquals("Exchange Header PRE_STAGE key data should be false ::", false, headers.get("PRE_STAGE"));
		Assert.assertEquals("Exchange Header routingRuleLoopCount key data should be 1 ::", 1,
				headers.get("routingRuleLoopCount"));

	}

	private SubscribeEvent addConfigurationService() throws EventFrameworkConfigurationException {
		EventFramework subscription = LeapCoreTestFileRead.getKafkaSubscription();
		SubscribeEvent subscribeEvent = subscription.getEventSubscription().getSubscribeEvent().get(0);
		EventFrameworkConfigHelper configHelper = new EventFrameworkConfigHelper();
		configHelper.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), subscribeEvent);
		return subscribeEvent;
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION
				+ "-TestKafka";
	}

	private Map<String, Object> setHeader() throws ConfigurationValidationFailedException, EventFrameworkConfigurationException {
		LeapDataContext dataContext = new LeapDataContext();
		AbstractSubscriptionRetryStrategy retryStrategy=new LeapDefaultRetryStrategy(addConfigurationService().getFailureHandlingStrategy().getFailureStrategyConfig());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(LEAP_DATA_CONTEXT, dataContext);
		map.put(SUBSCRIPTION_ID,setSubscriptionId());
		map.put("RetryStrategy", retryStrategy);
		map.put("EventSubscriptionTracker", getEventSubscription());
		map.put("subscriptionQuartzTrigger",false);
		map.put("CamelKafkaManualCommit", LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker));
		return map;
	}
	
	
	private EventSubscriptionTracker getEventSubscription() throws EventFrameworkConfigurationException {
		EventSubscriptionTracker tracker = new EventSubscriptionTracker();
		tracker.setTenantId(TEST_TENANT);
		tracker.setSiteId(TEST_SITE);
		tracker.setEventData(DEMO_JSON_DATA);
		tracker.setSubscriptionId(setSubscriptionId());
		tracker.setTopic(addConfigurationService().getSubscribeTo());
		tracker.setPartition("1");
		tracker.setOffset("1");
		tracker.setIsRetryable(true);
		return tracker;
	}

}
