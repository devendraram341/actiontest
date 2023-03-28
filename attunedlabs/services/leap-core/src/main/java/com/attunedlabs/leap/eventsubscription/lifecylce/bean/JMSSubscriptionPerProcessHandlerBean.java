/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.lifecylce.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import javax.jms.Message;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriptionPerProcessHandlerBean implements Processor {

	final Logger log = LoggerFactory.getLogger(JMSSubscriptionPerProcessHandlerBean.class);
	Logger perflog = LoggerFactory.getLogger("performanceLog");

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
		AbstractSubscriptionRetryStrategy.processBean(exchange);
		JmsMessage jmsMessage = (JmsMessage) exchange.getIn();
		Message message = jmsMessage.getJmsMessage();
		message.acknowledge();
		log.debug("{} exiting from the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}
