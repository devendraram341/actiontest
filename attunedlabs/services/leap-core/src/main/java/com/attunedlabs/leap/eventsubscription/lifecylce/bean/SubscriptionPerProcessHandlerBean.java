package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;

/**
 * <code>SubscriptionPerProcessHandlerBean</code> initialized the configuration
 * and stores the retry implementation in cache.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionPerProcessHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionFailureHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		AbstractSubscriptionRetryStrategy.processBean(exchange);
		KafkaManualCommit manual = exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
		if (manual != null) {
			manual.commitSync();
			log.trace("{} manual commit is done", LEAP_LOG_KEY);
		}
		log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}