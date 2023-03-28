package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.AssertPermissionInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to check that user is having permission on any specific
 * resource or not.
 * 
 * @author Getusleap
 */
public class HasPermissionForUserBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the user, resourceName,permissionType from the
	 * message exchange and returning the boolean result.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..hasUserPermission service bean");
		try {
			Message message = exchange.getIn();
			String userName = null;
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
				userName = assertData.getString(ResourceConstant.USER);
				resourceName = assertData.getString(ResourceConstant.RESOURCE);
				permissionType = assertData.getString(ResourceConstant.PERMISSION);
				tenant = assertData.getString(ResourceConstant.TENANT_NAME);
				
				userName = AuthUtil.decodeParameter(userName);
				resourceName = AuthUtil.decodeParameter(resourceName);
				permissionType = AuthUtil.decodeParameter(permissionType);
				tenant = AuthUtil.decodeParameter(tenant);
			} catch (JSONException exp) {
				throw new AssertPermissionInvalidRequestException(
						"bad json format/USER,RESOURCE and PERMISSION,TENANT field is mandatory", exp, exp.getMessage(),
						403);
			}
			Boolean hasPermission;
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			User user = new User();
			user.setUserName(userName);
			try {
				hasPermission = service.hasPermissionForUser(user, resourceName, permissionType, tenant);
			} catch (Exception exp) {
				throw new AssertPermissionInvalidRequestException(
						"empty field/USER,RESOURCE and PERMISSION,TENANT does not exist", exp, exp.getMessage(), 403);
			}
			if (!hasPermission) {
				throw new AssertPermissionInvalidRequestException("user is not authorized", null,
						"user is not authorized for " + resourceName, 403);
			}
			JSONObject data = new JSONObject();
			data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
			message.setBody(data);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, e.getMessage(), 400);
		}
	}// ..end of the method
}// ..end of the bean
