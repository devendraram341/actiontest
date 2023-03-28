package com.leap.authentication.camelbean.adminservice;

import java.util.List;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetAllDomains extends AbstractMetaModelBean {

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List<Domain> listAllTenants = adminService.listAllDomains();
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		for (Domain domain : listAllTenants) {
			JSONObject each = new JSONObject();
			each.put("company", domain.getCompany());
			each.put("domain", domain.getDomainName());
			each.put("tenantId", domain.getTenantId());
			each.put("siteId", domain.getSiteId());
			array.put(each);
		}
		response.put("domains", array);
		exchange.getIn().setBody(response.toString());	
		
	}

}
