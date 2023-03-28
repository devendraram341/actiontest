package com.attunedlabs.featuredeployment.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuremetainfo.util.FeatureMetaInfoResourceUtil;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapServiceContext;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class FeatureDeploymentService implements IFeatureDeployment {

	final Logger logger = LoggerFactory.getLogger(FeatureDeploymentService.class);

	/**
	 * This method is used to add new feature deployment detail
	 * 
	 * @param configContext : Configuration Context Object
	 * @param ImplName      : Implementation name
	 * @param isActive      : feature is active(true/false)
	 * @param isPrimary     : feature is primary feature (true/false)
	 * @param isCustomized  : feature is customizable (true/false)
	 * @throws FeatureDeploymentServiceException
	 */
	public void addFeatureDeployement(ConfigurationContext configContext, boolean isActive, boolean isPrimary,
			boolean isCustomized) throws FeatureDeploymentServiceException {
		String methodName = "addFeatureDeployement";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int featureMasterId = 0;
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.trace("{} feature master id : {}", LEAP_LOG_KEY, featureMasterId);
			if (featureMasterId != 0) {
				FeatureDeployment featureDeployment = new FeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion(), isActive, isPrimary, isCustomized,
						configContext.getProvider(), configContext.getVendorTaxonomyId());
				FeatureDeployment isInsertedFeatureDeployment = configPersistenceService
						.insertFeatureDeploymentDetails(featureDeployment);
				if (isInsertedFeatureDeployment.getId() != 0) {
					addfeatureDeploymentInCache(isInsertedFeatureDeployment, siteNodeId);
				}
			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to add feature with config : " + configContext + " in feature deployemnt  is active : "
							+ isActive + ", isPrimary : " + isPrimary + ", is customizable: " + isCustomized,
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of method addFeatureDeployement

	/**
	 * This method is used to add new feature deployment detail
	 * 
	 * @param configContext : Configuration Context Object
	 * @param ImplName      : Implementation name
	 * @param isActive      : feature is active(true/false)
	 * @param isPrimary     : feature is primary feature (true/false)
	 * @param isCustomized  : feature is customizable (true/false)
	 * @throws FeatureDeploymentServiceException
	 */
	public void CheckAndaddFeatureDeployementInCache(ConfigurationContext configContext, boolean isActive,
			boolean isPrimary, boolean isCustomized) throws FeatureDeploymentServiceException {
		String methodName = "CheckAndaddFeatureDeployementInCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int featureMasterId = 0;
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.trace("{} feature master id : {}", LEAP_LOG_KEY, featureMasterId);
			if (featureMasterId != 0) {
				FeatureDeployment featureDeployment = configPersistenceService.getFeatureDeploymentDetails(
						featureMasterId, configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion());
				if (featureDeployment == null) {
					featureDeployment = new FeatureDeployment(featureMasterId, configContext.getFeatureName(),
							configContext.getImplementationName(), configContext.getVendorName(),
							configContext.getVersion(), isActive, isPrimary, isCustomized);
					FeatureDeployment isInsertedFeatureDeployment = configPersistenceService
							.insertFeatureDeploymentDetails(featureDeployment);
					if (isInsertedFeatureDeployment.getId() != 0) {
						featureDeployment.setProvider(configContext.getProvider());
						addfeatureDeploymentInCache(featureDeployment, siteNodeId);
					}
				} else {
					featureDeployment.setProvider(configContext.getProvider());
					addfeatureDeploymentInCache(featureDeployment, siteNodeId);
				}

			}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to add feature with config : " + configContext + " in feature deployemnt  is active : "
							+ isActive + ", isPrimary : " + isPrimary + ", is customizable: " + isCustomized,
					e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method addFeatureDeployement

	/**
	 * This method is used to get the featureDeployment deatiled based on
	 * featureName,impl name, vendor and version
	 * 
	 * @param configContext : Configuration Object
	 * @param implName      : Implementation Name
	 * @return
	 * @throws FeatureDeploymentServiceException
	 */
	public FeatureDeployment getFeatureDeployedDeatils(ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		String methodName = "getFeatureDeployedDeatils";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		FeatureDeployment featureDeployment = null;
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			logger.trace("{} siteNodeId: {}", LEAP_LOG_KEY, siteNodeId);
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("{} feature master id : {}", LEAP_LOG_KEY, featureMasterId);
			if (featureMasterId != 0) {
				featureDeployment = configPersistenceService.getFeatureDeploymentDetails(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion());
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return featureDeployment;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to get feature with config : " + configContext + " in feature deployemnt ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return featureDeployment;

	}// end of method getFeatureDeployedDeatils

	/**
	 * This method is used to delete feature deployed for specific
	 * featurename,implementation name,vendor name, version name
	 * 
	 * @param configContext : Configuration Context object
	 * @param implName      : Implementation Name
	 * @return isDeleted : boolean
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean deleteFeatureDeployed(ConfigurationContext configContext) throws FeatureDeploymentServiceException {
		String methodName = "deleteFeatureDeployed";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("{} feature master id : {}", LEAP_LOG_KEY, featureMasterId);
			if (featureMasterId != 0) {

				boolean isDeleted = configPersistenceService.deleteFeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion());
				if (isDeleted) {
					deleteFeatureDeploymentFromCache(configContext, siteNodeId);
				}
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return isDeleted;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException(
					"Unable to delete feature with config : " + configContext + " in feature deployemnt ");
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;

	}// end of method deleteFeatureDeployed

	/**
	 * This method is used to delete feature deployed for specific
	 * featurename,implementation name,vendor name, version name
	 * 
	 * @param configContext : Configuration Context object
	 * @param implName      : Implementation Name
	 * @return isDeleted : boolean
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean updateFeatureDeployed(ConfigurationContext configContext, boolean isPrimary, boolean isActive)
			throws FeatureDeploymentServiceException {
		String methodName = "updateFeatureDeployed";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int featureMasterId = 0;
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			featureMasterId = configPersistenceService.getFeatureMasterIdByFeatureAndFeaturegroup(
					configContext.getFeatureName(), configContext.getFeatureGroup(), configContext.getVersion(),
					siteNodeId);
			logger.debug("{} feature master id : {}", LEAP_LOG_KEY, featureMasterId);
			if (featureMasterId != 0) {
				boolean isUpdated = configPersistenceService.updateFeatureDeployment(featureMasterId,
						configContext.getFeatureName(), configContext.getImplementationName(),
						configContext.getVendorName(), configContext.getVersion(), isPrimary, isActive);
				logger.info("{} updated feature deployment: {}", LEAP_LOG_KEY, isUpdated);
				if (isUpdated) {
					updateFeatureDeploymentFromCache(configContext, isPrimary, isActive, siteNodeId);
				}
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return isUpdated;
			}
		} catch (ConfigPersistenceException | InvalidNodeTreeException e) {
			throw new FeatureDeploymentServiceException("Unable to update feature with config : " + configContext
					+ " in feature deployemnt with is primary :  " + isPrimary);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return false;

	}// end of method updateFeatureDeployed

	/**
	 * This method is used to find out if feature is already deployed irrespective
	 * of its implementation
	 * 
	 * @param configContext : Configuration Context Object
	 * @return true if exist else false
	 * @throws FeatureDeploymentServiceException
	 */
	public boolean checkIfFeatureIsAlreadyDeployed(ConfigurationContext configContext)
			throws FeatureDeploymentServiceException {
		String methodName = "checkIfFeatureIsAlreadyDeployed";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		boolean alreadyExist = false;
		List<FeatureDeployment> copyOfFeatureDeployment = new ArrayList<>();
		try {
			int siteNodeId = getApplicableNodeIdForSite(configContext.getTenantId(), configContext.getSiteId());
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
			logger.trace("{} cacheMapKey : {}, ConfigurationContext : {} ", LEAP_LOG_KEY, cacheMapKey, configContext);
			List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
			if (featureDeploymentList != null && !(featureDeploymentList.isEmpty())
					&& featureDeploymentList.size() > 0) {
				logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return true;
			}

		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException("Unable to check feature is already deployed with config : "
					+ configContext + " in feature deployemnt  ");

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return alreadyExist;

	}

	private void updateFeatureDeploymentFromCache(ConfigurationContext configContext, boolean isPrimary,
			boolean isActive, int siteNodeId) {
		String methodName = "updateFeatureDeploymentFromCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
		logger.trace("{} cacheMapKey : {} , ConfigurationContext : {}", LEAP_LOG_KEY, cacheMapKey, configContext);
		List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (featureDeploymentList != null && !(featureDeploymentList.isEmpty())) {
			for (FeatureDeployment featureDeployment : featureDeploymentList) {
				if (featureDeployment.getFeatureName().equalsIgnoreCase(configContext.getFeatureName())
						&& featureDeployment.getVendorName().equalsIgnoreCase(configContext.getVendorName())
						&& featureDeployment.getImplementationName()
								.equalsIgnoreCase(configContext.getImplementationName())
						&& featureDeployment.getFeatureVersion().equalsIgnoreCase(configContext.getVersion())) {
					featureDeployment.setPrimary(isPrimary);
					featureDeployment.setActive(isActive);
					logger.info("{} featureDeploymentList after update : {}", LEAP_LOG_KEY, featureDeploymentList);
					map.put(cacheMapKey, (Serializable) featureDeploymentList);
				}
			}
		} // end of if
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method updateFeatureDeploymentFromCache

	/**
	 * This method is use get the deployed feature for the tenant , site and feature
	 * name based on given provider id
	 * 
	 * @param tenant             : Name of the tenant
	 * @param site               : Name of the Site
	 * @param featureName        : Name of the feature name
	 * @param provider           : Name of the provider
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link FeatureDeployment} - Instance of feature deployed whose
	 *         provider was given
	 * @throws FeatureDeploymentServiceException
	 */
	public FeatureDeployment getActiveAndPrimaryFeatureDeployedFromCache(String tenant, String site, String featureName,
			String provider, LeapServiceContext leapServiceContext) throws FeatureDeploymentServiceException {
		String methodName = "getActiveAndPrimaryFeatureDeployedFromCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(tenant, site);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			logger.trace("{} Map getActiveAndPrimaryFeatureDeployedFromCache with provider : {}", LEAP_LOG_KEY, map);
			String cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
			logger.debug("{} cacheMapKey : {}, tenant  :{}, site : {},, feature Name : {},, siteNodeId :{}",
					LEAP_LOG_KEY, cacheMapKey, tenant, site, featureName, siteNodeId);
			List<FeatureDeployment> featureDeploymentList = null;
			// Whenever we are onbording the new tenant and in case we are
			// sending multiple concurrent request for the tenant , we were
			// getting issue of duplicate config loading.
			synchronized (this) {
				logger.trace("{} inisde synchronized...{}", LEAP_LOG_KEY, Thread.currentThread().getName());
				featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
				logger.debug("{} feature deployment list : {}", LEAP_LOG_KEY, featureDeploymentList);
				logger.debug("{} --------------------------------", LEAP_LOG_KEY);
				if (featureDeploymentList == null) {

					Pattern pattern = Pattern.compile("featureMetaInfo.xml");
					FeatureMetaInfoResourceUtil fmiResList = new FeatureMetaInfoResourceUtil(
							leapServiceContext.getTenant(), leapServiceContext.getSite(),
							leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(), null, null,
							null);
					fmiResList.getClassPathResources(pattern);
					map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
					cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
					featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
				}
			}

			logger.debug("{} feature deployment list : {}", LEAP_LOG_KEY, featureDeploymentList);
			logger.debug("{} --------------------------------", LEAP_LOG_KEY);
			if (featureDeploymentList != null)
				for (FeatureDeployment featureDeployment : featureDeploymentList) {
					logger.trace("{} feature deployemnt : {}", LEAP_LOG_KEY, featureDeployment);
					if (featureDeployment.getFeatureName().equalsIgnoreCase(featureName)) {
						logger.trace(
								"{} feature name : {}, impl name : {}, active : {}, feature deployemnt primary : {}",
								LEAP_LOG_KEY, featureName, featureDeployment.getImplementationName(),
								featureDeployment.isActive(), featureDeployment.isPrimary());
						if (featureDeployment.isActive() && featureDeployment.getProvider().equals(provider)) {
							logger.info("{} feature deployment : {}", LEAP_LOG_KEY, featureDeployment);
							logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
							return featureDeployment;
						}
					}
				}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;

	}

	/**
	 * This method is use get the deployed feature for the tenant , site and feature
	 * name based on one which is active and primary
	 * 
	 * @param tenant             : Name of the tenant
	 * @param site               : Name of the Site
	 * @param featureName        : Name of the feature name
	 * @param leapServiceContext : {@link LeapServiceContext}
	 * @return {@link FeatureDeployment} - Instance of feature deployed which is
	 *         active and primary
	 * @throws FeatureDeploymentServiceException
	 */
	public FeatureDeployment getActiveAndPrimaryFeatureDeployedFromCache(String tenant, String site, String featureName,
			LeapServiceContext leapServiceContext) throws FeatureDeploymentServiceException {
		String methodName = "getActiveAndPrimaryFeatureDeployedFromCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int siteNodeId;
		try {
			siteNodeId = getApplicableNodeIdForSite(tenant, site);
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
			String cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
			logger.trace("{} cacheMapKey : {}, tenant  : {}, site : {}, feature Name : {}, siteNodeId : {}",
					LEAP_LOG_KEY, cacheMapKey, tenant, site, featureName, siteNodeId);
			List<FeatureDeployment> featureDeploymentList = null;

			// Whenever we are onbording the new tenant and in case we are
			// sending multiple concurrent request for the tenant , we were
			// getting issue of duplicate config loading.
			synchronized (this) {
				logger.trace("{} inisde synchronized...{}", LEAP_LOG_KEY, Thread.currentThread().getName());
				featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
				logger.debug("{} feature deployment list : {}", LEAP_LOG_KEY, featureDeploymentList);
				logger.debug("{} --------------------------------", LEAP_LOG_KEY);
				if (featureDeploymentList == null) {

					Pattern pattern = Pattern.compile("featureMetaInfo.xml");
					FeatureMetaInfoResourceUtil fmiResList = new FeatureMetaInfoResourceUtil(
							leapServiceContext.getTenant(), leapServiceContext.getSite(),
							leapServiceContext.getFeatureGroup(), leapServiceContext.getFeatureName(), null, null,
							null);
					fmiResList.getClassPathResources(pattern);
					map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
					cacheMapKey = createKeyForFeatureNameAndSiteId(featureName, siteNodeId);
					featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
				}
			}
			if (featureDeploymentList != null)
				for (FeatureDeployment featureDeployment : featureDeploymentList) {
					logger.debug("{} feature deployemnt : {}", LEAP_LOG_KEY, featureDeployment);
					if (featureDeployment.getFeatureName().equalsIgnoreCase(featureName)) {
						logger.trace(
								"{} feature name : {}, impl name :{}, active : {}, feature deployemnt primary : {}",
								LEAP_LOG_KEY, featureName, featureDeployment.getImplementationName(),
								featureDeployment.isActive(), featureDeployment.isPrimary());
						if (featureDeployment.isActive() && featureDeployment.isPrimary()) {
							logger.info("{} feature deployment : {}", LEAP_LOG_KEY, featureDeployment);
							logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
							return featureDeployment;
						}
					}
				}
		} catch (InvalidNodeTreeException | ConfigPersistenceException e) {
			throw new FeatureDeploymentServiceException();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;

	}// end of getActiveAndPrimaryFeatureDeployedFromCache

	/**
	 * This method is used to delete feature deployment detail from cache
	 * 
	 * @param configContext : Configuration context Object
	 * @param siteNodeId    : site id in int
	 */
	private void deleteFeatureDeploymentFromCache(ConfigurationContext configContext, int siteNodeId) {
		String methodName = "deleteFeatureDeploymentFromCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<FeatureDeployment> copyOfFeatureDeployment = new ArrayList<>();
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(configContext.getFeatureName(), siteNodeId);
		logger.trace("{} cacheMapKey : {}, ConfigurationContext : {}", LEAP_LOG_KEY, cacheMapKey, configContext);
		List<FeatureDeployment> featureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (featureDeploymentList != null && !(featureDeploymentList.isEmpty())) {
			for (FeatureDeployment featureDeployment : featureDeploymentList) {
				if (featureDeployment.getFeatureName().equalsIgnoreCase(configContext.getFeatureName())
						&& featureDeployment.getVendorName().equalsIgnoreCase(configContext.getVendorName())
						&& featureDeployment.getImplementationName()
								.equalsIgnoreCase(configContext.getImplementationName())
						&& featureDeployment.getFeatureVersion().equalsIgnoreCase(configContext.getVersion())) {
					copyOfFeatureDeployment.add(featureDeployment);
				}
			}
		} // end of if
		featureDeploymentList.removeAll(copyOfFeatureDeployment);
		logger.info("feature deployment after removal : " + featureDeploymentList);
		map.put(cacheMapKey, (Serializable) featureDeploymentList);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of method deleteFeatureDeploymentFromCache

	/**
	 * This method is used to add feature deployment detail in cache
	 * 
	 * @param featureDeployment : FeatureDeployment Object need to be cached
	 * @param siteId            : site id in int
	 */
	private void addfeatureDeploymentInCache(FeatureDeployment featureDeployment, int siteId) {
		String methodName = "addfeatureDeploymentInCache";
		logger.debug("{} entered into the method {}, featureDeployement :{}", LEAP_LOG_KEY, methodName,
				featureDeployment);
		logger.debug("{} featureDeployment in add feature :{} " , LEAP_LOG_KEY, featureDeployment);
		List<FeatureDeployment> newfeatureDeploymentList = new ArrayList<>();
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureDeploymentKey());
		String cacheMapKey = createKeyForFeatureNameAndSiteId(featureDeployment.getFeatureName(), siteId);
		logger.trace("{} cacheMapKey : {}, featureDeployment : {}", LEAP_LOG_KEY, cacheMapKey, featureDeployment);
		List<FeatureDeployment> existingFeatureDeploymentList = (List<FeatureDeployment>) map.get(cacheMapKey);
		if (existingFeatureDeploymentList != null) {
			logger.trace("{} feature deployment existing list in add {} " , LEAP_LOG_KEY, existingFeatureDeploymentList);
			existingFeatureDeploymentList.add(featureDeployment);
			map.put(cacheMapKey, (Serializable) existingFeatureDeploymentList);
		} else {
			logger.trace("feature deployment new list in add {} {}" , LEAP_LOG_KEY, newfeatureDeploymentList);
			newfeatureDeploymentList.add(featureDeployment);
			map.put(cacheMapKey, (Serializable) newfeatureDeploymentList);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of method addfeatureDeploymentInCache

	private String createKeyForFeatureNameAndSiteId(String featureName, int siteId) {
		return (featureName + "-" + siteId).trim();
	}// end of method createKeyForFeatureNameAndSiteId

	private String getGlobalFeatureDeploymentKey() {
		return "GlobalFeatureDeployment".trim();
	}// end of method getGlobalFeatureDeploymentKey

	/**
	 * to get siteNodId By tenant name,site name,feature group
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @return siteNodeID
	 * @throws InvalidNodeTreeException
	 * @throws ConfigPersistenceException
	 */

	private Integer getApplicableNodeIdForSite(String tenantId, String siteId)
			throws InvalidNodeTreeException, ConfigPersistenceException {
		logger.debug("{} Finding ParentNodeId for Tenant= {}, siteId={}", LEAP_LOG_KEY, tenantId, siteId);

		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		return configPersistenceService.getApplicableNodeId(tenantId, siteId);
	}//

}
