package com.attunedlabs.leap.logging;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;

public class LeapLoggingConstants {
	private static final String LEAP_LOG_PROPERTY = "leap.logging.key";
	public static String LEAP_LOG_KEY = LeapConfigUtil.getGlobalPropertyValue(LEAP_LOG_PROPERTY,LeapDefaultConstants.DEFAULT_LEAP_LOGGING_KEY);
}
