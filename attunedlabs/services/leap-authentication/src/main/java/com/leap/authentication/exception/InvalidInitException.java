package com.leap.authentication.exception;

public class InvalidInitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4787340430025714577L;

	public InvalidInitException() {
	}

	public InvalidInitException(String message) {
		super(message);
	}

	public InvalidInitException(Throwable cause) {
		super(cause);
	}

	public InvalidInitException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidInitException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
