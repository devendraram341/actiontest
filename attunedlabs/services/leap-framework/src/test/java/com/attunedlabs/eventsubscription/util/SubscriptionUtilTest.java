package com.attunedlabs.eventsubscription.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.InvokeCamelRoute;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;

public class SubscriptionUtilTest {

	private SubscriptionUtil subscriptionUtil;
//	private Properties properties;
	final Logger log = LoggerFactory.getLogger(SubscriptionUtilTest.class);

	final String json = "{\r\n" + "	\"eventHeader\": {\r\n"
			+ "		\"EVT_CONTEXT\": \"ConfigurationContext=[tenant=TestTenant,site=TestSite,featureGroup=TestFeatureGroup,featureName=TestFeature,implementationName=TestImpl,vendor=TestVendor,version=1.0]\"\r\n"
			+ "	}\r\n" + "}";
	final String retryJson = "{\"retryCount\":\"4\",\"retryInterval\":\"0\",\"retryIntervalMultiplier\":\"1\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}";

	/**
	 * In this method used for Initialization.
	 */
	@Before
	public void init() throws PropertiesConfigException {
		if (subscriptionUtil == null) 
			subscriptionUtil = new SubscriptionUtil();}
		
//		if (properties == null)
//			properties = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
//	}

	@Test
	public void testAddExtraHeadersToEndpoint() {
		Map<String, HashMap<String, String>> actionSpecificMap = new HashMap<>();
		InvokeCamelRoute invokeCamelRoute = new InvokeCamelRoute();
		invokeCamelRoute.setFeatureGroup(GenericTestConstant.TEST_FEATUREGROUP);
		invokeCamelRoute.setFeatureName(GenericTestConstant.TEST_FEATURE);
		invokeCamelRoute.setServiceName(GenericTestConstant.SERVICE_NAME);
		Map<String, HashMap<String, String>> result = subscriptionUtil.addExtraHeadersToEndpoint(actionSpecificMap,
				invokeCamelRoute);
		Assert.assertNotNull("result data should not be null :: ", result);
		Assert.assertEquals("feature od Invoke Camel endpint should be same as 'TestFeature'",
				GenericTestConstant.TEST_FEATURE, result.get("invoke-camel-endpoint").get("feature"));
	}

	@Test
	public void testMapCheck() {
		HashMap<String, String> mapData = new HashMap<>();
		mapData.put("featuregroup", GenericTestConstant.TEST_FEATUREGROUP);
		mapData.put("feature", GenericTestConstant.TEST_FEATURE);
		mapData.put("serviceName", GenericTestConstant.SERVICE_NAME);

		boolean isResult = subscriptionUtil.mapCheck(mapData);
		Assert.assertTrue("mapCheck Should be true :: ", isResult);

		mapData.clear();
		mapData.put("featureGroup", GenericTestConstant.TEST_FEATUREGROUP);
		mapData.put("feature", GenericTestConstant.TEST_FEATURE);
		mapData.put("serviceName", GenericTestConstant.SERVICE_NAME);

		isResult = subscriptionUtil.mapCheck(mapData);
		Assert.assertFalse("MapData Should be false beacuse key value mismatch ::", isResult);
	}

	@Test
	public void testAttributeEmptyCheck() {
		boolean isNullOrNot = subscriptionUtil.attributeEmptyCheck("");
		Assert.assertTrue(isNullOrNot);

		isNullOrNot = subscriptionUtil.attributeEmptyCheck(null);
		Assert.assertTrue(isNullOrNot);

		isNullOrNot = subscriptionUtil.attributeEmptyCheck("demo");
		Assert.assertFalse(isNullOrNot);
	}

	@Test
	public void testEvaluateMVELForCriteriaMatch() {

	}

	@Test
	public void testGetConfigContextFromEventBody() {

	}

	@Test
	public void testJsonToMap() {
		Map<String, Object> jsonTomap = SubscriptionUtil.jsonToMap(getJsonObject());
		Assert.assertNotNull("json to map value should not be null :", jsonTomap);
		Assert.assertEquals("json test key value demo should be same as map key of test value ::", "Demo",
				jsonTomap.get("test"));
	}

