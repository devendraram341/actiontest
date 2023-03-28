package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.AssertPermissionInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to check that role is having permission on any specific
 * resource or not.
 * 
 * @author Getusleap
 */
public class HasPermissionForRoleBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the role, resourceName,permissionType from the
	 * message exchange and returning the boolean result.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..hasRolePermission service bean");
		try {
			Message message = exchange.getIn();
			String role = null;
			String resourceName = null;
			String permissionType = null;
			String tenant = null;
			try {
//				JSONObject assertData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject assertData = new JSONObject(inputJsonStr);
				if (AuthUtil.hasDataKey(assertData)) {
					JSONArray jsonArray = new JSONArray(assertData.get(ResourceConstant.DATA_KEY).toString());
					assertData = (JSONObject) jsonArray.get(0);
					assertData = AuthUtil.toUpperCaseKey(assertData);
				} else {
					assertData = AuthUtil.toUpperCaseKey(assertData);
				}
				role = assertData.getString(ResourceConstant.ROLE);
				resourceName = assertData.getString(ResourceConstant.RESOURCE);
				permissionType = assertData.getString(ResourceConstant.PERMISSION);
				tenant = assertData.getString(ResourceConstant.TENANT_NAME);
				
				role = AuthUtil.decodeParameter(role);
				resourceName = AuthUtil.decodeParameter(resourceName);
				permissionType = AuthUtil.decodeParameter(permissionType);
				tenant = AuthUtil.decodeParameter(tenant);
			} catch (JSONException exp) {
				throw new AssertPermissionInvalidRequestException(
						"bad json format/ROLE,RESOURCE and PERMISSION,TENANT field is mandatory", exp, exp.getMessage(),
						403);
			}
			Boolean hasPermission;
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			try {
				hasPermission = service.hasPermissionForRole(role, resourceName, permissionType, tenant);
			} catch (Exception exp) {
				throw new AssertPermissionInvalidRequestException(
						"empty field/ROLE,RESOURCE and PERMISSION,TENANT does not exist", exp, exp.getMessage(), 403);
			}
			if (!hasPermission) {
				throw new AssertPermissionInvalidRequestException("role is not authorized", null,
						"role is not authorized for " + resourceName, 403);
			}
			JSONObject data = new JSONObject();
			data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
			message.setBody(data);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
