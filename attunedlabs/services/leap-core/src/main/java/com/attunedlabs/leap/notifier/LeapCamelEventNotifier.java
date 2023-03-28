package com.attunedlabs.leap.notifier;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.camel.ComponentEventProducer;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

//NOTE: event notifier is moved to the event dispatcher and this class is not used anywhere, it is keep for future references.
public class LeapCamelEventNotifier extends EventNotifierSupport {
	final Logger logger = LoggerFactory.getLogger(LeapCamelEventNotifier.class);

	// EventNotifierHelper evtNotHelper=new EventNotifierHelper();
	ComponentEventProducer evtProducer = new ComponentEventProducer();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	public boolean isIgnoreCamelContextEvents() {
		return true;
	}

	public boolean isIgnoreExchangeEvents() {
		return false;
	}

	public boolean isIgnoreExchangeCompletedEvent() {
		return false;
	}

	public boolean isIgnoreExchangeCreatedEvent() {
		return true;
	}

	public boolean isIgnoreExchangeFailedEvents() {
		return false;
	}

	public boolean isIgnoreExchangeRedeliveryEvents() {
		return true;
	}

	public boolean isIgnoreExchangeSendingEvents() {
		return true;
	}

	public boolean isIgnoreExchangeSentEvents() {
		return true;
	}

	public boolean isIgnoreRouteEvents() {
		return true;
	}

	public boolean isIgnoreServiceEvents() {
		return true;
	}

