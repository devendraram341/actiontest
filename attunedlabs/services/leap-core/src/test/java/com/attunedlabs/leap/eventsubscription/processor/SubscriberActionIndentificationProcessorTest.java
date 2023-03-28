package com.attunedlabs.leap.eventsubscription.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.leap.LeapDataContext;

public class SubscriberActionIndentificationProcessorTest {

	private SubscriberActionIndentificationProcessor processor;
	private Exchange exchange;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() {
		if (processor == null)
			processor = new SubscriberActionIndentificationProcessor(LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * adds some extra headers in the exchange propagated which define where to
	 * route based on then rule With set Exchange Property.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithExchangeProperty() throws Exception {
		exchange.setProperty("CamelLoopIndex", 0);
		LeapCoreTestUtils.setServiceContextWithSubscribeData(leapDataCtx);
		exchange.getIn().setBody(DEMO_JSON_DATA);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setHeader("SubscriberEvent", setSubscribeEvent());

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull(headers);
		Assert.assertNull(headers.get("Action"));

		processor.process(exchange);

		headers = exchange.getIn().getHeaders();
		Assert.assertNotNull(headers.get("Action"));
		Assert.assertEquals("InvokeCamelRoute", headers.get("Action"));
	}

	/**
	 * adds some extra headers in the exchange propagated which define where to
	 * route based on then rule Without set Exchange Property.
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutExchangeProperty() throws Exception {
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setBody(DEMO_JSON_DATA);
		exchange.getIn().setHeader("SubscriberEvent", setSubscribeEvent());

		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull(headers);
		Assert.assertNull(headers.get("Action"));

		processor.process(exchange);
	}

	/**
	 * adds some extra headers in the exchange propagated which define where to
	 * route based on then rule Without set jmsSubscriberEvent
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutJMSSubscriberEvent() throws Exception {
		exchange.setProperty("CamelLoopIndex", 0);
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setBody(DEMO_JSON_DATA);

		processor.process(exchange);
	}

	private SubscribeEvent setSubscribeEvent() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getKafkaSubscription();
		System.out.println(
				"jmsSubscriptionjmsSubscription " + jmsSubscription.getEventSubscription().getSubscribeEvent());
		return jmsSubscription.getEventSubscription().getSubscribeEvent().get(0);
	}

}
