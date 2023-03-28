package com.attunedlabs.leap.header.initializer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.security.exception.AccountFetchException;

public class FeatureHeaderInitializerTest {

	private FeatureHeaderInitializer featureHeaderInitializer;
	private Exchange exchange;
	private Message message;

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (featureHeaderInitializer == null)
			featureHeaderInitializer = new FeatureHeaderInitializer();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		message = exchange.getIn();
	}

	/**
	 * This method is used to set header for request serviceName,tenantid and data
	 * 
	 * @throws JSONException
	 * @throws JsonParserException
	 * @throws AccountFetchException
	 * @throws FeatureHeaderInitialzerException
	 */
	@Test
	public void testProcess()
			throws JSONException, JsonParserException, AccountFetchException, FeatureHeaderInitialzerException {

		setExchangeWithRequiredHeader();

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);

		LeapHeader leapHeaderBefore = (LeapHeader) headers.get("leapHeader");
		Assert.assertNull("In exchange Header not have LeapHeader and Should be null ::", leapHeaderBefore);

		featureHeaderInitializer.process(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);

		LeapHeader leapHeaderAfter = (LeapHeader) headers.get("leapHeader");
		Assert.assertNotNull("In exchange Header have LeapHeader and Should not be null ::", leapHeaderAfter);
		Assert.assertEquals("Leap Header service name Should be Same as TestService", TEST_SERVICE,
				leapHeaderAfter.getServiceName());
		Assert.assertEquals("Leap Header featureGroup name Should be Same as TestFeatureGroup ::", TEST_FEATUREGROUP,
				leapHeaderAfter.getFeatureGroup());
		Assert.assertEquals("Leap Header feature name Should be Same as TestFeature::", TEST_FEATURE,
				leapHeaderAfter.getFeatureName());
		Assert.assertEquals("Leap Header EndPoint name Should be Same as 'HTTP-JSON' ::", "HTTP-JSON",
				leapHeaderAfter.getEndpointType());
	}

	/**
	 * This method is without set header.
	 * 
	 * @throws JSONException
	 * @throws JsonParserException
	 * @throws AccountFetchException
	 * @throws FeatureHeaderInitialzerException
	 */
	@Test(expected = FeatureHeaderInitialzerException.class)
	public void testProcessWithoutExchangeHeader()
			throws JSONException, JsonParserException, AccountFetchException, FeatureHeaderInitialzerException {
		message.setHeader(SERVICENAME, TEST_SERVICE);
		featureHeaderInitializer.process(exchange);
	}

	private void setExchangeWithRequiredHeader() {
		message.setHeader(FEATUREGROUP, TEST_FEATUREGROUP);
		message.setHeader(FEATURE, TEST_FEATURE);
		message.setHeader(SERVICENAME, TEST_SERVICE);
		message.setHeader(ACCOUNTID, TEST_ACCOUNT);
		message.setHeader(SITEID, TEST_SITE);
	}

}
