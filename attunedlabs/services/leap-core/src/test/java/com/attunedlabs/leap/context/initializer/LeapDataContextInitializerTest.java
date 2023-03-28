package com.attunedlabs.leap.context.initializer;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.configdbtest.FeatureDeploymentTestConfigDB;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapJSONResultSet;
import com.attunedlabs.leap.context.bean.LeapResultSet;

@SuppressWarnings("static-access")
public class LeapDataContextInitializerTest {

	private LeapDataContextInitializer initializer;
	private Exchange exchange;
	private FeatureDeploymentTestConfigDB configDB;
	private LeapDataContext leapDataCtx;

	Logger logger = LoggerFactory.getLogger(LeapDataContextInitializerTest.class);

	@Before
	public void setUp() {
		if (initializer == null)
			initializer = new LeapDataContextInitializer();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (configDB == null)
			configDB = new FeatureDeploymentTestConfigDB();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		configDB.addFeatureDeployement();
	}

	/**
	 * This method is use to create the leapDataContext object if not exist
	 * LeapDataContext with running service context
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndInitializeLeapContextWithoutSetHeaderLDC() throws Exception {
		Message message = exchange.getIn();
		message.setHeaders(new BaseRouteTestUtil().setHeader());
		initializer.createAndInitializeLeapContext(exchange);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertEquals("Exchange Header feature Value should be same as TestFeature ::", TEST_FEATURE,
				headers.get(FEATURE));
		Assert.assertTrue("In exchange header should be contain FeatureDeployment key ::",
				headers.containsKey("FeatureDeployment"));
		Assert.assertTrue("In exchange header should be contain timeZone key ::", headers.containsKey("timeZone"));
		
	}

	/**
	 * This method is use to create the leapDataContext object if already existing
	 * then re-initialize the with running service context
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndInitializeLeapContextWithSetHeaderLDC() throws Exception {
		LeapCoreTestUtils.setHeaders(leapDataCtx, exchange);
		initializer.createAndInitializeLeapContext(exchange);

		Message message = exchange.getIn();
		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertEquals("Exchange Header feature Value should be same as TestFeature ::", TEST_FEATURE,
				headers.get(FEATURE));
		Assert.assertTrue("In exchange header should be contain FeatureDeployment key ::",
				headers.containsKey("FeatureDeployment"));
		Assert.assertFalse("In exchange header should not be contain timeZone key ::", headers.containsKey("timeZone"));
	}

	/**
	 * This method is use to remove the runtime service context
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoveRuntimeContext() throws Exception {
		LeapCoreTestUtils.setHeaders(leapDataCtx, exchange);
		int size = leapDataCtx.getServiceDataContext().getLeapServiceRuntimeContextList().size();
		Assert.assertTrue("at least one service should be running :: ", size > 0);
		String serviceName = leapDataCtx.getServiceDataContext().getLeapServiceRuntimeContextList().get(0)
				.getServiceName();
		Assert.assertEquals("Service Name Should be match TestService ::", TEST_SERVICE, serviceName);
		
		int totalService=leapDataCtx.getServiceDataContext().getLeapServiceRuntimeContextList().size();

		initializer.removeRuntimeContext(exchange);

		int afterSize = leapDataCtx.getServiceDataContext().getLeapServiceRuntimeContextList().size();
		Assert.assertEquals("After Removing service should be 0 ::", totalService-1, afterSize);
	}

	/**
	 * Method to construct LeapData Object for Request Data
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructLeapDataJsonForRequestWithData() throws Exception {

		LeapResultSet leapResultSet = new LeapJSONResultSet();
		leapResultSet.setData(new JSONObject(DEMO_JSON_DATA));

		JSONObject requestLeapData = initializer.constructLeapDataJsonForRequest(setLeapDataElement(leapResultSet),
				exchange);
		Assert.assertNotNull("Request Leap Data Should not be null ::", requestLeapData);
		Assert.assertTrue(requestLeapData.has("element"));
		Assert.assertEquals(DEMO_JSON_DATA, requestLeapData.get("element").toString());
	}

	/**
	 * Method to construct LeapData Object for Request Data with test with null
	 * data.
	 * 
	 * @throws Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructLeapDataJsonForRequestWithOutData() throws Exception {

		LeapResultSet leapResultSet = new LeapJSONResultSet();
		leapResultSet.setData(null);

		initializer.constructLeapDataJsonForRequest(setLeapDataElement(leapResultSet), exchange);
	}

	/**
	 * Method to construct LeapData Object for Response Data with json object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructLeapDataJsonForResponseWithJSONObjectBody() throws Exception {
		LeapCoreTestUtils.setHeaders(leapDataCtx, exchange);
		exchange.getIn().setBody(new JSONObject(DEMO_JSON_DATA));

		JSONObject responseLeapData = initializer.constructLeapDataJsonForResponse(exchange);
		Assert.assertNotNull("Response Leap Data should not be null ::", responseLeapData);
		Assert.assertTrue("response leap Data Should be contain exchange body Data ::",
				responseLeapData.toString().contains(DEMO_JSON_DATA));
	}

	/**
	 * Method to construct LeapData Object for Response Data with json Array
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructLeapDataJsonForResponseWithJSONArrayBody() throws Exception {
		LeapCoreTestUtils.setHeaders(leapDataCtx, exchange);
		JSONObject jsonObject = new JSONObject(DEMO_JSON_DATA);
		exchange.getIn().setBody(new JSONArray().put(jsonObject));

		JSONObject responseLeapData = initializer.constructLeapDataJsonForResponse(exchange);
		Assert.assertNotNull("Response Leap Data should not be null ::", responseLeapData);
		Assert.assertTrue("response leap Data Should be contain exchange body Data ::",
				responseLeapData.toString().contains(DEMO_JSON_DATA));
	}

	/**
	 * Method to construct LeapData Object for Response Data with String
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructLeapDataJsonForResponseWithStringBody() throws Exception {
		LeapCoreTestUtils.setHeaders(leapDataCtx, exchange);

		exchange.getIn().setBody(DEMO_DATA);

		JSONObject responseLeapData = initializer.constructLeapDataJsonForResponse(exchange);
		Assert.assertNotNull("Response Leap Data should not be null ::", responseLeapData);
		Assert.assertTrue("response leap Data Should be contain exchange body Data ::",
				responseLeapData.toString().contains(DEMO_DATA));

	}

	@After
	public void cleanUp() {
		configDB.deleteFeatureDeployement();
	}

	private LeapDataElement setLeapDataElement(LeapResultSet leapResultSet) {
		LeapData leapData = new LeapData();
		leapData.setItems(leapResultSet);

		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setApiVersion("1.0");
		initialRequestHeaderElement.setContext("contextString");
		initialRequestHeaderElement.setLang("EN");
		initialRequestHeaderElement.setData(leapData);

		return initialRequestHeaderElement;
	}
}
