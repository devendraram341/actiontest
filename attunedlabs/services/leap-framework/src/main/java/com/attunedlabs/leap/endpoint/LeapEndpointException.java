package com.attunedlabs.leap.endpoint;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class LeapEndpointException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LeapEndpointException(String message, Throwable cause, String developerMessage, Integer appErrorCode) {
		super(message, cause);
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}

}
