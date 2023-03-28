package com.attunedlabs.featureinstaller.util;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.servicehandlers.AbstractServiceHandler;

public class TestServiceHandler extends AbstractServiceHandler {
	
	Logger logger=LoggerFactory.getLogger(getClass());

	@Override
	public boolean initializeConfiguration(JSONObject jsonObject) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In initializeConfiguration json body is:- " + jsonObject);
		logger.debug("-----------------------------------------------------------------------------------");
		return true;
	}

	@Override
	public void postExec(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In post Execution ..........");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void postService(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In post Service method..........");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preExec(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("pre Exec method.....");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preExecEnrichment(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preImpl(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In pre impl method.....");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preImplEnrichment(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In pre Impl Enrichment method.......");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preImplSelection(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In pre Impl Selection method.....");
		logger.debug("-----------------------------------------------------------------------------------");
	}

	@Override
	public void preService(Exchange exchange) {
		logger.debug("-----------------------------------------------------------------------------------");
		logger.debug("In pre service method..........");
		logger.debug("-----------------------------------------------------------------------------------");
	}

}
