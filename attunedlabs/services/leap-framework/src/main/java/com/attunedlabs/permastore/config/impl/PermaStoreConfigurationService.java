package com.attunedlabs.permastore.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;

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
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigParserException;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationBuilderException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationConstant;
import com.attunedlabs.permastore.config.PermaStoreConfigurationException;
import com.attunedlabs.permastore.config.PermaStoreConfigurationUnit;
import com.attunedlabs.permastore.config.jaxb.FeatureInfo;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfiguration;
import com.attunedlabs.permastore.config.jaxb.PermaStoreConfigurations;

public class PermaStoreConfigurationService extends GenericApplicableNode implements IPermaStoreConfigurationService {
	final Logger logger = LoggerFactory.getLogger(IPermaStoreConfigurationService.class);
	private PermaStoreConfigBuilderHelper configBuilderHelper;

	/**
	 * Adds PermastoreConfiguration to the cache and updates database for management
	 * purpose.
	 * 
	 * @param tenantId
	 * @param psConfig
	 * @throws PermaStoreConfigurationBuilderException ,InvalidNodeTreeException
	 */
	public void addPermaStoreConfiguration(ConfigurationContext configurationContext, PermaStoreConfiguration psConfig)
			throws PermaStoreConfigurationException {
		String methodName = "addPermaStoreConfiguration";
		logger.debug("{} entered into the method {}, PermaStoreConfiguration={} ", LEAP_LOG_KEY, methodName, psConfig);
		String tenantId = configurationContext.getTenantId();
		String siteId = configurationContext.getSiteId();
		String vendorName = configurationContext.getVendorName();
		String version = configurationContext.getVersion();
		String featureGroup = configurationContext.getFeatureGroup();
		String feature = configurationContext.getFeatureName();
		String implName = configurationContext.getImplementationName();
		try {
			Integer configNodeId = 0;
			// Check and get ConfigNodeId for this
			if ((vendorName != null && !(vendorName.isEmpty()) && !(vendorName.equalsIgnoreCase("")))
					&& (version != null && !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, configurationContext.getFeatureGroup(),
						configurationContext.getFeatureName(), implName, vendorName, version);
			} else {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, configurationContext.getFeatureGroup(),
						configurationContext.getFeatureName(), implName);
			}
			logger.debug("{} Applicable Config Node Id is = {}", LEAP_LOG_KEY, configNodeId);

			// Get the type of Configuration Bulder and get the DatatoCache from
			// the
			// Builder
			if (configBuilderHelper == null) {
				logger.trace("{} configBuilderHelper object creation", LEAP_LOG_KEY);
				configBuilderHelper = new PermaStoreConfigBuilderHelper();
			}
			logger.debug("{} configBuilderHelper : {}", LEAP_LOG_KEY, configBuilderHelper);
			Serializable objToCache = configBuilderHelper
					.handleConfigurationBuilder(psConfig.getConfigurationBuilder());

			// Convert configTo Valid XML to store independent inDataBase
			PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
			String xmlString = builder.unmarshallObjecttoXML(psConfig);

			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(psConfig.getName());
			configNodeData.setEnabled(psConfig.isIsEnabled());
			boolean isConfigEnabled = psConfig.isIsEnabled();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, psConfig.getName(), PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {

				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);

			} else {
				FeatureInfo featureInfo = psConfig.getFeatureInfo();
				throw new PermaStoreConfigurationException("PermaStoreConfiguration already exist for ConfigName="
						+ psConfig.getName() + "--tree=" + tenantId + "/" + siteId + "/" + featureInfo.getFeatureGroup()
						+ "/" + featureInfo.getFeatureName() + "/" + implName);
			}

			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;

