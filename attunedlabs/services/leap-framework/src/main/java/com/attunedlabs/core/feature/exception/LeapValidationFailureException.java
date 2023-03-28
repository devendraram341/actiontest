package com.attunedlabs.core.feature.exception;

public class LeapValidationFailureException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8040881198316922997L;
	public static final Integer RESPONSE_CODE = 403;

	private String developerMessage;
	private long appErrorCode;
	private String userMessage;
	private String feature;
	private String vendorID;
	private long vendorErrorCode;
	private String vendorErrorMessage;

	public LeapValidationFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public long getAppErrorCode() {
		return appErrorCode;
	}

	public void setAppErrorCode(long appErrorCode) {
		this.appErrorCode = appErrorCode;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	/**
	 * @return the vendorID
	 */
	public String getVendorID() {
		return vendorID;
	}

	/**
	 * @param vendorID
	 *            the vendorID to set
	 */
	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
	}

	/**
	 * @return the vendorErrorCode
	 */
	public long getVendorErrorCode() {
		return vendorErrorCode;
	}

	/**
	 * @param vendorErrorCode
	 *            the vendorErrorCode to set
	 */
	public void setVendorErrorCode(long vendorErrorCode) {
		this.vendorErrorCode = vendorErrorCode;
	}

	/**
	 * @return the vendorErrorMessage
	 */
	public String getVendorErrorMessage() {
		return vendorErrorMessage;
	}

	/**
	 * @param vendorErrorMessage
	 *            the vendorErrorMessage to set
	 */
	public void setVendorErrorMessage(String vendorErrorMessage) {
		this.vendorErrorMessage = vendorErrorMessage;
	}

}
