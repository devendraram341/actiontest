package com.leap.authentication.camelbean.usermgmntservice;

import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.InvalidDomainReqException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class GetAllDomainsByUser extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {

		String userNameQuery = (String) exchange.getIn().getHeader(Exchange.HTTP_QUERY);
		logger.debug("..getAllUserDomains.. " + userNameQuery);
		if (AuthNUtil.isEmpty(userNameQuery)) {
			throw new InvalidDomainReqException("Empty userName requested !", new Exception(),
					"Empty userName requested !, expected values! ", AuthNConstants.BAD_REQ_CODE);
		}
		if (!userNameQuery.trim().contains("username=")) {
			throw new InvalidDomainReqException("Query parameter unmatched with respect to 'username' ",
					new Exception(), "Query parameter unmatched with respect to 'username' ",
					AuthNConstants.BAD_REQ_CODE);
		}
		/*
		 * try { userNameQuery = URIEncoderDecoder.decode(userNameQuery); }
		 * catch (UnsupportedEncodingException e1) { throw new
		 * InvalidDomainReqException("Query parameters are not able to decode",
		 * e1, "Query parameters are not able to decode " + e1.getMessage() +
		 * " " + e1.getCause(), BAD_REQ_CODE); }
		 */
		String[] unameArr;
		try {
			unameArr = userNameQuery.split("=");
		} catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
			throw new InvalidDomainReqException("Query parameter unmatched with respect to 'username' ", e,
					"Query parameter unmatched with respect to 'username' " + e.getMessage() + " " + e.getCause(),
					AuthNConstants.BAD_REQ_CODE);
		}
		String userName = null;
		try {
			userName = unameArr[1];
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			throw new DomainIdentificationException(
					"Query parameter unmatched with respect to 'username' " + e.getMessage() + " " + e.getCause(), e);
		}
		if (AuthNUtil.isEmpty(userName)) {
			throw new InvalidDomainReqException("Empty userName requested !", new Exception(),
					"Empty userName requested !, expected values! ", AuthNConstants.BAD_REQ_CODE);
		}
		if (userName.contains("%20")) {
			userName = userName.replace("%20", " ");
		}
		IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
		List<String> domains;
		userName = AuthNUtil.decoder(userName, "%40", AuthNConstants.AT_SIGN);
		userName = AuthNUtil.decoder(userName, "%2D", "-");
		userName = AuthNUtil.decoder(userName, "%5F", "_");
		userName = AuthNUtil.decoder(userName, "%27", "'");

		userName = AuthNUtil.decoder(userName, "%26", "&");
		userName = AuthNUtil.decoder(userName, "%2C", ",");
		userName = AuthNUtil.decoder(userName, "%22", "\"");
		userName = AuthNUtil.decoder(userName, "%2E", ".");

		domains = service.getAllDomainsByUser(userName.trim());
		JSONArray jsonArray = new JSONArray();
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		if (!domains.isEmpty()) {
			for (String domain : domains) {
				JSONObject eachDomain;
				try {
					eachDomain = new JSONObject(adminService.getTenantIdAndSiteIdByDomain(domain));
					IAccountRegistryService registryService = new AccountRegistryServiceImpl();
					int accountIdByDomain = registryService.getAccountIdByDomain(domain);
					AccountDetails accountByAccountId = registryService.getAccountByAccountId(accountIdByDomain);
					eachDomain.put("company", accountByAccountId.getAccountName());
					eachDomain.put("domain", domain);
				} catch (JSONException e) {
					throw new DomainIdentificationException(
							"Unable to get the TenatID / SiteID for the domain! " + e.getMessage() + " " + e.getCause(),
							e);
				}
				jsonArray.put(eachDomain);
			}
		}
		JSONObject jsonObject = new JSONObject();
		/*
		 * if (domains.isEmpty()) { jsonObject.put("success", false); } else {
		 * jsonObject.put("success", true); }
		 */
		jsonObject.put("domains", jsonArray);
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(jsonObject.toString());

	}

}
