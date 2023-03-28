package com.attunedlabs.eventframework.camel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.camel.eventbuilder.EventBuilderHelper;
import com.attunedlabs.eventframework.camel.eventbuilder.OgnlEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionFailureEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServiceCompletionSuccessEventBuilder;
import com.attunedlabs.eventframework.camel.eventproducer.ServicePerformanceLoggingEventBuilder;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherException;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.ILeapEventService;
import com.attunedlabs.eventframework.event.InvalidEventException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.LeapEventService;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.jaxb.CamelEventBuilder;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.EventDispatcher;
import com.attunedlabs.eventframework.jaxb.EventDispatchers;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.LeapDataContext;

/**
 * This class is responsible for generating component internal events, route
 * completion internal event and system events
 * 
 * @author ubuntu
 *
 */
public class ComponentEventProducer {

	final Logger logger = LoggerFactory.getLogger(ComponentEventProducer.class);

	IEventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
	ILeapEventService eventService = new LeapEventService();
	EventFrameworkDispatcherService dispatchService = new EventFrameworkDispatcherService();

	// private LeapHeader leapHeader;

	/**
	 * This method is used to publish events present in the list.(i.e, service and
	 * component events.)
	 * 
	 * @param leapEvents : list of component and service events.
	 * @throws EventDispatcherTrackerException
	 */
	public void publishComponentEvent(ArrayList<LeapEvent> leapEvents, final String tenantId, final String siteId,
			final String requestId, final String eventStoreKey, LeapServiceContext serviceDataContext,
			Exchange camelExchange) throws EventDispatcherTrackerException {
		String methodName = "publishComponentEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		if (leapEvents != null && !(leapEvents.isEmpty())) {
			for (LeapEvent leapevent : leapEvents) {
				try {
					eventDispatcherTrackerService.updateEventStatus(tenant, site, requestId, eventStoreKey,
							EventTrackerTableConstants.STATUS_IN_PROCESS, leapevent.getId(), false, null, false, false,
							leapevent.getDispatchChannelId());
					eventService.publishEvent(leapevent, tenant, site, requestUUID, false);
				} catch (Exception insExp) {
					throw new EventDispatcherTrackerException(insExp.getMessage(), insExp.getCause());
				}
			}
		} else {
			logger.warn("No event is avaialable to produce  {}", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to publish system event
	 * 
	 * @param hcEventList : list of system event in hazelcast
	 */
	public void publishSystemEvent(List<LeapEvent> hcEventList, Exchange camelExchange) {
		// #TODO this method is no where used nedd to remove
		String methodName = "publishSystemEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (hcEventList == null || hcEventList.isEmpty()) {
			return;
		}
		LeapDataContext leapDataContext = camelExchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
				LeapDataContext.class);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String tenant = leapServiceContext.getTenant();
		String site = leapServiceContext.getSite();
		String requestUUID = leapServiceContext.getRequestUUID();
		for (LeapEvent leapevent : hcEventList) {
			try {
				eventService.publishSystemEvent(leapevent, tenant, site, requestUUID);
				logger.trace("-checkHazelCastListValue", LEAP_LOG_KEY, leapevent.toString());
			} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
					| EventFrameworkConfigurationException invalidEvtExp) {
				// #TODO Eating exception till I know what to do
				logger.error("{} InvalidSystemEvent {}", LEAP_LOG_KEY, invalidEvtExp);
				break;
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is to publish internal event for route completion for success
	 * 
	 * @param tenantId
	 * @param camelContextId
	 * @param serviceName
	 * @param camelExchange
	 */
	public void publishEventForRouteCompletion(String serviceName, Exchange camelExchange) {
		String methodName = "publishEventForRouteCompletion";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName); // LeapHeader data
		// need to delete below two lines after completely replace with LDC
		// LeapHeader leapHeader = (LeapHeader)
		// camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		// RequestContext reqCtx = leapHeader.getRequestContext();
		// service event are at site level
		// ConfigurationContext configCtx1 = new
		// ConfigurationContext(reqCtx.getTenantId(), reqCtx.getSiteId(), null,
		// null,
		// null);
		LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		RequestContext reqCtx = serviceDataContext.getRequestContext();
		ConfigurationContext configCtx1 = new ConfigurationContext(reqCtx);
		// Events to be fired when service completed sucessfully only
		try {
			configCtx1.setTenantId(serviceDataContext.getTenant());
			logger.debug("{} service name : {}, config context :{} ", LEAP_LOG_KEY, serviceName, configCtx1);
			List<Event> evtProdListSucess = evtConfigService
					.getEventConfigProducerForServiceSuccessCompletion(configCtx1, serviceName);

			// logger.info("CamelEventProducerList size in
			// CamelEventSynchProducer : " + evtProdListSucess.size());
			// Events to be fired when service completed for all condition
			// (Sucess or failure)
			List<Event> evtProdListAll = evtConfigService
					.getEventConfigProducerForServiceFailedAndSucessCompletion(configCtx1, serviceName);
			// Merge the two List into one and check for nulls
			List<Event> evtProdList = mergeEventProducerList(evtProdListSucess, evtProdListAll);
			if (evtProdList == null || evtProdList.isEmpty()) {
				logger.debug("{} No Event configured for SucessServiceCompletion for Service{} and requestContext={}",
						LEAP_LOG_KEY, serviceName, configCtx1);
				return;
			}
			// else build the Event and Publish Them
			addServiceEventFromEventProducerMap(camelExchange, evtProdList, serviceDataContext);
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkDispatcherException
				| EventFrameworkConfigurationException exp) {
			exp.printStackTrace();
		}
	}// end of method

	/**
	 * This method is use to update the event getting publish to in process in db
	 * and publish them
	 * 
	 * @param leapEvents         : List of all the service completion events
	 * @param tenantId           : name of the tenant
	 * @param siteId             : name of the site
	 * @param requestId          : unique request id
	 * @param eventStoreKey      : event store key
	 * @param serviceDataContext : {@link LeapServiceContext}
	 * @param camelExchange      : {@link CamelExchange}
	 * @throws EventDispatcherTrackerException
	 */
	public void updateServiceCompletionEventAndPublish(ArrayList<LeapEvent> leapEvents, final String tenantId,
			final String siteId, final String requestId, final String eventStoreKey,
			LeapServiceContext serviceDataContext, Exchange camelExchange) throws EventDispatcherTrackerException {
		String methodName = "updateServiceCompletionEventAndPublish";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		if (leapEvents != null && !(leapEvents.isEmpty())) {
			for (LeapEvent leapevent : leapEvents) {
				try {
					eventDispatcherTrackerService.updateEventStatus(tenant, site, requestId, eventStoreKey,
							EventTrackerTableConstants.STATUS_IN_PROCESS, leapevent.getId(), false, null, false, false,
							leapevent.getDispatchChannelId());
					eventService.publishEvent(leapevent, tenant, site, requestUUID, false);
				} catch (Exception insExp) {
					insExp.printStackTrace();
					throw new EventDispatcherTrackerException(insExp.getMessage(), insExp.getCause());
				}
			}
		} else {
			logger.warn("No event is avaialable to produce");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method updateServiceCompletionEventAndPublish

	public void publishEventForFailedRouteCompletion(String serviceName, Exchange camelExchange) {
		String methodName = "publishEventForFailedRouteCompletion";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// LeapHeader data
		LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		RequestContext reqCtx = serviceDataContext.getRequestContext();
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);
		try {
			List<Event> evtProdListFailure = evtConfigService
					.getEventConfigProducerForServiceFailedCompletion(configCtx, serviceName);
			List<Event> evtProdListAll = evtConfigService
					.getEventConfigProducerForServiceFailedAndSucessCompletion(configCtx, serviceName);
			// Merge the two List into one and check for nulls
			List<Event> evtProdList = mergeEventProducerList(evtProdListFailure, evtProdListAll);
			if (evtProdList == null || evtProdList.isEmpty()) {
				logger.debug("No Event configured for ServiceCompletionFailure for Service{} and requestContext={}",
						LEAP_LOG_KEY, serviceName, reqCtx);
				return;
			}
			// else build the Event and Publish Them
			publishEventForEventProducerList(camelExchange, evtProdList);

			// else build the Event and add Them to serviceEventList
			// addServiceEventFromEventProducerList(camelExchange, evtProdList);
		} catch (EventBuilderInstantiationException | InvalidEventException | EventFrameworkConfigurationException
				| LeapEventTransformationException | MessageDispatchingException exp) {
			// #TODO
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void addServiceEventFromEventProducerMap(Exchange camelExchange, List<Event> evtProdList,
			LeapServiceContext serviceDataContext)
			throws EventBuilderInstantiationException, InvalidEventException, EventFrameworkDispatcherException {
		AbstractCamelEventBuilder evtBuilder = null;
		// LeapHeader leapHeader = (LeapHeader)
		// camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);

		if (evtProdList == null || evtProdList.isEmpty())
			return;
		for (Event evt : evtProdList) {
			CamelEventProducer evtProd = evt.getCamelEventProducer();
			boolean isOgnl = EventBuilderHelper.isOgnlBuilderType(evtProd);
			if (isOgnl) {
				// EventBuilderHelper.constructSourceForOGNL(camelExchange);
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				evtBuilder = getEventBuilderInstance(evtProd.getCamelEventBuilder());
			}
			EventDispatchers eventDispatchers = evt.getEventDispatchers();
			if (eventDispatchers == null) {
				throw new InvalidEventException(" unable to get Dispatcher for Event ::" + evt.getId());
			}
			for (EventDispatcher eventDispatcher : eventDispatchers.getEventDispatcher()) {
				String dispatchChannelName = eventDispatcher.getDispatchChannelId();
				LeapEvent leapevent = evtBuilder.buildEvent(camelExchange, evt);
				leapevent.addObject(LeapDataContextConstant.TENANTID, serviceDataContext.getTenant());
				leapevent.addObject(LeapDataContextConstant.SITEID, serviceDataContext.getSite());
				leapevent.setDispatchChannelId(dispatchChannelName);
				String requestId = serviceDataContext.getRequestUUID();
				LeapEventContext.addServiceCompletionLeapEvent(requestId, leapevent, serviceDataContext);
				logger.debug("{} Event added for EventId {}", LEAP_LOG_KEY,leapevent.getId());
			}
			// we should not add any event in db here, instead should be added
			// all service
			// completion event at once
			/*
			 * try { eventDispatcherTrackerService.addEventTracking(serviceDataContext
			 * .getTenant() , serviceDataContext.getSite(),
			 * serviceDataContext.getRequestUUID(), serviceDataContext.getRequestUUID(),
			 * leapevent,false, camelExchange); } catch (EventDispatcherTrackerException e)
			 * { throw new EventFrameworkDispatcherException(
			 * "Exception Occured while adding Event details to database : " +
			 * e.getMessage(), e.getCause()); }
			 */

		}
	}

	/**
	 * Here instead of publishing we have added RoiEvent in service-evt-uuid list.
	 * 
	 * @param camelExchange
	 * @param evtProdList
	 * @throws EventBuilderInstantiationException
	 * @throws InvalidEventException
	 * @throws EventFrameworkDispatcherException
	 * @throws MessageDispatchingException
	 * @throws LeapEventTransformationException
	 * @throws EventFrameworkConfigurationException
	 */
	private void publishEventForEventProducerList(Exchange camelExchange, List<Event> evtProdList)
			throws EventBuilderInstantiationException, InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException {
		AbstractCamelEventBuilder evtBuilder = null;
		if (evtProdList == null || evtProdList.isEmpty())
			return;

		LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		for (Event evt : evtProdList) {
			CamelEventProducer evtProd = evt.getCamelEventProducer();
			boolean isOgnl = EventBuilderHelper.isOgnlBuilderType(evtProd);
			if (isOgnl) {
				// EventBuilderHelper.constructSourceForOGNL(camelExchange);
				evtBuilder = getOGNLEventBuilderInstance();
			} else {
				evtBuilder = getEventBuilderInstance(evtProd.getCamelEventBuilder());
			}

			LeapEvent leapevent = evtBuilder.buildEvent(camelExchange, evt);
			eventService.publishEvent(leapevent, tenant, site, requestUUID, false);
			logger.trace("{} Event published for EventId {}", LEAP_LOG_KEY, leapevent.getId());
		}
	}

	/**
	 * This method is to get OGNL Event Builder Instance
	 * 
	 * @return AbstractCamelEventBuilder
	 */
	private AbstractCamelEventBuilder getOGNLEventBuilderInstance() {
		AbstractCamelEventBuilder evtBuilderInstance = (AbstractCamelEventBuilder) new OgnlEventBuilder();
		return evtBuilderInstance;
	}

	private List<Event> mergeEventProducerList(List<Event> sucessFailureList, List<Event> allList) {
		if (sucessFailureList == null && allList == null) {
			return null;
		} else if (sucessFailureList != null && allList == null) {
			return sucessFailureList;
		} else if (sucessFailureList == null && allList != null) {
			return allList;
		} else {
			sucessFailureList.addAll(allList);
			return sucessFailureList;
		}
	}

	/**
	 * Publish the standard/mandatory ServiceCompletionSucessEvent.
	 * 
	 * @param camelExchange
	 */
	public void publishServiceCompletionSuccessSystemEvent(RequestContext reqCtx, LeapServiceContext serviceDataContext,
			Exchange camelExchange) {
		String methodName = "publishServiceCompletionSuccessSystemEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);
		logger.trace("{} ConfigurationContext is :{} ", LEAP_LOG_KEY, configCtx);
		ServiceCompletionSuccessEventBuilder builder = (ServiceCompletionSuccessEventBuilder) evtConfigService
				.getServiceCompletionSuccessEventBuilder(configCtx);

		// Event configuration not required
		LeapEvent evt = builder.buildEvent(camelExchange, null);

		// #TODO Handling in separate Dispatcher
		// IExecutorService
		// disExeService=HCService.getHCInstance().getSystemEventDispatcherExecutor(tenantId)
		// LeapHeader leapHeader =
		// camelExchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY,
		// LeapHeader.class);
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		try {
			configCtx.setTenantId(tenant);
			SystemEvent systemEventConfig = evtConfigService.getSystemEventConfiguration(configCtx, evt.getId());
			if (systemEventConfig == null) {
				logger.error("{} Unable to get System Event Configuration for EventId ::{} ", LEAP_LOG_KEY,
						evt.getId());
				return;
			}
			EventDispatchers eventDispatchers = systemEventConfig.getEventDispatchers();
			if (eventDispatchers != null) {
				List<EventDispatcher> dispatcherList = eventDispatchers.getEventDispatcher();
				for (EventDispatcher eventDispatcher : dispatcherList) {
					String dispatchChannelId = eventDispatcher.getDispatchChannelId();
					evt.setDispatchChannelId(dispatchChannelId);
					eventService.publishSystemEvent(evt, tenant, site, requestUUID);
				}
			} else {
				logger.error("{} Unable to get Event Dispatchers for leap Event Id :{} ", LEAP_LOG_KEY, evt.getId());
			}
		} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
				| EventFrameworkConfigurationException invalidEvtExp) {
			logger.error("InvalidSystemEvent", invalidEvtExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Publish the standard/mandatory ServiceCompletionFailureEvent.
	 * 
	 * @param camelExchange
	 */
	public void publishServiceCompletionFailureSystemEvent(RequestContext reqCtx, Exchange camelExchange) {
		String methodName = "publishServiceCompletionFailureSystemEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);

		ServiceCompletionFailureEventBuilder builder = (ServiceCompletionFailureEventBuilder) evtConfigService
				.getServiceCompletionFailureEventBuilder(configCtx);
		// Event configuration not required
		LeapEvent evt = builder.buildEvent(camelExchange, null);
		LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		try {
			configCtx.setTenantId(tenant);
			SystemEvent systemEventConfig = evtConfigService.getSystemEventConfiguration(configCtx, evt.getId());
			if (systemEventConfig == null) {
				logger.error("{} Unable to get System Event Configuration for EventId ::{}", LEAP_LOG_KEY, evt.getId());
				return;
			}
			EventDispatchers eventDispatchers = systemEventConfig.getEventDispatchers();
			if (eventDispatchers != null) {
				List<EventDispatcher> dispatcherList = eventDispatchers.getEventDispatcher();
				for (EventDispatcher eventDispatcher : dispatcherList) {
					String dispatchChannelId = eventDispatcher.getDispatchChannelId();
					evt.setDispatchChannelId(dispatchChannelId);
					eventService.publishSystemEvent(evt, tenant, site, requestUUID);
				}
			} else {
				logger.error("{} Unable to get Event Dispatchers for ServiceCompletionFailureSystemEvent :{}",
						LEAP_LOG_KEY, evt.getId());
			}
		} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
				| EventFrameworkConfigurationException invalidEvtExp) {
			// #TODO Eating exception till I know what to do
			logger.error("{} InvalidSystemEvent {}", LEAP_LOG_KEY, invalidEvtExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void publishServicePerformanceLoggingSystemEvent(RequestContext reqCtx,
			LeapServiceContext serviceDataContext, Exchange camelExchange) {
		String methodName = "publishServicePerformanceLoggingSystemEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ConfigurationContext configCtx = new ConfigurationContext(reqCtx);

		ServicePerformanceLoggingEventBuilder builder = (ServicePerformanceLoggingEventBuilder) evtConfigService
				.getServicePerformanceLoggingEventBuilder(configCtx);
		// Event configuration not required
		LeapEvent evt = builder.buildEvent(camelExchange, null);
		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();
		String requestUUID = serviceDataContext.getRequestUUID();
		String eventId = evt.getId();
		try {
			configCtx.setTenantId(tenant);
			SystemEvent systemEventConfig = evtConfigService.getSystemEventConfiguration(configCtx, eventId);
			if (systemEventConfig == null) {
				logger.warn("{}  failed to get SystemEventConfiguration for EventId{} and context={}", LEAP_LOG_KEY,
						eventId, configCtx);
				return;
			}
			EventDispatchers eventDispatchers = systemEventConfig.getEventDispatchers();
			if (eventDispatchers != null) {
				List<EventDispatcher> dispatcherList = eventDispatchers.getEventDispatcher();
				for (EventDispatcher eventDispatcher : dispatcherList) {
					String dispatchChannelId = eventDispatcher.getDispatchChannelId();
					evt.setDispatchChannelId(dispatchChannelId);
					eventService.publishSystemEvent(evt, tenant, site, requestUUID);
				}
			} else {
				logger.error("{} Unable to get Event Dispatchers for ServicePerformanceLoggingSystemEvent :{}",
						LEAP_LOG_KEY, eventId);
			}
		} catch (InvalidEventException | LeapEventTransformationException | MessageDispatchingException
				| EventFrameworkConfigurationException invalidEvtExp) {
			// #TODO Eating exception till I know what to do
			logger.error("{} InvalidSystemEvent {}", LEAP_LOG_KEY, invalidEvtExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * 
	 * @param exchangeProperties
	 * @return
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	private boolean checkforWireTap(Map<String, Object> exchangeProperties) {
		List list = (List) exchangeProperties.get("CamelMessageHistory");
		logger.trace("{} ListOf CamelProps: {}", LEAP_LOG_KEY, list.toString());
		String txt = list.toString();
		String re1 = ".*?"; // Non-greedy match on filler
		String re2 = "(wireTap)"; // Word 1
		boolean hasKey = false;
		try {
			Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(txt);
			String word1 = null;
			if (m.find()) {
				word1 = m.group(1);
			}
			if (word1.toLowerCase().equalsIgnoreCase("wiretap")) {
				hasKey = true;
			}
		} catch (NullPointerException e) {
			hasKey = false;
		}
		return hasKey;
	}// ..end of method

	// #TODO THIS WILL NOT WORK IN osgi.TO CHANGED EITHER loaded spring or osgi
	// registry
	/**
	 * This method is used to create custom Event builder Instance
	 * 
	 * @param cEvtbuilder
	 * @return
	 * @throws EventBuilderInstantiationException
	 */
	@SuppressWarnings("rawtypes")
	private AbstractCamelEventBuilder getEventBuilderInstance(CamelEventBuilder cEvtbuilder)
			throws EventBuilderInstantiationException {
		String methodName = "getEventBuilderInstance";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String fqcn = cEvtbuilder.getEventBuilder().getFqcn();
		Class builderClass = null;
		try {
			// #TODO will not work in OSGI
			builderClass = Class.forName(fqcn);
			AbstractCamelEventBuilder evtBuilderInstance;
			evtBuilderInstance = (AbstractCamelEventBuilder) builderClass.newInstance();
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return evtBuilderInstance;
		} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			throw new EventBuilderInstantiationException();
		}
	}

}
