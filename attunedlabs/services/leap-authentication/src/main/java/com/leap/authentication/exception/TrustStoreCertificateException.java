package com.leap.authentication.exception;

public class TrustStoreCertificateException extends Exception {

	private static final long serialVersionUID = 1L;

	public TrustStoreCertificateException() {
		super();
	}

	public TrustStoreCertificateException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TrustStoreCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrustStoreCertificateException(String message) {
		super(message);
	}

	public TrustStoreCertificateException(Throwable cause) {
		super(cause);
	}

}
