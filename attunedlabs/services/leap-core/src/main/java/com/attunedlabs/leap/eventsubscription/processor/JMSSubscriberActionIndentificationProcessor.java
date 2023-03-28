/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.EventRoutingRule;
import com.attunedlabs.eventframework.jaxb.EventRoutingRules;
import com.attunedlabs.eventframework.jaxb.HttpPostRequest;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.Pipeline;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriberActionIndentificationProcessor implements Processor {

	final static Logger log = LoggerFactory.getLogger(JMSSubscriberActionIndentificationProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public JMSSubscriberActionIndentificationProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	/**
	 * adds some extra headers in the exchange propagated which define where to
	 * route based on then rule.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		if (exchange.getIn() != null) {
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
			LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
			Map<String, Object> subscriberData = serviceDataContext.getSubscriberDataFromServiceContext();
			if (subscriberData.containsKey(SubscriptionConstant.DEFAULT_ASSIGNED_TENANT_KEY))
				throw new MissingConfigurationException(
						"TENANT/SITE DOESN'T EXISTS :Event Data will not be processed because \n "
								+ "tenantId and siteId not found in " + EventFrameworkConstants.METADATA_KEY
								+ " of event data \n" + "(Used Default GLOBAL tenant for logging purpose)...");
			if (subscriberData.containsKey(SubscriptionConstant.NO_CONFIG_FOR_TENANT_KEY))
				throw new MissingConfigurationException(
						"NO SUBSCRIBER CONFIG FOUND FOR TENANT :Event Data will not be processed because \n "
								+ "No configuration found particular tenant in memory(Loaded the configuration from "
								+ "GLOBAL tenant for logging purpose)...");

			String orignalRequest = exchange.getIn().getBody(String.class);
			log.info("{} orignal request is: {}", LEAP_LOG_KEY, orignalRequest);
			// get the data from the exchange.
			JSONObject eventBody = subscriptionUtil.identifyContentType(orignalRequest);

			log.debug("{} all exchange headers:{} ", LEAP_LOG_KEY, exchange.getIn().getHeaders());
			log.debug("{} event data used for identification: {}", LEAP_LOG_KEY, eventBody);

			JMSSubscribeEvent eventSubscription = exchange.getIn()
					.getHeader(SubscriptionConstant.JMS_SUBSCIBER_EVENT_CONFIG_KEY, JMSSubscribeEvent.class);
			EventRoutingRules eventRoutingRules = eventSubscription.getEventRoutingRules();
			List<EventRoutingRule> totalEventRoutingRule = eventRoutingRules.getEventRoutingRule();

			// getting the subscription event routing rule on index for
			// particular Subscriber.
			EventRoutingRule eventRoutingRule = totalEventRoutingRule
					.get(exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY, Integer.class));

			log.trace("{} evaluating routing rule present at index {} for subsciber {}", LEAP_LOG_KEY,
					exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY), eventSubscription.getSubscriptionId());

			String mvelExpressionRule = eventRoutingRule.getRule();

			// if the rule is not specified than directly evaluate the routing
			// action part.
			boolean evaluateRuleOrNot = false;
			if (mvelExpressionRule == null || mvelExpressionRule.trim().isEmpty())
				evaluateRuleOrNot = true;
			else
				evaluateRuleOrNot = subscriptionUtil.evaluateMVELForCriteriaMatch(mvelExpressionRule, eventBody);

			// decide where to route
			if (evaluateRuleOrNot) {
				HttpPostRequest httpPostRequest = eventRoutingRule.getHttpPostRequest();
				InvokeCamelRoute invokeCamelRoute = eventRoutingRule.getInvokeCamelRoute();
				Pipeline pipeline = eventRoutingRule.getPipeline();

				if (httpPostRequest != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY,
							SubscriptionConstant.HTTP_POST_REQUEST_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.HTTP_POST_REQUEST_KEY, httpPostRequest);
					log.trace("{} ACTION :{} ", LEAP_LOG_KEY, SubscriptionConstant.HTTP_POST_REQUEST_KEY);
				} else if (invokeCamelRoute != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY,
							SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY, invokeCamelRoute);
					log.trace("{} ACTION :{} ", LEAP_LOG_KEY, SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY);
				} else if (pipeline != null) {
					exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY, SubscriptionConstant.PIPELINE_KEY);
					exchange.getIn().setHeader(SubscriptionConstant.PIPELINE_KEY, pipeline);
					log.trace("{} ACTION :{} ", LEAP_LOG_KEY, SubscriptionConstant.PIPELINE_KEY);
				}

			} else {
				exchange.getIn().setHeader(SubscriptionConstant.ACTION_KEY, SubscriptionConstant.DEFAULT_ACTION);
			}

		}

	}
}
