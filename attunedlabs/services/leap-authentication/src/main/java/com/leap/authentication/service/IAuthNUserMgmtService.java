package com.leap.authentication.service;

import java.util.List;
import java.util.Map;

import com.attunedlabs.security.exception.AccountFetchException;
import com.leap.authentication.bean.User;
import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.InvalidAttributesFetchException;
import com.leap.authentication.exception.InvalidAuthenticationException;
import com.leap.authentication.exception.Key2ActSOAPFaultException;
import com.leap.authentication.exception.TokenRenewalException;
import com.leap.authentication.exception.TokenRevokeException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.exception.UserValidationRequestException;

public interface IAuthNUserMgmtService {

	/**
	 * API, to be used to request for token and related attributes for a given
	 * encodedBasicAuthUNamePwd, its upto developers to passin the btoa() or
	 * Basic userpassword to evaluate
	 * 
	 * @param userName
	 * @param password
	 * @param domain
	 * @return
	 * @throws InvalidAuthenticationException
	 * @throws InvalidAttributesFetchException
	 */
	public Map<String, String> authenticateUser(String userName, String password, String domain)
			throws InvalidAuthenticationException, InvalidAttributesFetchException;

	/**
	 * Used in the logout mechanism which will be used for revoking the given
	 * token, which will then automates the logout processing
	 * 
	 * @param accessToken
	 * @return
	 * @throws TokenRevokeException
	 */
	public boolean revokeUserOAuthToken(String accessToken) throws TokenRevokeException;

	/**
	 * Used to get a new access_token from a refresh token
	 * 
	 * @param refreshToken
	 * @return
	 * @throws RefreshTokenException
	 */
	public Map<String, Object> renewUserRefreshToken(String refreshToken) throws TokenRenewalException;

	/**
	 * API, UserRepositoryService.updatePassword() will update the user password
	 * with the earlier credentials.
	 * 
	 * @param userName
	 * @param domain
	 * @param oldPassword
	 * @param newPassword
	 * @throws CredentialUpdateException
	 * @throws Key2ActSOAPFaultException
	 */
	public void changeUserPassword(String userName, String domain, String oldPassword, String newPassword)
			throws CredentialUpdateException;

	/**
	 * API, to get the user & its details as object wrapped up.
	 * 
	 * @param bearerTokenString
	 * @return
	 * @throws UserProfileFetchException
	 */
	public User getUserProfile(String bearerTokenString) throws UserProfileFetchException;

	/**
	 * API, to validate the accessToken.
	 * 
	 * @param accessToken
	 * @return
	 * @throws InvalidAuthenticationException
	 */
	public boolean validateAccessToken(String accessToken) throws InvalidAuthenticationException;

	/**
	 * API, to self register a user based on the user details and the domain.
	 * 
	 * @param user
	 * @param domain
	 * @throws UserRegistrationException
	 */
	public void selfRegisterUser(User user, String domain) throws UserRepoUpdateException, UserRegistrationException;

	/**
	 * API, to get all the domains for the particular user.
	 * 
	 * @param userName
	 * @return
	 * @throws DomainIdentificationException
	 */
	public List<String> getAllDomainsByUser(String userName) throws DomainIdentificationException;

	/**
	 * API, to get the forget password code by using the userName and domain.
	 * 
	 * @param userName
	 * @param domain
	 * @return
	 * @throws UserValidationRequestException
	 */
	public String getForgotPasswordConfmCode(String userName, String domain) throws UserValidationRequestException;

	/**
	 * API, to validate the code and update the new password by using userName,
	 * domain and new password.
	 * 
	 * @param code
	 * @param userName
	 * @param domain
	 * @param newPassword
	 * @throws CredentialUpdateException
	 */
	public void validateAndUpdatePassword(String code, String userName, String domain, String newPassword)
			throws CredentialUpdateException;

	/**
	 * API, to get the user & its details.
	 * 
	 * @param userName
	 * @param domain
	 * @return
	 * @throws UserProfileObtainException
	 */
	public User getUserProfile(String userName, String domain) throws UserProfileFetchException;

	/**
	 * API is used to get the App Details
	 * 
	 * @param string
	 * @param siteId
	 * @param domainValue
	 * @return
	 * @throws AccountFetchException
	 */
	public Map<String, String> appDeatils(String tenantId, String siteId, String domainValue)
			throws AccountFetchException;

}
