package com.leap.authorization.menubean;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.GetResourceInvalidRequestException;
import com.leap.authorization.exception.ResourceAccessException;
import com.leap.authorization.service.IMenuAuthorizationService;
import com.leap.authorization.service.oacc.impl.MenuAuthorizationServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to get all the menu items which is accessible for the role.
 * 
 * @author Bizruntime
 *
 */
public class GetAllMenuItemsForRoleBean extends AbstractMetaModelBean {

	/**
	 * This method is getting the role and tenant name. According the values
	 * this method is returning all resources in the response.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..getAllAccessibleMenuItemsForRole service bean");
		Message message = exchange.getIn();
		String role = null;
		String tenant = null;
		try {
//			JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			role = resourceData.getString(ResourceConstant.ROLE);
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			
			role = AuthUtil.decodeParameter(role);
			tenant = AuthUtil.decodeParameter(tenant);
		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException("bad json format/ROLE,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}
		IMenuAuthorizationService service = new MenuAuthorizationServiceImpl();
		List<String> resourceList = null;
		try {
			resourceList = service.getAllMenuItemsAccessibleforRole(role, tenant);
		} catch (ResourceAccessException exp) {
			throw new GetResourceInvalidRequestException("empty field/ROLE,TENANT does not exist", exp,
					exp.getMessage(), 400);
		}
		JSONObject data = new JSONObject();
		data.put(ResourceConstant.RESOURCES, resourceList);
		message.setBody(data);
	}// ..end of the method
}// ..end of the bean
