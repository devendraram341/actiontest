package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.GrantingInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

public class DeleteUserBean extends AbstractMetaModelBean {
	
	private static final Logger logger = LoggerFactory.getLogger(DeleteUserBean.class);

	@Override
	protected void processBean(Exchange exchange) throws Exception {
		
		logger.debug("inside delete service bean");
		Message message = exchange.getIn();
		String userFromRequest = null;
		String tenant= null;
		try {
//			JSONObject request = new JSONObject(message.getBody(String.class).trim());
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject request = new JSONObject(inputJsonStr);
			logger.debug("userRoleData is: " + request);
			request = AuthUtil.toUpperCaseKey(request);
			userFromRequest = request.getString(ResourceConstant.USER);
			tenant = request.getString(ResourceConstant.TENANT_NAME);
		} catch (JSONException exp) {
			throw new GrantingInvalidRequestException("bad json format/USER,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}
		
		IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
		try {
			service.deleteUser(userFromRequest, tenant);
			
		} catch (Exception exp) {
			throw new GrantingInvalidRequestException("Empty field/USER, TENANT does not exist", exp,
					exp.getMessage(), 400);
		}
		AuthUtil.setResponse(message, 200, ServiceTypeConstant.REVOKE_RESOURCE_FROM_USER);
		
	}


}
