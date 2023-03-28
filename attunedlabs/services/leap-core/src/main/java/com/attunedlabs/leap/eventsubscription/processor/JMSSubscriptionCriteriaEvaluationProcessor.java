/**
 * 
 */
package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * @author reactiveworks
 *
 */
public class JMSSubscriptionCriteriaEvaluationProcessor implements Processor {

	final static Logger log = LoggerFactory.getLogger(JMSSubscriptionCriteriaEvaluationProcessor.class);

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	private IEventFrameworkConfigService eventFrameworkConfigService;
	private SubscriptionUtil subscriptionUtil;

	public JMSSubscriptionCriteriaEvaluationProcessor(IEventFrameworkConfigService eventFrameworkConfigService,
			SubscriptionUtil subscriptionUtil) {
		this.eventFrameworkConfigService = eventFrameworkConfigService;
		this.subscriptionUtil = subscriptionUtil;

	}

	/**
	 * adds some extra headers such as configuration-context to the exchange such as
	 * loop based on the subscribers who matches the subscription-criteria.
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		long startTime = LeapConfigurationUtil
				.startTimeChecker("JMSSubscriptionCriteriaEvaluationProcessor configure method  ");
		try {
			if (exchange.getIn() != null) {

				String topicName = null;
				String partition = null;
				String offset = null;
				String subscriberId = null;

				// converting eventbody consumed into jsonObj
				// and build the configuration Context.
				String body = exchange.getIn().getBody(String.class);
				log.info("{} Orignal request body is {}", LEAP_LOG_KEY, body);
				// will convert the xml to json
				JSONObject eventBody = subscriptionUtil.identifyContentType(body);
				log.info("{} request body after converting to json is {}", LEAP_LOG_KEY, eventBody);
				ConfigurationContext configCtx = null;

				// preserve topic metadata & keeping group Id as
				// subscriberid
				Object destinationObj = exchange.getIn().getHeader("JMSDestination");
				if (destinationObj instanceof RMQDestination) {
					log.debug("using RMQDestination {}", LEAP_LOG_KEY);
					RMQDestination rmqDestination = exchange.getIn().getHeader("JMSDestination", RMQDestination.class);
					topicName = rmqDestination.getDestinationName();
				} else {
					log.debug("{} using JMSDestination as String::{} ", LEAP_LOG_KEY, destinationObj);
					topicName = destinationObj.toString();
				}
				partition = exchange.getIn().getHeader(KafkaConstants.PARTITION, String.class);
				if (partition == null)
					exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
				offset = exchange.getIn().getHeader(KafkaConstants.OFFSET, String.class);
				if (offset == null) {
					String randomOffset = RandomStringUtils.randomAlphanumeric(8);
					exchange.getIn().setHeader(KafkaConstants.OFFSET, randomOffset);
				}
				// call from retry-mechanism
				subscriberId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);
				log.info("{} topicName :: {} offset ::{} partition :: {}", LEAP_LOG_KEY, topicName, offset, partition);
				if (subscriberId == null || subscriberId.trim().isEmpty())
					throw new MissingConfigurationException("JMS SUBSCRIBER ID DOESN'T EXISTS : '"
							+ SubscriptionConstant.SUBSCRIPTION_ID_KEY + "' key not found in exchange Header...");

				log.debug("{} topic name retrived: {}", LEAP_LOG_KEY, topicName);
				log.debug("{} subscribers for topic {}", LEAP_LOG_KEY, subscriberId);

				String tenant = null, site = null;
				try {
					// building configuration context to fetch eventing
					// configuration
					JSONObject eventHeadersJSON = null;
					// check metadata exist in Event Json
					if (eventBody.has(EventFrameworkConstants.METADATA_KEY)) {
						eventHeadersJSON = (JSONObject) eventBody.get(EventFrameworkConstants.METADATA_KEY);
					} else {
						eventHeadersJSON = new JSONObject();
						eventHeadersJSON.put(LeapHeaderConstant.ACCOUNT_ID, LeapHeaderConstant.GLOBAL_ACCOUNT_ID);
						eventHeadersJSON.put(LeapHeaderConstant.SITE_KEY, LeapHeaderConstant.GLOBAL_SITE_ID);
						eventBody.put(EventFrameworkConstants.METADATA_KEY, eventHeadersJSON);
					}
					log.info("{} final Event json is ::{} ", LEAP_LOG_KEY, eventBody);
					// getting tenant, site from event data.
					if (eventHeadersJSON != null) {
						site = eventHeadersJSON.getString(LeapHeaderConstant.SITE_KEY);
						String accountId = eventHeadersJSON.getString(LeapHeaderConstant.ACCOUNT_ID);
						tenant = LeapTenantSecurityUtil.getInternalTenantId(accountId, site);
						// avoided boiler plate code by throwing exception
						if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
							throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				Map<String, Object> subscriberData = setGenericLocalData(exchange, subscriberId, topicName, partition,
						offset, eventBody.toString());
				EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) subscriberData
						.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
				if (tenant != null) {
					eventSubscriptionTracker.setTenantId(tenant);
				}
				if (site != null) {
					eventSubscriptionTracker.setSiteId(site);
				}
				configCtx = subscriptionUtil.buildConfigContext(tenant, site, subscriberId);
				// setting tenant,site
				LeapDataContext leapDataContext = new LeapDataContext();
				LeapDataContextElement ldce = new LeapDataContextElement();
				ldce.setDataElement(new LeapDataElement("1.0", "context", "en", null));
				leapDataContext.addContextElement(ldce, LeapDataContextConstant.INITIAL_CONTEXT);
				LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext(tenant, site);
				serviceDataContext.setSubscriberDataInServiceContext(subscriberData);
				exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
				exchange.getIn().setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriberId);

				log.trace("{} setting the leap data context in exchange of subscriber : {} ==> {}", LEAP_LOG_KEY,
						subscriberId, leapDataContext);

				log.trace("{} SUBSCRIBER-GROUP-ID : {} \n Headers {}", LEAP_LOG_KEY, subscriberId,
						exchange.getIn().getHeaders());

				log.trace("{} ConfigurationContext formed {}", LEAP_LOG_KEY, configCtx);
				if (configCtx == null)
					throw new MissingConfigurationException(
							"MISSING CONFIGURATION PARAMETERS: for subscriber with Id : " + subscriberId
									+ " ==> expected parameters are not present to build ConfigurationContext!");

				JMSSubscribeEvent eventSubscription = null;
				try {
					eventSubscription = eventFrameworkConfigService.getJMSEventSubscriptionConfiguration(configCtx,
							subscriptionUtil.getActualSubscriberId(subscriberId));
				} catch (Exception e) {
					eventSubscription = null;

				}

				if (eventSubscription != null) {
					String eventSubscriptionCriteria = eventSubscription.getEventSubscriptionCriteria();

					log.trace("{} updating the retries leap header in exchange of subscriber :{} ==> {} ", LEAP_LOG_KEY,
							subscriberId, leapDataContext);

					// if the criteria is specified than add ,if not
					// specified then directly add subscriber in exchange
					// header.
					if (eventSubscriptionCriteria == null || eventSubscriptionCriteria.trim().isEmpty())
						exchange.getIn().setHeader(SubscriptionConstant.JMS_SUBSCIBER_EVENT_CONFIG_KEY,
								eventSubscription);
					else if (subscriptionUtil.evaluateMVELForCriteriaMatch(eventSubscriptionCriteria.trim(), eventBody))
						exchange.getIn().setHeader(SubscriptionConstant.JMS_SUBSCIBER_EVENT_CONFIG_KEY,
								eventSubscription);

				} else {
					eventSubscription = reloadJMSConfigurationForGlobalTenant(configCtx,
							subscriptionUtil.getActualSubscriberId(subscriberId));
					if (eventSubscription == null)
						throw new MissingConfigurationException(
								"NO CONFIGURATION FOUND : Fetched empty SubscriptionConfiguration  "
										+ "in DataGrid for requested subscriber : " + subscriberId);
					exchange.getIn().setHeader(SubscriptionConstant.JMS_SUBSCIBER_EVENT_CONFIG_KEY, eventSubscription);
					// just to make non-retry as false because of default
					// assignation
					subscriberData.put(SubscriptionConstant.NO_CONFIG_FOR_TENANT_KEY, true);
				}

				// setting required config in camel Exchange
				// Headers
				exchange.getIn().setBody(eventBody, String.class);
				Long endTime = LeapConfigurationUtil
						.endTimeChecker("JMSSubscriptionCriteriaEvaluationProcessor configure method  ");
				LeapConfigurationUtil.performanceChecker(
						"processing time in jms subscribtion router builder for consuming data from jms queue and sending it to the service ",
						startTime, endTime);
			}

		} catch (

		Exception e) {
			e.printStackTrace();
			JmsMessage message = (JmsMessage) exchange.getIn();
			javax.jms.Message msg = message.getJmsMessage();
			msg.acknowledge();
			throw new NonRetryableException("PRE-FAILED NON-RETRYABLE[" + e.getMessage() + "]", e);
		}
		log.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY,

				getClass().getName(), methodName);
	}

	/**
	 * loading configuration for global tenant.
	 * 
	 * @param configCtx
	 * @param actualSubscriberId
	 * @return
	 * @throws MissingConfigurationException
	 */
	private JMSSubscribeEvent reloadJMSConfigurationForGlobalTenant(ConfigurationContext configCtx,
			String actualSubscriberId) throws MissingConfigurationException {
		// will use global tenant for now, just atleast we
		// can log by getting subsciber configuration.
		configCtx.setTenantId(LeapHeaderConstant.tenant);
		configCtx.setSiteId(LeapHeaderConstant.site);

		try {
			JMSSubscribeEvent eventSubscription = eventFrameworkConfigService
					.getJMSEventSubscriptionConfiguration(configCtx, actualSubscriberId);
			return eventSubscription;
		} catch (Exception e) {

			// logic handled in
			// SubscriberActionIndentificationProcessor
			throw new MissingConfigurationException(
					"FETCHING CONFIGURATION FAILED : Unable to fetch the configuration context "
							+ "in DataGrid for requested subscriber : " + actualSubscriberId + " due to "
							+ e.getMessage());
		}
	}

