package com.attunedlabs.security.pojo;

import java.util.List;

public class AccountDetails {

	private String accountName;
	private String secretKey;
	private String internalTenantId;
	private int expirationCount;
	private long expirationTime;
	private List<SiteDetatils> siteDetails;
	private String description;

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName
	 *            the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @param secretKey
	 *            the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * @return the internalAccountId
	 */
	public String getInternalTenantId() {
		return internalTenantId;
	}

	/**
	 * @param internalTenantId
	 *            the internalTenantId to set
	 */
	public void setInternalTenantId(String internalTenantId) {
		this.internalTenantId = internalTenantId;
	}

	/**
	 * @return the expirationCount
	 */
	public int getExpirationCount() {
		return expirationCount;
	}

	/**
	 * @param expirationCount
	 *            the expirationCount to set
	 */
	public void setExpirationCount(int expirationCount) {
		this.expirationCount = expirationCount;
	}

	/**
	 * @return the expirationTime
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * @param expirationTime
	 *            the expirationTime to set
	 */
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}

	/**
	 * @return the siteDetails
	 */
	public List<SiteDetatils> getSiteDetails() {
		return siteDetails;
	}

	/**
	 * @param siteDetails
	 *            the siteDetails to set
	 */
	public void setSiteDetails(List<SiteDetatils> siteDetails) {
		this.siteDetails = siteDetails;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AccountDetails [accountName=" + accountName + ", secretKey=" + secretKey + ", internalAccountId="
				+ internalTenantId + ", expirationCount=" + expirationCount + ", expirationTime=" + expirationTime
				+ ", siteDetails=" + siteDetails + ", description=" + description + "]";
	}

}
