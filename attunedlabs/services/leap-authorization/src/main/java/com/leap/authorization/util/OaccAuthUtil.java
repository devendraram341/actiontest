package com.leap.authorization.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.acciente.oacc.encryptor.bcrypt.*;

import com.acciente.oacc.AccessControlContext;
import com.acciente.oacc.PasswordCredentials;
import com.acciente.oacc.Resources;
import com.acciente.oacc.sql.SQLAccessControlContextFactory;
import com.acciente.oacc.sql.SQLAccessControlSystemInitializer;
import com.acciente.oacc.sql.SQLProfile;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.InvalidAuthentication;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.service.User;

import static com.leap.authorization.ShiroAuthConstants.*;

/**
 * AuthUtil, defines the ability to perform generic tasks for AuthService
 * 
 * @author GetUsRoi
 *
 */
public class OaccAuthUtil {
	private static Logger logger = LoggerFactory.getLogger(OaccAuthUtil.class);
	private static Properties props = null;
	private static final String BASE_CONFIG_PATH = "base_config_path";

	/**
	 * Restricting the access on constructing new instance
	 */
	private OaccAuthUtil() {

	}

	/**
	 * Utility to check if requested matches with the existing
	 * 
	 * @param tenantRequested
	 * @param tenantComparable
	 * @return
	 */
	public static boolean isValidTenant(String tenantRequested, String tenantComparable) {
		logger.debug("tenantRequested: " + tenantRequested + " tenantComparable: " + tenantComparable);
		return tenantRequested.trim().equals(tenantComparable.trim());
	}// ..end of the method

	/**
	 * Utility to validate the user object
	 * 
	 * @param user
	 * @param isPasswordEnabled
	 * @return
	 */
	public static boolean isValidUser(User user, boolean isPasswordEnabled) {
		String userName = user.getUserName();
		if (!isPasswordEnabled) {
			return !isEmpty(userName);
		} else {
			return !isEmpty(userName) && !((user.getPassword() == null) || (user.getPassword().length == 0));
		}
	}

	/**
	 * Utility to get resource from specified resourceId.
	 * 
	 * @param resourceId
	 * @param tenant
	 * @return resource
	 */
	public static String getResource(String resourceId, String tenant) {
		return resourceId.split("-" + tenant)[0];
	}

	/**
	 * Utility to check source for empty
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
	 * Utility to convert the character case to upper case
	 * 
	 * @param source
	 * @return
	 */
	public static String toUpperCase(String source) {
		return source.toUpperCase();
	}// ..end of the method

	/**
	 * Utility to return dbConnection / not singleton
	 * 
	 * @return
	 * @throws InvalidConfiguration
	 */
	public static Connection getOaccConnection() throws InvalidConfiguration {
		try {
			Properties connProperties = propertiesLoader();
			String url = connProperties.getProperty(AuthConstants.OACC_DB_URL_KEY) + "?user="
					+ connProperties.getProperty(AuthConstants.OACC_DB_USER_KEY) + "&password="
					+ connProperties.getProperty(AuthConstants.OACC_DB_PASSWRD_KEY) + "&oaccsystempwd="
					+ connProperties.getProperty(AuthConstants.OACC_DB_ROOT_PWD_KEY);
			return DriverManager.getConnection(url);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			throw new InvalidConfiguration("Unable to connect to the database", e);
		}
	}// ..end of the method

