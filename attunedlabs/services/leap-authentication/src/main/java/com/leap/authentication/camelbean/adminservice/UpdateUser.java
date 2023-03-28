package com.leap.authentication.camelbean.adminservice;

import java.util.Map;

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
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class UpdateUser extends AbstractMetaModelBean {

	static final String SUCCESS = "success";

	@Override
	protected void processBean(Exchange exchange) throws Exception {
//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		String domain = (String) exchange.getIn().getHeader("domain");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		Map<String, String> mapTenantSite = adminService.getTenantIdAndSiteIdByDomain(domain);
		String tenantId = mapTenantSite.get(AuthNConstants.TENANT_ID);
		String siteId = mapTenantSite.get(AuthNConstants.SITE_ID);
		JSONObject jsonObject = null;
		JSONObject object = new JSONObject();
		try {
			jsonObject = new JSONObject(inBody);
		} catch (JSONException e) {
			throw new InvalidJSONFormatException("JSON Format Exception " + e.getMessage(), e,
					"JSON Format Exception " + e.getMessage(), AuthNConstants.BAD_REQ_CODE);
		}
		UserClaims claims = new UserClaims();
		User user = new User();
		try {
			user.setTenantId(tenantId);
			user.setSiteId(siteId);
			user.setUserName(jsonObject.getString(AuthNConstants.USER_NAME));
			user.setFirstName(jsonObject.getString(AuthNConstants.FIRST_NAME));
			user.setLastName(jsonObject.getString(AuthNConstants.LAST_NAME));
			user.setTitle(jsonObject.getString(AuthNConstants.TITLE));
			user.setDomain(domain);
			// user.setPassword(jsonObject.getString(AuthNConstants.PASSWORD));
			claims.setIsActive(jsonObject.getString(AuthNConstants.IS_ACTIVE).trim());
			claims.setIsLocked(jsonObject.getString(AuthNConstants.IS_LOCKED).trim());
			// Company
			JSONObject companyJSONObj = jsonObject.getJSONObject(AuthNConstants.COMPANY);
			claims.setOrganization(companyJSONObj.getString(AuthNConstants.COMAPNY_NAME));
			claims.setMobile(companyJSONObj.getString(AuthNConstants.CONTACT_NUMBER));
			claims.setTelephone(companyJSONObj.getString(AuthNConstants.CONTACT_NUMBER));
			JSONObject addressJSONObje = companyJSONObj.getJSONObject(AuthNConstants.ADDRESS);
			claims.setLocality(addressJSONObje.getString("locality"));
			claims.setCountry(addressJSONObje.getString(AuthNConstants.COUNTRY));
			claims.setStateOrProvince(addressJSONObje.getString(AuthNConstants.STATE));
			claims.setStreetaddress(addressJSONObje.getString("address1"));
			claims.setPostalCode(addressJSONObje.getString(AuthNConstants.POSTAL_CODE));
			claims.setRegion(addressJSONObje.getString(AuthNConstants.REGION));
			claims.setCity(addressJSONObje.getString("city"));
			claims.setEmailaddress(jsonObject.getString(AuthNConstants.EMAIL));
			logger.debug("User Claims: " + claims);
			user.setUserClaims(claims);

		} catch (JSONException e) {
			throw new InvalidJSONFormatException("JSON Format Exception " + e.getMessage(), e,
					"JSON Format Exception " + e.getMessage(), AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNAdminService authenticationAdminService = new AuthNAdminServiceImpl();
		authenticationAdminService.updateUserStatus(user, domain);
		authenticationAdminService.updateUserLock(domain, user.getUserName(), Boolean.parseBoolean(jsonObject.get("islocked").toString()));
		object.put("message", "successfully updated!");
		object.put(SUCCESS, true);
		exchange.getIn().setBody(object.toString());
	}

}
