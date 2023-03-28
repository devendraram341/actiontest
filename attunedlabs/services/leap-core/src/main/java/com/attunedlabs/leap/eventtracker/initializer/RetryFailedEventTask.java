package com.attunedlabs.leap.eventtracker.initializer;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.ConfigurationConstant;
import com.attunedlabs.config.RequestContext;
import com.attunedlabs.core.datagrid.DataGridService;
import com.attunedlabs.eventframework.config.EventFrameworkConstants;
import com.attunedlabs.eventframework.config.IEventFrameworkConfigService;
import com.attunedlabs.eventframework.config.impl.EventFrameworkConfigService;
import com.attunedlabs.eventframework.event.ILeapEventService;
import com.attunedlabs.eventframework.event.LeapEvent;
import com.attunedlabs.eventframework.event.LeapEventService;
import com.attunedlabs.eventframework.eventtracker.EventDispatcherTracker;
import com.attunedlabs.eventframework.eventtracker.IEventDispatcherTrackerService;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerException;
import com.attunedlabs.eventframework.eventtracker.impl.EventDispatcherTrackerImpl;
import com.attunedlabs.eventframework.eventtracker.impl.EventTrackerTableConstants;
import com.attunedlabs.eventframework.retrypolicy.service.EventFrameworkRetryPolicyService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * <code>RetryFailedEventTask</code> will retry the events that are marked as
 * FAILED or RETRY-FAILED or events that are IN_PROCESS for long time(assuming
 * some internal issue while dispatching).
 * 
 * @author Reactiveworks42
 *
 */
public class RetryFailedEventTask {

	final Logger logger = LoggerFactory.getLogger(RetryFailedEventTask.class);
	IEventFrameworkConfigService evtConfigService = new EventFrameworkConfigService();
	IEventDispatcherTrackerService eventDispatcherTrackerService = new EventDispatcherTrackerImpl();

