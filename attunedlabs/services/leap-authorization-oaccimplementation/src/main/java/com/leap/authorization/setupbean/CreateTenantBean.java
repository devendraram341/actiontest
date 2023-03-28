package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.oacc.impl.AuthorizationSetupImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ServiceTypeConstant;

/**
 * This bean is creating a new tenant.
 * 
 * @author Getusleap
 *
 */
public class CreateTenantBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the tenant name from the message exchange and
	 * creating a new tenant. If tenant data is incorrect then it is throwing an
	 * exception.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside..createTenant service bean");
		try {
			String tenantName = null;
			Message message = exchange.getIn();
			try {
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
//				JSONObject tenantData = new JSONObject(message.getBody(String.class).trim());
				JSONObject tenantData = new JSONObject(inputJsonStr);
				logger.debug("tenantData is: " + tenantData);
				tenantData = AuthUtil.toUpperCaseKey(tenantData);
				tenantName = tenantData.getString(ResourceConstant.TENANT_NAME);
			} catch (JSONException exp) {
				throw new CreateTenantInvalidRequestException("TENANT field is mandatory", exp, exp.getMessage(), 400);
			}	
			logger.debug("tenant name is: " + tenantName);
			IAuthorizationSetup service = new AuthorizationSetupImpl();
			try {
				service.addNewTenant(tenantName);
			} catch (Exception exp) {
				throw new CreateTenantInvalidRequestException("empty or existing tenant", exp, exp.getMessage(), 400);
			}
			logger.debug("created tenant " + tenantName);
			AuthUtil.setResponse(message, 200,ServiceTypeConstant.CREATE_TENANT);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
