package com.leap.authorization.util;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.leap.authorization.ShiroAuthConstants.*;

public class ShiroAuthUtil {
	private static ShiroAuthUtil dbConnService;
	private static Logger logger = LoggerFactory.getLogger(ShiroAuthUtil.class);
	private static final String DRIVER_CLASS = "SHIRO_DB_DRIVER_CLASS";
	private static final String URL = "SHIRO_DB_URL";
	private static final String USER_NAME = "SHIRO_DB_USER";
	private static final String PASSWORD = "SHIRO_DB_PASSWORD";
	private static final String BASE_CONFIG_PATH = "base_config_path";

	static Properties properties = new Properties();
	static {
		try {
			properties = OaccAuthUtil.propertiesLoader();
		} catch (Exception e) {
			logger.error("Unable to load the properties: " + e);
		}
	}

	private ShiroAuthUtil() {
	}

	/**
	 * Utility to get connection to perform DB operation
	 * 
	 * @return
	 * @throws DBConnectionException
	 */
	private Connection getConnection() throws DBConnectionException {
		try {
			Class.forName(properties.getProperty(DRIVER_CLASS));
			return DriverManager.getConnection(properties.getProperty(URL), properties.getProperty(USER_NAME),
					properties.getProperty(PASSWORD));
		} catch (SQLException | ClassNotFoundException e) {
			throw new DBConnectionException("Unable to connect to the database specified..!.." + e);
		}
	}// ..end of the method

	/**
	 * Utility to check empty source
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(String source) {
		boolean result;
		if (source == null || source.length() == 0) {
			result = true;
			return result;
		} else {
			result = false;
			return result;
		}
	}// ..end of the method

	/**
	 * 
	 * @param source
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmptyList(List source) {
		boolean result;
		if (source == null || source.isEmpty()) {
			result = true;
			return result;
		} else {
			result = false;
			return result;
		}
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws DBConnectionException
	 */
	public static synchronized Connection getDBConnection() throws DBConnectionException {
		if (dbConnService == null) {
			dbConnService = new ShiroAuthUtil();
		}
		return dbConnService.getConnection();
	}// ..end of method

	/**
	 * Utility to close connection & preparedStatement
	 * 
	 * @param connection
	 * @param preparedStatement
	 */
	public static void dbCleanUp(Connection connection, PreparedStatement preparedStatement) {
		try {
			if (preparedStatement != null)
				preparedStatement.close();
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error("CleanUp DB connection / PreparedStatement failed: " + e);
		}
	}// ..end of the method

	public static void dbCleanUp(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error("CleanUp DB connection / PreparedStatement failed: " + e);
		}
	}
}
