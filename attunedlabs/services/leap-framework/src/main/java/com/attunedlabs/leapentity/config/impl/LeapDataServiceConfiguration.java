package com.attunedlabs.leapentity.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.GenericApplicableNode;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.leapentity.config.ILeapDataServiceConfiguration;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationUnit;
import com.attunedlabs.leapentity.config.LeapDataServiceConstant;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.leapentity.config.jaxb.EntityAccess;
import com.attunedlabs.leapentity.config.jaxb.EntityAccessConfig;

public class LeapDataServiceConfiguration extends GenericApplicableNode implements ILeapDataServiceConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(LeapDataServiceConfiguration.class);

	@Override
	public void addEntityConfiguration(ConfigurationContext configurationContext, Entity entityConfig)
			throws LeapDataServiceConfigurationException {
		String methodName = "addEntityConfiguration";
		logger.debug("{} entered into the method {} , EntityName ={} ", LEAP_LOG_KEY, methodName,
				entityConfig.getName());
		try {
			String tenantId = configurationContext.getTenantId();
			String siteId = configurationContext.getSiteId();
			String vendorName = configurationContext.getVendorName();
			String version = configurationContext.getVersion();
			String featureGroup = configurationContext.getFeatureGroup();
			String feature = configurationContext.getFeatureName();
			String implName = configurationContext.getImplementationName();

			Integer configNodeId = 0;
			// Check and get ConfigNodeId for this
			if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
					&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, feature, implName,
						vendorName, version);
			} else {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, feature, implName);
			}
			logger.debug("{} Applicable Config Node Id is = {}", LEAP_LOG_KEY, configNodeId);
			LeapEntityConfigXMLParser configXMLParser = new LeapEntityConfigXMLParser();
			String entityAsString = configXMLParser.unmarshallEntityObjecttoXML(entityConfig);
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(entityConfig.getName());
			configNodeData.setEnabled(entityConfig.isIsEnable());
			boolean isConfigEnabled = entityConfig.isIsEnable();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(LeapDataServiceConstant.CONFIGURATION_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(entityAsString);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, entityConfig.getName(), LeapDataServiceConstant.CONFIGURATION_TYPE);
			int confignodeDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				confignodeDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new LeapDataServiceConfigurationException(
						"LeapDataServiceConfiguration already exist for ConfigName=" + entityConfig.getName()
								+ "--tree=" + tenantId + "/" + siteId + "/" + featureGroup + "/" + feature + "/"
								+ implName);
			}
			if (!isConfigEnabled)
				return;
			LeapDataServiceConfigurationUnit configurationUnit = new LeapDataServiceConfigurationUnit(tenantId, siteId,
					configNodeId, isConfigEnabled, entityConfig);
			configurationUnit.setDbconfigId(confignodeDataId);
			loadConfigurationInDataGrid(configurationUnit);
		} catch (InvalidNodeTreeException | ConfigPersistenceException | LeapEntityConfigParserException e) {
			logger.error("{} Failed to insert EntityConfiguration in DB for configName= {}", LEAP_LOG_KEY,
					entityConfig.getName(), e);
			throw new LeapDataServiceConfigurationException(
					"Failed to insert EntityConfiguration in DB for configName= " + entityConfig.getName(), e);
		} catch (Exception e) {
			logger.error("{} unable to add entity configuration : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to add entity configuration :" + e.getMessage(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public boolean checkEntityConfigarationExistOrNot(ConfigurationContext configurationContext, String entityName)
			throws LeapDataServiceConfigurationException {
		String methodName = "checkEntityConfigarationExistOrNot";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean isEnabled = false;
		LeapDataServiceConfigurationUnit configurationUnit = null;
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		try {
			// to Get NodeId of Feature
			int featureNodeId = getApplicableNodeId(requestContext);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
					entityName, LeapDataServiceConstant.CONFIGURATION_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return isEnabled;
			}

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				try {
					configurationUnit = getEntityConfiguration(requestContext, entityName);

					if (configurationUnit == null) {
						enableAndLoadEntityConfiguration(requestContext, configNodeData);
					}

				} catch (ConfigPersistenceException | LeapEntityConfigParserException e) {
					logger.error(
							"{} Error in Loading the PermastoreConfig to cache with configName = {}, Request Context = {}",
							LEAP_LOG_KEY, entityName, requestContext, e);
					throw new LeapDataServiceConfigurationException(
							"Error in Loading the PermastoreConfig to cache with configName = " + entityName
									+ " Request Context = " + requestContext,
							e);
				}
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			logger.error("{} Error in searching EntityCongaration with configName = {}, Request Context = {} ",
					LEAP_LOG_KEY, entityName, requestContext, e);
			throw new LeapDataServiceConfigurationException("Error in searching EntityCongaration with configName = "
					+ entityName + " Request Context = " + requestContext, e);
		} catch (Exception e) {
			logger.error("{} unable to check entity configuration : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to check entity configuration :" + e.getMessage(),
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	private void loadConfigurationInDataGrid(LeapDataServiceConfigurationUnit leapDataServiceConfigurationUnit)
			throws LeapDataServiceConfigurationException {
		String methodName = "loadConfigurationInDataGrid";
		logger.debug("{} entered into the method {}, LeapDataServiceConfigurationUnit={}", LEAP_LOG_KEY, methodName,
				leapDataServiceConfigurationUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.addConfiguration(leapDataServiceConfigurationUnit);
		} catch (ConfigServerInitializationException e) {
			throw new LeapDataServiceConfigurationException(
					"Failed to Upload in DataGrid configName=" + leapDataServiceConfigurationUnit.getKey(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public LeapDataServiceConfigurationUnit getEntityConfiguration(RequestContext requestContext, String entityName)
			throws LeapDataServiceConfigurationException {
		String methodName = "getEntityConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String vendorName = requestContext.getVendor();
		String version = requestContext.getVersion();
		try {
			Integer nodeId = 0;
			if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
					&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
				nodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
			} else {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			logger.debug("{} reqContext= {} ,configName= {}", LEAP_LOG_KEY, requestContext, entityName);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String psGroupKey = LeapDataServiceConfigurationUnit.getConfigGroupKey(nodeId);
			logger.debug("{} entityGroupKey= {}", LEAP_LOG_KEY, psGroupKey);
			LeapDataServiceConfigurationUnit leapDataServiceConfigurationUnit = (LeapDataServiceConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId(), psGroupKey, entityName);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (leapDataServiceConfigurationUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
				leapDataServiceConfigurationUnit = (LeapDataServiceConfigurationUnit) configServer
						.getConfiguration(requestContext.getTenantId(), psGroupKey, entityName);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return leapDataServiceConfigurationUnit;
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			e.printStackTrace();
			logger.error("{} Failed to get the EntityConfiguration for {}", LEAP_LOG_KEY, entityName, e);
			throw new LeapDataServiceConfigurationException("Failed to get the EntityConfiguration for " + entityName,
					e);
		} catch (Exception e) {
			logger.error("{} unable to get entity configuration : {}", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to get entity configuration :" + e.getMessage(), e);
		}

	}

	/*
	 * To Entity Configuration in Cache
	 */
	private void enableAndLoadEntityConfiguration(RequestContext requestContext, ConfigNodeData configNodeData)
			throws ConfigPersistenceException, LeapEntityConfigParserException, LeapDataServiceConfigurationException {
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		// Update Enable in the Database
		configPersistenceService.enableConfigNodeData(true, configNodeData.getNodeDataId());
		String entityConfig = configNodeData.getConfigData();
		LeapEntityConfigXMLParser configXMLParser = new LeapEntityConfigXMLParser();
		Entity entity = configXMLParser.marshallEntityConfigXMLtoObject(entityConfig);
		logger.trace("{} configNodeData :: {}", LEAP_LOG_KEY, configNodeData.getNodeDataId());
		LeapDataServiceConfigurationUnit configurationUnit = new LeapDataServiceConfigurationUnit(
				requestContext.getTenantId(), requestContext.getSiteId(), configNodeData.getParentConfigNodeId(),
				configNodeData.isEnabled(), entity);
		configurationUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(configurationUnit);
	}

	@Override
	public int updateEntityConfiguration(ConfigurationContext configurationContext,
			LeapDataServiceConfigurationUnit configurationUnit, int configNodeDataId)
			throws LeapDataServiceConfigurationException {
		String methodName = "updateEntityConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Entity entity = configurationUnit.getEntity();
		String EntityName = entity.getName();
		logger.trace("{} Entity to be updated :: {} ", LEAP_LOG_KEY, EntityName);
		String tenantId = configurationContext.getTenantId();
		String siteId = configurationContext.getSiteId();
		String vendorName = configurationContext.getVendorName();
		String version = configurationContext.getVersion();
		int sucess = 0;
		try {
			Integer configNodeId = 0;
			// Check and get ConfigNodeId for this
			if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
					&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, configurationContext.getFeatureGroup(),
						configurationContext.getFeatureName(), configurationContext.getImplementationName(), vendorName,
						version);
			} else {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, configurationContext.getFeatureGroup(),
						configurationContext.getFeatureName(), configurationContext.getImplementationName());
			}
			LeapEntityConfigXMLParser configXMLParser = new LeapEntityConfigXMLParser();
			String entityConfigAsString = configXMLParser.unmarshallEntityObjecttoXML(entity);
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(EntityName);
			// Enable or Disable should come from config need to add it in EntityConfig
			configNodeData.setEnabled(entity.isIsEnable());
			boolean isConfigEnabled = entity.isIsEnable();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(LeapDataServiceConstant.CONFIGURATION_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(entityConfigAsString);
			configNodeData.setNodeDataId(configNodeDataId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			sucess = configPersistenceService.updateConfigNodeData(configNodeData);
			try {
				changeStatusOfEntityConfiguration(configurationContext, EntityName, isConfigEnabled);
			} catch (LeapDataServiceConfigurationException e) {
				logger.error("{} error when changing status  of Entityconfigration {}", LEAP_LOG_KEY, e);
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException | LeapEntityConfigParserException e) {
			logger.error("{} Failed to update Entity configuration with name {}", LEAP_LOG_KEY, EntityName, e);
			throw new LeapDataServiceConfigurationException(
					"Failed to update Entity configuration with name " + EntityName, e);
		} catch (Exception e) {
			logger.error("{} unable to update entity configuration : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to update entity configuration :" + e.getMessage(),
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return sucess;
	}

	@Override
	public void changeStatusOfEntityConfiguration(ConfigurationContext configContext, String configName,
			boolean isEnable) throws LeapDataServiceConfigurationException {
		String methodName = "changeStatusOfEntityConfiguration";
		logger.debug("{} entered into the method {}, configName={} ", LEAP_LOG_KEY, methodName, configName);
		RequestContext requestContext = new RequestContext(configContext.getTenantId(), configContext.getSiteId(),
				configContext.getFeatureGroup(), configContext.getFeatureName(), configContext.getImplementationName(),
				configContext.getVendorName(), configContext.getVersion());
		try {

			Integer applicableNodeId = getApplicableNodeId(requestContext);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					configName, LeapDataServiceConstant.CONFIGURATION_TYPE);
			if (configNodeData == null) {
				// Not in DB so it does not exist throw exception
				throw new LeapDataServiceConfigurationException(
						"EntityConfig with Name( " + configName + ") does not exist in DB");
			}
			logger.debug("{} configNodeData :: {}", LEAP_LOG_KEY, configNodeData.getConfigData().toString());
			// Disable Request
			if (!isEnable) {
				logger.trace("{} Entity status is enabled , disabling it again", LEAP_LOG_KEY);

				// We have to Disable psConfig hence remove from DataGrid and
				// update DB as disabled Configuration
				configPersistenceService.enableConfigNodeData(false, configNodeData.getNodeDataId());

				// Now remove from DataGrid
				String psGroupKey = LeapDataServiceConfigurationUnit
						.getConfigGroupKey(configNodeData.getParentConfigNodeId());
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				configServer.deleteConfiguration(requestContext.getTenantId(), psGroupKey, configName);

			} else {
				logger.trace("{} Entity status is disabled , enabling it again", LEAP_LOG_KEY);
				// Enable Request-Load Config from DataBase and update the
				// DataGrid
				enableAndLoadEntityConfiguration(requestContext, configNodeData);
			}

		} catch (ConfigPersistenceException | LeapEntityConfigParserException | InvalidNodeTreeException
				| ConfigServerInitializationException e) {
			logger.error("{} Failed to Enable/Disable Entity with name {}", LEAP_LOG_KEY, configName, e);
			throw new LeapDataServiceConfigurationException("Failed to Enable/Disable Entity with name " + configName,
					e);
		} catch (Exception e) {
			logger.error("{} unable to change entity configuration status : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException(
					"unable to change entity configuration status :" + e.getMessage(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public boolean deleteEntityConfiguration(ConfigurationContext configurationContext, String configName)
			throws LeapDataServiceConfigurationException {
		String methodName = "deleteEntityConfiguration";
		logger.debug("{} entered into the method {}, ConfigName={} ", LEAP_LOG_KEY, methodName, configName);
		boolean isdeleted = false;
		RequestContext reqContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		LeapDataServiceConfigurationUnit configurationUnit = getEntityConfiguration(reqContext, configName);
		try {
			if (configurationUnit == null) {
				logger.warn("{} deleting NonCache EntityConfiguration from DataBase", LEAP_LOG_KEY);

				Integer configNodeId = getApplicableNodeId(reqContext);
				isdeleted = deleteEntityConfigurationFromDataBase(configName, configNodeId);
				return isdeleted;
			}
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			isdeleted = configPersistenceService.deleteConfigNodeData(configurationUnit.getDbconfigId());
			String entityGroupKey = LeapDataServiceConfigurationUnit
					.getConfigGroupKey(configurationUnit.getAttachedNodeId());
			logger.debug("{} deleted from db NodeDataId= {}", LEAP_LOG_KEY, configurationUnit.getDbconfigId());

			// Now remove from DataGrid
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(configurationUnit.getTenantId(), entityGroupKey, configName);
			logger.debug("{} deleted from DataGrid entityGroupKey= {}, configName={} ", LEAP_LOG_KEY, entityGroupKey,
					configName);

		} catch (InvalidNodeTreeException | ConfigPersistenceException | ConfigServerInitializationException e) {
			logger.error("{} Persistance exception deleting the node cause: {}", LEAP_LOG_KEY, e);
			throw new LeapDataServiceConfigurationException("Failed to Delete EntityConfig with name " + configName, e);
		} catch (Exception e) {
			logger.error("{} unable to delete entity configuration :{}", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to delete entity configuration :" + e.getMessage(),
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isdeleted;
	}

	/*
	 * to delete Entity Configuration From DB
	 */
	private boolean deleteEntityConfigurationFromDataBase(String configName, int nodeId)
			throws LeapDataServiceConfigurationException {
		String methodName = "deleteEntityConfigurationFromDataBase";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean isDeleted = false;
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, nodeId);
			isDeleted = true;
		} catch (ConfigPersistenceException e) {
			logger.error("{} Persistance exception deleting the node cause: {}", LEAP_LOG_KEY, e);
			throw new LeapDataServiceConfigurationException("Persistance exception deleting the node cause: " + e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return isDeleted;
	}

	@Override
	public Entity getEntityCacheObject(RequestContext context, String entityName)
			throws LeapDataServiceConfigurationException {
		logger.debug("inside getEntityCacheObject for entity: " + entityName);
		LeapDataServiceConfigurationUnit configurationUnit = getEntityConfiguration(context, entityName);
		if (configurationUnit != null) {
			return configurationUnit.getEntity();
		}
		return null;
	}

	@Override
	public List<Entity> getAllEntityCacheObject(RequestContext requestContext)
			throws LeapDataServiceConfigurationException {
		String methodName = "getAllEntityCacheObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<Entity> entities = new ArrayList<>();
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		String vendorName = requestContext.getVendor();
		String version = requestContext.getVersion();
		try {
			Integer nodeId = 0;
			if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
					&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
				nodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
			} else {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			logger.debug("{} nodeId :: {}", LEAP_LOG_KEY, nodeId);
			List<ConfigNodeData> ListOfConfigNode = configPersistenceService.getConfigNodeDataByNodeId(nodeId);
			for (ConfigNodeData configNodeData : ListOfConfigNode) {
				if (configNodeData.getConfigType().equalsIgnoreCase(LeapDataServiceConstant.CONFIGURATION_TYPE)
						&& configNodeData.isEnabled()) {
					logger.trace("{} configNodeData name {}", LEAP_LOG_KEY, configNodeData.getConfigName());
					LeapDataServiceConfigurationUnit configurationUnit = getEntityConfiguration(requestContext,
							configNodeData.getConfigName());
					if (configurationUnit != null) {
						Entity entity = configurationUnit.getEntity();
						entities.add(entity);
						logger.trace("{} added Entity {}", LEAP_LOG_KEY, entity.getName());
					}
				}
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			logger.error("{} Failed to get the AllEntityConfigurations : {} ", LEAP_LOG_KEY, e);
			throw new LeapDataServiceConfigurationException("Failed to get the AllEntityConfigurations :" + e);
		} catch (Exception e) {
			logger.error("{} unable to get  entities  : {} ", LEAP_LOG_KEY, e.getMessage(), e);
			throw new LeapDataServiceConfigurationException("unable to get entities configuration :" + e.getMessage(),
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entities;
	}

	/**
	 * method to addEntityAccess to an entity
	 */
	@Override
	public void addEntityAccess(RequestContext requestContext, String entityName, String configType,
			EntityAccess entityAccess) throws LeapDataServiceConfigurationException {
		String methodName = "addEntityAccess";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext != null && entityName != null && entityAccess != null && configType != null) {
			boolean isAdded = false;
			LeapDataServiceConfigurationUnit entityConfiguration = getEntityConfiguration(requestContext, entityName);
			Entity entity = entityConfiguration.getEntity();
			List<EntityAccessConfig> entityAccessConfigList = entity.getEntityAccessConfig();
			for (EntityAccessConfig entityAccessConfig : entityAccessConfigList) {
				if (entityAccessConfig.getConfigType().equalsIgnoreCase(configType)) {
					List<EntityAccess> entityAccesses = entityAccessConfig.getEntityAccess();
					entityAccesses.add(entityAccess);
					updateEntityConfiguration(requestContext.getConfigurationContext(), entityConfiguration,
							entityConfiguration.getDbconfigId());
					isAdded = true;
					break;
				}
			}
			if (isAdded)
				throw new LeapDataServiceConfigurationException(
						" configType " + configType + " for entity  " + entityName + " doesn't exits");
		} else {
			throw new LeapDataServiceConfigurationException("Unable to add entityAccess ,invalid method parameter ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * method to remove the entityAccess from Entity
	 */
	@Override
	public void removeEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource) throws LeapDataServiceConfigurationException {
		String methodName = "removeEntityAccess";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext != null && entityName != null && authorizedResource != null && configType != null) {
			LeapDataServiceConfigurationUnit entityConfiguration = getEntityConfiguration(requestContext, entityName);
			Entity entity = entityConfiguration.getEntity();
			List<EntityAccessConfig> accessConfigs = entity.getEntityAccessConfig();
			boolean exist = false;
			for (EntityAccessConfig accessConfig : accessConfigs) {
				if (accessConfig != null)
					if (accessConfig.getConfigType().equalsIgnoreCase(configType)) {
						List<EntityAccess> entityAccess = accessConfig.getEntityAccess();
						ListIterator<EntityAccess> listIterator = entityAccess.listIterator();
						while (listIterator.hasNext()) {
							EntityAccess next = listIterator.next();
							if (next.getAuthorizedResource().equalsIgnoreCase(authorizedResource)) {
								listIterator.remove();
								exist = true;
								break;
							}
						}
						break;
					}
			}
			if (exist)
				updateEntityConfiguration(requestContext.getConfigurationContext(), entityConfiguration,
						entityConfiguration.getDbconfigId());
			else
				throw new LeapDataServiceConfigurationException("entityAccess for authorizedResource "
						+ authorizedResource + " doesn't exits for entity " + entityName);

		} else {
			throw new LeapDataServiceConfigurationException("Unable to remove entityAccess ,invalid method parameter ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * update a specific entityAccess in the Entity.
	 */
	@Override
	public void updateEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource, EntityAccess entityAccess) throws LeapDataServiceConfigurationException {
		String methodName = "updateEntityAccess";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext != null && entityName != null && authorizedResource != null && configType != null
				&& entityAccess != null) {
			LeapDataServiceConfigurationUnit entityConfiguration = getEntityConfiguration(requestContext, entityName);
			Entity entity = entityConfiguration.getEntity();
			List<EntityAccessConfig> entityAccessConfigs = entity.getEntityAccessConfig();
			boolean exist = false;
			for (EntityAccessConfig entityAccessConfig : entityAccessConfigs) {
				if (entityAccessConfig.getConfigType().equalsIgnoreCase(configType)) {
					List<EntityAccess> entityAccessList = entityAccessConfig.getEntityAccess();
					logger.trace("{} entityAccessList is : {}", LEAP_LOG_KEY, entityAccessList);
					ListIterator<EntityAccess> listIterator = entityAccessList.listIterator();
					while (listIterator.hasNext()) {
						EntityAccess next = listIterator.next();
						logger.trace("{} next.getAuthorizedResource() :{} ", LEAP_LOG_KEY,
								next.getAuthorizedResource());
						if (next.getAuthorizedResource().equalsIgnoreCase(authorizedResource)) {
							listIterator.set(entityAccess);
							exist = true;
							break;
						}
					}
					break;
				}
				if (exist)
					updateEntityConfiguration(requestContext.getConfigurationContext(), entityConfiguration,
							entityConfiguration.getDbconfigId());
				else
					throw new LeapDataServiceConfigurationException("entityAccess for authorizedResource "
							+ authorizedResource + " doesn't exits for entity " + entityName);
			}
		} else {
			throw new LeapDataServiceConfigurationException("Unable to update entityAccess ,invalid method parameter ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * get a particular EntityAccess from an Entity based on authorizedResource.
	 */
	@Override
	public EntityAccess getEntityAccess(RequestContext requestContext, String entityName, String configType,
			String authorizedResource) throws LeapDataServiceConfigurationException {
		EntityAccess responseEntityAcces = null;
		String methodName = "getEntityAccess";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext != null && entityName != null && authorizedResource != null && configType != null) {
			LeapDataServiceConfigurationUnit entityConfiguration = getEntityConfiguration(requestContext, entityName);
			Entity entity = entityConfiguration.getEntity();
			List<EntityAccessConfig> entityAccessConfigs = entity.getEntityAccessConfig();
			for (EntityAccessConfig entityAccessConfig : entityAccessConfigs) {
				if (entityAccessConfig.getConfigType().equalsIgnoreCase(configType)) {
					List<EntityAccess> entityAccessList = entityAccessConfig.getEntityAccess();
					ListIterator<EntityAccess> listIterator = entityAccessList.listIterator();
					while (listIterator.hasNext()) {
						EntityAccess next = listIterator.next();
						if (next.getAuthorizedResource().equalsIgnoreCase(authorizedResource)) {
							responseEntityAcces = next;
							break;
						}
					}
					break;
				}
			}
		} else {
			throw new LeapDataServiceConfigurationException("Unable to get entityAccess ,invalid method parameter ");
		}
		if (responseEntityAcces == null)
			throw new LeapDataServiceConfigurationException(" entityAccess for authorizedResource " + authorizedResource
					+ " doesn't exits for entity " + entityName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return responseEntityAcces;
	}

	/**
	 * get All EntityAccess for a particular entityName
	 */
	@Override
	public List<EntityAccess> getAllEntityAccess(RequestContext requestContext, String entityName, String configType)
			throws LeapDataServiceConfigurationException {
		List<EntityAccess> entityAccessList = null;
		String methodName = "getAllEntityAccess";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext != null && entityName != null && configType != null) {
			LeapDataServiceConfigurationUnit entityConfiguration = getEntityConfiguration(requestContext, entityName);
			Entity entity = entityConfiguration.getEntity();
			List<EntityAccessConfig> entityAccessConfigs = entity.getEntityAccessConfig();
			for (EntityAccessConfig entityAccessConfig : entityAccessConfigs) {
				if (entityAccessConfig.getConfigType().equalsIgnoreCase(configType)) {
					entityAccessList = entityAccessConfig.getEntityAccess();
					break;
				}
			}
		} else {
			throw new LeapDataServiceConfigurationException(
					"Unable to get All entityAccess ,invalid method parameter ");
		}
		if (entityAccessList == null)
			throw new LeapDataServiceConfigurationException(
					"configType " + configType + " doesn't exits for entity " + entityName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entityAccessList;
	}
}
