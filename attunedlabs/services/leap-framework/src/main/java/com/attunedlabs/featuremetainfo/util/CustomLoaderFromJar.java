package com.attunedlabs.featuremetainfo.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLoaderFromJar extends URLClassLoader {
	final static Logger logger = LoggerFactory.getLogger(CustomLoaderFromJar.class);

	private URL[] urls;

	public CustomLoaderFromJar(URL[] urls) {
		super(urls);
		this.urls = urls;
	}

	@Override
	public URL getResource(String name) {
		String methodName = "getResource";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		/*
		 * eliminate the "/" at the beginning if there is one to avoid conflicts with
		 * the "!/" at the end of the URL
		 */
		if (name.startsWith("/"))
			name.substring(1);
		/*
		 * prepend "jar:" to indicate that the URL points to a jar file and use "!/" as
		 * the separator to indicate that the resource being loaded is inside the jar
		 * file
		 */
		String URLstring = "jar:" + getURLs()[0] + "!/" + name;
		logger.trace("{} URL string :{}", LEAP_LOG_KEY, URLstring);
		try {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return new URL(URLstring);
		} catch (MalformedURLException exception) {
			logger.error("{} There was something wrong with the URL representing this resource!", LEAP_LOG_KEY);
			logger.error("{} URL= {}", LEAP_LOG_KEY, URLstring);
			return null;
		}
	}// end of method

}
