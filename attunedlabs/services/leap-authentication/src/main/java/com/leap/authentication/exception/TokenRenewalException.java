package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;

public class TokenRenewalException extends LeapAuthorizationFailedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4617942474139374701L;

	public TokenRenewalException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}

}
