package com.attunedlabs.eventframework.camel.eventbuilder;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.camel.eventproducer.CamelEventProducerConstant;
import com.attunedlabs.eventframework.jaxb.CamelEventProducer;

/**
 * This class is to helper class for building and event
 * 
 * @author Deepali
 *
 */
//#TODO Review comments Change the implementation of this class.
public class EventBuilderHelper {

	protected static final Logger logger = LoggerFactory.getLogger(EventBuilderHelper.class);

	/**
	 * This method is to check CamelBuilder type is OGNL or not
	 * 
	 * @param evtProdConfig : CamelEventProducer Object
	 * @return boolean
	 */
	public static boolean isOgnlBuilderType(CamelEventProducer evtProdConfig) {
		String methodName = "isOgnlBuilderType";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String buildertype = evtProdConfig.getCamelEventBuilder().getType();
		if (buildertype.equalsIgnoreCase(CamelEventProducerConstant.OGNL_EVENT_BUILDER)) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return true;
		} else {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return false;
		}

	}// end of eventSourceBasedOnType method

	/**
	 * This method is to get internal configuration key
	 * 
	 * @param tenantid : tenant in String
	 * @return internal configuration key in String
	 */
	private static String getInternalEventKey(String tenantid) {
		if (tenantid == null && tenantid.isEmpty()) {
			tenantid = "default";
		}
		String internalgroupkey = tenantid + "-" + CamelEventProducerConstant.INTERNAL_GROUP_KEY;
		return internalgroupkey;

	}
}