			PermaStoreConfigurationUnit psConfigUnit = new PermaStoreConfigurationUnit(tenantId, siteId, configNodeId,
					isConfigEnabled, psConfig, objToCache);
			psConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(psConfigUnit);
		} catch (ConfigPersistenceException | PermaStoreConfigParserException sqlExp) {
			throw new PermaStoreConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + psConfig.getName(), sqlExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * update Permastoreconfigaration based on the request
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param PermaStoreConfiguration
	 * @param configNodedataId
	 * @throws PermaStoreConfigParserException
	 */
	public int updatePermaStoreConfiguration(ConfigurationContext configurationContext,
			PermaStoreConfiguration psConfig, int configNodedataId)
			throws PermaStoreConfigurationException, PermaStoreConfigParserException {
		int sucess = 0;
		String methodName = "updatePermaStoreConfiguration";
		logger.debug("{} entered into the method {},PermaStoreConfiguration={} ", LEAP_LOG_KEY, methodName, psConfig);
		String tenantId = configurationContext.getTenantId();
		String siteId = configurationContext.getSiteId();
		String vendorName = configurationContext.getVendorName();
		String version = configurationContext.getVersion();
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
			logger.debug("{} Applicable Config Node Id is = {}", LEAP_LOG_KEY, configNodeId);

			// Convert configTo Valid XML to store independent inDataBase
			PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
			String xmlString = builder.unmarshallObjecttoXML(psConfig);

			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(psConfig.getName());
			// #TODO Enable or Disable should come from config need to add it in
			// XMLSchema
			configNodeData.setEnabled(psConfig.isIsEnabled());
			boolean isConfigEnabled = psConfig.isIsEnabled();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			configNodeData.setNodeDataId(configNodedataId);
			RequestContext permaStoreRequestContext = null;
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			logger.debug("{} before updating the permastore configNodeData {}, configId {} ", LEAP_LOG_KEY,
					configNodeData, configNodedataId);
			sucess = configPersistenceService.updateConfigNodeData(configNodeData);
			try {
				if (psConfig.getFeatureInfo() != null && psConfig.getFeatureInfo().getFeatureName() != null) {
					permaStoreRequestContext = new RequestContext(tenantId, siteId,
							psConfig.getFeatureInfo().getFeatureGroup(), psConfig.getFeatureInfo().getFeatureName(),
							configurationContext.getImplementationName());
					changeStatusOfPermaStoreConfig(configurationContext, psConfig.getName(), isConfigEnabled);
				} else {
					permaStoreRequestContext = new RequestContext(tenantId, siteId,
							psConfig.getFeatureInfo().getFeatureGroup(), null, null);
					changeStatusOfPermaStoreConfig(configurationContext, psConfig.getName(), isConfigEnabled);

				}
			} catch (Exception e) {
				logger.error("{} error when changing status  of permastoreconfigration {} ", LEAP_LOG_KEY, e);
			}

		} catch (ConfigPersistenceException sqlExp) {
			throw new PermaStoreConfigurationException(
					"Failed to update ConfigData in DB for configName=" + psConfig.getName(), sqlExp);
		}

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return sucess;
	}

	/**
	 * Gets PermastoreConfiguration based on the request
	 */
	public PermaStoreConfigurationUnit getPermaStoreConfiguration(RequestContext requestContext, String configName)
			throws PermaStoreConfigRequestException {
		// check if tenet/site/featuregroup are not null and not blank
		String methodName = "getPermaStoreConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		isPermastoreRequestValid(requestContext);
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
			logger.trace("{} reqContext= {},configName= {}", LEAP_LOG_KEY, requestContext, configName);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String psGroupKey = PermaStoreConfigurationUnit.getConfigGroupKey(nodeId);
			logger.trace("{} psGroupKey= {} ", LEAP_LOG_KEY, psGroupKey);
			PermaStoreConfigurationUnit permaStoreConfigUnit = (PermaStoreConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId(), psGroupKey, configName);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (permaStoreConfigUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
				permaStoreConfigUnit = (PermaStoreConfigurationUnit) configServer
						.getConfiguration(requestContext.getTenantId(), psGroupKey, configName);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return permaStoreConfigUnit;
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new PermaStoreConfigRequestException(e);
		}
	}

	/**
	 * Gets PermastoreCached Object based on the requested permastoreConfig
	 */
	public Serializable getPermaStoreCachedObject(RequestContext requestContext, String configName)
			throws PermaStoreConfigRequestException {
		String methodName = "getPermaStoreCachedObject";
		logger.debug("{} entered into the method {}, reqContext= {}, configName={} ", LEAP_LOG_KEY, methodName,
				requestContext, configName);
		PermaStoreConfigurationUnit psconfigUnit = getPermaStoreConfiguration(requestContext, configName);

		if (psconfigUnit != null) {
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return psconfigUnit.getConfigData();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;
	}

	/**
	 * Re-loads the cacahed object in the permastore from the configured source.
	 */
	public boolean reloadPerStoreCacheObject(RequestContext requestContext, String configName)
			throws PermaStoreConfigurationException {
		String methodName = "reloadPerStoreCacheObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext == null && configName == null)
			throw new PermaStoreConfigurationException("requestContext and configName both should not be null");
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		PermaStoreConfigurationUnit configUnit;
		try {
			configUnit = getPermaStoreConfiguration(requestContext, configName);
			if (configUnit == null) {
				Integer applicableNodeId = getApplicableNodeId(requestContext);
				ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
						applicableNodeId, configName, PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
				if (configNodeData == null)
					return false;
				String psconfigStr = configNodeData.getConfigData();

				PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
				PermaStoreConfigurations psConfigs = builder.marshallXMLtoObject(psconfigStr);

				// As it is loaded from DB I know there will always be one
				// config
				// only
				PermaStoreConfiguration psConfig = psConfigs.getPermaStoreConfiguration().get(0);
				if (configBuilderHelper == null)
					configBuilderHelper = new PermaStoreConfigBuilderHelper();
				Serializable objToCache = configBuilderHelper
						.handleConfigurationBuilder(psConfig.getConfigurationBuilder());
				configUnit = new PermaStoreConfigurationUnit(requestContext.getTenantId(), requestContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), true, psConfig, objToCache);
				configUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(configUnit);
				return true;
			} else {

				return true;
			}
		} catch (PermaStoreConfigRequestException e) {
			logger.error(
					"{} Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name= {} ",
					LEAP_LOG_KEY, configName, e);
			throw new PermaStoreConfigurationException(
					"Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name="
							+ configName,
					e);
		} catch (ConfigPersistenceException e) {
			logger.error("{} Failed to reLoad Config from DB with Name= {}", LEAP_LOG_KEY, configName, e);
			throw new PermaStoreConfigurationException("Failed to reLoad Config from DB with Name=" + configName, e);
		} catch (PermaStoreConfigParserException e) {
			logger.error("{} Failed to xml-parse Config from DB with Name= {} ", LEAP_LOG_KEY, configName, e);
			throw new PermaStoreConfigurationException("Failed to xml-parse Config from DB with Name=" + configName, e);
		}

	}

	/**
	 * Finds the Respective PermaStoreConfiguration and Deletes it from the DataGrid
	 * as well as from the DataBase<BR>
	 * Note:-To purge/remove only from DataGrid use
	 * .changeStatusOfPermaStoreConfig() marking status as disabled.
	 * 
	 * @param requestContext
	 * @param configName
	 * @return
	 * @throws PermaStoreConfigRequestException
	 */
	public boolean deletePermaStoreConfiguration(ConfigurationContext configurationContext, String configName)
			throws PermaStoreConfigurationException {
		String methodName = "deletePermaStoreConfiguration";
		logger.debug("{} entered into the method {},configuartionContext={}, configName={}", LEAP_LOG_KEY, methodName,
				configurationContext, configName);
		RequestContext reqContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		try {
			// First get the configuration from the dataGrid so that we can get
			// the NodeDataId
			PermaStoreConfigurationUnit psconfigUnit = getPermaStoreConfiguration(reqContext, configName);

			if (psconfigUnit == null) {
				logger.warn("{} Delete request for Non Cache PermastoreConfig= {} ", LEAP_LOG_KEY, configName);
				// delete from DB
				Integer configNodeId = getApplicableNodeId(reqContext);
				return deletePermaStoreConfigurationFromDb(configName, configNodeId);
			}

			// Delete from the DB First so that configVerifier should not
			// revitalise the config in dataGrid
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			boolean isDeleted = configPersistenceService.deleteConfigNodeData(psconfigUnit.getDbconfigId());
			String psGroupKey = PermaStoreConfigurationUnit.getConfigGroupKey(psconfigUnit.getAttachedNodeId());
			logger.info("{} deleted from db NodeDataId= {}", LEAP_LOG_KEY, psconfigUnit.getDbconfigId());

			// Now remove from DataGrid
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(psconfigUnit.getTenantId(), psGroupKey, configName);
			logger.info("deleted from DataGrid psGroupKey= {}, configName={} ", LEAP_LOG_KEY, psGroupKey, configName);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return true;
		} catch (ConfigPersistenceException | PermaStoreConfigRequestException
				| ConfigServerInitializationException e) {
			throw new PermaStoreConfigurationException("Failed to Delete PermaStoreConfig with name " + configName, e);
		}
	}

	/**
	 * delete the Permastoreconfigaration by configName and NodeId
	 * 
	 * @param configName
	 * @param nodeId
	 * @return boolean
	 * @throws PermaStoreConfigurationException
	 */
	private boolean deletePermaStoreConfigurationFromDb(String configName, int nodeId)
			throws PermaStoreConfigurationException {
		// Delete from the DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		logger.debug("{}  deleted from db configName= {} " ,LEAP_LOG_KEY, configName);
		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, nodeId);
		} catch (ConfigPersistenceException e) {
			logger.error("{} Persistance exception deleting the node cause: {} " ,LEAP_LOG_KEY, e);
			throw new PermaStoreConfigurationException("Persistance exception deleting the node cause: " + e);
		}
		// Now remove from DataGrid
		return true;
	}

	/**
	 * 
	 * @param requestContext
	 * @param configName
	 * @param isEnable
	 * @throws PermaStoreConfigurationException
	 */

	public void changeStatusOfPermaStoreConfig(ConfigurationContext configContext, String configName, boolean isEnable)
			throws PermaStoreConfigurationException {
		String methodName = "changeStatusOfPermaStoreConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		RequestContext requestContext = new RequestContext(configContext.getTenantId(), configContext.getSiteId(),
				configContext.getFeatureGroup(), configContext.getFeatureName(), configContext.getImplementationName(),
				configContext.getVendorName(), configContext.getVersion());
		try {

			Integer applicableNodeId = getApplicableNodeId(requestContext);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					configName, PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);
			if (configNodeData == null) {
				// Not in DB so it does not exist throw exception
				throw new PermaStoreConfigurationException(
						"PermaStoreConfig with Name( " + configName + ") does not exist in DB");
			}

			// Disable Request
			if (!isEnable) {
				logger.trace("{} permastore status is enabled , disabling it again",LEAP_LOG_KEY);

				// We have to Disable psConfig hence remove from DataGrid and
				// update DB as disabled Configuration
				configPersistenceService.enableConfigNodeData(false, configNodeData.getNodeDataId());

				// Now remove from DataGrid
				String psGroupKey = PermaStoreConfigurationUnit
						.getConfigGroupKey(configNodeData.getParentConfigNodeId());
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				configServer.deleteConfiguration(requestContext.getTenantId(), psGroupKey, configName);

			} else {
				logger.trace("{} permastore status is disabled , enabling it again",LEAP_LOG_KEY);
				// Enable Request-Load Config from DataBase and update the
				// DataGrid
				enableAndLoadPermaStroreConfig(requestContext, configNodeData);
			}

		} catch (ConfigPersistenceException | ConfigServerInitializationException | PermaStoreConfigParserException e) {
			throw new PermaStoreConfigurationException(
					"Failed to Enable/Disable PermaStoreConfig with name " + configName, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void verifyPermaStoreConfigLoaded(RequestContext requestContext, String configName)
			throws PermaStoreConfigRequestException {
		// #TODO Code
	}

	private void enableAndLoadPermaStroreConfig(RequestContext reqCtx, ConfigNodeData configNodeData)
			throws ConfigPersistenceException, PermaStoreConfigParserException, PermaStoreConfigurationException {
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		// Update Enable in the Database
		configPersistenceService.enableConfigNodeData(true, configNodeData.getNodeDataId());
		// Get XML from DB and Load in the DataGrid
		String psconfigStr = configNodeData.getConfigData();
		PermaStoreConfigXMLParser builder = new PermaStoreConfigXMLParser();
		PermaStoreConfigurations psConfigs = builder.marshallXMLtoObject(psconfigStr);
		// As it is loaded from DB I know there will always be one config only
		PermaStoreConfiguration psConfig = psConfigs.getPermaStoreConfiguration().get(0);

		if (configBuilderHelper == null)
			configBuilderHelper = new PermaStoreConfigBuilderHelper();
		Serializable objToCache = configBuilderHelper.handleConfigurationBuilder(psConfig.getConfigurationBuilder());

		PermaStoreConfigurationUnit psConfigUnit = new PermaStoreConfigurationUnit(reqCtx.getTenantId(),
				reqCtx.getSiteId(), configNodeData.getParentConfigNodeId(), true, psConfig, objToCache);
		psConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(psConfigUnit);

	}

	private void loadConfigurationInDataGrid(PermaStoreConfigurationUnit psConfigUnit)
			throws PermaStoreConfigurationException {
		// PermaStoreConfigurationUnit psConfigUnit = new
		// PermaStoreConfigurationUnit(tenantId, siteId,configNodeId, true,
		// psConfig, objToCache);
		// psConfigUnit.setDbconfigId(configDataId);

		logger.debug("{} .loadConfigurationInDataGrid() PermaStoreConfigurationUnit= {} ",LEAP_LOG_KEY, psConfigUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			// I don't want to store the PermaStoreConfiguration in the DataGrid
			// Cache hence setting it as null
			psConfigUnit.setPermaStoreConfig(null);
			configServer.addConfiguration(psConfigUnit);
		} catch (ConfigServerInitializationException e) {
			throw new PermaStoreConfigurationException(
					"Failed to Upload in DataGrid configName=" + psConfigUnit.getKey(), e);
		}
	}

	private void isPermastoreRequestValid(RequestContext requestContext) throws PermaStoreConfigRequestException {
		if (requestContext == null || !requestContext.isValid()) {
			throw new PermaStoreConfigRequestException(
					"PermaStoreRequestContext is null or has required data as null or empty");
		}
	}

	/**
	 *
	 * based on the PermaStoreRequestContext and config name searching
	 * permastoreconfig ,if permastoreconfig Exist in DB check wether it enabled or
	 * not , if enabled check exist in cache or not , if not Exist load the data to
	 * cache and return true else false
	 * 
	 * @param requestContext
	 * @param configName
	 * @throws PermaStoreConfigRequestException
	 * @throws PermaStoreConfigurationException
	 */
	public boolean checkPermaStoreConfigarationExistOrNot(ConfigurationContext configurationContext, String configName)
			throws PermaStoreConfigRequestException, PermaStoreConfigurationException {
		String methodName = "checkPermaStoreConfigarationExistOrNot";
		logger.debug("{} entered into the method {},with configurationContext ={}, configName={}", LEAP_LOG_KEY, methodName,configurationContext,configName);
		boolean isEnabled = false;
		PermaStoreConfigurationUnit pUnit = null;
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());

		try {
			// to Get NodeId of Feature
			int featureNodeId = getApplicableNodeId(requestContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
					configName, PermaStoreConfigurationConstant.PERMASTORE_CONFIG_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null)
				return false;

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				try {
					pUnit = getPermaStoreConfiguration(requestContext, configName);

					if (pUnit == null) {
						enableAndLoadPermaStroreConfig(requestContext, configNodeData);
					}
				} catch (PermaStoreConfigParserException e) {
					throw new PermaStoreConfigurationException(
							"Error in Loading the PermastoreConfig to cache with configName = " + configName
									+ " Request Context = " + requestContext,
							e);
				}
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new PermaStoreConfigRequestException("Error in searching PermastoreCongaration with configName = "
					+ configName + " Request Context = " + requestContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}
}