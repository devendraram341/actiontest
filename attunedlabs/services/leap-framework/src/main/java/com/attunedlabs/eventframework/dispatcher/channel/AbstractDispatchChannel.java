package com.attunedlabs.eventframework.dispatcher.channel;

import java.io.Serializable;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;


public abstract class AbstractDispatchChannel {
	protected String channeljsonconfig;
	public abstract void dispatchMsg(Serializable msg,RequestContext reqContext,String evendId)throws MessageDispatchingException;
	
	public String getChanneljsonconfig() {
		return channeljsonconfig;
	}
	public void setChanneljsonconfig(String channeljsonconfig) throws DispatchChannelInitializationException {
		this.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}
	public abstract void initializeFromConfig()throws DispatchChannelInitializationException ;
}
