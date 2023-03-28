package com.attunedlabs.permastore.config.impl;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for calling the Camel route over HTTP 
 * and caches its json response as JSon String.
 * @author amit
 *
 */
public class PermaStoreCamelRouteCaheObjectBuilder {
	final Logger logger = LoggerFactory.getLogger(PermaStoreConfigBuilderHelper.class);
	private String serverURL;
	public Serializable loadDataForCache(){
		
		return null;
	}
	
	
	public void callCamelRouteOverRest(Object msg) {
		String methodName = "callCamelRouteOverRest";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			HttpPost postRequest = new HttpPost(serverURL);
			
			StringEntity input = new StringEntity(msg.toString());
			logger.debug("{} dispatch msg : " ,LEAP_LOG_KEY, input);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 201) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		} finally {
			logger.trace("{} inside finally of rest client",LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}
}
