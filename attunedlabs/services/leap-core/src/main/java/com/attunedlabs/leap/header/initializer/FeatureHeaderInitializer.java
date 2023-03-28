package com.attunedlabs.leap.header.initializer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_ACCESS_TOKEN_VALID;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_TENANT_TOKEN_VALID;

import java.util.Map;
import java.util.Properties;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;

public class FeatureHeaderInitializer implements Processor {

	protected static final Logger logger = LoggerFactory.getLogger(FeatureHeaderInitializer.class);

	/**
	 * This method is used to set header for request serviceName,tenantid and data
	 * 
	 * @param exchange : Exchange Object
	 * @throws JsonParserException
	 * @throws InvalidXATransactionException
	 * @throws JSONException
	 * @throws AccountFetchException
	 * @throws FeatureHeaderInitialzerException
	 */
	public void process(Exchange exchange)
			throws JsonParserException, JSONException, AccountFetchException, FeatureHeaderInitialzerException {
		long startTime = System.currentTimeMillis();
		String methodName = "process";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
		try {

			// checking if subscription call or not set the extra property
			if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY) == null)
				exchange.setProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, false);
			logger.trace("{} =============================================================================",
					LEAP_LOG_KEY);
			logger.trace("{} featureHeaderInit[start] : {}", LEAP_LOG_KEY, System.currentTimeMillis());
			logger.trace("{} inside Header Initializer bean process()", LEAP_LOG_KEY);
			String completdata = exchange.getIn().getBody(String.class);
			String tenant = null;
			String site = null;
			String serviceName = (String) exchange.getIn().getHeader(LeapHeaderConstant.SERVICENAME_KEY);
			String featureGroup = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_GROUP_KEY);
			String featureName = (String) exchange.getIn().getHeader(LeapHeaderConstant.FEATURE_KEY);
			String endpointType = (String) exchange.getIn().getHeader(LeapHeaderConstant.ENDPOINT_TYPE_KEY);
			LeapHeader leapHeaderAlreadyExists = (LeapHeader) exchange.getIn()
					.getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
			String isTenantValidationEnabled = getTenantTokenValidationStatus(exchange);
			String isAccessValidationEnabled = getAccessTokenValidationStatus(exchange);
			LeapHeader leapHeader;
			if (leapHeaderAlreadyExists != null) {
				logger.trace("{} Header already exist.....", LEAP_LOG_KEY);
				tenant = leapHeaderAlreadyExists.getTenant();
				site = leapHeaderAlreadyExists.getSite();
				if (!tenant.equalsIgnoreCase(LeapHeaderConstant.GLOBAL_TENANT_ID)
						&& featureGroup.equalsIgnoreCase(LeapHeaderConstant.AUTHENTICATION_FEATURE_GROUP)
						&& featureName.equalsIgnoreCase(LeapHeaderConstant.AUTHENTICATION_FEATURE)) {
					tenant = LeapHeaderConstant.GLOBAL_TENANT_ID;
					site = LeapHeaderConstant.GLOBAL_SITE_ID;
				}
				leapHeader = new LeapHeader(tenant, site);
				// for subscription set the leapHeader
				if (exchange.getProperty(SubscriptionConstant.IS_SUBSCRIPTION_INVOCATION_KEY, Boolean.class)) {
					Map<String, Object> oldLeap = leapHeader.getOriginalLeapHeader();
					oldLeap.put(LeapHeaderConstant.ORIGINAL_LEAP_HEADER_KEY, leapHeaderAlreadyExists);
				}

			} else {
				logger.debug("{} No header exixts.. featureName:{} featureGroup :{} ", LEAP_LOG_KEY, featureName,
						featureGroup);
				if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
						&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
						&& RootDeployableConfiguration.isRootDeployableFeature(featureGroup, featureName)) {
					logger.trace("{} Authentication is set to false & globally deployed.", LEAP_LOG_KEY);
					tenant = LeapHeaderConstant.GLOBAL_TENANT_ID;
					site = LeapHeaderConstant.GLOBAL_SITE_ID;
					leapHeader = new LeapHeader(tenant, site);

				} else {
					Object siteIdObj = exchange.getIn().getHeader(LeapHeaderConstant.SITE_KEY);
					Object accountNameObj = exchange.getIn().getHeader(LeapHeaderConstant.ACCOUNT_ID);
					AccountConfiguration configuration;
					if (serviceName.equals(LeapHeaderConstant.LOGIN_SERVICE) && siteIdObj == null
							&& accountNameObj == null)
						configuration = LeapTenantSecurityUtil
								.getAccountConfigurationByDomin(getDomainName(completdata));
					else {
						if (siteIdObj == null)
							throw new FeatureHeaderInitialzerException("SiteId is not specified in the request header");
						if (accountNameObj == null)
							throw new FeatureHeaderInitialzerException(
									"AccountId is not specified in the request header");
						site = (String) exchange.getIn().getHeader(LeapHeaderConstant.SITE_KEY);
						String accountName = (String) exchange.getIn().getHeader(LeapHeaderConstant.ACCOUNT_ID);
						configuration = LeapTenantSecurityUtil.getAccountConfigurationByExternalTenantId(accountName,
								site);
					}
					if (configuration == null)
						throw new FeatureHeaderInitialzerException("No Account is configured for " + accountNameObj);
					exchange.getIn().setHeader(LeapHeaderConstant.TIMEZONE, configuration.getTimezone());
					tenant = configuration.getInternalTenantId();
					site = configuration.getInternalSiteId();
					leapHeader = getLeapHeader(exchange, tenant, site, isTenantValidationEnabled,
							isAccessValidationEnabled);
				}
			}
			leapHeader.setServiceName(serviceName);
			leapHeader.setFeatureGroup(featureGroup);
			leapHeader.setFeatureName(featureName);
			leapHeader.setEndpointType(endpointType != null ? endpointType : LeapHeaderConstant.HTTP_JSON_KEY);
			if (completdata != null) {
				exchange.getIn().setBody(completdata);
			}
			exchange.getIn().setHeader(LeapHeaderConstant.LEAP_HEADER_KEY, leapHeader);
			removeHeaderDetails(exchange);
			logger.trace("{} leap header in LeapInitializer class : {}", LEAP_LOG_KEY, leapHeader);
			logger.trace("{} tenant : {}, site : {}", LEAP_LOG_KEY, tenant, site);
		} catch (PropertiesConfigException e) {
			LeapConfigurationUtil.setResponseCode(500, exchange, e.getMessage());
			throw new FeatureHeaderInitialzerException("unable to load the properties file", e);
		}
		long endTime = System.currentTimeMillis();
		logger.debug("{} TimeTaken in FeatureHeaderInitializer :{} ", LEAP_LOG_KEY, (endTime - startTime));
		logger.debug("{} exiting from the method {}.{}()", LEAP_LOG_KEY, getClass().getName(), methodName);
	}

	private String getDomainName(String completdata) throws AccountFetchException {
		String methodName = "getDomainName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			if (completdata != null && completdata.length() > 0 && !(completdata.isEmpty())) {
				JSONObject requestObj = new JSONObject(completdata);
				if (requestObj.has(TenantSecurityConstant.DOMAIN)) {
					String domain = requestObj.getString(TenantSecurityConstant.DOMAIN);
					logger.debug("{} Domain name from the request body : {}", LEAP_LOG_KEY, domain);
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
					return domain;
				} else
					throw new AccountFetchException("No domin is specified in request body.");
			} else
				throw new AccountFetchException("Request body is empty.");
		} catch (JSONException e) {
			throw new AccountFetchException(e.getMessage(), e.getCause());
		}

	}

	private String getTenantTokenValidationStatus(Exchange exchange) throws PropertiesConfigException {
		String methodName = "getTenantTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String isTenantTokenValid;
		Object tenantValidationEnableObj = exchange.getIn().getHeader(IS_TENANT_TOKEN_VALID);
		if (tenantValidationEnableObj != null) {
			isTenantTokenValid = exchange.getIn().getHeader(IS_TENANT_TOKEN_VALID, String.class);
		} else {
			isTenantTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_TENANT_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_TENANT_TOKEN_VALID);
		}
		logger.debug("{} Tenant Token Validation status : {}", LEAP_LOG_KEY, isTenantTokenValid);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isTenantTokenValid;
	}// end of the method

	private String getAccessTokenValidationStatus(Exchange exchange) throws PropertiesConfigException {
		String methodName = "getAccessTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String isAccessTokenValid;
		Object accessValidationEnableObj = exchange.getIn().getHeader(IS_ACCESS_TOKEN_VALID);
		if (accessValidationEnableObj != null) {
			isAccessTokenValid = exchange.getIn().getHeader(IS_ACCESS_TOKEN_VALID, String.class);
		} else {
			isAccessTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_ACCESS_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_ACCESS_TOKEN_VALID);
		}
		logger.debug("{} Access Token Validation status : {}", LEAP_LOG_KEY, isAccessTokenValid);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isAccessTokenValid;
	}// end of the method

	private void removeHeaderDetails(Exchange exchange) {
		exchange.getIn().removeHeader(LeapHeaderConstant.ACCOUNT_ID);
		exchange.getIn().removeHeader(LeapHeaderConstant.SITE_KEY);
		exchange.getIn().removeHeader(TenantSecurityConstant.TENANT_TOKEN);
		exchange.getIn().removeHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME);
		exchange.getIn().removeHeader(OAuth.OAUTH_ACCESS_TOKEN);
	}// end of the method

	private LeapHeader getLeapHeader(Exchange exchange, String tenant, String site, String isTenantValidationEnabled,
			String isAccessValidationEnabled) throws FeatureHeaderInitialzerException {
		String methodName = "getLeapHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapHeader leapHeader;
		if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
			logger.trace("{} Both access token and tenant token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			long expirationTime = Long
					.parseLong(messageMap.get(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME).toString());
			String tenantToken = (String) messageMap.get(TenantSecurityConstant.TENANT_TOKEN);
			String accessToken = (String) messageMap.get(OAuth.OAUTH_ACCESS_TOKEN);
			leapHeader = new LeapHeader(tenant, site, tenantToken, accessToken, expirationTime);
		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
			logger.trace("{} Only access token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			String accessToken = (String) messageMap.get(OAuth.OAUTH_ACCESS_TOKEN);
			leapHeader = new LeapHeader(tenant, site, accessToken);
		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.TRUE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())) {
			logger.trace("{} Only tenant token validation is enabled", LEAP_LOG_KEY);
			Map<String, Object> messageMap = exchange.getIn().getHeaders();
			long expirationTime = Long
					.parseLong(messageMap.get(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME).toString());
			String tenantToken = (String) messageMap.get(TenantSecurityConstant.TENANT_TOKEN);
			leapHeader = new LeapHeader(tenant, site, tenantToken, expirationTime);
		} else if (isTenantValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())
				&& isAccessValidationEnabled.equalsIgnoreCase(Boolean.FALSE.toString())) {
			logger.trace("{} Both access token and tenant token validation is disabled", LEAP_LOG_KEY);
			leapHeader = new LeapHeader(tenant, site);
		} else
			throw new FeatureHeaderInitialzerException("Failed to initiate Header due to validation error.");
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return leapHeader;
	}// end of the method

}
