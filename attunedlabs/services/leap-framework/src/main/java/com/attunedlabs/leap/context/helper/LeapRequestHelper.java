package com.attunedlabs.leap.context.helper;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContextConfigException;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapRequest;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Reactiveworks
 *
 */
public class LeapRequestHelper {

	static Logger logger = LoggerFactory.getLogger(LeapRequestHelper.class);

	/**
	 * Method used to create Leap Request Instance from the given king and data.
	 * 
	 * @param jsonObject
	 * @param kind
	 * @return Returns LeapRequest Instance.
	 * @throws LeapDataContextConfigException
	 */
	public static LeapRequest constructLeapRequestFromJSONObject(JSONObject jsonObject)
			throws LeapDataContextConfigException {
		String methodName = "constructLeapRequestFromJSONObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		LeapRequest leapRequest = new LeapRequest();
		try {

			leapRequest.setApiVersion(LeapDataContextConstant.API_VERSION);
			leapRequest.setContext(LeapDataContextConstant.CONTEXT);
			leapRequest.setLang(LeapDataContextConstant.LANG);
			leapRequest.setData(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapRequest;
	}

	/**
	 * Method used to convert a LeapRequest instance to JSON Object.
	 * 
	 * @param leapRequest
	 * @return Returns a Leap Request in JSON format.
	 */
	public static JSONObject toJson(LeapRequest leapRequest) {
		logger.debug("{} Inside toJson method {}" ,LEAP_LOG_KEY, leapRequest);
		JSONObject jsonObj = new JSONObject(leapRequest);
		logger.debug("The JSON value is ::" + jsonObj);
		return jsonObj;
	}

	/**
	 * Method used to get the Json data present in LeapRequest.
	 * 
	 * @param leapRequest
	 * @return Returns LeapRequest JSON data.
	 */
	public static JSONObject getJsonData(String leapRequest) {
		logger.debug("{} Inside getJsonData",LEAP_LOG_KEY);
		JSONObject leapData = null;
		JSONObject itemsObject = null;
		if (!leapRequest.isEmpty()) {
			leapData = new JSONObject(leapRequest);
			return leapData.getJSONObject("data");
		}
		return itemsObject;
	}
}
