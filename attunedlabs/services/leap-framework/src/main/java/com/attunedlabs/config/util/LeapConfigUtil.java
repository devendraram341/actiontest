package com.attunedlabs.config.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.config.ConfigurationConstant.BASE_CONFIG_PATH;
import static com.attunedlabs.config.ConfigurationConstant.BASE_PATH;
import static com.attunedlabs.config.ConfigurationConstant.ENV_CONFIG_FOLDER;
import static com.attunedlabs.config.ConfigurationConstant.PROFILE_ID_PROPERTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.mchange.util.AssertException;

import ch.qos.logback.classic.ClassicConstants;

public class LeapConfigUtil {
	private static final String LOGGER_FILE_NAME = "logback.xml";
	private static final String APPS_DEPLOYMENT_ENV_CONFIG = "globalAppDeploymentConfig.properties";
	private static final String USER_DIR = "user.dir";
	private static Logger logger;
	private static Properties globalProperties = new Properties();
	private static Properties featureProperties = new Properties();
	private static String configPath = null;
	static {
		try {
			String profileId = getSystemProperty(PROFILE_ID_PROPERTY);
			String basePath = getPathForConfig();
			setSystemProperty(BASE_PATH, basePath);
			configPath = basePath.concat(File.separator).concat(ENV_CONFIG_FOLDER).concat(File.separator)
					.concat(profileId);
			setSystemProperty(BASE_CONFIG_PATH, configPath);
			String logbackXmlLocation = configPath.concat(File.separator).concat(LOGGER_FILE_NAME);
			setSystemProperty(ClassicConstants.CONFIG_FILE_PROPERTY, logbackXmlLocation);
			logger = LoggerFactory.getLogger(LeapConfigUtil.class);
			String globalAppPath = configPath.concat(File.separator).concat(APPS_DEPLOYMENT_ENV_CONFIG);
			InputStream globalAppInputStream = new FileInputStream(globalAppPath);
			globalProperties.load(globalAppInputStream);
			logger.info("{} *** logger initilize completed. ****", LEAP_LOG_KEY);
			logger.info("{}**** {} profile loaded ****", LEAP_LOG_KEY, profileId);

		} catch (Exception e) {
			logger.error("{} unable to load {} ", LEAP_LOG_KEY, APPS_DEPLOYMENT_ENV_CONFIG);
			e.printStackTrace();
		}
	}

	private LeapConfigUtil() {
		throw new AssertException("No com.attunedlabs.config.util.LeapConfigUtit instances for you!");
	}

	/**
	 * This method is been called by FeatureMetaInfoConfigInstaller.
	 *
	 */
	public static void loadLogback() {
	}

	/**
	 * <p>
	 * This method help us to get file either from Feature specific directory or in
	 * resource folder
	 * </p>
	 * <b>Note:</b>
	 * <li>It first check file in feature specific directory. If the file is not
	 * present there the it will check in resource.</li>
	 * <li>If the given file not found in feature specific directory or in classpath
	 * then it will return null</li>
	 * 
	 * @param requestContext
	 * @param fileName       name of the file
	 * @return
	 * @throws LeapConfigUtilException:
	 */
	public static InputStream getFile(RequestContext requestContext, String fileName) throws LeapConfigUtilException {
		try {
			nullOrEmptyCheck(fileName, "file name cannot be null/Empty");
			nullOrEmptyCheck(requestContext, "request Context cannot be null");
			InputStream fileAsStream = getFileFromFeatureSpecificDir(requestContext, fileName);
			if (fileAsStream == null)
				return getResourceAsStream(fileName);
			nullOrEmptyCheck(fileAsStream, fileName + " file not found in resource or feature specific directory");
			return fileAsStream;
		} catch (LeapConfigUtilException e) {
			throw e;
		} catch (Exception e) {
			throw new LeapConfigUtilException(e.getMessage());
		}

	}

	/**
	 * To load the properties file globalProperties
	 * 
	 * @return Properties Object
	 */
	public static Properties getGlobalAppDeploymentConfigProperties() {
		return globalProperties;
	}

	/**
	 * This method help us to get the global properties firstly from the Environment
	 * variables if not found in Environment Variables checking in
	 * globalAppDeploymentConfig.properties file
	 * 
	 * @return String
	 * @throws PropertiesConfigException
	 */
	public static String getGlobalPropertyValue(String key, String defaultPropertyValue)
			throws PropertiesConfigException {
		if (System.getenv(key) != null) {
			logger.info("{} Got the global property value for {}  key from Environment variables.", LEAP_LOG_KEY, key);
			return System.getenv(key);
		} else if (getGlobalAppDeploymentConfigProperties().getProperty(key) != null) {
			logger.info("{} Got the global property value for {}  key from getGlobal app deployment config properties.", LEAP_LOG_KEY, key);
			return getGlobalAppDeploymentConfigProperties().getProperty(key);
		} else {
			logger.warn(
					"{} Unable to get the global property value for {}  key in both Environment variables and globalAppDeploymentConfig.properties file. using defaultValue:: {}",
					LEAP_LOG_KEY, key, defaultPropertyValue);

			return defaultPropertyValue;
		}

	}

