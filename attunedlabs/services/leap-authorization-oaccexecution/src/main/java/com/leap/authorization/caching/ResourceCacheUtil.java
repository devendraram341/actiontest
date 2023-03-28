package com.leap.authorization.caching;

import static com.leap.authorization.caching.AuthDBConstants.AccessedResource;
import static com.leap.authorization.caching.AuthDBConstants.AccessedResourceClass;
import static com.leap.authorization.caching.AuthDBConstants.AccessorResource;
import static com.leap.authorization.caching.AuthDBConstants.AccessorResourceClass;
import static com.leap.authorization.caching.AuthDBConstants.Domain;
import static com.leap.authorization.caching.AuthDBConstants.Permission;
import static com.leap.authorization.caching.AuthDBConstants.RESOURCE_DOMAIN_SEPERATOR;
import static com.leap.authorization.caching.AuthDBConstants.RESOURCE_SEPERATOR;
import static com.leap.authorization.caching.AuthDBConstants.ResourceClass;
import static com.leap.authorization.caching.AuthDBConstants._ROLES;
import static com.leap.authorization.caching.AuthDBConstants._USERS;
import static com.leap.authorization.caching.AuthQueries.OACC_RESOURCE_CLASS_QUERY;
import static com.leap.authorization.caching.AuthQueries.OACC_ROLES_QUERY;
import static com.leap.authorization.caching.AuthQueries.OACC_USER_QUERY;
import static com.leap.authorization.caching.AuthQueries.OACC_USERS_QUERY;
import static com.leap.authorization.caching.AuthQueries.OACC_USER_ROLES_QUERY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.camel.Exchange;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.leap.authorization.exception.GetResourcePermissionInvalidRequestException;
import com.leap.authorization.util.OaccAuthUtil;
import com.leap.authorization.util.ResourceConstant;
import com.leap.authorization.util.ShiroAuthUtil;

public class ResourceCacheUtil {

	private static HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
	final static Logger logger = LoggerFactory.getLogger(ResourceCacheUtil.class);