	/**
	 * setting the generic data in leapHeader
	 * 
	 * @param exchange
	 * @param leapHeader
	 * @param subscriberId
	 * @param topicName
	 * @param partition
	 * @param offset
	 * @param body
	 * @throws MissingConfigurationException
	 * @throws ParseException
	 */
	private Map<String, Object> setGenericLocalData(Exchange exchange, String subscriberId, String topicName,
			String partition, String offset, String body) throws MissingConfigurationException, ParseException {
		EventSubscriptionTracker eventSubscriptionTracker = exchange.getIn()
				.getHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS, EventSubscriptionTracker.class);
		String methodName = "setGenericLocalData";
		log.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		String retryCount = exchange.getIn().getHeader(SubscriptionConstant.RETRY_COUNT, String.class);
		EventSubscriptionTracker eventSubscriptionTracker2 = eventSubscriptionTracker;

		HashMap<String, Object> threadProcessMap = new HashMap<>();

		if (eventSubscriptionTracker2 == null) {
			eventSubscriptionTracker2 = new EventSubscriptionTracker();
			eventSubscriptionTracker2.setSubscriptionId(subscriberId);
			eventSubscriptionTracker2.setTopic(topicName);
			eventSubscriptionTracker2.setPartition(partition);
			eventSubscriptionTracker2.setOffset(offset);
			eventSubscriptionTracker2.setEventData(body);
			eventSubscriptionTracker2.setIsRetryable(false);
			eventSubscriptionTracker2.setRetryCount(0);
		}