	@Test
	public void testToMap() {
		Map<String, Object> tomap = SubscriptionUtil.toMap(getJsonObject());
		Assert.assertNotNull("to map value should not be null :", tomap);
		Assert.assertEquals("json test key value demo should be same as map key of test value ::", "Demo",
				tomap.get("test"));
	}

	@Test
	public void testToList() {
		List<Object> toList = SubscriptionUtil.toList(getJsonArray());
		Assert.assertNotNull("to List value should not be null :", toList);
		Assert.assertTrue(toList.get(0).toString().contains("test"));
	}

	@Test
	public void testConstructKafkaURI() {
		Random randomClientId = new Random();
		String kafkaUri = SubscriptionUtil.constructKafkaURI(null,"kafka-topic", "test-subscriptionid", subscriptionUtil,
				randomClientId);
		Assert.assertNotNull("kafka Uri Should not be null ::", kafkaUri);
		Assert.assertTrue("kafka Uri Should not be empty ::", !kafkaUri.isEmpty());
	}

	@Test
	public void testConstructJMSQueueURI() {
		String jmsUri = SubscriptionUtil.constructJMSQueueURI("Test-JMS");
		Assert.assertNotNull("jms uri should not be null ::", jmsUri);
		Assert.assertEquals("jms:queue:Test-JMS", jmsUri);
	}

	@Test
	public void testConstructJMSConsumeQueueURI() {
		JMSSubscribeEvent jmsSubscribeEvent = new JMSSubscribeEvent();
		jmsSubscribeEvent.setAcknowledge("AUTO_ACKNOWLEDGE");
		jmsSubscribeEvent.setConcurrentConsumer("2");
		String jmsConsumer = SubscriptionUtil.constructJMSConsumeQueueURI("jms-Queue", "JMS-Subscrip",
				jmsSubscribeEvent);
		Assert.assertNotNull("jms Uri Should not be null ::", jmsConsumer);
		Assert.assertTrue("jms Uri Should not be empty ::", !jmsConsumer.isEmpty());
	}

	@Test
	public void testConstructJMSRetryQueueURI() {
		FailureHandlingStrategy handlingStrategy = new FailureHandlingStrategy();
		handlingStrategy.setFailureStrategyConfig(
				"{\"retryCount\":\"4\",\"retryInterval\":\"20\",\"retryIntervalMultiplier\":\"1\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}");

		JMSSubscribeEvent jmsSubscribeEvent = new JMSSubscribeEvent();
		jmsSubscribeEvent.setFailureHandlingStrategy(handlingStrategy);
		String jmsRetryUri = SubscriptionUtil.constructJMSRetryQueueURI(jmsSubscribeEvent);
		Assert.assertNotNull("jms uri should not be null ::", jmsRetryUri);
		Assert.assertEquals("jms:queue:JMSQueue1", jmsRetryUri);
	}

	@Test
	public void testConstructJMSRetryConsumeQueueURI() {
		FailureHandlingStrategy handlingStrategy = new FailureHandlingStrategy();
		handlingStrategy.setFailureStrategyConfig(
				"{\"retryCount\":\"4\",\"retryInterval\":\"20\",\"retryIntervalMultiplier\":\"1\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}");

		JMSSubscribeEvent jmsSubscribeEvent = new JMSSubscribeEvent();
		jmsSubscribeEvent.setFailureHandlingStrategy(handlingStrategy);

		String jmsRetryConsume = SubscriptionUtil.constructJMSRetryConsumeQueueURI("JMS-Sub", jmsSubscribeEvent);
		Assert.assertNotNull("jms Uri Should not be null ::", jmsRetryConsume);
		Assert.assertTrue("jms Uri Should not be empty ::", !jmsRetryConsume.isEmpty());
	}

