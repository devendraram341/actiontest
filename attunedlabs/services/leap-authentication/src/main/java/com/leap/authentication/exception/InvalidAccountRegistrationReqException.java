package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidAccountRegistrationReqException extends LeapBadRequestException {

	public InvalidAccountRegistrationReqException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3093372020583743931L;

}
