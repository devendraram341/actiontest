package com.attunedlabs.baseroute;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class RestGetPostRouteTest extends BaseRouteTestUtil {

	/**
	 * This route support POST request of rest with JSON Data.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestGetPostRouteWithJsonDataAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_JSON_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_GET_POST), null, setHeader());

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNotNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));
		Assert.assertEquals("feature group Should be same as POST ::", TEST_FEATUREGROUP, headers.get(FEATUREGROUP));

		Assert.assertEquals("Expeceted And actual data should be same ::", DEMO_JSON_DATA, message.getBody());

		JSONObject jsonObject = new JSONObject(DEMO_JSON_DATA);
		String next = jsonObject.keys().next();
		Assert.assertNotNull("root key should not be null ::", next);
		Assert.assertEquals("Expected and Actual Data should be same ::", "test", next);

	}

	/**
	 * This route support POST request of rest With XML Data.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestGetPostRouteWithXmlDataAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_XML_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_GET_POST), null, setHeader());

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNotNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHtppMethod Should be same as POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));
		Assert.assertEquals("Expeceted And actual Value Should be Same ::", TEST_FEATUREGROUP,
				headers.get(FEATUREGROUP));

		Assert.assertEquals("Expeceted And actual data should be same ::", DEMO_XML_DATA, message.getBody());
		Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(DEMO_XML_DATA)));
		String nodeName = parse.getFirstChild().getNodeName();

		Assert.assertNotNull("node name should not be null ::", nodeName);
		Assert.assertEquals("Expected and Actual Data should be same ::", "Demo", nodeName);
	}

	/**
	 * This route support POST request of rest with String Data
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestGetPostRouteWithStringDataAndPost() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST)).setBody(constant(DEMO_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_GET_POST), null, setHeader());

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNotNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("LeapEndpointHttpMethod Should be same as POST ::", HTTP_POST.toLowerCase(),
				headers.get("LeapEndPointHttpMethod").toString().toLowerCase());

		String body = message.getBody(String.class);
		Assert.assertNotNull("Exchange body Should not be null ::", body);
		Assert.assertEquals("Exchange body data should be same as Expected data ::", DEMO_DATA, body);
	}

	/**
	 * This route support GET request of rest
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestGetPostRouteWithGet() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_GET_POST);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(CAMEL_HTTP_METHOD, constant(HTTP_GET)).setHeader(CAMEL_HTTP_QUERT,
						constant(DEMO_CAMEL_HTTP_QUERY_DATA));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_GET_POST), null, setHeader());

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNotNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange Header should not be null :", headers);
		Assert.assertEquals("camelHttpMethod Should be same as POST ::", HTTP_GET, headers.get(CAMEL_HTTP_METHOD));
		Assert.assertEquals("Expeceted And actual Value Should be Same ::", TEST_FEATUREGROUP,
				headers.get(FEATUREGROUP));

		Assert.assertEquals("Expeceted And actual data should be same ::", DEMO_REST_GET_DATA, message.getBody());

		JSONObject jsonObject = new JSONObject(DEMO_REST_GET_DATA);
		String next = jsonObject.keys().next();
		Assert.assertNotNull("root key should not be null ::", next);
		Assert.assertEquals("Expected and Actual Data should be same ::", "data", next);
	}

	/**
	 * This route support GET/POST request of rest but testing with PUT http request
	 * 
	 * @throws Exception
	 */
	@Test(expected = CamelExecutionException.class)
	public void testRestGetPostWithPut() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_GET_POST);
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

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_GET_POST), null, setHeader());
		endpoint.assertIsSatisfied();
	}
}
