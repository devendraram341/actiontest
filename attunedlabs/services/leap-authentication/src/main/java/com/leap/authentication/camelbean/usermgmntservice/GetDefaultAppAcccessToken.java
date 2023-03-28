package com.leap.authentication.camelbean.usermgmntservice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.InvalidAttributesFetchException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.exception.LogInFailureException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

/**
 * This class is used to get the AccessToken for Default User
 * 
 * @author reactiveworks
 *
 */
public class GetDefaultAppAcccessToken {

	private Logger logger = LoggerFactory.getLogger(GetDefaultAppAcccessToken.class);
	private HazelcastInstance hazelcastInstance;

	IAuthNUserMgmtService iAuthenticationService;
	IAccountRegistryService accountRegService;

	public GetDefaultAppAcccessToken() throws com.leap.authentication.exception.PropertiesConfigException {
		iAuthenticationService = new AuthNUserMgmtServiceImpl();
		hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		accountRegService = new AccountRegistryServiceImpl();
	}

	static final String CONTENT_TYPE = "Content-type";
	static final String APP_JSON = "application/json";

	/**
	 * This method is used to get the Access token and set in the respone body
	 * 
	 * @param exchange
	 *            : Camel Exchange Object Body
	 * @throws LogInFailureException
	 * @throws InvalidAuthenticationException
	 * @throws DigestMakeException
	 */
	public void authenticatedinternalLogin(Exchange exchange)
			throws LogInFailureException, InvalidAuthenticationException, DigestMakeException {
		logger.debug("...authenticatedinternalLogin()...");
		Map<String, String> mapQ;
		JSONObject jsonObject;
		Message messageBody = exchange.getIn();
//		String queryString = messageBody.getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String queryString = leapDataContext.getInitialRequestData().getData().toString();
		if (AuthNUtil.isEmpty(queryString)) {
			throw new LogInFailureException("Empty domain-json data requested", new Exception(),
					"Empty domain-json data requested", 400);
		}
		try {
			jsonObject = new JSONObject(queryString);
			logger.debug("Request which is coming: " + jsonObject);
		} catch (JSONException e) {
			throw new LogInFailureException("Invalid Json-format requested in authenication request attributes: ", e,
					"Invalid Json-format requested in authenication request attributes" + e.getMessage(), 400);
		}
		mapQ = new LinkedHashMap<>();

		String domainValue = null;
		try {
			domainValue = jsonObject.getString("domain");
			if (AuthNUtil.isEmpty(domainValue)) {
				throw new LogInFailureException("Empty json data requested", new Exception(),
						"Empty domain-json data requested", 400);
			}
		} catch (JSONException e) {
			throw new LogInFailureException("Unable to get the domain from the request: ", e,
					"Unable to get the domain from the request" + e.getMessage(), 400);
		}
		try {
			mapQ = getUserValuesFromHazelcast(hazelcastInstance, messageBody, domainValue);
		} catch (AccountFetchException e1) {
			e1.printStackTrace();
		}
		mapQ.put("domain", domainValue);
		String domain = mapQ.get("domain");
		logger.debug("domain: " + domain);
		String userName = null;
		String password = null;
		try {
			userName = mapQ.get("userName");
			password = mapQ.get("password");
		} catch (JSONException e) {
			throw new LogInFailureException("Invalid username/password requested! ", e,
					"Invalid username/password requested! " + e.getMessage() + " " + e.getCause(), 400);
		}
		if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(password)) {
			throw new LogInFailureException("tenantid or Username or password Empty!", null,
					"tenantid or Username or password Empty!", 400);
		}
		if (jsonObject.has("defaultapp")) {
			if (userName.equalsIgnoreCase(jsonObject.getString("defaultapp"))) {
				userName = jsonObject.getString("defaultapp");
			} else {
				throw new LogInFailureException("default app is not mapped with the existing from request!", null,
						"default app is not mapped with the existing from request!", 400);
			}
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
		exchange.getIn().setBody(new JSONObject().put(AuthNConstants.JWT_TOKEN, jwtTokenData));
		messageBody.removeHeader(TenantSecurityConstant.TENANT_TOKEN_LOGIN);
		messageBody.removeHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME_LOGIN);
	}// ..end of the method

	/**
	 * This method is used to store the User Detail Information in hazelcast
	 * 
	 * @param userHazelcastInstance
	 *            : Hazelcast Instance Object
	 * @param Message
	 *            :: {@link Message}
	 * @param domainValue
	 *            : String From Exchnage Body
	 * @return Map Object
	 * @throws AccountFetchException
	 */
	private Map<String, String> getUserValuesFromHazelcast(HazelcastInstance userHazelcastInstance, Message messageBody,
			String domainValue) throws AccountFetchException {
		logger.debug("inside the getUserValuesFromHazelcast method of InternalAuthenticateUser Class"
				+ userHazelcastInstance);
		LeapDataContext leapDataCtxHeader = messageBody.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,
				LeapDataContext.class);
		LeapServiceContext serviceDataContext = leapDataCtxHeader.getServiceDataContext();
		userHazelcastInstance
				.getMap(storeGroupKey(serviceDataContext.getTenant(), serviceDataContext.getSite(), domainValue));
		Map<String, String> userDeatils = iAuthenticationService.appDeatils(serviceDataContext.getTenant(),
				serviceDataContext.getSite(), domainValue);
		return userDeatils;

	}// ..end of method getUserValuesFromHazelcast

	/**
	 * This method is used to create the key in hazelcast based on tenant, site
	 * and domain
	 * 
	 * @param accountId
	 *            : String accountId from Exchange Header
	 * @param siteId
	 *            : String siteId from Exchange Header
	 * @param domainValue
	 *            : String From Exchnage Body
	 * @return String
	 */
	private String storeGroupKey(String tenantId, String siteId, String domainValue) throws AccountFetchException {
		logger.debug("IMap group key : " + tenantId + "-" + siteId + "-" + domainValue);
		return tenantId + "-" + siteId + "-" + domainValue;

	}// ..end of method storeGroupKey

}
