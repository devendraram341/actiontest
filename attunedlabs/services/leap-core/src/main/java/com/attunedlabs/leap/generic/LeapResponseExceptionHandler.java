package com.attunedlabs.leap.generic;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;
import com.attunedlabs.core.feature.exception.LeapBadRequestException;
import com.attunedlabs.core.feature.exception.LeapValidationFailureException;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.i18n.exception.LocaleResolverException;
import com.attunedlabs.leap.i18n.service.ILeapResourceBundleResolver;
import com.attunedlabs.leap.i18n.service.LeapResourceBundleResolverImpl;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

public class LeapResponseExceptionHandler implements Processor {

	static Logger logger = LoggerFactory.getLogger(LeapResponseExceptionHandler.class);

	/**
	 * Handler processed across the below process method. Which will identify the
	 * type to set the respective response formats and codes
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName = "process";
		logger.debug("entered into the method {}.{}()", getClass().getName(), methodName);
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		String developerMessage;
		Integer status;
		Long errorCode;
		String userMessage;
		String feature;
		Long vendorErrorCode = 400L;
		String vendorErrorMessage = null;
		String vendorID = null;
		if (exception instanceof LeapBadRequestException) {
			LeapBadRequestException badRequestException = (LeapBadRequestException) exception;
			developerMessage = badRequestException.getDeveloperMessage();
			status = (int) badRequestException.getStatusCode();
			errorCode = badRequestException.getAppErrorCode();
			feature = badRequestException.getFeature();
			userMessage = getLocalizedMessage(badRequestException.getUserMessage(), exchange).toString();
			vendorID = badRequestException.getVendorID();
			if (vendorID == null) {
				vendorID = "";
			}
			vendorErrorCode = badRequestException.getVendorErrorCode();
			vendorErrorMessage = badRequestException.getVendorErrorMessage();
			setResponseData(developerMessage, userMessage, status, errorCode, feature, vendorID, vendorErrorCode,
					vendorErrorMessage, exchange);
		} else if (exception instanceof LeapAuthorizationFailedException) {
			LeapAuthorizationFailedException authorizationFailedException = (LeapAuthorizationFailedException) exception;
			developerMessage = authorizationFailedException.getDeveloperMessage();
			status = LeapAuthorizationFailedException.RESPONSE_CODE;
			errorCode = authorizationFailedException.getAppErrorCode();
			userMessage = getLocalizedMessage(authorizationFailedException.getUserMessage(), exchange).toString();
			feature = authorizationFailedException.getFeature();
			vendorID = authorizationFailedException.getVendorID();
			if (vendorID == null) {
				vendorID = "";
			}
			vendorErrorCode = authorizationFailedException.getVendorErrorCode();
			vendorErrorMessage = authorizationFailedException.getVendorErrorMessage();
			setResponseData(developerMessage, userMessage, status, errorCode, feature, vendorID, vendorErrorCode,
					vendorErrorMessage, exchange);
		} else if (exception instanceof LeapValidationFailureException) {
			LeapValidationFailureException validationFailureException = (LeapValidationFailureException) exception;
			developerMessage = validationFailureException.getDeveloperMessage();
			status = LeapValidationFailureException.RESPONSE_CODE;
			errorCode = validationFailureException.getAppErrorCode();
			userMessage = getLocalizedMessage(validationFailureException.getUserMessage(), exchange).toString();
			feature = validationFailureException.getFeature();
			vendorID = validationFailureException.getVendorID();
			if (vendorID == null) {
				vendorID = "";
			}
			vendorErrorCode = validationFailureException.getVendorErrorCode();
			vendorErrorMessage = validationFailureException.getVendorErrorMessage();
			setResponseData(developerMessage, userMessage, status, errorCode, feature, vendorID, vendorErrorCode,
					vendorErrorMessage, exchange);
		} else {
			developerMessage = exception.getMessage();
			status = 500;
			errorCode = 500L;
			feature = exchange.getIn().getHeader(LeapResponseHandlerConstant.FEATURE, String.class);
			setResponseData(developerMessage, LeapResponseHandlerConstant.INTERNAL_ERROR, status, errorCode, feature,
					vendorID, vendorErrorCode, vendorErrorMessage, exchange);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method

	/**
	 * Setting error response data from the subClass
	 * 
	 * @param developerMessage
	 * @param userMessage
	 * @param status
	 * @param errorCode
	 * @param exchange
	 * @throws JSONException
	 * @throws SOAPException
	 * @throws IOException
	 */
	public void setResponseData(String developerMessage, String userMessage, Integer status, long errorCode,
			String feature, String vendorID, long vendorErrorCode, String vendorErrorMessage, Exchange exchange)
			throws JSONException, SOAPException, IOException {
		String methodName = "setResponseData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Message message = exchange.getIn();
		logger.trace("{} Exchange Headers ::{}", LEAP_LOG_KEY, exchange.getIn().getHeaders());
		String featureGroup = message.getHeader(LeapResponseHandlerConstant.FEATURE_GROUP, String.class);
		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		logger.debug("{} leapDataCtx :: {}", LEAP_LOG_KEY, leapDataCtx);
		LeapDataElement leapDataElement = null;
		String apiVersion = null;
		String context = null;
		String lang = null;
		String requestUUID = null;
		if (leapDataCtx != null) {
			leapDataElement = leapDataCtx.getContextElement(LeapDataContextConstant.INITIAL_CONTEXT);
			apiVersion = leapDataElement.getApiVersion();
			context = leapDataElement.getContext();
			lang = leapDataElement.getLang();
			requestUUID = leapDataCtx.getServiceDataContext().getRequestUUID();
		} else {
			apiVersion = "1.0";
			context = "defaultContext";
			lang = "en";
			requestUUID = UUID.randomUUID().toString().substring(0, 7);
		}

		if (feature == null || feature.isEmpty()) {
			feature = exchange.getIn().getHeader(LeapResponseHandlerConstant.FEATURE, String.class);
		}
		String serviceName = message.getHeader(LeapResponseHandlerConstant.SERVICE_NAME, String.class);

		JSONObject finalResponse = new JSONObject();
		JSONObject error = new JSONObject();
		JSONArray errorText = new JSONArray();
		JSONObject errorTextobj = new JSONObject();
		errorText.put(errorTextobj);
		error.put(LeapResponseHandlerConstant.ERROR_CODE, errorCode);
		error.put(LeapResponseHandlerConstant.ERROR_TEXT, errorText);
		JSONObject developerInformation = new JSONObject();
		error.put(LeapResponseHandlerConstant.DEVLOPER_INFORMATION, developerInformation);
		finalResponse.put(LeapResponseHandlerConstant.ERROR, error);
		finalResponse.put(LeapResponseHandlerConstant.ID, requestUUID);

		if (apiVersion != null) {
			finalResponse.put(LeapResponseHandlerConstant.API_VERSON, apiVersion);
		} else {
			finalResponse.put(LeapResponseHandlerConstant.API_VERSON, LeapResponseHandlerConstant.API_VERSION_VAL);
		}

		if (context != null) {
			finalResponse.put(LeapResponseHandlerConstant.CONTEXT, context);
		} else {
			finalResponse.put(LeapResponseHandlerConstant.CONTEXT, LeapResponseHandlerConstant.CONTEXT);
		}
		if (lang != null) {
			errorTextobj.put(LeapResponseHandlerConstant.LANG, lang);
		} else {
			errorTextobj.put(LeapResponseHandlerConstant.LANG, LeapResponseHandlerConstant.LANG_VAL);
		}
		errorTextobj.put(LeapResponseHandlerConstant.TEXT, userMessage);
		developerInformation.put(LeapResponseHandlerConstant.DOMAIN, featureGroup + "/" + feature);
		developerInformation.put(LeapResponseHandlerConstant.LOCATION, serviceName);
		developerInformation.put(LeapResponseHandlerConstant.DEVELOPER_MESSAGE, developerMessage);
		if (vendorErrorMessage != null) {
			JSONObject vendorDetails = new JSONObject();
			vendorDetails.put(LeapResponseHandlerConstant.VENDOR_ID, vendorID);
			vendorDetails.put(LeapResponseHandlerConstant.VENDOR_ERROR_CODE, vendorErrorCode);
			vendorDetails.put(LeapResponseHandlerConstant.VENDOR_ERROR_MESSAGE, vendorErrorMessage);
			developerInformation.put(LeapResponseHandlerConstant.VENDOR_DETAILS, vendorDetails);
		}
		message.setHeader(Exchange.HTTP_RESPONSE_CODE, status);
		message.setHeader(LeapResponseHandlerConstant.ACCESS_CONTROL_ALLOW_ORIGIN,
				LeapResponseHandlerConstant.ACCESS_CONTROL_ALLOW_ASTERISK_VALUE);
		message.setHeader(LeapResponseHandlerConstant.ACCESS_CONTROL_ALLOW_METHODS,
				message.getHeader(Exchange.HTTP_METHOD));
		message.setHeader(LeapResponseHandlerConstant.ACCESS_CONTROL_ALLOW_HEADERS,
				LeapResponseHandlerConstant.ACCESS_CONTROL_ALLOW_ASTERISK_VALUE);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);

