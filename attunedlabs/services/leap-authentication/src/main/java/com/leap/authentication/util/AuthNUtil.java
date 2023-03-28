package com.leap.authentication.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONObject;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.restlet.Request;
import org.restlet.data.Cookie;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.util.DataSourceInstance;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.DigestMakeException;
import com.attunedlabs.security.utils.TenantSecurityUtil;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.exception.LogInFailureException;
import com.leap.authentication.exception.PropertiesConfigException;

public class AuthNUtil {

	private AuthNUtil() {
	}

	private static Logger logger = LoggerFactory.getLogger(AuthNUtil.class);
	private static Properties soConfigProperties = null;
	private static Properties idapConfigProperties = null;
	private static Properties basewso2ConfigProperties = null;
	public static String ADMIN_USER = null;
	public static String ADMIN_PASS = null;
	static final Pattern TAG_REGEX = Pattern.compile("<html>(.+?)</html>");
	private static Exchange exchange = null;

	/**
	 * This method is used to get the value of each attribute value from the
	 * Attributes object.
	 * 
	 * @param attrKey
	 * @param answer
	 * @return value of the attrkey.
	 */
	public static Object getEachAttribute(String attrKey, Attributes answer) {
		try {
			return answer.get(attrKey).get();
		} catch (NullPointerException | NamingException e) {
			return null;
		}
	}// ..end of the method

	/**
	 * This method is used to initialize the boot strap for the saml sso.
	 * 
	 * @throws ConfigurationException
	 * @throws SSOAgentException
	 */
	public static void initDefaultBootstrap(Properties properties) throws ConfigurationException, SSOAgentException {
		logger.debug(". initDefaultBootstrap method of  Key2actSecurityRequestBuilder ");
		SSOAgentConfigs.initConfig(properties);
		SSOAgentConfigs.initCheck();
		DefaultBootstrap.bootstrap();
	}// ..end of the method initDefaultBootstrap

	/**
	 * This method is used to check the source string is empty or contains a
	 * value.
	 * 
	 * @param source
	 * @return boolean value whether it is empty or non-empty.
	 */
	public static boolean isEmpty(String source) {
		return (source == null || source.isEmpty()) ? true : false;
	}// ..end of the method

