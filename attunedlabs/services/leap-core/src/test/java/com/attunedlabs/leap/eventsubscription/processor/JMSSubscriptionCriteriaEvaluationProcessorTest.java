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
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.leap.LeapDataContext;

public class JMSSubscriptionCriteriaEvaluationProcessorTest {

	private JMSSubscriptionCriteriaEvaluationProcessor evaluationProcessor;
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
			evaluationProcessor = new JMSSubscriptionCriteriaEvaluationProcessor(configService,
					LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createJmsExchange();
	}

	/**
	 * adds some extra headers such as configuration-context to the exchange such as
	 * loop based on the subscribers who matches the subscription-criteria with
	 * SubscriberId.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithSubscriptionId() throws Exception {
		exchange.getIn().setBody(DEMO_LEAP_DATA);
		exchange.getIn().setHeader("JMSDestination", "testJmsTopicName");
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());

		addConfigurationUtil();

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertFalse("exchange header should not be contain kafka.OFFSET key :: ",
				headers.containsKey("kafka.OFFSET"));
		Assert.assertFalse("exchange header should not be contain kafka.PARTITION key :: ",
				headers.containsKey("kafka.PARTITION"));
		Assert.assertFalse("exchange header should not be contain leapDataContext key :: ",
				headers.containsKey(LEAP_DATA_CONTEXT));
		evaluationProcessor.process(exchange);

		headers = exchange.getIn().getHeaders();

		Assert.assertTrue("exchange header should be contain kafka.OFFSET key :: ",
				headers.containsKey("kafka.OFFSET"));
		Assert.assertNotNull("kafka.OFFSET value should not be null ::", headers.get("kafka.OFFSET"));
		Assert.assertTrue("exchange header should be contain kafka.PARTITION key :: ",
				headers.containsKey("kafka.PARTITION"));
		Assert.assertEquals("kafka.PARTITION value should be zero (0) ::", 0, headers.get("kafka.PARTITION"));
		Assert.assertTrue("exchange header should be contain leapDataContext key :: ",
				headers.containsKey(LEAP_DATA_CONTEXT));
		Assert.assertEquals("LeapDataContext class should be same :: ", LeapDataContext.class,
				headers.get(LEAP_DATA_CONTEXT).getClass());
	}

	/**
	 * adds some extra headers such as configuration-context to the exchange such as
	 * loop based on the subscribers who matches the subscription-criteria without
	 * subscriberId
	 * 
	 * @throws Exception
	 */
	@Test(expected = NonRetryableException.class)
	public void testProcessWithoutSubscriptionId() throws Exception {
		exchange.getIn().setBody(DEMO_LEAP_DATA);
		exchange.getIn().setHeader("JMSDestination", "testJmsTopicName");
		addConfigurationUtil();
		evaluationProcessor.process(exchange);

	}

	@AfterClass
	public static void tearDownAfterClass() {
		new DeleteConfigNodeForTesting();
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION
				+ "-TestJMS";
	}

	private void addConfigurationUtil() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		JMSSubscribeEvent jmsSubscribeEvent = jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0);

		EventFrameworkConfigurationUnit configurationUnit = new EventFrameworkConfigurationUnit(TEST_TENANT, TEST_SITE,
				TEST_VENDOR_NODEID, false, jmsSubscribeEvent);

		try {
			LeapConfigurationServer.getConfigurationService().addConfiguration(configurationUnit);
		} catch (ConfigServerInitializationException e) {
			e.printStackTrace();
		}
	}
}
