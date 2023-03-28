package com.attunedlabs.config.util;

public class HSQLDBConnectionException extends Exception{

	private static final long serialVersionUID = 1L;

	public HSQLDBConnectionException() {
	}

	public HSQLDBConnectionException(String message) {
		super(message);
	}

	public HSQLDBConnectionException(Throwable cause) {
		super(cause);
	}

	public HSQLDBConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public HSQLDBConnectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
