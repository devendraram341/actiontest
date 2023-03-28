package com.attunedlabs.baseroute;

import static com.attunedlabs.LeapCoreTestConstant.*;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Message;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Cookie;
import org.restlet.util.Series;

import com.attunedlabs.baseroute.util.BaseRouteTestUtil;
import com.attunedlabs.baseroute.util.PropertiesForTesting;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.identityservice.IdentityServiceConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.util.LeapTenantSecurityUtil;
import com.attunedlabs.security.utils.TenantSecurityUtil;

public class TenantTokenValidationRouteTest extends BaseRouteTestUtil {

	/**
	 * This route is used for Identity service with isAccessTokenValidation false
	 * and isTenantTokenValidation true and valid Tenant Token
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIdentityServiceWithTenantTokenTrue() throws Exception {
		RouteDefinition route = context.getRouteDefinition(TENANT_TOKEN_VALIDATION_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(ACCESS_TOKEN_VALIDATION, constant(FALSE))
						.setHeader(TENANT_TOKEN_VALIDATION_ROUTE, constant(TRUE))
						.setHeader(CAMEL_RESTLET_REQUEST, constant(setTenantToken()));
				weaveAddLast().to(MOCK_FINISH);

			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(TENANT_TOKEN_VALIDATION_ROUTE), null,
				setHeader());
		assertMockEndpointsSatisfied();

		Message message = mockEndpoint.getReceivedExchanges().get(0).getIn();
		Assert.assertNotNull("Exchange message should not be null ::", message);
		Assert.assertNull("Exchange Mesaage Body Shoud be null ::", message.getBody());
		Assert.assertNotNull("Exchange Header Should not be null ::", message.getHeaders());

		Map<String, Object> headers = message.getHeaders();
		Assert.assertNotNull("Exchange Header Should not be null ::", headers);
		Assert.assertFalse("Header Value should be false  ::",
				Boolean.valueOf(headers.get(ACCESS_TOKEN_VALIDATION).toString()));
		Assert.assertTrue("Header Value should be true  ::",
				Boolean.valueOf(headers.get(TENANT_TOKEN_VALIDATION_ROUTE).toString()));
		Assert.assertEquals(TENANT_TOKEN_EXPIRATION_DATA, headers.get(TENANT_TOKEN_EXPIRATION));
	}

	/**
	 * This route is used for Identity service with isAccessTokenValidation false
	 * and isTenantTokenValidation true and invalid Tenant Token
	 * 
	 * @throws Exception
	 */
	@Test(expected = CamelExecutionException.class)
	public void testIdentityServiceWithTenantTokenTrueAndInvalidToken() throws Exception {
		RouteDefinition route = context.getRouteDefinition(TENANT_TOKEN_VALIDATION_ROUTE);
		route.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				weaveAddFirst().setHeader(ACCESS_TOKEN_VALIDATION, constant(FALSE))
						.setHeader(TENANT_TOKEN_VALIDATION_ROUTE, constant(TRUE))
						.setHeader(CAMEL_RESTLET_REQUEST, constant(setInvalidTenantToken()));
				weaveAddLast().to(MOCK_FINISH);

			}
		});

		MockEndpoint mockEndpoint = getMockEndpoint(MOCK_FINISH);
		mockEndpoint.expectedMessageCount(1);
		template.sendBodyAndHeaders(PropertiesForTesting.getInstance().getValue(TENANT_TOKEN_VALIDATION_ROUTE), null,
				setHeader());
		assertMockEndpointsSatisfied();
	}

	private Request setTenantToken() throws AccountFetchException, DigestMakeException {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN, createJWTTenantToken());
		Series<Cookie> series = new Series<Cookie>(Cookie.class);
		series.add(cookie);
		Request request = new Request();
		request.setCookies(series);
		return request;
	}

	private Request setInvalidTenantToken() {
		Cookie cookie = new Cookie(LEAP_AUTH_TOKEN, "eyJhbGciOiJIU.eyJ0ZW5hbnRTaXRlSW5mbyG9rZW4iOiIy5NGM5YzcyZCJ9fQ");
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
}
