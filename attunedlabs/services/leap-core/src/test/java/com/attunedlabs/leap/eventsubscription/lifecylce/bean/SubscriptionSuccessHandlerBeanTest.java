package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.LeapCoreTestConstant.*;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class SubscriptionSuccessHandlerBeanTest {

	private SubscriptionSuccessHandlerBean successHandlerBean;
	private static Exchange exchange;
	private LeapDataContext leapDataCtx;

	@BeforeClass
	public static void init() throws Exception {
		System.setProperty(PROFILE_ID, LOCAL);
		exchange = LeapCoreTestUtils.createExchange();
	}

	@Before
	public void setUp() {
		if (successHandlerBean == null)
			successHandlerBean = new SubscriptionSuccessHandlerBean();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * this method use for handle if Subscription is success with SubscriberData.
	 */
	@Test
	public void testProcess() throws Exception {
		LeapCoreTestUtils.setServiceContextWithSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		successHandlerBean.process(exchange);
	}
}
