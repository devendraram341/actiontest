package com.attunedlabs.leap.eventsubscription.processor;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.HeaderParams;
import com.attunedlabs.eventframework.jaxb.HttpPostRequest;
import com.attunedlabs.eventsubscription.exception.NonRetryableException;
import com.attunedlabs.eventsubscription.exception.RetryableException;
import com.attunedlabs.eventsubscription.exception.ServiceCallInvocationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

/**
 * <code>HttpPostRequestProcessor</code> will apply the routing-rule specified
 * by the particular subscriber and forms the new exchange which is forwarded to
 * the service configured by the subscriber if the rule evaluates successfully
 * else drops the message for the following subscriber.
 * 
 * @author Reactiveworks42
 *
 */
public class HttpPostRequestProcessor implements Processor {
	final static Logger log = LoggerFactory.getLogger(HttpPostRequestProcessor.class);

	private SubscriptionUtil subscriptionUtil;

	public HttpPostRequestProcessor(SubscriptionUtil subscriptionUtil) {
		this.subscriptionUtil = subscriptionUtil;
	}

	/**
	 * adds some extra service headers and makes the async call to service by
	 * constructing URL string.
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		log.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		String subscriptionId = exchange.getIn().getHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, String.class);

		String tenant = serviceDataContext.getTenant();
		String site = serviceDataContext.getSite();

		List<String> configParams = Arrays
				.asList(subscriptionId.split(EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER));

		try {
			if (configParams.size() - 1 == 5) {
				String featureGroup = configParams.get(0);
				String featureName = configParams.get(1);
				String implementation = configParams.get(2);
				String vendor = configParams.get(3);
				String version = configParams.get(4);

				if (tenant == null || site == null || tenant.trim().isEmpty() || site.trim().isEmpty())
					throw new ServiceCallInvocationException(
							"TENANT/SITE DOESN'T EXISTS :tenantId and siteId not found in eventHeaders of "
									+ "http post request invocation failed...");

				serviceDataContext.setFeatureGroup(featureGroup);
				serviceDataContext.setFeatureName(featureName);
				serviceDataContext.setImplementationName(implementation);
				serviceDataContext.setVendor(vendor);
				serviceDataContext.setVersion(version);
				serviceDataContext.setEndpointType("HTTP-JSON");
			}

			if (exchange.getIn() != null) {

				// get the data from the exchange.
				String eventBody = exchange.getIn().getBody(String.class);
				String topicName = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);

				HttpPostRequest httpPostRequest = exchange.getIn().getHeader(SubscriptionConstant.HTTP_POST_REQUEST_KEY,
						HttpPostRequest.class);

				// holds the feature-info about the
				// particular route.
				Map<String, HashMap<String, String>> actionSpecificMap = new HashMap<>();
				actionSpecificMap = subscriptionUtil.addExtraHeadersToEndpoint(actionSpecificMap, httpPostRequest);

				if (httpPostRequest != null) {
					HashMap<String, String> serviceHeaderMap = actionSpecificMap
							.get(SubscriptionConstant.HTTP_POST_REQUEST_SERVICE_CALL_KEY);
					String featureGroupForInvoke = serviceHeaderMap.get(LeapHeaderConstant.FEATURE_GROUP_KEY);
					String featureNameForInvoke = serviceHeaderMap.get(LeapHeaderConstant.FEATURE_KEY);
					String serviceNameForInvoke = serviceHeaderMap.get(LeapHeaderConstant.SERVICENAME_KEY);
					String hostName = serviceHeaderMap.get(SubscriptionConstant.HOST_NAME_KEY);
					String portAddress = serviceHeaderMap.get(SubscriptionConstant.PORT_ADDRESS_KEY);

					String url = "";
					if (eventBody.trim().startsWith("<") && eventBody.trim().endsWith(">"))
						url = "http://" + hostName + ":" + portAddress + "/ecomm/xml/" + featureGroupForInvoke + "/"
								+ featureNameForInvoke + "/" + serviceNameForInvoke + "";
					else
						url = "http://" + hostName + ":" + portAddress + "/ecomm/rest/" + featureGroupForInvoke + "/"
								+ featureNameForInvoke + "/" + serviceNameForInvoke + "";

					Message outMessage = exchange.getOut();

					// default attribute adding on every exchange for
					// the subscriber to identify topic.
					outMessage.setHeader(KafkaConstants.TOPIC, topicName);
					outMessage.setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriptionId);

					outMessage.setBody(eventBody);

					/* HTTPCLIENT AND HTTPPOST OOBJECT */
					HttpClient httpClient = HttpClientBuilder.create().build();
					HttpPost request = new HttpPost(url);

