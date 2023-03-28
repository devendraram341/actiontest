package com.attunedlabs.eventframework.eventtracker;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.ContextData;
import com.attunedlabs.GenericTestConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;

public class EventDispatcherTrackerServiceTest {

	private IEventDispatcherTrackerService trackerService = new EventDispatcherTrackerImpl();

	final Logger log = LoggerFactory.getLogger(getClass());

	private IConfigPersistenceService perService;

	/**
	 * In this method used for Initialization.
	 * 
	 * @throws EventDispatcherTrackerException
	 * @throws ConfigPersistenceException
	 */
	@Before
	public void init() throws EventDispatcherTrackerException, ConfigPersistenceException {
		if (perService == null)
			perService = new ConfigPersistenceServiceMySqlImpl();
		perService.deleteConfigNodeDataByNodeId(GenericTestConstant.TEST_VENDOR_NODEID);
		testAddEventTracking();
	}

	/**
	 * this method used for adding event into EventDispatcher table into DB.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	private void testAddEventTracking() throws EventDispatcherTrackerException {
		RequestContext context = ContextData.getRequestContext();
		Assert.assertNotNull("request context should not be null ", context);

		LeapEvent event = new LeapEvent("FILE_STORE", context);
		event.setDispatchChannelId("FILE_STORE");
		Assert.assertEquals("Request context should be same as event request context ::", context,
				event.getRequestContext());
		boolean isAdded = trackerService.addEventTracking(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", event, false, null);
		Assert.assertTrue("should be addEvent data into DB ::", isAdded);

	}

	/**
	 * this method used for get retryCount data from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetRetryCount() throws EventDispatcherTrackerException {

		int count = trackerService.getRetryCount(null, GenericTestConstant.TEST_TENANT, GenericTestConstant.TEST_SITE,
				GenericTestConstant.TEST_REQUEST_ID, "xyz", "FILE_STORE", "FILE_STORE");
		Assert.assertEquals("Retry Count should be zero ::", 0, count);
	}

	/**
	 * this method used for get status from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetTrackStatusForEventList() throws EventDispatcherTrackerException {
		String trackerStatus = trackerService.getTrackStatusForEventList(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "FILE_STORE", null);
		Assert.assertNotNull("Tracker status should not be null ::", trackerStatus);
		Assert.assertEquals("Tracker satus should be same as 'NEW' :: ", "NEW", trackerStatus);
	}

	/**
	 * this method used for get All Record from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllTrackRecords() throws EventDispatcherTrackerException {
		List<EventDispatcherTracker> allEventDispatcherRecord = trackerService.getAllTrackRecords(null);
		Assert.assertTrue("EventDispatcher List should not be empty or zero::", allEventDispatcherRecord.size() > 0);
		Assert.assertEquals("Event Tracker Status Should be same as 'NEw' :", "NEW",
				allEventDispatcherRecord.get(0).getStatus());
	}

	/**
	 * this method used for get All Record data from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllTrackRecordsOnStatus() throws EventDispatcherTrackerException {
		List<EventDispatcherTracker> allEventDispatcherRecord = trackerService.getAllTrackRecordsOnStatus(null, "NEW");
		Assert.assertTrue("EventDispatcher List should not be empty or zero::", allEventDispatcherRecord.size() > 0);
		Assert.assertEquals("Event Tracker Status Should be same as 'NEW' :", "NEW",
				allEventDispatcherRecord.get(0).getStatus());
	}

	/**
	 * this method used for get all record in Intitial status for long time from
	 * EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllTrackRecordsIntitializedForLongTime() throws EventDispatcherTrackerException {
		List<EventDispatcherTracker> allTrackRecord = trackerService.getAllTrackRecordsIntitializedForLongTime(null);
		Assert.assertEquals("EventDispatcherRecord should be 0 because at a same time insert and fatch the data ", 0,
				allTrackRecord.size());
	}

	/**
	 * this method used for get all record InProcess status for long time from
	 * EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllTrackRecordsInProcessForLongTime() throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "IN_PROCESS", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allInProcessData = trackerService.getAllTrackRecordsInProcessForLongTime(null,
				"IN_PROCESS");
		Assert.assertEquals("EventDispatcherRecord should be 0 because at a same time insert and fatch the data", 0,
				allInProcessData.size());
	}

	/**
	 * this method used for get all record in InProcess status for long time and
	 * arrange by retry from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllTrackRecordsInProcessForLongTimeArrangedByRetry() throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "RETRY_INPROCESS",
				"FILE_STORE", false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allInProcessData = trackerService
				.getAllTrackRecordsInProcessForLongTimeArrangedByRetry(null, "RETRY_INPROCESS");
		Assert.assertEquals("EventDispatcherRecord should be 0 because at a same time insert and fatch the data", 0,
				allInProcessData.size());
	}

	/**
	 * this method used for get event record on request Id from EventDispatcher
	 * table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetEventRecordOnRequestUUID() throws EventDispatcherTrackerException {
		EventDispatcherTracker getEventDispatcher = trackerService.getEventRecordOnRequestUUID(null,
				GenericTestConstant.TEST_REQUEST_ID, "FILE_STORE");
		Assert.assertNotNull("Event Dispatcher Tracker should not be null ::", getEventDispatcher);
		Assert.assertEquals("Event Status should be same as 'NEW' :: ", "NEW", getEventDispatcher.getStatus());
	}

	/**
	 * this method used for get all failed event record with arranged by failure
	 * Time from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllFailedEventRecordsArrangedByFailureTime() throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "FAILED", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allFailEventDispatcher = trackerService
				.getAllFailedEventRecordsArrangedByFailureTime(null);
		Assert.assertTrue("Event Dispatcher should be exist :: ", allFailEventDispatcher.size() > 0);
		Assert.assertEquals("Status should be same as failEventDispatcher status ::", "FAILED",
				allFailEventDispatcher.get(0).getStatus());
	}

	/**
	 * this method used for get all faileed event record Arranged by fail time and
	 * retry count from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllFailedEventRecordsArrangedByFailureTimeAndRetryCount()
			throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "FAILED", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allFailEventDispatcher = trackerService
				.getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount(null);
		Assert.assertTrue("Event Dispatcher should be exist :: ", allFailEventDispatcher.size() > 0);
		Assert.assertEquals("Status should be same as failEventDispatcher status ::", "FAILED",
				allFailEventDispatcher.get(0).getStatus());
		Assert.assertEquals("for testing retry count should be zero ::", 0,
				(int) allFailEventDispatcher.get(0).getRetryCount());
	}

	/**
	 * this method used for get all RETRY_FAILED record with arranged by failure
	 * time from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testGetAllRetryFailedEventRecordsArrangedByFailureTime() throws EventDispatcherTrackerException {

		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "RETRY_FAILED", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allRetryFailEventDispatcher = trackerService
				.getAllRetryFailedEventRecordsArrangedByFailureTime(null);
		Assert.assertTrue("Event Dispatcher should be exist :: ", allRetryFailEventDispatcher.size() > 0);
		Assert.assertEquals("Status should be same as failEventDispatcher status ::", "RETRY_FAILED",
				allRetryFailEventDispatcher.get(0).getStatus());
	}

	/**
	 * this method used for get all RETRY_FAILED record with arranged by failure
	 * time and retry count from EventDispatcher table.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testgetAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount()
			throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "RETRY_FAILED", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);

		List<EventDispatcherTracker> allRetryFailEventDispatcher = trackerService
				.getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount(null);
		Assert.assertTrue("Event Dispatcher should be exist :: ", allRetryFailEventDispatcher.size() > 0);
		Assert.assertEquals("Status should be same as failEventDispatcher status ::", "RETRY_FAILED",
				allRetryFailEventDispatcher.get(0).getStatus());
		Assert.assertEquals("for testing retry count should be zero ::", 0,
				(int) allRetryFailEventDispatcher.get(0).getRetryCount());
	}

	/**
	 * this method used for update event Status.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	private void testUpdateEventStatus() throws EventDispatcherTrackerException {
		boolean isUpdate = trackerService.updateEventStatus(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "COMPLETE", "FILE_STORE",
				false, null, false, false, "FILE_STORE");
		Assert.assertTrue("Should be update data into DB ::", isUpdate);
	}

	private void testRemoveEventTrackRecord() throws EventDispatcherTrackerException {
		boolean isDeleted = trackerService.removeEventTrackRecord(GenericTestConstant.TEST_TENANT,
				GenericTestConstant.TEST_SITE, GenericTestConstant.TEST_REQUEST_ID, "xyz", "FILE_STORE", "FILE_STORE");
		Assert.assertTrue("Should be delete data after updating from DB ::", isDeleted);
	}

	/**
	 * This method use for eventdispatcher data update and deleted after each
	 * method.
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@After
	public void clearEventDispatcherDB() throws EventDispatcherTrackerException {
		testUpdateEventStatus();
		testRemoveEventTrackRecord();
	}
}
