package com.attunedlabs.featureinstaller;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceException;
import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceUtil;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.TenantSitePropertiesLoader;
import com.attunedlabs.leap.TenantSitePropertiesLoadingException;
import com.attunedlabs.leap.load.resource.CamelApplicationRun;
import com.attunedlabs.leap.logging.LeapLoggingConstants;


/**
 * This method is used to search featureMetaInfo in all jars available in
 * classpath and load resources available at feature level
 * 
 * @author Reactiveworks
 * 
 */
public class FeatureMetaInfoConfigInstaller {
	private static Logger logger;
	public static final String PATTERN_SEARCH_KEY = "featureMetaInfo.xml";
	public static final String PROFILE_ID_PROPERTY = "profileId";
	private static final String PROFILE_PROPERTIES_FILE_NAME = "Profile.properties";
	static {
		try {
			InputStream resourceAsStream = FeatureMetaInfoConfigInstaller.class.getClassLoader()
					.getResourceAsStream(PROFILE_PROPERTIES_FILE_NAME);
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			System.setProperty(PROFILE_ID_PROPERTY, properties.getProperty(PROFILE_ID_PROPERTY));
			LeapConfigUtil.loadLogback();
			logger = LoggerFactory.getLogger(FeatureMetaInfoConfigInstaller.class);
		} catch (Exception e1) {
			logger.error("{} unable to start the application ::{}", LEAP_LOG_KEY, e1.getMessage());
			e1.printStackTrace();

		}

	}

	/**
	 * This method is used to search featureMetaInfo in all jars available in
	 * classpath and load resources available at feature level
	 * 
	 * @throws FeatureMetaInfoConfigInstallerException
	 */
	public void loadFeatureMetaInfoResources() throws FeatureMetaInfoConfigInstallerException {
		String methodName = "loadFeatureMetaInfoResources";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		TenantSitePropertiesLoader propLoader = new TenantSitePropertiesLoader();
		try {

			propLoader.setTenantAndSite();
			logger.trace("tenant :{}, site :{} {}", LeapHeaderConstant.tenant, LeapHeaderConstant.site, LEAP_LOG_KEY);
			FeatureMetaInfoResourceUtil fmiResList = new FeatureMetaInfoResourceUtil();
			Pattern pattern = Pattern.compile(PATTERN_SEARCH_KEY);
			try {
				fmiResList.getClassPathResources(pattern);
				CamelApplicationRun runCamelApplication = new CamelApplicationRun();
				try {
					runCamelApplication.startCamelApplication();
					
				} catch (Exception e) {
					logger.error("Unable to start camel routes: {} {}", e.getMessage(), LEAP_LOG_KEY);
					throw new FeatureMetaInfoConfigInstallerException("Unable start camel application ", e);
				}
			} catch (FeatureMetaInfoResourceException e) {
				throw new FeatureMetaInfoConfigInstallerException(
						"Unable to load " + PATTERN_SEARCH_KEY + " from class path ", e);
			} catch (PropertiesConfigException e) {
				throw new FeatureMetaInfoConfigInstallerException(e.getMessage());
			}
		} catch (TenantSitePropertiesLoadingException e1) {
			throw new FeatureMetaInfoConfigInstallerException("Unable to site and tenant from class path : ");
		}
		logger.debug("exiting the method {} {}", methodName,LEAP_LOG_KEY);
	}// end of method

	public static void main(String[] args) throws FeatureMetaInfoConfigInstallerException {
		cleanAtomikosLogs();
		FeatureMetaInfoConfigInstaller installer = new FeatureMetaInfoConfigInstaller();
		installer.loadFeatureMetaInfoResources();
		cleanAtomikosLogs();

	}

	public static void cleanAtomikosLogs() {
		try {
			File currentDir = new File(".");
			final File[] tmLogs = currentDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".epoch") || name.startsWith("tmlog")) {
						return true;
					}
					return false;
				}
			});
			for (File tmLog : tmLogs) {
				tmLog.delete();
			}
		} catch (Exception e) {
			logger.error("failed to delete atomikos logs ... {} {}", e,LEAP_LOG_KEY);
		}
	}

}