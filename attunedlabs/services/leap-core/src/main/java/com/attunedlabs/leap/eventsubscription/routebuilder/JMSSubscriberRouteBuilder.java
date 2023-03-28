/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Set;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;
import com.attunedlabs.eventsubscriptiontracker.impl.JMSEventSubscriptionTrackerImpl;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.JMSSubscriptionFailureHandlerBean;
import com.attunedlabs.leap.eventsubscription.processor.JMSSubscriptionCriteriaEvaluationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberKafkaCommitProcessor;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriberRouteBuilder extends RouteBuilder {

	final static Logger log = LoggerFactory.getLogger(JMSSubscriberRouteBuilder.class);
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final IEventSubscriptionTrackerService eventSubscriptionLogService = new JMSEventSubscriptionTrackerImpl();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();

	@Override
	public void configure() throws Exception {
		String methodName = "configure";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), LEAP_LOG_KEY, methodName);
		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();

		Set<String> subscribers = leapConfigurationServer.getAllJMSSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			log.debug("{} these are all the subscribers available of JMSSubscriberBuilder {}", LEAP_LOG_KEY,
					subscribers);
			eventSubscriptionLogService.createTrackerTableForSubscription();

			for (final String subscriptionId : subscribers) {
				// got all the subscriberTopics based on subscibeId
				String queueName = eventFrameworkConfigService.getSubscriptionQueuebySubscriptionId(subscriptionId);
				log.trace("{} subscriber processing {}  subscribing topic's {} ", LEAP_LOG_KEY, subscriptionId,
						queueName);
				// for getting the jms event configuration from cache
				ConfigurationContext configCtx = subscriptionUtil.buildConfigContext(
						LeapDataContextConstant.GLOBAL_TENANT_ID, LeapDataContextConstant.GLOBAL_SITE_ID,
						subscriptionId);
				log.trace("{} ConfigurationContext formed {}", LEAP_LOG_KEY, configCtx);
				if (configCtx == null)
					throw new MissingConfigurationException(
							"MISSING CONFIGURATION PARAMETERS: for subscriber with Id : " + subscriptionId
									+ " ==> expected parameters are not present to build ConfigurationContext!");
				JMSSubscribeEvent jmsEventSubscriptionConfiguration = eventFrameworkConfigService
						.getJMSEventSubscriptionConfiguration(configCtx,
								subscriptionUtil.getActualSubscriberId(subscriptionId));
				log.trace("{} jmsEventSubscriptionConfiguration :: {}", LEAP_LOG_KEY,
						jmsEventSubscriptionConfiguration);
				RouteDefinition startJMSSubscriberRouteEndpoint = null;

				startJMSSubscriberRouteEndpoint = from(SubscriptionUtil.constructJMSConsumeQueueURI(queueName,
						subscriptionId, jmsEventSubscriptionConfiguration))
								.process(new SubscriberKafkaCommitProcessor());
				startJMSSubscriberRouteEndpoint.onException(Exception.class).handled(true)
						.process(new JMSSubscriptionFailureHandlerBean()).end()
						.setProperty(SubscriptionConstant.IS_JMS_SUBSCRIPTION_INVOCATION_KEY).constant(true)
						.process(new JMSMessageProcessingWay(eventFrameworkConfigService, subscriptionUtil))
						// Subscription criteria evaluation
						.process(new JMSSubscriptionCriteriaEvaluationProcessor(eventFrameworkConfigService,
								subscriptionUtil))
						.toD("${header." + SubscriptionConstant.PROCESSING_DECISION_KEY + "}");
			}

		}
		log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), LEAP_LOG_KEY, methodName);
	}

}
