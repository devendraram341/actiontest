package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidTokenRevokeRequestException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5297363092280855952L;

	public InvalidTokenRevokeRequestException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}
}
