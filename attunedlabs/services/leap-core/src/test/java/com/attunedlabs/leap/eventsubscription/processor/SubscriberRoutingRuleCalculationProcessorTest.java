package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;

public class SubscriberRoutingRuleCalculationProcessorTest {

	private SubscriberRoutingRuleCalculationProcessor ruleCalculationProcessor;
	private IEventFrameworkConfigService configService = new EventFrameworkConfigService();
	private Exchange exchange;

	@Before
	public void setUp() {
		if (ruleCalculationProcessor == null)
			ruleCalculationProcessor = new SubscriberRoutingRuleCalculationProcessor(configService,
					LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
	}

	/**
	 * adds some extra headers based on the result of exchange which is the number
	 * of subsciber routing rules count and the subsciption configuration with
	 * SubscribeEvent.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithSubscribeEvent() throws Exception {
		exchange.getIn().setHeader("SubscriberEvent", setSubscribeEvent());
		ruleCalculationProcessor.process(exchange);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Exchange Header should not be null :: ", headers);
		Assert.assertFalse("Pre_stage data Should be True ::", (boolean) headers.get("PRE_STAGE"));
		Assert.assertTrue("Routing Rule Loop count should be grater then Zero ::",
				(int) headers.get("routingRuleLoopCount") > 0);
	}

	/**
	 * adds some extra headers based on the result of exchange which is the number
	 * of subsciber routing rules count and the subsciption configuration without
	 * subscribe Event.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithoutSubscribeEvent() throws Exception {
		ruleCalculationProcessor.process(exchange);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Exchange Header should not be null :: ", headers);
		Assert.assertFalse("Pre_stage data Should be True ::", (boolean) headers.get("PRE_STAGE"));
		Assert.assertFalse("Routing Rule Loop count should be grater then Zero ::",
				(int) headers.get("routingRuleLoopCount") > 0);
	}

	private SubscribeEvent setSubscribeEvent() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getKafkaSubscription();
		System.out.println(
				"jmsSubscriptionjmsSubscription " + jmsSubscription.getEventSubscription().getSubscribeEvent());
		return jmsSubscription.getEventSubscription().getSubscribeEvent().get(0);
	}

}
