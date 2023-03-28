package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreatePermissionInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to create permission for a specific resource type.
 * 
 * @author Getusleap
 *
 */
public class CreateResourceTypePermissionBean extends AbstractMetaModelBean {

	/**
	 * This method is getting the resource type and permission from the message
	 * exchange and returning the response according the values of the data.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..createResourceTypePermission service bean");
		try {
			Message message = exchange.getIn();
			String resourceType = null;
			JSONArray permission = null;
			try {
//				JSONObject permissionData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				logger.debug("inputJsonStr: " + inputJsonStr);
				JSONObject permissionData = new JSONObject(inputJsonStr);
				
				logger.debug("permissionData is: " + permissionData);
				permissionData = AuthUtil.toUpperCaseKey(permissionData);
				permission = permissionData.getJSONArray(ResourceConstant.PERMISSIONS);
				resourceType = permissionData.getString(ResourceConstant.RESOURCE_TYPE);
			} catch (JSONException exp) {
				throw new CreatePermissionInvalidRequestException(
						"bad json format/RESOURCETYPE,PERMISSIONS field is mandatory", exp, exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.addNewPermissionsOnResourceType(AuthUtil.jsonArrayToList(permission), resourceType);
			} catch (Exception exp) {
				throw new CreatePermissionInvalidRequestException("Empty field/RESOURCETYPE,PERMISSIONS already exist",
						exp, exp.getMessage(), 400);
			}
			logger.debug("permissions " + permission + " created for resource type " + resourceType);
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.CREATE_RESOURCE_TYPE_PERMISSION);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
