package com.attunedlabs.dynastore.persistence.dao;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.DefaultUpdateSummary;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.dynastore.config.impl.ConfigDynaStoreInitializationException;
import com.attunedlabs.dynastore.persistence.DynaStoreLog;

public class DynastoreLogDAO {

	private final Logger logger = LoggerFactory.getLogger(DynastoreLogDAO.class);

	/**
	 * to insert DynastoreLog into DB
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @param status
	 * @param info
	 * @return if updated return 1 else 0
	 * 
	 * 
	 * @throws ConfigDynaStoreInitializationException
	 */
	public int insertDynastoreLog(final int siteNodeId, final String sessionId, final String status, final String info)
			throws ConfigDynaStoreInitializationException {
		String methodParam = "siteNodeId: " + siteNodeId + " sessionId: " + sessionId + " status: " + status + " info: "
				+ info;
		String methodName = "insertDynastoreLog";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, methodParam);
		Integer totalInsertedRows = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DefaultUpdateSummary insertSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowInsertionBuilder insert = callback.insertInto(table);
					insert.value(LeapConstants.DYNASTORE_SITE_NODE_ID, siteNodeId)
							.value(LeapConstants.DYNASTORE_SESSION_ID, sessionId)
							.value(LeapConstants.DYNASTORE_STATUS, status)
							.value(LeapConstants.DYNASTORE_OPENED_DTM,
									new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()))
							.value(LeapConstants.DYNASTORE_INFO, info).execute();

				}
			});

			if (insertSummary.getInsertedRows().isPresent()) {
				totalInsertedRows = (Integer) insertSummary.getInsertedRows().get();
				logger.debug("{} inserted-Dynastorelog --> {}", LEAP_LOG_KEY, totalInsertedRows);
			} else
				logger.debug("{} nothing inserted in Dynastore..", LEAP_LOG_KEY);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to insert dynastorelog data --> " + e.getMessage(),
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return totalInsertedRows;

	}

	/**
	 * updating dynastoreLog status ,closedDTM in DB
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @param status
	 * @return if updated return 1 else 0
	 * @throws ConfigDynaStoreInitializationException
	 * 
	 * 
	 */
	public int updateDynastoreLog(final int siteNodeId, final String sessionId, final String status)
			throws ConfigDynaStoreInitializationException {
		String methodParam = "siteNodeId: " + siteNodeId + " sessionId: " + sessionId + " status: " + status;
		String methodName = "updateDynastoreLog";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsUpdated = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DefaultUpdateSummary updateSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowUpdationBuilder update = callback.update(table);
					update.where(LeapConstants.DYNASTORE_SITE_NODE_ID).eq(siteNodeId)
							.where(LeapConstants.DYNASTORE_SESSION_ID).eq(sessionId);
					update.value(LeapConstants.DYNASTORE_STATUS, status).value(LeapConstants.DYNASTORE_CLOSED_DTM,
							new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis())).execute();

				}
			});
			if (updateSummary.getUpdatedRows().isPresent()) {
				totalRowsUpdated = (Integer) updateSummary.getUpdatedRows().get();
				logger.debug("{} total updated rows in dynastorelog: {}", LEAP_LOG_KEY, totalRowsUpdated);
			} else
				logger.debug("{} total updated node dynastorelog:{} ", LEAP_LOG_KEY, totalRowsUpdated);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException(
					"failed to update dynastorelog table --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return totalRowsUpdated;
	}

	/**
	 * To get status of dynastoreLog fromDB By siteId and sessionId
	 * 
	 * @param siteNodeId
	 * @param sessionId
	 * @return status (closed or opened)
	 * 
	 * 
	 */
	public String getDynaStoreLog(int siteNodeId, String sessionId) throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLog";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String status = "";
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table)
					.select(table.getColumnByName(LeapConstants.DYNASTORE_STATUS))
					.where(LeapConstants.DYNASTORE_SESSION_ID).eq(sessionId).and(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteNodeId).execute();
			Iterator<Row> itr = dataSet.iterator();
			if (itr.hasNext()) {
				Row row = itr.next();
				status = row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_STATUS)).toString();
				logger.info("{} fetched status : {}", LEAP_LOG_KEY, status);
				return status;
			} else {
				logger.warn("{} status of dynastoreLog fromDB  with siteNodeId:{}  sessionId:{}  is empty!",
						LEAP_LOG_KEY, siteNodeId, sessionId);
			}
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException(
					"failed to get  status of dynastoreLog fromDB with siteNodeId: " + siteNodeId + " sessionId: "
							+ sessionId + " is empty! --> " + e.getMessage(),
					e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return status;
	}

	/**
	 * To delete all dynastoreLog
	 * 
	 * 
	 * 
	 * @throws ConfigDynaStoreInitializationException
	 */
	public void deleteDynaStoreLog() throws ConfigDynaStoreInitializationException {
		String methodName = "deleteDynaStoreLog";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int totalRowsDeleted = 0;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DefaultUpdateSummary deleteSummary = (DefaultUpdateSummary) dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					final RowDeletionBuilder delete = callback.deleteFrom(table);
					delete.execute();

				}
			});
			if (deleteSummary.getDeletedRows().isPresent()) {
				totalRowsDeleted = (Integer) deleteSummary.getDeletedRows().get();
				logger.info("{} total deleted records: {}", LEAP_LOG_KEY, totalRowsDeleted);
			} else
				logger.info("{} total deleted records: {}", LEAP_LOG_KEY, totalRowsDeleted);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to delete records --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * get list of dynastoreLogs fromDb By siteId, status , openedDate
	 * 
	 * @param siteId
	 * @param status
	 * @param opnedDate
	 * @return list of DynastoreLogs
	 * 
	 * 
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatusAndOpenDate(int siteId, String status, java.util.Date openedDate)
			throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogByStatusAndOpenDate";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).where(LeapConstants.DYNASTORE_STATUS).eq(status)
					.where(LeapConstants.DYNASTORE_OPENED_DTM).greaterThan(openedDate).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.info("{} fetched dynastorelog table  row:{} ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				logger.info("{} parsed DynaStoreLog  from row retrieved:{} ", LEAP_LOG_KEY, dynaStoreLog);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved: {}", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to get all dyanstorelog data at siteId: " + siteId
					+ " openedDate: " + openedDate + " status: " + status + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * to get list of dynastoreLog fromDB by siteid, status, closeddate
	 * 
	 * @param siteId
	 * @param status
	 * @param closedDate
	 * @return list Of dynastoreLog
	 * @throws ConfigDynaStoreInitializationException
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatusAndClosedDate(int siteId, String status, Date closedDate)
			throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogByStatusAndClosedDate";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).where(LeapConstants.DYNASTORE_STATUS).eq(status)
					.where(LeapConstants.DYNASTORE_CLOSED_DTM).eq(closedDate).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.trace("{} fetched dynastorelog table  row:{} ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				logger.info("{}parsed DynaStoreLog  from row retrieved:{} ", LEAP_LOG_KEY, dynaStoreLog);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved:{} ", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to get all dyanstorelog data at siteId: " + siteId
					+ " closedDate: " + closedDate + " status: " + status + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * to get list of dynastoreLog from DB where opendate greater than given date,
	 * siteid
	 * 
	 * @param siteId
	 * @param date
	 * @return
	 * @throws ConfigDynaStoreInitializationException
	 * 
	 * 
	 */

	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDate(int siteId, Date date)
			throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogByGreaterThanGivenDate";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).where(LeapConstants.DYNASTORE_OPENED_DTM).greaterThan(date).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.trace("{} fetched dynastorelog table  row:{} ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				logger.trace("{} parsed DynaStoreLog  from row retrieved:{} ", LEAP_LOG_KEY, dynaStoreLog);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved: {}", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to get all dyanstorelog data at siteId: " + siteId
					+ " date: " + date + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * to get list of dynastoreLog from DB by sitedId and status
	 * 
	 * @param siteId
	 * @param status
	 * @return list of dynastoreLogs
	 * @throws ConfigDynaStoreInitializationException
	 */
	public List<DynaStoreLog> getDynaStoreLogByStatus(int siteId, String status)
			throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogByStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).where(LeapConstants.DYNASTORE_STATUS).eq(status).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.trace("{} fetched dynastorelog table  row:{} ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved:{} ", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to get all dyanstorelog data at siteId: " + siteId
					+ " status: " + status + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * to get list of dynastoreLog from DB where open date greater than given date,
	 * siteid, status
	 * 
	 * @param siteId
	 * @param openedDate
	 * @return list of
	 * 
	 * 
	 */
	public List<DynaStoreLog> getDynaStoreLogByGreaterThanGivenDateAndStatus(int siteId, Date openedDate, String status)
			throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogByGreaterThanGivenDateAndStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).where(LeapConstants.DYNASTORE_STATUS).eq(status)
					.where(LeapConstants.DYNASTORE_CLOSED_DTM).greaterThan(openedDate).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.trace("{} fetched dynastorelog table  row:{} ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				logger.trace("{} parsed DynaStoreLog  from row retrieved:{} ", LEAP_LOG_KEY, dynaStoreLog);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved: {}", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException("failed to get all dyanstorelog data at siteId: " + siteId
					+ " openedDate: " + openedDate + " status: " + status + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	/**
	 * to get list of dynastoreLog from DB siteid
	 * 
	 * @param siteId
	 * @param date
	 * @return
	 * @throws ConfigDynaStoreInitializationException
	 * 
	 * 
	 */
	public List<DynaStoreLog> getDynaStoreLogBySiteId(int siteId) throws ConfigDynaStoreInitializationException {
		String methodName = "getDynaStoreLogBySiteId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ArrayList<DynaStoreLog> list = new ArrayList<DynaStoreLog>();
		DynaStoreLog dynaStoreLog = null;
		Connection connection = null;
		try {
			connection = DataSourceInstance.getConnection();
			UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
			final Table table = dataContext.getTableByQualifiedLabel(LeapConstants.DYNASTORE_LOG_TABLE);
			DataSet dataSet = dataContext.query().from(table).selectAll().where(LeapConstants.DYNASTORE_SITE_NODE_ID)
					.eq(siteId).execute();
			Iterator<Row> itr = dataSet.iterator();
			while (itr.hasNext()) {
				Row row = itr.next();
				dynaStoreLog = new DynaStoreLog();
				logger.info("{} fetched dynastorelog table  row:{}  ", LEAP_LOG_KEY, row);
				parseROW(dynaStoreLog, row, table);
				logger.info("{} parsed DynaStoreLog  from row retrieved:{}  ", LEAP_LOG_KEY, dynaStoreLog);
				list.add(dynaStoreLog);
			}
			logger.info("{} all dynastorelog  rows retrieved:{}  ", LEAP_LOG_KEY, list);
		} catch (Exception e) {
			throw new ConfigDynaStoreInitializationException(
					"failed to get all dyanstorelog data at siteId: " + siteId + " --> " + e.getMessage(), e);
		} finally {
			DataSourceInstance.closeConnection(connection);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return list;
	}

	private void parseROW(DynaStoreLog dynaStoreLog, Row row, Table table) {
		dynaStoreLog.setSiteId(((Integer) row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_SITE_NODE_ID))));
		dynaStoreLog.setSessionId(row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_SITE_NODE_ID)).toString());
		dynaStoreLog.setStatus(row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_STATUS)).toString());
		dynaStoreLog.setOpendDTM(((Date) row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_OPENED_DTM))));
		dynaStoreLog.setClosedDTM(((Date) row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_CLOSED_DTM))));
		dynaStoreLog.setInfo((row.getValue(table.getColumnByName(LeapConstants.DYNASTORE_INFO)).toString()));
	}

}
