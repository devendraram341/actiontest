package com.leap.authentication.bean;

public class Domain {

	private String domainName;
	private String tenantId;
	private String siteId;
	private String adminUserName;
	private String adminPassword;
	private String adminEmail;
	private String adminFirstName;
	private String company;
	private String timeZone;

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminFirstName() {
		return adminFirstName;
	}

	public void setAdminFirstName(String adminFirstName) {
		this.adminFirstName = adminFirstName;
	}

	public String getAdminLastName() {
		return adminLastName;
	}

	public void setAdminLastName(String adminLastName) {
		this.adminLastName = adminLastName;
	}

	private String adminLastName;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
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

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getTimezone() {
		return timeZone;
	}

	public void setTimezone(String timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	public String toString() {
		return "Domain [domainName=" + domainName + ", tenantId=" + tenantId + ", siteId=" + siteId + ", adminUserName="
				+ adminUserName + ", adminPassword=" + adminPassword + ", adminEmail=" + adminEmail
				+ ", adminFirstName=" + adminFirstName + ", company=" + company + ", timeZone=" + timeZone
				+ ", adminLastName=" + adminLastName + "]";
	}

}
