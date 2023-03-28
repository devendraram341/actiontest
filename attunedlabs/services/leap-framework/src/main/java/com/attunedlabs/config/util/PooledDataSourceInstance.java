package com.attunedlabs.config.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.dao.LeapConstants;

/**
 * PooledDataSourceInstance is a singleton class for getting the connection and
 * closing the connection from connection Pool.
 * 
 * @author Reactiveworks
 *
 */
public class PooledDataSourceInstance {
	private final static Logger logger = LoggerFactory.getLogger(PooledDataSourceInstance.class);
	private static final String DB_PASSWORD = "DB_PASSWORD";
	private static final String DB_USER = "DB_USER";
	private static final String DB_DRIVER_CLASS = "DB_DRIVER_CLASS";
	private static final String DB_URL = "DB_URL";

	private static String URL = null;
	private static String DRIVER_CLASS = null;
	private static String USER = null;
	private static String PASSWORD = null;
	private static PooledDataSourceInstance ds;
	private static GenericObjectPool connectionPool = null;
	private DataSource dataSource;

	private PooledDataSourceInstance() {

		// Creates an instance of GenericObjectPool that holds our
		// pool of connections object.
		connectionPool = new GenericObjectPool();
		connectionPool.setMinIdle(5);
		connectionPool.setMaxIdle(10);
		connectionPool.setMaxActive(15);

		// Creates a connection factory object which will be use by
		// the pool to create the connection object. We passes the
		// JDBC URL info, userName and password.
		ConnectionFactory cf = new DriverManagerConnectionFactory(URL, USER, PASSWORD);

		// Creates a PoolableConnectionFactory that will wraps the
		// connection object created by the ConnectionFactory to add
		// object pooling functionality.
		@SuppressWarnings("unused")
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
		connectionPool.setFactory(pcf);
		dataSource = new PoolingDataSource(connectionPool);
	}

	private static PooledDataSourceInstance getInstance() throws Exception {
		if (URL == null)
			loadConfigrationDbPropertyFile();
		if (ds == null)
			ds = new PooledDataSourceInstance();
		return ds;
	}

	/**
	 * Returns the connection.
	 * 
	 * @return {@link Connection}
	 * @throws DataSourceInstanceException
	 */
	public static Connection getConnection() throws DataSourceInstanceException {
		try {
			return getInstance().dataSource.getConnection();
		} catch (Exception e) {
			throw new DataSourceInstanceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Closes the connection.
	 * 
	 * @return {@link Connection}
	 * @throws Exception
	 */
	public static void closeConnection(Connection connection) {
		try {
			if (connection != null) {
				logger.debug("{} Closing connection form PooledDataSourceInstance.", LEAP_LOG_KEY);
				connection.close();
			} else
				logger.debug("{} Closing connection failed, since connection is null.", LEAP_LOG_KEY);
		} catch (Exception e) {
			logger.error("{} failed to close connection instance due to ...", LEAP_LOG_KEY, e.getMessage());
		}
	}

	private synchronized static void loadConfigrationDbPropertyFile() throws Exception {
		URL = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_URL,LeapDefaultConstants.DEFAULT_DB_URL);
		DRIVER_CLASS = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_DRIVER_CLASS,LeapDefaultConstants.DEFAULT_DB_DRIVER_CLASS);
		USER = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_USER,LeapDefaultConstants.DEFAULT_DB_USER);
		PASSWORD = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_PASSWORD,LeapDefaultConstants.DEFAULT_DB_PASSWORD);
		Class.forName(DRIVER_CLASS).newInstance();
	}

	/**
	 * Returns the connection.
	 * 
	 * @return {@link Connection}
	 * @throws DataSourceInstanceException
	 */
	public static Connection getConnection(String methodName) throws DataSourceInstanceException {
		try {
			String nameOfTheMethod = "getConnection";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, nameOfTheMethod);
			Connection con = getInstance().dataSource.getConnection();
			return con;
		} catch (Exception e) {
			throw new DataSourceInstanceException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Closes the connection.
	 * 
	 * @return {@link Connection}
	 * @throws Exception
	 */
	public static void closeConnection(Connection connection, String methodName) {
		try {
			if (connection != null) {
				logger.debug("{} Closing connection form PooledDataSourceInstance for {}", LEAP_LOG_KEY, methodName);
				connection.close();
			} else
				logger.debug("{} Closing connection failed for {} since connection is null.", LEAP_LOG_KEY, methodName);
		} catch (Exception e) {
			logger.error("{} failed to close connection instance due to ...{} ", LEAP_LOG_KEY, e.getMessage());
		}
	}

}
