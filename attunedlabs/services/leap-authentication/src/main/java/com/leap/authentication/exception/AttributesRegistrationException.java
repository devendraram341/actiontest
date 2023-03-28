package com.leap.authentication.exception;

public class AttributesRegistrationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1087625807886991780L;

	public AttributesRegistrationException() {
	}

	public AttributesRegistrationException(String message) {
		super(message);
	}

	public AttributesRegistrationException(Throwable cause) {
		super(cause);
	}

	public AttributesRegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributesRegistrationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
