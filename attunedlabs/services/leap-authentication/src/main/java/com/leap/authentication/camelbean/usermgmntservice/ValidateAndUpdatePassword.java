package com.leap.authentication.camelbean.usermgmntservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.InvalidConfirmationCodeReqException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class ValidateAndUpdatePassword extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inputBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject jsonObject;
		String userName = null;
		String domain = null;
		String newPassword;
		String code;
		try {
			jsonObject = new JSONObject(inputBody);
			userName = jsonObject.getString("username");
			domain = jsonObject.getString("domain");
			newPassword = jsonObject.getString("newpassword");
			code = jsonObject.getString("confirmationcode");
		} catch (JSONException e) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					e, e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(code)
				|| AuthNUtil.isEmpty(newPassword)) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					new Exception(),
					"Unable to procceed because invalid parameters requested! Empty userName-domain requested! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		try {
			service.validateAndUpdatePassword(code.trim(), userName.trim(), domain.trim(), newPassword.trim());
			JSONObject object = new JSONObject();
			object.put(AuthNConstants.SUCCESS, true);
			object.put(AuthNConstants.MSG_KEY, "Successfully updated password for user: " + userName);
			exchange.getIn().setBody(object.toString());
		} catch (CredentialUpdateException e) {
			throw new CredentialUpdateException("Unable to validate and update the password! ", e, e.getMessage(), 500);
		}

	}

}
