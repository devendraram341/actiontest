package com.attunedlabs.featuremetainfo.util;

import static com.attunedlabs.config.ConfigurationConstant.BASE_CONFIG_PATH;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.attunedlabs.config.ConfigurationContext;

public class FeatureMetaInfoUtil {
	private static final String LEAP_HOME_CONFIG_PATH = System.getProperty(BASE_CONFIG_PATH);

	/**
	 * This method help us to get desire file by checking in feature specific folder
	 * and resource folder </br>
	 * </br>
	 * Note: If the give file doesn't present in feature specific folder or in
	 * resource folder it will return null
	 * 
	 * @param resourceName  : Name of the file
	 * @param configContext
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL checkAndGetResourceUrl(String resourceName, ConfigurationContext configContext)
			throws MalformedURLException {
		URL resourceUrl = null;
		String externalEventPath = getFeatureSpecificDirectoryLocOfFile(resourceName, configContext);
		File eventFile = new File(externalEventPath);
		if (eventFile.exists() && eventFile.isFile()) {
			resourceUrl = eventFile.toURI().toURL();
		} else {
			resourceUrl = FeatureMetaInfoResourceUtil.class.getClassLoader().getResource(resourceName);
		}
		return resourceUrl;
	}

	/**
	 * This method helps us to get location of the give file from the feature
	 * specific folder
	 * 
	 * @param fileName      : name of the file
	 * @param configContext
	 * @return
	 */
	private static String getFeatureSpecificDirectoryLocOfFile(String fileName, ConfigurationContext configContext) {
		StringBuilder stingBuilder = new StringBuilder();
		stingBuilder.append(LEAP_HOME_CONFIG_PATH);
		stingBuilder.append(File.separator);
		stingBuilder.append(configContext.getFeatureGroup());
		stingBuilder.append(File.separator);
		stingBuilder.append(configContext.getFeatureName());
		stingBuilder.append(File.separator);
		stingBuilder.append(configContext.getImplementationName());
		stingBuilder.append(File.separator);
		stingBuilder.append(configContext.getVendorName());
		stingBuilder.append(File.separator);
		stingBuilder.append(fileName);
		return stingBuilder.toString();
	}
}
