package com.attunedlabs.leap.eventsubscription.routebuilder;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.abstractretrystrategy.InstantiateSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapHeaderConstant;

/**
 * <code>GenericRetryRouteBuilder</code> route builder implementation for
 * retrying the failed jms subscriptions based on the list provided by the
 * getRetryableRecords() invoked on the implementation class of
 * <code>AbstractSubscriptionRetryStrategy</code>.
 * 
 * @see {@link AbstractSubscriptionRetryStrategy}.
 * @author Reactiveworks
 *
 */
public class GenericJMSRetryRouteBuilder extends RouteBuilder {

	final static Logger logger = LoggerFactory.getLogger(GenericJMSRetryRouteBuilder.class);
	static SubscriptionUtil subscriptionUtil = new SubscriptionUtil();
	protected final IEventFrameworkConfigService eventFrameworkConfigService = new EventFrameworkConfigService();
	final public static List<EventSubscriptionTracker> totalRetryableRecords = new ArrayList<>();
	JSONObject failureStrategyConfigJSON = null;

	private static Properties props = new Properties();
	static {
		try {
			props = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		} catch (Exception e) {
			logger.error("{} failed to load quartz config...{}", LEAP_LOG_KEY,
					SubscriptionConstant.SUBSCRIPTION_QUARTZ_CONFIGS);
		}
	}// ..end of static block to load the ConsumerProperties

