package com.attunedlabs.leap.identityservice;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.util.Series;

import static com.attunedlabs.LeapCoreTestConstant.*;
import com.attunedlabs.LeapCoreTestUtils;
import com.attunedlabs.config.util.PropertiesConfigException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.header.initializer.JsonParserException;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;
import com.attunedlabs.security.utils.TenantSecurityUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class IdentityServiceBeanTest{

	private Exchange exchange;
	private Message message;
	private IdentityServiceBean identityServiceBean;

	private String responseXMLTrue = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><beans xmlns:ax2395=\"http://dto.oauth2.identity.carbon.wso2.org/xsd\"><body><ax2395:valid>true</ax2395:valid></body></beans>";
	private String responseXMLFalse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><beans xmlns:ax2395=\"http://dto.oauth2.identity.carbon.wso2.org/xsd\"><body><ax2395:valid>false</ax2395:valid></body></beans>";

	@Before
	public void setUp() {
		System.setProperty(PROFILE_ID, LOCAL);
		if (exchange == null)
			exchange = LeapCoreTestUtils.createExchange();
		if (identityServiceBean == null)
			identityServiceBean = new IdentityServiceBean();
		message = exchange.getIn();
	}

	/**
	 * 
	 * @throws JSONException
	 * @throws IdentityServiceException
	 * @throws JsonParserException
	 */
	@Test
	public void testIsAuthenticated() throws JSONException, IdentityServiceException, JsonParserException {

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange header should not be null ::", headers);
		Assert.assertEquals("exchange header should be empty like zero (0) ::", 0, headers.size());

		identityServiceBean.isAuthenticated(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null :", headers);
		Assert.assertNotEquals("After Calling isAuthenticated method header should greater then zero (0) ::", 0,
				headers.size());
		Assert.assertEquals("Exchange Header isAccessTokenValidationEnable Should be false ::", FALSE,
				headers.get(ACCESS_TOKEN_VALIDATION));
		Assert.assertEquals("exchnage Header isTenantTokenValidationEnabled should be false ::", FALSE,
				headers.get(TENANT_TOKEN_VALIDATION));
	}

	/**
	 * this method validate JWttenantToken with valid token.
	 * 
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 * @throws IdentityServiceException
	 */
	@Test
	public void testValidateTenantTokenWithValidToken()
			throws AccountFetchException, DigestMakeException, IdentityServiceException {
		message.setHeader(ACCOUNTID, TEST_ACCOUNT);
		message.setHeader(SITEID, TEST_SITE);
		message.setHeader(CAMEL_RESTLET_REQUEST, setTenatToken());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange Header is not contain tenantToken data then should be null ::",
				headers.get(TENANT_TOKEN));

		identityServiceBean.validateTenantToken(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNotNull("Exchange Header contain tenantToken data then should not be null ::",
				headers.get(TENANT_TOKEN));
		Assert.assertEquals("Exchange header tenantToken should be same as expected data ::", tokenGen(),
				headers.get(TENANT_TOKEN));
		Assert.assertEquals("Exchange Header TenatExpirationData Should be same as expected data :",
				TENANT_TOKEN_EXPIRATION_DATA, headers.get(TENANT_TOKEN_EXPIRATION));

	}

	/**
	 * this method validate JWttenantToken without valid token.
	 * 
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 * @throws IdentityServiceException
	 */
	@Test(expected = JSONException.class)
	public void testValidateTenantTokenWithInvalidToken()
			throws AccountFetchException, DigestMakeException, IdentityServiceException {
		message.setHeader(ACCOUNTID, TEST_TENANT);
		message.setHeader(SITEID, TEST_SITE);
		message.setHeader(CAMEL_RESTLET_REQUEST, setInvalidTenatToken());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange Header is not contain tenantToken data then should be null ::",
				headers.get(TENANT_TOKEN));

		identityServiceBean.validateTenantToken(exchange);
	}

	/**
	 * this method validate access token with valid token.
	 * 
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 * @throws IdentityServiceException
	 */
	@Test
	public void testValidateAccessTokenWithValidToken()
			throws AccountFetchException, DigestMakeException, IdentityServiceException {
		message.setHeader(ACCOUNTID, TEST_ACCOUNT);
		message.setHeader(SITEID, TEST_SITE);
		message.setHeader(CAMEL_RESTLET_REQUEST, setAccessToken());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange Header is not contain tenantToken data then should be null ::",
				headers.get(TENANT_TOKEN));

		identityServiceBean.validateAccessToken(exchange);

		headers = message.getHeaders();

		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNotNull("Exchange Header contain tenantToken data then should not be null ::",
				headers.get(ACCESS_TOKEN));
		Assert.assertEquals("Exchange header tenantToken should be same as expected data ::", ACCESS_TOKEN_DATA,
				headers.get(ACCESS_TOKEN));
	}

	/**
	 * this method validate access token with invalid token.
	 */
	@Test(expected = IdentityServiceException.class)
	public void testValidateAccessTokenWithInvalidToken()
			throws AccountFetchException, DigestMakeException, IdentityServiceException {
		message.setHeader(ACCOUNTID, TEST_ACCOUNT);
		message.setHeader(SITEID, TEST_SITE);
		message.setHeader(CAMEL_RESTLET_REQUEST, setInvalidAccessToken());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange Header is not contain tenantToken data then should be null ::",
				headers.get(TENANT_TOKEN));

		identityServiceBean.validateAccessToken(exchange);
	}

	/**
	 * This method is used to get the validate value from the response XML file with
	 * value is true.
	 * 
	 * @throws ParserException
	 * @throws ValidationFailedException
	 */
	@Test
	public void testValidateTokenProcessBeanIfTrue() throws ParserException, ValidationFailedException {
		identityServiceBean.validateTokenProcessBean(responseXMLTrue);
	}

	/**
	 * This method is used to get the validate value from the response XML file with
	 * value is false.
	 * 
	 * @throws ParserException
	 * @throws ValidationFailedException
	 */
	@Test(expected = ValidationFailedException.class)
	public void testValidateTokenProcessBeanIfFalse() throws ParserException, ValidationFailedException {
		identityServiceBean.validateTokenProcessBean(responseXMLFalse);
	}

	/**
	 * This method is used to add account details in the header if
	 * isTenantTokenValidationEnabled is false .
	 * 
	 * @throws IdentityServiceException
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 * @throws PropertiesConfigException
	 */
	@Test
	public void testSetAccountDetailsInHeader()
			throws IdentityServiceException, AccountFetchException, DigestMakeException, PropertiesConfigException {
		addAccountConfigurationByInternalTenantId();
		LeapDataContext context = new LeapDataContext();
		LeapCoreTestUtils.setServiceContext(context);
		message.setHeader(LEAP_DATA_CONTEXT, context);
		message.setHeader(TENANT_TOKEN_VALIDATION, FALSE);
		message.setHeader(SERVICENAME, TEST_SERVICE);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNull("Exchange header not have accountId then should be null ::", headers.get(ACCOUNTID));

		identityServiceBean.setAccountDetailsInHeader(exchange);

		headers = message.getHeaders();
		Assert.assertNotNull("Exchange header have account then should not be null ::", headers.get(ACCOUNTID));

	}

	/**
	 * This method is used to add account details in the header if
	 * isTenantTokenValidationEnabled is true
	 * 
	 * @throws IdentityServiceException
	 * @throws AccountFetchException
	 * @throws DigestMakeException
	 * @throws PropertiesConfigException
	 */
	@Test
	public void testSetAccountDetailsInHeaderWithTenantValidationTrue()
			throws IdentityServiceException, AccountFetchException, DigestMakeException, PropertiesConfigException {
		addAccountConfigurationByInternalTenantId();
		LeapDataContext context = new LeapDataContext();
		LeapCoreTestUtils.setServiceContext(context);
		message.setHeader(LEAP_DATA_CONTEXT, context);
		message.setHeader(TENANT_TOKEN_VALIDATION, TRUE);
		message.setHeader(ACCESS_TOKEN_VALIDATION, FALSE);
		message.setHeader(SERVICENAME, TEST_SERVICE);
		message.setHeader(CAMEL_RESTLET_REQUEST, setTenatToken());
		message.setHeader(CAMEL_RESTLET_RESPONSE, getResponse());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("exchange header should not be null ::", headers);

		Response responseData = (Response) headers.get(CAMEL_RESTLET_RESPONSE);
		Assert.assertNotNull("responseData should not be null ::", responseData);
		Assert.assertEquals("CookieSeeting of response data should be 1 :: ", 1,
				responseData.getCookieSettings().size());
		Assert.assertEquals("GetCookieSetting data should be same as expecetd :: ", "asdfgfdsa",
				responseData.getCookieSettings().get(0).getValue());

		identityServiceBean.setAccountDetailsInHeader(exchange);

		headers = message.getHeaders();

		responseData = (Response) headers.get(CAMEL_RESTLET_RESPONSE);
		Assert.assertNotNull("responseData should not be null ::", responseData);
		Assert.assertEquals("CookieSeeting of response data should be 2 :: ", 2,
				responseData.getCookieSettings().size());
		Assert.assertNotEquals("GetCookieSetting data should not be same as expecetd :: ", "asdfgfdsa",
				responseData.getCookieSettings().get(1).getValue());

	}

	/**
	 * This method is used to remove tenant_token and tenant_token_expiration in the
	 * header if available.
	 * 
	 * @throws AccountFetchException
	 */
	@Test
	public void testRemoveTenantTokenDetails() throws AccountFetchException {
		LeapDataContext context = new LeapDataContext();
		LeapCoreTestUtils.setServiceContext(context);
		addAccountConfigurationByInternalTenantId();

		message.setHeader(LEAP_DATA_CONTEXT, context);

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header should not be null ::", headers);
		Assert.assertNull("Exchange Header not have accountId then should be null ::", headers.get(ACCOUNTID));
		Assert.assertNull("Exchange Header not have siteId then should be null ::", headers.get(SITEID));

		identityServiceBean.removeTenantTokenDetails(exchange);
	}

	private Request setTenatToken() throws AccountFetchException, DigestMakeException {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN, createJWTTenantToken());
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private Request setInvalidTenatToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN, "eyJhbGciOiJIU.eyJ0ZW5hbnRTaXRlSW5mbyG9rZW4iOiIy5NGM5YzcyZCJ9fQ");
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private Request setAccessToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN,
				"eyJkZW1vIjoiVGVzdERlbW8iLCJhbGciOiJSUzI1NiJ9.eyJhdXRoRGF0YSI6eyJhY2Nlc3NfdG9rZW4iOiJUZXN0aW5nVG9rZW4iLCJhdXRoUHJvdmlkZXIiOiJwaW5nIn19.Xz/PhzM/V3DRZgj2etiNGQ==");
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private Response getResponse() throws AccountFetchException, DigestMakeException {

		CookieSetting cookieSetting = new CookieSetting(LEAP_AUTH_TOKEN, "asdfgfdsa");
		Series<CookieSetting> series = new Series<CookieSetting>(CookieSetting.class);
		series.add(cookieSetting);
		Response response = new Response(setTenatToken());
		response.setCookieSettings(series);
		return response;
	}

	private Request setInvalidAccessToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN,
				"eyJkZW1vIjoiVGVz1NiJ9.eyJhdXRoRGF0YSI6eyJhY2Nlc3NfdG9rZW4iOiJUZXN0aW5nVG.Xz/PhztiNGQ==");
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private String tokenGen() throws AccountFetchException, DigestMakeException {
		AccountConfiguration configuration = LeapTenantSecurityUtil
				.getAccountConfigurationByExternalTenantId(TEST_ACCOUNT, TEST_SITE);
		String tokenGen = TenantSecurityUtil.getMD5(TEST_ACCOUNT, TEST_SITE, configuration.getSecretKey(),
				TENANT_TOKEN_EXPIRATION_DATA);
		return tokenGen;
	}

	private String createJWTTenantToken() throws AccountFetchException, DigestMakeException {
		JSONObject jwtHeaderJson = new JSONObject();
		JSONObject payloadDataJson = new JSONObject();
		JSONObject tenantSiteDataJson = new JSONObject();
		String jwtTokenData = null;
		AccountConfiguration accountConfig = LeapTenantSecurityUtil
				.getAccountConfigurationByExternalTenantId(TEST_ACCOUNT, TEST_SITE);
		jwtHeaderJson.put(IdentityServiceConstant.JWT_TYPE_KEY, IdentityServiceConstant.JWT_TYPE_VALUE);
		jwtHeaderJson.put(IdentityServiceConstant.JWT_TYPE_ALGO, IdentityServiceConstant.JWT_TYPE_ALGO_VALUE);
		byte[] headerData = jwtHeaderJson.toString().getBytes();
		String headerbase64Encoded = DatatypeConverter.printBase64Binary(headerData);
		payloadDataJson.put(IdentityServiceConstant.TENANT_SITE_INFO_KEY,
				tenantSiteDataJson.put(LeapDataContextConstant.TENANT_TOKEN, tokenGen())
						.put(LeapDataContextConstant.TENANT_TOKEN_EXPIRATION, TENANT_TOKEN_EXPIRATION_DATA));
		byte[] payloadData = payloadDataJson.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		byte[] securityloadData = TenantSecurityUtil.getMD5(accountConfig.getInternalTenantId(),
				accountConfig.getInternalSiteId());
		String securityloadDatabase64Encoded = DatatypeConverter.printBase64Binary(securityloadData);
		jwtTokenData = headerbase64Encoded + "." + payloadDatabase64Encoded + "." + securityloadDatabase64Encoded;

		return jwtTokenData;
	}

	private AccountConfiguration addAccountConfigurationForInternal() {
		AccountConfiguration accountConfiguration = new AccountConfiguration("ALL", "all", "all", null, null,
				"<daER<@%$5", 3000);
		return accountConfiguration;
	}

	private void addAccountConfigurationByInternalTenantId() {
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, AccountConfiguration> map = hazelcastInstance.getMap("internalAccountConfig".trim());
		map.put(TEST_TENANT + "-" + TEST_SITE, addAccountConfigurationForInternal());

	}
}
