package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreatePermissionInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to grant permission for role on resource.
 * 
 * @author Getusleap
 *
 */
public class GrantPermissionForRoleBean extends AbstractMetaModelBean {

	/**
	 * Inside this method we are taking the data from the exchange and according
	 * the data we are granting the permission for role on resource.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..grantPermissionForRole service bean");
		try {
			Message message = exchange.getIn();
			String roleId = null;
			String resourceId = null;
			String permission = null;
			String tenant = null;
			try {
//				JSONObject resourcePermissionData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourcePermissionData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, resourcePermissionData);

				logger.debug("resourcePermissionData is " + resourcePermissionData);
				resourcePermissionData = AuthUtil.toUpperCaseKey(resourcePermissionData);
				permission = resourcePermissionData.getString(ResourceConstant.PERMISSION);
				roleId = resourcePermissionData.getString(ResourceConstant.ROLE);
				resourceId = resourcePermissionData.getString(ResourceConstant.RESOURCE);
				tenant = resourcePermissionData.getString(ResourceConstant.TENANT_NAME);
			} catch (JSONException exp) {
				throw new CreatePermissionInvalidRequestException(
						"bad json format/PERMISSION,ROLE,RESOURCE,TENANT field is mandatory", exp, exp.getMessage(),
						400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			try {
				service.grantPermissionOnRole(tenant, roleId, permission, resourceId);
			} catch (Exception exp) {
				throw new CreatePermissionInvalidRequestException(
						"Empty fields/PERMISSION already exist/ROLE,RESOURCE,TENANT does not exist", exp,
						exp.getMessage(), 400);
			}
			logger.debug("permission " + permission + " for " + roleId + " on resource " + resourceId + " is granted");
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.GRANT_PERMISSION_FOR_ROLE);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean