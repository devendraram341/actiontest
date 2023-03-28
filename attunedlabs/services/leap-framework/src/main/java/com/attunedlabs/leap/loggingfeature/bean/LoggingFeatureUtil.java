package com.attunedlabs.leap.loggingfeature.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.server.ComponentPerformance;
import com.attunedlabs.config.server.ConfigServerInitializationException;
import com.attunedlabs.config.server.LeapConfigurationServer;

public class LoggingFeatureUtil {
	final static Logger logger = LoggerFactory.getLogger(LoggingFeatureUtil.class);
	static LeapConfigurationServer leapConfigurationServer;

	public LoggingFeatureUtil() {
		try {
			leapConfigurationServer = LeapConfigurationServer.getConfigurationService();
		} catch (ConfigServerInitializationException e) {
			logger.error("{} Failed to get access to DataGrid .. {}" ,LEAP_LOG_KEY, e.getMessage(), e);
		}

	}

	/**
	 * Utility to check logging feature present ion datagrid.
	 * 
	 * @param featureGroup
	 * @param featureName
	 * @return
	 */
	public static boolean isLoggingFeature(String featureGroup, String featureName) {
		return leapConfigurationServer.isLoggingFeaturePresentInDataGrid(featureGroup, featureName);
	}

	/**
	 * Utility to aggregate time of various components to DataGrid.
	 * 
	 * @param requestUUID
	 * @param endpoint
	 * @param timeTaken
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	public static void aggregateWithOtherComponents(String requestUUID, String endpoint,String mainEndpointKey,long timeTaken,
			Object source) {
		ComponentPerformance componentPerformanceFeatureCache = leapConfigurationServer
				.getComponentPerformanceFeatureCache(requestUUID);
		if(componentPerformanceFeatureCache !=null){
		List<Object> endpointList = componentPerformanceFeatureCache.getEndpoint();
		List<Object> timeTakenList = componentPerformanceFeatureCache.getTimeTaken();
		boolean newComponentAddedFlag = false;
		if (endpointList.size() > 1) {
			Object endpointObj = endpointList.get(endpointList.size() - 1);
			Object timeTakenObj = timeTakenList.get(timeTakenList.size() - 1);
			if (endpointObj != null && endpointObj instanceof List<?>) {
				List<String> internalCalls = (List<String>) endpointObj;
				List<String> internalCallTimes = (List<String>) timeTakenObj;
				if (internalCalls.contains(mainEndpointKey) && internalCallTimes.contains(mainEndpointKey)) {
					internalCalls.remove(mainEndpointKey);
					internalCallTimes.remove(mainEndpointKey);
					endpointList.remove(endpointObj);
					timeTakenList.remove(timeTakenObj);
					componentPerformanceFeatureCache.addNewComponent(endpoint, timeTaken + "ms");
					endpointList.add(endpointObj);
					timeTakenList.add(timeTakenObj);
					newComponentAddedFlag = true;
				} else
					newComponentAddedFlag = false;
			} else
				newComponentAddedFlag = false;
		} else
			newComponentAddedFlag = false;
		if (!newComponentAddedFlag)
			componentPerformanceFeatureCache.addNewComponent(endpoint, timeTaken + "ms");
		leapConfigurationServer.addComponentToPerformanceCache(componentPerformanceFeatureCache);
		}
	}

	/**
	 * Utility to aggregate time within component to DataGrid.
	 * 
	 * @param requestUUID
	 * @param endpoint
	 * @param timeTaken
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	public static void aggregateWithinComponents(String requestUUID, String endpointInvoked, String methodInvoked,
			String processedTime) {
		String methodName = "aggregateWithinComponents";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ComponentPerformance componentPerformanceFeatureCache = leapConfigurationServer
				.getComponentPerformanceFeatureCache(requestUUID);
		if(componentPerformanceFeatureCache !=null){
		List<Object> endpoint = componentPerformanceFeatureCache.getEndpoint();
		List<Object> timeTaken = componentPerformanceFeatureCache.getTimeTaken();
		Object endpointObj = endpoint.get(endpoint.size() - 1);
		Object timeTakenObj = timeTaken.get(timeTaken.size() - 1);
		List<String> internalCalls = null;
		List<String> internalCallTimes = null;
		boolean toCreateNewList = false;
		if (endpointObj instanceof List) {
			internalCalls = (List<String>) endpointObj;
			internalCallTimes = (List<String>) timeTakenObj;
			if (!internalCalls.contains(endpointInvoked) && !internalCallTimes.contains(endpointInvoked))
				toCreateNewList = true;
		} else
			toCreateNewList = true;

		if (toCreateNewList) {
			internalCalls = new ArrayList<>();
			internalCallTimes = new ArrayList<>();
			// just adding before to identify
			internalCalls.add(endpointInvoked);
			internalCallTimes.add(endpointInvoked);
			endpoint.add(internalCalls);
			timeTaken.add(internalCallTimes);
		}
		internalCalls.add(methodInvoked);
		internalCallTimes.add(processedTime);
		leapConfigurationServer.addComponentToPerformanceCache(componentPerformanceFeatureCache);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * Utility to aggregate multiple timings within component to DataGrid.
	 * 
	 * @param requestUUID
	 * @param endpoint
	 * @param timeTaken
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	public static void aggregateWithinComponents(String requestUUID, String endpointInvoked, String[] methodInvoked,
			long[] processedTime) {
		String methodName = "aggregateWithinComponents";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		ComponentPerformance componentPerformanceFeatureCache = leapConfigurationServer
				.getComponentPerformanceFeatureCache(requestUUID);
		if(componentPerformanceFeatureCache!=null){
		List<Object> endpoint = componentPerformanceFeatureCache.getEndpoint();
		List<Object> timeTaken = componentPerformanceFeatureCache.getTimeTaken();
		Object endpointObj = endpoint.get(endpoint.size() - 1);
		Object timeTakenObj = timeTaken.get(timeTaken.size() - 1);
		List<String> internalCalls = null;
		List<String> internalCallTimes = null;
		boolean toCreateNewList = false;
		if (endpointObj instanceof List) {
			internalCalls = (List<String>) endpointObj;
			internalCallTimes = (List<String>) timeTakenObj;
			if (!internalCalls.contains(endpointInvoked) && !internalCallTimes.contains(endpointInvoked))
				toCreateNewList = true;
		} else
			toCreateNewList = true;

		if (toCreateNewList) {
			internalCalls = new ArrayList<>();
			internalCallTimes = new ArrayList<>();
			// just adding before to identify
			internalCalls.add(endpointInvoked);
			internalCallTimes.add(endpointInvoked);
			endpoint.add(internalCalls);
			timeTaken.add(internalCallTimes);
		}
		for (int i = 0; i < methodInvoked.length; i++)
			internalCalls.add(methodInvoked[i]);
		for (int i = 0; i < processedTime.length; i++)
			internalCallTimes.add(processedTime[i] + "ms");
		leapConfigurationServer.addComponentToPerformanceCache(componentPerformanceFeatureCache);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}
}
