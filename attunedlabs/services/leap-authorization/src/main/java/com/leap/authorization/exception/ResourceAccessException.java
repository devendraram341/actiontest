package com.leap.authorization.exception;

public class ResourceAccessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5674677866151507015L;

	public ResourceAccessException() {
	}

	public ResourceAccessException(String message) {
		super(message);
	}

	public ResourceAccessException(Throwable cause) {
		super(cause);
	}

	public ResourceAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
