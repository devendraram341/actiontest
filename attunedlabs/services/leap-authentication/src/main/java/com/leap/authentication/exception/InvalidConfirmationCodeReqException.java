package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidConfirmationCodeReqException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6639467903646938726L;

	public InvalidConfirmationCodeReqException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

}
