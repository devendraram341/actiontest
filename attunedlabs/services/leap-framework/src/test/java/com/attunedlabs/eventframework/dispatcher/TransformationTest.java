package com.attunedlabs.eventframework.dispatcher;

import java.io.Serializable;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.eventframework.dispatcher.transformer.LeapEventTransformationException;
import com.attunedlabs.eventframework.event.LeapEvent;

public class TransformationTest implements ILeapEventTransformer{

	final Logger log=LoggerFactory.getLogger(getClass());
	@Override
	public Serializable transformEvent(LeapEvent leapevent) throws LeapEventTransformationException {
		JSONObject json = new JSONObject(leapevent);
		log.debug("CUSTOM JSON DATA"+json.toString());
		String xml =XML.toString(json);
		return xml;
	}

}
