package com.attunedlabs.eventframework.dispatcher.transformer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.event.LeapEvent;

public class GenericLeapEventJsonTransformer implements ILeapEventTransformer {

	protected static final Logger logger = LoggerFactory.getLogger(GenericLeapEventJsonTransformer.class);

	@Override
	public Serializable transformEvent(LeapEvent leapevent) throws LeapEventTransformationException {
		String methodName = "transformEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject json = new JSONObject();
		if (leapevent == null) {
			throw new LeapEventTransformationException();
		}
		try {
			json.put(EventFrameworkConstants.EVENT_ID_KEY, leapevent.getId());
			json.put(EventFrameworkConstants.METADATA_KEY, leapevent.getMetadata());
			json.put(EventFrameworkConstants.OBJECT_KEY, leapevent.getObject());

			EventFrameworkConfigHelper.removeReqContextFromLeapEvent(json, leapevent);
			EventFrameworkConfigHelper.formattingEventStructure(json);
			logger.trace("{} json data :{} ", LEAP_LOG_KEY, json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return json.toString();
	}
}
