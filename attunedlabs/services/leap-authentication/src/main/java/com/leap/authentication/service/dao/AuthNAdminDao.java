package com.leap.authentication.service.dao;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.SecretKeyGenException;
import com.attunedlabs.security.pojo.AccountDetails;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.leap.authentication.AuthNConstants;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.CompanyIdentificationException;
import com.leap.authentication.exception.CompanyRegistrationException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.DomainRegistrationException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.TenantIdentificationException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.helper.AuthNAdminServiceHelper;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;
import com.leap.authentication.util.AuthNUtil;

public class AuthNAdminDao {

	protected Logger logger = LoggerFactory.getLogger(AuthNAdminDao.class.getName());

	static final String SELECT_DOMAIN_COMPANY_ID = "select domainname from domaincompany where companyid=?;";
	static final String GET_COMPANY_ID = "select id from company where companyname = ?;";
	static final String SELECT_DOMAIN_TENANT = "select domainname from domaincompany where tenantid=?;";
	static final String GET_TENANT_BY_DOMAIN = "select tenantid from domaincompany where domainname = ?";
	static final String SELECT_TENANT_AND_SITE_BY_DOMAIN = "select tenantid, siteid from domaincompany where domainname=?";
	static final String SELECT_DOMAIN_BY_TENANT_AND_SITE = "select domainname from domaincompany where tenantid=? and siteid=?";
	static final String SELECT_DOMAINID_DOMAINNAME = "select id from domaincompany where domainname = ?;";
	static final String INSERT_DOMAIN_COMPANY = "insert into domaincompany (domainname,companyid,tenantid,siteid) values (?,?,?,?)";
	static final String DELETE_DOMAIN_COMPANY = "delete from domaincompany where id = ?;";
	static final String DELETE_DOMAIN_ADMIN = "delete from domainadmin where domainid = ?;";
	static final String INSERT_DOMAIN_ADMIN = "insert into domainadmin(domainid, adminusername,adminpassword ,encriptedform)values (?,?,?,?);";
	static final String SELECT_ALL_DOMAIN_COMPANY = "SELECT * FROM  domaincompany WHERE domainname = ? ";
	static final String SELECT_ALL_COMPANY = "SELECT * from company where id = ? ";
	static final String DELETE_USER_DOMAIN = "delete from userdomain where username = ? and domainid = ?;";
	static final String INSERT_COMPANY = "insert into company (companyname) values (?)";
	static final String SELECT_ALL_DOMAIN_ADMIN = "select * from domainadmin where domainid = ?";
	static final String SELECT_COMPANY_NAME = "select companyname from company where id=?";
	static final String DELETE_COMPANY_NAME = "delete from roi.company where companyname=?";
	static final String SELECT_COMPANY_ID = "select companyid from roi.domaincompany where id=?";

//	static Properties configproperties;
	private static final String BASE_URL="baseurl";
//	static {
//		try {
//			properties = new Properties();
//			configproperties = AuthNUtil.getldapProperties();
//			properties = AuthNAdminServiceImpl.getBasewso2ConfigProperties();
//			baseUrl = properties.getProperty("baseurl");
//		} catch (Exception e) {
//		}
//	}

	private AuthNAdminServiceHelper helper = new AuthNAdminServiceHelper();

	/**
	 * 
	 * @param arrList
	 * @return
	 * @throws DomainIdentificationException
	 * @throws AccountFetchException
	 */
	public List<Domain> getDomainAccount(List<String> arrList) throws DomainIdentificationException {
		List<Domain> domainList = new ArrayList<Domain>();
		try {
			IAccountRegistryService registryService = new AccountRegistryServiceImpl();
			for (int i = 0; i < arrList.size(); i++) {
				String tenantByDomain = registryService.getTenantByDomain(arrList.get(i));
				String siteIdByDomain = registryService.getSiteIdByDomain(arrList.get(i));
				int accountIdByDomain = registryService.getAccountIdByDomain(arrList.get(i));
				AccountDetails accountDetails = registryService.getAccountByAccountId(accountIdByDomain);
				Domain domain = new Domain();
				domain.setDomainName(arrList.get(i));
				domain.setTenantId(tenantByDomain);
				domain.setSiteId(siteIdByDomain);
				domain.setCompany(accountDetails.getAccountName());
				domainList.add(domain);
			}
		} catch (AccountFetchException e) {
			throw new DomainIdentificationException("" + e.getMessage() + " " + e.getCause(), e);
		}
		return domainList;
	}

