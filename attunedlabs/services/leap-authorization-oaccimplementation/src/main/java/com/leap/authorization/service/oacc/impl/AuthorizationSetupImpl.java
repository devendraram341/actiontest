package com.leap.authorization.service.oacc.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acciente.oacc.AccessControlContext;
import com.acciente.oacc.ResourcePermission;
import com.acciente.oacc.Resources;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.AuthorizationSetupException;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.PermissionAccessException;
import com.leap.authorization.exception.ResourceRegistryException;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.util.OaccAuthUtil;

public class AuthorizationSetupImpl implements IAuthorizationSetup {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private AccessControlContext accessControlContext;
	private Connection connection;

	public AuthorizationSetupImpl() throws InvalidConfiguration {
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
	public void addNewResourceTypeToAuthorize(String resourceType, List<String> typeofPermissions)
			throws AuthorizationSetupException {
		logger.debug(".addNewResourceTypeToAuthorize ");
		if (OaccAuthUtil.isEmpty(resourceType)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException("Invalid params requested to register..");
		}
		if (OaccAuthUtil.isEmptyList(typeofPermissions)) {
			try {
				accessControlContext.createResourceClass(resourceType, false, false);
				logger.info("Registered new resourceType- " + resourceType + " - with empty permissions");
			} catch (IllegalArgumentException exp) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new AuthorizationSetupException(exp.getMessage(), exp);
			} finally {
				OaccAuthUtil.dbCleanUp(connection);
			}
		} else {
			try {
				accessControlContext.createResourceClass(resourceType, false, false);
				for (String typeofPermission : typeofPermissions) {
					accessControlContext.createResourcePermission(resourceType, typeofPermission);
				}
				logger.info("Registered new resourceType- " + resourceType + " - with list of permissions"
						+ typeofPermissions);
			} catch (IllegalArgumentException exp) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new AuthorizationSetupException(exp.getMessage(), exp);
			} finally {
				OaccAuthUtil.dbCleanUp(connection);
			}
		}
	}

	@Override
	public List<String> getAllPermissions(String resourceType) throws PermissionAccessException {
		if (OaccAuthUtil.isEmpty(resourceType)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionAccessException("Empty resourceType is requested to getPermissions");
		}
		List<String> permissionList;
		try {
			permissionList = accessControlContext.getResourcePermissionNames(resourceType);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionAccessException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
		return permissionList;
	}

	@Override
	public List<String> getAllUserPermissionsOnResource(String user, String resource, String tenant)
			throws PermissionAccessException {
		logger.debug("getting accessed permission for: " + user + " on resource: " + resource);
		if (OaccAuthUtil.isEmpty(user) || OaccAuthUtil.isEmpty(resource) || OaccAuthUtil.isEmpty(tenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionAccessException("Empty values are not allowed");
		}
		String compactTenant = tenant.trim();
		List<String> allowedPermInString = new ArrayList<>();
		Set<ResourcePermission> allowedPerm = null;
		try {
			if (!OaccAuthUtil.isValidTenant(compactTenant,
					accessControlContext.getDomainNameByResource(Resources.getInstance(user + "-" + compactTenant)))
					|| !OaccAuthUtil.isValidTenant(compactTenant, accessControlContext
							.getDomainNameByResource(Resources.getInstance(resource + "-" + compactTenant)))) {
				throw new PermissionAccessException("Belonging tenants mismatched !");
			}
			allowedPerm = accessControlContext.getResourcePermissions(Resources.getInstance(user + "-" + compactTenant),
					Resources.getInstance(resource + "-" + compactTenant));
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new PermissionAccessException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
		for (ResourcePermission resourcePermission : allowedPerm) {
			allowedPermInString.add(resourcePermission.toString());
		}
		return allowedPermInString;
	}

	@Override
	public void addNewTenant(String tenantName) throws AuthorizationSetupException {
		logger.debug(".registerTenant...");
		if (OaccAuthUtil.isEmpty(tenantName)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException("Empty domain is requested to register");
		}
		String compactTenant = tenantName.trim();
		try {
			accessControlContext.createDomain(compactTenant);
			logger.info("New tenant - " + compactTenant + " - has been registered successfully..!");
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void addNewTenant(String tenantName, String parentTenant) throws AuthorizationSetupException {
		logger.debug(".registerTenant...");
		if (OaccAuthUtil.isEmpty(tenantName) || OaccAuthUtil.isEmpty(parentTenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException("Empty domain is requested to register");
		}
		String compactParentTenant = parentTenant.trim();
		String compactTenant = tenantName.trim();
		try {
			accessControlContext.createDomain(compactTenant, compactParentTenant);
			logger.info("New tenant - " + compactTenant + " - has been registered successfully..!");
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void registerRoles(List<String> roles, String tenant) throws ResourceRegistryException {
		logger.debug(".registerRoles...");
		if (!OaccAuthUtil.isEmptyList(roles) || !OaccAuthUtil.isEmpty(tenant)) {
			String compactTenant = tenant.trim();
			try {
				for (String role : roles) {
					if (OaccAuthUtil.isEmpty(role)) {
						OaccAuthUtil.dbCleanUp(connection);
						throw new ResourceRegistryException("Empty role requested to register..");
					}
					String compactRole = role.trim();
					accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_ROLE, compactTenant,
							compactRole + "-" + compactTenant);
				}
			} catch (IllegalArgumentException exp) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new ResourceRegistryException(exp.getMessage(), exp);
			} finally {
				OaccAuthUtil.dbCleanUp(connection);
			}
		} else {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException("Empty roles/tenant requested to register..!");
		}
	}

	@Override
	public void registerRole(String role, String tenant) throws ResourceRegistryException {
		logger.debug(".registerRole...");
		if (OaccAuthUtil.isEmpty(role) || OaccAuthUtil.isEmpty(tenant)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException("Empty role requested to register..");
		}
		String compactRole = role.trim();
		String comactTenant = tenant.trim();
		try {
			accessControlContext.createResource(AuthConstants.RESOURCE_TYPE_ROLE, comactTenant,
					compactRole + "-" + comactTenant);
			logger.info("Roles requested has been registered successfully..!");
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void addNewPermissionsOnResourceType(List<String> typesOfPermissions, String resourceType)
			throws AuthorizationSetupException {
		logger.debug(".addNewPermissionsOnResourceType ");
		if (OaccAuthUtil.isEmpty(resourceType)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException("Invalid params requested to register..");
		}
		if (!OaccAuthUtil.isEmptyList(typesOfPermissions)) {
			try {
				for (String typeofPermission : typesOfPermissions) {
					accessControlContext.createResourcePermission(resourceType, typeofPermission);
				}
				logger.info("Registered new permissions- " + typesOfPermissions + " -on resourceType -" + resourceType);
			} catch (IllegalArgumentException exp) {
				OaccAuthUtil.dbCleanUp(connection);
				throw new AuthorizationSetupException(exp.getMessage(), exp);
			} finally {
				OaccAuthUtil.dbCleanUp(connection);
			}
		} else {
			OaccAuthUtil.dbCleanUp(connection);
			throw new AuthorizationSetupException("Empty permissions requested to add..!");
		}
	}

	@Override
	public void addNewResources(List<String> resources, String resourceType, String tenant)
			throws ResourceRegistryException {
		logger.debug("inside addNewResources()... resourcesToBeAdded : " + resources + " in resource Type "
				+ resourceType + " under domain " + tenant);
		if (OaccAuthUtil.isEmptyList(resources)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException("empty resources requested to create");
		}
		try {
			for (String resource : resources) {
				if (OaccAuthUtil.isEmpty(resource) || OaccAuthUtil.isEmpty(tenant)
						|| OaccAuthUtil.isEmpty(resourceType)) {
					OaccAuthUtil.dbCleanUp(connection);
					throw new ResourceRegistryException("Invalid params requested to create/register ");
				}
				String comactResource = resource.trim();
				String compactTenant = tenant.trim();
				accessControlContext.createResource(resourceType, compactTenant, comactResource + "-" + compactTenant);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}

	@Override
	public void addNewResource(String resource, String resourceType, String tenant) throws ResourceRegistryException {
		logger.debug(".createItems....");
		if (OaccAuthUtil.isEmpty(resource) || OaccAuthUtil.isEmpty(tenant) || OaccAuthUtil.isEmpty(resourceType)) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException("Invalid params requested to create");
		}
		try {
			String compactResource = resource.trim();
			String compactTenant = tenant.trim();
			accessControlContext.createResource(resourceType, compactTenant, compactResource + "-" + compactTenant);
		} catch (IllegalArgumentException exp) {
			OaccAuthUtil.dbCleanUp(connection);
			throw new ResourceRegistryException(exp.getMessage(), exp);
		} finally {
			OaccAuthUtil.dbCleanUp(connection);
		}
	}
}