					// default header for subscriber to verify.
					request.setHeader(KafkaConstants.TOPIC, topicName);
					request.setHeader(SubscriptionConstant.SUBSCRIPTION_ID_KEY, subscriptionId);
					// request.setHeader(LeapHeaderConstant.LEAP_HEADER_KEY,
					// leapHeader.toString());

					// add the headers in request
					HeaderParams headerParams = httpPostRequest.getHeaderParams();
					// add the headers in request
					SubscriptionUtil.addHeaderParamsInHeader(request, headerParams);

					/* JSON AS STRINGENTITY */
					StringEntity input = null;
					try {
						input = new StringEntity(eventBody);
					} catch (UnsupportedEncodingException e) {
						throw new ServiceCallInvocationException("FORMATTING EVENTBODY FAILED: " + e.getMessage());
					}
					request.setEntity(input);

					/* SEND AND RETRIEVE RESPONSE */
					HttpResponse response = null;
					try {
						response = httpClient.execute(request);
					} catch (IOException e) {
						throw new ServiceCallInvocationException(
								"SERVICE INVOKATION FAILED: connection problem or protocol error arised"
										+ e.getMessage());
					}
					log.debug("{} STATUS CODE {}", LEAP_LOG_KEY, response.getStatusLine().toString());

					/* RESPONSE AS JSON STRING */
					String result = null;
					try {
						result = IOUtils.toString(response.getEntity().getContent(),
								SubscriptionConstant.CHARSET_UTF_8_FORMAT);

						if (response.getStatusLine().getStatusCode() >= 500)
							throw new ServiceCallInvocationException("SERVICE INVOKATION FAILED: Response Code : "
									+ response.getStatusLine().getStatusCode() + " Reason : "
									+ response.getStatusLine().getReasonPhrase() + " Response : " + result);
						else if (response.getStatusLine().getStatusCode() != 200
								&& response.getStatusLine().getStatusCode() != 201)
							throw new ServiceCallInvocationException("PROCESS REQUEST FAILED: Response Code : "
									+ response.getStatusLine().getStatusCode() + " Reason : "
									+ response.getStatusLine().getReasonPhrase() + " Response : " + result);

						// set exchange with headers and body
						// Message outMessage = exchange.getOut();
						// set all headers
						Header[] headers = response.getAllHeaders();
						if (headers != null)
							for (Header header : headers) {
								outMessage.setHeader(header.getName(), header.getValue());
							}
						// set body
						outMessage.setBody(result);
						outMessage.setHeader(LEAP_DATA_CONTEXT, leapDataContext);

					} catch (IOException e) {
						throw new ServiceCallInvocationException(
								"SERVICE RESPONSE DECODING FAILED: response failed to decode due to " + e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			if (exchange.hasOut())
				exchange.getOut().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			else
				exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);

			if (e.getMessage() != null && !(e instanceof JSONException)) {
				if (e.getMessage().contains("FORMATTING EVENTBODY FAILED")
						|| e.getMessage().contains("SERVICE RESPONSE DECODING FAILED")
						|| e.getMessage().contains("PROCESS REQUEST FAILED")
						|| e.getMessage().contains("TENANT/SITE DOESN'T EXISTS"))
					throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);
				else
					throw new RetryableException("RETRYABLE[" + e.getMessage() + "]", e);
			} else
				throw new NonRetryableException("NON-RETRYABLE[" + e.getMessage() + "]", e);
		}

	}
}
