package com.attunedlabs.config.server;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.config.event.LeapConfigurationListener;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.featuremetainfo.jaxb.Feature;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.security.TenantSecurityConstant;
import com.attunedlabs.security.pojo.AccountConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;

public class LeapConfigurationServer {
	protected static final Logger logger = LoggerFactory.getLogger(LeapConfigurationServer.class);

	private static LeapConfigurationServer configService;

	private HazelcastInstance hazelcastInstance;
	/** #TODO Refactor and Remove this Map not Needed */
	IMap<String, String> configGroupKeys = null;

	/** Singleton method **/
	public static LeapConfigurationServer getConfigurationService() throws ConfigServerInitializationException {
		if (configService == null) {
			synchronized (LeapConfigurationServer.class) {
				try {
					String methodName = "getConfigurationService";
					logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
					HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
					IMap<String, String> configGroupKeys = hazelcastInstance
							.getMap(ConfigurationConstant.CONFIG_GROUP_KEYS);
					configService = new LeapConfigurationServer(hazelcastInstance, configGroupKeys);
					logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
				} catch (Exception exp) {
					logger.error("{} LeapConfigurationServer initializatiob error {}", LEAP_LOG_KEY, exp);
					throw new ConfigServerInitializationException("Failed to initialize the ConfigServer", exp);
				}
			}
		}
		return configService;
	}// end of singleton method

	// public HazelcastInstance getHazelcastInstance(){
	// return hazelcastInstance;
	// }