	public static void preLoadRoles(String forDB) throws Exception {
		logger.debug("inside preLoadRoles method of ResourceCacheUtil forDB ::: " + forDB);
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		ResultSet usrs = null;
		try {
			switch (forDB) {
			case "oacc":
				connection = OaccAuthUtil.getOaccConnection();
				statement = connection.createStatement();
				rs = statement.executeQuery(OACC_ROLES_QUERY);
				while (rs.next())
					cacheAuthorizationForRoles(rs);

				usrs = statement.executeQuery(OACC_USERS_QUERY);
				while (usrs.next())
					cacheAuthorizationForUser(usrs);

				break;
			case "shiro":
				connection = ShiroAuthUtil.getDBConnection();
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeConnection(rs, statement, connection);
		}
	}

	@SuppressWarnings("resource")
	public void loadUserResourcesToCache(Exchange exchange) throws Exception {
		logger.debug("inside loadUserResourcesToCache()...");
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String user = exchange.getIn().getHeader("user", String.class);
		String domain = exchange.getIn().getHeader("domain", String.class);
		if (user == null || domain == null)
			throw new GetResourcePermissionInvalidRequestException("User / Domain Not specified!", null,
					"Login Failed!", 400);
		try {
			switch (exchange.getIn().getHeader("provider", String.class)) {
			case "oacc":
				connection = OaccAuthUtil.getOaccConnection();
				statement = connection.prepareStatement(OACC_USER_QUERY);
				statement.setString(1, user + RESOURCE_DOMAIN_SEPERATOR + domain);
				statement.setString(2, domain);
				rs = statement.executeQuery();
				while (rs.next())
					cacheAuthorizationForUser(rs, domain);
				statement = connection.prepareStatement(OACC_USER_ROLES_QUERY);
				statement.setString(1, user + RESOURCE_DOMAIN_SEPERATOR + domain);
				statement.setString(2, domain);
				rs = statement.executeQuery();
				while (rs.next())
					cacheRolesForUser(rs, domain);
				break;
			case "shiro":
				connection = ShiroAuthUtil.getDBConnection();
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeConnection(rs, statement, connection);
		}
		exchange.getIn().setHeader("cachedResult", true);
	}

	@SuppressWarnings("unused")
	public static void cacheAuthorizationForUser(ResultSet rs, String domain) throws Exception {
		String accessorResourceClass = rs.getString(AccessorResourceClass);
		String accessorResource = rs.getString(AccessorResource).split(RESOURCE_DOMAIN_SEPERATOR)[0];
		String accessedResource = rs.getString(AccessedResource).split(RESOURCE_DOMAIN_SEPERATOR)[0];
		String accessedResourceClass = rs.getString(AccessedResourceClass);
		String permission = rs.getString(Permission);
		Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(domain + _USERS);
		String resourceKey = accessedResourceClass + RESOURCE_SEPERATOR + accessedResource;
		if (!resourceCache.containsKey(accessorResource))
			resourceCache.put(accessorResource, new HashMap<String, Set<String>>());

		HashMap<String, Set<String>> userResourceMap = resourceCache.get(accessorResource);
		if (!userResourceMap.containsKey(resourceKey)) {
			Set<String> permissions = new HashSet<>();
			permissions.add(permission);
			userResourceMap.put(resourceKey, permissions);
		} else {
			Set<String> lastPermissions = userResourceMap.get(resourceKey);
			lastPermissions.add(permission);
			userResourceMap.put(resourceKey, lastPermissions);
		}
		resourceCache.put(accessorResource, userResourceMap);

	}

	@SuppressWarnings("unused")
	public static void cacheRolesForUser(ResultSet rs, String domain) throws Exception {
		String accessorResourceClass = rs.getString(AccessorResourceClass);
		String accessorResource = rs.getString(AccessorResource).split(RESOURCE_DOMAIN_SEPERATOR)[0];
		String accessedResource = rs.getString(AccessedResource).split(RESOURCE_DOMAIN_SEPERATOR)[0];
		String accessedResourceClass = rs.getString(AccessedResourceClass);
		Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(domain + _USERS);

		if (!resourceCache.containsKey(accessorResource))
			resourceCache.put(accessorResource, new HashMap<String, Set<String>>());

		HashMap<String, Set<String>> userRoles = resourceCache.get(accessorResource);
		if (!userRoles.containsKey(_ROLES)) {
			Set<String> roles = new HashSet<>();
			roles.add(accessedResource);
			userRoles.put(_ROLES, roles);
		} else {
			Set<String> lastRoles = userRoles.get(_ROLES);
			lastRoles.add(accessedResource);
			userRoles.put(_ROLES, lastRoles);
		}
		resourceCache.put(accessorResource, userRoles);

	}

	@SuppressWarnings("unused")
	public static void cacheAuthorizationForRoles(ResultSet rs) throws Exception {
		String accessorResourceClass = rs.getString(AccessorResourceClass);
		String accessorResource = rs.getString(AccessorResource);
		accessorResource = accessorResource.substring(0, accessorResource.lastIndexOf(RESOURCE_DOMAIN_SEPERATOR));
		String accessedResource = rs.getString(AccessedResource);
		accessedResource = accessedResource.substring(0, accessedResource.lastIndexOf(RESOURCE_DOMAIN_SEPERATOR));
		String accessedResourceClass = rs.getString(AccessedResourceClass);
		String permission = rs.getString(Permission);
		String domain = rs.getString(Domain);
		Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(domain + _ROLES);
		String resourceKey = accessedResourceClass + RESOURCE_SEPERATOR + accessedResource;
		if (!resourceCache.containsKey(accessorResource))
			resourceCache.put(accessorResource, new HashMap<String, Set<String>>());

		HashMap<String, Set<String>> roleResourceMap = resourceCache.get(accessorResource);
		if (!roleResourceMap.containsKey(resourceKey)) {
			Set<String> permissions = new HashSet<>();
			permissions.add(permission);
			roleResourceMap.put(resourceKey, permissions);
		} else {
			Set<String> lastPermissions = roleResourceMap.get(resourceKey);
			lastPermissions.add(permission);
			roleResourceMap.put(resourceKey, lastPermissions);
		}
		resourceCache.put(accessorResource, roleResourceMap);

	}

	@SuppressWarnings("unused")
	public static void cacheAuthorizationForUser(ResultSet rs) throws Exception {
		logger.debug(".cacheAuthorizationForUser method");
		String accessorResourceClass = rs.getString(AccessorResourceClass);
		String accessorResource = rs.getString(AccessorResource);
		accessorResource = accessorResource.substring(0, accessorResource.lastIndexOf(RESOURCE_DOMAIN_SEPERATOR));
		String accessedResource = rs.getString(AccessedResource);
		accessedResource = accessedResource.substring(0, accessedResource.lastIndexOf(RESOURCE_DOMAIN_SEPERATOR));
		String accessedResourceClass = rs.getString(AccessedResourceClass);
		String permission = rs.getString(Permission);
		String domain = rs.getString(Domain);
		Map<String, HashMap<String, Set<String>>> resourceCache = hazelcastInstance.getMap(domain + _USERS);
		String resourceKey = accessedResourceClass + RESOURCE_SEPERATOR + accessedResource;
		if (!resourceCache.containsKey(accessorResource))
			resourceCache.put(accessorResource, new HashMap<String, Set<String>>());

		HashMap<String, Set<String>> userResourceMap = resourceCache.get(accessorResource);
		if (!userResourceMap.containsKey(resourceKey)) {
			Set<String> permissions = new HashSet<>();
			permissions.add(permission);
			userResourceMap.put(resourceKey, permissions);
		} else {
			Set<String> lastPermissions = userResourceMap.get(resourceKey);
			lastPermissions.add(permission);
			userResourceMap.put(resourceKey, lastPermissions);
		}
		resourceCache.put(accessorResource, userResourceMap);
		logger.debug("resourceCache " + resourceCache);

	}

	public static String getResourceClassName(String db, String domain, String resource) throws Exception {
		logger.debug("inside getResourceClassName()...from " + db + " under " + domain + " find " + resource);
		long start = System.currentTimeMillis();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String className = "NotFound";
		try {
			switch (db) {
			case "oacc":
				connection = OaccAuthUtil.getOaccConnection();
				statement = connection.prepareStatement(OACC_RESOURCE_CLASS_QUERY);
				statement.setString(1, domain);
				statement.setString(2, resource + RESOURCE_DOMAIN_SEPERATOR + domain);
				rs = statement.executeQuery();
				if (rs.next())
					className = rs.getString(ResourceClass);
				break;
			case "shiro":
				connection = ShiroAuthUtil.getDBConnection();
				className = "#toImplement";
			}
		} catch (Exception e) {
			throw e;
		} finally {
			closeConnection(rs, statement, connection);
		}
		long end = System.currentTimeMillis();
		logger.debug("getResourceClassName Took : " + (end - start));
		return className;
	}

	static void closeConnection(ResultSet rs, Statement smt, Connection con) {
		if (rs != null) {
			try {
				rs.close();
				rs = null;
			} catch (SQLException s) {
				s.printStackTrace();
			}
		}
		if (smt != null) {
			try {
				smt.close();
				smt = null;
			} catch (SQLException s) {
				s.printStackTrace();
			}
		}

		if (con != null) {
			try {
				con.close();
				con = null;
			} catch (SQLException s) {
				s.printStackTrace();
			}
		}
	}

	/**
	 * This method is used to update the payload data with required authz values
	 * 
	 * @param base64PayloadEncodedData :: String base64PayloadEncodedData
	 * @param data
	 * @return
	 */
	public static String updateJWTTokenValueWithAuthzValue(String base64PayloadEncodedData, JSONObject data) {
		byte[] payloadDecodeBase64Data = Base64.decodeBase64(base64PayloadEncodedData.getBytes());
		JSONObject payloadJsonObject = new JSONObject(new String(payloadDecodeBase64Data));
		JSONObject userDataJsonObj = payloadJsonObject.getJSONObject("userData");
		userDataJsonObj.put(ResourceConstant.ROLES, data.get(ResourceConstant.ROLES));
		payloadJsonObject.put("userData", userDataJsonObj);
		byte[] payloadData = payloadJsonObject.toString().getBytes();
		String payloadDatabase64Encoded = DatatypeConverter.printBase64Binary(payloadData);
		return payloadDatabase64Encoded;

	}// ..end of method updateJWTTokenValueWithAuthzValue
}
