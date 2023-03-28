package com.leap.authorization.setupbean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.AuthorizationSetupException;
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
public class CreateChildTenantBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the tenant name from the message exchange and
	 * creating a new tenant. If tenant data is incorrect then it is throwing an
	 * exception.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside..createTenant service bean");
	try{	String tenantName = null;
		String parentTenant = null;
		Message message = exchange.getIn();
		try {
//			JSONObject tenantData = new JSONObject(message.getBody(String.class).trim());
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
			JSONObject tenantData = new JSONObject(inputJsonStr);
			logger.debug("tenantData is: " + tenantData);
			tenantData = AuthUtil.toUpperCaseKey(tenantData);
			tenantName = tenantData.getString(ResourceConstant.TENANT_NAME);
			parentTenant = tenantData.getString(ResourceConstant.PARENT_TENANT);
		} catch (JSONException exp) {
			throw new CreateTenantInvalidRequestException("TENANT/PARENT field is mandatory", exp, exp.getMessage(),
					400);
		}
		logger.debug("tenant name is: " + tenantName);
		IAuthorizationSetup service = new AuthorizationSetupImpl();
		try {
			service.addNewTenant(tenantName, parentTenant);
		} catch (AuthorizationSetupException exp) {
			exp.printStackTrace();
			throw new CreateTenantInvalidRequestException("empty or existing tenant/PARENT tenant not found", exp,
					exp.getMessage(), 400);
		}
		logger.debug("created tenant " + tenantName);
		AuthUtil.setResponse(message, 200,ServiceTypeConstant.CREATE_TENANT);
	} catch (NullPointerException e) {
		throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
	}
	}// ..end of the method
}// ..end of the bean
