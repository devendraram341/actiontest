package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.leap.LeapDataContext;

public class LeapEventDispatcherServiceRouteTest extends BaseRouteTestUtil {

	private LeapDataContext leapDataContext;
	private FeatureDeploymentTestConfigDB configDB;

	@Before
	public void init() {
		leapDataContext = new LeapDataContext();
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataContext);
		if (configDB == null)
			configDB = new FeatureDeploymentTestConfigDB();
		configDB.addFeatureDeployement();

	}

	/**
	 * This route is used for dispatching events without service completed header.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapEventDispatcherServiceRouteWithIsServiceCompletedNotSet() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_EVENT_DISPATCHER_SERVICE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_EVENT_DISPATCHER_SERVICE),
				DEMO_DATA, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Message Data should not be null ::", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Header Should not be null ::", headers);
		Assert.assertEquals(TEST_SITE, headers.get(SITEID));

		String body = message.getBody(String.class);
		Assert.assertNotNull("Body data Should not be null ::", body);
		Assert.assertEquals(DEMO_DATA, body);
	}

	/**
	 * This route is used for dispatching events with service completed header is
	 * true.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapEventDispatcherServiceRouteWithIsServiceCompletedSetTrue() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_EVENT_DISPATCHER_SERVICE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(IS_SERVICE_COMPLETED, constant(TRUE)).setHeader(LEAP_DATA_CONTEXT,
						constant((Object) leapDataContext));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_EVENT_DISPATCHER_SERVICE), null,
				setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertTrue(Boolean.valueOf(headers.get(IS_SERVICE_COMPLETED).toString()));

	}

	/**
	 * This route is used for dispatching events with service completed header is
	 * false .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeapEventDispatcherServiceRouteWithIsServiceCompletedSetFalse() throws Exception {
		RouteDefinition route = context.getRouteDefinition(LEAP_EVENT_DISPATCHER_SERVICE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(IS_SERVICE_COMPLETED, constant(FALSE)).setHeader(LEAP_DATA_CONTEXT,
						constant((Object) leapDataContext));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(LEAP_EVENT_DISPATCHER_SERVICE), null,
				setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange header should not be null ::", headers);
		Assert.assertFalse(Boolean.valueOf(headers.get(IS_SERVICE_COMPLETED).toString()));
	}

	@After
	public void cleanUp() {
		configDB.deleteFeatureDeployement();
	}

}
