package com.attunedlabs.baseroute;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Test;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class BaseEntryRouteTest extends BaseRouteTestUtil {

	/**
	 * This Route use for every rest request apply transaction for the service
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestCallFromBaseEntry() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_CALL_JSON);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		template.sendBody(PropertiesForTesting.getInstance().getValue(BASE_ROUTE), null);

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		assertMockEndpointsSatisfied();

		List<Exchange> receivedExchanges = mockEndpoint.getReceivedExchanges();
		Assert.assertTrue("Receive Exchange Value Should be GreaterThen 0 ::", receivedExchanges.size() > 0);

		Exchange exchange = receivedExchanges.get(0);
		Assert.assertNotNull("Exchange Should not be null ::", exchange);

		Object header = exchange.getIn().getHeader(IS_SERVICE_COMPLETED);
		Assert.assertNotNull("Header Value Should not be null ::", header);
		Assert.assertTrue("Header Value should be true ::", Boolean.valueOf(header.toString()));
	}
}
