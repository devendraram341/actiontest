package com.leap.authentication.exception;

public class CreateSoapRequestException extends Exception {

	private static final long serialVersionUID = 1L;

	public CreateSoapRequestException() {
		super();
	}

	public CreateSoapRequestException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CreateSoapRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public CreateSoapRequestException(String message) {
		super(message);
	}

	public CreateSoapRequestException(Throwable cause) {
		super(cause);
	}

}
