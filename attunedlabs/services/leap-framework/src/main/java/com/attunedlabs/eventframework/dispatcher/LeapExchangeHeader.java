package com.attunedlabs.eventframework.dispatcher;

import java.io.Serializable;

public class LeapExchangeHeader implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tenant;
	private String site;
	private String requestUUID;
	private boolean isRetryCall;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LeapExchangeHeader [tenant=" + tenant + ", site=" + site + ", requestUUID=" + requestUUID
				+ ", isRetryCall=" + isRetryCall + "]";
	}

	public LeapExchangeHeader(String tenant, String site, String requestUUID, boolean isRetryCall) {
		super();
		this.tenant = tenant;
		this.site = site;
		this.requestUUID = requestUUID;
		this.isRetryCall = isRetryCall;
	}

	/**
	 * @return the tenant
	 */
	public String getTenant() {
		return tenant;
	}

	/**
	 * @param tenant
	 *            the tenant to set
	 */
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * @return the requestUUID
	 */
	public String getRequestUUID() {
		return requestUUID;
	}

	/**
	 * @param requestUUID
	 *            the requestUUID to set
	 */
	public void setRequestUUID(String requestUUID) {
		this.requestUUID = requestUUID;
	}

	/**
	 * @return the isRetryCall
	 */
	public boolean isRetryCall() {
		return isRetryCall;
	}

	/**
	 * @param isRetryCall
	 *            the isRetryCall to set
	 */
	public void setRetryCall(boolean isRetryCall) {
		this.isRetryCall = isRetryCall;
	}

}
