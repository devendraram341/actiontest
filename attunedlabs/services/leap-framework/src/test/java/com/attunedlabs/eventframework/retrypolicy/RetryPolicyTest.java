package com.attunedlabs.eventframework.retrypolicy;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryPolicyTest {

	final Logger logger = LoggerFactory.getLogger(RetryPolicyTest.class);

	/**
	 * this method used for get policy properties.
	 */
	@Test
	public void testGetPolicyProperties() {
		Properties policyProp = RetryPolicy.getPolicyProperties();
		Assert.assertNotNull("Policy property should not be null :: ", policyProp);
	}

	/**
	 * this method used for get failed retry interval from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetFailedRetryInterval() {
		int retryInterval = RetryPolicy.getFailedRetryInterval();
		Assert.assertNotEquals("Retry Interval value should not be zero :: ", 0, retryInterval);
		Assert.assertEquals("Retry Interval value should be one :: ", 1, retryInterval);
	}

	/**
	 *  this method used for get Failed Retry Interval Multiplier from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetFailedRetryIntervalMultiplier() {
		int retryIntervalMultip = RetryPolicy.getFailedRetryIntervalMultiplier();
		Assert.assertNotEquals("Retry Interval Multiplier value should not be zero :: ", 0, retryIntervalMultip);
		Assert.assertEquals("Retry Interval Multiplier value should be two :: ", 2, retryIntervalMultip);
	}

	/**
	 *  this method used for get Failed Maximum Retry Count from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetFailedMaximumRetryCount() {
		int maxRetryCount = RetryPolicy.getFailedMaximumRetryCount();
		Assert.assertNotEquals("Max Retry count value should not be zero :: ", 0, maxRetryCount);
		Assert.assertEquals("Max Retry count value should be five :: ", 5, maxRetryCount);
	}

	/**
	 *  this method used for get Failed Maximum Retry Interval from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetFailedMaximumRetryInterval() {
		int maxRetryInterval = RetryPolicy.getFailedMaximumRetryInterval();
		Assert.assertNotEquals("Max Retry Interval value should not be zero :: ", 0, maxRetryInterval);
		Assert.assertEquals("Max Retry Interval value should be 200 :: ", 200, maxRetryInterval);
	}

	/**
	 *  this method used for get Failed Time Interval Unit from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetFailedTimeIntervalUnit() {
		String timeIntervalUnit = RetryPolicy.getFailedTimeIntervalUnit();
		Assert.assertNotNull("Time Interval Unit value should not be null ::", timeIntervalUnit);
		Assert.assertEquals("Time Interval unit Value should be same as 'MINUTES' ::", "MINUTES", timeIntervalUnit);
	}

	/**
	 *  this method used for get Normal Retry Interval from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetNormalRetryInterval() {
		int normalRetryInterval = RetryPolicy.getNormalRetryInterval();
		Assert.assertNotEquals("Normal Retry Interval value should not be zero :: ", 0, normalRetryInterval);
		Assert.assertEquals("Normal Retry Interval value should be one :: ", 1, normalRetryInterval);
	}

	/**
	 *  this method used for get Normal Retry Count from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetNormalRetryCount() {
		int normalRetryCount = RetryPolicy.getNormalRetryCount();
		Assert.assertNotEquals("Normal Retry Count value should not be zero :: ", 0, normalRetryCount);
		Assert.assertEquals("Normal Retry Count value should be two :: ", 2, normalRetryCount);
	}

	/**
	 *  this method used for get Normal Time Interval Unit from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetNormalTimeIntervalUnit() {
		String normalTimeIntervalUnit = RetryPolicy.getNormalTimeIntervalUnit();
		Assert.assertNotNull("Normal Time Interval Unit value should not be null ::", normalTimeIntervalUnit);
		Assert.assertEquals("Normal Time Interval unit Value should be same as 'MINUTES' ::", "MINUTES",
				normalTimeIntervalUnit);
	}

	/**
	 *  this method used for get Max Retry Records Count from globalAppDeploymentConfig file
	 */
	@Test
	public void testGetMaxRetryRecordsCount() {
		int maxRetryCount = RetryPolicy.getMaxRetryRecordsCount();
		Assert.assertNotEquals("Max Retry Records Count value should not be zero :: ", 0, maxRetryCount);
		Assert.assertEquals("Max Retry Records Count value should be 15 :: ", 15, maxRetryCount);
	}

}
