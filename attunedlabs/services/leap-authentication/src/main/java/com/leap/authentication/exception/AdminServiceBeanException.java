package com.leap.authentication.exception;

public class AdminServiceBeanException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdminServiceBeanException() {
		super();
	}

	public AdminServiceBeanException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AdminServiceBeanException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdminServiceBeanException(String message) {
		super(message);
	}

	public AdminServiceBeanException(Throwable cause) {
		super(cause);
	}

}
