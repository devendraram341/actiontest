package com.leap.authentication.camelbean.usermgmntservice;

import java.util.Map;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.InvalidTokenRevokeRequestException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class GetUser extends AbstractMetaModelBean {

	static final String ACCESS_TOKEN = "access_token";
	static final String EXPIERS_IN = "expires_in";

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		User userObj;
		String bearerString;
		Object inQuery =null;
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
		String jwtToken = (String) jwtTokenValue;
		Map<String, Object> decodeHashMap = AuthNUtil.decodeJwtHeaderAndPayloadData(jwtToken);
		JSONObject payloadJsonObj = (JSONObject) decodeHashMap.get("payloadData");
		inQuery = payloadJsonObj.getJSONObject("authData").getString(OAuth.OAUTH_ACCESS_TOKEN);
		if (inQuery == null) {
			throw new UserProfileFetchException(
					"Invalid parameter requested, expected non-empty key - " + ACCESS_TOKEN, null,
					"Invalid parameter requested, expected non-empty key - " + ACCESS_TOKEN,
					AuthNConstants.BAD_REQ_CODE);
		}
		String inQueryString = inQuery.toString().trim();
		String authBearer;
		if (!AuthNUtil.isEmpty(inQueryString)) {
			authBearer = inQueryString;
		} else {
			throw new UserProfileFetchException(
					"Invalid parameter requested, expected non-empty value for key - " + ACCESS_TOKEN, null,
					"Invalid parameter requested, expected non-empty value for key - " + ACCESS_TOKEN,
					AuthNConstants.BAD_REQ_CODE);
		}
		if (AuthNUtil.isEmpty(authBearer)) {
			throw new UserProfileFetchException("Invalid Empty token requested: ", null,
					"Invalid Empty token requested: ", AuthNConstants.BAD_REQ_CODE);
		}
		bearerString = "Bearer " + authBearer;
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		logger.debug("bearerString: " + bearerString);
		userObj = service.getUserProfile(bearerString);
		logger.debug("Successfully retreived userObject from the store! ");
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
