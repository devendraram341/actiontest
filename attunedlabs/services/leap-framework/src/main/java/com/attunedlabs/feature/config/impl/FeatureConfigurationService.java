package com.attunedlabs.feature.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.List;
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
import com.attunedlabs.feature.config.FeatureConfigParserException;
import com.attunedlabs.feature.config.FeatureConfigRequestContext;
import com.attunedlabs.feature.config.FeatureConfigRequestException;
import com.attunedlabs.feature.config.FeatureConfigurationConstant;
import com.attunedlabs.feature.config.FeatureConfigurationException;
import com.attunedlabs.feature.config.FeatureConfigurationUnit;
import com.attunedlabs.feature.config.IFeatureConfigurationService;
import com.attunedlabs.feature.jaxb.Feature;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.feature.jaxb.Service;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.featuremaster.FeatureMasterServiceException;
import com.attunedlabs.featuremaster.IFeatureMasterService;
import com.attunedlabs.featuremaster.impl.FeatureMasterService;
import com.attunedlabs.permastore.config.PermaStoreConfigurationConstant;

/**
 * This is a service class for feature
 * 
 * @author bizruntime
 *
 */
public class FeatureConfigurationService extends GenericApplicableNode implements IFeatureConfigurationService {
	final Logger logger = LoggerFactory.getLogger(FeatureConfigurationService.class);

	/**
	 * This method is used to add Feature Configuration in cache and database
	 * 
	 * @param : tenantid String type
	 * @param : siteId String type
	 * @param : groupName String type
	 * @param : Feature Object
	 */
	public void addFeatureConfiguration(ConfigurationContext configContext, Feature feature)
			throws FeatureConfigurationException {
		String methodName = "addFeatureConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String groupName = configContext.getFeatureGroup();
		String vendorName = configContext.getVendorName();
		String version = configContext.getVersion();
		String implName = configContext.getImplementationName();
		// Check and get ConfigNodeId for this
		try {
			Integer configNodeId = 0;
			if (vendorName != null && !(vendorName.isEmpty())) {
				configNodeId = getApplicableNodeId(tenantId, siteId, groupName, implName, vendorName, version, feature);
			} else {
				configNodeId = getApplicableNodeId(tenantId, siteId, groupName, implName, feature);
			}
			logger.trace("{} Applicable Config Node Id is {}", LEAP_LOG_KEY, configNodeId);

			String xmlString = convertFeatureObjectXmlToString(feature, groupName);

			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(feature.getFeatureName());
			configNodeData.setEnabled(feature.isEnabled());
			boolean isConfigEnabled = feature.isEnabled();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
					configNodeId, feature.getFeatureName(), FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			int configDataId = 0;

			// Check if Configuration already exist in the DataBase or not
			if (loadedConfigNodeData == null) {

				configDataId = configPersistenceService.insertConfigNodeData(configNodeData);

			} else {

				throw new FeatureConfigurationException("FeatureConfiguration already exist for ConfigName="
						+ feature.getFeatureName() + "--tree=" + tenantId + "/" + siteId + "/" + groupName + "/"
						+ feature.getFeatureName() + "/" + implName);
			}

			// UpDate Cache for this if config is enabled
			if (!isConfigEnabled)
				return;

			FeatureConfigurationUnit featureConfigUnit = new FeatureConfigurationUnit(tenantId, siteId, configNodeId,
					isConfigEnabled, feature);
			featureConfigUnit.setGroupId(groupName);
			featureConfigUnit.setDbconfigId(configDataId);
			loadConfigurationInDataGrid(featureConfigUnit);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | FeatureConfigParserException sqlExp) {

			throw new FeatureConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + feature.getFeatureName(), sqlExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of addFeatureConfiguration method

	/**
	 * Gets FeatureConfiguration based on the request
	 */
	public FeatureConfigurationUnit getFeatureConfiguration(FeatureConfigRequestContext requestContext,
			String configName) throws FeatureConfigurationException {
		// check if tenet/site/featuregroup are not null and not blank
		// logger.error("getFeatureConfiguration method started at :: " +
		// System.currentTimeMillis());
		String methodName = "getFeatureConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer nodeId = 0;
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
			// logger.error("time taken to getNodeId :: " + System.currentTimeMillis());
			logger.trace("{} request Context= {},configName= {}", LEAP_LOG_KEY, requestContext, configName);
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			// logger.error("time taken to get LeapConfigurationServer :: " +
			// System.currentTimeMillis());
			String fsGroupKey = FeatureConfigurationUnit.getConfigGroupKey(nodeId);
			// logger.error("time taken to get feature config group key :: " +
			// System.currentTimeMillis());
			logger.trace("{} fsGroupKey= {}", LEAP_LOG_KEY, fsGroupKey);
			FeatureConfigurationUnit featureConfigUnit = (FeatureConfigurationUnit) configServer.getConfiguration(
					requestContext.getTenantId() + "-" + requestContext.getSiteId(), fsGroupKey, configName);
			// logger.error("time taken to get config unit :: " +
			// System.currentTimeMillis());
			// if not found at the FeatureName level find it at Feature Group
			// Level.
			if (featureConfigUnit == null && requestContext.getFeatureName() != null) {
				nodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getImplementationName(), null);
				if (nodeId > 0)
					featureConfigUnit = (FeatureConfigurationUnit) configServer.getConfiguration(
							requestContext.getTenantId() + "-" + requestContext.getSiteId(), fsGroupKey, configName);
				else
					logger.debug("{} no feature config found ", LEAP_LOG_KEY);
			}
			// logger.error("getFeatureConfiguration method stopped at :: " +
			// System.currentTimeMillis());
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return featureConfigUnit;
		} catch (ConfigServerInitializationException | InvalidNodeTreeException | ConfigPersistenceException
				| FeatureConfigRequestException e) {
			throw new FeatureConfigurationException("Unable to get the feature for tenant : "
					+ requestContext.getTenantId() + ", site : " + requestContext.getSiteId() + ", feature group : "
					+ requestContext.getFeatureGroup() + ", feature : " + requestContext.getFeatureName()
					+ ", imple Name : " + requestContext.getImplementationName() + ", and configname : " + configName,
					e);
		}
	}

