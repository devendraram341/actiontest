package com.attunedlabs.leap.context.initializer;

import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.*;
import static com.attunedlabs.leap.context.initializer.LeapDataContextInitializerConstant.INITIAL_CONTEXT_KIND;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_ACCESS_TOKEN_VALID;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_TENANT_TOKEN_VALID;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapDataContextConfigException;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.LeapServiceRuntimeContext;
import com.attunedlabs.leap.UnableToRemoveServiceContextException;
import com.attunedlabs.leap.context.bean.InitialLeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapData;
import com.attunedlabs.leap.context.bean.LeapDataContextElement;
import com.attunedlabs.leap.context.bean.LeapDataElement;
import com.attunedlabs.leap.context.bean.LeapJSONArrayResultSet;
import com.attunedlabs.leap.context.bean.LeapJSONResultSet;
import com.attunedlabs.leap.context.bean.LeapResultSet;
import com.attunedlabs.leap.context.bean.LeapXMLResultSet;
import com.attunedlabs.leap.context.bean.MetaData;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.context.exception.LeapDataContextInitialzerException;
import com.attunedlabs.leap.context.helper.LeapDataContextHelper;
import com.attunedlabs.leap.feature.routing.DynamicallyImplRoutingFailedException;
import com.attunedlabs.leap.header.initializer.RootDeployableConfiguration;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.leapentity.LeapEntityArchivalUtility;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;

/**
 * This class is use to work on leapDataContext Object - create, initialize or
 * reinitialize it
 * 
 * @author Reactiveworks
 *
 */
public class LeapDataContextInitializer {

	static Logger logger = LoggerFactory.getLogger(LeapDataContextInitializer.class);
	private IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();

	JSONArray dataArray = new JSONArray();

