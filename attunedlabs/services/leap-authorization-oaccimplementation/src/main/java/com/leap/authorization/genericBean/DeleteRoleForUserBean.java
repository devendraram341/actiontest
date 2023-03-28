package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.GrantingInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

public class DeleteRoleForUserBean implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(DeleteRoleForUserBean.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		logger.debug("inside ..associateRoleToUser service bean");
		try {
			String userExtId = null;
			String roleToInherit = null;
			String tenant;
			String roleAlreadyAssociated = null;
			Message message = exchange.getIn();
			try {
//				JSONObject userRoleData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject userRoleData = new JSONObject(inputJsonStr);
				message.setHeader(AuthConstants.REQUEST_DATA, userRoleData);

				logger.debug("userRoleData is: " + userRoleData);
				userRoleData = AuthUtil.toUpperCaseKey(userRoleData);
				userExtId = userRoleData.getString(ResourceConstant.USER);
				roleToInherit = userRoleData.getString(ResourceConstant.ROLE);
				tenant = userRoleData.getString(ResourceConstant.TENANT_NAME);
				roleAlreadyAssociated = userRoleData.getString(ResourceConstant.ROLE_ALREADY_ASSOCIATED);
			} catch (JSONException exp) {
				throw new GrantingInvalidRequestException("bad json format/USER, ROLE,TENANT field is mandatory", exp,
						exp.getMessage(), 400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			User user = new User();
			user.setUserName(userExtId);
			try {
				service.deleteRoleForUser(user, roleAlreadyAssociated, roleToInherit, tenant);
			} catch (Exception exp) {
				throw new GrantingInvalidRequestException("Empty field/USER, ROLE,TENANT does not exist", exp,
						exp.getMessage(), 400);
			}
			logger.debug("role " + roleToInherit + " is associated successfully to " + userExtId);
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.ASSOCIATE_ROLE_TO_USER);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
		
		
	}

}
