package com.attunedlabs.leap.loggingfeature.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import static com.attunedlabs.leap.loggingfeature.bean.LoggingFeatureUtil.aggregateWithOtherComponents;
import static com.attunedlabs.leap.loggingfeature.bean.LoggingFeatureUtil.aggregateWithinComponents;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultMessageHistory;
import org.apache.camel.management.event.ExchangeSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.util.LeapConfigUtil;
import com.attunedlabs.config.util.LeapDefaultConstants;
import com.attunedlabs.leap.LeapDataContext;
import com.attunedlabs.leap.LeapServiceContext;
import com.attunedlabs.leap.context.constant.LeapDataContextConstant;

public class LoggingFeatureUtilitiesBean {
	final static Logger logger = LoggerFactory.getLogger(LoggingFeatureUtilitiesBean.class);
	// initialize config server.
	LoggingFeatureUtil loggingFeatureUtil = new LoggingFeatureUtil();

	public static boolean isPerformanceLoggingEnabled;
	private static final String IS_PERFORMANCE_LOGGING_ENABLED = "isPerformanceLoggingEnabled";
	static {
		try {
			String logging = LeapConfigUtil.getGlobalPropertyValue(IS_PERFORMANCE_LOGGING_ENABLED,LeapDefaultConstants.DEFAULT_ISPERFORMANCELOGGINGENABLED);
			isPerformanceLoggingEnabled = Boolean.parseBoolean(logging);
		} catch (Exception e) {
			logger.warn("{} unable to get {}  Propertie from globalAppDeployment , defualt is false ", LEAP_LOG_KEY,
					IS_PERFORMANCE_LOGGING_ENABLED);
			isPerformanceLoggingEnabled = false;
		}
		logger.debug("{} isPerformanceLoggingEnabled ? {}", LEAP_LOG_KEY, isPerformanceLoggingEnabled);
	}
	public static final String BASE_ROUTE = "BASE";
	public static final String EXEC_ROUTE = "EXEC";
	public static final String IMPL_ROUTE = "IMPL";
	final List<String> baseRouteCalls = Arrays.asList("subscriber-execution-route", "rest-call", "rest-get-post",
			"rest-call-xml", "BaseTransformation", "entryRoute", "ExitRoute");
	public static final String SEPERATOR = ".";

	public void controlNotifierLogging(Exchange exchange, PerformanceLoggingEventNotifer performanceLoggingEventNotifer)
			throws Exception {
		String methodName = "controlNotifierLogging";
		logger.debug("{} entered into the method {}, PerformanceLoggingEventNotifer={} ", LEAP_LOG_KEY, methodName,
				performanceLoggingEventNotifer);

		if (LoggingFeatureUtilitiesBean.isPerformanceLoggingEnabled) {
			logger.trace("{} Recording Performance Log...", LEAP_LOG_KEY);
			performanceLoggingEventNotifer.setIgnoreExchangeSentEvents(false);
			performanceLoggingEventNotifer.setIgnoreExchangeSendingEvents(false);
		} else {
			logger.trace("{} Ignoring Performance Log...", LEAP_LOG_KEY);
			performanceLoggingEventNotifer.setIgnoreExchangeSentEvents(true);
			performanceLoggingEventNotifer.setIgnoreExchangeSendingEvents(true);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@SuppressWarnings("unchecked")
	public void addComponentsToGrid(ExchangeSentEvent sent) {
		String methodName = "addComponentsToGrid";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) sent.getExchange().getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		if (leapServiceContext != null)
			if (leapServiceContext.getRequestUUID() != null && !leapServiceContext.getRequestUUID().trim().isEmpty()) {
				String requestUUID = leapServiceContext.getRequestUUID().trim();
				List<DefaultMessageHistory> propertyList = sent.getExchange().getProperty("CamelMessageHistory",
						List.class);
				Optional<DefaultMessageHistory> findFirst = propertyList.stream().filter(e -> {
					return e.getNode().getLabel().replaceFirst(":", "://").equals(sent.getEndpoint().getEndpointKey());
				}).findFirst();
				DefaultMessageHistory defaultMessageHistory = findFirst.orElse(null);
				String endpointKey = sent.getEndpoint().getEndpointKey().trim();
				if (defaultMessageHistory != null) {
					String routeId = defaultMessageHistory.getRouteId();
					logger.debug("{} RouteId : {}", LEAP_LOG_KEY, routeId);
					logger.debug("{} endpointKey : {} ", LEAP_LOG_KEY, endpointKey);

					if (baseRouteCalls.contains(routeId))
						endpointKey = BASE_ROUTE + SEPERATOR + endpointKey;
					else if (routeId.endsWith("-ER") || routeId.endsWith("-TR"))
						endpointKey = BASE_ROUTE + SEPERATOR + EXEC_ROUTE + SEPERATOR + endpointKey;
					else
						endpointKey = BASE_ROUTE + SEPERATOR + EXEC_ROUTE + SEPERATOR + IMPL_ROUTE + SEPERATOR
								+ endpointKey;

				} else {// toD
					System.out.println("toD : " + endpointKey);

					if (endpointKey.endsWith("-IR"))
						endpointKey = BASE_ROUTE + SEPERATOR + EXEC_ROUTE + SEPERATOR + endpointKey;
					else if (endpointKey.endsWith("-TR"))
						endpointKey = BASE_ROUTE + SEPERATOR + endpointKey;
					else
						endpointKey = BASE_ROUTE + SEPERATOR + EXEC_ROUTE + SEPERATOR + IMPL_ROUTE + SEPERATOR
								+ endpointKey;
				}
				aggregateWithOtherComponents(requestUUID, endpointKey, sent.getEndpoint().getEndpointKey(),
						sent.getTimeTaken(), sent.getSource());
			}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

	}

	public static void addInternalComponentCalls(LeapServiceContext leapServiceContext, String methodInvoked,
			long processedTime) {
		String methodName = "addInternalComponentCalls";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (leapServiceContext != null)
			if (leapServiceContext.getRequestUUID() != null && !leapServiceContext.getRequestUUID().trim().isEmpty()) {
				String requestUUID = leapServiceContext.getRequestUUID().trim();
				aggregateWithinComponents(requestUUID, "CamelToEndpoint", methodInvoked, processedTime + "ms");
			}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	public static void addInternalComponentCalls(Exchange exchange, String[] methodInvoked, long[] processedTime) {
		String methodName = "addInternalComponentCalls";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		LeapDataContext leapDataContext = (LeapDataContext) exchange.getIn()
				.getHeader(LeapDataContextConstant.LEAP_DATA_CONTEXT);
		LeapServiceContext leapServiceContext = leapDataContext.getServiceDataContext();
		if (leapServiceContext != null)
			if (leapServiceContext.getRequestUUID() != null && !leapServiceContext.getRequestUUID().trim().isEmpty()) {
				String requestUUID = leapServiceContext.getRequestUUID().trim();
				aggregateWithinComponents(requestUUID, exchange.getProperty(Exchange.TO_ENDPOINT, String.class),
						methodInvoked, processedTime);
			}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

}
