package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;

public class UserProfileFetchException extends LeapAuthorizationFailedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5481389557871766872L;

	public UserProfileFetchException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}

}
