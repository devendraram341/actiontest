package com.attunedlabs.eventframework.dispatcher.transformer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.event.LeapEvent;

public class LeapEventXmlTransformer implements ILeapEventTransformer {

	protected static final Logger logger = LoggerFactory.getLogger(LeapEventXmlTransformer.class);

	private String xsltname = null;
	private String xsltAsString = null;

	public LeapEventXmlTransformer(String xsltname, String xsltAsString) {
		this.xsltname = xsltname;
		this.xsltAsString = xsltAsString;
	}

	@Override
	public Serializable transformEvent(LeapEvent leapevent) throws LeapEventTransformationException {
		String methodName = "transformEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		XmlTransformerHelper xmlTransformerHelper = new XmlTransformerHelper();
		// converting Leapevent object to xml
		String eventxml = xmlTransformerHelper.convertEventObjectToXml(leapevent);
		logger.trace("{} leap event xml string :{} ", LEAP_LOG_KEY, eventxml);
		// converting eventxml to custom xml using xslt
		String customeventxml = xmlTransformerHelper.createCustomXml(eventxml, xsltname, xsltAsString);
		logger.trace("{} leap custom event xml : {}", LEAP_LOG_KEY, customeventxml);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return customeventxml;
	}
}
