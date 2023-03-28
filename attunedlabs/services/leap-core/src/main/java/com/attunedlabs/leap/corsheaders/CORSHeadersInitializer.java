package com.attunedlabs.leap.corsheaders;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CORSHeadersInitializer {
	Logger logger = LoggerFactory.getLogger(CORSHeadersInitializer.class.getName());

	public void setCorsHeaders(Exchange exchange) {
		String originHeader = (String) exchange.getIn().getHeader("Origin");
		logger.debug("{} originHeader :{} ", LEAP_LOG_KEY, originHeader);
		exchange.getIn().setHeader("originHeader", originHeader);

	}

}
