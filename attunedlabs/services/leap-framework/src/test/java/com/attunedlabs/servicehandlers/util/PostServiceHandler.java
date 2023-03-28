package com.attunedlabs.servicehandlers.util;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.servicehandlers.AbstractServiceHandler;

public class PostServiceHandler extends AbstractServiceHandler {
	final static Logger logger = LoggerFactory.getLogger(PostServiceHandler.class);

	@Override
	public boolean initializeConfiguration(JSONObject jsonObject) {
		logger.debug("inside initializeConfiguration..authWork.");
		return false;
	}

	@Override
	public void postService(Exchange exchange) {
		logger.debug("inside postService :");
	}

	


}
