package com.attunedlabs.feature.service.call.handler;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.ACCOUNTID;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATUREGROUP;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.PROVIDER;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.SERVICENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.SITEID;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;

public class FeatureServiceCall {

	static Logger logger = LoggerFactory.getLogger(FeatureServiceCall.class);
	private static final String CONTENT_TYPE_LEAP = "application/vnd.leap+json";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String BASE_ENTRY_ENDPOINT = "direct:baseEntry";
	private static final String HEADERS_KEY = "headers";

	private String siteId;
	private String accountId;
	private String provider;
	private String featureGroup;
	private String featurename;
	private String serviceName;
	private Boolean leapResponseType;
	// Additional headers for service
	private Map<String, Object> serviceCallHeaders;

	// We have few default values
	public FeatureServiceCall() {
		this.accountId = "ALL";
		this.siteId = "all";
		this.leapResponseType = false;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getFeatureGroup() {
		return featureGroup;
	}

	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}

	public String getFeaturename() {
		return featurename;
	}

	public void setFeaturename(String featurename) {
		this.featurename = featurename;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Boolean getLeapResponseType() {
		return leapResponseType;
	}

	public void setLeapResponseType(Boolean leapResponseType) {
		this.leapResponseType = leapResponseType;
	}

	public Map<String, Object> getServiceCallHeaders() {
		return serviceCallHeaders;
	}

	public void setServiceCallHeaders(Map<String, Object> serviceCallHeaders) {
		this.serviceCallHeaders = serviceCallHeaders;
	}

	/**
	 * Method used to make a service call from one feature service to different
	 * feature service.
	 * 
	 * @param exchange
	 * @param featureGroup
	 * @param featurename
	 * @param serviceName
	 * @param leapResponseType
	 */
	public void makeFeatureServiceCall(Exchange exchange) {
		String methodName = "makeFeatureServiceCall";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// copy(true) means we need a new map of headers getting copied into new
		// Exchange.
		Exchange newExchange = exchange.copy(true);
		// Remove the un wanted headers
		removeInternalHeaders(newExchange);

		// Set Tenent Details
		newExchange.getIn().setHeader(SITEID, this.getSiteId());
		newExchange.getIn().setHeader(ACCOUNTID, this.getAccountId());
		// Set new FSC details
		newExchange.getIn().setHeader(FEATUREGROUP, this.getFeatureGroup());
		newExchange.getIn().setHeader(FEATURENAME, this.getFeaturename());
		newExchange.getIn().setHeader(SERVICENAME, this.getServiceName());
		// Set provider if exits
		if (this.getProvider() != null)
			newExchange.getIn().setHeader(PROVIDER, this.getProvider());

		// Set Content-Type to 'application/vnd.leap+json' if true.
		if (this.getLeapResponseType())
			newExchange.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_LEAP);

		// Add all the this headers into new Exchange
		if (this.getServiceCallHeaders() != null)
			for (Entry<String, Object> entry : this.getServiceCallHeaders().entrySet()) {
				newExchange.getIn().setHeader(entry.getKey(), entry.getValue());
			}

		ProducerTemplate producerTemplate = newExchange.getContext().createProducerTemplate();
		producerTemplate.send(BASE_ENTRY_ENDPOINT, newExchange);

		// Add the Response data to exiting LDC.
		addResponseDataToLeapDataContext(exchange, newExchange, this.getServiceName());
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method used to add the current exchange body to the existing LDC present in
	 * exchange headers. Inside LDC the body will be stored as a LeapDataElement
	 * with the name of element same as service name.
	 * 
	 * @param exchange
	 * @param serviceName
	 */
	public static void addResponseDataToLeapDataContext(Exchange exchange, Exchange newExchange, String serviceName) {
		String methodName = "addResponseDataToLeapDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		LeapDataContext leapDataCtx = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);

		Object responseData = newExchange.getIn().getBody();
		Map<String, Object> headersMap = newExchange.getIn().getHeaders();
		JSONObject newExchangeHeaders = getJsonFromMap(headersMap);

		try {
			// Add the newExchange response data and headers to old exchange LDC
			if (responseData instanceof JSONObject || responseData instanceof JSONArray) {
				leapDataCtx.addContextElement(responseData, serviceName, serviceName, null);
				leapDataCtx.addContextElement(newExchangeHeaders, serviceName + HEADERS_KEY, serviceName + HEADERS_KEY,
						null);
				exchange.getIn().setBody("");
			} else {
				if (responseData != null && responseData != "")
					leapDataCtx.addContextElement(responseData.toString(), serviceName, serviceName, null);
				leapDataCtx.addContextElement(newExchangeHeaders, serviceName + HEADERS_KEY, serviceName + HEADERS_KEY,
						null);
				exchange.getIn().setBody("");
			}

		} catch (LeapDataContextInitialzerException e) {
			e.printStackTrace();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	private static JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
		JSONObject jsonData = new JSONObject();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value instanceof Map<?, ?>) {
				value = getJsonFromMap((Map<String, Object>) value);
			}
			jsonData.put(key, value);
		}
		return jsonData;
	}

	private static Exchange removeInternalHeaders(Exchange exchange) {
		String methodName = "removeInternalHeaders";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String headerList = "Accept,Access-Control-Allow-Methods,Access-Control-Allow-Origin,access-control-allow-headers,actionEndpoint,actionEndpointMethod,"
				+ "breadcrumbId,CamelHttpMethod,CamelHttpUri,CamelRestletRequest,CamelRestletResponse,Connection,Content-length,createdisplaymetadata"
				+ "endpointtype,exeroute,exeroute,featuregroup,FeatureDeployment,Host,implroute,isaccesstokenvalidationenabled,istenanttokenvalidationenabled"
				+ "leapdatacontext,LeapEndPointHttpMethod,org.restlet.startTime,performacelognotifier,serviceName,servicetype,timeZone,User-agent,leapDataContext";

		String HeadersArray[] = headerList.split(",");
		for (String header : HeadersArray) {
			exchange.getIn().removeHeader(header);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return exchange;
	}

	@Override
	public String toString() {
		return "FeatureServiceCall [siteId=" + siteId + ", accountId=" + accountId + ", provider=" + provider
				+ ", featureGroup=" + featureGroup + ", featurename=" + featurename + ", serviceName=" + serviceName
				+ ", leapResponseType=" + leapResponseType + ", serviceCallHeaders=" + serviceCallHeaders + "]";
	}

}
