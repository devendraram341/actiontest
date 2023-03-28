package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreatePermissionInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to grant permissions for user on resource.
 * 
 * @author Getusleap
 *
 */
public class GrantPermissionForUserBean extends AbstractMetaModelBean {

	/**
	 * Inside this method we are taking the data from the exchange and according the
	 * data we are granting the permissions for user on resource.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..grantPermissionForUser service bean");
		try {
			Message message = exchange.getIn();
			String userString = null;
			String resourcetoGrantOn = null;
			JSONArray permissions = null;
			JSONArray resources = null;
			String tenant = null;
			try {
//				JSONObject resourcePermissionData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourcePermissionData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, resourcePermissionData);

				logger.debug("resourcePermissionData is " + resourcePermissionData);
				resourcePermissionData = AuthUtil.toUpperCaseKey(resourcePermissionData);
				permissions = resourcePermissionData.getJSONArray(ResourceConstant.PERMISSIONS);
				userString = resourcePermissionData.getString(ResourceConstant.USER);
				if (resourcePermissionData.has(ResourceConstant.RESOURCE))
					resourcetoGrantOn = resourcePermissionData.getString(ResourceConstant.RESOURCE);
				tenant = resourcePermissionData.getString(ResourceConstant.TENANT_NAME);
				if (resourcePermissionData.has(ResourceConstant.RESOURCES))
					resources = resourcePermissionData.getJSONArray(ResourceConstant.RESOURCES);
			} catch (JSONException exp) {
				throw new CreatePermissionInvalidRequestException(
						"bad json format/PERMISSIONS,USER,RESOURCE,TENANT field is mandatory", exp, exp.getMessage(),
						400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			AuthorizationGenericServiceImpl serviceimpl = new AuthorizationGenericServiceImpl();
			User user = new User();
			user.setUserName(userString);
			try {
				if (resourcetoGrantOn != null)
					service.grantResourcePermissionsForUser(user, AuthUtil.jsonArrayToList(permissions),
							resourcetoGrantOn, tenant);
				else
					serviceimpl.grantResourcePermissionsForUserForMultipleResources(user,
							AuthUtil.jsonArrayToList(permissions), AuthUtil.jsonArrayToList(resources), tenant);

			} catch (Exception exp) {
				throw new CreatePermissionInvalidRequestException(
						"Empty fields/PERMISSIONS already exist/USER,RESOURCE,TENANT does not exist", exp,
						exp.getMessage(), 400);
			}
			logger.debug(
					"permission " + permissions + " for " + user + " on resource " + resourcetoGrantOn + " is granted");
			AuthUtil.setResponse(message, 200, ServiceTypeConstant.GRANT_PERMISSION_FOR_USERS);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean