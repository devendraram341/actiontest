package com.attunedlabs.config.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * singleton
 * 
 * @author Reactiveworks42
 *
 */
public class DataSourceInstance {
	final static Logger logger = LoggerFactory.getLogger(DataSourceInstance.class);

	private static String URL = null;
	private static String DRIVER_CLASS = null;
	private static String USER = null;
	private static String PASSWORD = null;
	private static DataSourceInstance ds;
	private ComboPooledDataSource cpds;

	private DataSourceInstance() {
		cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(DRIVER_CLASS);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		cpds.setJdbcUrl(URL);
		cpds.setUser(USER);
		cpds.setPassword(PASSWORD);
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(10);
		cpds.setIdleConnectionTestPeriod(18000);
		cpds.setPreferredTestQuery("SELECT 1");
	}

	public static DataSourceInstance getInstance() throws IOException, PropertiesConfigException {
		if (URL == null) {
			loadConfigrationDbPropertyFile();
		}
		if (ds == null) {
			ds = new DataSourceInstance();
			return ds;
		} else
			return ds;
	}

	public static Connection getConnection() throws SQLException, IOException, PropertiesConfigException {
		Connection con = getInstance().cpds.getConnection();
		return con;
	}

	public ComboPooledDataSource getDataSource() throws SQLException {
		return this.cpds;
	}

	public static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception e) {
			logger.error("{} failed to close connection instance due to ...{}", LEAP_LOG_KEY, e.getMessage());
		}
	}

	private synchronized static void loadConfigrationDbPropertyFile() throws IOException, PropertiesConfigException {
		URL = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_URL,LeapDefaultConstants.DEFAULT_DB_URL);
		DRIVER_CLASS = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_DRIVER_CLASS,LeapDefaultConstants.DEFAULT_DB_DRIVER_CLASS);
		USER = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_USER,LeapDefaultConstants.DEFAULT_DB_USER);
		PASSWORD = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_PASSWORD,LeapDefaultConstants.DEFAULT_DB_PASSWORD);
	}
}
