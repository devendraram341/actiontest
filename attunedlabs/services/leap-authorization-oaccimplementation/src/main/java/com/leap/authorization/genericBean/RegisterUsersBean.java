package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.CreateUserInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is creating multiple users.
 * 
 * @author Getusleap
 *
 */
public class RegisterUsersBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the users array and tenant name from the message
	 * exchange and returning the response.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..registerUsers service bean");
		try {
			JSONArray users = null;
			String tenant = null;
			Boolean isPasswordEnabled = false;
			Message message = exchange.getIn();
			try {
//				JSONObject usersData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject usersData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, usersData);
				
				usersData = AuthUtil.toUpperCaseKey(usersData);
				users = usersData.getJSONArray((ResourceConstant.USERS));
				tenant = usersData.getString(ResourceConstant.TENANT_NAME);
				isPasswordEnabled = usersData.getBoolean(ResourceConstant.PESSWORD_ENABLED);
			} catch (JSONException exp) {
				throw new CreateUserInvalidRequestException(
						"bad json format/USERS,TENANT,PASSWORDENABLED field is mandatory", exp, exp.getMessage(), 400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			try {
				service.registerUsers(AuthUtil.jsonArrayToList(users, isPasswordEnabled), tenant, isPasswordEnabled);
			} catch (Exception exp) {
				throw new CreateUserInvalidRequestException("empty field/USERS already exist/TENANT does not exist",
						exp, exp.getMessage(), 400);
			}
			logger.debug("created users " + users + " for tenant " + tenant);
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.REGISTER_USERS);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
