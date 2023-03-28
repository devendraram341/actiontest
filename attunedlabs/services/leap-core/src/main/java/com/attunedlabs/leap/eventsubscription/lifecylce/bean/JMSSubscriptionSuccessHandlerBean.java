package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.Map;

import javax.jms.Message;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * <code>JMSSubscriptionSuccessHandlerBean</code> marks the records as SUCCESS
 * after performing the action successfully.
 * 
 * @author Reactiveworks
 *
 */
public class JMSSubscriptionSuccessHandlerBean implements Processor {
	final Logger log = LoggerFactory.getLogger(JMSSubscriptionSuccessHandlerBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);

		if (leapDataContext != null) {
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> subscriberData = serviceDataContext.getSubscriberDataFromServiceContext();
			log.trace("{} subscriberData inside JMSSubscriptionSuccessHandlerBean ::{}", LEAP_LOG_KEY, subscriberData);
			if (subscriberData != null) {
				AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) subscriberData
						.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
				abstractRetryStrategyBean.onSuccess(exchange, subscriberData);
				return;
			}
			JmsMessage jmsMessage = (JmsMessage) exchange.getIn();
			Message message = jmsMessage.getJmsMessage();
			message.acknowledge();
		}
		log.warn("{} leapHeader or genricData is missing in exchange ..failed to find any retrystrategy", LEAP_LOG_KEY);
		log.debug("{} exiting from the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}
