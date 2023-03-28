package com.attunedlabs.leapentity.config;

import java.io.Serializable;

import com.attunedlabs.config.beans.ConfigurationUnit;
import com.attunedlabs.leapentity.config.jaxb.Entity;

public class LeapDataServiceConfigurationUnit extends ConfigurationUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5716538328062746075L;
	public static final String LEAPDATASERVICE_GROUPKEY_SUFFIX = "-LDS-ENTITY";
	private Entity entity;
	private String siteId;
	/**
	 * It is matches with the ConfigId in the database i.e
	 * confignodedata{table}.nodeDataId{column}
	 */
	private Integer dbconfigId;
	/** Id of the Node in db that this configuration is attached with */
	private Integer attachedNodeId;

	public LeapDataServiceConfigurationUnit(String tenantId, String siteId, Integer attachedNodeId, Boolean isEnabled,
			Entity entity) {
		super(tenantId, entity, isEnabled, entity.getName(), getConfigGroupKey(attachedNodeId));
		this.siteId = siteId;
		this.entity = entity;
		this.attachedNodeId = attachedNodeId;

	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
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

	public static String getConfigGroupKey(Integer attachedToNodeId) {
		String psGroupKey = attachedToNodeId.intValue() + LEAPDATASERVICE_GROUPKEY_SUFFIX;
		return psGroupKey;
	}

}
