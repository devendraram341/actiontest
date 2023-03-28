package com.leap.authorization.genericBean;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.GetResourceInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to get all the resources which is accessible for the role.
 * 
 * @author Getusleap
 *
 */
public class GetAllResourcesForRoleBean extends AbstractMetaModelBean {

	/**
	 * This method is getting the role, resource on whi ch role want to access,
	 * tenant name, and permission. According the values this method is
	 * returning all resources in the response.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..getAllRoleAccessibleResource service bean");
		try {
			Message message = exchange.getIn();
			String role = null;
			String resourceType = null;
			String tenant = null;
			JSONArray permissions = null;
			try {
//				JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
				
				LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceData = new JSONObject(inputJsonStr);
				logger.debug("resourceData is " + resourceData);
				if (AuthUtil.hasDataKey(resourceData)) {
					JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
					resourceData = (JSONObject) jsonArray.get(0);
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				} else {
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				}
				role = resourceData.getString(ResourceConstant.ROLE);
				resourceType = resourceData.getString(ResourceConstant.RESOURCETYPE);
				tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
				permissions = resourceData.getJSONArray(ResourceConstant.PERMISSIONS);
				
				role = AuthUtil.decodeParameter(role);
				resourceType = AuthUtil.decodeParameter(resourceType);
				tenant = AuthUtil.decodeParameter(tenant);
			} catch (JSONException exp) {
				throw new GetResourceInvalidRequestException(
						"bad json format/ROLE,RESOURCETYPE,TENANT,PERMISSIONS field is mandatory", exp,
						exp.getMessage(), 400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			List<String> resourceList = null;
			try {
				resourceList = service.getAllRolePermittedResource(resourceType, role, tenant,
						AuthUtil.jsonArrayToList(permissions));
			} catch (Exception exp) {
				throw new GetResourceInvalidRequestException("Resource Doesn't Exist", exp, exp.getMessage(), 400);
			}
			JSONObject data = new JSONObject();
			data.put(ResourceConstant.RESOURCES, resourceList);
			message.setBody(data);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
