package com.attunedlabs.security.pojo;

import java.io.Serializable;

/**
 * AccountConfiguration contains all the basic information like tenant, site,
 * domain, timeZone and name of account to which the site belongs to.
 * 
 * @author Reactiveworks
 *
 */
public class AccountConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String accountName;
	private String internalTenantId;
	private String internalSiteId;
	private String domain;
	private String timezone;
	private String secretKey;
	private int expirationCount;

	private AccountConfiguration() {
		super();
	}

	public AccountConfiguration(String accountName, String internalTenantId, String internalSiteId, String domain,
			String timezone,String secretKey, int expirationCount) {
		this();
		this.accountName = accountName;
		this.internalTenantId = internalTenantId;
		this.internalSiteId = internalSiteId;
		this.domain = domain;
		this.timezone = timezone;
		this.secretKey = secretKey;
		this.expirationCount = expirationCount;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @return the internalTenantId
	 */
	public String getInternalTenantId() {
		return internalTenantId;
	}

	/**
	 * @return the internalSiteId
	 */
	public String getInternalSiteId() {
		return internalSiteId;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @return the expirationTime
	 */
	public int getExpirationCount() {
		return expirationCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AccountConfiguration [accountName=" + accountName + ", internalTenantId=" + internalTenantId
				+ ", internalSiteId=" + internalSiteId + ", domain=" + domain + ", timezone=" + timezone
				+ ", secretKey=" + secretKey + ", expirationCount=" + expirationCount + "]";
	}
}