	/**
	 * This method is to check notifier component is enabled or disabled.
	 * 
	 * @param event : EventObject
	 * @return boolean
	 */
	public boolean isEnabled(EventObject event) {
		String methodName = "isEnabled";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// AbstractExchangeEvent compEvent = (AbstractExchangeEvent) event;
		// Exchange exchange = compEvent.getExchange();
		// String routeId=exchange.getFromRouteId();
		// String contextStr=exchange.getContext().getName();
		// logger.info("isEnabled -- Exchange="+exchange);
		if (event instanceof ExchangeSendingEvent) {
			// logger.info("isEnabled -- CompletedEvent=");
			return true;
			// return
			// evtNotHelper.hasEventForRoute("default",contextStr,routeId);
		} else if (event instanceof ExchangeSentEvent) {
			// logger.info("isEnabled -- ExchangeSentEvent="+exchange);
			return false;
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	/**
	 * This method is to notify Exchange Completed (success or failure)
	 * 
	 * @param event : EventObject
	 * @return boolean
	 */

	@Override
	public void notify(EventObject event) throws Exception {
		String methodName = "notify";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (event instanceof ExchangeCompletedEvent) {
			// Exchange/complete route from base till Impl route can be
			// Completed with Failure or Success
			logger.trace("when event is ExchangeComplete Event :{}", LEAP_LOG_KEY);
			ExchangeCompletedEvent compEvent = (ExchangeCompletedEvent) event;
			Exchange exchange = compEvent.getExchange();

			if (exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY) != null) {
				logger.debug("Quartz triggered  successfully...{}", LEAP_LOG_KEY);
				return;
			}
			if (exchange.getIn().getHeader("cachedResult") != null) {
				logger.debug("No Execution Feature Dynamic called...{}", LEAP_LOG_KEY);
				return;
			}
			LeapDataContext leapDataContext = null;
			if (exchange.getIn().getHeader("quartzTrigger") != null) {
				logger.trace("{} Quartz triggered  successfully...", LEAP_LOG_KEY);
				if (exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT) instanceof LeapDataContext) {
					leapDataContext = (LeapDataContext) exchange.getIn()
							.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
					// closing all datasource connection on success{
					closeAllDataSourceConnection(leapDataContext);
				}
				return;
			}

			if (exchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT) instanceof LeapDataContext) {
				leapDataContext = (LeapDataContext) exchange.getIn()
						.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
				LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
				if (leapServiceContext != null && leapServiceContext.getTenant() != null
						&& leapServiceContext.getSite() != null && leapServiceContext.getRequestUUID() != null) {
					RequestContext reqCtx = leapServiceContext.getRequestContext();
					logger.trace("{} leapHeader in notifier :{} ", LEAP_LOG_KEY, leapServiceContext);
					// tenat and site
					String tenantid = leapServiceContext.getTenant();
					String siteid = leapServiceContext.getSite();
					String requestId = leapServiceContext.getRequestUUID();
					String eventStoreKey = leapServiceContext.getRequestUUID();
					String featureName = leapServiceContext.getFeatureName();
					String serviceName = leapServiceContext.getRunningContextServiceName();
					logger.trace("{} Inside LeapCamelEventNotifier Events published for tenant :{}, site :{} ",
							LEAP_LOG_KEY, tenantid, siteid);
					logger.trace(
							"{} data into notifier exchngeCompleteEvent : serviceName :{} , tenantid :{} , feature Name :{}",
							LEAP_LOG_KEY, serviceName, tenantid, featureName);
					Map<String, Object> exchangeProp = exchange.getProperties();
					logger.trace("{} exchnage property values  in LeapCamelEventNotifier :{} ", LEAP_LOG_KEY,
							exchangeProp);
					if (!checkMultipleExchange(exchangeProp)) {
						boolean iscompletedWithFailure = isCompletedWithFailure(exchangeProp);
						logger.trace("{} value of isCompletedWithFailure :{} ", LEAP_LOG_KEY, iscompletedWithFailure);
						if (iscompletedWithFailure) {
							logger.trace("{} notify --FialedCompletedEvent-- EventObject={}", LEAP_LOG_KEY,
									featureName);
							// Build Event for Route Failed Condition and publish to
							// event Service
							evtProducer.publishEventForFailedRouteCompletion(serviceName, exchange);
							// Build standard mandatory event for Failure and
							// publish to eventService
							evtProducer.publishServiceCompletionFailureSystemEvent(reqCtx, exchange);
							// closing all datasource connection on success
							closeAllDataSourceConnection(leapDataContext);
						} else {
							logger.trace("{} notify --Entered CompletedEvent (SuccessEvents)--Feature={}", LEAP_LOG_KEY,
									featureName);

							// Add the event generated by the service during
							// camel Route and gather it same HazelCastList where
							// component events are added.
							evtProducer.publishEventForRouteCompletion(serviceName, exchange);

							IMap<String, ArrayList<LeapEvent>> hzEventMap = getHazelCastMapForEvent(leapServiceContext);

							// publish component and service event and updates
							// status of event track table.
							evtProducer.publishComponentEvent(hzEventMap.get(requestId), tenantid, siteid, requestId,
									eventStoreKey, null, exchange);
							logger.trace("{} notify --No CompletedEvent--found for Feature={}", LEAP_LOG_KEY,
									serviceName);

							// Build <Configured> Events for Route Sucecss Condition
							// and publish to event Service
							// Build standard mandatory event for serviceName/Route
							// sucess and publish to eventService
							evtProducer.publishServiceCompletionSuccessSystemEvent(reqCtx, null, exchange);
							evtProducer.publishServicePerformanceLoggingSystemEvent(reqCtx, null, exchange);

							// closing all datasource connection on success
							closeAllDataSourceConnection(leapDataContext);
						}
						logger.trace("{} notify -- EventObject=:", LEAP_LOG_KEY, exchange.getIn());
					}
				}
			}
		} else if (event instanceof ExchangeFailedEvent) {
			// Cases were Exchange failed specifically
			logger.debug("{} inside exchanged failed event : ", LEAP_LOG_KEY);
			ExchangeFailedEvent compEvent = (ExchangeFailedEvent) event;
			Exchange exchange = compEvent.getExchange();
			LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
					.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			if (leapServiceContext != null && leapServiceContext.getTenant() != null
					&& leapServiceContext.getSite() != null && leapServiceContext.getRequestUUID() != null) {
				logger.trace("{} leapHeader in notifier : {}", LEAP_LOG_KEY, leapServiceContext);
				RequestContext reqCtx = leapServiceContext.getRequestContext();
				String serviceName = leapServiceContext.getRunningContextServiceName();

				// Build Event for Route Failed Condition and publish to event
				// Service
				evtProducer.publishEventForFailedRouteCompletion(serviceName, exchange);
				// Build standard mandatory event for Failure and publish to
				// eventService
				evtProducer.publishServiceCompletionFailureSystemEvent(reqCtx, exchange);
				// closing all datasource connection on success
				closeAllDataSourceConnection(leapDataContext);
			}
		} else {
			logger.debug("{} notify --OtherEventType--Event={}", LEAP_LOG_KEY, event);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * to check the multiplicity of exchanges, for the Multicast-EIP
	 * 
	 * @param exchangeProp
	 * @return true if chances of multipleExchangeExists
	 */
	private boolean checkMultipleExchange(Map<String, Object> exchangeProp) {
		if ((!exchangeProp.containsKey(Exchange.MULTICAST_INDEX))) {
			return false;
		} else {
			exchangeProp.remove(Exchange.MULTICAST_INDEX);
			return true;
		}
	}// ..end of the method

	/**
	 * This method is to get List of all LeapEvents Store in hazelcast
	 * 
	 * @param exchange : Exchange
	 * @return List
	 */
	private IMap<String, ArrayList<LeapEvent>> getHazelCastMapForEvent(LeapServiceContext leapServiceContext) {
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		String requestId = leapServiceContext.getRequestUUID();
		logger.trace("{} getHazelCastMapForEvent requestId:{} ", LEAP_LOG_KEY, requestId);
		IMap<String, ArrayList<LeapEvent>> eventMap = hazelcastInstance.getMap(requestId);
		logger.debug("{} eventList id in getHazelcast :{} ", LEAP_LOG_KEY, eventMap);

		if (eventMap == null || eventMap.isEmpty() || (ArrayList<LeapEvent>) eventMap.get(requestId) == null) {
			return eventMap;
		}
		for (LeapEvent event : (ArrayList<LeapEvent>) eventMap.get(requestId)) {
			logger.debug("{} notify  ------getHazelCastListValue{}", LEAP_LOG_KEY, event.getId());

		}

		return eventMap;
	}

	/**
	 * This method is to check Exchanged finised success or failure
	 * 
	 * @param exchangeProp
	 * @return
	 */
	private boolean isCompletedWithFailure(Map<String, Object> exchangeProp) {
		logger.debug("inside isCompleteWithFailure() in LeapCamelNotifier bean {}", LEAP_LOG_KEY);
		boolean isfailure = exchangeProp.containsKey("CamelFailureRouteId");
		logger.debug("{} isFailure is  : {}", LEAP_LOG_KEY, isfailure);
		return isfailure;
	}

	/**
	 * This method is used close all open data source connection
	 * 
	 * @param leapHeader : leapHeader Object
	 * @throws SQLException
	 */
	private void closeAllDataSourceConnection(LeapDataContext leapDataContext) throws SQLException {
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		Map<Object, Object> mapResourceHolder = leapServiceContext.getResourceHolder();
		for (int idx = 0; idx < mapResourceHolder.size(); idx++) {
			Connection connection = (Connection) mapResourceHolder.get(idx);
			if (connection != null) {
				if (!(connection.isClosed())) {
					connection.close();
				}
			}
		} // end of for
	}// end of method closeAllConnection

}
