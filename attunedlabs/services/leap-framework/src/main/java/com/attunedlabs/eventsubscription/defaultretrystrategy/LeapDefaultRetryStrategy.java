package com.attunedlabs.eventsubscription.defaultretrystrategy;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;

import com.attunedlabs.eventsubscription.abstractretrystrategy.AbstractSubscriptionRetryStrategy;
import com.attunedlabs.eventsubscription.exception.ConfigurationValidationFailedException;
import com.attunedlabs.eventsubscription.retrypolicy.SubscriptionRetryPolicy;
import com.attunedlabs.eventsubscription.retrypolicy.service.EventSubscriptionRetryPolicyService;
import com.attunedlabs.eventsubscription.util.EventSubscriptionTrackerConstants;
import com.attunedlabs.eventsubscription.util.SubscriptionConstant;
import com.attunedlabs.eventsubscription.util.SubscriptionUtil;
import com.attunedlabs.eventsubscriptiontracker.EventSubscriptionTracker;

/**
 * Default implementation provided for {@link AbstractSubscriptionRetryStrategy}
 * class. This class will store the record in EventSubscriptionTracker table and
 * also take care of the status at each stage.Retry attempt will be done on the
 * subscriber configuration and the retryPolicy.
 * 
 * @see : {@link SubscriptionRetryPolicy}
 * @author Reactiveworks42
 *
 */
@RetryPolicyConfiguration(defaultMaximumRetryInterval = 999)
public class LeapDefaultRetryStrategy extends AbstractSubscriptionRetryStrategy {

	public LeapDefaultRetryStrategy(String strategyConfig) throws ConfigurationValidationFailedException {
		super(strategyConfig);
		initializeRetryConfig(getRetryConfiguration());
	}

	@Override
	public boolean validatePassedStrategyConfiguration() {
		return true;
	}

	@Override
	public List<String> getAllStrategyConfigurationKeys() {
		return Arrays.asList(new String[] { SubscriptionRetryPolicy.RETRY_COUNT_KEY,
				SubscriptionRetryPolicy.RETRY_INTERVAL_KEY, SubscriptionRetryPolicy.RETRY_INTERVAL_MULTIPLIER_KEY,
				SubscriptionRetryPolicy.TIME_INTERVAL_UNIT_KEY, SubscriptionRetryPolicy.MAXIMUM_RETRY_INTERVAL_KEY,
				SubscriptionRetryPolicy.TOP_MAX_RETRY_RECORDS_COUNT_KEY });
	}

	@Override
	public void preProcessing(Exchange exchange, Map<String, Object> metaData) {
		String methodName = "preProcessing";
		log.debug("{} entered into the method {} of LeapDefaultRetryStrategy", LEAP_LOG_KEY, methodName);
		EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) metaData
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		log.debug("{} eventSubscriptionTracker is : {}", LEAP_LOG_KEY, eventSubscriptionTracker);

		// just to identify the retry
		Boolean isRetryTriggered = (Boolean) metaData.get(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY);

