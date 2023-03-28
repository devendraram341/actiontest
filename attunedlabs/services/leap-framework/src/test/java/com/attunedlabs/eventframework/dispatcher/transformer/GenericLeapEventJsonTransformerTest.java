package com.attunedlabs.eventframework.dispatcher.transformer;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.event.LeapEvent;

public class GenericLeapEventJsonTransformerTest {

	final Logger log = LoggerFactory.getLogger(GenericLeapEventJsonTransformerTest.class);

	/**
	 * this method used for transform event.
	 * 
	 * @throws EventFrameworkConfigurationException
	 * @throws LeapEventTransformationException
	 */
	@Test
	public void testTransformEvent() throws EventFrameworkConfigurationException, LeapEventTransformationException {
		RequestContext requestContext = ContextData.getRequestContext();

		LeapEvent leapEvent = new LeapEvent("demoData", requestContext);
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		GenericLeapEventJsonTransformer eventJsonTransformer = new GenericLeapEventJsonTransformer();
		Serializable result = eventJsonTransformer.transformEvent(leapEvent);
		Assert.assertNotNull("Result should not be null", result);
		Assert.assertEquals("result shuld be instance of String ", String.class, result.getClass());
	}
}
