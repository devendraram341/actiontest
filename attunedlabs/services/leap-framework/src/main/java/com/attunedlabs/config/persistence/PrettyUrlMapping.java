package com.attunedlabs.config.persistence;

public class PrettyUrlMapping {
	private String tenantId;
	private String siteId;
	private String prettyString;
	private String actualString;
	private int id;

	public PrettyUrlMapping(String tenantId, String siteId, String prettyString, String actualString, int id) {
		super();
		this.tenantId = tenantId;
		this.siteId = siteId;
		this.prettyString = prettyString;
		this.actualString = actualString;
		this.id = id;
	}

	public PrettyUrlMapping() {
		// TODO Auto-generated constructor stub
	}

	public String getPrettyString() {
		return prettyString;
	}

	public void setPrettyString(String prettyString) {
		this.prettyString = prettyString;
	}

	public String getActualString() {
		return actualString;
	}

	public void setActualString(String actualString) {
		this.actualString = actualString;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	@Override
	public String toString() {
		return "PrettyUrlMapping [prettyString=" + prettyString + ", actualString=" + actualString + ", id=" + id + "]";
	}
}
