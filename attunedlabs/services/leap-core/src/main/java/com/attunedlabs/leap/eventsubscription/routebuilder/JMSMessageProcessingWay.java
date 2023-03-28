/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.routebuilder;

import javax.jms.Message;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.jms.JmsMessage;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * @author reactiveworks
 *
 */
public class JMSMessageProcessingWay implements Processor {

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public JMSMessageProcessingWay(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	@Override
	public void process(Exchange exchange) throws Exception {
		try {
			String subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);

			Endpoint fromEndpoint = exchange.getFromEndpoint();

			if (fromEndpoint instanceof JmsEndpoint)
				subscriberId = ((JmsEndpoint) fromEndpoint).getConfiguration().getSubscriptionName();

			exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriberId.trim());

			boolean configPresent = true;
			ConfigurationContext configCtx = subscriptionUtil.buildConfigContext(LeapHeaderConstant.tenant,
					LeapHeaderConstant.site, subscriberId);
			configPresent = (configCtx != null);
			JMSSubscribeEvent eventSubscription = null;
			if (configPresent) {
				try {
					eventSubscription = eventFrameworkConfigService.getJMSEventSubscriptionConfiguration(configCtx,
							subscriptionUtil.getActualSubscriberId(subscriberId));
				} catch (EventFrameworkConfigurationException e) {
					configPresent = false;
				}
				configPresent = (eventSubscription != null);

			}

			String simpleProcessingEndpoint = SubscriptionConstant.SIMPLE_PROCESSING_ROUTE_ENDPOINT + subscriberId;
			exchange.getIn().setHeader(SubscriptionConstant.PROCESSING_DECISION_KEY, simpleProcessingEndpoint);

		} catch (Exception e) {
			e.printStackTrace();
			JmsMessage jmsMessage = (JmsMessage) exchange.getIn();
			Message message = jmsMessage.getJmsMessage();
			message.acknowledge();
			throw new NonRetryableException(
					"MANUAL COMMIT OFFSET : jms counter explicilty incremented.. no subscriber configuration available -> "
							+ e.getMessage());
		}
	}

}
