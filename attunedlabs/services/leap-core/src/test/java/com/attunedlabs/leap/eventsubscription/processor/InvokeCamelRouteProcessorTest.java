package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.LeapCoreTestConstant.*;
import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.jaxb.EventFramework;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.leap.LeapDataContext;

public class InvokeCamelRouteProcessorTest {

	private InvokeCamelRouteProcessor camelRouteProcessor;
	private Exchange exchange;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() {
		if (camelRouteProcessor == null)
			camelRouteProcessor = new InvokeCamelRouteProcessor(LeapCoreTestUtils.setSubscriptionUtil());
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * adds some extra headers based on the result of rule evaluation on the event
	 * message present the exchange and based on the attributes configured by
	 * Subscriber to invoke feature specific camel endpoint with InvokeCamelRoute
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithInvokeCamelRoute() throws Exception {
		leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());
		exchange.getIn().setBody("{\"Test\":\"Evening\"}");
		exchange.getIn().setHeader("InvokeCamelRoute", getInvockCamelRoute());

		Object body = exchange.getOut().getBody();
		Assert.assertNull("Exchnage GetOut body Should be null ::", body);

		camelRouteProcessor.process(exchange);

		body = exchange.getOut().getBody();
		Assert.assertNotNull("Exchnage GetOut body Should be Not null ::", body);
		Assert.assertEquals("Body Data should be same as set exchange body ::", exchange.getIn().getBody().toString(),
				body.toString());
	}

	/**
	 * adds some extra headers based on the result of rule evaluation on the event
	 * message present the exchange and based on the attributes configured by
	 * Subscriber to invoke feature specific camel endpoint without LDC
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutLDC() throws Exception {
		exchange.getIn().setHeader(SUBSCRIPTION_ID, setSubscriptionId());
		exchange.getIn().setBody("{\"Test\":\"Evening\"}");
		exchange.getIn().setHeader("InvokeCamelRoute", getInvockCamelRoute());
		camelRouteProcessor.process(exchange);
	}

	private String setSubscriptionId() {
		return TEST_FEATUREGROUP + "-" + TEST_FEATURE + "-" + TEST_IMPL + "-" + TEST_VENDOR + "-" + TEST_VERSION + "-"
				+ TEST_PROVIDER;
	}

	private InvokeCamelRoute getInvockCamelRoute() {
		EventFramework jmsSubscription = LeapCoreTestFileRead.getJmsSubscription();
		return jmsSubscription.getEventSubscription().getJmsSubscribeEvent().get(0).getEventRoutingRules()
				.getEventRoutingRule().get(0).getInvokeCamelRoute();
	}

}
