package com.leap.authentication.exception;

public class SAMLResponseException extends Exception {

	private static final long serialVersionUID = 1L;

	public SAMLResponseException() {
		super();
	}

	public SAMLResponseException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SAMLResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SAMLResponseException(String message) {
		super(message);
	}

	public SAMLResponseException(Throwable cause) {
		super(cause);
	}

}
