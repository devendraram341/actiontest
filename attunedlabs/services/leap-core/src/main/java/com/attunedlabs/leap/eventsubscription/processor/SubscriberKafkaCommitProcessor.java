package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriberKafkaCommitProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(SubscriberKafkaCommitProcessor.class);
	Logger perflog = LoggerFactory.getLogger("performanceLog");

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		Map<String, Object> headers = exchange.getIn().getHeaders();
		log.trace("{} all headers ::{}", LEAP_LOG_KEY, headers);
		KafkaManualCommit manual = exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
		log.trace("{} manual headers ::{}", LEAP_LOG_KEY, manual);
		exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, manual);
		if (manual != null) {
			long commitStartTime = System.currentTimeMillis();
			manual.commitSync();
			long commitEndTime = System.currentTimeMillis();
			perflog.info("{} Time taken to commit the record : {}", LEAP_LOG_KEY, (commitEndTime - commitStartTime));
			log.debug("manual commit is done {}", LEAP_LOG_KEY);
		}

	}

}
