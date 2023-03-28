package com.attunedlabs.leap;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.pipeline.PipelineContext;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.integrationfwk.config.IIntegrationPipeLineConfigurationService;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigException;
import com.attunedlabs.integrationfwk.config.IntegrationPipelineConfigUnit;
import com.attunedlabs.integrationfwk.config.impl.IntegrationPipelineConfigurationService;
import com.attunedlabs.leapentity.config.ILeapDataServiceConfiguration;
import com.attunedlabs.leapentity.config.LeapDataServiceConfigurationException;
import com.attunedlabs.leapentity.config.impl.LeapDataServiceConfiguration;
import com.attunedlabs.leapentity.config.jaxb.Entity;
import com.attunedlabs.permastore.config.IPermaStoreConfigurationService;
import com.attunedlabs.permastore.config.PermaStoreConfigRequestException;
import com.attunedlabs.permastore.config.impl.PermaStoreConfigurationService;
import com.attunedlabs.servicehandlers.AbstractServiceHandler;
import com.hazelcast.transaction.TransactionContext;

/**
 * This class is used to store the service request specific data
 * 
 * @author Reactiveworks
 *
 */
public class LeapServiceContext implements Serializable {
	private static Logger logger = LoggerFactory.getLogger(LeapServiceContext.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 8616947111318084011L;

	private String tenant;
	private String site;
	private String endpointType;
	private String featureGroup;
	private String featureName;
	private String requestUUID;
	// It is used to store vendor taxonomyId
	private String vendorTaxonomyId;
	// It is used to store taxonomyId from the request
	private String taxonomyId;

	private RequestContext requestContext;

	// We may not require hazelcastTransactional Context. It may be removed in
	// future
	private TransactionContext hazelcastTransactionalContext;

	// It is used to store the resources, like connection object for the request
	private Map<Object, Object> resourceHolder;

	// It is used to store the events need to be generated at the end (after exit of
	// the service)
	private Map<String, Object> eventHolder;

	// It is used to store the events need to be generated at the end (after exit of
	// the service) for successful
	// service completion
	private Map<String, Object> serviceCompletionEventHolder;

	// It is use to store the context ( all configuration and information about
	// running service)
	private List<LeapServiceRuntimeContext> leapServiceRuntimeContextList;

	public LeapServiceContext() {
		super();
	}

	/**
	 * Constructs a new LeapServiceContext object, when Authentication is false.
	 * 
	 * @param tenant
	 * @param site
	 */
	public LeapServiceContext(String tenant, String site) {
		this();
		this.tenant = tenant;
		this.site = site;
	}

	/**
	 * Constructs a new LeapServiceContext object, if you know tenant, site feature
	 * group and feature name
	 * 
	 * @param tenant       : tenant Id
	 * @param site         : siteId
	 * @param featureGroup : feature group
	 * @param featureName  : feature name
	 */
	public LeapServiceContext(String tenant, String site, String featureGroup, String featureName) {
		this();
		this.tenant = tenant;
		this.site = site;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
	}

	// All setter and getter are defined below
	public String getTenant() {
		return tenant;
	}

	public String getSite() {
		return site;
	}

	public String getFeatureGroup() {
		return featureGroup;
	}

	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public String getRequestUUID() {
		return requestUUID;
	}

	public void setRequestUUID(String requestUUID) {
		this.requestUUID = requestUUID;
	}

	public String getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(String endpointType) {
		this.endpointType = endpointType;
	}

	public String getVendorTaxonomyId() {
		return vendorTaxonomyId;
	}

	public void setVendorTaxonomyId(String vendorTaxonomyId) {
		this.vendorTaxonomyId = vendorTaxonomyId;
	}

	public String getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(String taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public Map<Object, Object> getResourceHolder() {
		// if resource holder object is not created the create new object and return
		// else,
		// return the existing resource holder object reference
		if (resourceHolder == null)
			resourceHolder = new HashMap<>();
		return resourceHolder;
	}

	public TransactionContext getHazelcastTransactionalContext() {
		return hazelcastTransactionalContext;
	}

	public void setHazelcastTransactionalContext(TransactionContext hazelcastTransactionalContext) {
		this.hazelcastTransactionalContext = hazelcastTransactionalContext;
	}

	public Map<String, Object> getEventHolder() {
		// if event holder object is not created the create new object and
		// return else, return the existing event holder object reference
		if (eventHolder == null)
			eventHolder = new HashMap<>();
		return eventHolder;
	}

	public Map<String, Object> getServiceCompletionEventHolder() {
		// if service completion event holder object is not created the create new
		// object and
		// return else, return the existing event holder object reference
		if (serviceCompletionEventHolder == null)
			serviceCompletionEventHolder = new HashMap<>();
		return serviceCompletionEventHolder;
	}

	public List<LeapServiceRuntimeContext> getLeapServiceRuntimeContextList() {
		if (leapServiceRuntimeContextList != null)
			return leapServiceRuntimeContextList;
		else {
			leapServiceRuntimeContextList = new ArrayList<>();
			return leapServiceRuntimeContextList;
		}
	}

	public void setLeapServiceRuntimeContextList(List<LeapServiceRuntimeContext> leapServiceRuntimeContextList) {
		this.leapServiceRuntimeContextList = leapServiceRuntimeContextList;
	}

	public void setRequestContext(String vendor, String version) {
		if (requestContext != null) {
			requestContext.setVendor(vendor);
			requestContext.setVersion(version);
		}
	}

	public void setRequestContext(String tenant, String site, String featureGroup, String feature,
			String implementationName, String vendor, String version, String provider) {
		RequestContext requestContext = new RequestContext(tenant, site, featureGroup, feature, implementationName,
				vendor, version, provider);
		this.requestContext = requestContext;

	}

	public void setRequestContext(String tenant, String site, String featureGroup, String feature,
			String implementationName, String vendor, String version) {
		RequestContext requestContext = new RequestContext(tenant, site, featureGroup, feature, implementationName,
				vendor, version);
		this.requestContext = requestContext;

	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	/**
	 * This method is use to get the implementation name from the current leap
	 * service runtime context
	 * 
	 * @return {@link String} - name of the implementation name
	 */
	public String getImplementationName() {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		String implName = null;
		if (leapServiceRuntimeContext != null)
			implName = leapServiceRuntimeContext.getImplementationName();
		return implName;
	}

	/**
	 * This method is use to get the vendor name from the current leap service
	 * runtime context
	 * 
	 * @return {@link String} - name of the vendor
	 */
	public String getVendor() {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		String vendor = null;
		if (leapServiceRuntimeContext != null)
			vendor = leapServiceRuntimeContext.getVendor();
		return vendor;
	}

	/**
	 * This method is use to get the version from the current leap service runtime
	 * context
	 * 
	 * @return {@link String} - name of the version
	 */
	public String getVersion() {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		String version = null;
		if (leapServiceRuntimeContext != null)
			version = leapServiceRuntimeContext.getVersion();
		return version;
	}

	/**
	 * This method is use to get the provider name from the current leap service
	 * runtime context
	 * 
	 * @return {@link String} - name of the provider
	 */
	public String getProvider() {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		String provider = null;
		if (leapServiceRuntimeContext != null)
			provider = leapServiceRuntimeContext.getProvider();
		return provider;
	}

	/**
	 * This method is use to set the implementation name from the current leap
	 * service runtime context
	 * 
	 * @param implementationName - name of the implementationName
	 */
	public void setImplementationName(String implementationName) {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		if (leapServiceRuntimeContext != null)
			leapServiceRuntimeContext.setImplementationName(implementationName);
	}

	/**
	 * This method is use to set the vendor name from the current leap service
	 * runtime context
	 * 
	 * @param vendor - name of the vendor
	 */
	public void setVendor(String vendor) {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		if (leapServiceRuntimeContext != null)
			leapServiceRuntimeContext.setVendor(vendor);
	}

	/**
	 * This method is use to set the version from the current leap service runtime
	 * context
	 * 
	 * @param version - name of the version
	 */
	public void setVersion(String version) {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		if (leapServiceRuntimeContext != null)
			leapServiceRuntimeContext.setVersion(version);
	}

	/**
	 * This method is use to set the provider name from the current leap service
	 * runtime context
	 * 
	 * @param provider - name of the provider
	 */
	public void setProvider(String provider) {
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		if (leapServiceRuntimeContext != null)
			leapServiceRuntimeContext.setProvider(provider);
	}

	public RequestContext getRequestContext() {
		if (requestContext != null) {
			requestContext.setHcTransactionalContext(this.hazelcastTransactionalContext);
			requestContext.setRequestId(requestUUID);
			return requestContext;
		} else {
			// this will be removed when we cleanup the code by removing
			// tenant,site,vendor,feature from leap header etc
			String implementationName = getImplementationName();
			String vendor = getVendor();
			String version = getVersion();
			String provider = getProvider();
			RequestContext reqCtx = null;
			if (provider != null && !(provider.isEmpty()))
				reqCtx = new RequestContext(tenant, site, featureGroup, featureName, implementationName, vendor,
						version, provider);
			else
				reqCtx = new RequestContext(tenant, site, featureGroup, featureName, implementationName, vendor,
						version);
			reqCtx.setHcTransactionalContext(this.hazelcastTransactionalContext);
			reqCtx.setRequestId(requestUUID);
			return reqCtx;
		}
	}

	/**
	 * This method is use to initialize the leap service runtime context
	 * 
	 * @param serviceName : Name of the service
	 */
	public void initializeLeapRuntimeServiceContext(String serviceName) {
		String methodName = "initializeLeapRuntimeServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (leapServiceRuntimeContextList != null && leapServiceRuntimeContextList.size() > 0) {
			LeapServiceRuntimeContext leapServiceRuntimeContext = new LeapServiceRuntimeContext();
			leapServiceRuntimeContext.setServiceName(serviceName);
			leapServiceRuntimeContextList.add(leapServiceRuntimeContext);
		} else {
			leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
			LeapServiceRuntimeContext leapServiceRuntimeContext = new LeapServiceRuntimeContext();
			leapServiceRuntimeContext.setServiceName(serviceName);
			leapServiceRuntimeContextList.add(leapServiceRuntimeContext);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method initializeLeapRuntimeServiceContext

	/**
	 * This method is use to get the current leap service runtime context
	 * 
	 * @return LeapServiceRuntimeContext Object
	 */
	public LeapServiceRuntimeContext getCurrentLeapServiceRuntimeContext() {
		LeapServiceRuntimeContext leapServiceRuntimeContext = null;
		if (leapServiceRuntimeContextList != null && leapServiceRuntimeContextList.size() > 0)
			leapServiceRuntimeContext = leapServiceRuntimeContextList.get(leapServiceRuntimeContextList.size() - 1);
		return leapServiceRuntimeContext;
	}

	@Override
	public String toString() {
		return "LeapServiceContext [tenant=" + tenant + ", site=" + site + ", endpointType=" + endpointType
				+ ", featureGroup=" + featureGroup + ", featureName=" + featureName + ", requestUUID=" + requestUUID
				+ ", requestContext=" + requestContext + "]";
	}

	/**
	 * This method is use to get the running context service name.
	 * 
	 * @return {@String} - name of the running context service
	 */
	public String getRunningContextServiceName() {
		String methodName = "getRunningContextServiceName";
		logger.debug("{} entered {} method of ServiceContext class ....", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		String runningContextServiceName = leapServiceRuntimeContext.getServiceName();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return runningContextServiceName;
	}// end of the method getRunningContextServiceName

	/**
	 * This method is use to set the running context service name.
	 * 
	 * @return {@String} - name of the running context service
	 */
	public void SetRunningContextServiceName(String serviceName) {
		String methodName = "SetRunningContextServiceName";
		logger.debug("{} entered {} method of ServiceContext class", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		leapServiceRuntimeContext.setServiceName(serviceName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method SetRunningContextServiceName

	/**
	 * This method is use to get the running service permastore configs, stored in
	 * from the LeapServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getPermastoreFromServiceContext() {
		String methodName = "getPermastoreFromServiceContext";
		logger.debug("{} entered {} method of ServiceContext class ....", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextPermastoreList = leapServiceRuntimeContext.getPermadata();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextPermastoreList;
	}// end of the method getPermastoreFromServiceContext

	/**
	 * This method is use to get the specific permastore from the running service
	 * context by the configname
	 * 
	 * @param configName : name of the permastore config
	 * @return {@link Object}
	 */
	public Object getPermastoreByNameFromServiceContext(String configName) {
		String methodName = "getPermastoreByNameFromServiceContext";
		logger.debug("{} entered {} method of ServiceContext class ....", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPermastoreList = getPermastoreFromServiceContext();
		// getting the permastore object from the map using configname
		Object permaObject = serviceRuntimeContextPermastoreList.get(configName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return permaObject;
	}// end of the method getPermastoreByNameFromServiceContext

	/**
	 * This method is use to store the permastore config to the running service
	 * context.
	 * 
	 * @param configName : name of the config
	 * @throws PermaStoreConfigRequestException :
	 *                                          {@link PermaStoreConfigRequestException}
	 */
	public void storePermastoreConfigToServiceContext(String configName) throws PermaStoreConfigRequestException {
		String methodName = "storePermastoreConfigToServiceContext";
		logger.debug("{} entered {} method of ServiceContext class ....", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPermastoreList = getPermastoreFromServiceContext();
		// getting the request context object
		RequestContext requestContext = getRequestContext();
		logger.trace("{} request context in Service context class: {} ", LEAP_LOG_KEY, requestContext);
		IPermaStoreConfigurationService permaConfigService = new PermaStoreConfigurationService();
		// fetching the permastore config from cache and storing it in the running
		// service context of the service
		Object permaobj = permaConfigService.getPermaStoreCachedObject(requestContext, configName);
		serviceRuntimeContextPermastoreList.put(configName, permaobj);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method storePermastoreConfigToServiceContext

	/**
	 * This method is use to get the running service entities configs, stored in
	 * context
	 * 
	 * @return {@link Map}
	 */
	public Map<String, Object> getEntitesConfigFromServiceContext() {
		String methodName = "getEntitesConfigFromServiceContext";
		logger.debug("{} entered {} method", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> entitesMap = leapServiceRuntimeContext.getEntityData();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return entitesMap;
	}

	/**
	 * This method is use to get the specific Entity from the running service
	 * context by the configname
	 * 
	 * @param name
	 * @return
	 */
	public Object getEntityConfigByNameFromServiceContext(String name) {
		String methodName = "getEntityConfigByNameFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> entitesMap = getEntitesConfigFromServiceContext();
		Object object = entitesMap.get(name);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return object;
	}

	/**
	 * This method is use to store the Entity config to the running service context.
	 * 
	 * @param configName
	 * @throws LeapDataServiceConfigurationException
	 */
	public void storeEntityConfigToServiceContext(String configName) throws LeapDataServiceConfigurationException {
		String methodName = "storeEntityConfigToServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> entityFromServiceContext = getEntitesConfigFromServiceContext();
		RequestContext requestContext = getRequestContext();
		logger.trace("{} request context in Service context class: {}", LEAP_LOG_KEY, requestContext);
		ILeapDataServiceConfiguration dataServiceConfiguration = new LeapDataServiceConfiguration();
		Entity entity = dataServiceConfiguration.getEntityCacheObject(requestContext, configName);
		entityFromServiceContext.put(configName, entity);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is use to store the All Entities config to the running service
	 * context.
	 * 
	 * @throws LeapDataServiceConfigurationException
	 */
	public void storeAllEntityConfigToServiceContext() throws LeapDataServiceConfigurationException {
		String methodName = "storeAllEntityConfigToServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> entityFromServiceContext = getEntitesConfigFromServiceContext();
		RequestContext requestContext = getRequestContext();
		logger.trace("request context in Service context class: ", LEAP_LOG_KEY, requestContext);
		ILeapDataServiceConfiguration dataServiceConfiguration = new LeapDataServiceConfiguration();
		List<Entity> entities = dataServiceConfiguration.getAllEntityCacheObject(requestContext);
		if (entities != null) {
			for (Entity entity : entities) {
				entityFromServiceContext.put(entity.getName(), entity);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is use to get from the running service policy configs stored in
	 * the LeapServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getPolicyFromServiceContext() {
		String methodName = "getPolicyFromServiceContext";
		logger.debug("{} entered into the method {} ", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextPolicyList = leapServiceRuntimeContext.getPolicydata();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextPolicyList;
	}// end of the method getPolicyFromServiceContext

	/**
	 * This method is use to get from the running service generic data stored in the
	 * LeapServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getGenericDataFromServiceContext() {
		String methodName = "getGenericDataFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextGenericDataList = leapServiceRuntimeContext.getGenricdata();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextGenericDataList;
	}// end of the method getGenericDataFromServiceContext

	/**
	 * This method is use to get the policy from the running service context by the
	 * configname
	 * 
	 * @param configName : name of the policy config
	 * @return {@link Object}
	 */
	public Object getPolicyByNameFromServiceContext(String configName) {
		String methodName = "getPolicyByNameFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPolicyList = getPolicyFromServiceContext();
		Object policyObject = serviceRuntimeContextPolicyList.get(configName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return policyObject;
	}// end of the method getPolicyByNameFromServiceContext

	/**
	 * This method is use to store the policy config to the current service context.
	 * 
	 * @param configName : Name of the policy config
	 */
	public void storePolicyConfigToServiceContext(String configName) {
		String methodName = "storePolicyConfigToServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);// TODO need to store policy data in
																				// runtime service context object.
	}// end of the method storePolicyConfigToServiceContext

	/**
	 * This method is use to get the running service pipeline Context Data configs,
	 * stored in the LeapServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getPipelineContextDataFromServiceContext() {
		String methodName = "getPipelineContextDataFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextPiplineContextList = leapServiceRuntimeContext.getPipeContextData();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextPiplineContextList;
	}// end of the method getPipelineContextDataFromServiceContext

	/**
	 * This method is use to get the piplineData Context Data from the running
	 * service context by configname
	 * 
	 * @param configName : name of the pipelinedata Context Data config
	 * @return {@link String}
	 */
	public String getPipeContextDataByNameFromServiceContext(String configName) {
		String methodName = "getPipeContextDataByNameFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPipelineContextDataList = getPipelineContextDataFromServiceContext();
		String pipelineDataObject = (String) serviceRuntimeContextPipelineContextDataList.get(configName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return pipelineDataObject;
	}// end of the method getPipeContextDataByNameFromServiceContext

	/**
	 * This method is use to store the pipelineData config to the current service
	 * context.
	 * 
	 * @param configName          : Name of the pipelinedata config
	 * @param piplineDataContext: pipeline data in string format
	 */
	public void storePipelineDataConfigToServiceContext(String configName, String piplineDataContext) {
		String methodName = "storePipelineDataConfigToServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPipelineContextDataList = getPipelineContextDataFromServiceContext();
		serviceRuntimeContextPipelineContextDataList.put(configName, piplineDataContext);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method storePipelineDataConfigToCurrentServiceContext

	/**
	 * This method is use to get the running service integration pipeline configs,
	 * stored in the LeapServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getIntegrationPipelineFromServiceContext() {
		String methodName = "getIntegrationPipelineFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextIntgrationPiplineList = leapServiceRuntimeContext
				.getIntegrationpipelineData();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextIntgrationPiplineList;
	}// end of the method getIntegrationPipelineFromServiceContext

	/**
	 * This method is use to get the integration pipline from the running service
	 * context by configname
	 * 
	 * @param configName : name of the integration pipeline config
	 * @return {@link IntegrationPipelineConfigUnit}
	 */
	public IntegrationPipelineConfigUnit getIntegrationPipelineByNameFromServiceContext(String configName) {
		String methodName = "getIntegrationPipelineByNameFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextIntegrationPipelineList = getIntegrationPipelineFromServiceContext();
		IntegrationPipelineConfigUnit pipelineDataObject = (IntegrationPipelineConfigUnit) serviceRuntimeContextIntegrationPipelineList
				.get(configName);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return pipelineDataObject;
	}// end of the method getIntegrationPipelineByNameFromServiceContext

	/**
	 * This method is use to get the running service PipelineContext, stored in the
	 * LeapServiceContext.
	 * 
	 * @return{@link PipelineContext}
	 */
	public PipelineContext getPipelineContextFromServiceContext() {
		String methodName = "getPipelineContextFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		return leapServiceRuntimeContext.getPipelineContext();
	}

	/**
	 * this method is use to set the pipeLineContext into the
	 * LeapServiceRuntimeContext
	 * 
	 * @param pipelineContext
	 */
	public void setPipelineContextInServiceContext(PipelineContext pipelineContext) {
		String methodName = "setPipelineContextInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		leapServiceRuntimeContext.setPipelineContext(pipelineContext);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is use to store the integration pipeline config to the running
	 * service context.
	 * 
	 * @param configName : Name of the integration pipeline config
	 * @throws IntegrationPipelineConfigException
	 */
	public void storeIntegrationPipelineConfigToServiceContext(String configName)
			throws IntegrationPipelineConfigException {
		String methodName = "storeIntegrationPipelineConfigToServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceRuntimeContextPipelineContextDataList = getIntegrationPipelineFromServiceContext();
		RequestContext requestContext = getRequestContext();
		logger.trace("{} request context in Service context class: {}", LEAP_LOG_KEY, requestContext);
		IIntegrationPipeLineConfigurationService pipeLineConfigurationService = new IntegrationPipelineConfigurationService();
		IntegrationPipelineConfigUnit pipelineConfigUnit = pipeLineConfigurationService
				.getIntegrationPipeConfiguration(requestContext, configName);
		serviceRuntimeContextPipelineContextDataList.put(configName, pipelineConfigUnit);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method storeIntegrationPipelineConfigToServiceContext

	/**
	 * This method is use to get the resource holder which contains all the resource
	 * object reference from the LeapServiceContext. Resource could be the
	 * connection object etc.
	 * 
	 * @return {@link Map(Object, Object)}
	 */
	public Map<Object, Object> getResourceHolderFromServiceContext() {
		String methodName = "getResourceHolderFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<Object, Object> serviceContextResourceHolderList = getResourceHolder();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceContextResourceHolderList;
	}// end of the method getResourceHolderFromServiceContext

	/**
	 * This method is use to get event holder from the service context which
	 * contains the events generated while processing the service.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getEventHolderFromServiceContext() {
		String methodName = "getEventHolderFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContextEventHolderList = getEventHolder();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceContextEventHolderList;
	}// end of the method getEventHolderFromServiceContext

	/**
	 * This method is use to get the events available in the event holder using
	 * unique service request id object from service context.
	 * 
	 * @param uniqueRequestId : unique request id in string
	 * @return {@link ArrayList(LeapEvent)}
	 */
	public ArrayList<LeapEvent> getEventHolderByRequestId(String uniqueRequestId) {
		String methodName = "getEventHolderByRequestId";
		logger.debug("{} entered into the method {} of ServiceContext class ", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContextEventHolderList = getEventHolder();
		ArrayList<LeapEvent> eventObj = (ArrayList<LeapEvent>) serviceContextEventHolderList.get(uniqueRequestId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventObj;
	}// end of the method getEventHolderByRequestId

	/**
	 * This method is use to store the events generated while processing the service
	 * in the service context event holder.
	 * 
	 * @param uniqueRequestId : unique service request object
	 * @param leapEvents:     ArrayList on LeapEvent Object
	 */
	public void storeEventHolderInServiceContext(String uniqueRequestId, List<LeapEvent> leapEvents) {
		String methodName = "storeEventHolderInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContexEventHolderList = getEventHolder();
		List<LeapEvent> eventList = (ArrayList<LeapEvent>) serviceContexEventHolderList.get(uniqueRequestId);
		if (eventList != null && !(eventList.isEmpty())) {
			for (LeapEvent leapEvent : leapEvents) {
				if (leapEvent != null)
					eventList.add(leapEvent);
			}
		} else {
			logger.debug(
					"{} event holder doesn't contain any event, so add compelete event list in the event holder map",
					LEAP_LOG_KEY);
			serviceContexEventHolderList.put(uniqueRequestId, leapEvents);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method storeEventHolderInServiceContext

	/**
	 * This method is use to store the events generated while processing the service
	 * in the service context event holder.
	 * 
	 * @param uniqueRequestId : unique service request object
	 * @param leapEvents:     {@link LeapEvent}
	 */
	public void storeEventHolderInServiceContext(String uniqueRequestId, LeapEvent leapEvent) {
		String methodName = "storeEventHolderInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContexEventHolderList = getEventHolder();
		List<LeapEvent> eventList = (ArrayList<LeapEvent>) serviceContexEventHolderList.get(uniqueRequestId);
		if (eventList != null && !(eventList.isEmpty())) {
			if (leapEvent != null) {
				eventList.add(leapEvent);
			} else {
				logger.debug("{} Cannot add any event in  service context as leap event is empty : {}", LEAP_LOG_KEY,
						leapEvent);
			}
		} else {
			List<LeapEvent> leapEventList = new ArrayList<>();
			leapEventList.add(leapEvent);
			serviceContexEventHolderList.put(uniqueRequestId, leapEventList);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of the method storeEventHolderInServiceContext

	/**
	 * This method is use to get service completion event holder from the service
	 * context which contains the events generated while processing the service.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getServiceCompletionEventHolderFromServiceContext() {
		String methodName = "getServiceCompletionEventHolderFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContextEventHolderList = getServiceCompletionEventHolder();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceContextEventHolderList;
	}// end of the method getServiceCompletionEventHolderFromServiceContext

	/**
	 * This method is use to get the service completion events available in the
	 * event holder using unique service request id object from service context.
	 * 
	 * @param uniqueRequestId : unique request id in string
	 * @return {@link ArrayList(LeapEvent)}
	 */
	public ArrayList<LeapEvent> getServiceCompletionEventHolderByRequestId(String uniqueRequestId) {
		String methodName = "getServiceCompletionEventHolderByRequestId";
		logger.debug("{} entered into the method {} of ServiceContext class ", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContextEventHolderList = getServiceCompletionEventHolder();
		ArrayList<LeapEvent> eventObj = (ArrayList<LeapEvent>) serviceContextEventHolderList.get(uniqueRequestId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return eventObj;
	}// end of the method getServiceCompletionEventHolderByRequestId

	/**
	 * This method is use to store the service completion events generated while
	 * processing the service in the service context event holder.
	 * 
	 * @param uniqueRequestId : unique service request object
	 * @param leapEvents:     ArrayList on LeapEvent Object
	 */
	public void storeServiceCompletionEventHolderInServiceContext(String uniqueRequestId, List<LeapEvent> leapEvents) {
		String methodName = "storeServiceCompletionEventHolderInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContexEventHolderList = getServiceCompletionEventHolder();
		List<LeapEvent> eventList = (ArrayList<LeapEvent>) serviceContexEventHolderList.get(uniqueRequestId);
		if (eventList != null && !(eventList.isEmpty())) {
			for (LeapEvent leapEvent : leapEvents) {
				if (leapEvent != null)
					eventList.add(leapEvent);
			}
		} else {
			logger.debug(
					"{} event holder doesn't contain any event, so add compelete event list in the event holder map",
					LEAP_LOG_KEY);
			serviceContexEventHolderList.put(uniqueRequestId, leapEvents);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of the method storeServiceCompletionEventHolderInServiceContext

	/**
	 * This method is use to store the service completion events generated while
	 * processing the service in the service context event holder.
	 * 
	 * @param uniqueRequestId : unique service request object
	 * @param leapEvents:     {@link LeapEvent}
	 */
	public void storeServiceCompletionEventHolderInServiceContext(String uniqueRequestId, LeapEvent leapEvent) {
		String methodName = "storeServiceCompletionEventHolderInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		Map<String, Object> serviceContexEventHolderList = getServiceCompletionEventHolder();
		List<LeapEvent> eventList = (ArrayList<LeapEvent>) serviceContexEventHolderList.get(uniqueRequestId);
		if (eventList != null && !(eventList.isEmpty())) {
			if (leapEvent != null) {
				eventList.add(leapEvent);
			} else {
				logger.debug("{} Cannot add any event in service context as leap event is empty : {}", LEAP_LOG_KEY,
						leapEvent);
			}
		} else {
			List<LeapEvent> leapEventList = new ArrayList<>();
			leapEventList.add(leapEvent);
			serviceContexEventHolderList.put(uniqueRequestId, leapEventList);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of the method storeServiceCompletionEventHolderInServiceContext

	/**
	 * This method is use to get the service handlers from the service context which
	 * contains the handlers defined for the service.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getServiceHandlerFromServiceContext() {
		String methodName = "getServiceHandlerFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceRuntimeContextServiceHandlersList = leapServiceRuntimeContext.getServiceHandlers();
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceRuntimeContextServiceHandlersList;
	}// end of the method getServiceHandlerFromServiceContext

	/**
	 * This method is use to get the specific service handler available in the
	 * service context using the unique id (e.g pre-service(Servicename)).
	 * 
	 * @param uniqueId : unique id in string
	 * @return {@link List(AbstractServiceHandler)}
	 */
	public List<AbstractServiceHandler> getServiceHandlerById(String uniqueId) {
		String methodName = "getServiceHandlerById";
		logger.debug("{} entered into the method {} of ServiceContext class ", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceContextServiceHandlerList = leapServiceRuntimeContext.getServiceHandlers();
		List<AbstractServiceHandler> serviceHandlerList = (List<AbstractServiceHandler>) serviceContextServiceHandlerList
				.get(uniqueId);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return serviceHandlerList;
	}// end of the method getServiceHandlerById

	/**
	 * This method is use to store the service handler in the service context
	 * 
	 * @param uniqueId            : unique id in string
	 * @param listServiceHandler: list of serviceHandlers
	 */
	public void storeServiceHandlerInServiceContext(String uniqueId, List<Object> listServiceHandler) {
		String methodName = "storeServiceHandlerInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		Map<String, Object> serviceContextServiceHandlerList = leapServiceRuntimeContext.getServiceHandlers();
		List<Object> serviceHandlerList = (List<Object>) serviceContextServiceHandlerList.get(uniqueId);
		if (serviceHandlerList != null && !(serviceHandlerList.isEmpty())) {
			// service handlers could be list of abstractService Handler or list of string
			// handlerConfig
			for (Object serviceHandler : listServiceHandler) {
				if (serviceHandler instanceof AbstractServiceHandler) {
					AbstractServiceHandler abstractServiceHandler = (AbstractServiceHandler) serviceHandler;
					serviceHandlerList.add(abstractServiceHandler);
				} else {
					logger.trace("Handlers are in string config");
					String StringServiceHandler = (String) serviceHandler;
					serviceHandlerList.add(StringServiceHandler);
				}
			}

		} else {
			logger.debug(
					"{} service handler holder doesn't contain any service handler, so add compelete service handler list in the service handler holder map",
					LEAP_LOG_KEY);
			serviceContextServiceHandlerList.put(uniqueId, listServiceHandler);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}// end of the method storeServiceHandlerInServiceContext

	/**
	 * this method is used to get the ServiceRequestData from ServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getServiceRequestDataFromServiceContext() {
		String methodName = "getServiceRequestDataFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		return leapServiceRuntimeContext.getServiceRequestData();

	}// end of the method getServiceRequestDataFromServiceContext

	/**
	 * this method is used to get the SubscriberData from ServiceContext.
	 * 
	 * @return {@link Map(String, Object)}
	 */
	public Map<String, Object> getSubscriberDataFromServiceContext() {
		String methodName = "getSubscriberDataFromServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = null;
		if (leapServiceRuntimeContextList != null && leapServiceRuntimeContextList.size() > 0) {
			leapServiceRuntimeContext = leapServiceRuntimeContextList.get(0);
			return leapServiceRuntimeContext.getSubscriberData();
		}
		return null;
	}// ..end of method getSubscriberDataFromServiceContext

	/**
	 * this method is used to get the subscriber Data from ServiceContext.
	 * 
	 * @param subscriber : Map<String, Object>
	 * @return {@link Map(String, Object)}
	 */
	public void setSubscriberDataInServiceContext(Map<String, Object> subscriber) {
		String methodName = "setSubscriberDataInServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapServiceRuntimeContext leapServiceRuntimeContext = getCurrentLeapServiceRuntimeContext();
		if (leapServiceRuntimeContext == null) {
			leapServiceRuntimeContext = new LeapServiceRuntimeContext();
			leapServiceRuntimeContext.setSubscriberData(subscriber);
			addLeapServiceRuntimeContext(leapServiceRuntimeContext);
		} else {
			leapServiceRuntimeContext.setSubscriberData(subscriber);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method setSubscriberDataInServiceContext

	/**
	 * This method is use to add the service runtime context to the service context
	 * 
	 * @param leapServiceRuntimeContext : {@link LeapServiceRuntimeContext}
	 */
	public void addLeapServiceRuntimeContext(LeapServiceRuntimeContext leapServiceRuntimeContext) {
		String methodName = "addLeapServiceRuntimeContext";
		logger.debug("{} entered into the method {} of ServiceContext class", LEAP_LOG_KEY, methodName);
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
		leapServiceRuntimeContextList.add(leapServiceRuntimeContext);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method addLeapServiceRuntimeContext

	/**
	 * This method is use to remove the running service context but on a condition
	 * that it shouldn't be the only running service context in the list
	 * 
	 * @throws UnableToRemoveServiceContextException
	 */
	public void removeCurrentServiceContext() throws UnableToRemoveServiceContextException {
		String methodName = "removeCurrentServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
		boolean isRemovableServiceContext = runtimeServiceContextIsRemovable(leapServiceRuntimeContextList);
		if (isRemovableServiceContext) {
			LeapServiceRuntimeContext leapServiceRuntimeContext = leapServiceRuntimeContextList
					.get(leapServiceRuntimeContextList.size() - 1);
			leapServiceRuntimeContext = null;
			leapServiceRuntimeContextList.remove(leapServiceRuntimeContextList.size() - 1);
		} else
			throw new UnableToRemoveServiceContextException(
					"Cann't remove running service context because running context is the only service context available.");
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method removeCurrentServiceContext

	/**
	 * This method is use to remove the last service context from the list
	 * 
	 * @throws UnableToRemoveServiceContextException
	 */
	public void removeLastServiceContext() throws UnableToRemoveServiceContextException {
		String methodName = "removeLastServiceContext";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
		boolean isLastServiceContext = runtimeServiceContextIsRemovable(leapServiceRuntimeContextList);
		if (isLastServiceContext)
			throw new UnableToRemoveServiceContextException(
					"Cann't remove running service context because running context is not the only service context available.");
		else
			leapServiceRuntimeContextList.clear();

		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the method removeLastServiceContext

	/**
	 * This method is use to remove all the runtime context except the parent
	 * runtime service context
	 * 
	 * @throws UnableToRemoveServiceContextException - Cann't remove running service
	 *                                               context because there is
	 *                                               nothing to remove from the
	 *                                               service context list
	 */
	public void removeAllServiceRuntimeContextExceptParent() throws UnableToRemoveServiceContextException {
		String methodName = "removeAllServiceRuntimeContextExceptParent";
		logger.debug("{} Entered {} method of ServiceContext class ....", LEAP_LOG_KEY, methodName);
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
		if (leapServiceRuntimeContextList != null && !(leapServiceRuntimeContextList.isEmpty())) {
			int runtimeContextSize = leapServiceRuntimeContextList.size();
			logger.trace("no. of runtime context in service context list = " + runtimeContextSize);
			for (int i = (runtimeContextSize - 1); i >= 1; i--)
				leapServiceRuntimeContextList.remove(i);
		} else {
			throw new UnableToRemoveServiceContextException(
					"Cann't remove running service context because there is nothing to remove from the service context list");

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}// end of the removeAllServiceRuntimeContextExceptParent

	/**
	 * This method is use to identify whether it is feasible to remove runtime
	 * service context or not
	 * 
	 * @return true/false - can or cannot remove
	 */
	public boolean isOnlyRuntimeServiceContext() {
		logger.debug("{} entered isOnlyRuntimeServiceContext method of ServiceContext class ....", LEAP_LOG_KEY);
		List<LeapServiceRuntimeContext> leapServiceRuntimeContextList = getLeapServiceRuntimeContextList();
		if (leapServiceRuntimeContextList != null && !(leapServiceRuntimeContextList.isEmpty())) {
			if (leapServiceRuntimeContextList.size() > 1)
				return false;
			else
				return true;
		} else {
			return false;
		}
	}// end of method isOnlyRuntimeServiceContext

	/**
	 * This method is use to check if we can remove the running service context or
	 * not.
	 * 
	 * @param leapServiceRuntimeContextList : List of running service context.
	 * @return {@link boolean}
	 */
	private boolean runtimeServiceContextIsRemovable(List<LeapServiceRuntimeContext> leapServiceRuntimeContextList) {
		logger.debug("{} entered runtimeServiceContextIsRemovable method of ServiceContext class ....", LEAP_LOG_KEY);
		int runtimeServiceContextSize = leapServiceRuntimeContextList.size();
		if (runtimeServiceContextSize > 1)
			return true;
		else
			return false;

	}// end of the method runtimeServiceContextIsRemovable

}
