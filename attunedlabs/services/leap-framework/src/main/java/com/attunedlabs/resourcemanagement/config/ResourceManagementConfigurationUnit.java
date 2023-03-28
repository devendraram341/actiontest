package com.attunedlabs.resourcemanagement.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.resourcemanagement.jaxb.ConnectionInfo;
import com.attunedlabs.resourcemanagement.jaxb.GetResourceContent;

public class ResourceManagementConfigurationUnit extends ConfigurationUnit implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3115380906965607833L;
	public static final String RESOURCE_MANAGEMENT_GET_RESOURCE_CONTENT_GROUPKEY_SUFFIX = "-GETRESOURCECONTENT";
	public static final String RESOURCE_MANAGEMENT_CONNECTION_INFO = "-CONNECTIONINFO";
	private String siteId;
	/**
	 * It is matches with the ConfigId in the database i.e
	 * confignodedata{table}.nodeDataId{column}
	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId;
	private String groupId;

	public ResourceManagementConfigurationUnit(String tenantId, String siteId,Integer attachedNodeId, Boolean isEnabled, String key,GetResourceContent configData) {
		super(tenantId, configData, isEnabled, key,getGetResourceContentConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
	}
	
	public ResourceManagementConfigurationUnit(String tenantId, String siteId,Integer attachedNodeId, Boolean isEnabled, String key,ConnectionInfo configData) {
		super(tenantId, configData, isEnabled, key,getConnectionInfoConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.attachedNodeId = attachedNodeId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Integer getDbconfigId() {
		return dbconfigId;
	}

	public void setDbconfigId(Integer dbconfigId) {
		this.dbconfigId = dbconfigId;
	}

	public Integer getAttachedNodeId() {
		return attachedNodeId;
	}

	public void setAttachedNodeId(Integer attachedNodeId) {
		this.attachedNodeId = attachedNodeId;
	}
	

	
	/**
	 * Util Method to generate the resource management Configuration Group Key
	 * 
	 * @param attachedToNodeId
	 * @return
	 */
	public static String getGetResourceContentConfigGroupKey(Integer attachedToNodeId) {
		String fdcGroupKey = attachedToNodeId.intValue() + RESOURCE_MANAGEMENT_GET_RESOURCE_CONTENT_GROUPKEY_SUFFIX;
		return fdcGroupKey;
	}
	
	/**
	 * Util Method to generate the resource management Configuration Group Key
	 * 
	 * @param attachedToNodeId
	 * @return
	 */
	public static String getConnectionInfoConfigGroupKey(Integer attachedToNodeId) {
		String fdcGroupKey = attachedToNodeId.intValue() + RESOURCE_MANAGEMENT_CONNECTION_INFO;
		return fdcGroupKey;
	}
}
