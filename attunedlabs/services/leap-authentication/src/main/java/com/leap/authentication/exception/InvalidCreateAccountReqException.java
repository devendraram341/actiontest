package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidCreateAccountReqException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 620042011024403222L;

	public InvalidCreateAccountReqException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

}
