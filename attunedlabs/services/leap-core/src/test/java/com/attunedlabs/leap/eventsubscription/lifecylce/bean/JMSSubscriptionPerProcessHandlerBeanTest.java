package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import javax.jms.JMSException;
import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class JMSSubscriptionPerProcessHandlerBeanTest {

	private JMSSubscriptionPerProcessHandlerBean jmsSubPreProcessBean;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() throws JMSException {
		System.setProperty(PROFILE_ID, LOCAL);
		if (jmsSubPreProcessBean == null)
			jmsSubPreProcessBean = new JMSSubscriptionPerProcessHandlerBean();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * This method use for handle preProcess for jmsSubscriotion with Exchange
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
	 * class cast exception because defaultMessage -> JmsMessage
	 * 
	 * @throws Exception
	 */
	@Test(expected = ClassCastException.class)
	public void testProcessWithDefaultExchange() throws Exception {
		Exchange exchange = LeapCoreTestUtils.createExchange();
		LeapCoreTestUtils.setServiceContextWithJmsSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		jmsSubPreProcessBean.process(exchange);
	}

	/**
	 * This method use for handle preProcess for jmsSubscriotion with Exchange
	 * message of JMSMessage and Without jmsSubscriber data..
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutSubscriberAndWithJMSMessageExchange() throws Exception {
		Exchange exchange = LeapCoreTestUtils.createJmsExchange();
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		jmsSubPreProcessBean.process(exchange);
	}

}
