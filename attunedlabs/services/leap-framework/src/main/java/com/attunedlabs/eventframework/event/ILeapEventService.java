package com.attunedlabs.eventframework.event;

import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;

public interface ILeapEventService {
	public void publishEvent(LeapEvent event, String tenant, String site, String requestUUID, boolean retryStatus)
			throws InvalidEventException, EventFrameworkConfigurationException, LeapEventTransformationException,
			MessageDispatchingException;

	public void publishEvent(LeapEvent event, String tenant, String site, String requestUUID, boolean retryStatus,
			EventDispatcherTracker tracker) throws InvalidEventException, EventFrameworkConfigurationException,
			LeapEventTransformationException, MessageDispatchingException;

	public boolean validateEvent(LeapEvent event) throws InvalidEventException;

	public void publishSystemEvent(LeapEvent event, String tenant, String site, String requestUUID)
			throws InvalidEventException, LeapEventTransformationException, MessageDispatchingException,
			EventFrameworkConfigurationException;
}
