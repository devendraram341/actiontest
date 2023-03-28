package com.leap.authentication.exception;

public class InvalidRequestException extends Exception {


	
	public InvalidRequestException() {
		super();
	}

	public InvalidRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRequestException(String message) {
		super(message);
	}

	public InvalidRequestException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -5985914017240095409L;

}
