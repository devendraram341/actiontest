package com.attunedlabs.eventframework.dispatcher.channel;

public class DispatchChannelInstantiationException extends Exception {

	private static final long serialVersionUID = -4532193568410942468L;

	public DispatchChannelInstantiationException(Exception rootException){
		super(rootException);
	}

	public DispatchChannelInstantiationException() {
		super();
	
	}

	public DispatchChannelInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public DispatchChannelInstantiationException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public DispatchChannelInstantiationException(String message) {
		super(message);
		
	}

	public DispatchChannelInstantiationException(Throwable cause) {
		super(cause);
		
	}
	
	
}
