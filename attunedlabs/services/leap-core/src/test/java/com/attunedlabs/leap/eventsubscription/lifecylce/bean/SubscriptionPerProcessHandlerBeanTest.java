package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import static com.attunedlabs.LeapCoreTestConstant.*;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class SubscriptionPerProcessHandlerBeanTest {

	private SubscriptionPerProcessHandlerBean perProcessHandlerBean;
	private Exchange exchange;
	private LeapDataContext leapDataCtx;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);
	private static EmbeddedKafkaBroker embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (perProcessHandlerBean == null)
			perProcessHandlerBean = new SubscriptionPerProcessHandlerBean();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
	}

	/**
	 * This method use for handle preProcess for subscription with embeddedKafka.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithEmbeddedKafka() throws Exception {
		LeapCoreTestUtils.setServiceContextWithSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setHeader("CamelKafkaManualCommit", LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker));
		perProcessHandlerBean.process(exchange);
	}

	/**
	 * This method use for handle preProcess for subscription without embeddedKafka.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcessWithoutEmbeddedKafka() throws Exception {
		LeapCoreTestUtils.setServiceContextWithSubscribeData(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		perProcessHandlerBean.process(exchange);
	}

	/**
	 * This method use for handle preProcess for subscription with embeddedKafka and
	 * without subscriberData.
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testProcessWithoutSubscribeData() throws Exception {
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setHeader("CamelKafkaManualCommit", LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker));
		perProcessHandlerBean.process(exchange);
	}
}
