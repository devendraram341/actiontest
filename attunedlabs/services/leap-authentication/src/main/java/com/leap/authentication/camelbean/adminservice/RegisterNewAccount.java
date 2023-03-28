package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.AccountDetail;
import com.leap.authentication.exception.InvalidCreateAccountReqException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class RegisterNewAccount extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
//		String body = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
		String body = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject object = new JSONObject(body);
		String accountName = (String) object.get("accountName");
		String internalTenantId = (String) object.get("internalTenantId");
		int expirationCount = Integer.valueOf((String) object.get("tenantToken_expiration_init_in_seconds"));
		AccountDetail details = new AccountDetail();
		if (AuthNUtil.isEmpty(accountName.trim()) || AuthNUtil.isEmpty(internalTenantId.trim()))
			throw new InvalidCreateAccountReqException(
					"accountName / internalTenantId / tenantToken_expiration_init_in_seconds is empty..", null,
					"accountName / internalTenantId / tenantToken_expiration_init_in_seconds is empty..",
					AuthNConstants.BAD_REQ_CODE);
		details.setAccountName(accountName);
		details.setExpirationTime(expirationCount);
		details.settenantId(internalTenantId);

		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		adminService.registerNewAccount(details);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AuthNConstants.MSG_KEY, "Account resistered succesfully !");
		jsonObject.put(AuthNConstants.SUCCESS, true);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(jsonObject.toString());
	}

}
