package com.attunedlabs.config.persistence.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.persistence.PrettyUrlMapping;
import com.attunedlabs.config.persistence.dao.PrettyURLMappingDao;
import com.attunedlabs.config.persistence.exception.PrettyUrlMappingDaoException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * This class deals with prettyUrl Mapping
 * 
 * @author Reactiveworks
 *
 */
public class PrettyUrlMappingService {
	private PrettyURLMappingDao prettyUrlDao = new PrettyURLMappingDao();
	private final Logger logger = LoggerFactory.getLogger(PrettyUrlMappingService.class);

	/**
	 * This method is used to add pretty url mapping into DataBase and also into
	 * hazelcast </br>
	 * </br>
	 * 
	 * @param prettyUrlMapping
	 * @throws PrettyUrlMappingServiceException
	 */
	public void addPrettyUrlMappingInDBAndCache(PrettyUrlMapping prettyUrlMapping)
			throws PrettyUrlMappingServiceException {
		try {
			String methodName = "addPrettyUrlMappingInDBAndCache";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			if (!isMappingExist(prettyUrlMapping)) {
				prettyUrlDao.addPrettyUrlMapping(prettyUrlMapping);
				addPrettyUrlMappingInCache(prettyUrlMapping);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (PrettyUrlMappingDaoException e) {
			logger.error("{} unable to add mapping::{}", LEAP_LOG_KEY, e.getMessage());
			throw new PrettyUrlMappingServiceException(e.getMessage(), e);
		}
	}

	/**
	 * This method is used to add mapping into Hazelcast in following format </br>
	 * <p>
	 * <i>GlobalFeatureService={tenantId={siteId={prettyString=actualString}}}</i>
	 * </p>
	 * 
	 * @param prettyUrlMapping
	 */
	private void addPrettyUrlMappingInCache(PrettyUrlMapping prettyUrlMapping) {
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureServiceKey());
		Map<String, Serializable> siteMap = new HashMap<>();
		Map<String, String> prettyUriMap = new HashMap<>();
		prettyUriMap.put(prettyUrlMapping.getPrettyString(), prettyUrlMapping.getActualString());
		siteMap.put(prettyUrlMapping.getSiteId(), (Serializable) prettyUriMap);
		map.put(prettyUrlMapping.getTenantId(), (Serializable) siteMap);

	}

	/**
	 * This method is used check whether mapping exist or not in database
	 * table(prettyurlmapping)
	 * 
	 * @param prettyuri
	 * @return - returns true if mapping exist, returns false if mapping doesnt
	 *         exists
	 * @throws PrettyUrlMappingServiceException
	 * @throws PrettyUrlMappingDaoException
	 */
	private boolean isMappingExist(PrettyUrlMapping prettyuri)
			throws PrettyUrlMappingServiceException, PrettyUrlMappingDaoException {
		PrettyUrlMapping prettyUrlMapping = prettyUrlDao.getPrettyUrlMappingByPrettyUrlString(prettyuri);
		if (prettyUrlMapping == null) {
			return false;
		}
		return true;
	}

	/**
	 * This method is used to get verbose url or actual url from Hazelcast
	 * 
	 * @param prettyuri - pretty uri string
	 * @param tenantId  - tenant id
	 * @param siteId    -site id
	 * @return - return actual uri or verbose uri
	 * @throws PrettyUrlMappingServiceException
	 */
	@SuppressWarnings("unchecked")
	public String getVerbosUrlFromCache(String prettyuri, String tenantId, String siteId)
			throws PrettyUrlMappingServiceException {
		try {
			String methodName = "getVerbosUrlFromCache";
			logger.debug("{} entered into the method {} PrettyUrl {} tenantId {} siteId {}", LEAP_LOG_KEY, methodName,
					prettyuri, tenantId, siteId);
			nullOrEmptyCheck(prettyuri, "prettyuri cannnot be empty or null");
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, Serializable> map = hazelcastInstance.getMap(getGlobalFeatureServiceKey());
			Map<String, Serializable> siteMap = (Map<String, Serializable>) map.get(tenantId);
			Map<String, Serializable> prettyStringMap = (Map<String, Serializable>) siteMap.get(siteId);
			String actualUrl = (String) prettyStringMap.get(prettyuri);
			nullOrEmptyCheck(actualUrl,
					"unable to find mapping for '" + prettyuri + "'. please check you pretty string uri");
			return actualUrl;
		} catch (PrettyUrlMappingServiceException e) {
			logger.error("{} {}", LEAP_LOG_KEY, e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("{} unable to get pretty url mapping {}", LEAP_LOG_KEY, e.getMessage());
			throw new PrettyUrlMappingServiceException(
					"unable to get pretty url mapping for give string '" + prettyuri + "': " + e.getMessage());
		}
	}

	/**
	 * This method is used to load database table data into hazelcast
	 * 
	 * @throws PrettyUrlMappingServiceException
	 */
	public void checkAndLoadeTableDataIntoHC() throws PrettyUrlMappingServiceException {
		try {
			List<PrettyUrlMapping> allPrettyUrlMappings = prettyUrlDao.getAllPrettyUrlMappings();
			logger.info("{} loading prettyUrlMapping table data into Hazelcast, table size:: {}", LEAP_LOG_KEY,
					allPrettyUrlMappings.size());
			for (PrettyUrlMapping singleMapping : allPrettyUrlMappings) {
				addPrettyUrlMappingInCache(singleMapping);
			}
		} catch (PrettyUrlMappingDaoException e) {
			logger.error("unable to load pretty url mapping into Hazelcast {} {}" , LEAP_LOG_KEY, e.getMessage());
			throw new PrettyUrlMappingServiceException(
					"unable to load pretty url mapping into Hazelcast " + e.getMessage());
		}
	}

	private void nullOrEmptyCheck(String string, String message) throws PrettyUrlMappingServiceException {
		if (string == null || string.isEmpty()) {
			logger.error("{} {}", LEAP_LOG_KEY,message);
			throw new PrettyUrlMappingServiceException(message);
		}
	}

	private static String getGlobalFeatureServiceKey() {
		return "GlobalFeatureService".trim();
	}
}
