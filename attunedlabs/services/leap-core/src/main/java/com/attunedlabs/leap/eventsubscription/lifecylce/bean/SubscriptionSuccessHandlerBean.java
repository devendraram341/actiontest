package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericRetryRouteBuilder;

/**
 * <code>SubscriptionSuccessHandlerBean</code> marks the records as SUCCESS
 * after performing the action successfully.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionSuccessHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionSuccessHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);

		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		if (leapDataContext != null) {
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> subscriberData = serviceDataContext.getSubscriberDataFromServiceContext();
			if (subscriberData != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) subscriberData
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				abstractRetryStrategyBean.onSuccess(exchange, subscriberData);

				// deleting from retry list
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) subscriberData
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
				if (eventSubscriptionTracker != null)
					if (GenericRetryRouteBuilder.totalRetryableRecords.contains(eventSubscriptionTracker)) {
						GenericRetryRouteBuilder.totalRetryableRecords.remove(eventSubscriptionTracker);
						log.trace("{} DELETING {} GenericRetryRouteBuilder.totalRetryableRecords {}", LEAP_LOG_KEY,
								eventSubscriptionTracker, GenericRetryRouteBuilder.totalRetryableRecords.size());
					}
				return;
			}
		}
		log.warn("{} leapHeader or genricData is missing in exchange ..failed to find any retrystrategy", LEAP_LOG_KEY);
		log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}
