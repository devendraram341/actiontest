package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.camel.eventproducer.ICamelEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventDispatchers;
import com.attunedlabs.eventframework.jaxb.EventPipeline;
import com.attunedlabs.integrationfwk.activities.event.PipelineEventBuilder;
import com.attunedlabs.integrationfwk.config.jaxb.EventPublishActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class EventPublishActivityProcessor {
	Logger logger = LoggerFactory.getLogger(EventPublishActivityProcessor.class);

	/**
	 * This method is used to process the event publish pipeline activity, by
	 * associating the pipeline activity data with event param of event
	 * configuration.
	 * 
	 * @param exchange : Camel Exchange Object
	 * @throws EventPublishActivityException
	 */
	public void processPipelineEvent(Exchange exchange) throws EventPublishActivityException {
		String methodName = "processPipelineEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		EventPublishActivity eventPublishActivity = pipeactivity.getEventPublishActivity();
		String eventname = eventPublishActivity.getEventName();
		try {
			Event event = searchEventConfigurationByEventName(eventname, exchange);
			if (event != null) {
				logger.trace("{} Event is not null for event name : {}" ,LEAP_LOG_KEY, eventname);
				createAndStorePipelineEventInCache(event, exchange);
			} else {
				throw new EventPublishActivityException(
						"event configuration defined by event name " + eventname + " is null");
			}
		} catch (EventFrameworkConfigurationException e) {
			logger.error("{} No event configuration defined by event name {}" ,LEAP_LOG_KEY, eventname, e.getMessage());
			throw new EventPublishActivityException("No event configuration defined by event name " + eventname, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method processPipelineEvent

	/**
	 * This method is usd to create pipeline activity event and store in cache,so
	 * that it should be dispatched later
	 * 
	 * @param event    : Event Object
	 * @param exchange : Camel Exchange object
	 * @throws JSONException
	 */
	private void createAndStorePipelineEventInCache(Event event, Exchange exchange) {
		String methodName = "createAndStorePipelineEventInCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String requestId = serviceDataContext.getRequestUUID();
		ICamelEventBuilder evtBuilder = new PipelineEventBuilder();
		EventDispatchers eventDispatchers = event.getEventDispatchers();
		if (eventDispatchers != null) {
			for (EventDispatcher eventDispatcher : eventDispatchers.getEventDispatcher()) {
				String dispatchChannelId = eventDispatcher.getDispatchChannelId();
				LeapEvent leapEvent = evtBuilder.buildEvent(exchange, event);
				leapEvent.setDispatchChannelId(dispatchChannelId);
				logger.debug("{} leapEvent : : {}",LEAP_LOG_KEY, leapEvent.toString());
				LeapEventContext.addLeapEvent(requestId, leapEvent, serviceDataContext);
			}
		}
		/*
		 * HazelcastInstance hazelcastInstance =
		 * DataGridService.getDataGridInstance().getHazelcastInstance();
		 * 
		 * logger.debug(".getHazelCastListforServiceEvent() requestId: " + requestId);
		 * IMap<String, ArrayList<LeapEvent>> serviceEventMap =
		 * hazelcastInstance.getMap(requestId); ArrayList<LeapEvent> leapEventList =
		 * null;
		 * 
		 * if (((ArrayList<LeapEvent>) serviceEventMap.get(requestId)) == null)
		 * leapEventList = new ArrayList<>(); else leapEventList =
		 * ((ArrayList<LeapEvent>) serviceEventMap.get(requestId));
		 * 
		 * logger.debug("eventlist before adding event : " + leapEventList); if
		 * (serviceEventMap != null && leapEventList != null) {
		 * leapEventList.add(leapEvent); serviceEventMap.put(requestId, leapEventList);
		 * }
		 */

		// build an event
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method addEventParamFromEventPipeActivity

	/**
	 * This method is used to search event configuration defined in configuration
	 * file from cache using event name
	 * 
	 * @param eventname : Event Name in String
	 * @param exchange  : Camel Exchange Object
	 * @return Event Object
	 * @throws EventFrameworkConfigurationException
	 * @throws EventPublishActivityException
	 */
	private Event searchEventConfigurationByEventName(String eventname, Exchange exchange)
			throws EventFrameworkConfigurationException, EventPublishActivityException {
		String methodName = "searchEventConfigurationByEventName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		RequestContext requestContext = serviceDataContext.getRequestContext();
		ConfigurationContext configContext = requestContext.getConfigurationContext();
		logger.trace("{} configuarion context : {}" ,LEAP_LOG_KEY, configContext);
		IEventFrameworkConfigService evtFwkConfigService = new EventFrameworkConfigService();
		Event event = evtFwkConfigService.getEventConfiguration(configContext, eventname);

		// check if event is pipeline enabled or not
		EventPipeline eventpipline = event.getEventPipeline();

		logger.debug("{} eventpipeline joga : {}" ,LEAP_LOG_KEY, eventpipline.toString());
		if (eventpipline != null) {
			boolean enabled = eventpipline.isEnabled();
			if (enabled) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return event;
			} else {
				throw new EventPublishActivityException("event : " + eventname + " is pipeline event but not enabled");
			}
		} else {
			throw new EventPublishActivityException("event : " + eventname + " is not a pipeline event");
		}
	}// end of method searchEventConfigurationByEventName

}
