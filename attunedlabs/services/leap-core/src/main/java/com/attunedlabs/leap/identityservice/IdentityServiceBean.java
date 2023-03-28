package com.attunedlabs.leap.identityservice;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATUREGROUP;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.FEATURENAME;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.LEAP_DATA_CONTEXT;
import static com.attunedlabs.leap.context.constant.LeapDataContextConstant.SERVICENAME;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.ACCESS_TOKEN;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.ACCESS_TOKEN_SKIP_SERVICES;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.AUTHERIZATION;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.FALSE_KEY;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IDENTIFIER;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_ACCESS_TOKEN_VALID;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_AUTHZ_ENABLED;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.IS_TENANT_TOKEN_VALID;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.OAUTH_VALIDATOR_SERVIE_URL;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.TENANT_TOKEN_SKIP_SERVICES;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.TOKEN_TYPE;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.TOKEN_TYPE_VALUE;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.VALIDATION_REQUEST_DTO;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.XSD_NAMESPACE1;
import static com.attunedlabs.leap.identityservice.IdentityServiceConstant.XSD_NAMESPACE2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.integrationfwk.pipeline.service.PipelineServiceConstant;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.header.initializer.FeatureHeaderInitialzerException;
import com.attunedlabs.leap.header.initializer.JsonParserException;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.attunedlabs.security.exception.TenantTokenValidationException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;
import com.attunedlabs.security.utils.TenantSecurityUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class IdentityServiceBean {
	private static Logger logger = LoggerFactory.getLogger(IdentityServiceBean.class.getName());
	private static Properties properties;
	private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder;
	private SOAPConnectionFactory soapConnFactory;
	private MessageFactory msgFct;
	private SOAPConnection soapCon;
	private LeapByteArrayOutputStream outStream = new LeapByteArrayOutputStream();

	static {
		try {
			initializeClientCall();
			properties = LeapConfigUtil.getGlobalAppDeploymentConfigProperties();
		} catch (TrustValidationException | PropertiesConfigException e) {
			logger.error("{} error msg: {}", LEAP_LOG_KEY, e.getMessage());
		}
	}

	public void isAuthenticated(Exchange exchange) throws IdentityServiceException, JsonParserException, JSONException {
		long startTime = System.currentTimeMillis();
		logger.debug("{} Starting Time isAuthenticated : {}", LEAP_LOG_KEY, (startTime));
		String methodName = "isAuthenticated";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Message message = exchange.getIn();
		try {
			String isTenantTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_TENANT_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_TENANT_TOKEN_VALID);
			if (isTenantTokenValid.equalsIgnoreCase(IdentityServiceConstant.TRUE_KEY))
				isTenantTokenValid = getTenantTokenValidationStatus(message, isTenantTokenValid);
			String isAccessTokenValid = LeapConfigUtil.getGlobalPropertyValue(IS_ACCESS_TOKEN_VALID,LeapDefaultConstants.DEFAULT_IS_ACCESS_TOKEN_VALID);
			if (isAccessTokenValid.equalsIgnoreCase(IdentityServiceConstant.TRUE_KEY))
				isAccessTokenValid = getAccessTokenValidationStatus(message, isAccessTokenValid);
			if (isTenantTokenValid.equalsIgnoreCase(IdentityServiceConstant.TRUE_KEY)
					&& isAccessTokenValid.equalsIgnoreCase(IdentityServiceConstant.FALSE_KEY))
				createJWTTenantToken(message);
			String isAuthzEnable = LeapConfigUtil.getGlobalPropertyValue(IS_AUTHZ_ENABLED,LeapDefaultConstants.DEFAULT_IS_AUTHZ_ENABLED);
			if (isAuthzEnable.equalsIgnoreCase(IdentityServiceConstant.TRUE_KEY))
				message.setHeader(IS_AUTHZ_ENABLED, isAuthzEnable);

			message.setHeader(IS_TENANT_TOKEN_VALID, isTenantTokenValid);
			message.setHeader(IS_ACCESS_TOKEN_VALID, isAccessTokenValid);
		} catch (JSONException | PropertiesConfigException e) {
			LeapConfigurationUtil.setResponseCode(422, exchange, e.getMessage());
			throw new IdentityServiceException("Unable to load the Property of SkipServices into JSONArray", e);
		}
		long endTime = System.currentTimeMillis();
		logger.debug("TimeTaken in isAuthenticated : " + (endTime - startTime));
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method

	private String getTenantTokenValidationStatus(Message message, String isTenantTokenValid) throws JSONException {
		String methodName = "getTenantTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			JSONArray servicesToSkip = new JSONArray(LeapConfigUtil.getGlobalPropertyValue(TENANT_TOKEN_SKIP_SERVICES,LeapDefaultConstants.DEFAULT_TENANT_TOKEN_SKIP_SERVICES));
			for (int i = 0; i < servicesToSkip.length(); i++) {
				String service = servicesToSkip.getString(i);
				String[] serviceDetail = service.trim().split("-");
				if (message.getHeader(FEATUREGROUP).toString().equalsIgnoreCase(serviceDetail[0])) {
					if (message.getHeader(FEATURENAME).toString().equalsIgnoreCase(serviceDetail[1])) {
						if (message.getHeader(SERVICENAME).toString().equalsIgnoreCase(serviceDetail[2])) {
							isTenantTokenValid = FALSE_KEY;
							try {
								addTenantTokenDetailsForLoginService(message);
							} catch (IdentityServiceException | AccountFetchException e) {
								logger.error("{} unable to add the tenant token deatils in exchange header",
										LEAP_LOG_KEY);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("{} Tenant token Validation Status : {}", LEAP_LOG_KEY, isTenantTokenValid);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isTenantTokenValid;
	}// end of the method

	private String getAccessTokenValidationStatus(Message message, String isAccessTokenValid) throws IdentityServiceException {
		String methodName = "getAccessTokenValidationStatus";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			JSONArray servicesToSkip = new JSONArray(LeapConfigUtil.getGlobalPropertyValue(ACCESS_TOKEN_SKIP_SERVICES,LeapDefaultConstants.DEFAULT_ACCESS_TOKEN_SKIP_SERVICES));
			for (int i = 0; i < servicesToSkip.length(); i++) {
				String service = servicesToSkip.getString(i);
				String[] serviceDetail = service.trim().split("-");
				if (message.getHeader(FEATUREGROUP).toString().equalsIgnoreCase(serviceDetail[0])) {
					if (message.getHeader(FEATURENAME).toString().equalsIgnoreCase(serviceDetail[1])) {
						if (message.getHeader(SERVICENAME).toString().equalsIgnoreCase(serviceDetail[2])) {
							isAccessTokenValid = FALSE_KEY;
						}
					}
				}
			}
			logger.debug("{} Access token Validation Status : {}", LEAP_LOG_KEY, isAccessTokenValid);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (JSONException | PropertiesConfigException e) {
			throw new IdentityServiceException("Unable to load the Property of accessTokenSkipServices into JSONArray", e);

		}
		return isAccessTokenValid;
	}// end of the method

	/**
	 * This method is used to create the JWT token if the istenantenabled is true
	 * and isauthenabled is false
	 * 
	 * @param message :: {@link Message}
	 */
	private void createJWTTenantToken(Message message) {
		JSONObject jwtHeaderJson = new JSONObject();
		JSONObject payloadDataJson = new JSONObject();
		JSONObject tenantSiteDataJson = new JSONObject();
		try {
			addTenantTokenDetailsForLoginService(message);
			String accountId = message.getHeader(LeapDataContextConstant.ACCOUNTID, String.class);
			String siteId = message.getHeader(LeapDataContextConstant.SITEID, String.class);
			AccountConfiguration accountConfig = LeapTenantSecurityUtil
					.getAccountConfigurationByExternalTenantId(accountId, siteId);
			if (accountConfig == null)
				throw new FeatureHeaderInitialzerException("No Account is configured for " + accountId);
			String tenantTokenFromHeader = message.getHeader(TenantSecurityConstant.TENANT_TOKEN_LOGIN, String.class);
			String tenantTokenExpTimeFromHeader = message
					.getHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME_LOGIN, String.class);
			jwtHeaderJson.put(IdentityServiceConstant.JWT_TYPE_KEY, IdentityServiceConstant.JWT_TYPE_VALUE);
			jwtHeaderJson.put(IdentityServiceConstant.JWT_TYPE_ALGO, IdentityServiceConstant.JWT_TYPE_ALGO_VALUE);
			byte[] headerData = jwtHeaderJson.toString().getBytes();
			String headerbase64Encoded = DatatypeConverter.printBase64Binary(headerData);
			payloadDataJson.put(IdentityServiceConstant.TENANT_SITE_INFO_KEY,
					tenantSiteDataJson.put(LeapDataContextConstant.TENANT_TOKEN, tenantTokenFromHeader)
							.put(LeapDataContextConstant.TENANT_TOKEN_EXPIRATION, tenantTokenExpTimeFromHeader));
			byte[] payloadData = payloadDataJson.toString().getBytes();
			String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
			byte[] securityloadData = TenantSecurityUtil.getMD5(accountConfig.getInternalTenantId(),
					accountConfig.getInternalSiteId());
			String securityloadDatabase64Encoded = DatatypeConverter.printBase64Binary(securityloadData);
			logger.trace("{} securityBase64Data :: {}", LEAP_LOG_KEY, securityloadDatabase64Encoded);
			String jwtTokenData = headerbase64Encoded + "." + payloadDatabase64Encoded + "."
					+ securityloadDatabase64Encoded;
			message.setHeader(IdentityServiceConstant.JWT_TOKEN_KEY, jwtTokenData);
		} catch (IdentityServiceException | AccountFetchException | DigestMakeException
				| FeatureHeaderInitialzerException e) {
			e.printStackTrace();
		}
	}// ..end of method createJWTTenantToken

	public void validateTenantToken(Exchange exchange) throws IdentityServiceException {
		String methodName = "validateTenantToken";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> messageMap = exchange.getIn().getHeaders();
		Object jwtTokenObj = null;
		Object accountIdObj = messageMap.get(TenantSecurityConstant.ACCOUNT_ID);
		if (accountIdObj == null)
			throw new IdentityServiceException("accountId is not specified in the request header");
		String accountId = (String) messageMap.get(TenantSecurityConstant.ACCOUNT_ID);

		Object siteIdObj = messageMap.get(TenantSecurityConstant.SITE_ID);
		if (siteIdObj == null)
			throw new IdentityServiceException("SiteId is not specified in the request header");
		String siteId = (String) messageMap.get(TenantSecurityConstant.SITE_ID);

		// getting the cookie from request
		String jwtTokenFromCookie = getJWTTokenFromCookie(exchange);
		logger.trace("{} jwtTokenFromCookie in validateTenantToken method :: {}", LEAP_LOG_KEY, jwtTokenFromCookie);
		if (jwtTokenFromCookie != null) {
			jwtTokenObj = jwtTokenFromCookie;
		} // ..end of if condition to check the jwt token value from cookie
			// is not null
		if (jwtTokenFromCookie == null) {
			// getting the jwt token from header
			jwtTokenObj = messageMap.get(IdentityServiceConstant.JWT_TOKEN_KEY);
			if (jwtTokenObj == null)
				throw new IdentityServiceException("jwt token is not available in header.");
		}
		String jwtToken = (String) jwtTokenObj;
		Map<String, Object> jwtTokenInfoMap = IdentityServiceUtil.getTenantAndAccessTokenDetailsFromJWT(jwtToken);
		JSONObject payloadJsonObj = (JSONObject) jwtTokenInfoMap.get(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY);

		Long expirationTime = payloadJsonObj.getJSONObject(IdentityServiceConstant.TENANT_SITE_INFO_KEY)
				.getLong(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME);
		String tenantToken = payloadJsonObj.getJSONObject(IdentityServiceConstant.TENANT_SITE_INFO_KEY)
				.getString(TenantSecurityConstant.TENANT_TOKEN);
		messageMap.put(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME, expirationTime);
		messageMap.put(TenantSecurityConstant.TENANT_TOKEN, tenantToken);
		logger.trace("{} accountId : {}, siteId : {}, expirationTime : {}, tenantToken : {}" + accountId, siteId,
				expirationTime, tenantToken);
		try {
			boolean isValid = validateTenantToken(accountId, siteId, expirationTime, tenantToken);
			if (!isValid)
				throw new TenantTokenValidationException("Invalid tenantToken.");
		} catch (TenantTokenValidationException e) {
			throw new IdentityServiceException(e.getMessage(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method tenantTokenValidator

	public void validateAccessToken(Exchange exchange) throws IdentityServiceException {
		String methodName = "validateAccessToken";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> messageMap = exchange.getIn().getHeaders();
		Object jwtTokenObj = null;
		try {
			Object accountIdObj = messageMap.get(LeapHeaderConstant.ACCOUNT_ID);
			if (accountIdObj == null)
				throw new IdentityServiceException("accountId is not available in header.");
			Object siteIdObj = messageMap.get(LeapHeaderConstant.SITE_KEY);
			if (siteIdObj == null)
				throw new IdentityServiceException("siteId is not available in header.");

			// getting the cookie from request
			String jwtTokenFromCookie = getJWTTokenFromCookie(exchange);
			if (jwtTokenFromCookie != null) {
				jwtTokenObj = jwtTokenFromCookie;
			} // ..end of if condition to check the jwt token value from cookie
				// is not null
			if (jwtTokenFromCookie == null) {
				jwtTokenObj = messageMap.get(IdentityServiceConstant.JWT_TOKEN_KEY);
				if (jwtTokenObj == null)
					throw new IdentityServiceException("jwt token is not available in header.");
			} // ..end of if condition
			String accountId = (String) accountIdObj;
			String siteId = (String) siteIdObj;
			AccountConfiguration accountConfig = LeapTenantSecurityUtil
					.getAccountConfigurationByExternalTenantId(accountId, siteId);
			if (accountConfig == null)
				throw new FeatureHeaderInitialzerException("No Account is configured for " + accountId);
			String jwtToken = (String) jwtTokenObj;
			logger.trace("{} inside validateAccessToken method of jwtToken :: {}", LEAP_LOG_KEY, jwtToken);
			Map<String, Object> validateHashMap = validateJWTSignatureVerification(accountConfig.getInternalTenantId(),
					accountConfig.getInternalSiteId(), jwtToken);
			JSONObject payloadJsonObj = (JSONObject) validateHashMap.get(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY);
			String accessToken = payloadJsonObj.getJSONObject(IdentityServiceConstant.PAYLOAD_AUTH_DATA_KEY)
					.getString(OAuth.OAUTH_ACCESS_TOKEN);
			messageMap.put(OAuth.OAUTH_ACCESS_TOKEN, accessToken);
			String authProvider = "";
			if (payloadJsonObj.getJSONObject(IdentityServiceConstant.PAYLOAD_AUTH_DATA_KEY)
					.has(IdentityServiceConstant.AUTH_PROVIDER_KEY)) {
				authProvider = payloadJsonObj.getJSONObject(IdentityServiceConstant.PAYLOAD_AUTH_DATA_KEY)
						.getString(IdentityServiceConstant.AUTH_PROVIDER_KEY);
			}
			logger.trace("{} authProvider :{}", LEAP_LOG_KEY, authProvider);
			if (authProvider.trim().equalsIgnoreCase(IdentityServiceConstant.OKTA_AUTH_PROVIDER)) {
				validateAccessTokenWithOkta(accessToken);
			} else if (authProvider.trim().equalsIgnoreCase(IdentityServiceConstant.PING_AUTH_PROVIDER)) {
				// validateAccessTokenWithPing(accessToken);
			} else {
				String responseXml = validateAccessTokenWithWso2(accessToken);
				validateTokenProcessBean(responseXml);
			}

		} catch (ValidationFailedException | AccountFetchException | FeatureHeaderInitialzerException e) {
			throw new IdentityServiceException(e.getMessage(), e);
		} catch (ParserException | TrustValidationException | PropertiesConfigException e) {
			throw new IdentityServiceException("unable to validate the access token from the header", e);
		} catch (DigestMakeException | SecretKeyGenException e) {
			throw new IdentityServiceException(e);
		}

	}// end of method accessTokenValidator

	/**
	 * This method is used to get the JWT Token From Cookie
	 * 
	 * @param exchange :: {@link Exchange}
	 * @return String CookieValue
	 */
	private String getJWTTokenFromCookie(Exchange exchange) {
		String cookieValue = null;
		Message message = exchange.getIn();
		Request requestObj = message.getHeader(LeapDataContextConstant.CAMEL_RESTLET_REQUEST, Request.class);
		Series<Cookie> requestCookies = requestObj.getCookies();
		for (int i = 0; i < requestCookies.size(); i++) {
			Cookie requestCookie = requestCookies.get(i);
			if (requestCookie.getName().equalsIgnoreCase(IdentityServiceConstant.REQ_COOKIE_HEADER_KEY)) {
				cookieValue = requestCookie.getValue();
			} // ..end of if condition

		} // ..end of for loop
		return cookieValue;
	}// ..end of method getJWTTokenFromCookie

	/**
	 * This method is used to validate the jwtSignature verification
	 * 
	 * @param tenantId : String tenantId
	 * @param siteId   : String siteId
	 * @param jwtToken : String JwtToken
	 * @return {@link Map} Map Data
	 * @throws DigestMakeException
	 * @throws SecretKeyGenException
	 * @throws IdentityServiceException
	 */
	private Map<String, Object> validateJWTSignatureVerification(String tenantId, String siteId, String jwtToken)
			throws DigestMakeException, SecretKeyGenException, IdentityServiceException {
		byte[] securityByteData = TenantSecurityUtil.getMD5(tenantId, siteId);
		String securityBase64Data = DatatypeConverter.printBase64Binary(securityByteData);
		String[] jwtTokenSplit = jwtToken.split("\\.");
		if (jwtToken.length() < 3) {
			throw new IdentityServiceException("jwt token length size should be three");
		}
		String headerBase64Data = jwtTokenSplit[0];
		String payloadBase64Data = jwtTokenSplit[1];
		String actualSecurityBase64Data = jwtTokenSplit[2];
		boolean secVerifyflag = TenantSecurityUtil.isValid(actualSecurityBase64Data, securityBase64Data);
		if (secVerifyflag) {
			return decodeJwtHeaderAndPayloadData(headerBase64Data, payloadBase64Data);
		} else {
			throw new IdentityServiceException(
					"jwt signature has not verified, check the signature verification data in jwt ");
		}

	}// ..end of method validateJWTSignatureVerification

	/**
	 * This method is used to decode the jwt Header and Paycode Base64Value
	 * 
	 * @param headerBase64Data
	 * @param payloadBase64Data
	 * @return {@link Map} DecodeHashMap
	 */
	private static Map<String, Object> decodeJwtHeaderAndPayloadData(String headerBase64Data,
			String payloadBase64Data) {
		Map<String, Object> decodeHashMap = new HashMap<>();
		byte[] byteHeaderDecodeData = Base64.decodeBase64(headerBase64Data);
		byte[] bytePayloadDecodeData = Base64.decodeBase64(payloadBase64Data);
		String headerDecodeData = new String(byteHeaderDecodeData);
		String payloadDecodeData = new String(bytePayloadDecodeData);
		logger.debug("{} headerDecodeData :: {} payloadDecodeData :: {}", LEAP_LOG_KEY, headerDecodeData,
				payloadDecodeData);
		decodeHashMap.put(IdentityServiceConstant.JWT_HEADER_DATA_KEY, new JSONObject(headerDecodeData));
		decodeHashMap.put(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY, new JSONObject(payloadDecodeData));
		return decodeHashMap;
	}// ..end of method decodeJwtHeaderAndPayloadData

	private boolean validateTenantToken(String accountId, String siteId, long expirationTime, String tenantToken)
			throws IdentityServiceException {
		try {
			if (expirationTime > TenantSecurityUtil.getCurrentEpoch()) {
				AccountConfiguration configuration = LeapTenantSecurityUtil
						.getAccountConfigurationByExternalTenantId(accountId, siteId);
				String tokenGen = TenantSecurityUtil.getMD5(accountId, siteId, configuration.getSecretKey(),
						expirationTime);
				boolean isValid = TenantSecurityUtil.isValid(tenantToken, tokenGen);
				return isValid;
			} else
				throw new TenantTokenValidationException("TenantToken Expired.");
		} catch (TenantTokenValidationException exc) {
			throw new IdentityServiceException(exc.getMessage(), exc);
		} catch (DigestMakeException | AccountFetchException exc) {
			throw new IdentityServiceException("unable to fetch the Account configuration for " + accountId, exc);
		}
	}// end of method validateTenantToken

	/**
	 * This method is used to get the validate value from the response XML file
	 * 
	 * @param responseXml
	 * 
	 * @param exchange    : Exchange Object
	 * @throws IdentityServiceException
	 * @throws JSONException
	 * @throws ValidationFailedException
	 * @throws ParserException
	 * @throws JsonParserException
	 */
	public void validateTokenProcessBean(String responseXml) throws ParserException, ValidationFailedException {
		String methodName = "validateTokenProcessBean";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String valid = fetchRequiredValuesFromDocument(generateDocumentFromString(responseXml));
		if (valid.equalsIgnoreCase(FALSE_KEY)) {
			throw new ValidationFailedException("Access Denied. Authentication failed");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method validate

	/**
	 * This method is used to add account details in the header.
	 * 
	 * @throws IdentityServiceException
	 * @throws PropertiesConfigException
	 */
	public void setAccountDetailsInHeader(Exchange exchange)
			throws IdentityServiceException, AccountFetchException, DigestMakeException, PropertiesConfigException {
		String methodName = "setAccountDetailsInHeader";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = null;

		Object leapDataContextInHeader = exchange.getIn().getHeader(LEAP_DATA_CONTEXT);

		if (leapDataContextInHeader instanceof String) {
			logger.trace("{} LDC is a String Instance {}", LEAP_LOG_KEY, leapDataContextInHeader.toString());
			Gson gson = new Gson();
			JsonElement jsonElement = gson.toJsonTree(leapDataContextInHeader.toString());
			leapDataContext = gson.fromJson(jsonElement, LeapDataContext.class);
			logger.trace("{} LDC is after conversion:: {}", LEAP_LOG_KEY, leapDataContext);
		} else {
			leapDataContext = exchange.getIn().getHeader(LEAP_DATA_CONTEXT, LeapDataContext.class);
		}

		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		logger.trace("{} Got Leap Service Context ..", LEAP_LOG_KEY);
		boolean canSetAccountDetailInHeader = leapServiceContext.isOnlyRuntimeServiceContext();
		logger.trace("{} canSetAccountDetailInHeader :: {}", LEAP_LOG_KEY, canSetAccountDetailInHeader);
		if (canSetAccountDetailInHeader) {
			// for pipeline execution
			setDefaultValue(exchange);

			// for loginwithCookie
			Object cookieHeader = exchange.getIn().getHeader(LeapDataContextConstant.COOKIE);
			if (cookieHeader != null
					&& cookieHeader.toString().equalsIgnoreCase(IdentityServiceConstant.RES_COOKIE_HEADER_VALUE))
				setJWTAsCookieInResponse(exchange, cookieHeader);

			Message message = exchange.getIn();
			Object isValidTenantObj = message.getHeader(IS_TENANT_TOKEN_VALID);
			if (isValidTenantObj == null) {
				logger.debug("{} isTenantTokenValidationEnabled is not available in header");
				return;
			}
			String isValidTenant = (String) message.getHeader(IS_TENANT_TOKEN_VALID);
			String serviceName = (String) message.getHeader(SERVICENAME);
			if (isValidTenant.equalsIgnoreCase(IdentityServiceConstant.TRUE_KEY)
					|| serviceName.equalsIgnoreCase(IdentityServiceConstant.LOGIN_SERVICE)
					|| serviceName.equalsIgnoreCase(IdentityServiceConstant.DEFAULT_APP_ACCESS_TOKEN_SERVICE)
					|| serviceName.equalsIgnoreCase(IdentityServiceConstant.LOGIN_WITH_COOKIE_SERVICE)) {
				List<String> skipServices = new ArrayList<>();
				getSkipServicesList(skipServices, exchange);
				if (!skipServices.isEmpty() && (skipServices.contains(serviceName))) {
					logger.trace("Skip Services list is not empty.");
					removeTenantTokenDetails(exchange);
				} else
					addTenantTokenDetails(exchange);
			} else {
				removeTenantTokenDetails(exchange);
			}
		} else {
			logger.debug("{} Cannot set account deatils in header, as it is a internal call", LEAP_LOG_KEY);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to set the cookie in camel http response for
	 * loginwithcookie service
	 * 
	 * @param exchange     :: {@link Exchange}
	 * @param cookieHeader :: Object exchange Header
	 */
	private void setJWTAsCookieInResponse(Exchange exchange, Object cookieHeader) {
		logger.debug("{} inside the setJWTAsCookieInResponse method of IdentityServiceClass");
		Message message = exchange.getIn();
		String responseBody = message.getBody(String.class);
		JSONObject responseJsonObj = new JSONObject(responseBody);
		String jwtTokenValue = responseJsonObj.getString(IdentityServiceConstant.JWT_TOKEN_KEY);
		Response response = message.getHeader(LeapDataContextConstant.CAMEL_RESTLET_RESPONSE, Response.class);
		String cookieValue = IdentityServiceConstant.JWT_TOKEN_KEY + "=" + jwtTokenValue + "; Path=/";
		message.setHeader("Set-Cookie", cookieValue);
		// CookieSetting cookieSettings = new CookieSetting(1,
		// IdentityServiceConstant.JWT_TOKEN_KEY, jwtTokenValue);
		// logger.debug("{} cookie details in setJWTAsCookieInResponse ::: " +
		// cookieSettings.getValue() + "name :: "
		// + cookieSettings.getName());
		// response.getCookieSettings().add(cookieSettings);
		message.setBody("");
		message.removeHeader(LeapDataContextConstant.COOKIE);
	}// ..end of method setJWTAsCookieInResponse

	/**
	 * This method is used to set the default value to the counter in pipeline
	 * Execution.
	 */
	private void setDefaultValue(Exchange exchange) {
		exchange.setProperty(PipelineServiceConstant.PIPE_NAME_COUNTER, 0);
	}

	/**
	 * This method is used to add tenant_token and tenant_token_expiration in the
	 * header.
	 * 
	 * @throws IdentityServiceException
	 * @throws AccountFetchException
	 */
	private void addTenantTokenDetails(Exchange exchange) throws IdentityServiceException, AccountFetchException {
		logger.debug("{} inside addTenantTokenDetails(..) from IdentityServiceBean..");
		Message message = exchange.getIn();
		LeapDataContext leapDataContext = exchange.getIn().getHeader(LEAP_DATA_CONTEXT, LeapDataContext.class);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		AccountConfiguration configuration = LeapTenantSecurityUtil.getAccountConfigurationByInternalTenantId(
				leapServiceContext.getTenant(), leapServiceContext.getSite());
		String accountId = configuration.getAccountName();
		String siteId = configuration.getInternalSiteId();
		long expiration = TenantSecurityUtil.getExpirationTime(configuration.getExpirationCount());
		logger.debug("{} account Name :{} , site :{}", LEAP_LOG_KEY, accountId, siteId);
		try {
			String tenantToken = TenantSecurityUtil.getMD5(accountId, siteId, configuration.getSecretKey(), expiration);
			long expirationTime = TenantSecurityUtil.getExpirationTime(configuration.getExpirationCount());
			String jwtTokenHeader = message.getHeader(IdentityServiceConstant.JWT_TOKEN_KEY, String.class);
			if (jwtTokenHeader == null) {
				String jwtTokenFromCookie = getJWTTokenFromCookie(exchange);
				String replacedJwtToken = replaceTenantTokenInfoInJWT(jwtTokenFromCookie, tenantToken, expirationTime);
				updateJWTTokenInRequestCookie(message, replacedJwtToken);
			}
			if (jwtTokenHeader != null && !jwtTokenHeader.isEmpty()) {
				String replacedJwtToken = replaceTenantTokenInfoInJWT(jwtTokenHeader, tenantToken, expirationTime);
				message.setHeader(IdentityServiceConstant.JWT_TOKEN_KEY, replacedJwtToken);
			} // ..end of if condition , if jwt token is not null or empty then
				// replaced the tenant token and set the JWT in header
		} catch (DigestMakeException e) {
			throw new IdentityServiceException(
					"Unable to fetch the tenant token and expiration for the given account. " + e.getMessage());
		}
	}// ..end of method addTenantTokenDetails

	/**
	 * This method is used to update the JWT token value from request
	 * 
	 * @param message
	 * 
	 * @param replacedJwtToken
	 */
	private void updateJWTTokenInRequestCookie(Message message, String replacedJwtToken) {
		Response camelResponse = message.getHeader(LeapDataContextConstant.CAMEL_RESTLET_RESPONSE, Response.class);
		CookieSetting cookieSettings = new CookieSetting(1, IdentityServiceConstant.JWT_TOKEN_KEY, replacedJwtToken);
		logger.trace("{} cookie details in updateJWTTokenInRequestCookie :::{} name :: {}  ", LEAP_LOG_KEY,
				cookieSettings.getValue(), cookieSettings.getName());
		camelResponse.getCookieSettings().remove(IdentityServiceConstant.JWT_TOKEN_KEY);
		camelResponse.getCookieSettings().add(cookieSettings);
	}// ..end of method updateJWTTokenInRequestCookie

	/**
	 * This method is used to replace the tenant token info from existing JWT and
	 * set JWT in header
	 * 
	 * @param jwtTokenHeader :: String JWT Token Value
	 * @param tenantToken    ::String
	 * @param expirationTime : long
	 * @return
	 */
	private String replaceTenantTokenInfoInJWT(String jwtTokenHeader, String tenantToken, long expirationTime) {
		Map<String, Object> encodedFormatDeatilsFromJWT = IdentityServiceUtil
				.getEncodedFormatDeatilsFromJWT(jwtTokenHeader);
		String payloadBase64DataFromJWT = (String) encodedFormatDeatilsFromJWT
				.get(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY);
		byte[] bytePayloadDecodeData = Base64.decodeBase64(payloadBase64DataFromJWT);
		String payloadDecodeData = new String(bytePayloadDecodeData);
		JSONObject payloadDecodeJsonData = new JSONObject(payloadDecodeData);
		JSONObject tenantSiteTokenInfo = payloadDecodeJsonData
				.getJSONObject(IdentityServiceConstant.TENANT_SITE_INFO_KEY);
		tenantSiteTokenInfo.put(TenantSecurityConstant.TENANT_TOKEN, tenantToken);
		tenantSiteTokenInfo.put(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME, expirationTime);
		payloadDecodeJsonData.put(IdentityServiceConstant.TENANT_SITE_INFO_KEY, tenantSiteTokenInfo);
		byte[] payloadData = payloadDecodeJsonData.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		String jwtTokenData = encodedFormatDeatilsFromJWT.get(IdentityServiceConstant.JWT_HEADER_DATA_KEY) + "."
				+ payloadDatabase64Encoded + "."
				+ encodedFormatDeatilsFromJWT.get(IdentityServiceConstant.JWT_SECURITY_DATA_KEY);
		return jwtTokenData;

	}// ..end of method replaceTenantTokenInfoInJWT

	/**
	 * This method is used to add tenant_token and tenant_token_expiration in the
	 * header.
	 * 
	 * @throws IdentityServiceException
	 * @throws AccountFetchException
	 */
	private void addTenantTokenDetailsForLoginService(Message message)
			throws IdentityServiceException, AccountFetchException {
		logger.debug("{} inside addTenantTokenDetailsForLoginService(..) from IdentityServiceBean..", LEAP_LOG_KEY);
		String domainValue = null;
		AccountConfiguration configuration = null;
		String messageBody = message.getBody(String.class);
		JSONObject requestBody = new JSONObject(messageBody);
		logger.trace("{} requestBody for login service :: " + requestBody);
		if (requestBody.has(IdentityServiceConstant.DOMAIN)) {
			domainValue = requestBody.getString(IdentityServiceConstant.DOMAIN);
		}
		if (requestBody.has("credentials")) {
			JSONObject credentialsJsonObj = requestBody.getJSONObject("credentials");
			domainValue = credentialsJsonObj.getString(IdentityServiceConstant.DOMAIN);
		}
		logger.trace("{} domainValue for login service ::{} ", LEAP_LOG_KEY, domainValue);
		if (domainValue != null) {
			configuration = LeapTenantSecurityUtil.getAccountConfigurationByDomin(domainValue);
		} else {
			configuration = LeapTenantSecurityUtil.getAccountConfigurationByExternalTenantId(
					message.getHeader(LeapDataContextConstant.ACCOUNTID, String.class),
					message.getHeader(LeapDataContextConstant.SITEID, String.class));
		}
		message.setHeader("domianValueFromRequest", domainValue);
		String accountId = configuration.getAccountName();
		String siteId = configuration.getInternalSiteId();
		long expiration = TenantSecurityUtil.getExpirationTime(configuration.getExpirationCount());
		logger.trace("{} account Name :{} , site :{} ", LEAP_LOG_KEY, accountId, siteId);
		try {
			String tenantToken = TenantSecurityUtil.getMD5(accountId, siteId, configuration.getSecretKey(), expiration);
			message.setHeader(TenantSecurityConstant.TENANT_TOKEN_LOGIN, tenantToken);
			long expirationTime = TenantSecurityUtil.getExpirationTime(configuration.getExpirationCount());
			message.setHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME_LOGIN, expirationTime);
		} catch (DigestMakeException e) {
			throw new IdentityServiceException(
					"Unable to fetch the tenant token and expiration for the given account. " + e.getMessage());
		}
	}// ..end of method addTenantTokenDetailsForLoginService

	/**
	 * This method is used to remove tenant_token and tenant_token_expiration in the
	 * header if available.
	 * 
	 * @throws IdentityServiceException
	 */
	public void removeTenantTokenDetails(Exchange exchange) throws AccountFetchException {
		logger.debug("{} inside removeTenantTokenDetails(..) from IdentityServiceBean..");
		String domainValue = null;
		Message message = exchange.getIn();
		LeapDataContext leapDataContext = message.getHeader(LEAP_DATA_CONTEXT, LeapDataContext.class);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		AccountConfiguration configuration = LeapTenantSecurityUtil.getAccountConfigurationByInternalTenantId(
				leapServiceContext.getTenant(), leapServiceContext.getSite());
		String accountId = configuration.getAccountName();
		String siteId = configuration.getInternalSiteId();
		domainValue = (String) message.getHeader("domianValueFromRequest");
		logger.trace("{} domainValue for login service :: " + domainValue);
		if (domainValue != null) {
			configuration = LeapTenantSecurityUtil.getAccountConfigurationByDomin(domainValue);
		} else {
			configuration = LeapTenantSecurityUtil.getAccountConfigurationByExternalTenantId(accountId, siteId);
		}
		accountId = configuration.getAccountName();
		siteId = configuration.getInternalSiteId();
		message.removeHeader(TenantSecurityConstant.TENANT_TOKEN);
		message.removeHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME);
		message.setHeader(LeapDataContextConstant.ACCOUNTID, accountId);
		message.setHeader(LeapDataContextConstant.SITEID, siteId);
	}// end of method getSkipServicesList

	private void getSkipServicesList(List<String> skipServices, Exchange exchange) throws IdentityServiceException {
		logger.debug("{} inside getSkipServicesList(..) from IdentityServiceBean..");
		try {
			JSONArray servicesToSkip = new JSONArray(LeapConfigUtil.getGlobalPropertyValue(TENANT_TOKEN_SKIP_SERVICES,LeapDefaultConstants.DEFAULT_TENANT_TOKEN_SKIP_SERVICES));
			for (int i = 0; i < servicesToSkip.length(); i++) {
				String service = servicesToSkip.getString(i);
				String[] serviceDetail = service.trim().split("-");
				skipServices.add(serviceDetail[2]);
			}
		} catch (Exception e) {
			throw new IdentityServiceException("Unable to fetch the skip services name. " + e.getMessage());
		}
	}// end of method getSkipServicesList

	private Document generateDocumentFromString(String xmlInput) throws ParserException {
		logger.debug("{} . generateDoocumentFromString method of IdentityService" + xmlInput);
		Document xmlDocument = null;
		if (xmlInput != null) {
			this.builderFactory.setNamespaceAware(true);
			try {
				if (this.builder == null) {
					this.builder = this.builderFactory.newDocumentBuilder();
				}
				InputSource insrc = new InputSource(new StringReader(xmlInput));
				xmlDocument = this.builder.parse(insrc);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new ParserException("Unable to Build the document object from the xml string ", e);
			}
		}
		return xmlDocument;
	}// end of method generateDocumentFromString

	private String fetchRequiredValuesFromDocument(Document xmlDoc) throws ParserException, ValidationFailedException {
		logger.debug("{} . generateXpathExpression method of IdentityService");
		try {
			String valid = xmlDoc.getElementsByTagName(IdentityServiceConstant.XML_VALID_TAG).item(0).getTextContent();
			return valid;
		} catch (Exception e) {
			throw new ValidationFailedException("Access Denied. Authentication failed - System error occurred", e);
		}
	}// ..end of the method fetchRequiredValuesFromDocument

	private String validateAccessToken(String accessToken)
			throws IdentityServiceException, TrustValidationException, PropertiesConfigException {
		logger.debug("{} . validateAccessToken method of IdentityService");
		try {
			String outhValidatorURL = LeapConfigUtil.getGlobalPropertyValue(OAUTH_VALIDATOR_SERVIE_URL,LeapDefaultConstants.DEFAULT_OAUTH_VALIDATOR_SERVIE_URL);
			if (this.soapConnFactory == null) {
				this.soapConnFactory = SOAPConnectionFactory.newInstance();
			}
			if (msgFct == null) {
				this.msgFct = MessageFactory.newInstance();
			}
			SOAPMessage message = this.msgFct.createMessage();
			SOAPPart mySPart = message.getSOAPPart();
			SOAPEnvelope myEnvp = mySPart.getEnvelope();
			Name xsdName = myEnvp.createName(XSD_NAMESPACE1);
			Name xsd1Name = myEnvp.createName(XSD_NAMESPACE2);
			myEnvp.addAttribute(xsdName, "http://org.apache.axis2/xsd");
			myEnvp.addAttribute(xsd1Name, "http://dto.oauth2.identity.carbon.wso2.org/xsd");
			SOAPBody body = myEnvp.getBody();
			Name bodyName = myEnvp.createName("xsd:validate");
			SOAPBodyElement gltp = body.addBodyElement(bodyName);
			SOAPElement myxsdvalidationReqDTO = gltp.addChildElement(VALIDATION_REQUEST_DTO, "xsd");
			SOAPElement myxsd1accessToken = myxsdvalidationReqDTO.addChildElement(ACCESS_TOKEN, "xsd");
			SOAPElement myxsd1tokenType = myxsd1accessToken.addChildElement(TOKEN_TYPE, "xsd1");
			myxsd1tokenType.addTextNode(TOKEN_TYPE_VALUE);
			SOAPElement myxsd1identifier = myxsd1accessToken.addChildElement(IDENTIFIER, "xsd1");
			myxsd1identifier.addTextNode(accessToken);
			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader(AUTHERIZATION,
					"Basic " + org.apache.commons.codec.binary.Base64.encodeBase64String("admin:admin".getBytes()));
			message.saveChanges();
			if (this.soapCon == null) {
				this.soapCon = this.soapConnFactory.createConnection();
			}
			message.writeTo(outStream);
			outStream.reset();
			SOAPMessage resp = this.soapCon.call(message, outhValidatorURL);
			resp.writeTo(outStream);
			String responseStr = new String(outStream.toByteArray());
			return responseStr;
		} catch (IOException | UnsupportedOperationException | SOAPException e) {
			throw new IdentityServiceException(
					"unable to generate the response xml of the validate token " + e.getMessage(), e);
		}
	}// end of method validateAccessToken

	private String validateAccessTokenWithWso2(String accessToken)
			throws IdentityServiceException, TrustValidationException, PropertiesConfigException {
		logger.debug("{} . validateAccessTokenWithWso2 method of IdentityService");
		try {
			String outhValidatorURL = LeapConfigUtil.getGlobalPropertyValue(OAUTH_VALIDATOR_SERVIE_URL,LeapDefaultConstants.DEFAULT_OAUTH_VALIDATOR_SERVIE_URL);
			if (this.soapConnFactory == null) {
				this.soapConnFactory = SOAPConnectionFactory.newInstance();
			}
			if (msgFct == null) {
				this.msgFct = MessageFactory.newInstance();
			}
			SOAPMessage message = this.msgFct.createMessage();
			SOAPPart mySPart = message.getSOAPPart();
			SOAPEnvelope myEnvp = mySPart.getEnvelope();
			Name xsdName = myEnvp.createName(XSD_NAMESPACE1);
			Name xsd1Name = myEnvp.createName(XSD_NAMESPACE2);
			myEnvp.addAttribute(xsdName, "http://org.apache.axis2/xsd");
			myEnvp.addAttribute(xsd1Name, "http://dto.oauth2.identity.carbon.wso2.org/xsd");
			SOAPBody body = myEnvp.getBody();
			Name bodyName = myEnvp.createName("xsd:validate");
			SOAPBodyElement gltp = body.addBodyElement(bodyName);
			SOAPElement myxsdvalidationReqDTO = gltp.addChildElement(VALIDATION_REQUEST_DTO, "xsd");
			SOAPElement myxsd1accessToken = myxsdvalidationReqDTO.addChildElement(ACCESS_TOKEN, "xsd");
			SOAPElement myxsd1tokenType = myxsd1accessToken.addChildElement(TOKEN_TYPE, "xsd1");
			myxsd1tokenType.addTextNode(TOKEN_TYPE_VALUE);
			SOAPElement myxsd1identifier = myxsd1accessToken.addChildElement(IDENTIFIER, "xsd1");
			myxsd1identifier.addTextNode(accessToken);
			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader(AUTHERIZATION,
					"Basic " + org.apache.commons.codec.binary.Base64.encodeBase64String("admin:admin".getBytes()));
			message.saveChanges();
			if (this.soapCon == null) {
				this.soapCon = this.soapConnFactory.createConnection();
			}
			message.writeTo(outStream);
			outStream.reset();
			SOAPMessage resp = this.soapCon.call(message, outhValidatorURL);
			resp.writeTo(outStream);
			String responseStr = new String(outStream.toByteArray());
			return responseStr;
		} catch (IOException | UnsupportedOperationException | SOAPException e) {
			throw new IdentityServiceException(
					"unable to generate the response xml of the validate token " + e.getMessage(), e);
		}
	}// end of method validateAccessToken

	private void validateAccessTokenWithOkta(String accessToken) throws ValidationFailedException {
		HttpURLConnection connection = null;
		URL url;
		String oktaResponse = null;
		try {
			String urlParameters = "token_type_hint=access_token&token=" + accessToken;
			url = new URL("https://dev-804590.okta.com/oauth2/default/v1/introspect");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(IdentityServiceConstant.HTTP_POST_MTD);
			connection.setRequestProperty(IdentityServiceConstant.HTTP_CONTENT_TYPE_KEY,
					IdentityServiceConstant.HTTP_CONTENT_TYPE_URL_ENCODED);
			connection.setRequestProperty(IdentityServiceConstant.HTTP_AUTHZ_KEY,
					IdentityServiceConstant.HTTP_BASIC_AUTHZ + " "
							+ IdentityServiceConstant.OKTA_ENCODED_CLIENT_CREDENTIALS);
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				wr.writeBytes(urlParameters);
				wr.flush();
			}
			oktaResponse = IOUtils.toString(connection.getInputStream(), "UTF-8");
			logger.trace("{} validateAccessTokenWithOkta response :{}", LEAP_LOG_KEY, oktaResponse);
			if (oktaResponse != null && !oktaResponse.isEmpty()) {
				if (!new JSONObject(oktaResponse).getBoolean("active")) {
					throw new ValidationFailedException("Access Denied. Authentication failed");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ValidationFailedException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * trust store path. this must contains server's certificate or Server's CA
	 * chain
	 * 
	 * @throws TrustValidationException
	 */
	private static void initializeClientCall() throws TrustValidationException {
		disablesslTrustValidation();
		System.setProperty("java.net.useSystemProxies", "true");
	}// ..end of the method initializeClientCall

	/**
	 * This method is used to disable the java trust store certificate
	 * 
	 * @throws TrustValidationException
	 * @throws TrustStoreCertificateException
	 */
	private static void disablesslTrustValidation() throws TrustValidationException {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {

				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException |

				KeyManagementException e) {
			throw new TrustValidationException("Java Certificate Exception : " + e.getMessage());
		}
	}

}
