package com.attunedlabs.leap.randomuuid;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;

public class RandomStringUUIDTest {

	private RandomStringUUID randomStringUUID;
	private Exchange exchange;
	private Message message;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() {
		if (randomStringUUID == null)
			randomStringUUID = new RandomStringUUID();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		message = exchange.getIn();

	}

	/**
	 * This method is to generate random uuid for request without set requestUUID in serviceDataContext
	 */
	@Test
	public void testUuidgenrateWithoutSetUUIDRequest() {
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		Assert.assertNull("service request UUID should be null ::", requestUUID);

		randomStringUUID.uuidgenrate(exchange);

		requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		Assert.assertNotNull("service request UUID should not be null ::", requestUUID);
	}
	
	/**
	 * This method is to generate random uuid for request with set the requestUUID in serviceDataContext
	 */

	@Test
	public void testUuidgenrateWithSetRequestUUID() {
		leapDataCtx.getServiceDataContext().setRequestUUID("123456789");
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);

		String requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		Assert.assertNotNull("service request UUID should be null ::", requestUUID);
		Assert.assertEquals("123456789", requestUUID);

		randomStringUUID.uuidgenrate(exchange);

		requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		Assert.assertNotNull("service request UUID should not be null ::", requestUUID);
		Assert.assertEquals("123456789", requestUUID);
	}

}
