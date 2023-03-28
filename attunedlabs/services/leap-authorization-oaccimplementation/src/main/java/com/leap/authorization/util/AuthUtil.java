package com.leap.authorization.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.camel.Message;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.leap.authorization.service.User;
import com.leap.authorization.setupbean.CreateResourcesBean;

/**
 * It is the util class for authorization.
 * 
 * @author Bizruntime
 *
 */
public class AuthUtil {

	private static final String DATA_LOWER_CASE = "data";
	private static final String DATA_UPPER_CASE = "DATA";
//	private static Properties props = new Properties();
//	private static Properties responseProps = new Properties();
	private static Properties serviceNameProps = new Properties();

	private static final String BASE_TENANT_FILE = "baseTenant.properties";
	public static Properties tenantProp;

//	static {
//		tenantProp = new Properties();
//		try {
//			tenantProp.load(CreateResourcesBean.class.getClassLoader().getResourceAsStream(BASE_TENANT_FILE));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	static {
		try {
//			props.load(AuthUtil.class.getClassLoader().getResourceAsStream(ResponseConstant.RESPONSE_CODE_FILE));
//			responseProps
//					.load(AuthUtil.class.getClassLoader().getResourceAsStream(ResponseConstant.RESPONSE_MESSAGE_FILE));
			serviceNameProps
					.load(AuthUtil.class.getClassLoader().getResourceAsStream(ResponseConstant.SERRVICE_TYPE_FILE));
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * Utility to set success response.
	 * 
	 * @param message
	 * @param resCode
	 * @throws JSONException
	 */
	public static void setResponse(Message message, Integer resCode, String serviceType) throws JSONException {
		JSONObject response = new JSONObject();
		response.put(ResponseConstant.MESSAGE, serviceNameProps.getProperty(serviceType));
		if (resCode.intValue() == 200)
			response.put(ResponseConstant.SUCCESS, true);
		else
			response.put(ResponseConstant.SUCCESS, false);

		message.setBody(response);
	}// ..end of the method

	/**
	 * Utility to convert the json key into upper case.
	 * 
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject toUpperCaseKey(JSONObject data) throws JSONException {
		JSONObject dataWithUpperKey = new JSONObject();
		Iterator<String> keyIterator = data.keys();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			dataWithUpperKey.put(key.toUpperCase(), data.get(key));
		}
		return dataWithUpperKey;
	}// ..end of the method

	/**
	 * Utility to check the jsonObject has data as key
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static boolean hasDataKey(JSONObject jsonObject) {
		return jsonObject.has(DATA_LOWER_CASE) || jsonObject.has(DATA_UPPER_CASE);
	}// ..end of the method

	/**
	 * Utility to convert JSON array into list.
	 * 
	 * @throws JSONException
	 */
	public static List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
		List<String> list = new ArrayList<>();
		for (int arrayIterator = 0; arrayIterator < jsonArray.length(); arrayIterator++) {
			list.add(jsonArray.get(arrayIterator).toString());
		}
		return list;
	}// ..end of the method

	/**
	 * Utility to convert JSON array into set.
	 * 
	 * @throws JSONException
	 */
	public static Set<String> jsonArrayToSet(JSONArray jsonArray) throws JSONException {
		Set<String> set = new HashSet<>();
		for (int arrayIterator = 0; arrayIterator < jsonArray.length(); arrayIterator++) {
			set.add(jsonArray.get(arrayIterator).toString());
		}
		return set;
	}// ..end of the method

	/**
	 * Utility to convert userWithPassword jsonArray to list of User
	 * 
	 * @param users
	 * @param isPasswordEnabled
	 * @return
	 * @throws JSONException
	 */
	public static List<User> jsonArrayToList(JSONArray users, Boolean isPasswordEnabled) throws JSONException {
		List<User> list = new ArrayList<>();
		for (int arrayIterator = 0; arrayIterator < users.length(); arrayIterator++) {
			String[] userAndPassword = users.get(arrayIterator).toString().split(":");
			User user = new User();
			user.setUserName(userAndPassword[0]);
			if (isPasswordEnabled) {
				user.setPassword(userAndPassword[1].toCharArray());
			}
			list.add(user);
		}
		return list;
	}

	/**
	 * This method is used to update the payload data with required authz values
	 * 
	 * @param base64PayloadEncodedData
	 *            :: String base64PayloadEncodedData
	 * @param data
	 * @return
	 */
	public static String updateJWTTokenValueWithAuthzValue(String base64PayloadEncodedData, JSONObject data) {
		byte[] payloadDecodeBase64Data = Base64.decodeBase64(base64PayloadEncodedData.getBytes());
		JSONObject payloadJsonObject = new JSONObject(new String(payloadDecodeBase64Data));
		System.out.println("payloadJsonObject :: " + payloadJsonObject);
		JSONObject userDataJsonObj = payloadJsonObject.getJSONObject("userData");
		userDataJsonObj.put(ResourceConstant.ROLES, data.get(ResourceConstant.ROLES));
		payloadJsonObject.put("userData", userDataJsonObj);
		byte[] payloadData = payloadJsonObject.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		return payloadDatabase64Encoded;

	}// ..end of method updateJWTTokenValueWithAuthzValue

	/**
	 * Utility for decoding special characters.
	 * 
	 * @param source
	 * @return decodedSource
	 */
	public static String decodeParameter(String source) {
		source = OaccAuthUtil.decoder(source, "%40", "@");
		source = OaccAuthUtil.decoder(source, "%2D", "-");
		source = OaccAuthUtil.decoder(source, "%5F", "_");
		source = OaccAuthUtil.decoder(source, "%27", "'");

		source = OaccAuthUtil.decoder(source, "%26", "&");
		source = OaccAuthUtil.decoder(source, "%2C", ",");
		source = OaccAuthUtil.decoder(source, "%22", "\"");
		source = OaccAuthUtil.decoder(source, "%2E", ".");
		source = OaccAuthUtil.decoder(source, "%20", " ");
		return source;
	}
}
