package com.attunedlabs.leap.notifier;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.camel.ComponentEventProducer;
import com.attunedlabs.eventframework.event.ILeapEventService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.LeapEventService;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * LeapEventDispatcher decides and dispatches the events on service execution
 * with failure or without failure.
 * 
 * @author Reactiveworks dispatchEvents
 */
public class LeapEventDispatcher {

	private static final String IS_SERVICE_COMPLETED = "isServiceCompleted";

	private static final String QUARTZ_TRIGGER = "quartzTrigger";

	private static final Logger logger = LoggerFactory.getLogger(LeapEventDispatcher.class);

	ComponentEventProducer evtProducer = new ComponentEventProducer();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
	ILeapEventService eventService = new LeapEventService();

	/**
	 * Decides whether the event has to be dispatched or not.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void dispatchEvents(Exchange exchange) throws Exception {

		// no event dispatching for the failed event retrying.
		if (exchange.getIn().getHeader(QUARTZ_TRIGGER) != null) {
			logger.debug("{} Quartz triggered  successfully from Event RetrySide...", LEAP_LOG_KEY);
			return;
		}

		// no event dispatching for the subscription quartz trigger.
		if (exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY) != null) {
			logger.debug("{} Quartz triggered  successfully...", LEAP_LOG_KEY);
			return;
		}

		// no event dispatching for the cached result.
		if (exchange.getIn().getHeader("cachedResult") != null) {
			logger.debug("{} No Execution Feature Dynamic called...", LEAP_LOG_KEY);
			return;
		}
		// event is dispatched only if service is completed successfully or failed.
		if (exchange.getIn().getHeader(IS_SERVICE_COMPLETED) != null) {
			Boolean isServiceCompleted = exchange.getIn().getHeader(IS_SERVICE_COMPLETED, Boolean.class);
			if (isServiceCompleted) {
				// dispatching events on successful completion.
				dispatchEventsOnSuccess(exchange);
			} else {
				// dispatching events on failure completion.
				dispatchEventsOnFailure(exchange);
			}
		} // end of outer-if
	}// end of method

	/**
	 * This method is used to store the successful service completion event in the
	 * service context event holder map.
	 * 
	 * @param exchange
	 */
	public void storeSuccessServiceCompletionEventIncache(Exchange exchange) {
		String methodName = "storeSuccessServiceCompletionEventIncache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String serviceName = serviceDataContext.getRunningContextServiceName();
		logger.trace("{} name of the service to get the service completion event : {}", LEAP_LOG_KEY, serviceName);
		evtProducer.publishEventForRouteCompletion(serviceName, exchange);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method storeSuccessServiceCompletionEventIncache

	/**
	 * Dispatches all the successful events when the service execution is completed.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	private void dispatchEventsOnSuccess(Exchange exchange) throws Exception {
		final String methodName = "dispatchEventsOnSuccess";
		long startTime = System.currentTimeMillis();
		logger.debug("{} enter inside the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		RequestContext reqCtx = serviceDataContext.getRequestContext();
		String tenantid = serviceDataContext.getTenant();
		String siteid = serviceDataContext.getSite();
		String requestId = serviceDataContext.getRequestUUID();
		String eventStoreKey = serviceDataContext.getRequestUUID();
		String serviceName = serviceDataContext.getRunningContextServiceName();
		logger.trace("{} publishing event for method {} tenant {} , site {} , requestId {} , serviceName {} ",
				LEAP_LOG_KEY, methodName, tenantid, siteid, requestId, serviceName);

		// Adding route completion event to service context service completion
		// serviceCompletionEventHolder
		evtProducer.publishEventForRouteCompletion(serviceName, exchange);
		long endTime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in publishEventForRouteCompletion {} ", LEAP_LOG_KEY, (endTime - startTime));

		// Fetching the component events from service context event holder map.
		ArrayList<LeapEvent> leapEvents = LeapEventContext.getLeapEvents(requestId, serviceDataContext);
		long fetchtime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in LeapEventContext.getLeapEvents {} ", LEAP_LOG_KEY, (fetchtime - endTime));

		// Publishing both component and service event.
		evtProducer.publishComponentEvent(leapEvents, tenantid, siteid, requestId, eventStoreKey, serviceDataContext,
				exchange);
		long publishTime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in publishComponentEvent {} ", LEAP_LOG_KEY, (publishTime - fetchtime));

		// publish all the success service completion event stored in
		// serviceCompletionEventHolder of service context
		ArrayList<LeapEvent> serviceCompletionLeapEvents = getAllServiceCompletionEvent(serviceDataContext, exchange);
		evtProducer.updateServiceCompletionEventAndPublish(serviceCompletionLeapEvents, tenantid, siteid, requestId,
				eventStoreKey, serviceDataContext, exchange);

		// publishing system events.
		evtProducer.publishServiceCompletionSuccessSystemEvent(reqCtx, serviceDataContext, exchange);
		long systemTime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in publishServiceCompletionSuccessSystemEvent {}", LEAP_LOG_KEY,
				(systemTime - publishTime));

		// logging the performance
		evtProducer.publishServicePerformanceLoggingSystemEvent(reqCtx, serviceDataContext, exchange);
		long loggingTime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in publishServicePerformanceLoggingSystemEvent {}", LEAP_LOG_KEY,
				(loggingTime - systemTime));

		logger.debug("{} Ending Time for method {} {}", LEAP_LOG_KEY, methodName, (loggingTime));
		logger.debug("{} exit from the method {}", LEAP_LOG_KEY, methodName);

	}// end of method

	/**
	 * Dispatches all the failure events when the service execution is failed.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	private void dispatchEventsOnFailure(Exchange exchange) throws Exception {
		final String methodName = "dispatchEventsOnFailure";
		logger.debug("{} enter inside the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		if (leapDataContext == null) {
			return;
		}
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		serviceDataContext.removeAllServiceRuntimeContextExceptParent();
		RequestContext reqCtx = serviceDataContext.getRequestContext();
		String tenantid = serviceDataContext.getTenant();

		String siteid = serviceDataContext.getSite();

		String serviceName = serviceDataContext.getRunningContextServiceName();
		logger.trace("{} publishing event for method {} tenant {} , site {} , requestctx {} , serviceName {} ",
				LEAP_LOG_KEY, methodName, tenantid, siteid, reqCtx, serviceName);
		// Adding route failure event to database and header.
		evtProducer.publishEventForFailedRouteCompletion(serviceName, exchange);

		// publishing system events.
		evtProducer.publishServiceCompletionFailureSystemEvent(reqCtx, exchange);

		logger.debug("{} exit from the method {}", LEAP_LOG_KEY, methodName);

	}// end of method

	/**
	 * This method is used to publish all the service completion event from service
	 * context
	 * 
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @param exchange
	 * @return {@link ArrayList<LeapEvent>}
	 * @throws EventDispatcherTrackerException
	 */
	private ArrayList<LeapEvent> getAllServiceCompletionEvent(LeapServiceContext leapServiceContext, Exchange exchange)
			throws EventDispatcherTrackerException {
		final String methodName = "getAllServiceCompletionEvent";
		logger.debug("{} enter inside the method {}", LEAP_LOG_KEY, methodName);
		String requestId = leapServiceContext.getRequestUUID();
		ArrayList<LeapEvent> leapEvents = LeapEventContext.getServiceCompletionLeapEvents(requestId,
				leapServiceContext);
		if (leapEvents != null && !leapEvents.isEmpty()) {
			logger.trace(" {} no. of service completion event {} ", LEAP_LOG_KEY, leapEvents.size());
			eventDispatcherTrackerService.addEventTracking(leapServiceContext.getTenant(), leapServiceContext.getSite(),
					leapServiceContext.getRequestUUID(), leapServiceContext.getRequestUUID(), leapEvents, false,
					exchange);
		} else
			logger.warn("{} No service completion events are defined ", LEAP_LOG_KEY);

		logger.debug("{} exit from the method {}", LEAP_LOG_KEY, methodName);
		return leapEvents;
	}

}