		if (retryCount != null) {
			String isRetryable = exchange.getIn().getHeader(SubscriptionConstant.IS_RETRYABLE, String.class);
			String failureMsg = exchange.getIn().getHeader(SubscriptionConstant.FAILURE_MSG, String.class);
			String status = exchange.getIn().getHeader(SubscriptionConstant.EVENT_STATUS, String.class);
			// String eventFetchedDtm =
			// exchange.getIn().getHeader(SubscriptionConstant.EVENT_FETCHED_DTM,
			// String.class);
			// String eventLastFetchedDtm =
			// exchange.getIn().getHeader(SubscriptionConstant.EVENT_LAST_FAILED_DTM,
			// String.class);
			eventSubscriptionTracker2 = new EventSubscriptionTracker();
			eventSubscriptionTracker2.setSubscriptionId(subscriberId);
			eventSubscriptionTracker2.setTopic(topicName);
			eventSubscriptionTracker2.setPartition(partition);
			eventSubscriptionTracker2.setOffset(offset);
			eventSubscriptionTracker2.setEventData(body);
			eventSubscriptionTracker2.setIsRetryable(Boolean.valueOf(isRetryable));
			eventSubscriptionTracker2.setRetryCount(Integer.parseInt((retryCount)));
			eventSubscriptionTracker2.setFailureMsg(failureMsg);
			eventSubscriptionTracker2.setStatus(status);
			// eventSubscriptionTracker2.setEventFetchedDTM(Timestamp.valueOf(eventFetchedDtm));
			// eventSubscriptionTracker2.setLastFailureDTM(Timestamp.valueOf(eventLastFetchedDtm));
			threadProcessMap.put(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS, eventSubscriptionTracker2);
		}

		if (eventSubscriptionTracker2 != null)
			threadProcessMap.put(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS, eventSubscriptionTracker2);

		// removing from the exchange as stored in leapHeader generic map
		if (exchange.getIn().getHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS) != null)
			exchange.getIn().removeHeader(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		// just to identify the retry
		Boolean isRetryTriggered = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY,
				Boolean.class);

		if (isRetryTriggered == null)
			threadProcessMap.put(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, false);
		else
			threadProcessMap.put(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY, isRetryTriggered);
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		// set leap Header before fetching tenant and site from event
		// body and setting topic-metadata
		return threadProcessMap;
	}

}
