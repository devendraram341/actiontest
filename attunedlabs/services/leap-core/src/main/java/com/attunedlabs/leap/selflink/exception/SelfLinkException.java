package com.attunedlabs.leap.selflink.exception;

public class SelfLinkException extends Exception {

	private static final long serialVersionUID = 5755238312609948715L;

	public SelfLinkException() {
	}

	public SelfLinkException(String message) {
		super(message);
	}

	public SelfLinkException(Throwable cause) {
		super(cause);
	}

	public SelfLinkException(String message, Throwable cause) {
		super(message, cause);
	}

	public SelfLinkException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
