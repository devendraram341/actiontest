
package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventsubscription.exception.RetryableException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;
import com.attunedlabs.eventsubscriptiontracker.impl.EventSubscriptionTrackerImpl;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionFailureHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionPerProcessHandlerBean;
import com.attunedlabs.leap.eventsubscription.lifecylce.bean.SubscriptionSuccessHandlerBean;
import com.attunedlabs.leap.eventsubscription.processor.SubscriberRoutingRuleCalculationProcessor;
import com.attunedlabs.leap.eventsubscription.processor.SubscriptionCriteriaEvaluationProcessor;
import com.attunedlabs.leap.util.LeapConfigurationUtil;

/**
 * <code>SubscriberRouteBuilder</code> route builder implementation for
 * consuming message from topic and decide whether to process parallely or
 * sequentially based on subscriber condfiuration.
 * {@link SubscriptionCriteriaEvaluationProcessor},{@link SubscriberRoutingRuleCalculationProcessor}.
 * 
 * @author Reactiveworks42
 *
 */
public class SubscriberRouteBuilder extends RouteBuilder {

	final static Logger log = LoggerFactory.getLogger(SubscriberRouteBuilder.class);
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	protected final IEventSubscriptionTrackerService eventSubscriptionLogService = new EventSubscriptionTrackerImpl();
	protected final static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
	int i = 0;
	private static Properties props;
	static {
		try {
			props = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		} catch (Exception e) {
			log.error("{} failed to load consumer properties...{}", LEAP_LOG_KEY,
					SubscriptionConstant.KAFKA_CONSUMER_CONFIGS);
		}
	}// ..end of static block to load the ConsumerProperties

	/**
	 * 
	 * Given below is the sample incoming message and headers consumed from the
	 * subscribed topic on which the subscription based on subscriber is applied.
	 * <ul>
	 * <li>you can either send json or xml request to the topic.</li>
	 * <li>If the request has metadata then we are not going to add it to request
	 * json.</li>
	 * <li>If the metadata key is not present then we are going to add metadata with
	 * default values to the request json.</li>
	 * </ul>
	 * <p>
	 * <i><strong>Example:</strong></i>
	 * </p>
	 * {"projectId":"ki78uy","projectName":"leap"}
	 * 
	 * <p>
	 * after adding metadata to the request:
	 * </p>
	 * 
	 * <pre>
	 * {
	"metadata": {
		"tenantId": "all",
		"siteId": "all"
	},
	"projectId": "ki78uy",
	"projectName": "leap"
	}
	 * </pre>
	 * 
	 */
	@Override
	public void configure() throws Exception {
		onException(Exception.class).handled(true).process(new SubscriptionFailureHandlerBean());

		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
		final Random randomClientId = new Random();
		from("direct:SubscriberGloablExceptionHandler").setHeader("PRE_STAGE").constant("false").process((e) -> {
			throw new RetryableException("unable to connect to jms");
		});
		// get the topics names from the subscription configured by
		// feature developer and subscribe to all the topics.
		Set<String> subscribers = leapConfigurationServer.getAllSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			log.debug("{} these are all the subscribers available {}", LEAP_LOG_KEY, subscribers);

			// subscribers are present check the table exists or not.
			eventSubscriptionLogService.createTrackerTableForSubscription();

			for (final String subscriptionId : subscribers) {
				long startTime = LeapConfigurationUtil.startTimeChecker("SubscriberRouteBuilder configure method  ");
				// got all the subscriberTopics based on subscibeId
				String topicNames = eventFrameworkConfigService.getSubscriptionTopicsbySubscriptionId(subscriptionId);
				String queueName = topicNames + "_queue";
				log.trace("{} subscriber processing {} subscribing topic's {}", LEAP_LOG_KEY, subscriptionId,
						topicNames);
				RouteDefinition startSubscriberRouteEndpoint = null;
				/********** KAFKA ENDPOINT ***********/
				startSubscriberRouteEndpoint = from(SubscriptionUtil.constructKafkaURI(eventFrameworkConfigService,
						topicNames, subscriptionId, subscriptionUtil, randomClientId))
								.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY).constant(true)
								.process(new MessageProcessingWayDecider(eventFrameworkConfigService, subscriptionUtil,
										props))
								// Subscription criteria evaluation
								.process(new SubscriptionCriteriaEvaluationProcessor(eventFrameworkConfigService,
										subscriptionUtil))
								// invoking pre-process activity from retry
								// lifecycle
								.process(new SubscriptionPerProcessHandlerBean());
			if (LeapConfigUtil.getGlobalPropertyValue(LeapConstants.JMS_INVOCATION,LeapDefaultConstants.DEFAULT_JMS_INVOCATION).equalsIgnoreCase("true")) {
					startSubscriberRouteEndpoint.doTry().to(SubscriptionUtil.constructJMSQueueURI(queueName))
							.process(new SubscriptionSuccessHandlerBean()).doCatch(Exception.class)
							.to("direct:SubscriberGloablExceptionHandler").end();
				} else {
					startSubscriberRouteEndpoint.toD("${header." + SubscriptionConstant.PROCESSING_DECISION_KEY + "}");
				}
				Long endTime = LeapConfigurationUtil.endTimeChecker("SubscriberRouteBuilder configure method  ");
				LeapConfigurationUtil.performanceChecker(
						"processing time in subscribtion router builder for consuming data from kafka and pushing to jms queue ",
						startTime, endTime);

			}
		} else {
			log.info("{} There is no subsciber feature found to load...", LEAP_LOG_KEY);
		}

	}

}