	/**
	 * loadLoggingFeatureInDataGrid to enable and disabling notifier.
	 * 
	 */
	public void loadLoggingFeatureInDataGrid(Feature feature, String featureGroupName) {
		String methodName = "loadLoggingFeatureInDataGrid";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IList<String> listcontexts = hazelcastInstance.getList(ConfigurationConstant.LOGGING_FEATURES_KEY);
		String loggingFeature = featureGroupName + ConfigurationConstant.FEATURE_SEPERATOR + feature.getName();
		if (!listcontexts.contains(loggingFeature))
			listcontexts.add(loggingFeature);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * Gives me info whether feature is logging or not.
	 * 
	 * @param featureGroup
	 * @param featureName
	 * @return
	 */
	public boolean isLoggingFeaturePresentInDataGrid(String featureGroup, String featureName) {
		String methodName = "isLoggingFeaturePresentInDataGrid";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IList<String> loggingFeatures = hazelcastInstance.getList(ConfigurationConstant.LOGGING_FEATURES_KEY);
		String loggingFeature = featureGroup + ConfigurationConstant.FEATURE_SEPERATOR + featureName;
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return loggingFeatures.contains(loggingFeature);

	}

	/**
	 * creates the new object for performance logging.
	 * 
	 * @param requestUUID
	 * @return
	 */
	public ComponentPerformance getComponentPerformanceFeatureCache(String requestUUID) {
		String methodName = "getComponentPerformanceFeatureCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, ComponentPerformance> featurePerformanceLogMap = hazelcastInstance
				.getMap(ConfigurationConstant.FEATURE_PERFORMANCE_LOGS_KEY);
		if (featurePerformanceLogMap != null && !(featurePerformanceLogMap.isEmpty()))
			if (!featurePerformanceLogMap.containsKey(requestUUID)) {
				ComponentPerformance componentPerformance = new ComponentPerformance(requestUUID);
				featurePerformanceLogMap.put(requestUUID, componentPerformance);
			} else
				logger.debug("{} featurePerformanceLogMap doesnt contain data with request id :{} ", LEAP_LOG_KEY,
						requestUUID);
		else
			logger.debug("{} featurePerformanceLogMap is null", LEAP_LOG_KEY);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return featurePerformanceLogMap.get(requestUUID);
	}

	/**
	 * replace the same key with new value.
	 * 
	 * @param componentPerformanceFeatureCache
	 */
	public void addComponentToPerformanceCache(ComponentPerformance componentPerformanceFeatureCache) {
		String methodName = "addComponentToPerformanceCache";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, ComponentPerformance> featurePerformanceLogMap = hazelcastInstance
				.getMap(ConfigurationConstant.FEATURE_PERFORMANCE_LOGS_KEY);
		if (featurePerformanceLogMap != null && !(featurePerformanceLogMap.isEmpty()))
			featurePerformanceLogMap.replace(componentPerformanceFeatureCache.getRequestUUID(),
					componentPerformanceFeatureCache);
		else
			logger.debug("{} No feature performance log key found in cache", LEAP_LOG_KEY);

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * loading all configuration context in datagrid.
	 * 
	 * 
	 */
	public void loadFeatureInDataGrid(Feature feature, String featureGroupName) {
		ConfigurationContext configContext = new ConfigurationContext(LeapHeaderConstant.tenant,
				LeapHeaderConstant.site, featureGroupName, feature.getName(), feature.getImplementationName(),
				feature.getVendorName(), feature.getVendorVersion());
		logger.trace("{} inside loadFeatureInDataGrid to load context :{} ", LEAP_LOG_KEY, configContext);
		IList<String> listcontexts = hazelcastInstance.getList(ConfigurationConstant.ALL_SUBSCRIPTION_CONFIG_KEY);
		if (!listcontexts.contains(configContext.toString()))
			listcontexts.add(configContext.toString());

	}

	/**
	 * gives all the config-context configured.
	 * 
	 * @return
	 */
	public List<ConfigurationContext> getAllConfigContext() {
		String methodName = "getAllConfigContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IList<String> listStringContexts = hazelcastInstance.getList(ConfigurationConstant.ALL_SUBSCRIPTION_CONFIG_KEY);

		List<ConfigurationContext> configurationContexts = new ArrayList<>();
		ConfigurationContext configurationContext = null;
		if (listStringContexts != null)
			for (String jsonContext : listStringContexts) {
				configurationContext = getConfigContextFromJson(jsonContext);
				if (configurationContext != null) {
					configurationContexts.add(configurationContext);
					configurationContext = null;
				}
			}
		logger.trace("{} list of all context {}", LEAP_LOG_KEY, configurationContexts);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configurationContexts;
	}

	/**
	 * build the configuration context based on the request context passed from the
	 * json body.
	 * 
	 * @param eventBody consumed data.
	 * @return {@link ConfigurationContext}
	 */
	protected ConfigurationContext getConfigContextFromJson(String jsonContext) {
		ConfigurationContext configurationContext = null;
		try {
			configurationContext = new ObjectMapper().readValue(jsonContext, ConfigurationContext.class);
			logger.trace("{} config context getConfigContextFromEventBody..{} ", LEAP_LOG_KEY, configurationContext);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("{} unable to get requestCtx  from event body...{}", LEAP_LOG_KEY, e);
		}
		return configurationContext;
	}

	/**
	 * loading subscribers for particular topic to dataGrid.
	 * 
	 * @param subscriptionId
	 * @param subscribeTopics
	 */
	public void loadSubscribersByTopicName(String subscriptionId, String subscribeTopics) {
		String methodName = "loadSubscribersByTopicName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, Set<String>> topicMap = hazelcastInstance.getMap(ConfigurationConstant.TOPIC_SUBSCRIBERS_KEYS);
		if (!subscribeTopics.isEmpty()) {
			String[] topics = subscribeTopics.split(",");
			List<String> topicsParticularSubscriber = Arrays.asList(topics);
			if (topicsParticularSubscriber != null)
				for (String topicName : topicsParticularSubscriber) {
					Set<String> subscriberAlreadyRegistered = topicMap.get(topicName);
					if (subscriberAlreadyRegistered == null) {
						subscriberAlreadyRegistered = new HashSet<String>();
						subscriberAlreadyRegistered.add(subscriptionId);
					} else
						subscriberAlreadyRegistered.add(subscriptionId);
					topicMap.put(topicName, subscriberAlreadyRegistered);
				}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * loading subscribers for particular topic to dataGrid.
	 * 
	 * @param subscriptionId
	 * @param subscribeTopics
	 */
	public void loadSubscribersByQueueName(String subscriptionId, String subscribeTopics) {
		String methodName = "loadSubscribersByQueueName";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, Set<String>> topicMap = hazelcastInstance.getMap(ConfigurationConstant.QUEUE_SUBSCRIBERS_KEYS);
		if (!subscribeTopics.isEmpty()) {
			String[] topics = subscribeTopics.split(",");
			List<String> topicsParticularSubscriber = Arrays.asList(topics);
			if (topicsParticularSubscriber != null)
				for (String topicName : topicsParticularSubscriber) {
					Set<String> subscriberAlreadyRegistered = topicMap.get(topicName);
					if (subscriberAlreadyRegistered == null) {
						subscriberAlreadyRegistered = new HashSet<String>();
						subscriberAlreadyRegistered.add(subscriptionId);
					} else
						subscriberAlreadyRegistered.add(subscriptionId);
					topicMap.put(topicName, subscriberAlreadyRegistered);
				}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * getAllTopicSubscribers gets subscribers for particular topic.
	 * 
	 * 
	 * @return topicName
	 */
	public Set<String> getAllTopicSubscribers(String topicName) {
		String methodName = "getAllTopicSubscribers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Set<String> topicSubscribers = null;
		IMap<String, Set<String>> topicMap = hazelcastInstance.getMap(ConfigurationConstant.TOPIC_SUBSCRIBERS_KEYS);
		Set<String> topicNames = topicMap.keySet();
		if (topicNames != null && !topicName.isEmpty())
			for (String topic : topicNames) {
				if (topic.equals(topicName))
					topicSubscribers = topicMap.get(topicName);
			}
		if (topicSubscribers == null)
			topicSubscribers = new HashSet<>();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return topicSubscribers;

	}

	/***
	 * loading the topicNames to dataGrid.
	 * 
	 * @param subscribeIdKey
	 * @param subscribeTopics
	 */
	public void loadSubscriberTopicBySubscribeId(String subscribeIdKey, String subscribeTopics) {
		String methodName = "loadSubscriberTopicBySubscribeId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		topicMap.put(subscribeIdKey, subscribeTopics);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/***
	 * loading the queue names to dataGrid.
	 * 
	 * @param subscribeIdKey
	 * @param subscribeTopics
	 */
	public void loadSubscriberQueueBySubscribeId(String subscribeIdKey, String subscribeTopics) {
		String methodName = "loadSubscriberQueueBySubscribeId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_QUEUE_KEYS);
		topicMap.put(subscribeIdKey, subscribeTopics);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * getSubscriptionTopicsbySubscriptionId gets all the subscribed topics and
	 * returns you the topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public String getSubscriptionTopicsbySubscriptionId(String subscriptionId) {
		String methodName = "getSubscriptionTopicsbySubscriptionId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<String> topicNames = new ArrayList<>();

		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		if (subscriptionKeys != null)
			for (String subscriptionKey : subscriptionKeys) {
				if (subscriptionId.equals(subscriptionKey)) {
					String topicNamesStr = topicMap.get(subscriptionKey);
					String[] topics = topicNamesStr.split(",");
					List<String> topicsParticularSubscriber = Arrays.asList(topics);
					topicNames.addAll(topicsParticularSubscriber);
				}
			}

		Set<String> topicNamesSet = new HashSet<String>(topicNames);
		logger.trace("{} topicNamesSet :{} ", LEAP_LOG_KEY, topicNamesSet);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return topicNamesSet.toString().replace("[", "").trim().replace("]", "").trim().replace(" ", "".trim());
	}

	/**
	 * getAllSubscribersAvailable gets all the subscribed topics and returns you the
	 * topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public Set<String> getAllSubscribersAvailable() {
		String methodName = "getAllSubscribersAvailable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		logger.trace("{} subscriptionKeys : {}", LEAP_LOG_KEY, subscriptionKeys);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return subscriptionKeys;
	}

	/**
	 * getAllSubscribersAvailable gets all the subscribed topics and returns you the
	 * topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public Set<String> getAllJMSSubscribersAvailable() {
		String methodName = "getAllJMSSubscribersAvailable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_QUEUE_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		logger.trace("{} subscriptionKeys :{} ", LEAP_LOG_KEY, subscriptionKeys);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return subscriptionKeys;
	}

	/**
	 * getSubscriptionQueuebySubscriptionId gets all the jms subscribed topics and
	 * returns you the queue names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public String getSubscriptionQueuebySubscriptionId(String subscriptionId) {
		String methodName = "getSubscriptionQueuebySubscriptionId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<String> topicNames = new ArrayList<>();

		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_QUEUE_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		if (subscriptionKeys != null)
			for (String subscriptionKey : subscriptionKeys) {
				if (subscriptionId.equals(subscriptionKey)) {
					String topicNamesStr = topicMap.get(subscriptionKey);
					String[] topics = topicNamesStr.split(",");
					List<String> topicsParticularSubscriber = Arrays.asList(topics);
					topicNames.addAll(topicsParticularSubscriber);
				}
			}

		Set<String> topicNamesSet = new HashSet<String>(topicNames);
		logger.trace("{} topicNamesSet :{} ", LEAP_LOG_KEY, topicNamesSet);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return topicNamesSet.toString().replace("[", "").trim().replace("]", "").trim().replace(" ", "".trim());
	}

	/**
	 * getAllSubscriberTopic gets all the subscribed topics and returns you the
	 * topic names to be subscribed.
	 * 
	 * @return topicNames
	 */
	public String getAllSubscriberTopic() {
		String methodName = "getAllSubscriberTopic";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<String> topicNames = new ArrayList<>();
		IMap<String, String> topicMap = hazelcastInstance.getMap(ConfigurationConstant.SUBSCRIPTION_TOPIC_KEYS);
		Set<String> subscriptionKeys = topicMap.keySet();
		if (subscriptionKeys != null)
			for (String subscriptionKey : subscriptionKeys) {
				String topicNamesStr = topicMap.get(subscriptionKey);
				String[] topics = topicNamesStr.split(",");
				List<String> topicsParticularSubscriber = Arrays.asList(topics);
				topicNames.addAll(topicsParticularSubscriber);
			}

		Set<String> topicNamesSet = new HashSet<String>(topicNames);
		logger.trace("{} topicNamesSet :{} ", LEAP_LOG_KEY, topicNamesSet);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return topicNamesSet.toString().replace("[", "").trim().replace("]", "").trim().replace(" ", "".trim());
	}

	public List<ConfigurationUnit> getConfigUnitList() {
		String methodName = "getConfigUnitList";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<ConfigurationUnit> configunitList = new ArrayList<>();
		IMap<String, String> configGroupMap = hazelcastInstance.getMap(ConfigurationConstant.CONFIG_GROUP_KEYS);
		if (configGroupMap != null && !configGroupMap.isEmpty()) {

			for (Map.Entry<String, String> configgroupkeys : configGroupMap.entrySet()) {
				String configgroupkey = configgroupkeys.getValue();
				logger.debug("{} config group key :{} ", LEAP_LOG_KEY, configgroupkey);
				IMap<String, ConfigurationUnit> configGroupMapList = hazelcastInstance.getMap(configgroupkey);

				if (configGroupMapList != null && !configGroupMapList.isEmpty()) {
					for (Map.Entry<String, ConfigurationUnit> configunit : configGroupMapList.entrySet()) {
						configunitList.add(configunit.getValue());

					} // end of inner for
				} else {
					logger.debug("{} no configuration unit with config key :{} ", LEAP_LOG_KEY, configgroupkey);
				} // end of else of inner if
			} // end of outter for
		} else {
			logger.debug("{} no config group key list with :{} ", LEAP_LOG_KEY,
					ConfigurationConstant.CONFIG_GROUP_KEYS);
		} // end of else of outter if
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configunitList;
	}

	public List<ConfigurationUnit> getSpecificConfigUnitList(String groupkey) {
		String methodName = "getSpecificConfigUnitList";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<ConfigurationUnit> configunitList = new ArrayList<>();

		IMap<String, ConfigurationUnit> configGroupMapList = hazelcastInstance.getMap(groupkey);

		if (configGroupMapList != null && !configGroupMapList.isEmpty()) {
			for (Map.Entry<String, ConfigurationUnit> configunit : configGroupMapList.entrySet()) {
				logger.debug("{} inside for loop ", LEAP_LOG_KEY);
				configunitList.add(configunit.getValue());

			} // end of inner for
		} else {
			logger.debug("{} no configuration unit with config key :{} ", LEAP_LOG_KEY, groupkey);
		} // end of else of inner if
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configunitList;
	}

	/** Private constructor due to singleton */
	private LeapConfigurationServer(HazelcastInstance hazelcastInstance, IMap<String, String> configGroupKeys) {
		this.hazelcastInstance = hazelcastInstance;
		this.configGroupKeys = configGroupKeys;
	}

	public void addConfiguration(ConfigurationUnit configUnit) {
		String methodName = "addConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String groupkey = getGroupKey(configUnit);
		IMap<String, ConfigurationUnit> groupConfigMap = hazelcastInstance.getMap(groupkey);
		groupConfigMap.put(configUnit.getKey(), configUnit);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * 
	 * @param configUnit
	 */
	public void addConfigurationWithoutTenant(ConfigurationUnit configUnit) {
		String groupkey = getGroupKeyWithoutTenant(configUnit);
		IMap<String, ConfigurationUnit> groupConfigMap = hazelcastInstance.getMap(groupkey);
		groupConfigMap.put(configUnit.getKey(), configUnit);
	}// ..end of the method

	public void addConfiguration(List<ConfigurationUnit> configUnits) {
		for (ConfigurationUnit configUnit : configUnits) {
			addConfiguration(configUnit);
		}
	}

	public ConfigurationUnit getConfiguration(String tenantId, String configGroup, String configKey) {
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		logger.trace("{} config Group map :{} ", LEAP_LOG_KEY, configGroupMap);
		if (configGroupMap == null || configGroupMap.isEmpty()) {
			logger.debug("{} config map is empty/null", LEAP_LOG_KEY);
			return null;
		}
		return configGroupMap.get(configKey);
	}

	/**
	 * to get the configuration unit by passing the key value, where tenant-vendorId
	 * has been changed to get only 'VendorNodeId'
	 * 
	 * @param configGroup
	 * @param configKey
	 * @return configurationUnit Object
	 */
	public ConfigurationUnit getConfiguration(String configGroup, String configKey) {
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(configGroup));
		logger.trace("{} IMapKey-:-{}", LEAP_LOG_KEY, configGroupMap);
		if (configGroupMap.isEmpty()) {
			logger.debug("{} Config Imap is null", LEAP_LOG_KEY);
		}
		return configGroupMap.get(configKey);
	}// ..end of the method

	public void deleteConfiguration(String tenantId, String configGroup, String configKey) {
		String methodName = "deleteConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		if (configGroupMap != null && !configGroupMap.isEmpty())
			configGroupMap.remove(configKey);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public void changeConfigState(String tenantId, String configGroup, String configKey, boolean enableConfig) {
		logger.debug("{} inside changeConfigState()", LEAP_LOG_KEY);
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		if (configGroupMap != null && !configGroupMap.isEmpty()) {
			ConfigurationUnit configUntit = configGroupMap.get(configKey);
			configUntit.setIsEnabled(Boolean.valueOf(enableConfig));
			configGroupMap.put(configKey, configUntit);
		}
	}

	public void addConfigListener(String tenantId, String configGroup, LeapConfigurationListener listener) {
		IMap<String, ConfigurationUnit> configGroupMap = hazelcastInstance.getMap(getGroupKey(tenantId, configGroup));
		if (configGroupMap != null) {
			configGroupMap.addEntryListener(listener, true);
		}
	}

	public void addConfiguration(String taxCofigKey, String filename) {
		logger.debug("{} addConfiguration called ", LEAP_LOG_KEY);
		IMap<String, String> groupConfigMap = hazelcastInstance.getMap("TaxonomyConfigs");
		groupConfigMap.put(taxCofigKey, filename);

	}

	public String getConfiguration(String taxCofigKey) {
		logger.debug("{} getConfiguration called ", LEAP_LOG_KEY);
		IMap<String, String> groupConfigMap = hazelcastInstance.getMap("TaxonomyConfigs");
		return groupConfigMap.get(taxCofigKey);

	}

	private String getGroupKey(ConfigurationUnit configUnit) {
		String tenantId = configUnit.getTenantId();
		String groupId = configUnit.getConfigGroup();
		storeGroupKeys(tenantId + "-" + groupId);
		return tenantId + "-" + groupId;
	}

	private String getGroupKeyWithoutTenant(ConfigurationUnit configurationUnit) {
		return configurationUnit.getConfigGroup();
	}

	private String getGroupKey(String tenantId, String configGroup) {
		return tenantId + "-" + configGroup;
	}

	private String getGroupKey(String configGroup) {
		return configGroup;
	}

	private void storeGroupKeys(String configGroupKey) {
		configGroupKeys.put(configGroupKey, configGroupKey);
	}

	/**
	 * This method is used to add the {@link List} of {@link AccountConfiguration}
	 * to the dataGrid service.
	 * 
	 * @param configurations
	 */
	public void addAccountConfiguration(List<AccountConfiguration> configurations) {
		String methodName = "addAccountConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, AccountConfiguration> externalConfigMap = hazelcastInstance
				.getMap(TenantSecurityConstant.EXTERNAL_ACCOUNT_CONFIG);
		IMap<String, AccountConfiguration> internalConfigMap = hazelcastInstance
				.getMap(TenantSecurityConstant.INTERNAL_ACCOUNT_CONFIG);
		for (AccountConfiguration configuration : configurations) {
			externalConfigMap.put(configuration.getAccountName() + TenantSecurityConstant.FEATURE_SEPERATOR
					+ configuration.getInternalSiteId(), configuration);
			internalConfigMap.put(configuration.getInternalTenantId() + TenantSecurityConstant.FEATURE_SEPERATOR
					+ configuration.getInternalSiteId(), configuration);
		} // end of the method.
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is used to remove a {@link AccountConfiguration} to the dataGrid
	 * service.
	 * 
	 * @param configuration
	 */
	public void removeAccountConfiguration(AccountConfiguration configuration) {
		String methodName = "removeAccountConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IMap<String, AccountConfiguration> externalConfigMap = hazelcastInstance
				.getMap(TenantSecurityConstant.EXTERNAL_ACCOUNT_CONFIG);
		IMap<String, AccountConfiguration> internalConfigMap = hazelcastInstance
				.getMap(TenantSecurityConstant.INTERNAL_ACCOUNT_CONFIG);
		externalConfigMap.remove(configuration.getAccountName() + TenantSecurityConstant.FEATURE_SEPERATOR
				+ configuration.getInternalSiteId(), configuration);
		internalConfigMap.remove(configuration.getInternalTenantId() + TenantSecurityConstant.FEATURE_SEPERATOR
				+ configuration.getInternalSiteId(), configuration);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method.
}
