package com.attunedlabs.leap.endpoint;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.bean.DisplayMetaData;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;

/**
 * Responsible for leap type request and response
 * 
 * @author Reactiveworks42
 *
 */
public class LeapEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(LeapEndpoint.class);
	private static final String ENTITY_KIND = "leapEndpointKindName";
	private static final String CONTENT_TYPE_LEAP = "application/vnd.leap+json";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ENTITY_RESPONSE_TAXONOMY = "TaxonomyId";
	private static final String CREATE_DISPLAY_META_DATA = "createDisplayMetaData";
	private static final String HTTP_METHOD = "CamelHttpMethod";
	private static final String HTTP_METHOD_GET = "GET";
	private static final String LEAP_HTTP_METHOD = "LeapEndPointHttpMethod";

	/**
	 * this method validate the request for leap format
	 * 
	 * @param exchange
	 * @throws LeapEndpointException
	 */
	public void validateLeapEndpointRequest(Exchange exchange) throws LeapEndpointException {
		String methodName = "validateLeapEndpointRequest";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Boolean createDisplayMetaData = false;
		try {
			String contentType = exchange.getIn().getHeader(CONTENT_TYPE, String.class);
			if (contentType != null && contentType.equals(CONTENT_TYPE_LEAP)) {
				logger.trace("{} Request is for  leap-endpoint", LEAP_LOG_KEY);
				createDisplayMetaData = true;
				LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
						.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
				LeapDataElement requestContextElement = leapDataContext
						.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
				LeapData leapData = requestContextElement.getData();
				String httpMthod = exchange.getIn().getHeader(LEAP_HTTP_METHOD, String.class);
				if (httpMthod == null || httpMthod.isEmpty()) {
					httpMthod = exchange.getIn().getHeader(HTTP_METHOD, String.class);
				}
				logger.debug("{} httpMthod :: {} ", LEAP_LOG_KEY, httpMthod);
				boolean isInvalidRequest = true;
				if (!httpMthod.equals(HTTP_METHOD_GET)) {
					if (leapData != null) {
						Object leapRequest = leapData.getItems().getData();
						logger.info("{} leap request :: {}", LEAP_LOG_KEY, leapRequest);
						if (leapRequest != null) {
							JSONObject requestJSON = new JSONObject(leapRequest.toString());
							if (requestJSON.has(LeapDataContextConstant.API_VERSION)
									&& requestJSON.has(LeapDataContextConstant.CONTEXT)
									&& requestJSON.has(LeapDataContextConstant.LANG)
									&& requestJSON.has(LeapDataContextConstant.DATA)) {
								logger.debug("request is valid leap-endpoint-request");
								isInvalidRequest = false;
							}
						}
					} else {
						isInvalidRequest = false;
						logger.debug("{} Valid Leap-Endpoint - POST  Type Request with empty body.. ", LEAP_LOG_KEY);
					}
				} else {
					isInvalidRequest = false;
					logger.debug("{} Leap-Endpoint - Get Type Request ", LEAP_LOG_KEY);
				}

				if (isInvalidRequest) {
					throw new LeapEndpointException("Request is not vaild for Leap-Endpoint ", new Throwable(),
							"Invalid Leap-Endpoint request", 400);
				}
			} else {
				logger.debug("{} Request is not leap endpoint request", LEAP_LOG_KEY);
			}
			exchange.getIn().setHeader(CREATE_DISPLAY_META_DATA, createDisplayMetaData);
		} catch (LeapEndpointException e) {
			logger.error("{} error: {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} Invalid Leap Endpoint Request ", LEAP_LOG_KEY, e);
			throw new LeapEndpointException(e.getMessage(), new Throwable(), "Request is not vaild for Leap-Endpoint ",
					400);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * this method create the leap type response
	 * 
	 * @param exchange
	 * @throws LeapEndpointException
	 */
	public void getResponseForLeapEndpoint(Exchange exchange) throws LeapEndpointException {
		String methodName = "getResponseForLeapEndpoint";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			String contentType = exchange.getIn().getHeader(CONTENT_TYPE, String.class);
			Object responseObject = null;
			if (contentType != null && contentType.equals(CONTENT_TYPE_LEAP)) {
				logger.trace("{} Response is for leap-endpoint", LEAP_LOG_KEY);
				LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
						.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
				LeapDataElement responseContextElement = leapDataContext
						.getContextElement(LeapDataContextConstant.RESPONSE_CONTEXT);
				String responseTaxonomyId = leapDataContext.getServiceDataContext().getVendorTaxonomyId();
				logger.info("{} response Taxonomy Id :: {}", LEAP_LOG_KEY, responseTaxonomyId);
				if (responseContextElement != null) {
					responseObject = responseContextElement.getData();
				}
				if (responseObject == null) {
					responseObject = exchange.getIn().getBody();
					if (responseObject == null)
						responseObject = new Object();
				}
				logger.trace("{} response befor leap-endpoint response ", LEAP_LOG_KEY);
				String kind = leapDataContext.getServiceDataContext().getCurrentLeapServiceRuntimeContext()
						.getServiceName();
				if (kind.equals("data")) {
					kind = (String) exchange.getIn().getHeader(ENTITY_KIND);
					responseTaxonomyId = (String) exchange.getIn().getHeader(ENTITY_RESPONSE_TAXONOMY);
				}
				if (responseTaxonomyId != null)
					responseObject = leapDataContext.getLDCResponse(responseObject, kind, responseTaxonomyId);
				else
					responseObject = leapDataContext.getLDCResponse(responseObject, kind);

				logger.debug("{} response of leap-endpoint {}", LEAP_LOG_KEY, responseObject.toString());
				exchange.getIn().setBody(responseObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{} Unable to get LeapEndpoint response ", LEAP_LOG_KEY, e);
			throw new LeapEndpointException(e.getMessage(), new Throwable(), "Unable to create Leap-Endpoint response ",
					500);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}
}
