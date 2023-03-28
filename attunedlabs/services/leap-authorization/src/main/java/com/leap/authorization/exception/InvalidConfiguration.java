package com.leap.authorization.exception;

public class InvalidConfiguration extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1616726120868657093L;

	public InvalidConfiguration() {
	}

	public InvalidConfiguration(String message) {
		super(message);
	}

	public InvalidConfiguration(Throwable cause) {
		super(cause);
	}

	public InvalidConfiguration(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidConfiguration(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
