package com.attunedlabs.leap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.attunedlabs.config.pipeline.PipelineContext;

/**
 * This class have information related to running service like service name and the configuration used for the service.
 * @author Reactiveworks
 *
 */
public class LeapServiceRuntimeContext implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8616947111318084011L;
	
	
	private String serviceName;	
	private String implementationName;
	private String vendor;
	private String version;
	private String provider;
	

	private PipelineContext pipelineContext;	
	
	//It is use to store the service handlers
	Map<String, Object> serviceHandlers;
	
	//It is used to store the application specific generic data
	Map<String, Object> genricdata;
	
	//It is used to store the application specific generic data
		Map<String, Object> subscriberData;
	
	//It is used to store the permastore used in application, rather than every time fetching for cache.
	Map<String, Object> permadata;
	
	//It is used to store the policy result, rather than every time fetching for cache.
	Map<String, Object> policydata;
	
	//It is used to store the integration pipeline used in application, rather than every time fetching for cache.
	Map<String, Object> integrationpipelineData;
	
	//#TODO It is used to set the integration pipeline as a service
	Map<String, Object> pipeContextData;
	
	//#TODO Need to check why it is required and later implement it
	Map<String, Object> serviceRequestData;
	
	// It is  used to store the  entityData used in application , rather than every time fetching for cache
	Map<String, Object> entityData;
    //default constructor
	public LeapServiceRuntimeContext() {
		super();
	}
	
	// Parameterized  constructor
	public LeapServiceRuntimeContext(String serviceName, String implementationName, String vendor, String version) {
		super();
		this.serviceName = serviceName;
		this.implementationName = implementationName;
		this.vendor = vendor;
		this.version = version;
	}

	//all setter and getter are defined here
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getImplementationName() {
		return implementationName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}
	
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}	
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public PipelineContext getPipelineContext() {
		return pipelineContext;
	}

	public void setPipelineContext(PipelineContext pipelineContext) {
		this.pipelineContext = pipelineContext;
	}
	
	public void setGenricdata(Map<String, Object> genericDataMap) {
		this.genricdata=genericDataMap;
	}
	
	
	public Map<String, Object> getEntityData() {
		if(entityData == null)
		{
			entityData = new HashMap<String, Object>(); 
		}
		return entityData;
	}

	public void setEntityData(Map<String, Object> entityData) {
		this.entityData = entityData;
	}

	public Map<String, Object> getSubscriberData() {
		return subscriberData;
	}

	public void setSubscriberData(Map<String, Object> subscriberData) {
		this.subscriberData = subscriberData;
	}
	
	public Map<String, Object> getServiceHandlers() {
		// if serviceHandlers object is not created then create new object and return 
	    //else, return the existing serviceHandlers object reference
		if(serviceHandlers == null)
			serviceHandlers=new HashMap<>();
		return serviceHandlers;
	}	

	public Map<String, Object> getGenricdata() {
		// if genericdata object is not created then create new object and return 
		//else, return the existing genericdata object reference
		if(genricdata == null )
			genricdata=new HashMap<>();
		return genricdata;
	}

	public Map<String, Object> getPermadata() {
		// if permadata object is not created then create new object and return 
		//else, return the existing permadata object reference
		if(permadata == null)
			permadata=new HashMap<>();
		return permadata;
	}
	
	public Map<String, Object> getPolicydata() {
		// if policyData object is not created then create new object and return 
		//else, return the existing policyData object reference
		if(policydata==null)
			policydata=new HashMap<>();
		return policydata;
	}

	public Map<String, Object> getIntegrationpipelineData() {
		// if integrationPipelineData object is not created then create new object and return 
		//else, return the existing integrationPipelineData object reference
		if(integrationpipelineData == null)
			integrationpipelineData=new HashMap<>();
		return integrationpipelineData;
	}	

	public Map<String, Object> getPipeContextData() {
		// if pipelineContextData object is not created then create new object and return 
		//else, return the existing pipelineContextData object reference
		if(pipeContextData == null)
			pipeContextData=new HashMap<>();
		return pipeContextData;
	}	

	public Map<String, Object> getServiceRequestData() {
		// if serviceRequestData object is not created then create new object and return 
	    //else, return the existing serviceRequestData object reference
		if(serviceRequestData== null)
			serviceRequestData=new HashMap<>();
		return serviceRequestData;
	}	
	
	

	@Override
	public String toString() {
		return "LeapServiceRuntimeContext [serviceName=" + serviceName + ", implementationName=" + implementationName
				+ ", vendor=" + vendor + ", version=" + version + ", genricdata=" + genricdata + ", provider= "+provider +"]";
	}

	
	  /**
     * This method is use for cloning the leapServiceContext object.
     * Might have used for pipeline as a service. Need to check its usage.
     * @return LeapServiceContext
     */
	/*public LeapServiceRuntimeContext deepClone() {
		LeapServiceRuntimeContext clonedLeapServiceContext = new LeapServiceRuntimeContext();
		clonedLeapServiceContext.tenant = this.tenant;
		clonedLeapServiceContext.site = this.site;
		clonedLeapServiceContext.endpointType = this.endpointType;
		clonedLeapServiceContext.serviceName = this.serviceName;
		clonedLeapServiceContext.featureGroup = this.featureGroup;
		clonedLeapServiceContext.featureName = this.featureName;
		clonedLeapServiceContext.implementationName = this.implementationName;
		clonedLeapServiceContext.vendor = this.vendor;
		clonedLeapServiceContext.version = this.version;
		clonedLeapServiceContext.requestUUID = this.requestUUID;
		clonedLeapServiceContext.hazelcastTransactionalContext = this.hazelcastTransactionalContext;
		clonedLeapServiceContext.requestContext = this.requestContext;
		clonedLeapServiceContext.pipelineContext = this.pipelineContext;
		clonedLeapServiceContext.genricdata = this.genricdata;
		clonedLeapServiceContext.permadata = this.permadata;
		clonedLeapServiceContext.policydata = this.policydata;
		clonedLeapServiceContext.resourceHolder = this.resourceHolder;
		clonedLeapServiceContext.integrationpipelineData = this.integrationpipelineData;
		clonedLeapServiceContext.pipeContext = this.pipeContext;
		clonedLeapServiceContext.serviceRequestData = this.serviceRequestData;
		return clonedLeapServiceContext;

	}*/

	

	

}