		if (eventSubscriptionTracker.getStatus() == null || eventSubscriptionTracker.getStatus().trim().isEmpty()
				|| !isRetryTriggered) {
			if (eventSubscriptionLogService.recordIsNotAlreadyPresent(exchange, metaData)) {
				eventSubscriptionLogService.addNewSubscriptionRecord(exchange, metaData);
//				eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
//						EventSubscriptionTrackerConstants.STATUS_IN_PROCESS, null, this.getRetryConfiguration());
			}
		} else
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_RETRY_IN_PROCESS, null, this.getRetryConfiguration());
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public void onSuccess(Exchange exchange, Map<String, Object> metaData) {
		String methodName = "onSuccess";
		log.debug("{} entered into the method {} of LeapDefaultRetryStrategy", LEAP_LOG_KEY, methodName);
		eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
				EventSubscriptionTrackerConstants.STATUS_COMPLETE, null, this.getRetryConfiguration());
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public void onFailure(Exchange exchange, Map<String, Object> metaData, Exception exception) {
		String methodName = "onFailure";
		log.debug("{} entered into the method {} of LeapDefaultRetryStrategy", LEAP_LOG_KEY, methodName);
		EventSubscriptionTracker eventSubscriptionTracker = (EventSubscriptionTracker) metaData
				.get(SubscriptionConstant.EVENT_SUBSCRIPTION_TRACKER_CLASS);

		// just to identify the retry
		Boolean isRetryTriggered = (Boolean) metaData.get(SubscriptionConstant.SUBSCRIPTION_QUARTZ_TRIGGER_KEY);

		if (eventSubscriptionTracker.getStatus() == null || eventSubscriptionTracker.getStatus().trim().isEmpty()
				|| !isRetryTriggered)
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_FAILED, exception, this.getRetryConfiguration());
		else
			eventSubscriptionLogService.updateSubscriptionRecordStatus(exchange, metaData,
					EventSubscriptionTrackerConstants.STATUS_RETRY_FAILED, exception, this.getRetryConfiguration());
		log.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	@Override
	public List<EventSubscriptionTracker> getRetryableRecords(Exchange exchange, String tenantId, String siteId,
			String subscriptionId) throws Exception {

		// check for new subscriptions from long time and retry them.
		List<EventSubscriptionTracker> newLongTimeSubscription = eventSubscriptionLogService
				.getAllSubscriptionRecordsIntitializedForLongTime(exchange, tenantId, siteId, subscriptionId,
						this.getRetryConfiguration());

		// check for in_process subscriptions for long time and retry them.
		List<EventSubscriptionTracker> inProgressSubscription = eventSubscriptionLogService
				.getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(exchange, tenantId, siteId,
						subscriptionId, EventSubscriptionTrackerConstants.STATUS_IN_PROCESS,
						this.getRetryConfiguration());

		// check for retry_inprocess subscriptions for long time and retry them.
		List<EventSubscriptionTracker> retryInProgressSubscription = eventSubscriptionLogService
				.getAllSubscriptionRecordsInProcessForLongTimeArrangedByRetry(exchange, tenantId, siteId,
						subscriptionId, EventSubscriptionTrackerConstants.STATUS_RETRY_IN_PROCESS,
						this.getRetryConfiguration());

		// check for failed subscriptions and retry them.
		List<EventSubscriptionTracker> failedSubscription = eventSubscriptionLogService
				.getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(exchange, tenantId, siteId,
						subscriptionId, EventSubscriptionTrackerConstants.STATUS_FAILED, this.getRetryConfiguration());

		// check for retry-failed subscriptions and retry them.
		List<EventSubscriptionTracker> retryFailedSubscription = eventSubscriptionLogService
				.getAllFailedSubscriptionRecordsArrangedByFailureTimeAndRetryCount(exchange, tenantId, siteId,
						subscriptionId, EventSubscriptionTrackerConstants.STATUS_RETRY_FAILED,
						this.getRetryConfiguration());

		log.debug("{} new subscription list for long time...{} " ,LEAP_LOG_KEY, newLongTimeSubscription);
		log.debug("{} inProgress subscription list ...{} " ,LEAP_LOG_KEY, inProgressSubscription);
		log.debug("{} retryInProgress subscription list ...{} " ,LEAP_LOG_KEY, retryInProgressSubscription);
		log.debug("{} failed subscription list ... {}" ,LEAP_LOG_KEY, failedSubscription);
		log.debug("{} retryFailed subscription list ... {}" ,LEAP_LOG_KEY, retryFailedSubscription);

		// merging all the list and retrying one by one subscription form the
		// list.
		List<EventSubscriptionTracker> entireFailedList = SubscriptionUtil.mergeAllFailedEventList(
				newLongTimeSubscription, failedSubscription, retryFailedSubscription, inProgressSubscription,
				retryInProgressSubscription);

		return EventSubscriptionRetryPolicyService.filterFailedListWithPoilcy(entireFailedList,
				this.getRetryConfiguration());

	}

	/**
	 * initialize in jsonConfig for specific default strategy.
	 * 
	 * @param retryConfigurationJSON
	 */
	private void initializeRetryConfig(JSONObject retryConfigurationJSON) {
		SubscriptionRetryPolicy.getRetryIntervalMultiplier(retryConfigurationJSON);
		SubscriptionRetryPolicy.getRetryInterval(retryConfigurationJSON);
		SubscriptionRetryPolicy.getMaximumRetryInterval(retryConfigurationJSON);
		SubscriptionRetryPolicy.getRetryCount(retryConfigurationJSON);
		SubscriptionRetryPolicy.getMaxRetryRecordsCount(retryConfigurationJSON);
		SubscriptionRetryPolicy.getTimeIntervalUnit(retryConfigurationJSON);

	}

}
