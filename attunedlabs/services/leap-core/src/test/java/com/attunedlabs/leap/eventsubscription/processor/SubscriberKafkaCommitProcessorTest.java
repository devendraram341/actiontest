package com.attunedlabs.leap.eventsubscription.processor;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

import com.attunedlabs.LeapCoreTestUtils;

public class SubscriberKafkaCommitProcessorTest {

	private SubscriberKafkaCommitProcessor commitProcessor;
	private Exchange exchange;

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);
	private static EmbeddedKafkaBroker embeddedKafkaBroker = embeddedKafka.getEmbeddedKafka();

	@Before
	public void setUp() {
		if (commitProcessor == null)
			commitProcessor = new SubscriberKafkaCommitProcessor();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess() throws Exception {
		exchange.getIn().setHeader("CamelKafkaManualCommit", LeapCoreTestUtils.setConsumerKafka(embeddedKafkaBroker));
		commitProcessor.process(exchange);
	}

}
