package com.attunedlabs.datacontext.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.datacontext.config.DataContextConstant.DATACONTEXT_CONFIG_TYPE;

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
import com.attunedlabs.datacontext.config.DataContextConfigurationUnit;
import com.attunedlabs.datacontext.config.DataContextConstant;
import com.attunedlabs.datacontext.config.DataContextParserException;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.DataContext;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;

public class DataContextConfigurationService extends GenericApplicableNode implements IDataContextConfigurationService {
	final Logger logger = LoggerFactory.getLogger(DataContextConfigurationService.class);

	/**
	 * This method is used to add dataContext into the db and dataGrid
	 * 
	 * @param configContext      : configuration Context contain the detail of
	 *                           tenant,site,featuregroup,feature,vendor,version
	 * @param featureDataContext : FeatureDataContext need to be added
	 * @throws DataContextConfigurationException : Unable to add data context into
	 *                                           the db/datagrid
	 */
	public void addDataContext(ConfigurationContext configContext, FeatureDataContext featureDataContext)
			throws DataContextConfigurationException {
		String methodName = "addDataContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId = 0;
		String configKey = getConfigname(configContext.getFeatureGroup(), configContext.getFeatureName());
		try {
			logger.trace("{} Config key ={}", LEAP_LOG_KEY, configKey);
			configNodeId = getApplicableNodeId(configContext);
			logger.trace("{} Applicable Config Node Id is =", LEAP_LOG_KEY, configNodeId);
			String xmlString = convertDataContextObjectXmlToString(featureDataContext);
			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(configKey);
			configNodeData.setEnabled(true);
			boolean isConfigEnabled = true;
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(DataContextConstant.DATACONTEXT_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, configKey, DataContextConstant.DATACONTEXT_CONFIG_TYPE);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new DataContextConfigurationException("FeatureDataContext already exist for ConfigName="
						+ configKey + "--tree=" + configContext.getTenantId() + "/" + configContext.getSiteId() + "/"
						+ configContext.getFeatureGroup() + "/" + configContext.getFeatureName() + "/"
						+ configContext.getImplementationName() + "/" + configContext.getVendorName() + "/"
						+ configContext.getVersion());
			}
			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;
			DataContextConfigurationUnit dataContextConfigUnit = new DataContextConfigurationUnit(
					configContext.getTenantId(), configContext.getSiteId(), configNodeId, isConfigEnabled, configKey,
					featureDataContext);
			dataContextConfigUnit.setGroupId(configContext.getFeatureGroup());
			dataContextConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(dataContextConfigUnit);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | DataContextParserException sqlExp) {

			throw new DataContextConfigurationException("Failed to insert ConfigData in DB for configName=" + configKey,
					sqlExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method

	/**
	 * This method is used to get The DataContext configuration unit
	 * 
	 * @param requestContext : Request Context
	 * @return DataContextConfigurationUnit : Data context configuration data
	 * @throws DataContextConfigurationException : Unable to get the configuration
	 *                                           Data
	 */
	public DataContextConfigurationUnit getDataContextConfiguration(RequestContext requestContext)
			throws DataContextConfigurationException {
		String methodName = "getDataContextConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// check if tenet/site/featuregroup are not null and not blank
		Integer nodeId = 0;
		String configName = getConfigname(requestContext.getFeatureGroup(), requestContext.getFeatureName());
		try {
			isFeatureRequestValid(requestContext);
			if (requestContext.getVendor() != null) {
				nodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
			} else {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			String fdcGroupKey = DataContextConfigurationUnit.getConfigGroupKey(nodeId);
			logger.trace("{} .getFeatureDataContextConfiguration fdcGroupKey={}", LEAP_LOG_KEY, fdcGroupKey);
			DataContextConfigurationUnit featureDataContextConfigUnit = (DataContextConfigurationUnit) configServer
					.getConfiguration(requestContext.getTenantId() + "-" + requestContext.getSiteId(), fdcGroupKey,
							configName);
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (featureDataContextConfigUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), null, null);
				featureDataContextConfigUnit = (DataContextConfigurationUnit) configServer
						.getConfiguration(requestContext.getTenantId(), fdcGroupKey, configName);
			}
			logger.trace("{} featureDataContextConfigUnit is :{} ", LEAP_LOG_KEY, featureDataContextConfigUnit);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureDataContextConfigUnit;
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new DataContextConfigurationException(
					"Unable to get the feature for tenant : " + requestContext.getTenantId() + ", site : "
							+ requestContext.getSiteId() + ", feature group : " + requestContext.getFeatureGroup()
							+ ", feature : " + requestContext.getFeatureName() + ", implementation Name : "
							+ requestContext.getImplementationName() + ", vendor : " + requestContext.getVendor()
							+ "version : " + requestContext.getVersion() + ", and configname : " + configName,
					e);
		}

	}

	/**
	 * Finds the Respective DataContextConfiguration and Deletes it from the
	 * DataGrid as well as from the DataBase<BR>
	 * Note:-To purge/remove only from DataGrid use .changeStatusOfFeatureConfig()
	 * marking status as disabled.
	 * 
	 * @param requestContext
	 * @return boolean : dataContext configuration deleted successfully or not
	 * @throws DataContextConfigurationException
	 * 
	 */
	public boolean deleteDataContextConfiguration(ConfigurationContext configContext)
			throws DataContextConfigurationException {
		String methodName = "deleteDataContextConfiguration";
		logger.debug("{} entered into the method {} configContext{}", LEAP_LOG_KEY, methodName, configContext);
		RequestContext reqContext = new RequestContext(configContext.getTenantId(), configContext.getSiteId(),
				configContext.getFeatureGroup(), configContext.getFeatureName(), configContext.getImplementationName(),
				configContext.getVendorName(), configContext.getVersion());
		String configName = getConfigname(reqContext.getFeatureGroup(), reqContext.getFeatureName());
		try {
			// First get the configuration from the dataGrid so that we can get
			// the
			// NodeDataId
			DataContextConfigurationUnit fdcConfigUnit = getDataContextConfiguration(reqContext);
			logger.trace("{} DataContextCOnfiguration Unit :{} ", LEAP_LOG_KEY, fdcConfigUnit);
			if (fdcConfigUnit == null) {
				logger.warn("{} Delete request for Non Cache DataContextConfig={}", LEAP_LOG_KEY, configName);
				// delete from DB
				Integer configNodeId = getApplicableNodeId(reqContext);
				return deleteFeatureConfigurationFromDb(configName, configNodeId);
			}
			// Delete from the DB First so that configVerifier should not
			// revitalise the config in dataGrid
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			boolean isDeleted = configPersistenceService.deleteConfigNodeData(fdcConfigUnit.getDbconfigId());
			String fdcGroupKey = FeatureConfigurationUnit.getConfigGroupKey(fdcConfigUnit.getAttachedNodeId());
			logger.trace("{} deleted from db NodeDataId={}", LEAP_LOG_KEY, fdcConfigUnit.getDbconfigId());
			// Now remove from DataGrid
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(fdcConfigUnit.getTenantId(), fdcGroupKey, configName);
			DataContextConfigurationUnit fsconfigUnit1 = getDataContextConfiguration(reqContext);
			logger.trace("{} DataContextConfiguration Unit after delete :{} ", LEAP_LOG_KEY, fsconfigUnit1);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return true;
		} catch (ConfigPersistenceException | ConfigServerInitializationException | InvalidNodeTreeException e) {
			throw new DataContextConfigurationException(
					"Failed to Delete DataContextConfiguration with name " + configName, e);
		}
	}// end of method

	/**
	 * This method is used to convert FeatureDataContext Object into xml string
	 * 
	 * @param featureDataContext : FeatureDataContext Object
	 * @return xmlString : FeatureDataContext Object into xml string
	 * @throws DataContextParserException : Unable to Convert FeatureDataContext
	 *                                    into xml String
	 */
	private String convertDataContextObjectXmlToString(FeatureDataContext featureDataContext)
			throws DataContextParserException {
		String methodName = "convertDataContextObjectXmlToString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		// Convert configTo Valid XML to store independent inDataBase
		DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
		String xmlString = builder.unmarshallObjecttoXML(featureDataContext);
		logger.debug("{} xml string :{} ", LEAP_LOG_KEY, xmlString);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xmlString;

	}

	/**
	 * This method is used to give config name
	 * 
	 * @param featureGroup : feature group in String
	 * @param featureName  : feature name in String
	 * @return configName : configname in string
	 */
	private String getConfigname(String featureGroup, String featureName) {
		String methodName = "getConfigname";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String configName = featureGroup + "-" + featureName + "-" + DataContextConstant.DATACONTEXT_SUFFIX_KEY;
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configName;
	}

	/**
	 * This method is used to load configuration into data grid
	 * 
	 * @param featureConfigUnit : Configuration unit for feature
	 * @throws DataContextConfigurationException
	 */
	private void loadConfigurationInDataGrid(DataContextConfigurationUnit dataContextConfigUnit)
			throws DataContextConfigurationException {
		String methodName = "loadConfigurationInDataGrid";
		logger.debug("{} entered into the method {} dataContextConfigUnit{}", LEAP_LOG_KEY, methodName,
				dataContextConfigUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.addConfiguration(dataContextConfigUnit);

		} catch (ConfigServerInitializationException e) {
			throw new DataContextConfigurationException(
					"Failed to Upload in DataGrid configName=" + dataContextConfigUnit.getKey(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method loadConfigurationInDataGrid

	private void isFeatureRequestValid(RequestContext requestContext) throws DataContextConfigurationException {
		if (requestContext == null || !requestContext.isValid()) {
			throw new DataContextConfigurationException("RequestContext is null or has required data as null or empty");
		}
	}

	/**
	 * delete the DataContextconfigaration by configName and NodeId
	 * 
	 * @param configName
	 * @param nodeId
	 * @return boolean
	 * @throws DataContextConfigurationException
	 */
	private boolean deleteFeatureConfigurationFromDb(String configName, int nodeId)
			throws DataContextConfigurationException {
		// Delete from the DB
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		logger.trace("{} .deleteFeatureConfigurationFromDb() deleted from db configName={}", LEAP_LOG_KEY, configName);
		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, nodeId);
		} catch (ConfigPersistenceException e) {
			logger.error("Persistance exception deleting the node cause:{} {} ", LEAP_LOG_KEY, e);
			throw new DataContextConfigurationException("Persistance exception deleting the node cause " + e);
		}
		return true;
	}// end of method

	@Override
	public boolean checkDataContextConfigExistOrNot(ConfigurationContext configurationContext, String configName)
			throws DataContextParserException, DataContextConfigurationException {
		boolean isEnabled = false;
		String methodParam = "configurationContext = " + configurationContext + " configName = " + configName;
		String methodName = "checkDataContextConfigExistOrNot";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, methodParam);
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());
		String vendorName = requestContext.getVendor();
		DataContextConfigurationUnit dataContextConfigurationUnit = null;
		try {
			int featureNodeId = 0;
			if (vendorName != null && !(vendorName.isEmpty())) {
				logger.debug("{} getting the node id till vendor level", LEAP_LOG_KEY);
				featureNodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());

			} else {
				logger.debug("{} getting the node id till feature level ", LEAP_LOG_KEY);
				featureNodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			logger.trace("{} featureNodeId :{} ", LEAP_LOG_KEY, featureNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
					configName, DATACONTEXT_CONFIG_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null)
				return false;

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				dataContextConfigurationUnit = getDataContextConfiguration(requestContext);
				if (dataContextConfigurationUnit == null) {
					enableAndLoadDataContextConfig(requestContext, configNodeData);
				}
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new DataContextParserException("Error in Searching the Feature with FeatureName = " + configName
					+ " and with requestContext = " + requestContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	/**
	 * Re-loads the DataContext into cache from the configured source.
	 * 
	 * @throws DataContextConfigurationException
	 * @throws
	 * @throws DataContextParserException
	 */
	@Override
	public boolean reloadDataContextCacheObject(RequestContext requestContext, String configName)
			throws DataContextConfigurationException {
		String methodName = "reloadDataContextCacheObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext == null && configName == null)
			throw new DataContextConfigurationException("requestContext and configName both should not be null");
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

		DataContextConfigurationUnit dataContextConfigurationUnit = null;
		try {
			dataContextConfigurationUnit = getDataContextConfiguration(requestContext);
			if (dataContextConfigurationUnit == null) {
				Integer applicableNodeId = getApplicableNodeId(requestContext);
				ConfigNodeData configNodeData = configPersistenceService
						.getConfigNodeDatabyNameAndNodeId(applicableNodeId, configName, DATACONTEXT_CONFIG_TYPE);
				if (configNodeData == null) {
					return false;
				}
				String psconfigStr = configNodeData.getConfigData();
				DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
				FeatureDataContext dsConfigs = builder.marshallConfigXMLtoObject(psconfigStr);

				DataContextConfigurationUnit dcConfigUnit = new DataContextConfigurationUnit(
						requestContext.getTenantId(), requestContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), true, configNodeData.getConfigName(), dsConfigs);
				dcConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(dcConfigUnit);
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			} else {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			}
		} catch (DataContextParserException e) {
			logger.error(
					"{} Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name={}",
					LEAP_LOG_KEY, configName, e);
			throw new DataContextConfigurationException(
					"Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name="
							+ configName,
					e);
		} catch (ConfigPersistenceException e) {
			logger.error("{} Failed to reLoad Config from DB with Name={}", LEAP_LOG_KEY, configName, e);
			throw new DataContextConfigurationException("Failed to reLoad Config from DB with Name=" + configName, e);
		} catch (InvalidNodeTreeException e) {
			logger.error("{} Failed to find the applicable NodeId {}", LEAP_LOG_KEY, e);
			throw new DataContextConfigurationException("Failed to find the applicable NodeId ", e);
		}

	}

	private void enableAndLoadDataContextConfig(RequestContext reqCtx, ConfigNodeData configNodeData)
			throws ConfigPersistenceException, DataContextParserException, DataContextConfigurationException {
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		configPersistenceService.enableConfigNodeData(true, configNodeData.getNodeDataId());
		String dsConfigStr = configNodeData.getConfigData();
		DataContextConfigXMLParser builder = new DataContextConfigXMLParser();
		FeatureDataContext dsConfigs = builder.marshallConfigXMLtoObject(dsConfigStr);
		DataContext dsConfig = dsConfigs.getDataContexts().getDataContext().get(0);
		DataContextConfigurationUnit dcConfigUnit = new DataContextConfigurationUnit(reqCtx.getTenantId(),
				reqCtx.getSiteId(), configNodeData.getParentConfigNodeId(), true, configNodeData.getConfigName(),
				dsConfigs);
		dcConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(dcConfigUnit);
	}

}
