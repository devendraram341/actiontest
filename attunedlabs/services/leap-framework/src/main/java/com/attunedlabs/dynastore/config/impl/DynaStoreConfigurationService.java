package com.attunedlabs.dynastore.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.Map;

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
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.dynastore.config.DynaStoreConfigParserException;
import com.attunedlabs.dynastore.config.DynaStoreConfigRequestContextException;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationConstant;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationException;
import com.attunedlabs.dynastore.config.DynaStoreConfigurationUnit;
import com.attunedlabs.dynastore.config.IDynaStoreConfigurationService;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfiguration;
import com.attunedlabs.dynastore.config.jaxb.DynastoreConfigurations;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class DynaStoreConfigurationService extends GenericApplicableNode implements IDynaStoreConfigurationService {

	final Logger logger = LoggerFactory.getLogger(DynaStoreConfigurationService.class);

	/**
	 * to add dynastoreconfiguration into DB as well as in Data Grid if config is
	 * Enabled
	 * 
	 * @param configurationContext
	 * @param DynastoreConfiguration
	 * @throws DynaStoreConfigurationException
	 */
	public void addDynaStoreConfiguration(ConfigurationContext configurationContext,
			DynastoreConfiguration dynastoreConfiguration) throws DynaStoreConfigurationException {
		String methodName = "addDynaStoreConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			int applicableId = getApplicableNodeId(configurationContext);

			DynaStoreConfigXmlParser dynaStoreConfigXmlParser = new DynaStoreConfigXmlParser();
			String xmlStringData = dynaStoreConfigXmlParser.unmarshallObjecttoXML(dynastoreConfiguration);

			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(dynastoreConfiguration.getDynastoreName().getValue());
			configNodeData.setParentConfigNodeId(applicableId);
			configNodeData.setEnabled(dynastoreConfiguration.isIsEnabled());
			configNodeData.setConfigLoadStatus("Success");
			configNodeData.setConfigType(DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);
			configNodeData.setConfigData(xmlStringData);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					applicableId, dynastoreConfiguration.getDynastoreName().getValue(),
					DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);
			int configDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {
				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
			} else {
				throw new DynaStoreConfigParserException("DynastoreConfiguration already exist for ConfigName="
						+ dynastoreConfiguration.getDynastoreName().getValue() + "--tree="
						+ configurationContext.getTenantId() + "/" + configurationContext.getSiteId() + "/"
						+ configurationContext.getFeatureGroup() + "/" + configurationContext.getFeatureName());

			}

			// To load dynastoreconfig data into Data Grid if config is in
			// Enable status
			if (dynastoreConfiguration.isIsEnabled()) {
				// initializaed Dyna Store
				ConfigDynastoreInitializerHelper helper = new ConfigDynastoreInitializerHelper();
				Map<String, Serializable> initializedDynaStore = helper.initializeDynaStore(dynastoreConfiguration);
				// uPDATE DATA GRID FORinitialized Map
				String dynaCollectionId = this.initializeGridWithDynaStore(initializedDynaStore);

				DynaStoreConfigurationUnit dynaStoreConfigurationUnit = new DynaStoreConfigurationUnit(
						configurationContext.getTenantId(), configurationContext.getSiteId(), applicableId,
						dynastoreConfiguration.isIsEnabled(), dynastoreConfiguration, dynaCollectionId);
				dynaStoreConfigurationUnit.setDbconfigId(configDataId);
				loadConfigurationToDatagrid(dynaStoreConfigurationUnit);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (InvalidNodeTreeException | ConfigPersistenceException | DynaStoreConfigParserException
				| ConfigDynaStoreInitializationException e) {
			throw new DynaStoreConfigurationException("Error in adding DynaStore Configuration with configNAme ="
					+ dynastoreConfiguration.getDynastoreName().getValue(), e);
		}
	}

	/**
	 * to getDynaStoreConfiguration from Data Grid
	 * 
	 * @param dynaStoreConfigRequestContext
	 * @param dynaStoreConfigName
	 * @param version
	 * @throws DynaStoreConfigRequestContextException
	 */
	public DynastoreConfiguration getDynaStoreConfiguration(RequestContext dynaStoreConfigRequestContext,
			String dynaStoreConfigName, String version) throws DynaStoreConfigRequestContextException {
		String methodParams = "requestContext= " + dynaStoreConfigRequestContext + "  dynaStoreConfigName= "
				+ dynaStoreConfigName + "  version= " + version;
		String methodName = "addDynaStoreConfiguration";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, methodParams);
		DynaStoreConfigurationUnit configUnit = getDynaStoreConfigurationUnit(dynaStoreConfigRequestContext,
				dynaStoreConfigName, version);
		if (configUnit == null)
			return null;
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configUnit.getDynastoreConfiguration();
	}

	public DynaStoreConfigurationUnit getDynaStoreConfigurationUnit(RequestContext dynaStoreConfigRequestContext,
			String dynaStoreConfigName, String version) throws DynaStoreConfigRequestContextException {
		String methodParam = "requestContext= " + dynaStoreConfigRequestContext + "  dynaStoreConfigName= "
				+ dynaStoreConfigName + "  version= " + version;
		String methodName = "getDynaStoreConfigurationUnit";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, methodParam);
		isDynaStoreConfigurationRequestValid(dynaStoreConfigRequestContext);
		ConfigurationContext configurationContext = new ConfigurationContext(dynaStoreConfigRequestContext);
		logger.trace("{} Configuration context : {}", LEAP_LOG_KEY, configurationContext);
		try {
			int applicableId = getApplicableNodeId(configurationContext);
			logger.trace("{} applicableNodeId={}", LEAP_LOG_KEY, applicableId);
			LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
			String configGroup = DynaStoreConfigurationUnit.getConfigGroupKey(applicableId);
			String configKey = "";
			// to concatination of configName with version
			if (version != null && !version.isEmpty()) {
				configKey = dynaStoreConfigName + "-" + version;
			} else {
				configKey = dynaStoreConfigName + "-" + DynaStoreConfigurationUnit.DYNASTORECONFIG_DEFULAT_VERSION;
			}
			DynaStoreConfigurationUnit dynaStoreConfigurationUnit = (DynaStoreConfigurationUnit) leapConfigurationServer
					.getConfiguration(configurationContext.getTenantId(), configGroup, configKey);
			logger.trace("{} configUnit={}", LEAP_LOG_KEY, dynaStoreConfigurationUnit);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return dynaStoreConfigurationUnit;
		} catch (InvalidNodeTreeException | ConfigPersistenceException | ConfigServerInitializationException e) {
			// TODO Auto-generated catch block
			throw new DynaStoreConfigRequestContextException("Error in getting dynastoreConfiguration with configname ="
					+ dynaStoreConfigName + " version=" + version);
		}
	}

	/**
	 * to change status of DynaStoreConfiguration to enable or disable, if enabling
	 * will load the configuration to Data Grid and change the status in DB to
	 * enable, for disable will delete the configuration from Data Grid and change
	 * the status in DB to disable
	 * 
	 * @param dyConfigRequestContext
	 * @param dynStoreConfigName
	 * @param version
	 * @param isEnable
	 * @throws DynaStoreConfigurationException
	 *
	 */
	public boolean changeStatusOfDynaStoreConfiguration(RequestContext dyConfigRequestContext,
			String dynStoreConfigName, String version, boolean isEnable) throws DynaStoreConfigurationException {
		String methodParam = "requestContext= " + dyConfigRequestContext + " configName =" + dynStoreConfigName
				+ ", version= " + version + " isEnable=" + isEnable;
		String methodName = "changeStatusOfDynaStoreConfiguration";
		logger.debug("{} entered into the method {} {}", LEAP_LOG_KEY, methodName, methodParam);
		try {
			isDynaStoreConfigurationRequestValid(dyConfigRequestContext);
			ConfigurationContext configurationContext = new ConfigurationContext(dyConfigRequestContext);

			int applicableId = getApplicableNodeId(configurationContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableId,
					dynStoreConfigName, DynaStoreConfigurationConstant.DYNASTORE_CONFIG_TYPE);

			if (configNodeData == null)
				throw new DynaStoreConfigurationException(
						" configuration with name=" + dynStoreConfigName + " doesnot exist in DB ");

			if (isEnable) {
				enableDynaStoreConfiguration(configurationContext, configNodeData);
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				return true;
			} else {
				DynaStoreConfigurationUnit dynaConfigUnit = this.getDynaStoreConfigurationUnit(dyConfigRequestContext,
						dynStoreConfigName, version);
				configPersistenceService.enableConfigNodeData(isEnable, configNodeData.getNodeDataId());
				if (dynaConfigUnit != null) {
					disabledDynaStoreConfiguration(configurationContext, applicableId, version, dynStoreConfigName,
							dynaConfigUnit.getDynaCollectionId());
				}
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			}

		} catch (DynaStoreConfigRequestContextException | InvalidNodeTreeException | ConfigPersistenceException
				| DynaStoreConfigParserException | ConfigServerInitializationException e) {
			throw new DynaStoreConfigurationException("error in changeStatus of DynastoreConfiguration with configNAme="
					+ dynStoreConfigName + " , version=" + version);
		} catch (ConfigDynaStoreInitializationException e) {
			throw new DynaStoreConfigurationException(
					"error in initializing ConfigDynastore at enabling and reloading time with configNAme="
							+ dynStoreConfigName + " , version=" + version);
		}

	}

	/**
	 * to delete DynaStoreConfiguration from DB and Data Grid (if Exist in Data
	 * Grid)
	 * 
	 * @param dyConfigRequestContext
	 * @param dynStoreConfigName
	 * @param version
	 * @throws DynaStoreConfigurationException
	 */
	public boolean deleteDynaStoreConfiguration(RequestContext dyConfigRequestContext, String dynStoreConfigName,
			String version) throws DynaStoreConfigurationException {
		String methodName = "deleteDynaStoreConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			isDynaStoreConfigurationRequestValid(dyConfigRequestContext);

			ConfigurationContext configurationContext = new ConfigurationContext(dyConfigRequestContext);

			int applicableId = getApplicableNodeId(configurationContext);

			DynastoreConfiguration dynastoreConfiguration = getDynaStoreConfiguration(dyConfigRequestContext,
					dynStoreConfigName, version);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

			if (dynastoreConfiguration == null) {

				configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(dynStoreConfigName, applicableId);
				return true;
			} else {
				DynaStoreConfigurationUnit dynaConfigUnit = this.getDynaStoreConfigurationUnit(dyConfigRequestContext,
						dynStoreConfigName, version);
				configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(dynStoreConfigName, applicableId);
				if (dynaConfigUnit != null)
					disabledDynaStoreConfiguration(configurationContext, applicableId, version, dynStoreConfigName,
							dynaConfigUnit.getDynaCollectionId());
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			}

		} catch (DynaStoreConfigRequestContextException | InvalidNodeTreeException | ConfigPersistenceException
				| ConfigServerInitializationException e) {

			throw new DynaStoreConfigurationException("error in deleting DynaStoreConfiguration with with configNAme="
					+ dynStoreConfigName + " , version=" + version);
		}

	}

	private String initializeGridWithDynaStore(Map<String, Serializable> initializedDyna) {
		Long id = DataGridService.getDataGridInstance()
				.getClusterUniqueId(DynaStoreConfigurationUnit.DYNA_UNIQUE_ID_NAME);
		HazelcastInstance hcInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> dynaMap = hcInstance
				.getMap(DynaStoreConfigurationUnit.DYNA_COLLECTION_PREFIX + id.toString());
		dynaMap.putAll(initializedDyna);
		return DynaStoreConfigurationUnit.DYNA_COLLECTION_PREFIX + id.toString();

	}

	private void deleteGridForDynaStore(String dynaStoreId) {
		HazelcastInstance hcInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> dynaMap = hcInstance
				.getMap(DynaStoreConfigurationUnit.DYNA_COLLECTION_PREFIX + dynaStoreId);
		dynaMap.clear();
	}

	/**
	 * ] to loadConfiguration Data to Data Grid
	 * 
	 * @param dynaStoreConfigurationUnit
	 * @throws DynaStoreConfigParserException
	 */
	private void loadConfigurationToDatagrid(DynaStoreConfigurationUnit dynaStoreConfigurationUnit)
			throws DynaStoreConfigParserException {

		try {
			if (dynaStoreConfigurationUnit.getIsEnabled()) {
				LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
				leapConfigurationServer.addConfiguration(dynaStoreConfigurationUnit);
			}
		} catch (ConfigServerInitializationException e) {
			throw new DynaStoreConfigParserException("Error adding dynatsoreConfig data to DataGrid", e);
		}
	}

	private void isDynaStoreConfigurationRequestValid(RequestContext requestContext)
			throws DynaStoreConfigRequestContextException {
		if (requestContext == null || !requestContext.isValid()) {
			throw new DynaStoreConfigRequestContextException(
					"DynaStoreConfigRequestContext is null or has required data as null or empty");
		}
	}

	/**
	 * to enable the configuration and load configuration to Data Grid
	 * 
	 * @param configurationContext
	 * @param configNodeData
	 * @throws DynaStoreConfigParserException
	 * @throws ConfigDynaStoreInitializationException
	 */
	private void enableDynaStoreConfiguration(ConfigurationContext configurationContext, ConfigNodeData configNodeData)
			throws DynaStoreConfigParserException, ConfigDynaStoreInitializationException {
		DynaStoreConfigXmlParser dynaStoreConfigXmlParser = new DynaStoreConfigXmlParser();
		DynastoreConfigurations dynastoreConfigurations = dynaStoreConfigXmlParser
				.marshallConfigXMLtoObject(configNodeData.getConfigData());
		DynastoreConfiguration dynastoreConfiguration = dynastoreConfigurations.getDynastoreConfiguration().get(0);

		ConfigDynastoreInitializerHelper helper = new ConfigDynastoreInitializerHelper();
		Map<String, Serializable> initializedDynaStore = helper.initializeDynaStore(dynastoreConfiguration);

		// uPDATE DATA GRID FORinitialized Map
		String dynaCollectionId = this.initializeGridWithDynaStore(initializedDynaStore);

		DynaStoreConfigurationUnit dynaStoreConfigurationUnit = new DynaStoreConfigurationUnit(
				configurationContext.getTenantId(), configurationContext.getSiteId(),
				configNodeData.getParentConfigNodeId(), true, dynastoreConfiguration, dynaCollectionId);
		dynaStoreConfigurationUnit.setDbconfigId(configNodeData.getNodeDataId());

		loadConfigurationToDatagrid(dynaStoreConfigurationUnit);

	}

	/**
	 * to disable the configuration and by delete the configuration in Data Grid
	 * 
	 * @param configurationContext
	 * @param applicableId
	 * @param version
	 * @param dynStoreConfigName
	 * @throws ConfigServerInitializationException
	 */
	private void disabledDynaStoreConfiguration(ConfigurationContext configurationContext, int applicableId,
			String version, String dynStoreConfigName, String dynaCollectionId)
			throws ConfigServerInitializationException {
		LeapConfigurationServer leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
		String configGroup = DynaStoreConfigurationUnit.getConfigGroupKey(applicableId);
		String configKey = "";
		if (version != null && !version.isEmpty()) {
			configKey = dynStoreConfigName + "-" + version;
		} else {
			configKey = dynStoreConfigName + "-" + DynaStoreConfigurationUnit.DYNASTORECONFIG_DEFULAT_VERSION;
		}
		leapConfigurationServer.deleteConfiguration(configurationContext.getTenantId(), configGroup, configKey);
		this.deleteGridForDynaStore(dynaCollectionId);
	}

}
