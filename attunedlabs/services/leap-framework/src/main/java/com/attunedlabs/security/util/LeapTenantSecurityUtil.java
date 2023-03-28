package com.attunedlabs.security.util;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.attunedlabs.security.service.IAccountRetrieveService;
import com.attunedlabs.security.service.impl.AccountRetrieveServiceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * {@link LeapTenantSecurityUtil} is used to get the tenant
 * Id(internal/external).
 * 
 * @author Reactiveworks
 *
 */
public class LeapTenantSecurityUtil {

	private static final Logger log = LoggerFactory.getLogger(LeapTenantSecurityUtil.class);

	/**
	 * This method is used to fetch the internal tenantId by using the external
	 * tenant and the respective site.
	 * 
	 * @param externalTenatId
	 * @param siteId
	 * @return internalTenantId
	 * @throws AccountFetchException
	 */
	public static String getInternalTenantId(String externalTenatId, String siteId) throws AccountFetchException {
		String methodName = "getInternalTenantId";
		log.debug("{} entered into the method {} for tenant : {}, site : {}", LEAP_LOG_KEY, methodName, externalTenatId,
				siteId);
		AccountConfiguration configuration = getAccountConfigurationByExternalTenantId(externalTenatId, siteId);
		String internalTenantId = configuration.getInternalTenantId();
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return internalTenantId;
	}// end of the method.

	/**
	 * This method is used to fetch the external tenantId by using the internal
	 * tenant and the respective site.
	 * 
	 * @param internalTenatId
	 * @param siteId
	 * @return externalTenantId
	 * @throws AccountFetchException
	 */
	public static String getExternalTenantId(String internalTenatId, String siteId) throws AccountFetchException {
		String methodName = "getExternalTenantId";
		log.debug("{} entered into the method {} for tenant : {}, site : {}", LEAP_LOG_KEY, methodName, internalTenatId,
				siteId);
		AccountConfiguration configuration = getAccountConfigurationByInternalTenantId(internalTenatId, siteId);
		String accountName = configuration.getAccountName();
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return accountName;
	}// end of the method.

	/**
	 * This method is used to fetch the {@link AccountConfiguration} by using the
	 * external tenant and the respective site.
	 * 
	 * @param externalTenatId
	 * @param siteId
	 * @return {@link AccountConfiguration}
	 * @throws AccountFetchException
	 */
	public static AccountConfiguration getAccountConfigurationByExternalTenantId(String externalTenatId, String siteId)
			throws AccountFetchException {
		String methodName = "getAccountConfigurationByExternalTenantId";
		log.debug("{} entered into the method {} for tenant : {}, site : {}", LEAP_LOG_KEY, methodName, externalTenatId,
				siteId);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, AccountConfiguration> configurations = hazelcastInstance
				.getMap(TenantSecurityConstant.EXTERNAL_ACCOUNT_CONFIG);
		AccountConfiguration configuration = configurations
				.get(externalTenatId + TenantSecurityConstant.FEATURE_SEPERATOR + siteId);
		if (configuration == null) {
			IAccountRetrieveService retrieveService = new AccountRetrieveServiceImpl();
			AccountConfiguration detailsConfiguration = retrieveService.getAccountDetailConfiguration(externalTenatId);
			if (detailsConfiguration != null) {
				IMap<String, AccountConfiguration> internalConfigMap = hazelcastInstance
						.getMap(TenantSecurityConstant.INTERNAL_ACCOUNT_CONFIG);
				configurations.put(externalTenatId + TenantSecurityConstant.FEATURE_SEPERATOR + siteId,
						detailsConfiguration);
				internalConfigMap.put(detailsConfiguration.getInternalTenantId()
						+ TenantSecurityConstant.FEATURE_SEPERATOR + detailsConfiguration.getInternalSiteId(),
						detailsConfiguration);
				return detailsConfiguration;
			} else
				throw new AccountFetchException(
						siteId + " is not available under the given account : " + externalTenatId);
		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configuration;
	}// end of the method.

	/**
	 * This method is used to fetch the {@link AccountConfiguration} by using the
	 * internal tenant and the respective site.
	 * 
	 * @param internalTenatId
	 * @param siteId
	 * @return {@link AccountConfiguration}
	 * @throws AccountFetchException
	 */
	public static AccountConfiguration getAccountConfigurationByInternalTenantId(String internalTenatId, String siteId)
			throws AccountFetchException {
		String methodName = "getAccountConfigurationByInternalTenantId";
		log.debug("{} entered into the method {} for tenant : {}, site : {}", LEAP_LOG_KEY, methodName, internalTenatId,
				siteId);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, AccountConfiguration> internalConfigMap = hazelcastInstance
				.getMap(TenantSecurityConstant.INTERNAL_ACCOUNT_CONFIG);
		AccountConfiguration configuration = internalConfigMap
				.get(internalTenatId + TenantSecurityConstant.FEATURE_SEPERATOR + siteId);
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configuration;
	}// end of the method.

	/**
	 * This method is used to fetch the {@link AccountConfiguration} by using the
	 * domain.
	 * 
	 * @param domainName
	 * @return {@link AccountConfiguration}
	 */
	public static AccountConfiguration getAccountConfigurationByDomin(String domainName) {
		String methodName = "getAccountConfigurationByDomin";
		log.debug("{} entered into the method {} for deomain : {}", LEAP_LOG_KEY, methodName, domainName);
		HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
		IMap<String, AccountConfiguration> configurationMap = hazelcastInstance
				.getMap(TenantSecurityConstant.EXTERNAL_ACCOUNT_CONFIG);
		Set<Entry<String, AccountConfiguration>> entrySet = configurationMap.entrySet();
		for (Entry<String, AccountConfiguration> configurations : entrySet) {
			AccountConfiguration configuration = configurations.getValue();
			log.debug("{} : config.domain:domain -> {}:{} ", LEAP_LOG_KEY, configuration.getDomain(), domainName);
			if (configuration.getDomain().equals(domainName)) {
				log.trace("{} Matching domain configuration : {}", LEAP_LOG_KEY, configuration);
				log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				return configuration;
			}
		}
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return null;
	}
}
