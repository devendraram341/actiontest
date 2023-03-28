package com.leap.authentication.exception;

public class OauthAdminServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	public OauthAdminServiceException() {
		super();
	}

	public OauthAdminServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OauthAdminServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public OauthAdminServiceException(String message) {
		super(message);
	}

	public OauthAdminServiceException(Throwable cause) {
		super(cause);
	}

}
