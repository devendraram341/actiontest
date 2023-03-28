package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationUnit;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.leap.LeapHeaderConstant;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageProcessingWayDeciderTest {

	private IEventFrameworkConfigService configService = new EventFrameworkConfigService();
	private Properties properties = new Properties();
	private MessageProcessingWayDecider processingWayDecider;
	private Exchange exchange;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);
	private static EmbeddedKafkaBroker embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (processingWayDecider == null)
			processingWayDecider = new MessageProcessingWayDecider(configService,
					LeapCoreTestUtils.setSubscriptionUtil(), properties);
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
	}

	/**
	 * this method use for kafka message Processing Way decider with SubscritionId
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBProcessWithSubId() throws Exception {
		LeapHeaderConstant.tenant = TEST_TENANT;
		LeapHeaderConstant.site = TEST_SITE;
		addConfigurationUtil();
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());
		processingWayDecider.process(exchange);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertTrue("Exchange header should be contain kafkaComponentInvocation key ::",
				headers.containsKey("kafkaComponentInvocation"));
		Assert.assertTrue("kafka Component Invocation key value should be true ::",
				Boolean.valueOf(headers.get("kafkaComponentInvocation").toString()));
		Assert.assertTrue("Exchange header should be contain PRE_STAGE key ::", headers.containsKey("PRE_STAGE"));
		Assert.assertTrue("PRE_STAGE key value should be true ::",
				Boolean.valueOf(headers.get("PRE_STAGE").toString()));
		Assert.assertTrue("Exchange header should be contain processingDecision key ::",
				headers.containsKey("processingDecision"));
		Assert.assertNotNull("processingDecision key value should not be null ::", headers.get("processingDecision"));
	}

	/**
	 * this method use for kafka message Processing Way decider without
	 * SubscritionId
	 * 
	 * @throws Exception
	 */
	@Test(expected = NonRetryableException.class)
	public void testAProcessWithoutSubId() throws Exception {
		LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker);
		exchange.getIn().setHeader("CamelKafkaManualCommit", LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker));
		processingWayDecider.process(exchange);
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
