package com.attunedlabs.eventframework.camel.eventproducer;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultMessageHistory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.server.ComponentPerformance;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.ServicePerformanceLoggingEvent;
import com.attunedlabs.eventframework.jaxb.Event;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;

/**
 * <code>SystemPerformanceLoggingEventBuilder</code> class will be used to log
 * the performance for each component in service.
 * 
 * @author Reactiveworks42
 *
 */
public class ServicePerformanceLoggingEventBuilder extends AbstractCamelEventBuilder {

	/**
	 * This method is to Service Performance Logging Event
	 * 
	 * @return : LeapEvent
	 */
	@Override
	public LeapEvent buildEvent(Exchange camelExchange, Event event) {
		RequestContext reqCtx = super.getRequestContextFromCamelExchange(camelExchange);
		ServicePerformanceLoggingEvent evt = new ServicePerformanceLoggingEvent(reqCtx);
		super.updateStandardCamelHeader(camelExchange, evt);
		LeapDataContext leapDataContext=camelExchange.getIn().getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT,LeapDataContext.class);
		LeapServiceContext leapServiceContext=leapDataContext.getServiceDataContext();
		if (leapServiceContext != null) {
			evt.addMetadata(LeapDataContextConstant.FEATUREGROUP, leapServiceContext.getFeatureGroup());
			evt.addMetadata(LeapDataContextConstant.FEATURENAME, leapServiceContext.getFeatureName());
			evt.addMetadata(LeapDataContextConstant.SERVICENAME, leapServiceContext.getRunningContextServiceName());
		}
		try {
			ComponentPerformance componentPerformanceFeatureCache = LeapConfigurationServer.getConfigurationService()
					.getComponentPerformanceFeatureCache(reqCtx.getRequestId());
			if(componentPerformanceFeatureCache != null){
				LinkedHashMap<String, Object> overallCervicePerformance = createOverAllPerformaceMap(
						componentPerformanceFeatureCache.getEndpoint(), componentPerformanceFeatureCache.getTimeTaken());
				evt.addMetadata("Performance_Traced", overallCervicePerformance);
			}
			List<DefaultMessageHistory> propertyList = camelExchange.getProperty("CamelMessageHistory", List.class);
			evt.addMetadata("DEEP_TRACING", (Serializable) propertyList.stream().map(e -> {
				Map<String, String> map = new LinkedHashMap<>();
				map.put("time ", e.getElapsed() + "ms");
				map.put("operation ", e.getNode().getLabel());
				map.put("routeId ", e.getRouteId());
				return map.toString();
			}).collect(Collectors.toList()));
			evt.addMetadata("TOTAL_TIME",
					(Serializable) propertyList.stream()
							.filter(e -> e.getRouteId().equals(propertyList.get(0).getRouteId()))
							.mapToLong(e -> e.getElapsed()).sum());
		} catch (ConfigServerInitializationException e) {
			logger.error("failed to get performance component due to " + e.getMessage(), e);
		}
		return evt;
	}


	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> createOverAllPerformaceMap(List<Object> endpoint, List<Object> timeTaken) {
		LinkedHashMap<String, Object> overAllMap = new LinkedHashMap<>();
		AtomicInteger callIndex = new AtomicInteger(0);
		for (int endpointIndex = 0; endpointIndex < endpoint.size(); endpointIndex++) {
			Object previousObj = null;
			if (endpointIndex != 0)
				previousObj = endpoint.get(endpointIndex - 1);

			Object endpointObj = endpoint.get(endpointIndex);
			Object timeTakenObj = timeTaken.get(endpointIndex);

			if (endpointObj instanceof String)
				overAllMap.put("(" + (callIndex.incrementAndGet()) + ") '" + (String) endpointObj + "'",
						(String) timeTakenObj);
			else if (endpoint instanceof List<?>) {
				LinkedHashMap<String, String> innerMap = new LinkedHashMap<>();
				List<String> innerCallList = (List<String>) endpoint.get(endpointIndex);
				List<String> innerCallTimeList = (List<String>) timeTaken.get(endpointIndex);
				innerMap.put("totalTimeTaken",
						(String) overAllMap.get("(" + callIndex.get() + ") '" + (String) previousObj + "'"));

				for (int callInsideEndpointIndex = 0; callInsideEndpointIndex < innerCallList
						.size(); callInsideEndpointIndex++) {
					String innerCall = innerCallList.get(callInsideEndpointIndex);
					String innerCallTime = innerCallTimeList.get(callInsideEndpointIndex);
					innerMap.put("(" + (callInsideEndpointIndex + 1) + ") '" + innerCall + "'", innerCallTime);
				}
				overAllMap.replace("(" + callIndex.get() + ") '" + (String) previousObj + "'", innerMap);
			}

		}

		return overAllMap;
	}

}
