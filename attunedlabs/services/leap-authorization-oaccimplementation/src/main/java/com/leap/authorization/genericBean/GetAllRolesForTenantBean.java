package com.leap.authorization.genericBean;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.ResourceConstant;

public class GetAllRolesForTenantBean implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GetAllRolesForTenantBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.debug("inside getAllRoles for tenant service bean");

//		JSONObject resourceData = new JSONObject(exchange.getIn().getBody(String.class).trim());
		LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
		String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
		JSONObject resourceData = new JSONObject(inputJsonStr);
		JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
		resourceData = (JSONObject) jsonArray.get(0);
		String tenant = resourceData.getString(ResourceConstant.TENANT);

		IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
		List<String> externalIds = service.getResouceClassIdByTenanatAndResourceClass(tenant);
		List<String> roles = new ArrayList<>();
		externalIds.forEach(externalId -> {
			logger.debug("inside fo loop of externalId"+externalId);
			String role = externalId.substring(0, externalId.indexOf('-'));
			roles.add(role);
		});

		JSONObject roleJsonObj = new JSONObject();
		roleJsonObj.put("Roles", roles);
		exchange.getIn().setBody(roleJsonObj);

	}

}
