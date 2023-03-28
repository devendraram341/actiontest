package com.leap.authentication.exception;

public class UserValidationRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6239659912338973669L;

	public UserValidationRequestException() {
	}

	public UserValidationRequestException(String message) {
		super(message);
	}

	public UserValidationRequestException(Throwable cause) {
		super(cause);
	}

	public UserValidationRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserValidationRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
