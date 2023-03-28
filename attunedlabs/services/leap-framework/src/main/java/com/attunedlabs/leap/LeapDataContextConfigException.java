/**
 * 
 */
package com.attunedlabs.leap;

import com.attunedlabs.core.feature.exception.LeapBadRequestException;

/**
 * @author Reactiveworks
 *
 */
public class LeapDataContextConfigException extends LeapBadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public LeapDataContextConfigException(String message, Throwable cause, String developerMessage,
			Integer appErrorCode) {
		super(message, cause);
		setDeveloperMessage(developerMessage);
		setAppErrorCode(appErrorCode);
		setUserMessage(message);
	}
}
