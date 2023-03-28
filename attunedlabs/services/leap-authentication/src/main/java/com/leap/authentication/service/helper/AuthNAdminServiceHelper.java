package com.leap.authentication.service.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.codec.binary.Base64;

import com.leap.authentication.AuthNConstants;
import com.leap.authentication.LdapConstant;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.AttributesRemovalException;
import com.leap.authentication.exception.CompanyRegistrationException;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.dao.AuthNAdminDao;
import com.leap.authentication.util.AuthNUtil;

public class AuthNAdminServiceHelper {

	/**
	 * 
	 * @param inPut
	 * @param sourceString
	 * @return
	 */
	public String compareSearchKeys(String inPut, String sourceString) {
		String actualSubstringToBeSearched = null;
		if ((inPut.toLowerCase().matches("a\\.\\*") && inPut.toLowerCase().contains(AuthNConstants.ADMIN))
				|| (sourceString.toLowerCase().startsWith(AuthNConstants.ADMIN)
						&& sourceString.toLowerCase().contains(AuthNConstants.ADMIN))) {
			return "!@#$%^&*(-)*&^%$#@!";
		}
		if (inPut.equalsIgnoreCase("") || inPut.equalsIgnoreCase(null) || inPut.equals("*")) {
			return sourceString;
		} else {
			if (inPut.contains("*")) {
				inPut = inPut.replaceAll("\\*", "\\.\\*");
				actualSubstringToBeSearched = inPut;
			} else {
				actualSubstringToBeSearched = inPut;
			}
		}
		if (sourceString.matches(actualSubstringToBeSearched.trim())) {
			return sourceString;
		} else {
			return "!@#$%^&*(-)*&^%$#@!";
		}
	}// ..end of the method

	/**
	 * 
	 * @param nameSearchable
	 * @param domain
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List findUserByName(String nameSearchable, String domain) throws UserProfileFetchException {
		Map<String, Map<String, Object>> allUsers = getAll(domain);
		System.out.println("allUsers in findUserByName :: " + allUsers);
		List usersAndSize = new LinkedList<>();
		List<User> listOfUsers = new LinkedList<User>();
		for (String eachKey : allUsers.keySet()) {
			Map<String, Object> attributes = allUsers.get(eachKey);
			String userName = (String) attributes.get(AuthNConstants.UID);
			if (userName != null) {
				String userNameReturned = compareSearchKeys(nameSearchable, userName);
				if (!userNameReturned.equals("!@#$%^&*(-)*&^%$#@!")) {
					listOfUsers.add(setNullNotAvailable(setUserDetail(attributes, domain)));
				}
			}
		}
		usersAndSize.add(listOfUsers);
		usersAndSize.add(allUsers.size());
		return (usersAndSize);
	}// ..end of the method

	/**
	 * 
	 * @param emailSearchable
	 * @param domain
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List findUserByEmail(String emailSearchable, String domain) throws UserProfileFetchException {
		Map<String, Map<String, Object>> allUsers = getAll(domain);
		System.out.println("allUsers in findUserByEmail :: " + allUsers);
		List usersAndSize = new LinkedList<>();
		List<User> listOfUsers = new LinkedList<User>();
		for (String eachKey : allUsers.keySet()) {
			String emails = AuthNUtil.splitKeysByUid(eachKey);
			String availableNames = compareSearchKeys(emailSearchable, emails);
			if (!availableNames.equals("!@#$%^&*(-)*&^%$#@!")) {
				Map<String, Object> eachUserAttrMap = allUsers.get(eachKey);
				listOfUsers.add(setNullNotAvailable(setUserDetail(eachUserAttrMap, domain)));
			}
		}
		usersAndSize.add(listOfUsers);
		usersAndSize.add(allUsers.size());
		return (usersAndSize);
	}// ..end of the method

	/**
	 * 
	 * @param user
	 * @return
	 */
	public User setNullNotAvailable(User user) {
		UserClaims claims = user.getUserClaims();
		if (AuthNUtil.isEmpty(user.getTenantId()))
			user.setTenantId("");
		if (AuthNUtil.isEmpty(user.getSiteId()))
			user.setSiteId("");
		if (AuthNUtil.isEmpty(user.getUserName()))
			user.setUserName("");
		if (AuthNUtil.isEmpty(user.getFirstName()))
			user.setFirstName("");
		if (AuthNUtil.isEmpty(user.getLastName()))
			user.setLastName("");
		if (AuthNUtil.isEmpty(user.getTitle()))
			user.setTitle("");
		if (AuthNUtil.isEmpty(user.getPassword()) || !AuthNUtil.isEmpty(user.getPassword()))
			user.setPassword("");
		if (AuthNUtil.isEmpty(claims.getOrganization()))
			claims.setOrganization("");
		if (user.getRoleList() == null)
			user.setRoleList(new ArrayList<String>());
		if (AuthNUtil.isEmpty(claims.getMobile()))
			claims.setMobile("");
		if (AuthNUtil.isEmpty(claims.getTelephone()))
			claims.setTelephone("");
		if (AuthNUtil.isEmpty(claims.getLocality()))
			claims.setLocality("");
		if (AuthNUtil.isEmpty(claims.getCountry()))
			claims.setCountry("");
		if (AuthNUtil.isEmpty(claims.getStateOrProvince()))
			claims.setStateOrProvince("");
		if (AuthNUtil.isEmpty(claims.getStreetaddress()))
			claims.setStreetaddress("");
		if (AuthNUtil.isEmpty(claims.getPostalCode()))
			claims.setPostalCode("");
		if (AuthNUtil.isEmpty(claims.getRegion()))
			claims.setRegion("");
		if (AuthNUtil.isEmpty(claims.getEmailaddress()))
			claims.setEmailaddress("");
		if (AuthNUtil.isEmpty(claims.getIsActive()))
			claims.setIsActive("");
		if (AuthNUtil.isEmpty(claims.getIsLocked()))
			claims.setIsLocked("");
		if (AuthNUtil.isEmpty(claims.getStreet()))
			claims.setStreet("");
		return user;
	}// ..end of the method

