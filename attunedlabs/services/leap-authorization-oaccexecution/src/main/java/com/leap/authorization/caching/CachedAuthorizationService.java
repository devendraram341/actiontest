package com.leap.authorization.caching;

import static com.leap.authorization.caching.AuthDBConstants.RESOURCE_SEPERATOR;
import static com.leap.authorization.caching.AuthDBConstants._ROLES;
import static com.leap.authorization.caching.AuthDBConstants._USERS;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;
import com.attunedlabs.leap.identityservice.IdentityServiceConstant;
import com.attunedlabs.leap.identityservice.IdentityServiceUtil;
import com.attunedlabs.leap.util.LeapConfigurationUtil;
import com.hazelcast.core.HazelcastInstance;
import com.leap.authorization.AuthConstants;
import com.leap.authorization.exception.AssertPermissionInvalidRequestException;
import com.leap.authorization.exception.CreatePermissionInvalidRequestException;
import com.leap.authorization.exception.CreateRoleInvalidRequestException;
import com.leap.authorization.exception.CreateUserInvalidRequestException;
import com.leap.authorization.exception.GetResourceInvalidRequestException;
import com.leap.authorization.exception.GetResourcePermissionInvalidRequestException;
import com.leap.authorization.exception.GrantingInvalidRequestException;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.RemoveUserInvalidRequestException;
import com.leap.authorization.service.User;
import com.leap.authorization.service.oacc.impl.MenuAuthorizationServiceImpl;
import com.leap.authorization.util.AuthUtil;
import com.leap.authorization.util.ResourceConstant;

public class CachedAuthorizationService {
	final static Logger logger = LoggerFactory.getLogger(CachedAuthorizationService.class);

	private HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();

