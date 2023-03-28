/**
 * 
 */
package com.leap.authorization.service.oacc.impl;

import static com.leap.authorization.util.ResourceConstant._USERS;
import static com.leap.authorization.util.ResourceConstant.RESOURCE_SEPERATOR;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.ResourceAccessException;
import com.leap.authorization.exception.ResourceAuthorizeException;
import com.leap.authorization.exception.ResourceRegistryException;
import com.leap.authorization.service.IAuthorizationGenericService;
import com.leap.authorization.service.IAuthorizationSetup;
import com.leap.authorization.service.IMenuAuthorizationService;
import com.leap.authorization.service.User;

/**
 * @author bizruntime33
 *
 */
public class MenuAuthorizationServiceImpl implements IMenuAuthorizationService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private IAuthorizationSetup iAuthorizationSetup;
	private IAuthorizationGenericService iAuthorizationGenericService;
	public static final String RESOURCE_TYPE_MENU = "MENU";
	public static final String PERMISSION_READ = "READ";

	private HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();

	/**
	 * @throws InvalidConfiguration
	 * 
	 */
	public MenuAuthorizationServiceImpl() throws InvalidConfiguration {
		this.iAuthorizationSetup = new AuthorizationSetupImpl();
		this.iAuthorizationGenericService = new AuthorizationGenericServiceImpl();
		logger.info("Successfully initialized services..");
	}

	@Override
	public void addMenuItems(List<String> menuItems, String tenant)
			throws ResourceRegistryException, InvalidConfiguration {
		iAuthorizationSetup.addNewResources(menuItems, RESOURCE_TYPE_MENU, tenant);
		logger.info("added new menu items " + menuItems);
	}

	@Override
	public List<String> getAllMenuItemsAccessibleforUser(String user, String tenant) throws ResourceAccessException {
		List<String> permissions = new ArrayList<>();
		permissions.add(PERMISSION_READ);
		logger.debug("..getAllMenuItemsAccessibleforUser.."+permissions);
		User userDetail = new User();
		userDetail.setUserName(user);
		return iAuthorizationGenericService.getAllUserPermittedResource(RESOURCE_TYPE_MENU, userDetail, tenant,
				permissions);
	}

	@Override
	public List<String> getAllMenuItemsAccessibleforRole(String role, String tenant) throws ResourceAccessException {
		List<String> permissions = new ArrayList<>();
		permissions.add(PERMISSION_READ);
		// permissions.add("Clock");
		logger.debug("..getAllMenuItemsAccessibleforRole..");
		return iAuthorizationGenericService.getAllRolePermittedResource(RESOURCE_TYPE_MENU, role, tenant, permissions);
	}

	@Override
	public boolean hasPermissionForUser(String user, String menuItem, String permission, String tenant)
			throws ResourceAuthorizeException {
		logger.debug("..hasPermissionForUser..");
		User userDetail = new User();
		userDetail.setUserName(user);
		return iAuthorizationGenericService.hasPermissionForUser(userDetail, menuItem, permission, tenant);
	}

}
