package com.attunedlabs.eventsubscriptiontracker.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.CompiledQuery;
import org.apache.metamodel.query.QueryParameter;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.JMSLeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.exception.EventSubscriptionTrackerException;
import com.attunedlabs.eventsubscription.exception.RetryableException;
import com.attunedlabs.eventsubscription.exception.SubscriptionTableExistenceException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;
import com.attunedlabs.eventsubscriptiontracker.IEventSubscriptionTrackerService;

/**
 * @author bizruntime44
 *
 */
public class JMSEventSubscriptionTrackerImpl implements IEventSubscriptionTrackerService {

	final Logger logger = LoggerFactory.getLogger(JMSEventSubscriptionTrackerImpl.class);
	private Map<String, Column> tableColumnMap = new ConcurrentHashMap<>();
	private Table subscriptionTable;

	@Override
	public boolean createTrackerTableForSubscription() throws SubscriptionTableExistenceException {
		String methodName = "createTrackerTableForSubscription";
		logger.debug("{} entered into the method {} ", LEAP_LOG_KEY, methodName);
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			final Schema tableSchema = dataContext.getDefaultSchema();

			dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					logger.trace("{} creating tracker table for subscription for jmseventsubscription....",
							LEAP_LOG_KEY);
					final TableCreationBuilder createSuccessTable = callback.createTable(tableSchema,
							EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
					createSuccessTable.withColumn(EventSubscriptionTrackerConstants.TENANT_ID_COL)
							.ofType(ColumnType.VARCHAR).ofSize(100)
							.withColumn(EventSubscriptionTrackerConstants.SITE_ID_COL).ofType(ColumnType.VARCHAR)
							.ofSize(100).withColumn(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)
							.ofType(ColumnType.VARCHAR).ofSize(100)
							.withColumn(EventSubscriptionTrackerConstants.TOPIC_COL).ofType(ColumnType.VARCHAR)
							.ofSize(45).withColumn(EventSubscriptionTrackerConstants.PARTITION_COL)
							.ofType(ColumnType.VARCHAR).ofSize(50)
							.withColumn(EventSubscriptionTrackerConstants.OFFEST_COL).ofType(ColumnType.VARCHAR)
							.ofSize(50).withColumn(EventSubscriptionTrackerConstants.EVENT_DATA_COL)
							.ofType(ColumnType.VARCHAR).ofSize(15000)
							.withColumn(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)
							.ofType(ColumnType.TIMESTAMP)
							.withColumn(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)
							.ofType(ColumnType.TIMESTAMP).withColumn(EventSubscriptionTrackerConstants.FAILURE_MSG_COL)
							.ofType(ColumnType.VARCHAR).ofSize(500)
							.withColumn(EventSubscriptionTrackerConstants.TRACK_STATUS).ofType(ColumnType.VARCHAR)
							.ofSize(45).withColumn(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)
							.ofType(ColumnType.BOOLEAN).withColumn(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)
							.ofType(ColumnType.INTEGER).execute();

				}
			});

		} catch (Exception e) {
			if (!e.getMessage().contains("already exists"))
				throw new SubscriptionTableExistenceException(
						"Failed to create  EventSubscriptionTracker table ..." + e.getMessage());
			else
				logger.info("{} EventSubscriptionTracker table already exist's!", LEAP_LOG_KEY);

		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	@Override
	public boolean recordIsNotAlreadyPresent(Exchange exchange, Map<String, Object> metaData) {
		String methodName = "recordIsNotAlreadyPresent";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) metaData
				.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
		final EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) metaData
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);
		setSubscriptionDetailsFromConfig(abstractRetryStrategyBean, eventSubscriptionTracker);

		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			if (subscriptionTable == null)
				subscriptionTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);
			QueryParameter queryParameter = new QueryParameter();
			CompiledQuery compiledQuery = dataContext.query().from(subscriptionTable).selectAll()
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(queryParameter)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(queryParameter)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)).eq(queryParameter)
					// .where(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL)).eq(queryParameter)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL)).eq(queryParameter)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL)).eq(queryParameter)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(queryParameter)
					.or(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(queryParameter)
					.compile();

			Iterator<Row> itr = dataContext.executeQuery(compiledQuery,
					new Object[] { eventSubscriptionTracker.getTenantId(), eventSubscriptionTracker.getSiteId(),
							eventSubscriptionTracker.getSubscriptionId(), eventSubscriptionTracker.getPartition(),
							eventSubscriptionTracker.getOffset(), EventSubscriptionTrackerConstants.STATUS_FAILED,
							EventSubscriptionTrackerConstants.STATUS_RETRY_FAILED })
					.iterator();
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return !itr.hasNext();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} failed to identify record to the EventSubscriptionTracker table ...{}", LEAP_LOG_KEY,
					e.getMessage());
			return false;
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
	}

	@Override
	public boolean addNewSubscriptionRecord(Exchange exchange, Map<String, Object> recordsDetails) {
		String methodName = "addNewSubscriptionRecord";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) recordsDetails
				.get(SubscriptionConstant.RETRY_STRATEGY_CLASS);
		final EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) recordsDetails
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		setSubscriptionDetailsFromConfig(abstractRetryStrategyBean, eventSubscriptionTracker);

		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			if (subscriptionTable == null)
				subscriptionTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(subscriptionTable);
					insert.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL),
							eventSubscriptionTracker.getTenantId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL),
									eventSubscriptionTracker.getSiteId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL),
									eventSubscriptionTracker.getSubscriptionId())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL),
									eventSubscriptionTracker.getTopic())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL),
									eventSubscriptionTracker.getPartition())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL),
									eventSubscriptionTracker.getOffset())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_DATA_COL),
									eventSubscriptionTracker.getEventData())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL),
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS),
									EventSubscriptionTrackerConstants.STATUS_FAILED)
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
									eventSubscriptionTracker.getIsRetryable())
							.value(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL), 0);

					insert.execute();
				}
			});
			Integer totalInsertedRows = 0;
			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				logger.info("{} total added rows to EventSubscriptionTracker: {}", LEAP_LOG_KEY, totalInsertedRows);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return totalInsertedRows > 0;
			} else {
				logger.info("{} nothing added to EventSubscriptionTracker..", LEAP_LOG_KEY);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} failed to add record to the EventSubscriptionTracker table ...{}", LEAP_LOG_KEY,
					e.getMessage());
			return false;
		} finally {
			DataSourceInstance.closeConnection(connection);

		}

	}

	@Override
	public boolean updateSubscriptionRecordStatus(Exchange exchange, Map<String, Object> recordsDetails,
			String trackStatus, Exception exception, JSONObject retryConfigurationJSON) {
		String methodName = "updateSubscriptionRecordStatus";
		logger.debug("{} entered into the method {}, retryConfigJson : {}", LEAP_LOG_KEY, methodName,
				retryConfigurationJSON);
		Integer totalRowsUpdated = 0;

		final EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) recordsDetails
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		final Boolean isRetryTriggered = (Boolean) recordsDetails
				.get(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY);
		logger.debug("{} isRetryTriggered {}", LEAP_LOG_KEY, isRetryTriggered);

		if (exception != null && eventSubscriptionTracker.getIsRetryable())
			eventSubscriptionTracker.setIsRetryable(exception instanceof RetryableException);

		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			if (subscriptionTable == null)
				subscriptionTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(subscriptionTable);
					int retryCount = 0;
					update.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL))
							.eq(eventSubscriptionTracker.getSubscriptionId())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL))
							.eq(eventSubscriptionTracker.getOffset())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL))
							.eq(eventSubscriptionTracker.getPartition());

					update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS), trackStatus).value(
							tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
							eventSubscriptionTracker.getIsRetryable());

