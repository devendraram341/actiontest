package com.attunedlabs.servicehandlers.config.impl;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.servicehandlers.config.IServiceHandlerConfigurationService;
import com.attunedlabs.servicehandlers.config.ServiceHandlerConfigurationException;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler;

public class ServiceHandlerConfigurationService implements IServiceHandlerConfigurationService {
	final static Logger logger = LoggerFactory.getLogger(ServiceHandlerConfigurationService.class);
	private ServiceHandlerConfigurationHelper serviceHandlerConfigurationHelper = new ServiceHandlerConfigurationHelper();

	@Override
	public void addApplicationCommonServiceHandlersConfiguration(CommonServiceHandler commonServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		logger.trace("{} inside addApplicationCommonServiceHandlersConfiguration()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.addApplicationCommonServicesConfiguration(commonServiceHandler,
				configContext);
	}

	@Override
	public void addApplicationFeatureServiceHandlersConfiguration(
			com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		logger.trace("{} inside addApplicationFeatureServiceHandlersConfiguration()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.addApplicationFeatureServiceHandlersConfiguration(featureServiceHandler,
				configContext);
	}

	@Override
	public void addFeatureServiceHandlerConfiguration(ConfigurationContext configurationContext,
			FeatureServiceHandler featureServiceHandler)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException {
		logger.trace("{} inside addFeatureServiceHandlerConfiguration()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.addFeatureServiceHandlerConfiguration(configurationContext,
				featureServiceHandler, null);
	}

	@Override
	public void cacheHandlersForEachService(ConfigurationContext configContext, FeaturesServiceInfo feaureServiceInfo)
			throws ServiceHandlerConfigurationException {
		logger.trace("{} inside cacheHandlersForEachService()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.cacheHandlersForEachService(configContext, feaureServiceInfo);
	}

	@Override
	public List<String> getApplicationLevelHandlersForService(Integer nodeId, String serviceName) {
		logger.trace("{} inside getApplicationLevelHandlersForService()...",LEAP_LOG_KEY);
		return serviceHandlerConfigurationHelper.getApplicationLevelHandlersForService(nodeId, serviceName);
	}

	@Override
	public List<String> getFeatureLevelHandlersForService(Integer nodeId, String serviceName) {
		logger.trace("{} inside getFeatureLevelHandlersForService()...",LEAP_LOG_KEY);
		return serviceHandlerConfigurationHelper.getFeatureLevelHandlersForService(nodeId, serviceName);
	}

	@Override
	public void updateFSHandlersConfiguration(RequestContext requestContext, String handlerId, String execute,
			String type, String fqcn, String beanRefId, String config)
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException {
		logger.trace("{} inside updateFSHandlersConfiguration()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.updateFSHandlersConfiguration(requestContext, handlerId, execute, type, fqcn,
				beanRefId, config);
	}

	@Override
	public void includeNewService(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException {
		logger.trace("{} inside includeNewService()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.includeNewService(handlerId, requestContext, serviceName);
	}

	@Override
	public void removeServicesForHandler(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException {
		logger.trace("{} inside removeServicesForHandler()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.removeServicesForHandler(handlerId, requestContext, serviceName);
	}

	@Override
	public void disableAllFeatureLevelHandlers(RequestContext requestContext)
			throws ServiceHandlerConfigurationException {
		logger.trace("{} inside disableAllFeatureLevelHandlers()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.disableAllFeatureLevelHandlers(requestContext);
	}

	@Override
	public void enableAllFeatureLevelHandlers(RequestContext requestContext, FeaturesServiceInfo feaureServiceInfo,
			FeatureServiceHandler featureServiceHandler)
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException {
		logger.trace("{} inside enableAllFeatureLevelHandlers()...",LEAP_LOG_KEY);
		serviceHandlerConfigurationHelper.enableAllFeatureLevelHandlers(requestContext, feaureServiceInfo,
				featureServiceHandler);
	}

	@Override
	public int getConfigNodeId(String tenantId, String siteId, String vendorName, String implName, String version,
			String featureGroup, String featureName) throws ServiceHandlerConfigurationException {
		logger.trace("{} inside getConfigNodeId()...",LEAP_LOG_KEY);
		return serviceHandlerConfigurationHelper.getConfigNodeId(tenantId, siteId, vendorName, implName, version,
				featureGroup, featureName);
	}

}
