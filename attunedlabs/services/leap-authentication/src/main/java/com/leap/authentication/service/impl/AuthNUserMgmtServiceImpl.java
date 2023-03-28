package com.leap.authentication.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentCredential;
import org.wso2.carbon.identity.sso.agent.saml.X509CredentialImpl;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.LdapConstant;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.InvalidAttributesFetchException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.TokenRenewalException;
import com.leap.authentication.exception.TokenRevokeException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.exception.UserValidationRequestException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.dao.AuthNUserMgmntDao;
import com.leap.authentication.service.helper.AuthNUserMgmntServiceHelper;
import com.leap.authentication.util.AuthNUtil;

@SuppressWarnings("deprecation")
public class AuthNUserMgmtServiceImpl implements IAuthNUserMgmtService {
	public static final Logger logger = LoggerFactory.getLogger(AuthNUserMgmtServiceImpl.class.getName());
	private AuthNUserMgmntServiceHelper helper = new AuthNUserMgmntServiceHelper();
	// static Properties properties;
	X509Credential credential = null;
	// static Properties key2actssoconfigproperties;
	// static Properties propertiesBase;
	// static String baseUrl;
	// static Properties configproperties;
	IAuthNAdminService adminService;
	AuthNUserMgmntDao userMgmntDao;
	private IAccountRegistryService registryService;

	public AuthNUserMgmtServiceImpl() throws PropertiesConfigException {
		adminService = new AuthNAdminServiceImpl();
		// properties = AuthNUtil.getSAML2OAuth2Config();
		userMgmntDao = new AuthNUserMgmntDao();
		registryService = new AccountRegistryServiceImpl();
	}// .. end of default constructor

