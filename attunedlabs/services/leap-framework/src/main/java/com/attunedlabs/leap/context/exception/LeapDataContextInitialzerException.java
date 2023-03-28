package com.attunedlabs.leap.context.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class LeapDataContextInitialzerException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LeapDataContextInitialzerException(String message,Throwable cause,String developerMessage, Integer appErrorCode) {
		super(message, cause);
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}

}
