package com.leap.authentication.camelbean.usermgmntservice;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.InvalidJSONFormatException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class SelfRegisterUser extends AbstractMetaModelBean {

	private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{8,30})";

	private Pattern pattern;
	private Matcher matcher;

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		String domain = (String) exchange.getIn().getHeader(AuthNConstants.DOMAIN);
		String tenantId;
		String siteId;
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		Map<String, String> mapTenantSite = adminService.getTenantIdAndSiteIdByDomain(domain);
		tenantId = mapTenantSite.get(AuthNConstants.TENANT_ID);
		logger.debug("Domain3: " + domain);
		siteId = mapTenantSite.get(AuthNConstants.SITE_ID);
		if (AuthNUtil.isEmpty(siteId)) {
			throw new UserRepoUpdateException("Invalid Header Key for Authentication", new Exception());
		}
		JSONObject userJSONObject;
		try {
//			String inputJsonStr = exchange.getIn().getBody(String.class);
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			userJSONObject = new JSONObject(inputJsonStr);
		} catch (JSONException e1) {
			throw new UserRepoUpdateException(
					"Invalid JSON requested to parse: " + e1.getMessage() + " " + e1.getCause(), e1);
		}
		User user;
		try {
			user = buildUserFromJsonObj(userJSONObject, tenantId, siteId);
		} catch (UserRepoUpdateException e) {
			throw new InvalidJSONFormatException("JSON Format Exception", e,
					"JSON Format Exception " + e.getCause() + " " + e.getMessage(), AuthNConstants.BAD_REQ_CODE);

		}

		pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(user.getPassword());
		if (!matcher.matches()) {
			throw new UserRepoUpdateException(
					"Password must be at least 8 characters long and contain at least: one lowercase, one uppercase, one number, and one special character",
					"Password Criteria not matched", 400L, new Exception());
		}
		// adminService.get
		try {
			service.selfRegisterUser(user, domain);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(AuthNConstants.MSG_KEY, "User - " + user.getUserName() + " succesfully created! ");
			jsonObject.put(AuthNConstants.SUCCESS, true);

			exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
			exchange.getIn().setBody(jsonObject.toString());
		} catch (Exception e) {
			throw new UserRepoUpdateException(e.getMessage(), e.getMessage(), 400L, e);

		}
		logger.info("Users successFully created in WSO2 ldap-repository !");

	}

	private User buildUserFromJsonObj(JSONObject userObj, String tenantId, String siteId)
			throws UserRepoUpdateException {
		JSONObject userJSONObj = (JSONObject) userObj;
		UserClaims claims = new UserClaims();
		User user = new User();
		try {
			user.setTenantId(tenantId);
			user.setSiteId(siteId);
			user.setUserName(userJSONObj.getString(AuthNConstants.USER_NAME));
			user.setFirstName(userJSONObj.getString(AuthNConstants.FIRST_NAME));
			user.setLastName(userJSONObj.getString(AuthNConstants.LAST_NAME));
			user.setTitle(userJSONObj.getString(AuthNConstants.TITLE));
			user.setPassword(userJSONObj.getString(AuthNConstants.PASSWORD));
			claims.setIsActive(userJSONObj.getString(AuthNConstants.IS_ACTIVE));
			claims.setIsLocked(userJSONObj.getString(AuthNConstants.IS_LOCKED));
			claims.setEmailaddress(userJSONObj.getString(AuthNConstants.EMAIL));
			// Company
			JSONObject companyJSONObj = userJSONObj.getJSONObject(AuthNConstants.COMPANY);
			claims.setOrganization(companyJSONObj.getString(AuthNConstants.COMAPNY_NAME));
			claims.setMobile(companyJSONObj.getString(AuthNConstants.CONTACT_NUMBER));
			claims.setTelephone(companyJSONObj.getString(AuthNConstants.CONTACT_NUMBER));
			JSONObject addressJSONObje = companyJSONObj.getJSONObject(AuthNConstants.ADDRESS);
			String address1 = "";
			String loca = "";
			try {
				address1 = addressJSONObje.getString("address1");
				loca = addressJSONObje.getString("locality");
			} catch (JSONException e) {
				logger.error("Will be ignored, and empty values will be stored across the claims: " + e.getMessage()
						+ " " + e.getCause());
			}
			claims.setStreetaddress(address1);
			// claims.setStreet(addressJSONObje.getString("address2"));
			claims.setLocality(loca);
			claims.setPostalCode(addressJSONObje.getString(AuthNConstants.POSTAL_CODE));
			claims.setRegion(addressJSONObje.getString(AuthNConstants.CITY));
			claims.setStateOrProvince(addressJSONObje.getString(AuthNConstants.STATE));
			claims.setCountry(addressJSONObje.getString(AuthNConstants.COUNTRY));
			claims.setRegion(addressJSONObje.getString(AuthNConstants.REGION));
			user.setUserClaims(claims);
		} catch (JSONException e) {
			throw new UserRepoUpdateException(
					"Unable to get the User constructed from jsonArray - mandatory fields are missing! "
							+ e.getMessage() + " " + e.getCause(),
					e);
		}
		return user;
	}// ..end of method

}