	@Override
	public Map<String, String> authenticateUser(String userName, String password, String domain)
			throws InvalidAuthenticationException, InvalidAttributesFetchException {
		if (AuthNUtil.isEmpty(domain)) {
			throw new InvalidAttributesFetchException("Unable to identify a valid domain for given domain.. ");
		}
		String usernamePassDomain = userName.trim() + "@" + domain.trim() + ":" + password.trim();
		byte[] enodedBasicAuth = Base64.encodeBase64(usernamePassDomain.getBytes());
		String encodedBasicAuthUNamePwd = new String((enodedBasicAuth), Charset.forName(AuthNConstants.CHARSET_UTF_8));
		Map<String, String> resMap;
		String oauthAttributes;
		try {
			String requestURL = helper.buildCompleteRequestURLString("Basic " + encodedBasicAuthUNamePwd);
			String SAMLResponse = helper.userAuthentication(requestURL);
			String requestArrayString = Arrays.toString(AuthNUtil.getHTMLTagValue(SAMLResponse).toArray());
			String xmlResp = helper.getXMLResponseValue(requestArrayString);
			String grant_type = "grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer&assertion=";
			String samlAssertionXml = xmlResp;
			String consumerKey = AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_OAuth2ClientID);
			String consumerSecret = AuthNUtil.getSAML2OAuth2Config()
					.getProperty(AuthNConstants.SAML_OAuth2ClientSecret);
			String clientCredentilas = org.opensaml.xml.util.Base64
					.encodeBytes((byte[]) new String(consumerKey + ":" + consumerSecret).getBytes()).replace("\n", "");
			String tokenEndpoint = AuthNUtil.getSAML2OAuth2Config()
					.getProperty(AuthNConstants.SAML_OAuth2TokenEndpoint);
			String urlParameter = grant_type + URLEncoder.encode(org.opensaml.xml.util.Base64
					.encodeBytes((byte[]) samlAssertionXml.getBytes()).replaceAll("\n", ""));
			oauthAttributes = helper.executePost(tokenEndpoint, urlParameter, clientCredentilas);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidAuthenticationException("Unable to perform a valid authentication: - " + e.getMessage(),
					e);
		}
		try {
			resMap = helper.buildOauth2Headers(oauthAttributes, userName, domain);
		} catch (Exception e) {
			throw new InvalidAuthenticationException("Unable to get the queried attribute to set in response: ", e);
		}
		return resMap;
	}// ..end of the method

	@SuppressWarnings({ "resource" })
	@Override
	public boolean revokeUserOAuthToken(String accessToken) throws TokenRevokeException {
		try {
			AuthNUtil.initDefaultBootstrap(AuthNUtil.getSAML2OAuth2Config());
		} catch (SSOAgentException | ConfigurationException | PropertiesConfigException e1) {
			throw new TokenRevokeException("Unable to initialize the certificate configs: ", e1);
		}
		try {
			System.setProperty(AuthNConstants.TRUST_STORE_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStore));
			System.setProperty(AuthNConstants.TRUST_STORE_PASS_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStorePassword));
		} catch (PropertiesConfigException e) {
			e.printStackTrace();
			throw new TokenRevokeException("Unable to Read properties file ", e);
		}

		synchronized (this) {
			if (credential == null) {
				synchronized (this) {
					SSOAgentCredential credential = null;
					try {
						credential = (SSOAgentCredential) Class
								.forName(SSOAgentConfigs.getSSOAgentCredentialImplClass()).newInstance();
						credential.init();
						this.credential = new X509CredentialImpl(credential);
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
							| SSOAgentException e) {
						throw new TokenRevokeException("Unable to initialize the certificate configs: ", e);
					}
				}
			}
		} // ..end of credential build
		AuthNUtil.disablesslTrustValidation();
		String url1 = null;
		String encoding = null;
		try {
			url1 = AuthNUtil.getSAML2OAuth2Config().getProperty("revokeEndpoint").trim() + "?token=" + accessToken
					+ "&token_type_hint=access_token";
			logger.debug("revoke url : " + url1);
			encoding = (AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_OAuth2ClientID) + ":"
					+ AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_OAuth2ClientSecret));
		} catch (PropertiesConfigException e) {
			throw new TokenRevokeException("Unable to Read properties file ", e);
		}

		logger.debug("encoding string  : " + encoding);
		String encodedString = org.opensaml.xml.util.Base64.encodeBytes(encoding.getBytes());
		logger.debug("encodedString   : " + encodedString);
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
		HttpPost httppost = new HttpPost(url1);
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httppost.setHeader("Authorization", "Basic " + encodedString.trim());
		httppost.setHeader("token", accessToken);
		httppost.setHeader("token_type_hint", "access_token");
		// HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			response = httpClient.execute(httppost);
			logger.debug("response : " + response.toString().getBytes());
			// URL url = new URL(url1);
			// logger.debug(url.toString());
			// connection = (HttpURLConnection) url.openConnection();
			// connection.setRequestMethod("POST");
			// connection.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded");
			// connection.setRequestProperty("Authorization", "Basic " +
			// encodedString.trim());
			// connection.setRequestProperty("token", accessToken);
			// connection.setRequestProperty("token_type_hint", "access_token");
			// connection.setDoInput(true);
			// connection.setDoOutput(true);
			// DataOutputStream wr = new
			// DataOutputStream(connection.getOutputStream());
			// // wr.writeBytes(urlParameters);
			// // wr.flush();
			// // wr.close();
			// InputStream is = connection.getInputStream();
			// BufferedReader rd = new BufferedReader(new
			// InputStreamReader(is));
			// StringBuffer response = new StringBuffer();
			// while ((line = rd.readLine()) != null) {
			// response.append(line);
			// response.append('\r');
			// }
			// rd.close();
			// string = response.toString();
			// logger.debug("string : " + string);

		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new TokenRevokeException(
					"Unable to execute the http-post request for specified url: " + e.getMessage());
		}
		StatusLine statusLine = response.getStatusLine();
		int statCode = statusLine.getStatusCode();
		logger.info("revoked the access_token.." + statCode);
		// if (string.equalsIgnoreCase("200"))
		if (statCode == 200)
			return true;
		else
			return false;
	}// ..end of the method

	@SuppressWarnings({})
	@Override
	public Map<String, Object> renewUserRefreshToken(String refreshToken) throws TokenRenewalException {
		if (AuthNUtil.isEmpty(refreshToken)) {
			throw new TokenRenewalException("Invalid - empty refreshToken requested to process..", null,
					"Invalid - empty refreshToken requested to process..", 400);
		}
		String refTokenTemp = refreshToken.trim();
		Map<String, Object> map = new HashMap<>();
		try {
			AuthNUtil.initDefaultBootstrap(AuthNUtil.getSAML2OAuth2Config());

			System.setProperty(AuthNConstants.TRUST_STORE_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStore));
			System.setProperty(AuthNConstants.TRUST_STORE_PASS_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStorePassword));
		} catch (SSOAgentException | ConfigurationException | PropertiesConfigException e1) {
			throw new TokenRenewalException("Unable to initialize the certificate configs: ", null,
					"Unable to initialize the certificate configs: ", 400);
		}
		synchronized (this) {
			if (credential == null) {
				synchronized (this) {
					SSOAgentCredential credential_ = null;
					try {
						credential_ = (SSOAgentCredential) Class
								.forName(SSOAgentConfigs.getSSOAgentCredentialImplClass()).newInstance();
						credential_.init();
						this.credential = new X509CredentialImpl(credential_);
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
							| SSOAgentException e) {
						throw new TokenRenewalException("Unable to initialize the certificate configs : ", e,
								e.getMessage(), 400);
					}
				}
			}
		} // ..end of credential build
		String url1 = null;
		try {
			url1 = AuthNUtil.getBasewso2Properties().getProperty("refreshtokenrenewal").trim()
					+ "?grant_type=refresh_token&refresh_token=" + refTokenTemp + "&scope=PRODUCTION;";
		} catch (PropertiesConfigException e1) {
			e1.printStackTrace();
			throw new TokenRenewalException("Unable to Read properties files: ", e1, e1.getMessage(), 400);
		}
		String encoding = null;
		try {
			encoding = (AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_OAuth2ClientID) + ":"
					+ AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_OAuth2ClientSecret));
		} catch (PropertiesConfigException e1) {
			e1.printStackTrace();
			throw new TokenRenewalException("Unable to Read properties files: ", e1, e1.getMessage(), 400);
		}
		String encodedString = org.opensaml.xml.util.Base64.encodeBytes(encoding.getBytes());
		HttpClient httpclient = AuthNUtil.httpsClientCertificateVerifier();
		HttpPost httppost = new HttpPost(url1);
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httppost.setHeader("Authorization", "Basic " + encodedString.trim());
		// HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (IOException e) {
			throw new TokenRenewalException("Unable to execute the http-post request for specified url: ", e,
					e.getMessage(), 400);
		}
		StatusLine statusLine = response.getStatusLine();
		logger.debug("StatusLine: " + statusLine);
		if (statusLine.getStatusCode() != 200) {
			throw new TokenRenewalException("Unable to get token renewed with the refreshToken requested....", null,
					"Unable to get token renewed with the refreshToken requested....", 500);
		} else {
			try {
				StringWriter writer = new StringWriter();
				IOUtils.copy(response.getEntity().getContent(), writer, AuthNConstants.CHARSET_UTF_8);
				String theString = writer.toString();
				JSONObject jsonObject = new JSONObject(theString);
				map.put("access_token", jsonObject.getString("access_token"));
				map.put("refresh_token", jsonObject.getString("refresh_token"));
				map.put("expires_in", jsonObject.get("expires_in"));
				return map;
			} catch (UnsupportedOperationException | IOException | JSONException e) {
				throw new TokenRenewalException(
						"Unable to get token & attribues from the response / connection failed due to: ", e,
						e.getMessage(), 400);
			}
		}
	}// ..end of the method

	@Override
	public void changeUserPassword(String userName, String domain, String oldPassword, String newPassword)
			throws CredentialUpdateException {
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty(AuthNConstants.BASE_URL)
					+ AuthNConstants.PART_USER_IDENTITYREPO;
			/*
			 * String domain; domain = getDomainNameByTenantId(tenantId);
			 */
			logger.debug("Domain changeUserPassword: " + domain);
			if (AuthNUtil.isEmpty(domain)) {
				throw new CredentialUpdateException("Unable to identify a valid domain for given tenantId", null,
						"Unable to identify a valid domain for given tenantId", 401);
			}
			soapResponse = soapConnection.call(
					helper.getChangeUserPasswordSoapMessage(userName, domain, newPassword.trim(), oldPassword.trim()),
					url);
			logger.info("Successfully updated the user password !" + soapResponse.getSOAPBody().getTextContent());

			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new CredentialUpdateException("Unauthorized User!", null, "Unauthorized User!", 401);
				}
			}
			soapConnection.close();
		} catch (SOAPException | PropertiesConfigException e) {
			throw new CredentialUpdateException("Unable to make a service connection to change UserPassword.. ", e,
					e.getMessage(), 500);
		}
	}// ..end of the method

	@SuppressWarnings({ "unused" })
	@Override
	public User getUserProfile(String bearerTokenString) throws UserProfileFetchException {
		if (!bearerTokenString.contains("Bearer")) {
			throw new UserProfileFetchException("'Bearer <token>' (Bearer) missing.", null,
					"'Bearer <token>' (Bearer) missing.", 500);
		}
		try {
			System.setProperty(AuthNConstants.TRUST_STORE_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStore));
			System.setProperty(AuthNConstants.TRUST_STORE_PASS_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStorePassword));
		} catch (PropertiesConfigException e1) {
			e1.printStackTrace();
			throw new UserProfileFetchException("Unable to fetch properties : ", e1, e1.getMessage(), 500);
		}
		AuthNUtil.disablesslTrustValidation();
		String domain;
		String userName;
		HttpClient httpclient = AuthNUtil.httpsClientCertificateVerifier();
		// DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		try {
			httppost = new HttpPost(AuthNUtil.getBasewso2Properties().getProperty("userinfourl"));
		} catch (PropertiesConfigException e1) {
			e1.printStackTrace();
			throw new UserProfileFetchException("Unable to fetch properties : ", e1, e1.getMessage(), 500);

		}
		if (bearerTokenString.contains("Bearer%20")) {
			bearerTokenString = URLDecoder.decode(bearerTokenString);
		}
		httppost.setHeader("Authorization", bearerTokenString);
		HttpResponse response;
		String body;
		try {
			response = httpclient.execute(httppost);
			InputStream content = response.getEntity().getContent();
			body = IOUtils.toString(content);
		} catch (IOException e) {
			throw new UserProfileFetchException("Unable to get the subject: ", e, e.getMessage(), 500);
		}
		logger.debug("UserSubject : " + body);
		JSONObject jsonObject = new JSONObject(body);
		httpclient.getConnectionManager().shutdown();
		if (!jsonObject.has("sub")) {
			throw new UserProfileFetchException("Requested Bearer <token> expired / invalid", null,
					"Requested Bearer <token> expired / invalid", 500);
		}
		String userDomainCarbon = jsonObject.get("sub").toString();
		String[] arr = userDomainCarbon.split("@carbon.super");
		String userDomain = arr[0];
		String[] newArr = userDomain.split(AuthNConstants.AT_SIGN);
		logger.debug("checking new arr: *********" + newArr[0] + " & " + newArr[1]);
		domain = newArr[newArr.length - 1];
		String[] userArr = userDomain.split(AuthNConstants.AT_SIGN + domain);
		userName = userArr[0] + AuthNConstants.AT_SIGN + domain;
		logger.debug(".getUserProfile()... userName: " + userArr[0] + " domain: " + domain);
		User user = new User();
		UserClaims claims = new UserClaims();
		Map<String, Object> attributesMap;
		String uName = null;
		if (userArr[0].contains(AuthNConstants.AT_SIGN)) {
			uName = userArr[0];
		} else {
			uName = newArr[0].trim() + AuthNConstants.AT_SIGN + newArr[1].trim();
		}
		logger.debug("uName : " + uName);
		try {
			attributesMap = helper.getUserAttributes(uName, domain);
		} catch (UserProfileFetchException e) {
			throw new UserProfileFetchException("Unable to fetch the user attributes successfully : ", e,
					e.getMessage(), 500);
		}
		logger.debug("attributesMap : " + attributesMap);
		claims.setCountry(attributesMap.get(LdapConstant.COUNTRY_KEY).toString());
		claims.setEmailaddress(attributesMap.get(LdapConstant.EMAIL_KEY).toString());
		claims.setMobile(attributesMap.get(LdapConstant.MOBILE_KEY).toString());
		claims.setOrganization(attributesMap.get(LdapConstant.COMPANY_NAME_KEY).toString());
		claims.setStreetaddress(attributesMap.get(LdapConstant.ADDRESS1_KEY).toString());
		claims.setTelephone(attributesMap.get(LdapConstant.TELEPHONE_KEY).toString());
		user.setCreatedDate(attributesMap.get(LdapConstant.CREATED_DATE).toString());
		user.setLastModifiedDate(attributesMap.get(LdapConstant.LAST_MODIFIED_DATE).toString());
		// claims.setLocality(attributesMap.get("locality").toString());
		claims.setLocality(attributesMap.get(AuthNConstants.LOCALITY) == null ? ""
				: attributesMap.get(AuthNConstants.LOCALITY).toString());
		claims.setPostalCode(attributesMap.get(AuthNConstants.POSTAL_CODE).toString());
		claims.setCity(attributesMap.get(AuthNConstants.POSTAL_ADDRESS) == null ? ""
				: attributesMap.get(AuthNConstants.POSTAL_ADDRESS).toString());
		claims.setRegion(attributesMap.get(AuthNConstants.REGION).toString());
		claims.setStateOrProvince(attributesMap.get(AuthNConstants.STATE).toString());

		try {
			String isAct = helper.getUserEnabledProperty(uName, domain);
			if (isAct == null || isAct.equalsIgnoreCase(AuthNConstants.TRUE_KEY))
				claims.setIsActive(AuthNConstants.FALSE_KEY);
			else
				claims.setIsActive(AuthNConstants.TRUE_KEY);
		} catch (InvalidAttributesFetchException | DomainIdentificationException e) {
			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
		}

		try {
			claims.setIsLocked(helper.getUserLockProperty(uName, domain));
		} catch (InvalidAttributesFetchException | DomainIdentificationException e) {
			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
		}

		user.setFirstName(attributesMap.get(LdapConstant.FIRST_NAME_KEY).toString());
		user.setLastName(attributesMap.get(LdapConstant.LAST_NAME_KEY).toString());
		user.setUserName(attributesMap.get(LdapConstant.USER_NAME_KEY).toString());
		String tenSite = attributesMap.get(LdapConstant.TENANT_SITE_KEY).toString();
		String[] tenantSiteArr = tenSite.split("/");
		user.setSiteId(tenantSiteArr[1]);
		user.setTenantId(tenantSiteArr[0]);
		user.setTitle(attributesMap.get(LdapConstant.TITLE_KEY).toString());
		user.setUserClaims(claims);
		user.setDomain(domain);
		return user;
	}// ..end of the method

	@Override
	public boolean validateAccessToken(String accessToken) throws InvalidAuthenticationException {
		if (AuthNUtil.isEmpty(accessToken)) {
			throw new InvalidAuthenticationException("Empty token requested to validate ! ");
		}
		byte[] bytesEncoded;
		try {
			bytesEncoded = Base64.encodeBase64(
					(AuthNUtil.getldapProperties().getProperty("identityServerUsername", AuthNConstants.ADMIN) + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN))
									.getBytes());
		} catch (PropertiesConfigException e) {
			throw new InvalidAuthenticationException(e.getMessage());
		}
		String authorization = new String(bytesEncoded);
		String responseXml = helper.validateAccessToken(accessToken, authorization);
		String isValidString = helper.validateTokenProcessBean(responseXml);
		boolean isValid;
		if (isValidString.equals(AuthNConstants.TRUE_KEY)) {
			isValid = true;
		} else {
			isValid = false;
		}
		return isValid;
	}// ..end of the method

	@Override
	public void selfRegisterUser(User user, String domain) throws UserRegistrationException, UserRepoUpdateException {
		if (AuthNUtil.isEmpty(domain))
			throw new UserRegistrationException(
					"Unable to identify a valid domain for given company name of the user! ");
		if (AuthNUtil.validateUsername(user.getUserName()))
			throw new UserRegistrationException("Invalid format for username!");

		SOAPMessage soapMessage = helper.getUserSelfSoapMessage(user, domain);
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty(AuthNConstants.BASE_URL)
					+ AuthNConstants.PART_USER_RECOVERYREPO;
			soapResponse = soapConnection.call(soapMessage, url);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			soapResponse.writeTo(stream);
			String message = new String(stream.toByteArray(), AuthNConstants.CHARSET_UTF_8);
			logger.debug("SoapResponse to createUser: " + message);
			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new UserRegistrationException("Unable to register user some internal fault occured");
				}
			}
			soapConnection.close();
		} catch (SOAPException | IOException | PropertiesConfigException e1) {
			throw new UserRegistrationException("Unable to register user, since service call is unsuccessful...", e1);
		}

		LdapContext ldapCtx = AuthNUtil.getLdapContext();
		ModificationItem[] mods;
		if (AuthNUtil.isEmpty(user.getUserClaims().getCity()))
			mods = new ModificationItem[1];
		else {
			mods = new ModificationItem[2];
			mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(AuthNConstants.POSTAL_ADDRESS, user.getUserClaims().getCity().trim()));
		}
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("description", user.getTenantId().trim() + "/" + user.getSiteId().trim()));
		logger.debug(
				"Name: " + "uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG");
		try {
			if (domain.trim().equals("carbon.super")) {
				ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG", mods);
			} else {
				// if(!domain.trim().equals("carbon.super")){
				ldapCtx.modifyAttributes(
						"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG", mods);
			}
			// }else{
			// logger.debug("inside else block becaue the user is related to the
			// wso2Is");
			// }
			int accountId = registryService.getDomainIdByDomain(domain);
			userMgmntDao.addNewUserInDB(user.getUserName().trim(), accountId);
		} catch (NamingException | AccountFetchException e) {
			throw new UserRegistrationException(
					"Invalid attribues / UserAlreadyExist / attributes already set against this user..", e);
		}
	}// ..end of the method

	@Override
	public List<String> getAllDomainsByUser(String userName) throws DomainIdentificationException {
		return userMgmntDao.getAllDomainsByUser(userName);
	}

	@Override
	public String getForgotPasswordConfmCode(String userName, String domain) throws UserValidationRequestException {
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(domain)) {
			throw new UserValidationRequestException("unable to proceed, because of empty values requested! ");
		}
		String notificationCode = null;
		try {
			Map<String, String> mapOfPathNKey = helper.callGetCaptcha();
			logger.debug("--" + mapOfPathNKey);
			Map<String, String> mapOfKeyValid = helper.callVerifyUser(userName, domain, mapOfPathNKey);
			logger.debug("--" + mapOfKeyValid);
			notificationCode = helper.callSendNotification(userName, domain, mapOfKeyValid);
			logger.debug("--" + notificationCode);

		} catch (UserValidationRequestException e) {
			throw new UserValidationRequestException(
					"Unable to retreive a valid UserConfirmationCode to reset the passowrd! " + e.getMessage(), e);
		}
		return notificationCode;
	}// ..end of the method

	@Override
	public void validateAndUpdatePassword(String code, String userName, String domain, String newPassword)
			throws CredentialUpdateException {
		try {
			Map<String, String> map = helper.callGetCaptcha();
			Map<String, String> mapRes = helper.callVerifyConfirmationCode(userName, domain, code, map);
			if (Boolean.valueOf(mapRes.get("isvalid"))) {
				helper.callUpdatePassword(userName, domain, mapRes.get("newKey"), newPassword);
			} else {
				throw new CredentialUpdateException(
						"Invalid code has been requested to validate against to update the credentials ", null,
						"Invalid code has been requested to validate against to update the credentials ", 500);
			}
		} catch (UserValidationRequestException e) {
			throw new CredentialUpdateException("Unable to update the credential, because of the reason: ", e,
					e.getMessage(), 500);
		}
	}// ..end of the method

	@Override
	public User getUserProfile(String userName, String domain) throws UserProfileFetchException {
		try {
			System.setProperty(AuthNConstants.TRUST_STORE_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStore));
			System.setProperty(AuthNConstants.TRUST_STORE_PASS_KEY,
					AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.KeyStorePassword));
		} catch (PropertiesConfigException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new UserProfileFetchException("Unable to fetch properties : ", e1, e1.getMessage(), 500);
		}
		AuthNUtil.disablesslTrustValidation();
		User user = new User();
		UserClaims claims = new UserClaims();
		Map<String, Object> attributesMap;
		attributesMap = helper.getUserAttributes(userName, domain);
		user.setCreatedDate(attributesMap.get("createdDate").toString());
		user.setLastModifiedDate(attributesMap.get("lastModifiedDate").toString());
		if (attributesMap.get(LdapConstant.COUNTRY_KEY) == null)
			claims.setCountry("");
		else
			claims.setCountry(attributesMap.get(LdapConstant.COUNTRY_KEY).toString());

		if (attributesMap.get(LdapConstant.EMAIL_KEY) == null)
			claims.setEmailaddress("");
		else
			claims.setEmailaddress(attributesMap.get(LdapConstant.EMAIL_KEY).toString());

		if (attributesMap.get(LdapConstant.MOBILE_KEY) == null)
			claims.setMobile("");
		else
			claims.setMobile(attributesMap.get(LdapConstant.MOBILE_KEY).toString());

		if (attributesMap.get(LdapConstant.COMPANY_NAME_KEY) == null)
			claims.setOrganization("");
		else
			claims.setOrganization(attributesMap.get(LdapConstant.COMPANY_NAME_KEY).toString());

		if (attributesMap.get(LdapConstant.ADDRESS1_KEY) == null)
			claims.setStreetaddress("");
		else
			claims.setStreetaddress(attributesMap.get(LdapConstant.ADDRESS1_KEY).toString());

		if (attributesMap.get(LdapConstant.TELEPHONE_KEY) == null)
			claims.setTelephone("");
		else
			claims.setTelephone(attributesMap.get(LdapConstant.TELEPHONE_KEY).toString());

		if (attributesMap.get(AuthNConstants.LOCALITY) == null) {
			claims.setLocality("");
		} else {
			claims.setLocality(attributesMap.get(AuthNConstants.LOCALITY).toString());
		}

		if (attributesMap.get(AuthNConstants.POSTAL_CODE) == null)
			claims.setPostalCode("");
		else
			claims.setPostalCode(attributesMap.get(AuthNConstants.POSTAL_CODE).toString());

		if (attributesMap.get(AuthNConstants.REGION) == null)
			claims.setRegion("");
		else
			claims.setRegion(attributesMap.get(AuthNConstants.REGION).toString());

		if (attributesMap.get(AuthNConstants.STATE) == null)
			claims.setStateOrProvince("");
		else
			claims.setStateOrProvince(attributesMap.get(AuthNConstants.STATE).toString());

		if (attributesMap.get(AuthNConstants.POSTAL_ADDRESS) == null)
			claims.setCity("");
		else
			claims.setCity(attributesMap.get(AuthNConstants.POSTAL_ADDRESS).toString());

		try {
			String isAct = helper.getUserEnabledProperty(userName, domain);
			if (isAct == null || isAct.equalsIgnoreCase(AuthNConstants.TRUE_KEY))
				claims.setIsActive(AuthNConstants.FALSE_KEY);
			else
				claims.setIsActive(AuthNConstants.TRUE_KEY);
		} catch (InvalidAttributesFetchException | DomainIdentificationException e) {
			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
		}

		try {
			claims.setIsLocked(helper.getUserLockProperty(userName, domain));
		} catch (InvalidAttributesFetchException | DomainIdentificationException e) {
			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
		}

		if (attributesMap.get(LdapConstant.FIRST_NAME_KEY) == null)
			user.setFirstName("");
		else
			user.setFirstName(attributesMap.get(LdapConstant.FIRST_NAME_KEY).toString());

		if (attributesMap.get(LdapConstant.LAST_NAME_KEY) == null)
			user.setLastName("");
		else
			user.setLastName(attributesMap.get(LdapConstant.LAST_NAME_KEY).toString());

		if (attributesMap.get(LdapConstant.USER_NAME_KEY) == null)
			user.setUserName("");
		else
			user.setUserName(attributesMap.get(LdapConstant.USER_NAME_KEY).toString());

		try {
			String tenSite = attributesMap.get(LdapConstant.TENANT_SITE_KEY).toString();
			String[] tenantSiteArr = tenSite.split("/");
			user.setSiteId(tenantSiteArr[1]);
			user.setTenantId(tenantSiteArr[0]);
		} catch (NullPointerException e) {
			user.setSiteId("");
			user.setTenantId("");
		}

		if (attributesMap.get(LdapConstant.TITLE_KEY) == null)
			user.setTitle("");
		else
			user.setTitle(attributesMap.get(LdapConstant.TITLE_KEY).toString());

		user.setUserClaims(claims);
		return user;
	}// ..end of the method

	@Override
	public Map<String, String> appDeatils(String tenantId, String siteId, String domainValue)
			throws AccountFetchException {
		return userMgmntDao.getAppDeatils(tenantId, siteId, domainValue);
	}
}
