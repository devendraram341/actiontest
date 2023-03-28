package com.leap.authentication.camelbean.adminservice;

import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetAllUsers extends AbstractMetaModelBean {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		JSONObject jsonObject;
		int pageIndex = 0;
		int pageSize = 0;
		String domainsApp = null;
		try {
//			jsonObject = new JSONObject(exchange.getIn().getBody(String.class));
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inBody = leapDataContext.getInitialRequestData().getData().toString();
			jsonObject = new JSONObject(inBody);
			pageIndex = jsonObject.getInt("pageindex");
			pageSize = jsonObject.getInt("pagesize");
			domainsApp = jsonObject.getString("domains");
		} catch (JSONException e) {
			throw new UserProfileFetchException("Invalid format requested ! ", e,
					"" + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		String[] domains = null;
		try {
			domains = domainsApp.split("\\|");
		} catch (Exception e) {
			throw new UserProfileFetchException("Invalid format requested ! ", e,
					"" + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		JSONArray jsonArray = new JSONArray();
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		if (domains == null) {
			throw new UserProfileFetchException("Empty domains, parse exception ! ", new Exception(), "", AuthNConstants.BAD_REQ_CODE);
		}
		List listAll = adminService.getAllUsers(pageIndex, pageSize, domains);
		List<User> allUsersList = (List<User>) listAll.get(0);
		JSONObject userJsonObj ;
		for (User user : allUsersList) {
			userJsonObj = new JSONObject();
			JSONObject companyObje = new JSONObject();
			JSONObject addrObj = new JSONObject();
			userJsonObj.put(AuthNConstants.DOMAIN, user.getDomain());
			userJsonObj.put(AuthNConstants.USER_NAME, user.getUserName());
			userJsonObj.put(AuthNConstants.FIRST_NAME, user.getFirstName());
			userJsonObj.put(AuthNConstants.LAST_NAME, user.getLastName());
			userJsonObj.put(AuthNConstants.TITLE, user.getTitle());
			userJsonObj.put(AuthNConstants.CREATED_DATE, user.getCreatedDate());
			userJsonObj.put(AuthNConstants.LAST_MODIFIED_DATE, user.getLastModifiedDate());
			addrObj.put(AuthNConstants.COUNTRY, user.getUserClaims().getCountry());
			userJsonObj.put(AuthNConstants.EMAIL, user.getUserClaims().getEmailaddress());
			userJsonObj.put(AuthNConstants.MOBILE, user.getUserClaims().getMobile());
			companyObje.put(AuthNConstants.COMAPNY_NAME, user.getUserClaims().getOrganization());
			addrObj.put(AuthNConstants.ADDERSS_1, user.getUserClaims().getStreetaddress());
			companyObje.put(AuthNConstants.CONTACT_NUMBER, user.getUserClaims().getTelephone());
			userJsonObj.put(AuthNConstants.IS_ACTIVE, user.getUserClaims().getIsActive());
			userJsonObj.put(AuthNConstants.IS_LOCKED, user.getUserClaims().getIsLocked());
			addrObj.put(AuthNConstants.STREET, user.getUserClaims().getStreet());
			addrObj.put(AuthNConstants.STATE, user.getUserClaims().getStateOrProvince());
			addrObj.put(AuthNConstants.REGION, user.getUserClaims().getRegion());
			addrObj.put(AuthNConstants.LOCALITY, user.getUserClaims().getLocality());
			addrObj.put(AuthNConstants.CITY, user.getUserClaims().getCity());
			addrObj.put(AuthNConstants.POSTAL_CODE, user.getUserClaims().getPostalCode());
			companyObje.put(AuthNConstants.ADDRESS, addrObj);
			userJsonObj.put(AuthNConstants.COMPANY, companyObje);
			if (!(userJsonObj.getString(AuthNConstants.FIRST_NAME).equalsIgnoreCase(AuthNConstants.ADMIN)
					|| userJsonObj.getString(AuthNConstants.LAST_NAME).equalsIgnoreCase(AuthNConstants.ADMIN)
					|| userJsonObj.getString(AuthNConstants.EMAIL).contains(AuthNConstants.ADMIN))) {
				jsonArray.put(userJsonObj);
			}
		}
		JSONObject responseJson = new JSONObject();
		responseJson.put("users", jsonArray);
		responseJson.put("totalcount", listAll.get(1));
		exchange.getIn().setHeader("Content-Type", "application/json");
		exchange.getIn().setBody(responseJson.toString());
		
	}

}
