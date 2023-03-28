package com.attunedlabs.baseroute.util;

import java.io.IOException;
import java.util.Properties;

import com.attunedlabs.LeapCoreTestConstant;

public class PropertiesForTesting {

	private static PropertiesForTesting instance = null;
	private Properties properties;

	protected PropertiesForTesting() throws IOException {

		properties = new Properties();
		properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream(LeapCoreTestConstant.PROPERTIES_FILE));

	}

	public static PropertiesForTesting getInstance() {
		if (instance == null) {
			try {
				instance = new PropertiesForTesting();
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