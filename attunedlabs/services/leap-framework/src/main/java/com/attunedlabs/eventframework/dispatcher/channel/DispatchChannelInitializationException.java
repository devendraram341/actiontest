package com.attunedlabs.eventframework.dispatcher.channel;

public class DispatchChannelInitializationException extends Exception {
	private static final long serialVersionUID = 7583863823729821247L;

	public DispatchChannelInitializationException() {
	}

	public DispatchChannelInitializationException(String message) {
		super(message);
	}

	public DispatchChannelInitializationException(Throwable cause) {
		super(cause);
	}

	public DispatchChannelInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DispatchChannelInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
