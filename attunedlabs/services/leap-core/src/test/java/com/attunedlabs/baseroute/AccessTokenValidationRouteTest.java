package com.attunedlabs.baseroute;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Cookie;
import org.restlet.util.Series;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class AccessTokenValidationRouteTest extends BaseRouteTestUtil {
	/**
	 * This route is used for Identity service with isAccessTokenValidation true and
	 * isTenantTokenValidation false and valid access Token
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIdentityServiceWithAccessTokenTrue() throws Exception {
		RouteDefinition route = context.getRouteDefinition(ACCESS_TOKEN_VALIDATION_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(ACCESS_TOKEN_VALIDATION, constant(TRUE))
						.setHeader(TENANT_TOKEN_VALIDATION_ROUTE, constant(FALSE))
						.setHeader(CAMEL_RESTLET_REQUEST, constant(setAccessToken()));
				weaveAddLast().to(MOCK_FINISH);

			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(ACCESS_TOKEN_VALIDATION_ROUTE), null,
				setHeader());
		assertMockEndpointsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNull("Exchange Mesaage Body Shoud be null ::", message.getBody());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertTrue("Header Value should be true  ::",
				Boolean.valueOf(headers.get(ACCESS_TOKEN_VALIDATION).toString()));
		Assert.assertFalse("Header Value should be true  ::",
				Boolean.valueOf(headers.get(TENANT_TOKEN_VALIDATION_ROUTE).toString()));
		Assert.assertEquals(ACCESS_TOKEN_DATA, headers.get(ACCESS_TOKEN));
	}

	/**
	 * This route is used for Identity service with isAccessTokenValidation true and
	 * isTenantTokenValidation false and invalid access Token
	 * 
	 * @throws Exception
	 */
	@Test(expected = CamelExecutionException.class)
	public void testIdentityServiceWithAccessTokenTrueAndInvalidToken() throws Exception {
		RouteDefinition route = context.getRouteDefinition(ACCESS_TOKEN_VALIDATION_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(ACCESS_TOKEN_VALIDATION, constant(TRUE))
						.setHeader(TENANT_TOKEN_VALIDATION_ROUTE, constant(FALSE))
						.setHeader(CAMEL_RESTLET_REQUEST, constant(setInvalidAccessToken()));
				weaveAddLast().to(MOCK_FINISH);

			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(ACCESS_TOKEN_VALIDATION_ROUTE), null,
				setHeader());
		assertMockEndpointsSatisfied();
	}

	
	private Request setAccessToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN,
				"eyJkZW1vIjoiVGVzdERlbW8iLCJhbGciOiJSUzI1NiJ9.eyJhdXRoRGF0YSI6eyJhY2Nlc3NfdG9rZW4iOiJUZXN0aW5nVG9rZW4iLCJhdXRoUHJvdmlkZXIiOiJwaW5nIn19.Xz/PhzM/V3DRZgj2etiNGQ==");
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private Request setInvalidAccessToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN,
				"eyJkZW1vIjoiVGVz1NiJ9.eyJhdXRoRGF0YSI6eyJhY2Nlc3NfdG9rZW4iOiJUZXN0aW5nVG.Xz/PhztiNGQ==");
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}
}
