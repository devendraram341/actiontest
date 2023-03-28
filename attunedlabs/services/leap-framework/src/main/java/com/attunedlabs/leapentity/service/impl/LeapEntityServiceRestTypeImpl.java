package com.attunedlabs.leapentity.service.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.DATA;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.FORWARD_SLASH;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.HTTP_METHOD_DELETE;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.HTTP_METHOD_GET;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.HTTP_METHOD_POST;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.HTTP_METHOD_UPDATE;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.REQUEST_SOURCE;
import static com.attunedlabs.leapentity.service.LeapEntityServiceConstant.STATIC_SOURCE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leapentity.LeapEntityArchivalUtility;
import com.attunedlabs.leapentity.LeapEntityHttpUtility;
import com.attunedlabs.leapentity.config.jaxb.AuthenticationConfig;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityAccessConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityKey;
import com.attunedlabs.leapentity.config.jaxb.EntityReadKeysMapping;
import com.attunedlabs.leapentity.config.jaxb.EntityRestAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityRestConfig;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequest;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequestBody;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequestHeader;
import com.attunedlabs.leapentity.config.jaxb.EntityRestRequestHeaders;
import com.attunedlabs.leapentity.config.jaxb.FeatureServiceConfig;
import com.attunedlabs.leapentity.config.jaxb.FeatureServiceRouteURI;
import com.attunedlabs.leapentity.config.jaxb.SSLConfig;
import com.attunedlabs.leapentity.service.ILeapEntityService;
import com.attunedlabs.leapentity.service.LeapEntityServiceException;

public class LeapEntityServiceRestTypeImpl implements ILeapEntityService {

	protected Logger logger = LoggerFactory.getLogger(LeapEntityServiceRestTypeImpl.class.getName());

	// Initialization of variables
	private Map<String, Map<String, String>> loadEntitiesReadKeysMap = new HashMap<>();

	private IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();

