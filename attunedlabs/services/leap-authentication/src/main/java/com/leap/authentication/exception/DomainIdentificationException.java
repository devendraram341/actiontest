package com.leap.authentication.exception;

public class DomainIdentificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 280524042843350471L;

	public DomainIdentificationException() {
	}

	public DomainIdentificationException(String arg0) {
		super(arg0);
	}

	public DomainIdentificationException(Throwable cause) {
		super(cause);
	}

	public DomainIdentificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DomainIdentificationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