	/**
	 * 
	 * @param userName
	 * @param domainId
	 */
	@SuppressWarnings("unused")
	public void removeUserFromDB(String userName, int domainId) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(DELETE_USER_DOMAIN);
			preparedStatement.setString(1, userName);
			preparedStatement.setInt(2, domainId);
			boolean res = preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}// ..end of the method

	/**
	 * 
	 * @param companyName
	 * @return
	 * @throws DomainIdentificationException
	 */
	public List<String> getDomainNameByCompanyName(String companyName) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		String domainName = null;
		int compnyId;
		if (AuthNUtil.isEmpty(companyName)) {
			throw new DomainIdentificationException("Empty companyName requested to get the domain lists..");
		}
		compnyId = getCompanyIdByName(companyName);
		if (compnyId == 0) {
			throw new DomainIdentificationException("Unable to identify the companyId by companyName ");
		}
		List<String> domainList = new ArrayList<>();
		try {
			preparedStatement = conn.prepareStatement(SELECT_DOMAIN_COMPANY_ID);
			preparedStatement.setInt(1, compnyId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				domainName = resultSet.getString(1);
				domainList.add(domainName);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new DomainIdentificationException("Unable to identify the domain from the given company name: ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return domainList;
	}// ..end of the method

	/**
	 * 
	 * @param tenantId
	 * @return
	 * @throws DomainIdentificationException
	 */
	public String getDomainNameByTenantId(String tenantId) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		String domainName = null;
		try {
			preparedStatement = conn.prepareStatement(SELECT_DOMAIN_TENANT);
			preparedStatement.setString(1, tenantId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				domainName = resultSet.getString(1);
			}
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to identify the domain from the given company name: ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return domainName;
	}// ..end of the method

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws DomainIdentificationException
	 */
	public String getTenantByDomain(String domain) throws DomainIdentificationException {
		Connection connection = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		String tenantid;
		try {
			preparedStatement = connection.prepareStatement(GET_TENANT_BY_DOMAIN);
			tenantid = getTenantIdByDomainName(domain, preparedStatement);
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to process db query to get the tenantId! " + e.getMessage(),
					e);
		} finally {
			AuthNUtil.dbCleanUp(connection, preparedStatement);
		}
		if (AuthNUtil.isEmpty(tenantid)) {
			throw new DomainIdentificationException("Not a valid tenant configured! ");
		}
		return tenantid;
	}// ..end of the method

	/**
	 * 
	 * @param domain
	 * @return
	 * @throws DomainIdentificationException
	 * @throws AccountFetchException
	 */
	public Map<String, String> getTenantIdAndSiteIdByDomain(String domain) throws TenantIdentificationException {
		try {
			IAccountRegistryService registryService = new AccountRegistryServiceImpl();
			String siteIdByDomain = registryService.getSiteIdByDomain(domain);
			String tenantByDomain = registryService.getTenantByDomain(domain);
			Map<String, String> tenantAndSite = new HashMap<>();
			tenantAndSite.put(AuthNConstants.TENANT_ID, tenantByDomain);
			tenantAndSite.put(AuthNConstants.SITE_ID, siteIdByDomain);
			return tenantAndSite;
		} catch (AccountFetchException e) {
			throw new TenantIdentificationException("Unable to identify the domain from the given company name", e);
		}
	}// ..end of the method

	/**
	 * 
	 * @param tenantId
	 * @param siteId
	 * @return
	 * @throws DomainIdentificationException
	 */
	public List<String> getDomainByTenantAndSite(String tenantId, String siteId) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		List<String> domains = new ArrayList<>();
		try {
			preparedStatement = conn.prepareStatement(SELECT_DOMAIN_BY_TENANT_AND_SITE);
			preparedStatement.setString(1, tenantId);
			preparedStatement.setString(2, siteId);
			ResultSet resultSet = preparedStatement.executeQuery();
			String domain = "";
			while (resultSet.next()) {
				domain = resultSet.getString("domainname");
				domains.add(domain);
			}
			return domains;
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to get domain name", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}// ..end of the method

	/**
	 * 
	 * @param domainName
	 * @return
	 * @throws DomainIdentificationException
	 */
	public int getDomainIDByDomainName(String domainName) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		int domainNameId = 0;
		if (AuthNUtil.isEmpty(domainName)) {
			throw new DomainIdentificationException("Empty domainName requested to get the domainID lists..");
		}
		try {
			preparedStatement = conn.prepareStatement(SELECT_DOMAINID_DOMAINNAME);
			preparedStatement.setString(1, domainName.trim());
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				domainNameId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to identify the domainID from the given domainName name: ",
					e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return domainNameId;
	}// ..end of the method

	/**
	 * 
	 * @param companyName
	 * @throws CompanyRegistrationException
	 */
	public void registerNewCompany(String companyName) throws CompanyRegistrationException {
		if (AuthNUtil.isEmpty(companyName)) {
			throw new CompanyRegistrationException("Empty-companyname requested to register! ");
		}
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(INSERT_COMPANY);
			preparedStatement.setString(1, companyName.trim());
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new CompanyRegistrationException(e.getMessage(), e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}// ..end of the method

	public void registerNewDomainByAccount(String companyName, Domain domain, boolean deafultAppFlag,
			String defaultAppName) throws DomainRegistrationException {
		if (AuthNUtil.validateUsername(domain.getAdminUserName()))
			if (AuthNUtil.isEmpty(companyName) || AuthNUtil.isEmpty(domain.getDomainName())
					|| AuthNUtil.isEmpty(domain.getSiteId())) {
				throw new DomainRegistrationException(
						"Empty-values 'companyName' / 'domainName' / 'tenatnID' / 'siteID' requested to register! ");
			}
		AuthNUtil.disablesslTrustValidation();
		try {
			IAccountRegistryService registryService = new AccountRegistryServiceImpl();
			int accountId = registryService.getDomainIdByDomain(domain.getDomainName());
			registerNewDomainInDB(domain, accountId, false, null, null);
			registerNewDomainInIS(domain);
			if (deafultAppFlag) {
				String defaultPassword = RandomStringUtils.random(15, AuthNConstants.CHARACTERS_SET);
				registerNewDomainInDB(domain, accountId, deafultAppFlag, defaultAppName, defaultPassword);
				registerNewAppInIS(domain, defaultAppName, defaultPassword);

			}
		} catch (CompanyRegistrationException | AccountFetchException e) {
			throw new DomainRegistrationException(e.getMessage());
		}
	}// ..end of the method

	/**
	 * This method is used to create the Default App User in Wso2Is
	 * 
	 * @param domain          : String domainName
	 * @param defaultAppName
	 * @param defaultPassword
	 */
	private void registerNewAppInIS(Domain domain, String defaultAppName, String defaultPassword) {
		try {
			IAuthNUserMgmtService service = new AuthNUserMgmtServiceImpl();
			User user = buildDefaultUserObject(domain, defaultAppName, defaultPassword);
			service.selfRegisterUser(user, domain.getDomainName());
		} catch (PropertiesConfigException | UserRepoUpdateException | UserRegistrationException
				| SecretKeyGenException e) {
			e.printStackTrace();
		}

	}// ..end of method registerAppInIS

	/**
	 * This method is used to construct the Default App User Object
	 * 
	 * @param domain          : Domain Object
	 * @param defaultAppName  : String defaultAppName
	 * @param defaultPassword
	 * @return User Object
	 * @throws SecretKeyGenException
	 */
	private User buildDefaultUserObject(Domain domain, String defaultAppName, String defaultPassword)
			throws SecretKeyGenException {
		User user = new User();
		UserClaims claims = new UserClaims();
		user.setTenantId(domain.getTenantId());
		user.setSiteId(domain.getSiteId());
		user.setUserName("DEFAULT_APP_" + defaultAppName);
		user.setFirstName(AuthNConstants.DEFAULT_APP_USER);
		user.setLastName(AuthNConstants.DEFAULT_APP_USER);
		user.setTitle(AuthNConstants.DEFAULT_APP_USER);
		user.setPassword(defaultPassword);
		claims.setIsActive("true");
		claims.setIsLocked("false");
		claims.setEmailaddress(domain.getAdminUserName());
		claims.setOrganization(AuthNConstants.DEFAULT_APP_USER);
		claims.setMobile("0000");
		claims.setTelephone("0000");
		String address1 = AuthNConstants.DEFAULT_APP_USER;
		String loca = AuthNConstants.DEFAULT_APP_USER;
		claims.setStreetaddress(address1);
		claims.setLocality(loca);
		claims.setPostalCode(AuthNConstants.DEFAULT_APP_USER);
		claims.setRegion(AuthNConstants.DEFAULT_APP_USER);
		claims.setStateOrProvince(AuthNConstants.DEFAULT_APP_USER);
		claims.setCountry(AuthNConstants.DEFAULT_APP_USER);
		claims.setRegion(AuthNConstants.DEFAULT_APP_USER);
		user.setUserClaims(claims);
		return user;

	}// ..end of method buildDefaultUserObject

	private void registerNewDomainInDB(Domain domain, int domainId, boolean defaultAppFlag, String defaultAppName,
			String defaultPassword) throws CompanyRegistrationException {
		String adminPassword = domain.getAdminPassword();
		String adminUserName = domain.getAdminUserName();

		if (AuthNUtil.isEmpty(adminUserName) || AuthNUtil.isEmpty(adminPassword))
			throw new CompanyRegistrationException("'adminUserName' / 'adminPassword' is missing!");

		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(INSERT_DOMAIN_ADMIN);
			preparedStatement.setInt(1, domainId);
			preparedStatement.setString(2, adminUserName);
			preparedStatement.setString(3, adminPassword);
			if (defaultAppFlag && defaultAppName != null) {
				String usernamePassDomain = "DEFAULT_APP_" + defaultAppName.trim() + ":" + defaultPassword.trim();
				byte[] enodedBasicAuth = Base64.getEncoder().encode(usernamePassDomain.getBytes());
				String encodedBasicAuthUNamePwd = new String((enodedBasicAuth),
						Charset.forName(AuthNConstants.CHARSET_UTF_8));
				preparedStatement.setString(4, encodedBasicAuthUNamePwd);
			} else {
				preparedStatement.setString(4, null);
			}
			preparedStatement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new CompanyRegistrationException("Unable to register new admin to database ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}

	}

	/**
	 * Register new domain from below soap message in IS
	 * 
	 * @param domain
	 * @throws AttributesRegistrationException
	 */
	private void registerNewDomainInIS(Domain domain) throws CompanyRegistrationException {
		String adminEmail = domain.getAdminEmail();
		String adminFName = domain.getAdminFirstName();
		String adminLName = domain.getAdminLastName();
		String adminPass = domain.getAdminPassword();
		String adminUName = domain.getAdminUserName();
		String domainName = domain.getDomainName();
		String tenantId = domain.getTenantId();
		String siteId = domain.getSiteId();
		if (AuthNUtil.isEmpty(domainName) || AuthNUtil.isEmpty(adminUName) || AuthNUtil.isEmpty(adminPass)) {
			throw new CompanyRegistrationException(
					"Empty values for 'domain' / 'adminUserName' / 'adminPassword' is requested! ");
		}
		if (AuthNUtil.isEmpty(adminFName) || AuthNUtil.isEmpty(adminLName) || AuthNUtil.isEmpty(adminEmail)) {
			throw new CompanyRegistrationException(
					"Empty values for 'admin-firstName' / 'admin-lastName' / 'admin-emailAddress' is requested! ");
		}
		if (AuthNUtil.isEmpty(tenantId) || AuthNUtil.isEmpty(siteId)) {
			throw new CompanyRegistrationException("Empty values for 'tenantId' / 'siteId' is requested! ");
		}
		if (AuthNUtil.validateUsername(adminUName)) {
			throw new CompanyRegistrationException("Invalide usernme format");
		}
		SOAPConnection soapConnection = null;
		SOAPConnectionFactory soapConnectionFactory;
		SOAPMessage soapResponse = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			String url = AuthNUtil.getBasewso2Properties().getProperty(BASE_URL) + AuthNConstants.PART_REMOTE_TENANTREPO;
			soapResponse = soapConnection.call(helper.getNewTenanatAddSoapMessage(adminUName.trim(), adminPass.trim(),
					adminEmail.trim(), adminFName.trim(), adminLName.trim(), domainName.trim(),
					AuthNUtil.getldapProperties().getProperty("identityServerUsername"),
					AuthNUtil.getldapProperties().getProperty("identityServerPassword")), url);
			if (soapResponse.getSOAPBody().hasFault()) {
				System.out.println(
						soapResponse.getSOAPBody().getFirstChild().getFirstChild().getNextSibling().getTextContent());
				throw new CompanyRegistrationException("Unable to register tenant ");
			}
			soapConnection.close();
			// eg:
			// mail=admin@email.com,ou=users,ou=mycompanydomain.com,dc=WSO2,dc=ORG
			LdapContext ldapCtx = AuthNUtil.getLdapContext();
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("description", tenantId.trim() + "/" + siteId.trim()));
			ldapCtx.modifyAttributes(
					"uid=" + adminUName.trim() + ",ou=users,ou=" + domainName.trim() + ",dc=WSO2,dc=ORG", mods);
		} catch (SOAPException | NamingException | PropertiesConfigException e1) {
			throw new CompanyRegistrationException("Unable to register tenant...", e1);
		}
	}// ..end of the method

	/**
	 * 
	 * @param domain
	 * @param preparedStatement
	 * @return
	 * @throws DomainIdentificationException
	 */
	private String getTenantIdByDomainName(String domain, PreparedStatement preparedStatement)
			throws DomainIdentificationException {
		String res = null;
		try {
			preparedStatement.setString(1, domain);
			ResultSet resltStmt = preparedStatement.executeQuery();
			while (resltStmt.next()) {
				res = resltStmt.getString(1);
			}
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to get the tenantId by domain requested ", e);
		}
		return res;
	}// ..end of the method

	/**
	 * 
	 * @param companyName
	 * @return
	 * @throws CompanyIdentificationException
	 */
	public int getCompanyIdByName(String companyName) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		int res = 0;
		try {
			preparedStatement = conn.prepareStatement(GET_COMPANY_ID);
			preparedStatement.setString(1, companyName);
			ResultSet resltStmt = preparedStatement.executeQuery();
			while (resltStmt.next()) {
				res = resltStmt.getInt(1);
			}
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to get the Domain ids from the mapped tables: ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return res;
	}// ..end of the method

	public Domain getDomainAttributes(String domainName) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		logger.debug("conn object in getDomainAttributes ::: " + conn);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet=null;
		Domain domain = new Domain();
		try {
			IAccountRegistryService registryService = new AccountRegistryServiceImpl();
			int domainId = registryService.getDomainIdByDomain(domainName);
			logger.debug("domainId  in getDomainAttributes ::: " + domainId);
			if (domainId == 0) {
				throw new DomainIdentificationException("Unable to identify the domain from the given name");
			}
			preparedStatement = conn.prepareStatement(SELECT_ALL_DOMAIN_ADMIN);
			logger.debug("preparedStatement  in getDomainAttributes ::: " + preparedStatement);
			preparedStatement.setInt(1, domainId);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				domain.setDomainName(domainName);
				domain.setAdminUserName(resultSet.getString("adminusername"));
				domain.setAdminPassword(resultSet.getString("adminpassword"));
			}
			logger.debug("domain  in getDomainAttributes ::: " + domain);
			return domain;
		} catch (SQLException | AccountFetchException e) {
			throw new DomainIdentificationException(
					"Unable to identify the domain from the given name: " + e.getMessage() + " " + e.getCause(), e);
		}

		finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement,resultSet);
		}
	}

	public void removeDomainFromDB(String domain, String company) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			IAccountRegistryService registryService = new AccountRegistryServiceImpl();
			int domainIDByDomainName = registryService.getAccountIdByDomain(domain);
			removeDomainAdminByDomainId(domainIDByDomainName);
			preparedStatement = conn.prepareStatement(DELETE_DOMAIN_COMPANY);
			preparedStatement.setInt(1, domainIDByDomainName);
			preparedStatement.execute();
		} catch (SQLException | AccountFetchException e) {
			e.printStackTrace();
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}

	public void removeDomainAdminByDomainId(int domainId) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(DELETE_DOMAIN_ADMIN);
			preparedStatement.setInt(1, domainId);
			preparedStatement.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}

	public void removeCompany(String company) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(DELETE_COMPANY_NAME);
			preparedStatement.setString(1, company.trim());
			preparedStatement.execute();
		} catch (SQLException e) {
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}

	public String getCompanyNameByCompanyId(int companyId) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		String companyName = "";
		try {
			preparedStatement = conn.prepareStatement(SELECT_ALL_COMPANY);
			preparedStatement.setInt(1, companyId);
			ResultSet resSet = preparedStatement.executeQuery();
			if (resSet.next()) {
				companyName = resSet.getString("companyname");
			}

		} catch (SQLException e) {
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return companyName;
	}

	public int getCompanyIdByDomainId(int domainId) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		int companyid = 0;
		try {
			preparedStatement = conn.prepareStatement(SELECT_COMPANY_ID);
			preparedStatement.setInt(1, domainId);
			ResultSet resSet = preparedStatement.executeQuery();
			if (resSet.next()) {
				companyid = resSet.getInt("companyid");
			}

		} catch (SQLException e) {
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return companyid;
	}

}
