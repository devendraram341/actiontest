package com.leap.authentication.exception;

public class TokenRevokeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8960774413071875808L;

	public TokenRevokeException() {
	}

	public TokenRevokeException(String message) {
		super(message);
	}

	public TokenRevokeException(Throwable cause) {
		super(cause);
	}

	public TokenRevokeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TokenRevokeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
