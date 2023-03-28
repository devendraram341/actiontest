package com.attunedlabs.servicehandlers.config;

import java.util.List;

import com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.CommonServiceHandler;
import com.attunedlabs.config.ConfigurationContext;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigPersistenceException;
import com.attunedlabs.core.BeanDependencyResolveException;
import com.attunedlabs.feature.jaxb.FeaturesServiceInfo;
import com.attunedlabs.servicehandlers.config.jaxb.ServiceHandlerConfiguration.FeatureServiceHandler;

/**
 * <code>IServiceHandlerConfigurationService</code> is for putting,fetching and
 * modifying the service handlers configuration from your store.
 * 
 * @author Reactiveworks42
 *
 */
public interface IServiceHandlerConfigurationService {

	/**
	 * This method is for storing the application level common service handlers
	 * anywhere you want.
	 * 
	 * @param commonServiceHandler
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addApplicationCommonServiceHandlersConfiguration(CommonServiceHandler commonServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException;

	/**
	 * This method is for storing the application level feature service handlers
	 * anywhere you want.
	 * 
	 * @param featureServiceHandler
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addApplicationFeatureServiceHandlersConfiguration(
			com.attunedlabs.applicationservicehandlers.config.jaxb.ApplicationServiceHandlerConfiguration.FeatureServiceHandler featureServiceHandler,
			ConfigurationContext configContext)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException;

	/**
	 * This method is for storing the feature service handlers anywhere you
	 * want.
	 * 
	 * @param configurationContext
	 * @param featureServiceHandler
	 * @throws BeanDependencyResolveException
	 * @throws ServiceHandlerConfigurationException
	 */
	public void addFeatureServiceHandlerConfiguration(ConfigurationContext configurationContext,
			FeatureServiceHandler featureServiceHandler)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException;

	/**
	 * Maintaining the handlers to be invoked for services in feature-service.
	 * 
	 * @param configContext
	 * @param feaureServiceInfo
	 * @throws ServiceHandlerConfigurationException
	 */
	public void cacheHandlersForEachService(ConfigurationContext configContext, FeaturesServiceInfo feaureServiceInfo)
			throws ServiceHandlerConfigurationException;

	/**
	 * Fetching the details of application level serviceHandlers respective to
	 * the specified service.[ ACSH|AFSH|FSH-{handlerId} ]
	 * 
	 * @param nodeId
	 * @param feaureServiceInfo
	 */
	public List<String> getApplicationLevelHandlersForService(Integer nodeId, String serviceName);

	/**
	 * Fetching the details of feature level serviceHandlers respective to the
	 * specified service.[ ACSH|AFSH|FSH-{handlerId} ]
	 * 
	 * @param nodeId
	 * @param feaureServiceInfo
	 */
	public List<String> getFeatureLevelHandlersForService(Integer nodeId, String serviceName);

	/**
	 * This method return the configNodeId.
	 * 
	 * @param tenantId
	 * @param siteId
	 * @param vendorName
	 * @param implName
	 * @param version
	 * @param featureGroup
	 * @param featureName
	 * @return
	 * @throws ServiceHandlerConfigurationException
	 */
	public int getConfigNodeId(String tenantId, String siteId, String vendorName, String implName, String version,
			String featureGroup, String featureName) throws ServiceHandlerConfigurationException;

	/**
	 * This method is to update the FeatureServiceHandler Configuration for the
	 * specified handler. Null parameter will be considered as non-updated
	 * values.
	 * 
	 * @param handlerId
	 * @param configContext
	 */
	public void updateFSHandlersConfiguration(RequestContext requestContext, String handlerId, String execute,
			String type, String fqcn, String beanRefId, String config)
			throws BeanDependencyResolveException, ServiceHandlerConfigurationException;

	/**
	 * This method is to add new service to be included for the specified
	 * handler.
	 * 
	 * @param handlerId
	 * @param requestContext
	 * @param serviceName
	 */
	public void includeNewService(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException;

	/**
	 * This method is to remove the specified services for the provided
	 * handlerId.
	 * 
	 * @param handlerId
	 * @param requestContext
	 * @param serviceName
	 */
	public void removeServicesForHandler(String handlerId, RequestContext requestContext, String serviceName)
			throws ServiceHandlerConfigurationException, ConfigPersistenceException;

	/**
	 * This method is to disable all the handlers when feature is disabled.
	 * 
	 *
	 * @param requestContext
	 */
	public void disableAllFeatureLevelHandlers(RequestContext requestContext)
			throws ServiceHandlerConfigurationException;

	/**
	 * This method is to enable all the handlers when the feature is enabled.
	 * 
	 * 
	 * @param requestContext
	 * @param feaureServiceInfo
	 * @param featureServiceHandler
	 */
	public void enableAllFeatureLevelHandlers(RequestContext requestContext, FeaturesServiceInfo feaureServiceInfo,
			FeatureServiceHandler featureServiceHandler)
			throws ServiceHandlerConfigurationException, BeanDependencyResolveException;

}
