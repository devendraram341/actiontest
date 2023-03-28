package com.attunedlabs.staticconfig.factory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.staticconfig.IStaticConfigurationService;
import com.attunedlabs.staticconfig.StaticConfigInitializationException;
import com.attunedlabs.staticconfig.impl.FileStaticConfigurationServiceImpl;
import com.attunedlabs.zookeeper.staticconfig.service.impl.ZookeeperFilemanagementServiceImpl;

public class StaticConfigurationFactory {

	private IStaticConfigurationService iStaticConfigurationService;
	private static String staticConfigImpl;
	static final String STATIC_FILECONFIG_IMPL_KEY = "staticconfigimpl";
	static final String STATIC_FILE_CONFIG_PROP_KEY = "globalAppDeploymentConfig.properties";
	static {
		try {
			staticConfigImpl = (String) LeapConfigUtil.getGlobalPropertyValue(STATIC_FILECONFIG_IMPL_KEY,LeapDefaultConstants.DEFAULT_STATIC_FILECONFIG_IMPL_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// ..end of static propertiesLoader

	/**
	 * Default constructor
	 */
	public StaticConfigurationFactory() {

	}

	/**
	 * Parameterized constructor kept , if actual Strategy pattern is
	 * implemented
	 * 
	 * @param iStaticConfigurationService
	 */
	public StaticConfigurationFactory(IStaticConfigurationService iStaticConfigurationService) {
		this.iStaticConfigurationService = iStaticConfigurationService;
	}

	/**
	 * Static method to get the instance of FileManagerServices
	 * 
	 * @param isZookeeperFilemanager
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws StaticConfigInitializationException
	 */
	public static IStaticConfigurationService getFilemanagerInstance()
			throws InstantiationException, IllegalAccessException, StaticConfigInitializationException {
		if ("com.attunedlabs.staticconfig".equals(staticConfigImpl)) {
			return FileStaticConfigurationServiceImpl.class.newInstance();
		} else if ("com.attunedlabs.zookeeper.staticconfig".equals(staticConfigImpl)) {
			return ZookeeperFilemanagementServiceImpl.class.newInstance();
		} else {
			throw new StaticConfigInitializationException("Unable to identify the implementation");
		}
	}// ..end of the static method

}
