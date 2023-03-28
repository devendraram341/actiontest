package com.leap.authorization.genericBean;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.attunedlabs.eventframework.abstractbean.AbstractMetaModelBean;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.identityservice.IdentityServiceConstant;
import com.attunedlabs.leap.identityservice.IdentityServiceUtil;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.leap.authorization.exception.CreateTenantInvalidRequestException;
import com.leap.authorization.exception.GetResourceInvalidRequestException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.AuthorizationGenericServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

/**
 * This bean is used to get all the roles assigned for the user.
 * 
 * @author Getusleap
 *
 */
public class GetRolesForUser extends AbstractMetaModelBean {

	/**
	 * This method is getting the user name on which user want to roles assigned
	 * for user.
	 */
	@Override
	protected void processBean(Exchange exchange) throws Exception {
		logger.debug("inside ..GetRolesForUser bean ");
		try {
			Message message = exchange.getIn();
			String username = null;
			String tenant = null;

			try {
//				JSONObject resourceData = new JSONObject(message.getBody(String.class).trim());
				LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
				String inputJsonStr = leapDataContext.getInitialRequestData().getData().toString();
				JSONObject resourceData = new JSONObject(inputJsonStr);
				logger.debug("resourceData is " + resourceData);
				if (AuthUtil.hasDataKey(resourceData)) {
					JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
					resourceData = (JSONObject) jsonArray.get(0);
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				} else {
					resourceData = AuthUtil.toUpperCaseKey(resourceData);
				}
				logger.debug("resourceData is after changing.." + resourceData);
				username = resourceData.getString(ResourceConstant.USER);
				tenant = resourceData.getString(ResourceConstant.TENANT_NAME);

				username = AuthUtil.decodeParameter(username);
				tenant = AuthUtil.decodeParameter(tenant);
			} catch (JSONException exp) {
				throw new GetResourceInvalidRequestException("bad json format/USER/TENANT field is mandatory", exp,
						exp.getMessage(), 400);
			}
			IAuthorizationGenericService service = new AuthorizationGenericServiceImpl();
			User user = new User();
			List<String> resourceList = null;
			user.setUserName(username);
			try {
				logger.debug("calling the service implementation is after changing..");
				resourceList = service.getRolesAssignedForUser(user, tenant);
			} catch (Exception exp) {
				throw new GetResourceInvalidRequestException("empty field/USER/TENANT does not exist", exp,
						exp.getMessage(), 400);
			}
			JSONObject data = new JSONObject();
			if (resourceList == null || resourceList.isEmpty())
				data.put(ResourceConstant.ROLES, "no roles assigned yet!");
			else
				data.put(ResourceConstant.ROLES, resourceList);
			message.setBody(data);
			updateJWTValue(message, data);

		} catch (NullPointerException e) {
			throw new CreateTenantInvalidRequestException("Empty values requested!", e, "Empty values requested!", 400);
		}

	}// ..end

	/**
	 * This method is used to update the JWT token value
	 * 
	 * @param message
	 * @param data
	 */
	private void updateJWTValue(Message message, JSONObject data) {
		Object isAuthzEnableObj = message.getHeader(IdentityServiceConstant.IS_AUTHZ_ENABLED);
		if (isAuthzEnableObj != null) {
			Boolean isAuthzEnable = Boolean
					.valueOf(message.getHeader(IdentityServiceConstant.IS_AUTHZ_ENABLED, String.class));
			if (isAuthzEnable) {
				Object jwtTokenObj = message.getHeader(IdentityServiceConstant.JWT_TOKEN_KEY);
				if (jwtTokenObj != null) {
					String jwtTokenValueFromHeader = message.getHeader(IdentityServiceConstant.JWT_TOKEN_KEY,
							String.class);
					Map<String, Object> jwtTokenEncodedDataMapValues = IdentityServiceUtil
							.getEncodedFormatDeatilsFromJWT(jwtTokenValueFromHeader);
					String payloadEncodedData = (String) jwtTokenEncodedDataMapValues
							.get(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY);
					logger.debug("payloadEncodedData :: " + payloadEncodedData);
					String updatedJWTToken = AuthUtil.updateJWTTokenValueWithAuthzValue(payloadEncodedData, data);
					String jwtTokenVal = jwtTokenEncodedDataMapValues.get(IdentityServiceConstant.JWT_HEADER_DATA_KEY)
							+ "." + updatedJWTToken + "."
							+ jwtTokenEncodedDataMapValues.get(IdentityServiceConstant.JWT_SECURITY_DATA_KEY);
					message.setHeader(IdentityServiceConstant.JWT_TOKEN_KEY, jwtTokenVal);
				} // ..end of if condition checking the jwt token value is
					// not
					// null
					// or not
			} // ..end of if method , to check isAuthzEnable is true
		} // ..end of if method to check the isAuthzEnable is null or not
	}// ..end of updateJWTValue method

}
