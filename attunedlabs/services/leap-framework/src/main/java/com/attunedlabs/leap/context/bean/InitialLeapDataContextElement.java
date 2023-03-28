package com.attunedlabs.leap.context.bean;

import java.util.Map;

import com.attunedlabs.config.RequestContext;
import com.hazelcast.transaction.TransactionContext;

/**
 * 
 * @author Reactiveworks
 * 
 * This holds all the Initial LeapDataContext Elements
 *
 */
public class InitialLeapDataContextElement extends LeapDataElement {

	private Map<String, Object> privateHeaderElement;
	private Map<String, Object> requestHeaderElement;

	private String tenantId;
	private String siteId;
	private String endpointType;
	private String serviceName;
	private String featureGroup;
	private String featureName;
	private String implementationName;
	private String vendor;
	private String version;
	private String operation;
	private String requestUUID;
	private TransactionContext hazelcastTransactionalContext;
	private RequestContext requestContext;
	private String taxomonyId;
	private String vendorTaxomonyId;

	public InitialLeapDataContextElement() {
		super();
	}

	public InitialLeapDataContextElement(String tenantId, String siteId, String serviceName, String featureGroup,
			String featureName) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.serviceName = serviceName;
		this.featureGroup = featureGroup;
		this.featureName = featureName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getEndpointType() {
		return endpointType;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getFeatureGroup() {
		return featureGroup;
	}

	public String getFeatureName() {
		return featureName;
	}

	public String getImplementationName() {
		return implementationName;
	}

	public String getVendor() {
		return vendor;
	}

	public String getVersion() {
		return version;
	}

	public String getOperation() {
		return operation;
	}

	public String getRequestUUID() {
		return requestUUID;
	}

	public TransactionContext getHazelcastTransactionalContext() {
		return hazelcastTransactionalContext;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public void setEndpointType(String endpointType) {
		this.endpointType = endpointType;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public void setImplementationName(String implementationName) {
		this.implementationName = implementationName;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setRequestUUID(String requestUUID) {
		this.requestUUID = requestUUID;
	}

	public void setHazelcastTransactionalContext(TransactionContext hazelcastTransactionalContext) {
		this.hazelcastTransactionalContext = hazelcastTransactionalContext;
	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public Map<String, Object> getPrivateHeaderElement() {
		return privateHeaderElement;
	}

	public void setPrivateHeaderElement(Map<String, Object> privateHeaderElement) {
		this.privateHeaderElement = privateHeaderElement;
	}

	public Map<String, Object> getRequestHeaderElement() {
		return requestHeaderElement;
	}

	public void setRequestHeaderElement(Map<String, Object> requestHeaderElement) {
		this.requestHeaderElement = requestHeaderElement;
	}

	public String getTaxomonyId() {
		return taxomonyId;
	}

	public void setTaxomonyId(String taxomonyId) {
		this.taxomonyId = taxomonyId;
	}

	public String getVendorTaxomonyId() {
		return vendorTaxomonyId;
	}

	public void setVendorTaxomonyId(String vendorTaxomonyId) {
		this.vendorTaxomonyId = vendorTaxomonyId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InitialLeapDataContextElement [");
		if (privateHeaderElement != null) {
			builder.append("privateHeaderElement=");
			builder.append(privateHeaderElement);
			builder.append(", ");
		}
		if (requestHeaderElement != null) {
			builder.append("requestHeaderElement=");
			builder.append(requestHeaderElement);
			builder.append(", ");
		}
		if (tenantId != null) {
			builder.append("tenantId=");
			builder.append(tenantId);
			builder.append(", ");
		}
		if (siteId != null) {
			builder.append("siteId=");
			builder.append(siteId);
			builder.append(", ");
		}
		if (endpointType != null) {
			builder.append("endpointType=");
			builder.append(endpointType);
			builder.append(", ");
		}
		if (serviceName != null) {
			builder.append("serviceName=");
			builder.append(serviceName);
			builder.append(", ");
		}
		if (featureGroup != null) {
			builder.append("featureGroup=");
			builder.append(featureGroup);
			builder.append(", ");
		}
		if (featureName != null) {
			builder.append("featureName=");
			builder.append(featureName);
			builder.append(", ");
		}
		if (implementationName != null) {
			builder.append("implementationName=");
			builder.append(implementationName);
			builder.append(", ");
		}
		if (vendor != null) {
			builder.append("vendor=");
			builder.append(vendor);
			builder.append(", ");
		}
		if (version != null) {
			builder.append("version=");
			builder.append(version);
			builder.append(", ");
		}
		if (operation != null) {
			builder.append("operation=");
			builder.append(operation);
			builder.append(", ");
		}
		if (requestUUID != null) {
			builder.append("requestUUID=");
			builder.append(requestUUID);
			builder.append(", ");
		}
		if (hazelcastTransactionalContext != null) {
			builder.append("hazelcastTransactionalContext=");
			builder.append(hazelcastTransactionalContext);
			builder.append(", ");
		}
		if (requestContext != null) {
			builder.append("requestContext=");
			builder.append(requestContext);
			builder.append(", ");
		}
		if (taxomonyId != null) {
			builder.append("taxomonyId=");
			builder.append(taxomonyId);
			builder.append(", ");
		}
		if (vendorTaxomonyId != null) {
			builder.append("vendorTaxomonyId=");
			builder.append(vendorTaxomonyId);
		}
		builder.append("]");
		return builder.toString();
	}

	

}