	/**
	 * This method is use to either create the leapDataContext object or if already
	 * existing then re-initialize the with runing service context
	 * 
	 * @param exchange : {@link Exchange}
	 * @throws FeatureHeaderInitialzerException
	 * @throws AccountFetchException
	 * @throws DynamicallyImplRoutingFailedException
	 */
	public void createAndInitializeLeapContext(Exchange exchange)
			throws LeapDataContextInitialzerException, AccountFetchException, DynamicallyImplRoutingFailedException {
		String methodName = "createAndInitializeLeapContext";
		logger.debug("{} entered into the method {}, leapDataContext {}", LEAP_LOG_KEY, methodName,
				exchange.getIn().getHeader(LEAP_DATA_CONTEXT));
		long startTime = System.currentTimeMillis();
		// checking if subscription call or not set the extra property
		if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY) == null)
			exchange.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, false);
		logger.trace("{} <<<=============================================================================>>>");
		logger.trace("{} LeapDataContextInitializer [start] : {}", LEAP_LOG_KEY, startTime);

		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		String featureGroup = (String) exchange.getIn().getHeader(FEATUREGROUP);
		String featureName = (String) exchange.getIn().getHeader(FEATURENAME);
		String serviceName = (String) exchange.getIn().getHeader(SERVICENAME);
		try {
			String isTenantValidationEnabled = getTenantTokenValidationStatus(exchange);
			String isAccessValidationEnabled = getAccessTokenValidationStatus(exchange);

			// Check if leapDataContext already exist in the exchange or not.
			if (leapDataContext == null) {
				logger.debug(
						"{} No leap data context exist in the exchange header so, considering it is a new request.. ",
						LEAP_LOG_KEY);
				// call method createNewLeapDataContext
				leapDataContext = createNewLeapDataContext(featureGroup, featureName, serviceName,
						isTenantValidationEnabled, isAccessValidationEnabled, exchange);
			} else {
				logger.debug("{} Leap data context exist in the exchange header ", LEAP_LOG_KEY);
				if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, Boolean.class)) {
					leapDataContext.getServiceDataContext().SetRunningContextServiceName(serviceName);
				}
				leapDataContext = reInitializeLeapServiceContext(serviceName, leapDataContext, exchange);

			}
			exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
			removeHeaderDetails(exchange);
		} catch (PropertiesConfigException e) {
			LeapConfigurationUtil.setResponseCode(500, exchange, e.getMessage());
			throw new LeapDataContextInitialzerException("unable to load the properties file", e.getCause(),
					e.getMessage(), 400);
		} catch (Exception e) {
			e.printStackTrace();
			throw new LeapDataContextInitialzerException(e.getMessage(), e.getCause(), e.getMessage(), 400);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);

	}// end of method initializeLeapContext

	/**
	 * This method is use to remove the runtime service context
	 * 
	 * @param exchange : {@link Exchange}
	 * @throws LeapDataContextInitialzerException
	 */
	public void removeRuntimeContext(Exchange exchange) throws LeapDataContextInitialzerException {
		String methodName = "removeRuntimeContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		boolean isOnlyRuntimeServiceContext = leapServiceContext.isOnlyRuntimeServiceContext();
		try {
			if (isOnlyRuntimeServiceContext)
				leapServiceContext.removeLastServiceContext();
			else
				leapServiceContext.removeCurrentServiceContext();

		} catch (UnableToRemoveServiceContextException e) {
			LeapConfigurationUtil.setResponseCode(500, exchange, e.getMessage());
			throw new LeapDataContextInitialzerException("Unable to remove the runtime context", e.getCause(),
					e.getMessage(), 400);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method removeRuntimeContext

	/**
	 * This method is use to create new data context object
	 * 
	 * @param featureGroup              : name of the feature group
	 * @param featureName               : name of the feature name
	 * @param serviceName               : name of the service
	 * @param isTenantValidationEnabled : tenant validation is enabled or not
	 * @param isAccessValidationEnabled : access validation is enabled or not
	 * @param exchange                  : Camel Exchange Object
	 * @return {@link LeapDataContext}
	 * @throws AccountFetchException
	 * @throws LeapDataContextInitialzerException
	 * @throws DynamicallyImplRoutingFailedException
	 */
	private LeapDataContext createNewLeapDataContext(String featureGroup, String featureName, String serviceName,
			String isTenantValidationEnabled, String isAccessValidationEnabled, Exchange exchange)
			throws AccountFetchException, LeapDataContextInitialzerException, DynamicallyImplRoutingFailedException {
		String methodName = "createNewLeapDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		logger.trace("{} {}", LEAP_LOG_KEY, isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString()));
		logger.trace("{} {}", LEAP_LOG_KEY, isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString()));
		String endpointType = (String) exchange.getIn().getHeader(ENDPOINT_TYPE_KEY);
		String tenant = null;
		String site = null;
		String requestBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = new LeapDataContext();
		LeapServiceContext leapServiceContext = null;
		if ((isTenantValidationEnabled != null && !(isTenantValidationEnabled.isEmpty()))
				&& (isAccessValidationEnabled != null && !(isAccessValidationEnabled.isEmpty())))
			if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
					&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
					&& RootDeployableConfiguration.isRootDeployableFeature(featureGroup, featureName)) {
				logger.trace("{} Authentication is set to false &  feature need to be globally deployed.",
						LEAP_LOG_KEY);
				tenant = GLOBAL_TENANT_ID;
				site = GLOBAL_SITE_ID;
				// create and initialize leapData context and service context
				leapServiceContext = initializeLeapServiceContext(tenant, site, featureGroup, featureName, serviceName,
						endpointType, leapDataContext, exchange);
				initializeLeapDataContextObj(serviceName, requestBody, isTenantValidationEnabled,
						isAccessValidationEnabled, leapServiceContext, leapDataContext, exchange);
			} else {
				Object siteIdObj = exchange.getIn().getHeader(SITEID);
				Object accountNameObj = exchange.getIn().getHeader(ACCOUNTID);
				AccountConfiguration configuration = null;
				// checking if request is for login service. If login service
				// then there wont be any accountId and siteId
				// in the request header
				if ((serviceName != null && !(serviceName.isEmpty()) && LOGIN_SERVICE.equalsIgnoreCase(serviceName))
						&& (siteIdObj == null && accountNameObj == null)) {
					logger.trace("{} request is for login service so, there wont be an account id and site id",
							LEAP_LOG_KEY);
					configuration = LeapTenantSecurityUtil.getAccountConfigurationByDomin(getDomainName(requestBody));

				} else if (accountNameObj != null && siteIdObj != null) {
					logger.trace("{} accountId and siteId  is define in the request header ", LEAP_LOG_KEY);
					site = (String) siteIdObj;
					String accountName = (String) accountNameObj;
					configuration = LeapTenantSecurityUtil.getAccountConfigurationByExternalTenantId(accountName, site);

				} else {
					logger.warn("{} accountId and siteId  is missing in the request header", LEAP_LOG_KEY);
					// accountId and sitedId is not coming from request header
					// and it is it not even login service
					// hence, throw an error
					LeapConfigurationUtil.setResponseCode(500, exchange,
							"Please check if siteId and AccountId is specified properly or not");
					throw new LeapDataContextInitialzerException(
							"Please check if siteId and AccountId is specified properly or not",
							new Throwable("Please check if siteId and AccountId is specified properly or not"),
							"account id or site id is missing in the request header", 400);
				}
				if (configuration == null)
					throw new LeapDataContextInitialzerException(
							"No Account is configured for AccountId : " + accountNameObj + " for site : " + site,
							new Throwable("No Account is configured for the account and site define in request header"),
							"No Account is configured for AccountId : " + accountNameObj + " for site : " + site, 400);
				exchange.getIn().setHeader(TIMEZONE, configuration.getTimezone());
				tenant = configuration.getInternalTenantId();
				site = configuration.getInternalSiteId();
				leapServiceContext = initializeLeapServiceContext(tenant, site, featureGroup, featureName, serviceName,
						endpointType, leapDataContext, exchange);
				initializeLeapDataContextObj(serviceName, requestBody, isTenantValidationEnabled,
						isAccessValidationEnabled, leapServiceContext, leapDataContext, exchange);

			}
		exchange.getIn().setBody("");
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return leapDataContext;
	}// end of createNewLeapDataContext

	/**
	 * This method is used to reinitialize the existing service context by adding
	 * new runtime context
	 * 
	 * @param serviceName     : name of the service
	 * @param leapDataContext : {@link LeapDataContext}
	 * @return {@link LeapDataContext}
	 * @throws DynamicallyImplRoutingFailedException
	 */
	private LeapDataContext reInitializeLeapServiceContext(String serviceName, LeapDataContext leapDataContext,
			Exchange exchange) throws DynamicallyImplRoutingFailedException {
		String methodName = "reInitializeLeapServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		leapServiceContext.initializeLeapRuntimeServiceContext(serviceName);
		String tenant = leapServiceContext.getTenant();
		String site = leapServiceContext.getSite();
		String featureGroup = leapServiceContext.getFeatureGroup();
		String featureName = leapServiceContext.getFeatureName();
		String endpointType = leapServiceContext.getEndpointType();
		// just checking if another runtimeservice context is added in the list
		// or not. Size should be more than one
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = leapServiceContext
				.getLeapServiceRuntimeContextList();
		logger.trace("{} no of runtime context in service context = {} ", LEAP_LOG_KEY,
				leapServiceRuntimeContextList.size());
		RequestContext requestContext = reloadConfigurationIfDoesntExist(tenant, site, featureGroup, featureName,
				serviceName, endpointType, leapServiceContext, exchange);
		leapServiceContext.setRequestContext(requestContext);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return leapDataContext;
	}// end of method reInitializeLeapServiceContext

	/**
	 * This method is use to initialize the leap service context object
	 * 
	 * @param tenant          : tenant name
	 * @param site            : site name
	 * @param featureGroup    : feature group name
	 * @param featureName     : feature name
	 * @param serviceName     : service name
	 * @param endpointType    : endpoint type
	 * @param leapDataContext : Leap Data Context Object
	 * @return {@link LeapServiceContext}
	 * @throws DynamicallyImplRoutingFailedException
	 */
	private LeapServiceContext initializeLeapServiceContext(String tenant, String site, String featureGroup,
			String featureName, String serviceName, String endpointType, LeapDataContext leapDataContext,
			Exchange exchange) throws DynamicallyImplRoutingFailedException {
		String methodName = "initializeLeapServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceContext serviceContext = leapDataContext.getServiceDataContext(tenant, site, featureGroup,
				featureName);
		serviceContext.setEndpointType(endpointType != null ? endpointType : LeapDataContextConstant.HTTP_JSON_KEY);
		serviceContext.initializeLeapRuntimeServiceContext(serviceName);
		RequestContext reqContext = reloadConfigurationIfDoesntExist(tenant, site, featureGroup, featureName,
				serviceName, endpointType, serviceContext, exchange);
		serviceContext.setRequestContext(reqContext);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return serviceContext;
	}// end of the method initializeLeapServiceContext

	private RequestContext reloadConfigurationIfDoesntExist(String tenant, String site, String featureGroup,
			String featureName, String serviceName, String endpointType, LeapServiceContext leapServiceContext,
			Exchange exchange) throws DynamicallyImplRoutingFailedException {
		String methodName = "reloadConfigurationIfDoesntExist";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		RequestContext reqcontext = null;
		String provider = null;
		if (exchange.getIn().getHeaders().containsKey(PROVIDER)) {
			provider = (String) exchange.getIn().getHeader(PROVIDER);
			logger.trace("{} provider in if : {}", LEAP_LOG_KEY, provider);
		}
		FeatureDeployment featureDeployment = null;
		try {
			if (provider != null) {
				logger.trace("{} provider is not null", LEAP_LOG_KEY);
				featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenant, site,
						featureName, provider, leapServiceContext);
			} else {
				logger.trace("{} provider is Null", LEAP_LOG_KEY);
				featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(tenant, site,
						featureName, leapServiceContext);
			}

			if (featureDeployment != null) {
				exchange.getIn().setHeader(FEATURE_DEPLOYMENT, featureDeployment);
				reqcontext = new RequestContext(tenant, site, featureGroup, featureName,
						featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
						featureDeployment.getFeatureVersion());
				logger.trace("{} req contxt : {}", LEAP_LOG_KEY, reqcontext);
				logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);

				return reqcontext;
			} else
				throw new DynamicallyImplRoutingFailedException(
						"unable to get the active and primary feature deployement : " + featureDeployment);
		} catch (FeatureDeploymentServiceException e) {
			LeapConfigurationUtil.setResponseCode(404, exchange, e.getMessage());
			throw new DynamicallyImplRoutingFailedException(
					"unable to get the active and primary feature deployement : " + featureDeployment, e);
		}
	}

	/**
	 * This method is use to initialize the leapDataContext object with private,
	 * initial headers and initial context
	 * 
	 * @param serviceName               : Name of the service
	 * @param requestBody               : request body
	 * @param isTenantValidationEnabled : tenant validation is enabled or not
	 * @param isAccessValidationEnabled : access validation is enabled or not
	 * @param leapServiceContext        : {@link LeapServiceContext} - used to
	 *                                  initialize private and initial headers
	 * @param leapDataContext           : {@link leapDataContext}
	 * @param exchange                  : {@link Exchange}
	 * @throws AccountFetchException
	 * @throws LeapDataContextInitialzerException
	 */
	private void initializeLeapDataContextObj(String serviceName, String requestBody, String isTenantValidationEnabled,
			String isAccessValidationEnabled, LeapServiceContext leapServiceContext, LeapDataContext leapDataContext,
			Exchange exchange) throws AccountFetchException, LeapDataContextInitialzerException {
		String methodName = "initializeLeapDataContextObj";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// step1. initialize private headers and add to leapDatacontext
		LeapDataContextElement privateHeaderElement = initializeLeapDataContextPrivateHeader(isTenantValidationEnabled,
				isAccessValidationEnabled, leapServiceContext, exchange);
		leapDataContext.addContextElement(privateHeaderElement, PRIVATE_HEADERS);

		// step2. initialize headers and add to leapDatacontext
		LeapDataContextElement requestHeaderElement = initializeLeapDataContextRequestHeader(leapServiceContext,
				exchange);
		leapDataContext.addContextElement(requestHeaderElement, HEADER);

		// getting the taxonmay id from the request if it is available
		String taxonomyIdFromRequest = getTaxonomyIdFromRequest(requestBody);

		// step3. initialize the initial context
		LeapDataContextElement initialContextElement = initializeInitialContext(requestBody, exchange,
				taxonomyIdFromRequest, leapDataContext);

		// adding the initial context element to LDC by applying the taxonamy id
		leapDataContext.addContextElement(initialContextElement, INITIAL_CONTEXT);

		JSONObject requestContextData = initializeInitialRequestContextData(initialContextElement);
		leapDataContext.addContextElement(requestContextData, INITIAL_CONTEXT_KIND, INITIAL_CONTEXT_KIND,
				taxonomyIdFromRequest);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}// end of method initializeLeapDataContextObj

	/**
	 * This method is used to get the Taxonmy Id from request if the value is exists
	 * other wise considering the taxonamy value as null
	 * 
	 * @param requestBody :: RequestBody as string from request
	 * @return taxonmayid
	 */
	private String getTaxonomyIdFromRequest(String requestBody) {
		String methodName = "getTaxonomyIdFromRequest";
		logger.debug("{} entered into the method {}, requestBody:{}", LEAP_LOG_KEY, methodName, requestBody);
		String taxonomy_id = null;

		// 1. check if request contain any taxonomy id or not. If not, it means
		// it is default (leapDefault). Therefore no need to change.
		// 2. if value contain "leapDefault". Then also we dont need to change
		// anything.
		// 3. If value is other than "leapDefault" then we need to set the
		// "leapDefault", to convert request to internal taxonomy first.
		if (requestBody != null && !requestBody.isEmpty()) {

			JSONObject requestBodyJson = null;
			JSONArray requestBodyJsonArray = null;
			if (requestBody.startsWith("{")) {
				requestBodyJson = new JSONObject(requestBody);
			} else {
				requestBodyJsonArray = new JSONArray(requestBody);
				requestBodyJson = requestBodyJsonArray.getJSONObject(0);
			}
			if (requestBodyJson.has(LeapDataContextConstant.REQUEST_LEAP_lOCAL)) {
				JSONObject leapLocalJsonObj = requestBodyJson.getJSONObject(REQUEST_LEAP_lOCAL);
				logger.trace("{} inside getTaxonomyIdFromRequest method of leapLocalJsonObj {}", LEAP_LOG_KEY,
						leapLocalJsonObj);
				JSONObject taxonomyJsonObj = leapLocalJsonObj.getJSONObject(TAXONOMY_KEY);

				if (taxonomyJsonObj.has(TAXONOMY_ID)) {
					taxonomy_id = taxonomyJsonObj.getString(TAXONOMY_ID);

					if (taxonomy_id.equalsIgnoreCase(LEAPDEFAULT_TAXONOMY))
						taxonomy_id = null;
					else
						taxonomy_id = LEAPDEFAULT_TAXONOMY;
				} // ..end of if condition, cheking taxonomy value is exist in
					// request or not
			} // ..end of if condition, checking if request is having the key
				// "leapLocal"
		} else {
			taxonomy_id = LEAPDEFAULT_TAXONOMY;
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return taxonomy_id;

	}// ..end of method getTaxonamyIdFromRequest

	/**
	 * This method is use to initialize the private header into the leap data
	 * context
	 * 
	 * @param isTenantValidationEnabled : tenant validation is enabled or not
	 * @param isAccessValidationEnabled : access validation is enabled or not
	 * @param leapServiceContext        : {@link LeapServiceContext} - used to
	 *                                  initialize private headers
	 * @param exchange                  : {@link Exchange}
	 * @return LeapDataContextElement Object
	 * @throws AccountFetchException
	 * @throws LeapDataContextInitialzerException
	 */
	private LeapDataContextElement initializeLeapDataContextPrivateHeader(String isTenantValidationEnabled,
			String isAccessValidationEnabled, LeapServiceContext leapServiceContext, Exchange exchange)
			throws AccountFetchException, LeapDataContextInitialzerException {
		String methodName = "initializeLeapDataContextPrivateHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		InitialLeapDataContextElement initialPrivateHeaderElement = new InitialLeapDataContextElement();
		Map<String, Object> privateHeaderList = initializePrivateHeader(leapServiceContext);
		setSecurityTokenInLeapDataContext(isTenantValidationEnabled, isAccessValidationEnabled, privateHeaderList,
				exchange);
		logger.trace("{} private headers are:{} ", LEAP_LOG_KEY, privateHeaderList.toString());
		initialPrivateHeaderElement.setPrivateHeaderElement(privateHeaderList);
		// setLeapContextConfig(leapDataContext, leapHeader);
		LeapDataContextElement privateHeaderElement = new LeapDataContextElement(PRIVATE_HEADERS,
				initialPrivateHeaderElement);
		logger.trace("{} privateHeaderElement : {}", LEAP_LOG_KEY, privateHeaderElement);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return privateHeaderElement;
	}// end of initializeLeapDataContextPrivateHeader

	/**
	 * This method is use to initialize the request header into the leap data
	 * context
	 * 
	 * @param leapServiceContext
	 * @param exchange           : Camel Exchange
	 * @return LeapDataContextElement Object
	 */
	private LeapDataContextElement initializeLeapDataContextRequestHeader(LeapServiceContext leapServiceContext,
			Exchange exchange) {
		String methodName = "initializeLeapDataContextRequestHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		InitialLeapDataContextElement initialRequestHeaderElement = new InitialLeapDataContextElement();
		initialRequestHeaderElement.setRequestHeaderElement(initializeRequestHeaders(leapServiceContext, exchange));
		LeapDataContextElement requestHeaderElement = new LeapDataContextElement(HEADER, initialRequestHeaderElement);
		logger.trace("{} requestHeaderElement :{} ", LEAP_LOG_KEY, requestHeaderElement);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return requestHeaderElement;
	}// end of method initializeLeapDataContextRequestHeader

	/**
	 * This method is use to initialize the initial context into the leap data
	 * context
	 * 
	 * @param requestBody            : Request body
	 * @param exchange               : Camel Exchange
	 * @param leapDataContext
	 * @param taxonomyIdFromRequest: taxonamy id from request
	 * @return LeapDataContextElement Object
	 */
	private LeapDataContextElement initializeInitialContext(String requestBody, Exchange exchange, String taxonomy_id,
			LeapDataContext leapDataContext) {
		String methodName = "initializeInitialContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContextElement initialContextElement = new LeapDataContextElement();
		LeapDataElement initialDataElement = constructLeapDataForRequest(requestBody, taxonomy_id, leapDataContext,
				exchange);
		initialContextElement.setDataElement(initialDataElement);
		logger.trace("{} initialContextElement : {}", LEAP_LOG_KEY, initialContextElement);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return initialContextElement;
	}// end of method initializeInitialContext

	/**
	 * This method is use to initialize the initial Request context data context
	 * 
	 * @param LeapDataContextElement : Request body
	 * @return JSONObject : Request context Object
	 */
	private JSONObject initializeInitialRequestContextData(LeapDataContextElement initialContextElement) {
		String methodName = "initializeInitialRequestContextData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject initialRequestJson = new JSONObject();
		LeapDataElement initialDataElement = initialContextElement.getDataElement();
		boolean requestContextAdded = false;
		if (initialDataElement != null) {
			LeapData leapDataElement = initialDataElement.getData();
			if (leapDataElement != null && leapDataElement.getItems() != null) {
				Object initialRequestData = leapDataElement.getItems().getData();
				initialRequestJson.put(INITIAL_CONTEXT_KIND, initialRequestData);
				requestContextAdded = true;
			}
		}
		if (!requestContextAdded) {
			initialRequestJson.put(INITIAL_CONTEXT_KIND, new JSONObject());
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return initialRequestJson;
	}// end of method initializeInitialRequestContextData

	/**
	 * Method to initialize the Private Headers
	 * 
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return Map<String, Object>
	 * @throws AccountFetchException
	 */
	private Map<String, Object> initializePrivateHeader(LeapServiceContext leapServiceContext)
			throws AccountFetchException {
		String methodName = "initializePrivateHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String externalTenantId = LeapTenantSecurityUtil.getExternalTenantId(leapServiceContext.getTenant(),
				leapServiceContext.getSite());
		Map<String, Object> privateHeaders = new HashMap<>();
		privateHeaders.put(ACCOUNTID, externalTenantId);
		privateHeaders.put(SITEID, leapServiceContext.getSite());
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return privateHeaders;
	}// end of initializePrivateHeader

	/**
	 * Method to initialize the Request Headers
	 * 
	 * @param leapHeader
	 * @param exchange
	 * @return
	 * @throws JSONException
	 */
	private Map<String, Object> initializeRequestHeaders(LeapServiceContext leapServiceContext, Exchange exchange) {
		String methodName = "initializeRequestHeaders";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> requestHeaders = new HashMap<>();
		FeatureDeployment featureDeployment = (FeatureDeployment) exchange.getIn().getHeader(FEATURE_DEPLOYMENT);
		if (featureDeployment != null) {
			// providing implementation, vendor and version support
			requestHeaders.put(IMPLEMENTATION_NAME, featureDeployment.getImplementationName());
			requestHeaders.put(VENDOR, featureDeployment.getVendorName());
			requestHeaders.put(VERSION, featureDeployment.getFeatureVersion());
		}
		requestHeaders.put(TENANTID, leapServiceContext.getTenant());
		requestHeaders.put(SITEID, leapServiceContext.getSite());
		requestHeaders.put(FEATUREGROUP, leapServiceContext.getFeatureGroup());
		requestHeaders.put(FEATURENAME, leapServiceContext.getFeatureName());
		requestHeaders.put(SERVICENAME, leapServiceContext.getRunningContextServiceName());
		Object httpMethod = exchange.getIn().getHeader(Exchange.HTTP_METHOD);
		if (httpMethod == null) {
			httpMethod = HTTP_POST;
		}
		requestHeaders.put(REQUEST_METHOD, httpMethod.toString());
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return requestHeaders;
	}// end of method createRequestHeaders

	/**
	 * This method is use to set the privacy token values in private headers if they
	 * are enabled
	 * 
	 * @param isTenantValidationEnabled : tenant validation is enabled or not
	 * @param isAccessValidationEnabled : access validation is enabled or not
	 * @param headerMap                 : private header Map<String, Object>
	 *                                  reference object
	 * @param exchange                  : {@link Exchange}
	 * @throws LeapDataContextInitialzerException
	 */
	private void setSecurityTokenInLeapDataContext(String isTenantValidationEnabled, String isAccessValidationEnabled,
			Map<String, Object> headerMap, Exchange exchange) throws LeapDataContextInitialzerException {
		String methodName = "setSecurityTokenInLeapDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
			logger.trace("{} Both access token and tenant token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			long expirationTime = Long
					.parseLong(messageMap.get(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME).toString());
			String tenantToken = (String) messageMap.get(TenantSecurityConstant.TENANT_TOKEN);
			String accessToken = (String) messageMap.get(OAuth.OAUTH_ACCESS_TOKEN);
			headerMap.put(TENANT_TOKEN, tenantToken);
			headerMap.put(ACCESS_TOKEN, accessToken);
			headerMap.put(TENANT_TOKEN_EXPIRATION, expirationTime);

		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
			logger.trace("{} Only access token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			String accessToken = (String) messageMap.get(OAuth.OAUTH_ACCESS_TOKEN);
			headerMap.put(ACCESS_TOKEN, accessToken);
		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())) {
			logger.trace("{} Only tenant token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			long expirationTime = Long
					.parseLong(messageMap.get(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME).toString());
			String tenantToken = (String) messageMap.get(TenantSecurityConstant.TENANT_TOKEN);
			headerMap.put(TENANT_TOKEN, tenantToken);
			headerMap.put(TENANT_TOKEN_EXPIRATION, expirationTime);
		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())) {
			logger.trace("{} Both access token and tenant token validation is disabled",LEAP_LOG_KEY);
		} else
			throw new LeapDataContextInitialzerException("Failed to initiate Header due to validation error.",
					new Throwable("Failed to initiate Header due to validation error."),
					"Failed to initiate Header due to validation error.", 400);

	}// end of the method setSecurityTokenInLeapDataContext

	/**
	 * This method is use to get the domain name
	 * 
	 * @param completdata
	 * @return domain name
	 * @throws AccountFetchException
	 */
	private String getDomainName(String completdata) throws AccountFetchException {
		String methodName = "getDomainName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			if (completdata != null && completdata.length() > 0 && !(completdata.isEmpty())) {
				JSONObject requestObj = new JSONObject(completdata);
				if (requestObj.has(TenantSecurityConstant.DOMAIN)) {
					String domain = requestObj.getString(TenantSecurityConstant.DOMAIN);
					logger.trace("{} Domain name from the request body : {}", LEAP_LOG_KEY, domain);
					logger.trace("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
					return domain;
				} else
					throw new AccountFetchException("No domin is specified in request body.");
			} else
				throw new AccountFetchException("Request body is empty.");
		} catch (JSONException e) {
			throw new AccountFetchException(e.getMessage(), e.getCause());
		}

	}// end of getDomainName

	/**
	 * This method is use to check if tenant token validation is require or not
	 * 
	 * @param exchange : {@link Exchange}
	 * @return {@link boolean} - access token validation is require or not
	 * @throws PropertiesConfigException - Exception is thrown when unable to load
	 *                                   the globaAppDeploymentConfig property fie
	 *                                   from the class path
	 */
	private String getTenantTokenValidationStatus(Exchange exchange) throws PropertiesConfigException {
		String methodName = "getTenantTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String isTenantTokenValid;
		Object tenantValidationEnableObj = exchange.getIn().getHeader(IS_TENANT_TOKEN_VALID);
		if (tenantValidationEnableObj != null) {
			// After login these values will be available in exchange header
			isTenantTokenValid = (String) tenantValidationEnableObj;
		} else {
			// for the very first request (login) take value from properties
			// file
			isTenantTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_TENANT_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_TENANT_TOKEN_VALID);
		}
		logger.trace("{} Tenant Token Validation status :{} ", LEAP_LOG_KEY, isTenantTokenValid);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return isTenantTokenValid;
	}// end of the method getTenantTokenValidationStatus

	/**
	 * This method is use to check if access token validation is required or not
	 * 
	 * @param exchange : {@link Exchange}
	 * @return {@link boolean} - access token validation is require or not
	 * @throws PropertiesConfigException - Exception is thrown when unable to load
	 *                                   the globaAppDeploymentConfig property fie
	 *                                   from the class path
	 */
	private String getAccessTokenValidationStatus(Exchange exchange) throws PropertiesConfigException {
		String methodName = "getAccessTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String isAccessTokenValid;
		Object accessValidationEnableObj = exchange.getIn().getHeader(IS_ACCESS_TOKEN_VALID);
		if (accessValidationEnableObj != null) {
			// After login these values will be available in exchange header
			isAccessTokenValid = (String) accessValidationEnableObj;
		} else {
			// for the very first request (login) take value from properties
			// file
			isAccessTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_ACCESS_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_ACCESS_TOKEN_VALID);
		}
		logger.trace("{} Access Token Validation status : {}", LEAP_LOG_KEY, isAccessTokenValid);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return isAccessTokenValid;
	}// end of the method getTenantTokenValidationStatus

	/**
	 * This method is used to removed the headers from the exchange
	 * 
	 * @param exchange : Camel Exchange object
	 */
	private void removeHeaderDetails(Exchange exchange) {
		exchange.getIn().removeHeader(ACCOUNTID);
		exchange.getIn().removeHeader(SITEID);
		exchange.getIn().removeHeader(TenantSecurityConstant.TENANT_TOKEN);
		exchange.getIn().removeHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME);
		exchange.getIn().removeHeader(OAuth.OAUTH_ACCESS_TOKEN);
	}// end of the method removeHeaderDetails

	/**
	 * Method to construct LeapData Object for Request Data
	 * 
	 * @param leapDataElement
	 * @param exchange
	 * @return {@link JSONObject}
	 * @throws JSONException
	 * @throws ParseException
	 */
	public static JSONObject constructLeapDataJsonForRequest(LeapDataElement leapDataElement, Exchange exchange)
			throws JSONException, ParseException {
		JSONObject req = new JSONObject();
		req.put(API_VERSION, leapDataElement.getApiVersion());
		req.put(CONTEXT, leapDataElement.getContext());
		req.put(LANG, leapDataElement.getLang());
		req.put(MEDIA_TYPE, exchange.getIn().getHeader("Content-Type"));
		req.put(ELEMENT, (org.json.simple.JSONObject) new JSONParser()
				.parse(leapDataElement.getData().getItems().getData().toString()));
		return req;
	}

	/**
	 * Method to construct LeapData Object for Response Data
	 * 
	 * @param exchange
	 * @return {@link JSONObject}
	 * @throws JSONException
	 * @throws ParseException
	 */
	public static JSONObject constructLeapDataJsonForResponse(Exchange exchange) throws JSONException, ParseException {
		JSONObject res = new JSONObject();
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		res.put("id", leapServiceContext.getRequestUUID());

		res.put(API_VERSION, API_VERSION_VAL);
		res.put(CONTEXT, CONTEXT);
		res.put("selfLink",
				"/" + leapServiceContext.getRunningContextServiceName() + "/" + leapServiceContext.getRequestUUID());
		JSONObject data = new JSONObject();
		data.put("kind", "response");
		data.put("updated", new Date().toString());
		int count = 0;
		Object exchangeBody = exchange.getIn().getBody();

		if (exchangeBody != null)
			if (exchangeBody.toString().startsWith("{")) {
				count = 1;
				data.put("items", (org.json.simple.JSONObject) new JSONParser().parse(exchangeBody.toString()));

			} else if (exchangeBody.toString().startsWith("[")) {
				JSONArray exchangeArray = new JSONArray(exchangeBody.toString());
				count = exchangeArray.length();
				data.put("items", (org.json.simple.JSONArray) new JSONParser().parse(exchangeBody.toString()));

			} else {
				count = 1;
				data.put("items", exchangeBody);
			}

		data.put("totalItems", count);

		res.put("data", data);
		return res;
	}

	/**
	 * Method to construct the LeapData body
	 * 
	 * @param exchange
	 * @param taxonomy_id
	 * @param leapDataContext
	 * @return {@link LeapDataElement}
	 */
	private LeapDataElement constructLeapDataForRequest(String requestBody, String taxonomy_id,
			LeapDataContext leapDataContext, Exchange exchange) {
		LeapDataElement leapDataElement = new LeapDataElement();
		String methodName = "constructLeapDataForRequest";
		logger.debug("{} entered into the method {}, requestBody{}, taxonomy_id{}", LEAP_LOG_KEY, methodName,
				requestBody, taxonomy_id);
		if (requestBody == null)
			requestBody = "{}";
		LeapData leapData = new LeapData();
		LeapResultSet leapJSONResultSet = new LeapJSONResultSet();
		LeapResultSet leapJSONArrayResultSet = new LeapJSONArrayResultSet();
		LeapResultSet leapXMLResultSet = new LeapXMLResultSet();
		JSONObject exchangeJson = null;
		JSONArray exchangeArray;
		String apiVersion = null, lang = null, context = null;
		String contentType = (String) exchange.getIn().getHeader(LeapDataContextInitializerConstant.CONTENT_TYPE);
		logger.trace("{} contentType ::{} ", LEAP_LOG_KEY, contentType);
		if (requestBody != null && !requestBody.isEmpty()) {
			requestBody = requestBody.trim();
			if (contentType == null || contentType.isEmpty()) {
				contentType = LeapDataContextInitializerConstant.CONTENT_TYPE_JSON;
			}
			if (contentType.equals(LeapDataContextInitializerConstant.CONTENT_TYPE_JSON)
					|| contentType.equals(LeapDataContextInitializerConstant.CONTENT_TYPE_JSON_LEAP)) {
				if (requestBody.startsWith("{")) {
					exchangeJson = new JSONObject(requestBody);
					leapJSONResultSet.setData(exchangeJson);
					leapData.setItems(leapJSONResultSet);
				} else {
					exchangeArray = new JSONArray(requestBody);
					leapJSONArrayResultSet.setData(exchangeArray);
					exchangeJson = exchangeArray.getJSONObject(0);
					leapData.setItems(leapJSONArrayResultSet);
				}

			} else {
				exchangeJson = XML.toJSONObject(requestBody);
				leapXMLResultSet.setData(requestBody);
				leapData.setItems(leapXMLResultSet);
				// exchangeJson = exchangeJson.getJSONObject("root");
			}
			List<MetaData> metaDataList = new ArrayList<>();
			// this is handled because if we are passing the content type other
			// than application/json the metadata list is coming as empty
			if (exchangeJson != null && exchangeJson.length() == 0)
				exchangeJson = new JSONObject(requestBody);

			// for original request also we are applying the taxonomy while
			// pushing the data into LDC deque
			try {
				metaDataList = LeapDataContextHelper.createMetaData(exchangeJson, taxonomy_id, leapDataContext);
			} catch (LeapDataContextConfigException e) {
				e.printStackTrace();
			}
			// metaDataList = createMetaData(exchangeJson);

			leapData.setKind(KIND);
			leapData.setMetadata(metaDataList);
			leapData.setTaxonomyId(taxonomy_id);

			if (contentType.equals(LeapDataContextInitializerConstant.CONTENT_TYPE_JSON_LEAP)) {

				if (exchangeJson.has(API_VERSION)) {
					apiVersion = exchangeJson.getString(API_VERSION);
					if (apiVersion != null) {
						leapDataElement.setApiVersion(apiVersion);
					}
				} else {
					leapDataElement.setApiVersion(API_VERSION_VAL);
				}

				if (exchangeJson.has(CONTEXT)) {
					context = exchangeJson.getString(CONTEXT);
					if (context != null) {
						leapDataElement.setContext(context);
					}
				} else {
					leapDataElement.setContext(CONTEXT);
				}

				if (exchangeJson.has(LANG)) {
					lang = exchangeJson.getString(LANG);
					if (lang != null) {
						leapDataElement.setLang(lang);
					}
				} else {
					leapDataElement.setLang(LANG_VAL);
				}

			} else {
				leapDataElement.setApiVersion(API_VERSION_VAL);
				leapDataElement.setLang(LANG_VAL);
				leapDataElement.setContext(CONTEXT);
			}

			leapDataElement.setData(leapData);
		}
		logger.debug("{} after setting the data into LDC for intial tag :: {}", LEAP_LOG_KEY,
				leapDataElement.toString());
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return leapDataElement;

	}

	/**
	 * Method to create the LeapDataContext for Response
	 * 
	 * @param dataSet
	 * @param exchange
	 * @return LeapDataContext Object
	 * @throws LeapDataContextConfigException
	 */
	public void buildResponseLeapContext(DataSet dataSet, String kind, Exchange exchange)
			throws LeapDataContextConfigException {
		String methodName = "buildResponseLeapContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn().getHeader(LEAP_DATA_CONTEXT);
		leapDataContext = constructResponseContext(leapDataContext, dataSet, kind, exchange);
		exchange.getIn().setHeader(LEAP_DATA_CONTEXT, leapDataContext);
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Method to create the context element for a service response
	 * 
	 * @param leapDataContext
	 * @param dataSet
	 * @param kind
	 * @param exchange
	 * @return LeaoDataContext Object
	 * @throws LeapDataContextConfigException
	 */
	private LeapDataContext constructResponseContext(LeapDataContext leapDataContext, DataSet dataSet, String kind,
			Exchange exchange) throws LeapDataContextConfigException {
		LeapDataContextElement dataContextElement = new LeapDataContextElement();
		LeapDataElement constructLeapData = constructLeapDataForResponse(dataSet, kind, exchange);
		dataContextElement.setDataElement(constructLeapData);
		leapDataContext.addContextElement(dataContextElement, RESPONSE_CONTEXT);
		// leapDataContext.getContextElement("header");
		return leapDataContext;
	}

	/**
	 * Method to create the LeapDataElement from the MetaModel DataSet
	 * 
	 * @param dataSet
	 * @param exchange
	 * @return
	 * @throws LeapDataContextConfigException
	 */
	private LeapDataElement constructLeapDataForResponse(DataSet dataSet, String kind, Exchange exchange)
			throws LeapDataContextConfigException {
		LeapData leapData = new LeapData();
		LeapDataElement leapDataElement = new LeapDataElement();
		JSONArray eventParamJsonArr = new JSONArray();
		boolean noSQL = LeapEntityArchivalUtility.isSQL(exchange);
		if (noSQL) {
			eventParamJsonArr = getDataForNoSql(dataSet, exchange);
			logger.trace("{} eventParamJsonArr : {}", LEAP_LOG_KEY, eventParamJsonArr);
		} else {
			eventParamJsonArr = getDataForSql(dataSet, exchange);
			logger.trace("{} eventParamJsonArr : {}", LEAP_LOG_KEY, eventParamJsonArr);
		}
		LeapResultSet mmItems = new LeapJSONArrayResultSet();
		mmItems.setData(eventParamJsonArr);
		leapData.setItems(mmItems);
		List<MetaData> metaDataList = new ArrayList<>();
		metaDataList = createMetaData(eventParamJsonArr);
		logger.trace("{} metaDataList : {}", LEAP_LOG_KEY, metaDataList);

		leapData.setKind(kind);
		leapData.setMetadata(metaDataList);
		logger.debug("{} data : {}", LEAP_LOG_KEY, leapData.toString());

		leapDataElement.setApiVersion(API_VERSION);
		leapDataElement.setContext(CONTEXT);
		leapDataElement.setLang(LANG);
		leapDataElement.setData(leapData);
		logger.trace("{} leapDataElement : {}", LEAP_LOG_KEY, leapDataElement);
		return leapDataElement;
	}

	/**
	 * Method to get all rows of data from the Sql dataset
	 * 
	 * @param dataSet
	 * @param exchange
	 * @return {@link JSONArray}
	 * @throws LeapDataContextConfigException
	 */
	public JSONArray getDataForSql(DataSet dataSet, Exchange exchange) throws LeapDataContextConfigException {
		String methodName = "getDataForSql";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		JSONObject rowJSONObject;
		dataArray = new JSONArray();
		try {
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				rowJSONObject = new JSONObject();
				for (SelectItem selectItem : dataSet.getSelectItems()) {
					rowJSONObject.put(selectItem.getColumn().getName(), row.getValue(selectItem));
				}
				dataArray.put(rowJSONObject);
			}
		} catch (Exception e) {
			throw new LeapDataContextConfigException(e.getMessage(), e.getCause(),
					"Unable to build data array from Sql DataSet", 400);
		}
		logger.debug("{} exiting from the method {}", LEAP_LOG_KEY, methodName);
		return dataArray;
	}

	/**
	 * Method to get all rows of data from the noSql dataset
	 * 
	 * @param dataSet
	 * @param exchange
	 * @return {@link JSONArray}
	 * @throws LeapDataContextConfigException
	 */
	public JSONArray getDataForNoSql(DataSet dataSet, Exchange exchange) throws LeapDataContextConfigException {
		JSONArray valueArray = new JSONArray();
		JSONObject rowJSONObject = new JSONObject();
		JSONObject eventParamJsonObj = new JSONObject();
		dataArray = new JSONArray();
		try {
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				for (SelectItem selectItem : dataSet.getSelectItems()) {
					rowJSONObject.put(selectItem.getColumn().getName(), row.getValue(selectItem));
					valueArray.put(rowJSONObject);
					eventParamJsonObj = new JSONObject(rowJSONObject.get("eventbody").toString());
					dataArray.put(eventParamJsonObj);
				}
			}
		} catch (JSONException e) {
			throw new LeapDataContextConfigException(e.getMessage(), e.getCause(),
					"Unable to build data array from NoSql DataSet", 400);
		}
		return dataArray;
	}

	/**
	 * 
	 * @param map
	 * @return List<MetaData>
	 */
	private List<MetaData> parserMapData(Map<String, String> map) {
		List<MetaData> list = new ArrayList<>();
		MetaData data;
		for (Entry<String, String> entry : map.entrySet()) {
			data = parseMetaData(entry.getValue(), entry.getKey());
			list.add(data);
		}
		return list;
	}

	/**
	 * 
	 * @param val
	 * @param key
	 * @return {@link MetaData}
	 */
	private MetaData parseMetaData(String val, String key) {
		MetaData data = new MetaData();
		data.setActualColumnName(key);
		data.setByteLenth(key.length());
		data.setType(val);
		data.setEffectiveColumnName(key.toUpperCase());
		// data.setI18nID("dd");
		// data.setI18nLangText("sd");
		return data;
	}

	/**
	 * 
	 * @param jsonIn
	 * @param map
	 */
	private void findKeysOfJsonArray(JSONArray jsonIn, Map<String, String> map) {
		if (jsonIn != null && jsonIn.length() != 0) {
			for (int i = 0; i < jsonIn.length(); i++) {
				if (jsonIn.get(i) instanceof JSONArray) {
					JSONArray jsonObjArray = jsonIn.getJSONArray(i);
					findKeysOfJsonArray(jsonObjArray, map);
				} else if (jsonIn.get(i) instanceof String) {
					map.put(jsonIn.getString(i), "String");
				} else {
					JSONObject jsonObjIn = jsonIn.getJSONObject(i);
					findKeysOfJsonObject(jsonObjIn, map);
				}
			}
		}
	}

	/**
	 * 
	 * @param jsonIn
	 * @param map
	 */
	private void findKeysOfJsonObject(JSONObject jsonIn, Map<String, String> map) {
		Iterator<String> itr = jsonIn.keys();
		itr = jsonIn.keys();
		while (itr.hasNext()) {
			String itrStr = itr.next();
			Object obj = jsonIn.get(itrStr);
			JSONObject jsout = null;
			JSONArray jsArr = null;
			if (obj instanceof JSONObject) {
				map.put(itrStr, "Object");
				jsout = (JSONObject) obj;
				findKeysOfJsonObject(jsout, map);
			} else if (obj instanceof JSONArray) {
				map.put(itrStr, "Object");
				jsArr = (JSONArray) obj;
				findKeysOfJsonArray(jsArr, map);
			} else {
				if (obj instanceof String) {
					map.put(itrStr, "String");
				} else if (obj instanceof Integer) {
					map.put(itrStr, "Integer");
				} else if (obj instanceof Boolean) {
					map.put(itrStr, "Boolean");
				} else {
					map.put(itrStr, "Double");
				}
			}
		}
	}

	/**
	 * Method to build MetaData for the input Object
	 * 
	 * @param obj
	 * @param taxonomy_id
	 * @return List<MetaData>
	 */
	public List<MetaData> createMetaData(Object obj) {
		Map<String, String> map = new HashMap<>();
		if (obj instanceof JSONArray) {
			JSONArray array = (JSONArray) obj;
			findKeysOfJsonArray(array, map);
		} else {
			JSONObject json = (JSONObject) obj;
			findKeysOfJsonObject(json, map);
		}
		return parserMapData(map);
	}

}