	/**
	 * This method is used to add the service into existing feature
	 * 
	 * @param featureRequestContext : feature request object which contain tenant
	 *                              ,site , featuregroup and feature name
	 * @param service               : Object of VendorImplementation that need to be
	 *                              added into feature
	 */
	public void addNewServiceInFeatureConfiguration(ConfigurationContext configContext, Service service)
			throws FeatureConfigurationException {
		String methodName = "addNewServiceInFeatureConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		Integer applicableNodeId = 0;
		Feature feature = null;
		boolean cacheflag = false;
		ConfigNodeData configNodeData = null;
		FeatureConfigRequestContext featureRequestContext = new FeatureConfigRequestContext(configContext.getTenantId(),
				configContext.getSiteId(), configContext.getFeatureGroup(), configContext.getFeatureName(),
				configContext.getImplementationName(), configContext.getVendorName(), configContext.getVersion());
		try {
			logger.trace("{} feature configuration is null , checking if data exist in db or not", LEAP_LOG_KEY);
			applicableNodeId = getApplicableNodeId(featureRequestContext);
			configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					featureRequestContext.getFeatureName(), FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			if (configNodeData == null) {
				// Not in DB so it does not exist throw exception
				throw new FeatureConfigurationException("FeatureConfig with Name( "
						+ featureRequestContext.getFeatureName() + ") does not exist in DB");
			}
			String configData = configNodeData.getConfigData();
			feature = convertFeatureXmlStringToObject(configData);
			List<Service> serviceList = feature.getService();
			serviceList.add(service);
			String updatedXml = convertFeatureObjectXmlToString(feature, featureRequestContext.getFeatureGroup());
			logger.trace("{} added new service : {}", LEAP_LOG_KEY, updatedXml);
			configNodeData.setConfigData(updatedXml);
			configPersistenceService.updateConfigNodeData(configNodeData);
			FeatureConfigurationUnit configUnit = getFeatureConfiguration(featureRequestContext,
					featureRequestContext.getFeatureName());
			logger.trace("{} featureCOnfiguration before : {}", LEAP_LOG_KEY, configUnit);
			if (configUnit != null) {
				logger.trace("{} isEnabled : {}", LEAP_LOG_KEY, configUnit.getIsEnabled());
				cacheflag = configUnit.getIsEnabled();
			}

			if (cacheflag) {
				logger.trace("{} Feature is active and exist in configuration, therefore adding new vendor into it",
						LEAP_LOG_KEY);
				FeatureConfigurationUnit featureConfigUnit = new FeatureConfigurationUnit(
						featureRequestContext.getTenantId(), featureRequestContext.getSiteId(), applicableNodeId,
						cacheflag, feature);
				featureConfigUnit.setGroupId(featureRequestContext.getFeatureGroup());
				featureConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(featureConfigUnit);
			}
			FeatureConfigurationUnit config = getFeatureConfiguration(featureRequestContext,
					featureRequestContext.getFeatureName());
			logger.trace("{} featureConfig after adding new Service : {}", LEAP_LOG_KEY, config);

		} catch (ConfigPersistenceException | InvalidNodeTreeException | FeatureConfigParserException e) {
			throw new FeatureConfigurationException(
					"Failed to add a new service for the feature group : " + featureRequestContext.getFeatureGroup()
							+ " and for feature : " + featureRequestContext.getFeatureName(),
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of method*/

	/**
	 * update Featureconfigaration based on the request
	 * 
	 * @param tenantId         : tenant name
	 * @param siteId           : site name
	 * @param Feature          : feature name
	 * @param configNodedataId : config node data id
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 */
	public int updateFeatureConfiguration(ConfigurationContext configContext, String groupName, Feature fsConfig,
			int configNodedataId) throws FeatureConfigurationException {
		int sucess = 0;
		String methodName = "updateFeatureConfiguration";
		logger.debug("{} entered into the method {}, feature: {}", LEAP_LOG_KEY, methodName, fsConfig);
		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		try {
			// Check and get ConfigNodeId for this
			Integer configNodeId = getApplicableNodeId(configContext);
			logger.trace("{} Applicable Config Node Id is ={}", LEAP_LOG_KEY, configNodeId);

			// Convert configTo Valid XML to store independent inDataBase
			String xmlString = convertFeatureObjectXmlToString(fsConfig, groupName);

			// Update DB for this configuration
			ConfigNodeData configNodeData = new ConfigNodeData();
			configNodeData.setConfigName(fsConfig.getFeatureName());

			configNodeData.setEnabled(fsConfig.isEnabled());
			boolean isConfigEnabled = fsConfig.isEnabled();
			configNodeData.setConfigLoadStatus("Sucess");
			configNodeData.setConfigType(FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			configNodeData.setParentConfigNodeId(configNodeId);
			configNodeData.setConfigData(xmlString);
			configNodeData.setNodeDataId(configNodedataId);
			FeatureConfigRequestContext featureRequestContext = null;
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();

			logger.trace("{} before updating the feature configNodeData: {}, configId {} ", LEAP_LOG_KEY,
					configNodeData, configNodedataId);
			sucess = configPersistenceService.updateConfigNodeData(configNodeData);

			if (fsConfig.getFeatureName() != null) {
				featureRequestContext = new FeatureConfigRequestContext(tenantId, siteId, groupName,
						fsConfig.getFeatureName(), configContext.getImplementationName());
				changeStatusOfFeatureConfig(configContext, fsConfig.getFeatureName(), isConfigEnabled);
			} else {
				featureRequestContext = new FeatureConfigRequestContext(tenantId, siteId, groupName);
				changeStatusOfFeatureConfig(configContext, fsConfig.getFeatureName(), isConfigEnabled);
			}

		} catch (ConfigPersistenceException | InvalidNodeTreeException | FeatureConfigParserException sqlExp) {
			throw new FeatureConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + fsConfig.getFeatureName(), sqlExp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return sucess;

	}

	/**
	 * This method is used to change the enabled value of feature in database and in
	 * cache if exist
	 * 
	 * @param featureRequestContext : FeatureConfigRequestContext object containing
	 *                              tenant,site,featuregroup and feature name
	 * @param featureName           : name of the feature whose enabled value we
	 *                              need to change
	 * @param isConfigEnabled       : boolean value to enable or disable feature
	 */
	public void changeStatusOfFeatureConfig(ConfigurationContext configContext, String featureName,
			boolean isConfigEnabled) throws FeatureConfigurationException {
		String methodName = "changeStatusOfFeatureConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			FeatureConfigRequestContext featureRequestContext = new FeatureConfigRequestContext(
					configContext.getTenantId(), configContext.getSiteId(), configContext.getFeatureGroup(),
					configContext.getFeatureName(), configContext.getImplementationName(),
					configContext.getVendorName(), configContext.getVersion());
			Integer applicableNodeId = getApplicableNodeId(featureRequestContext);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					featureName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			if (configNodeData == null) {
				// Not in DB so it does not exist throw exception
				throw new FeatureConfigurationException(
						"FeatureConfig with Name( " + featureName + ") does not exist in DB");
			}

			// Disable Request
			if (!isConfigEnabled) {
//				logger.debug("request came to disable the feature service");
				updateFeatureXmlInDBOnStatusChange(featureRequestContext, configNodeData, featureName, isConfigEnabled,
						configPersistenceService, applicableNodeId);

				// We have to Disable fsConfig hence remove from DataGrid and update
				// DB as disabled Configuration
				configPersistenceService.enableConfigNodeData(false, configNodeData.getNodeDataId());

				// Now remove from DataGrid
				String fsGroupKey = FeatureConfigurationUnit.getConfigGroupKey(configNodeData.getParentConfigNodeId());
				LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
				configServer.deleteConfiguration(featureRequestContext.getTenantId(), fsGroupKey, featureName);

			} else {
//				logger.debug("request came to enable the feature service : "+isConfigEnabled);
				// Enable Request-Load Config from DataBase and update the DataGrid
				updateFeatureXmlInDBOnStatusChange(featureRequestContext, configNodeData, featureName, isConfigEnabled,
						configPersistenceService, applicableNodeId);
				ConfigNodeData configNodeData1 = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
						applicableNodeId, featureName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
				enableAndLoadFeatureConfig(featureRequestContext, configNodeData1);
			}

		} catch (ConfigPersistenceException | ConfigServerInitializationException | InvalidNodeTreeException
				| FeatureConfigParserException e) {
			throw new FeatureConfigurationException("Failed to Enable/Disable FeatureConfig with name " + featureName,
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used change Feature Service enabled value for a feature
	 * 
	 * @param featureRequestContext : requesxt context object
	 * @param configName            : name of config
	 * @param isEnable              : true or false
	 * @throws FeatureConfigurationException
	 */
	public void changeStatusOfFeatureService(ConfigurationContext configContext, String configName,
			Map<String, Boolean> enabled) throws FeatureConfigurationException {
		String methodName = "changeStatusOfFeatureService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer applicableNodeId;
		FeatureConfigRequestContext featureRequestContext = new FeatureConfigRequestContext(configContext.getTenantId(),
				configContext.getSiteId(), configContext.getFeatureGroup(), configContext.getFeatureName(),
				configContext.getImplementationName(), configContext.getVendorName(), configContext.getVersion());
		try {
			applicableNodeId = getApplicableNodeId(featureRequestContext);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(applicableNodeId,
					configName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			if (configNodeData == null) {
				// Not in DB so it does not exist throw exception
				throw new FeatureConfigurationException(
						"featureConfig with Name( " + configName + ") does not exist in DB");
			}
			Feature feature = null;
			String featureGroupKey = FeatureConfigurationUnit.getConfigGroupKey(configNodeData.getParentConfigNodeId());
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			FeatureConfigurationUnit featureConfigUnit = (FeatureConfigurationUnit) configServer.getConfiguration(
					featureRequestContext.getTenantId() + "-" + featureRequestContext.getSiteId(), featureGroupKey,
					configName);

			if (featureConfigUnit != null) {
				Object configobject = (Object) featureConfigUnit.getConfigData();
				if (configobject instanceof Feature)
					feature = (Feature) configobject;
			} else {
				String featureXml = configNodeData.getConfigData();
				feature = convertFeatureXmlStringToObject(featureXml);
			}
			// logic to change service status
			if (enabled != null) {
				// iterating over all values in vendStatus map
				for (Map.Entry<String, Boolean> entry : enabled.entrySet()) {
					// get key for whom status need to change
					String serviceName = entry.getKey();
					logger.trace("{} serivce name : {}", LEAP_LOG_KEY, serviceName);
					List<Service> serviceList = feature.getService();
					for (Service service : serviceList) {

						if (service.getName().equalsIgnoreCase(serviceName)) {
							service.setEnabled(entry.getValue());
						}
					} // end of for (Service service : serviceList)
				} // end of outer for loop
			} // end of if(enabled != null)

			// update featureXml store in db
			String updatedFeature = convertFeatureObjectXmlToString(feature, featureRequestContext.getFeatureGroup());
			logger.debug("{} feature after changing isenabled value for service : {}", LEAP_LOG_KEY, updatedFeature);
			configPersistenceService.updateConfigdataInConfigNodeData(updatedFeature, applicableNodeId,
					featureRequestContext.getFeatureName(), FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
			loadFeatureConfig(featureRequestContext, configNodeData, updatedFeature, feature.isEnabled());
		} catch (ConfigPersistenceException | InvalidNodeTreeException | ConfigServerInitializationException
				| FeatureConfigParserException e) {
			throw new FeatureConfigurationException("Failed to Enable/Disable Feature Service with name " + configName,
					e);

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * This method is used to change the value of enabled value of xml store as
	 * config data in db
	 * 
	 * @param featureRequestContext    : FeatureConfigRequestContext object
	 *                                 containing tenant,site,featuregroup and
	 *                                 feature name
	 * @param configNodeData           : It is a VO object for confignodedata table
	 * @param featureName              : name of the feature
	 * @param isConfigEnabled          : enable value of feature (true/false)
	 * @param configPersistenceService : configPersistenceService object used to
	 *                                 call update method
	 * @param applicableNodeId         : nodeId of feature
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 * @throws ConfigPersistenceException
	 */
	private void updateFeatureXmlInDBOnStatusChange(FeatureConfigRequestContext featureRequestContext,
			ConfigNodeData configNodeData, String featureName, boolean isConfigEnabled,
			IConfigPersistenceService configPersistenceService, Integer applicableNodeId)
			throws FeatureConfigurationException, FeatureConfigParserException, ConfigPersistenceException {

		String configData = configNodeData.getConfigData();
		Feature feature = convertFeatureXmlStringToObject(configData);
//		logger.debug("isenabled in updateFeatureXmlInDBOnStatusChange : "+isConfigEnabled);
		feature.setEnabled(isConfigEnabled);
		String updateFeature = convertFeatureObjectXmlToString(feature, featureRequestContext.getFeatureGroup());
		logger.trace("{} updated isenabled of  feature in updateFeatureXmlInDBOnStatusChange : {}", LEAP_LOG_KEY,
				updateFeature);
		boolean updateConfiData = configPersistenceService.updateConfigdataInConfigNodeData(updateFeature,
				applicableNodeId, featureName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
		loadFeatureConfig(featureRequestContext, configNodeData, updateFeature, isConfigEnabled);

	}

	/**
	 * This method is used to reloadFeatureConfig
	 * 
	 * @param featureRequestContext : FeatureConfigRequestContext object containing
	 *                              tenant,site,featuregroup and feature name
	 * @param configNodeData        : vo object of confignodedata table
	 * @param updatedfeature        : featurexml in string format
	 * @param isEnabled             : feature is enable/disbled
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigParserException
	 */
	private void loadFeatureConfig(FeatureConfigRequestContext featureRequestContext, ConfigNodeData configNodeData,
			String updatedfeature, boolean isEnabled)
			throws FeatureConfigurationException, FeatureConfigParserException {

		Feature feature = convertFeatureXmlStringToObject(updatedfeature);

		FeatureConfigurationUnit featureConfigUnit = new FeatureConfigurationUnit(featureRequestContext.getTenantId(),
				featureRequestContext.getSiteId(), configNodeData.getParentConfigNodeId(), isEnabled, feature);
		featureConfigUnit.setGroupId(featureRequestContext.getFeatureGroup());
		featureConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(featureConfigUnit);

	}

	/**
	 * Finds the Respective FeatureConfiguration and Deletes it from the DataGrid as
	 * well as from the DataBase<BR>
	 * Note:-To purge/remove only from DataGrid use .changeStatusOfFeatureConfig()
	 * marking status as disabled.
	 * 
	 * @param requestContext
	 * @param configName
	 * @return boolean : Feature configuration deleted successfully or not
	 * @throws FeatureConfigurationException
	 * 
	 */
	public boolean deleteFeatureConfiguration(ConfigurationContext configContext, String configName)
			throws FeatureConfigurationException {
		String methodName = "deleteFeatureConfiguration";
		logger.debug("{} entered into the method {}, configContext={}, configName={}", LEAP_LOG_KEY, methodName,
				configContext, configName);
		FeatureConfigRequestContext reqContext = new FeatureConfigRequestContext(configContext.getTenantId(),
				configContext.getSiteId(), configContext.getFeatureGroup(), configContext.getFeatureName(),
				configContext.getImplementationName(), configContext.getVendorName(), configContext.getVersion());
		try {
			// First get the configuration from the dataGrid so that we can get the
			// NodeDataId
			FeatureConfigurationUnit fsconfigUnit = getFeatureConfiguration(reqContext, configName);
			logger.debug("{} featureCOnfiguration Unit : {}", LEAP_LOG_KEY, fsconfigUnit);

			if (fsconfigUnit == null) {
				logger.warn("{} Delete request for Non Cache FeatureConfig={}", LEAP_LOG_KEY, configName);
				// delete from DB
				Integer configNodeId = getApplicableNodeIdFeatureName(reqContext.getTenantId(), reqContext.getSiteId(),
						reqContext.getFeatureGroup(), reqContext.getImplementationName(), reqContext.getFeatureName());
				return deleteFeatureConfigurationFromDb(configName, configNodeId);
			}

			// Delete from the DB First so that configVerifier should not
			// revitalise the config in dataGrid
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			boolean isDeleted = configPersistenceService.deleteConfigNodeData(fsconfigUnit.getDbconfigId());
			String fsGroupKey = FeatureConfigurationUnit.getConfigGroupKey(fsconfigUnit.getAttachedNodeId());
			logger.debug("{} deleted from db NodeDataId={}", LEAP_LOG_KEY, fsconfigUnit.getDbconfigId());

			// Now remove from DataGrid
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			configServer.deleteConfiguration(fsconfigUnit.getTenantId(), fsGroupKey, configName);
			logger.debug("{} deleted from DataGrid fsGroupKey={}, configName={}", LEAP_LOG_KEY, fsGroupKey, configName);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return true;
		} catch (ConfigPersistenceException | ConfigServerInitializationException | InvalidNodeTreeException e) {
			throw new FeatureConfigurationException("Failed to Delete FeatureConfig with name " + configName, e);
		}
	}

	/**
	 * delete the Featureconfigaration by configName and NodeId
	 * 
	 * @param configName
	 * @param nodeId
	 * @return boolean
	 * @throws FeatureConfigurationException
	 */
	private boolean deleteFeatureConfigurationFromDb(String configName, int nodeId)
			throws FeatureConfigurationException {
		// Delete from the DB
		String methodName = "deleteFeatureConfigurationFromDb";
		logger.debug("{} entered into the method {}, configName={}", LEAP_LOG_KEY, methodName, configName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		try {
			configPersistenceService.deleteConfigNodeDataByNodeIdAndConfigName(configName, nodeId);
		} catch (ConfigPersistenceException e) {
			logger.error("{} Persistance exception deleting the node cause: {}", LEAP_LOG_KEY, e);
			throw new FeatureConfigurationException("Persistance exception deleting the node cause " + e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	private String convertFeatureObjectXmlToString(Feature feature, String groupName)
			throws FeatureConfigParserException {
		String methodName = "convertFeatureObjectXmlToString";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		// Convert configTo Valid XML to store independent inDataBase
		FeatureConfigXMLParser builder = new FeatureConfigXMLParser();
		String xmlString = builder.unmarshallObjecttoXML(feature, groupName);

		logger.trace("{} xml string : {}", LEAP_LOG_KEY, xmlString);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return xmlString;

	}

	/**
	 * This method is used to convert feature xml string into Object
	 * 
	 * @param featurexmlString : featurexml in String type
	 * @return Feature
	 * @throws FeatureConfigParserException
	 */
	private Feature convertFeatureXmlStringToObject(String featurexmlString) throws FeatureConfigParserException {

		String methodName = "convertFeatureXmlStringToObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		FeatureConfigXMLParser builder = new FeatureConfigXMLParser();
		FeaturesServiceInfo fsConfigs = builder.marshallXMLtoObject(featurexmlString);
		// As it is loaded from DB I know there will always be one config only
		Feature feature = fsConfigs.getFeatures().getFeature();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return feature;

	}

	/**
	 * 
	 * @param featureRequestContext
	 * @param configNodeData
	 * @throws FeatureConfigurationException
	 * @throws ConfigPersistenceException
	 * @throws FeatureConfigParserException
	 */
	private void enableAndLoadFeatureConfig(FeatureConfigRequestContext featureRequestContext,
			ConfigNodeData configNodeData)
			throws FeatureConfigurationException, ConfigPersistenceException, FeatureConfigParserException {
		String methodName = "enableAndLoadFeatureConfig";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		// Update Enable in the Database
		configPersistenceService.enableConfigNodeData(true, configNodeData.getNodeDataId());
		// Get XML from DB and Load in the DataGrid
		String fsconfigStr = configNodeData.getConfigData();

		Feature feature = convertFeatureXmlStringToObject(fsconfigStr);

		FeatureConfigurationUnit fsConfigUnit = new FeatureConfigurationUnit(featureRequestContext.getTenantId(),
				featureRequestContext.getSiteId(), configNodeData.getParentConfigNodeId(), true, feature);
//		logger.debug("config id  while reloading feature to enable stage : "+configNodeData.getNodeDataId());
		fsConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
		loadConfigurationInDataGrid(fsConfigUnit);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to load configuration into data grid
	 * 
	 * @param featureConfigUnit : Configuration unit for feature
	 * @throws FeatureConfigurationException
	 */
	private void loadConfigurationInDataGrid(FeatureConfigurationUnit featureConfigUnit)
			throws FeatureConfigurationException {

		String methodName = "loadConfigurationInDataGrid";
		logger.debug("{} entered into the method {},FeatureConfigurationUnit={}", LEAP_LOG_KEY, methodName,
				featureConfigUnit);
		try {
			LeapConfigurationServer configServer = LeapConfigurationServer.getConfigurationService();
			// I don't want to store the FeatureConfiguration in the DataGrid Cache
			// hence setting it as null

			configServer.addConfiguration(featureConfigUnit);

		} catch (ConfigServerInitializationException e) {
			throw new FeatureConfigurationException(
					"Failed to Upload in DataGrid configName=" + featureConfigUnit.getKey(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method loadConfigurationInDataGrid

	private void isFeatureRequestValid(FeatureConfigRequestContext requestContext)
			throws FeatureConfigRequestException {
		if (requestContext == null || !requestContext.isValid()) {
			throw new FeatureConfigRequestException(
					"FeatureRequestContext is null or has required data as null or empty");
		}
	}

	/**
	 * Based on featureRequestContext and configName find in Db, if exist check
	 * wether it enabled or not,if enabled , check in cache if not exist load into
	 * cache and return true, if data not exist in Db return false Tag
	 * FeatureConfiguration <BR>
	 *
	 * @param FeatureConfigRequestContext
	 * @param configName
	 * @return Boolean
	 * @throws FeatureConfigurationException
	 * @throws FeatureConfigRequestException
	 */
	public boolean checkFeatureExistInDBAndCache(ConfigurationContext configContext, String configName)
			throws FeatureConfigurationException, FeatureConfigRequestException {
		String methodName = "checkFeatureExistInDBAndCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		FeatureConfigRequestContext requestContext = new FeatureConfigRequestContext(configContext.getTenantId(),
				configContext.getSiteId(), configContext.getFeatureGroup(), configContext.getFeatureName(),
				configContext.getImplementationName(), configContext.getVendorName(), configContext.getVersion());
		boolean isEnabled = false;
		FeatureConfigurationUnit fetConfigurationUnit = null;
		String vendorName = requestContext.getVendor();
		String version = requestContext.getVersion();
		try {
			int featureNodeId = 0;
			if (vendorName != null && !(vendorName.isEmpty())) {
				logger.trace("{} getting the node id till vendor level", LEAP_LOG_KEY);
				featureNodeId = getApplicableNodeIdVendorName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName(), requestContext.getVendor(),
						requestContext.getVersion());
			} else {
				logger.trace("{} getting the node id till feature level", LEAP_LOG_KEY);
				featureNodeId = getApplicableNodeIdFeatureName(requestContext.getTenantId(), requestContext.getSiteId(),
						requestContext.getFeatureGroup(), requestContext.getFeatureName(),
						requestContext.getImplementationName());
			}
			logger.debug("{} featureNodeId : {}", LEAP_LOG_KEY, featureNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(featureNodeId,
					configName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);

			// if confignodedata not Exist
			if (configNodeData == null)
				return false;

			isEnabled = configNodeData.isEnabled();
			if (isEnabled) {
				try {
					fetConfigurationUnit = getFeatureConfiguration(requestContext, configName);
					if (fetConfigurationUnit == null) {
						enableAndLoadFeatureConfig(requestContext, configNodeData);
					}
				} catch (FeatureConfigurationException e) {
					logger.error("{} Error in finding the feature in cache {}", LEAP_LOG_KEY, e);
				} catch (FeatureConfigParserException e) {
					throw new FeatureConfigurationException(
							"Failed to load featureConfigaraion to cache  with featureName = " + configName
									+ " requestContext = " + requestContext);
				}

			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureConfigRequestException("Error in Searching the Feature with FeatureName = " + configName
					+ " and with requestContext = " + requestContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	/**
	 * Re-loads the cacahed object in the permastore from the configured source.
	 * 
	 * @throws FeatureConfigRequestException
	 * 
	 * @throws FeatureConfigParserException
	 * @throws FeatureConfigurationException
	 * @throws InvalidNodeTreeException
	 */
	@Override
	public boolean reloadFeatureCacheObject(RequestContext requestContext, String configName)
			throws FeatureConfigRequestException {
		String methodName = "reloadFeatureCacheObject";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (requestContext == null && configName == null)
			throw new FeatureConfigRequestException("requestContext and configName both should not be null");
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		FeatureConfigurationUnit featureConfigUnit;
		try {
			FeatureConfigRequestContext featureRequestContext = new FeatureConfigRequestContext(
					requestContext.getTenantId(), requestContext.getSiteId(), requestContext.getFeatureGroup(),
					requestContext.getFeatureName(), requestContext.getImplementationName(), requestContext.getVendor(),
					requestContext.getVersion());
			featureConfigUnit = getFeatureConfiguration(featureRequestContext, configName);
			if (featureConfigUnit == null) {
				Integer applicableNodeId = getApplicableNodeId(requestContext);
				ConfigNodeData configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(
						applicableNodeId, configName, FeatureConfigurationConstant.FEATURE_CONFIG_TYPE);
				if (configNodeData == null)
					return false;
				// Get XML from DB and Load in the DataGrid
				String fsconfigStr = configNodeData.getConfigData();

				Feature feature = convertFeatureXmlStringToObject(fsconfigStr);

				FeatureConfigurationUnit fsConfigUnit = new FeatureConfigurationUnit(
						featureRequestContext.getTenantId(), featureRequestContext.getSiteId(),
						configNodeData.getParentConfigNodeId(), true, feature);
				// logger.debug("config id while reloading feature to enable
				// stage : "+configNodeData.getNodeDataId());
				fsConfigUnit.setDbconfigId(configNodeData.getNodeDataId());
				loadConfigurationInDataGrid(fsConfigUnit);
				ConfigurationContext configurationContext = requestContext.getConfigurationContext();
				logger.debug("{} configurationContext inside loadFeatureResourceInFeatureMetaInfo : {}", LEAP_LOG_KEY,
						configurationContext);
				try {
					IFeatureConfigurationService featureConfigService = new FeatureConfigurationService();
					boolean isExist = featureConfigService.checkFeatureExistInDBAndCache(configurationContext,
							feature.getFeatureName());
					if (!isExist) {
						logger.trace("{} inside if block in reload method", LEAP_LOG_KEY);
						addFeatureInFeatureDeployment(configurationContext);
						featureConfigService.addFeatureConfiguration(configurationContext, feature);
					} else {
						logger.trace("{} inside else block in reload method", LEAP_LOG_KEY);
						addFeatureInFeatureDeploymentForCache(configurationContext);
						logger.debug(
								"{} feature configuration for : {}, already exist for featuregroup : {}, feature : {}, impl name : {} in db",
								LEAP_LOG_KEY, feature.getFeatureName(), configurationContext.getFeatureGroup(),
								configurationContext.getFeatureName(), configurationContext.getImplementationName());
					}
				} catch (FeatureConfigurationException | FeatureConfigRequestException
						| FeatureDeploymentServiceException e) {
					throw new FeatureConfigRequestException(
							"error in loading the feature Configuration for feature = " + feature.getFeatureName(), e);
				}
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			} else {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			}
		} catch (FeatureConfigurationException e) {
			logger.error(
					"{} Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name={}",
					LEAP_LOG_KEY, configName, e);
			throw new FeatureConfigRequestException(
					"Failed to reLoad ConfigurationUnit from cache it either not exist or is disabled with Name="
							+ configName,
					e);
		} catch (ConfigPersistenceException e) {
			logger.error("{} Failed to reLoad Config from DB with Name={}", LEAP_LOG_KEY, configName, e);
			throw new FeatureConfigRequestException("Failed to reLoad Config from DB with Name=" + configName, e);
		} catch (InvalidNodeTreeException | FeatureConfigParserException e) {
			logger.error("{} Failed to xml-parse Config from DB with Name={}", LEAP_LOG_KEY, configName, e);
			throw new FeatureConfigRequestException("Failed to xml-parse Config from DB with Name=" + configName, e);
		}

	}

	private void addFeatureInFeatureDeployment(ConfigurationContext configurationContext)
			throws FeatureDeploymentServiceException {
		String methodName = "addFeatureInFeatureDeployment";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IFeatureDeployment featureDeployment = new FeatureDeploymentService();

		boolean isAlreadyDeployed = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		if (isAlreadyDeployed) {
			logger.trace("{} configurationContext in addFeatureInFeatureDeployment : {}", LEAP_LOG_KEY,
					configurationContext);
			featureDeployment.addFeatureDeployement(configurationContext, true, false, true);
			/*
			 * try { List<LeapI18nMessage> i18nMessageContextList =
			 * bundleResolver.getAllLeapLocaleObjects(); if
			 * (!i18nMessageContextList.isEmpty())
			 * localeRegistryService.buildLocaleBundle(i18nMessageContextList);
			 * 
			 * } catch (LocaleResolverException e) { throw new
			 * FeatureDeploymentServiceException("Unable to build the bundles as expected! "
			 * , e); }
			 */
		} else {
			featureDeployment.addFeatureDeployement(configurationContext, true, true, true);
			/*
			 * try { List<LeapI18nMessage> i18nMessageContextList =
			 * bundleResolver.getAllLeapLocaleObjects(); if
			 * (!i18nMessageContextList.isEmpty())
			 * localeRegistryService.buildLocaleBundle(i18nMessageContextList);
			 * 
			 * } catch (LocaleResolverException e) { throw new
			 * FeatureDeploymentServiceException("Unable to build the bundles as expected! "
			 * , e); }
			 */
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private void addFeatureInFeatureDeploymentForCache(ConfigurationContext configurationContext)
			throws FeatureDeploymentServiceException {
		String methodName = "addFeatureInFeatureDeploymentForCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IFeatureDeployment featureDeployment = new FeatureDeploymentService();

		boolean isAlreadyDeployed = featureDeployment.checkIfFeatureIsAlreadyDeployed(configurationContext);
		if (isAlreadyDeployed) {
			logger.trace("{} configurationContext in addFeatureInFeatureDeployment : {}", LEAP_LOG_KEY,
					configurationContext);
			featureDeployment.CheckAndaddFeatureDeployementInCache(configurationContext, true, false, true);
			/*
			 * try { List<LeapI18nMessage> i18nMessageContextList =
			 * bundleResolver.getAllLeapLocaleObjects(); if
			 * (!i18nMessageContextList.isEmpty())
			 * localeRegistryService.buildLocaleBundle(i18nMessageContextList);
			 * 
			 * } catch (LocaleResolverException e) { throw new
			 * FeatureDeploymentServiceException("Unable to build the bundles as expected! "
			 * , e); }
			 */
		} else {
			featureDeployment.CheckAndaddFeatureDeployementInCache(configurationContext, true, true, true);
			/*
			 * try { List<LeapI18nMessage> i18nMessageContextList =
			 * bundleResolver.getAllLeapLocaleObjects(); if
			 * (!i18nMessageContextList.isEmpty())
			 * localeRegistryService.buildLocaleBundle(i18nMessageContextList);
			 * 
			 * } catch (LocaleResolverException e) { throw new
			 * FeatureDeploymentServiceException("Unable to build the bundles as expected! "
			 * , e); }
			 */
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

}
