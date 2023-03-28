package com.attunedlabs.eventframework.config;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.AbstractLeapCamelBean;

public class TestEventConfig extends AbstractLeapCamelBean{

	Logger logger=LoggerFactory.getLogger(TestEventConfig.class);
	@Override
	protected void processBean(Exchange exch) throws Exception {
		
		logger.debug("Successfully Calling Event Configuration ::");
	}

}
