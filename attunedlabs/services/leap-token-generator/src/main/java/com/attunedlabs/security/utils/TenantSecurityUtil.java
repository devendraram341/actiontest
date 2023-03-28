package com.attunedlabs.security.utils;

import static com.attunedlabs.security.TenantSecurityConstant.APPS_DEPLOYMENT_ENV_CONFIG;
import static com.attunedlabs.security.TenantSecurityConstant.BASE_CONFIG_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Properties;

import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.DBConfigurationException;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.exception.SecretKeyGenException;

public class TenantSecurityUtil {
	static Properties properties;

	private TenantSecurityUtil() {
	}

	/**
	 * 
	 * @return
	 * @throws DBConfigurationException
	 */
	public static Connection getDBConnection() {
		loadPropertyFile();
		try {
			return DriverManager.getConnection((properties.getProperty(TenantSecurityConstant.DB_URL)),
					properties.getProperty(TenantSecurityConstant.DB_USER),
					properties.getProperty(TenantSecurityConstant.DB_PASS));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}// ..end of the method

	/**
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(String source) {
		return (source == null) || (source.length() == 0);
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchProviderException
	 */
	public static String getSalt() throws SecretKeyGenException {
		SecureRandom ranGen;
		try {
			ranGen = SecureRandom.getInstance(TenantSecurityConstant.SEC_ALG, "SUN");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new SecretKeyGenException("Unable to generate secret key ! " + e.getMessage(), e);
		}
		byte[] aesKey = new byte[16];
		ranGen.nextBytes(aesKey);
		return new String(aesKey);
	}// ..end of the method

	/**
	 * 
	 * @param intTenantId
	 * @param siteId
	 * @param saltString
	 * @param interval
	 * @return
	 * @throws DigestMakeException
	 */
	public static String getMD5(String intTenantId, String siteId, String saltString, long expirationTime)
			throws DigestMakeException {
		if (isEmpty(intTenantId) || isEmpty(siteId) || isEmpty(saltString)) {
			throw new DigestMakeException("Unable to perform digest !, empty values requested! ");
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(TenantSecurityConstant.SEC_DIGEST);
		} catch (NoSuchAlgorithmException e) {
			throw new DigestMakeException("Unable to perform digest ! " + e.getMessage(), e);
		}
		md.update((intTenantId + siteId + saltString + expirationTime).getBytes());
		byte[] byteData = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		String tenantToken = sb.toString();
		return tenantToken;
	}// ..end of the method

	/**
	 * 
	 * @param intTenantId
	 * @param siteId
	 * @param saltString
	 * @param interval
	 * @return
	 * @throws DigestMakeException
	 */
	public static byte[] getMD5(String publicTenantId, String siteId) throws DigestMakeException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(TenantSecurityConstant.SEC_DIGEST);
		} catch (NoSuchAlgorithmException e) {
			throw new DigestMakeException("Unable to perform digest ! " + e.getMessage(), e);
		}
		md.update((publicTenantId + siteId).getBytes());
		byte[] byteData = md.digest();
		return byteData;
	}// ..end of the method

	/**
	 * 
	 * @param tenantToken
	 * @param constructedToken
	 * @return
	 */
	public static boolean isValid(String tenantToken, String constructedToken) {
		return constructedToken.trim().equals(tenantToken.trim());
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static long getCurrentEpoch() {
		return Instant.now().getEpochSecond();
	}// ..end of the method

	/**
	 * 
	 * @param expCountinSeconds
	 * @param currentGMT
	 * @return
	 */
	public static long getExpirationTime(int expCountInSeconds) {
		return (getCurrentEpoch() + expCountInSeconds);
	}// ..end of the method

	/**
	 * This method is used to close the opened {@link Connection} and
	 * {@link PreparedStatement}
	 * 
	 * @param conn
	 * @param preparedStatement
	 */
	public static void dbCleanUp(Connection conn, PreparedStatement preparedStatement) {
		try {
			if (preparedStatement != null)
				preparedStatement.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}// ..end of the method

	public static void loadPropertyFile() {
		if (properties == null) {
			properties = new Properties();
			try {
				// String getenv = System.getenv(ENV_VARIABLE);
				String configPath = System.getProperty(BASE_CONFIG_PATH);
				String configPathForGlobalApp = configPath + File.separator + APPS_DEPLOYMENT_ENV_CONFIG;
				FileInputStream fileInput = new FileInputStream(configPathForGlobalApp);
				properties.load(fileInput);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
