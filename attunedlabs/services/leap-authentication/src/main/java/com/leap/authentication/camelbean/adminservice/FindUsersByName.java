package com.leap.authentication.camelbean.adminservice;

import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class FindUsersByName extends AbstractMetaModelBean {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processBean(Exchange exchange) throws Exception {

//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject input;
		int pageSize;
		int pageIndex;
		String domain;
		String nameSearchable;
		try {
			input = new JSONObject(inBody);
			nameSearchable = input.get("namekeyword").toString();
			domain = input.get("domain").toString();
			pageIndex = Integer.parseInt(input.get("pageindex").toString());
			pageSize = Integer.parseInt(input.get("pagesize").toString());
		} catch (Exception e) {
			throw new UserProfileFetchException("Invalid format requested ! ", e,
					"" + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List usrList = adminService.findUserByNamePaginated(nameSearchable, domain, pageIndex, pageSize);
		JSONArray jsonArray = new JSONArray();
		for (User user : (List<User>) usrList.get(0)) {
			JSONObject jsonObject = new JSONObject();
			/*
			 * jsonObject.put(AuthNConstants.DOMAIN, user.getDomain());
			 * jsonObject.put(AuthNConstants.USER_NAME, user.getUserName());
			 */
			jsonObject.put(AuthNConstants.FIRST_NAME, user.getFirstName());
			jsonObject.put(AuthNConstants.LAST_NAME, user.getLastName());
			jsonObject.put(AuthNConstants.TITLE, user.getTitle());
			
			/*
			 * jsonObject.put(AuthNConstants.CREATED_DATE, user.getCreatedDate());
			 * jsonObject.put(AuthNConstants.LAST_MODIFIED_DATE,
			 * user.getLastModifiedDate()); addrObj.put(AuthNConstants.COUNTRY,
			 * user.getUserClaims().getCountry());
			 */

			jsonObject.put(AuthNConstants.EMAIL, user.getUserClaims().getEmailaddress());

			/*
			 * jsonObject.put(AuthNConstants.MOBILE, user.getUserClaims().getMobile());
			 * companyObje.put(AuthNConstants.COMAPNY_NAME,
			 * user.getUserClaims().getOrganization());
			 * addrObj.put(AuthNConstants.ADDERSS_1,
			 * user.getUserClaims().getStreetaddress());
			 * companyObje.put(AuthNConstants.CONTACT_NUMBER,
			 * user.getUserClaims().getTelephone());
			 * jsonObject.put(AuthNConstants.IS_ACTIVE, user.getUserClaims().getIsActive());
			 * jsonObject.put(AuthNConstants.IS_LOCKED, user.getUserClaims().getIsLocked());
			 * addrObj.put(AuthNConstants.STREET, user.getUserClaims().getStreet());
			 * addrObj.put(AuthNConstants.STATE, user.getUserClaims().getStateOrProvince());
			 * addrObj.put(AuthNConstants.REGION, user.getUserClaims().getRegion());
			 * addrObj.put(AuthNConstants.LOCALITY, user.getUserClaims().getLocality());
			 * addrObj.put(AuthNConstants.CITY, user.getUserClaims().getCity());
			 * addrObj.put(AuthNConstants.POSTAL_CODE,
			 * user.getUserClaims().getPostalCode());
			 * companyObje.put(AuthNConstants.ADDRESS, addrObj);
			 * jsonObject.put(AuthNConstants.COMPANY, companyObje);
			 */

			if (!(jsonObject.getString(AuthNConstants.FIRST_NAME).equalsIgnoreCase(AuthNConstants.ADMIN)
					|| jsonObject.getString(AuthNConstants.LAST_NAME).equalsIgnoreCase(AuthNConstants.ADMIN)
					|| jsonObject.getString(AuthNConstants.EMAIL).contains(AuthNConstants.ADMIN))) {
				jsonArray.put(jsonObject);
			}
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("users", jsonArray);
		jsonObject.put("totalcount", Integer.parseInt(usrList.get(1).toString()));
		exchange.getIn().setBody(jsonArray);

	}

}
