/**
 * 
 */
package com.leap.authorization.exception;

/**
 * @author bizruntime33
 *
 */
public class PermissionAccessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5057218540350845957L;

	/**
	 * 
	 */
	public PermissionAccessException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PermissionAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public PermissionAccessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PermissionAccessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public PermissionAccessException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
