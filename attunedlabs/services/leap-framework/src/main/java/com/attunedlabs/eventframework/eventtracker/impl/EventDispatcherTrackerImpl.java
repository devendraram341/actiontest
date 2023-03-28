package com.attunedlabs.eventframework.eventtracker.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.Exchange;
import org.apache.metamodel.BatchUpdateScript;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.util.PooledDataSourceInstance;
import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigHelper;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.retrypolicy.RetryPolicy;

public class EventDispatcherTrackerImpl extends AbstractMetaModelBean
		implements IEventDispatcherTrackerService, Serializable {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(EventDispatcherTrackerImpl.class);
	private static Map<String, Column> columnMap = new ConcurrentHashMap<>();
	private static Table trackerTable;
	private static final String LEAP_DATASOURCE = "dataSourceSQL";

	@Override
	public boolean addEventTracking(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, LeapEvent leapEvent, boolean isInTransaction, Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String methodName = "addEventTracking";
		logger.debug(
				"{} entered into the method {}, tenantId : {}, siteId : {}, requestId : {}, eventStoreKey : {}, leapEvent :{}",
				LEAP_LOG_KEY, methodName, tenantId, siteId, requestId, eventStoreKey, leapEvent);
		JdbcDataContext dataContext;
		Connection connection = null;
		try {
			if (isInTransaction) {
				setDataSource(camelExchange, LEAP_DATASOURCE);
				dataContext = getLocalDataContext(camelExchange);
			} else {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				dataContext.setIsInTransaction(false);
			}
			initializeTableAndColumn(dataContext);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					if (leapEvent != null) {
						final RowInsertionBuilder insert = callback.insertInto(trackerTable);
						logger.trace("{} adding Event With EventId.....: {}", LEAP_LOG_KEY, leapEvent.getId());
						insert.value(columnMap.get(EventTrackerTableConstants.TENANT_ID), tenantId)
								.value(columnMap.get(EventTrackerTableConstants.SITE_ID), siteId)
								.value(columnMap.get(EventTrackerTableConstants.REQUEST_ID), requestId)
								.value(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID), eventStoreKey)
								.value(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM),
										new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
								.value(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID),
										leapEvent.getDispatchChannelId())
								.value(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS),
										EventTrackerTableConstants.STATUS_NEW)
								.value(columnMap.get(EventTrackerTableConstants.RETRY_COUNT), 0)
								.value(columnMap.get(EventTrackerTableConstants.LEAP_EVENT_ID), leapEvent.getId())
								.value(columnMap.get(EventTrackerTableConstants.LEAP_EVENT),
										transformLeapEvent(leapEvent));
						insert.execute();
					} else {
						logger.warn("{} No event to persist in the db", LEAP_LOG_KEY);
					}
				}
			});
			Integer totalInsertedRows = 0;

			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				logger.info("{} added event to tracktable totalrecordsAddded : {}", LEAP_LOG_KEY, totalInsertedRows);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			} else {
				logger.info("{} nothing added to event tracktable..", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return false;
			}
		} catch (Exception e) {
			throw new EventDispatcherTrackerException("Failed to persist event details...!" + e.getMessage(), e);
		} finally {
			if (connection != null)
				PooledDataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public boolean addEventTracking(String tenantId, String siteId, String requestId, String eventStoreKey,
			List<LeapEvent> leapEvents, boolean isInTransaction, Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String methodName = "addEventTracking";
		logger.debug(
				"{} entered into the method {}, tenantId : {}, siteId : {}, requestId : {}, eventStoreKey : {}, leapEvents :{}",
				LEAP_LOG_KEY, methodName, tenantId, siteId, requestId, eventStoreKey, leapEvents);
		JdbcDataContext dataContext;
		Connection connection = null;
		try {
			if (isInTransaction) {
				setDataSource(camelExchange, LEAP_DATASOURCE);
				dataContext = getLocalDataContext(camelExchange);
			} else {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				dataContext.setIsInTransaction(false);
			}
			initializeTableAndColumn(dataContext);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext
					.executeUpdate(new BatchUpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							if (leapEvents != null && !(leapEvents.isEmpty())) {
								for (LeapEvent leapEvent : leapEvents) {
									final RowInsertionBuilder insert = callback.insertInto(trackerTable);
									logger.trace("{} adding Event With EventId.....: {}", LEAP_LOG_KEY,
											leapEvent.getId());
									logger.trace("{} adding Event : {}", LEAP_LOG_KEY, leapEvent);
									insert.value(columnMap.get(EventTrackerTableConstants.TENANT_ID), tenantId)
											.value(columnMap.get(EventTrackerTableConstants.SITE_ID), siteId)
											.value(columnMap.get(EventTrackerTableConstants.REQUEST_ID), requestId)
											.value(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID),
													eventStoreKey)
											.value(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM),
													new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
											.value(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS),
													EventTrackerTableConstants.STATUS_NEW)
											.value(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID),
													leapEvent.getDispatchChannelId())
											.value(columnMap.get(EventTrackerTableConstants.RETRY_COUNT), 0)
											.value(columnMap.get(EventTrackerTableConstants.LEAP_EVENT_ID),
													leapEvent.getId())
											.value(columnMap.get(EventTrackerTableConstants.LEAP_EVENT),
													transformLeapEvent(leapEvent));
									insert.execute();
								}
							} else {
								logger.warn("{} No event to persist in the db", LEAP_LOG_KEY);
							}
						}
					});
			Integer totalInsertedRows = 0;

			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				logger.info("{} added event to tracktable total records Addded : {}", LEAP_LOG_KEY, totalInsertedRows);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			} else {
				logger.info("{} nothing added to event tracktable..", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return false;
			}
		} catch (Exception e) {
			throw new EventDispatcherTrackerException("Failed to persist event details...!" + e.getMessage(), e);
		} finally {
			if (connection != null)
				PooledDataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public boolean updateEventStatus(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, final String processingStatus, final String leapEventId,
			final boolean isFailure, final String failureMsg, final boolean isFailedNow, final boolean isRetried,
			String dispatchChannelId) throws EventDispatcherTrackerException {
		String methodName = "updateEventStatus";
		logger.debug("{} entered into the method {},processingStatus :{}, isFailure : {}", LEAP_LOG_KEY, methodName,
				processingStatus, isFailure);
		logger.debug("{} leapEvent : {}: dispatchChannelId :{}", LEAP_LOG_KEY, leapEventId, dispatchChannelId);
		int totalRowsUpdated = 0;
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {

				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(trackerTable);
					int retryCount = 0;
					try {
						retryCount = getRetryCount(dataContext, tenantId, siteId, requestId, eventStoreKey, leapEventId,
								dispatchChannelId);
					} catch (EventDispatcherTrackerException e) {
						logger.error("{} some error couured while fetchingRetry Count..{}", LEAP_LOG_KEY,
								e.getMessage());
					}

					update.where(columnMap.get(EventTrackerTableConstants.TENANT_ID)).eq(tenantId)
							.where(columnMap.get(EventTrackerTableConstants.SITE_ID)).eq(siteId)
							.where(columnMap.get(EventTrackerTableConstants.REQUEST_ID)).eq(requestId)
							.where(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID)).eq(eventStoreKey)
							.value(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS), processingStatus)
							.where(columnMap.get(EventTrackerTableConstants.LEAP_EVENT_ID)).eq(leapEventId)
							.where(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID)).eq(dispatchChannelId);

					// execution of following if will update failure and retry
					// count
					if (isFailure || isRetried) {
						if (isFailedNow)
							update.value(columnMap.get(EventTrackerTableConstants.LAST_FAILURE_DTM),
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

						if (failureMsg != null)
							update.value(columnMap.get(EventTrackerTableConstants.FAILURE_REASON), failureMsg);

						if (isRetried)
							update.value(columnMap.get(EventTrackerTableConstants.RETRY_COUNT), ++retryCount);
					}
					update.execute();
				}
			});

			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.info("{} total updated rows in event tracker table: {} ", LEAP_LOG_KEY, totalRowsUpdated);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return totalRowsUpdated > 0;
			} else {
				logger.info("{} updated rows in event tracker table: {}", LEAP_LOG_KEY, totalRowsUpdated);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return false;
			}
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to update status for events configured to requestId : " + requestId + "...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public Integer getRetryCount(final DataContext context, final String tenantId, final String siteId,
			final String requestId, final String eventStoreKey, String leapEventId, String dispatchChannelId)
			throws EventDispatcherTrackerException {
		String methodName = "getRetryCount";
		logger.debug("{} entered into the method {},tenantId :{}, siteId :{}, requestId :{}, eventStoreKey :{} ",
				LEAP_LOG_KEY, methodName, tenantId, siteId, requestId, eventStoreKey);

		Integer retryCount = 0;
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			if (context != null && context instanceof JdbcDataContext)
				dataContext = (JdbcDataContext) context;
			else {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			}

			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable)
					.select(columnMap.get(EventTrackerTableConstants.RETRY_COUNT))
					.where(columnMap.get(EventTrackerTableConstants.TENANT_ID)).eq(tenantId)
					.where(columnMap.get(EventTrackerTableConstants.SITE_ID)).eq(siteId)
					.where(columnMap.get(EventTrackerTableConstants.REQUEST_ID)).eq(requestId)
					.where(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID)).eq(eventStoreKey)
					.where(columnMap.get(EventTrackerTableConstants.LEAP_EVENT_ID)).eq(leapEventId)
					.where(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID)).eq(dispatchChannelId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				retryCount = (Integer) row
						.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.RETRY_COUNT));
				if (retryCount == null)
					retryCount = 0;
				logger.debug("{} current retry count : {}", LEAP_LOG_KEY, retryCount);
			} else
				logger.debug("{} first time to be retried count will be set to 1...", LEAP_LOG_KEY);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get retryCount for events configured to requestId : " + requestId + "...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return retryCount;
	}

	@Override
	public String getTrackStatusForEventList(String tenantId, String siteId, String requestId, String eventStoreKey,
			String dispatchChannelId, Exchange camelExchange) throws EventDispatcherTrackerException {
		String methodName = "getTrackStatusForEventList";
		logger.debug("{} entered into the method {},tenantId :{}, siteId :{}, requestId :{}, eventStoreKey :{}",
				LEAP_LOG_KEY, methodName, tenantId, siteId, requestId, eventStoreKey);
		String processingStatus = null;
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable)
					.select(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
					.where(columnMap.get(EventTrackerTableConstants.TENANT_ID)).eq(tenantId)
					.where(columnMap.get(EventTrackerTableConstants.SITE_ID)).eq(siteId)
					.where(columnMap.get(EventTrackerTableConstants.REQUEST_ID)).eq(requestId)
					.where(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID)).eq(eventStoreKey)
					.where(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID)).eq(dispatchChannelId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				processingStatus = (String) row
						.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.PROCESSING_STATUS));
				logger.info("{} processing status for events configured  is {}", LEAP_LOG_KEY, processingStatus);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return processingStatus;
			} else
				logger.info("{} No processing status present for events configured", LEAP_LOG_KEY);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get status for events configured to requestId : " + requestId + "...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return processingStatus;
	}

	@Override
	public boolean removeEventTrackRecord(final String tenantId, final String siteId, final String requestId,
			final String eventStoreKey, String dispatchChannelId, String leapEventId)
			throws EventDispatcherTrackerException {
		String methodName = "removeEventTrackRecord";
		logger.debug("{} entered into the method {}, tenantId :{}, siteId :{}, requestId :{}, eventStoreKey ",
				LEAP_LOG_KEY, methodName, tenantId, siteId, requestId, eventStoreKey);

		int totalRowsDeleted = 0;
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DefaultUpdateSummary eventTrackerDelete = (DefaultUpdateSummary) dataContext
					.executeUpdate(new UpdateScript() {
						@Override
						public void run(UpdateCallback callback) {
							final RowDeletionBuilder delete = callback.deleteFrom(trackerTable);
							delete.where(columnMap.get(EventTrackerTableConstants.TENANT_ID)).eq(tenantId)
									.where(columnMap.get(EventTrackerTableConstants.SITE_ID)).eq(siteId)
									.where(columnMap.get(EventTrackerTableConstants.REQUEST_ID)).eq(requestId)
									.where(columnMap.get(EventTrackerTableConstants.EVENT_STORE_ID)).eq(eventStoreKey)
									.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
									.eq(EventTrackerTableConstants.STATUS_COMPLETE)
									.where(columnMap.get(EventTrackerTableConstants.LEAP_EVENT_ID)).eq(leapEventId)
									.where(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID))
									.eq(dispatchChannelId);
							delete.execute();
						}
					});
			if (eventTrackerDelete.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) eventTrackerDelete.getDeletedRows().get();
				logger.info("{} track record deleted successfully ,total records deleted {}", LEAP_LOG_KEY,
						totalRowsDeleted);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return totalRowsDeleted > 0;
			} else {
				logger.info("{} track record not present or else already deleted!", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return false;
			}
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to delete track record configured for requestId : " + requestId + "...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecords(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String methodName = "getAllTrackRecords";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);

			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking {}", LEAP_LOG_KEY, eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsOnStatus(Exchange camelExchange, String processingStatus)
			throws EventDispatcherTrackerException {
		String methodName = "getAllTrackRecordsOnStatus";
		logger.debug("{} entered into the method {}, processingStatus :{}", LEAP_LOG_KEY, methodName, processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			if (validateProcessingStatus(processingStatus)) {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				initializeTableAndColumn(dataContext);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS)).eq(processingStatus)
						.execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("{} retrieved list of tracking for processingStatus : {} -> {}", LEAP_LOG_KEY,
						processingStatus, eventTrackingList);
			} else
				logger.warn(
						"{} processingStatus : {} dosen't match should be either NEW, IN_PRCESS, RETRY, RETRY_INPROCESS, FAILED, COMPLETE",
						LEAP_LOG_KEY, processingStatus);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	/**
	 * calcuation done based on the getting the top records on policy
	 * maxRetryRecords.
	 * 
	 * @param eventTrackingList
	 * @return topFilterList
	 */
	@SuppressWarnings("unused")
	private List<EventDispatcherTracker> getTopRecords(List<EventDispatcherTracker> eventTrackingList) {
		int topRecords = RetryPolicy.getMaxRetryRecordsCount();
		if (topRecords == -1)
			return eventTrackingList;
		int maxSize = eventTrackingList.size();
		if (topRecords > maxSize)
			topRecords = maxSize - 1;
		try {
			return eventTrackingList.subList(0, topRecords + 1);
		} catch (Exception e) {
			return eventTrackingList;
		}
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsIntitializedForLongTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String processingStatus = EventTrackerTableConstants.STATUS_NEW;
		String methodName = "getAllTrackRecordsIntitializedForLongTime";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).lessThanOrEquals(pastTime)
					.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS)).eq(processingStatus)
					.orderBy(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking for processingStatus : {}  for long time -> {}", LEAP_LOG_KEY,
					processingStatus, eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords which are intitialized for long time!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTime(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException {
		String methodName = "getAllTrackRecordsInProcessForLongTime";
		logger.debug("{} entered into the method {}, processingStatus :{}", LEAP_LOG_KEY, methodName, processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			if (validateInProcessProcessingStatus(processingStatus)) {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
				initializeTableAndColumn(dataContext);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).lessThanOrEquals(pastTime)
						.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS)).eq(processingStatus)
						.orderBy(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).asc().execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("{} retrieved list of tracking for processingStatus : {} -> {}", LEAP_LOG_KEY,
						processingStatus, eventTrackingList);
			} else
				logger.warn("{} processingStatus : {}  dosen't match should be either  IN_PROCESS, RETRY_INPROCESS",
						LEAP_LOG_KEY, processingStatus);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllTrackRecordsInProcessForLongTimeArrangedByRetry(Exchange camelExchange,
			String processingStatus) throws EventDispatcherTrackerException {
		String methodName = "getAllTrackRecordsInProcessForLongTimeArrangedByRetry";
		logger.debug("{} entered into the method {}, processingStatus :{}", LEAP_LOG_KEY, methodName, processingStatus);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			if (validateInProcessProcessingStatus(processingStatus)) {
				connection = PooledDataSourceInstance.getConnection();
				dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				// setting transaction as false because update will not be in
				// transaction called from notifier.
				dataContext.setIsInTransaction(false);
				Date pastTime = getPreviousDateInstance(RetryPolicy.getNormalRetryInterval());
				initializeTableAndColumn(dataContext);
				DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
						.limit(RetryPolicy.getMaxRetryRecordsCount())
						.where(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).lessThanOrEquals(pastTime)
						.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS)).eq(processingStatus)
						.orderBy(columnMap.get(EventTrackerTableConstants.RETRY_COUNT)).asc()
						.orderBy(columnMap.get(EventTrackerTableConstants.EVENT_CREATED_DTM)).asc().execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
				}
				logger.debug("{} retrieved list of tracking for processingStatus : {} -> {} ", LEAP_LOG_KEY,
						processingStatus, eventTrackingList);
			} else
				logger.warn("{} processingStatus : {}  dosen't match should be either  IN_PROCESS, RETRY_INPROCESS",
						LEAP_LOG_KEY, processingStatus);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String methodName = "getAllFailedEventRecordsArrangedByFailureTime";
		logger.debug("{} entered into the method {}, processingStatus : FAILED", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
					.eq(EventTrackerTableConstants.STATUS_FAILED)
					.orderBy(columnMap.get(EventTrackerTableConstants.RETRY_COUNT)).asc()
					.orderBy(columnMap.get(EventTrackerTableConstants.LAST_FAILURE_DTM)).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				logger.debug("{} dataset row : {}", LEAP_LOG_KEY, Arrays.toString(row.getValues()));
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking for processingStatus :  FAILED -> {}", LEAP_LOG_KEY,
					eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException {
		String methodName = "getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount";
		logger.debug("{} entered into the method {}, processingStatus : FAILED", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
					.eq(EventTrackerTableConstants.STATUS_FAILED)
					.orderBy(columnMap.get(EventTrackerTableConstants.RETRY_COUNT)).asc()
					.orderBy(columnMap.get(EventTrackerTableConstants.LAST_FAILURE_DTM)).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking for processingStatus :  FAILED -> {}", LEAP_LOG_KEY,
					eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTime(Exchange camelExchange)
			throws EventDispatcherTrackerException {
		String methodName = "getAllRetryFailedEventRecordsArrangedByFailureTime";
		logger.debug("{} entered into the method {}, processingStatus : RETRY_FAILED", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount()).limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
					.eq(EventTrackerTableConstants.STATUS_RETRY_FAILED)
					.orderBy(columnMap.get(EventTrackerTableConstants.LAST_FAILURE_DTM)).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking for processingStatus : RETRY_FAILED -> {} ", LEAP_LOG_KEY,
					eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		return eventTrackingList;
	}

	@Override
	public List<EventDispatcherTracker> getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange) throws EventDispatcherTrackerException {

		String methodName = "#";
		logger.debug("{} entered into the method {}, processingStatus : RETRY_FAILED ", LEAP_LOG_KEY, methodName);
		List<EventDispatcherTracker> eventTrackingList = new ArrayList<EventDispatcherTracker>();
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.limit(RetryPolicy.getMaxRetryRecordsCount())
					.where(columnMap.get(EventTrackerTableConstants.PROCESSING_STATUS))
					.eq(EventTrackerTableConstants.STATUS_RETRY_FAILED)
					.orderBy(columnMap.get(EventTrackerTableConstants.RETRY_COUNT)).asc()
					.orderBy(columnMap.get(EventTrackerTableConstants.LAST_FAILURE_DTM)).asc().execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventDispatcherTracker eventDispatcherTracker = new EventDispatcherTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row));
			}
			logger.debug("{} retrieved list of tracking for processingStatus : RETRY_FAILED -> {} ", LEAP_LOG_KEY,
					eventTrackingList);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get list of all the EventTrackingRecords available...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public EventDispatcherTracker getEventRecordOnRequestUUID(Exchange camelExchange, String requestUUID,
			String dispatchChannelId) throws EventDispatcherTrackerException {
		String methodName = "getEventRecordOnRequestUUID";
		logger.debug("{} entered into the method {}, requestUUID :{}", LEAP_LOG_KEY, methodName, requestUUID);
		Connection connection = null;
		JdbcDataContext dataContext;
		try {
			connection = PooledDataSourceInstance.getConnection();
			dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			// setting transaction as false because update will not be in
			// transaction called from notifier.
			dataContext.setIsInTransaction(false);
			initializeTableAndColumn(dataContext);
			DataSet dataSet = dataContext.query().from(trackerTable).selectAll()
					.where(columnMap.get(EventTrackerTableConstants.REQUEST_ID)).eq(requestUUID)
					.where(columnMap.get(EventTrackerTableConstants.DISPATCH_CHANNEL_ID)).eq(dispatchChannelId)
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			EventDispatcherTracker eventDispatcherTracker = null;
			if (itr.hasNext()) {
				Row row = itr.next();
				eventDispatcherTracker = new EventDispatcherTracker();
				parseRowToEventDispatcherTracker(eventDispatcherTracker, trackerTable, row);
				logger.debug("{} retrieved eventDispatcherTracker for requestId : {} -> {}", LEAP_LOG_KEY, requestUUID,
						eventDispatcherTracker);
				return eventDispatcherTracker;
			}
			logger.debug("{} retrieved eventDispatcherTracker for requestId : {} -> {}", LEAP_LOG_KEY, requestUUID,
					eventDispatcherTracker);
			return null;
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(
					"Failed to get eventDispatcherTracker for given requestUUID " + requestUUID + "...!", e);
		} finally {
			PooledDataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	protected void processBean(Exchange exch) throws Exception {
	}

	/**
	 * returning the instance of Date but instance will contain specified interval
	 * before the current Date instance of system.
	 * 
	 * @param timeIntervalBefore
	 * @return date instance
	 */
	private Date getPreviousDateInstance(int timeIntervalBefore) {
		long currentTime = System.currentTimeMillis();
		long specifiedMinutesBeforeTime = 0;
		switch (RetryPolicy.getNormalTimeIntervalUnit().toUpperCase()) {
		case RetryPolicy.TIMEUNIT_HOURS:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 3600) * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_MINUTES:
			specifiedMinutesBeforeTime = currentTime - (timeIntervalBefore * 60) * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_SECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore * 1000 + 0;
			break;
		case RetryPolicy.TIMEUNIT_MILLSECONDS:
			specifiedMinutesBeforeTime = currentTime - timeIntervalBefore;
			break;
		default:
			// default will be considered in minutes.
			specifiedMinutesBeforeTime = currentTime - (0 + timeIntervalBefore * 60 + 0) * 1000 + 0;
			break;
		}

		Date calculatedMinutesBeforeDate = new Date(specifiedMinutesBeforeTime);
		return calculatedMinutesBeforeDate;
	}

	/**
	 * utility for parsing the row retrieved from dataset to EventDispatcherTracker.
	 * 
	 * @param eventDispatcherTracker
	 * @param trackerTable
	 * @param row
	 * @return
	 */
	private EventDispatcherTracker parseRowToEventDispatcherTracker(EventDispatcherTracker eventDispatcherTracker,
			Table trackerTable, Row row) {
		eventDispatcherTracker
				.setTenantId((String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.TENANT_ID)));
		eventDispatcherTracker
				.setSiteId((String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.SITE_ID)));
		eventDispatcherTracker.setRequestId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.REQUEST_ID)));
		eventDispatcherTracker.setEventStoreId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.REQUEST_ID)));
		eventDispatcherTracker.setEventCreatedDTM((java.util.Date) row
				.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.EVENT_CREATED_DTM)));
		eventDispatcherTracker.setLastFailureDTM((java.util.Date) row
				.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.LAST_FAILURE_DTM)));
		eventDispatcherTracker.setStatus(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.PROCESSING_STATUS)));
		eventDispatcherTracker.setFailureReason(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.FAILURE_REASON)));
		eventDispatcherTracker.setRetryCount(
				(Integer) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.RETRY_COUNT)));
		eventDispatcherTracker.setLeapEventId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.LEAP_EVENT_ID)));
		eventDispatcherTracker.setLeapEvent(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.LEAP_EVENT)));
		eventDispatcherTracker.setDispatchChannelId(
				(String) row.getValue(trackerTable.getColumnByName(EventTrackerTableConstants.DISPATCH_CHANNEL_ID)));
		return eventDispatcherTracker;
	}

	/**
	 * validating weather processing status belongs the status registered.
	 * 
	 * @param processingStatus
	 * @return validationStatus
	 */
	private boolean validateProcessingStatus(String processingStatus) {
		List<String> processingStatusList = new ArrayList<>();
		processingStatusList.add(EventTrackerTableConstants.STATUS_NEW);
		processingStatusList.add(EventTrackerTableConstants.STATUS_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_FAILED);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_FAILED);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_COMPLETE);
		for (String status : processingStatusList) {
			if (status.equalsIgnoreCase(processingStatus))
				return true;
		}
		return false;
	}

	/**
	 * validating weather processing status belongs the status registered.
	 * 
	 * @param processingStatus
	 * @return validationStatus
	 */
	private boolean validateInProcessProcessingStatus(String processingStatus) {
		List<String> processingStatusList = new ArrayList<>();
		processingStatusList.add(EventTrackerTableConstants.STATUS_IN_PROCESS);
		processingStatusList.add(EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS);
		for (String status : processingStatusList) {
			if (status.equalsIgnoreCase(processingStatus))
				return true;
		}
		return false;
	}

	private void initializeTableAndColumn(JdbcDataContext dataContext) {
		if (trackerTable == null) {
			trackerTable = dataContext
					.getTableByQualifiedLabel(EventTrackerTableConstants.EVENT_DISPATCH_TRACKER_TABLE);
			intializeColumnMap(trackerTable);
		}
		if (columnMap == null || columnMap.isEmpty())
			intializeColumnMap(trackerTable);
	}

	private void intializeColumnMap(Table trackerTable) {
		columnMap.put(EventTrackerTableConstants.TENANT_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.TENANT_ID));
		columnMap.put(EventTrackerTableConstants.SITE_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.SITE_ID));
		columnMap.put(EventTrackerTableConstants.LEAP_EVENT_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.LEAP_EVENT_ID));
		columnMap.put(EventTrackerTableConstants.REQUEST_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.REQUEST_ID));
		columnMap.put(EventTrackerTableConstants.LEAP_EVENT,
				trackerTable.getColumnByName(EventTrackerTableConstants.LEAP_EVENT));
		columnMap.put(EventTrackerTableConstants.EVENT_STORE_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.EVENT_STORE_ID));
		columnMap.put(EventTrackerTableConstants.EVENT_CREATED_DTM,
				trackerTable.getColumnByName(EventTrackerTableConstants.EVENT_CREATED_DTM));
		columnMap.put(EventTrackerTableConstants.LAST_FAILURE_DTM,
				trackerTable.getColumnByName(EventTrackerTableConstants.LAST_FAILURE_DTM));
		columnMap.put(EventTrackerTableConstants.PROCESSING_STATUS,
				trackerTable.getColumnByName(EventTrackerTableConstants.PROCESSING_STATUS));
		columnMap.put(EventTrackerTableConstants.FAILURE_REASON,
				trackerTable.getColumnByName(EventTrackerTableConstants.FAILURE_REASON));
		columnMap.put(EventTrackerTableConstants.RETRY_COUNT,
				trackerTable.getColumnByName(EventTrackerTableConstants.RETRY_COUNT));
		columnMap.put(EventTrackerTableConstants.DISPATCH_CHANNEL_ID,
				trackerTable.getColumnByName(EventTrackerTableConstants.DISPATCH_CHANNEL_ID));
	}

	private JSONObject transformLeapEvent(LeapEvent leapevent) {
		String methodName = "transformLeapEvent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject eventJson = new JSONObject();
		try {
			eventJson.put(EventFrameworkConstants.OBJECT_KEY, setEventParam(leapevent));
			eventJson.put(EventFrameworkConstants.METADATA_KEY, setEventHeader(leapevent));
			eventJson.put(EventTrackerTableConstants.EVENT_ID, leapevent.getId());
			EventFrameworkConfigHelper.removeReqContextFromLeapEvent(eventJson, leapevent);
			EventFrameworkConfigHelper.formattingEventStructure(eventJson);
		} catch (JSONException e) {
			logger.error("{} Failed to convert leapEvent to Json. {}", LEAP_LOG_KEY, e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventJson;
	}

	private JSONObject setEventParam(LeapEvent leapevent) throws JSONException {
		JSONObject paramJson = new JSONObject();
		Map<String, Serializable> eventParamMap = leapevent.getObject();
		for (String key : eventParamMap.keySet())
			paramJson.put(key, eventParamMap.get(key));
		return paramJson;
	}

	private JSONObject setEventHeader(LeapEvent leapevent) throws JSONException {
		JSONObject headerJson = new JSONObject();
		Map<String, Serializable> eventHeaderMap = leapevent.getMetadata();
		for (String key : eventHeaderMap.keySet()) {
			if (key.equals(ConfigurationConstant.EVENT_CONTEXT_KEY))
				headerJson.put(key, getRequestContextJson(eventHeaderMap.get(key)));
			else
				headerJson.put(key, eventHeaderMap.get(key));
		}
		return headerJson;
	}

	private JSONObject getRequestContextJson(Serializable serializable) throws JSONException {
		if (serializable != null && serializable instanceof RequestContext) {
			RequestContext context = (RequestContext) serializable;
			JSONObject object = new JSONObject();
			object.put(ConfigurationConstant.TENANT_ID, context.getTenantId());
			object.put(ConfigurationConstant.SITE_ID, context.getSiteId());
			object.put(ConfigurationConstant.FEATURE_GROUP, context.getFeatureGroup());
			object.put(ConfigurationConstant.FEATURE_NAME, context.getFeatureName());
			object.put(ConfigurationConstant.IMPLEMENTATION_NAME, context.getImplementationName());
			object.put(ConfigurationConstant.VENDOR, context.getVendor());
			object.put(ConfigurationConstant.VERSION, context.getVersion());
			object.put(ConfigurationConstant.REQUEST_ID, context.getRequestId());
			object.put(ConfigurationConstant.PROVIDER, context.getProvider());
			return object;
		}
		return null;
	}

}
