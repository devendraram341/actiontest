package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.LeapCoreTestConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.LeapCoreTestConstant.LOCAL;
import static com.attunedlabs.LeapCoreTestConstant.PROFILE_ID;

import javax.jms.JMSException;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class JMSSubscriptionSuccessHandlerBeanTest {

	private JMSSubscriptionSuccessHandlerBean jmsSubPreProcessBean;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() throws JMSException {

		System.setProperty(PROFILE_ID, LOCAL);
		if (jmsSubPreProcessBean == null)
			jmsSubPreProcessBean = new JMSSubscriptionSuccessHandlerBean();

		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * this method use for handle if JMSSubscription is success with Exchange
	 * message of JMSMessage.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithJMSMessageExchange() throws Exception {
		Exchange exchange = LeapCoreTestUtils.createJmsExchange();
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		jmsSubPreProcessBean.process(exchange);
	}

	/**
	 * this method use for handle if JMSSubscription is success without Exchange
	 * message of JMSMessage.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithDefaultExchange() throws Exception {
		Exchange exchange = LeapCoreTestUtils.createExchange();
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		jmsSubPreProcessBean.process(exchange);
	}

}
