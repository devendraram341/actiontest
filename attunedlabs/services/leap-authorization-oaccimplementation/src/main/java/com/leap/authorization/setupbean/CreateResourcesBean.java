package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateItemInvalidRequestException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to create resources for a specific resourceType.
 * 
 * @author Getusleap
 *
 */
public class CreateResourcesBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the resources, resourceType and tenant name from
	 * the exchange and creating the resources and returning the response
	 * according to the values.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..createResources service bean");
		try {
			Message message = exchange.getIn();
			JSONArray resource = null;
			String tenant = null;
			String resourceClass = null;
			try {
//				JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceData = new JSONObject(inputJsonStr);
				logger.debug("resourceData is: " + resourceData);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
				resource = resourceData.getJSONArray(ResourceConstant.RESOURCES);
				// tenant = AuthUtil.tenantProp.getProperty(BASE_TENANT);
				tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
				resourceClass = resourceData.getString(ResourceConstant.RESOURCE_TYPE);
			} catch (JSONException exp) {
				throw new CreateItemInvalidRequestException("bad json format/RESOURCES,RESOURCETYPE field is mandatory",
						exp, exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.addNewResources(AuthUtil.jsonArrayToList(resource), resourceClass, tenant);
			} catch (Exception exp) {
				throw new CreateItemInvalidRequestException(
						"Empty field/RESOURCES already exist/TENANT,RESOURCETYPE does not exist", exp, exp.getMessage(),
						400);
			}
			logger.debug("resources " + resource + " for " + resourceClass + " created successfully");
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.CREATE_RESOURCES);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
