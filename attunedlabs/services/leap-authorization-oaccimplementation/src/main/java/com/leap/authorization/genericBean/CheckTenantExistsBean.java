package com.leap.authorization.genericBean;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.AssertResourceAvailabilityException;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to check that tenant exists or not.
 * 
 * @author Getusleap
 */
public class CheckTenantExistsBean extends AbstractMetaModelBean {

	/**
	 * This method is taking the tenant from the message exchange and returning
	 * the boolean result.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..checkTenantAvailable service bean");
		try {
			Message message = exchange.getIn();
			String tenant = null;
			try {
//				JSONObject assertData = new JSONObject(message.getBody(String.class).trim());
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
				tenant = assertData.getString(ResourceConstant.TENANT_NAME);
				tenant = AuthUtil.decodeParameter(tenant);

			} catch (JSONException exp) {
				throw new AssertResourceAvailabilityException("bad json format/TENANT field is mandatory", exp,
						exp.getMessage(), 403);
			}
			Boolean isTenantAvailable;
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			try {
				isTenantAvailable = service.tenantExists(tenant);
			} catch (Exception exp) {
				throw new AssertResourceAvailabilityException("empty field/TENANT does not exist", exp,
						exp.getMessage(), 403);
			}
			JSONObject data = new JSONObject();
			data.put(ResourceConstant.TENANT_EXISTS, isTenantAvailable);
			message.setBody(data);
		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}
	}// ..end of the method
}// ..end of the bean
