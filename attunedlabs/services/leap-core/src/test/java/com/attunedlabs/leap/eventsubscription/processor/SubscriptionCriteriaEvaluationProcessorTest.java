package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.configdbtest.DeleteConfigNodeForTesting;
import com.attunedlabs.configdbtest.InsertConfigNodeForTesting;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationUnit;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.leap.LeapDataContext;

public class SubscriptionCriteriaEvaluationProcessorTest {

	private SubscriptionCriteriaEvaluationProcessor evaluationProcessor;
	private IEventFrameworkConfigService configService = new EventFrameworkConfigService();
	private Exchange exchange;

	@BeforeClass
	public static void init() {
		System.setProperty(PROFILE_ID, LOCAL);
		new InsertConfigNodeForTesting();
	}

	@Before
	public void setUp() {
		if (evaluationProcessor == null)
			evaluationProcessor = new SubscriptionCriteriaEvaluationProcessor(configService,
					LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createJmsExchange();
	}

	/**
	 * adds some extra headers such as configuration-context to the exchange such as
	 * loop based on the subscribers who matches the subscription-criteria with
	 * subscriberId
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithSubscriptionId() throws Exception {
		exchange.getIn().setBody(DEMO_LEAP_DATA);
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());
		addConfigurationUtil();

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertFalse("exchange header should not be contain leapDataContext key :: ",
				headers.containsKey(LEAP_DATA_CONTEXT));
		Assert.assertFalse("exchange header should not be contain SubscriberEvent key :: ",
				headers.containsKey("SubscriberEvent"));

		evaluationProcessor.process(exchange);

		headers = exchange.getIn().getHeaders();
		Assert.assertTrue("exchange header should be contain leapDataContext key :: ",
				headers.containsKey(LEAP_DATA_CONTEXT));
		Assert.assertEquals("LeapDataContext class should be same as expected :: ", LeapDataContext.class,
				headers.get(LEAP_DATA_CONTEXT).getClass());

		Assert.assertTrue("exchange header should be contain SubscriberEvent key :: ",
				headers.containsKey("SubscriberEvent"));
		Assert.assertEquals("SubscriberEvent class should be same as expected :: ", SubscribeEvent.class,
				headers.get("SubscriberEvent").getClass());
	}

	/**
	 * adds some extra headers such as configuration-context to the exchange such
	 * as loop based on the subscribers who matches the subscription-criteria
	 * without subscriberId
	 * 
	 * @throws Exception
	 */
	@Test(expected = NonRetryableException.class)
	public void testProcessWithoutSubscriptionId() throws Exception {
		exchange.getIn().setBody(DEMO_LEAP_DATA);
		addConfigurationUtil();
		evaluationProcessor.process(exchange);
	}

	@AfterClass
	public static void cleanUp() {
		new DeleteConfigNodeForTesting();
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION
				+ "-TestKafka";
	}

	private void addConfigurationUtil() {
		EventFramework subscription = LeapCoreTestFileRead.getKafkaSubscription();
		SubscribeEvent subscribeEvent = subscription.getEventSubscription().getSubscribeEvent().get(0);

		EventFrameworkConfigurationUnit configurationUnit = new EventFrameworkConfigurationUnit(TEST_TENANT, TEST_SITE,
				TEST_VENDOR_NODEID, false, subscribeEvent);

		try {
			LeapConfigurationServer.getConfigurationService().addConfiguration(configurationUnit);
		} catch (ConfigServerInitializationException e) {
			e.printStackTrace();
		}
	}
}
