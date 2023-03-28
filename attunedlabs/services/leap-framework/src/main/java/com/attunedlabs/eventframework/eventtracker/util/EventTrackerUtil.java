package com.attunedlabs.eventframework.eventtracker.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;

public class EventTrackerUtil {

	private static Logger logger = LoggerFactory.getLogger(EventTrackerUtil.class);

	/**
	 * create the json for failure events.
	 * 
	 * @param failureJson
	 * @param exception
	 * @param failureMsg
	 */
	public static void setFailureJSONString(JSONObject failureJson, Exception exception, String failureMsg) {
		String methodName = "setFailureJSONString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (failureMsg == null)
			failureMsg = "empty exception message!";
		else
			failureMsg = exception.getMessage();

		try {
			failureJson.put(EventTrackerTableConstants.FAILURE_TYPE, exception.getClass().getSimpleName());
			failureJson.put(EventTrackerTableConstants.FAILURE_MESSAGE, failureMsg);
			failureJson.put(EventTrackerTableConstants.IS_RETRYABLE,
					exception instanceof RetryableMessageDispatchingException);
		} catch (JSONException e) {
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}
}
