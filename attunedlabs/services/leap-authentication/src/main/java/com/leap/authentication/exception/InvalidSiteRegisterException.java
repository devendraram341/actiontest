package com.leap.authentication.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class InvalidSiteRegisterException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -572189045009812587L;

	public InvalidSiteRegisterException(String message, Throwable cause, String developerMessage,
			Integer errorCode) {
		super(message, cause);
		setAppErrorCode(errorCode);
		setDeveloperMessage(developerMessage);
		setUserMessage(message);
	}

}
