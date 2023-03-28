package com.leap.authentication.exception;

public class BuildSAMLProcessBeanException extends Exception {

	private static final long serialVersionUID = 1L;

	public BuildSAMLProcessBeanException() {
		super();
	}

	public BuildSAMLProcessBeanException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BuildSAMLProcessBeanException(String message, Throwable cause) {
		super(message, cause);
	}

	public BuildSAMLProcessBeanException(String message) {
		super(message);
	}

	public BuildSAMLProcessBeanException(Throwable cause) {
		super(cause);
	}

}
