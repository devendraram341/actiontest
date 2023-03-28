/**
 * 
 */
package com.leap.authorization.service;

import java.util.List;

import com.leap.authorization.exception.PermissionGrantException;
import com.leap.authorization.exception.ResourceAccessException;
import com.leap.authorization.exception.ResourceAuthorizeException;
import com.leap.authorization.exception.ResourceRegistryException;
import com.leap.authorization.exception.ResourceRemovalException;

/**
 * @author GetUsRoi
 *
 */
public interface IAuthorizationGenericService {

	/**
	 * Service to register List<User> of users under same tenant.
	 * 
	 * @param users
	 * @param tenant
	 * @param isPasswordEnabled
	 * @throws ResourceRegistryException
	 */
	public void registerUsers(List<User> users, String tenant, boolean isPasswordEnabled)
			throws ResourceRegistryException;

	/**
	 * Service to register a user under a tenant.
	 * 
	 * @param user
	 * @param tenant
	 * @param isPasswordEnabled
	 * @throws ResourceRegistryException
	 */
	public void registerUser(User user, String tenant, boolean isPasswordEnabled) throws ResourceRegistryException;

	/**
	 * Service to remove a user under a specific tenant.
	 * 
	 * @param user
	 * @param tenant
	 * @return isRemoved
	 * @throws ResourceRemovalException
	 */
	public boolean removeUser(User user, String tenant) throws ResourceRemovalException;

	/**
	 * Service to associate user to a role, associates a user under a role by
	 * inheriting the permissions from the role specified
	 * 
	 * @param user
	 * @param roleToAssociate
	 * @param tenant
	 * @throws PermissionGrantException
	 */
	public void associateUserwithRole(User user, String roleToAssociate, String tenant) throws PermissionGrantException;

	/**
	 * Service to get roles assigned for the particular user with has certain
	 * permission(s), from specified domain.
	 * 
	 * @param user
	 * @param domain
	 * @throws ResourceAuthorizeException
	 */
	public List<String> getRolesAssignedForUser(User user, String domain) throws ResourceAccessException;

	/**
	 * Service to grant permission for user on resource, this is to grant permission
	 * such as read, write, etc
	 * 
	 * @param user
	 * @param permissions
	 * @param resourcetoGrantOn
	 * @param tenant
	 * @throws PermissionGrantException
	 */
	public void grantResourcePermissionsForUser(User user, List<String> permissions, String resourcetoGrantOn,
			String tenant) throws PermissionGrantException;

	/**
	 * Service to get all resources accessible for a user from a resourceType,
	 * generic purpose, can be used to check in eg: Location, Menu etc..
	 * 
	 * NB: api-deoesn't exist for getting all the authorized resources for user in
	 * SHIRO
	 * 
	 * @param resourceType
	 * @param user
	 * @param tenant
	 * @param permissions
	 * @return
	 * @throws ResourceAccessException
	 */
	public List<String> getAllUserPermittedResource(String resourceType, User user, String tenant,
			List<String> permissions) throws ResourceAccessException;

	/**
	 * Service to check weather specified User belong or exists to the given tenant
	 * or not.
	 * 
	 * @param user
	 * @param tenant
	 * @return userExists
	 * @throws ResourceAuthorizeException
	 */
	public boolean userExists(User user, String tenant) throws ResourceAuthorizeException;

	/**
	 * Service to check weather tenant exists or not.
	 * 
	 * @param user
	 * @param tenant
	 * @return tenantExists
	 * @throws ResourceAuthorizeException
	 */
	public boolean tenantExists(String tenant) throws ResourceAuthorizeException;

	/**
	 * Does the assertion for user on a resource Service to check whether user has
	 * permission on an item, to check whether the requested permission is available
	 * for a user on the requested resource.
	 * 
	 * @param user
	 * @param resourceName
	 * @param permissionType
	 * @param tenant
	 * @return
	 * @throws ResourceAuthorizeException
	 */
	public boolean hasPermissionForUser(User user, String resourceName, String permissionType, String tenant)
			throws ResourceAuthorizeException;

	/** role-related apis */

	/**
	 * Service to get all resources accessible for a role, to retrieve all the items
	 * under a resourceType
	 * 
	 * NB: api-deoesn't exist for getting all the authorized resources for roles in
	 * SHIRO
	 * 
	 * @param resourceType
	 * @param role
	 * @return
	 * @throws ResourceAccessException
	 */
	public List<String> getAllRolePermittedResource(String resourceType, String role, String tenant,
			List<String> permissions) throws ResourceAccessException;

	/**
	 * Does the assertion for user on a resource Service to check whether user has
	 * permission on an item, to check whether the requested permission is available
	 * for a role on the requested resource.
	 * 
	 * Usecase: with Shiro, 2 step verification to associate the User with role, is
	 * by checking subject.hasRole() & subject.hasPermission()
	 * 
	 * @param role
	 * @param resourceName
	 * @param permission
	 * @param tenant
	 * @return
	 * @throws ResourceAuthorizeException
	 */
	public boolean hasPermissionForRole(String tenant, String role, String resourceName, String permission)
			throws ResourceAuthorizeException;

	/**
	 * Service to grant permission for role on resource, if not available and if has
	 * to be granted
	 * 
	 * UseCase: Oacc keeps the roles as resource instances, whereas SHIRO - Realms
	 * are dependent on the role & permission.
	 * 
	 * @param roleId
	 * @param permission
	 * @param resourceId
	 * @throws PermissionGrantException
	 */
	public void grantPermissionOnRole(String tenant, String roleId, String permission, String resourceId)
			throws PermissionGrantException;

	public List<String> getResouceClassIdByTenanatAndResourceClass(String tenant)
			throws ResourceAuthorizeException, PermissionGrantException;

	/**
	 * Service to update user to a role, associates a user under a role by
	 * inheriting the permissions from the role specified
	 * 
	 * @param user
	 * @param roleToAssociate
	 * @param tenant
	 * @throws PermissionGrantException
	 */
	public void updateRoleForUser(User user, String roleAlreadyAssociated, String roleToAssociate, String tenant)
			throws PermissionGrantException;

	/**
	 * Service to delete user to a role, associates a user under a role by
	 * inheriting the permissions from the role specified
	 * 
	 * @param user
	 * @param roleToAssociate
	 * @param tenant
	 * @throws PermissionGrantException
	 */
	public void deleteRoleForUser(User user, String roleAlreadyAssociated, String roleToAssociate, String tenant)
			throws PermissionGrantException;

	public void revokeResourcesForUser(String user, List<String> resource, String tenant, String resourceType)
			throws PermissionGrantException;
	
	public void revokeResourceForUser(String user, String resource, String tenant, String resourceType)
			throws PermissionGrantException;
	
	public void revokeResourcePermissionsForUser(String user, String resource, String tenant, List<String> permissions);
	
	public void deleteUser(String userName, String tenant) throws PermissionGrantException;

}
