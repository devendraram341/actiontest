package com.attunedlabs.eventsubscription.retrypolicy;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionNoRetryPolicyTest {

	final Logger log = LoggerFactory.getLogger(SubscriptionNoRetryPolicyTest.class);
	final String retryJson = "{\"retryCount\":\"4\",\"retryInterval\":\"0\",\"retryIntervalMultiplier\":\"1\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}";

	/**
	 * This method use for check message load enabled or not
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testAssertMessageLogEnabled() throws ParseException {
		boolean flag = SubscriptionNoRetryPolicy.assertMessageLogEnabled(getJsonObject());
		Assert.assertTrue(flag);
	}

	/**
	 * This method use for check paralled processing is enabled or not.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testAssertParallelProcessingEnabled() throws ParseException {
		boolean flag = SubscriptionNoRetryPolicy.assertParallelProcessingEnabled(getJsonObject());
		Assert.assertTrue(flag);
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
