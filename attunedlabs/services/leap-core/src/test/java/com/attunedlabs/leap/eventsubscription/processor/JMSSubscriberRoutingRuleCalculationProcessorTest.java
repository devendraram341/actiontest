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
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;

public class JMSSubscriberRoutingRuleCalculationProcessorTest {

	private JMSSubscriberRoutingRuleCalculationProcessor ruleCalculationProcessor;
	private IEventFrameworkConfigService configService = new EventFrameworkConfigService();
	private Exchange exchange;

	@Before
	public void setUp() {
		if (ruleCalculationProcessor == null)
			ruleCalculationProcessor = new JMSSubscriberRoutingRuleCalculationProcessor(configService,
					LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithSubscribeEvent() throws Exception {
		exchange.getIn().setHeader("JMSSubscriberEvent", setSubscribeEvent());
		ruleCalculationProcessor.process(exchange);

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Exchange Header should not be null :: ", headers);
		Assert.assertFalse("Pre_stage data Should be True ::", (boolean) headers.get("PRE_STAGE"));
		Assert.assertTrue("Routing Rule Loop count should be grater then Zero ::",
				(int) headers.get("routingRuleLoopCount") > 0);
	}

	/**
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

	private JMSSubscribeEvent setSubscribeEvent() {
		EventFramework eventFramework = LeapCoreTestFileRead.getJmsSubscription();
		return eventFramework.getEventSubscription().getJmsSubscribeEvent().get(0);
	}

}