		try {
			LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
			if (leapServiceContext.getEndpointType().equals(LeapResponseHandlerConstant.ENDPOINT_XML)) {
				message.setBody(XML.toString(finalResponse));
			} else if (leapServiceContext.getEndpointType().equals(LeapResponseHandlerConstant.ENDPOINT_SOAP)) {
				SOAPMessage soapMessage = createSoapFault(developerMessage, userMessage, errorCode, message, feature);
				message.setBody(soapMessage);
			} else {
				message.setBody(finalResponse);
			}
		} catch (Exception e) {
			message.setBody(finalResponse);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * To construct the soapFault message when endpoint is CXF
	 * 
	 * @param developerMessage
	 * @param userMessage
	 * @param errorCode
	 * @param message
	 * @param status
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	private static SOAPMessage createSoapFault(String developerMessage, String userMessage, Long errorCode,
			Message message, String feature) throws SOAPException, IOException {
		String featureGroup = message.getHeader(LeapResponseHandlerConstant.FEATURE_GROUP, String.class);
		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
		SOAPFault fault = body.addFault();
		fault.setFaultCode(errorCode.toString());
		fault.setFaultString(userMessage);
		SOAPElement detailElement = fault.addChildElement(LeapResponseHandlerConstant.DETAIL);
		detailElement.addChildElement(LeapResponseHandlerConstant.DEVELOPER_MESSAGE).addTextNode(developerMessage);
		detailElement.addChildElement(LeapResponseHandlerConstant.FEATURE).addTextNode(feature);
		detailElement.addChildElement(LeapResponseHandlerConstant.INFO).addTextNode(
				LeapResponseHandlerConstant.BASE_URL_INFO + featureGroup + "/" + feature + "/" + errorCode);
		return soapMessage;
	}// ..end of the private method

	/**
	 * Method to get localized messages
	 * 
	 * @param content
	 * @param exchange
	 * @return
	 */
	private static Object getLocalizedMessage(Object content, Exchange exchange) {
		ILeapResourceBundleResolver bundleResolver = new LeapResourceBundleResolverImpl();
		ResourceBundle resourceBundle;
		Object message = "";
		String localeId;
		if (exchange.getIn().getHeaders().containsKey("locale")) {
			localeId = (String) exchange.getIn().getHeader("locale");
			logger.trace("localeId in if :{} ", localeId);
		} else {
			localeId = "en_US";
			return content;
		}

		String tenantId, siteId, featureName;
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		tenantId = leapServiceContext.getTenant();
		siteId = leapServiceContext.getSite();
		featureName = leapServiceContext.getFeatureName();

		try {
			resourceBundle = bundleResolver.getLeapLocaleBundle(tenantId, siteId, featureName, localeId, exchange);
			if (content instanceof java.lang.String) {
				if (resourceBundle.containsKey(content.toString())) {
					message = resourceBundle.getString(content.toString());
					return message;
				}
			} else if (content instanceof java.lang.Integer) {
				if (resourceBundle.containsKey(content.toString())) {
					message = Integer.parseInt(resourceBundle.getString(content.toString()));
					return message;
				}
			}
		} catch (LocaleResolverException e) {
			e.printStackTrace();
		}

		return content;
	}
}
