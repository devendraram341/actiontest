package com.leap.authentication.camelbean.adminservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.InvalidJSONFormatException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.dao.AuthNAdminDao;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class CreateUsers extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		String domain;
		String tenantId;
		String siteId;
		domain = (String) exchange.getIn().getHeader(AuthNConstants.DOMAIN);
		tenantId = (String) exchange.getIn().getHeader(AuthNConstants.TENANT_ID);

		siteId = (String) exchange.getIn().getHeader(AuthNConstants.SITE);
		if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(tenantId)) {
			throw new UserRepoUpdateException("Invalid Header Key set for Authentication", new Exception());
		}

		JSONArray userJSONArray;
		try {
//			String inputJsonStr = exchange.getIn().getBody(String.class);
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			userJSONArray = new JSONArray(inputJsonStr);
			logger.debug("userJSONArray :: " + userJSONArray);

		} catch (JSONException e1) {
			// AuthNUtil.setResponseCode(BAD_REQ_CODE, exchange,
			// "Invalid JSON Format", e1.getMessage());
			throw new UserRepoUpdateException(
					"Invalid JSON requested to parse: " + e1.getMessage() + " " + e1.getCause(), e1);
		}
		List<User> users;
		try {
			users = buildUserFromJSON(userJSONArray, tenantId, siteId, domain);
		} catch (InvalidJSONFormatException e) {
			throw new UserRepoUpdateException("Unable to get the User constructed from input: ", e);
		}
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		try {
			AuthNAdminDao adminDao = new AuthNAdminDao();
			Domain domainAttributes = adminDao.getDomainAttributes(domain);
			String adminTenantuserName = domainAttributes.getAdminUserName();
			String adminTenantpassword = domainAttributes.getAdminPassword();
			adminService.createUsers(users, domain.trim(),
					adminTenantuserName.trim() + AuthNConstants.AT_SIGN + domain.trim(), adminTenantpassword.trim());
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(AuthNConstants.MSG_KEY, "Users succesfully created! ");
			exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
			exchange.getIn().setBody(jsonObject.toString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new UserRepoUpdateException(
					"Unable to Register User, details : " + e.getMessage() + " " + e.getCause(), e);
		}
		logger.info("Users successFully created in WSO2 ldap-repository !");

	}

	/**
	 * 
	 * @param userJSONArr
	 * @param tenantId
	 * @param siteId
	 * @return
	 * @throws InvalidJSONFormatException
	 */
	@SuppressWarnings("unused")
	private List<User> buildUserFromJSON(JSONArray userJSONArr, String tenantId, String siteId, String domain)
			throws InvalidJSONFormatException {
		logger.debug("..buildUserFromJSON()..");
		List<User> users = new ArrayList<>();
		User user;
		List<String> roleList;
		UserClaims claims;
		for (int i = 0; i < userJSONArr.length(); i++) {
			JSONObject userJSONObj = (JSONObject) userJSONArr.get(i);
			logger.debug("userJSONObj :: " + userJSONObj);
			claims = new UserClaims();
			user = new User();
			try {
				user.setTenantId(tenantId);
				user.setSiteId(siteId);
				user.setDomain(domain);
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
				throw new InvalidJSONFormatException("JSON Format Exception", e, "JSON Format Exception",
						AuthNConstants.BAD_REQ_CODE);
			}
			users.add(user);
		}
		return users;
	}// ..end of the method

}
