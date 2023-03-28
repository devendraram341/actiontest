package com.attunedlabs.eventframework.config;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.camel.eventproducer.AbstractCamelEventBuilder;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.jaxb.Event;

public class TestEventBuilder extends AbstractCamelEventBuilder{

	private Logger logger=LoggerFactory.getLogger(TestEventBuilder.class);
	@Override
	public LeapEvent buildEvent(Exchange camelExchange, Event eventConfig) {
		
		logger.debug("Successfully calling EventBuilder class ::");
		return null;
	}

}
