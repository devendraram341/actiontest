package com.leap.authentication.exception;

public class DocumentBuilderParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public DocumentBuilderParseException() {
		super();
	}

	public DocumentBuilderParseException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DocumentBuilderParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentBuilderParseException(String message) {
		super(message);
	}

	public DocumentBuilderParseException(Throwable cause) {
		super(cause);
	}

}
