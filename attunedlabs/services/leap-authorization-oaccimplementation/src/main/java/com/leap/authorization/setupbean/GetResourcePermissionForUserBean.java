package com.leap.authorization.setupbean;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.GetResourcePermissionInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to get the permission on any resource for a specific user.
 * 
 * @author Getusleap
 *
 */
public class GetResourcePermissionForUserBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the user name and resource name from the message
	 * exchange and returning the permission on that resource for the user.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..getPermissionForUser service bean");
		try {
			Message message = exchange.getIn();
			String user = null;
			String resourceName = null;
			String tenant = null;
			try {
//				JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceData = new JSONObject(inputJsonStr);
				logger.debug("resourceData is: " + resourceData);
				if (AuthUtil.hasDataKey(resourceData)) {
					JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
					resourceData = (JSONObject) jsonArray.get(0);
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				} else {
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				}
				user = resourceData.getString(ResourceConstant.USER);
				resourceName = resourceData.getString(ResourceConstant.RESOURCE_NAME);
				tenant = resourceData.getString(ResourceConstant.TENANT_NAME);

				user = AuthUtil.decodeParameter(user);
				resourceName = AuthUtil.decodeParameter(resourceName);
				tenant = AuthUtil.decodeParameter(tenant);
			} catch (JSONException exp) {
				throw new GetResourcePermissionInvalidRequestException(
						"bad json format/USER,RESOURCENAME,TENANT field is mandatory", exp, exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			List<String> permissionsSet = null;
			try {
				permissionsSet = service.getAllUserPermissionsOnResource(user, resourceName, tenant);
			} catch (Exception exp) {
				throw new GetResourcePermissionInvalidRequestException(
						"Empty field/USER,RESOURCENAME,TENANT does not exist", exp, exp.getMessage(), 400);
			}
			JSONObject data = new JSONObject();
			data.put(ResourceConstant.ACCESS_PERMISSION, permissionsSet);
			message.setBody(data);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
