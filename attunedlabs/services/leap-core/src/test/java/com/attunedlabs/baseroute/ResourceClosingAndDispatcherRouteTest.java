package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class ResourceClosingAndDispatcherRouteTest extends BaseRouteTestUtil {

	/**
	 * This route is used for close all the resources and dispatching events
	 * 
	 * @throws Exception
	 */
	@Test
	public void testResourceClosingAndDispatcherRoute() throws Exception {
		RouteDefinition route = context.getRouteDefinition(RESOURCE_CLOSING_AND_DISPATCHER);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_SEDA_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(RESOURCE_CLOSING_AND_DISPATCHER), DEMO_DATA,
				setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message Should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("feature Should be same as POST ::", TEST_FEATURE, headers.get(FEATURE));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchnage Body Should not be null :", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_DATA, body);
	}
}
