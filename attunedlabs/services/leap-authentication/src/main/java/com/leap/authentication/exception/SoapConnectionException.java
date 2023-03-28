package com.leap.authentication.exception;

public class SoapConnectionException  extends Exception{
	private static final long serialVersionUID = 1L;

	public SoapConnectionException() {
		super();
	}

	public SoapConnectionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SoapConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SoapConnectionException(String message) {
		super(message);
	}

	public SoapConnectionException(Throwable cause) {
		super(cause);
	}

	
}
