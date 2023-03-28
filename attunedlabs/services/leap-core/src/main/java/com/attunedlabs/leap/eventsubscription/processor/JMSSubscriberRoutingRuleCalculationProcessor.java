package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.EventRoutingRule;
import com.attunedlabs.eventframework.jaxb.EventRoutingRules;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;

public class JMSSubscriberRoutingRuleCalculationProcessor implements Processor {

	final static Logger log = LoggerFactory.getLogger(JMSSubscriberRoutingRuleCalculationProcessor.class);

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public JMSSubscriberRoutingRuleCalculationProcessor(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		if (exchange.getIn() != null) {
			exchange.getIn().setHeader("PRE_STAGE", false);

			// initialize routing rules loop count
			exchange.getIn().setHeader(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY, 0);

			// getSubscription Event confiuration from header.
			JMSSubscribeEvent eventSubscription = exchange.getIn()
					.getHeader(SubscriptionConstant.JMS_SUBSCIBER_EVENT_CONFIG_KEY, JMSSubscribeEvent.class);

			if (eventSubscription != null) {
				log.trace("{} actual subscription Id {}", LEAP_LOG_KEY, eventSubscription.getSubscriptionId());

				EventRoutingRules eventRoutingRules = eventSubscription.getEventRoutingRules();
				if (eventRoutingRules != null) {
					List<EventRoutingRule> totalEventRoutingRule = eventRoutingRules.getEventRoutingRule();

					log.trace("{} total number of routing rules configured for subsciberId : {} are {}", LEAP_LOG_KEY,
							eventSubscription.getSubscriptionId(), totalEventRoutingRule.size());

					// setting subscriber routing rules count in exchange header.
					exchange.getIn().setHeader(SubscriptionConstant.ROUTING_RULES_PER_SUBSCIBER_LOOP_COUNT_KEY,
							totalEventRoutingRule.size());
				}
			}

		}
		log.debug("{} exiting from the method {}.{}()",LEAP_LOG_KEY, getClass().getName(), methodName);
	}

}
