package com.attunedlabs.leap.load.resource;

import java.util.EventObject;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.management.event.CamelContextStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeapOnStartProcessor {
	private static final String STARTUP_BASEROUTE = "startup-baseroute";
	private static Logger log = LoggerFactory.getLogger(LeapOnStartProcessor.class);

	/**
	 * This method is used to call the feature specific custom route after the camel
	 * context is started .
	 * 
	 * <p>
	 * <b>Note:</b> If you pass routeId as null or empty then it is going to call
	 * the default route present in baseroute.xml
	 * </p>
	 * 
	 * @param routeId
	 * @param event
	 */
	public static void callRoute(String routeId, EventObject event) {
		if (event instanceof CamelContextStartedEvent) {
			CamelContextStartedEvent evnt = (CamelContextStartedEvent) event;
			CamelContext context = evnt.getContext();
			Route route = null;
			if (routeId == null || routeId.isEmpty()) {
				route = context.getRoute(STARTUP_BASEROUTE);
			} else {
				route = context.getRoute(routeId);
			}
			log.debug("route Id: " + route);
			Endpoint endpoint = route.getEndpoint();
			log.debug("endpoint" + endpoint);
			ProducerTemplate producerTemplate = context.createProducerTemplate();
			producerTemplate.send(endpoint, e -> log.debug("calling the route: " + endpoint.getEndpointUri()));

		}
	}
}
/**
 * May be if you what to use in future --- we have used EntryAddedListener from
 * hazelcast for notifying the other features.
 */
//private static final String FEATURE_STARTUP_MAP = "features-startup";
//protected static void loadStartupFeaturesMap() {
//	log.info("****** loading Startup Features Map ******");
//	HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
//	featuresStartupMap = hazelcastInstance.getMap(FEATURE_STARTUP_MAP);
//	featuresStartupMap.addEntryListener(new EntryAddedListener<Object, Object>() {
//		@Override
//		public void entryAdded(EntryEvent<Object, Object> ee) {
//			log.info("key: " + ee.getKey() + " with value: " + ee.getValue());
//		}
//	}, true);
//	hazelcastInstance.addDistributedObjectListener(new DistributedObjectListener() {
//
//		@Override
//		public void distributedObjectCreated(DistributedObjectEvent event) {
//			DistributedObject instance = event.getDistributedObject();
//			log.debug("Created: " + instance.getName());
//		}
//
//		@Override
//		public void distributedObjectDestroyed(DistributedObjectEvent event) {
//			log.debug("Destroyed " + event.getObjectName() + ", service=" + event.getServiceName());
//		}
//	});
//}
