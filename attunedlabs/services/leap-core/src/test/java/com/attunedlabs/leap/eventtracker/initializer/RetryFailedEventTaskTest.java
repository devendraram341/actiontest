package com.attunedlabs.leap.eventtracker.initializer;

import java.util.List;

import org.apache.camel.Exchange;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.leap.LeapDataContext;

public class RetryFailedEventTaskTest {

	private static RetryFailedEventTask eventTask;
	private Exchange exchange;
	private IEventDispatcherTrackerService eventDispatcherTrackerService;
	private LeapDataContext leapDataCtx;

	@BeforeClass
	public static void init() {
		System.setProperty(PROFILE_ID, LOCAL);
		eventTask = new RetryFailedEventTask();
	}

	@Before
	public void setUp() throws EventDispatcherTrackerException {
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (eventDispatcherTrackerService == null)
			eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();

		addEventDispatcherTracker();
	}

	/**
	 * this method will retry the events that are marked as FAILED or RETRY-FAILED
	 * or events that are IN_PROCESS for long time(assuming some internal issue
	 * while dispatching).
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testPerformTask() throws EventDispatcherTrackerException {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		Assert.assertNotNull("LeapDataContext Should not be null :: ", leapDataCtx);
		setLeapEvent();

		eventTask.performTask(exchange);

		boolean flag = CheckEventDispatcherTracker();
		Assert.assertTrue("flag Data Should be true ::", flag);
	}

	@After
	public void cleanUp() {
		eventUpdateAndDelete();
	}

	private LeapEvent setLeapEvent() throws EventDispatcherTrackerException {
		LeapEvent event = new LeapEvent("testEventId");
		event.setDispatchChannelId("testDispatchChannelId");
		event.addMetadata("EVT_CONTEXT", LeapCoreTestUtils.getRequestContext());
		LeapEventContext.addLeapEvent(leapDataCtx.getServiceDataContext().getRequestUUID(), event,
				leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE));
		return event;
	}

	private void addEventDispatcherTracker() throws EventDispatcherTrackerException {
		LeapEvent setLeapEvent = setLeapEvent();
		eventDispatcherTrackerService.addEventTracking(TEST_TENANT, TEST_SITE, "123456789", "123456789", setLeapEvent,
				false, exchange);
	}

	private void eventUpdateAndDelete() {
		try {
			eventDispatcherTrackerService.updateEventStatus(TEST_TENANT, TEST_SITE, "123456789", "123456789",
					"COMPLETE", "testEventId", false, null, false, false, "testDispatchChannelId");
			eventDispatcherTrackerService.removeEventTrackRecord(TEST_TENANT, TEST_SITE, "123456789", "123456789",
					"testDispatchChannelId", "testEventId");
		} catch (EventDispatcherTrackerException e) {
			e.printStackTrace();
		}
	}

	private boolean CheckEventDispatcherTracker() throws EventDispatcherTrackerException {
		boolean flag = false;
		List<EventDispatcherTracker> allTrackRecords = eventDispatcherTrackerService.getAllTrackRecords(exchange);
		for (EventDispatcherTracker tracker : allTrackRecords) {
			if (tracker.getLeapEventId().equalsIgnoreCase("testEventId")) {
				flag = true;
				break;
			}
		}
		return flag;
	}

}
