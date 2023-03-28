package com.attunedlabs.featureinstaller;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * This class is used to capture the profile while building the project and
 * write into the profile.properties which is present in the classpath.
 * 
 * @author Reactiveworks
 *
 */
public class ProfileIdLoader {
	private static final String PROFILE_ID = "profileId";
	private static final String PROFILE_PROPERTIES = "/src/main/resources/Profile.properties";

	public static void main(String[] args) throws Exception {
		String profile = System.getProperty(PROFILE_ID);
		Properties profileProp = new Properties();
		profileProp.put(PROFILE_ID, profile);
		OutputStream outputStream = null;
		try {
			String userdir = System.getProperty("user.dir");
			outputStream = new FileOutputStream(userdir + PROFILE_PROPERTIES);
			profileProp.store(outputStream, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

	}

}
