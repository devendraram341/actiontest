package com.attunedlabs.eventframework.dispatcher;

import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;

public interface IEventFrameworkIDispatcherService {
	public void dispatchforEvent(LeapEvent leapEvent, String tenant, String site, String requestId, boolean retryStatus)
			throws LeapEventTransformationException, MessageDispatchingException, EventFrameworkConfigurationException;
}
