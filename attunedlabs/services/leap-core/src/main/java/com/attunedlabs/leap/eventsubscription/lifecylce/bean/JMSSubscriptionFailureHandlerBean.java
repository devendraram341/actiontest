/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

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
import com.attunedlabs.leap.eventsubscription.routebuilder.GenericJMSRetryRouteBuilder;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriptionFailureHandlerBean implements Processor {

	final Logger log = LoggerFactory.getLogger(JMSSubscriptionFailureHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}(), body:- {}",LEAP_LOG_KEY, getClass().getName(), methodName,
				exchange.getIn().getBody());
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		if (leapDataContext != null) {
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> subscriberData = serviceDataContext.getSubscriberDataFromServiceContext();
			log.trace("{} subscriberData inside JMSSubscriptionFailureHandlerBean :: {}",LEAP_LOG_KEY, subscriberData);
			if (subscriberData != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) subscriberData
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				if (abstractRetryStrategyBean == null) {
					log.debug("{} abstractRetryStrategyBean is null ");
					return;
				}
				abstractRetryStrategyBean.onFailure(exchange, subscriberData, exception);
				log.trace("{} after on failure call inside JMSSubscriptionFailureHandlerBean ");

				// deleting from retry list
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) subscriberData
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
				log.trace("{} after on failure call inside JMSSubscriptionFailureHandlerBean eventSubscriptionTracker {}",LEAP_LOG_KEY,
						eventSubscriptionTracker);
				if (eventSubscriptionTracker != null) {
					int size = GenericJMSRetryRouteBuilder.totalRetryableRecords.size();
					log.trace("{}  total retry records size ::{} ",LEAP_LOG_KEY, GenericJMSRetryRouteBuilder.totalRetryableRecords);
					log.trace("{}  total retry records exists or not  :: {}",LEAP_LOG_KEY,
							GenericJMSRetryRouteBuilder.totalRetryableRecords.contains(eventSubscriptionTracker));
					for (int i = 0; i < size; i++) {
						EventSubscriptionTracker eventSubscriptionTrackerFromRetrybleRecords = GenericJMSRetryRouteBuilder.totalRetryableRecords
								.get(i);
						String tenantId = eventSubscriptionTrackerFromRetrybleRecords.getTenantId();
						String siteId = eventSubscriptionTrackerFromRetrybleRecords.getSiteId();
						String subscriptionId = eventSubscriptionTrackerFromRetrybleRecords.getSubscriptionId();
						String partition = eventSubscriptionTrackerFromRetrybleRecords.getPartition();
						String offset = eventSubscriptionTrackerFromRetrybleRecords.getOffset();
						if (tenantId.equalsIgnoreCase(eventSubscriptionTracker.getTenantId())
								&& siteId.equalsIgnoreCase(eventSubscriptionTracker.getSiteId())
								&& subscriptionId.equalsIgnoreCase(eventSubscriptionTracker.getSubscriptionId())
								&& partition.equalsIgnoreCase(eventSubscriptionTracker.getPartition())
								&& offset.equalsIgnoreCase(eventSubscriptionTracker.getOffset())) {
							GenericJMSRetryRouteBuilder.totalRetryableRecords.remove(i);
							log.trace(LEAP_LOG_KEY+" DELETING \n" + eventSubscriptionTracker
									+ "\n GenericRetryRouteBuilder.totalRetryableRecords \n"
									+ GenericJMSRetryRouteBuilder.totalRetryableRecords.size());
						}
					}
				}
				log.debug("{} exiting from the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
				return;
			}
		} else
			throw exception;

		log.debug("{} exiting from the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
		log.warn("{} leapHeader or genricData is missing in exchange ..failed to find any retrystrategy",LEAP_LOG_KEY);

	}

}
