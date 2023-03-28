package com.attunedlabs.leap.identityservice;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author reactivworks
 * 
 *         This Class is used to handle the methods which has no state (fields)
 *         of its own, so all the methods can be class methods (static) rather
 *         than object methods (requiring an instantiation of an
 *         object),Provides methods for multiple other classes (shared code)
 *
 *
 */
public class IdentityServiceUtil {

	protected static Logger logger = LoggerFactory.getLogger(IdentityServiceUtil.class.getName());

	/**
	 * This method is used to get the tenant and access token info from jwt token
	 * 
	 * @param jwtToken :: String JwtToken
	 * @return {@link Map} :: Map Values
	 */
	public static Map<String, Object> getTenantAndAccessTokenDetailsFromJWT(String jwtToken) {
		String[] jwtTokenSplit = jwtToken.split("\\.");
		String headerBase64Data = jwtTokenSplit[0];
		String payloadBase64Data = jwtTokenSplit[1];
		Map<String, Object> decodeHashMap = new HashMap<>();
		byte[] byteHeaderDecodeData = Base64.decodeBase64(headerBase64Data);
		byte[] bytePayloadDecodeData = Base64.decodeBase64(payloadBase64Data);
		String headerDecodeData = new String(byteHeaderDecodeData);
		String payloadDecodeData = new String(bytePayloadDecodeData);
		logger.debug("{} headerDecodeData :: {}  payloadDecodeData :: {}", LEAP_LOG_KEY, headerDecodeData,
				payloadDecodeData);
		decodeHashMap.put(IdentityServiceConstant.JWT_HEADER_DATA_KEY, new JSONObject(headerDecodeData));
		decodeHashMap.put(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY, new JSONObject(payloadDecodeData));
		return decodeHashMap;
	}// ..end of method getTenantAndAccessTokenDetailsFromJWT

	/**
	 * This method is used to get each and every part of JWT token encoded from
	 * 
	 * @param jwtToken :: JWT Token Value
	 * @return {@link Map}
	 */
	public static Map<String, Object> getEncodedFormatDeatilsFromJWT(String jwtToken) {
		String[] jwtTokenSplit = jwtToken.split("\\.");
		String headerBase64Data = jwtTokenSplit[0];
		String payloadBase64Data = jwtTokenSplit[1];
		String securityBase64Data = jwtTokenSplit[2];
		Map<String, Object> encodeHashMap = new HashMap<>();
		encodeHashMap.put(IdentityServiceConstant.JWT_HEADER_DATA_KEY, headerBase64Data);
		encodeHashMap.put(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY, payloadBase64Data);
		encodeHashMap.put(IdentityServiceConstant.JWT_SECURITY_DATA_KEY, securityBase64Data);
		return encodeHashMap;
	}// ..end of method getEncodedFormatDeatilsFromJWT

}// ..end of class IdentityServiceUtil
