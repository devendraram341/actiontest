package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDynamicDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.leap.LeapDataContext;

public class EntryRouteTest extends BaseRouteTestUtil {

	private LeapDataContext leapDataContext;

	@Before
	public void init() {
		leapDataContext = new LeapDataContext();
		LeapCoreTestUtils.addContextElementForEntryRoute(leapDataContext);
	}

	/**
	 * This is the first route to be called and generate unique request id,load bean
	 * which decide which execution route to call and call other routes without
	 * contentType
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntryRouteWithoutLeapContentType() throws Exception {
		RouteDefinition route = context.getRouteDefinition(ENTRY_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(LEAP_DATA_CONTEXT, constant((Object) leapDataContext));
				weaveByType(ToDynamicDefinition.class).remove();
				weaveByToUri(PERMSTORE_CONFIG_BEAN).remove();
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(ENTRY_ROUTE), DEMO_DATA, setHeader());
		mockEndpoint.assertIsSatisfied();
		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Message should not be null ::", message);

		Object header = message.getHeader("exeroute");
		Assert.assertNotNull("Header should not be null ::", header);
		Assert.assertEquals("Expected and Actual Data Should be same ::", getExeRoute(), header.toString());

		String body = message.getBody(String.class);
		Assert.assertNotNull("body data should not be null ::", body);
		Assert.assertEquals("Body data must be same as expected data ::", DEMO_DATA, body);
	}

	/**
	 * This is the first route to be called and generate unique request id,load bean
	 * which decide which execution route to call and call other routes with content
	 * Type
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntryRouteWithContentTypeLeapAndLeapJson() throws Exception {
		RouteDefinition route = context.getRouteDefinition(ENTRY_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(LEAP_DATA_CONTEXT, constant((Object) leapDataContext))
						.setHeader("Content-Type", constant("application/vnd.leap+json"))
						.setHeader(CAMEL_HTTP_METHOD, constant(HTTP_POST));
				weaveByType(ToDynamicDefinition.class).remove();
				weaveByToUri(PERMSTORE_CONFIG_BEAN).remove();
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(ENTRY_ROUTE), null, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchage message should not be null ::", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Header data should not be null ::", headers);
		Assert.assertEquals("camelHttpMethod should be POST ::", HTTP_POST, headers.get(CAMEL_HTTP_METHOD));
		Assert.assertEquals("feature group data should be same as TestFetureGroup ::", TEST_FEATUREGROUP,
				headers.get(FEATURE_GROUP));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Body Data should not be null :", body);
	}

	@Test
	public void testEntryRouteFailed() throws Exception {
		RouteDefinition route = context.getRouteDefinition(ENTRY_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveByType(ToDynamicDefinition.class).remove();
				weaveByToUri(PERMSTORE_CONFIG_BEAN).remove();
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(ENTRY_ROUTE), DEMO_DATA, setHeader());
		mockEndpoint.assertIsNotSatisfied();
	}

	private String getExeRoute() {
		return TEST_FEATURE + "-" + TEST_SERVICE + "-" + "executionEnrichmentRoute";
	}
}
