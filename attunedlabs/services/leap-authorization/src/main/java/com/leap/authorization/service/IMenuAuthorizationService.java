/**
 * 
 */
package com.leap.authorization.service;

import java.util.List;

import com.leap.authorization.exception.InvalidConfiguration;
import com.leap.authorization.exception.ResourceAccessException;
import com.leap.authorization.exception.ResourceAuthorizeException;
import com.leap.authorization.exception.ResourceRegistryException;

/**
 * @author bizruntime33
 *
 */
public interface IMenuAuthorizationService {

	/**
	 * Service to add menuItems as list, all newly additions of menu items has
	 * to be done through this api
	 * 
	 * @param menuItems
	 * @throws ResourceRegistryException
	 * @throws InvalidConfiguration 
	 */
	public void addMenuItems(List<String> menuItems, String tenant) throws ResourceRegistryException, InvalidConfiguration;

	/**
	 * Service to get all user accessible menu-items after verifying the
	 * permitted resources within
	 * 
	 * @param user
	 * @return
	 * @throws ResourceAccessException
	 */
	public List<String> getAllMenuItemsAccessibleforUser(String user, String tenant) throws ResourceAccessException;

	/**
	 * Service to get all items accessible for roles after verifying the
	 * permitted resources within
	 * 
	 * @param role
	 * @return
	 * @throws ResourceAccessException
	 */
	public List<String> getAllMenuItemsAccessibleforRole(String role, String tenant) throws ResourceAccessException;

	/**
	 * Does the assertion for user on a menuItem Service to check the permission
	 * on a resource for a user
	 * 
	 * @param user
	 * @param menuItem
	 * @param permission
	 * @return
	 * @throws ResourceAuthorizeException
	 */
	public boolean hasPermissionForUser(String user, String menuItem, String permission,String tenant)
			throws ResourceAuthorizeException;

}
