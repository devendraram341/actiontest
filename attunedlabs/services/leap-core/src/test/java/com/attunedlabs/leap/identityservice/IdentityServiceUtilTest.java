package com.attunedlabs.leap.identityservice;

import java.util.Base64;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static com.attunedlabs.LeapCoreTestConstant.*;

public class IdentityServiceUtilTest {

	private String validJWTToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZXN0Ijp7ImRhdGEiOiJkZW1vIn19.aAkD-9CYqpuFOmGJ7Y9j_eCVUuyXvHJH4ujbTGMiOFI";
	private String invalidJWTToken = "eyJ0eXAiOiJI1NiJ9.eyJ0ZX19.aAkD-9CYqpMiOFI";

	/**
	 * This method is used to get the tenant and access token info from valid jwt
	 * token
	 */
	@Test
	public void testGetTenantAndAccessTokenDetailsFromValidJWT() {
		Map<String, Object> tenantAndAccessToken = IdentityServiceUtil
				.getTenantAndAccessTokenDetailsFromJWT(validJWTToken);
		Assert.assertNotNull("Tenat And Access token  should not be null ::", tenantAndAccessToken);

		Object headerData = tenantAndAccessToken.get("headerData");
		Assert.assertNotNull("HeaderData Should not be null ::", headerData);
		Assert.assertEquals("Header Data should be same as expected data ::", "{\"typ\":\"JWT\",\"alg\":\"HS256\"}",
				headerData.toString());
		Assert.assertEquals("Header Data Instanse should be same as JsonObject ::", JSONObject.class,
				headerData.getClass());

		Object payloadData = tenantAndAccessToken.get("payloadData");
		Assert.assertNotNull("payload Data Should not be null ::", payloadData);
		Assert.assertEquals("payloadData Should be same as expected data ::", DEMO_JSON_DATA, payloadData.toString());
		Assert.assertEquals("Payload Data Instanse should be same as JsonObject ::", JSONObject.class,
				payloadData.getClass());
	}

	/**
	 * 
	 * This method is used to get the tenant and access token info from invalid jwt
	 * token
	 */

	@Test(expected = JSONException.class)
	public void testGetTenantAndAccessTokenDetailsFromInvalidJWT() {
		IdentityServiceUtil.getTenantAndAccessTokenDetailsFromJWT(invalidJWTToken);
	}

	/**
	 * This method is used to get each and every part of valid JWT token encoded.
	 */
	@Test
	public void testGetEncodedFormatDeatilsFromJWT() {
		Map<String, Object> encodedFormatDeatilsFromJWT = IdentityServiceUtil
				.getEncodedFormatDeatilsFromJWT(validJWTToken);
		Assert.assertNotNull("encodedFormat should not be null ::", encodedFormatDeatilsFromJWT);

		Object headerData = encodedFormatDeatilsFromJWT.get("headerData");
		Assert.assertNotNull("Header Data should not be null ::", headerData);
		Assert.assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9", headerData);

		String decodedHeaderData = getDecodedData(headerData);
		Assert.assertNotNull("decoded data should not be null ::", decodedHeaderData);
		Assert.assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS256\"}", decodedHeaderData);

		Object payloadData = encodedFormatDeatilsFromJWT.get("payloadData");
		Assert.assertNotNull("payloadData should not be null ::", payloadData);
		Assert.assertEquals("eyJ0ZXN0Ijp7ImRhdGEiOiJkZW1vIn19", payloadData);

		String decodedPayloadDataData = getDecodedData(payloadData);
		Assert.assertNotNull("decodedPayloadDataData should not be null ::", decodedPayloadDataData);
		Assert.assertEquals(DEMO_JSON_DATA, decodedPayloadDataData);

		Object securityData = encodedFormatDeatilsFromJWT.get("securityData");
		Assert.assertNotNull("securityData should not be null ::", securityData);
		Assert.assertEquals("aAkD-9CYqpuFOmGJ7Y9j_eCVUuyXvHJH4ujbTGMiOFI", securityData);

	}

	/**
	 * This method is used to get each and every part of invalid JWT token encoded to try to
	 * decode.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetEncodedFormatDeatilsFromInvalidJWT() {
		Map<String, Object> encodedFormatDeatilsFromJWT = IdentityServiceUtil
				.getEncodedFormatDeatilsFromJWT(invalidJWTToken);
		Assert.assertNotNull("encodedFormat should not be null ::", encodedFormatDeatilsFromJWT);

		Object headerData = encodedFormatDeatilsFromJWT.get("headerData");
		Assert.assertNotNull("Header Data should not be null ::", headerData);
		Assert.assertEquals("eyJ0eXAiOiJI1NiJ9", headerData);

		getDecodedData(headerData);

	}

	private String getDecodedData(Object data) {
		byte[] decode = Base64.getDecoder().decode(data.toString());
		return new String(decode);
	}

}
