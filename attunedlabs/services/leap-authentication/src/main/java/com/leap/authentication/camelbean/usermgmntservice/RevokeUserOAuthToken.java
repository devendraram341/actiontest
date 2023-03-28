package com.leap.authentication.camelbean.usermgmntservice;

import java.util.Map;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.InvalidTokenRevokeRequestException;
import com.leap.authentication.exception.TokenRevokeException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class RevokeUserOAuthToken extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		String accessToken;
		String jwtToken;
//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject jsonObject;
		if (!AuthNUtil.isEmpty(inBody)) {
			try {
				jsonObject = new JSONObject(inBody);
			} catch (JSONException e) {
				throw new InvalidTokenRevokeRequestException("Invalid json format requested ", e,
						"Invalid json format requested " + e.getMessage(), AuthNConstants.BAD_REQ_CODE);
			}
			if (jsonObject.has(AuthNConstants.JWT_TOKEN)) {
				String jwtTokenFromBody = jsonObject.getString(AuthNConstants.JWT_TOKEN);
				Map<String, Object> decodeHashMap = AuthNUtil.decodeJwtHeaderAndPayloadData(jwtTokenFromBody);
				JSONObject payloadJsonObj = (JSONObject) decodeHashMap.get("payloadData");
				String accTkn = payloadJsonObj.getJSONObject("authData").getString(OAuth.OAUTH_ACCESS_TOKEN);
				if (AuthNUtil.isEmpty(accTkn)) {
					throw new InvalidTokenRevokeRequestException("access TokenValue Not Present in requested json",
							new NullPointerException(), "access TokenValue Not Present in requested json ",
							AuthNConstants.BAD_REQ_CODE);
				}
				accessToken = accTkn;
			} else {
				throw new InvalidTokenRevokeRequestException("access TokenKey Not Present in requested json",
						new Exception(), "access TokenKey Not Present in requested json", AuthNConstants.BAD_REQ_CODE);
			}
		} else {
			try {
				// getting the cookie from request
				Object jwtTokenValue = null;
				String jwtTokenFromCookie = AuthNUtil.getJWTTokenFromCookie(exchange);
				if (jwtTokenFromCookie != null) {
					jwtTokenValue = jwtTokenFromCookie;
				}
				if (jwtTokenFromCookie == null) {
					jwtTokenValue = exchange.getIn().getHeader(AuthNConstants.JWT_TOKEN);
					if (jwtTokenValue == null) {
						throw new InvalidTokenRevokeRequestException("jwt_token Key Not Present in Header",
								new Exception(), "jwt_token Key Not Present in request header",
								AuthNConstants.BAD_REQ_CODE);
					}
				}
				jwtToken = (String) jwtTokenValue;
				Map<String, Object> decodeHashMap = AuthNUtil.decodeJwtHeaderAndPayloadData(jwtToken);
				JSONObject payloadJsonObj = (JSONObject) decodeHashMap.get("payloadData");
				accessToken = payloadJsonObj.getJSONObject("authData").getString(OAuth.OAUTH_ACCESS_TOKEN);
				if (AuthNUtil.isEmpty(accessToken)) {
					throw new InvalidTokenRevokeRequestException("access TokenKey Not Present in requested json",
							new Exception(), "access TokenKey Not Present in requested json",
							AuthNConstants.BAD_REQ_CODE);
				}
			} catch (Exception e1) {
				throw new TokenRevokeException("Unable to logout for the user ", e1);
			}
		}
		IAuthNUserMgmtService authenticationService = new AuthNUserMgmtServiceImpl();
		boolean isRevoked;
		try {
			isRevoked = authenticationService.revokeUserOAuthToken(accessToken);
			if (isRevoked) {
				exchange.getIn().removeHeader("Access_token");
				exchange.getIn().removeHeader("access_token");
				exchange.getIn().removeHeader(AuthNConstants.JWT_TOKEN);
				exchange.getIn().removeHeader("Authorization");
				exchange.getIn().removeHeader("authorization");
				exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
				exchange.getIn()
						.setBody(new JSONObject().put("message", "Successfully logged out").put("isSuccess", true));
			} else {
				throw new TokenRevokeException("Unable to logout for the user");
			}
		} catch (TokenRevokeException e) {
			throw new TokenRevokeException("Unable to logout for the user ", e);
		}
	}

}
