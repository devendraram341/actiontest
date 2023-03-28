package com.attunedlabs.leap.notifier;

import java.util.EventObject;

import org.apache.camel.management.event.CamelContextStartedEvent;
import org.apache.camel.management.event.CamelContextStoppingEvent;
import org.apache.camel.spi.EventNotifier;

public abstract class LeapStartupNotifier implements EventNotifier {

	public abstract void notify(EventObject event) throws Exception;

	@Override
	public boolean isEnabled(EventObject event) {
		if (event instanceof CamelContextStartedEvent || event instanceof CamelContextStoppingEvent) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDisabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreCamelContextEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeCompletedEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeCreatedEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeFailedEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeRedeliveryEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeSendingEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreExchangeSentEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreRouteEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIgnoreServiceEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIgnoreCamelContextEvents(boolean ignoreCamelContextEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeCompletedEvent(boolean ignoreExchangeCompletedEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeCreatedEvent(boolean ignoreExchangeCreatedEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeEvents(boolean ignoreExchangeEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeFailedEvents(boolean ignoreExchangeFailureEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeRedeliveryEvents(boolean ignoreExchangeRedeliveryEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeSendingEvents(boolean ignoreExchangeSendingEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreExchangeSentEvents(boolean ignoreExchangeSentEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreRouteEvents(boolean ignoreRouteEvents) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreServiceEvents(boolean ignoreServiceEvents) {
		// TODO Auto-generated method stub

	}

}
