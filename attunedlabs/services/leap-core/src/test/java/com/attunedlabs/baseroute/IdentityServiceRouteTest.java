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

public class IdentityServiceRouteTest extends BaseRouteTestUtil {

	/**
	 * This route is used for Identity service with isAccessTokenValidation false
	 * and isTenantTokenValidation false
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIdentityService() throws Exception {
		RouteDefinition route = context.getRouteDefinition(IDENTITY_SERVICE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(ACCESS_TOKEN_VALIDATION, constant(FALSE)).setHeader(TENANT_TOKEN_VALIDATION_ROUTE, constant(FALSE));
				weaveAddLast().to(MOCK_FINISH);

			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(IDENTITY_SERVICE), null, setHeader());
		assertMockEndpointsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertFalse("Header Value should be true  ::", Boolean.valueOf(headers.get(ACCESS_TOKEN_VALIDATION).toString()));
		Assert.assertFalse("Header Value should be true  ::", Boolean.valueOf(headers.get(TENANT_TOKEN_VALIDATION_ROUTE).toString()));
	}
}
