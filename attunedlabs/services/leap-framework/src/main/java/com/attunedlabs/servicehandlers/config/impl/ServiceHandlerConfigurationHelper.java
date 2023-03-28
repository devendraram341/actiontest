package com.attunedlabs.servicehandlers.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler.ServiceHandler;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler.ServiceHandler.ExcludedServices.Service;
import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler.ServiceHandler.HandlerImplementation;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.GenericApplicableNode;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigNodeData;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.config.persistence.IConfigPersistenceService;
import com.attunedlabs.config.persistence.InvalidNodeTreeException;
import com.attunedlabs.config.persistence.impl.ConfigPersistenceServiceMySqlImpl;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigParserException;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigXMLParser;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigurationException;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;

public class ServiceHandlerConfigurationHelper extends GenericApplicableNode {
	final static Logger logger = LoggerFactory.getLogger(ServiceHandlerConfigurationHelper.class);
	private HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
	private ServiceHandlerConfigXMLParser serviceHandlerConfigXMLParser = new ServiceHandlerConfigXMLParser();
	public static final String KEY_SEPERATOR = "-";
	public static final String VALUE_SEPERATOR = "|#";
	public static final String ESCAPE_CHAR = "\\";
	static final String WILDCARD = "*";
	static final String ALL_AppCommonServiceHanlders = "All-ACSH";
	public static final String AppCommonServiceHanlders = "ACSH";
	public static final String AppCommonServiceHanldersLookup = "ACSH-lookup";
	public static final String AppCommonServiceHanldersFQCN = "ACSH-fqcn";
	public static final String AppFeatureServiceHanlders = "AFSH";
	public static final String AppFeatureServiceHanldersLookup = "AFSH-lookup";
	public static final String AppFeatureServiceHanldersFQCN = "AFSH-fqcn";
	public static final String FeatureServiceHanlders = "FFSH";
	public static final String FeatureServiceHanldersLookup = "FFSH-lookup";
	public static final String FeatureServiceHanldersFQCN = "FFSH-fqcn";
	public static final String APP_Handlers = "AppHandlers";
	public static final String Feature_Handlers = "FeatureHandlers";

