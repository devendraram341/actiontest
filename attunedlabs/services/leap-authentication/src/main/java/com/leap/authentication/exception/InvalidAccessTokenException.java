package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidAccessTokenException extends LeapBadRequestException {

	private static final long serialVersionUID = 1L;

	public InvalidAccessTokenException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}

}
