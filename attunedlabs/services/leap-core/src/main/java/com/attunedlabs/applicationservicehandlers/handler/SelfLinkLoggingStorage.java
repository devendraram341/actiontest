package com.attunedlabs.applicationservicehandlers.handler;

import static com.attunedlabs.eventframework.abstractbean.util.CassandraUtil.DRIVER_CLASS_KEY;
import static com.attunedlabs.eventframework.abstractbean.util.CassandraUtil.URL_KEY;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.util.CassandraConnectionException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.selflink.SelfLinkDataProcessor;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.servicehandlers.AbstractServiceHandler;

public class SelfLinkLoggingStorage extends AbstractServiceHandler {

	public static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";
	public static final String TIMEZONE_ID = "UTC";
	final static Logger logger = LoggerFactory.getLogger(SelfLinkLoggingStorage.class);
	private SelfLinkDataProcessor selfLinkDataProcessor = new SelfLinkDataProcessor();
	private UpdateableDataContext updateableDataContext;
	private Connection con;
	private Table table;

	@Override
	public boolean initializeConfiguration(JSONObject jsonObject) {
		try {
			con = getLocalCassandraConnection(jsonObject);
			updateableDataContext = getUpdateableDataContextForCassandra(con);
			((JdbcDataContext) updateableDataContext).setIsInTransaction(false);
			table = updateableDataContext.getDefaultSchema().getTableByName("selflinklogging");
		} catch (Exception e) {
			logger.error("{} Failed to create connection - selflinklogging {}", LEAP_LOG_KEY, e.getMessage());
		}
		return true;
	}

	@Override
	public void postService(Exchange exchange) {
		String methodName = "postService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		long startTime = System.currentTimeMillis();
		LeapDataContext leapDataContext = exchange.getIn().getHeader("leapDataContext", LeapDataContext.class);
		org.json.JSONObject requestHeaders = leapDataContext.getRequestHeaders(LeapDataContextConstant.HEADER);

		try {
			selfLinkDataProcessor.process(exchange);
		} catch (Exception e) {
			e.printStackTrace();
		}

		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		String tenantId = (String) requestHeaders.get(LeapDataContextConstant.TENANTID);
		String siteId = (String) requestHeaders.get(LeapDataContextConstant.SITEID);
		String reqId = leapServiceContext.getRequestUUID();
		// Long isDate = calculateUTCTransactionTime(receivedDTM, tenantId,
		// siteId, timeZoneId);

		InsertInto valuesInsertObject = new InsertInto(table);
		valuesInsertObject.value("tenantid", tenantId).value("siteid", siteId).value("requestid", reqId)
				.value("createddtm", new Date().getTime())
				.value("document", exchange.getIn().getHeader("selfLink", org.json.JSONObject.class).toString());

		updateableDataContext.executeUpdate(valuesInsertObject);

		long stopTime = System.currentTimeMillis();
		logger.debug("{} postService time ms :{}", LEAP_LOG_KEY, (stopTime - startTime));
	}

	/**
	 * This method is used to get data require for Cassandra local
	 * 
	 * @param jsonObject
	 * 
	 * @return Connection Object
	 * @throws CassandraConnectionException
	 */
	private Connection getLocalCassandraConnection(JSONObject jsonObject) throws CassandraConnectionException {
		String methodName = "getLocalCassandraConnection";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			if (con == null || con.isClosed()) {
				try {
					Class.forName((String) jsonObject.get(DRIVER_CLASS_KEY));
					try {
						con = DriverManager.getConnection((String) jsonObject.get(URL_KEY));
						logger.trace("{} Connection Object :{} ", LEAP_LOG_KEY, con);
						return con;
					} catch (SQLException e) {
						throw new CassandraConnectionException(
								"URL to get the connection object for cassandra is invalid : "
										+ (String) jsonObject.get(URL_KEY),
								e);
					}
				} catch (ClassNotFoundException e) {
					throw new CassandraConnectionException("unable to load the driver name for cassandra : "
							+ (String) jsonObject.get(DRIVER_CLASS_KEY), e);
				}

			}
		} catch (Exception e) {
			throw new CassandraConnectionException("unable to get the connection object for cassandra : " + jsonObject,
					e);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return con;
	}// end
		// of
		// method
		// getLocalCassandraConnection

	protected UpdateableDataContext getUpdateableDataContextForCassandra(Connection connection) {
		logger.debug("{} .getUpdateableDataContextForCassandra method of AbstractCassandraBean", LEAP_LOG_KEY);
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(connection);
		return dataContext;
	}

