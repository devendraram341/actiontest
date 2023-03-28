package com.attunedlabs.eventsubscription.retrypolicy;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionRetryPolicyTest {

	final Logger log = LoggerFactory.getLogger(SubscriptionRetryPolicyTest.class);
	final String retryJson = "{\"retryCount\":\"4\",\"retryInterval\":\"20\",\"retryIntervalMultiplier\":\"4\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}";

	/**
	 * This method used for get Retry Interval Multiplier from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetRetryIntervalMultiplier() throws ParseException {
		int retryIntervalMulti = SubscriptionRetryPolicy.getRetryIntervalMultiplier(getJsonObject());
		Assert.assertNotEquals("Retry Interval Multiplier should not be zero ::", 0, retryIntervalMulti);
		Assert.assertEquals("This test case have retry interval multiplyer should be 4 ::", 4, retryIntervalMulti);
	}

	/**
	 * This method used for get Retry Interval from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testgetRetryInterval() throws ParseException {
		int retryInterval = SubscriptionRetryPolicy.getRetryInterval(getJsonObject());
		Assert.assertNotEquals("Retry Interval should not be zero ::", 0, retryInterval);
		Assert.assertEquals("This test case have retry interval should be 20 ::", 20, retryInterval);
	}

	/**
	 * This method used for get Maximum Retry Interval from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetMaximumRetryInterval() throws ParseException {
		int maxRetry = SubscriptionRetryPolicy.getMaximumRetryInterval(getJsonObject());
		Assert.assertNotEquals("Max Retry Interval should not be zero ::", 0, maxRetry);
		Assert.assertEquals("This test case have Max retry interval should be 60 ::", 60, maxRetry);
	}

	/**
	 * This method used for get Time Interval Unit from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetTimeIntervalUnit() throws ParseException {
		String timeIntervalUnit = SubscriptionRetryPolicy.getTimeIntervalUnit(getJsonObject());
		Assert.assertNotNull("Time interval Unit should not be null ::", timeIntervalUnit);
		Assert.assertEquals("This test case have Time interval Unit should be SECOND ::", "SECOND", timeIntervalUnit);
	}

	/**
	 * This method used for get Retry Count from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetRetryCount() throws ParseException {
		int retryCount = SubscriptionRetryPolicy.getRetryCount(getJsonObject());
		Assert.assertNotEquals("Retry count should not be zero ::", 0, retryCount);
		Assert.assertEquals("This test case have retry count should be 4 ::", 4, retryCount);
	}

	/**
	 * This method used for get Max Retry Records Count from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetMaxRetryRecordsCount() throws ParseException {
		int maxRetryRecordCount = SubscriptionRetryPolicy.getMaxRetryRecordsCount(getJsonObject());
		Assert.assertNotEquals("max Retry count should not be zero ::", 0, maxRetryRecordCount);
		Assert.assertEquals("This test case have max retry count should be 50 ::", 50, maxRetryRecordCount);
	}

	/**
	 * This method used for get Retry QueueName from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetRetryQueueName() throws ParseException {
		String retryQueueName = SubscriptionRetryPolicy.getRetryQueueName(getJsonObject());
		Assert.assertNotNull("Queue Name should not be null ::", retryQueueName);
		Assert.assertEquals("This test case have Queue Name should be retry_all_all ::", "retry_all_all",
				retryQueueName);
	}

	/**
	 * This method used for get Retry Consumers from retryjson jsonObejct.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testGetRetryConsumers() throws ParseException {
		int retryConsumer = SubscriptionRetryPolicy.getRetryConsumers(getJsonObject());
		Assert.assertNotEquals("Retry consumer should not be zero ::", 0, retryConsumer);
		Assert.assertEquals("This test case have retry consumer should be 2 ::", 2, retryConsumer);
	}

	/**
	 * This method use for retryJson String convert to jsonObject.
	 * 
	 * @return
	 * @throws ParseException
	 */
	private JSONObject getJsonObject() throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject retryConfigurationJSON = (JSONObject) parser.parse(retryJson);
		return retryConfigurationJSON;
	}
}
