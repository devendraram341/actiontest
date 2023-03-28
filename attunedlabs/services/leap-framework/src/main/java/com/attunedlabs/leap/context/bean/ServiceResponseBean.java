package com.attunedlabs.leap.context.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;

public class ServiceResponseBean implements Processor {
	final static Logger log = LoggerFactory.getLogger(ServiceResponseBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
		log.trace("{} exchange Headers  : {}" ,LEAP_LOG_KEY, exchange.getIn().getHeaders());
		if (exchange.getIn().getHeaders().containsKey("leapDataContext")) {
			LeapDataContext leapDataContext = exchange.getIn().getHeader("leapDataContext", LeapDataContext.class);
			log.trace("{} All elemnts in queue : {}" + leapDataContext.getAllContextElement());
			Object serviceResponse = leapDataContext.getContextElementForTag("#leap_service_response")
					.getJSONObject("#leap_service_response").getJSONObject("data").getJSONObject("items")
					.get("element");
			exchange.getIn().setBody(serviceResponse);
		}
		log.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
	}

}
