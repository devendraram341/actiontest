package com.leap.authorization.service.oacc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acciente.oacc.AccessControlContext;
import com.acciente.oacc.PasswordCredentials;
import com.acciente.oacc.Resource;
import com.acciente.oacc.ResourceClassInfo;
import com.acciente.oacc.ResourcePermission;
import com.acciente.oacc.ResourcePermissions;
import com.acciente.oacc.Resources;
import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.PermissionGrantException;
import com.leap.authorization.exception.ResourceAccessException;
import com.leap.authorization.exception.ResourceAuthorizeException;
import com.leap.authorization.exception.ResourceRegistryException;
import com.leap.authorization.exception.ResourceRemovalException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.User;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.OaccAuthUtil;
import com.leap.authorization.util.QueryConstant;
import com.leap.authorization.util.ResourceConstant;

public class AuthorizationGenericServiceImpl implements IAuthorizationGenericService {

	private static Logger logger = LoggerFactory.getLogger(AuthorizationGenericServiceImpl.class);
	private AccessControlContext accessControlContext;
	private List<String> items = new ArrayList<>();
	private List<String> resources = new ArrayList<>();
	private Connection connection;

	private HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();

	public AuthorizationGenericServiceImpl() throws InvalidConfiguration {
		connection = OaccAuthUtil.getOaccConnection();
		this.setAccessControlContext(OaccAuthUtil.getAuthenticatedAccessControlContext(connection));
		logger.info("Authorization Registry initiated successfully..!");
	}

	/**
	 * Assignment of authorized accessControlContext
	 * 
	 * @param accessControlContext
	 */
	private void setAccessControlContext(AccessControlContext accessControlContext) {
		this.accessControlContext = accessControlContext;
	}// ..setter ended

