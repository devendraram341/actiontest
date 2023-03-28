package com.leap.authorization.menubean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.AssertPermissionInvalidRequestException;
import com.leap.authorization.exception.ResourceAuthorizeException;
import com.leap.authorization.service.IMenuAuthorizationService;
import com.leap.authorization.service.oacc.impl.MenuAuthorizationServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to check that user is having permission on menu item or
 * not.
 * 
 * @author Bizruntime
 */
public class HasPermissionForMenuItemBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the user, menu item name,permissionType from the
	 * message exchange and returning the boolean result.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..hasMenuItemUserPermission service bean");
		Message message = exchange.getIn();
		String user = null;
		String menuItem = null;
		String permissionType = null;
		String tenant = null;
		try {
//			JSONObject assertData = new JSONObject(message.getBody(String.class).trim());
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
			user = assertData.getString(ResourceConstant.USER);
			menuItem = assertData.getString(ResourceConstant.MENU_ITEM);
			tenant = assertData.getString(ResourceConstant.TENANT_NAME);
			permissionType = assertData.getString(ResourceConstant.PERMISSION);

			user = AuthUtil.decodeParameter(user);
			menuItem = AuthUtil.decodeParameter(menuItem);
			permissionType = AuthUtil.decodeParameter(permissionType);
			tenant = AuthUtil.decodeParameter(tenant);
		} catch (JSONException exp) {
			throw new AssertPermissionInvalidRequestException(
					"bad json format/USER,MENUITEM and PERMISSION field is mandatory", exp, exp.getMessage(), 403);
		}
		Boolean hasPermission;
		IMenuAuthorizationService service = new MenuAuthorizationServiceImpl();
		try {
			hasPermission = service.hasPermissionForUser(user, menuItem, permissionType, tenant);
		} catch (ResourceAuthorizeException exp) {
			throw new AssertPermissionInvalidRequestException("empty field/USER,MENUITEM and PERMISSION does not exist",
					exp, exp.getMessage(), 403);
		}
		if (!hasPermission) {
			throw new AssertPermissionInvalidRequestException("user is not authorized", null,
					"user is not authorized for " + menuItem, 403);
		}
		JSONObject data = new JSONObject();
		data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
		message.setBody(data);
	}// ..end of the method
}// ..end of the bean
