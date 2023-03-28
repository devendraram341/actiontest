package com.leap.authentication.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.attunedlabs.security.exception.SiteRegistrationException;
import com.attunedlabs.security.pojo.SiteDetatils;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.AccountDetail;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.DomainRegistrationException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.TenantIdentificationException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.dao.AuthNAdminDao;
import com.leap.authentication.service.dao.AuthNUserMgmntDao;
import com.leap.authentication.service.helper.AuthNAdminServiceHelper;
import com.leap.authentication.util.AuthNUtil;

public class AuthNAdminServiceImpl implements IAuthNAdminService {

	private static Logger logger = LoggerFactory.getLogger(AuthNAdminServiceImpl.class);
	static String baseUrl;
	static String validationServiceUrl;

	private AuthNAdminServiceHelper helper = new AuthNAdminServiceHelper();
	private AuthNAdminDao authNAdminDao = new AuthNAdminDao();
	private AuthNUserMgmntDao mgmntDao = new AuthNUserMgmntDao();
	private IAccountRegistryService registryService = new AccountRegistryServiceImpl();

//	public static Properties getBasewso2ConfigProperties() throws PropertiesConfigException {
//		// logger.debug(".loadPropertiesFile...");
//		if (properties != null && !(properties.isEmpty())) {
//			return properties;
//
//		} else {
//			throw new PropertiesConfigException("Could not load  from classpath (fileName): "
//					+ AuthNConstants.WSO2_CONFIG_PROPS + ", hence setting defaults:");
//		}
//	}// ..end of the method loadPropertiesFile

	@Override
	public void registerNewAccount(AccountDetail accountDetails) throws AccountRegistrationException {
		if (AuthNUtil.isEmpty(accountDetails.getAccountName()) || AuthNUtil.isEmpty(accountDetails.getTenantId()))
			throw new AccountRegistrationException("Invalid request [accountName/tenantId - is empty]");
		try {
			registryService.addNewAccount(accountDetails.getAccountName().trim(), accountDetails.getTenantId().trim(),
					accountDetails.getExpirationTime());
		} catch (AccountRegistrationException e) {
			throw new AccountRegistrationException(e.getMessage());
		}
	}

