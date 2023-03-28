package com.leap.authorization.exception;

public class PermissionGrantException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8424822840698535548L;

	public PermissionGrantException() {
	}

	public PermissionGrantException(String arg0) {
		super(arg0);
	}

	public PermissionGrantException(Throwable cause) {
		super(cause);
	}

	public PermissionGrantException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermissionGrantException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