	/**
	 * registering role inside the cache
	 * 
	 * @param exchange
	 * @throws CreateRoleInvalidRequestException
	 */
	public void registerRole(Exchange exchange) throws CreateRoleInvalidRequestException {
		logger.debug("inside registerRole()...");
		Message message = exchange.getIn();
		try {

			JSONObject roleData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			logger.debug("roleData is: " + roleData);
			roleData = AuthUtil.toUpperCaseKey(roleData);
			String role = roleData.getString(ResourceConstant.ROLE);
			String tenant = roleData.getString(ResourceConstant.TENANT_NAME);

			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _ROLES);
			if (!resourceCache.containsKey(role))
				resourceCache.put(role, new HashMap<String, Set<String>>());
		} catch (JSONException exp) {
			throw new CreateRoleInvalidRequestException("bad json format/TENANT, ROLE field is mandatory", exp,
					exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * registering roles inside the cache
	 * 
	 * @param exchange
	 * @throws CreateRoleInvalidRequestException
	 */
	public void registerRoles(Exchange exchange) throws CreateRoleInvalidRequestException {
		logger.debug("inside registerRoles()...");
		Message message = exchange.getIn();
		try {
			JSONObject roleData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			logger.debug("roleData is: " + roleData);
			roleData = AuthUtil.toUpperCaseKey(roleData);
			JSONArray roles = roleData.getJSONArray(ResourceConstant.ROLES);
			String tenant = roleData.getString(ResourceConstant.TENANT_NAME);

			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _ROLES);
			for (int i = 0; i < roles.length(); i++) {
				String role = roles.getString(i);
				if (!resourceCache.containsKey(role))
					resourceCache.put(role, new HashMap<String, Set<String>>());
			}
		} catch (JSONException exp) {
			throw new CreateRoleInvalidRequestException("bad json format/TENANT, ROLE field is mandatory", exp,
					exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);
	}

	/**
	 * registering users inside the cache
	 * 
	 * @param exchange
	 * @throws CreateUserInvalidRequestException
	 */
	public void registerUsers(Exchange exchange) throws CreateUserInvalidRequestException {
		logger.debug("inside registerUsers()...");
		Message message = exchange.getIn();
		try {
			JSONObject usersData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			usersData = AuthUtil.toUpperCaseKey(usersData);
			JSONArray users = usersData.getJSONArray((ResourceConstant.USERS));
			String tenant = usersData.getString(ResourceConstant.TENANT_NAME);

			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _USERS);
			for (int i = 0; i < users.length(); i++) {
				String user = users.getString(i);
				if (!resourceCache.containsKey(user))
					resourceCache.put(user, new HashMap<String, Set<String>>());
			}
		} catch (JSONException exp) {
			throw new CreateUserInvalidRequestException("bad json format/TENANT, USERS field is mandatory", exp,
					exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * registering user inside the cache
	 * 
	 * @param exchange
	 * @throws CreateUserInvalidRequestException
	 */
	public void registerUser(Exchange exchange) throws CreateUserInvalidRequestException {
		logger.debug("inside registerUser()...");
		Message message = exchange.getIn();
		try {
			JSONObject usersData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			usersData = AuthUtil.toUpperCaseKey(usersData);
			String userName = usersData.getString((ResourceConstant.USER));
			String tenant = usersData.getString(ResourceConstant.TENANT_NAME);
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _USERS);
			if (!resourceCache.containsKey(userName))
				resourceCache.put(userName, new HashMap<String, Set<String>>());
		} catch (JSONException exp) {
			throw new CreateUserInvalidRequestException("bad json format/TENANT, USER field is mandatory", exp,
					exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * removing user inside the cache
	 * 
	 * @param exchange
	 * @throws RemoveUserInvalidRequestException
	 * @throws CreateUserInvalidRequestException
	 */
	public void removeUser(Exchange exchange) throws RemoveUserInvalidRequestException {
		logger.debug("inside ..removeUser service bean");
		String userName = null;
		String tenant = null;
		User user = new User();
		Message message = exchange.getIn();
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject usersData = new JSONObject(inputJsonStr);
			message.setHeader(AuthConstants.REQUEST_DATA, usersData);

			usersData = AuthUtil.toUpperCaseKey(usersData);
			userName = usersData.getString((ResourceConstant.USER));
			user.setUserName(userName);
			tenant = usersData.getString(ResourceConstant.TENANT_NAME);
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _USERS);
			removeCachedUser(resourceCache, userName);
		} catch (JSONException exp) {
			throw new RemoveUserInvalidRequestException("bad json format/USER,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}

		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * grantPermissionForUser inside the cache e.g : tenant_users -> mark ->
	 * item:inventory -> read,write,delete
	 * 
	 * @param exchange
	 * @throws CreatePermissionInvalidRequestException
	 */
	public void grantPermissionForUser(Exchange exchange) throws CreatePermissionInvalidRequestException {
		logger.debug("inside grantPermissionForUser()...");
		Message message = exchange.getIn();
		try {
			JSONObject resourcePermissionData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			logger.debug("resourcePermissionData is " + resourcePermissionData);
			resourcePermissionData = AuthUtil.toUpperCaseKey(resourcePermissionData);
			String tenant = resourcePermissionData.getString(ResourceConstant.TENANT_NAME);
			JSONArray permissions = resourcePermissionData.getJSONArray(ResourceConstant.PERMISSIONS);
			String userString = resourcePermissionData.getString(ResourceConstant.USER);
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _USERS);
			if (resourcePermissionData.has(ResourceConstant.RESOURCE)) {
				String resourcetoGrantOn = resourcePermissionData.getString(ResourceConstant.RESOURCE);
				String parentClass = getParentResourceClass(resourcetoGrantOn, tenant, exchange);
				logger.debug("resource belongs to " + parentClass);
				String resourceKey = parentClass + RESOURCE_SEPERATOR + resourcetoGrantOn;
				if (resourceCache.containsKey(userString)) {
					HashMap<String, Set<String>> userResourceMap = resourceCache.get(userString);
					// if (!userResourceMap.containsKey(resourceKey))
					userResourceMap.put(resourceKey, AuthUtil.jsonArrayToSet(permissions));
					// else {
					// Set<String> lastPermissions =
					// userResourceMap.get(resourceKey);
					// Set<String> passedPermissions =
					// AuthUtil.jsonArrayToSet(permissions);
					// lastPermissions.addAll(passedPermissions);
					// userResourceMap.put(resourceKey, lastPermissions);
					// }
					resourceCache.put(userString, userResourceMap);
				}
				for (Entry<String, HashMap<String, Set<String>>> iterable_element : resourceCache.entrySet()) {
					System.out.println("user permission : " + iterable_element);
				}
			} else if (resourcePermissionData.has(ResourceConstant.RESOURCES)) {
				JSONArray resourcetoGrantOns = resourcePermissionData.getJSONArray(ResourceConstant.RESOURCES);
				resourcetoGrantOns.forEach(resourcetoGrantOn -> {
					String parentClass = null;
					try {
						parentClass = getParentResourceClass(resourcetoGrantOn.toString(), tenant, exchange);
					} catch (Exception e) {
						e.printStackTrace();
					}
					logger.debug("resource belongs to " + parentClass);
					String resourceKey = parentClass + RESOURCE_SEPERATOR + resourcetoGrantOn;
					if (resourceCache.containsKey(userString)) {
						HashMap<String, Set<String>> userResourceMap = resourceCache.get(userString);
						userResourceMap.put(resourceKey, AuthUtil.jsonArrayToSet(permissions));
						resourceCache.put(userString, userResourceMap);
					}
				});

			}
		} catch (Exception exp) {
			exp.printStackTrace();
			throw new CreatePermissionInvalidRequestException(
					"bad json format/TENANT, PERMISSIONS,USER,RESOURCE field is mandatory/resource doesn't belong to resource class",
					exp, exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * grantPermissionForRole inside the cache e.g : tenant_roles -> admin ->
	 * item:inventory -> fetch
	 * 
	 * @param exchange
	 * @throws CreatePermissionInvalidRequestException
	 */
	public void grantPermissionForRole(Exchange exchange) throws CreatePermissionInvalidRequestException {
		logger.debug("inside grantPermissionForRole()...");
		Message message = exchange.getIn();
		try {

			JSONObject resourcePermissionData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);
			logger.debug("resourcePermissionData is " + resourcePermissionData);
			resourcePermissionData = AuthUtil.toUpperCaseKey(resourcePermissionData);

			String permission = resourcePermissionData.getString(ResourceConstant.PERMISSION);
			String roleString = resourcePermissionData.getString(ResourceConstant.ROLE);
			String resourcetoGrantOn = resourcePermissionData.getString(ResourceConstant.RESOURCE);
			String tenant = resourcePermissionData.getString(ResourceConstant.TENANT_NAME);
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _ROLES);
			String parentClass = getParentResourceClass(resourcetoGrantOn, tenant, exchange);
			logger.debug("resource belongs to " + parentClass);
			String resourceKey = parentClass + RESOURCE_SEPERATOR + resourcetoGrantOn;
			if (resourceCache.containsKey(roleString)) {
				HashMap<String, Set<String>> roleResourceMap = resourceCache.get(roleString);
				if (!roleResourceMap.containsKey(resourceKey)) {
					Set<String> premissions = new HashSet<>();
					premissions.add(permission);
					roleResourceMap.put(resourceKey, premissions);
				} else {
					Set<String> lastPermissions = roleResourceMap.get(resourceKey);
					lastPermissions.add(permission);
					roleResourceMap.put(resourceKey, lastPermissions);
				}
				resourceCache.put(roleString, roleResourceMap);
				for (Entry<String, HashMap<String, Set<String>>> iterable_element : resourceCache.entrySet()) {
					System.out.println("role permission : " + iterable_element);
				}
			}

		} catch (Exception exp) {
			throw new CreatePermissionInvalidRequestException(
					"bad json format/TENANT, PERMISSIONS,ROLE,RESOURCE field is mandatory/resource doesn't belong to resource class",
					exp, exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * this method gets the cached permission for user on the specific resource.
	 * 
	 * @param exchange
	 * @throws GetResourcePermissionInvalidRequestException
	 * @throws InvalidConfiguration
	 */
	public void getPermissionForUser(Exchange exchange)
			throws GetResourcePermissionInvalidRequestException, InvalidConfiguration {
		logger.debug("inside getPermissionForUser()...");
		Message message = exchange.getIn();
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is: " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String user = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.USER));
			final String resourceName = AuthUtil
					.decodeParameter(resourceData.getString(ResourceConstant.RESOURCE_NAME));
			String tenant = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.TENANT_NAME));
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _USERS);
			String resource = getParentResourceClass(resourceName, tenant, exchange) + RESOURCE_SEPERATOR
					+ resourceName;

			permissionForUser(message, resourceCache, resource, user);

		} catch (Exception exp) {
			throw new GetResourcePermissionInvalidRequestException(
					"bad json format/TENANT, USER,RESOURCENAME field is mandatory", exp, exp.getMessage(), 400);
		}
	}

	/**
	 * this method gets the cached permission for role on the specific resource.
	 * 
	 * @param exchange
	 * @throws GetResourcePermissionInvalidRequestException
	 * @throws InvalidConfiguration
	 */
	public void getPermissionForRole(Exchange exchange)
			throws GetResourcePermissionInvalidRequestException, InvalidConfiguration {
		logger.debug("inside getPermissionForRole()...");
		Message message = exchange.getIn();
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is: " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String role = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.ROLE));
			final String resourceName = AuthUtil
					.decodeParameter(resourceData.getString(ResourceConstant.RESOURCE_NAME));
			String tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			tenant = AuthUtil.decodeParameter(tenant);
			Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(tenant + _ROLES);
			String resource = getParentResourceClass(resourceName, tenant, exchange) + RESOURCE_SEPERATOR
					+ resourceName;

			permissionForRole(message, resourceCache, resource, role);

		} catch (Exception exp) {
			throw new GetResourcePermissionInvalidRequestException(
					"bad json format/TENANT, USER,RESOURCENAME field is mandatory", exp, exp.getMessage(), 400);
		}
	}

	/**
	 * this method will associate the role to the user.
	 * 
	 * @param exchange
	 * @throws GrantingInvalidRequestException
	 */
	public void associateRoleToUser(Exchange exchange) throws GrantingInvalidRequestException {
		logger.debug("inside associateRoleToUser()...");
		Message message = exchange.getIn();
		try {
			JSONObject userRoleData = message.getHeader(AuthConstants.REQUEST_DATA, JSONObject.class);

			logger.debug("userRoleData is: " + userRoleData);
			userRoleData = AuthUtil.toUpperCaseKey(userRoleData);
			String userExtId = userRoleData.getString(ResourceConstant.USER);
			String roleToInherit = userRoleData.getString(ResourceConstant.ROLE);
			String tenant = userRoleData.getString(ResourceConstant.TENANT_NAME);

			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);
			if (userResourceCache.containsKey(userExtId)) {
				HashMap<String, Set<String>> userRoles = userResourceCache.get(userExtId);
				if (!userRoles.containsKey(_ROLES)) {
					Set<String> roles = new HashSet<>();
					roles.add(roleToInherit);
					userRoles.put(_ROLES, roles);
				} else {
					Set<String> lastRoles = userRoles.get(_ROLES);
					lastRoles.add(roleToInherit);
					userRoles.put(_ROLES, lastRoles);
				}
				userResourceCache.put(userExtId, userRoles);
			}

		} catch (JSONException exp) {
			throw new GrantingInvalidRequestException("bad json format/TENANT, USER, ROLE field is mandatory", exp,
					exp.getMessage(), 400);
		}
		message.removeHeader(AuthConstants.REQUEST_DATA);

	}

	/**
	 * get roles assigned to user.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 */
	public void getRolesForUser(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside getRolesForUser()...");
		Message message = exchange.getIn();
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String username = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.USER));
			String tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			tenant = AuthUtil.decodeParameter(tenant);
			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);

			roleForUser(message, userResourceCache, username);

		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException("bad json format/TENANT, USER field is mandatory", exp,
					exp.getMessage(), 400);
		}

	}

