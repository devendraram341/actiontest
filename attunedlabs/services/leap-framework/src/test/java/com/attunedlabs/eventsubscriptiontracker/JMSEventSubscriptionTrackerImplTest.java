package com.attunedlabs.eventsubscriptiontracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.eventsubscription.exception.EventSubscriptionTrackerException;
import com.attunedlabs.eventsubscription.exception.SubscriptionTableExistenceException;
import com.attunedlabs.eventsubscriptiontracker.impl.JMSEventSubscriptionTrackerImpl;

public class JMSEventSubscriptionTrackerImplTest {
	final Logger log = LoggerFactory.getLogger(getClass());
	private static IEventSubscriptionTrackerService subscriptionTrackerService;
	final String retryJson = "{\"retryCount\":\"4\",\"retryInterval\":\"0\",\"retryIntervalMultiplier\":\"1\",\"maximumRetryInterval\":\"60\",\"timeIntervalUnit\":\"SECOND\",\"retryTopRecords\":\"50\",\"retryQueueName\":\"JMSQueue1\",\"retryConsumers\":\"2\"}";

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws TimeoutException
	 * @throws IOException
	 */
	@BeforeClass
	public static void init() throws IOException, TimeoutException {
		if (subscriptionTrackerService == null) {
			subscriptionTrackerService = new JMSEventSubscriptionTrackerImpl();
		}
	}
	
	@Before
	public void addEventSubscriptionRecord() {
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", getIntialEventSubscriptionTracker());
		subscriptionTrackerService.addNewSubscriptionRecord(null, metaData);
	}

	/**
	 * This method use for create table of eventSubscription.
	 * 
	 * @throws SubscriptionTableExistenceException
	 */
	@Test
	public void testCreateTrackerTableForSubscription() throws SubscriptionTableExistenceException {
		boolean isCreate = subscriptionTrackerService.createTrackerTableForSubscription();
		Assert.assertTrue("Table should be exist :: ", isCreate);
	}

	/**
	 * This method use for check record exist or not
	 */
	@Test
	public void testRecordIsNotAlreadyPresent() {
		EventSubscriptionTracker subscriptionTracker = new EventSubscriptionTracker();
		subscriptionTracker.setTenantId(GenericTestConstant.TEST_TENANT);
		subscriptionTracker.setEventData(GenericTestConstant.EVENT_DATA);
		subscriptionTracker.setIsRetryable(true);
		subscriptionTracker.setOffset("0");
		subscriptionTracker.setPartition("0");
		subscriptionTracker.setSiteId(GenericTestConstant.TEST_SITE);
		subscriptionTracker.setFailureMsg(null);
		subscriptionTracker.setRetryCount(0);
		subscriptionTracker.setSubscriptionId("Test-RecordIsNot");

		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", subscriptionTracker);
		boolean isPresent = subscriptionTrackerService.recordIsNotAlreadyPresent(null, metaData);
		Assert.assertTrue("", isPresent);
	}

	/**
	 * This method use for update status of eventSubscription table.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testUpdateSubscriptionRecordStatus() throws ParseException {
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", getIntialEventSubscriptionTracker());
		metaData.put("subscriptionQuartzTrigger", true);

		JSONParser parser = new JSONParser();
		JSONObject retryConfigurationJSON = (JSONObject) parser.parse(retryJson);
		Assert.assertNotNull("Simple JSONObject Should not be null :: ", retryConfigurationJSON);

		boolean isUpdated = subscriptionTrackerService.updateSubscriptionRecordStatus(null, metaData, "SUCCESS", null,
				retryConfigurationJSON);
		Assert.assertTrue("Subscription Record should be updated into DB ::", isUpdated);
	}

	/**
	 * This method use for get all event subscription record of status is new.
	 * 
	 * @throws EventSubscriptionTrackerException
	 * @throws ParseException
	 */
	@Test
	public void testGetAllSubscriptionRecordsIntitializedForLongTime()
			throws EventSubscriptionTrackerException, ParseException {
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", getIntialEventSubscriptionTracker());
		JSONParser parser = new JSONParser();
		JSONObject retryConfigJson = (JSONObject) parser.parse(retryJson);

		boolean isUpdated = subscriptionTrackerService.updateSubscriptionRecordStatus(null, metaData, "NEW", null,
				retryConfigJson);
		Assert.assertTrue("Subscription Record should be updated into DB ::", isUpdated);

		List<EventSubscriptionTracker> listOfEventSubTracker = subscriptionTrackerService
				.getAllSubscriptionRecordsIntitializedForLongTime(null, GenericTestConstant.TEST_TENANT,
						GenericTestConstant.TEST_SITE, "Test-JMSID", retryConfigJson);
		Assert.assertNotNull("List Of Event Subscription Tracker should not be null ",
				listOfEventSubTracker);
	}

