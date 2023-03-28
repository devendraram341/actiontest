package com.leap.authentication.camelbean.usermgmntservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.InvalidAttributesFetchException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.exception.InvalidRequestException;
import com.leap.authentication.exception.LogInFailureException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class AuthenticateUserWithCookie {

	private Logger logger = LoggerFactory.getLogger(AuthenticateUserWithCookie.class);
	IAuthNUserMgmtService iAuthenticationService;

	public AuthenticateUserWithCookie() throws PropertiesConfigException {
		iAuthenticationService = new AuthNUserMgmtServiceImpl();
	}

	static final String CONTENT_TYPE = "Content-type";
	static final String APP_JSON = "application/json";

	public void authenticatedLogin(Exchange exchange)
			throws LogInFailureException, InvalidAuthenticationException, DigestMakeException, SecretKeyGenException {
		logger.debug("...authenticatedLogin()...");

		Map<String, String> mapQ;
		JSONObject jsonObject = new JSONObject();
		Message messageBody = exchange.getIn();
		LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
		String queryString = leapDataContext.getInitialRequestData().getData().toString();
//		String queryString = messageBody.getBody(String.class);
//		if (AuthNUtil.isEmpty(queryString)) {
//			throw new LogInFailureException("Empty domain-json data requested", new Exception(),
//					"Empty domain-json data requested", 400);
//		}
		try {
			if (queryString != null && !queryString.isEmpty())
				jsonObject = new JSONObject(queryString);
			// logger.debug("Request which is coming: " + jsonObject);
		} catch (JSONException e) {
			throw new LogInFailureException("Invalid Json-format requested in authenication request attributes: ", e,
					"Invalid Json-format requested in authenication request attributes" + e.getMessage(), 400);
		}
		mapQ = new LinkedHashMap<>();
		String domainValue = null;
		try {
			Object domainHeaderObj = messageBody.getHeader("domain");
			if (domainHeaderObj == null)
				domainValue = "carbon.super";

			if (domainHeaderObj != null) {
				domainValue = domainHeaderObj.toString();
			} else {
				if (jsonObject.has("domain"))
					domainValue = jsonObject.getString("domain");
				else
					domainValue = "carbon.super";
			} // ..end of else , fetching the domain value from body request, if
				// domian value doest exist in request then we are passing the
				// super domian value which is "carbon.super"
			if (AuthNUtil.isEmpty(domainValue)) {
				throw new LogInFailureException("Empty json data requested", new Exception(),
						"Empty domain-json data requested", 400);
			}
		} catch (JSONException e) {
			throw new LogInFailureException("Unable to get the domain from the request: ", e,
					"Unable to get the domain from the request" + e.getMessage(), 400);
		}
		mapQ.put("domain", domainValue);
		String domain = mapQ.get("domain");
		logger.debug("domain: " + domain);
		String userName = null;
		String password = null;
		try {
			Object userNameHeaderObj = messageBody.getHeader("username");
			Object passwordHeaderObj = messageBody.getHeader("password");
			if (userNameHeaderObj != null && passwordHeaderObj != null) {
				userName = userNameHeaderObj.toString();
				password = passwordHeaderObj.toString();
			} // ..end of if checking if userName and password value exists in
				// headers then take from Headers otherwise fetch from body
			else {
				userName = jsonObject.getString("username");
				password = jsonObject.getString("password");
			}
		} catch (JSONException e) {
			throw new LogInFailureException("Invalid username/password requested! ", e,
					"Invalid username/password requested! " + e.getMessage() + " " + e.getCause(), 400);
		}
		if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(password)) {
			throw new LogInFailureException("tenantid or Username or password Empty!", null,
					"tenantid or Username or password Empty!", 400);
		}
		Map<String, String> tokenMap = null;
		try {
			logger.trace("userName:   " + userName + "  " + password.getBytes().hashCode() + "  " + domain);
			tokenMap = iAuthenticationService.authenticateUser(userName, password, domain);
			logger.debug("response from the service post-authenticating user ");
		} catch (InvalidAttributesFetchException e) {
			if ((e instanceof InvalidAttributesFetchException)) {
				throw new LogInFailureException(e.getMessage(), e, e.getMessage(), 500);
			}
		}
		String jwtTokenData = AuthNUtil.createJWTTokenStructure(messageBody, tokenMap, userName);
		exchange.getIn().setHeader(CONTENT_TYPE, APP_JSON);
		exchange.getIn().setHeader(LeapDataContextConstant.COOKIE, "jwtTokenlogin");
		exchange.getIn().setBody(new JSONObject().put(AuthNConstants.JWT_TOKEN, jwtTokenData));

	}// ..end of the method

	/**
	 * utility to get the keys from the json
	 * 
	 * @param jsonObject
	 * @return
	 * @throws InvalidRequestException
	 */
	@SuppressWarnings("unused")
	private String getJsonKey(JSONObject jsonObject) throws InvalidRequestException {
		logger.debug("..getJsonKey: ");
		Iterator<String> iterator = jsonObject.keys();
		List<String> copy = new ArrayList<>();
		while (iterator.hasNext())
			copy.add(iterator.next());
		if (!copy.isEmpty()) {
			return copy.get(0);
		} else {
			throw new InvalidRequestException("Unable to get the domain key from request ");
		}
	}// ..end of the method

}
