package com.attunedlabs.leapentity.service;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

/**
 * Generic exception for EntityService
 * @author Reactiveworks
 *
 */
public class LeapEntityServiceException extends LeapBadRequestException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LeapEntityServiceException(Throwable cause, String userMessage, String developerMessage, Integer appErrorCode) {
		super(userMessage, cause);
		setDeveloperMessage(developerMessage);
		setUserMessage(userMessage);
		setAppErrorCode(appErrorCode);
	}

	
}
