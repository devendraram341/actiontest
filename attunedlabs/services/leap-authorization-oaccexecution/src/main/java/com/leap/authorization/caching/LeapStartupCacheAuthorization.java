package com.leap.authorization.caching;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.leap.load.resource.LeapOnStartProcessor;
import com.attunedlabs.leap.notifier.LeapStartupNotifier;

public class LeapStartupCacheAuthorization extends LeapStartupNotifier {
	private Logger log = LoggerFactory.getLogger(LeapStartupCacheAuthorization.class);
	private static final String CUSTOM_ROUTE_ID = "cs-authorization-authorizationservice-leap-leap";

	@Override
	public void notify(EventObject event) {
		log.debug("started notify method inside LeapStartupCacheAuthorization...");
		LeapOnStartProcessor.callRoute(CUSTOM_ROUTE_ID, event);

	}

}
