package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreateRoleInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to create roles.
 * 
 * @author Getusleap
 *
 */
public class RegisterRolesBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the array of roles and tenant name from the message
	 * exchange and returning the response back after creating roles.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..registerRoles service bean");
		try {
			JSONArray roles = null;
			String tenant = null;
			Message message = exchange.getIn();
			try {
//				JSONObject roleData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject roleData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, roleData);

				logger.debug("roleData is: " + roleData);
				roleData = AuthUtil.toUpperCaseKey(roleData);
				roles = roleData.getJSONArray(ResourceConstant.ROLES);
				tenant = roleData.getString(ResourceConstant.TENANT_NAME);
			} catch (JSONException exp) {
				throw new CreateRoleInvalidRequestException("bad json format/ROLES,TENANT field is mandatory", exp,
						exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.registerRoles(AuthUtil.jsonArrayToList(roles), tenant);
			} catch (Exception exp) {
				throw new CreateRoleInvalidRequestException("Empty field/ROLES already exist/TENANT does not exist",
						exp, exp.getMessage(), 400);
			}
			logger.debug("roles " + roles + " for tenant + " + tenant + " created successfully");
			AuthUtil.setResponse(message, 200, ServiceTypeConstant.REGISTER_ROLES);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
