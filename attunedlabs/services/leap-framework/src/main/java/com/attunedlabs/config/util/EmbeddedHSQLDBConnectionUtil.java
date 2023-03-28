package com.attunedlabs.config.util;

import static com.attunedlabs.config.ConfigurationConstant.PROFILE_ID_PROPERTY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;

public class EmbeddedHSQLDBConnectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedHSQLDBConnectionUtil.class);

	private static final String LEAP_DUMP = "leapdump.sql";

	private static String oldData = null;

	/**
	 * This method use for embedded HSQLDB connection, Read leapDump file and
	 * execute statement.
	 * 
	 * @param requestContext (Required)
	 * @param fileName       (Optional)
	 * @throws HSQLDBConnectionException
	 */
	public static void readFileAndExecute(RequestContext requestContext, String fileName)
			throws HSQLDBConnectionException {
		Connection connection = null;
		try {
			updateProperties();
			if (fileName == null || fileName.isEmpty())
				fileName = LEAP_DUMP;
			connection = DataSourceInstance.getConnection();
			Statement statement = connection.createStatement();
			InputStream file = LeapConfigUtil.getFile(requestContext, fileName);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				statement.execute(line);
			}
			statement.close();
			bufferedReader.close();
			file.close();

		} catch (IOException e) {
			logger.error("not able to read file " + e);
			throw new HSQLDBConnectionException("not able to read file ", e);
		} catch (SQLException e) {
			logger.error("database access error occurs :: " + e);
			throw new HSQLDBConnectionException("Connection not established :: ", e);
		} catch (LeapConfigUtilException e) {
			logger.error("file not found inresource or feature specific directory -> " + fileName + " ::" + e);
			throw new HSQLDBConnectionException("file not found -> " + fileName, e);
		} catch (PropertiesConfigException e) {
			logger.error("properties file not loaded " + e);
			throw new HSQLDBConnectionException("properties file not loaded ", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("database access error occurs :: " + e);
					throw new HSQLDBConnectionException("Connection not established :: ", e);
				}
			}
		}
	}

	public static void revertGlobalAppDeploymentProperties() throws HSQLDBConnectionException {
		File globalAppDeploymentFile = new File(getGlobalAppDeploymentFileLocation());
		updateGlobalAppDeploymentProperies(globalAppDeploymentFile, oldData);

	}

	private static void updateProperties() throws HSQLDBConnectionException {
		try {
			File globalAppDeploymentFile = new File(getGlobalAppDeploymentFileLocation());
			oldData = new String(Files.readAllBytes(globalAppDeploymentFile.toPath()), StandardCharsets.UTF_8);
			String newData = oldData;
			String replace = newData.replaceAll(".*DB_USER.*", "DB_USER=sa")
					.replaceAll(".*DB_PASSWORD.*", "DB_PASSWORD=")
					.replaceAll(".*DB_DRIVER_CLASS.*", "DB_DRIVER_CLASS=org.hsqldb.jdbcDriver")
					.replaceAll(".*DB_URL.*", "DB_URL=jdbc:hsqldb:mem:~/leap");

			updateGlobalAppDeploymentProperies(globalAppDeploymentFile, replace);
		} catch (IOException e1) {
			logger.error("properties file not loaded ! " + e1);
			throw new HSQLDBConnectionException("properties file not loaded !", e1);
		}

	}

	private static String getGlobalAppDeploymentFileLocation() {
		String profileId = System.getProperty(PROFILE_ID_PROPERTY);
		String basePath = getPathForConfig();
		String configPath = basePath.concat(File.separator).concat("config").concat(File.separator).concat(profileId);
		String globalAppPath = configPath.concat(File.separator).concat("globalAppDeploymentConfig.properties");
		return globalAppPath;
	}

	private static String getPathForConfig() {
		String userDir = System.getProperty("user.dir");
		Path path = Paths.get(userDir).getParent().getParent();
		return path.toString();
	}

	private static void updateGlobalAppDeploymentProperies(File globalApp, String replaceData)
			throws HSQLDBConnectionException {
		try (FileOutputStream fileOutputStream = new FileOutputStream(globalApp)) {
			Files.write(Paths.get(globalApp.getPath()), replaceData.getBytes(), StandardOpenOption.WRITE);

		} catch (FileNotFoundException e) {
			logger.error("properties file not found ! " + e);
			throw new HSQLDBConnectionException("properties file not found !", e);
		} catch (IOException e) {
			logger.error("properties file not loaded ! " + e);
			throw new HSQLDBConnectionException("properties file not loaded !", e);
		}
	}
}