	@Override
	public void registerUsers(List<User> users, String tenant, boolean isPasswordEnabled)
			throws ResourceRegistryException {
		if (OaccAuthUtil.isEmptyList(users)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException("Invalid userList requested to register..");
		}
		boolean isEnabled = isPasswordEnabled;

		try {
			for (User user : users) {
				if (!(OaccAuthUtil.isValidUser(user, isPasswordEnabled)) || (OaccAuthUtil.isEmpty(tenant))) {
					OaccAuthUtil.dbCleanUp(connection);
					throw new ResourceRegistryException(
							"Invalid user requested to register, with invalid User:attributes ..");
				}
				String compactTenant = tenant.trim();
				if (!isEnabled) {
					accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_USERS, compactTenant,
							user.getUserName() + "-" + compactTenant);
				} else {
					accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_USERS, compactTenant,
							user.getUserName() + "-" + compactTenant,
							PasswordCredentials.newInstance(user.getPassword()));
				}
			}
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void registerUser(User user, String tenant, boolean isPasswordEnabled) throws ResourceRegistryException {
		boolean isEnabled = isPasswordEnabled;
		try {
			if ((!OaccAuthUtil.isValidUser(user, isPasswordEnabled)) || (OaccAuthUtil.isEmpty(tenant))) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new ResourceRegistryException(
						"Invalid user requested to register, with invalid User:attributes ..");
			}

			String tenantCompact = tenant.trim();
			if (!isEnabled) {
				accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_USERS, tenantCompact,
						user.getUserName().trim() + "-" + tenantCompact);
			} else {
				accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_USERS, tenantCompact,
						user.getUserName().trim() + "-" + tenantCompact,
						PasswordCredentials.newInstance(user.getPassword()));
			}
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public boolean removeUser(User user, String tenant) throws ResourceRemovalException {
		try {
			if ((!OaccAuthUtil.isValidUser(user, false)) || (OaccAuthUtil.isEmpty(tenant))) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new ResourceRemovalException(
						"Invalid user requested to unregister, with invalid User:attributes ..");
			}

			Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
			ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
			String resourceClass = resourceClassInfo.getResourceClassName();
			logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
			if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
				throw new ResourceRemovalException(
						"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

			return accessControlContext.deleteResource(userAsResource);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRemovalException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void associateUserwithRole(User user, String roleToAssociate, String tenant)
			throws PermissionGrantException {
		boolean isPasswordEnabled = false;
		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled) || OaccAuthUtil.isEmpty(roleToAssociate)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the role association");
		}
		Set<ResourcePermission> resperSet1 = new HashSet<>();
		resperSet1.add(ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		Resource roleResource = Resources.getInstance(roleToAssociate + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new PermissionGrantException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		resourceClassInfo = accessControlContext.getResourceClassInfoByResource(roleResource);
		resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + roleToAssociate + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new PermissionGrantException(
					"resource " + roleToAssociate + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		try {
			accessControlContext.grantResourcePermissions(userAsResource, roleResource, resperSet1);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public List<String> getRolesAssignedForUser(User user, String tenant) throws ResourceAccessException {
		logger.debug("inside .getRolesAssignedForUser..");
		if (!OaccAuthUtil.isValidUser(user, false) || OaccAuthUtil.isEmpty(tenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAccessException("Empty values are not allowed to get detailed");
		}

		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new ResourceAccessException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new ResourceAccessException("tenant mismatched on validating ..!" + tenant + " -vs- "
					+ accessControlContext.getDomainNameByResource(userAsResource));
		}

		Set<Resource> roles = null;
		List<String> rolesAccessible = new ArrayList<>();
		try {
			roles = accessControlContext.getResourcesByResourcePermissions(userAsResource,
					AuthConstants.RESOURCE_TYPE_ROLE, ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
		} catch (IllegalArgumentException e) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAccessException("user does not exist", e);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}

		if (roles != null)
			for (Resource roleId : roles)
				rolesAccessible.add(OaccAuthUtil.getResource(roleId.getExternalId(), tenant));

		return rolesAccessible;
	}

	@Override
	public void grantResourcePermissionsForUser(User user, List<String> permissions, String resourcetoGrantOn,
			String tenant) throws PermissionGrantException {
		logger.debug("creating permissions between user: " + user + " and resource: " + resourcetoGrantOn);
		if (!OaccAuthUtil.isValidUser(user, false) || OaccAuthUtil.isEmpty(resourcetoGrantOn)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Empty values are not allowed to register");
		}
		Set<ResourcePermission> permissionsSet = new HashSet<>();
		for (int permIterate = 0; permIterate < permissions.size(); permIterate++) {
			permissionsSet.add(ResourcePermissions.getInstance(permissions.get(permIterate)));
		}
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}

		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new PermissionGrantException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		try {
			accessControlContext.setResourcePermissions(userAsResource,
					Resources.getInstance(resourcetoGrantOn + "-" + tenant), permissionsSet);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
		logger.debug("permission created between user: " + user + " and resource: " + resourcetoGrantOn);
	}

	public void grantResourcePermissionsForUserForMultipleResources(User user, List<String> permissions,
			List<String> resourcetoGrantOn, String tenant) throws PermissionGrantException {
		logger.debug("creating permissions between user: " + user + " and resource: " + resourcetoGrantOn);
		if (!OaccAuthUtil.isValidUser(user, false)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Empty values are not allowed to register");
		}
		Set<ResourcePermission> permissionsSet = new HashSet<>();
		for (int permIterate = 0; permIterate < permissions.size(); permIterate++) {
			permissionsSet.add(ResourcePermissions.getInstance(permissions.get(permIterate)));
		}
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}

		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new PermissionGrantException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		try {
			for (int i = 0; i < resourcetoGrantOn.size(); i++) {
				accessControlContext.setResourcePermissions(userAsResource,
						Resources.getInstance(resourcetoGrantOn.get(i) + "-" + tenant), permissionsSet);
			}
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
		logger.debug("permission created between user: " + user + " and resource: " + resourcetoGrantOn);
	}

	public static String getResourceClassName(String resource, String domain) throws InvalidConfiguration {
		long start = System.currentTimeMillis();
		Resource resourceInstance = Resources.getInstance(resource + "-" + domain);
		ResourceClassInfo resourceClassInfo = new AuthorizationGenericServiceImpl().accessControlContext
				.getResourceClassInfoByResource(resourceInstance);
		long end = System.currentTimeMillis();
		logger.debug("getResourceClassName Took : " + (end - start));
		return resourceClassInfo.getResourceClassName();
	}

	@Override
	public List<String> getAllUserPermittedResource(String resourceType, User user, String tenant,
			List<String> permissions) throws ResourceAccessException {
		logger.debug(".getAllUserPermittedResource..");
		if (!OaccAuthUtil.isValidUser(user, false) || OaccAuthUtil.isEmpty(resourceType) || OaccAuthUtil.isEmpty(tenant)
				|| OaccAuthUtil.isEmptyList(permissions)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAccessException("Empty values are not allowed to get detailed");
		}
		logger.debug(".all are valid..");
		String compactTenant = tenant.trim();
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + compactTenant);
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new ResourceAccessException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		Set<ResourcePermission> resperSet = new HashSet<>();
		for (String permission : permissions) {
			resperSet.add(ResourcePermissions.getInstance(AuthUtil.decodeParameter(permission.toLowerCase())));
		}
		logger.debug("resperSet " + resperSet);
		Set<Resource> resourceSet = null;
		try {
			resourceSet = accessControlContext.getResourcesByResourcePermissionsAndDomain(userAsResource, resourceType,
					tenant, resperSet);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ResourceAccessException("user, resourceType, tenant or permission does not exist", e);
		}
		List<String> allResourceClass = accessControlContext.getResourceClassNames();
		for (Resource retResource : resourceSet) {
			items.add(OaccAuthUtil.getResource(retResource.getExternalId(), tenant));
			for (int allResIte = 0; allResIte < allResourceClass.size(); allResIte++) {
				String childResource = retResource.getExternalId() + AuthConstants.RESOURCE_APPENDER;
				if ((childResource).equals(allResourceClass.get(allResIte))) {
					try {
						getAllUserPermittedResource(childResource, user, tenant, permissions);
					} catch (IllegalArgumentException exp) {
						OaccAuthUtil.dbCleanUp(connection);
						throw new ResourceAccessException("user or domain does not exist", exp);
					} finally {
						OaccAuthUtil.dbCleanUp(connection);
					}
				}
			}
		}
		return items;
	}

	@Override
	public boolean userExists(User user, String tenant) throws ResourceAuthorizeException {
		logger.debug("checking user availabilty...");
		if (!OaccAuthUtil.isValidUser(user, false) || OaccAuthUtil.isEmpty(tenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException("Empty values are not allowed to validate");
		}

		Set<String> domainDescendants = accessControlContext.getDomainDescendants(tenant);
		if (domainDescendants == null || !domainDescendants.contains(tenant))
			throw new ResourceAuthorizeException("tenant " + tenant + " doesn't exists!");

		try {
			Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
			if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
				throw new ResourceAuthorizeException("tenant mismatched on validating ..!" + tenant + " -vs- "
						+ accessControlContext.getDomainNameByResource(userAsResource));
			}
			ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
			String resourceClass = resourceClassInfo.getResourceClassName();
			logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
			return resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			return false;
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public boolean tenantExists(String tenant) throws ResourceAuthorizeException {
		logger.debug("checking tenant availabilty...");
		if (OaccAuthUtil.isEmpty(tenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException("Empty values are not allowed to validate");
		}

		try {
			Set<String> domainDescendants = accessControlContext.getDomainDescendants(tenant);
			if (domainDescendants != null)
				return domainDescendants.contains(tenant);
			else
				return false;
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}

	}

	@Override
	public boolean hasPermissionForUser(User user, String resourceName, String permissionType, String tenant)
			throws ResourceAuthorizeException {
		logger.debug("checking the permission");
		if (!OaccAuthUtil.isValidUser(user, false) || OaccAuthUtil.isEmpty(resourceName)
				|| OaccAuthUtil.isEmpty(permissionType)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException("Empty values are not allowed to validate");
		}
		Set<ResourcePermission> permissionSet = new HashSet<>();
		permissionSet.add(ResourcePermissions.getInstance(permissionType));
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new ResourceAuthorizeException("tenant mismatched on validating ..!" + tenant + " -vs- "
					+ accessControlContext.getDomainNameByResource(userAsResource));
		}

		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new ResourceAuthorizeException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		try {
			return accessControlContext.hasResourcePermissions(userAsResource,
					Resources.getInstance(resourceName + "-" + tenant), permissionSet);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public List<String> getAllRolePermittedResource(String resourceType, String role, String tenant,
			List<String> permissions) throws ResourceAccessException {
		logger.debug(".getAllUserPermittedResource..");
		if (OaccAuthUtil.isEmpty(role) || OaccAuthUtil.isEmpty(resourceType) || OaccAuthUtil.isEmpty(tenant)
				|| OaccAuthUtil.isEmptyList(permissions)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAccessException("Empty values are not allowed");
		}

		Resource roleAsResource = Resources.getInstance(role + "-" + tenant);
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(roleAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + role + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new ResourceAccessException(
					"resource " + role + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		Set<ResourcePermission> resperSet = new HashSet<>();
		for (String permission : permissions) {
			logger.debug("permission " + permission);
			resperSet.add(ResourcePermissions.getInstance(AuthUtil.decodeParameter(permission.toLowerCase())));
		}
		Set<Resource> resourceSet;
		try {
			resourceSet = accessControlContext.getResourcesByResourcePermissionsAndDomain(
					Resources.getInstance(role + "-" + tenant), resourceType, tenant, resperSet);
		} catch (IllegalArgumentException e) {
			throw new ResourceAccessException("role, resourceType, tenant or permission does not exist", e);
		}
		List<String> allResourceClass = accessControlContext.getResourceClassNames();
		for (Resource retResource : resourceSet) {
			resources.add(OaccAuthUtil.getResource(retResource.getExternalId(), tenant));
			for (int allResIte = 0; allResIte < allResourceClass.size(); allResIte++) {
				String childResource = retResource.getExternalId() + AuthConstants.RESOURCE_APPENDER;
				if ((childResource).equals(allResourceClass.get(allResIte))) {
					try {
						getAllRolePermittedResource(role, childResource, tenant, permissions);
					} catch (IllegalArgumentException exp) {
						OaccAuthUtil.dbCleanUp(connection);
						throw new ResourceAccessException(exp.getMessage(), exp);
					} finally {
						OaccAuthUtil.dbCleanUp(connection);
					}
				}
			}
		}
		return resources;
	}

	@Override
	public boolean hasPermissionForRole(String role, String resourceName, String permission, String tenant)
			throws ResourceAuthorizeException {
		logger.debug("checking the permission");
		if (OaccAuthUtil.isEmpty(role) || OaccAuthUtil.isEmpty(resourceName) || OaccAuthUtil.isEmpty(permission)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException("Empty values are not allowed");
		}
		Set<ResourcePermission> permissionSet = new HashSet<>();
		permissionSet.add(ResourcePermissions.getInstance(permission));
		Resource roleAsResource = Resources.getInstance(role + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(roleAsResource))) {
			throw new ResourceAuthorizeException("Invalid tenant specified !");
		}

		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(roleAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + role + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new ResourceAuthorizeException(
					"resource " + role + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		try {
			return accessControlContext.hasResourcePermissions(roleAsResource,
					Resources.getInstance(resourceName + "-" + tenant), permissionSet);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceAuthorizeException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void grantPermissionOnRole(String tenant, String roleId, String permission, String resourceId)
			throws PermissionGrantException {
		if (OaccAuthUtil.isEmpty(roleId) || OaccAuthUtil.isEmpty(permission) || OaccAuthUtil.isEmpty(resourceId)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the permission grant");
		}

		Resource role = Resources.getInstance(roleId + "-" + tenant);
		Resource resource = Resources.getInstance(resourceId + "-" + tenant);
		ResourcePermission activePer = ResourcePermissions.getInstance(permission);

		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(role))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(role);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + roleId + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new PermissionGrantException(
					"resource " + roleId + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		try {
			accessControlContext.grantResourcePermissions(role, resource, activePer);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	public List<String> getResouceClassIdByTenanatAndResourceClass(String tenant)
			throws ResourceAuthorizeException, PermissionGrantException {

		if (tenant == null || tenant.trim().isEmpty()) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the permission grant");
		}

		List<String> externalIds = new ArrayList<>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(QueryConstant.QUERY_FOR_EXTERNAL_ID);
			preparedStatement.setString(1, "%" + tenant + "%");
			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				while (rs.next()) {
					String externalId = rs.getString("ExternalId");
					externalIds.add(externalId);
				}
			} else
				throw new PermissionGrantException(
						"please provide valid tenant!!!!... Tenant " + tenant + " not exists");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return externalIds;

	}

	@Override
	public void updateRoleForUser(User user, String roleAlreadyAssociated, String roleToAssociate, String tenant)
			throws PermissionGrantException {

		logger.debug("inside update role for user method....");
		boolean isPasswordEnabled = false;
		List<String> externalIds = new ArrayList<>();

		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled) || OaccAuthUtil.isEmpty(roleToAssociate)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the role association");
		}
		Set<ResourcePermission> resperSet1 = new HashSet<>();
		resperSet1.add(ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		logger.debug("userAsResource " + userAsResource);
		Resource roleResource = Resources.getInstance(roleToAssociate + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new PermissionGrantException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		resourceClassInfo = accessControlContext.getResourceClassInfoByResource(roleResource);
		resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + roleToAssociate + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new PermissionGrantException(
					"resource " + roleToAssociate + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		PreparedStatement preparedStatementForAccessorId = null;
		PreparedStatement preparedStatementForUpdate = null;
		ResultSet rs = null;
		try {
			preparedStatementForAccessorId = connection.prepareStatement(QueryConstant.QUERY_FOR_FETCHING_ACCESSOR_ID);
			preparedStatementForAccessorId.setString(1, "%" + userAsResource.getExternalId() + "%");
			rs = preparedStatementForAccessorId.executeQuery();

			if (rs.next()) {
				do {
					String externalId = rs.getString("ExternalID");
					logger.debug("ExternalID is " + externalId);
					externalIds.add(externalId);
				} while (rs.next());

			} else
				throw new PermissionGrantException("User " + user.getUserName() + "doesn't exists");

			rs.close();

			if (externalIds.contains(roleAlreadyAssociated + "-" + tenant)) {

				preparedStatementForUpdate = connection.prepareStatement(QueryConstant.QUERY_FOR_UPDATE_ROLE);
				preparedStatementForUpdate.setString(1, "%" + roleToAssociate + "-" + tenant + "%");
				preparedStatementForUpdate.setString(2, "%" + userAsResource.getExternalId() + "%");
				int count = preparedStatementForUpdate.executeUpdate();
				if (count < 0)
					throw new PermissionGrantException("Cloud not able to update");

			} else
				throw new PermissionGrantException("Role cann't be upadted as role provided " + roleAlreadyAssociated
						+ " is not associated with USER");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (preparedStatementForAccessorId != null)
					preparedStatementForAccessorId.close();
				if (preparedStatementForUpdate != null)
					preparedStatementForUpdate.close();
				if (rs != null)
					rs.close();
				OaccAuthUtil.dbCleanUp(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void deleteRoleForUser(User user, String roleAlreadyAssociated, String roleToAssociate, String tenant)
			throws PermissionGrantException {

		logger.debug("inside delet role for user method....");
		boolean isPasswordEnabled = false;
		List<String> externalIds = new ArrayList<>();

		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled) || OaccAuthUtil.isEmpty(roleToAssociate)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the role association");
		}
		Set<ResourcePermission> resperSet1 = new HashSet<>();
		resperSet1.add(ResourcePermissions.getInstance(ResourcePermissions.INHERIT));
		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		logger.debug("userAsResource " + userAsResource);
		Resource roleResource = Resources.getInstance(roleToAssociate + "-" + tenant);
		if (!OaccAuthUtil.isValidTenant(tenant, accessControlContext.getDomainNameByResource(userAsResource))) {
			throw new PermissionGrantException("Invalid tenant specified !");
		}
		ResourceClassInfo resourceClassInfo = accessControlContext.getResourceClassInfoByResource(userAsResource);
		String resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + user.getUserName() + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_USERS))
			throw new PermissionGrantException(
					"resource " + user.getUserName() + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_USERS);

		resourceClassInfo = accessControlContext.getResourceClassInfoByResource(roleResource);
		resourceClass = resourceClassInfo.getResourceClassName();
		logger.debug("resource  " + roleToAssociate + " belongs to  " + resourceClass);
		if (!resourceClass.trim().equalsIgnoreCase(AuthConstants.RESOURCE_TYPE_ROLE))
			throw new PermissionGrantException(
					"resource " + roleToAssociate + " doesn't belongs to " + AuthConstants.RESOURCE_TYPE_ROLE);

		PreparedStatement preparedStatementForFexrchingAccessorId = null;
		PreparedStatement preparedStatementForDeleteQuery = null;
		ResultSet rs = null;

		try {
			preparedStatementForFexrchingAccessorId = connection
					.prepareStatement(QueryConstant.QUERY_FOR_FETCHING_ACCESSOR_ID);
			preparedStatementForFexrchingAccessorId.setString(1, "%" + userAsResource.getExternalId() + "%");
			rs = preparedStatementForFexrchingAccessorId.executeQuery();

			if (rs.next()) {
				do {
					String externalId = rs.getString("ExternalID");
					logger.debug("ExternalID is " + externalId);
					externalIds.add(externalId);
				} while (rs.next());

			} else
				throw new PermissionGrantException("User " + user.getUserName() + "doesn't exists");

			if (externalIds.contains(roleAlreadyAssociated + "-" + tenant)) {

				preparedStatementForDeleteQuery = connection.prepareStatement(QueryConstant.QUERY_FOR_DELETE_ROLE);
				preparedStatementForDeleteQuery.setString(1, "%" + userAsResource.getExternalId() + "%");
				int count = preparedStatementForDeleteQuery.executeUpdate();
				if (count < 0)
					throw new PermissionGrantException("Cloud not able to update");

			} else
				throw new PermissionGrantException("Role cann't be upadted as role provided " + roleAlreadyAssociated
						+ " is not associated with USER");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (preparedStatementForFexrchingAccessorId != null)
					preparedStatementForFexrchingAccessorId.close();
				if (preparedStatementForDeleteQuery != null)
					preparedStatementForDeleteQuery.close();
				if (rs != null)
					rs.close();
				OaccAuthUtil.dbCleanUp(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

	}

	private void doRevoke(String userResource, String roleResource, String resourceType)
			throws PermissionGrantException {

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(QueryConstant.QUERY_FOR_DELETE_RESOURCE);
			preparedStatement.setString(1, "%" + userResource + "%");
			preparedStatement.setString(2, "%" + roleResource + "%");
			preparedStatement.setString(3, "%" + resourceType + "%");
			int noOfRowAffected = preparedStatement.executeUpdate();
			if (noOfRowAffected < 0)
				throw new PermissionGrantException("Cloud not able to update");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (preparedStatement != null)
					preparedStatement.close();
				OaccAuthUtil.dbCleanUp(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void revokeResourcesForUser(String userString, List<String> resources, String tenant, String resourceType)
			throws PermissionGrantException {

		logger.debug("inside revoke resource for user method....");
		boolean isPasswordEnabled = false;
		User user = new User();
		user.setUserName(userString);

		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the role association");
		}

		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		logger.debug("userAsResource " + userAsResource);

		Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance
				.getMap(tenant + ResourceConstant._USERS);

		HashMap<String, Set<String>> userResourceMap = userResourceCache.get(userString);

		resources.forEach(resource -> {

			try {
				doRevoke(userString + "-" + tenant, resource + "-" + tenant, resourceType);
			} catch (PermissionGrantException e) {
				e.printStackTrace();
			}

			if (userResourceMap != null) {
				userResourceMap.remove(MenuAuthorizationServiceImpl.RESOURCE_TYPE_MENU
						+ ResourceConstant.RESOURCE_SEPERATOR + resource);
				logger.debug("after deleting the data the cache map :: " + userResourceCache.get(userString));
			}
			userResourceCache.put(userString, userResourceMap);
		});

	}

	@Override
	public void revokeResourceForUser(String userString, String resource, String tenant, String resourceType)
			throws PermissionGrantException {

		logger.debug("inside revoke resource for user method....");
		boolean isPasswordEnabled = false;
		User user = new User();
		user.setUserName(userString);

		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Invalid params are being passed to process the role association");
		}

		Resource userAsResource = Resources.getInstance(user.getUserName() + "-" + tenant);
		logger.debug("userAsResource " + userAsResource);
		doRevoke(userString + "-" + tenant, resource + "-" + tenant, resourceType);

	}

	@Override
	public void revokeResourcePermissionsForUser(String user, String resource, String tenant,
			List<String> permissions) {

		Resource userAsResource = Resources.getInstance(user + "-" + tenant);
		Resource roleAsResource = Resources.getInstance(resource + "-" + tenant);
		Set<ResourcePermission> permissionSet = new HashSet<>();
		permissions.forEach(permission -> {
			permissionSet.add(ResourcePermissions.getInstance(permission));
		});

		accessControlContext.revokeResourcePermissions(roleAsResource, userAsResource, permissionSet);

	}
	
	private void deleteUser(String userResource) throws PermissionGrantException {

		logger.debug("inside delete user db connection method");

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(QueryConstant.QUERY_FOR_DELETE_USER);
			preparedStatement.setString(1, userResource);

			logger.debug("query after settimg ... " + preparedStatement.toString());

			int noOfRowAffected = preparedStatement.executeUpdate();
			if (noOfRowAffected == 0)
				throw new PermissionGrantException("Cloud not able to update");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				if (preparedStatement != null)
					preparedStatement.close();

				OaccAuthUtil.dbCleanUp(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void deleteUser(String userName, String tenant) throws PermissionGrantException {

		logger.debug("inside delete user method....");
		boolean isPasswordEnabled = false;
		User user = new User();
		user.setUserName(userName);

		if (!OaccAuthUtil.isValidUser(user, isPasswordEnabled)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionGrantException("Please provide configure user");
		}

		deleteUser(userName + "-" + tenant);

		Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + "_users");
		logger.debug("userResourceCache :: " + userResourceCache);
		userResourceCache.remove(userName);
		logger.debug("userResourceCache after removing user :: " + userResourceCache);

	}

}
