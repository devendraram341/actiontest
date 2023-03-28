package com.leap.authentication.service;

import java.util.List;
import java.util.Map;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.exception.AccountRegistrationException;
import com.leap.authentication.bean.AccountDetail;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.AttributesRemovalException;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.DomainRegistrationException;
import com.leap.authentication.exception.InvalidAccountRegistrationReqException;
import com.leap.authentication.exception.InvalidDomainReqException;
import com.leap.authentication.exception.InvalidSiteRegisterException;
import com.leap.authentication.exception.Key2ActSOAPFaultException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.TenantIdentificationException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;

public interface IAuthNAdminService {

	/**
	 * API, to create new account.
	 * 
	 * @param AccountDetail
	 * @return
	 * @throws InvalidAccountRegistrationReqException
	 */
	public void registerNewAccount(AccountDetail accountDetails) throws AccountRegistrationException;

	/**
	 * API, to create new company to existing account.
	 * 
	 * @param accountName
	 * @param deafultAppFlag 
	 * @param defaultAppName 
	 * @param SiteDetatils
	 * @return
	 * @throws InvalidSiteRegisterException
	 */
	public void registerNewDomainByAccount(String accountName, Domain domin, boolean deafultAppFlag,
			String defaultAppName) throws DomainRegistrationException;

	/**
	 * Admin service, where userName which belongs to a particular domain will be
	 * updated with the given new password
	 * 
	 * @param userName
	 * @param newPassword
	 * @param domainName
	 * @throws CredentialUpdateException
	 * @throws DomainIdentificationException
	 * @throws Key2ActSOAPFaultException
	 */
	public void resetUserPassword(String userName, String newPassword, String domainName)
			throws CredentialUpdateException;

	/**
	 * API, to create new users along with tenant/site as attribute in the repo
	 * (internally handled), which is an admin service, hence we have to passin the
	 * tenant-admin credentials
	 * 
	 * @param users
	 * @param tenantAdminUname
	 * @param tenantAdminPwd
	 * @throws UserRegistrationException
	 * @throws Key2ActSOAPFaultException
	 */
	public void createUsers(List<User> users, String domin, String tenantAdminUname, String tenantAdminPwd)
			throws UserRepoUpdateException, UserRegistrationException;

	/**
	 * API, to get the domain name by the company name.
	 * 
	 * @param accountname
	 * @return list of account names.
	 * @throws AccountFetchException 
	 */
	public List<String> getDomainNameByAccountName(String accountname) throws  AccountFetchException;

	/**
	 * API, to get the domain name by the tenantId.
	 * 
	 * @param tenantId
	 * @return domain name.
	 * @throws AccountFetchException 
	 */
	public String getDomainNameByTenantId(String tenantId) throws AccountFetchException;

	/**
	 * API, to get the tenant from the given domain.
	 * 
	 * @param domain
	 * @return
	 * @throws TenantIdentificationException
	 */
	public String getTenantByDomain(String domain) throws DomainIdentificationException;

	/**
	 * API, to get the tenant and site for the given domain.
	 * 
	 * @param domain
	 * @return
	 * @throws TenantIdentificationException 
	 */
	public Map<String, String> getTenantIdAndSiteIdByDomain(String domain) throws TenantIdentificationException;

	/**
	 * API, to get the domain for the given tenant and site.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @return
	 * @throws UserRepoUpdateException
	 */
	public List<String> getDomainByTenantAndSite(String tenantId, String siteId) throws DomainIdentificationException;

	/**
	 * API, to get the domain Id for the given domain name.
	 * 
	 * @param domainName
	 * @return
	 * @throws DomainIdentificationException
	 */
	public int getDomainIDByDomainName(String domainName) throws DomainIdentificationException;

	/**
	 * API, to check whether the given user is existing in the domain or not.
	 * 
	 * @param userName
	 * @param domainName
	 * @return
	 */
	public boolean isUserExist(String userName, String domainName);

	/**
	 * API, to delete the existing user from the particular domain.
	 * 
	 * @param userName
	 * @param domain
	 * @throws DomainIdentificationException
	 * @throws UserRegistrationException
	 * @throws UserRepoUpdateException
	 * @throws UserProfileObtainException
	 */
	public void deleteUser(String userName, String domain)
			throws UserProfileFetchException, DomainIdentificationException, UserRepoUpdateException;

	/**
	 * API, to get the list of all the domains .
	 * 
	 * @return
	 * @throws DomainIdentificationException
	 * @throws AccountFetchException 
	 */
	public List<Domain> listAllDomains() throws DomainIdentificationException, AccountFetchException;

	/**
	 * API, to get the list of all the users for the given domains.
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param domains
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	@SuppressWarnings("rawtypes")
	public List getAllUsers(int pageIndex, int pageSize, String[] domains)
			throws UserProfileFetchException;

	/**
	 * API, to get the list of user based on the given email.
	 * 
	 * @param emailSearchable
	 * @param domain
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	@SuppressWarnings("rawtypes")
	public List findUserByEmailPaginated(String emailSearchable, String domain, int pageIndex, int pageSize)
			throws UserProfileFetchException;

	/**
	 * API, to get the list of user based on the given username.
	 * 
	 * @param nameSearchable
	 * @param domain
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws PropertiesConfigException
	 * @throws UserProfileFetchException
	 */
	@SuppressWarnings("rawtypes")
	public List findUserByNamePaginated(String nameSearchable, String domain, int pageIndex, int pageSize)
			throws UserProfileFetchException;

	/**
	 * API, to update user status by using domain name, user name, email and account
	 * disable.
	 * 
	 * @param domain
	 * @param userName
	 * @param emailAddress
	 * @param accountDisable
	 * @return
	 * @throws AttributesRegistrationException
	 */
	public boolean updateUserStatus(String domain, String userName, String emailAddress, boolean accountDisable)
			throws AttributesRegistrationException;

	/**
	 * API, to update user status by using user object and domain name.
	 * 
	 * @param user
	 * @param domain
	 * @throws AttributesRegistrationException
	 * @throws UserRepoUpdateException
	 */
	public void updateUserStatus(User user, String domain)
			throws UserRepoUpdateException;

	/**
	 * API, to update user status by using domain name, user name and account
	 * disable.
	 * 
	 * @param domain
	 * @param userName
	 * @param accountDisable
	 * @return
	 * @throws AttributesRegistrationException
	 */
	public boolean updateUserLock(String domain, String userName, boolean accountDisable)
			throws AttributesRegistrationException;

	/**
	 * API, to delete the tenant available for the company provided.
	 * 
	 * @param domain
	 * @param company
	 * @return
	 * @throws UserRepoUpdateException
	 * @throws DomainIdentificationException
	 * @throws AttributesRemovalException
	 * @throws InvalidDomainReqException
	 */
	public boolean deleteDomain(String domain, String company) throws UserRepoUpdateException;
}
