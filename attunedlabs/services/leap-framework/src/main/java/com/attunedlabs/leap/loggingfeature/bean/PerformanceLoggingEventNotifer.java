package com.attunedlabs.leap.loggingfeature.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.EventObject;

import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;

/**
 * <code>PerformanceLoggingEventNotifer</code> used for aggregating the time
 * computed for each individual bean for single request if feature has
 * enabledPerformance Logging.
 * 
 * @author Reactiveworks42
 *
 */
public class PerformanceLoggingEventNotifer extends EventNotifierSupport {
	LoggingFeatureUtilitiesBean loggingFeatureUtilitiesBean = new LoggingFeatureUtilitiesBean();

	public void notify(EventObject event) throws Exception {

		// react only when its the sent event
		if (event instanceof ExchangeSentEvent) {
			ExchangeSentEvent sent = (ExchangeSentEvent) event;
			log.info("{} Took {} millis to send to: {} from: {} key : {} uri : ",LEAP_LOG_KEY, sent.getTimeTaken(), sent.getEndpoint(),
					sent.getSource(), sent.getEndpoint().getEndpointKey(), sent.getEndpoint().getEndpointUri());
			// // aggregating time with current service.
			 loggingFeatureUtilitiesBean.addComponentsToGrid(sent);
		}

	}

	// only care about ExchangeSentEvent.
	public boolean isEnabled(EventObject event) {
		return event instanceof ExchangeSentEvent;
	}

	protected void doStart() throws Exception {
	}

	protected void doStop() throws Exception {
	}

}