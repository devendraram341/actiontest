package com.attunedlabs.leapentity.config;

import java.util.List;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;

public interface ILeapDataServiceConfiguration {

	/**
	 * To add the Entity Configuration in DB and cahe
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param entityConfig
	 * @throws LeapDataServiceConfigurationException
	 */
	public void addEntityConfiguration(ConfigurationContext configurationContext, Entity entityConfig)
			throws LeapDataServiceConfigurationException;

	/**
	 * To check Entity in DB and cache ,if Exist in DB check wether it is Enabled or
	 * not if enabled check data exist in cache or not , if config data not Exist
	 * load the the data to cache
	 * 
	 * @param requestContext
	 * @param entityName
	 * @return boolean Value
	 * @throws LeapDataServiceConfigurationException
	 */

	public boolean checkEntityConfigarationExistOrNot(ConfigurationContext configurationContext, String entityName)
			throws LeapDataServiceConfigurationException;

	/**
	 * To get the Entity Configuration Unit
	 * 
	 * @param context
	 * @param entityName
	 * @return {@link LeapDataServiceConfigurationUnit}
	 * @throws LeapDataServiceConfigurationException
	 */
	public LeapDataServiceConfigurationUnit getEntityConfiguration(RequestContext context, String entityName)
			throws LeapDataServiceConfigurationException;

	/**
	 * To update the existing Entity Configuration
	 * 
	 * @param configurationContext
	 * @param configurationUnit
	 * @param configNodeDataId
	 * @return {@link Integer}
	 * @throws LeapDataServiceConfigurationException
	 */
	public int updateEntityConfiguration(ConfigurationContext configurationContext,
			LeapDataServiceConfigurationUnit configurationUnit, int configNodeDataId)
			throws LeapDataServiceConfigurationException;

	/**
	 * To Enable/Disable the Entity Configuration
	 * 
	 * @param configContext
	 * @param configName
	 * @param isEnable
	 * @throws LeapDataServiceConfigurationException
	 */
	public void changeStatusOfEntityConfiguration(ConfigurationContext configContext, String configName,
			boolean isEnable) throws LeapDataServiceConfigurationException;

	/**
	 * To Delete the Entity Configuration
	 * 
	 * @param configurationContext
	 * @param configName
	 * @return {@link Boolean}
	 * @throws LeapDataServiceConfigurationException
	 */
	public boolean deleteEntityConfiguration(ConfigurationContext configurationContext, String configName)
			throws LeapDataServiceConfigurationException;

	/**
	 * To get the Entity Configuration Data
	 * 
	 * @param context
	 * @param entityName
	 * @return
	 * @throws LeapDataServiceConfigurationException
	 */
	public Entity getEntityCacheObject(RequestContext context, String entityName)
			throws LeapDataServiceConfigurationException;

	/**
	 * To get the All Entity Configuration Data
	 * 
	 * @param context
	 * @return {@link List} of {@link Entity}
	 * @throws LeapDataServiceConfigurationException
	 */
	public List<Entity> getAllEntityCacheObject(RequestContext context) throws LeapDataServiceConfigurationException;

	/**
	 * adds an EntityAccess for a particular entity
	 * 
	 * @param requestContext
	 * @param entityName
	 * @param entityAccess
	 * @throws LeapDataServiceConfigurationException
	 */
	public void addEntityAccess(RequestContext requestContext, String entityName, String configType,
			EntityAccess entityAccess) throws LeapDataServiceConfigurationException;

	/**
	 * removes an EntityAccess for a particular Entity
	 * 
	 * @param requestContext
	 * @param entityName
	 * @param authorizedResource
	 * @throws LeapDataServiceConfigurationException
	 */
	public void removeEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource) throws LeapDataServiceConfigurationException;

	/**
	 * update an EntityAccess by entity name and authorizedResource.
	 * 
	 * @param requestContext
	 * @param entityName
	 * @param authorizedResource
	 * @param entityAccess
	 * @throws LeapDataServiceConfigurationException
	 */
	public void updateEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource, EntityAccess entityAccess) throws LeapDataServiceConfigurationException;

	/**
	 * get EntityAccess based on entityName and authorizedResource.
	 * 
	 * @param requestContext
	 * @param entityName
	 * @param authorizedResource
	 * @return EntityAccess
	 * @throws LeapDataServiceConfigurationException
	 */
	public EntityAccess getEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource) throws LeapDataServiceConfigurationException;

	/**
	 * gets the all EntityAccess for an Entity.
	 * 
	 * @param requestContext
	 * @param entityName
	 * @return List<EntityAccess>
	 * @throws LeapDataServiceConfigurationException
	 */
	public List<EntityAccess> getAllEntityAccess(RequestContext requestContext, String entityName,String configType)
			throws LeapDataServiceConfigurationException;
}
