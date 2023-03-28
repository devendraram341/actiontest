/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class JMSSubscriptionFailureHandlerBeanTest {

	private JMSSubscriptionFailureHandlerBean jmsSubFailHandleBean;
	private Exchange exchange;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (jmsSubFailHandleBean == null)
			jmsSubFailHandleBean = new JMSSubscriptionFailureHandlerBean();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createJmsExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * this method use for handle if JMSSubscription is failure with JMS
	 * SubscriberData.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithSubscriberData() throws Exception {
		exchange.setProperty("CamelExceptionCaught", setException());
		exchange.getIn().setBody("demo");
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		jmsSubFailHandleBean.process(exchange);
	}

	/**
	 * this method use for handle if JMSSubscription is failure without JMS
	 * SubscriberData.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithoutSubscriberData() throws Exception {
		exchange.setProperty("CamelExceptionCaught", setException());
		exchange.getIn().setBody("demo");
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		jmsSubFailHandleBean.process(exchange);
	}

	/**
	 * this method use for handle if JMSSubscription is failure without LDC.
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutLDC() throws Exception {
		jmsSubFailHandleBean.process(exchange);
	}

	private Exception setException() {
		Exception exception = new Exception("Test Custome Exception");
		return exception;
	}

}