	@Override
	public void resetUserPassword(String userName, String newPassword, String domainName)
			throws CredentialUpdateException {
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(newPassword) || AuthNUtil.isEmpty(domainName)) {
			throw new CredentialUpdateException(
					"unable to update password with empty values being requested to process..", null,
					"unable to update password with empty values being requested to process..",
					AuthNConstants.BAD_REQ_CODE);
		}
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
			Domain domainObj = authNAdminDao.getDomainAttributes(domainName);
			System.out.println(domainObj);
			soapResponse = soapConnection.call(helper.getChangeCredentialsTenantAdminSoapMessage(userName.trim(),
					newPassword, domainObj.getAdminUserName() + AuthNConstants.AT_SIGN + domainName,
					domainObj.getAdminPassword()), url);
			logger.info(
					"Succesfully changed the credential attributes: " + soapResponse.getSOAPBody().getTextContent());

			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new CredentialUpdateException(
							"Unauthorized User! " + soapResponse.getSOAPBody().getFault().getValue(), null,
							"Unauthorized User! " + soapResponse.getSOAPBody().getFault().getValue(),
							AuthNConstants.INT_SRVR_CODE);
				}
			}
			soapConnection.close();
		} catch (SOAPException | DomainIdentificationException e1) {
			throw new CredentialUpdateException(
					"unable to update password, since WS-call resulted in internal processing exception: ", null,
					e1.getMessage(), AuthNConstants.INT_SRVR_CODE);
		}
	}// ..end of the method

	@Override
	public void createUsers(List<User> users, String domain, String tenantAdminUname, String tenantAdminPwd)
			throws UserRepoUpdateException, UserRegistrationException {
		if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(tenantAdminUname) || AuthNUtil.isEmpty(tenantAdminPwd)) {
			throw new UserRepoUpdateException(
					"unable to create new users with empty values being requested to process..", new Exception());
		}
		if (!users.isEmpty()) {
			AuthNUtil.disablesslTrustValidation();
			SOAPConnection soapConnection;
			try {
				SOAPConnectionFactory soapConnectionFactory;
				soapConnectionFactory = SOAPConnectionFactory.newInstance();
				soapConnection = soapConnectionFactory.createConnection();
				String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
				for (User user : users) {
					if (AuthNUtil.validateUsername(user.getUserName()))
						throw new UserRepoUpdateException("Invalid format for username! : " + user.getUserName(),
								new Exception());
					SOAPMessage soapResponse = soapConnection.call(helper.getUserSoapMessage(user.getUserName(),
							user.getFirstName(), user.getLastName(), user.getPassword(), domain, user.getRoleList(),
							user.getTitle(), user.getUserClaims(), tenantAdminUname, tenantAdminPwd), url);

					logger.info("Succesfully the user and its attributes: " + soapResponse);

					if (soapResponse != null) {
						logger.debug("SOAP Response with fault: " + soapResponse.getSOAPBody().hasFault());
						if (soapResponse.getSOAPBody().hasFault()) {
							logger.debug("soapResponse :" + soapResponse.getSOAPBody().getFirstChild().getFirstChild()
									.getNextSibling().getTextContent());
							throw new UserRepoUpdateException("User Aready Exist!", new Exception());
						}
					} else {
						try {
							int domainId = authNAdminDao.getDomainIDByDomainName(domain);
							mgmntDao.addNewUserInDB(user.getUserName(), domainId);
						} catch (DomainIdentificationException e) {
							throw new UserRepoUpdateException("Domain is not valid! " + e.getMessage(), e);
						}
					}

					LdapContext ldapCtx = AuthNUtil.getLdapContext();
					ModificationItem[] mods;
					if (AuthNUtil.isEmpty(user.getUserClaims().getCity()))
						mods = new ModificationItem[1];
					else {
						mods = new ModificationItem[2];
						mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(
								AuthNConstants.POSTAL_ADDRESS, user.getUserClaims().getCity().trim()));
					}
					mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("description",
							user.getTenantId().trim() + "/" + user.getSiteId().trim()));
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
					logger.debug(user.getUserName() + " created successfully");
				}
			} catch (UnsupportedOperationException | SOAPException | NamingException | CredentialUpdateException e) {
				throw new UserRepoUpdateException("Unable to registe user to the repository! " + e.getMessage(), e);
			}
			try {
				soapConnection.close();
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		} else {
			throw new UserRepoUpdateException(
					"unable to create new users with empty values being requested to process..", new Exception());
		}
	}// ..end of the method

	@Override
	public List<String> getDomainNameByAccountName(String accountName) throws AccountFetchException {
		return registryService.getDomainNamesByAccountName(accountName);

	}// ..end of the method

	@Override
	public String getDomainNameByTenantId(String tenantId) throws AccountFetchException {
		return registryService.getDomainNameByTenantId(tenantId);
	}// ..end of the method

	@Override
	public String getTenantByDomain(String domain) throws DomainIdentificationException {
		return authNAdminDao.getTenantByDomain(domain);
	}// ..end of the method

	@Override
	public Map<String, String> getTenantIdAndSiteIdByDomain(String domain) throws TenantIdentificationException {
		return authNAdminDao.getTenantIdAndSiteIdByDomain(domain);
	}// ..end of the method

	@Override
	public List<String> getDomainByTenantAndSite(String tenantId, String siteId) throws DomainIdentificationException {
		return authNAdminDao.getDomainByTenantAndSite(tenantId, siteId);
	}// ..end of the method

	@Override
	public int getDomainIDByDomainName(String domainName) throws DomainIdentificationException {
		return authNAdminDao.getDomainIDByDomainName(domainName);
	}// ..end of the method

	// @Override
	// public void registerNewCompany(String companyName) throws
	// CompanyRegistrationException {
	// authNAdminDao.registerNewCompany(companyName);
	// }// ..end of the method
	//
	// @Override
	// public void registerNewDomainByCompany(String companyName, Domain domain)
	// throws DomainRegistrationException {
	// authNAdminDao.registerNewDomainByAccount(companyName, domain);
	// }// ..end of the method

	@SuppressWarnings("unchecked")
	@Override
	public boolean isUserExist(String userName, String domainName) {
		if (AuthNUtil.isEmpty(userName) || AuthNUtil.isEmpty(domainName)) {
			return false;
		}
		Hashtable<String, Object> env = (Hashtable<String, Object>) AuthNUtil.getLdapContextAsMap();
		DirContext ctx = null;
		Attributes answer = null;
		try {
			ctx = new InitialDirContext(env);
			/*
			 * Attributes answer = ctx .getAttributes("mail=" + userName.trim()
			 * + ",ou=users,ou=" + domainName.trim() + ",dc=WSO2,dc=ORG");
			 */
			if (domainName.equalsIgnoreCase("carbon.super")) {
				answer = ctx.getAttributes("uid=" + userName.trim() + ",ou=Users" + ",dc=WSO2,dc=ORG");
			} else {
				answer = ctx.getAttributes(
						"uid=" + userName.trim() + ",ou=users,ou=" + domainName.trim() + ",dc=WSO2,dc=ORG");
			}
			logger.debug("Able to get the user searched in repo! " + answer);
			return true;
		} catch (NamingException e) {
			logger.warn("Invalid user! " + e.getMessage() + " " + e.getCause());
			return false;
		}
	}// ..end of the method

	@SuppressWarnings("unused")
	@Override
	public void deleteUser(String userName, String domain)
			throws UserProfileFetchException, DomainIdentificationException, UserRepoUpdateException {
		String data = "uid=" + userName.trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG";
		SOAPConnectionFactory soapConnectionFactory;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
			try {
				AuthNUtil.disablesslTrustValidation();
				SOAPMessage soapResponse = soapConnection.call(helper.deleteUserSoapMessage(userName, domain), url);
				if (soapResponse != null) {
					logger.debug("SOAP Response Null: " + soapResponse.getSOAPBody().hasFault());
					if (soapResponse.getSOAPBody().hasFault()) {
						logger.debug("soapResponse :" + soapResponse.getSOAPBody().getFirstChild().getFirstChild()
								.getNextSibling().getTextContent());
						throw new UserRepoUpdateException("User does not Exist!", new Exception());
					}
				}
			} catch (CredentialUpdateException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedOperationException | SOAPException e1) {
			e1.printStackTrace();
		}
		try {
			int id = registryService.getDomainIdByDomain(domain);
			logger.debug("DomainId to search for: " + id);
			authNAdminDao.removeUserFromDB(userName.trim(), id);
		} catch (AccountFetchException e) {
			logger.error("" + e.getMessage());
		}
	}// ..end of the method

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Domain> listAllDomains() throws DomainIdentificationException, AccountFetchException {
		SOAPMessage soapMessage = helper.getListAllTenantSoap();
		AuthNUtil.disablesslTrustValidation();
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		List arrList = new ArrayList<String>();
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = baseUrl + AuthNConstants.PART_REMOTE_TENANTREPO;
			soapResponse = soapConnection.call(soapMessage, url);
			try {
				soapResponse = soapConnection.call(soapMessage, url);
			} catch (SOAPException e) {
			}
			logger.info("Successfull !" + soapResponse);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			soapResponse.writeTo(stream);
			String message = new String(stream.toByteArray(), "utf-8");
			logger.debug("SoapResponse to all tenants: " + message);
			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new DomainIdentificationException(
							"Unable to retreive tenants : " + soapResponse.getSOAPBody().getFault().getFaultString());
				}
			}
			soapConnection.close();
			Node res = soapResponse.getSOAPBody().getFirstChild();
			NodeList list = res.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node each = list.item(i);
				String tenant = each.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling()
						.getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling()
						.getTextContent();
				arrList.add(tenant);
			}
		} catch (SOAPException | IOException e1) {
			throw new DomainIdentificationException("Unable to retreive tenants", e1);
		}
		return authNAdminDao.getDomainAccount(arrList);
	}// ..end of the method

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getAllUsers(int pageIndex, int pageSize, String[] domains) throws UserProfileFetchException {
		List userAndsize = new LinkedList<>();
		List<User> listOfUsers = new LinkedList<User>();
		for (String domain : domains) {
			Map<String, Map<String, Object>> allUsers = helper.getAll(domain);
			for (String eachKey : allUsers.keySet()) {
				Map<String, Object> attributes = allUsers.get(eachKey);
				String userName = (String) attributes.get(AuthNConstants.UID);
				if (userName != null) {
					String userNameReturned = helper.compareSearchKeys("*", userName);
					if (!userNameReturned.equals("!@#$%^&*(-)*&^%$#@!")) {
						listOfUsers.add(helper.setNullNotAvailable(helper.setUserDetail(attributes, domain)));
					}
				}
			}
		}
		int totalUsersSize = listOfUsers.size();
		int calculatedPageNos = totalUsersSize / pageSize;
		if (totalUsersSize % pageSize > 0) {
			calculatedPageNos = calculatedPageNos + 1;
		}
		List<User> userListResult = null;
		if (pageIndex <= calculatedPageNos) {
			int front = (pageSize * (pageIndex + 1)) - pageSize;
			int rear = pageSize + front;
			userListResult = new LinkedList<>();
			for (int i = front; i < rear; i++) {
				try {
					userListResult.add(((List<User>) listOfUsers).get(i));
				} catch (Exception e) {
				}
			}
		}
		userAndsize.add(userListResult);
		userAndsize.add((totalUsersSize));
		return userAndsize;
	}// ..end of the method

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List findUserByEmailPaginated(String emailSearchable, String domain, int pageIndex, int pageSize)
			throws UserProfileFetchException {
		List userAndsize = new LinkedList<>();
		List userList = helper.findUserByEmail(emailSearchable, domain);
		int userListSize = ((List<User>) userList.get(0)).size();
		int calculatedPageNos = userListSize / pageSize;
		if (userListSize % pageSize > 0) {
			calculatedPageNos = calculatedPageNos + 1;
		}
		List<User> userListResult = null;
		if (pageIndex <= calculatedPageNos) {
			int front = (pageSize * (pageIndex + 1)) - pageSize;
			int rear = pageSize + front;
			userListResult = new LinkedList<>();
			for (int i = front; i < rear; i++) {
				try {
					userListResult.add(((List<User>) userList.get(0)).get(i));
				} catch (Exception e) {
				}
			}
		}
		userAndsize.add(userListResult);
		userAndsize.add(userListSize);
		return (userAndsize);
	}// ..end of the method

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List findUserByNamePaginated(String nameSearchable, String domain, int pageIndex, int pageSize)
			throws UserProfileFetchException {
		List userAndsize = new LinkedList<>();
		List userList = helper.findUserByName(nameSearchable, domain);
		int userListSize = ((List<User>) userList.get(0)).size();
		int calculatedPageNos = userListSize / pageSize;
		if (userListSize % pageSize > 0) {
			calculatedPageNos = calculatedPageNos + 1;
		}
		List<User> userListResult = null;
		if (pageIndex <= calculatedPageNos) {
			int front = (pageSize * (pageIndex + 1)) - pageSize;
			int rear = pageSize + front;
			userListResult = new LinkedList<>();
			for (int i = front; i < rear; i++) {
				try {
					userListResult.add(((List<User>) userList.get(0)).get(i));
				} catch (Exception e) {
				}
			}
		}
		userAndsize.add(userListResult);
		userAndsize.add(userListSize);
		return (userAndsize);
	}// ..end of the method

	@Override
	public boolean updateUserStatus(String domain, String userName, String emailAddress, boolean accountDisable)
			throws AttributesRegistrationException {
		String claimUri = "http://wso2.org/claims/emailaddress";
		Domain domainObject = null;
		try {
			domainObject = authNAdminDao.getDomainAttributes(domain);
		} catch (DomainIdentificationException e) {
			throw new AttributesRegistrationException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		try {
			SOAPMessage userNameSoapMessage = helper.getUpdateUserClaimSoapMessage(userName,
					domainObject.getAdminUserName() + AuthNConstants.AT_SIGN + domain.trim(),
					domainObject.getAdminPassword(), claimUri, emailAddress);
			SOAPMessage accountDisableSoapMessage = helper.getUpdateUserStatusSoapMessage(userName,
					domainObject.getAdminUserName() + AuthNConstants.AT_SIGN + domain.trim(),
					domainObject.getAdminPassword(), Boolean.toString(accountDisable));
			SOAPMessage soapMessage = userNameSoapMessage;
			AuthNUtil.disablesslTrustValidation();
			SOAPConnection soapConnection = null;
			SOAPConnectionFactory soapConnectionFactory;
			SOAPMessage soapResponse = null;
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
			soapResponse = soapConnection.call(soapMessage, url);
			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new AttributesRegistrationException(
							"Unable to update : " + soapResponse.getSOAPBody().getFault().getFaultString());
				}
			}
			soapConnection.close();
			helper.callUpdateStatus(accountDisableSoapMessage, url);
		} catch (AttributesRegistrationException | SOAPException e) {
			throw new AttributesRegistrationException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		return true;
	}// ..end of the method

	@Override
	public void updateUserStatus(User user, String domain) throws UserRepoUpdateException {
		String userName = user.getUserName();
		UserClaims userClaims = user.getUserClaims();
		String emailAddress = userClaims.getEmailaddress();
		String isActive = userClaims.getIsActive();
		String accountDisable;
		if (isActive.equalsIgnoreCase(AuthNConstants.TRUE_KEY)) {
			accountDisable = AuthNConstants.FALSE_KEY;
		} else {
			accountDisable = AuthNConstants.TRUE_KEY;
		}
		Domain domainObject = null;

		if (emailAddress == null || userName == null || userName.trim().length() == 0
				|| emailAddress.trim().length() == 0) {
			throw new UserRepoUpdateException("Unable to update the email and status ! ", new Exception());
		}
		try {
			domainObject = authNAdminDao.getDomainAttributes(domain);
		} catch (DomainIdentificationException e) {
			throw new UserRepoUpdateException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		try {
			if (accountDisable != null) {
				SOAPMessage accountDisableSoapMessage = helper.getUpdateUserStatusSoapMessage(userName,
						domainObject.getAdminUserName() + AuthNConstants.AT_SIGN + domain.trim(),
						domainObject.getAdminPassword(), accountDisable);

				String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
				helper.callUpdateStatus(accountDisableSoapMessage, url);
			}
		} catch (AttributesRegistrationException | SOAPException e) {
			throw new UserRepoUpdateException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		try {

			helper.setNullNotAvailable(user);
			LdapContext ldapCtx = AuthNUtil.getLdapContext();
			if (!user.getTenantId().equals("") && !user.getSiteId().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("description", user.getTenantId().trim() + "/" + user.getSiteId().trim()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getCountry().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("country", userClaims.getCountry()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!emailAddress.equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("mail", emailAddress));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getMobile().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute(AuthNConstants.MOBILE, userClaims.getMobile()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getOrganization().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("organizationName", userClaims.getOrganization()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getStreetaddress().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("streetAddress", userClaims.getStreetaddress()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getTelephone().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("telephoneNumber", userClaims.getTelephone()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getRegion().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute(AuthNConstants.REGION, userClaims.getRegion()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getIsActive().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("active", userClaims.getIsActive()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getStateOrProvince().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("stateOrProvinceName", userClaims.getStateOrProvince()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!userClaims.getLocality().equals("")) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("localityName", userClaims.getLocality()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}

			if (!(user.getLastName().equals("") || user.getLastName() == null)) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("sn", user.getLastName()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!(user.getTitle().equals("") || user.getTitle() == null)) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("title", user.getTitle()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!(user.getFirstName().equals("") || user.getFirstName() == null)) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("givenName", user.getFirstName()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!(userClaims.getPostalCode().equals("") || userClaims.getPostalCode() == null)) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute(AuthNConstants.POSTAL_CODE, userClaims.getPostalCode()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			if (!(userClaims.getCity().equals("") || userClaims.getCity() == null)) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute(AuthNConstants.POSTAL_ADDRESS, userClaims.getCity()));
				if (domain.trim().equalsIgnoreCase("carbon.super"))
					ldapCtx.modifyAttributes("uid=" + user.getUserName().trim() + ",ou=Users" + ",dc=WSO2,dc=ORG",
							mods);
				else
					ldapCtx.modifyAttributes(
							"uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim() + ",dc=WSO2,dc=ORG",
							mods);
			}
			logger.debug("Name: " + "uid=" + user.getUserName().trim() + ",ou=users,ou=" + domain.trim()
					+ ",dc=WSO2,dc=ORG");

		} catch (UnsupportedOperationException | NamingException e) {
			throw new UserRepoUpdateException(
					"Unable to registe user to the repository! " + e.getMessage() + " " + e.getCause(), e);
		}
	}// ..end of the method

	@Override
	public boolean updateUserLock(String domain, String userName, boolean accountDisable)
			throws AttributesRegistrationException {
		Domain domainObject = null;
		try {
			domainObject = authNAdminDao.getDomainAttributes(domain);
		} catch (DomainIdentificationException e) {
			throw new AttributesRegistrationException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		try {
			SOAPMessage accountLockSoapMessage = helper.getUpdateUserLockSoapMessage(userName,
					domainObject.getAdminUserName() + AuthNConstants.AT_SIGN + domain.trim(),
					domainObject.getAdminPassword(), Boolean.toString(accountDisable));
			AuthNUtil.disablesslTrustValidation();
			SOAPConnection soapConnection = null;
			SOAPConnectionFactory soapConnectionFactory;
			SOAPMessage soapResponse = null;
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = baseUrl + AuthNConstants.PART_REMOTE_USERREPO;
			soapResponse = soapConnection.call(accountLockSoapMessage, url);
			if (soapResponse != null) {
				if (soapResponse.getSOAPBody().hasFault()) {
					throw new AttributesRegistrationException(
							"Unable to update : " + soapResponse.getSOAPBody().getFault().getFaultString());
				}
			}
			soapConnection.close();
		} catch (AttributesRegistrationException | SOAPException e) {
			throw new AttributesRegistrationException(
					"Unable to update the email and status ! " + e.getMessage() + " " + e.getCause(), e);
		}
		return true;
	}// ..end of the method

	@Override
	public boolean deleteDomain(String domain, String company) throws UserRepoUpdateException {
		// if (AuthNUtil.isEmpty(domain) || AuthNUtil.isEmpty(company))
		// throw new UserRepoUpdateException("unable to delete domain with empty
		// values being requested to process..");
		// List<String> domainNameByCompanyName =
		// getDomainNameByCompanyName(company);
		// if (domainNameByCompanyName.size() == 0)
		// throw new UserRepoUpdateException("unable to delete domain with empty
		// values being requested to process..");
		// for (String domainName : domainNameByCompanyName) {
		// if (domainName.equalsIgnoreCase(domain)) {
		// Map<String, Map<String, Object>> allUsers = helper.getAll(domain);
		// if (allUsers.size() > 1)
		// throw new UserRepoUpdateException("Domain with users cannot be
		// deleted...");
		// else {
		// AuthNUtil.disablesslTrustValidation();
		// SOAPConnection soapConnection;
		// try {
		// SOAPConnectionFactory soapConnectionFactory;
		// soapConnectionFactory = SOAPConnectionFactory.newInstance();
		// soapConnection = soapConnectionFactory.createConnection();
		// String url = baseUrl + AuthNConstants.PART_REMOTE_TENANT_MANAGERREPO;
		// SOAPMessage tenantIdSoapResponse =
		// soapConnection.call(helper.getTenantIdSoapMessage(domain),
		// url);
		// String tenantIdStr =
		// tenantIdSoapResponse.getSOAPBody().getFirstChild().getFirstChild()
		// .getTextContent();
		// int tenantId = Integer.parseInt(tenantIdStr);
		// SOAPMessage soapResponse = null;
		// if (tenantId != -1)
		// soapResponse =
		// soapConnection.call(helper.getdeleteTenantSoapMessage(tenantIdStr),
		// url);
		// else
		// throw new InvalidDomainReqException("Invalid domain name", null,
		// "Give a valid domain name",
		// AuthNConstants.BAD_REQ_CODE);
		//
		// if (soapResponse != null) {
		// if (soapResponse.getSOAPBody().hasFault()) {
		// throw new AttributesRemovalException(
		// "Unable to update : " +
		// soapResponse.getSOAPBody().getFault().getFaultString());
		// }
		// }
		// logger.debug("Deleted domain " + domain + " from company " +
		// company);
		// soapConnection.close();
		// try {
		// helper.removeContextFromLdap(domain);
		// authNAdminDao.removeDomainFromDB(domain, company);
		// if (domainNameByCompanyName.size() == 1)
		// authNAdminDao.removeCompany(company);
		// } catch (NamingException e) {
		// System.out.println("error2 " + e.getMessage());
		// }
		// } catch (UnsupportedOperationException | SOAPException e) {
		// throw new AttributesRemovalException("Unable to remove tenant " +
		// e.getMessage());
		// }
		// }
		// }
		// }
		return true;
	}

	// @Override
	// public void registerSite(String accountName, List<SiteDetatils>
	// siteDetails) throws InvalidSiteRegisterException {
	// if (AuthNUtil.isEmpty(accountName.trim()))
	// throw new InvalidSiteRegisterException("Valid " + accountName + " is
	// missing from request body", null,
	// "Valid " + accountName + " is missing from request body",
	// AuthNConstants.BAD_REQ_CODE);
	// try {
	// registryService.addNewSites(siteDetails, accountName);
	// } catch (SiteRegistrationException e) {
	// throw new InvalidSiteRegisterException("Unable to register site.", null,
	// e.getMessage(),
	// AuthNConstants.INT_SRVR_CODE);
	// }
	// }

	@Override
	public void registerNewDomainByAccount(String accountName, Domain domain, boolean deafultAppFlag,
			String defaultAppName) throws DomainRegistrationException {
		if (AuthNUtil.isEmpty(domain.getDomainName()) || AuthNUtil.isEmpty(domain.getSiteId()))
			throw new DomainRegistrationException("Invalid request [domainName/siteId - is empty]");
		if (AuthNUtil.isEmpty(accountName))
			throw new DomainRegistrationException("Invalid request [accountName - is empty]");
		try {
			SiteDetatils siteDetatils = new SiteDetatils();
			siteDetatils.setDomain(domain.getDomainName().trim());
			siteDetatils.setInternalSiteId(domain.getSiteId().trim());
			siteDetatils.setDescription(domain.getDomainName().trim() + " & " + domain.getSiteId().trim());
			siteDetatils.setTimezone(domain.getTimezone().trim());
			String tenantByDomain = registryService.getInternalTenantIdByAccount(accountName);
			if (AuthNUtil.isEmpty(tenantByDomain))
				throw new DomainRegistrationException("No Account found for the given AccountName! ");
			domain.setTenantId(tenantByDomain);
			registryService.addNewSite(siteDetatils, accountName.trim(), domain.getTenantId().trim());
			authNAdminDao.registerNewDomainByAccount(accountName.trim(), domain, deafultAppFlag, defaultAppName);
		} catch (SiteRegistrationException | DomainRegistrationException | AccountFetchException e) {
			throw new DomainRegistrationException(e.getMessage());
		}
	}

	public static void main(String[] args) throws AccountRegistrationException {
		AuthNAdminServiceImpl serviceImpl = new AuthNAdminServiceImpl();
		AccountDetail accountDetails = new AccountDetail();
		accountDetails.setAccountName("ALL");
		accountDetails.setExpirationTime(3600);
		accountDetails.settenantId("all");
		serviceImpl.registerNewAccount(accountDetails);
	}
}
