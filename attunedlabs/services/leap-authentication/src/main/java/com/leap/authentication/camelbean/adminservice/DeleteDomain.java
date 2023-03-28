package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class DeleteDomain extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

//		String inBody = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inBody = leapDataContext.getInitialRequestData().getData().toString();
		String companyName;
		String domain;
		if (AuthNUtil.isEmpty(inBody)) {
			throw new UserProfileFetchException("Invalid request! ", new Exception(), "Invalid request! ",
					AuthNConstants.BAD_REQ_CODE);
		}
		try {
			JSONObject jsonObject = new JSONObject(inBody);
			companyName = jsonObject.getString("companyname");
			domain = jsonObject.getString("domain");
		} catch (Exception e) {
			throw new UserProfileFetchException("Invalid request to delete ", e,
					"Invalid request to delete " + e.getMessage() + " " + e.getCause(), AuthNConstants.BAD_REQ_CODE);
		}
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		adminService.deleteDomain(domain, companyName);
		JSONObject res = new JSONObject();
		res.put(AuthNConstants.MSG_KEY, "Successfully removed - " + domain + " -from the company -" + companyName + " !");
		res.put(AuthNConstants.SUCCESS, true);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(res.toString());
	}

}
