package com.attunedlabs.leap.context.helper;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContextConfigException;
import com.attunedlabs.leap.context.bean.LeapRequest;

public class LeapRequestHelperTest {

	private Logger log = LoggerFactory.getLogger(LeapRequestHelperTest.class);
	private String jsonString = "{\"test\":\"demo\"}";
	private String leapRequst = "{\"apiVersion\":\"1.0\",\"context\":\"context\",\" lang\":\"en\",\"data\":{\"test\":\"demo\"}}";

	/**
	 * Method used to create Leap Request Instance from the given king and data.
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testConstructLeapRequestFromJSONObject() throws LeapDataContextConfigException {
		JSONObject jsonObject = new JSONObject(jsonString);
		LeapRequest leapRequest = LeapRequestHelper.constructLeapRequestFromJSONObject(jsonObject);
		log.debug("LeapRequestLeapRequest " + leapRequest);
		Assert.assertNotNull("Leap Request Data Should not be null ::", leapRequest);
		Assert.assertTrue(leapRequest.toString().contains(jsonString));
	}

	/**
	 * Method used to convert a LeapRequest instance to JSON Object.
	 * 
	 * @throws LeapDataContextConfigException
	 */
	@Test
	public void testToJson() throws LeapDataContextConfigException {
		JSONObject jsonObject = new JSONObject(jsonString);
		LeapRequest leapRequest = LeapRequestHelper.constructLeapRequestFromJSONObject(jsonObject);

		JSONObject json = LeapRequestHelper.toJson(leapRequest);
		Assert.assertNotNull("jsonObject should not be null ::", jsonObject);
		Assert.assertEquals("jsonString data should be same as jsonObject :: ", jsonString,
				json.getJSONObject("data").toString());
		Assert.assertTrue("jsonObject Shuld be Contains JsonString Data ::", json.toString().contains(jsonString));
	}

	/**
	 * Method used to get the Json data present in LeapRequest.
	 */
	@Test
	public void testGetJsonData() {
		JSONObject jsonData = LeapRequestHelper.getJsonData(leapRequst);
		log.debug("jsonData ::::: " + jsonData);
		Assert.assertNotNull("json Object should not be null ::", jsonData);
		Assert.assertEquals("jsonObject data Should be same as retrun jsonData::",
				new JSONObject(jsonString).toString(), jsonData.toString());
	}

}
