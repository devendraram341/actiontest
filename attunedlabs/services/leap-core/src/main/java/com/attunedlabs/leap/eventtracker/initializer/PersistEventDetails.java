package com.attunedlabs.leap.eventtracker.initializer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

/**
 * <code>PersistEventDetails</code> is called from exit route for persisting
 * event details for newly created event List which is still in transaction so
 * the persist operation will also be in transaction until the commit invoked by
 * JTATransactionManager.
 * 
 * @author Reactiveworks42
 *
 */
public class PersistEventDetails {
	final Logger logger = LoggerFactory.getLogger(PersistEventDetails.class);
	public static final String REQUEST_ID = "REQUEST_ID";

	/**
	 * adding the event details with status NEW and Event_Created_DTM will be
	 * current system time.
	 * 
	 * @throws EventDispatcherTrackerException
	 * 
	 */
	public void processBean(Exchange camelExchange) throws EventDispatcherTrackerException {
		String leapId = null;
		try {
			String methodName = "processBean";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			LeapDataContext leapDataContext = (LeapDataContext) camelExchange.getIn().getHeader(LEAP_DATA_CONTEXT);
			LeapServiceContext leapserviceContext = leapDataContext.getServiceDataContext();
			IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
			boolean canPersistEvent = leapserviceContext.isOnlyRuntimeServiceContext();
			if (canPersistEvent) {
				logger.trace("{} Can persist the event as the running context is the only context", LEAP_LOG_KEY);
				String requestId = leapserviceContext.getRequestUUID();
				ArrayList<LeapEvent> leapEvents = LeapEventContext.getLeapEvents(requestId, leapserviceContext);
				if (leapEvents != null && !leapEvents.isEmpty())
					eventDispatcherTrackerService.addEventTracking(leapserviceContext.getTenant(),
							leapserviceContext.getSite(), leapserviceContext.getRequestUUID(),
							leapserviceContext.getRequestUUID(), leapEvents, true, camelExchange);
				camelExchange.getIn().setHeader(REQUEST_ID, leapserviceContext.getRequestUUID());
			} else {
				logger.debug("{} Don't persist the event now, it a internal service call", LEAP_LOG_KEY);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Error Occured while adding the Event to even Dispatch Tracker service for : " + leapId);
		}
	}

}