	public void performTask(Exchange exchange) throws EventDispatcherTrackerException {
		String methodName = "performTask";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		logger.debug("{} RETRY_Event Job started at time {}", LEAP_LOG_KEY, new Date(System.currentTimeMillis()));

		// check for new events from long time and re-publish them.
		List<EventDispatcherTracker> newLongTimeEvents = eventDispatcherTrackerService
				.getAllTrackRecordsIntitializedForLongTime(exchange);

		// check for in_process events for long time and re-publish them.
		List<EventDispatcherTracker> inProgressEvents = eventDispatcherTrackerService
				.getAllTrackRecordsInProcessForLongTimeArrangedByRetry(exchange,
						EventTrackerTableConstants.STATUS_IN_PROCESS);

		// check for retry_inprocess events for long time and re-publish them.
		List<EventDispatcherTracker> retryInProgressEvents = eventDispatcherTrackerService
				.getAllTrackRecordsInProcessForLongTimeArrangedByRetry(exchange,
						EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS);

		// check for failed events and re-publish them.
		List<EventDispatcherTracker> failedEvents = eventDispatcherTrackerService
				.getAllFailedEventRecordsArrangedByFailureTimeAndRetryCount(exchange);

		// check for retry-failed events and re-publish them.
		List<EventDispatcherTracker> retryFailedEvents = eventDispatcherTrackerService
				.getAllRetryFailedEventRecordsArrangedByFailureTimeAndRetryCount(exchange);

		logger.trace("{} new event list for long time... {} ", LEAP_LOG_KEY, newLongTimeEvents);
		logger.trace("{} inProgress event list ... {}", LEAP_LOG_KEY, inProgressEvents);
		logger.trace("{} retryInProgress event list ...{} ", LEAP_LOG_KEY, retryInProgressEvents);
		logger.trace("{} failed event list ... {}", LEAP_LOG_KEY, failedEvents);
		logger.trace("{} retryFailed event list ... {}", LEAP_LOG_KEY, retryFailedEvents);

		// merging all the list and retrying one by one events form the list and
		// priority is list is set.
		List<EventDispatcherTracker> entireFailedList = mergeAllFailedEventList(newLongTimeEvents, failedEvents,
				retryFailedEvents, inProgressEvents, retryInProgressEvents);
		logger.trace("{} entireFailedList...{}", LEAP_LOG_KEY, entireFailedList);

		// filtering the event list according to retry-policy.
		List<EventDispatcherTracker> policyFilteredEventFailedList = EventFrameworkRetryPolicyService
				.filterFailedListWithPoilcy(entireFailedList);

		logger.trace("{} policyFilteredEventFailedList...{}", LEAP_LOG_KEY, policyFilteredEventFailedList.size());
		List<EventDispatcherTracker> eventDispatchTrackerList = removeExistingEventDispatchTracker(
				policyFilteredEventFailedList);
		logger.trace("{} eventDispatchTrackerList...{}", LEAP_LOG_KEY, eventDispatchTrackerList.size());

		retryFailedEventsOneByOne(eventDispatchTrackerList, exchange);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private List<EventDispatcherTracker> removeExistingEventDispatchTracker(List<EventDispatcherTracker> filteredList) {
		List<EventDispatcherTracker> trackersList = new ArrayList<>();
		String methodName = "removeExistingEventDispatchTracker";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			HazelcastInstance hazelcastInstance = DataGridService.getDataGridInstance().getHazelcastInstance();
			IMap<String, List<EventDispatcherTracker>> cachedTrackersMap = hazelcastInstance
					.getMap(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS);
			List<EventDispatcherTracker> cachedList = cachedTrackersMap
					.get(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS);

			if (cachedList == null)
				cachedList = new ArrayList<>();

			if (cachedList.isEmpty()) {
				for (EventDispatcherTracker filterTask : filteredList) {
					trackersList.add(filterTask);
					cachedList.add(filterTask);
				}
			} else {
				for (EventDispatcherTracker filterTask : filteredList) {
					if (!cachedList.contains(filterTask)) {
						trackersList.add(filterTask);
						cachedList.add(filterTask);
					}
				}
			}
			logger.trace("{} List Of events in the retry side :{}:{}:{} ", LEAP_LOG_KEY, filteredList.size(),
					cachedList.size(), trackersList.size());
			cachedTrackersMap.put(EventTrackerTableConstants.EVENT_DISPATCH_TRACKERS, cachedList);
		} catch (Exception e) {
			logger.error("{} Error Occured while removing event from hazelcast due to :{} ", LEAP_LOG_KEY,
					e.getMessage());
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return trackersList;
	}

	private void retryFailedEventsOneByOne(final List<EventDispatcherTracker> failedEvents, final Exchange exchange)
			throws EventDispatcherTrackerException {
		if (!failedEvents.isEmpty()) {
			logger.trace(
					"{} republish is done after updating status as RETRY_INPROCESS for particular failed event list ... ",
					LEAP_LOG_KEY, failedEvents);
			for (EventDispatcherTracker eventDispatcherTracker : failedEvents) {
				String tenantId = eventDispatcherTracker.getTenantId();
				String siteId = eventDispatcherTracker.getSiteId();
				String requestId = eventDispatcherTracker.getRequestId();
				String eventStoreKey = eventDispatcherTracker.getEventStoreId();
				String failureMsg = eventDispatcherTracker.getFailureReason();
				Integer retryCount = eventDispatcherTracker.getRetryCount();
				String leapId = eventDispatcherTracker.getLeapEventId();
				String leapEventStr = eventDispatcherTracker.getLeapEvent();
				String dipatchChannelId = eventDispatcherTracker.getDispatchChannelId();
				boolean isFailure = failureMsg != null;
				logger.info("{} retry attempt for events on requestId: {} is {}, leapId: {} dipatchChannelId: {}",
						LEAP_LOG_KEY, requestId, retryCount, leapId, dipatchChannelId);

				// republish will be done for events with
				// NEW,IN_PROCESS,RETRY_INPROCESS(based on retry count) for
				// long time and retryAble exceptions.
				if (checkIsRetryable(failureMsg, isFailure)
						|| eventDispatcherTracker.getStatus().equalsIgnoreCase(EventTrackerTableConstants.STATUS_NEW)
						|| eventDispatcherTracker.getStatus()
								.equalsIgnoreCase(EventTrackerTableConstants.STATUS_IN_PROCESS)
						|| eventDispatcherTracker.getStatus()
								.equalsIgnoreCase(EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS)) {
					LeapEvent leapEvent = generateLeapEventFromEventString(leapEventStr);
					leapEvent.setDispatchChannelId(dipatchChannelId);
					logger.trace("{} leapId :{}, leapEvent.getId() :{}", LEAP_LOG_KEY, leapId, leapEvent.getId());
					rePublishComponentEvent(leapEvent, tenantId, siteId, requestId, eventStoreKey,
							eventDispatcherTracker);
				}
			}
		}

	}

	/**
	 * utility to check whether the eventlist is retryable or not.
	 * 
	 * @param failureMsg
	 * @param isFailure
	 * @return isRetryable
	 */
	private boolean checkIsRetryable(String failureMsg, boolean isFailure) {
		String methodName = "checkIsRetryable";
		logger.debug("entered into the method {}, failureMsg:{}", methodName, failureMsg);
		if (isFailure) {
			try {
				JSONObject failureJSON = new JSONObject(failureMsg);
				boolean parseBoolean = Boolean.parseBoolean(
						failureJSON.get(EventTrackerTableConstants.IS_RETRYABLE).toString().toLowerCase().trim());
				logger.debug(" isretriable {}", parseBoolean);
				return parseBoolean;
			} catch (Exception e) {
				logger.warn("Due to unable to check retry status retry will be not performed...{}", e.getMessage());
				return false;
			}
		} else {
			logger.debug("is Failure " + isFailure);
			return isFailure;
		}
	}

	/**
	 * combining all failed event list and adding failed list in order for
	 * execution.
	 * 
	 * @param newLongTimeEvents
	 * @param failedEvents
	 * @param retryFailedEvents
	 * @param inProgressEvents
	 * @param retryInProgressEvents
	 * @return
	 */
	private List<EventDispatcherTracker> mergeAllFailedEventList(List<EventDispatcherTracker> newLongTimeEvents,
			List<EventDispatcherTracker> failedEvents, List<EventDispatcherTracker> retryFailedEvents,
			List<EventDispatcherTracker> inProgressEvents, List<EventDispatcherTracker> retryInProgressEvents) {
		List<EventDispatcherTracker> finalRetryEventList = new ArrayList<EventDispatcherTracker>();

		// adding failed events in the linked-set will define the priority of
		// execution.
		Set<List<EventDispatcherTracker>> failedLists = new LinkedHashSet<>();
		failedLists.add(newLongTimeEvents);
		failedLists.add(inProgressEvents);
		failedLists.add(failedEvents);
		failedLists.add(retryFailedEvents);
		failedLists.add(retryInProgressEvents);

		for (List<EventDispatcherTracker> eventList : failedLists) {
			if (eventList != null)
				finalRetryEventList.addAll(eventList);
		}
		return finalRetryEventList;
	}

	/**
	 * This method is used to publish events present in the list.(i.e, service and
	 * component events.)
	 * 
	 * @param hcEventList : list of component and service event in hazelcast
	 * @throws EventDispatcherTrackerException
	 */
	public void rePublishComponentEvent(LeapEvent leapEvent, final String tenantId, final String siteId,
			final String requestId, final String eventStoreKey, EventDispatcherTracker tracker)
			throws EventDispatcherTrackerException {
		String methodName = "rePublishComponentEvent";
		logger.debug("entered into the method {}", methodName);
		ILeapEventService eventService = new LeapEventService();
		logger.debug("indudival Event : {}", leapEvent);
		try {
			eventDispatcherTrackerService.updateEventStatus(tenantId, siteId, requestId, eventStoreKey,
					EventTrackerTableConstants.STATUS_RETRY_IN_PROCESS, leapEvent.getId(), false, null, false, true,
					leapEvent.getDispatchChannelId());
			eventService.publishEvent(leapEvent, tenantId, siteId, requestId, true, tracker);
		} catch (Exception e) {
			throw new EventDispatcherTrackerException(e.getMessage(), e.getCause());
		}
		logger.debug("exiting from the {}", methodName);
	}

	private LeapEvent generateLeapEventFromEventString(String leapEventStr) throws EventDispatcherTrackerException {
		String methodName = "generateLeapEventFromEventString";
		logger.debug("entered into the method {}", methodName);

		try {
			JSONObject eventJson = new JSONObject(leapEventStr);
			JSONObject eventParamJson = eventJson.getJSONObject(EventFrameworkConstants.OBJECT_KEY);
			JSONObject eventHeaderJson = eventJson.getJSONObject(EventFrameworkConstants.METADATA_KEY);
			String id = eventJson.getString(EventTrackerTableConstants.EVENT_ID);
			LeapEvent event = new LeapEvent();
			event.setId(id);
			event.setMetadata(getEventDetails(eventHeaderJson));
			event.setObject(getEventDetails(eventParamJson));
			logger.debug("exiting from the {}", methodName);
			return event;
		} catch (Exception e) {
			throw new EventDispatcherTrackerException("Failed to convert Json to leapEvent...!" + e.getMessage());
		}

	}

	private Map<String, Serializable> getEventDetails(JSONObject eventObjectJson) throws JSONException {
		Map<String, Serializable> eventParamMap = new HashMap<>();
		Iterator<String> eventParamKeys = eventObjectJson.keys();
		while (eventParamKeys.hasNext()) {
			String paramKey = (String) eventParamKeys.next();
			if (paramKey.equals(ConfigurationConstant.EVENT_CONTEXT_KEY))
				eventParamMap.put(paramKey, getRequestContext(eventObjectJson.getJSONObject(paramKey)));
			else
				eventParamMap.put(paramKey, (Serializable) eventObjectJson.get(paramKey));
		}
		return eventParamMap;
	}

	private RequestContext getRequestContext(JSONObject object) throws JSONException {
		if (object != null) {
			RequestContext context = new RequestContext();
			if (object.has(ConfigurationConstant.TENANT_ID))
				context.setTenantId(object.getString(ConfigurationConstant.TENANT_ID));
			if (object.has(ConfigurationConstant.SITE_ID))
				context.setSiteId(object.getString(ConfigurationConstant.SITE_ID));
			if (object.has(ConfigurationConstant.FEATURE_GROUP))
				context.setFeatureGroup(object.getString(ConfigurationConstant.FEATURE_GROUP));
			if (object.has(ConfigurationConstant.FEATURE_NAME))
				context.setFeatureName(object.getString(ConfigurationConstant.FEATURE_NAME));
			if (object.has(ConfigurationConstant.IMPLEMENTATION_NAME))
				context.setImplementationName(object.getString(ConfigurationConstant.IMPLEMENTATION_NAME));
			if (object.has(ConfigurationConstant.VENDOR))
				context.setVendor(object.getString(ConfigurationConstant.VENDOR));
			if (object.has(ConfigurationConstant.VERSION))
				context.setVersion(object.getString(ConfigurationConstant.VERSION));
			if (object.has(ConfigurationConstant.REQUEST_ID))
				context.setRequestId(object.getString(ConfigurationConstant.REQUEST_ID));
			if (object.has(ConfigurationConstant.PROVIDER))
				context.setProvider(object.getString(ConfigurationConstant.PROVIDER));
			return context;
		}
		return null;
	}

}
