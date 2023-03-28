package com.leap.authorization.menubean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateItemInvalidRequestException;
import com.leap.authorization.exception.ResourceRegistryException;
import com.leap.authorization.service.IMenuAuthorizationService;
import com.leap.authorization.service.oacc.impl.MenuAuthorizationServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is used to create menu items for a specific tenant.
 * 
 * @author Bizruntime
 *
 */
public class CreateMenuItmesBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the items and tenant name from the exchange and
	 * creating the items and returning the response according to the values.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..createMenuItems service bean");
		Message message = exchange.getIn();
		JSONArray items = null;
		String tenant = null;
		try {
//			JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is: " + resourceData);
			resourceData = AuthUtil.toUpperCaseKey(resourceData);
			items = resourceData.getJSONArray(ResourceConstant.MENU_ITEMS);
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
		} catch (JSONException exp) {
			throw new CreateItemInvalidRequestException("bad json format/MENUITEMS,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}
		IMenuAuthorizationService service = new MenuAuthorizationServiceImpl();
		try {
			service.addMenuItems(AuthUtil.jsonArrayToList(items), tenant);
		} catch (ResourceRegistryException exp) {
			throw new CreateItemInvalidRequestException("Empty field/ITEMS already exist/TENANT does not exist", exp,
					exp.getMessage(), 400);
		}
		logger.debug("items " + items + " created successfully");
		AuthUtil.setResponse(message, 200,ServiceTypeConstant.CREATE_MENU_ITEMS);
	}// ..end of the method
}// ..end of the bean
