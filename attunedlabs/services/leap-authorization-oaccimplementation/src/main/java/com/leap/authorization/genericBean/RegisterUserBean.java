package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.CreateUserInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is creating a new user.
 * 
 * @author Getusleap
 *
 */
public class RegisterUserBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the user name and tenant name from the message
	 * exchange and returning the response.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..registerUser service bean");
		try{String userName = null;
		String tenant = null;
		String password = null;
		Boolean isPasswordEnabled = false;
		User user = new User();
		Message message = exchange.getIn();
		try {
//			JSONObject usersData = new JSONObject(message.getBody(String.class).trim());
			LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject usersData = new JSONObject(inputJsonStr);
			message.setHeader(AuthConstants.REQUEST_DATA, usersData);

			usersData = AuthUtil.toUpperCaseKey(usersData);
			userName = usersData.getString((ResourceConstant.USER));
			tenant = usersData.getString(ResourceConstant.TENANT_NAME);
			isPasswordEnabled = usersData.getBoolean(ResourceConstant.PESSWORD_ENABLED);
			if (isPasswordEnabled) {
				password = usersData.getString(ResourceConstant.PASSWORD);
				user.setPassword(password.toCharArray());
			}
		} catch (JSONException exp) {
			throw new CreateUserInvalidRequestException(
					"bad json format/USER,TENANT,PASSWORDENABLED,PASSWORD field is mandatory", exp, exp.getMessage(),
					400);
		}
		IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
		user.setUserName(userName);
		try {
			service.registerUser(user, tenant, isPasswordEnabled);
		} catch (Exception exp) {
			throw new CreateUserInvalidRequestException("User Already Exist", exp,
					exp.getMessage(), 400);
		}
		logger.debug("created user " + userName + " for tenant " + tenant);
		AuthUtil.setResponse(message, 200,ServiceTypeConstant.REGISTER_USER);
	} catch (NullPointerException e) {
		throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
	}
	}// ..end of the method
}// ..end of the bean
