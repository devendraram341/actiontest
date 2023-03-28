package com.attunedlabs.eventframework.dispatcher.transformer;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

import com.attunedlabs.ContextData;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.event.LeapEvent;

public class LeapEventXmlTransformerTest {

	private String xslName = "Transform.xml";
	private String xslAsString = "{\"metadata\":{\"implementationName\":\"TestImpl\",\"featureName\":\"TestFeature\",\"featureGroup\":\"TestFeatureGroup\"},\"id\":\"PRINT_SERVICE_XSLT\",\"object\":{\"metaData\":{\"data\":\"\",\"contentType\":\"\"},\"processMetaData\":{},\"id\":\"\",\"content\":{\"contentType\":\"\"},\"objectType\":\"\"}}";

	/**
	 * this method used for xml transform according to XSLT.
	 * 
	 * @throws LeapEventTransformationException
	 */
	@Test
	public void testTransformEvent() throws LeapEventTransformationException {
		RequestContext requestContext = ContextData.getRequestContext();

		LeapEvent leapEvent = new LeapEvent("demoData", requestContext);
		Assert.assertNotNull("Leap Event Value Should Not be null", leapEvent);
		Assert.assertTrue("leap event value shold not be empty ::", !leapEvent.toString().isEmpty());

		LeapEventXmlTransformer eventXmlTransformer = new LeapEventXmlTransformer(xslName, xslAsString);
		Serializable result = eventXmlTransformer.transformEvent(leapEvent);
		Assert.assertNotNull("Result should not be null", result);
		Assert.assertEquals("result shuld be instance of String ", String.class, result.getClass());

	}

}
