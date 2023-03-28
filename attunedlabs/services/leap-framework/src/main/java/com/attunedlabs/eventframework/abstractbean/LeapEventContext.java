package com.attunedlabs.eventframework.abstractbean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.leap.LeapServiceContext;

@SuppressWarnings("unchecked")
public class LeapEventContext {

	private static final String LEAP_EVENTS = "LEAP_EVENTS-";

	protected static final Logger logger = LoggerFactory.getLogger(LeapEventContext.class);

	public static void deleteLeapEvents(String requestId, LeapEvent leapEvent, Exchange exchange) {
		String methodName = "deleteLeapEvents";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object eventHeaders = exchange.getIn().getHeader(LEAP_EVENTS + requestId);
		Map<String, ArrayList<LeapEvent>> txEventMap = null;

		if (eventHeaders != null)
			txEventMap = (Map<String, ArrayList<LeapEvent>>) eventHeaders;
		logger.debug("{} size of txEventMap :{} ", LEAP_LOG_KEY, txEventMap.size());
		ArrayList<LeapEvent> events = txEventMap.get(requestId);
		for (int i = 0; i < events.size(); i++) {
			String eventId = leapEvent.getId();
			LeapEvent event = events.get(i);
			if (eventId.equals(event.getId()))
				events.remove(events.remove(events.indexOf(event)));
		}
		if (events.isEmpty())
			txEventMap.remove(requestId);
		else
			txEventMap.put(requestId, events);
		logger.trace("{} Deleted Event list for {}:{}", LEAP_LOG_KEY, requestId, events);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to add event to service context event holder Map
	 * 
	 * @param requestId          : unique request id
	 * @param leapEvent          : {@link LeapEvent}
	 * @param serviceDataContext : {@link LeapServiceContext}
	 */
	public static void addLeapEvent(String requestId, LeapEvent leapEvent, LeapServiceContext serviceDataContext) {
		String methodName = "addLeapEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		serviceDataContext.storeEventHolderInServiceContext(requestId, leapEvent);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method of addLeapEvent

	/**
	 * This method is used to get event from service context event holder Map
	 * 
	 * @param requestId           : unique request id
	 * @param leapEvent           : {@link LeapEvent}
	 * @param serviceDataContext: {@link LeapServiceContext}
	 */
	public static ArrayList<LeapEvent> getLeapEvents(String requestId, LeapServiceContext serviceDataContext) {
		String methodName = "getLeapEvents";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<LeapEvent> leapEventList = serviceDataContext.getEventHolderByRequestId(requestId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapEventList;
	}// end of method of getLeapEvents

	/**
	 * This method is used to add service completion event to service context event
	 * holder Map
	 * 
	 * @param requestId          : unique request id
	 * @param leapEvent          : {@link LeapEvent}
	 * @param serviceDataContext : {@link LeapServiceContext}
	 */
	public static void addServiceCompletionLeapEvent(String requestId, LeapEvent leapEvent,
			LeapServiceContext serviceDataContext) {
		String methodName = "addServiceCompletionLeapEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		serviceDataContext.storeServiceCompletionEventHolderInServiceContext(requestId, leapEvent);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method of addServiceCompletionLeapEvent

	/**
	 * This method is used to get the service completion event from service context
	 * 
	 * @param requestId          : unique request id
	 * @param serviceDataContext : {@link LeapServiceContext}
	 * @return
	 */
	public static ArrayList<LeapEvent> getServiceCompletionLeapEvents(String requestId,
			LeapServiceContext serviceDataContext) {
		String methodName = "getServiceCompletionLeapEvents";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<LeapEvent> leapEventList = serviceDataContext.getServiceCompletionEventHolderByRequestId(requestId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapEventList;
	}// end of method of getServiceCompletionLeapEvents

}