	/**
	 * This method is used to disable the ssl trust validation.
	 * 
	 */
	public static void disablesslTrustValidation() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
	}// end of method disablesslTrustValidation

	/**
	 * In production we have to avoid these method
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static DefaultHttpClient httpsClientCertificateVerifier() {
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		DefaultHttpClient client = new DefaultHttpClient();

		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

		// Set verifier
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return httpClient;

	}

	/**
	 * This method is used to give the ldapcontext attribute in the form of map.
	 * 
	 * @return {@link Hashtable} of ldap values.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Hashtable getLdapContextAsMap() {
		Hashtable env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "Simple");
		env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
		env.put(Context.SECURITY_CREDENTIALS, AuthNConstants.ADMIN);
		env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
		env.put(Context.REFERRAL, "follow");
		return env;
	}// ..end of the method

	/**
	 * This method is used to get the ldap context.
	 * 
	 * @return {@link LdapContext}
	 */
	@SuppressWarnings("unchecked")
	public static LdapContext getLdapContext() {
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = getLdapContextAsMap();
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException nex) {
			nex.printStackTrace();
		}
		return ctx;
	}// ..end of the method

	/**
	 * This method is used to give the connection object.
	 * 
	 * @return {@link Connection}
	 */
	public static Connection getDbConnection() {
		Connection con = null;
		logger.debug("inside the getDbConnection object..." + con);
		try {
			/*
			 * Class.forName(dbProps.getProperty("DB_DRIVER_CLASS")); // if (con
			 * == null) { //
			 * logger.debug("inside the if block of getDbConnection object...");
			 * con = DriverManager.getConnection(dbProps.getProperty("DB_URL") +
			 * "?user=" + dbProps.getProperty("DB_USER") + "&password=" +
			 * dbProps.getProperty("DB_PASSWORD"));
			 */
			con = DataSourceInstance.getConnection();
			// }
		} catch (SQLException | IOException | com.attunedlabs.config.util.PropertiesConfigException e) {
			e.printStackTrace();
		}
		return con;
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
			if (conn != null) {
				conn.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}// ..end of the method

	/**
	 * This method is used to split the keys based on the uid.
	 * 
	 * @param stringToSplit
	 * @return string value
	 */
	public static String splitKeysByUid(String stringToSplit) {
		String[] afterMail = stringToSplit.split("uid=");
		String[] finalRes = afterMail[1].split(",ou=users");
		return finalRes[0];
	}// ..end of the splitKeysByUid method

	/**
	 * This method is used to get the HTML tag from the response XML String
	 * 
	 * @param str
	 *            : Response XML in string format
	 * @return list of string data
	 */
	public static List<String> getHTMLTagValue(String str) {
		final List<String> tagValues = new ArrayList<String>();
		final Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		return tagValues;
	}// end of method getHTMLTagValue

	/**
	 * This method is used to load the given properties file with the given
	 * fileName.
	 * 
	 * @return Properties for the respective fileName specified
	 * @throws PropertiesConfigException
	 */
	// public static Properties loadPropertiesFile() throws
	// PropertiesConfigException {
	// logger.debug(".loadPropertiesFile...");
	// InputStream inputStream =
	// AuthNUtil.class.getClassLoader().getResourceAsStream(fileName);
	// if (configproperties != null && !(configproperties.isEmpty())) {
	// return configproperties;
	//
	// } else {
	// throw new PropertiesConfigException(
	// "Could not load LDAP-CONFIG file " + AuthNConstants.CONFIG_PROPS + ",
	// hence
	// setting defaults:");
	// }
	// }// ..end of the method loadPropertiesFile

	/**
	 * This method is used to replace the source string with the given value to
	 * the new value.
	 * 
	 * @param source
	 * @param toReplaceFrom
	 * @param toReplaceTo
	 * 
	 * @return String of replaced value.
	 */
	public static String decoder(String source, String toReplaceFrom, String toReplaceTo) {
		return source.replace(toReplaceFrom, toReplaceTo);
	}// end of method decoder

	/**
	 * This method is used to validate username which must be in the format of
	 * an email.
	 * 
	 * @param user
	 * 
	 * @return boolean value of valid username or not.
	 */
	public static boolean validateUsername(String username) {
		String emailRegex = AuthNConstants.EMAIL_REGEX;
		Pattern pattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(username);
		return !matcher.matches();
	}// end of method validateUsername

	/**
	 * This method is used to validate password with minimum 5 characters.
	 * 
	 * @param user
	 * 
	 * @return boolean value of valid password or not.
	 */
	public static boolean validatePassword(String password) {
		return password.length() >= AuthNConstants.PASSWORD_MIN_LENGTH ? false : true;
	}// end of method validatePassword

	/**
	 * This method is used to decode the jwt Header and Paycode Base64Value
	 * 
	 * @param headerBase64Data
	 * @param payloadBase64Data
	 * @return {@link Map} DecodeHashMap
	 */
	public static Map<String, Object> decodeJwtHeaderAndPayloadData(String jwtToken) {
		Map<String, Object> decodeHashMap = new HashMap<>();
		String[] jwtTokenSplit = jwtToken.split("\\.");
		String headerBase64Data = jwtTokenSplit[0];
		String payloadBase64Data = jwtTokenSplit[1];
		byte[] byteHeaderDecodeData = Base64.decodeBase64(headerBase64Data);
		byte[] bytePayloadDecodeData = Base64.decodeBase64(payloadBase64Data);
		String headerDecodeData = new String(byteHeaderDecodeData);
		String payloadDecodeData = new String(bytePayloadDecodeData);
		logger.debug("headerDecodeData :: " + headerDecodeData + " payloadDecodeData :: " + payloadDecodeData);
		decodeHashMap.put("headerData", new JSONObject(headerDecodeData));
		decodeHashMap.put("payloadData", new JSONObject(payloadDecodeData));
		return decodeHashMap;
	}// ..end of method decodeJwtHeaderAndPayloadData

	/**
	 * This method is used to get the JWT Token From Cookie
	 * 
	 * @param exchange
	 *            :: {@link Exchange}
	 * @return String CookieValue
	 */
	public static String getJWTTokenFromCookie(Exchange exchange) {
		String cookieValue = null;
		Message message = exchange.getIn();
		Request requestObj = message.getHeader(LeapDataContextConstant.CAMEL_RESTLET_REQUEST, Request.class);
		Series<Cookie> requestCookies = requestObj.getCookies();
		for (int i = 0; i < requestCookies.size(); i++) {
			Cookie requestCookie = requestCookies.get(i);
			if (requestCookie.getName().equalsIgnoreCase(AuthNConstants.JWT_TOKEN)) {
				cookieValue = requestCookie.getValue();
			} // ..end of if condition

		} // ..end of for loop
		return cookieValue;
	}// ..end of method getJWTTokenFromCookie

	/**
	 * This method is used to create the JWT Token Structure and return JWT
	 * 
	 * @param messageBody
	 *            :: {@link Message}
	 * @param tokenMap
	 *            :: {@link Map}
	 * @param userName
	 *            :: String userName
	 * @return
	 * @throws LogInFailureException
	 * @throws DigestMakeException
	 */
	public static String createJWTTokenStructure(Message messageBody, Map<String, String> tokenMap, String userName)
			throws LogInFailureException, DigestMakeException {
		String tenantId;
		String siteId;
		try {
			tenantId = tokenMap.get(AuthNConstants.TENANT_ID);
			siteId = tokenMap.get(AuthNConstants.SITE_ID);
		} catch (Exception e) {
			throw new LogInFailureException("Tenant/Site Id's not configured in repository", e,
					"Tenant/Site Id's not configured in repository", 500);
		}
		JSONObject jwtHeaderJson = new JSONObject();
		JSONObject payloadDataJson = new JSONObject();
		JSONObject authDataJson = new JSONObject();
		JSONObject userDataJson = new JSONObject();
		JSONObject tenantSiteDataJson = new JSONObject();
		String tenantTokenFromHeader = messageBody.getHeader(TenantSecurityConstant.TENANT_TOKEN_LOGIN, String.class);
		String tenantTokenExpTimeFromHeader = messageBody
				.getHeader(TenantSecurityConstant.TENANT_TOKEN_EXPIRATION_TIME_LOGIN, String.class);
		logger.debug("tenantId :: " + tenantId + " siteId :: " + siteId + " tenantTokenFromHeader:: "
				+ tenantTokenFromHeader + " tenantTokenExpTimeFromHeader :: " + tenantTokenExpTimeFromHeader);
		String accessToken = tokenMap.get(OAuth.OAUTH_ACCESS_TOKEN);
		String refreshToken = tokenMap.get(OAuth.OAUTH_REFRESH_TOKEN);
		String expiresIn = tokenMap.get(OAuth.OAUTH_EXPIRES_IN);
		logger.debug(accessToken + " : " + refreshToken);
		jwtHeaderJson.put(AuthNConstants.JWT_TYPE_KEY, AuthNConstants.JWT_TYPE_VALUE);
		jwtHeaderJson.put(AuthNConstants.JWT_TYPE_ALGO, AuthNConstants.JWT_TYPE_ALGO_VALUE);
		byte[] headerData = jwtHeaderJson.toString().getBytes();
		String headerbase64Encoded = DatatypeConverter.printBase64Binary(headerData);
		payloadDataJson.put(AuthNConstants.AUTH_DATA_KEY,
				authDataJson.put(OAuth.OAUTH_ACCESS_TOKEN, accessToken).put(OAuth.OAUTH_REFRESH_TOKEN, refreshToken)
						.put(OAuth.OAUTH_EXPIRES_IN, expiresIn).put(AuthNConstants.IS_AUTH, true));
		payloadDataJson.put(AuthNConstants.USER_DATA_KEY, userDataJson.put(AuthNConstants.USER_NAME, userName));
		payloadDataJson.put(AuthNConstants.TENANT_SITE_INFO_KEY,
				tenantSiteDataJson.put(LeapDataContextConstant.TENANT_TOKEN, tenantTokenFromHeader)
						.put(LeapDataContextConstant.TENANT_TOKEN_EXPIRATION, tenantTokenExpTimeFromHeader));
		byte[] payloadData = payloadDataJson.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		byte[] securityloadData = TenantSecurityUtil.getMD5(tenantId, siteId);
		String securityloadDatabase64Encoded = DatatypeConverter.printBase64Binary(securityloadData);
		logger.debug("securityBase64Data :: " + securityloadDatabase64Encoded);
		String jwtTokenData = headerbase64Encoded + "." + payloadDatabase64Encoded + "."
				+ securityloadDatabase64Encoded;
		logger.debug("jwttokendata ::: " + jwtTokenData);
		return jwtTokenData;
	}// ..end of method createJWTTokenStructure

	public static void dbCleanUp(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) {
		try {
			if (conn != null) {
				conn.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to load the saml properties.
	 * 
	 * @return Properties of the saml.
	 * @throws PropertiesConfigException
	 */
	public static Properties getSAML2OAuth2Config() throws PropertiesConfigException {
		if (soConfigProperties == null && exchange != null) {
			soConfigProperties = new Properties();
			FileInputStream inStream = null;
			try {
				inStream = getFeatureConfigFileAsStream(AuthNConstants.SSO_CONFIG_PROPS);
				soConfigProperties.load(inStream);
			} catch (IOException e) {
				logger.error("Unable to get the baseProperties config file: ", e);
				throw new PropertiesConfigException("Could not load SSO-CONFIG file  " + AuthNConstants.SSO_CONFIG_PROPS
						+ ", hence setting defaults:");
			} finally {
				try {
					if (inStream != null)
						inStream.close();
				} catch (IOException e) {
				}
			}
			return soConfigProperties;

		} else if (soConfigProperties != null && !(soConfigProperties.isEmpty()))
			return soConfigProperties;
		else
			throw new PropertiesConfigException(
					"Could not load SSO-CONFIG file  " + AuthNConstants.SSO_CONFIG_PROPS + ", hence setting defaults:");

	}// ..end of the method

	/**
	 * This method is used to load the given properties file with the given
	 * fileName.
	 * 
	 * @return Properties for the respective fileName specified
	 * @throws PropertiesConfigException
	 */
	public static Properties getldapProperties() throws PropertiesConfigException {
		if (idapConfigProperties == null && exchange != null) {
			logger.info("setting the idapConfigProperties in Utill Class");
			FileInputStream fileInput = null;
			try {
				idapConfigProperties = new Properties();
				fileInput = getFeatureConfigFileAsStream(AuthNConstants.CONFIG_PROPS);
				idapConfigProperties.load(fileInput);
				ADMIN_USER = idapConfigProperties.getProperty("identityServerUsername", "admin@wso2.com");
				ADMIN_PASS = idapConfigProperties.getProperty("identityServerPassword", AuthNConstants.ADMIN);
			} catch (IOException e) {
				logger.error("Unable to get the baseProperties config file: ", e);
				throw new PropertiesConfigException("Could not load LDAP-CONFIG file  " + AuthNConstants.CONFIG_PROPS
						+ ", hence setting defaults:");
			} finally {
				try {
					if (fileInput != null)
						fileInput.close();
				} catch (IOException e) {
				}
			}
			return idapConfigProperties;
		} else if (idapConfigProperties != null && !(idapConfigProperties.isEmpty()))
			return idapConfigProperties;
		else {
			throw new PropertiesConfigException(
					"Could not load LDAP-CONFIG file  " + AuthNConstants.CONFIG_PROPS + ", hence setting defaults:");
		}
	}// ..end
		// of
		// the
		// method
		// loadPropertiesFile

	/**
	 * This method is used to load the given properties file with the given
	 * fileName.
	 * 
	 * @return Properties for the respective fileName specified
	 * @throws PropertiesConfigException
	 */
	public static Properties getBasewso2Properties() throws PropertiesConfigException {
		if (basewso2ConfigProperties == null && exchange != null) {
			FileInputStream fileInput = null;
			try {
				basewso2ConfigProperties = new Properties();
				fileInput = getFeatureConfigFileAsStream(AuthNConstants.WSO2_CONFIG_PROPS);
				basewso2ConfigProperties.load(fileInput);
			} catch (IOException e) {
				logger.error("Unable to get the baseProperties config file: ", e);
				throw new PropertiesConfigException("Could not load BASEWSO2-CONFIG file  "
						+ AuthNConstants.WSO2_CONFIG_PROPS + ", hence setting defaults:");
			} finally {
				try {
					if (fileInput != null)
						fileInput.close();
				} catch (IOException e) {
				}
			}
			return basewso2ConfigProperties;
		} else if (basewso2ConfigProperties != null && !(basewso2ConfigProperties.isEmpty()))
			return basewso2ConfigProperties;
		else {
			throw new PropertiesConfigException("Could not load BASEWSO2-CONFIG file  "
					+ AuthNConstants.WSO2_CONFIG_PROPS + ", hence setting defaults:");
		}
	}// ..end of the method loadPropertiesFile

	/**
	 * This method is used to get location of given fileName.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * 
	 */
	private static FileInputStream getFeatureConfigFileAsStream(String fileName) throws FileNotFoundException {
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		RequestContext requestContext = leapDataContext.getServiceDataContext().getRequestContext();
		String featureLvlConfigPath = requestContext.getFeatureGroup() + File.separator
				+ requestContext.getFeatureName() + File.separator + requestContext.getImplementationName()
				+ File.separator + requestContext.getVendor();
		String baseConfigPath = System.getProperty(AuthNConstants.BASE_CONFIG_PATH);
		String fileLoc = baseConfigPath + File.separator + featureLvlConfigPath + File.separator + fileName;
		logger.info("File Location is: " + fileLoc);
		return new FileInputStream(fileLoc);
	}

	public static void setExchange(Exchange exchange) {
		if (AuthNUtil.exchange == null) {
			logger.info("setting the exchange in Utill Class");
			AuthNUtil.exchange = exchange;
		}
	}
}
