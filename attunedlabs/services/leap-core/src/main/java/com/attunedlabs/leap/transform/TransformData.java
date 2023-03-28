package com.attunedlabs.leap.transform;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.impl.PrettyUrlMappingService;
import com.attunedlabs.config.persistence.impl.PrettyUrlMappingServiceException;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.generic.LeapGenericConstant;
import com.attunedlabs.leap.generic.LeapResponseHandlerConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;

public class TransformData {
	private final Logger logger = LoggerFactory.getLogger(TransformData.class);
	private final static String CAMEL_HTTP_URI_HEADER = "CamelHttpUri";
	private PrettyUrlMappingService prettyService = new PrettyUrlMappingService();

	/**
	 * Custom bean to marshal the XML string to org.Json Object
	 * 
	 * @param exchange
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	public void marshalXmltoJson(Exchange exchange) throws JSONException, RequestDataTransformationException {
		String methodName = "marshalXmltoJson";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			String exchngeContentType = (String) exchange.getIn().getHeader(Exchange.CONTENT_TYPE);
			if (exchngeContentType != null
					&& exchngeContentType.equalsIgnoreCase(LeapGenericConstant.HTTP_CONTENT_TYPE_ENCODED_KEY)) {
				// request is coming for service request of SAC as encoded
				// value
				processRequestForEncodedData(exchange);

			} else {
				// request is for other service
				String bodyIn = exchange.getIn().getBody(String.class);
				// if body is null then check in custom header
				if (bodyIn != null) {
					createJSONRequestFromXMLString(bodyIn, exchange);
				} else {
					throw new RequestDataTransformationException(
							"Unable to transform exchange body as no data availble in exchange body for service : "
									+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
									+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
									+ " and feature group : "
									+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
				}
			}
		} catch (RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException(
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method marshalXmltoJson

	/**
	 * Custom bean to unmarshal the JSON string to XML
	 * 
	 * @param exchange
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	public void unmarshalJsonToXML(Exchange exchange) throws JSONException, RequestDataTransformationException {
		String methodName = "unmarshalJsonToXML";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String bodyIn = exchange.getIn().getBody(String.class);
		if (bodyIn != null) {
			org.json.JSONObject jsonObject = new org.json.JSONObject(bodyIn);
			if (jsonObject.has(LeapDataContextConstant.DATA_KEY)) {
				JSONArray jsonArray = (JSONArray) jsonObject.get(LeapDataContextConstant.DATA_KEY);
				org.json.JSONObject jobj = (org.json.JSONObject) jsonArray.get(0);
				String xmlString = XML.toString(jobj);
				logger.debug("{} unmarshalled - jsonObject to xml: {}", LEAP_LOG_KEY, xmlString);
				exchange.getIn().setBody(xmlString);
			} else {
				String xmlString = XML.toString(jsonObject);
				logger.debug("{} unmarshalled - jsonObject to xml: {}", LEAP_LOG_KEY, xmlString);
				exchange.getIn().setBody(xmlString);
			}
		} else {
			LeapConfigurationUtil.setResponseCode(503, exchange,
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			throw new RequestDataTransformationException(
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Custom bean to unmarshal the JSON string to XML for XML ENDPOINT
	 * 
	 * @param exchange
	 * @throws JSONException
	 * @throws RequestDataTransformationException
	 */
	public void unmarshalJsonToXMLForXmlEndpoint(Exchange exchange)
			throws JSONException, RequestDataTransformationException {
		String methodName = "unmarshalJsonToXMLForXmlEndpoint";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String bodyIn = exchange.getIn().getBody(String.class);
		if (bodyIn != null) {
			if (bodyIn.startsWith("<")) {
				exchange.getIn().setBody(bodyIn);
			} else if (bodyIn.startsWith("{") || bodyIn.startsWith("[")) {
				org.json.JSONObject jsonObject = new org.json.JSONObject(bodyIn);
				String xmlString = XML.toString(jsonObject);
				logger.debug("{} unmarshalled - jsonObject to xml:{} ", LEAP_LOG_KEY, xmlString);
				exchange.getIn().setBody(xmlString);
			} else {
				LeapConfigurationUtil.setResponseCode(503, exchange,
						"Unable to transform exchange body as invalid data format in exchange body for service : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
				throw new RequestDataTransformationException(
						"Unable to transform exchange body as invalid data format in exchange body for service : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} else {
			LeapConfigurationUtil.setResponseCode(503, exchange,
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			throw new RequestDataTransformationException(
					"Unable to transform exchange body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public String transformRequestData(Exchange exchange) throws JSONException, RequestDataTransformationException {
		String methodName = "transformRequestData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String jsonstring = null;
		try {
			String body = exchange.getIn().getBody(String.class);
			if (body != null && !(body.isEmpty())) {
				jsonstring = convertBodyToJSONString(body, exchange);
			} else {
				throw new RequestDataTransformationException(
						"Unable to transform request data body as no data availble in exchange body for service : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME)
								+ " and feature group : "
								+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
			}
		} catch (ParseException | RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException(
					"Unable to transform request data body as no data availble in exchange body for service : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME) + ", feature : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATURENAME) + " and feature group : "
							+ exchange.getIn().getHeader(LeapDataContextConstant.FEATUREGROUP));
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return jsonstring;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String convertBodyToJSONString(String body, Exchange exh) throws ParseException {
		JSONObject data = null;
		JSONObject jsonobj2 = null;
		JSONParser parser = new JSONParser();
		JSONObject jobj = (JSONObject) parser.parse(body);
		logger.trace("{} json onj : {}", LEAP_LOG_KEY, jobj);
		logger.trace("{} jobj keys :{} ", LEAP_LOG_KEY, jobj.keySet());
		Iterator itr = jobj.keySet().iterator();
		String key = null;
		while (itr.hasNext()) {
			key = (String) itr.next();
			logger.trace("{} key :{} ", LEAP_LOG_KEY, key);
			data = (JSONObject) jobj.get(key);
			logger.trace("{} key : {}, data : {}", key, data);

		}
		if (data != null) {
			logger.trace("{} data is not null : {}", LEAP_LOG_KEY, data);
			JSONArray jsonArr = new JSONArray();
			jsonArr.put(data);
			logger.trace("{} json array :{} ", LEAP_LOG_KEY, jsonArr.toString());
			jsonobj2 = new JSONObject();
			jsonobj2.put(LeapDataContextConstant.DATA_KEY, jsonArr);
			logger.trace("{} json obj :{} ", LEAP_LOG_KEY, jsonobj2.toString());
		} else {
			return null;
		}
		exh.getIn().setHeader(LeapDataContextConstant.SERVICENAME, key.toLowerCase().trim());
		return jsonobj2.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public String transformRestRequestData(Exchange exchange) throws RequestDataTransformationException, JSONException {
		String body = null;
		String httpMethod = null;
		try {
			httpMethod = (String) exchange.getIn().getHeader(LeapDataContextConstant.CAMEL_HTTP_METHOD);
			logger.debug("{} httpMethod : {}", LEAP_LOG_KEY, httpMethod);
			if (httpMethod.equalsIgnoreCase("GET")) {
				String httpQuery = (String) exchange.getIn().getHeader("CamelHttpQuery");
				if (httpQuery != null) {
					Map<String, Object> mapdata = new HashMap<>();
					logger.debug("{} http query data : {}", LEAP_LOG_KEY, httpQuery);
					String[] splitQueryString = httpQuery.split("&");
					int noOfqueryEle = splitQueryString.length;
					for (int i = 0; i <= noOfqueryEle - 1; i++) {
						String[] singleQueryParam = splitQueryString[i].split("=");
						if (singleQueryParam.length == 2) {
							mapdata.put(singleQueryParam[0].trim(), singleQueryParam[1].trim());
						}
					}
					logger.debug("map data :{} {}", LEAP_LOG_KEY, mapdata);

					org.json.JSONObject jobj = new org.json.JSONObject(mapdata);
					JSONArray jsonArr = new JSONArray();
					jsonArr.put(jobj);
					JSONObject jobj1 = new JSONObject();
					jobj1.put(LeapDataContextConstant.DATA_KEY, jsonArr);
					return jobj1.toJSONString();
				}
			} else if (httpMethod.equalsIgnoreCase(LeapDataContextConstant.POST_KEY)) {
				body = exchange.getIn().getBody(String.class);
				logger.debug("{} body for post request :{} ", LEAP_LOG_KEY, body);
			} else {
				logger.debug("UNSUPPORTED http method {}", LEAP_LOG_KEY);
				throw new RequestDataTransformationException("UNSUPPORTED http method " + httpMethod);
			}
		} catch (JSONException | RequestDataTransformationException e) {
			LeapConfigurationUtil.setResponseCode(503, exchange, e.getMessage());
			throw new RequestDataTransformationException("UNSUPPORTED http method " + httpMethod);
		}
		return body;
	}

	public void loadActualUri(Exchange exchange) throws RequestDataTransformationException {
		String methodName = "loadActualUri";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Message message = exchange.getIn();
		String url = exchange.getIn().getHeader(CAMEL_HTTP_URI_HEADER).toString();
		int lastIndexOf = url.lastIndexOf("/") + 1;
		String prettyString = url.substring(lastIndexOf);
		String siteId = message.getHeader(LeapHeaderConstant.SITE_KEY, String.class);
		String accountId = message.getHeader(LeapHeaderConstant.ACCOUNT_ID, String.class);
		try {
			nullOrEmptyCheck(accountId, "accountId header is missing or empty");
			nullOrEmptyCheck(siteId, "siteId header is missing or empty");
			String tenant = LeapTenantSecurityUtil.getInternalTenantId(accountId, siteId);
			logger.debug("{} tenatId is :: {}", LEAP_LOG_KEY, tenant);
			logger.debug("{} prettyString is ::{}", LEAP_LOG_KEY, prettyString);
			String urlMapping = prettyService.getVerbosUrlFromCache(prettyString, tenant, siteId);
			String[] verboseUrl = urlMapping.split("/");
			message.setHeader(LeapResponseHandlerConstant.FEATURE, verboseUrl[1]);
			message.setHeader(LeapResponseHandlerConstant.FEATURE_GROUP, verboseUrl[0]);
			message.setHeader(LeapResponseHandlerConstant.SERVICE_NAME, verboseUrl[2]);
		} catch (PrettyUrlMappingServiceException e) {
			logger.error("unable to find prettyString {} {} ", LEAP_LOG_KEY, e.getMessage());
			message.setHeader(LeapResponseHandlerConstant.FEATURE, "");
			message.setHeader(LeapResponseHandlerConstant.FEATURE_GROUP, "");
			message.setHeader(LeapResponseHandlerConstant.SERVICE_NAME, "");
			throw new RequestDataTransformationException(e.getMessage());
		} catch (AccountFetchException e) {
			throw new RequestDataTransformationException("Invalid id: " + e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void nullOrEmptyCheck(String string, String message) throws PrettyUrlMappingServiceException {
		if (string == null || string.isEmpty()) {
			throw new PrettyUrlMappingServiceException(message);
		}
	}

	/**
	 * This method is used to see if encoded request data available in body or
	 * custom header and convert into required json data.
	 * 
	 * @param exchange : Camel Exchange Object
	 * @throws RequestDataTransformationException
	 */
	private void processRequestForEncodedData(Exchange exchange) throws RequestDataTransformationException {
		String bodyIn = exchange.getIn().getBody(String.class);
		// if body is null then check in custom header
		if (bodyIn != null) {
			createJSONRequestFromXMLString(bodyIn, exchange);
		} else {
			String headerDataValue = (String) exchange.getIn().getHeader(LeapGenericConstant.ENCODED_REQUEST_DATA_KEY);
			logger.debug("{} message value in header : {}", LEAP_LOG_KEY, headerDataValue);
			createJSONRequestFromXMLString(headerDataValue, exchange);

		}
	}// end of method processRequestForEncodedData

	/**
	 * This method is used to convert xml request data into JSON format
	 * 
	 * @param xmlRequest : request XML string
	 * @param exchange   : Camel Exchnage Object
	 * @throws RequestDataTransformationException
	 */
	private void createJSONRequestFromXMLString(String xmlRequest, Exchange exchange)
			throws RequestDataTransformationException {
		String serviceName = (String) exchange.getIn().getHeader(LeapDataContextConstant.SERVICENAME);
		logger.trace("{} serviceName :{} " + serviceName);
		org.json.JSONObject jsonObject;
		// skipping for exposing pipeline as service.
		if (serviceName.equalsIgnoreCase(PipelineServiceConstant.EXECUTE_PIPELINE)) {
			logger.debug("skipping the XML to Json for executing the Pipeline {}", LEAP_LOG_KEY);
		} else {
			try {
				jsonObject = XML.toJSONObject(xmlRequest);
				logger.trace("{} marshalled - jsonObject from body:{} ", LEAP_LOG_KEY, jsonObject.toString());
				if (jsonObject.has(LeapDataContextConstant.DATA_KEY)) {
					JSONArray jsonArray = new JSONArray();
					jsonArray.put(jsonObject);
					org.json.JSONObject reqJsonObject = new org.json.JSONObject();
					reqJsonObject.put(LeapDataContextConstant.DATA_KEY, jsonArray);
					exchange.getIn().setBody(reqJsonObject.toString());
				} else
					exchange.getIn().setBody(jsonObject);

			} catch (JSONException e) {
				throw new RequestDataTransformationException(
						"Unable to transform xml data " + xmlRequest + " into json ", e);
			}
		}

	}// end of method createJSONRequestFromXMLString

}
