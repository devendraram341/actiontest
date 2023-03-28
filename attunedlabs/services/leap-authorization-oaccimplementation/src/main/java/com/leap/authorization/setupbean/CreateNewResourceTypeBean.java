package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateResourceClassInvalidRequest;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to create new resource type.
 * 
 * @author Getusleap
 *
 */
public class CreateNewResourceTypeBean extends AbstractMetaModelBean {

	/**
	 * This method is taking resource class name from the message exchange and
	 * validating the data. According to validation it is returning the response.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..createResourceType service bean");
		try {
			Message message = exchange.getIn();
			String resourceType = null;
			JSONArray resourceTypePermission = null;
			try {
//				JSONObject resourceTypeData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceTypeData = new JSONObject(inputJsonStr);
				logger.debug("resourceTypeData is: " + resourceTypeData);
				resourceTypeData = AuthUtil.toUpperCaseKey(resourceTypeData);
				resourceType = resourceTypeData.getString(ResourceConstant.RESOURCE_TYPE);
				resourceTypePermission = resourceTypeData.getJSONArray(ResourceConstant.PERMISSIONS);
			} catch (JSONException exp) {
				throw new CreateResourceClassInvalidRequest(
						"bad json format/RESOURCETYPE, PERMISSIONS field is mandatory", exp, exp.getMessage(), 400);
			}
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.addNewResourceTypeToAuthorize(resourceType, AuthUtil.jsonArrayToList(resourceTypePermission));
			} catch (Exception exp) {
				throw new CreateResourceClassInvalidRequest("empty fields/RESOURCETYPE,PERMISSIONS already exist", exp,
						exp.getMessage(), 400);
			}
			logger.debug("created resource type " + resourceType + " with permission " + resourceTypePermission);
			AuthUtil.setResponse(message, 200, ServiceTypeConstant.CREATE_RESOURCE_TYPE_PERMISSION);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
