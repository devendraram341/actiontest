package com.attunedlabs.resourcemanagement.config.impl;

import java.io.Serializable;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
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
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.resourcemanagement.config.IResourceManagementConfigService;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigParserException;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigurationException;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConfigurationUnit;
import com.attunedlabs.resourcemanagement.config.ResourceManagementConstant;
import com.attunedlabs.resourcemanagement.jaxb.ConnectionInfo;
import com.attunedlabs.resourcemanagement.jaxb.GetResourceContent;

public class ResourceManagementConfigService extends GenericApplicableNode implements IResourceManagementConfigService {
	final Logger logger = LoggerFactory.getLogger(ResourceManagementConfigService.class);

	@Override
	public void addGetResourceContentConfiguration(ConfigurationContext configContext,
			GetResourceContent getResourceContent) throws ResourceManagementConfigurationException {
		String methodName = "addGetResourceContentConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId = 0;
		String configKey = getResourceContent.getName();
		try {
			logger.debug("{} Config key = {} ", LEAP_LOG_KEY, configKey);
			configNodeId = getApplicableNodeId(configContext);
			logger.debug("{} Applicable Config Node Id is = {} ", LEAP_LOG_KEY, configNodeId);
			ResourceManagementConfigXmlParser parser = new ResourceManagementConfigXmlParser();
			String xmlString = parser.unmarshallObjecttoXML(getResourceContent);
			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(configKey);
			configNodeData.setEnabled(true);
			boolean isConfigEnabled = true;
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(ResourceManagementConstant.RESOURCE_MANAGEMENT_GET_RESOURCE_CONTENT);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, configKey, ResourceManagementConstant.RESOURCE_MANAGEMENT_GET_RESOURCE_CONTENT);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new ResourceManagementConfigurationException("ResouceMangement already exist for ConfigName="
						+ configKey + "--tree=" + configContext.getTenantId() + "/" + configContext.getSiteId() + "/"
						+ configContext.getFeatureGroup() + "/" + configContext.getFeatureName() + "/"
						+ configContext.getImplementationName() + "/" + configContext.getVendorName() + "/"
						+ configContext.getVersion());
			}
			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;
			ResourceManagementConfigurationUnit resourceManagementConfigUnit = new ResourceManagementConfigurationUnit(
					configContext.getTenantId(), configContext.getSiteId(), configNodeId, isConfigEnabled, configKey,
					getResourceContent);
			resourceManagementConfigUnit.setGroupId(configContext.getFeatureGroup());
			resourceManagementConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(resourceManagementConfigUnit);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | ResourceManagementConfigParserException e) {
			e.printStackTrace();
			throw new ResourceManagementConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + configKey, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to load configuration into data grid
	 * 
	 * @param featureConfigUnit : Configuration unit for feature
	 * @throws DataContextConfigurationException
	 */
	private void loadConfigurationInDataGrid(ResourceManagementConfigurationUnit resourceManagementConfigUnit)
			throws ResourceManagementConfigurationException {
		String methodName = "loadConfigurationInDataGrid";
		logger.debug("{} entered into the method {},ResourceManagementConfigService={}  ", LEAP_LOG_KEY, methodName,
				resourceManagementConfigUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.addConfiguration(resourceManagementConfigUnit);

		} catch (ConfigServerInitializationException e) {
			throw new ResourceManagementConfigurationException(
					"Failed to Upload in DataGrid configName=" + resourceManagementConfigUnit.getKey(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method loadConfigurationInDataGrid

	@Override
	public void addConnectionInfoConfiguration(ConfigurationContext configContext, ConnectionInfo connectionInfo)
			throws ResourceManagementConfigurationException {
		String methodName = "addConnectionInfoConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId = 0;
		String configKey = ResourceManagementConstant.RESOURCE_MANAGEMENT_CONNECTION_INFO_CONFIG_NAME;
		try {
			logger.debug("{} Config key = {}", LEAP_LOG_KEY, configKey);
			configNodeId = getApplicableNodeId(configContext);
			logger.debug("{} Applicable Config Node Id is = {} ", LEAP_LOG_KEY, configNodeId);
			ResourceManagementConfigXmlParser parser = new ResourceManagementConfigXmlParser();
			String xmlString = parser.unmarshallObjecttoXML(connectionInfo);
			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(configKey);
			configNodeData.setEnabled(true);
			boolean isConfigEnabled = true;
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(ResourceManagementConstant.RESOURCE_MANAGEMENT_CONNECTION_INFO_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, configKey,
					ResourceManagementConstant.RESOURCE_MANAGEMENT_CONNECTION_INFO_CONFIG_TYPE);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new ResourceManagementConfigurationException("ResouceMangement already exist for ConfigName="
						+ configKey + "--tree=" + configContext.getTenantId() + "/" + configContext.getSiteId() + "/"
						+ configContext.getFeatureGroup() + "/" + configContext.getFeatureName() + "/"
						+ configContext.getImplementationName() + "/" + configContext.getVendorName() + "/"
						+ configContext.getVersion());
			}
			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;
			ResourceManagementConfigurationUnit resourceManagementConfigUnit = new ResourceManagementConfigurationUnit(
					configContext.getTenantId(), configContext.getSiteId(), configNodeId, isConfigEnabled, configKey,
					connectionInfo);
			resourceManagementConfigUnit.setGroupId(configContext.getFeatureGroup());
			resourceManagementConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(resourceManagementConfigUnit);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | ResourceManagementConfigParserException e) {
			e.printStackTrace();
			throw new ResourceManagementConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + configKey, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Gets GetResourceContentConfiguration based on the request
	 */
	@Override
	public GetResourceContent getGetResourceContentConfiguration(RequestContext requestContext, String configName)
			throws ResourceManagementConfigurationException {
		// check if tenet/site/featuregroup are not null and not blank
		String methodName = "getGetResourceContentConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		isRequestContextValid(requestContext);
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
			logger.trace("{} reqContext= {}, configName={}", LEAP_LOG_KEY, requestContext, configName);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String rmGroupKey = ResourceManagementConfigurationUnit.getGetResourceContentConfigGroupKey(nodeId);
			logger.trace("{} rmGroupKey= {}", LEAP_LOG_KEY, rmGroupKey);
			ResourceManagementConfigurationUnit resourceManagementConfigUnit = (ResourceManagementConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId(), rmGroupKey, configName);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (resourceManagementConfigUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
				resourceManagementConfigUnit = (ResourceManagementConfigurationUnit) configServer
						.getConfiguration(requestContext.getTenantId(), rmGroupKey, configName);
			}
			if (resourceManagementConfigUnit == null) {
				throw new ResourceManagementConfigurationException("unable to get GetResourceContent configuration for configName " + configName);
			}
			Serializable configData = resourceManagementConfigUnit.getConfigData();
			if (configData instanceof GetResourceContent) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return (GetResourceContent) configData;
			} else {
				throw new ResourceManagementConfigurationException("unable to get GetResourceContent configuration");
			}
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new ResourceManagementConfigurationException(e);
		}
	}

	/**
	 * Gets ConnectionInfo based on the request
	 */
	@Override
	public ConnectionInfo getConnectionInfoConfiguration(RequestContext requestContext)
			throws ResourceManagementConfigurationException {
		// check if tenet/site/featuregroup are not null and not blank
		String methodName = "getConnectionInfoConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		isRequestContextValid(requestContext);
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
			logger.trace("{} reqContext= {}", LEAP_LOG_KEY, requestContext);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String rmGroupKey = ResourceManagementConfigurationUnit.getConnectionInfoConfigGroupKey(nodeId);
			logger.trace("{} rmGroupKey= {} ", LEAP_LOG_KEY, rmGroupKey);
			ResourceManagementConfigurationUnit resourceManagementConfigUnit = (ResourceManagementConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId(), rmGroupKey,
							ResourceManagementConstant.RESOURCE_MANAGEMENT_CONNECTION_INFO_CONFIG_NAME);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (resourceManagementConfigUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
				resourceManagementConfigUnit = (ResourceManagementConfigurationUnit) configServer.getConfiguration(
						requestContext.getTenantId(), rmGroupKey,
						ResourceManagementConstant.RESOURCE_MANAGEMENT_CONNECTION_INFO_CONFIG_NAME);
			}
			Serializable configData = resourceManagementConfigUnit.getConfigData();
			if (configData instanceof ConnectionInfo) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return (ConnectionInfo) configData;
			} else {
				throw new ResourceManagementConfigurationException("unable to get ConnectionInfo configuration");
			}
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new ResourceManagementConfigurationException(e);
		}
	}
	
	
	/**
	 * checks whether RequestContext is valid or not.
	 * 
	 * @param requestContext
	 * @throws ResourceManagementConfigurationException
	 */
	private void isRequestContextValid(RequestContext requestContext) throws ResourceManagementConfigurationException {
		if (requestContext == null) {
			throw new ResourceManagementConfigurationException("ResourceManagementRequestContext is null ");
		} else {
			if (!requestContext.isValid()) {
				throw new ResourceManagementConfigurationException(
						"ResourceManagementRequestContext is has required data as null or empty");
			}
		}
	}
}
