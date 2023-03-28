package com.attunedlabs.scheduler.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.dao.LeapConstants;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.permastore.config.PermaStoreConfigurationUnit;
import com.attunedlabs.scheduler.ScheduledJobData;
import com.attunedlabs.scheduler.ScheduledJobConfigParserException;
import com.attunedlabs.scheduler.ScheduledJobConfigurationException;
import com.attunedlabs.scheduler.config.IScheduledJobConfigurationService;
import com.attunedlabs.scheduler.config.ScheduledJobConfigRequestException;
import com.attunedlabs.scheduler.jaxb.ScheduledJobConfiguration;

public class ScheduledJobConfigurationService implements IScheduledJobConfigurationService {

	Logger logger = LoggerFactory.getLogger(ScheduledJobConfigurationService.class);

	@Override
	public boolean checkScheduledJobConfigarationExistOrNot(ConfigurationContext configurationContext,
			String configName) throws ScheduledJobConfigRequestException {
		String methodName = "checkScheduledJobConfigarationExistOrNot";
		logger.debug("{} entered into the method {},configurationContext ={}, configName ={} ", LEAP_LOG_KEY, methodName,configurationContext,configName);
		boolean isEnabled = false;
		PermaStoreConfigurationUnit pUnit = null;
		RequestContext requestContext = new RequestContext(configurationContext.getTenantId(),
				configurationContext.getSiteId(), configurationContext.getFeatureGroup(),
				configurationContext.getFeatureName(), configurationContext.getImplementationName(),
				configurationContext.getVendorName(), configurationContext.getVersion());

		try {
			// to Get NodeId of Feature
//			int featureNodeId = getApplicableNodeId(requestContext);

			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			int jobId = configPersistenceService.checkSchedulerDatabyName(configName);

			// if confignodedata not Exist
			if (jobId == 0)
				return false;

			/*
			 * isEnabled = configNodeData.isEnabled(); if (isEnabled) { try { pUnit =
			 * getPermaStoreConfiguration(requestContext, configName);
			 * 
			 * if (pUnit == null) { enableAndLoadPermaStroreConfig(requestContext,
			 * configNodeData); } } catch (PermaStoreConfigParserException e) { throw new
			 * PermaStoreConfigurationException(
			 * "Error in Loading the PermastoreConfig to cache with configName = " +
			 * configName + " Request Context = " + requestContext, e); } }
			 */
		} catch (ConfigPersistenceException e) {
			throw new ScheduledJobConfigRequestException("Error in searching SchedulerCongaration with configName = "
					+ configName + " Request Context = " + requestContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return true;
	}

	@Override
	public void addScheduledJobConfiguration(ConfigurationContext configurationContext,
			ScheduledJobConfiguration schedulerConfig) throws ScheduledJobConfigurationException {
		String methodName = "addScheduledJobConfiguration";
		logger.debug("{} entered into the method {}, chedulerConfiguration={} ", LEAP_LOG_KEY, methodName,schedulerConfig);
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
			/*
			 * if ((vendorName != null && !(vendorName.isEmpty()) &&
			 * !(vendorName.equalsIgnoreCase(""))) && (version != null &&
			 * !(version.isEmpty()) && !(version.equalsIgnoreCase("")))) { configNodeId =
			 * getApplicableNodeIdVendorName(tenantId, siteId,
			 * configurationContext.getFeatureGroup(),
			 * configurationContext.getFeatureName(), implName, vendorName, version); } else
			 * { configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId,
			 * configurationContext.getFeatureGroup(),
			 * configurationContext.getFeatureName(), implName); }
			 * logger.debug("Applicable Config Node Id is =" + configNodeId);
			 */

			// Get the type of Configuration Bulder and get the DatatoCache from
			// the
			// Builder
			/*
			 * if (configBuilderHelper == null) {
			 * logger.debug("configBuilderHelper object creation"); configBuilderHelper =
			 * new PermaStoreConfigBuilderHelper(); } logger.debug("configBuilderHelper : "
			 * + configBuilderHelper); Serializable objToCache = configBuilderHelper
			 * .handleConfigurationBuilder(psConfig.getConfigurationBuilder());
			 */

			// Convert configTo Valid XML to store independent inDataBase
			ScheduledJobConfigXMLParser builder = new ScheduledJobConfigXMLParser();
			String xmlString = builder.unmarshallObjecttoXML(schedulerConfig);
			logger.debug("{} xmlString : {} " ,LEAP_LOG_KEY, xmlString);
			JSONObject jobJSON = XML.toJSONObject(xmlString);
			logger.debug("{} jobJSON : {} " ,LEAP_LOG_KEY, jobJSON);
			String key = null;
			JSONObject schedulingExpr = jobJSON.getJSONObject("ScheduledJobConfigurations")
					.getJSONObject("ScheduledJobConfiguration").getJSONObject("SchedulingExpresssion");
			// logger.debug("schedulingExpresssion : "+schedulingExpresssion);
			// JSONObject schedulingExpr = new JSONObject(schedulingExpresssion);
			logger.debug("{} schedulingExpr : {} ",LEAP_LOG_KEY, schedulingExpr);
			Iterator<String> iterator = schedulingExpr.keys();
			while (iterator.hasNext()) {
				key = iterator.next();
			}
			logger.debug("{} key : {}", LEAP_LOG_KEY, key);

			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String createdDTM = formatter.format(Calendar.getInstance().getTime());
			logger.debug("{} createdDTM : {} ",LEAP_LOG_KEY, createdDTM);
			// Update DB for this configuration
			ScheduledJobData jobData = new ScheduledJobData();
			jobData.setJobName(schedulerConfig.getName());
			jobData.setJobService(schedulerConfig.getJobservice());
			jobData.setJobType(key);
			jobData.setFeatureGroup(schedulerConfig.getFeatureInfo().getFeatureGroup());
			jobData.setFeature(schedulerConfig.getFeatureInfo().getFeatureName());
			jobData.setJobContextDetail(schedulerConfig.getJobContextData());
			jobData.setSchedulingExpresssion(schedulingExpr);
			jobData.setCreatedDTM(createdDTM);
			jobData.setEnabled(schedulerConfig.getIsEnabled());
			jobData.setAllApplicableTenant(true);
			jobData.setConcurrent(schedulerConfig.isConcurrent());
			jobData.setAuthenticated(schedulerConfig.isAuthenticated());
			jobData.setRequestedBy(schedulerConfig.getAddedByUser());
			logger.debug("{} jobData : {} " ,LEAP_LOG_KEY, jobData);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			int jobID = configPersistenceService.checkSchedulerDatabyName(schedulerConfig.getName());
			int jobDataId = 0;
			// Check if Configuration already exist in the DataBase or not
			if (jobID == 0) {
				logger.trace("{} jobID : {} " ,LEAP_LOG_KEY, jobID);
				jobDataId = configPersistenceService.insertScheduledJobData(jobData);
				logger.info("{} successfully added schedulerJobData, jobDataId is : {} " ,LEAP_LOG_KEY, jobDataId);
				
			} else {
				throw new ScheduledJobConfigurationException(
						"SchedulerConfiguration already exist for ConfigName=" + schedulerConfig.getName());
			}

			// UpDate Cache for this if config is enabled
			/*
			 * if (!isConfigEnabled) return;
			 */

			/*
			 * PermaStoreConfigurationUnit psConfigUnit = new
			 * PermaStoreConfigurationUnit(tenantId, siteId, configNodeId, isConfigEnabled,
			 * psConfig, objToCache); psConfigUnit.setDbconfigId(configDataId);
			 * loadConfigurationInDataGrid(psConfigUnit);
			 */
		} catch (ConfigPersistenceException | ScheduledJobConfigurationException | ScheduledJobConfigParserException
				| JSONException sqlExp) {
			throw new ScheduledJobConfigurationException(
					"Failed to insert ConfigData in DB for configName=" + schedulerConfig.getName(), sqlExp);
		}
		
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

}
