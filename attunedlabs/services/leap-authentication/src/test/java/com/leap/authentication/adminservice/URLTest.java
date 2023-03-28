package com.leap.authentication.adminservice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLTest {
	private static Logger logger = LoggerFactory.getLogger(URLTest.class);

	public boolean isValidUrl(String src) {
		try {
			URL url = new URL(src);
			URLConnection urlConnection = url.openConnection();
			logger.debug(urlConnection.toString());
			HttpURLConnection connection = (HttpURLConnection) urlConnection;
			connection.setRequestMethod("HEAD");
			logger.debug("response code from url :" + connection.getResponseCode());
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				logger.info("Invalid Url: " + src);
				return false;
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Test
	public void testUrl(){
		// https://secure.brainpop.com/new_common_images/ipad_movies/27/273990.mp4
		String url ="https://secure.brainpop.com/new_common_images/ipad_movies/27/273727.mp4";
		boolean falgValue = isValidUrl(url);
		logger.debug("falgValue  from url : "+falgValue);
	}
}
