package com.attunedlabs.leap.corsheaders;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestUtils;

public class CORSHeadersInitializerTest {

	private Exchange exchange = LeapCoreTestUtils.createExchange();
	private CORSHeadersInitializer corsHeadersInitializer = new CORSHeadersInitializer();

	/**
	 * this method use for set OriginHeader with originHeader.
	 */
	@Test
	public void testSetCorsHeadersWithOrigin() {
		exchange.getIn().setHeader("Origin", "TestData");
		Map<String, Object> headers = exchange.getIn().getHeaders();
		Assert.assertNotNull("Header Should not be null :: ", headers);
		Assert.assertNull("Exchange header should not have data of OriginHeader ::", headers.get("originHeader"));

		corsHeadersInitializer.setCorsHeaders(exchange);

		headers = exchange.getIn().getHeaders();
		Assert.assertTrue("Exchange header should be contain data of OriginHeader ::",
				headers.containsKey("originHeader"));
		Assert.assertEquals("Exchange header should be same as TestData", "TestData", headers.get("originHeader"));
	}

	/**
	 * this method use for set OriginHeader without originHeader.
	 */
	@Test
	public void testSetCorsHeadersWithoutOrigin() {
		Map<String, Object> headers = exchange.getIn().getHeaders();

		Assert.assertNotNull("Header Should not be null :: ", headers);
		Assert.assertNull("Exchange header should not have data of OriginHeader ::", headers.get("originHeader"));

		corsHeadersInitializer.setCorsHeaders(exchange);

		headers = exchange.getIn().getHeaders();
		Assert.assertTrue("Exchange Header should have originHeader key ::", headers.containsKey("originHeader"));
		Assert.assertNull("Exchange header should not have data of OriginHeader ::", headers.get("originHeader"));
	}

}
