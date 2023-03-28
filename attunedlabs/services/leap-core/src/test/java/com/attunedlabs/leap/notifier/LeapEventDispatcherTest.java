package com.attunedlabs.leap.notifier;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.impl.DefaultMessageHistory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.attunedlabs.LeapCoreTestFileRead;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.configdbtest.DeleteConfigNodeForTesting;
import com.attunedlabs.configdbtest.InsertConfigNodeForTesting;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.jaxb.DispatchChannel;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.eventframework.jaxb.SystemEvent;
import com.attunedlabs.leap.LeapDataContext;

public class LeapEventDispatcherTest {

	private LeapEventDispatcher dispatcher;
	private LeapDataContext leapDataCtx;
	private Exchange exchange;
	private IEventDispatcherTrackerService eventDispatcherTrackerService;

	@BeforeClass
	public static void init() {
		System.setProperty(PROFILE_ID, LOCAL);
		new InsertConfigNodeForTesting();
	}

	@Before
	public void setUp() {
		if (dispatcher == null)
			dispatcher = new LeapEventDispatcher();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (eventDispatcherTrackerService == null)
			eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
	}

	/**
	 * Decides whether the event has to be dispatched or not with isServiceCompleted
	 * is true..
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDispatchEventsWithIsServiceCompletedTrue() throws Exception {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.setProperty("CamelMessageHistory", setDefaultMessageHistoryList());
		exchange.getIn().setHeader(IS_SERVICE_COMPLETED, true);
		setLeapEvent(0);
		
		dispatcher.dispatchEvents(exchange);
		String readDispatcherFile = LeapCoreTestFileRead.readDispatcherFile();
		Thread.sleep(100);
		Assert.assertNotNull("File content should not be null ::", readDispatcherFile);
		Assert.assertTrue("file should be contain systemEvent success Id ::",
				readDispatcherFile.contains("SERVICE_COMPLETION_SUCCESS"));
		Assert.assertTrue("file should be contain event Id ::", readDispatcherFile.contains("Success_Event_Test"));
	}

	/**
	 * Decides whether the event has to be dispatched or not with isServiceCompleted
	 * is false.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDispatchEventsWithIsServiceCompletedFalse() throws Exception {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		exchange.getIn().setHeader(IS_SERVICE_COMPLETED, false);
		setLeapEvent(1);
		
		dispatcher.dispatchEvents(exchange);
		String readDispatcherFile = LeapCoreTestFileRead.readDispatcherFile();
		Thread.sleep(100);
		Assert.assertNotNull("File content should not be null ::", readDispatcherFile);
		Assert.assertTrue("file should be contain SystemEvent Failure Id ::",
				readDispatcherFile.contains("SERVICE_COMPLETION_FAILURE"));
	}

	/**
	 * This method is used to store the successful service completion event in the
	 * service context event holder map.
	 */
	@Test
	public void testStoreSuccessServiceCompletionEventIncache() {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		dispatcher.storeSuccessServiceCompletionEventIncache(exchange);
	}

	@AfterClass
	public static void cleanUp() {
		new DeleteConfigNodeForTesting();
	}

	private void setLeapEvent(int value) {
		try {
			IEventFrameworkConfigService eventConfigService = new EventFrameworkConfigService();
			List<SystemEvent> eventList = LeapCoreTestFileRead.getSystemEventFramework();
			List<Event> events = LeapCoreTestFileRead.getEventFramework();
			SystemEvent evt = null;
			Event evet = null;
			LeapEvent leapEvent = null;
			if (value == 0) {
				for (Event event : events) {
					if (event.getId().equalsIgnoreCase("Success_Event_Test")) {
						evet = event;
						break;
					}
				}
				for (SystemEvent sysevent : eventList) {
					if (sysevent.getId().equalsIgnoreCase("SERVICE_COMPLETION_SUCCESS")) {
						evt = sysevent;
						break;
					}
				}
				eventConfigService.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), evt);
				eventConfigService.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), evet);
				leapEvent = new LeapEvent(evet.getId());
			}

			if (value == 1) {
				for (SystemEvent sysevent : eventList) {
					if (sysevent.getId().equalsIgnoreCase("SERVICE_COMPLETION_FAILURE")) {
						evt = sysevent;
						break;
					}
				}
				eventConfigService.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), evt);
				leapEvent = new LeapEvent(evt.getId());
			}

			DispatchChannel fileStoreDis = getDispatchChannel();
			eventConfigService.addEventFrameworkConfiguration(LeapCoreTestUtils.getConfigContext(), fileStoreDis);

			leapEvent.setDispatchChannelId(fileStoreDis.getId());
			leapEvent.addMetadata("EVT_CONTEXT", LeapCoreTestUtils.getRequestContext());
			LeapEventContext.addLeapEvent(leapDataCtx.getServiceDataContext().getRequestUUID(), leapEvent,
					leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE));

		} catch (EventFrameworkConfigurationException e) {
			e.printStackTrace();
		}
	}

	private DispatchChannel getDispatchChannel() {
		List<DispatchChannel> disChannel = LeapCoreTestFileRead.getDispatchChannel();
		DispatchChannel fileStoreDis = null;
		for (DispatchChannel event : disChannel) {
			if (event.getId().equalsIgnoreCase("FILE_STORE")) {
				fileStoreDis = event;
				break;
			}
		}
		return fileStoreDis;
	}

	private List<DefaultMessageHistory> setDefaultMessageHistoryList() {
		DefaultMessageHistory messageHistory = new DefaultMessageHistory("TestId", new NamedNode() {
			public String getShortName() { return "testSortName"; }
			public String getLabel() { return "testLabel"; }
			public String getId() { return "testId"; }
			public String getDescriptionText() { return "testDiscription"; }
		}, 123456789l);

		List<DefaultMessageHistory> list = new ArrayList<DefaultMessageHistory>();
		list.add(messageHistory);
		return list;
	}
}
