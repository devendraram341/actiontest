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
 * <code>SubscriptionFailureHandlerBean</code> marks the failed record in
 * subscriptionTracker table to FAILED. This class is also responsible to
 * perform retry on retryable type subscriptions if invoked through quartz.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriptionFailureHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(SubscriptionFailureHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		Boolean isPreStage = exchange.getIn().getHeader("PRE_STAGE", Boolean.class);
		// action is been invoked so assign false
		if (isPreStage == null)
			isPreStage = false;
		if (leapDataContext != null && !isPreStage) {
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> subscriberData = serviceDataContext.getSubscriberDataFromServiceContext();
			if (subscriberData != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) subscriberData
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				if (abstractRetryStrategyBean == null) {
					log.trace("{} abstractRetryStrategyBean is null ", LEAP_LOG_KEY);
					return;
				}
				abstractRetryStrategyBean.onFailure(exchange, subscriberData, exception);

				// deleting from retry list
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) subscriberData
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
				if (eventSubscriptionTracker != null)
					if (GenericRetryRouteBuilder.totalRetryableRecords.contains(eventSubscriptionTracker)) {
						GenericRetryRouteBuilder.totalRetryableRecords.remove(eventSubscriptionTracker);
						log.debug(LEAP_LOG_KEY + " DELETING \n" + eventSubscriptionTracker
								+ "\n GenericRetryRouteBuilder.totalRetryableRecords \n"
								+ GenericRetryRouteBuilder.totalRetryableRecords.size());
					}
				log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
				return;
			}
		} else
			throw exception;
		log.warn("{} leapHeader or genricData is missing in exchange ..failed to find any retrystrategy", LEAP_LOG_KEY);
		log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}
