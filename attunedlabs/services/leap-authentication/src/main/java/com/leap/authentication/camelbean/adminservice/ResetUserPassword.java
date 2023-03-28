package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class ResetUserPassword extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		String userName;
		String newPassword;
		String domain;
		try {
//			String inputJsonStr = exchange.getIn().getBody(String.class);
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject inputJson = new JSONObject(inputJsonStr);
			userName = inputJson.get(AuthNConstants.USER_NAME).toString().trim();
			newPassword = inputJson.get(AuthNConstants.NEW_PASSWORD).toString().trim();
			domain = inputJson.get(AuthNConstants.DOMAIN).toString().trim();
			if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(newPassword)) {
				throw new CredentialUpdateException("Invalid Domain OR Username and Password", null,
						"Invalid Domain OR Username and Password", AuthNConstants.BAD_REQ_CODE);
			}

		} catch (JSONException e) {
			throw new CredentialUpdateException("Invalid JSON Exception at createUser", e,
					"Invalid JSON Exception at createUser" + e.getMessage() + " " + e.getCause(),
					AuthNConstants.BAD_REQ_CODE);
		}
		try {
			IAuthNAdminService adminService = new AuthNAdminServiceImpl();
			adminService.resetUserPassword(userName, newPassword, domain);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(AuthNConstants.SUCCESS, true);
			jsonObject.put(AuthNConstants.MSG_KEY, "Password reset successful");
			exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
			exchange.getIn().setBody(jsonObject);
		} catch (Exception e) {
			throw new CredentialUpdateException("Error while reseting the password" + e.getMessage(), e, e.getMessage(),
					AuthNConstants.BAD_REQ_CODE);
		}

	}

}
