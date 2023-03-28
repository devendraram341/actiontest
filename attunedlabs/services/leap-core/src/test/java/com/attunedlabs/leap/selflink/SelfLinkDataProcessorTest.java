package com.attunedlabs.leap.selflink;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import static com.attunedlabs.LeapCoreTestConstant.*;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.selflink.exception.SelfLinkException;

public class SelfLinkDataProcessorTest {

	private SelfLinkDataProcessor dataProcessor;
	private Exchange exchange;
	private Message message;
	private LeapDataContext leapDataCtx;

	@Before
	public void setUp() {
		if (dataProcessor == null)
			dataProcessor = new SelfLinkDataProcessor();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		message = exchange.getIn();
	}

	@Test
	public void testProcessWithPrivateHeaderSuccess() throws Exception {
		LeapCoreTestUtils.setLeapPrivateHeader(leapDataCtx);
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setHeader(CAMEL_RESTLET_RESPONSE, setResponse());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertFalse("Exchange Header Should not be contain SelfLink Key ::", headers.containsKey("selfLink"));

		dataProcessor.process(exchange);

		headers = message.getHeaders();
		Assert.assertTrue("After Execute Method then exchnage header should be cointain selflink key ::",
				headers.containsKey("selfLink"));
		JSONObject jsonObject = (JSONObject) headers.get("selfLink");

		Assert.assertNotNull("selfLink should be jsonObject Data ::", jsonObject);
		Assert.assertTrue("jsonObjetct should be contain selfLink Key ::", jsonObject.has("selfLink"));
		Assert.assertEquals("selfLink key data should be same as expected data ::", getSelfLinkData(),
				jsonObject.get("selfLink"));
		Assert.assertEquals("all", jsonObject.getString("userId"));
	}

	@Test(expected = SelfLinkException.class)
	public void testProcessWithoutPrivateHeaderFail() throws Exception {
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataCtx);
		message.setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		message.setHeader(CAMEL_RESTLET_RESPONSE, setResponse());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertFalse("Exchange Header Should not be contain SelfLink Key ::", headers.containsKey("selfLink"));

		dataProcessor.process(exchange);
	}

	private Response setResponse() {
		Request request = new Request();
		Status status = new Status(200);
		Response response = new Response(request);
		response.setStatus(status);
		return response;
	}

	private String getSelfLinkData() {
		String requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		return "/" + TEST_SERVICE + "/" + requestUUID;
	}

}