	/**
	 * to get the accessible resources for user.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 */
	public void getAllUserAccessibleResource(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside ..getAllUserAccessibleResource service bean");
		Message message = exchange.getIn();
		String tenant = null;
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String userString = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.USER));
			final String resourceType = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.RESOURCETYPE));
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			tenant = AuthUtil.decodeParameter(tenant);
			JSONArray permissions = resourceData.getJSONArray(ResourceConstant.PERMISSIONS);
			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);
			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			Set<String> permissionsSet = AuthUtil.jsonArrayToSet(permissions);
			if (userResourceCache.containsKey(userString)) {
				// resources under user and role
				Set<String> resourceAccessibleSync = userAccessibleResources(userResourceCache, rolesResourceCache,
						permissionsSet, userString, resourceType);

				JSONObject data = new JSONObject();
				data.put(ResourceConstant.RESOURCES, resourceAccessibleSync);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + userString);

		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException(
					"bad json format/USER,RESOURCETYPE,PERMISSIONS,TENANT field is mandatory", exp, exp.getMessage(),
					400);
		}
	}

	/**
	 * gets all the accessible resources for the role.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 */
	public void getAllRoleAccessibleResource(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside ..getAllRoleAccessibleResource service bean");
		Message message = exchange.getIn();
		String tenant = null;
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String role = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.ROLE));
			final String resourceType = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.RESOURCETYPE));
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			JSONArray permissions = resourceData.getJSONArray(ResourceConstant.PERMISSIONS);
			Map<String, HashMap<String, Set<String>>> roleResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			tenant = AuthUtil.decodeParameter(tenant);
			Set<String> permissionsSet = AuthUtil.jsonArrayToSet(permissions);
			if (roleResourceCache.containsKey(role)) {
				// resources under role
				Set<String> resourceAccessibleSync = roleAccessibleResources(roleResourceCache, role, resourceType,
						permissionsSet);

				JSONObject data = new JSONObject();
				data.put(ResourceConstant.RESOURCES, resourceAccessibleSync);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + role);

		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException(
					"bad json format/ROLE,RESOURCETYPE,PERMISSIONS,TENANT field is mandatory", exp, exp.getMessage(),
					400);
		}
	}

	/**
	 * validates whether user has permission or not
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 * @throws AssertPermissionInvalidRequestException
	 */
	public void hasUserPermission(Exchange exchange) throws AssertPermissionInvalidRequestException {
		logger.debug("inside ..hasUserPermission service bean");
		try {
			Message message = exchange.getIn();
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject assertData = new JSONObject(inputJsonStr);
			if (AuthUtil.hasDataKey(assertData)) {
				JSONArray jsonArray = new JSONArray(assertData.get(ResourceConstant.DATA_KEY).toString());
				assertData = (JSONObject) jsonArray.get(0);
				assertData = AuthUtil.toUpperCaseKey(assertData);
			} else {
				assertData = AuthUtil.toUpperCaseKey(assertData);
			}
			final String userName = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.USER));
			final String resourceName = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.RESOURCE));
			final String permissionType = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.PERMISSION));
			String tenant = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.TENANT_NAME));

			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);
			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			if (userResourceCache.containsKey(userName)) {
				// check resources under user and user assigned role
				boolean hasPermission = hasUserResourceAccess(userResourceCache, rolesResourceCache, permissionType,
						userName, resourceName);

				if (!hasPermission) {
					throw new AssertPermissionInvalidRequestException("user is not authorized", null,
							"user is not authorized for " + resourceName, 403);
				}
				JSONObject data = new JSONObject();
				data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + userName);

		} catch (JSONException exp) {
			throw new AssertPermissionInvalidRequestException(
					"bad json format/USER,RESOURCE and PERMISSION,TENANT field is mandatory", exp, exp.getMessage(),
					403);
		}

	}

	/**
	 * validates whether role has permission or not
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 * @throws AssertPermissionInvalidRequestException
	 */
	public void hasRolePermission(Exchange exchange) throws AssertPermissionInvalidRequestException {
		logger.debug("inside ..hasRolePermission service bean");
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject assertData = new JSONObject(inputJsonStr);
			if (AuthUtil.hasDataKey(assertData)) {
				JSONArray jsonArray = new JSONArray(assertData.get(ResourceConstant.DATA_KEY).toString());
				assertData = (JSONObject) jsonArray.get(0);
				assertData = AuthUtil.toUpperCaseKey(assertData);
			} else {
				assertData = AuthUtil.toUpperCaseKey(assertData);
			}
			final String role = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.ROLE));
			final String resourceName = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.RESOURCE));
			final String permissionType = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.PERMISSION));
			String tenant = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.TENANT_NAME));

			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			if (rolesResourceCache.containsKey(role)) {
				boolean hasPermission = hasRoleResourceAccess(rolesResourceCache, permissionType, resourceName, role);
				if (!hasPermission) {
					throw new AssertPermissionInvalidRequestException("role is not authorized", null,
							"role is not authorized for " + resourceName, 403);
				}
				JSONObject data = new JSONObject();
				data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
				exchange.getIn().setHeader("cachedResult", true);
				exchange.getIn().setBody(data);
			} else
				logger.debug("no authorization info found for " + role);
		} catch (JSONException exp) {
			throw new AssertPermissionInvalidRequestException(
					"bad json format/ROLE,RESOURCE and PERMISSION,TENANT field is mandatory", exp, exp.getMessage(),
					403);
		}

	}

	/**
	 * validates whether user has permission or not
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 * @throws AssertPermissionInvalidRequestException
	 */
	public void hasMenuItemUserPermission(Exchange exchange) throws AssertPermissionInvalidRequestException {
		logger.debug("inside ..hasMenuItemUserPermission service bean");
		try {
			Message message = exchange.getIn();
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject assertData = new JSONObject(inputJsonStr);
			if (AuthUtil.hasDataKey(assertData)) {
				JSONArray jsonArray = new JSONArray(assertData.get(ResourceConstant.DATA_KEY).toString());
				assertData = (JSONObject) jsonArray.get(0);
				assertData = AuthUtil.toUpperCaseKey(assertData);
			} else {
				assertData = AuthUtil.toUpperCaseKey(assertData);
			}
			final String userName = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.USER));
			final String menuItem = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.MENU_ITEM));
			final String permissionType = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.PERMISSION));
			String tenant = AuthUtil.decodeParameter(assertData.getString(ResourceConstant.TENANT_NAME));

			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);
			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			if (userResourceCache.containsKey(userName)) {
				// check resources under user and user assigned role
				boolean hasPermission = hasUserResourceAccess(userResourceCache, rolesResourceCache, permissionType,
						userName, menuItem);

				if (!hasPermission) {
					throw new AssertPermissionInvalidRequestException("user is not authorized", null,
							"user is not authorized for " + menuItem, 403);
				}
				JSONObject data = new JSONObject();
				data.put(ResourceConstant.HAS_PERMISSION, hasPermission);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + userName);

		} catch (JSONException exp) {
			throw new AssertPermissionInvalidRequestException(
					"bad json format/USER,MENUITEM and PERMISSION field is mandatory", exp, exp.getMessage(), 403);
		}

	}

	/**
	 * gets all the menuitems that user has permission for.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 * @throws AssertPermissionInvalidRequestException
	 */
	public void getAllAccessibleMenuItemsForUser(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside ..getAllAccessibleMenuItemsForUser service bean");
		Message message = exchange.getIn();
		String tenant = null;
		try {
			LeapDataContext leapDataContext =LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String userString = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.USER));
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			tenant = AuthUtil.decodeParameter(tenant);
			final String resourceType = MenuAuthorizationServiceImpl.RESOURCE_TYPE_MENU;
			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _USERS);
			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			Set<String> permissionsSet = new HashSet<>();

			if (userResourceCache.containsKey(userString)) {
				// resources under user and role
				Set<String> resourceAccessibleSync = userAccessibleResources(userResourceCache, rolesResourceCache,
						permissionsSet, userString, resourceType);

				JSONObject data = new JSONObject();
				data.put(ResourceConstant.RESOURCES, resourceAccessibleSync);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + userString);
		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException("bad json format/USER,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}

	}

	/**
	 * gets all the menuitems that role has permission for.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 * @throws AssertPermissionInvalidRequestException
	 */
	public void getAllAccessibleMenuItemsForRole(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside ..getAllAccessibleMenuItemsForRole service bean");
		Message message = exchange.getIn();
		String tenant = null;
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			final String role = AuthUtil.decodeParameter(resourceData.getString(ResourceConstant.ROLE));
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			tenant = AuthUtil.decodeParameter(tenant);
			final String resourceType = MenuAuthorizationServiceImpl.RESOURCE_TYPE_MENU;
			Map<String, HashMap<String, Set<String>>> rolesResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			Set<String> permissionsSet = new HashSet<>();
			permissionsSet.add(MenuAuthorizationServiceImpl.PERMISSION_READ);
			if (rolesResourceCache.containsKey(role)) {
				// resources under role
				Set<String> resourceAccessibleSync = roleAccessibleResources(rolesResourceCache, role, resourceType,
						permissionsSet);
				JSONObject data = new JSONObject();
				data.put(ResourceConstant.RESOURCES, resourceAccessibleSync);
				logger.debug("resourceAccessibleSync " + resourceAccessibleSync);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			} else
				logger.debug("no authorization info found for " + role);

		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException("bad json format/ROLE,TENANT field is mandatory", exp,
					exp.getMessage(), 400);
		}

	}

	/**
	 * gets all the accessible resources for the role.
	 * 
	 * @param exchange
	 * @throws GetResourceInvalidRequestException
	 */
	public void getAllRoleAccessibleResourceJava7(Exchange exchange) throws GetResourceInvalidRequestException {
		logger.debug("inside ..getAllRoleAccessibleResource service bean");
		Message message = exchange.getIn();
		String role = null;
		String resourceType = null;
		String tenant = null;
		try {
			LeapDataContext leapDataContext = LeapConfigurationUtil.getLDCFromExchange(exchange);
			String inputJsonStr = leapDataContext.getInitialRequestData()
					.getData().toString();
			JSONObject resourceData = new JSONObject(inputJsonStr);
			logger.debug("resourceData is " + resourceData);
			if (AuthUtil.hasDataKey(resourceData)) {
				JSONArray jsonArray = new JSONArray(resourceData.get(ResourceConstant.DATA_KEY).toString());
				resourceData = (JSONObject) jsonArray.get(0);
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			} else {
				resourceData = AuthUtil.toUpperCaseKey(resourceData);
			}
			role = resourceData.getString(ResourceConstant.ROLE);
			resourceType = resourceData.getString(ResourceConstant.RESOURCETYPE);
			tenant = resourceData.getString(ResourceConstant.TENANT_NAME);
			JSONArray permissions = resourceData.getJSONArray(ResourceConstant.PERMISSIONS);

			role = AuthUtil.decodeParameter(role);
			resourceType = AuthUtil.decodeParameter(resourceType);
			tenant = AuthUtil.decodeParameter(tenant);
			Map<String, HashMap<String, Set<String>>> userResourceCache = hazelcastInstance.getMap(tenant + _ROLES);

			Set<String> permissionsSet = AuthUtil.jsonArrayToSet(permissions);
			if (userResourceCache.containsKey(role)) {
				HashMap<String, Set<String>> userRoles = userResourceCache.get(role);
				Set<String> resourcesAvailable = userRoles.keySet();
				Set<String> resourceAccessible = new HashSet<>();
				JSONObject data = new JSONObject();
				for (String resource : resourcesAvailable) {
					String[] splitResource = resource.split(RESOURCE_SEPERATOR);
					if (splitResource.length == 2) {
						String resourceClass = splitResource[0];
						if (resourceClass.equalsIgnoreCase(resourceType)) {
							String resourceId = splitResource[1];
							Set<String> permissionsProvided = userRoles.get(resource);
							boolean flag = false;
							for (String permission : permissionsSet) {
								flag = permissionsProvided.contains(permission);
								if (!flag)
									break;
							}
							if (flag)
								resourceAccessible.add(resourceId);
						}
					}
				}
				data.put(ResourceConstant.RESOURCES, resourceAccessible);
				message.setHeader("cachedResult", true);
				message.setBody(data);
			}

		} catch (JSONException exp) {
			throw new GetResourceInvalidRequestException(
					"bad json format/ROLE,RESOURCETYPE,PERMISSIONS,TENANT field is mandatory", exp, exp.getMessage(),
					400);
		}
	}

	private void permissionForUser(Message message, Map<String, HashMap<String, Set<String>>> resourceCache,
			String resource, String user) {

		logger.debug("inside permissionForUser cache check...for " + user + " on resource " + resource);
		long startTime = System.currentTimeMillis();
		resourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(user)).findFirst()
				.ifPresent(userResources -> userResources.getValue().entrySet().parallelStream()
						.filter(e -> e.getKey().equalsIgnoreCase(resource)).findFirst().ifPresent(userResource -> {
							try {
								logger.debug("userResource : " + userResource);
								JSONObject data = new JSONObject();
								data.put(ResourceConstant.ACCESS_PERMISSION, userResource.getValue());
								message.setBody(data);
								message.setHeader("cachedResult", true);
							} catch (JSONException e2) {
								logger.error("failed to get the access permission from cache ", userResource);
								e2.printStackTrace();
							}
						}));
		long endTime = System.currentTimeMillis();
		logger.debug("permissionForUser Took : " + (endTime - startTime));

	}

	private void permissionForRole(Message message, Map<String, HashMap<String, Set<String>>> resourceCache,
			String resource, String role) {
		logger.debug("inside permissionForRole cache check...for " + role + " on resource " + resource);
		long startTime = System.currentTimeMillis();

		resourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(role)).findFirst()
				.ifPresent(roleResources -> roleResources.getValue().entrySet().parallelStream()
						.filter(e -> e.getKey().equalsIgnoreCase(resource)).findFirst().ifPresent(roleResource -> {
							try {
								JSONObject data = new JSONObject();
								data.put(ResourceConstant.ACCESS_PERMISSION, roleResource.getValue());
								message.setBody(data);
								message.setHeader("cachedResult", true);
							} catch (JSONException e2) {
								logger.error("failed to get the access permission from cache ", roleResource);
								e2.printStackTrace();
							}
						}));
		long endTime = System.currentTimeMillis();
		logger.debug("permissionForRole Took : " + (endTime - startTime));
	}

	private void roleForUser(Message message, Map<String, HashMap<String, Set<String>>> userResourceCache,
			String username) {
		logger.debug("inside roleForUser cache check...for " + username);
		long startTime = System.currentTimeMillis();

		userResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(username)).findFirst()
				.ifPresent(roleResources -> roleResources.getValue().entrySet().parallelStream()
						.filter(e -> e.getKey().contains(_ROLES)).findFirst().map(roleResource -> {
							try {
								JSONObject data = new JSONObject();
								data.put(ResourceConstant.ROLES, roleResource.getValue());
								message.setHeader("cachedResult", true);
								message.setBody(data);
								updateJWTValue(message, data);
							} catch (JSONException e2) {
								logger.error("failed to get the access permission from cache ", roleResource);
								e2.printStackTrace();
							}
							return roleResource;
						}));
		long endTime = System.currentTimeMillis();
		logger.debug("roleForUser Took : " + (endTime - startTime));
	}

	/**
	 * This method is used to update the JWT token value
	 * 
	 * @param message
	 * @param data
	 */
	private void updateJWTValue(Message message, JSONObject data) {
		Object isAuthzEnableObj = message.getHeader(IdentityServiceConstant.IS_AUTHZ_ENABLED);
		if (isAuthzEnableObj != null) {
			Boolean isAuthzEnable = Boolean
					.valueOf(message.getHeader(IdentityServiceConstant.IS_AUTHZ_ENABLED, String.class));
			if (isAuthzEnable) {
				Object jwtTokenObj = message.getHeader(IdentityServiceConstant.JWT_TOKEN_KEY);
				if (jwtTokenObj != null) {
					String jwtTokenValueFromHeader = message.getHeader(IdentityServiceConstant.JWT_TOKEN_KEY,
							String.class);
					Map<String, Object> jwtTokenEncodedDataMapValues = IdentityServiceUtil
							.getEncodedFormatDeatilsFromJWT(jwtTokenValueFromHeader);
					String payloadEncodedData = (String) jwtTokenEncodedDataMapValues
							.get(IdentityServiceConstant.JWT_PAYLOAD_DATA_KEY);
					logger.debug("payloadEncodedData :: " + payloadEncodedData);
					String updatedJWTToken = AuthUtil.updateJWTTokenValueWithAuthzValue(payloadEncodedData, data);
					String jwtTokenVal = jwtTokenEncodedDataMapValues.get(IdentityServiceConstant.JWT_HEADER_DATA_KEY)
							+ "." + updatedJWTToken + "."
							+ jwtTokenEncodedDataMapValues.get(IdentityServiceConstant.JWT_SECURITY_DATA_KEY);
					message.setHeader(IdentityServiceConstant.JWT_TOKEN_KEY, jwtTokenVal);
				} // ..end of if condition checking the jwt token value is
					// not
					// null
					// or not
			} // ..end of if method , to check isAuthzEnable is true
		} // ..end of if method to check the isAuthzEnable is null or not
	}// ..end of updateJWTValue method

	private boolean hasUserResourceAccess(Map<String, HashMap<String, Set<String>>> userResourceCache,
			Map<String, HashMap<String, Set<String>>> rolesResourceCache, String permissionType, String userName,
			String resourceName) {
		logger.debug("inside hasUserResourceAccess cache check...for " + userName + " for resource " + resourceName
				+ " has permission " + permissionType);
		long startTime = System.currentTimeMillis();

		AtomicBoolean hasPermission = new AtomicBoolean(false);
		// resources under user
		userResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(userName))
				.map(e -> e.getValue()).findFirst()
				.ifPresent(userResources -> userResources.entrySet().parallelStream().map(userResourceSet -> {
					String[] splitResource = userResourceSet.getKey().split(RESOURCE_SEPERATOR);
					if (splitResource.length == 2) {
						String resourceId = splitResource[1];
						if (resourceId.equalsIgnoreCase(resourceName))
							hasPermission.set(userResourceSet.getValue().parallelStream()
									.anyMatch(permissionType::equalsIgnoreCase));
					}
					return userResourceSet;
				}).count());

		if (!hasPermission.get())
			// resources under roles for user
			userResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(userName)).findFirst()
					.ifPresent(userResources -> userResources.getValue().entrySet().parallelStream()
							.filter(e -> e.getKey().contains(_ROLES)).findFirst()
							.ifPresent(roles -> roles.getValue().parallelStream()
									.forEach(role -> rolesResourceCache.entrySet().parallelStream()
											.filter(e -> e.getKey().equals(role)).map(e -> e.getValue()).findFirst()
											.ifPresent(roleResources -> roleResources.entrySet().parallelStream()
													.map(roleResourceSet -> {
														String[] splitResource = roleResourceSet.getKey()
																.split(RESOURCE_SEPERATOR);
														if (splitResource.length == 2) {
															String resourceId = splitResource[1];
															if (resourceId.equalsIgnoreCase(resourceName))
																hasPermission.set(roleResourceSet.getValue()
																		.parallelStream()
																		.anyMatch(permissionType::equalsIgnoreCase));
														}
														return roleResourceSet;
													}).count()))));
		long endTime = System.currentTimeMillis();
		logger.debug("hasUserResourceAccess Took : " + (endTime - startTime));
		return hasPermission.get();
	}

	private boolean hasRoleResourceAccess(Map<String, HashMap<String, Set<String>>> rolesResourceCache,
			String permissionType, String resourceName, String role) {
		logger.debug("inside hasRoleResourceAccess cache check...for " + role + " for resource " + resourceName
				+ " has permission " + permissionType);
		long startTime = System.currentTimeMillis();

		AtomicBoolean hasPermission = new AtomicBoolean(false);
		rolesResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(role))
				.map(e -> e.getValue()).findFirst()
				.ifPresent(roleResources -> roleResources.entrySet().parallelStream().map(roleResourcesSet -> {
					String[] splitResource = roleResourcesSet.getKey().split(RESOURCE_SEPERATOR);
					if (splitResource.length == 2) {
						String resourceId = splitResource[1];
						if (resourceId.equalsIgnoreCase(resourceName))
							if (resourceId.equalsIgnoreCase(resourceName))
								hasPermission.set(roleResourcesSet.getValue().parallelStream()
										.anyMatch(permissionType::equalsIgnoreCase));
					}
					return roleResourcesSet;
				}).count());
		long endTime = System.currentTimeMillis();
		logger.debug("hasRoleResourceAccess Took : " + (endTime - startTime));
		return hasPermission.get();
	}

	private Set<String> userAccessibleResources(Map<String, HashMap<String, Set<String>>> userResourceCache,
			Map<String, HashMap<String, Set<String>>> rolesResourceCache, Set<String> permissionsSet, String userString,
			String resourceType) {
		logger.debug("inside userAccessibleResources cache check...for " + userString + " under resource "
				+ resourceType + " with permissions " + permissionsSet);

		// long sttTime = System.currentTimeMillis();
		// for (Entry<String, HashMap<String, Set<String>>> iterable_element :
		// userResourceCache.entrySet()) {
		// System.out.println("user resources & permission : " +
		// iterable_element);
		// }
		//
		// for (Entry<String, HashMap<String, Set<String>>> iterable_element :
		// rolesResourceCache.entrySet()) {
		// System.out.println("role resources & permission : " +
		// iterable_element);
		// }
		//
		// long enTime = System.currentTimeMillis();
		// System.out.println("Ignore Time : " + (enTime - sttTime));

		long startTime = System.currentTimeMillis();

		Set<String> resourceAccessibleSync = Collections.synchronizedSet(new HashSet<>());
		Map<String, Set<String>> resourcesAccessible = new ConcurrentHashMap<String, Set<String>>();

		// resources under user
		userResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(userString))
				.map(e -> e.getValue()).findFirst()
				.ifPresent(e -> e.entrySet().parallelStream().map(userResourcesSet -> {
					String resourceKey = userResourcesSet.getKey();
					Set<String> uncheckedPermissions = userResourcesSet.getValue().parallelStream()
							.filter(permission -> !userResourcesSet.getValue().contains(permission))
							.collect(Collectors.toSet());
					resourcesAccessible.put(resourceKey, uncheckedPermissions);
					return userResourcesSet;
				}).count());

		// resources under roles for user
		userResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(userString)).findFirst()
				.ifPresent(userResourcesSet -> userResourcesSet.getValue().entrySet().parallelStream()
						.filter(e -> e.getKey().contains(_ROLES)).findFirst()
						.ifPresent(roles -> roles.getValue().parallelStream()
								.forEach(role -> rolesResourceCache.entrySet().parallelStream()
										.filter(e -> e.getKey().equals(role)).map(e -> e.getValue()).findFirst()
										.ifPresent(roleResources -> roleResources.entrySet().parallelStream()
												.map(roleResourcesSet -> {

													String resourceKey = roleResourcesSet.getKey();
													Set<String> uncheckedPermissions = roleResourcesSet.getValue();
													if (resourcesAccessible.containsKey(resourceKey))
														uncheckedPermissions = resourcesAccessible.get(resourceKey);
													Set<String> checkedPermissions = uncheckedPermissions
															.parallelStream().filter(permission -> !roleResourcesSet
																	.getValue().contains(permission))
															.collect(Collectors.toSet());
													resourcesAccessible.put(resourceKey, checkedPermissions);
													return roleResourcesSet;
												}).count()))));

		// add the accessible resources to the set.
		resourcesAccessible.entrySet().parallelStream().filter(e -> e.getValue().isEmpty()).map(e -> e.getKey())
				.forEach(resource -> {
					String[] splitResource = resource.split(RESOURCE_SEPERATOR);
					if (splitResource.length == 2) {
						String resourceClass = splitResource[0];
						if (resourceClass.equalsIgnoreCase(resourceType)) {
							String resourceId = splitResource[1];
							resourceAccessibleSync.add(resourceId);
						}
					}
				});
		long endTime = System.currentTimeMillis();
		logger.debug("userAccessibleResources Took : " + (endTime - startTime));
		return resourceAccessibleSync;
	}

	private Set<String> roleAccessibleResources(Map<String, HashMap<String, Set<String>>> rolesResourceCache,
			String role, String resourceType, Set<String> permissionsSet) {
		logger.debug("inside roleAccessibleResources cache check...for " + role + " under resource " + resourceType
				+ " with permissions " + permissionsSet);
		long startTime = System.currentTimeMillis();

		Set<String> resourceAccessibleSync = Collections.synchronizedSet(new HashSet<>());
		rolesResourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(role))
				.map(e -> e.getValue()).findFirst()
				.ifPresent(roleResources -> roleResources.entrySet().parallelStream().map(roleResourcesSet -> {
					String[] splitResource = roleResourcesSet.getKey().split(RESOURCE_SEPERATOR);
					if (splitResource.length == 2) {
						String resourceClass = splitResource[0];
						if (resourceClass.equalsIgnoreCase(resourceType)) {
							String resourceId = splitResource[1];
							if (permissionsSet.parallelStream().allMatch(roleResourcesSet.getValue()::contains))
								resourceAccessibleSync.add(resourceId);
						}
					}
					return roleResourcesSet;
				}).count());
		long endTime = System.currentTimeMillis();
		logger.debug("roleAccessibleResources Took : " + (endTime - startTime));
		return resourceAccessibleSync;
	}

	private Map<String, HashMap<String, Set<String>>> removeCachedUser(
			Map<String, HashMap<String, Set<String>>> resourceCache, String user) {
		logger.debug("inside removeCachedUser cache check...for " + user);
		long startTime = System.currentTimeMillis();
		resourceCache.entrySet().parallelStream().filter(e -> e.getKey().equalsIgnoreCase(user)).findFirst()
				.ifPresent(r -> resourceCache.remove(r.getKey(), r.getValue()));
		long endTime = System.currentTimeMillis();
		logger.debug("removeCachedUser Took : " + (endTime - startTime));
		return resourceCache;

	}

	// utility to get the parent class based upon implementation feature.
	private String getParentResourceClass(String resource, String domain, Exchange exchange) throws Exception {
		switch (exchange.getIn().getHeader("provider", String.class)) {
		case "oacc":
			return ResourceCacheUtil.getResourceClassName("oacc", domain, resource);
		case "shiro":
			return ResourceCacheUtil.getResourceClassName("shiro", domain, resource);
		default:
			throw new InvalidConfiguration("Incorrect Provider specified!");

		}

	}
}