	/**
	 * 
	 * @param map
	 * @param domain
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	public User setUserDetail(Map<String, Object> map, String domain) throws UserProfileFetchException {
		User user = new User();
		user.setDomain(domain);
		UserClaims claims = new UserClaims();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (key.equalsIgnoreCase(AuthNConstants.UID))
				user.setUserName((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.GIVEN_NAME))
				user.setFirstName((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.LAST_NAME))
				user.setLastName((String) value);

			if (key.equalsIgnoreCase("organizationName"))
				claims.setOrganization((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.MOBILE))
				claims.setMobile((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.POSTAL_ADDRESS))
				claims.setTelephone((String) value);

			if (key.equalsIgnoreCase("localityName"))
				claims.setLocality((String) value);

			if (key.equalsIgnoreCase(LdapConstant.COUNTRY_KEY))
				claims.setCountry(value.toString());

			if (key.equalsIgnoreCase("stateOrProvinceName"))
				claims.setStateOrProvince((String) value);

			if (key.equalsIgnoreCase("streetAddress"))
				claims.setStreetaddress((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.POSTAL_CODE))
				claims.setPostalCode((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.REGION))
				claims.setRegion((String) value);

			if (key.equalsIgnoreCase("mail"))
				claims.setEmailaddress((String) value);

			if (key.equalsIgnoreCase("sn"))
				user.setLastName((String) value);

			if (key.equalsIgnoreCase(LdapConstant.TITLE_KEY))
				user.setTitle((String) value);

			if (key.equalsIgnoreCase("telephoneNumber"))
				claims.setTelephone((String) value);

			if (key.equalsIgnoreCase("createdDate"))
				user.setCreatedDate((String) value);

			if (key.equalsIgnoreCase("lastModifiedDate"))
				user.setLastModifiedDate((String) value);

			if (key.equalsIgnoreCase(AuthNConstants.POSTAL_ADDRESS))
				claims.setCity((String) value);

			if (key.equalsIgnoreCase("active"))
				claims.setIsActive((String) value);
			
			if (key.equalsIgnoreCase("accountLock"))
				claims.setIsLocked((String) value);
		}
//		try {
//			AuthNUserMgmntServiceHelper helper = new AuthNUserMgmntServiceHelper();
//			String isAct = helper.getUserEnabledProperty(user.getUserName(), domain);
//			if (isAct == null || isAct.equalsIgnoreCase(AuthNConstants.TRUE_KEY))
//
//				claims.setIsActive(AuthNConstants.FALSE_KEY);
//			else
//				claims.setIsActive(AuthNConstants.TRUE_KEY);
//		} catch (InvalidAttributesFetchException | DomainIdentificationException | PropertiesConfigException e) {
//			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
//					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
//		}
//
//		try {
//			AuthNUserMgmntServiceHelper helper = new AuthNUserMgmntServiceHelper();
//			claims.setIsLocked(helper.getUserLockProperty(user.getUserName(), domain));
//		} catch (InvalidAttributesFetchException | DomainIdentificationException | PropertiesConfigException e) {
//			throw new UserProfileFetchException("Unable to fetch the user attributes successfully " + e.getMessage(), e,
//					"Unable to fetch the user attributes successfully", AuthNConstants.INT_SRVR_CODE);
//		}
		user.setUserClaims(claims);
		return user;
	}// ..end of the method

	/**
	 * 
	 * @param domain
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Map<String, Object>> getAll(String domain) {
		Map<String, Map<String, Object>> pMap = null;
		try {
			NamingEnumeration objs = null;
			LdapContext ctx = AuthNUtil.getLdapContext();
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			if (domain.equalsIgnoreCase("carbon.super")) {
				objs = ctx.search("ou=Users" + ",dc=WSO2,dc=ORG", "(objectClass=*)", searchControls);
			} else {
				objs = ctx.search("ou=" + domain.trim() + ",dc=WSO2,dc=ORG", "(objectClass=*)", searchControls);
			}
			pMap = new LinkedHashMap();
			while (objs.hasMoreElements()) {
				SearchResult match = (SearchResult) objs.nextElement();
				Attributes attrs = match.getAttributes();
				NamingEnumeration e = attrs.getAll();
				Map<String, Object> map = new LinkedHashMap<>();
				while (e.hasMoreElements()) {
					Attribute attr = (Attribute) e.nextElement();
					for (int i = 0; i < attr.size(); i++) {
						map.put(attr.getID(), attr.get(i));
					}
				}
				System.out.println("pMap in getAll :: " + pMap);
				pMap.put(match.getName(), map);
				if (pMap.containsKey("")) {
					pMap.remove("");
				}
				if (pMap.containsKey("ou=groups")) {
					pMap.remove("ou=groups");
				}
				if (pMap.containsKey("uid=admin,ou=users")) {
					pMap.remove("uid=admin,ou=users");
				}
				if (pMap.containsKey("uid=krbtgt")) {
					pMap.remove("uid=krbtgt");
				}
				if (pMap.containsKey("uid=admin,ou=Users")) {
					pMap.remove("uid=admin,ou=Users");
				}
				if (pMap.containsKey("uid=ldap")) {
					pMap.remove("uid=ldap");
				}
				if (pMap.containsKey("uid=admin@wso2.com,ou=Users")) {
					pMap.remove("uid=admin@wso2.com,ou=Users");
				}
				if (pMap.containsKey("cn=admin,ou=groups")) {
					pMap.remove("cn=admin,ou=groups");
				}
				if (pMap.containsKey("ou=users")) {
					pMap.remove("ou=users");
				}
				if (pMap.containsKey("cn=Leap,ou=groups")) {
					pMap.remove("cn=Leap,ou=groups");
				}
			}
			return pMap;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return pMap;
	}

	/**
	 * Generate the SoapMessage for adding new Tenant
	 * 
	 * @param adminUserName
	 * @param adminPassword
	 * @param adminEmail
	 * @param adminFName
	 * @param adminLName
	 * @param domainName
	 * @param adminUN
	 * @param adminUP
	 * @return
	 * @throws AttributesRegistrationException
	 */
	public SOAPMessage getNewTenanatAddSoapMessage(String adminUserName, String adminPassword, String adminEmail,
			String adminFName, String adminLName, String domainName, String adminUN, String adminUP)
			throws CompanyRegistrationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.tenant.carbon.wso2.org";
			String xsdURI = "http://beans.common.stratos.carbon.wso2.org/xsd";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", xsdURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("addTenant", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("tenantInfoBean", "ser");
			SOAPElement soapBodyElem2 = soapBodyElem1.addChildElement("active", "xsd");
			soapBodyElem2.addTextNode("true");
			SOAPElement soapBodyElem3 = soapBodyElem1.addChildElement("admin", "xsd");
			soapBodyElem3.addTextNode(adminUserName);
			SOAPElement soapBodyElem4 = soapBodyElem1.addChildElement("adminPassword", "xsd");
			soapBodyElem4.addTextNode(adminPassword);
			SOAPElement soapBodyElem5 = soapBodyElem1.addChildElement("email", "xsd");
			soapBodyElem5.addTextNode(adminEmail);
			SOAPElement soapBodyElem6 = soapBodyElem1.addChildElement("firstname", "xsd");
			soapBodyElem6.addTextNode(adminFName);
			SOAPElement soapBodyElem7 = soapBodyElem1.addChildElement("lastname", "xsd");
			soapBodyElem7.addTextNode(adminLName);
			SOAPElement soapBodyElem8 = soapBodyElem1.addChildElement("tenantDomain", "xsd");
			soapBodyElem8.addTextNode(domainName);

			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "addTenant");
			String authorization = new String(Base64.encodeBase64((adminUN + ":" + adminUP).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			System.out.println("Basic " + authorization);
			soapMessage.saveChanges();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				soapMessage.writeTo(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return soapMessage;
		} catch (SOAPException e) {
			throw new CompanyRegistrationException("Unable to costruct SoapMessage for Tenant Registration !", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param newPassword
	 * @param roleList
	 * @return
	 * @throws CredentialUpdateException
	 */
	public SOAPMessage getChangeCredentialsTenantAdminSoapMessage(String userName, String newPassword,
			String tenantadminUname, String tenantadminPwd) throws CredentialUpdateException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("updateCredentialByAdmin", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("newCredential", "ser");
			soapBodyElem2.addTextNode(newPassword);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "updateCredentialByAdmin");
			String authorization = new String(Base64.encodeBase64((tenantadminUname + ":" + tenantadminPwd).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new CredentialUpdateException("Unable to costruct SoapMessage for User Registration !", e,
					e.getMessage(), AuthNConstants.BAD_REQ_CODE);
		}
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @param password
	 * @param roleList
	 * @return
	 * @throws CredentialUpdateException
	 */
	public SOAPMessage getUserSoapMessage(String userName, String firstName, String lastname, String password,
			String domain, List<String> roleList, String title, UserClaims userClaims, String tenantAdminName,
			String tenantAdminPassword) throws CredentialUpdateException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", "http://common.mgt.user.carbon.wso2.org/xsd");
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("addUser", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("credential", "ser");
			soapBodyElem2.addTextNode(password);
			// for (String roles : roleList) {
			SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("roleList", "ser");
			soapBodyElem3.addTextNode(AuthNConstants.DEFAULT_ROLE);
			// }

			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem4_1 = soapBodyElem4.addChildElement("claimURI", "xsd");
			soapBodyElem4_1.addTextNode("http://wso2.org/claims/country");
			SOAPElement soapBodyElem4_2 = soapBodyElem4.addChildElement("value", "xsd");
			soapBodyElem4_2.addTextNode(userClaims.getCountry());

			SOAPElement soapBodyElem20 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem20_1 = soapBodyElem20.addChildElement("claimUri", "xsd");
			soapBodyElem20_1.addTextNode("http://wso2.org/claims/lastname");
			SOAPElement soapBodyElem20_2 = soapBodyElem20.addChildElement("claimValue", "xsd");
			soapBodyElem20_2.addTextNode(lastname);

			SOAPElement soapBodyElem6 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem6_1 = soapBodyElem6.addChildElement("claimURI", "xsd");
			soapBodyElem6_1.addTextNode("http://wso2.org/claims/mobile");
			SOAPElement soapBodyElem6_2 = soapBodyElem6.addChildElement("value", "xsd");
			soapBodyElem6_2.addTextNode(userClaims.getMobile());

			SOAPElement soapBodyElem7 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem7_1 = soapBodyElem7.addChildElement("claimURI", "xsd");
			soapBodyElem7_1.addTextNode("http://wso2.org/claims/organization");
			SOAPElement soapBodyElem7_2 = soapBodyElem7.addChildElement("value", "xsd");
			soapBodyElem7_2.addTextNode(userClaims.getOrganization());

			SOAPElement soapBodyElem8 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem8_1 = soapBodyElem8.addChildElement("claimURI", "xsd");
			soapBodyElem8_1.addTextNode("http://wso2.org/claims/streetaddress");
			SOAPElement soapBodyElem8_2 = soapBodyElem8.addChildElement("value", "xsd");
			soapBodyElem8_2.addTextNode(userClaims.getStreetaddress());

			SOAPElement soapBodyElem9 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem9_1 = soapBodyElem9.addChildElement("claimURI", "xsd");
			soapBodyElem9_1.addTextNode("http://wso2.org/claims/telephone");
			SOAPElement soapBodyElem9_2 = soapBodyElem9.addChildElement("value", "xsd");
			soapBodyElem9_2.addTextNode(userClaims.getTelephone());

			SOAPElement soapBodyElem13 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem13_1 = soapBodyElem13.addChildElement("claimURI", "xsd");
			soapBodyElem13_1.addTextNode("http://wso2.org/claims/givenname");
			SOAPElement soapBodyElem13_2 = soapBodyElem13.addChildElement("value", "xsd");
			soapBodyElem13_2.addTextNode(firstName);

			SOAPElement soapBodyElem14 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem14_1 = soapBodyElem14.addChildElement("claimURI", "xsd");
			soapBodyElem14_1.addTextNode("http://wso2.org/claims/title");
			SOAPElement soapBodyElem14_2 = soapBodyElem14.addChildElement("value", "xsd");
			soapBodyElem14_2.addTextNode(title);

			SOAPElement soapBodyElem16 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem16_1 = soapBodyElem16.addChildElement("claimUri", "xsd");
			soapBodyElem16_1.addTextNode("http://wso2.org/claims/locality");
			SOAPElement soapBodyElem16_2 = soapBodyElem16.addChildElement("claimValue", "xsd");
			soapBodyElem16_2.addTextNode(userClaims.getLocality());

			SOAPElement soapBodyElem17 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem17_1 = soapBodyElem17.addChildElement("claimUri", "xsd");
			soapBodyElem17_1.addTextNode("http://wso2.org/claims/stateorprovince");
			SOAPElement soapBodyElem17_2 = soapBodyElem17.addChildElement("claimValue", "xsd");
			soapBodyElem17_2.addTextNode(userClaims.getStateOrProvince());

			SOAPElement soapBodyElem18 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem18_1 = soapBodyElem18.addChildElement("claimUri", "xsd");
			soapBodyElem18_1.addTextNode("http://wso2.org/claims/postalcode");
			SOAPElement soapBodyElem18_2 = soapBodyElem18.addChildElement("claimValue", "xsd");
			soapBodyElem18_2.addTextNode(userClaims.getPostalCode());

			SOAPElement soapBodyElem19 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem19_1 = soapBodyElem19.addChildElement("claimUri", "xsd");
			soapBodyElem19_1.addTextNode("http://wso2.org/claims/region");
			SOAPElement soapBodyElem19_2 = soapBodyElem19.addChildElement("claimValue", "xsd");
			soapBodyElem19_2.addTextNode(userClaims.getRegion());

			SOAPElement soapBodyElem21 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem21_1 = soapBodyElem21.addChildElement("claimUri", "xsd");
			soapBodyElem21_1.addTextNode("http://wso2.org/claims/emailaddress");
			SOAPElement soapBodyElem21_2 = soapBodyElem21.addChildElement("claimValue", "xsd");
			soapBodyElem21_2.addTextNode(userClaims.getEmailaddress());

			SOAPElement soapBodyElem11 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem11_1 = soapBodyElem11.addChildElement("claimUri", "xsd");
			soapBodyElem11_1.addTextNode("http://wso2.org/claims/identity/accountDisabled");
			SOAPElement soapBodyElem11_2 = soapBodyElem11.addChildElement("claimValue", "xsd");
			String isActiveValue = userClaims.getIsActive();
			if (isActiveValue.equalsIgnoreCase(AuthNConstants.TRUE_KEY)) {
				soapBodyElem11_2.addTextNode(AuthNConstants.FALSE_KEY);
			} else {
				soapBodyElem11_2.addTextNode(AuthNConstants.TRUE_KEY);
			}

			SOAPElement soapBodyElem22 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem22_1 = soapBodyElem22.addChildElement("claimUri", "xsd");
			soapBodyElem22_1.addTextNode("http://wso2.org/claims/identity/accountLocked");
			SOAPElement soapBodyElem22_2 = soapBodyElem22.addChildElement("claimValue", "xsd");
			String islocked = userClaims.getIsLocked();
			if (islocked.equalsIgnoreCase(AuthNConstants.TRUE_KEY)) {
				soapBodyElem22_2.addTextNode(AuthNConstants.TRUE_KEY);
			} else {
				soapBodyElem22_2.addTextNode(AuthNConstants.FALSE_KEY);
			}

			SOAPElement soapBodyElem10 = soapBodyElem.addChildElement("requirePasswordChange", "ser");
			soapBodyElem10.addTextNode(AuthNConstants.FALSE_KEY);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "addUser");
			String authorization = new String(
					Base64.encodeBase64((tenantAdminName + ":" + tenantAdminPassword).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new CredentialUpdateException("Unable to costruct SoapMessage for User Registration !", e,
					"Unable to costruct SoapMessage for User Registration !", AuthNConstants.INT_SRVR_CODE);
		}
	}// ..end of the method

	@SuppressWarnings("unused")
	public SOAPMessage getListAllTenantSoap() throws DomainIdentificationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://services.mgt.tenant.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("retrieveTenants", "ser");
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "updateCredentialByAdmin");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.ADMIN_USER + ":" + AuthNUtil.ADMIN_PASS).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new DomainIdentificationException("Unable to costruct SoapMessage for retreiving all the tenants !"
					+ e.getMessage() + " " + e.getCause(), e);
		}
	}// ..end of the method

	public SOAPMessage deleteUserSoapMessage(String userName, String domainName)
			throws CredentialUpdateException, DomainIdentificationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();

			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("deleteUser", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "updateCredentialByAdmin");
			AuthNAdminDao adminDao = new AuthNAdminDao();
			Domain domainAttributes = adminDao.getDomainAttributes(domainName);
			String authorization = new String(Base64.encodeBase64((domainAttributes.getAdminUserName()
					+ AuthNConstants.AT_SIGN + domainName + ":" + domainAttributes.getAdminPassword()).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			headers.addHeader("SOAPAction", serverURI + "deleteUser");
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new CredentialUpdateException("Unable to costruct SoapMessage for User Registration !", e,
					e.getMessage(), 500);

		}
	}

	public SOAPMessage getUpdateUserStatusSoapMessage(String userName, String adminUserName, String adminPassword,
			String disable) throws AttributesRegistrationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			String xsd = "http://common.mgt.user.carbon.wso2.org/xsd";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", xsd);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("setUserClaimValues", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem3 = soapBodyElem2.addChildElement("claimURI", "ser");
			soapBodyElem3.addTextNode("http://wso2.org/claims/identity/accountDisabled");
			SOAPElement soapBodyElem4 = soapBodyElem2.addChildElement("value", "ser");
			soapBodyElem4.addTextNode(disable);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "addTenant");
			String authorization = new String(Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new AttributesRegistrationException("Unable to costruct SoapMessage for Tenant Registration !", e);
		}
	}// ..end of the method

	public void callUpdateStatus(SOAPMessage soapMessage, String url)
			throws SOAPException, AttributesRegistrationException {
		SOAPMessage soapMessage1 = soapMessage;
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection1 = null;
		SOAPConnectionFactory soapConnectionFactory1;
		SOAPMessage soapResponse1 = null;
		soapConnectionFactory1 = SOAPConnectionFactory.newInstance();
		soapConnection1 = soapConnectionFactory1.createConnection();
		soapResponse1 = soapConnection1.call(soapMessage1, url);
		if (soapResponse1 != null) {
			if (soapResponse1.getSOAPBody().hasFault()) {
				throw new AttributesRegistrationException(
						"Unable to update : " + soapResponse1.getSOAPBody().getFault().getFaultString());
			}
		}
		soapConnection1.close();
	}

	public SOAPMessage getUpdateUserLockSoapMessage(String userName, String adminUserName, String adminPassword,
			String disable) throws AttributesRegistrationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			String xsd = "http://common.mgt.user.carbon.wso2.org/xsd";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			envelope.addNamespaceDeclaration("xsd", xsd);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("setUserClaimValues", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("claims", "ser");
			SOAPElement soapBodyElem3 = soapBodyElem2.addChildElement("claimURI", "ser");
			soapBodyElem3.addTextNode("http://wso2.org/claims/identity/accountLocked");
			SOAPElement soapBodyElem4 = soapBodyElem2.addChildElement("value", "ser");
			soapBodyElem4.addTextNode(disable);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "addTenant");
			String authorization = new String(Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new AttributesRegistrationException("Unable to costruct SoapMessage for Tenant Registration !", e);
		}
	}// ..end of the method

	public SOAPMessage getUpdateUserClaimSoapMessage(String userName, String adminUserName, String adminPassword,
			String claimUri, String claimValue) throws AttributesRegistrationException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("setUserClaimValue", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("userName", "ser");
			soapBodyElem1.addTextNode(userName);
			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("claimURI", "ser");
			soapBodyElem2.addTextNode(claimUri);
			SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("claimValue", "ser");
			soapBodyElem3.addTextNode(claimValue);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "addTenant");
			String authorization = new String(Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes()),
					Charset.forName(AuthNConstants.CHARSET_UTF_8));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new AttributesRegistrationException("Unable to costruct SoapMessage for Tenant Registration !", e);
		}
	}// ..end of the method

	public SOAPMessage getdeleteTenantSoapMessage(String domain) throws AttributesRemovalException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("deleteTenant", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("tenantId", "ser");
			soapBodyElem1.addTextNode(domain);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "deleteTenant");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.ADMIN_USER + ":" + AuthNUtil.ADMIN_PASS).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new AttributesRemovalException("Unable to costruct SoapMessage for deleting domain !", e);
		}
	}

	public SOAPMessage getTenantIdSoapMessage(String domain) throws AttributesRemovalException {
		MessageFactory messageFactory;
		try {
			messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			String serverURI = "http://service.ws.um.carbon.wso2.org";
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ser", serverURI);
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = soapBody.addChildElement("getTenantId", "ser");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("domain", "ser");
			soapBodyElem1.addTextNode(domain);
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "getTenantId");
			String authorization = new String(
					Base64.encodeBase64((AuthNUtil.ADMIN_USER + ":" + AuthNUtil.ADMIN_PASS).getBytes()),
					Charset.forName("UTF-8"));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			return soapMessage;
		} catch (SOAPException e) {
			throw new AttributesRemovalException("Unable to costruct SoapMessage for getting Tenant Id!", e);
		}
	}

	public void removeContextFromLdap(String domainName) throws NamingException, DomainIdentificationException {
		LdapContext ldapCtx = AuthNUtil.getLdapContext();
		ldapCtx.destroySubcontext("cn=admin,ou=groups,ou=" + domainName + ",dc=WSO2,dc=ORG");
		AuthNAdminDao adminDao = new AuthNAdminDao();
		Domain domain = adminDao.getDomainAttributes(domainName);
		ldapCtx.destroySubcontext(
				"uid=" + domain.getAdminUserName() + ",ou=users,ou=" + domainName + ",dc=WSO2,dc=ORG");
		ldapCtx.destroySubcontext("ou=groups,ou=" + domainName + ",dc=WSO2,dc=ORG");
		ldapCtx.destroySubcontext("ou=users,ou=" + domainName + ",dc=WSO2,dc=ORG");
		ldapCtx.destroySubcontext("ou=" + domainName + ",dc=WSO2,dc=ORG");
	}
}
