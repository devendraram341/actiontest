package com.attunedlabs.baseroute.util;

import java.io.IOException;
import java.util.Properties;

import com.attunedlabs.LeapCoreTestConstant;

public class getPropertiesForTesting {

	private static getPropertiesForTesting instance = null;
	private Properties properties;

	protected getPropertiesForTesting() throws IOException {

		properties = new Properties();
		properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream(LeapCoreTestConstant.PROPERTIES_FILE));

	}

	public static getPropertiesForTesting getInstance() {
		if (instance == null) {
			try {
				instance = new getPropertiesForTesting();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return instance;
	}

	public String getValue(String key) {
		return properties.getProperty(key);
	}
}