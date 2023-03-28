package com.leap.authentication.bean;

public class AccountDetail {
	private String accountName;
	private String tenantId;
	private long expirationTime;
	/**
	 * @param accountName
	 * @param tenantId
	 * @param expirationTime
	 */
	public AccountDetail(String accountName, String tenantId, long expirationTime) {
		super();
		this.accountName = accountName;
		this.tenantId = tenantId;
		this.expirationTime = expirationTime;
	}
	/**
	 * 
	 */
	public AccountDetail() {
		super();
	}
	/**
	 * @return the accountName6
	 */
	public String getAccountName() {
		return accountName;
	}
	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}
	/**
	 * @param tenantId the tenantId to set
	 */
	public void settenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	/**
	 * @return the expirationTime
	 */
	public long getExpirationTime() {
		return expirationTime;
	}
	/**
	 * @param expirationTime the expirationTime to set
	 */
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AccountDetail [accountName=" + accountName + ", tenantId=" + tenantId + ", expirationTime="
				+ expirationTime + "]";
	}
	
}
