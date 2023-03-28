package com.leap.authentication.camelbean.usermgmntservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class ChangeUserPasssword extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		try {
//			String inputJsonStr = exchange.getIn().getBody(String.class);
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject inputJson = new JSONObject(inputJsonStr);
			String userName = inputJson.get(AuthNConstants.USER_NAME).toString().trim();
			String domain = inputJson.get(AuthNConstants.DOMAIN).toString().trim();
			String oldPassword = inputJson.get(AuthNConstants.OLD_PASSWORD).toString().trim();
			String newPassword = inputJson.get(AuthNConstants.NEW_PASSWORD).toString().trim();
			if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(userName)
					|| AuthNUtil.isEmpty(newPassword) || AuthNUtil.isEmpty(oldPassword)) {
				throw new CredentialUpdateException("Invalid Domain OR Username OR Password", null,
						"Invalid Domain OR Username OR Password", AuthNConstants.BAD_REQ_CODE);
			}
			doChangePassword(userName, domain, oldPassword, newPassword, exchange);
		} catch (JSONException e) {
			throw new CredentialUpdateException("Invalid JSON Exception at createUser", e,
					"Invalid JSON Exception at createUser" + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AuthNConstants.MSG_KEY, "successfully updated your password");
		jsonObject.put(AuthNConstants.SUCCESS, true);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(jsonObject.toString());
		
	}
	
	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param oldPassword
	 * @param newPassword
	 * @param exchange
	 * @throws CredentialUpdateException
	 * @throws PropertiesConfigException 
	 */
	private void doChangePassword(String userName, String domain, String oldPassword, String newPassword,
			Exchange exchange) throws CredentialUpdateException, PropertiesConfigException {
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		try {
			service.changeUserPassword(userName, domain, oldPassword, newPassword);
		} catch (Exception e) {
			throw new CredentialUpdateException("Unable to Change Password", e, "Unable to Change Password",
					AuthNConstants.INT_SRVR_CODE);
		}
	}// ..end of method

}
