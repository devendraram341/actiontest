package com.leap.authorization.exception;

import com.attunedlabs.core.feature.exception.LeapAuthorizationFailedException;

/**
 * It is the custom exception for the asserting permission for user on resource.
 * 
 * @author Bizruntime
 *
 */
public class AssertPermissionInvalidRequestException extends LeapAuthorizationFailedException {
	private static final long serialVersionUID = 5735431728215179209L;

	public AssertPermissionInvalidRequestException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}
}
