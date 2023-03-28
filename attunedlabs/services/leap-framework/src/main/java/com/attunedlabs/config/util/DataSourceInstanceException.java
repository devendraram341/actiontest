package com.attunedlabs.config.util;

/**
 * @author Reactiveworks
 *
 */
public class DataSourceInstanceException extends Exception {

	private static final long serialVersionUID = 1L;
	public DataSourceInstanceException() {
		super();
	}

	public DataSourceInstanceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataSourceInstanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataSourceInstanceException(String message) {
		super(message);
	}

	public DataSourceInstanceException(Throwable cause) {
		super(cause);
	}
}