	@Override
	public Object performEntityOperation(Entity entity, EntityAccessConfig entityAccessConfig,
			EntityAccess entityAccess, EntityRestAccess entityRestAccess, Object requestJson, Exchange exchange)
			throws LeapEntityServiceException {
		String methodName = "performEntityOperation";
		logger.debug("{} entered into the method {}.{}(), requestJson={}", LEAP_LOG_KEY, getClass().getName(),
				methodName, requestJson);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext serviceDataContext = leapDataContext.getServiceDataContext();
		JSONObject initialRequestBody = (JSONObject) LeapEntityArchivalUtility.getIntialRequestBodyFromLDC(exchange);
		List<EntityRestRequestHeader> requestRequiredHeaders = null;
		SSLConfig sslConfig = null;
		AuthenticationConfig authConfig = null;
		String entityName = entity.getName();
		EntityRestConfig entityRestConfig = entityAccessConfig.getEntityRestConfig();
		if (entityRestConfig != null) {
			String baseUrl = entityRestConfig.getEntityBaseRestURL().getValue();
			logger.debug("{} baseUrl :: {} ", leapDataContext, baseUrl.trim());
			String serviceMethod = entityRestAccess.getServiceMethod();
			String serviceName = entityRestAccess.getServiceName();
			String url = constructRequestUrl(baseUrl.trim(), serviceName);
			logger.debug("{} url of the thrid party service :: {},  service Method :: {} ", LEAP_LOG_KEY, url,
					serviceMethod);
			if (url == null || url.isEmpty())
				throw new LeapEntityServiceException(new Throwable(),
						"unable to perform the rest entity operation becasue url ", url + " is null", 400);
			EntityRestRequest entityRestRequest = entityRestAccess.getEntityRestRequest();
			String contentType = entityRestRequest.getContentType();
			EntityRestRequestHeaders entityRestRequestHeaders = entityRestAccess.getEntityRestRequest()
					.getEntityRestRequestHeaders();
			if (entityRestRequestHeaders != null) {
				requestRequiredHeaders = entityRestRequestHeaders.getEntityRestRequestHeader();
			}
			Boolean sslRequriedFlag = Boolean.valueOf(entityRestConfig.getEntityBaseRestURL().getSslRequried());
			if (sslRequriedFlag)
				sslConfig = entityRestConfig.getSSLConfig();
			logger.debug("{} ssl config value from entity :: {}", LEAP_LOG_KEY, sslConfig);
			boolean isAuthenticated = entityRestConfig.getEntityBaseRestURL().isIsAuthenticated();
			if (isAuthenticated)
				authConfig = entityRestConfig.getAuthenticationConfig();
			logger.debug("{} auth config value from entity :: {} ", LEAP_LOG_KEY, authConfig);

			EntityReadKeysMapping entityReadKeysMapping = entityRestAccess.getEntityReadKeysMapping();
			if (entityReadKeysMapping != null) {
				String loadEntityReapKey = entityName + "_" + serviceName;
				validataionOfRequestWithEntity(entityRestAccess, loadEntityReapKey, entityReadKeysMapping,
						initialRequestBody);
			} // ..end of if condition

			String responseBody = invokeHttpClient(serviceMethod, entityRestRequest, url, contentType, sslConfig,
					authConfig, requestRequiredHeaders, initialRequestBody, requestJson);
			logger.info("{} responseBody of the webservice call :: {}", LEAP_LOG_KEY, responseBody);
			if (responseBody != null && !responseBody.isEmpty()) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return responseBody;
			} else
				throw new LeapEntityServiceException(new Throwable(),
						"response from the webservice call shouldn't be empty", responseBody, 400);
		} // end of if condition for checking the rest entity access object is
			// null or not
		else {
			EntityReadKeysMapping entityReadKeysMapping = entityRestAccess.getEntityReadKeysMapping();
			if (entityReadKeysMapping != null) {
				String loadEntityReapKey = entityName + "_" + entityRestAccess.getServiceName();
				validataionOfRequestWithEntity(entityRestAccess, loadEntityReapKey, entityReadKeysMapping,
						initialRequestBody);
			} // ..end of if condition
			FeatureServiceConfig featureServiceConfig = entityAccessConfig.getFeatureServiceConfig();
			LeapServiceRuntimeContext currentLeapServiceRuntimeContext = serviceDataContext
					.getCurrentLeapServiceRuntimeContext();
			if (featureServiceConfig != null) {
				FeatureServiceRouteURI featureServiceRouteURI = featureServiceConfig.getFeatureServiceRouteURI();
				String endpointName = featureServiceRouteURI.getEndpoint();
				if (endpointName == null) {
					Object provider = exchange.getIn().getHeader(LeapDataContextConstant.PROVIDER);
					if (provider != null)
						endpointName = provider.toString();
				}
				String featureServiceName = featureServiceRouteURI.getFeatureServiceName();
				String serviceName = featureServiceRouteURI.getServiceName();
				currentLeapServiceRuntimeContext.setServiceName(serviceName);
				String uri = featureServiceName + "-" + serviceName + "-" + "executionEnrichmentRoute";
				exchange.getIn().setHeader("featureConfig", true);
				exchange.getIn().setHeader("execRoute", uri);
				FeatureDeployment featureDeployment = null;
				try {
					if (endpointName != null)
						featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
								serviceDataContext.getTenant(), serviceDataContext.getSite(),
								serviceDataContext.getFeatureName(), endpointName, serviceDataContext);
					else
						featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
								serviceDataContext.getTenant(), serviceDataContext.getSite(),
								serviceDataContext.getFeatureName(), serviceDataContext);
					exchange.getIn().setHeader(LeapDataContextConstant.FEATURE_DEPLOYMENT, featureDeployment);
				} catch (FeatureDeploymentServiceException e) {
					e.printStackTrace();
				}
				logger.info("{} featureDeployment value in performEntityOperation :: {} ", LEAP_LOG_KEY,
						featureDeployment);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return "";
			} else
				throw new LeapEntityServiceException(new Throwable(),
						"either the rest enity or feature service config is missed in xml ",
						"either the rest enity or feature service config is missed in xml ", 400);

		}

	}// ..end of the method performEntityOperation

	/**
	 * This method is used to invoke the http client and call the webservice
	 * 
	 * @param serviceMethod
	 * @param entityRestRequest
	 * @param url
	 * @param contentType
	 * @param sslConfig
	 * @param authConfig
	 * @param requestRequiredHeaders
	 * @param requestJson
	 * @param intialRequestBody
	 * @return
	 * @throws LeapEntityServiceException
	 */
	private String invokeHttpClient(String serviceMethod, EntityRestRequest entityRestRequest, String url,
			String contentType, SSLConfig sslConfig, AuthenticationConfig authConfig,
			List<EntityRestRequestHeader> requestRequiredHeaders, JSONObject initialRequestBody, Object requestJson)
			throws LeapEntityServiceException {
		String methodName = "invokeHttpClient";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String body = null;
		String responseBody = null;
		switch (serviceMethod.toLowerCase()) {
		case HTTP_METHOD_POST:
			body = getRequestBodyFromConfig(entityRestRequest, initialRequestBody, requestJson, contentType);
			responseBody = LeapEntityHttpUtility.httpPost(url, body, contentType, requestRequiredHeaders, sslConfig,
					authConfig);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return responseBody;
		case HTTP_METHOD_GET:
			responseBody = LeapEntityHttpUtility.sendGet(url, contentType, requestRequiredHeaders, sslConfig,
					authConfig);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return responseBody;

		case HTTP_METHOD_UPDATE:
			body = getRequestBodyFromConfig(entityRestRequest, initialRequestBody, requestJson, contentType);
			responseBody = LeapEntityHttpUtility.httpPost(url, body, contentType, requestRequiredHeaders, sslConfig,
					authConfig);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return responseBody;
		case HTTP_METHOD_DELETE:
			body = getRequestBodyFromConfig(entityRestRequest, initialRequestBody, requestJson, contentType);
			responseBody = LeapEntityHttpUtility.httpPost(url, body, contentType, requestRequiredHeaders, sslConfig,
					authConfig);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return responseBody;
		default:
			throw new LeapEntityServiceException(new Throwable(), "the service method is not supported", serviceMethod,
					405);
		}
	}// ..end
		// of
		// the
		// method
		// invokeHttpClient

	/**
	 * This method is used to validation of request with entity
	 * 
	 * @param entityRestAccess
	 * @param loadEntityReapKey
	 * @param entityReadKeysMapping
	 * @param intialRequestBody2
	 * @throws LeapEntityServiceException
	 */
	private void validataionOfRequestWithEntity(EntityRestAccess entityRestAccess, String loadEntityReapKey,
			EntityReadKeysMapping entityReadKeysMapping, JSONObject intialRequestBody)
			throws LeapEntityServiceException {
		String methodName = "validataionOfRequestWithEntity";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> intialRequestMapObj = intialRequestBody.toMap();
		Map<String, String> entityReadKeyMap = null;
		entityReadKeyMap = loadEntitiesReadKeyMap(loadEntityReapKey, entityReadKeyMap, entityReadKeysMapping);
		logger.debug("{} entityReadKeyMap value :: {} ", LEAP_LOG_KEY, entityReadKeyMap);
		if (entityReadKeyMap == null || entityReadKeyMap.size() == 0) {
			throw new LeapEntityServiceException(new Throwable(),
					"entity read keys map is not initialized for the key ", loadEntityReapKey, 400);
		}
		logger.debug("{} entityReadKeyMap key set :: {} ", LEAP_LOG_KEY, entityReadKeyMap.keySet());
		logger.debug("{} intialRequestMapObj key set :: {} ", LEAP_LOG_KEY, intialRequestMapObj.keySet());
		boolean flag = entityReadKeyMap.keySet().equals(intialRequestMapObj.keySet());
		logger.debug("{} flag value of validation :: {} ", LEAP_LOG_KEY, flag);
		if (flag)
			logger.info("{} the validation of entity required keys and request keys is success ", LEAP_LOG_KEY);
		else
			throw new LeapEntityServiceException(new Throwable(),
					" Unable to perform Entity operation one of the required key is not matched with the request and in entity config",
					entityReadKeyMap.keySet().toString(), 400);
	}// ..end
		// of
		// the
		// method
		// validataionOfRequestWithEntity

	/**
	 * This method is used to load the entities read key mapping and cached in local
	 * (map key is entityName_serviceName)
	 * 
	 * @param loadEntityReapKey
	 * @param entityReadKeyMap
	 * @param entityReadKeysMapping
	 * @return
	 */
	private Map<String, String> loadEntitiesReadKeyMap(String loadEntityReapKey, Map<String, String> entityReadKeyMap,
			EntityReadKeysMapping entityReadKeysMapping) {
		String methodName = "loadEntitiesReadKeyMap";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (loadEntitiesReadKeysMap.containsKey(loadEntityReapKey))
			entityReadKeyMap = loadEntitiesReadKeysMap.get(loadEntityReapKey);
		else {
			entityReadKeyMap = new HashMap<>();
			if (entityReadKeysMapping != null) {
				List<EntityKey> listOfEntityKeys = entityReadKeysMapping.getEntityKey();
				for (EntityKey entityKey : listOfEntityKeys) {
					boolean requiredFlag = Boolean.valueOf(entityKey.getRequired());
					if (requiredFlag) {
						String name = entityKey.getName();
						entityReadKeyMap.put(name, name);
					} // ..end of the if

				} // ..end of the for loop
				loadEntitiesReadKeysMap.put(loadEntityReapKey, entityReadKeyMap);
			}
			logger.debug("{} loadEntitiesReadKeysMap value :: {} ", LEAP_LOG_KEY, loadEntitiesReadKeysMap.toString());
		}

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityReadKeyMap;
	}// ..end
		// of
		// the
		// method
		// loadEntitiesReadKeyMap

	/**
	 * This method is used to get the request body from the entity config if the
	 * source is static
	 * 
	 * @param entityRestRequest
	 * @param requestJson
	 * @param contentType
	 * @param intialRequestBody
	 * @return {@link String}
	 * @throws LeapEntityServiceException
	 */
	private String getRequestBodyFromConfig(EntityRestRequest entityRestRequest, JSONObject initialRequestBody,
			Object requestJson, String contentType) throws LeapEntityServiceException {
		String methodName = "getRequestBodyFromConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String requestBody = null;
		EntityRestRequestBody entityRestRequestBody = entityRestRequest.getEntityRestRequestBody();
		String source = entityRestRequestBody.getSource();
		if (source.equalsIgnoreCase(STATIC_SOURCE)) {
			requestBody = entityRestRequestBody.getRequestBody();
			requestBody = updateRequestBodyWithRefValues(requestBody, initialRequestBody);
			logger.info("{} requestBody :: {} ", LEAP_LOG_KEY, requestBody);
		} else {
			if (contentType.equalsIgnoreCase("application/xml") || contentType.equalsIgnoreCase("text/xml")
					|| contentType.equalsIgnoreCase("xml")) {
				requestBody = requestJson.toString();
			} else {
				String entityDefaultReqString = requestJson.toString();
				JSONObject requestJsonObj = XML.toJSONObject(entityDefaultReqString, false);
				if (requestJsonObj.has(DATA))
					requestBody = requestJsonObj.getJSONObject(DATA).toString();
				else
					requestBody = requestJsonObj.toString();
			}
		}
		if (requestBody == null) {
			throw new LeapEntityServiceException(new Throwable(), "Body is either null or empty",
					"Body is either null or empty", 401);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return requestBody;
	}// ..end
		// of
		// the
		// method
		// getRequestBodyFromConfig

	/**
	 * This method is used to update the request body which is append with $ of
	 * enity reference keys values coming from request
	 * 
	 * @param requestBody
	 * @param intialRequestBody
	 * @return
	 * @throws LeapEntityServiceException
	 */
	private String updateRequestBodyWithRefValues(String requestBody, JSONObject initialRequestBody)
			throws LeapEntityServiceException {
		String methodName = "updateRequestBodyWithRefValues";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		JSONObject entityConfigRequestBody = new JSONObject(requestBody);
		JSONObject entityDataJsonObj = null;
		JSONArray requestJsonArrayObj = null;

		if (entityConfigRequestBody.has(DATA))
			entityDataJsonObj = entityConfigRequestBody.getJSONObject(DATA);
		else
			entityDataJsonObj = entityConfigRequestBody;

		if (entityDataJsonObj.has(REQUEST_SOURCE))
			requestJsonArrayObj = entityDataJsonObj.getJSONArray(REQUEST_SOURCE);
		else
			throw new LeapEntityServiceException(new Throwable(),
					"mandatory key is missing in the entity config request in json object", REQUEST_SOURCE, 400);

		for (int i = 0; i < requestJsonArrayObj.length(); i++) {
			JSONObject requestJsonObj = requestJsonArrayObj.getJSONObject(i);
			Iterator<String> requestJsonKeySet = requestJsonObj.keys();
			while (requestJsonKeySet.hasNext()) {
				String e = requestJsonKeySet.next();
				String value = requestJsonObj.getString(e);
				if (value.startsWith("$")) {
					String replacedValue = value.replace("$", "");
					checkTheDataTypeAndUpdateValue(replacedValue, initialRequestBody, e, requestJsonObj);
				} // ..end of if condition

			} // ..end of the while loop
		} // ..end of the for loop
		logger.debug("{} after updating the entity request json array body {}", LEAP_LOG_KEY, entityConfigRequestBody);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityConfigRequestBody.toString();

	}// ..end

	/**
	 * This method is used to check the data type value in the intial request and
	 * return
	 * 
	 * @param replacedValue
	 * @param intialRequestBody
	 * @param requestJsonObj
	 * @param e
	 */
	private void checkTheDataTypeAndUpdateValue(String replacedValue, JSONObject initialRequestBody, String key,
			JSONObject requestJsonObj) {
		String methodName = "checkTheDataTypeAndUpdateValue";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Object updatedValueObj = initialRequestBody.get(replacedValue);
		if (updatedValueObj instanceof String) {
			String updatedValue = initialRequestBody.getString(replacedValue);
			requestJsonObj.put(key, updatedValue);
		}
		if (updatedValueObj instanceof Integer) {
			int updatedValue = initialRequestBody.getInt(replacedValue);
			requestJsonObj.put(key, updatedValue);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// ..end of the method checkTheDataTypeAndUpdateValue

	// of
	// the
	// method
	// updateRequestBodyWithRefValues

	/**
	 * This method is used to construct the request url to connect with the
	 * webservice
	 * 
	 * @param baseUrl
	 * @param serviceName
	 * @return
	 */
	private String constructRequestUrl(String baseUrl, String serviceName) {
		String methodName = "constructRequestUrl";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String url = null;
		logger.trace("{} flag value :: {} ", LEAP_LOG_KEY, baseUrl.endsWith(FORWARD_SLASH));
		if (!baseUrl.endsWith(FORWARD_SLASH))
			url = baseUrl.trim() + FORWARD_SLASH + serviceName;
		else
			url = baseUrl.trim() + serviceName;
		logger.trace("{} url :: {}", LEAP_LOG_KEY, url);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return url;

	}// ..end of the method constructRequestUrl

}
