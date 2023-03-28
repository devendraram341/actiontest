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

public class LeapResourceRetentionRouteTest extends BaseRouteTestUtil {

	/**
	 * This route is used for close all the resources that are created during the
	 * service execution
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapResourceRetentionRoute() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_RESOURCE_RETENTION);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_RESOURCE_RETENTION), null, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message Should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("feature Should be same as POST ::", TEST_FEATURE, headers.get(FEATURE));

	}
}
