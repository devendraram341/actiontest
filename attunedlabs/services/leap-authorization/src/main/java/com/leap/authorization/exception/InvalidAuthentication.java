package com.leap.authorization.exception;

public class InvalidAuthentication extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9054552140061660988L;

	public InvalidAuthentication() {
		super();
	}

	public InvalidAuthentication(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidAuthentication(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAuthentication(String message) {
		super(message);
	}

	public InvalidAuthentication(Throwable cause) {
		super(cause);
	}

}
