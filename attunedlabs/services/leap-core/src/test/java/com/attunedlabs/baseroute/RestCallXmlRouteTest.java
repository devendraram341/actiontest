package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;

public class RestCallXmlRouteTest extends BaseRouteTestUtil {

	/**
	 * This route takes xml based request data
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRestCallXmlRouteWithXMLData() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_CALL_XML);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_CALL_XML), DEMO_XML_DATA, setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
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

		Assert.assertEquals("Expeceted And actual data should be same ::", DEMO_XML_DATA, message.getBody());
	}

	/**
	 * This route takes xml based request data but testing JSON Data
	 * 
	 * @throws Exception
	 */
	@Test(expected = JSONException.class)
	public void testRestCallXmlRouteWithJSonData() throws Exception {
		RouteDefinition route = context.getRouteDefinition(REST_CALL_XML);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(REST_CALL_XML), DEMO_JSON_DATA, setHeader());

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
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

		Assert.assertEquals("Expeceted And actual data should be same ::", "", message.getBody());

		new JSONObject(message.getBody().toString());
	}
}
