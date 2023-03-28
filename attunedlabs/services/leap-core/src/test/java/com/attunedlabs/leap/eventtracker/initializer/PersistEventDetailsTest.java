package com.attunedlabs.leap.eventtracker.initializer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.attunedlabs.LeapCoreTestConstant.*;

import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.configdbtest.DeleteConfigNodeForTesting;
import com.attunedlabs.configdbtest.InsertConfigNodeForTesting;
import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.eventframework.abstractbean.LeapEventContext;
import com.attunedlabs.eventframework.abstractbean.NoDataSourceFoundException;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.leap.LeapDataContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistEventDetailsTest {

	private static Exchange exchange;
	private PersistEventDetails persistEventDetails;
	private LeapDataContext leapDataCtx;
	private IEventDispatcherTrackerService eventDispatcherTrackerService;
	private AbstractMetaModelBean abstractMetaModelBean;

	@BeforeClass
	public static void init() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchangeWithRegistry();

	}

	@Before
	public void setUp() throws EventDispatcherTrackerException {
		new InsertConfigNodeForTesting();
		if (persistEventDetails == null)
			persistEventDetails = new PersistEventDetails();
		if (leapDataCtx == null)
			leapDataCtx = new LeapDataContext();
		if (eventDispatcherTrackerService == null)
			eventDispatcherTrackerService = new EventDispatcherTrackerImpl();
		if (abstractMetaModelBean == null)
			abstractMetaModelBean = new AbstractMetaModelBean() {
				protected void processBean(Exchange exch) throws Exception {
				}
			};
		boolean checkEventDispatcherTracker = CheckEventDispatcherTracker();
		if (checkEventDispatcherTracker)
			eventUpdateAndDelete();

	}

	/**
	 * adding the event details with status NEW and Event_Created_DTM will be
	 * current system time with leapEvent
	 * 
	 * @throws EventDispatcherTrackerException
	 * @throws NoDataSourceFoundException
	 * @throws SQLException
	 */
	@Test
	public void testProcessBeanWithLeapEvent()
			throws EventDispatcherTrackerException, NoDataSourceFoundException, SQLException {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		setLeapEvent();
		Map<String, Object> headers = exchange.getIn().getHeaders();

		persistEventDetails.processBean(exchange);
		connectionCommitAndClose();
		headers = exchange.getIn().getHeaders();
		Assert.assertTrue(headers.containsKey("REQUEST_ID"));

		boolean flag = CheckEventDispatcherTracker();
		Assert.assertTrue("flag Data Should be true ::", flag);

	}

	/**
	 * adding the event details with status NEW and Event_Created_DTM will be
	 * current system time without leapEvent
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testProcessBeanWithoutLeapEvent() throws EventDispatcherTrackerException {
		LeapCoreTestUtils.setServiceContext(leapDataCtx);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		Map<String, Object> headers = exchange.getIn().getHeaders();

		persistEventDetails.processBean(exchange);

//		leapDataCtx.getServiceDataContext()

		headers = exchange.getIn().getHeaders();
		Assert.assertTrue(headers.containsKey("REQUEST_ID"));

		boolean flag = CheckEventDispatcherTracker();
		Assert.assertFalse("flag Data Should be true ::", flag);
	}

	/**
	 * adding the event details with status NEW and Event_Created_DTM will be
	 * current system time without set ServiceDataContext
	 * 
	 * @throws EventDispatcherTrackerException
	 */
	@Test
	public void testProcessBeanWithoutServiceContext() throws EventDispatcherTrackerException {
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataCtx);
		persistEventDetails.processBean(exchange);
		boolean flag = CheckEventDispatcherTracker();
		Assert.assertFalse("flag Data Should be true ::", flag);
	}

	@After
	public void cleanUp() {
		new DeleteConfigNodeForTesting();
	}

	private void setLeapEvent() {
		LeapEvent event = new LeapEvent("testEventId");
		event.setDispatchChannelId("testDispatchChannelId");
		event.addMetadata("EVT_CONTEXT", LeapCoreTestUtils.getRequestContext());
		LeapEventContext.addLeapEvent(leapDataCtx.getServiceDataContext().getRequestUUID(), event,
				leapDataCtx.getServiceDataContext(TEST_TENANT, TEST_SITE));
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

	private void connectionCommitAndClose() throws NoDataSourceFoundException, SQLException {
		abstractMetaModelBean.setDataSource(exchange, "dataSourceSQL");
		JdbcDataContext dataContext = abstractMetaModelBean
				.getTransactionalLocalDataContext(leapDataCtx.getServiceDataContext());
		Connection connection = dataContext.getConnection();

		if (connection != null && !connection.getAutoCommit()) {
			connection.commit();
			connection.close();
		}

	}

}
