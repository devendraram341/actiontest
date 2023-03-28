package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidDomainReqException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4352010383934509917L;

	public InvalidDomainReqException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

}
