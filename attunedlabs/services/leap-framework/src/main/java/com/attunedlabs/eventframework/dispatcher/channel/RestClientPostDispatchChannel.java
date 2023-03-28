package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.RetryableMessageDispatchingException;

/**
 * jsonConfig is {"postURI": "http://localhost:8080/"}
 * 
 * @author Bizruntime
 *
 */
public class RestClientPostDispatchChannel extends AbstractDispatchChannel {
	final Logger logger = LoggerFactory.getLogger(RestClientPostDispatchChannel.class);
	private String serverURL;// ="http://localhost:8080/";
	private CloseableHttpClient httpClient;

	public RestClientPostDispatchChannel(String channeljsonconfig) throws DispatchChannelInitializationException {
		this.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}

	public RestClientPostDispatchChannel() {

	}

	/**
	 * This method is to dispatch message to rest url as post data
	 * 
	 * @param msg :Object
	 */
	@Override
	public void dispatchMsg(Serializable msg, RequestContext requestContext, String eventId)
			throws MessageDispatchingException {
		String methodName = "dispatchMsg";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			HttpPost postRequest = new HttpPost(serverURL);
			StringEntity input = new StringEntity(msg.toString());
			logger.debug("{} dispatch msg : {}", LEAP_LOG_KEY, input);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			HttpResponse response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 201) {
				throw new RetryableMessageDispatchingException(
						"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}
		} catch (IOException exp) {
			throw new RetryableMessageDispatchingException(
					"RestClientPostDispatchChannel failed to Dispatch EventMsg to Server {" + serverURL + "}", exp);
		} finally {
			logger.debug("{} inside finally of rest client", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is to configure rest url and create rest client
	 * 
	 * @param channeljsonconfig
	 * @throws DispatchChannelInitializationException
	 */
	// #TODO Write clean and better code for Channel.
	public void initializeFromConfig() throws DispatchChannelInitializationException {
		String methodName = "initializeFromConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		parseConfiguration(channeljsonconfig);
		this.httpClient = HttpClients.createDefault();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used to parse json data
	 * 
	 * @param channeljsonconfig
	 * @throws DispatchChannelInitializationException
	 */
	private void parseConfiguration(String channeljsonconfig) throws DispatchChannelInitializationException {
		String methodName = "parseConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(channeljsonconfig);
			JSONObject jsonObject = (JSONObject) obj;
			logger.debug("{} jsonObject : {}", LEAP_LOG_KEY, jsonObject);
			this.serverURL = (String) jsonObject.get("restpath");
			// logger.info("parseConfiguration:serverURL="+serverURL);
		} catch (ParseException e) {
			throw new DispatchChannelInitializationException("RestClientPostDispatchChannel failed to initialize");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void closeResource() throws Throwable {
		try {
			if (httpClient != null)
				httpClient.close();
		} catch (Exception exp) {
			logger.error("{} Error while closing the http restlet client", LEAP_LOG_KEY);
			throw exp;
		}
	}

	protected void finalize() throws Throwable {
		closeResource();
	}

}
