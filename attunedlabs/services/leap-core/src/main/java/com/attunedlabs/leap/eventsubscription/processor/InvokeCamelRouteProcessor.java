package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.exception.RouteInvocationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.google.common.collect.Multiset.Entry;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * <code>SubscriberRoutingRuleEvaluationProcessor</code> will apply the
 * routing-rule specified by the particular subscriber and forms the new
 * exchange which is forwarded to the routeEndpoint configured by the subscriber
 * if the rule evaluates successfully else drops the message for the following
 * subscriber.
 * 
 * @author Reactiveworks42
 *
 */
public class InvokeCamelRouteProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(InvokeCamelRouteProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public InvokeCamelRouteProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	/**
	 * adds some extra headers based on the result of rule evaluation on the event
	 * message present the exchange and based on the attributes configured by
	 * Subscriber to invoke feature specific camel endpoint.
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()",LEAP_LOG_KEY,getClass().getName(), methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String subscriptionId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();

		List<String> configParams = Arrays
				.asList(subscriptionId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));

		if (configParams.size() - 1 == 5) {
			String featureGroup = configParams.get(0);
			String featureName = configParams.get(1);
			String implementation = configParams.get(2);
			String vendor = configParams.get(3);
			String version = configParams.get(4);

			if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
				throw new RouteInvocationException(
						"TENANT/SITE DOESN'T EXISTS :tenantId and siteId not found in eventHeaders of "
								+ "route invokation failed to load...");

			serviceDataContext.setFeatureGroup(featureGroup);
			serviceDataContext.setFeatureName(featureName);
			serviceDataContext.setImplementationName(implementation);
			serviceDataContext.setVendor(vendor);
			serviceDataContext.setVersion(version);
			serviceDataContext.setEndpointType("HTTP-JSON");
		}

		try {
			if (exchange.getIn() != null) {
				Map<String, Object> getInHeaders = exchange.getIn().getHeaders();
				log.trace("{} Exchange Headers before getOut are ::{}",LEAP_LOG_KEY, getInHeaders);
				Message outMessage = exchange.getOut();
				Set<java.util.Map.Entry<String, Object>> entrySet = getInHeaders.entrySet();
				for (java.util.Map.Entry<String, Object> singleEntry : entrySet) {
					outMessage.setHeader(singleEntry.getKey(), singleEntry.getValue());
				}
				log.trace("{} all headers after getOut aree::{} ",LEAP_LOG_KEY, outMessage.getHeaders());
				// get the data from the exchange.
				JSONObject eventBody = subscriptionUtil.identifyContentType(exchange.getIn().getBody(String.class));

				InvokeCamelRoute invokeCamelRoute = exchange.getIn()
						.getHeader(SubscriptionConstant.INVOKE_CAMEL_ROUTE_KEY, InvokeCamelRoute.class);

				// holds the feature-info about the
				// particular route.
				Map<String, HashMap<String, String>> routeInfoMap = new HashMap<>();
				routeInfoMap = subscriptionUtil.addExtraHeadersToEndpoint(routeInfoMap, invokeCamelRoute);

				if (invokeCamelRoute != null) {
					String routeEndpoint = invokeCamelRoute.getValue().trim();
					log.trace("{} camel endpoint need to verify:{} ",LEAP_LOG_KEY, routeEndpoint);
					HashMap<String, String> invokeCamelRouteMap = routeInfoMap
							.get(SubscriptionConstant.INVOKE_ENDPOINT_KEY);

					boolean containsFeatureCallAttr = subscriptionUtil.mapCheck(invokeCamelRouteMap);
					Endpoint endpoint = null;

					if (routeEndpoint != null && !routeEndpoint.isEmpty())
						endpoint = exchange.getContext().hasEndpoint(routeEndpoint);

					log.trace("{} camel endpoint verified to call :{} ",LEAP_LOG_KEY, endpoint);

					outMessage.setBody(eventBody);

					// if the routeInvocation
					// contains feature-attributes than add that in header
					// else.
					if (containsFeatureCallAttr) {
						outMessage.setHeader(LeapHeaderConstant.SERVICENAME_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.SERVICENAME_KEY));
						outMessage.setHeader(LeapHeaderConstant.FEATURE_GROUP_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.FEATURE_GROUP_KEY));
						outMessage.setHeader(LeapHeaderConstant.FEATURE_KEY,
								invokeCamelRouteMap.get(LeapHeaderConstant.FEATURE_KEY));
						// if entry route is not mentioned than add as
						// default.
						if (endpoint != null)
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, routeEndpoint);
						else
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY,
									SubscriptionConstant.ENTRY_ROUTE_FOR_SUBSCIBER);

					} else {
						if (endpoint != null)
							outMessage.setHeader(SubscriptionConstant.ROUTE_ENDPOINT_KEY, routeEndpoint);
						else
							throw new RouteInvocationException(
									"NO CONSUMER-ENDPOINT AVAILABLE:- the route endpoint mentioned to invoke doesn't exist ==> "
											+ routeEndpoint);
					}
				} else
					throw new RouteInvocationException(
							"NO INVOKE_CAMEL_ROUTE ACTION FOUND :- No route to invoke the either specify serviceAttributes or mention endpointConsumer correctly");
				log.info("{} CamelExchange before call: Headers => {} ",LEAP_LOG_KEY, exchange.getOut().getHeaders());
				log.info("{} CamelExchange before call: BODY => {} ",LEAP_LOG_KEY, exchange.getOut().getBody());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (exchange.hasOut())
				exchange.getOut().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			else
				exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);

		}
	}
}
