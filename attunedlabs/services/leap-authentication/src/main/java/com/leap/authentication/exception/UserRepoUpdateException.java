package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class UserRepoUpdateException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6144143102643976931L;

	public UserRepoUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserRepoUpdateException(String message, String developerMessage, Long appErrorCode, Throwable cause) {

		super(message, cause);
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}

}
