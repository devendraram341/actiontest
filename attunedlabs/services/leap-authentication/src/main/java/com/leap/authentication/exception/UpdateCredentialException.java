package com.leap.authentication.exception;

public class UpdateCredentialException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2674720738803395060L;

	public UpdateCredentialException() {
	}

	public UpdateCredentialException(String message) {
		super(message);
	}

	public UpdateCredentialException(Throwable cause) {
		super(cause);
	}

	public UpdateCredentialException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateCredentialException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
