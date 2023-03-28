package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.GrantingInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

public class RevokeResourceForUser implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(RevokeResourceForUser.class);

	

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.debug("inside ..associateRoleToUser service bean");
		try {
			String user = null;
			JSONArray resources = new JSONArray();
			String tenant;
			String resourceType = null;
			String resource = null;
			Message message = exchange.getIn();
			try {

//				JSONObject resourcePermissionData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourcePermissionData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, resourcePermissionData);

				logger.debug("userRoleData is: " + resourcePermissionData);
				resourcePermissionData = AuthUtil.toUpperCaseKey(resourcePermissionData);
				user = resourcePermissionData.getString(ResourceConstant.USER);
				if (resourcePermissionData.has(ResourceConstant.RESOURCE))
					resource = resourcePermissionData.getString(ResourceConstant.RESOURCE);
				tenant = resourcePermissionData.getString(ResourceConstant.TENANT_NAME);
				resourceType = resourcePermissionData.getString(ResourceConstant.RESOURCETYPE);
				if (resourcePermissionData.has(ResourceConstant.RESOURCES))
					resources = resourcePermissionData.getJSONArray(ResourceConstant.RESOURCES);
			} catch (JSONException exp) {
				throw new GrantingInvalidRequestException("bad json format/USER, ROLE,TENANT field is mandatory", exp,
						exp.getMessage(), 400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			try {
				if (resource != null)
					service.revokeResourceForUser(user, resource, tenant, resourceType);
				else
					service.revokeResourcesForUser(user, AuthUtil.jsonArrayToList(resources), tenant, resourceType);
			} catch (Exception exp) {
				throw new GrantingInvalidRequestException("Empty field/USER, ROLE,TENANT does not exist", exp,
						exp.getMessage(), 400);
			}
			AuthUtil.setResponse(message, 200, ServiceTypeConstant.REVOKE_RESOURCE_FROM_USER);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}

	}

}
