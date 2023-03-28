package com.attunedlabs.baseroute;

import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

public class SubscriberTransactionRouteTest extends BaseRouteTestUtil {

	private LeapDataContext leapDataContext;
	private FeatureDeploymentTestConfigDB configDB;

	/**
	 * before initialization for testing
	 */
	@Before
	public void init() {
		leapDataContext = new LeapDataContext();
		leapDataContext.getServiceDataContext(TEST_TENANT, TEST_SITE, TEST_FEATUREGROUP, TEST_FEATURE);
		setTenantAndSite();
		LeapCoreTestUtils.addContextElementWithLeapTag(leapDataContext);
		configDB = new FeatureDeploymentTestConfigDB();
		configDB.addFeatureDeployement();
	}

	/**
	 * This route will apply transaction for subscriber
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSubscriberTransactionRoute() throws Exception {
		RouteDefinition route = context.getRouteDefinition(SUBSCRIBER_TRANSACTION);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveByToUri(ALL_DIRECT_URI).remove();
				weaveAddFirst().setHeader(LEAP_DATA_CONTEXT, constant(leapDataContext));
				weaveAddLast().to(MOCK_FINISH);
			}
		});

		MockEndpoint mockEndpoint = context.getEndpoint(MOCK_FINISH, MockEndpoint.class);
		mockEndpoint.expectedMessageCount(1);

		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(SUBSCRIBER_TRANSACTION), null, setHeader());
		mockEndpoint.assertIsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertEquals("Exchange header Access Control Allow Header should be * ::", "*",
				headers.get("Access-Control-Allow-Headers"));
		Assert.assertEquals("Exchange header Access Control Allow Method should be POST ::", HTTP_POST,
				headers.get("Access-Control-Allow-Methods"));
		Assert.assertTrue("Exchange Header should be contain FeatureDeployment data ::",
				headers.containsKey("FeatureDeployment"));

		Assert.assertNull("In this test case body should be null :", message.getBody());

	}

	/**
	 * cleanUp data after Testing
	 */
	@After
	public void cleanUp() {
		configDB.deleteFeatureDeployement();
	}

	private void setTenantAndSite() {
		LeapHeaderConstant.tenant = TEST_TENANT;
		LeapHeaderConstant.site = TEST_SITE;
	}
}