	@Override
	public void configure() throws Exception {
		String methodName = "configure";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), LEAP_LOG_KEY,
				methodName);
		final HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> cachingInstance = InstantiateSubscriptionRetryStrategy
				.getCachingInstance();
		final LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
		Set<String> subscribers = leapConfigurationServer.getAllJMSSubscribersAvailable();
		if (!subscribers.isEmpty()) {
			if (cachingInstance.size() != 0) {

				// quartz for failed subscription next quartz will trigger once
				// all
				// the failed subscription are processed
				from(SubscriptionUtil.constructQuartzJMSURI())

						// skip the failed subscription of failed to fing
						// required
						// data
						.onException(Exception.class).continued(true).process(new Processor() {
							@Override
							public void process(Exchange exchange) throws Exception {
								Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
								logger.error(
										"{} ignore exception's thrown from retry route proceed for next failed subscription {}",
										LEAP_LOG_KEY, exception.getMessage());

							}
						}).end().

						// calling getRetryableRecords on all the subscriptions.
						process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								logger.trace(LEAP_LOG_KEY + "INITAL \n"
										+ "\n GenericJMSRetryRouteBuilder.totalRetryableRecords \n"
										+ GenericJMSRetryRouteBuilder.totalRetryableRecords.size());
								exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, true);
								Set<String> tenantSiteKeySet = cachingInstance.keySet();
								for (String tenantSiteKey : tenantSiteKeySet) {
									Map<String, AbstractSubscriptionRetryStrategy> classInstanceMap = cachingInstance
											.get(tenantSiteKey);
									String tenantId = LeapHeaderConstant.tenant;
									String siteId = LeapHeaderConstant.site;
									String[] tenantSiteArray = tenantSiteKey
											.split(SubscriptionConstant.TENANT_SITE_SEPERATOR);
									if (tenantSiteArray.length == 2) {
										tenantId = tenantSiteArray[0];
										siteId = tenantSiteArray[1];
									}
									Set<String> classNameKeys = classInstanceMap.keySet();
									for (String subIdAndclassName : classNameKeys) {
										String[] subIdClassArray = subIdAndclassName
												.split(SubscriptionConstant.SUB_ID_CLASS_SEPERATOR);
										String subscriptionId = subIdClassArray[0];
										AbstractSubscriptionRetryStrategy abstractSubscriptionRetryStrategy = classInstanceMap
												.get(subIdAndclassName);
										List<EventSubscriptionTracker> retryableRecords = abstractSubscriptionRetryStrategy
												.getRetryableRecords(exchange, tenantId, siteId, subscriptionId);
										if (retryableRecords != null)
											totalRetryableRecords.addAll(retryableRecords);
									}
								}
								exchange.getIn().setHeader(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY,
										totalRetryableRecords.size());

							}

						}).
						// looping on each subscription
						loop(header(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY)).
						// clone the exchange for every subscription
						copy()

						// exception handling to skip the iteration if
						// the subscription failed to retry.
						.doTry().

						process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								EventSubscriptionTracker eventSubscriptionTracker = totalRetryableRecords
										.get(exchange.getProperty(SubscriptionConstant.LOOP_INDEX_KEY, Integer.class));
								try {

									if (subscriptionUtil
											.attributeEmptyCheck(eventSubscriptionTracker.getSubscriptionId())
											|| subscriptionUtil.attributeEmptyCheck(eventSubscriptionTracker.getTopic())
											|| subscriptionUtil
													.attributeEmptyCheck(eventSubscriptionTracker.getPartition())
											|| subscriptionUtil
													.attributeEmptyCheck(eventSubscriptionTracker.getOffset())
											|| subscriptionUtil
													.attributeEmptyCheck(eventSubscriptionTracker.getEventData()))
										throw new MissingConfigurationException(
												"RETRY ATTEMPT FAILED : Failed subscriptionId "
														+ eventSubscriptionTracker.getSubscriptionId() + ""
														+ " cannot be retried due to either details in EventSubscriptionTracker are"
														+ " empty(subscriptionId,topic,partition,offset,eventData)");

									String eventBody = eventSubscriptionTracker.getEventData();
									JSONObject jsonBody = subscriptionUtil.identifyContentType(eventBody);
									if (!jsonBody.has(EventFrameworkConstants.METADATA_KEY))
										throw new MissingConfigurationException(
												"INVALID EVENT DATA : Cannot retry subscription with ID "
														+ eventSubscriptionTracker.getSubscriptionId()
														+ "because, either of key is not found in event data " + "("
														+ EventFrameworkConstants.METADATA_KEY + ") ");

									ConfigurationContext configCtx = subscriptionUtil.buildConfigContext(
											LeapHeaderConstant.tenant, LeapHeaderConstant.site,
											eventSubscriptionTracker.getSubscriptionId());
									JMSSubscribeEvent eventSubscription = eventFrameworkConfigService
											.getJMSEventSubscriptionConfiguration(configCtx,
													subscriptionUtil.getActualSubscriberId(
															eventSubscriptionTracker.getSubscriptionId()));
									FailureHandlingStrategy failureHandlingStrategy = eventSubscription
											.getFailureHandlingStrategy();
									String failureStrategyConfig = failureHandlingStrategy.getFailureStrategyConfig();

									failureStrategyConfigJSON = new JSONObject(failureStrategyConfig);
									logger.trace("{} failureStrategyConfig in jmsgenericretryroutebuilder::{}",
											LEAP_LOG_KEY, failureStrategyConfigJSON);

									exchange.getIn().setHeader("PRE_STAGE", false);
									exchange.getIn().setHeader(SubscriptionConstant.RETRYABLE_SUBSCRIPTIONS_COUNT_KEY,
											totalRetryableRecords.size());
									exchange.getIn().setHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS,
											eventSubscriptionTracker);
									exchange.getIn().setHeader(SubscriptionConstant.RETRY_COUNT,
											eventSubscriptionTracker.getRetryCount());
									exchange.getIn().setHeader(SubscriptionConstant.IS_RETRYABLE,
											eventSubscriptionTracker.getIsRetryable());
									exchange.getIn().setHeader(SubscriptionConstant.EVENT_FETCHED_DTM,
											eventSubscriptionTracker.getEventFetchedDTM());
									exchange.getIn().setHeader(SubscriptionConstant.EVENT_LAST_FAILED_DTM,
											eventSubscriptionTracker.getLastFailureDTM());
									exchange.getIn().setHeader(SubscriptionConstant.FAILURE_MSG,
											eventSubscriptionTracker.getFailureMsg());
									exchange.getIn().setHeader(SubscriptionConstant.EVENT_STATUS,
											eventSubscriptionTracker.getStatus());
									exchange.getIn().setHeader(KafkaConstants.TOPIC,
											eventSubscriptionTracker.getTopic());
									exchange.getIn().setHeader(KafkaConstants.PARTITION,
											eventSubscriptionTracker.getPartition());
									exchange.getIn().setHeader(KafkaConstants.OFFSET,
											eventSubscriptionTracker.getOffset());
									exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY,
											eventSubscriptionTracker.getSubscriptionId());
									exchange.getIn().setHeader(SubscriptionConstant.RETRY_QUEUE_CONFIG,
											SubscriptionUtil.constructJMSRetryConsumeQueueURI(failureStrategyConfigJSON
													.getString(SubscriptionConstant.RETRY_QUEUE_NAME).toString(),
													String.valueOf(failureStrategyConfigJSON
															.getInt(SubscriptionConstant.RETRY_CONSUMERS)),
													eventSubscription.getAcknowledge()));
									exchange.getIn().setBody(eventBody, String.class);
									logger.trace("{} end of for loop offset is : {}", LEAP_LOG_KEY,
											eventSubscriptionTracker.getOffset());
								} catch (Exception e) {
									logger.error("{} FAILED RETRYING SUBSCRIPTION : {}", LEAP_LOG_KEY, e.getMessage());
									throw e;
								}
							}

						}).
						// call the same logic to invoke any action for failed
						// subscriptions
						toD("${header." + SubscriptionConstant.RETRY_QUEUE_CONFIG + "}")

						// end of the
						// try blocks
						.endDoTry()

						// catch the exception raised.
						.doCatch(Exception.class)

						// warp the exception and rethrow it the route
						// level onException clause specified.
						.process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
								logger.trace("wrap and rethrow retry jms subscription exception,{} {}", LEAP_LOG_KEY,
										exception.getMessage());
								throw exception;
							}
						})

						// end of try-catch clause
						.end()
						// end of loop .
						.end().process(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								// clearing the record list for retry to trigger
								// next time.
								// Commenting as we saw issue, while retrying-
								// when
								// retry invoking sec is less then process it
								// take
								// to
								// process the records
								// totalRetryableRecords.clear();
								logger.trace("FINISHED JMS RETRYING...{}", LEAP_LOG_KEY);
							}
						});

			} else
				logger.debug("{} no jms retry startegy cached found...GenericJMSRetryRouteBuilder cannot be build",
						LEAP_LOG_KEY);

		} else {
			logger.debug("{} no jms retry startegy...GenericJMSRetryRouteBuilder as jms invocation is false",
					LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, LEAP_LOG_KEY, getClass().getName(),
				methodName);
	}

}
