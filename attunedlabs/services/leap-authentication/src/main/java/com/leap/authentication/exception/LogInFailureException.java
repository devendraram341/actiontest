package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class LogInFailureException extends LeapBadRequestException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8869395297481937473L;

	public LogInFailureException(String message, Throwable cause) {
		super(message, cause);
	}
	public LogInFailureException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

}