	/**
	 * This method help us to get the feature properties firstly from the
	 * Environment variables if not found in Environment Variables then checks in
	 * the properties object passed by the user, if not found in properties then it
	 * will check in the specified file, also if it is not found in the specified
	 * file then returns the default value.
	 * 
	 * @return String
	 * @throws PropertiesConfigException
	 */
	public static String getFeaturePropertyVale(String key, Properties props, RequestContext requestContext,
			String fileName, String defaultPropertyValue) throws PropertiesConfigException {
		try {
			if (System.getenv(key) != null) {
				logger.info("{} Got the feature property value for {}  key from Environment variables. ", LEAP_LOG_KEY, key);
				return System.getenv(key);
			} else if (props != null) {
				if (props.containsKey(key)) {
					logger.info("{} Got the feature property value for {}  key from properties object. ", LEAP_LOG_KEY, key);
					return props.getProperty(key);
				} else {
					logger.warn(
							"{} Unable to get the feature property value for {}  key from properties object. using defaultValue:: {} ",
							LEAP_LOG_KEY, key, defaultPropertyValue);
					return defaultPropertyValue;
				}
			} else if (requestContext != null && fileName != null) {
				if (featureProperties == null) {
					InputStream propsInpustStream = getFile(requestContext, fileName);
					featureProperties.load(propsInpustStream);
					if (featureProperties.containsKey(key)) {
						logger.info("{} Got the feature property value for {}  key from the file : {}.", LEAP_LOG_KEY, key,
								fileName);
						return featureProperties.getProperty(key);
					} else {
						logger.warn(
								"{} Unable to get feature the property value for {}  key from from the file : {}. using defaultValue:: {} ",
								LEAP_LOG_KEY, key, fileName, defaultPropertyValue);
						return defaultPropertyValue;
					}
				} else if (featureProperties.containsKey(key)) {
					logger.info("{} Got the feature property value for {}  key from the file : {} ", LEAP_LOG_KEY, key,
							fileName);
					return featureProperties.getProperty(key);
				} else {
					logger.warn(
							"{} Unable to get feature the property value for {}  key from from the file : {}. using defaultValue:: {} ",
							LEAP_LOG_KEY, key, fileName, defaultPropertyValue);
					return defaultPropertyValue;
				}
			} else {
				logger.warn(
						"{} Unable to get the feature property value for {}  key in  Environment variables ,in properties object and in the specified file. using defaultValue:: {}",
						LEAP_LOG_KEY, key, defaultPropertyValue);
				return defaultPropertyValue;
			}
		} catch (LeapConfigUtilException | IOException e) {
			throw new PropertiesConfigException(e.getMessage());
		}
	}

	/**
	 * This method helps us to set system property cassandra_url
	 * 
	 * @param key
	 * @param value
	 */
	private static void setSystemProperty(String key, String value) {
		System.setProperty(key, value);
	}

	/**
	 * This method helps us to get system property
	 * 
	 * @param systemKey
	 * @return
	 */
	private static String getSystemProperty(String systemKey) {
		return System.getProperty(systemKey);
	}

	/**
	 * This method helps us to get resource from classpath
	 * 
	 * @param resourceName
	 * @return
	 */
	private static InputStream getResourceAsStream(String resourceName) {
		logger.debug("{} getting {} file from classpath", LEAP_LOG_KEY, resourceName);
		return LeapConfigUtil.class.getClassLoader().getResourceAsStream(resourceName);
	}

	private static String getPathForConfig() {
		String userDir = getSystemProperty(USER_DIR);
		Path path = Paths.get(userDir).getParent().getParent();
		return path.toString();
	}

	/**
	 * <p>
	 * This method is used to get the file from feature specific folder and return
	 * it as FileInputStream.
	 * </p>
	 * <p>
	 * <i>Exmple:</i> ../attunedlabs/config/{profile}/{FG}/{FN}/{Impl}/{Vendor}
	 * </p>
	 * <b>Note</b> If the file does not exist in feature specific folder then it
	 * return null
	 * </p>
	 * 
	 */
	private static FileInputStream getFileFromFeatureSpecificDir(RequestContext requestContext, String fileName) {
		String methodName = "getFileFromFeatureSpecificDir";
		logger.debug("{} entered into the method {} fileName:{}", LEAP_LOG_KEY, methodName, fileName);
		try {
			String featureLvlConfigPath = getFeatreConfigFolderStructure(requestContext);
			String fileLoc = configPath.concat(File.separator).concat(featureLvlConfigPath).concat(File.separator)
					.concat(fileName);
			logger.info("{} {} Absolute file path is : ", LEAP_LOG_KEY, fileName, fileLoc);
			return new FileInputStream(new File(fileLoc));
		} catch (FileNotFoundException e) {
			logger.error("{} unable to find the file:{} :: error:- {}", LEAP_LOG_KEY, fileName, e.getMessage());
			return null;
		}
	}

	/**
	 * This method is used to check given object is null or not
	 * 
	 * @param obj
	 * @param text
	 * @throws LeapDataServiceConfigurationException
	 */
	private static void nullOrEmptyCheck(Object obj, final String text) throws LeapConfigUtilException {
		if (obj == null) {
			logger.error("{} {}", LEAP_LOG_KEY, text);
			throw new LeapConfigUtilException(text);
		}
		if (obj instanceof String && obj.toString().isEmpty()) {
			logger.error("{} {}", LEAP_LOG_KEY, text);
			throw new LeapConfigUtilException(text);
		}

	}

	private static String getFeatreConfigFolderStructure(RequestContext requestContext) {
		StringBuilder featureConfigFolderStructure = new StringBuilder().append(requestContext.getFeatureGroup())
				.append(File.separator).append(requestContext.getFeatureName()).append(File.separator)
				.append(requestContext.getImplementationName()).append(File.separator)
				.append(requestContext.getVendor());
		return featureConfigFolderStructure.toString();
	}

}
