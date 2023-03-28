package com.attunedlabs.eventsubscription.abstractretrystrategy;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.BeanDependencyResolverFactory;
import com.attunedlabs.core.IBeanDependencyResolver;
import com.attunedlabs.eventframework.config.EventFrameworkConfigurationException;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.jaxb.FailureHandlingStrategy;
import com.attunedlabs.eventframework.jaxb.JMSSubscribeEvent;
import com.attunedlabs.eventframework.jaxb.SubscribeEvent;
import com.attunedlabs.eventsubscription.defaultretrystrategy.JMSLeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapDefaultRetryStrategy;
import com.attunedlabs.eventsubscription.defaultretrystrategy.LeapNoRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.exception.MissingConfigurationException;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;

public class InstantiateSubscriptionRetryStrategy {
	final static Logger logger = LoggerFactory.getLogger(InstantiateSubscriptionRetryStrategy.class);
	// map holding
	// tenantSiteKey --> (generatedSubscriptionId + '/' + strategyClass)key -->
	// classInstance
	private static HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> cachingInstance = new HashMap<>();

	public static void cacheStrategyClassInstancePerSubscription(Object eventSubscriptionConfig, String tenantId,
			String siteId, String featureGroup, String featureName, String implementation, String vendorName,
			String version) throws EventFrameworkConfigurationException {
		String methodName = "cacheStrategyClassInstancePerSubscription";
		logger.debug("{} entered into the method {}, subsribeEvent :{}", LEAP_LOG_KEY, methodName,
				eventSubscriptionConfig);
		if (eventSubscriptionConfig instanceof SubscribeEvent) {
			// we upload in cache only when enabled
			SubscribeEvent subEvent = (SubscribeEvent) eventSubscriptionConfig;
			try {
				if (subEvent.isIsEnabled()) {
					String strategy = "";
					String strategyConfig = "{}";
					FailureHandlingStrategy failureHandlingStrategy = subEvent.getFailureHandlingStrategy();
					if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
						strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
						if (strategy == null || strategy.trim().isEmpty())
							strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();
						strategyConfig = subEvent.getFailureHandlingStrategy().getFailureStrategyConfig();

					} else
						strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

					if (strategyConfig == null || strategyConfig.trim().isEmpty())
						strategyConfig = "{}";

					// setting the strategy in header for executing onSuccess
					// and
					// onFailure
					String tenantSiteKey = tenantId + SubscriptionConstant.TENANT_SITE_SEPERATOR + siteId;
					String generatedSubscriptionId = generateSubscriptionId(tenantId, siteId, featureGroup, featureName,
							implementation, vendorName, version, subEvent.getSubscriptionId().trim());
					loadAndGetStrategyImplementation(strategy.trim(), generatedSubscriptionId, strategyConfig,
							tenantSiteKey);
				}
			} catch (Exception e) {
				logger.error("{} subscription failed to cache the strategy instace for subscriptionId  : {}",
						LEAP_LOG_KEY, subEvent.getSubscriptionId());
				throw new EventFrameworkConfigurationException(e.getMessage(), e);
			}
		} else {
			JMSSubscribeEvent jmsSubEvent = (JMSSubscribeEvent) eventSubscriptionConfig;
			try {
				if (jmsSubEvent.getIsEnabled()) {

					String strategy = "";
					String strategyConfig = "{}";
					FailureHandlingStrategy failureHandlingStrategy = jmsSubEvent.getFailureHandlingStrategy();
					if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
						strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
						if (strategy == null || strategy.trim().isEmpty())
							strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();
						strategyConfig = jmsSubEvent.getFailureHandlingStrategy().getFailureStrategyConfig();

					} else
						strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

					if (strategyConfig == null || strategyConfig.trim().isEmpty())
						strategyConfig = "{}";

					// setting the strategy in header for executing onSuccess
					// and
					// onFailure
					String tenantSiteKey = tenantId + SubscriptionConstant.TENANT_SITE_SEPERATOR + siteId;
					String generatedSubscriptionId = generateSubscriptionId(tenantId, siteId, featureGroup, featureName,
							implementation, vendorName, version, jmsSubEvent.getSubscriptionId().trim());
					loadAndGetStrategyImplementation(strategy.trim(), generatedSubscriptionId, strategyConfig,
							tenantSiteKey);
				}
			} catch (Exception e) {
				logger.error("{} jms subscription failed to cache the strategy instace for subscriptionId  : {}",
						LEAP_LOG_KEY, jmsSubEvent.getSubscriptionId());
				throw new EventFrameworkConfigurationException(e.getMessage(), e);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	public static void cacheStrategyClassInstancePerSubscription(JMSSubscribeEvent eventSubscriptionConfig,
			String tenantId, String siteId, String featureGroup, String featureName, String implementation,
			String vendorName, String version) throws EventFrameworkConfigurationException {
		String methodName = "cacheStrategyClassInstancePerSubscription";
		logger.debug("{} entered into the method {}, subscribeEvent: {}", LEAP_LOG_KEY, methodName,
				eventSubscriptionConfig);
		try {
			// we upload in cache only when enabled
			if (eventSubscriptionConfig.getIsEnabled()) {

				String strategy = "";
				String strategyConfig = "{}";
				FailureHandlingStrategy failureHandlingStrategy = eventSubscriptionConfig.getFailureHandlingStrategy();
				if (failureHandlingStrategy != null && failureHandlingStrategy.getFailureStrategyName() != null) {
					strategy = failureHandlingStrategy.getFailureStrategyName().getValue();
					if (strategy == null || strategy.trim().isEmpty())
						strategy = failureHandlingStrategy.getFailureStrategyName().getHandlerQualifiedClass();
					strategyConfig = eventSubscriptionConfig.getFailureHandlingStrategy().getFailureStrategyConfig();

				} else
					strategy = SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS;

				if (strategyConfig == null || strategyConfig.trim().isEmpty())
					strategyConfig = "{}";

				// setting the strategy in header for executing onSuccess and
				// onFailure
				String tenantSiteKey = tenantId + SubscriptionConstant.TENANT_SITE_SEPERATOR + siteId;
				String generatedSubscriptionId = generateSubscriptionId(tenantId, siteId, featureGroup, featureName,
						implementation, vendorName, version, eventSubscriptionConfig.getSubscriptionId().trim());
				loadAndGetStrategyImplementation(strategy.trim(), generatedSubscriptionId, strategyConfig,
						tenantSiteKey);
			}
		} catch (Exception e) {
			logger.error("{} jms subscription failed to cache the strategy instace for subscriptionId  : {}",
					LEAP_LOG_KEY, eventSubscriptionConfig.getSubscriptionId());
			throw new EventFrameworkConfigurationException(e.getMessage(), e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * provides the implementation of strategy class if present in the local cache
	 * else creates new instance and stores in local cache and return the same.
	 * 
	 * @param strategy
	 * @param subscriptionId
	 * @param strategyConfig
	 * @param tenantSiteKey
	 * @return
	 * @throws MissingConfigurationException
	 * @throws ConfigurationValidationFailedException
	 */
	public static AbstractSubscriptionRetryStrategy loadAndGetStrategyImplementation(String strategy,
			String subscriptionId, String strategyConfig, String tenantSiteKey)
			throws MissingConfigurationException, ConfigurationValidationFailedException {
		String methodName = "loadAndGetStrategyImplementation";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, AbstractSubscriptionRetryStrategy> tenantStrategyMap = cachingInstance.get(tenantSiteKey);
		AbstractSubscriptionRetryStrategy abstractRetryStrategyBean = null;
		if (tenantStrategyMap != null)
			abstractRetryStrategyBean = (AbstractSubscriptionRetryStrategy) tenantStrategyMap
					.get(subscriptionId + SubscriptionConstant.SUB_ID_CLASS_SEPERATOR + strategy);
		// found in the local Map
		if (abstractRetryStrategyBean != null) {
			logger.info("{} strategy already instantiated and found in localcache", LEAP_LOG_KEY);
			return abstractRetryStrategyBean;
		}

		switch (strategy) {
		case SubscriptionConstant.LEAP_DEFAULT_RETRY_STRATEGY_CLASS:
			abstractRetryStrategyBean = new LeapDefaultRetryStrategy(strategyConfig);
			break;
		case SubscriptionConstant.JMS_LEAP_DEFAULT_RETRY_STRATEGY_CLASS:
			abstractRetryStrategyBean = new JMSLeapDefaultRetryStrategy(strategyConfig);
			break;
		case SubscriptionConstant.LEAP_NO_RETRY_STRATEGY_CLASS:
			abstractRetryStrategyBean = new LeapNoRetryStrategy(strategyConfig);
			break;
		default:
			try {
				abstractRetryStrategyBean = getRetryStrategyInstance(strategy, strategyConfig);
			} catch (BeanDependencyResolveException e) {
				/*
				 * logger. error("failed to get the custom getStrategyImplementation with " +
				 * strategy + ", will not use strategy..."); abstractRetryStrategyBean = new
				 * LeapNonRetryableSubscriptionStrategy(strategyConfig);
				 */

				throw new MissingConfigurationException(
						"BEAN INSTANTIATION FAILED  : Unable to getStrategyImplementation " + " due to "
								+ e.getMessage(),
						e);

			}
		}
		// caching the instance before returning
		cachingRetryStrategyInstance(abstractRetryStrategyBean, strategy, subscriptionId, tenantSiteKey);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return abstractRetryStrategyBean;
	}

	/**
	 * Instantiation of retry strategy class.
	 * 
	 * @param strategyClass
	 * @return
	 * @throws BeanDependencyResolveException
	 */
	private static AbstractSubscriptionRetryStrategy getRetryStrategyInstance(String strategyClass, String retryConfig)
			throws BeanDependencyResolveException {
		String methodName = "getRetryStrategyInstance";
		logger.debug("{} entered into the method {}, strategyClass: {}", LEAP_LOG_KEY, methodName, strategyClass);
		AbstractSubscriptionRetryStrategy retryStrategyImpl = null;
		try {
			IBeanDependencyResolver beanResolver = BeanDependencyResolverFactory.getBeanDependencyResolver();
			retryStrategyImpl = (AbstractSubscriptionRetryStrategy) beanResolver.getBeanInstance(
					AbstractSubscriptionRetryStrategy.class, strategyClass, new Class<?>[] { retryConfig.getClass() },
					new String[] { retryConfig });
		} catch (IllegalArgumentException | BeanDependencyResolveException exp) {
			logger.error("{} Failed to initialize CustomRetryStrategy {}", LEAP_LOG_KEY, strategyClass, exp);
			throw new BeanDependencyResolveException(
					"Failed to initialize CustomRetryStrategy {" + strategyClass + "} ==> " + exp.getMessage(), exp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return retryStrategyImpl;

	}

	/**
	 * caching the strategy instance for next request.
	 * 
	 * @param abstractRetryStrategyBean
	 * @param strategy
	 * @param subscriptionId
	 * @param tenantSiteKey
	 * @param site
	 */
	private static void cachingRetryStrategyInstance(AbstractSubscriptionRetryStrategy abstractRetryStrategyBean,
			String strategy, String subscriptionId, String tenantSiteKey) {
		Map<String, AbstractSubscriptionRetryStrategy> tenantStrategyMap = cachingInstance.get(tenantSiteKey);
		if (tenantStrategyMap == null) {
			tenantStrategyMap = new HashMap<>();
		}
		tenantStrategyMap.put(subscriptionId + SubscriptionConstant.SUB_ID_CLASS_SEPERATOR + strategy,
				abstractRetryStrategyBean);
		cachingInstance.put(tenantSiteKey, tenantStrategyMap);
	}

	/**
	 * subscription id construction as
	 * fGroup-fName-impl-vendor-version-subscriptionId
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param featureGroup
	 * @param featureName
	 * @param implementation
	 * @param vendorName
	 * @param version
	 * @param subscriptionId
	 * @return
	 */
	private static String generateSubscriptionId(String tenantId, String siteId, String featureGroup,
			String featureName, String implementation, String vendorName, String version, String subscriptionId) {
		return nullParameterCheck(featureGroup) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(featureName) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(implementation) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(vendorName) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(version) + EventFrameworkConstants.SUBSCRIPTION_ID_CONSTRUCTOR_DELIMITER
				+ nullParameterCheck(subscriptionId);
	}

	/**
	 * nodeType empty check
	 * 
	 * @param nodeType
	 * @return
	 */
	private static String nullParameterCheck(String nodeType) {
		nodeType.replace(EventFrameworkConstants.ATTRIBUTE_CHARACTER_REPLACE,
				EventFrameworkConstants.EMPTY_REPLACEMENT);
		return nodeType.isEmpty() ? "" : nodeType;
	}

	/**
	 * @return the cachingInstance
	 */
	public static HashMap<String, Map<String, AbstractSubscriptionRetryStrategy>> getCachingInstance() {
		return cachingInstance;
	}

}
