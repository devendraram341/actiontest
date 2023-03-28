package com.leap.authentication.exception;

public class InvalidTenantSiteException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidTenantSiteException() {
		super();
	}

	public InvalidTenantSiteException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidTenantSiteException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTenantSiteException(String message) {
		super(message);
	}

	public InvalidTenantSiteException(Throwable cause) {
		super(cause);
	}

}
