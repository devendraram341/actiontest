package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Test;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

public class SubscriberExecutionRouteTest extends BaseRouteTestUtil {

	/**
	 * This route will apply transaction for subscriber
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSubscriberExecutionRoute() throws Exception {
		RouteDefinition route = context.getRouteDefinition(SUBSCRIPTION_EXECUTION);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(SUBSCRIPTION_EXECUTION), DEMO_DATA,
				setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		assertMockEndpointsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull(message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", "true", headers.get(IS_SERVICE_COMPLETED));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchange body should not be null ::", body);
		Assert.assertEquals("exchange body data should be same as expected data ::", DEMO_DATA, body);
	}

}
