package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.leap.LeapDataContext;

public class JMSRequestResponseQueueTest extends BaseRouteTestUtil {

	private LeapDataContext leapDataContext;

	@Before
	public void init() {
		leapDataContext = new LeapDataContext();
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataContext);
	}

	/**
	 * This route check JMS Request and response queue.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJMSRequestResponseQueue() throws Exception {
		RouteDefinition route = context.getRouteDefinition(JMS_REQUEST_RESPONSE_QUEUE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(LEAP_DATA_CONTEXT, constant(leapDataContext));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(JMS_REQUEST_RESPONSE_QUEUE), DEMO_DATA,
				setHeader());

		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange Message Should not be null: ", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertEquals("content type should be application/json", CONTENT_TYPE_JSON, headers.get(CONTENT_TYPE));
		Assert.assertEquals("siteId should be same as 'all' ::", TEST_SITE, headers.get(SITEID));

		Object body = message.getBody();
		Assert.assertNotNull("Exchange Body should not be null ::", body);
		Assert.assertEquals("body should be jsonObeject Form ::", JSONObject.class, body.getClass());
	}
}
