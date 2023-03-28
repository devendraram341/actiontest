package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class IsUserExist extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject jsonObject;
		String userName = null;
		String domain = null;
		try {
			jsonObject = new JSONObject(inBody);
			userName = jsonObject.getString("username");
			domain = jsonObject.getString("domain");
		} catch (JSONException e) {
			throw new UserProfileFetchException("Invalid request ", e,
					"Invalid request : " + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		JSONObject object = new JSONObject();
		//object.put(SUCCESS, service.isUserExist(userName, domain));
		object.put("isexist", adminService.isUserExist(userName, domain));
		exchange.getIn().setBody(object.toString());
		
	}

}