	/**
	 * <b>storing in this way to exclude:</b></br>
	 * {tenantId}-{siteId}-ACSH --> {{featureGrp}-{featureName}-{serviceName} ->
	 * [handlerId0,handlerId1,...]}</br>
	 * 
	 * <b> storing handlers in this way : </b></br>
	 * ACSH-lookup --> {hanldersId0 ->
	 * {handlerId|#ACSH/AFSH/FFSH|#lookup|#{beanId}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * </br>
	 * ACSH-fqcn --> {hanldersId0 ->
	 * handlerId|#ACSH/AFSH/FFSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * </br>
	 * <b>all common service handlers are stored in way : </b> </br>
	 * {tenantId}-{siteId}-All_ACSH --> [handlerId0,handlerId1,...]</br>
	 * 
	 * @param commonServiceHandler
	 * @param site
	 * @param tenant
	 * @param applicationFeature
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addApplicationCommonServicesConfiguration(CommonServiceHandler commonServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		String methodName = "addApplicationCommonServicesConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		List<ServiceHandler> serviceHandlers = commonServiceHandler.getServiceHandler();

		addAppCommonServiceHandlerToDB(commonServiceHandler, configContext);

		for (ServiceHandler serviceHandler : serviceHandlers) {
			String tenantId = serviceHandler.getTenantId();
			String siteId = serviceHandler.getSiteId();
			String commonServiceHanldersMapKey = tenantId + KEY_SEPERATOR + siteId + KEY_SEPERATOR
					+ AppCommonServiceHanlders;
			IMap<String, List<String>> commomnHandlersMap = hazelcastInstance.getMap(commonServiceHanldersMapKey);
			if (serviceHandler.getExcludedServices() != null)
				if (serviceHandler.getExcludedServices().getService() != null)
					for (Service excludeService : serviceHandler.getExcludedServices().getService()) {
						String featureGrp = excludeService.getFeatureGroup();
						String featureName = excludeService.getFeatureName();
						String serviceName = excludeService.getServiceName();
						String serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR
								+ serviceName;
						List<String> handlers = commomnHandlersMap.get(serviceHandlersKey);
						if (handlers == null)
							handlers = new ArrayList<>();
						handlers.add(serviceHandler.getHandlerId());
						commomnHandlersMap.put(serviceHandlersKey, handlers);
					}
			logger.debug("{} ApplicationCommonServicesHandlers Map : ", LEAP_LOG_KEY);
			commomnHandlersMap.forEach((k, v) -> logger.debug(k + " : " + v));
			HandlerImplementation handlerImplementation = serviceHandler.getHandlerImplementation();
			if (handlerImplementation.getBeanId() != null) {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(AppCommonServiceHanldersLookup);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + AppCommonServiceHanlders + VALUE_SEPERATOR
								+ "lookup" + VALUE_SEPERATOR + handlerImplementation.getBeanId().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute());
			} else {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(AppCommonServiceHanldersFQCN);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + AppCommonServiceHanlders + VALUE_SEPERATOR
								+ "instance" + VALUE_SEPERATOR + handlerImplementation.getFqcn().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute() + VALUE_SEPERATOR
								+ ((handlerImplementation.getHandlerConfig() != null)
										? handlerImplementation.getHandlerConfig()
										: ""));
				HandlerCacheSerivce.initializeHandler(handlerImplementation.getFqcn().trim(),
						commonServiceHanldersMapKey, handlerImplementation.getHandlerConfig());
			}
			// all common handlers are store in this list
			String allcommonServiceHanldersMapKey = tenantId + KEY_SEPERATOR + siteId + KEY_SEPERATOR
					+ ALL_AppCommonServiceHanlders;
			IList<String> allCommonHandlersMap = hazelcastInstance.getList(allcommonServiceHanldersMapKey);
			allCommonHandlersMap.add(serviceHandler.getHandlerId());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * <b>storing in this way :</b></br>
	 * {tenantId}-{siteId}-AFSH --> {{featureGrp}-{featureName}-{serviceName} ->
	 * [handlerId0,handlerId1,...]}</br>
	 * 
	 * <b> storing handlers in this way : </b></br>
	 * AFSH-lookup --> {hanldersId0 ->
	 * {handlerId|#ACSH/AFSH/FFSH|#lookup|#{beanId}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * </br>
	 * AFSH-fqcn --> {hanldersId0 ->
	 * handlerId|#ACSH/AFSH/FFSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * 
	 * @param featureServiceHandler
	 * @param site
	 * @param tenant
	 * @param applicationFeature
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addApplicationFeatureServiceHandlersConfiguration(
			com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		String methodName = "addApplicationFeatureServiceHandlersConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		addAppFeatureServiceHandlerToDB(featureServiceHandler, configContext);

		List<com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler> serviceHandlers = featureServiceHandler
				.getServiceHandler();
		for (com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler serviceHandler : serviceHandlers) {
			String tenantId = serviceHandler.getTenantId();
			String siteId = serviceHandler.getSiteId();
			String featureServiceHanldersMapKey = tenantId + KEY_SEPERATOR + siteId + KEY_SEPERATOR
					+ AppFeatureServiceHanlders;
			IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(featureServiceHanldersMapKey);
			if (serviceHandler.getIncludedServices() != null)
				if (serviceHandler.getIncludedServices().getService() != null)
					for (com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service includeService : serviceHandler
							.getIncludedServices().getService()) {
						String featureGrp = includeService.getFeatureGroup();
						String featureName = includeService.getFeatureName();
						String serviceName = includeService.getServiceName();
						String serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR
								+ serviceName;
						List<String> handlers = featureHandlersMap.get(serviceHandlersKey);
						if (handlers == null)
							handlers = new ArrayList<>();
						handlers.add(serviceHandler.getHandlerId());
						featureHandlersMap.put(serviceHandlersKey, handlers);
					}
			logger.debug("{} ApplicationFeatureServiceHandlers Map : ", LEAP_LOG_KEY);
			featureHandlersMap.forEach((k, v) -> logger.debug(k + " : " + v));
			com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.HandlerImplementation handlerImplementation = serviceHandler
					.getHandlerImplementation();
			if (handlerImplementation.getBeanId() != null) {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(AppFeatureServiceHanldersLookup);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + AppFeatureServiceHanlders + VALUE_SEPERATOR
								+ "lookup" + VALUE_SEPERATOR + handlerImplementation.getBeanId().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute());
			} else {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(AppFeatureServiceHanldersFQCN);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + AppFeatureServiceHanlders + VALUE_SEPERATOR
								+ "instance" + VALUE_SEPERATOR + handlerImplementation.getFqcn().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute() + VALUE_SEPERATOR
								+ ((handlerImplementation.getHandlerConfig() != null)
										? handlerImplementation.getHandlerConfig()
										: ""));
				HandlerCacheSerivce.initializeHandler(handlerImplementation.getFqcn().trim(),
						featureServiceHanldersMapKey, handlerImplementation.getHandlerConfig());
			}

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * <b>storing in this way :</b></br>
	 * {nodeId}-FFSH --> {{serviceName} -> [handlerId0,handlerId1,...]}</br>
	 * 
	 * <b> storing handlers in this way : </b></br>
	 * FFSH-lookup --> {hanldersId0 ->
	 * {handlerId|#ACSH/AFSH/FFSH|#lookup|#{beanId}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * </br>
	 * FFSH-fqcn --> {hanldersId0 -> {
	 * handlerId|#ACSH/AFSH/FFSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig}}
	 * 
	 * @param featureServiceHandler
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addFeatureServiceHandlerConfiguration(ConfigurationContext configurationContext,
			FeatureServiceHandler featureServiceHandler, Integer nodeId)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		String methodName = "addFeatureServiceHandlerConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		if (nodeId == null)
			nodeId = addFeatureLevelFeatureServiceHandlerToDB(featureServiceHandler, configurationContext);

		List<com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler> serviceHandlers = featureServiceHandler
				.getServiceHandler();
		String featureServiceHanldersMapKey = nodeId + KEY_SEPERATOR + FeatureServiceHanlders;
		for (com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler serviceHandler : serviceHandlers) {
			IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(featureServiceHanldersMapKey);
			if (serviceHandler.getIncludedServices() != null)
				if (serviceHandler.getIncludedServices().getService() != null)
					for (com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service includeService : serviceHandler
							.getIncludedServices().getService()) {
						String serviceName = includeService.getServiceName();
						String serviceHandlersKey = serviceName;
						List<String> handlers = featureHandlersMap.get(serviceHandlersKey);
						if (handlers == null)
							handlers = new ArrayList<>();
						handlers.add(serviceHandler.getHandlerId());
						featureHandlersMap.put(serviceHandlersKey, handlers);
					}
			logger.debug("{} FeatureServiceHandler Map : ", LEAP_LOG_KEY);
			featureHandlersMap.forEach((k, v) -> logger.debug(k + " : " + v));
			com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.HandlerImplementation handlerImplementation = serviceHandler
					.getHandlerImplementation();
			if (handlerImplementation.getBeanId() != null) {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(FeatureServiceHanldersLookup);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + FeatureServiceHanlders + VALUE_SEPERATOR
								+ "lookup" + VALUE_SEPERATOR + handlerImplementation.getBeanId().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute());
			} else {
				IMap<String, String> hanldersStore = hazelcastInstance.getMap(FeatureServiceHanldersFQCN);
				hanldersStore.put(serviceHandler.getHandlerId(),
						serviceHandler.getHandlerId() + VALUE_SEPERATOR + FeatureServiceHanlders + VALUE_SEPERATOR
								+ "instance" + VALUE_SEPERATOR + handlerImplementation.getFqcn().trim()
								+ VALUE_SEPERATOR + serviceHandler.getType() + VALUE_SEPERATOR
								+ serviceHandler.getExecute() + VALUE_SEPERATOR
								+ ((handlerImplementation.getHandlerConfig() != null)
										? handlerImplementation.getHandlerConfig()
										: ""));
				HandlerCacheSerivce.initializeHandler(handlerImplementation.getFqcn().trim(),
						featureServiceHanldersMapKey, handlerImplementation.getHandlerConfig());
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	// no wildcards allowed for service name while updating.
	public void includeNewService(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException {
		String methodName = "includeNewService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int nodeId = getConfigNodeId(requestContext);
		if (updateConfigNodeData(nodeId, requestContext.getFeatureGroup(), requestContext.getFeatureName(), serviceName,
				"add", handlerId) > 0) {
			// update configuration cache
			String featureServiceHanldersMapKey = nodeId + KEY_SEPERATOR + FeatureServiceHanlders;
			IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(featureServiceHanldersMapKey);
			Set<String> configServices = featureHandlersMap.keySet();
			if (configServices != null && handlerId != null && !handlerId.trim().isEmpty()) {
				List<String> serviceHandlers = featureHandlersMap.get(serviceName);
				if (serviceHandlers == null)
					serviceHandlers = new ArrayList<>();
				if (!serviceHandlers.contains(handlerId))
					serviceHandlers.add(handlerId);
				featureHandlersMap.put(serviceName, serviceHandlers);
			}
			// update service cache
			String featServiceHandlerMap = nodeId + KEY_SEPERATOR + Feature_Handlers;
			IMap<String, List<String>> serviceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
			Set<String> services = serviceHandlerMapData.keySet();
			if (services != null && handlerId != null && !handlerId.trim().isEmpty()) {
				List<String> serviceHandlers = serviceHandlerMapData.get(serviceName);
				if (serviceHandlers == null)
					serviceHandlers = new ArrayList<>();
				if (!serviceHandlers.contains(FeatureServiceHanlders + KEY_SEPERATOR + handlerId))
					serviceHandlers.add(FeatureServiceHanlders + KEY_SEPERATOR + handlerId);
				serviceHandlerMapData.put(serviceName, serviceHandlers);
			}
		} else
			logger.error("{} unable to update the handler from db henace datagrid wont be updated : {} ", LEAP_LOG_KEY,
					handlerId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	// no wildcards allowed for service name while removing.
	public void removeServicesForHandler(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException {
		String methodName = "removeServicesForHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int nodeId = getConfigNodeId(requestContext);
		if (updateConfigNodeData(nodeId, requestContext.getFeatureGroup(), requestContext.getFeatureName(), serviceName,
				"remove", handlerId) > 0) {
			// update configuration cache
			String featureServiceHanldersMapKey = nodeId + KEY_SEPERATOR + FeatureServiceHanlders;
			IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(featureServiceHanldersMapKey);
			Set<String> configServices = featureHandlersMap.keySet();
			if (configServices != null && configServices.contains(serviceName) && handlerId != null
					&& !handlerId.trim().isEmpty()) {
				List<String> serviceHandlers = featureHandlersMap.get(serviceName);
				if (serviceHandlers == null)
					serviceHandlers = new ArrayList<>();
				if (serviceHandlers.contains(handlerId))
					serviceHandlers.remove(handlerId);
				featureHandlersMap.put(serviceName, serviceHandlers);
			}

			// update service cache
			String featServiceHandlerMap = nodeId + KEY_SEPERATOR + Feature_Handlers;
			IMap<String, List<String>> serviceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
			Set<String> services = serviceHandlerMapData.keySet();
			if (services != null && services.contains(serviceName) && handlerId != null
					&& !handlerId.trim().isEmpty()) {
				List<String> serviceHandlers = serviceHandlerMapData.get(serviceName);
				if (serviceHandlers == null)
					serviceHandlers = new ArrayList<>();
				serviceHandlers.remove(FeatureServiceHanlders + KEY_SEPERATOR + handlerId);
				serviceHandlerMapData.put(serviceName, serviceHandlers);
			}
			updateConfigNodeData(nodeId, requestContext.getFeatureGroup(), requestContext.getFeatureName(), serviceName,
					"remove", handlerId);
		} else
			logger.error("{} unable to update the handler from db henace datagrid wont be updated : {} ", LEAP_LOG_KEY,
					handlerId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	// handlerId|#ACSH/AFSH/FSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig}
	public void updateFSHandlersConfiguration(RequestContext requestContext, String handlerId, String execute,
			String type, String fqcn, String beanRefId, String config)
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException {
		String methodName = "updateFSHandlersConfiguration";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		int nodeId = getConfigNodeId(requestContext);
		String featureServiceHanldersMapKey = nodeId + KEY_SEPERATOR + FeatureServiceHanlders;

		IMap<String, String> hanldersStoreLookup = hazelcastInstance.getMap(FeatureServiceHanldersLookup);
		if (hanldersStoreLookup.containsKey(handlerId)) {
			String handlerConfigStr = hanldersStoreLookup.get(handlerId);
			String handlerConfig[] = handlerConfigStr.split(ESCAPE_CHAR + VALUE_SEPERATOR);
			if (execute != null && (execute.equalsIgnoreCase("sync") || execute.equalsIgnoreCase("async")))
				handlerConfig[5] = execute.toLowerCase();
			if (type != null && validateType(type))
				handlerConfig[4] = type.toLowerCase();
			if (beanRefId != null && !beanRefId.trim().isEmpty()) {
				handlerConfig[3] = beanRefId.trim();
				handlerConfig[2] = "lookup";
			}
			if (handlerConfig.length == 7 && config != null && !config.trim().isEmpty())
				handlerConfig[6] = config.trim();
			else {
				config = "";
				handlerConfig[handlerConfig.length - 1] = handlerConfig[handlerConfig.length - 1] + VALUE_SEPERATOR;
			}
			String handConfigUpdated = "";
			for (int i = 0; i < handlerConfig.length; i++)
				if (i != handlerConfig.length - 1)
					handConfigUpdated = handConfigUpdated + handlerConfig[i] + VALUE_SEPERATOR;
				else
					handConfigUpdated = handConfigUpdated + handlerConfig[i];
			// updated config
			hanldersStoreLookup.put(handlerId, handConfigUpdated);
		} else {
			IMap<String, String> hanldersStoreFqcn = hazelcastInstance.getMap(FeatureServiceHanldersFQCN);
			if (hanldersStoreFqcn.containsKey(handlerId)) {
				String handlerConfigStr = hanldersStoreFqcn.get(handlerId);
				String handlerConfig[] = handlerConfigStr.split(ESCAPE_CHAR + VALUE_SEPERATOR);
				if (execute != null && (execute.equalsIgnoreCase("sync") || execute.equalsIgnoreCase("async")))
					handlerConfig[5] = execute.toLowerCase();
				if (type != null && validateType(type))
					handlerConfig[4] = type.toLowerCase();
				if (fqcn != null && !fqcn.trim().isEmpty()) {
					handlerConfig[3] = fqcn.trim();
					handlerConfig[2] = "instance";
					if (handlerConfig.length == 7 && config != null && !config.trim().isEmpty())
						handlerConfig[6] = config.trim();
					else {
						config = "";
						handlerConfig[handlerConfig.length - 1] = handlerConfig[handlerConfig.length - 1]
								+ VALUE_SEPERATOR;
					}
					HandlerCacheSerivce.initializeHandler(fqcn.trim(), featureServiceHanldersMapKey, config.trim());
				}
				String handConfigUpdated = "";
				for (int i = 0; i < handlerConfig.length; i++)
					if (i != handlerConfig.length - 1)
						handConfigUpdated = handConfigUpdated + handlerConfig[i] + VALUE_SEPERATOR;
					else
						handConfigUpdated = handConfigUpdated + handlerConfig[i];
				// updated config
				hanldersStoreFqcn.put(handlerId, handConfigUpdated);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private boolean validateType(String type) {
		boolean containsInvokeType = false;
		switch (type.toLowerCase()) {
		case "pre-service":
			return !containsInvokeType;
		case "post-service":
			return !containsInvokeType;
		case "pre-exec":
			return !containsInvokeType;
		case "post-exec":
			return !containsInvokeType;
		case "pre-exec-enrichment":
			return !containsInvokeType;
		case "pre-impl-selection":
			return !containsInvokeType;
		case "pre-impl-enrichment":
			return !containsInvokeType;
		case "pre-impl":
			return !containsInvokeType;
		case "*":
			return !containsInvokeType;
		}
		logger.error("{} Invocation type is invalid!  : {} ", LEAP_LOG_KEY, type);
		return containsInvokeType;

	}

	// destroy map config, cache and disable in db.
	public void disableAllFeatureLevelHandlers(RequestContext requestContext)
			throws ServiceHandlerConfigurationException {
		String methodName = "disableAllFeatureLevelHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int nodeId = getConfigNodeId(requestContext);
		if (updateFeatureLevelFeatureServiceHandlerToDB(new ConfigurationContext(requestContext), false, nodeId) > 0) {
			// feature config
			String featureServiceHanldersMapKey = nodeId + KEY_SEPERATOR + FeatureServiceHanlders;
			IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(featureServiceHanldersMapKey);

			// service cache handlers map
			String featServiceHandlerMap = nodeId + KEY_SEPERATOR + Feature_Handlers;
			IMap<String, List<String>> serviceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
			String appServiceHandlerMap = nodeId + KEY_SEPERATOR + APP_Handlers;
			IMap<String, List<String>> appServiceHandlerMapData = hazelcastInstance.getMap(appServiceHandlerMap);
			serviceHandlerMapData.destroy();
			featureHandlersMap.destroy();
			appServiceHandlerMapData.destroy();
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	// reload map config, cache and enable in db.
	public void enableAllFeatureLevelHandlers(RequestContext requestContext, FeaturesServiceInfo feaureServiceInfo,
			FeatureServiceHandler featureServiceHandler)
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException {
		String methodName = "enableAllFeatureLevelHandlers";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int nodeId = getConfigNodeId(requestContext);
		ConfigurationContext configurationContext = new ConfigurationContext(requestContext);
		if (updateFeatureLevelFeatureServiceHandlerToDB(configurationContext, true, nodeId) > 0) {
			addFeatureServiceHandlerConfiguration(configurationContext, featureServiceHandler, nodeId);
			cacheHandlersForEachService(configurationContext, feaureServiceInfo);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * get all the handlers to be invoked on the particular service and store into
	 * list in the way : </br>
	 * {nodeId}-Apphandlers/FeatureHandlers --></br>
	 * {sn ->
	 * handlerId|#ACSH/AFSH/FFSH|#instance|#{fqcn}|#{per/post/both}|#{sync/async}|#{handlerConfig},</br>
	 * handlerId|#ACSH/AFSH/FFSH|#lookup|#{beanId}|#{per/post/both}|#{sync/async}|#{handlerConfig},...]}
	 * 
	 * @param tenant
	 * @param site
	 * @param feaureServiceInfo
	 * @throws ServiceHandlerConfigurationException
	 */
	public void cacheHandlersForEachService(ConfigurationContext configContext, FeaturesServiceInfo feaureServiceInfo)
			throws ServiceHandlerConfigurationException {
		String methodName = "cacheHandlersForEachService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String featureGrp = feaureServiceInfo.getFeatures().getFeatureGroup();
		String featureName = feaureServiceInfo.getFeatures().getFeature().getFeatureName();
		String tenant = configContext.getTenantId();
		String site = configContext.getSiteId();
		String vendorName = configContext.getVendorName();
		String version = configContext.getVersion();
		String implementation = configContext.getImplementationName();
		String excludeCommonServiceHanldersMapKey = tenant + KEY_SEPERATOR + site + KEY_SEPERATOR
				+ AppCommonServiceHanlders;
		String allcommonServiceHanldersMapKey = tenant + KEY_SEPERATOR + site + KEY_SEPERATOR
				+ ALL_AppCommonServiceHanlders;
		String includeAppFeatureServiceHanldersMapKey = tenant + KEY_SEPERATOR + site + KEY_SEPERATOR
				+ AppFeatureServiceHanlders;

		logger.debug(
				" {} ConfigurationContext-Object: tenantId-{}, siteId-{}, vendorName-{}, version-{}, featureGroup-{}, featureName-{}, implName-{}",
				LEAP_LOG_KEY, tenant, site, vendorName, version, featureGrp, featureName, implementation);
		Integer configNodeId = getConfigNodeId(tenant, site, vendorName, implementation, version, featureGrp,
				featureName);
		String includeFeatureServiceHanldersMapKey = configNodeId + KEY_SEPERATOR + FeatureServiceHanlders;

		IMap<String, List<String>> excludecommomnHandlersMap = hazelcastInstance
				.getMap(excludeCommonServiceHanldersMapKey);
		IList<String> allCommonHandlersMap = hazelcastInstance.getList(allcommonServiceHanldersMapKey);
		IMap<String, List<String>> appFeatureHandlersMap = hazelcastInstance
				.getMap(includeAppFeatureServiceHanldersMapKey);
		IMap<String, List<String>> featureHandlersMap = hazelcastInstance.getMap(includeFeatureServiceHanldersMapKey);

		logger.trace("{} excludecommomnHandlersMap Map : ", LEAP_LOG_KEY);
		excludecommomnHandlersMap
				.forEach((k, v) -> logger.debug("{} excludecommomnHandlersMap -> {}:{}", LEAP_LOG_KEY, k, v));
		logger.trace("{} allCommonHandlersMap Map : ", LEAP_LOG_KEY);
		allCommonHandlersMap.forEach((k) -> logger.debug("{} allCommonHandlersMap -> {}", LEAP_LOG_KEY, k));
		logger.trace("{} appFeatureHandlersMap Map : " + LEAP_LOG_KEY);
		appFeatureHandlersMap.forEach((k, v) -> logger.debug("{} appFeatureHandlersMap -> {}:{}", LEAP_LOG_KEY, k, v));
		logger.trace("{} featureHandlersMap Map : ", LEAP_LOG_KEY);
		featureHandlersMap.forEach((k, v) -> logger.debug("{} featureHandlersMap -> {}:{}", LEAP_LOG_KEY, k, v));

		for (com.attunedlabs.feature.jaxb.Service service : feaureServiceInfo.getFeatures().getFeature().getService()) {
			String serviceName = service.getName();
			List<String> invokeHandlers = new ArrayList<>();

			// check for app common service handlers
			List<String> mergedList = mergeAllPossibilitiesForCommonHandler(featureGrp, featureName, serviceName,
					excludecommomnHandlersMap, allCommonHandlersMap);
			List<String> invokeCommonHandlers = getHandlersDataForHandlerIDs(mergedList, AppCommonServiceHanldersFQCN,
					AppCommonServiceHanldersLookup, AppCommonServiceHanlders);
			invokeHandlers.addAll(invokeCommonHandlers);

			// check for app feature service handlers
			List<String> mergedList0 = mergeAllPossibilitiesForAppFeatureHandler(featureGrp, featureName, serviceName,
					appFeatureHandlersMap);
			List<String> invokeAppFeatureHandlers = getHandlersDataForHandlerIDs(mergedList0,
					AppFeatureServiceHanldersFQCN, AppFeatureServiceHanldersLookup, AppFeatureServiceHanlders);
			invokeHandlers.addAll(invokeAppFeatureHandlers);

			// check for feature service handlers
			List<String> mergedList1 = mergeAllPossibilitiesForFeatureHandler(serviceName, featureHandlersMap);
			List<String> invokeFeatureHandlers = getHandlersDataForHandlerIDs(mergedList1, FeatureServiceHanldersFQCN,
					FeatureServiceHanldersLookup, FeatureServiceHanlders);

			// add the handlers to be invoked for a particular service.
			String appServiceHandlerMap = configNodeId + KEY_SEPERATOR + APP_Handlers;
			String featServiceHandlerMap = configNodeId + KEY_SEPERATOR + Feature_Handlers;
			IMap<String, List<String>> appServiceHandlerMapData = hazelcastInstance.getMap(appServiceHandlerMap);
			appServiceHandlerMapData.put(serviceName, invokeHandlers);
			IMap<String, List<String>> featServiceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
			featServiceHandlerMapData.put(serviceName, invokeFeatureHandlers);
		}
		// to remove the below code just for testing
		String appServiceHandlerMap = configNodeId + KEY_SEPERATOR + APP_Handlers;
		String featServiceHandlerMap = configNodeId + KEY_SEPERATOR + Feature_Handlers;
		IMap<String, List<String>> serviceHandlerMapData = hazelcastInstance.getMap(appServiceHandlerMap);
		serviceHandlerMapData.forEach((k, v) -> logger.debug("{} {} : {}",LEAP_LOG_KEY,k,v));
		IMap<String, List<String>> featServiceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
		featServiceHandlerMapData.forEach((k, v) -> logger.debug("{} {} : {}",LEAP_LOG_KEY,k,v));

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method return the application level handlers associated for particular
	 * service.
	 * 
	 * @param nodeId
	 * @param serviceName
	 * @return
	 */
	public List<String> getApplicationLevelHandlersForService(Integer nodeId, String serviceName) {
		String methodName = "getApplicationLevelHandlersForService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String appServiceHandlerMap = nodeId + KEY_SEPERATOR + APP_Handlers;
		IMap<String, List<String>> appServiceHandlerMapData = hazelcastInstance.getMap(appServiceHandlerMap);
		List<String> appHandlerList = appServiceHandlerMapData.get(serviceName);
		if (appHandlerList == null)
			appHandlerList = new ArrayList<>();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return appHandlerList;
	}

	/**
	 * This method return the feature level handlers associated for particular
	 * service.
	 * 
	 * @param nodeId
	 * @param serviceName
	 * @return
	 */
	public List<String> getFeatureLevelHandlersForService(Integer nodeId, String serviceName) {
		String methodName = "getFeatureLevelHandlersForService";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String featServiceHandlerMap = nodeId + KEY_SEPERATOR + Feature_Handlers;
		IMap<String, List<String>> featServiceHandlerMapData = hazelcastInstance.getMap(featServiceHandlerMap);
		logger.trace("{} featServiceHandlerMapData is : {} ", LEAP_LOG_KEY, featServiceHandlerMapData);
		List<String> featHandlerList = featServiceHandlerMapData.get(serviceName);
		logger.trace("{} featHandlerList:  {} ", LEAP_LOG_KEY, featHandlerList);
		if (featHandlerList == null)
			featHandlerList = new ArrayList<>();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return featHandlerList;

	}

	private List<String> mergeAllPossibilitiesForCommonHandler(String featureGrp, String featureName,
			String serviceName, IMap<String, List<String>> excludecommomnHandlersMap,
			IList<String> allCommonHandlersMap) {
		String methodName = "mergeAllPossibilitiesForCommonHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		// fg-fn-sn
		String serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR + serviceName;
		List<String> handlers = excludecommomnHandlersMap.get(serviceHandlersKey);
		// fg-*-sn
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + WILDCARD + KEY_SEPERATOR + serviceName;
		List<String> handlers0 = excludecommomnHandlersMap.get(serviceHandlersKey);
		// fg-fn-*
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR + WILDCARD;
		List<String> handlers1 = excludecommomnHandlersMap.get(serviceHandlersKey);
		// fg-*-*
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + WILDCARD + KEY_SEPERATOR + WILDCARD;
		List<String> handlers2 = excludecommomnHandlersMap.get(serviceHandlersKey);

		List<String> mergedHandlers = new ArrayList<>();
		for (String handlerId : allCommonHandlersMap) {
			boolean flag = true;
			if (handlers != null)
				if (handlers.contains(handlerId))
					flag = false;
			if (handlers0 != null && flag)
				if (handlers0.contains(handlerId))
					flag = false;
			if (handlers1 != null && flag)
				if (handlers1.contains(handlerId))
					flag = false;
			if (handlers2 != null && flag)
				if (handlers2.contains(handlerId))
					flag = false;
			if (flag)
				mergedHandlers.add(handlerId);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return mergedHandlers;
	}

	private List<String> mergeAllPossibilitiesForAppFeatureHandler(String featureGrp, String featureName,
			String serviceName, IMap<String, List<String>> featureHandlersMap) {
		String methodName = "mergeAllPossibilitiesForAppFeatureHandler";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		// fg-fn-sn
		String serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR + serviceName;
		List<String> handlers = featureHandlersMap.get(serviceHandlersKey);
		// fg-*-sn
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + WILDCARD + KEY_SEPERATOR + serviceName;
		List<String> handlers0 = featureHandlersMap.get(serviceHandlersKey);
		// fg-fn-*
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + featureName + KEY_SEPERATOR + WILDCARD;
		List<String> handlers1 = featureHandlersMap.get(serviceHandlersKey);
		// fg-*-*
		serviceHandlersKey = featureGrp + KEY_SEPERATOR + WILDCARD + KEY_SEPERATOR + WILDCARD;
		List<String> handlers2 = featureHandlersMap.get(serviceHandlersKey);

		List<String> mergedHandlers = new ArrayList<>();
		if (handlers != null)
			mergedHandlers.addAll(handlers);
		if (handlers0 != null)
			mergedHandlers.addAll(handlers0);
		if (handlers1 != null)
			mergedHandlers.addAll(handlers1);
		if (handlers2 != null)
			mergedHandlers.addAll(handlers2);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return mergedHandlers;
	}

	private List<String> mergeAllPossibilitiesForFeatureHandler(String serviceName,
			IMap<String, List<String>> featureHandlersMap) {
		String methodName = "#";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		// sn
		String serviceHandlersKey = serviceName;
		List<String> handlers = featureHandlersMap.get(serviceHandlersKey);
		// *
		serviceHandlersKey = WILDCARD;
		List<String> handlers0 = featureHandlersMap.get(serviceHandlersKey);

		List<String> mergedHandlers = new ArrayList<>();
		if (handlers != null)
			mergedHandlers.addAll(handlers);
		if (handlers0 != null)
			mergedHandlers.addAll(handlers0);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return mergedHandlers;
	}

	/**
	 * adding whether to take instance or by bean lookup handler instance in handler
	 * store.
	 * 
	 * @param mergedList
	 * @param fqcnKey
	 * @param beanRefKey
	 * @return
	 */
	private List<String> getHandlersDataForHandlerIDs(List<String> mergedList, String fqcnKey, String beanRefKey,
			String handlerType) {
		List<String> invokeHandlers = new ArrayList<>();
		for (String handlerId : mergedList)
			invokeHandlers.add(handlerType + KEY_SEPERATOR + handlerId);
		return invokeHandlers;
	}

	/**
	 * inserting the Feature level Feature Service Handlers to database at
	 * configContext
	 * 
	 * @param commonServiceHandler
	 * @param configContext
	 * @return
	 * @throws ServiceHandlerConfigurationException
	 */
	private Integer addFeatureLevelFeatureServiceHandlerToDB(FeatureServiceHandler featureServiceHandler,
			ConfigurationContext configContext) throws ServiceHandlerConfigurationException {
		String methodName = "addFeatureLevelFeatureServiceHandlerToDB";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId;

		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String vendorName = configContext.getVendorName();
		String version = configContext.getVersion();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();
		String implementation = configContext.getImplementationName();
		try {
			logger.debug(
					"{} ConfigurationContext-Object: tenantId-{} , siteId-{}, vendorName-{}, version-{}, , featureGroup-{}, featureName-{} , impl name-{} ",
					LEAP_LOG_KEY, tenantId, siteId, vendorName, version, featureGroup, featureName, implementation);
			configNodeId = getConfigNodeId(tenantId, siteId, vendorName, implementation, version, featureGroup,
					featureName);
			String evtFwkXMLStr = serviceHandlerConfigXMLParser
					.unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML(featureServiceHandler);

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, FeatureServiceHanlders, evtFwkXMLStr,
					Feature_Handlers);

			configNodeData.setEnabled(true);
			configNodeData.setConfigLoadStatus("Success");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(configNodeId, FeatureServiceHanlders, Feature_Handlers);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				logger.debug("{} added feature service handlers at nodeId : {}", LEAP_LOG_KEY, configDataId);
			}
		} catch (ConfigPersistenceException | ServiceHandlerConfigParserException e) {
			throw new ServiceHandlerConfigurationException(
					"Failed to add ServiceHandlers for  " + FeatureServiceHanlders, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configNodeId;
	}

	/**
	 * updating the Feature level Feature Service Handlers to database at
	 * configContext
	 * 
	 * @param commonServiceHandler
	 * @param configContext
	 * @return
	 * @return
	 * @throws ServiceHandlerConfigurationException
	 */
	private int updateFeatureLevelFeatureServiceHandlerToDB(ConfigurationContext configContext, boolean flag,
			int configNodeId) throws ServiceHandlerConfigurationException {
		String methodName = "updateFeatureLevelFeatureServiceHandlerToDB";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int updateConfigNodeId;
		try {

			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(configNodeId, FeatureServiceHanlders, Feature_Handlers);
			loadedConfigNodeData.setEnabled(flag);
			configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			logger.debug("{} updating feature service handlers at nodeId : {} " ,LEAP_LOG_KEY, configNodeId);
			updateConfigNodeId = configPersistenceService.updateConfigNodeData(loadedConfigNodeData);
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
			return updateConfigNodeId;
		} catch (ConfigPersistenceException e) {
			throw new ServiceHandlerConfigurationException(
					"Failed to update ServiceHandlers for  " + FeatureServiceHanlders, e);
		}
		
	}

	/**
	 * inserting the app level Feature Service Handlers to database at configContext
	 * 
	 * @param commonServiceHandler
	 * @param configContext
	 * @throws ServiceHandlerConfigurationException
	 */
	private void addAppFeatureServiceHandlerToDB(
			com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler,
			ConfigurationContext configContext) throws ServiceHandlerConfigurationException {
		String methodName = "addAppFeatureServiceHandlerToDB";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId;

		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String vendorName = configContext.getVendorName();
		String version = configContext.getVersion();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();
		String implementation = configContext.getImplementationName();
		try {
			logger.debug(
					"{} ConfigurationContext-Object: tenantId-{} , siteId-{}, vendorName-{}, version-{}, , featureGroup-{}, featureName-{} , impl name-{} ",
					LEAP_LOG_KEY, tenantId, siteId, vendorName, version, featureGroup, featureName, implementation);
			configNodeId = getConfigNodeId(tenantId, siteId, vendorName, implementation, version, featureGroup,
					featureName);
			String evtFwkXMLStr = serviceHandlerConfigXMLParser
					.unmarshallFeatureServiceHandlerObjecttoXML(featureServiceHandler);

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, AppFeatureServiceHanlders, evtFwkXMLStr,
					APP_Handlers);

			configNodeData.setEnabled(true);
			configNodeData.setConfigLoadStatus("Success");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(configNodeId, AppFeatureServiceHanlders, APP_Handlers);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				logger.debug("{} added feature service handlers at nodeId : {} " ,LEAP_LOG_KEY, configDataId);
			}
		} catch (ConfigPersistenceException | ServiceHandlerConfigParserException e) {
			throw new ServiceHandlerConfigurationException(
					"Failed to add ServiceHandlers for  " + AppFeatureServiceHanlders, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	/**
	 * inserting the Common Service Handlers to database at configContext
	 * 
	 * @param commonServiceHandler
	 * @param configContext
	 * @return
	 * @throws ServiceHandlerConfigurationException
	 */
	private Integer addAppCommonServiceHandlerToDB(CommonServiceHandler commonServiceHandler,
			ConfigurationContext configContext) throws ServiceHandlerConfigurationException {
		String methodName = "addAppCommonServiceHandlerToDB";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Integer configNodeId;

		String tenantId = configContext.getTenantId();
		String siteId = configContext.getSiteId();
		String vendorName = configContext.getVendorName();
		String version = configContext.getVersion();
		String featureGroup = configContext.getFeatureGroup();
		String featureName = configContext.getFeatureName();
		String implementation = configContext.getImplementationName();
		try {
			logger.debug(
					"{} ConfigurationContext-Object: tenantId-{} , siteId-{}, vendorName-{}, version-{}, , featureGroup-{}, featureName-{} , impl name-{} ",
					LEAP_LOG_KEY, tenantId, siteId, vendorName, version, featureGroup, featureName, implementation);
			configNodeId = getConfigNodeId(tenantId, siteId, vendorName, implementation, version, featureGroup,
					featureName);
			String evtFwkXMLStr = serviceHandlerConfigXMLParser
					.unmarshallCommonServiceHandlerObjecttoXML(commonServiceHandler);

			ConfigNodeData configNodeData = new ConfigNodeData(configNodeId, AppCommonServiceHanlders, evtFwkXMLStr,
					APP_Handlers);

			configNodeData.setEnabled(true);
			configNodeData.setConfigLoadStatus("Success");
			// Check if it exist in the db or not if not exist insert into DB.
			IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
			ConfigNodeData loadedConfigNodeData = configPersistenceService
					.getConfigNodeDatabyNameAndNodeId(configNodeId, AppCommonServiceHanlders, APP_Handlers);
			if (loadedConfigNodeData == null) {
				configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
				int configDataId = configPersistenceService.insertConfigNodeData(configNodeData);
				logger.debug("{} added common service handlers at nodeId : {} " ,LEAP_LOG_KEY, configDataId);
			}
		} catch (ConfigPersistenceException | ServiceHandlerConfigParserException e) {
			throw new ServiceHandlerConfigurationException(
					"Failed to add ServiceHandlers for  " + AppCommonServiceHanlders, e);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configNodeId;

	}

	/**
	 * updating confignodedata to add and remove particular service.
	 * 
	 * @param nodeId
	 * @param featureGroup
	 * @param featureName
	 * @param serviceName
	 * @param addOrRemove
	 * @param handlerId
	 * @return
	 * @throws ConfigPersistenceException
	 */
	private int updateConfigNodeData(int nodeId, String featureGroup, String featureName, String serviceName,
			String addOrRemove, String handlerId) throws ConfigPersistenceException {
		String methodName = "updateConfigNodeData";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		IConfigPersistenceService configPersistenceService = new ConfigPersistenceServiceMySqlImpl();
		ConfigNodeData configNodeData = null;
		com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler serviceHandler = null;
		ServiceHandlerConfiguration serviceHandlerConfiguration = null;
		try {
			configNodeData = configPersistenceService.getConfigNodeDatabyNameAndNodeId(nodeId, FeatureServiceHanlders,
					Feature_Handlers);
			if (configNodeData != null) {
				String configXMLFile = configNodeData.getConfigData();
				serviceHandlerConfiguration = serviceHandlerConfigXMLParser.marshallConfigXMLtoObject(configXMLFile);
				List<com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler> serviceHandlerList = serviceHandlerConfiguration
						.getFeatureServiceHandler().getServiceHandler();
				if (serviceHandlerList == null)
					serviceHandlerList = new ArrayList<>();
				for (com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler serviceHandlerCheck : serviceHandlerList) {
					if (serviceHandlerCheck.getHandlerId().equals(handlerId)) {
						serviceHandler = serviceHandlerCheck;
						for (com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service prePresent : serviceHandler
								.getIncludedServices().getService()) {
							if (addOrRemove.equals("add") && prePresent.getFeatureGroup().equals(featureGroup)
									&& prePresent.getFeatureName().equals(featureName)
									&& (prePresent.getServiceName().equals(serviceName)
											|| prePresent.getServiceName().equals("*")))
								return 0;
						}
					}
				}
			} else {
				logger.error("{} failed to fetch featurehandler configuration from confignodedat table for nodeId: {}, configNodeData : {}",LEAP_LOG_KEY, nodeId,configNodeData);
				return 0;
			}

			switch (addOrRemove) {
			case "add":
				serviceHandlerConfiguration.getFeatureServiceHandler().getServiceHandler().remove(serviceHandler);
				com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service serviceToAdd = new com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service();
				serviceToAdd.setServiceName(serviceName);
				serviceToAdd.setFeatureGroup(featureGroup);
				serviceToAdd.setFeatureName(featureName);
				serviceHandler.getIncludedServices().getService().add(serviceToAdd);
				serviceHandlerConfiguration.getFeatureServiceHandler().getServiceHandler().add(serviceHandler);
				String evtFwkXMLStr = serviceHandlerConfigXMLParser
						.unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML(
								serviceHandlerConfiguration.getFeatureServiceHandler());
				configNodeData.setConfigData(evtFwkXMLStr);
				return configPersistenceService.updateConfigNodeData(configNodeData);
			case "remove":
				com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service serviceHandlerToRemove = null;
				for (com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler.ServiceHandler.IncludedServices.Service serviceItr : serviceHandler
						.getIncludedServices().getService()) {
					if (serviceItr.getServiceName().equals(serviceName))
						serviceHandlerToRemove = serviceItr;
				}
				if (serviceHandlerToRemove != null)
					serviceHandler.getIncludedServices().getService().remove(serviceHandlerToRemove);
				String evtFwkXMLStrs = serviceHandlerConfigXMLParser
						.unmarshallFeatureLevelFeatureServiceHandlerObjecttoXML(
								serviceHandlerConfiguration.getFeatureServiceHandler());
				configNodeData.setConfigData(evtFwkXMLStrs);
				return configPersistenceService.updateConfigNodeData(configNodeData);
			}
		} catch (ConfigPersistenceException | ServiceHandlerConfigParserException e) {
			logger.error("{} failed to fetch featurehandler configuration from confignodedat table for nodeId: {}",LEAP_LOG_KEY, nodeId,
					e);
			return 0;
		}
		return 0;

	}

	/**
	 * locally invoked to get the configurationNodeId , once insertion is success
	 * full, checks for the version availability and when not available
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param vendorName
	 * @param version
	 * @param featureGroup
	 * @param featureName
	 * @return ConfigurationNodeId, after inserting the data
	 * @throws ServiceHandlerConfigurationException
	 * @throws IntegrationPipelineConfigException
	 */
	public int getConfigNodeId(String tenantId, String siteId, String vendorName, String implName, String version,
			String featureGroup, String featureName) throws ServiceHandlerConfigurationException {
		String methodName = "getConfigNodeId";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		int configNodeId = 0;
		try {
			if (!vendorName.isEmpty() && !version.isEmpty()) {
				configNodeId = getApplicableNodeIdVendorName(tenantId, siteId, featureGroup, featureName, implName,
						vendorName, version);
				logger.debug("{} Applicable nodeId is -> {} " ,LEAP_LOG_KEY, configNodeId);
			} else if (vendorName.isEmpty() && version.isEmpty()) {
				configNodeId = getApplicableNodeIdFeatureName(tenantId, siteId, featureGroup, featureName, implName);
				logger.debug("{} Applicable nodeId is -> {} " ,LEAP_LOG_KEY, configNodeId);
			} // ..end of if-else, conditional check with vendor-version support
		} catch (InvalidNodeTreeException | ConfigPersistenceException persistanceException) {
			throw new ServiceHandlerConfigurationException(
					"Failed loading nodeId, when version and vendor is empty for tenantId-" + tenantId + ", siteId-"
							+ siteId + ", vendorName-" + vendorName + ", version-" + version + ", featureGroup-"
							+ featureGroup + ", featureName-" + featureName + ", impl name : " + implName,
					persistanceException);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return configNodeId;
	}// ..end of the method

	public int getConfigNodeId(RequestContext requestContext) throws ServiceHandlerConfigurationException {
		return getConfigNodeId(requestContext.getTenantId(), requestContext.getSiteId(), requestContext.getVendor(),
				requestContext.getImplementationName(), requestContext.getVersion(), requestContext.getFeatureGroup(),
				requestContext.getFeatureName());
	}

}
