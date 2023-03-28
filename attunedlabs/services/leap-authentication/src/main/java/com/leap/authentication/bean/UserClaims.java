package com.leap.authentication.bean;

public class UserClaims {

	private String country;
	private String emailaddress;
	private String mobile;
	private String organization;
	private String streetaddress;
	private String telephone;
	private String isActive;
	private String isLocked;
	
	private String street;
	private String stateOrProvince;
	private String region;
	private String locality;
	private String city;
	private String postalCode;
	
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStateOrProvince() {
		return stateOrProvince;
	}

	public void setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEmailaddress() {
		return emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getStreetaddress() {
		return streetaddress;
	}

	public void setStreetaddress(String streetaddress) {
		this.streetaddress = streetaddress;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	@Override
	public String toString() {
		return "UserClaims [country=" + country + ", emailaddress=" + emailaddress + ", mobile=" + mobile
				+ ", organization=" + organization + ", streetaddress=" + streetaddress + ", telephone=" + telephone
				+ ", isActive=" + isActive + ", isLocked=" + isLocked + ", street=" + street + ", stateOrProvince="
				+ stateOrProvince + ", region=" + region + ", locality=" + locality + ", city=" + city + ", postalCode="
				+ postalCode + "]";
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}
	
	public String getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(String isLocked) {
		this.isLocked = isLocked;
	}
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

}
