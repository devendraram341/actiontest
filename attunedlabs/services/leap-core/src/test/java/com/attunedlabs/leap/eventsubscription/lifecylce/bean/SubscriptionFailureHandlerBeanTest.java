package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class SubscriptionFailureHandlerBeanTest {

	private SubscriptionFailureHandlerBean failureHandlerBean;
	private static Exchange exchange;
	private LeapDataContext leapDataCtx;

	@BeforeClass
	public static void init() throws Exception {
		System.setProperty(PROFILE_ID, LOCAL);
		exchange = LeapCoreTestUtils.createExchange();
	}

	@Before
	public void setUp() {
		if (failureHandlerBean == null)
			failureHandlerBean = new SubscriptionFailureHandlerBean();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * this method use for handle if Subscription is failure with SubscriberData.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess() throws Exception {
		exchange.setProperty("CamelExceptionCaught", setException());
		LeapCoreTestUtils.setServiceContextWithSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		failureHandlerBean.process(exchange);
	}

	private Exception setException() {
		Exception exception = new Exception("Test Custome Exception");
		return exception;
	}
}