	public static Long calculateUTCTransactionTime(String receivedDTMs, String tenantId, String siteId,
			String timeZoneId) throws ParseException, AccountFetchException {
		String methodName = "calculateUTCTransactionTime";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (timeZoneId == null || timeZoneId.isEmpty()) {
			logger.error("{} timezone not found in custer site for tenant and site {} : {}", LEAP_LOG_KEY, tenantId,
					siteId);
			timeZoneId = TimeZone.getDefault().getID();
		}
		TimeZone tenantSiteTimeZone = TimeZone.getTimeZone(timeZoneId);
		TimeZone orignalLocalTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(tenantSiteTimeZone);

		DateFormat tenantDf = new SimpleDateFormat(DATE_FORMAT);
		DateFormat utcTenantDF = new SimpleDateFormat(DATE_FORMAT);

		Date receivedDTM = tenantDf.parse(receivedDTMs);
		Calendar tenantCalendar = new GregorianCalendar();
		tenantCalendar.setTime(receivedDTM);
		logger.trace("{} initial time  :: {}", receivedDTM);
		tenantDf.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
		String formattedUTCTime = tenantDf.format(tenantCalendar.getTime());
		logger.trace("{} before foramtting utc time :: {}", formattedUTCTime);
		Date utcDATE = utcTenantDF.parse(formattedUTCTime);
		logger.trace("{} actual utc time to be inserted  :: ", LEAP_LOG_KEY, utcDATE);

		// formatting must be done before based on the time zone
		// added
		DateFormat cassandraLocalDF = new SimpleDateFormat(DATE_FORMAT);
		DateFormat cassandraUTCDF = new SimpleDateFormat(DATE_FORMAT);

		// once all calculation's are done setting default value
		// of timezone after formatting so we will add the
		TimeZone.setDefault(orignalLocalTimeZone);

		long receivedDTMms = receivedDTM.getTime();
		long actualUTCms = utcDATE.getTime();
		logger.trace("{} boolean  (convertedutc < receiveddtm) {}", LEAP_LOG_KEY, (actualUTCms < receivedDTMms));
		Long isDate;
		if (actualUTCms <= receivedDTMms) {
			isDate = addExtraTimeForCassandratoRemove(utcDATE, cassandraLocalDF, cassandraUTCDF, actualUTCms);
		} else {
			isDate = removeExtraTimeForCassandratoAdd(utcDATE, cassandraLocalDF, cassandraUTCDF, actualUTCms);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return isDate;
	}

	/**
	 * adding the time in existing date because cassandra driver will remove that
	 * much time based on local utc.In order to get actual timestamp.
	 * 
	 */
	private static Long addExtraTimeForCassandratoRemove(Date utcDATE, DateFormat cassandraLocalDF,
			DateFormat cassandraUTCDF, long actualUTCms) throws ParseException {
		Long isDate;
		Calendar cassandraCalendar = new GregorianCalendar();
		cassandraCalendar.setTime(utcDATE);
		logger.trace("{} utc to be converted based on cassandra : {}", LEAP_LOG_KEY, utcDATE);
		cassandraLocalDF.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
		String cassandraExtraAddedTime = cassandraLocalDF.format(cassandraCalendar.getTime());
		logger.trace("{} before foramtting utc time :: {}", LEAP_LOG_KEY, cassandraExtraAddedTime);
		Date extraOffsetAddedDate = cassandraUTCDF.parse(cassandraExtraAddedTime);

		long addedTimeToActualDTM = extraOffsetAddedDate.getTime();
		long diff = actualUTCms - addedTimeToActualDTM;
		isDate = Long.valueOf(new Date(actualUTCms + diff).getTime());
		logger.trace("{} cassandra utc extra added time based on local:: {}", LEAP_LOG_KEY, new Date(isDate));
		return isDate;
	}

	/**
	 * removing extra time from existing utc date because cassandra driver will add
	 * that much time based on local utc.In order to get actual timestamp
	 */
	private static Long removeExtraTimeForCassandratoAdd(Date utcDATE, DateFormat cassandraLocalDF,
			DateFormat cassandraUTCDF, long actualUTCms) throws ParseException {
		Long isDate;
		Calendar cassandraCalendar = new GregorianCalendar();
		cassandraCalendar.setTime(utcDATE);
		logger.trace("{} utc to be converted based on cassandra : {}", LEAP_LOG_KEY, utcDATE);
		cassandraLocalDF.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ID));
		String cassandraExtraAddedTime = cassandraLocalDF.format(cassandraCalendar.getTime());
		logger.trace("{} before foramtting utc time ::{} ", LEAP_LOG_KEY, cassandraExtraAddedTime);
		Date extraOffsetAddedDate = cassandraUTCDF.parse(cassandraExtraAddedTime);

		long removingExtraTime = extraOffsetAddedDate.getTime();
		long diff = removingExtraTime - actualUTCms;
		isDate = Long.valueOf(new Date(actualUTCms - diff).getTime());
		logger.trace("{} cassandra utc extra added time based on local::{} ", LEAP_LOG_KEY, new Date(isDate));
		return isDate;
	}

}
