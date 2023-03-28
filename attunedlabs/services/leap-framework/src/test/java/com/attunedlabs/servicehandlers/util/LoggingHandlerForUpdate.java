package com.attunedlabs.servicehandlers.util;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.servicehandlers.AbstractServiceHandler;

public class LoggingHandlerForUpdate extends AbstractServiceHandler {

	final static Logger logger = LoggerFactory.getLogger(LoggingHandlerForUpdate.class);

	@Override
	public boolean initializeConfiguration(JSONObject jsonObject) {
		logger.debug("inside initializeConfiguration...DB logging");
		return false;
	}

	public void preService(Exchange exchange) {
		logger.debug("inside preService...DB logging");
	};

	public void preExec(Exchange exchange) {
		logger.debug("inside preExec...DB logging");
	};

	public void preExecEnrichment(Exchange exchange) {
		logger.debug("inside preExecEnrichment...DB logging");
	};

	public void preImplSelection(Exchange exchange) {
		logger.debug("inside preImplSelection...DB logging");
	};

	public void preImplEnrichment(Exchange exchange) {
		logger.debug("inside preImplEnrichment...DB logging");
	};

	public void preImpl(Exchange exchange) {
		logger.debug("inside preImpl...DB logging");
	};

	public void postExec(Exchange exchange) {
		logger.debug("inside postExec...DB logging");
	};

	public void postService(Exchange exchange) {
		logger.debug("inside postService...DB logging");
	};
}