	/**
	 * This method use for get all inProcess record for long time and arrage by
	 * retry from eventSubscription table.
	 * 
	 * @throws EventSubscriptionTrackerException
	 * @throws ParseException
	 */
	@Test
	public void testGetAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry()
			throws EventSubscriptionTrackerException, ParseException {
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", getIntialEventSubscriptionTracker());

		JSONParser parser = new JSONParser();
		JSONObject retryConfigJson = (JSONObject) parser.parse(retryJson);

		boolean isUpdated = subscriptionTrackerService.updateSubscriptionRecordStatus(null, metaData, "IN_PROCESS",
				null, retryConfigJson);
		Assert.assertTrue("Subscription Record should be updated into DB ::", isUpdated);

		List<EventSubscriptionTracker> listOfInProcessRecord = subscriptionTrackerService
				.getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(null, GenericTestConstant.TEST_TENANT,
						GenericTestConstant.TEST_SITE, "Test-JMSID", "IN_PROCESS", retryConfigJson);
		Assert.assertNotNull("List Of Event Subscription Tracker should not be null ",
				listOfInProcessRecord);
	}

	/**
	 * This method use for get all fail record from eventSubscription table and
	 * arraged with fail time and retry count
	 * 
	 * @throws ParseException
	 * @throws EventSubscriptionTrackerException
	 */
	@Test
	public void testGetAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount()
			throws ParseException, EventSubscriptionTrackerException {
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("EventSubscriptionTracker", getIntialEventSubscriptionTracker());

		JSONParser parser = new JSONParser();
		JSONObject retryConfigJson = (JSONObject) parser.parse(retryJson);

		boolean isUpdated = subscriptionTrackerService.updateSubscriptionRecordStatus(null, metaData, "FAILED", null,
				retryConfigJson);
		Assert.assertTrue("Subscription Record should be updated into DB ::", isUpdated);

		List<EventSubscriptionTracker> listOfFailedRecord = subscriptionTrackerService
				.getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(null,
						GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE, "Test-JMSID", "FAILED",
						retryConfigJson);
		Assert.assertTrue("List Of Event Subscription Tracker size Should be Grater then 0 ,",
				listOfFailedRecord.size() > 0);
		Assert.assertEquals("listOfEventSubTracker topic name should be same as 'TEST-JMS' :: ", "TEST-JMS",
				listOfFailedRecord.get(0).getTopic());
	}

	/**
	 * init Event subscription data for eventSubscription table.
	 * 
	 * @return
	 */
	private static EventSubscriptionTracker getIntialEventSubscriptionTracker() {
		EventSubscriptionTracker subscriptionTracker = new EventSubscriptionTracker();
		subscriptionTracker.setTenantId(GenericTestConstant.TEST_TENANT);
		subscriptionTracker.setEventData(GenericTestConstant.EVENT_DATA);
		subscriptionTracker.setIsRetryable(true);
		subscriptionTracker.setOffset("0");
		subscriptionTracker.setTopic("TEST-JMS");
		subscriptionTracker.setPartition("0");
		subscriptionTracker.setSiteId(GenericTestConstant.TEST_SITE);
		subscriptionTracker.setFailureMsg(null);
		subscriptionTracker.setRetryCount(0);
		subscriptionTracker.setSubscriptionId("Test-JMSID");
		return subscriptionTracker;
	}
	
}
