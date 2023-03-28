package com.attunedlabs.baseroute;

import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.List;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class RestCallJsonRouteTest extends BaseRouteTestUtil {

	/**
	 * This route takes json request data
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestCallJsonWithoutToDirectUriWithJson() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_CALL_JSON);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_CALL_JSON), DEMO_JSON_DATA, setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		assertMockEndpointsSatisfied();

		List<Exchange> receivedExchanges = mockEndpoint.getReceivedExchanges();
		Assert.assertTrue("Receive Exchange Value Should be GreaterThen 0 ::", receivedExchanges.size() > 0);

		Exchange exchange = receivedExchanges.get(0);
		Assert.assertNotNull("Exchange Should not be null ::", exchange);

		Object body = exchange.getIn().getBody();
		Assert.assertNotNull("Body Should not be null ::", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_JSON_DATA, body.toString());

		JSONObject jsonObject = new JSONObject(body.toString());
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_JSON_DATA,
				jsonObject.toString());

		Object header = exchange.getIn().getHeader(IS_SERVICE_COMPLETED);
		Assert.assertNotNull("Header Value Should not be null ::", header);
		Assert.assertTrue("Header Value should be true ::", Boolean.valueOf(header.toString()));
	}

	/**
	 * This route test xml request data but getting exception
	 * 
	 * @throws Exception
	 */
	@Test(expected = JSONException.class)
	public void testRestCallJsonWithoutToDirectUriWithXMLData() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_CALL_JSON);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_CALL_JSON), DEMO_XML_DATA, setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		assertMockEndpointsSatisfied();

		List<Exchange> receivedExchanges = mockEndpoint.getReceivedExchanges();
		Assert.assertTrue("Receive Exchange Value Should be GreaterThen 0 ::", receivedExchanges.size() > 0);

		Exchange exchange = receivedExchanges.get(0);
		Assert.assertNotNull("Exchange Should not be null ::", exchange);

		Object body = exchange.getIn().getBody();
		Assert.assertNotNull("Body Should not be null ::", body);
		Assert.assertEquals("Exchange body data should be same as Expected Data :", DEMO_XML_DATA, body.toString());

		JSONObject jsonObject = new JSONObject(body.toString());
		Assert.assertNotEquals("Exchange body data should be not same as Expected Data :", DEMO_XML_DATA,
				jsonObject.toString());

		Object header = exchange.getIn().getHeader(IS_SERVICE_COMPLETED);
		Assert.assertNotNull("Header Value Should not be null ::", header);
		Assert.assertTrue("Header Value should be true ::", Boolean.valueOf(header.toString()));
	}
}
