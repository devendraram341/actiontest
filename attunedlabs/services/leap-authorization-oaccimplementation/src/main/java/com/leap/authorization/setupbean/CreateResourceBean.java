package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
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
 * This bean is used to create resource for a specific resourceType.
 * 
 * @author Getusleap
 *
 */
public class CreateResourceBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the resource, resourceType and tenant name from the
	 * exchange and creating the resource and returning the response according to
	 * the values.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..createResource service bean");
		try {
			Message message = exchange.getIn();
			String resource = null;
			String tenant = null;
			String resourceClass = null;
			try {
//				JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceData = new JSONObject(inputJsonStr);
				logger.debug("itemData is: " + resourceData);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
				resource = resourceData.getString(ResourceConstant.RESOURCE);
				// tenant = AuthUtil.tenantProp.getProperty(BASE_TENANT);
				tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
				resourceClass = resourceData.getString(ResourceConstant.RESOURCE_TYPE);
			} catch (JSONException exp) {
				throw new CreateItemInvalidRequestException("bad json format/RESOURCE,RESOURCETYPE field is mandatory",
						exp, exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.addNewResource(resource, resourceClass, tenant);
			} catch (Exception exp) {
				throw new CreateItemInvalidRequestException(
						"Empty field/RESOURCE already exist/TENANT,RESOURCETYPE does not exist", exp, exp.getMessage(),
						400);
			}
			logger.debug("resources " + resource + " for " + resourceClass + " created successfully");
			AuthUtil.setResponse(message, 200, ServiceTypeConstant.CREATE_RESOURCE);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
