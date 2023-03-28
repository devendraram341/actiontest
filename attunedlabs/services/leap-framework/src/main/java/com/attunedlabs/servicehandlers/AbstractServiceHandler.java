package com.attunedlabs.servicehandlers;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>IServiceHandler</code> is the interface allowing you to implement pre
 * and post service operations.
 * 
 * @author Reactiveworks42
 *
 */
public abstract class AbstractServiceHandler {
	final static Logger logger = LoggerFactory.getLogger(AbstractServiceHandler.class);

	/**
	 * These method is used to configure the json configuration passed in the
	 * HandlerImplementation of the service handler.
	 * 
	 * @param jsonObject
	 * @return configured or not configured.
	 */
	public boolean initializeConfiguration(JSONObject jsonObject) {
		logger.debug(" {} inside initializeConfiguration...", LEAP_LOG_KEY);
		return false;
	}

	/**
	 * PreActivity is the pre-operation that will be executed on any configured
	 * services that has added the handlers. Asks,What should be done in the pre
	 * execution of service?
	 * 
	 * @param exchange
	 */
	public void preService(Exchange exchange) {
		logger.debug(" {} inside preService...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the start of execution route.
	 * 
	 * @param exchange
	 */
	public void preExec(Exchange exchange) {
		logger.debug(" {} inside preExec...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the start of execution enrichment route.
	 * 
	 * @param exchange
	 */
	public void preExecEnrichment(Exchange exchange) {
		logger.debug(" {} inside preExecEnrichment...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the start of implementation selection route.
	 * 
	 * @param exchange
	 */
	public void preImplSelection(Exchange exchange) {
		logger.debug(" {} inside preImplSelection...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the start of implementation enrichment route.
	 * 
	 * @param exchange
	 */
	public void preImplEnrichment(Exchange exchange) {
		logger.debug(" {} inside preImplEnrichment...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the start of implementation route.
	 * 
	 * @param exchange
	 */
	public void preImpl(Exchange exchange) {
		logger.debug(" {} inside preImpl...", LEAP_LOG_KEY);
	};

	/**
	 * method executed at the end of execution route.
	 * 
	 * @param exchange
	 */
	public void postExec(Exchange exchange) {
		logger.debug(" {} inside postExec...", LEAP_LOG_KEY);
	};

	/**
	 * PostActivity is the post-operation that will be executed on any configured
	 * services that has added the handlers.Asks,What should be done in the post
	 * execution of service?
	 * 
	 * @param exchange
	 */
	public void postService(Exchange exchange) {
		logger.debug(" {} inside postService...", LEAP_LOG_KEY);
	};

}
