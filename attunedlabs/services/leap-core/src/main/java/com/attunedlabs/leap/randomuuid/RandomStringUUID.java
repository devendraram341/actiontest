package com.attunedlabs.leap.randomuuid;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;

public class RandomStringUUID {
	final Logger logger = LoggerFactory.getLogger(RandomStringUUID.class);

	/**
	 * This method is to generate random uuid for request
	 * 
	 * @param exchange :Exchange Object
	 */
	public void uuidgenrate(Exchange exchange) {
		String methodName = "uuidgenrate";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		logger.debug("{} Service context in before initializing RandomStringUUID {}", LEAP_LOG_KEY, leapServiceContext);
		if (leapServiceContext != null && leapServiceContext.getRequestUUID() == null) {
			// generating the random uuid
			String randomUUIDString = RandomStringUtils.randomAlphanumeric(8);
			logger.debug("{} random uuid {}", LEAP_LOG_KEY, randomUUIDString);
			leapServiceContext.setRequestUUID(randomUUIDString);
		} else
			logger.debug("{} request id already exist therefore, using the same request id", LEAP_LOG_KEY);
		logger.debug("{} Service context in  after initializing RandomStringUUID {}", LEAP_LOG_KEY, leapServiceContext);

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of method

}
