package com.leap.authentication.camelbean.usermgmntservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class GetUserByNameAndDomain extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		User userObj;
//		String inQuery = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inQuery = leapDataContext.getInitialRequestData().getData().toString();
		if (inQuery == null) {
			throw new UserProfileFetchException("Invalid parameter requested, expected non-empty request", null,
					"Invalid parameter requested, expected non-empty request", AuthNConstants.BAD_REQ_CODE);
		}
		String uName;
		String domain;
		try {
			JSONObject jsonObject = new JSONObject(inQuery);
			uName = jsonObject.getString("username");
			domain = jsonObject.getString("domain");
		} catch (JSONException e) {
			throw new UserProfileFetchException("Invalid request paramenter! ", e,
					"Invalid request paramenter! " + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		if (AuthNUtil.isEmpty(uName) || AuthNUtil.isEmpty(domain)) {
			throw new UserProfileFetchException("Invalid parameter requested, expected non-empty request", null,
					"Invalid parameter requested, expected non-empty request", AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		try {
			userObj = service.getUserProfile(uName, domain);
		} catch (UserProfileFetchException e) {
			throw new UserProfileFetchException("Unable to get user profile..", e,
					"Unable to get user profile.." + e.getMessage() + " " + e.getCause(), AuthNConstants.INT_SRVR_CODE);
		}
		String userDetails = createJSONResponseForGetUser(userObj);
		exchange.getIn().removeHeader("Access_token");
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(userDetails);

	}

	private String createJSONResponseForGetUser(User user) {
		logger.debug(".createJSONResponseForGetUser");

		JSONObject jsonObject = new JSONObject();
		JSONObject companyObje = new JSONObject();
		JSONObject addrObj = new JSONObject();
		jsonObject.put(AuthNConstants.DOMAIN, user.getDomain());
		jsonObject.put(AuthNConstants.USER_NAME, user.getUserName());
		jsonObject.put(AuthNConstants.FIRST_NAME, user.getFirstName());
		jsonObject.put(AuthNConstants.LAST_NAME, user.getLastName());
		jsonObject.put(AuthNConstants.TITLE, user.getTitle());
		jsonObject.put(AuthNConstants.CREATED_DATE, user.getCreatedDate());
		jsonObject.put(AuthNConstants.LAST_MODIFIED_DATE, user.getLastModifiedDate());
		addrObj.put(AuthNConstants.COUNTRY, user.getUserClaims().getCountry());
		jsonObject.put(AuthNConstants.EMAIL, user.getUserClaims().getEmailaddress());
		jsonObject.put(AuthNConstants.MOBILE, user.getUserClaims().getMobile());
		companyObje.put(AuthNConstants.COMAPNY_NAME, user.getUserClaims().getOrganization());
		addrObj.put(AuthNConstants.ADDERSS_1, user.getUserClaims().getStreetaddress());
		companyObje.put(AuthNConstants.CONTACT_NUMBER, user.getUserClaims().getTelephone());
		jsonObject.put(AuthNConstants.IS_ACTIVE, user.getUserClaims().getIsActive());
		jsonObject.put(AuthNConstants.IS_LOCKED, user.getUserClaims().getIsLocked());
		addrObj.put(AuthNConstants.STREET, user.getUserClaims().getStreet());
		addrObj.put(AuthNConstants.STATE, user.getUserClaims().getStateOrProvince());
		addrObj.put(AuthNConstants.REGION, user.getUserClaims().getRegion());
		addrObj.put(AuthNConstants.LOCALITY, user.getUserClaims().getLocality());
		addrObj.put(AuthNConstants.CITY, user.getUserClaims().getCity());
		addrObj.put(AuthNConstants.POSTAL_CODE, user.getUserClaims().getPostalCode());
		companyObje.put(AuthNConstants.ADDRESS, addrObj);
		jsonObject.put(AuthNConstants.COMPANY, companyObje);
		return jsonObject.toString();
	}// ..end of the method

}
