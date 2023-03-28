package com.leap.authentication.camelbean.adminservice;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.exception.InvalidDomainRegistrationReqException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class RegisterNewDomainByAccount extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

//		String domainReq = exchange.getIn().getBody(String.class);
		LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
		String domainReq = leapDataContext.getInitialRequestData().getData().toString();
		if (AuthNUtil.isEmpty(domainReq)) {
			throw new InvalidDomainRegistrationReqException("Empty domain requested to register! ", new Exception(),
					"Empty domain requested to register! ", AuthNConstants.BAD_REQ_CODE);
		}
		String adminemail = null;
		String accountname = null;
		String domainname = null;
		String siteid = null;
		String adminfirstname = null;
		String adminlastname = null;
		String adminusername = null;
		String adminpassword = null;
		String defaultAppName = null;
		String timezone = null;
		Domain domain = new Domain();
		boolean defaultAppFlag;
		try {
			JSONObject jsonObject = new JSONObject(domainReq);
			JSONObject domainAttrJson = jsonObject.getJSONObject("newdomain");
			accountname = domainAttrJson.getString("accountname");
			domainname = domainAttrJson.getString("domainname");
			siteid = domainAttrJson.getString("siteid");
			adminfirstname = domainAttrJson.getString("adminfirstname");
			adminlastname = domainAttrJson.getString("adminlastname");
			defaultAppFlag = domainAttrJson.has("defaultapp");
			if (defaultAppFlag) {
				defaultAppName = domainAttrJson.getString("defaultapp");
			}
			adminusername = domainAttrJson.getString("adminusername");
			adminpassword = domainAttrJson.getString("adminpassword");
			adminemail = domainAttrJson.getString("adminemail");
			timezone = domainAttrJson.getString("timezone");
		} catch (JSONException e) {
			throw new InvalidDomainRegistrationReqException(
					"Unable to get the required attributes to process registration! ", e,
					"Unable to get the required attributes to process registration! " + e.getMessage() + " "
							+ e.getCause(),
					AuthNConstants.BAD_REQ_CODE);
		}
		domain.setAdminEmail(adminemail);
		domain.setAdminFirstName(adminfirstname);
		domain.setAdminLastName(adminlastname);
		domain.setAdminPassword(adminpassword);
		domain.setAdminUserName(adminusername);
		domain.setDomainName(domainname);
		domain.setSiteId(siteid);
		domain.setTimezone(timezone);
		// domain.setTenantId(tenantid);
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		adminService.registerNewDomainByAccount(accountname, domain, defaultAppFlag, defaultAppName);
		JSONObject res = new JSONObject();
		res.put(AuthNConstants.MSG_KEY, "Successfully registerd - " + domain.getDomainName()
				+ " - as a new domain, under " + accountname + " !");
		res.put(AuthNConstants.SUCCESS, true);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(res.toString());

	}

}
