package com.leap.authentication.service.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.HashedMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentCredential;
import org.wso2.carbon.identity.sso.agent.saml.X509CredentialImpl;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.datastax.driver.core.exceptions.UnauthorizedException;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.LdapConstant;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.InvalidAccessTokenException;
import com.leap.authentication.exception.InvalidAttributesFetchException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.exception.InvalidInitException;
import com.leap.authentication.exception.InvalidTenantSiteException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.SAMLResponseException;
import com.leap.authentication.exception.TokenValidationException;
import com.leap.authentication.exception.TrustStoreCertificateException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.exception.UserValidationRequestException;
import com.leap.authentication.service.dao.AuthNAdminDao;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class AuthNUserMgmntServiceHelper {
	static final Logger logger = LoggerFactory.getLogger(AuthNUserMgmntServiceHelper.class.getName());
	
//	Properties ssoConfig;
	X509Credential credential = null;
//	Properties base;
	private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	public AuthNUserMgmntServiceHelper() throws PropertiesConfigException {
//		this.configs = AuthNUtil.getldapProperties();
//		this.base = AuthNUtil.getBasewso2Properties();
//		this.ssoConfig = AuthNUtil.getSAML2OAuth2Config();
	}

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param confCode
	 * @param newPass
	 * @throws UserValidationRequestException
	 */
	public void callUpdatePassword(String userName, String domain, String confCode, String newPass)
			throws UserValidationRequestException {
		SOAPMessage soapMessage = getUpdatePswdMessage(userName, domain, confCode, newPass);
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_USER_INFO_REPO;
			soapResponse = soapConnection.call(soapMessage, url);
			if (soapResponse != null) {

			}
			SOAPBody soapBody;
			try {
				soapBody = soapResponse.getSOAPBody();
			} catch (NullPointerException e) {
				throw new UserValidationRequestException(
						"Unable to get update password the service response! " + e.getMessage(), e);
			}
			Node newSecretKey = soapBody.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(5);
			logger.debug(newSecretKey.getTextContent());
			soapConnection.close();
		} catch (SOAPException | PropertiesConfigException e) {
			throw new UserValidationRequestException("Unable to updatePassword , since service call is unsuccessful...",
					e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param confCode
	 * @param newPass
	 * @return
	 * @throws UserValidationRequestException
	 */
	private SOAPMessage getUpdatePswdMessage(String userName, String domain, String confCode, String newPass)
			throws UserValidationRequestException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://beans.mgt.captcha.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("updatePassword", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("username", "ser");
			soapBodyElem1.addTextNode(userName + AuthNConstants.AT_SIGN + domain);
			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("confirmationCode", "ser");
			soapBodyElem4.addTextNode(confCode);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("newPassword", "ser");
			soapBodyElem2.addTextNode(newPass);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "verifyConfirmationCode");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			soapMessage.writeTo(stream);
			String message = new String(stream.toByteArray(), "utf-8");
			logger.debug("SoapRequest to createUser: " + message);
			return soapMessage;
		} catch (SOAPException | IOException | PropertiesConfigException e) {
			throw new UserValidationRequestException("Unable to construct update password request! " + e.getMessage(),
					e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param code
	 * @param map
	 * @return
	 * @throws UserValidationRequestException
	 */
	public Map<String, String> callVerifyConfirmationCode(String userName, String domain, String code,
			Map<String, String> map) throws UserValidationRequestException {
		SOAPMessage soapMessage = getVerifyConfirmationCodeMessage(userName, domain, code, map.get("imagePath"),
				map.get("secretKey"));
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		Map<String, String> mapRes;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_USER_INFO_REPO;
			soapResponse = soapConnection.call(soapMessage, url);
			if (soapResponse != null) {

			}
			SOAPBody soapBody;
			try {
				soapBody = soapResponse.getSOAPBody();
			} catch (NullPointerException e) {
				throw new UserValidationRequestException(
						"Unable to get code parsed from the service response! " + e.getMessage(), e);
			}
			String newSecretKey = soapBody.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1)
					.getTextContent();
			String isValid = soapBody.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(5)
					.getTextContent();
			logger.debug("" + isValid + newSecretKey);
			soapConnection.close();
			mapRes = new HashMap<>();
			mapRes.put("newKey", newSecretKey);
			mapRes.put("isvalid", isValid);
			return mapRes;
		} catch (SOAPException | PropertiesConfigException e) {
			throw new UserValidationRequestException(
					"Unable to verifyConfirmation code , since service call is unsuccessful...", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param confCode
	 * @param imagePath
	 * @param secretKey
	 * @return
	 * @throws UserValidationRequestException
	 */
	private SOAPMessage getVerifyConfirmationCodeMessage(String userName, String domain, String confCode,
			String imagePath, String secretKey) throws UserValidationRequestException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://beans.mgt.captcha.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("verifyConfirmationCode", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("username", "ser");
			soapBodyElem1.addTextNode(userName + AuthNConstants.AT_SIGN + domain);
			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("code", "ser");
			soapBodyElem4.addTextNode(confCode);

			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("captcha", "ser");
			SOAPElement soapElement2_1 = soapBodyElem2.addChildElement("imagePath", "xsd");
			soapElement2_1.addTextNode(imagePath);
			SOAPElement soapElement3_1 = soapBodyElem2.addChildElement("secretKey", "xsd");
			soapElement3_1.addTextNode(secretKey);
			SOAPElement soapElement5_1 = soapBodyElem2.addChildElement("userAnswer", "xsd");
			soapElement5_1.addTextNode("");
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "verifyConfirmationCode");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			soapMessage.writeTo(stream);
			String message = new String(stream.toByteArray(), "utf-8");
			logger.debug("SoapRequest to createUser: " + message);
			return soapMessage;
		} catch (SOAPException | IOException | PropertiesConfigException e) {
			throw new UserValidationRequestException("Unable to construct verify code request! " + e.getMessage(), e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param mapOfKeyValid
	 * @return
	 * @throws UserValidationRequestException
	 */
	public String callSendNotification(String userName, String domain, Map<String, String> mapOfKeyValid)
			throws UserValidationRequestException {
		SOAPMessage soapMessage = getSendNotificationMessage(userName, domain, mapOfKeyValid.get("key"));
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_USER_INFO_REPO;
			soapResponse = soapConnection.call(soapMessage, url);
			if (soapResponse != null) {
				if (!Boolean.valueOf(soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
						.getChildNodes().item(5).getTextContent())) {
					throw new UserValidationRequestException(
							"Unable to sendNotification service some internal fault occured: "
									+ soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
											.getChildNodes().item(0).getTextContent());
				}
			}
			SOAPBody soapBody;
			try {
				soapBody = soapResponse.getSOAPBody();
			} catch (NullPointerException e) {
				throw new UserValidationRequestException(
						"Unable to get code parsed from the service response! " + e.getMessage(), e);
			}
			soapConnection.close();
			logger.debug("");
			return soapBody.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(2).getChildNodes()
					.item(4).getTextContent();
		} catch (SOAPException | PropertiesConfigException e1) {
			throw new UserValidationRequestException(
					"Unable to sendNotification, since service call is unsuccessful...", e1);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param verifiedKey
	 * @return
	 * @throws UserValidationRequestException
	 */
	private SOAPMessage getSendNotificationMessage(String userName, String domain, String verifiedKey)
			throws UserValidationRequestException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://beans.mgt.captcha.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("sendRecoveryNotification", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("username", "ser");
			soapBodyElem1.addTextNode(userName + AuthNConstants.AT_SIGN + domain);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("key", "ser");
			soapBodyElem2.addTextNode(verifiedKey);
			SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("notificationType", "ser");
			soapBodyElem3.addTextNode("email");
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "sendRecoveryNotification");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException | PropertiesConfigException e) {
			throw new UserValidationRequestException("Unable to construct notification request! " + e.getMessage(), e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param mapOfPathNKey
	 * @return
	 * @throws UserValidationRequestException
	 */
	public Map<String, String> callVerifyUser(String userName, String domain, Map<String, String> mapOfPathNKey)
			throws UserValidationRequestException {
		SOAPMessage soapMessage = getVerifyUserMessage(userName, domain, mapOfPathNKey.get("imagePath"),
				mapOfPathNKey.get("secretKey"));
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		Map<String, String> map;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_USER_INFO_REPO;
			soapResponse = soapConnection.call(soapMessage, url);

			if (soapResponse != null) {
				if (!Boolean.valueOf(soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
						.getChildNodes().item(5).getTextContent())) {
					throw new UserValidationRequestException(
							"Unable to getCaptcha service some internal fault occured: "
									+ soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
											.getChildNodes().item(0).getTextContent());
				}
			}
			map = new HashMap<>();
			map.put("key", soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0).getChildNodes()
					.item(1).getTextContent());
			map.put("isvalid", soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
					.getChildNodes().item(5).getTextContent());
			logger.info("Successfully generated the soapResponse for verifyUser call !");
			soapConnection.close();
			return map;
		} catch (SOAPException | PropertiesConfigException e1) {
			throw new UserValidationRequestException("Unable to getCaptcha, since service call is unsuccessful...", e1);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @param imagePath
	 * @param secretKey
	 * @return
	 * @throws UserValidationRequestException
	 */
	private SOAPMessage getVerifyUserMessage(String userName, String domain, String imagePath, String secretKey)
			throws UserValidationRequestException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://beans.mgt.captcha.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("verifyUser", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("username", "ser");
			soapBodyElem1.addTextNode(userName + AuthNConstants.AT_SIGN + domain);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("captcha", "ser");
			SOAPElement soapElement2_1 = soapBodyElem2.addChildElement("imagePath", "xsd");
			soapElement2_1.addTextNode(imagePath);
			SOAPElement soapElement3_1 = soapBodyElem2.addChildElement("secretKey", "xsd");
			soapElement3_1.addTextNode(secretKey);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "verifyUser");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException | PropertiesConfigException e) {
			throw new UserValidationRequestException("Unable to construct verify user request! " + e.getMessage(), e);
		}
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws UserValidationRequestException
	 */
	public Map<String, String> callGetCaptcha() throws UserValidationRequestException {
		SOAPMessage soapMessage = getCaptchaMessage();
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		Map<String, String> map;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_USER_INFO_REPO;
			soapResponse = soapConnection.call(soapMessage, url);

			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new UserValidationRequestException(
							"Unable to getCaptcha service some internal fault occured");
				}
			}
			map = new HashMap<>();
			logger.info("Successfully generated the soapResponse for getCaptcha call !");
			soapConnection.close();
			String imagePath = soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
					.getChildNodes().item(0).getTextContent();
			map.put("imagePath", imagePath);
			String secretKey = soapResponse.getSOAPBody().getChildNodes().item(0).getChildNodes().item(0)
					.getChildNodes().item(1).getTextContent();
			map.put("secretKey", secretKey);
			return map;
		} catch (SOAPException | PropertiesConfigException e1) {
			throw new UserValidationRequestException("Unable to getCaptcha, since service call is unsuccessful...", e1);
		}
	}// ..end of the method

	/**
	 * 
	 * @return
	 * @throws UserValidationRequestException
	 */
	@SuppressWarnings("unused")
	private SOAPMessage getCaptchaMessage() throws UserValidationRequestException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("getCaptcha", "ser");
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "getCaptcha");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException | PropertiesConfigException e) {
			throw new UserValidationRequestException(
					"Unable to construct Captcha generation request! " + e.getMessage(), e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param user
	 * @param domain
	 * @return
	 * @throws UserRepoUpdateException
	 */
	public SOAPMessage getUserSelfSoapMessage(User user, String domain)
			throws UserRegistrationException, UserRepoUpdateException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://dto.mgt.identity.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("registerUser", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(user.getUserName());
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("password", "ser");
			soapBodyElem2.addTextNode(user.getPassword());

			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem4_1 = soapBodyElem4.addChildElement("claimUri", "xsd");
			soapBodyElem4_1.addTextNode("http://wso2.org/claims/country");
			SOAPElement soapBodyElem4_2 = soapBodyElem4.addChildElement("claimValue", "xsd");
			soapBodyElem4_2.addTextNode(user.getUserClaims().getCountry());

			SOAPElement soapBodyElem20 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem20_1 = soapBodyElem20.addChildElement("claimUri", "xsd");
			soapBodyElem20_1.addTextNode("http://wso2.org/claims/lastname");
			SOAPElement soapBodyElem20_2 = soapBodyElem20.addChildElement("claimValue", "xsd");
			soapBodyElem20_2.addTextNode(user.getLastName());

			SOAPElement soapBodyElem6 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem6_1 = soapBodyElem6.addChildElement("claimUri", "xsd");
			soapBodyElem6_1.addTextNode("http://wso2.org/claims/mobile");
			SOAPElement soapBodyElem6_2 = soapBodyElem6.addChildElement("claimValue", "xsd");
			soapBodyElem6_2.addTextNode(user.getUserClaims().getMobile());

			SOAPElement soapBodyElem7 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem7_1 = soapBodyElem7.addChildElement("claimUri", "xsd");
			soapBodyElem7_1.addTextNode("http://wso2.org/claims/organization");
			SOAPElement soapBodyElem7_2 = soapBodyElem7.addChildElement("claimValue", "xsd");
			soapBodyElem7_2.addTextNode(user.getUserClaims().getOrganization());

			if (!AuthNUtil.isEmpty(user.getUserClaims().getStreetaddress())) {
				SOAPElement soapBodyElem8 = soapBodyElem.addChildElement("claims", "ser");
				SOAPElement soapBodyElem8_1 = soapBodyElem8.addChildElement("claimUri", "xsd");
				soapBodyElem8_1.addTextNode("http://wso2.org/claims/streetaddress");
				SOAPElement soapBodyElem8_2 = soapBodyElem8.addChildElement("claimValue", "xsd");
				soapBodyElem8_2.addTextNode(user.getUserClaims().getStreetaddress());
			}

			SOAPElement soapBodyElem9 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem9_1 = soapBodyElem9.addChildElement("claimUri", "xsd");
			soapBodyElem9_1.addTextNode("http://wso2.org/claims/telephone");
			SOAPElement soapBodyElem9_2 = soapBodyElem9.addChildElement("claimValue", "xsd");
			soapBodyElem9_2.addTextNode(user.getUserClaims().getTelephone());

			SOAPElement soapBodyElem13 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem13_1 = soapBodyElem13.addChildElement("claimUri", "xsd");
			soapBodyElem13_1.addTextNode("http://wso2.org/claims/givenname");
			SOAPElement soapBodyElem13_2 = soapBodyElem13.addChildElement("claimValue", "xsd");
			soapBodyElem13_2.addTextNode(user.getFirstName());

			SOAPElement soapBodyElem14 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem14_1 = soapBodyElem14.addChildElement("claimUri", "xsd");
			soapBodyElem14_1.addTextNode("http://wso2.org/claims/title");
			SOAPElement soapBodyElem14_2 = soapBodyElem14.addChildElement("claimValue", "xsd");
			soapBodyElem14_2.addTextNode(user.getTitle());

			if (!AuthNUtil.isEmpty(user.getUserClaims().getLocality())) {
				SOAPElement soapBodyElem16 = soapBodyElem.addChildElement("claims", "ser");
				SOAPElement soapBodyElem16_1 = soapBodyElem16.addChildElement("claimUri", "xsd");
				soapBodyElem16_1.addTextNode("http://wso2.org/claims/locality");
				SOAPElement soapBodyElem16_2 = soapBodyElem16.addChildElement("claimValue", "xsd");
				soapBodyElem16_2.addTextNode(user.getUserClaims().getLocality());
			}

			SOAPElement soapBodyElem17 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem17_1 = soapBodyElem17.addChildElement("claimUri", "xsd");
			soapBodyElem17_1.addTextNode("http://wso2.org/claims/stateorprovince");
			SOAPElement soapBodyElem17_2 = soapBodyElem17.addChildElement("claimValue", "xsd");
			soapBodyElem17_2.addTextNode(user.getUserClaims().getStateOrProvince());

			SOAPElement soapBodyElem18 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem18_1 = soapBodyElem18.addChildElement("claimUri", "xsd");
			soapBodyElem18_1.addTextNode("http://wso2.org/claims/postalcode");
			SOAPElement soapBodyElem18_2 = soapBodyElem18.addChildElement("claimValue", "xsd");
			soapBodyElem18_2.addTextNode(user.getUserClaims().getPostalCode());

			if (!AuthNUtil.isEmpty(user.getUserClaims().getRegion())) {
				SOAPElement soapBodyElem19 = soapBodyElem.addChildElement("claims", "ser");
				SOAPElement soapBodyElem19_1 = soapBodyElem19.addChildElement("claimUri", "xsd");
				soapBodyElem19_1.addTextNode("http://wso2.org/claims/region");
				SOAPElement soapBodyElem19_2 = soapBodyElem19.addChildElement("claimValue", "xsd");
				soapBodyElem19_2.addTextNode(user.getUserClaims().getRegion());
			}

			SOAPElement soapBodyElem21 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem21_1 = soapBodyElem21.addChildElement("claimUri", "xsd");
			soapBodyElem21_1.addTextNode("http://wso2.org/claims/emailaddress");
			SOAPElement soapBodyElem21_2 = soapBodyElem21.addChildElement("claimValue", "xsd");
			soapBodyElem21_2.addTextNode(user.getUserClaims().getEmailaddress());

			// SOAPElement soapBodyElem11 =
			// soapBodyElem.addChildElement("claims", "ser");
			// SOAPElement soapBodyElem11_1 =
			// soapBodyElem11.addChildElement("claimUri",
			// "xsd");
			// soapBodyElem11_1.addTextNode("http://wso2.org/claims/identity/accountDisabled");
			// SOAPElement soapBodyElem11_2 =
			// soapBodyElem11.addChildElement("claimValue",
			// "xsd");
			// String isActiveValue = user.getUserClaims().getIsActive();
			// if (isActiveValue.equalsIgnoreCase(AuthNConstants.TRUE_KEY)) {
			// soapBodyElem11_2.addTextNode(AuthNConstants.FALSE_KEY);
			// } else {
			// soapBodyElem11_2.addTextNode(AuthNConstants.TRUE_KEY);
			// }

			SOAPElement soapBodyElem22 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem22_1 = soapBodyElem22.addChildElement("claimUri", "xsd");
			soapBodyElem22_1.addTextNode("http://wso2.org/claims/identity/accountLocked");
			SOAPElement soapBodyElem22_2 = soapBodyElem22.addChildElement("claimValue", "xsd");
			String islocked = user.getUserClaims().getIsLocked();
			if (islocked.equalsIgnoreCase(AuthNConstants.TRUE_KEY)) {
				soapBodyElem22_2.addTextNode(AuthNConstants.TRUE_KEY);
			} else {
				soapBodyElem22_2.addTextNode(AuthNConstants.FALSE_KEY);
			}

			SOAPElement soapBodyElem10 = soapBodyElem.addChildElement("profileName", "ser");
			soapBodyElem10.addTextNode("default");
			SOAPElement soapBodyElem15 = soapBodyElem.addChildElement("tenantDomain", "ser");
			soapBodyElem15.addTextNode(domain);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "registerUser");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.getldapProperties().getProperty("identityServerUsername", "admin@wso2.com") + ":"
							+ AuthNUtil.getldapProperties().getProperty("identityServerPassword", AuthNConstants.ADMIN)).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			soapMessage.writeTo(stream);
			String message = new String(stream.toByteArray(), "utf-8");
			logger.debug("SoapRequest to createUser: " + message);
			return soapMessage;
		} catch (SOAPException | IOException | PropertiesConfigException e) {
			throw new UserRepoUpdateException("Unable to costruct SoapMessage for User Registration !", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param responseXml
	 * @return
	 * @throws TokenValidationException
	 */
	public String validateTokenProcessBean(String responseXml) throws InvalidAuthenticationException {
		logger.debug(" . validate method of IdentityService");
		logger.debug(" . oauthValidateXml :" + responseXml);
		Document document = generateDocumentFromString(responseXml);
		return fetchRequiredValuesFromDocument(document);
	}// ..end of the method

	private Document generateDocumentFromString(String responseXml) throws InvalidAuthenticationException {
		logger.debug(". generateDoocumentFromString method of IdentityService" + responseXml);
		Document xmlDocument = null;
		if (responseXml != null) {
			builderFactory.setNamespaceAware(true);
			try {
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				InputSource insrc = new InputSource(new StringReader(responseXml));
				xmlDocument = builder.parse(insrc);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new InvalidAuthenticationException("Unable to Build the document object from the xml string ", e);
			}
		}
		return xmlDocument;
	}// ..end of the method

	private String fetchRequiredValuesFromDocument(Document document) throws InvalidAuthenticationException {
		logger.debug(". generateXpathExpression method of IdentityService");
		try {
			return document.getChildNodes().item(0).getFirstChild().getFirstChild().getFirstChild().getFirstChild()
					.getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling()
					.getTextContent();
		} catch (Exception e) {
			throw new InvalidAuthenticationException("Access Denied. Authentication failed - System error occurred", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param accessToken
	 * @param authorization
	 * @return
	 * @throws TokenValidationException
	 */
	public String validateAccessToken(String accessToken, String authorization) throws InvalidAuthenticationException {
		logger.debug(". validateAccessToken method of IdentityService");
		String strMsg1 = null;
		AuthNUtil.disablesslTrustValidation();
		System.setProperty("java.net.useSystemProxies", AuthNConstants.TRUE_KEY);
		try {
			String outhValidatorURL = AuthNUtil.getBasewso2Properties().getProperty("oauthValidatorServiceURL");
			SOAPConnectionFactory soapConnFactory;
			soapConnFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapCon = soapConnFactory.createConnection();
			MessageFactory msgFct = MessageFactory.newInstance();
			SOAPMessage message = msgFct.createMessage();
			SOAPPart mySPart = message.getSOAPPart();
			SOAPEnvelope myEnvp = mySPart.getEnvelope();
			Name xsdName = myEnvp.createName(AuthNConstants.XSD_NAMESPACE1);
			Name xsd1Name = myEnvp.createName(AuthNConstants.XSD_NAMESPACE2);
			myEnvp.addAttribute(xsdName, "http://org.apache.axis2/xsd");
			myEnvp.addAttribute(xsd1Name, "http://dto.oauth2.identity.carbon.wso2.org/xsd");
			SOAPBody body = myEnvp.getBody();
			Name bodyName = myEnvp.createName("xsd:validate");
			SOAPBodyElement gltp = body.addBodyElement(bodyName);
			SOAPElement myxsdvalidationReqDTO = gltp.addChildElement(AuthNConstants.VALIDATION_REQUEST_DTO, "xsd");
			SOAPElement myxsd1accessToken = myxsdvalidationReqDTO.addChildElement(AuthNConstants.ACCESS_TOKEN, "xsd");
			SOAPElement myxsd1tokenType = myxsd1accessToken.addChildElement(AuthNConstants.TOKEN_TYPE, "xsd1");
			myxsd1tokenType.addTextNode(AuthNConstants.TOKEN_TYPE_VALUE);
			SOAPElement myxsd1identifier = myxsd1accessToken.addChildElement(AuthNConstants.IDENTIFIER, "xsd1");
			myxsd1identifier.addTextNode(accessToken);
			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader(AuthNConstants.AUTHERIZATION, "Basic " + authorization);
			message.saveChanges();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			message.writeTo(out);
			String strMsg = new String(out.toByteArray());
			logger.debug(strMsg);
			String url = outhValidatorURL;
			SOAPMessage resp = soapCon.call(message, url);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream();
			resp.writeTo(out1);
			strMsg1 = new String(out1.toByteArray());
			logger.debug("oauth validator xml response : " + strMsg1);
			return strMsg1;
		} catch (IOException | UnsupportedOperationException | SOAPException | PropertiesConfigException e) {
			throw new InvalidAuthenticationException("unable to generate the response xml of the validate token ", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param domain
	 * @return
	 * @throws InvalidAttributesFetchException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUserAttributes(String userName, String domain) throws UserProfileFetchException {
		logger.debug("inside getUserAttributes method userName :: " + userName + " domain :: " + domain);
		Hashtable<String, Object> env = (Hashtable<String, Object>) AuthNUtil.getLdapContextAsMap();
		DirContext ctx = null;
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Attributes answer = null;
			ctx = new InitialDirContext(env);
			if (!domain.equalsIgnoreCase("carbon.super")) {
				answer = ctx
						.getAttributes("uid=" + userName.trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG");
			} else {
				answer = ctx.getAttributes("uid=" + userName.trim() + ",ou=Users" + ",dc=WSO2,dc=ORG");
			}
			logger.debug("answer : " + answer.toString());
			resultMap.put(LdapConstant.ADDRESS1_KEY, AuthNUtil.getEachAttribute(LdapConstant.ADDRESS1, answer));
			resultMap.put(LdapConstant.COMPANY_NAME_KEY, AuthNUtil.getEachAttribute(LdapConstant.COMPANY_NAME, answer));
			resultMap.put(LdapConstant.COUNTRY_KEY, AuthNUtil.getEachAttribute(LdapConstant.COUNTRY, answer));
			resultMap.put(LdapConstant.EMAIL_KEY, AuthNUtil.getEachAttribute(LdapConstant.EMAIL, answer));
			resultMap.put(LdapConstant.FIRST_NAME_KEY, AuthNUtil.getEachAttribute(LdapConstant.FIRST_NAME, answer));
			resultMap.put(LdapConstant.USER_NAME_KEY, AuthNUtil.getEachAttribute(LdapConstant.USER_NAME, answer));
			resultMap.put(LdapConstant.LAST_NAME_KEY, AuthNUtil.getEachAttribute(LdapConstant.LAST_NAME, answer));
			resultMap.put(LdapConstant.MOBILE_KEY, AuthNUtil.getEachAttribute(LdapConstant.MOBILE, answer));
			resultMap.put(LdapConstant.TELEPHONE_KEY, AuthNUtil.getEachAttribute(LdapConstant.TELEPHONE, answer));
			resultMap.put(LdapConstant.TENANT_SITE_KEY, AuthNUtil.getEachAttribute(LdapConstant.TENANT_SITE, answer));
			resultMap.put(LdapConstant.TITLE_KEY, AuthNUtil.getEachAttribute(LdapConstant.TITLE, answer));
			resultMap.put(LdapConstant.EMAIL_KEY, AuthNUtil.getEachAttribute(LdapConstant.EMAIL, answer));
			resultMap.put("active", AuthNUtil.getEachAttribute("active", answer));
			resultMap.put(AuthNConstants.REGION, AuthNUtil.getEachAttribute(AuthNConstants.REGION, answer));
			resultMap.put("locality", AuthNUtil.getEachAttribute("localityName", answer));
			resultMap.put(AuthNConstants.POSTAL_CODE, AuthNUtil.getEachAttribute(AuthNConstants.POSTAL_CODE, answer));
			resultMap.put(AuthNConstants.STATE, AuthNUtil.getEachAttribute("stateOrProvinceName", answer));
			resultMap.put("lastModifiedDate", AuthNUtil.getEachAttribute("lastModifiedDate", answer));
			resultMap.put("createdDate", AuthNUtil.getEachAttribute("createdDate", answer));
			resultMap.put(AuthNConstants.POSTAL_ADDRESS,
					AuthNUtil.getEachAttribute(AuthNConstants.POSTAL_ADDRESS, answer));

		} catch (NamingException e) {
			throw new UserProfileFetchException("Unsuccessfull in searching the attributes from ldap: ", e,
					e.getMessage(), 401);
		} finally {
			try {
				ctx.close();
			} catch (NamingException e) {
				logger.error("Unable to close the ldap Context: ", e);
			}
		}
		return resultMap;
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param newPassword
	 * @param roleList
	 * @return
	 * @throws CredentialUpdateException
	 */
	public SOAPMessage getChangeUserPasswordSoapMessage(String username, String domain, String newPassword,
			String oldPassword) throws CredentialUpdateException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.identity.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("changeUserPassword", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("newPassword", "ser");
			soapBodyElem1.addTextNode(newPassword);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("oldPassword", "ser");
			soapBodyElem2.addTextNode(oldPassword);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "changeUserPassword");
			String authorization = new String(
					Base64.encodeBase64((username + AuthNConstants.AT_SIGN + domain + ":" + oldPassword).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new CredentialUpdateException("Unable to costruct SoapMessage for User Registration !", e,
					e.getMessage(), 500);
		}
	}// ..end of the method

	/**
	 * This method is used to set the headers like accessToken,refreshToken in
	 * the exchange
	 * 
	 * @param exchange
	 *            : Exchange Object
	 * @param oauthAttributes
	 *            : oauthAttributes String
	 * @throws InvalidAccessTokenException
	 * @throws PropertiesConfigException
	 * @throws InvalidTenantSiteException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> buildOauth2Headers(String oauthAttributes, String userName, String domain)
			throws InvalidAuthenticationException, PropertiesConfigException {
		Map<String, String> map = new HashedMap();
		if (oauthAttributes != null) {
			String[] attributesSearchable = { "sn", "description" };
			String tenantSite;
			try {
				Properties configs = AuthNUtil.getldapProperties();
				tenantSite = getTenantSitefromWso2Ldap(userName, domain, configs.getProperty("baseDN"),
						configs.getProperty("ldapProviderUrl"), configs.getProperty("adminSecPrincipal"),
						configs.getProperty("identityServerPassword"), attributesSearchable);
				logger.debug("TenantIdSiteId: " + tenantSite);
			} catch (InvalidAuthenticationException e) {
				throw new InvalidAuthenticationException("Unable to query the tenantId/siteId  ", e);
			}
			try {
				String[] tenantSiteArray = tenantSite.split("/");

				String tenantId = tenantSiteArray[0];
				String siteId = tenantSiteArray[1];
				if (tenantId != null && siteId != null) {
					map.put("tenantId", tenantId);
					map.put("siteId", siteId);
				}
				JSONObject responseJsonObj = new JSONObject(oauthAttributes);
				if (responseJsonObj.has(OAuth.OAUTH_ACCESS_TOKEN)) {
					String accessToken = responseJsonObj.getString(OAuth.OAUTH_ACCESS_TOKEN);
					map.put(OAuth.OAUTH_ACCESS_TOKEN, accessToken);
				} else {
					throw new InvalidAuthenticationException("unautheroized", new Exception());
				}
				map.put(OAuth.OAUTH_REFRESH_TOKEN, responseJsonObj.getString(OAuth.OAUTH_REFRESH_TOKEN));
				map.put(OAuth.OAUTH_EXPIRES_IN, responseJsonObj.getString(OAuth.OAUTH_EXPIRES_IN));
			} catch (JSONException e) {
				throw new InvalidAuthenticationException(
						"unable to parse the json to get the key value " + e.getMessage(), e);
			}
		}
		return map;
	}// end of method settingOauthHeadersInExchange

	/**
	 * This method is used to get the tenant and site from wso2Is LDAP
	 * 
	 * @param searchableUserName
	 *            : searchableUserName String
	 * @param tenantDomain
	 *            : tenantDomain String
	 * @param baseDN
	 *            : baseDN String
	 * @param ldapProviderUrl
	 *            : ldapProviderUrl String
	 * @param adminSecPrincipal
	 *            : adminSecPrincipal String
	 * @param adminPassword
	 *            : adminPassword String
	 * @param attributesSearchable
	 *            : attributesSearchable String[]
	 * @return String with the combination of the tenant and site
	 * @throws InvalidTenantSiteException
	 */
	public String getTenantSitefromWso2Ldap(String searchableUserName, String tenantDomain, String baseDN,
			String ldapProviderUrl, String adminSecPrincipal, String adminPassword, String[] attributesSearchable)
			throws InvalidAuthenticationException {
		logger.debug(". getTenantSitefromWso2Ldap method of Key2actSecurityRequestBuilder ");
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapProviderUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, adminSecPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, adminPassword);
		String tenantSite = null;
		try {
			LdapContext ctx = new InitialLdapContext(env, null);
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String[] attrIDs = attributesSearchable;
			constraints.setReturningAttributes(attrIDs);
			// String queryString = "uid=" + searchableUserName +
			// ",ou=users,ou=" + tenantDomain + "," + baseDN;
			String queryString;
			if (tenantDomain.equalsIgnoreCase("carbon.super")) {
				queryString = "uid=" + searchableUserName + ",ou=Users," + baseDN;
				tenantSite = "all/all";
			} else {
				queryString = "uid=" + searchableUserName + ",ou=users,ou=" + tenantDomain + "," + baseDN;

				NamingEnumeration<SearchResult> result = ctx.search(queryString, "cn=" + searchableUserName,
						constraints);
				if (result.hasMore()) {
					Attributes attrs = ((SearchResult) result.next()).getAttributes();
					tenantSite = attrs.get(attrIDs[1]).get().toString().trim();
				} else {
					throw new InvalidAuthenticationException("unable to get the tenant and site from the ldap");
				}
			}

		} catch (NamingException | InvalidAuthenticationException | NullPointerException e) {
			throw new InvalidAuthenticationException("unable to get the tenant and site from the ldap", e);
		}
		return tenantSite;
	}// end of method getTenantSitefromWso2Ldap

	/**
	 * This method is used to post the oauth URL
	 * 
	 * @param targetURL
	 *            : targetURL String
	 * @param urlParameters
	 *            : urlParameters String
	 * @param clientCredentials
	 *            : clientCredentials String
	 * @return String of the accesToken,refreshToken,ExpiryTime
	 */
	public String executePost(String targetURL, String urlParameters, String clientCredentials) {
		logger.debug(". executePost method of Key2actSecurityRequestBuilder " + urlParameters);
		HttpURLConnection connection = null;
		String line;
		String string = null;
		try {

			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Authorization", "Basic " + clientCredentials);
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setDoInput(true);
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			// wr.flush();
			// wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			string = response.toString();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return string;
	}// end of method executePost

	/**
	 * 
	 * @param requestValue
	 * @return
	 * @throws SAMLResponseException
	 */
	public String getXMLResponseValue(String requestValue) throws InvalidAuthenticationException {
		String encodeValue = null;
		byte[] valueDecoded = null;
		String xmlString = null;
		try {
			if (requestValue != null) {
				String[] dateInStringAttArray = requestValue.replaceAll("\\[|\\]", "").split("<input");
				if (dateInStringAttArray != null) {
					for (String dateInStringAtt : dateInStringAttArray) {
						if (dateInStringAtt.contains("type='hidden' name='SAMLResponse'")) {
							String[] value = dateInStringAtt.split("'");
							encodeValue = value[5];
							logger.debug("responeEncodedValue from the saml response  : " + encodeValue);

						} // end of if check for the SAMLResponse value which is
							// of encode value
					}
				} else {
					throw new InvalidAuthenticationException("SAML Response encoded value does not exists ");
				}
				logger.info("Encoded Value From The SAML Response : " + encodeValue);
				valueDecoded = org.opensaml.xml.util.Base64.decode(encodeValue);
				logger.debug("Decoded the saml response  " + new String(valueDecoded));
				String decodeXml = new String(valueDecoded);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new InputSource(new StringReader(decodeXml)));
				Node nodeName = doc.getChildNodes().item(0).getFirstChild().getNextSibling().getNextSibling()
						.getNextSibling();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "yes");

				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(nodeName);
				transformer.transform(source, result);

				xmlString = result.getWriter().toString();
				logger.debug("SAML2 Assertion xml Response String :" + xmlString);

			} // end of if checks the HTML SAML string is present and decoding
				// the string
			else {
				throw new InvalidAuthenticationException("Request String is null or it is empty");
			}
		} catch (NullPointerException | SAXException | IOException | ParserConfigurationException
				| TransformerFactoryConfigurationError | TransformerException e) {
			throw new InvalidAuthenticationException("Authentication failure. Wrong username or password is provided  ",
					e);
		}
		return xmlString;
	}// ..end of the method

	/**
	 * 
	 * @param encodedBasicAuthUNamePwd
	 * @return
	 * @throws InvalidInitException
	 */
	public String buildCompleteRequestURLString(String encodedBasicAuthUNamePwd) throws InvalidInitException {
		logger.debug(". buildCompleteRequestURLString method of the Key2actSecurityRequestBuilder");
		String authEncodedValue = null;
		if (!AuthNUtil.isEmpty(encodedBasicAuthUNamePwd)) {
			String[] autherizationArrayVal = encodedBasicAuthUNamePwd.split(" ");
			authEncodedValue = autherizationArrayVal[1];
		} else {
		}

		String requestUrl = null;
		Properties configs = null;
		try {
			configs = AuthNUtil.getSAML2OAuth2Config();
			AuthNUtil.initDefaultBootstrap(configs);
			System.setProperty("javax.net.ssl.trustStore", configs.getProperty(AuthNConstants.KeyStore));
			System.setProperty("javax.net.ssl.trustStorePassword",
					configs.getProperty(AuthNConstants.KeyStorePassword));
			synchronized (this) {
				if (credential == null) {
					synchronized (this) {
						SSOAgentCredential credential = (SSOAgentCredential) Class
								.forName(SSOAgentConfigs.getSSOAgentCredentialImplClass()).newInstance();
						credential.init();
						this.credential = new X509CredentialImpl(credential);
					}
				}
			} // ..end of credential build
			AuthnRequest authnRequest = buildAuthRequest(configs);
			logger.debug("authnRequest:  " + authnRequest);
			String encodedAuthRequestString = authRequestMarshaller(authnRequest);
			requestUrl = configs.getProperty(AuthNConstants.SAML_IdPUrl).trim() + "?"
					+ buildQueryParams(encodedAuthRequestString, authEncodedValue);
		} catch (SSOAgentException | ConfigurationException | PropertiesConfigException | MarshallingException
				| IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new InvalidInitException("Unable to initialize with valid values: " + e.getMessage(), e);
		}
		return requestUrl;
	}// ..end of the method

	/**
	 * This method is used to build the query params for the request url of saml
	 * sso
	 * 
	 * @param encodedAuthnRequestString
	 * @param authEncodedValu
	 * @return
	 * @throws SSOAgentException
	 * @throws UnsupportedEncodingException
	 * @throws PropertiesConfigException 
	 */
	public String buildQueryParams(String encodedAuthnRequestString, String authEncodedValu)
			throws SSOAgentException, UnsupportedEncodingException, PropertiesConfigException {
		logger.debug(". buildQueryParams method of  Key2actSecurityRequestBuilder ");
		StringBuilder httpQueryString = new StringBuilder(
				"sectoken=" + authEncodedValu + "&SAMLRequest=" + encodedAuthnRequestString);
		httpQueryString.append("&RelayState="
				+ URLEncoder.encode(AuthNUtil.getSAML2OAuth2Config().getProperty(AuthNConstants.SAML_IssuerID), "UTF-8").trim());
		SSOAgentUtils.addDeflateSignatureToHTTPQueryString(httpQueryString, credential);
		logger.info("The query params for the SAML SSO Request URL is :" + httpQueryString.toString());
		return httpQueryString.toString().trim();
	}// ..end of the method buildQueryParams

	/**
	 * This method is used to get the xml file of the SAMLP
	 * 
	 * @param authnRequest
	 *            : AuthnRequest Object
	 * @return marshaled XML of SAMLP as a string
	 * @throws MarshallingException
	 * @throws IOException
	 */
	public String authRequestMarshaller(AuthnRequest authnRequest) throws MarshallingException, IOException {
		logger.debug(". authRequestMarshaller method of Key2actSecurityRequestBuilder ");
		Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authnRequest);
		Element authDOM = marshaller.marshall(authnRequest);
		StringWriter rspWrt = new StringWriter();
		XMLHelper.writeNode(authDOM, rspWrt);
		String requestMessage = rspWrt.toString();
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		deflaterOutputStream.write(requestMessage.getBytes());
		deflaterOutputStream.close();
		String encodedRequestMessage = org.opensaml.xml.util.Base64.encodeBytes(requestMessage.getBytes(),
				org.opensaml.xml.util.Base64.DONT_BREAK_LINES);
		return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();
	}// ..end of the method authRequestMarshaller

	/**
	 * This method is used to build the auth request
	 * 
	 * @return AuthnRequest Object
	 */
	public AuthnRequest buildAuthRequest(Properties properties) {
		logger.debug(". AuthnRequest method of Key2actSecurityRequestBuilder ");
		String issuerId = properties.getProperty(AuthNConstants.SAML_IssuerID);
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject(AuthNConstants.ISSUER_URN, AuthNConstants.ISSUER,
				AuthNConstants.SAML_ELEMENT);
		issuer.setValue(issuerId);
		String issuerUrl = properties.getProperty(AuthNConstants.SAML_ConsumerUrl);
		DateTime issueInstant = new DateTime();
		AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
		AuthnRequest authnRequest = authnRequestBuilder.buildObject(AuthNConstants.ISSUER_PROTOCOL_URN,
				AuthNConstants.AUTHN_REQ, AuthNConstants.SAML_ELEMENT);
		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setIssueInstant(issueInstant);
		authnRequest.setProtocolBinding(AuthNConstants.AUTH_REQ_PROTOCOL_BIND_URN);
		authnRequest.setAssertionConsumerServiceURL(issuerUrl);
		authnRequest.setIssuer(issuer);
		authnRequest.setID(new Random().toString());
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setDestination(AuthNConstants.SAML_DESTINATION);
		return authnRequest;
	}// ..end of the method buildAuthRequest

	/**
	 * This method is used for authenticating the user by using the SAML request
	 * URL and generating the response as a HTML in the string format
	 * 
	 * @param requestURL
	 *            : requestURL : String
	 * @throws UnauthorizedException
	 * @throws TrustStoreCertificateException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public String userAuthentication(String requestURL) throws InvalidAuthenticationException {
		logger.debug(". userAuthentication method of Key2actAuthenticator class" + requestURL);
		URL obj;
		AuthNUtil.disablesslTrustValidation();
		try {
			obj = new URL(requestURL);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			String responseSAMLHTML = response.toString();
			logger.debug("SAML response in the format of HTML" + responseSAMLHTML);
			return responseSAMLHTML;
		} catch (IOException e) {
			logger.error("This application is not registered with the particular tenant" + e.getMessage());
			throw new InvalidAuthenticationException(
					"This application is not registered with the particular tenant: or " + e.getMessage()
							+ e.getMessage());
		}

	}// end of method userAuthentication

	public String getUserEnabledProperty(String userName, String domain)
			throws InvalidAttributesFetchException, DomainIdentificationException {
		String claimUrl = "http://wso2.org/claims/identity/accountDisabled";
		AuthNAdminDao adminDao = new AuthNAdminDao();
		Domain domainObhect = adminDao.getDomainAttributes(domain);
		SOAPMessage soapReq = getClaimGetterSoapMessage(userName,
				domainObhect.getAdminUserName() + AuthNConstants.AT_SIGN + domain, domainObhect.getAdminPassword(),
				claimUrl);
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		String value = null;
		try {
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_REMOTE_USERREPO;
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			soapResponse = soapConnection.call(soapReq, url);
		} catch (SOAPException | PropertiesConfigException e1) {
			throw new InvalidAttributesFetchException("Unable to get status : ", e1);
		}
		if (soapResponse != null) {
			try {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new InvalidAttributesFetchException(
							"Unable to get status : " + soapResponse.getSOAPBody().getFault().getFaultString());
				}
				value = soapResponse.getSOAPBody().getFirstChild().getFirstChild().getTextContent();
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
		try {
			soapConnection.close();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return (value);
	}

	private SOAPMessage getClaimGetterSoapMessage(String userName, String adminUserName, String adminPassword,
			String claimUrl) throws InvalidAttributesFetchException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("getUserClaimValue", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("claim", "ser");
			soapBodyElem3.addTextNode(claimUrl);
			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("profileName", "ser");
			soapBodyElem4.addTextNode("default");
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "getUserClaimValue");
			String authorization = new String(Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new InvalidAttributesFetchException("Unable to costruct SoapMessage for Tenant Registration !", e);
		}
	}

	public String getUserLockProperty(String userName, String domain)
			throws InvalidAttributesFetchException, DomainIdentificationException {

		String claimUrl = "http://wso2.org/claims/identity/accountLocked";
		AuthNAdminDao adminDao = new AuthNAdminDao();
		Domain domainObhect = adminDao.getDomainAttributes(domain);
		SOAPMessage soapReq = getClaimGetterSoapMessage(userName,
				domainObhect.getAdminUserName() + AuthNConstants.AT_SIGN + domain, domainObhect.getAdminPassword(),
				claimUrl);
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		String value = null;
		try {
			String url = AuthNUtil.getBasewso2Properties().getProperty("baseurl") + AuthNConstants.PART_REMOTE_USERREPO;
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			soapResponse = soapConnection.call(soapReq, url);
		} catch (SOAPException | PropertiesConfigException e1) {
			throw new InvalidAttributesFetchException("Unable to get status : ", e1);
		}
		if (soapResponse != null) {
			try {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new InvalidAttributesFetchException(
							"Unable to get status : " + soapResponse.getSOAPBody().getFault().getFaultString());
				}
				value = soapResponse.getSOAPBody().getFirstChild().getFirstChild().getTextContent();
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
		try {
			soapConnection.close();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return (value);
	}
}
