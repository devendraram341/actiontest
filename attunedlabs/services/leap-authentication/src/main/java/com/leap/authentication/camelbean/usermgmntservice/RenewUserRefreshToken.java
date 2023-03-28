package com.leap.authentication.camelbean.usermgmntservice;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.identityservice.IdentityServiceConstant;
import com.attunedlabs.leap.identityservice.IdentityServiceUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.TokenRenewalException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class RenewUserRefreshToken extends AbstractMetaModelBean {

	static final String POST_METHOD = "post";
	static final String REFRESH_TOKEN = "refresh_token";

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		logger.debug("Inside renewRefreshToken");
		Object jwtTokenValue = null;
		String jwtTokenFromCookie = AuthNUtil.getJWTTokenFromCookie(exchange);
		if (jwtTokenFromCookie != null) {
			jwtTokenValue = jwtTokenFromCookie;
		}
		if (jwtTokenFromCookie == null) {
			jwtTokenValue = exchange.getIn().getHeader(AuthNConstants.JWT_TOKEN);
			if (jwtTokenValue == null) {
				throw new TokenRenewalException("jwt_token Key Not Present in Header", new Exception(),
						"jwt_token Key Not Present in request header", AuthNConstants.BAD_REQ_CODE);
			}
		}
		String jwtToken = (String) jwtTokenValue;
		Map<String, Object> encodedFormatDeatilsFromJWT = IdentityServiceUtil.getEncodedFormatDeatilsFromJWT(jwtToken);
		Map<String, Object> decodeHashMap = AuthNUtil.decodeJwtHeaderAndPayloadData(jwtToken);
		JSONObject payloadJsonObj = (JSONObject) decodeHashMap.get("payloadData");
		String refresh_token = doPostRenewRefreshToken(payloadJsonObj, exchange);
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		Map<String, Object> mapReturned = null;
		mapReturned = service.renewUserRefreshToken(refresh_token);
		if (mapReturned.isEmpty()) {
			throw new TokenRenewalException(
					"Unable to renew value for 'refresh_token' / either invalid token passed or refresh toen expired ",
					null,
					"Unable to renew value for 'refresh_token' / either invalid token passed or refresh toen expired",
					AuthNConstants.INT_SRVR_CODE);
		}
		JSONObject authDataJsonObj = payloadJsonObj.getJSONObject("authData");
		authDataJsonObj.put(OAuth.OAUTH_ACCESS_TOKEN, mapReturned.get("access_token"))
				.put(OAuth.OAUTH_REFRESH_TOKEN, mapReturned.get(REFRESH_TOKEN))
				.put(OAuth.OAUTH_EXPIRES_IN, mapReturned.get(AuthNConstants.EXPIERS_IN));
		payloadJsonObj.put("payloadData", authDataJsonObj);
		byte[] payloadData = payloadJsonObj.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		String jwtTokenData = encodedFormatDeatilsFromJWT.get(IdentityServiceConstant.JWT_HEADER_DATA_KEY) + "."
				+ payloadDatabase64Encoded + "."
				+ encodedFormatDeatilsFromJWT.get(IdentityServiceConstant.JWT_SECURITY_DATA_KEY);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(new JSONObject().put(AuthNConstants.JWT_TOKEN, jwtTokenData));
		// exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE,
		// AuthNConstants.APP_JSON);
		// exchange.getIn().setBody(res.toString());

	}

	/**
	 * 
	 * @param payloadJsonObj
	 * @param exchange
	 * @return
	 * @throws TokenRenewalException
	 */
	private String doPostRenewRefreshToken(JSONObject payloadJsonObj, Exchange exchange) throws Exception {
		String refresh_token;
		// getting the cookie from request
		try {
			refresh_token = payloadJsonObj.getJSONObject("authData").getString(OAuth.OAUTH_REFRESH_TOKEN);
			if (AuthNUtil.isEmpty(REFRESH_TOKEN)) {
				throw new TokenRenewalException("Expected value for 'refresh_token' key ", null,
						"Expected value for 'refresh_token' key", AuthNConstants.BAD_REQ_CODE);
			}
		} catch (Exception e) {
			if (e instanceof JSONException) {
				throw new TokenRenewalException("Expected value for 'refresh_token' key ", e,
						"Expected value for 'refresh_token' key" + e.getMessage() + " " + e.getCause(),
						AuthNConstants.BAD_REQ_CODE);
			} else {
				throw new TokenRenewalException("Expected value for 'refresh_token' key ", e,
						"Expected value for 'refresh_token' key ", AuthNConstants.BAD_REQ_CODE);
			}
		}
		return refresh_token;

	}

}
