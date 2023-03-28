package com.attunedlabs.eventframework.event;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.EventFrameworkDispatcherService;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;

public class LeapEventService implements ILeapEventService {
	private static final Logger logger = LoggerFactory.getLogger(LeapEventService.class);
	EventFrameworkDispatcherService dispatchService = new EventFrameworkDispatcherService();

	public void publishEvent(LeapEvent event, String tenant, String site, String requestId, boolean retryStatus)
			throws InvalidEventException, EventFrameworkConfigurationException, LeapEventTransformationException,
			MessageDispatchingException {
		String methodName = "publishEvent";
		logger.debug("{} entered into the method {} eventId {}", LEAP_LOG_KEY, methodName, event.getId());
		dispatchService.dispatchforEvent(event, tenant, site, requestId, retryStatus);
		logger.debug("{} eventId={} dispatched to dispatcher Service", LEAP_LOG_KEY, event.getId());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void publishEvent(LeapEvent event, String tenant, String site, String requestId, boolean retryStatus,
			EventDispatcherTracker tracker) throws InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException {
		String methodName = "publishEvent";
		logger.debug("{} entered into the method {} tracker {}", LEAP_LOG_KEY, methodName, tracker);
		dispatchService.dispatchforEvent(event, tenant, site, requestId, retryStatus, tracker);
		logger.debug("{} eventId={} dispatched to dispatcher Service", LEAP_LOG_KEY, event.getId());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public boolean validateEvent(LeapEvent event) throws InvalidEventException {
		// TODO Auto-generated method stub
		return false;
	}

	public void publishSystemEvent(LeapEvent event, String tenant, String site, String requestId)
			throws InvalidEventException, LeapEventTransformationException, MessageDispatchingException,
			EventFrameworkConfigurationException {
		String methodName = "publishSystemEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (!(event instanceof ServiceCompletionFailureEvent || event instanceof ServiceCompletionSuccessEvent
				|| event instanceof ServicePerformanceLoggingEvent)) {
			throw new InvalidEventException();
		}
		dispatchService.dispatchforSystemEvent(event, tenant, site, requestId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

}
