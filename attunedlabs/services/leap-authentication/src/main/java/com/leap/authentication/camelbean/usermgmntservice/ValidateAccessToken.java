package com.leap.authentication.camelbean.usermgmntservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.InvalidAccessTokenException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class ValidateAccessToken extends AbstractMetaModelBean {
	
	static final String POST_METHOD = "post";
	static final String ACCESS_TOKEN = "access_token";
	static final String AUTHRZN_EQ = "Authorization=";
	static final String AUTHRZN = "Authorization";
	static final String BEARER_SPC = "bearer ";
	static final String BEARER_ENC = "bearer%20";
	static final String BEARER_NRML = "bearer";

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		String accessToken = doPostValidation(exchange);
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		try {
			if (service.validateAccessToken(accessToken)) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(AuthNConstants.MSG_KEY, "Token successfully validated ");
				//jsonObject.put(ACCESS_TOKEN, accessToken);
				jsonObject.put(AuthNConstants.SUCCESS, true);
				exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
				exchange.getIn().setBody(jsonObject.toString());
			} else {
				throw new InvalidAccessTokenException("Invalid access_token", null, "Invalid access_token", 401);
			}
		} catch (InvalidAuthenticationException e) {
			throw new InvalidAccessTokenException("Unable to validate access_token! ", e,
					"Unable to validate access_token! " + e.getMessage() + " " + e.getCause(), AuthNConstants.INT_SRVR_CODE);
		}
		
	}
	
	private String doPostValidation(Exchange exchange) throws InvalidAccessTokenException {
//		String inputBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inputBody = leapDataContext.getInitialRequestData().getData().toString();
		if (inputBody == null) {
			throw new InvalidAccessTokenException("Empty JsonFormat requested!", null, "Empty JsonFormat requested! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		JSONObject inputObject;
		try {
			inputObject = new JSONObject(inputBody);
		} catch (JSONException e) {
			throw new InvalidAccessTokenException("Invalid JsonFormat requested! ", e,
					"Invalid JsonFormat requested! " + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		boolean isAccessToken = inputObject.has(ACCESS_TOKEN);
		boolean isAuthorization = inputObject.has(AUTHRZN);
		String accessToken = null;
		if (isAccessToken || isAuthorization) {
			if (isAccessToken) {
				accessToken = inputObject.getString(ACCESS_TOKEN);
			} else if (isAuthorization) {
				String tempBearer = inputObject.getString(AUTHRZN);
				String lowerCase = tempBearer.toLowerCase();
				if (!lowerCase.contains(BEARER_SPC)) {
					throw new InvalidAccessTokenException(
							"Missing '<Bearer> '<access_token>, overcome by sending bearer key along with the token..",
							null,
							"Missing '<Bearer> '<access_token>, overcome by sending bearer key along with the token..",
							AuthNConstants.BAD_REQ_CODE);
				}
				accessToken = lowerCase.split(BEARER_SPC)[1];
			}
		} else {
			throw new InvalidAccessTokenException("Invalid JsonKeys requested! ", null, "Invalid JsonKeys requested! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		return accessToken;
	}// ..end of method
	
}
