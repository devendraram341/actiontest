package com.leap.authorization.testing.util;

import java.io.IOException;
import java.util.Properties;
import org.json.JSONException;

public class TestUtil {
	/**
	 * Properties object for expected body data.
	 */
	private static Properties props = new Properties();

	/**
	 * Properties object for the request body data.
	 */
	private static Properties requestProps = new Properties();

	/**
	 * static block to load the properties file.
	 */
	static {
		ClassLoader classLoader = TestUtil.class.getClassLoader();
		try {
			props.load(classLoader.getResourceAsStream(TestConstant.EXPECTED_BODY_FILE));
			requestProps.load(classLoader.getResourceAsStream(TestConstant.REQUEST_BODY_FILE));
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}// ..end of the static block

	/**
	 * Utility to set expected body of the service call.
	 * 
	 * @return
	 * @throws JSONException
	 */
	public static String setExpectedBody(String type) {
		return props.getProperty(type);
	}// ..end of the method

	/**
	 * Utility to set request body.
	 * 
	 * @param requestType
	 * @return
	 */
	public static String setRequestBody(String requestType) {
		return requestProps.getProperty(requestType);
	}// ..end of the method
}
