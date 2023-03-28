package com.leap.authorization.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

/**
 * It is the custom exception for the role creation if role data is incorrect.
 * 
 * @author Bizruntime
 *
 */
public class CreateRoleInvalidRequestException extends LeapBadRequestException {
	private static final long serialVersionUID = 5735431728215179209L;

	public CreateRoleInvalidRequestException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage("Invalid data is requested to process");
	}
}