	@Test
	public void testConstructQuartzURI() {
		String quartzUri = SubscriptionUtil.constructQuartzURI();
		Assert.assertNotNull("quartz Uri Should not be null ::", quartzUri);
		Assert.assertTrue("jmquartz Uri Should not be empty ::", !quartzUri.isEmpty());
	}

	@Test
	public void testConstructQuartzJMSURI() {
		String quartzJmsUri = SubscriptionUtil.constructQuartzJMSURI();
		Assert.assertNotNull("quartz JMS Uri Should not be null ::", quartzJmsUri);
		Assert.assertTrue("jmquartz JMS Uri Should not be empty ::", !quartzJmsUri.isEmpty());
	}

	@Test
	public void testConstructJMSRetryConsumeQueueURIWithAck() {
		String jmsConsumeQueueURI = SubscriptionUtil.constructJMSRetryConsumeQueueURI("JMS-QUEUE", "2",
				"AUTO_ACKNOWLEDGE");
		Assert.assertNotNull("jms Consume Queue URI Should not be null ::", jmsConsumeQueueURI);
		Assert.assertTrue("jms Consume Queue URI Should not be empty ::", !jmsConsumeQueueURI.isEmpty());
	}

	@Test
	public void testConstructSedaURIForRetry() {
		String uriForRetry = SubscriptionUtil.constructSedaURIForRetry();
		Assert.assertNotNull("uri for retry Should not be null ::", uriForRetry);
		Assert.assertTrue("uri for retry Should not be empty ::", !uriForRetry.isEmpty());
	}

	@Test
	public void testConstructSedaURIToProcessMessage() {
		String uriToProcessMsg = SubscriptionUtil.constructSedaURIToProcessMessage( "JMS-Sub");
		Assert.assertNotNull("uri for ProcessMsg Should not be null ::", uriToProcessMsg);
		Assert.assertTrue("uri for ProcessMsg Should not be empty ::", !uriToProcessMsg.isEmpty());
	}

	@Test
	public void testBuildConfigContext() {
		String subId = GenericTestConstant.TEST_FEATUREGROUP + "-" + GenericTestConstant.TEST_FEATURE + "-"
				+ GenericTestConstant.TEST_IMPL + "-" + GenericTestConstant.TEST_VENDOR + "-"
				+ GenericTestConstant.TEST_VERSION + "-" + "provider_Name";
		ConfigurationContext configContext = subscriptionUtil.buildConfigContext(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, subId);
		Assert.assertNotNull("configuration context should not be null ::", configContext);
		Assert.assertEquals("feature name should be same as configuration Context feature name ::",
				GenericTestConstant.TEST_FEATURE, configContext.getFeatureName());
	}

	@Test
	public void testGetActualSubscriberId() {
		String subId = GenericTestConstant.TEST_FEATUREGROUP + "-" + GenericTestConstant.TEST_FEATURE + "-"
				+ GenericTestConstant.TEST_IMPL + "-" + GenericTestConstant.TEST_VENDOR + "-"
				+ GenericTestConstant.TEST_VERSION + "-" + "provider_Name";
		String getActualSubId = subscriptionUtil.getActualSubscriberId(subId);
		Assert.assertNotNull("configuration context should not be null ::", getActualSubId);
		Assert.assertEquals("provider name should be same as getActualSubId ::", "provider_Name", getActualSubId);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetPreviousDateInstance() throws ParseException {
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject retryConfigurationJSON = (org.json.simple.JSONObject) parser.parse(retryJson);
		Assert.assertNotNull("Simple JSONObject Should not be null :: ", retryConfigurationJSON);
		Date date = SubscriptionUtil.getPreviousDateInstance(retryConfigurationJSON);
		Assert.assertEquals(new Date().getDate(), date.getDate());
	}

	private JSONObject getJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("test", "Demo");
		jsonObject.put("testClassName", "SubscriptionUtil");
		return jsonObject;
	}

	private JSONArray getJsonArray() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("test", "Demo");
		jsonObject.put("testClassName", "SubscriptionUtil");

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(jsonObject);
		return jsonArray;
	}
}
