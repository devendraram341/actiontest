package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.InvalidConfirmationCodeReqException;
import com.leap.authentication.exception.UpdateCredentialException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class UpdateUserStatus extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject jsonObject;
		String userName = null;
		String domain = null;
		String email;
		boolean isActive;
		boolean accountDisable;
		try {
			jsonObject = new JSONObject(inBody);
			userName = jsonObject.getString("username");
			domain = jsonObject.getString("domain");
			email = jsonObject.getString("email");
			isActive = jsonObject.getBoolean("isactive");
			if(isActive){
				accountDisable = false;
			}else{
				accountDisable = true;
			}
		} catch (JSONException e) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					e, e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(domain)
				|| AuthNUtil.isEmpty(email)) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					new Exception(),
					"Unable to procceed because invalid parameters requested! Empty userName-domain requested! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNAdminService service = new AuthNAdminServiceImpl();
		try {
			boolean isUpdated = service.updateUserStatus(domain,userName,email,accountDisable);
			JSONObject object = new JSONObject();
			object.put(AuthNConstants.SUCCESS, isUpdated);
			object.put(AuthNConstants.MSG_KEY, "Successfully updated user status for: " + userName);
			exchange.getIn().setBody(object.toString());
		} catch (AttributesRegistrationException e) {
			throw new UpdateCredentialException(
					"Unable to validate and update the user status! " + e.getMessage() + " " + e.getCause(), e);
		}
	}


}
