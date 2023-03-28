package com.attunedlabs.eventframework.eventtracker.impl;

import java.util.concurrent.TimeUnit;

public class EventTrackerTableConstants {
	// event track table constants
	public static final String EVENT_DISPATCH_TRACKER_TABLE = "EventDispatchTracker";
	public static final String TENANT_ID = "tenantId";
	public static final String SITE_ID = "siteId";
	public static final String REQUEST_ID = "requestId";
	public static final String EVENT_STORE_ID = "eventStoreId";
	public static final String EVENT_CREATED_DTM = "eventCreatedDTM";
	public static final String LAST_FAILURE_DTM = "lastFailureDTM";
	public static final String PROCESSING_STATUS = "status";
	public static final String FAILURE_REASON = "failureReason";
	public static final String RETRY_COUNT = "retryCount";
	public static final String LEAP_EVENT_ID = "leapEventId";
	public static final String PROCESSING_WAY = "processingWay";
	public static final String LEAP_EVENT = "leapEvent";
	public static final String DISPATCH_CHANNEL_ID = "dispatchChannelId";
	// processing status
	public static final String STATUS_NEW = "NEW";
	public static final String STATUS_IN_PROCESS = "IN_PROCESS";
	public static final String STATUS_FAILED = "FAILED";
	public static final String STATUS_RETRY_IN_PROCESS = "RETRY_INPROCESS";
	public static final String STATUS_RETRY_FAILED = "RETRY_FAILED";
	public static final String STATUS_COMPLETE = "COMPLETE";

	// data source bean ref name from beans.xml
	public static final String Leap_DATASOURCE = "leapDataSource";
	public static final String Leap_SIMPLE_DATASOURCE = "leapXASource";

	// specified timeInterval given to cron execute dispatch task which are
	// IN_PROCESS or RETRY_INPROCESS
	// more than MAX_DISPATCH_TIME in order.
	public static final int MAX_DISPATCH_TIME = 10;
	public static final TimeUnit DISPATCH_TIME_UNIT = TimeUnit.MINUTES;

	// failure json constants keys
	public static final String FAILURE_TYPE = "failureType";
	public static final String FAILURE_MESSAGE = "failureMsg";
	public static final String IS_RETRYABLE = "isRetryable";
	public static final String RETRY_STATUS = "retryStatus";

	// retry-policy file name
	public static final String EVENT_RETRY_POLICY_FILE = "globalAppDeploymentConfig.properties";

	public static final String EVENT_DISPATCH_TRACKERS = "EVT-TRS";
	public static final String PROCESSING_WAY_ASYNC = "ASYNC";
	public static final String PROCESSING_WAY_SYNC = "SYNC";

	// leapEvent json keys
	public static final String EVENT_ID = "EventId";
//	public static final String EVENT_HEADER = "EventHeader";
//	public static final String EVENT_PARAM = "EventParam";

}
