package com.attunedlabs.baseroute;

import org.apache.camel.CamelExecutionException;
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

public class LeapGetPostRouteTest extends BaseRouteTestUtil {

	/**
	 * This route support POST request of leap request of rest without body
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapGetPostRouteWithPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_LEAP_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();

		Message message = endpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("exchange message should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchnage Body Should not be null :", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_LEAP_DATA, body);
	}

	/**
	 * This route support POST request of leap request of rest with json body
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapGetPostRouteWithJsonAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_JSON_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();

		Message message = endpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("exchange message should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchnage Body Should not be null :", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_JSON_DATA, body);
	}

	/**
	 * This route support POST request of leap request of rest with xml body
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapGetPostRouteWithXMLAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_XML_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();

		Message message = endpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("exchange message should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchnage Body Should not be null :", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_XML_DATA, body);
	}

	/**
	 * This route support POST request of leap request of rest with string
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapGetPostRouteWithStringAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();

		Message message = endpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("exchange message should not be null :", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchnage Body Should not be null :", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_DATA, body);
	}

	/**
	 * This route support GET request of leap request of rest
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapGetPostRouteWithGet() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_GET)).setHeader(CAMEL_HTTP_QUERT,
						constant(DEMO_CAMEL_HTTP_QUERY_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();

		Message message = endpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Header should not be null :", headers);
		Assert.assertEquals("camelHttpMethod Data should be get ::", HTTP_GET, headers.get(CAMEL_HTTP_METHOD));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Body data should not be null ::", body);
		Assert.assertEquals("exchange body data should be same as expected data ::", DEMO_REST_GET_DATA, body);
	}

	/**
	 * This route support GET/POST request of leap request but testing PUT request.
	 * 
	 * @throws Exception
	 */
	@Test(expected = CamelExecutionException.class)
	public void testLeapGetPostRouteWithPut() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_PUT));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint endpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		endpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();
	}
}
