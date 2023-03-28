package com.attunedlabs.leap.context.exception;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

public class UnableToApplyTaxonomyException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnableToApplyTaxonomyException(String message, Throwable cause, String developerMessage,
			Integer appErrorCode) {
		super(message, cause);
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}

	public UnableToApplyTaxonomyException(String message, String developerMessage, Integer appErrorCode) {
		super(message, new Throwable());
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}
}
