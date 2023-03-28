package com.leap.authorization.service;

import java.util.List;

import com.leap.authorization.exception.AuthorizationSetupException;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.PermissionAccessException;
import com.leap.authorization.exception.ResourceRegistryException;

public interface IAuthorizationSetup {

	/**
	 * Service to add new resourceTypes with associating permission
	 * 
	 * @param resourceType
	 * @param typeofPermissions
	 * @throws AuthorizationSetupException
	 */
	public void addNewResourceTypeToAuthorize(String resourceType, List<String> typeofPermissions)
			throws AuthorizationSetupException;

	/**
	 * Service to get all allocated permissions on a resourceType, includes the
	 * defaults as well
	 * 
	 * @param resourceType
	 * @return
	 * @throws PermissionAccessException
	 */
	public List<String> getAllPermissions(String resourceType) throws PermissionAccessException;

	/**
	 * Service to get all permissions on resource allocated for a user
	 * 
	 * NB: api-deoesn't exist for getting all the permission on resources for
	 * user in SHIRO
	 * 
	 * @param user
	 * @param resource
	 * @return
	 * @throws PermissionAccessException
	 */
	public List<String> getAllUserPermissionsOnResource(String user, String resource, String tenant)
			throws PermissionAccessException;

	/**
	 * Service to add new tenant to the repository
	 * 
	 * @param tenantName
	 * @throws AuthorizationSetupException
	 */
	public void addNewTenant(String tenantName) throws AuthorizationSetupException;

	/**
	 * Service to add new child tenant under given parent tenant to the
	 * repository
	 * 
	 * @param tenantName
	 * @throws AuthorizationSetupException
	 */
	public void addNewTenant(String tenantName, String parentTenant) throws AuthorizationSetupException;

	/**
	 * Service to create roles
	 * 
	 * @param roles
	 * @param tenant
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 */
	public void registerRoles(List<String> roles, String tenant) throws ResourceRegistryException;

	/**
	 * Service to create role
	 * 
	 * @param role
	 * @param tenant
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration
	 */
	public void registerRole(String role, String tenant) throws ResourceRegistryException;

	/**
	 * Service to add new roles for existing resourceTypes
	 * 
	 * @param typesOfPermissions
	 * @param resourceType
	 * @throws AuthorizationSetupException
	 */
	public void addNewPermissionsOnResourceType(List<String> typesOfPermissions, String resourceType)
			throws AuthorizationSetupException;

	/**
	 * Service to add new resources under a resourceType
	 * 
	 * @param resource
	 * @param resourceType
	 * @throws ResourceRegistryException
	 */
	public void addNewResources(List<String> resources, String resourceType, String tenant)
			throws ResourceRegistryException;

	/**
	 * Service to add a new resource under a resourceType
	 * 
	 * @param resource
	 * @param resourceType
	 * @throws ResourceRegistryException
	 */
	public void addNewResource(String resource, String resourceType, String tenant) throws ResourceRegistryException;

}