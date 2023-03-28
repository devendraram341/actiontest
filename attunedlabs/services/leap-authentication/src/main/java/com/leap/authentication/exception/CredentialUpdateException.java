package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;

public class CredentialUpdateException extends LeapAuthorizationFailedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4250998191238186236L;

	public CredentialUpdateException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}

}
