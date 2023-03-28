package com.leap.authentication.camelbean.adminservice;

import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.InvalidDomainReqException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class GetDomainNameByCompanyName extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		String companyNameQuery = (String) exchange.getIn().getHeader(Exchange.HTTP_QUERY);
		logger.debug("..getDomainNameByCompanyName.." + companyNameQuery);
		if (AuthNUtil.isEmpty(companyNameQuery)) {
			throw new InvalidDomainReqException("Empty companyName requested !", new Exception(),
					"Empty companyName requested !, expected values! ", AuthNConstants.BAD_REQ_CODE);
		}
		if (!companyNameQuery.contains("company=")) {
			throw new InvalidDomainReqException("Query parameters 'company' missing..", new Exception(),
					"Query parameters 'company' missing..", AuthNConstants.BAD_REQ_CODE);
		}

		/*
		 * try { companyNameQuery = URIEncoderDecoder.decode(companyNameQuery);
		 * } catch (UnsupportedEncodingException e1) { throw new
		 * InvalidDomainReqException("Query parameters are not able to decode",
		 * e1, "Query parameters are not able to decode" + e1.getMessage() + " "
		 * + e1.getCause(), BAD_REQ_CODE); }
		 */
		String[] companyArr = companyNameQuery.split("=");
		String companyName = companyArr[1];

		if (AuthNUtil.isEmpty(companyName)) {
			throw new InvalidDomainReqException("Empty parameters! 'company'.value missing..", new Exception(),
					"Empty parameters! 'company'.value missing..", AuthNConstants.BAD_REQ_CODE);
		}
		if (companyName.contains("%20")) {
			companyName = companyName.replace("%20", " ");
		}

		companyName = AuthNUtil.decoder(companyName, "%40", AuthNConstants.AT_SIGN);
		companyName = AuthNUtil.decoder(companyName, "%2D", "-");
		companyName = AuthNUtil.decoder(companyName, "%5F", "_");
		companyName = AuthNUtil.decoder(companyName, "%27", "'");

		companyName = AuthNUtil.decoder(companyName, "%26", "&");
		companyName = AuthNUtil.decoder(companyName, "%2C", ",");
		companyName = AuthNUtil.decoder(companyName, "%22", "\"");
		companyName = AuthNUtil.decoder(companyName, "%2E", ".");


		logger.debug("companyName: " + companyName);
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List<String> domainList = adminService.getDomainNameByAccountName(companyName.trim());
		JSONArray jsonArray = new JSONArray();
		if (!domainList.isEmpty()) {
			for (String domain : domainList) {
				JSONObject eachDomain;
				try {
					eachDomain = new JSONObject(adminService.getTenantIdAndSiteIdByDomain(domain));
					eachDomain.put("company", companyName);
					eachDomain.put("domain", domain);
				} catch (JSONException e1) {
					throw new DomainIdentificationException("Unable to get the TenatID / SiteID for the domain! "
							+ e1.getMessage() + " " + e1.getCause(), e1);
				}
				jsonArray.put(eachDomain);
			}
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("domains", jsonArray);
		/*if (domainList.isEmpty()) {
			jsonObject.put("success", false);
		} else {
			jsonObject.put("success", true);
		}*/
		exchange.getIn().setHeader(AuthNConstants.CONTENT_TYPE, AuthNConstants.APP_JSON);
		exchange.getIn().setBody(jsonObject.toString());
		
	}

}
