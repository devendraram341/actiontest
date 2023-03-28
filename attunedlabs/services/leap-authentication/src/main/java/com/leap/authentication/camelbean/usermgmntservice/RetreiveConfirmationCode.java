package com.leap.authentication.camelbean.usermgmntservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.InvalidConfirmationCodeReqException;
import com.leap.authentication.exception.UserValidationRequestException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class RetreiveConfirmationCode extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject jsonObject;
		String userName = null;
		String domain = null;
		try {
			jsonObject = new JSONObject(inBody);
			userName = jsonObject.getString("username");
			domain = jsonObject.getString("domain");
		} catch (JSONException e) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					e, e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(domain)) {
			throw new InvalidConfirmationCodeReqException("Unable to procceed because invalid parameters requested! ",
					new Exception(),
					"Unable to procceed because invalid parameters requested! Emptu userName-domain requested! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		try {
			JSONObject object = new JSONObject();
			object.put("confirmationcode", service.getForgotPasswordConfmCode(userName.trim(), domain.trim()));
			//object.put("success", true);
			exchange.getIn().setBody(object.toString());
		} catch (UserValidationRequestException e) {
			throw new UserValidationRequestException(
					"Unable to succeed in retreiving confirmation code! " + e.getMessage() + " " + e.getCause(), e);
		}
		
	}

}
