package com.attunedlabs.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.util.DataSourceInstanceException;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;

/**
 * #TODO TEMP class till the time we donn't solve the Database related concept
 * as how the datasource will be found and connected to.
 *
 */
public class CoreDataBaseUtil {

	private static String URL = null;
	private static String DRIVER_CLASS = null;
	private static String USER = null;
	private static String PASSWORD = null;

	/**
	 * 
	 * @return JDBCconnection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws DataSourceInstanceException
	 */
	public static Connection getConnection() throws ClassNotFoundException, SQLException, DataSourceInstanceException {
		if (URL == null) {
			loadConfigrationDbPropertyFile();
		}
		Class.forName(DRIVER_CLASS);
		Connection connection = (Connection) DriverManager.getConnection(URL, USER, PASSWORD);
		return connection;
	}

	public static void dbCleanup(Connection con, PreparedStatement ptst, ResultSet rs) {
		close(con);
		close(ptst);
		close(rs);
	}

	public static void dbCleanUp(Connection conn, PreparedStatement ps) {
		close(conn);
		close(ps);
	}

	public static void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}

	public static void close(PreparedStatement pStatement) {
		if (pStatement != null) {
			try {
				pStatement.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}

	public static void close(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException sqlexp) {
				sqlexp.printStackTrace();
			}
		}
	}

	private synchronized static void loadConfigrationDbPropertyFile() throws DataSourceInstanceException {
		try {
			URL = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_URL,LeapDefaultConstants.DEFAULT_DB_URL);
			DRIVER_CLASS = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_DRIVER_CLASS,LeapDefaultConstants.DEFAULT_DB_DRIVER_CLASS);
			USER = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_USER,LeapDefaultConstants.DEFAULT_DB_USER);
			PASSWORD = LeapConfigUtil.getGlobalPropertyValue(LeapConstants.DB_PASSWORD,LeapDefaultConstants.DEFAULT_DB_PASSWORD);
		} catch (PropertiesConfigException e) {
			throw new DataSourceInstanceException("Unable to get the database connection properties " + e.getMessage());

		}

	}

}