	/**
	 * Utility to initialize the Oacc system
	 */
	public static void initOaccSystem() throws InvalidConfiguration {
		try {
			Properties initProperties = propertiesLoader();
			String dbUrl = getOaccInitConnString(initProperties.getProperty(AuthConstants.OACC_DB_URL_KEY));
			String dbUser = initProperties.getProperty(AuthConstants.OACC_DB_USER_KEY);
			String dbPwd = initProperties.getProperty(AuthConstants.OACC_DB_PASSWRD_KEY);
			String dbSchema = initProperties.getProperty(AuthConstants.OACC_DB_SCHEMA_KEY);
			char[] oaccRootPwd = initProperties.getProperty(AuthConstants.OACC_DB_ROOT_PWD_KEY).toCharArray();
			SQLAccessControlSystemInitializer.initializeOACC(dbUrl, dbUser, dbPwd, dbSchema, oaccRootPwd,
					BCryptPasswordEncryptor.newInstance(12));
		} catch (SQLException | IOException e) {
			throw new InvalidConfiguration("Unable to initialize OaccSystem: ", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @throws InvalidAuthentication
	 * @throws IOException
	 */
	public static void authenticateSystem() throws InvalidAuthentication, IOException {
		getAccessControlContext().authenticate(Resources.getInstance(0), PasswordCredentials
				.newInstance(propertiesLoader().getProperty(AuthConstants.OACC_DB_ROOT_PWD_KEY).toCharArray()));
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws InvalidAuthentication
	 */
	public static AccessControlContext getAccessControlContext() throws InvalidAuthentication {
		try {
			return SQLAccessControlContextFactory.getAccessControlContext(getOaccConnection(),
					propertiesLoader().getProperty(AuthConstants.OACC_DB_SCHEMA_KEY),
					SQLProfile.MySQL_5_6_NON_RECURSIVE, BCryptPasswordEncryptor.newInstance(12));
		} catch (InvalidConfiguration | IOException e) {
			throw new InvalidAuthentication("Invalidated authentication, check the configuration: ", e);
		}
	}// ..end of the method

	/**
	 * Utility to get authenticated AccessControlContext
	 * 
	 * @return
	 * @throws InvalidAuthentication
	 * @throws InvalidConfiguration
	 * @throws IOException
	 */
	public static AccessControlContext getAuthenticatedAccessControlContext(Connection connection)
			throws InvalidConfiguration {
		try {
			AccessControlContext accessControlContext = SQLAccessControlContextFactory.getAccessControlContext(
					connection, propertiesLoader().getProperty(AuthConstants.OACC_DB_SCHEMA_KEY),
					SQLProfile.MySQL_5_6_NON_RECURSIVE, BCryptPasswordEncryptor.newInstance(12));
			accessControlContext.authenticate(Resources.getInstance(0), PasswordCredentials
					.newInstance(propertiesLoader().getProperty(AuthConstants.OACC_DB_ROOT_PWD_KEY).toCharArray()));
			return accessControlContext;
		} catch (IOException e) {
			dbCleanUp(connection);
			throw new InvalidConfiguration("Invalidated authentication, check the configuration: ", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	// private static Properties propertiesLoader(String fileName) throws
	// IOException {
	// Properties props = new Properties();
	// props.load(OaccAuthUtil.class.getClassLoader().getResourceAsStream(fileName));
	// return props;
	// }// ..end of the method

	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Properties propertiesLoader() throws IOException {
		if (props == null) {
			props = new Properties();
			String baseConfigPath = System.getProperty(BASE_CONFIG_PATH);
			props.load(new FileInputStream(baseConfigPath+ File.separator + APPS_DEPLOYMENT_ENV_CONFIG));
		}

		return props;
	}// ..end of the method

	/**
	 * 
	 * @param connection
	 * @throws InvalidConfiguration
	 * @throws SQLException
	 */
	public static void dbCleanUp(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Unable to destroy the connection..", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param dbUrl
	 * @return
	 */
	protected static String getOaccInitConnString(String dbUrl) {
		return dbUrl + "?sessionVariables=sql_mode='NO_AUTO_VALUE_ON_ZERO,STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'";
	}// ..end of the method

	/**
	 * Replaces the special characters in source into percent-encoding format.
	 * 
	 * @param source
	 * @param toReplaceFrom
	 * @param toRelaceTo
	 * @return replacedSource
	 */
	public static String decoder(String source, String toReplaceFrom, String toRelaceTo) {
		return source.replace(toReplaceFrom, toRelaceTo);
	}
}