//					update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL), trackStatus).value(
//							tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL),
//							eventSubscriptionTracker.getTopic());

					if (exception != null) {
						update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL),
								new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
								.value(tableColumnMap.get(EventSubscriptionTrackerConstants.FAILURE_MSG_COL),
										exception.getMessage());
					}

					if (trackStatus.equals(EventSubscriptionTrackerConstants.STATUS_COMPLETE) && isRetryTriggered) {
						retryCount = eventSubscriptionTracker.getRetryCount();
						update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL),
								++retryCount);
					}

					if (exception != null && eventSubscriptionTracker.getIsRetryable()) {
						logger.trace(
								"{} inside the exception not null isRetryTriggered in JMSEventSubscribtionTrackerImpl {}",
								LEAP_LOG_KEY, isRetryTriggered);
						retryCount = eventSubscriptionTracker.getRetryCount();
						logger.debug(
								"{} inside the exception not null retryCount in JMSEventSubscribtionTrackerImpl {}",
								LEAP_LOG_KEY, retryCount);
						if (isRetryTriggered) {
							update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL),
									++retryCount);

							eventSubscriptionTracker.setIsRetryable(
									!maxRetryExceeded(retryConfigurationJSON, eventSubscriptionTracker));
							update.value(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL),
									eventSubscriptionTracker.getIsRetryable());
							logger.debug(
									"{} record got updated to retry-failed :{},retry count is : {},is retryable: {} ",
									LEAP_LOG_KEY, eventSubscriptionTracker.getOffset(),
									eventSubscriptionTracker.getRetryCount(),
									eventSubscriptionTracker.getIsRetryable());
						}
					}
					update.execute();
				}
			});

			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.info("{} total updated rows in EventSubscriptionTracker table: {}", LEAP_LOG_KEY,
						totalRowsUpdated);
				deleteSuccessRecords(trackStatus, dataContext, eventSubscriptionTracker);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return totalRowsUpdated > 0;
			} else
				logger.info("{} updated rows in EventSubscriptionTracker table: {}", LEAP_LOG_KEY, totalRowsUpdated);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} failed to update record in EventSubscriptionTracker table {}", LEAP_LOG_KEY,
					e.getMessage());
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;
	}

	private void deleteSuccessRecords(String trackStatus, UpdateableDataContext dataContext,
			EventSubscriptionTracker eventSubscriptionTracker) {
		if (trackStatus.equals(EventSubscriptionTrackerConstants.STATUS_COMPLETE)) {
			logger.debug("{} {}", LEAP_LOG_KEY, eventSubscriptionTracker.toString());
			logger.info("since the record is sucessfully processed. we are deleting the record from table {}",
					LEAP_LOG_KEY);
			dataContext.executeUpdate(new UpdateScript() {

				@Override
				public void run(UpdateCallback callback) {
					if (subscriptionTable == null)
						subscriptionTable = dataContext.getTableByQualifiedLabel(
								EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
					callback.deleteFrom(subscriptionTable)
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL))
							.eq(eventSubscriptionTracker.getSubscriptionId())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL))
							.eq(eventSubscriptionTracker.getPartition())
							.where(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL))
							.eq(eventSubscriptionTracker.getOffset()).execute();
				}
			});
		}
	}

	@Override
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsIntitializedForLongTime(Exchange camelExchange,
			String tenantId, String siteId, String subscriptionId, JSONObject retryConfigurationJSON)
			throws EventSubscriptionTrackerException {
		String trackStatus = EventSubscriptionTrackerConstants.STATUS_NEW;
		String methodName = "getAllSubscriptionRecordsIntitializedForLongTime";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.debug("inside getAllSubscriptionRecordsIntitializedForLongTime() of JMSEventSubscriberImpl class..");
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			Date pastTime = SubscriptionUtil.getPreviousDateInstance(retryConfigurationJSON);
			if (subscriptionTable == null)
				subscriptionTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);

			DataSet dataSet = dataContext.query().from(subscriptionTable).selectAll()
					.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)).eq(subscriptionId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL))
					.lessThanOrEquals(pastTime)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(trackStatus)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)).asc()
					.execute();

			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
			}
			logger.info("{} retrieved list of tracking for processingStatus : {} for long time -> {}", LEAP_LOG_KEY,
					trackStatus, eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the EventSubscriptionTrackingRecords which are intitialized for long time!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventSubscriptionTracker> getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String processingStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException {
		// Auto-generated method stub
		String methodName = "getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry";
		logger.debug("{} entered into the method {}, processingStatus :{}", LEAP_LOG_KEY, methodName, processingStatus);
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			if (SubscriptionUtil.validateInProcessProcessingStatus(processingStatus)) {
				connection = DataSourceInstance.getConnection();
				JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
				dataContext.setIsInTransaction(false);
				Date pastTime = SubscriptionUtil.getPreviousDateInstance(retryConfigurationJSON);
				if (subscriptionTable == null)
					subscriptionTable = dataContext
							.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
				initializeSubscriptionTableColumnMap(subscriptionTable);
				DataSet dataSet = dataContext.query().from(subscriptionTable).selectAll()
						.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL))
						.eq(subscriptionId)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL))
						.lessThanOrEquals(pastTime)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(processingStatus)
						.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
						.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)).asc()
						.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)).asc()
						.execute();
				Iterator<Row> itr = dataSet.iterator();
				while (itr.hasNext()) {
					EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
					Row row = itr.next();
					eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
				}
				logger.info(
						"{} retrieved list of tracking for processingStatus of JMSEventSubscriberImpl class : {} -> {}",
						LEAP_LOG_KEY, processingStatus, eventTrackingList);
			} else
				logger.warn("{} processingStatus : {}, dosen't match should be either  IN_PROCESS, RETRY_INPROCESS",
						LEAP_LOG_KEY, processingStatus);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the EventSubscriptionTrackingRecords available for processingStatus : "
							+ processingStatus + "...!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	@Override
	public List<EventSubscriptionTracker> getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(
			Exchange camelExchange, String tenantId, String siteId, String subscriptionId, String failedStatus,
			JSONObject retryConfigurationJSON) throws EventSubscriptionTrackerException {
		String methodName = "getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount";
		logger.debug("{} entered into the method {}, processingStatus :{}", LEAP_LOG_KEY, methodName, failedStatus);
		List<EventSubscriptionTracker> eventTrackingList = new ArrayList<EventSubscriptionTracker>();
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			JdbcDataContext dataContext = (JdbcDataContext) DataContextFactory.createJdbcDataContext(connection);
			dataContext.setIsInTransaction(false);
			if (subscriptionTable == null)
				subscriptionTable = dataContext
						.getTableByQualifiedLabel(EventSubscriptionTrackerConstants.EVENT_SUBSCRIBER_TRACKER_TABLE);
			initializeSubscriptionTableColumnMap(subscriptionTable);
			DataSet dataSet = dataContext.query().from(subscriptionTable).selectAll()
					.limit(SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON))
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)).eq(tenantId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)).eq(siteId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)).eq(subscriptionId)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL)).eq(true)
					.where(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)).eq(failedStatus)
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)).asc()
					.orderBy(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)).asc()
					.execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				EventSubscriptionTracker eventSubscriptionTracker = new EventSubscriptionTracker();
				Row row = itr.next();
				eventTrackingList.add(parseRowToEventSubscriptionTracker(eventSubscriptionTracker, row));
			}
			logger.info("{} retrieved list of tracking for processingStatus :  FAILED or RETRY_FAILED--> {}",
					LEAP_LOG_KEY, eventTrackingList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EventSubscriptionTrackerException(
					"Failed to get list of all the FAILED/RETRY_FAILED EventSubscriptionTrackingRecords available...!",
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventTrackingList;
	}

	/**
	 * This method will check whether the retry count has been exceeded. added 1
	 * extra to mark that retryable as false so retry will not be able to fatch the
	 * data marked as nonretryable.
	 * 
	 * @param retryConfigurationJSON
	 * @param eventSubscriptionTracker
	 * @return
	 */
	private boolean maxRetryExceeded(JSONObject retryConfigurationJSON,
			EventSubscriptionTracker eventSubscriptionTracker) {
		if (SubscriptionRetryPolicy.getRetryCount(retryConfigurationJSON) == -1)
			return false;
		return !(SubscriptionRetryPolicy
				.getRetryCount(retryConfigurationJSON) > eventSubscriptionTracker.getRetryCount() + 1);
	}

	/**
	 * set retry count in EventSubscriptionTracker
	 * 
	 * @param abstractRetryStrategyBean
	 * @param eventSubscriptionTracker
	 */
	private void setSubscriptionDetailsFromConfig(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean,
			EventSubscriptionTracker eventSubscriptionTracker) {
		if (abstractRetryStrategyBean instanceof LeapDefaultRetryStrategy) {
			JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
			try {
				Integer retryCount = SubscriptionRetryPolicy.getRetryCount(retryConfiguration);
				if (retryCount >= -1)
					eventSubscriptionTracker.setIsRetryable(true);
				else
					eventSubscriptionTracker.setIsRetryable(false);

			} catch (Exception e1) {
				logger.error("{} failed to set rertycount {}", LEAP_LOG_KEY, e1.getLocalizedMessage());
			}
		} else if (abstractRetryStrategyBean instanceof JMSLeapDefaultRetryStrategy) {
			JSONObject retryConfiguration = abstractRetryStrategyBean.getRetryConfiguration();
			try {
				Integer retryCount = SubscriptionRetryPolicy.getRetryCount(retryConfiguration);
				if (retryCount >= -1)
					eventSubscriptionTracker.setIsRetryable(true);
				else
					eventSubscriptionTracker.setIsRetryable(false);

			} catch (Exception e1) {
				logger.error("{} failed to set rertycount {}", LEAP_LOG_KEY, e1.getLocalizedMessage());
			}
		} else if (abstractRetryStrategyBean instanceof LeapNoRetryStrategy)
			eventSubscriptionTracker.setIsRetryable(false);
	}

	/**
	 * provides you the map of subscription table column's.
	 * 
	 * @param subscriptionTable
	 */
	private void initializeSubscriptionTableColumnMap(Table subscriptionTable) {
		if (tableColumnMap.isEmpty() || tableColumnMap.size() != 13) {
			logger.info("{} initializeSubscriptionTableColumnMap only once and caching", LEAP_LOG_KEY);
			tableColumnMap.put(EventSubscriptionTrackerConstants.TENANT_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TENANT_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.SITE_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.SITE_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.TOPIC_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TOPIC_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.PARTITION_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.PARTITION_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.OFFEST_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.OFFEST_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.EVENT_DATA_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.EVENT_DATA_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.FAILURE_MSG_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.FAILURE_MSG_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.TRACK_STATUS,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.TRACK_STATUS));
			tableColumnMap.put(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL));
			tableColumnMap.put(EventSubscriptionTrackerConstants.RETRY_COUNT_COL,
					subscriptionTable.getColumnByName(EventSubscriptionTrackerConstants.RETRY_COUNT_COL));
		}
	}

	/**
	 * utility for parsing the row retrieved from dataset to
	 * EventSubscriptionTracker.
	 * 
	 * @param eventSubscriptionTracker
	 * @param row
	 * @return
	 */
	private EventSubscriptionTracker parseRowToEventSubscriptionTracker(
			EventSubscriptionTracker eventSubscriptionTracker, Row row) {
		logger.debug("{} tableColumnMap: {}, row : {}", LEAP_LOG_KEY, tableColumnMap, row);
		eventSubscriptionTracker.setTenantId(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TENANT_ID_COL)));
		eventSubscriptionTracker
				.setSiteId((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.SITE_ID_COL)));
		eventSubscriptionTracker.setSubscriptionId(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.SUBSCRIPTION_ID_COL)));
		eventSubscriptionTracker
				.setTopic((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TOPIC_COL)));
		eventSubscriptionTracker.setPartition(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.PARTITION_COL)));
		eventSubscriptionTracker
				.setOffset((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.OFFEST_COL)));
		eventSubscriptionTracker.setEventData(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_DATA_COL)));
		eventSubscriptionTracker.setEventFetchedDTM(
				(Date) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.EVENT_FETCHED_DTM_COL)));
		eventSubscriptionTracker.setLastFailureDTM(
				(Date) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.LAST_FAILURE_DTM_COL)));
		eventSubscriptionTracker.setFailureMsg(
				(String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.FAILURE_MSG_COL)));
		eventSubscriptionTracker
				.setStatus((String) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.TRACK_STATUS)));

		Object isRetryObject = row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.IS_RETRYABLE_COL));
		boolean isRetryAble = false;

		if (isRetryObject instanceof Boolean)
			isRetryAble = (Boolean) isRetryObject;
		if (isRetryObject instanceof Integer)
			isRetryAble = (Integer) isRetryObject != 0;

		eventSubscriptionTracker.setIsRetryable(isRetryAble);
		eventSubscriptionTracker.setRetryCount(
				(Integer) row.getValue(tableColumnMap.get(EventSubscriptionTrackerConstants.RETRY_COUNT_COL)));
		return eventSubscriptionTracker;
	}

}
