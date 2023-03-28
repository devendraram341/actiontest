package com.attunedlabs.leapentity.dao;

public class LeapEntityServiceSqlDAOException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2437832253397484723L;

	private int errorCode = 500;

	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * 
	 */
	public LeapEntityServiceSqlDAOException(int errorCode) {
		super();
		this.errorCode = errorCode;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public LeapEntityServiceSqlDAOException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, int errorCode) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorCode = errorCode;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LeapEntityServiceSqlDAOException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public LeapEntityServiceSqlDAOException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public LeapEntityServiceSqlDAOException(Throwable cause, int errorCode) {
		super(cause);
		this.errorCode = errorCode;
		// TODO Auto-generated constructor stub
	}

}
